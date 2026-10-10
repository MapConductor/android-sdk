package com.mapconductor.vectortile

import com.mapconductor.core.map.AttributionRule
import com.mapconductor.core.map.MapStyleHost
import com.mapconductor.core.raster.RasterLayerSource
import com.mapconductor.core.raster.RasterLayerState
import com.mapconductor.core.raster.RasterTilePreferenceKey
import com.mapconductor.core.raster.TileScheme
import com.mapconductor.vectorstyle.StyleAffects
import com.mapconductor.vectorstyle.VectorStyleRasterisation
import com.mapconductor.vectorstyle.VectorStyleRasteriser
import java.io.File
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import android.os.Handler
import android.os.Looper

/**
 * Draws a vector style for a map that cannot read one.
 *
 * ```kotlin
 * GoogleMapView(
 *     state = state,
 *     style = VectorStyle(
 *         document = VectorStyleSource.Url("https://…/style.json"),
 *         rules = rules,
 *         rasteriser = VectorTileRasteriser(),
 *     ),
 * )
 * ```
 *
 * Google Maps, MapKit, HERE, ArcGIS, Longdo, TomTom and the rest have no
 * vector renderer to hand a style to. This renders the style to PNG tiles on
 * the device and mounts them as an ordinary raster layer, so the same rules
 * produce the same map on a backend that has never heard of MapLibre.
 *
 * A separate class from [VectorTileLayer] rather than the same code, because
 * the two are mounted from different places and only one of them is a
 * composable: the layer is content the app declares, this is a property of
 * the view installed from outside it. What they share — the provider, the
 * two-route split, the tile server — is [VectorTileProvider].
 *
 * ## Why two layers
 *
 * The ground (geometry, drawn by the GPU) and the labels (glyphs, drawn on
 * the CPU) are separate raster layers stacked on each other, for the reason
 * [VectorTileLayer] gives: they render in parallel, and glyphs arriving late
 * refresh only the transparent overlay rather than redrawing the map.
 */
