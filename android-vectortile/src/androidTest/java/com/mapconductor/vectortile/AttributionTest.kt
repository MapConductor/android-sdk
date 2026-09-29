package com.mapconductor.vectortile

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.AttributionRule
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.resolveMapAttributions
import com.mapconductor.core.raster.RasterLayerSource
import com.mapconductor.core.raster.RasterLayerState
import com.mapconductor.core.raster.TileScheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * A style is data under someone's licence, and the credit has to survive the
 * whole way: style JSON -> Rust -> JNI -> provider -> raster layer -> the map's
 * attribution overlay.
 *
 * Worth a test of its own because this is the one thing about the layer that
 * can be wrong while nothing looks wrong. The tiles draw perfectly whether or
 * not anyone is credited for the data in them.
 */
@RunWith(AndroidJUnit4::class)
class AttributionTest {
    private val osm =
        "&copy; <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a> contributors"

    private fun styleWith(sources: String) =
        """
        {
          "version": 8,
          "sources": { $sources },
          "layers": [
            { "id": "bg", "type": "background", "paint": { "background-color": "#eee" } },
            { "id": "w", "type": "fill", "source": "osm", "source-layer": "water" }
          ]
        }
        """.trimIndent()

    private fun vectorSource(
        id: String,
        attribution: String?,
    ) = """
        "$id": {
          "type": "vector",
          "tiles": ["https://example.com/$id/{z}/{x}/{y}.pbf"],
          "minzoom": 0, "maxzoom": 14
          ${attribution?.let { ""","attribution": "${it.replace("\"", "\\\"")}"""" } ?: ""}
        }
        """

    private fun provider(styleJson: String) =
        VectorTileProvider.create(
            styleJson = styleJson,
            tileSize = 512,
            fetchTile = { ByteArray(0) },
        )

    /** The credit reaches Kotlin with its HTML intact — it is a licence link. */
    @Test
    fun carriesTheCreditOutOfTheStyle() {
        provider(styleWith(vectorSource("osm", osm))).use {
            assertEquals(listOf(osm), it.attributions())
        }
    }

    /** A style whose sources ask for nothing must not invent a credit. */
    @Test
    fun inventsNothingWhenTheStyleAsksForNothing() {
        provider(styleWith(vectorSource("osm", null))).use {
            assertTrue(it.attributions().toString(), it.attributions().isEmpty())
        }
    }

    /** A source that no drawn layer references is not this map's to credit. */
    @Test
    fun skipsASourceNothingDraws() {
        val sources = vectorSource("osm", osm) + "," + vectorSource("unused", "&copy; Nobody")
        provider(styleWith(sources)).use {
            assertEquals(listOf(osm), it.attributions())
        }
    }

    /** The bundled demo style declares none, so the layer shows none. */
    @Test
    fun theDemoStyleAsksForNothing() {
        val assets = InstrumentationRegistry.getInstrumentation().context.assets
        val demo = assets.open("demo-style.json").use { it.readBytes().decodeToString() }
        provider(demo).use {
            assertTrue(it.attributions().isEmpty())
        }
    }

    /**
     * The contract [VectorTileLayer] relies on: rules attached to the raster
     * source are what the map's overlay reads. Both halves of the layer carry
     * the same credit, and it must be printed once, not twice.
     */
    @Test
    fun theMapOverlayReadsWhatTheLayerAttaches() {
        fun layer(
            id: String,
            rules: List<AttributionRule>,
        ) = RasterLayerState(
            id = id,
            source =
                RasterLayerSource.UrlTemplate(
                    template = "http://127.0.0.1:8080/$id/{z}/{x}/{y}.png",
                    tileSize = 512,
                    maxZoom = 22,
                    attributionRules = rules,
                    scheme = TileScheme.XYZ,
                ),
        )

        val rules = listOf(AttributionRule(attribution = osm))
        val resolved =
            resolveMapAttributions(
                designRules = emptyList(),
                rasterLayers = listOf(layer("geom", rules), layer("labels", rules)),
                camera =
                    MapCameraPosition(
                        position = GeoPoint.fromLatLong(35.68, 139.76),
                        zoom = 16.0,
                        bearing = 0.0,
                        tilt = 0.0,
                        paddings = null,
                    ),
            )
        assertEquals(listOf(osm), resolved)
    }
}
