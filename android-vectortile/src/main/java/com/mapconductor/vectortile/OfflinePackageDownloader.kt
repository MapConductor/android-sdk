package com.mapconductor.vectortile

import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLDecoder
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.tan
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Fetches everything a style needs to draw one area and writes an
 * [OfflinePackage].
 *
 * What "everything" is comes from the renderer, not from guessing at the
 * style: `plan` says which source tiles each display tile reads (including
 * the shallower tile a magnified level reads from, so a source that stops at
 * z14 is not asked for z15), and `neededGlyphs` says which font ranges the
 * labels in those tiles use -- fetching every range a CJK font has would be
 * eighty files per font, nearly all unused. The sprite comes in both
 * resolutions, since the two platforms' rasterisers ask for different ones.
 */
object OfflinePackageDownloader {
    enum class Phase { PLANNING, TILES, GLYPHS, SPRITE, DONE }

    class Progress(
        val phase: Phase,
        val done: Int,
        val total: Int,
    ) {
        override fun toString(): String = "${phase.name.lowercase()} $done/$total"
    }

    /** A bounds and zoom range that would need more tiles than this is refused. */
    const val MAX_TILES = 20_000

    /**
     * @param styleJson the style, with absolute source, glyph and sprite URLs
     * @param bounds the area, in degrees
     * @param minZoom the shallowest display zoom to cover; 0 costs one tile
     * @param maxZoom the deepest display zoom; sources deeper than their own
     *   `maxzoom` are magnified from it, so this can exceed the data's zoom
     * @param directory where the package goes; anything there is replaced
     * @param fetch how bytes are got; null for plain HTTP with [headers]
     * @throws IllegalArgumentException when the style will not parse or the
     *   area would need more than [MAX_TILES] tiles
     */
    @JvmStatic
    suspend fun download(
        styleJson: String,
        bounds: OfflinePackage.Bounds,
        minZoom: Int = 0,
        maxZoom: Int = 14,
        directory: File,
        headers: Map<String, String> = emptyMap(),
        parallelism: Int = 8,
        fetch: ((String) -> ByteArray?)? = null,
        onProgress: (Progress) -> Unit = {},
    ): OfflinePackage =
        withContext(Dispatchers.IO) {
            require(minZoom in 0..maxZoom && maxZoom <= 22) { "zoom range $minZoom..$maxZoom" }
            val get: (String) -> FetchResult =
                fetch?.let { custom ->
                    { url ->
                        try {
                            custom(url)
                                ?.takeIf { it.isNotEmpty() }
                                ?.let { FetchResult.Success(it) }
                                ?: FetchResult.NotFound
                        } catch (t: Throwable) {
                            if (t is CancellationException) throw t
                            FetchResult.TemporaryFailure(url, t)
                        }
                    }
                } ?: { url ->
                    httpFetch(url, headers)
                }
            val normalizedStyleJson = normalizeStyle(styleJson, get)
            val renderer = VectorTileRenderer.create(normalizedStyleJson, VectorTileProvider.DEFAULT_TILE_SIZE)
            try {
                directory.deleteRecursively()
                directory.mkdirs()
                File(directory, OfflinePackage.STYLE_FILE).writeText(normalizedStyleJson)
                val index = java.util.concurrent.ConcurrentHashMap<String, String>()
                val bytes = java.util.concurrent.atomic.AtomicLong()

                // --- plan: which source tiles, for which display tiles ---
                onProgress(Progress(Phase.PLANNING, 0, 0))
                class DisplayTile(
                    val z: Int,
                    val x: Int,
                    val y: Int,
                    val urls: List<String?>,
                )
                val displayTiles = ArrayList<DisplayTile>()
                val wanted = LinkedHashMap<String, String>() // url -> relative path
                for (z in minZoom..maxZoom) {
                    val n = 1 shl z
                    val x0 = tileX(bounds.west, z).coerceIn(0, n - 1)
                    val x1 = tileX(bounds.east, z).coerceIn(0, n - 1)
                    val y0 = tileY(bounds.north, z).coerceIn(0, n - 1)
                    val y1 = tileY(bounds.south, z).coerceIn(0, n - 1)
                    val minX = minOf(x0, x1)
                    val maxX = maxOf(x0, x1)
                    val minY = minOf(y0, y1)
                    val maxY = maxOf(y0, y1)
                    for (x in minX..maxX) {
                        for (y in minY..maxY) {
                            ensureActive()
                            val plan = JSONArray(renderer.plan(z, x, y))
                            val urls = ArrayList<String?>(plan.length())
                            val boundary = x == minX || x == maxX || y == minY || y == maxY
                            for (i in 0 until plan.length()) {
                                val entry = plan.getJSONObject(i)
                                // Interior neighbours are only for label
                                // placement and stay out of the package. At
                                // the requested edge they are the one-tile
                                // buffer that keeps boundary labels intact.
                                if (entry.optBoolean("labelsOnly", false) && !boundary) {
                                    urls.add(null)
                                    continue
                                }
                                val url = entry.getString("url")
                                urls.add(url)
                                wanted.getOrPut(url) {
                                    "${OfflinePackage.TILES_DIR}/${entry.getString("sourceId")}/" +
                                        "${entry.getInt("z")}/${entry.getInt("x")}/${entry.getInt("y")}.mvt"
                                }
                                require(wanted.size <= MAX_TILES) {
                                    "more than $MAX_TILES tiles; shrink the area or the zoom range"
                                }
                            }
                            displayTiles.add(DisplayTile(z, x, y, urls))
                        }
                    }
                }

                // --- tiles ---
                val total = wanted.size
                val done = java.util.concurrent.atomic.AtomicInteger()
                onProgress(Progress(Phase.TILES, 0, total))
                val gate = Semaphore(parallelism.coerceAtLeast(1))
                coroutineScope {
                    wanted.entries.map { (url, relative) ->
                        async {
                            gate.withPermit {
                                ensureActive()
                                when (val result = get(url)) {
                                    is FetchResult.Success -> {
                                        write(directory, relative, result.data)
                                        index[url] = relative
                                        bytes.addAndGet(result.data.size.toLong())
                                    }
                                    FetchResult.NotFound -> Unit
                                    is FetchResult.TemporaryFailure -> throw result.toIOException()
                                }
                                onProgress(Progress(Phase.TILES, done.incrementAndGet(), total))
                            }
                        }
                    }.awaitAll()
                }

                // --- glyphs: what the fetched tiles' labels need ---
                val glyphTemplate = renderer.glyphsUrlTemplate()
                val glyphMatcher = glyphTemplate?.let(::templateRegex)
                var glyphCount = 0
                onProgress(Progress(Phase.GLYPHS, 0, displayTiles.size))
                if (glyphMatcher != null) {
                    displayTiles.forEachIndexed { i, tile ->
                        ensureActive()
                        val data =
                            tile.urls.map { url ->
                                url
                                    ?.let { index[it] }
                                    ?.let { File(directory, it).readBytes() }
                            }
                        val needed =
                            runCatching {
                                renderer.neededGlyphs(tile.z, tile.x, tile.y, data)
                            }.getOrDefault(emptyList())
                        for (url in needed) {
                            if (index.containsKey(url)) continue
                            val match = glyphMatcher.matchEntire(url) ?: continue
                            val fontstack = URLDecoder.decode(match.groupValues[1], "UTF-8")
                            val range = match.groupValues[2]
                            val pbfResult = get(url)
                            if (pbfResult is FetchResult.TemporaryFailure) throw pbfResult.toIOException()
                            val pbf = (pbfResult as? FetchResult.Success)?.data ?: continue
                            val relative = "${OfflinePackage.GLYPHS_DIR}/$fontstack/$range.pbf"
                            write(directory, relative, pbf)
                            index[url] = relative
                            bytes.addAndGet(pbf.size.toLong())
                            glyphCount += 1
                            // Fed back so the next tile asks only for what is still missing.
                            runCatching { renderer.addGlyphs(pbf) }
                        }
                        onProgress(Progress(Phase.GLYPHS, i + 1, displayTiles.size))
                    }
                }

                // --- sprite, both resolutions ---
                onProgress(Progress(Phase.SPRITE, 0, 2))
                var spriteCount = 0
                for (ratio in 1..2) {
                    val urls = runCatching { renderer.spriteUrls(ratio) }.getOrDefault(emptyList())
                    if (urls.size != 2) continue
                    val suffix = if (ratio > 1) "@2x" else ""
                    for ((url, ext) in listOf(urls[0] to "json", urls[1] to "png")) {
                        val dataResult = get(url)
                        if (dataResult is FetchResult.TemporaryFailure) throw dataResult.toIOException()
                        val data = (dataResult as? FetchResult.Success)?.data ?: continue
                        val relative = "${OfflinePackage.SPRITE_FILE}$suffix.$ext"
                        write(directory, relative, data)
                        index[url] = relative
                        bytes.addAndGet(data.size.toLong())
                        spriteCount += 1
                    }
                    onProgress(Progress(Phase.SPRITE, ratio, 2))
                }

                val style = JSONObject(normalizedStyleJson)
                val sources = style.optJSONObject("sources") ?: JSONObject()
                val templates =
                    sources.keys().asSequence().mapNotNull { id ->
                        sources.optJSONObject(id)?.optJSONArray("tiles")?.optString(0)
                            ?.takeIf { it.isNotEmpty() }?.let { id to it }
                    }.toMap()
                val manifest =
                    OfflinePackage.Manifest(
                        bounds = bounds,
                        minZoom = minZoom,
                        maxZoom = maxZoom,
                        styleDigest = TileDiskCache.digest(normalizedStyleJson),
                        createdAt = System.currentTimeMillis(),
                        tiles = index.count { it.value.startsWith("${OfflinePackage.TILES_DIR}/") },
                        glyphs = glyphCount,
                        sprites = spriteCount,
                        bytes = bytes.get(),
                        sources = templates,
                        glyphsTemplate = glyphTemplate,
                        spriteBase = style.optString("sprite").takeIf { it.isNotEmpty() },
                    )
                onProgress(Progress(Phase.DONE, total, total))
                OfflinePackage.create(directory, manifest, index)
            } finally {
                renderer.close()
            }
        }

