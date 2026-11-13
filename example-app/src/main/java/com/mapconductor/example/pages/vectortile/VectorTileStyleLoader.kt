package com.mapconductor.example.pages.vectortile

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Fetches a Shortbread-schema style and repoints it at the OSMF tile service.
 *
 * The style and the tiles come from different hosts on purpose: Shortbread is
 * an open schema, so any style written against it works with any server that
 * serves it. Neither needs an API key.
 */
object VectorTileStyleLoader {
    private const val STYLE_URL = "https://tiles.versatiles.org/assets/styles/colorful/style.json"
    private const val OSM_TILES = "https://vector.openstreetmap.org/shortbread_v1/{z}/{x}/{y}.mvt"

    /**
     * What the tiles this points at are owed.
     *
     * Kept here rather than taken on trust from the upstream style: the
     * sources are being repointed at the OSMF service, so the credit that
     * matters is the one *those* tiles require, whatever the original style
     * happened to say. The style's own wording is preferred when it has one --
     * it is the same licence, in the publisher's own phrasing.
     */
    private const val OSM_ATTRIBUTION =
        "&copy; <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a> contributors"

    /** Blocking; call from a background dispatcher. */
    fun load(): String {
        val style = JSONObject(fetch(STYLE_URL))
        val sources = style.getJSONObject("sources")
        for (id in sources.keys()) {
            // Carried across rather than dropped. Rewriting a source is not a
            // reason to stop crediting the data, and a bare replacement object
            // is how the credit went missing in the first place.
            val attribution =
                sources
                    .optJSONObject(id)
                    ?.optString("attribution")
                    ?.takeIf { it.isNotBlank() }
                    ?: OSM_ATTRIBUTION
            sources.put(
                id,
                JSONObject()
                    .put("type", "vector")
                    .put("tiles", org.json.JSONArray().put(OSM_TILES))
                    .put("minzoom", 0)
                    .put("maxzoom", 14)
                    .put("attribution", attribution),
            )
        }
        return style.toString()
    }

    private fun fetch(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        return try {
            connection.connectTimeout = 20_000
            connection.readTimeout = 20_000
            if (connection.responseCode !in 200..299) {
                throw java.io.IOException("style fetch failed: ${connection.responseCode}")
            }
            connection.inputStream.use { it.readBytes().decodeToString() }
        } finally {
            connection.disconnect()
        }
    }
}
