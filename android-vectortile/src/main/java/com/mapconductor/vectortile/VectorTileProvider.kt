package com.mapconductor.vectortile

import android.util.LruCache
import com.mapconductor.core.tileserver.TileProviderInterface
import com.mapconductor.core.tileserver.TileRequest
import java.io.Closeable
import java.net.HttpURLConnection
import java.net.URL
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
    cacheEntries: Int,
) : TileProviderInterface, Closeable {

    /**
     * Source tiles keyed by URL.
     *
     * Not an optimisation detail: neighbouring target tiles routinely need the
     * same source tile — always, once overzoom kicks in, where one magnified
     * ancestor serves 16 targets — and a map asks for a whole viewport at once.
     */
    private val cache = LruCache<String, ByteArray>(cacheEntries)

    /** URLs known to hold nothing, so a missing tile is not re-requested. */
    private val empties = java.util.Collections.synchronizedSet(HashSet<String>())

    @Volatile
    private var closed = false

    companion object {
        const val DEFAULT_TILE_SIZE: Int = 512

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
            cacheEntries: Int = 256,
            fetchTile: ((String) -> ByteArray?)? = null,
        ): VectorTileProvider {
            val renderer = VectorTileRenderer.create(styleJson)
            return VectorTileProvider(
                renderer = renderer,
                tileSize = tileSize,
                headers = headers,
                fetchTile = fetchTile ?: { url -> httpGet(url, headers) },
                cacheEntries = cacheEntries,
            )
        }

        private fun httpGet(url: String, headers: Map<String, String>): ByteArray? {
            val connection = URL(url).openConnection() as HttpURLConnection
            return try {
                connection.requestMethod = "GET"
                connection.connectTimeout = 15_000
                connection.readTimeout = 15_000
                headers.forEach(connection::setRequestProperty)
                when (connection.responseCode) {
                    // A missing tile is normal at the edge of a source's coverage.
                    HttpURLConnection.HTTP_NOT_FOUND, HttpURLConnection.HTTP_NO_CONTENT -> null
                    in 200..299 -> connection.inputStream.use { it.readBytes() }
                        .takeIf { it.isNotEmpty() }
                    else -> throw java.io.IOException(
                        "tile fetch failed: ${connection.responseCode} $url",
                    )
                }
            } finally {
                connection.disconnect()
            }
        }
    }

    /** Reasons the current style may not render as intended. */
    fun diagnostics(): List<String> {
        val array = JSONArray(renderer.diagnostics())
        return (0 until array.length()).map { array.getString(it) }
    }

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
    }

    override fun renderTile(request: TileRequest): ByteArray? {
        if (closed) return null

        val plan = JSONArray(renderer.plan(request.z, request.x, request.y))
        val tiles = ArrayList<ByteArray?>(plan.length())
        for (i in 0 until plan.length()) {
            tiles.add(sourceTile(plan.getJSONObject(i).getString("url")))
        }
        if (closed) return null

        return runCatching {
            renderer.render(request.z, request.x, request.y, tileSize, tiles)
        }.getOrNull()
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
        renderer.close()
    }
}