    private sealed class FetchResult {
        class Success(val data: ByteArray) : FetchResult()

        object NotFound : FetchResult()

        class TemporaryFailure(
            val url: String,
            val cause: Throwable? = null,
            val status: Int? = null,
        ) : FetchResult() {
            fun toIOException(): IOException =
                IOException(
                    status?.let { "offline package fetch failed: HTTP $it $url" }
                        ?: "offline package fetch failed: $url",
                    cause,
                )
        }
    }

    private fun normalizeStyle(
        styleJson: String,
        get: (String) -> FetchResult,
    ): String {
        val style = JSONObject(styleJson)
        val sources = style.optJSONObject("sources") ?: return style.toString()
        val ids = sources.keys().asSequence().toList()
        for (id in ids) {
            val source = sources.optJSONObject(id) ?: continue
            val tileJsonUrl = source.optString("url").takeIf { it.isNotEmpty() } ?: continue
            val data =
                when (val result = get(tileJsonUrl)) {
                    is FetchResult.Success -> result.data
                    FetchResult.NotFound -> throw IOException("offline package TileJSON not found: $tileJsonUrl")
                    is FetchResult.TemporaryFailure -> throw result.toIOException()
                }
            val tileJson = JSONObject(String(data, Charsets.UTF_8))
            val tiles = tileJson.optJSONArray("tiles")
                ?: throw IOException("offline package TileJSON has no tiles: $tileJsonUrl")
            val resolvedTiles = JSONArray()
            for (i in 0 until tiles.length()) {
                resolvedTiles.put(resolveUrl(tileJsonUrl, tiles.getString(i)))
            }
            source.put("tiles", resolvedTiles)
            for (field in TILEJSON_SOURCE_FIELDS) {
                if (tileJson.has(field)) source.put(field, tileJson.get(field))
            }
            source.remove("url")
        }
        return style.toString()
    }

