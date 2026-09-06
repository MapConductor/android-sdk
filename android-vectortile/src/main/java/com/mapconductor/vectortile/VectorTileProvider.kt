package com.mapconductor.vectortile

import com.mapconductor.core.tileserver.TileProviderInterface
import com.mapconductor.core.tileserver.TileRequest
import org.json.JSONArray
import java.io.Closeable
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Semaphore
import android.util.Log
import android.util.LruCache

/**
 * Renders a MapLibre vector style to raster tiles, for map backends that cannot
 * display a vector style themselves.
 *
 * Register it with `TileServerRegistry.get()` and point a
 * [com.mapconductor.core.raster.RasterLayerSource.UrlTemplate] at the resulting
 * route — which is what [VectorTileLayer] does for you.
 *
 * Network I/O happens here rather than in the native library, so the app's own
 * HTTP behaviour keeps applying: [headers] covers auth tokens, and swapping
 * [fetchTile] hands over completely.
 */
class VectorTileProvider private constructor(
    private val renderer: VectorTileRenderer,
    private val tileSize: Int,
    private val headers: Map<String, String>,
    private val fetchTile: (String) -> ByteArray?,
    cacheBytes: Int,
    private val diskCache: TileDiskCache?,
    private val glyphCache: StyleAssetCache?,
    private val spriteCache: StyleAssetCache?,
    styleJson: String,
    /** Null when rendering on the CPU. */
    private val gpu: GpuTileRasterizer?,
) : TileProviderInterface,
    Closeable {
    /** Where rasterisation actually happens, after any fallback. */
    val renderMode: RenderMode = if (gpu != null) RenderMode.GPU else RenderMode.CPU

    /**
     * Called when glyphs have arrived and the tiles drawn before them are
     * missing labels. The layer refetches; nothing here can make the map do
     * that on its own.
     */
    @Volatile
    var onGlyphsLoaded: (() -> Unit)? = null

    private val gpuRenderCount =
        java.util.concurrent.atomic
            .AtomicLong()
    private val gpuFallbackCount =
        java.util.concurrent.atomic
            .AtomicLong()

    /**
     * Tiles actually drawn by the GPU, and tiles that fell back to the CPU.
     *
     * Worth exposing rather than keeping internal: [renderMode] only says a GL
     * context was obtained, and a path that quietly falls back on every tile
     * looks exactly like a working one from the outside.
     */
    val gpuRenders: Long get() = gpuRenderCount.get()
    val gpuFallbacks: Long get() = gpuFallbackCount.get()

    /**
     * Identifies the style for disk cache keys. Content-addressed, so a
     * restyle misses rather than needing explicit invalidation.
     */
    @Volatile
    private var styleKey: String = TileDiskCache.digest(styleJson)

    /** Glyph URLs already claimed, so concurrent tiles fetch each once. */
    private val requestedGlyphs = java.util.Collections.synchronizedSet(HashSet<String>())

    /** Bumped when glyphs arrive, so tiles drawn before them stop being served. */
    private val glyphGeneration =
        java.util.concurrent.atomic
            .AtomicInteger()

    /** Set while a notification is already scheduled, so a burst sends one. */
    private val glyphNotifyPending =
        java.util.concurrent.atomic
            .AtomicBoolean(false)

    /**
     * How long to let a burst of ranges settle before telling the layer.
     *
     * A viewport's worth arrives within a second or two of each other, and
     * notifying per range would redraw every visible tile dozens of times to
     * reach the same picture.
     */
    private val glyphNotifyQuietMs = 400L

    /**
     * How long to keep waiting while ranges are still in flight.
     *
     * The quiet window alone was not enough. Ranges do not arrive in one
     * burst: six threads each take 0.4 to 1.3 seconds per range and a low
     * zoom wants dozens, so arrivals are spread over many seconds and the
     * 400ms window closed between them again and again. Every close was a
     * handover, and a handover redraws every visible tile — that steady
     * two-per-second replacement was the flicker. So the window is held open
     * while any fetch is outstanding, and the whole viewport's labels appear
     * in one go. The cap is there because a range that never answers must not
     * hold the labels back forever.
     */
    private val glyphNotifyMaxWaitMs = 10_000L

    /**
     * Whether any tile has been drawn short of glyphs since the last handover.
     *
     * Panning turns up new labels, which fetch new ranges, which used to hand
     * the whole viewport over again -- the generation reached 81 in half a
     * minute of use, and each handover refetched and redrew everything on
     * screen at roughly 0.6 to 0.9 seconds a tile. Most of those handovers
     * changed nothing: the ranges had arrived before anything needed them.
     */
    private val provisionalSinceHandover =
        java.util.concurrent.atomic
            .AtomicBoolean(false)

    /**
     * Fetches source tiles, several at a time.
     *
     * Sized for one tile's plan: the tile itself and the eight neighbours it
     * needs for labels that cross an edge.
     */
    private val sourceFetchers =
        java.util.concurrent.Executors.newFixedThreadPool(9) { runnable ->
            Thread(runnable, "mc-tiles").apply { isDaemon = true }
        }

    /** Glyph fetches queued or running, so the notify can wait for quiet. */
    private val glyphsInFlight =
        java.util.concurrent.atomic
            .AtomicInteger()

    /**
     * Fetches glyph ranges off the tile threads.
     *
     * One range is a round trip of 0.4 to 1.3 seconds and a tile at low zoom
     * wants dozens, so nothing waits on them: six at a time, and the tiles
     * that were drawn without them are handed over when they land.
     */
    private val glyphFetchers =
        java.util.concurrent.Executors.newFixedThreadPool(6) { runnable ->
            Thread(runnable, "mc-glyphs").apply { isDaemon = true }
        }

    /** True when the style names no glyph source, so labels can never draw. */
    private val glyphsUnavailable: Boolean = renderer.glyphsUrlTemplate() == null

    /**
     * Open once the ranges kept from earlier runs are back in the store.
     *
     * A tile drawn before that has no labels on it and has to be handed over
     * later, which is the thing this is here to avoid, so the first render
     * waits -- but only for reads of a few small local files, and only up to
     * [GLYPH_WARM_WAIT_MS] so a slow disk cannot hold the map up.
     */
    private val glyphsWarmed = java.util.concurrent.CountDownLatch(1)

    init {
        val cache = glyphCache
        if (cache == null && !renderer.needsSprite()) {
            glyphsWarmed.countDown()
        } else {
            Thread({
                val started = System.nanoTime()
                val loaded =
                    if (cache == null || glyphsUnavailable) {
                        0
                    } else {
                        runCatching { cache.warm { renderer.addGlyphs(it) } }.getOrDefault(0)
                    }
                // The sprite is looked for here too, because unlike glyphs its
                // URLs come from the style: a sheet that is already on disk can
                // be in place before the first tile rather than after it.
                val icons = runCatching { loadSpriteFromDisk() }.getOrDefault(0)
                glyphsWarmed.countDown()
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    val ms = (System.nanoTime() - started) / 1_000_000
                    Log.d(TAG, "loaded $loaded glyph ranges and $icons icons from disk in ${ms}ms")
                }
                // Only then the network, so a cold start does not hold the
                // first tile behind two more round trips.
                fetchSpriteIfMissing()
            }, "mc-style-assets").apply { isDaemon = true }.start()
        }
    }

    /** The style's sprite pair, or null when it names none. */
    private fun spriteUrls(): Pair<String, String>? {
        val urls = runCatching { renderer.spriteUrls(1) }.getOrDefault(emptyList())
        return if (urls.size == 2) urls[0] to urls[1] else null
    }

    /** Returns how many icons were loaded, 0 when the pair is not on disk. */
    private fun loadSpriteFromDisk(): Int {
        if (closed || !renderer.needsSprite()) return 0
        val (indexUrl, imageUrl) = spriteUrls() ?: return 0
        val index = spriteCache?.get(indexUrl) ?: return 0
        val image = spriteCache.get(imageUrl) ?: return 0
        return runCatching { renderer.addSprite(index.decodeToString(), image) }
            .onFailure { Log.w(TAG, "cached sprite would not parse; refetching", it) }
            .getOrDefault(0)
    }

    /**
     * Fetches the sheet once, and hands the drawn tiles over when it lands.
     *
     * The same machinery as a glyph range, for the same reason: tiles drawn
     * before the sheet arrived have holes where their icons belong.
     */
    private fun fetchSpriteIfMissing() {
        if (closed || !renderer.needsSprite()) return
        val (indexUrl, imageUrl) = spriteUrls() ?: return
        val index = runCatching { fetchTile(indexUrl) }.getOrNull()
        val image = runCatching { fetchTile(imageUrl) }.getOrNull()
        if (index == null || image == null || index.isEmpty() || image.isEmpty()) {
            Log.w(TAG, "sprite unavailable: $indexUrl")
            return
        }
        val icons =
            runCatching { renderer.addSprite(index.decodeToString(), image) }
                .onFailure { Log.w(TAG, "sprite would not parse: $indexUrl", it) }
                .getOrDefault(0)
        if (icons <= 0) return
        spriteCache?.put(indexUrl, index)
        spriteCache?.put(imageUrl, image)
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "sprite sheet in: $icons icons")
        }
        onGlyphsArrived()
    }

    /**
     * Source tiles keyed by URL.
     *
     * Not an optimisation detail: neighbouring target tiles routinely need the
     * same source tile — always, once overzoom kicks in, where one magnified
     * ancestor serves 16 targets — and a map asks for a whole viewport at once.
     */
    private val cache =
        object : LruCache<String, ByteArray>(cacheBytes) {
            // Sized in bytes, not entries. Counting entries is the default and
            // it is wrong here: a basemap tile is 150-300 KB, so a few hundred
            // entries is tens of megabytes, and the native allocator aborts the
            // host process when it runs out — `catch_unwind` cannot save it.
            override fun sizeOf(
                key: String,
                value: ByteArray,
            ): Int = value.size
        }

    /**
     * Caps concurrent rasterisation.
     *
     * The tile server will happily run eight requests at once, but each render
     * holds a decoded tile and a full pixmap, and on a mid-range device eight
     * at a time both exhausts memory and thrashes the CPU — measured render
     * times climbed from 235 ms to 1790 ms purely from contention.
     */
    private val renderSlots =
        Semaphore(
            maxOf(1, Runtime.getRuntime().availableProcessors() / 2),
            true,
        )

    /** URLs known to hold nothing, so a missing tile is not re-requested. */
    private val empties = java.util.Collections.synchronizedSet(HashSet<String>())

    @Volatile
    private var closed = false

    /** Which rasteriser to use. */
    enum class RenderMode {
        /** `tiny-skia`, on the calling thread. Always available. */
        CPU,

        /**
         * OpenGL ES. Roughly 3x faster on a mid-range device and, more to the
         * point, moves the drawing off the CPU that the map SDK and the app
         * are already competing for.
         *
         * Output is not bit-identical to [CPU]: anti-aliasing comes from MSAA
         * rather than analytic coverage, which measured at 0.39% of pixels
         * differing on a dense street tile, all of it on thin-line edges.
         */
        GPU,

        /** [GPU] where a context can be created, otherwise [CPU]. */
        AUTO,
    }

    companion object {
        private const val TAG = "VectorTileProvider"
        const val DEFAULT_TILE_SIZE: Int = 512

        /**
         * Source tile cache budget, in bytes.
         *
         * Raised when tiles started fetching their neighbours: the working set
         * for a screen went from the tiles on it to those plus the ring around
         * them, and at low zoom a tile is most of a megabyte. Too small a
         * cache here does not lose the picture, it re-fetches it.
         */
        const val DEFAULT_CACHE_BYTES: Int = 48 * 1024 * 1024

        /** Rendered tile disk cache budget, in bytes. */
        const val DEFAULT_DISK_CACHE_BYTES: Long = 64L * 1024 * 1024

        /**
         * Glyph range disk cache budget, in bytes.
         *
         * Measured rather than guessed: one screen of Tokyo wants 69 ranges
         * and they average 150KB each, so a single view is already 10MB. At
         * 8MB the sweep threw away exactly what the next launch was about to
         * ask for -- 43 of the 69 survived and the map still spent 3.4
         * seconds unlabelled. This holds roughly 200 ranges, which is a
         * couple of scripts' worth of roaming.
         */
        const val DEFAULT_GLYPH_CACHE_BYTES: Long = 32L * 1024 * 1024

        /**
         * Sprite sheet disk cache budget, in bytes.
         *
         * A style has one sheet, and a large one is a few hundred kilobytes;
         * the room is for the handful of styles an app switches between.
         */
        const val DEFAULT_SPRITE_CACHE_BYTES: Long = 4L * 1024 * 1024

        /**
         * How long a tile waits for the stored ranges to be read back.
         *
         * Reading them is local and takes milliseconds; the limit is only so
         * that a wedged filesystem costs a mute first tile rather than a map
         * that never draws.
         */
        private const val GLYPH_WARM_WAIT_MS: Long = 1_500

        /**
         * @param styleJson a MapLibre style document
         * @param headers sent with every source tile request
         * @param fetchTile overrides fetching entirely; return null for "no tile"
         * @throws IllegalArgumentException if the style cannot be parsed
         */
        @JvmStatic
        @JvmOverloads
        fun create(
            styleJson: String,
            tileSize: Int = DEFAULT_TILE_SIZE,
            headers: Map<String, String> = emptyMap(),
            cacheBytes: Int = DEFAULT_CACHE_BYTES,
            diskCacheDir: File? = null,
            diskCacheBytes: Long = DEFAULT_DISK_CACHE_BYTES,
            renderMode: RenderMode = RenderMode.AUTO,
            fetchTile: ((String) -> ByteArray?)? = null,
        ): VectorTileProvider {
            val renderer = VectorTileRenderer.create(styleJson)
            val gpu =
                when (renderMode) {
                    RenderMode.CPU -> null
                    // AUTO and GPU both probe; AUTO falls back silently, GPU says
                    // so, because asking for the GPU and quietly getting the CPU
                    // is how a performance regression hides.
                    RenderMode.AUTO -> GpuTileRasterizer.createOrNull(tileSize)
                    RenderMode.GPU ->
                        GpuTileRasterizer.createOrNull(tileSize).also {
                            if (it == null) Log.w(TAG, "GPU requested but unavailable; using the CPU")
                        }
                }
            return VectorTileProvider(
                renderer = renderer,
                tileSize = tileSize,
                headers = headers,
                fetchTile = fetchTile ?: { url -> httpGet(url, headers) },
                cacheBytes = cacheBytes,
                diskCache = diskCacheDir?.let { TileDiskCache(it, diskCacheBytes) },
                glyphCache =
                    diskCacheDir?.let {
                        StyleAssetCache(File(it, "glyphs"), DEFAULT_GLYPH_CACHE_BYTES)
                    },
                spriteCache =
                    diskCacheDir?.let {
                        StyleAssetCache(File(it, "sprites"), DEFAULT_SPRITE_CACHE_BYTES)
                    },
                styleJson = styleJson,
                gpu = gpu,
            )
        }

        private fun httpGet(
            url: String,
            headers: Map<String, String>,
        ): ByteArray? {
            val connection = URL(url).openConnection() as HttpURLConnection
            var reuse = false
            return try {
                connection.requestMethod = "GET"
                connection.connectTimeout = 15_000
                connection.readTimeout = 15_000
                // Tiles are gzipped on the wire by most servers; the client
                // undoes that transparently when the header is left to it.
                headers.forEach(connection::setRequestProperty)
                when (val status = connection.responseCode) {
                    // A missing tile is normal at the edge of a source's coverage.
                    HttpURLConnection.HTTP_NOT_FOUND, HttpURLConnection.HTTP_NO_CONTENT -> {
                        connection.errorStream?.use { it.readBytes() }
                        reuse = true
                        null
                    }
                    in 200..299 -> {
                        val bytes = connection.inputStream.use { it.readBytes() }
                        // Draining the body to completion is what lets the
                        // socket go back to the pool.
                        reuse = true
                        bytes.takeIf { it.isNotEmpty() }
                    }
                    else -> {
                        connection.errorStream?.use { it.readBytes() }
                        reuse = true
                        throw java.io.IOException("tile fetch failed: $status $url")
                    }
                }
            } finally {
                // `disconnect()` tears the socket down instead of returning it
                // to the keep-alive pool. With a viewport's worth of tiles
                // going to one host, that means a fresh TLS handshake per
                // tile — so it is only used when the connection is already
                // unusable.
                if (!reuse) connection.disconnect()
            }
        }
    }

    /** Reasons the current style may not render as intended. */
    fun diagnostics(): List<String> {
        val array = JSONArray(renderer.diagnostics())
        return (0 until array.length()).map { array.getString(it) }
    }

    /** Describes the GL context in use, or "cpu" when rasterising on the CPU. */
    fun rendererDescription(): String = gpu?.describe() ?: "cpu"

    /**
     * Replaces the style. Fetched vector tiles stay valid — the geometry is
     * unchanged, only the paint applied to it — so recolouring costs a
     * re-rasterise and no network traffic.
     *
     * The caller still has to make the map drop its *raster* tiles; pass a new
     * `cacheKey` to `LocalTileServer.urlTemplate`.
     */
    fun setStyle(styleJson: String) {
        renderer.setStyle(styleJson)
        // Rendered tiles are keyed by style, so the old ones simply stop being
        // found; there is nothing to invalidate.
        styleKey = TileDiskCache.digest(styleJson)
    }

    override fun renderTile(request: TileRequest): ByteArray? = renderTile(request) { false }

    override fun renderTile(
        request: TileRequest,
        isCancelled: () -> Boolean,
    ): ByteArray? {
        if (closed) return null

        // Two keys, because a rendered tile goes stale for one reason only:
        // it was drawn while some of its glyphs were still on the way. A tile
        // that had them all is finished, and no later arrival can change it,
        // so it is stored under a key with no generation in it and survives
        // every handover. Only the ones that were drawn short are tied to the
        // generation they were drawn at, and only they are redrawn.
        fun key(generation: String) =
            TileDiskCache.digest(
                styleKey,
                "$tileSize",
                // The renderer's own generation: a build that draws more
                // than the one that filled this cache must not serve its
                // tiles.
                "v${VectorTileRenderer.OUTPUT_VERSION}",
                generation,
                "${request.z}/${request.x}/${request.y}",
            )

        val completeKey = diskCache?.let { key("complete") }
        val provisionalKey = diskCache?.let { key("g${glyphGeneration.get()}") }
        for (candidate in listOfNotNull(completeKey, provisionalKey)) {
            val hit = diskCache?.get(candidate)
            if (hit != null) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(
                        TAG,
                        "tile ${request.z}/${request.x}/${request.y} disk-hit " +
                            "bytes=${hit.size}",
                    )
                }
                return hit
            }
        }

        val plan = JSONArray(renderer.plan(request.z, request.x, request.y))
        val tiles = ArrayList<ByteArray?>(plan.length())

        // Fetch and rasterise are timed separately: when a tile is slow, the
        // answer is almost always one or the other, and guessing wastes time.
        val fetchStarted = System.nanoTime()
        // In parallel, because a plan is no longer one tile: the renderer also
        // asks for the ring of neighbours so a label at the edge can be drawn
        // whole, and nine round trips one after another is a second of waiting
        // for what takes a fraction of it at once. Most are cache hits in
        // steady use -- every tile is its neighbours' neighbour.
        val urls =
            (0 until plan.length()).map { plan.getJSONObject(it).getString("url") }
        val pending =
            urls.map { url ->
                sourceFetchers.submit<ByteArray?> { if (closed) null else sourceTile(url) }
            }
        for (future in pending) {
            if (isCancelled()) {
                pending.forEach { it.cancel(false) }
                return null
            }
            tiles.add(runCatching { future.get() }.getOrNull())
        }
        val fetchMs = (System.nanoTime() - fetchStarted) / 1_000_000
        if (closed) return null

        // Cached ranges belong in the store before the tile is judged to be
        // missing them; otherwise the first tile of a launch is drawn bare and
        // handed over a moment later for no reason.
        if (glyphsWarmed.count > 0L) {
            runCatching {
                glyphsWarmed.await(GLYPH_WARM_WAIT_MS, java.util.concurrent.TimeUnit.MILLISECONDS)
            }
        }

        // Short of its sheet counts the same as short of its glyphs: the tile
        // is missing ink it will have later.
        val drawnShortOfGlyphs = requestGlyphs(request, tiles) || renderer.needsSprite()

        // The last chance to bail. Past here the work is native and cannot be
        // interrupted, and it is the part that takes hundreds of milliseconds
        // -- a tile drawn for a viewport the map has left holds up the tiles
        // it is waiting for, because the render slots and the GL thread are
        // shared.
        if (isCancelled()) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "tile ${request.z}/${request.x}/${request.y} dropped before rendering")
            }
            return null
        }

        val renderStarted = System.nanoTime()
        // A patterned fill needs an image repeated across the polygon, which
        // the GPU path cannot draw, so those tiles take the slower road.
        val onGpu =
            gpu != null &&
                runCatching { !renderer.needsCpu(request.z, tiles) }.getOrDefault(true)
        if (gpu != null && !onGpu && Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(
                TAG,
                "tile ${request.z}/${request.x}/${request.y} drawn on the CPU: " +
                    "the style paints a pattern here",
            )
        }
        val png =
            if (onGpu) {
                // The GL thread serialises drawing already, so the CPU-side
                // semaphore would only add queueing on top of it.
                val drawn =
                    try {
                        renderOnGpu(request, tiles)
                    } catch (error: Throwable) {
                        // Logged, not swallowed: a silent catch here is what made a
                        // GPU path that fell back on every single tile look healthy.
                        Log.w(TAG, "GPU render failed; falling back to the CPU", error)
                        null
                    }
                if (drawn != null) gpuRenderCount.incrementAndGet() else gpuFallbackCount.incrementAndGet()
                drawn
                    ?: run {
                        // A GPU failure must not lose the tile; the CPU can always
                        // draw it.
                        renderSlots.acquire()
                        try {
                            // Waiting for a slot is where a tile spends its
                            // time when the map is busy, and the map can lose
                            // interest while it waits.
                            if (isCancelled()) return null
                            runCatching {
                                renderer.render(request.z, request.x, request.y, tileSize, tiles)
                            }.getOrNull()
                        } finally {
                            renderSlots.release()
                        }
                    }
            } else {
                renderSlots.acquire()
                try {
                    if (isCancelled()) return null
                    runCatching {
                        renderer.render(request.z, request.x, request.y, tileSize, tiles)
                    }.getOrNull()
                } finally {
                    renderSlots.release()
                }
            }
        val renderMs = (System.nanoTime() - renderStarted) / 1_000_000

        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(
                TAG,
                "tile ${request.z}/${request.x}/${request.y} " +
                    "sources=${plan.length()} fetch=${fetchMs}ms " +
                    "render=${renderMs}ms bytes=${png?.size ?: 0}",
            )
        }
        if (drawnShortOfGlyphs) provisionalSinceHandover.set(true)
        if (png != null) {
            val store = if (drawnShortOfGlyphs) provisionalKey else completeKey
            if (store != null) diskCache?.put(store, png)
        }
        return png
    }

    /**
     * Starts fetching the glyph ranges this tile's labels need, and returns.
     *
     * Waiting was the obvious thing and the wrong one: a range is a 0.4 to 1.3
     * second round trip, a tile at low zoom wants dozens, and the tile drew
     * nothing until the last one landed — 13 seconds for 69 ranges, which is
     * what "it stops when you zoom out" was. The tile is drawn with whatever
     * glyphs are already in, exactly as MapLibre does it, and the ones that
     * arrive later bring the labels with them through [onGlyphsLoaded].
     *
     * Returns whether this tile is about to be drawn without glyphs it wants,
     * which is what decides between the two cache keys and whether a handover
     * is worth making.
     */
    private fun requestGlyphs(
        request: TileRequest,
        tiles: List<ByteArray?>,
    ): Boolean {
        if (glyphsUnavailable || closed) return false
        val needed =
            runCatching { renderer.neededGlyphs(request.z, request.x, request.y, tiles) }
                .getOrElse { emptyList() }
        if (needed.isEmpty()) return false

        // Only the ranges nobody has claimed yet. Two tiles wanting the same
        // range at once would otherwise both fetch it.
        val mine = needed.filter { requestedGlyphs.add(it) }
        if (mine.isEmpty()) return true

        for (url in mine) {
            glyphsInFlight.incrementAndGet()
            val queued =
                runCatching {
                    glyphFetchers.execute {
                        try {
                            if (closed) return@execute
                            val cached = glyphCache?.get(url)
                            val bytes =
                                cached ?: runCatching { fetchTile(url) }.getOrNull()?.also {
                                    // Ranges do not change, so this is kept
                                    // without an expiry.
                                    glyphCache?.put(url, it)
                                }
                            if (bytes == null || bytes.isEmpty()) {
                                // Left in the claimed set: a range the server
                                // does not have will not appear on a retry.
                                Log.w(TAG, "glyph range unavailable: $url")
                                return@execute
                            }
                            val added =
                                runCatching { renderer.addGlyphs(bytes) }.getOrElse {
                                    Log.w(TAG, "glyph range would not parse: $url", it)
                                    0
                                }
                            if (added > 0) onGlyphsArrived()
                        } finally {
                            glyphsInFlight.decrementAndGet()
                        }
                    }
                }
            if (queued.isFailure) glyphsInFlight.decrementAndGet()
        }
        return true
    }

    /**
     * Tells the layer that tiles drawn before now are missing labels.
     *
     * Coalesced: a viewport's worth of ranges lands in a burst, and asking the
     * map to refetch on each one would redraw everything dozens of times for
     * the same result. The generation is what makes the already-rendered tiles
     * stale — without it the caches would keep serving the unlabelled ones.
     */
    private fun onGlyphsArrived() {
        glyphGeneration.incrementAndGet()
        if (!glyphNotifyPending.compareAndSet(false, true)) return
        // Its own thread: the fetch pool is six wide and a sleeper sitting in
        // it is one fewer range in flight.
        val waiter =
            Thread {
                val deadline = System.nanoTime() + glyphNotifyMaxWaitMs * 1_000_000L
                try {
                    Thread.sleep(glyphNotifyQuietMs)
                    // Hold the window open while ranges are still coming.
                    while (glyphsInFlight.get() > 0 && System.nanoTime() < deadline && !closed) {
                        Thread.sleep(glyphNotifyQuietMs)
                    }
                } catch (_: InterruptedException) {
                    glyphNotifyPending.set(false)
                    return@Thread
                }
                glyphNotifyPending.set(false)
                if (closed) return@Thread
                // Nothing to hand over to: every tile drawn since the last one
                // had all the glyphs it wanted, so the picture on screen is
                // already the finished picture. A handover here would refetch
                // and redraw the viewport to arrive at the same pixels.
                if (!provisionalSinceHandover.compareAndSet(true, false)) {
                    if (Log.isLoggable(TAG, Log.DEBUG)) {
                        Log.d(TAG, "glyphs arrived but no tile was waiting on them")
                    }
                    return@Thread
                }
                // The vector tiles are deliberately not dropped. Glyphs change
                // what a tile is drawn *with*, not what it contains, and
                // throwing them away made every handover refetch the viewport
                // over the network before it could redraw it.
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "glyph generation -> ${glyphGeneration.get()}; tiles hand over")
                }
                runCatching { onGlyphsLoaded?.invoke() }
                    .onFailure { Log.w(TAG, "glyph notification failed", it) }
            }
        waiter.isDaemon = true
        waiter.name = "mc-glyph-notify"
        waiter.start()
    }

    private fun renderOnGpu(
        request: TileRequest,
        tiles: List<ByteArray?>,
    ): ByteArray? {
        val rasterizer = gpu ?: return null
        val lengths = IntArray(tiles.size) { tiles[it]?.size ?: 0 }
        val data = ByteArray(lengths.sum())
        var offset = 0
        for (tile in tiles) {
            if (tile == null) continue
            tile.copyInto(data, offset)
            offset += tile.size
        }
        val packed =
            renderer.tessellate(
                request.z, request.x, request.y, tileSize, data, lengths,
            )
        // The tessellator produces fills and lines; labels come from a
        // distance field per glyph and are drawn over the readback. Without
        // this the GPU path lost every label a style asked for and said
        // nothing about it.
        val decorate: ((ByteArray) -> Unit)? =
            if (glyphsUnavailable) {
                null
            } else {
                { rgba ->
                    runCatching {
                        renderer.drawLabels(request.z, request.x, request.y, tileSize, rgba, tiles)
                    }.onFailure { Log.w(TAG, "label pass failed on the GPU readback", it) }
                }
            }
        return rasterizer.renderPng(TessellatedTile(packed), decorate)
    }

    private fun sourceTile(url: String): ByteArray? {
        cache.get(url)?.let { return it }
        if (empties.contains(url)) return null

        // A source that answers "no tile here" is remembered, because it will
        // answer the same way tomorrow. A source that fails to answer is not:
        // treating a dropped connection as an empty tile leaves a hole in the
        // map for as long as the app runs, and the hole is invisible from
        // here -- the tile simply draws without that data.
        val fetched = runCatching { fetchTile(url) }
        val bytes = fetched.getOrNull()
        if (bytes == null) {
            if (fetched.isSuccess) {
                empties.add(url)
            } else {
                Log.w(TAG, "source tile failed; will try again: $url", fetched.exceptionOrNull())
            }
            return null
        }
        cache.put(url, bytes)
        return bytes
    }

    /** Releases the native renderer. Safe to call more than once. */
    override fun close() {
        glyphFetchers.shutdownNow()
        sourceFetchers.shutdownNow()
        if (closed) return
        closed = true
        cache.evictAll()
        empties.clear()
        gpu?.close()
        renderer.close()
    }
}
