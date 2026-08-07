package com.mapconductor.example.pages.geojson.layer

import com.mapconductor.geojson.GeoJSONFeature
import com.mapconductor.geojson.GeoJSONParser
import org.json.JSONArray
import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipInputStream
import android.content.res.AssetManager
import android.graphics.Color

data class ExampleGeoJSONLayerData(
    val features: List<GeoJSONFeature>,
    val styleProvider: ExampleGeoJSONStyler,
)

class ExampleGeoJSONLayerLoader(
    private val assets: AssetManager,
) {
    fun load(assetName: String): ExampleGeoJSONLayerData {
        var features: List<GeoJSONFeature>? = null
        var styleJson: String? = null

        assets.open(assetName).use { input ->
            ZipInputStream(input).use { zipInput ->
                while (true) {
                    val entry = zipInput.nextEntry ?: break
                    if (!entry.isDirectory) {
                        val fileName = entry.name.substringAfterLast('/')
                        when {
                            fileName.equals(GEOJSON_ENTRY, ignoreCase = true) -> {
                                // GeoJSONParser closes its input, so shield the ZIP stream itself.
                                features = GeoJSONParser.parseStream(NonClosingInputStream(zipInput))
                            }
                            fileName.equals(STYLE_ENTRY, ignoreCase = true) -> {
                                styleJson = zipInput.bufferedReader(Charsets.UTF_8).readText()
                            }
                        }
                    }
                    zipInput.closeEntry()
                }
            }
        }

        val loadedFeatures =
            features ?: throw IOException("$GEOJSON_ENTRY was not found in $assetName")
        val loadedStyleJson =
            styleJson ?: throw IOException("$STYLE_ENTRY was not found in $assetName")
        return ExampleGeoJSONLayerData(
            features = loadedFeatures,
            styleProvider = ExampleGeoJSONStyler(parseRouteColors(loadedStyleJson)),
        )
    }

    private fun parseRouteColors(json: String): Map<ExampleGeoJSONStyler.RouteKey, Int> {
        val result = mutableMapOf<ExampleGeoJSONStyler.RouteKey, Int>()
        val companies = JSONArray(json)
        for (companyIndex in 0 until companies.length()) {
            val company = companies.optJSONObject(companyIndex)?.optJSONObject("company") ?: continue
            val companyName = company.optString("name").takeIf(String::isNotBlank) ?: continue
            val lines = company.optJSONArray("lines") ?: continue
            for (lineIndex in 0 until lines.length()) {
                val line = lines.optJSONObject(lineIndex) ?: continue
                val lineName = line.optString("name").takeIf(String::isNotBlank) ?: continue
                val color = line.optString("color").takeIf(String::isNotBlank) ?: continue
                result[ExampleGeoJSONStyler.RouteKey(companyName, lineName)] = Color.parseColor(color)
            }
        }
        return result
    }

    private class NonClosingInputStream(
        input: InputStream,
    ) : FilterInputStream(input) {
        override fun close() = Unit
    }

    companion object {
        private const val GEOJSON_ENTRY = "N02-22_RailroadSection.geojson"
        private const val STYLE_ENTRY = "N02-22_RailroadSection.style.json"
    }
}