    private val TILEJSON_SOURCE_FIELDS =
        listOf("minzoom", "maxzoom", "bounds", "attribution", "scheme")

    private fun resolveUrl(
        base: String,
        value: String,
    ): String =
        try {
            URL(URL(base), value).toString()
        } catch (_: Throwable) {
            value
        }

    private fun httpFetch(
        url: String,
        headers: Map<String, String>,
    ): FetchResult {
        val connection =
            try {
                URL(url).openConnection() as HttpURLConnection
            } catch (t: Throwable) {
                if (t is CancellationException) throw t
                return FetchResult.TemporaryFailure(url, t)
            }
        var reuse = false
        return try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 15_000
            connection.readTimeout = 15_000
            headers.forEach(connection::setRequestProperty)
            when (val status = connection.responseCode) {
                HttpURLConnection.HTTP_NOT_FOUND, HttpURLConnection.HTTP_NO_CONTENT -> {
                    connection.errorStream?.use { it.readBytes() }
                    reuse = true
                    FetchResult.NotFound
                }
                in 200..299 -> {
                    val bytes = connection.inputStream.use { it.readBytes() }
                    reuse = true
                    bytes.takeIf { it.isNotEmpty() }?.let { FetchResult.Success(it) }
                        ?: FetchResult.NotFound
                }
                else -> {
                    connection.errorStream?.use { it.readBytes() }
                    reuse = true
                    FetchResult.TemporaryFailure(url, status = status)
                }
            }
        } catch (t: Throwable) {
            if (t is CancellationException) throw t
            FetchResult.TemporaryFailure(url, t)
        } finally {
            if (!reuse) connection.disconnect()
        }
    }

    private fun write(
        directory: File,
        relative: String,
        data: ByteArray,
    ) {
        val file = File(directory, relative)
        file.parentFile?.mkdirs()
        val temp = File(file.parentFile, "${file.name}.part")
        temp.writeBytes(data)
        if (!temp.renameTo(file)) {
            file.writeBytes(data)
            temp.delete()
        }
    }

    /** `{fontstack}` and `{range}` as capture groups; everything else literal. */
    private fun templateRegex(template: String): Regex {
        val parts = template.split("{fontstack}", "{range}")
        val fontstackFirst = template.indexOf("{fontstack}") < template.indexOf("{range}")
        require(parts.size == 3 && fontstackFirst) { "glyph template must name {fontstack} then {range}: $template" }
        return Regex(Regex.escape(parts[0]) + "(.+)" + Regex.escape(parts[1]) + "(\\d+-\\d+)" + Regex.escape(parts[2]))
    }

    private fun tileX(
        lon: Double,
        z: Int,
    ): Int = floor((lon + 180.0) / 360.0 * (1 shl z)).toInt()

    private fun tileY(
        lat: Double,
        z: Int,
    ): Int {
        val rad = Math.toRadians(lat.coerceIn(-85.05112878, 85.05112878))
        return floor((1.0 - ln(tan(rad) + 1.0 / cos(rad)) / PI) / 2.0 * (1 shl z)).toInt()
    }
}
