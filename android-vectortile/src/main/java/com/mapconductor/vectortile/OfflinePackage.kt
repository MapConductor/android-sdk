package com.mapconductor.vectortile

import java.io.File
import java.net.URLEncoder
import java.util.concurrent.atomic.AtomicLong
import org.json.JSONArray
import org.json.JSONObject

/**
 * A style and everything it needs to draw one area, on disk.
 *
 * Downloaded by [OfflinePackageDownloader] and read here. The layout is
 * plain files the style's own URL templates map onto, so a package can be
 * served to a map that draws vector styles itself (MapLibre, Mapbox,
 * MapTiler) as well as read by the rasteriser for every other map:
 *
 * ```
 * manifest.json                     what is in here and where it came from
 * style.json                        the style, as downloaded
 * index.json                        source URL -> relative path
 * tiles/<source>/<z>/<x>/<y>.mvt
 * glyphs/<fontstack>/<range>.pbf
 * sprite.json  sprite.png  sprite@2x.json  sprite@2x.png
 * ```
 *
 * ios-vectortile reads and writes the same layout, so a package made on one
 * platform serves the other.
 */
class OfflinePackage private constructor(
    val directory: File,
    val manifest: Manifest,
    private val index: Map<String, String>,
) {
    /** South-west to north-east, degrees. */
    class Bounds(
        val south: Double,
        val west: Double,
        val north: Double,
        val east: Double,
    ) {
        fun toJson(): JSONObject =
            JSONObject().put("south", south).put("west", west).put("north", north).put("east", east)

        companion object {
            fun fromJson(json: JSONObject): Bounds =
                Bounds(
                    south = json.getDouble("south"),
                    west = json.getDouble("west"),
                    north = json.getDouble("north"),
                    east = json.getDouble("east"),
                )
        }
    }

    class Manifest(
        val bounds: Bounds,
        val minZoom: Int,
        val maxZoom: Int,
        val styleDigest: String,
        val createdAt: Long,
        val tiles: Int,
        val glyphs: Int,
        val sprites: Int,
        val bytes: Long,
        /** Source id -> the tile URL template the tiles were fetched from. */
        val sources: Map<String, String>,
        val glyphsTemplate: String?,
        val spriteBase: String?,
    ) {
        fun toJson(): JSONObject =
            JSONObject()
                .put("version", FORMAT_VERSION)
                .put("bounds", bounds.toJson())
                .put("minZoom", minZoom)
                .put("maxZoom", maxZoom)
                .put("styleDigest", styleDigest)
                .put("createdAt", createdAt)
                .put("tiles", tiles)
                .put("glyphs", glyphs)
                .put("sprites", sprites)
                .put("bytes", bytes)
                .put("sources", JSONObject(sources))
                .putOpt("glyphsTemplate", glyphsTemplate)
                .putOpt("spriteBase", spriteBase)

        companion object {
            fun fromJson(json: JSONObject): Manifest {
                val sources = json.optJSONObject("sources") ?: JSONObject()
                return Manifest(
                    bounds = Bounds.fromJson(json.getJSONObject("bounds")),
                    minZoom = json.getInt("minZoom"),
                    maxZoom = json.getInt("maxZoom"),
                    styleDigest = json.getString("styleDigest"),
                    createdAt = json.getLong("createdAt"),
                    tiles = json.optInt("tiles"),
                    glyphs = json.optInt("glyphs"),
                    sprites = json.optInt("sprites"),
                    bytes = json.optLong("bytes"),
                    sources = sources.keys().asSequence().associateWith { sources.getString(it) },
                    glyphsTemplate = json.optString("glyphsTemplate").takeIf { it.isNotEmpty() },
                    spriteBase = json.optString("spriteBase").takeIf { it.isNotEmpty() },
                )
            }
        }
    }

    /** The style as it was downloaded, pointing at its original servers. */
    val styleJson: String by lazy { File(directory, STYLE_FILE).readText() }

    /** The bytes the package holds for a source URL, or null when it has none. */
    fun bytes(url: String): ByteArray? {
        val relative = index[url] ?: return null
        val file = File(directory, relative)
        return if (file.isFile) file.readBytes() else null
    }

    /** Whether the package answers for this URL. */
    fun contains(url: String): Boolean = index[url]?.let { File(directory, it).isFile } == true

    /**
     * Where a file the package does not have would come from.
     *
     * The relative path is the one the served style asks for, so it maps
     * back through the same templates the package was fetched with. Null for
     * a path that is not in the package's vocabulary.
     */
    fun upstreamUrl(relativePath: String): String? {
        val segments = relativePath.split('/')
        return when {
            segments.size == 5 && segments[0] == TILES_DIR && segments[4].endsWith(".mvt") -> {
                val template = manifest.sources[segments[1]] ?: return null
                template
                    .replace("{z}", segments[2])
                    .replace("{x}", segments[3])
                    .replace("{y}", segments[4].removeSuffix(".mvt"))
            }
            segments.size == 3 && segments[0] == GLYPHS_DIR && segments[2].endsWith(".pbf") -> {
                val template = manifest.glyphsTemplate ?: return null
                template
                    .replace("{fontstack}", URLEncoder.encode(segments[1], "UTF-8").replace("+", "%20"))
                    .replace("{range}", segments[2].removeSuffix(".pbf"))
            }
            segments.size == 1 && segments[0].startsWith(SPRITE_FILE) -> {
                val base = manifest.spriteBase ?: return null
                base + segments[0].removePrefix(SPRITE_FILE)
            }
            else -> null
        }
    }

    /**
     * The style rewritten to read from [baseUrl], for a map that draws the
     * style itself. Every source becomes a tile template under
     * `tiles/<source>/`, the glyphs and the sprite move under the same root,
     * and a TileJSON `url` is dropped in favour of the tiles it named.
     */
    fun styleServedBy(baseUrl: String): String {
        val style = JSONObject(styleJson)
        val sources = style.optJSONObject("sources") ?: JSONObject()
        for (id in sources.keys().asSequence().toList()) {
            val source = sources.optJSONObject(id) ?: continue
            if (!manifest.sources.containsKey(id)) continue
            source.remove("url")
            source.put("tiles", JSONArray().put("$baseUrl/$TILES_DIR/$id/{z}/{x}/{y}.mvt"))
        }
        if (manifest.glyphsTemplate != null) style.put("glyphs", "$baseUrl/$GLYPHS_DIR/{fontstack}/{range}.pbf")
        if (manifest.spriteBase != null) style.put("sprite", "$baseUrl/$SPRITE_FILE")
        return style.toString()
    }

    /**
     * How the fetches of one provider have gone so far: what the package
     * answered, what went to the network, and what was refused offline.
     * A value, so that each report is a change a UI notices.
     */
    data class Stats(
        val packageHits: Long = 0,
        val networkFetches: Long = 0,
        val blocked: Long = 0,
    ) {
        override fun toString(): String = "package=$packageHits network=$networkFetches blocked=$blocked"
    }

    private class Counters {
        val packageHits = AtomicLong()
        val networkFetches = AtomicLong()
        val blocked = AtomicLong()

        fun snapshot(): Stats = Stats(packageHits.get(), networkFetches.get(), blocked.get())
    }

    /**
     * A `fetchTile` for [VectorTileProvider]: the package first, then --
     * while [online] -- the network, and otherwise nothing.
     *
     * "Nothing" is thrown rather than returned: a null from a fetch means
     * "there is no such tile" and is remembered as such, whereas a tile the
     * network was not allowed to fetch is there to be had the moment it is.
     */
    class Fetcher(
        private val pkg: OfflinePackage,
        private val upstream: (String) -> ByteArray?,
        @Volatile var online: Boolean = true,
        private val onStats: ((Stats) -> Unit)? = null,
    ) : (String) -> ByteArray? {
        private val counters = Counters()

        val stats: Stats get() = counters.snapshot()

        override fun invoke(url: String): ByteArray? {
            val packaged = pkg.bytes(url)
            if (packaged != null) {
                counters.packageHits.incrementAndGet()
                onStats?.invoke(counters.snapshot())
                return packaged
            }
            if (!online) {
                counters.blocked.incrementAndGet()
                onStats?.invoke(counters.snapshot())
                throw OfflineUnavailableException(url)
            }
            counters.networkFetches.incrementAndGet()
            onStats?.invoke(counters.snapshot())
            return upstream(url)
        }
    }

    companion object {
        const val FORMAT_VERSION = 1
        const val MANIFEST_FILE = "manifest.json"
        const val STYLE_FILE = "style.json"
        const val INDEX_FILE = "index.json"
        const val TILES_DIR = "tiles"
        const val GLYPHS_DIR = "glyphs"
        const val SPRITE_FILE = "sprite"

        /** The package in [directory], or null when there is none or it will not parse. */
        @JvmStatic
        fun open(directory: File): OfflinePackage? {
            val manifestFile = File(directory, MANIFEST_FILE)
            val indexFile = File(directory, INDEX_FILE)
            if (!manifestFile.isFile || !indexFile.isFile) return null
            return runCatching {
                val manifest = Manifest.fromJson(JSONObject(manifestFile.readText()))
                val indexJson = JSONObject(indexFile.readText())
                val index = indexJson.keys().asSequence().associateWith { indexJson.getString(it) }
                OfflinePackage(directory, manifest, index)
            }.getOrNull()
        }

        /** Removes a package, whole. */
        @JvmStatic
        fun delete(directory: File): Boolean = directory.deleteRecursively()

        internal fun create(
            directory: File,
            manifest: Manifest,
            index: Map<String, String>,
        ): OfflinePackage {
            File(directory, MANIFEST_FILE).writeText(manifest.toJson().toString())
            File(directory, INDEX_FILE).writeText(JSONObject(index).toString())
            return OfflinePackage(directory, manifest, index)
        }
    }
}

/** A fetch refused because the network is off and the package has no answer. */
class OfflineUnavailableException(
    url: String,
) : RuntimeException("offline: $url")