class VectorTileRasteriser(
    /**
     * Tile size in pixels, or null to take the map's own preference.
     *
     * Null is almost always right: a provider that wants 256 says so through
     * [RasterTilePreferenceKey], and ArcGIS's 3D view is the one that does.
     */
    private val tileSize: Int? = null,
    private val headers: Map<String, String> = emptyMap(),
    private val diskCacheDir: File? = null,
    private val renderMode: VectorTileProvider.RenderMode = VectorTileProvider.RenderMode.AUTO,
    private val renderScale: Int? = null,
) : VectorStyleRasteriser {
    override fun install(
        host: MapStyleHost,
        styleJson: String,
        affects: StyleAffects,
    ): VectorStyleRasterisation = Rasterisation(host, this).also { it.start(styleJson) }

    private class Rasterisation(
        private val host: MapStyleHost,
        private val options: VectorTileRasteriser,
    ) : VectorStyleRasterisation {
        private val groupId = "vectorstyle-raster-${UUID.randomUUID()}"
        private val disposed = AtomicBoolean(false)
        private val main = Handler(Looper.getMainLooper())

        /**
         * Bumped whenever the pixels change for the same geometry.
         *
         * A map only refetches a raster source whose URL changed, so a
         * restyle has to reach the template. The ground and the labels are
         * counted apart: recolouring roads need not redraw the labels, and
         * `affects` is what says which moved.
         */
        private var groundGeneration = 0
        private var labelGeneration = 0

        private var provider: VectorTileProvider? = null
        private var credits: List<AttributionRule> = emptyList()

        /** Resolved once, when the first provider is built; the routes are keyed on it. */
        private var tileSize = VectorTileProvider.DEFAULT_TILE_SIZE

        fun start(styleJson: String) {
            workers.execute {
                tileSize =
                    options.tileSize
                        ?: host.serviceRegistry.get(RasterTilePreferenceKey)?.preferredTileSize
                        ?: VectorTileProvider.DEFAULT_TILE_SIZE
                val created = runCatching { build(styleJson) }
                if (disposed.get()) {
                    created.getOrNull()?.closeAsync()
                    return@execute
                }
                created
                    .onSuccess { ready(it) }
                    .onFailure {
                        host.report(listOf("the style could not be rasterised: ${it.message}"))
                    }
            }
        }

        private fun build(styleJson: String): VectorTileProvider =
            VectorTileProvider.create(
                styleJson = styleJson,
                tileSize = tileSize,
                headers = options.headers,
                diskCacheDir = options.diskCacheDir,
                renderMode = options.renderMode,
                renderScale = options.renderScale,
            )

        private fun ready(created: VectorTileProvider) {
            // Glyphs arrive after the tiles that need them -- a range is a
            // round trip and a low zoom wants dozens -- so tiles are drawn
            // with what is loaded and the labels refetched once more is.
            created.onGlyphsLoaded = {
                if (!disposed.get()) {
                    labelGeneration++
                    main.post { if (!disposed.get()) mountLabels() }
                }
            }
            host.tileServer.register("$groupId-geom", created.geometryTiles)
            host.tileServer.register("$groupId-labels", created.labelTiles)
            provider = created
            credits = created.attributions().map { AttributionRule(attribution = it) }
            host.report(created.diagnostics())
            main.post {
                if (disposed.get()) return@post
                mountGround()
                mountLabels()
            }
        }

        override fun restyle(
            styleJson: String,
            affects: StyleAffects,
        ) {
            if (affects == StyleAffects.NONE) return
            workers.execute {
                if (disposed.get()) return@execute
                // A new provider rather than a style swap: the renderer
                // holds the parsed document, and nothing in it is meant to
                // be mutated under the tiles it is already drawing. The
                // *source* tiles are what cost a network, and those live in
                // a cache the new provider shares -- so this is a
                // re-rasterise and no refetch, which is the promise.
                val rebuilt =
                    runCatching { build(styleJson) }.getOrElse { error ->
                        host.report(listOf("the style could not be redrawn: ${error.message}"))
                        return@execute
                    }
                if (disposed.get()) {
                    rebuilt.closeAsync()
                    return@execute
                }
                val previous = provider
                rebuilt.onGlyphsLoaded = {
                    if (!disposed.get()) {
                        labelGeneration++
                        main.post { if (!disposed.get()) mountLabels() }
                    }
                }
                // Re-registering a route replaces it, so the map never asks
                // for an id that is not served.
                host.tileServer.register("$groupId-geom", rebuilt.geometryTiles)
                host.tileServer.register("$groupId-labels", rebuilt.labelTiles)
                provider = rebuilt
                credits = rebuilt.attributions().map { AttributionRule(attribution = it) }
                previous?.closeAsync()
                // Only the half that moved. A recolour of the roads leaves
                // every label tile valid, and redrawing them would cost the
                // whole screen for nothing.
                if (affects == StyleAffects.GROUND || affects == StyleAffects.BOTH) groundGeneration++
                if (affects == StyleAffects.LABELS || affects == StyleAffects.BOTH) labelGeneration++
                main.post {
                    if (disposed.get()) return@post
                    if (affects == StyleAffects.GROUND || affects == StyleAffects.BOTH) mountGround()
                    if (affects == StyleAffects.LABELS || affects == StyleAffects.BOTH) mountLabels()
                }
            }
        }

        override fun dispose() {
            if (!disposed.compareAndSet(false, true)) return
            main.post {
                host.removeRaster("$groupId-geom")
                host.removeRaster("$groupId-labels")
            }
            host.tileServer.unregister("$groupId-geom")
            host.tileServer.unregister("$groupId-labels")
            provider?.closeAsync()
            provider = null
        }

        /** Geometry only, drawn by the GPU, never invalidated by glyphs. */
        private fun mountGround() {
            host.upsertRaster(
                RasterLayerState(
                    id = "$groupId-geom",
                    source =
                        RasterLayerSource.UrlTemplate(
                            template = host.tileServer.urlTemplate("$groupId-geom", tileSize, "g$groundGeneration"),
                            tileSize = tileSize,
                            maxZoom = MAX_ZOOM,
                            attributionRules = credits,
                            scheme = TileScheme.XYZ,
                        ),
                    visible = true,
                    // Under everything the app declared, and under the
                    // labels. A style *is* the basemap here.
                    zIndex = -2,
                ),
            )
        }

        /** The transparent overlay the glyphs land on. */
        private fun mountLabels() {
            host.upsertRaster(
                RasterLayerState(
                    id = "$groupId-labels",
                    source =
                        RasterLayerSource.UrlTemplate(
                            template = host.tileServer.urlTemplate("$groupId-labels", tileSize, "g$labelGeneration"),
                            tileSize = tileSize,
                            maxZoom = MAX_ZOOM,
                            // Both halves carry the credit, so hiding either
                            // one cannot silence it. `resolveMapAttributions`
                            // ends in `distinct()`.
                            attributionRules = credits,
                            scheme = TileScheme.XYZ,
                        ),
                    visible = true,
                    zIndex = -1,
                ),
            )
        }

        private companion object {
            const val MAX_ZOOM = 22

            /**
             * Where a style is parsed and redrawn. Daemon threads and few: a
             * map has one style, and the work is a parse plus a re-rasterise.
             */
            val workers =
                Executors.newFixedThreadPool(2) { runnable ->
                    Thread(runnable, "mapconductor-vectorstyle-raster").apply { isDaemon = true }
                }
        }
    }
}
