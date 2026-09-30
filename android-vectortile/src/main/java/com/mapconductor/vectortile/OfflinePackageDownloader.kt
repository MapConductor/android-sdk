package com.mapconductor.vectortile

import java.io.File
import java.net.URLDecoder
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.tan
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
            val get: (String) -> ByteArray? = fetch ?: { url -> VectorTileProvider.httpGet(url, headers) }
            val renderer = VectorTileRenderer.create(styleJson, VectorTileProvider.DEFAULT_TILE_SIZE)
            try {
                directory.deleteRecursively()
                directory.mkdirs()
                File(directory, OfflinePackage.STYLE_FILE).writeText(styleJson)
                val index = java.util.concurrent.ConcurrentHashMap<String, String>()
                val bytes = java.util.concurrent.atomic.AtomicLong()

                // --- plan: which source tiles, for which display tiles ---
                onProgress(Progress(Phase.PLANNING, 0, 0))
                class DisplayTile(val z: Int, val x: Int, val y: Int, val urls: List<String?>)
                val displayTiles = ArrayList<DisplayTile>()
                val wanted = LinkedHashMap<String, String>() // url -> relative path
                for (z in minZoom..maxZoom) {
                    val n = 1 shl z
                    val x0 = tileX(bounds.west, z).coerceIn(0, n - 1)
                    val x1 = tileX(bounds.east, z).coerceIn(0, n - 1)
                    val y0 = tileY(bounds.north, z).coerceIn(0, n - 1)
                    val y1 = tileY(bounds.south, z).coerceIn(0, n - 1)
                    for (x in minOf(x0, x1)..maxOf(x0, x1)) {
                        for (y in minOf(y0, y1)..maxOf(y0, y1)) {
                            ensureActive()
                            val plan = JSONArray(renderer.plan(z, x, y))
                            val urls = ArrayList<String?>(plan.length())
                            for (i in 0 until plan.length()) {
                                val entry = plan.getJSONObject(i)
                                // Neighbours wanted only for label placement
                                // at the edge are left out: they lie outside
                                // the area, and a package is what is inside it.
                                if (entry.optBoolean("labelsOnly", false)) {
                                    urls.add(null)
                                    continue
                                }
                                val url = entry.getString("url")
                                urls.add(url)
                                wanted.getOrPut(url) {
                                    "${OfflinePackage.TILES_DIR}/${entry.getString("sourceId")}/" +
                                        "${entry.getInt("z")}/${entry.getInt("x")}/${entry.getInt("y")}.mvt"
                                }
                                require(wanted.size <= MAX_TILES) { "more than $MAX_TILES tiles; shrink the area or the zoom range" }
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
                                val data = runCatching { get(url) }.getOrNull()
                                if (data != null && data.isNotEmpty()) {
                                    write(directory, relative, data)
                                    index[url] = relative
                                    bytes.addAndGet(data.size.toLong())
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
                        val data = tile.urls.map { url -> url?.let { index[it] }?.let { File(directory, it).readBytes() } }
                        val needed = runCatching { renderer.neededGlyphs(tile.z, tile.x, tile.y, data) }.getOrDefault(emptyList())
                        for (url in needed) {
                            if (index.containsKey(url)) continue
                            val match = glyphMatcher.matchEntire(url) ?: continue
                            val fontstack = URLDecoder.decode(match.groupValues[1], "UTF-8")
                            val range = match.groupValues[2]
                            val pbf = runCatching { get(url) }.getOrNull() ?: continue
                            if (pbf.isEmpty()) continue
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
                        val data = runCatching { get(url) }.getOrNull() ?: continue
                        if (data.isEmpty()) continue
                        val relative = "${OfflinePackage.SPRITE_FILE}$suffix.$ext"
                        write(directory, relative, data)
                        index[url] = relative
                        bytes.addAndGet(data.size.toLong())
                        spriteCount += 1
                    }
                    onProgress(Progress(Phase.SPRITE, ratio, 2))
                }

                val style = JSONObject(styleJson)
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
                        styleDigest = TileDiskCache.digest(styleJson),
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
