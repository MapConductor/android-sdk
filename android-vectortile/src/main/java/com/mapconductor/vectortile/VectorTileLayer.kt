package com.mapconductor.vectortile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.mapconductor.compose.MapViewScope
import com.mapconductor.compose.raster.RasterLayer
import com.mapconductor.core.raster.RasterLayerSource
import com.mapconductor.core.raster.RasterLayerState
import com.mapconductor.core.raster.TileScheme
import com.mapconductor.core.tileserver.TileServerRegistry
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Draws a MapLibre vector style on any map backend, by rendering it to raster
 * tiles on the device and serving them through the SDK's local tile server.
 *
 * The backend only ever sees an ordinary raster layer, which is what makes this
 * work on Google Maps, MapKit, HERE, ArcGIS and the rest — none of which can
 * render a vector style themselves.
 *
 * Symbol layers are not drawn; [onDiagnostics] reports that and anything else
 * about the style worth knowing.
 *
 * Pass [diskCacheDir] to keep rendered tiles across app launches. It does not
 * make the first view faster — the tiles still have to be fetched and
 * rasterised once — but on a Pixel 5a a second launch served all seven visible
 * tiles from disk with zero renders, against ~1 s of fetch plus ~1 s of
 * rasterising each on the first.
 *
 * **The host app must permit cleartext traffic to loopback.** Tiles are served
 * by the SDK's local tile server over plain HTTP on 127.0.0.1, and without a
 * network security config allowing it the layer simply stays blank:
 *
 * ```xml
 * <!-- res/xml/network_security_config.xml -->
 * <network-security-config>
 *     <domain-config cleartextTrafficPermitted="true">
 *         <domain includeSubdomains="false">127.0.0.1</domain>
 *         <domain includeSubdomains="false">localhost</domain>
 *     </domain-config>
 * </network-security-config>
 * ```
 *
 * This is not specific to this module — every tile-server-backed layer in the
 * SDK needs it, which is why `example-app` already carries one.
 *
 * ```kotlin
 * VectorTileLayer(styleJson = style, opacity = 0.9f)
 * ```
 */
