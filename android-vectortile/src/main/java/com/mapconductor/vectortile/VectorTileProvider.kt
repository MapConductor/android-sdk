package com.mapconductor.vectortile

import android.util.Log
import android.util.LruCache
import com.mapconductor.core.tileserver.TileProviderInterface
import com.mapconductor.core.tileserver.TileRequest
import java.io.Closeable
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Semaphore
import org.json.JSONArray

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
    styleJson: String,
    /** Null when rendering on the CPU. */
    private val gpu: GpuTileRasterizer?,
) : TileProviderInterface, Closeable {

    /** Where rasterisation actually happens, after any fallback. */
    val renderMode: RenderMode = if (gpu != null) RenderMode.GPU else RenderMode.CPU

    /**
     * Identifies the style for disk cache keys. Content-addressed, so a
     * restyle misses rather than needing explicit invalidation.
     */
    @Volatile
    private var styleKey: String = TileDiskCache.digest(styleJson)

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
            override fun sizeOf(key: String, value: ByteArray): Int = value.size
        }

    /**
     * Caps concurrent rasterisation.
     *
     * The tile server will happily run eight requests at once, but each render
     * holds a decoded tile and a full pixmap, and on a mid-range device eight
     * at a time both exhausts memory and thrashes the CPU — measured render
     * times climbed from 235 ms to 1790 ms purely from contention.
     */
    private val renderSlots = Semaphore(
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

        /** Source tile cache budget, in bytes. */
        const val DEFAULT_CACHE_BYTES: Int = 16 * 1024 * 1024

        /** Rendered tile disk cache budget, in bytes. */
        const val DEFAULT_DISK_CACHE_BYTES: Long = 64L * 1024 * 1024

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
            val gpu = when (renderMode) {
                RenderMode.CPU -> null
                // AUTO and GPU both probe; AUTO falls back silently, GPU says
                // so, because asking for the GPU and quietly getting the CPU
                // is how a performance regression hides.
                RenderMode.AUTO -> GpuTileRasterizer.createOrNull(tileSize)
                RenderMode.GPU -> GpuTileRasterizer.createOrNull(tileSize).also {
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
                styleJson = styleJson,
                gpu = gpu,
            )
        }

        private fun httpGet(url: String, headers: Map<String, String>): ByteArray? {
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

    override fun renderTile(request: TileRequest): ByteArray? {
        if (closed) return null

        val cacheKey = diskCache?.let {
            TileDiskCache.digest(
                styleKey,
                "$tileSize",
                "${request.z}/${request.x}/${request.y}",
            )
        }
        if (cacheKey != null) {
            val hit = diskCache.get(cacheKey)
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
        for (i in 0 until plan.length()) {
            tiles.add(sourceTile(plan.getJSONObject(i).getString("url")))
        }
        val fetchMs = (System.nanoTime() - fetchStarted) / 1_000_000
        if (closed) return null

        val renderStarted = System.nanoTime()
        val png = if (gpu != null) {
            // The GL thread serialises drawing already, so the CPU-side
            // semaphore would only add queueing on top of it.
            runCatching { renderOnGpu(request, tiles) }.getOrNull()
                ?: run {
                    // A GPU failure must not lose the tile; the CPU can always
                    // draw it.
                    renderSlots.acquire()
                    try {
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
        if (cacheKey != null && png != null) diskCache.put(cacheKey, png)
        return png
    }

    private fun renderOnGpu(request: TileRequest, tiles: List<ByteArray?>): ByteArray? {
        val rasterizer = gpu ?: return null
        val lengths = IntArray(tiles.size) { tiles[it]?.size ?: 0 }
        val data = ByteArray(lengths.sum())
        var offset = 0
        for (tile in tiles) {
            if (tile == null) continue
            tile.copyInto(data, offset)
            offset += tile.size
        }
        val packed = renderer.tessellate(
            request.z, request.x, request.y, tileSize, data, lengths,
        )
        return rasterizer.renderPng(TessellatedTile(packed))
    }

    private fun sourceTile(url: String): ByteArray? {
        cache.get(url)?.let { return it }
        if (empties.contains(url)) return null

        val bytes = runCatching { fetchTile(url) }.getOrNull()
        if (bytes == null) {
            empties.add(url)
            return null
        }
        cache.put(url, bytes)
        return bytes
    }

    /** Releases the native renderer. Safe to call more than once. */
    override fun close() {
        if (closed) return
        closed = true
        cache.evictAll()
        empties.clear()
        gpu?.close()
        renderer.close()
    }
}