@Composable
fun MapViewScope.VectorTileLayer(
    styleJson: String,
    tileSize: Int = VectorTileProvider.DEFAULT_TILE_SIZE,
    opacity: Float = 1.0f,
    visible: Boolean = true,
    maxZoom: Int = 22,
    headers: Map<String, String> = emptyMap(),
    /**
     * Where to rasterise. The default probes for an OpenGL ES context and
     * falls back to the CPU when there is none.
     *
     * The GPU mode matters less for raw speed than for *which* processor pays:
     * on a mid-range device it is roughly 3x faster, but more to the point it
     * takes the drawing off the CPU that the map SDK and the app are already
     * competing for.
     */
    renderMode: VectorTileProvider.RenderMode = VectorTileProvider.RenderMode.AUTO,
    /**
     * Where to keep rendered tiles across app launches, e.g.
     * `context.cacheDir.resolve("vectortile")`. Null disables it.
     *
     * Deliberately the caller's choice rather than something this module digs
     * out of a `Context`: apps have their own opinions about cache location and
     * lifetime, and a library helping itself to disk behind their back is not
     * a favour. Within a session it changes little — the map SDK already caches
     * raster tiles — but it removes re-rendering everything on the next launch.
     */
    diskCacheDir: File? = null,
    onDiagnostics: ((List<String>) -> Unit)? = null,
) {
    val groupId = remember { "vectortile-${UUID.randomUUID()}" }
    val tileServer = remember { TileServerRegistry.get() }

    // Parsing a real basemap style is not free, so it happens once and off the
    // main thread; the layer simply does not mount until it is ready.
    var provider by remember { mutableStateOf<VectorTileProvider?>(null) }
    // Bumped when glyphs land. The map only refetches a raster source whose
    // URL changed, so the generation has to reach the template.
    var glyphGeneration by remember { mutableIntStateOf(0) }
    var failure by remember { mutableStateOf<String?>(null) }

    DisposableEffect(groupId, tileSize) {
        onDispose {
            tileServer.unregister(groupId)
            provider?.close()
            provider = null
        }
    }

    LaunchedEffect(groupId, styleJson, tileSize) {
        val created =
            withContext(Dispatchers.Default) {
                runCatching {
                    VectorTileProvider.create(
                        styleJson = styleJson,
                        tileSize = tileSize,
                        headers = headers,
                        diskCacheDir = diskCacheDir,
                        renderMode = renderMode,
                    )
                }
            }
        created
            .onSuccess {
                // Glyphs arrive after the tiles that need them: a range is a
                // round trip and a tile at low zoom wants dozens, so tiles are
                // drawn with whatever is loaded and refetched once more is.
                it.onGlyphsLoaded = { glyphGeneration++ }
                tileServer.register(groupId, it)
                provider = it
                onDiagnostics?.invoke(it.diagnostics())
            }.onFailure {
                failure = it.message
                onDiagnostics?.invoke(listOf("style could not be loaded: ${it.message}"))
            }
    }

    val current = provider ?: return
    if (failure != null) return

    /**
     * The raster layers currently mounted: normally one, briefly two.
     *
     * Changing a raster layer's source URL is implemented as remove-then-add,
     * so the layer vanishes for as long as the new source takes to fetch its
     * first tiles — the flash after labels arrived. Adding the replacement
     * alongside and dropping the old one a moment later hands over instead:
     * the new tiles are opaque, so they cover the old ones as they land, and
     * there is never a frame with nothing on it.
     */
    val mounted =
        remember(groupId, tileSize) {
            mutableStateListOf(
                RasterLayerState(
                    id = "$groupId-g0",
                    source =
                        RasterLayerSource.UrlTemplate(
                            template = tileServer.urlTemplate(groupId, tileSize, "g0"),
                            tileSize = tileSize,
                            maxZoom = maxZoom,
                            scheme = TileScheme.XYZ,
                        ),
                    opacity = opacity.coerceIn(0.0f, 1.0f),
                    visible = visible,
                ),
            )
        }

    fun layerStateFor(generation: Int) =
        RasterLayerState(
            id = "$groupId-g$generation",
            source =
                RasterLayerSource.UrlTemplate(
                    template = tileServer.urlTemplate(groupId, tileSize, "g$generation"),
                    tileSize = tileSize,
                    maxZoom = maxZoom,
                    scheme = TileScheme.XYZ,
                ),
            opacity = opacity.coerceIn(0.0f, 1.0f),
            visible = visible,
        )

    LaunchedEffect(glyphGeneration) {
        if (glyphGeneration == 0) return@LaunchedEffect
        mounted.add(layerStateFor(glyphGeneration))
        // Long enough for the replacement's tiles to arrive. There is no
        // per-source "loaded" signal to wait on, and holding the old layer a
        // little too long only costs one extra layer for that moment.
        kotlinx.coroutines.delay(HANDOVER_MS)
        while (mounted.size > 1) {
            mounted.removeAt(0)
        }
    }

    LaunchedEffect(opacity, visible) {
        mounted.forEach {
            it.opacity = opacity.coerceIn(0.0f, 1.0f)
            it.visible = visible
        }
    }

    // Referencing `current` keeps the provider alive for as long as the layer is
    // mounted, and makes the dependency explicit rather than incidental.
    LaunchedEffect(current) { }

    mounted.forEach { RasterLayer(state = it) }
}

/**
 * How long the layer being replaced stays up.
 *
 * No signal says a raster source has drawn its first tiles, so this is a
 * window rather than a wait. Too short brings the flash back; too long leaves
 * two layers stacked, which costs a moment of overdraw and nothing else.
 */
private const val HANDOVER_MS = 900L
