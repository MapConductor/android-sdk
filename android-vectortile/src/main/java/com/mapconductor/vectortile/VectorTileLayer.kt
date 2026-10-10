package com.mapconductor.vectortile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import com.mapconductor.compose.MapViewScope
import com.mapconductor.compose.raster.RasterLayer
import com.mapconductor.core.map.AttributionRule
import com.mapconductor.core.map.LocalMapServiceRegistry
import com.mapconductor.core.map.VectorStyleSupport
import com.mapconductor.core.map.VectorStyleSupportKey
import com.mapconductor.core.raster.RasterLayerSource
import com.mapconductor.core.raster.RasterLayerState
import com.mapconductor.core.raster.RasterTilePreferenceKey
import com.mapconductor.core.raster.TileScheme
import com.mapconductor.core.tileserver.TileServerRegistry
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Draws a MapLibre vector style on any map backend, by rendering it to raster
 * tiles on the device and serving them through the SDK's local tile server.
 *
 * The backend only ever sees an ordinary raster layer, which is what makes this
 * work on Google Maps, MapKit, HERE, ArcGIS and the rest — none of which can
 * render a vector style themselves.
 *
 * Under the hood this mounts *two* raster layers: the geometry, GPU-drawn and
 * never invalidated, and a transparent label overlay above it, CPU-drawn and
 * refreshed alone when fonts arrive. To the map and the user they read as one
 * layer. (Measured on an iPad against one merged layer: the merged tile waits
 * for its nine neighbours and its glyphs before it can show anything, and
 * first paint was three times slower; the split lets the ground land first.)
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
 *
 * With [asBasemap] on a map that can draw the style itself (MapLibre, Mapbox,
 * MapTiler -- anything registering [VectorStyleSupportKey]), none of the
 * above happens: the style document is served to the map as its own style
 * and the map's vector renderer draws it. See [VectorStyleSupport] for what
 * that means -- chiefly that the style *replaces* the basemap and [opacity]
 * does not apply. Maps without the capability take the raster path whatever
 * [asBasemap] says.
 */
@Composable
fun MapViewScope.VectorTileLayer(
    styleJson: String,
    /**
     * タイル 1 枚の一辺（dp）。null なら地図 SDK の好みに従う。
     *
     * 既定を null にしてあるのは、良い値が**載せる地図による**から。1 枚あたりの
     * 固定費は枚数に比例するので同じ画面なら大きいタイルが安く、既定は 512。
     * ただし ArcGIS の 3D SceneView のように「タイルは 256px」という前提で
     * レベルを選ぶ SDK があり、そこへ 512 を渡すと 1 段深いレベルを 4 倍の枚数で
     * 引く。プロバイダが [RasterTilePreferenceKey] で宣言していればそれに従い、
     * 何も言わなければ [VectorTileProvider.DEFAULT_TILE_SIZE]。
     *
     * 明示した値は常に優先される。
     */
    tileSize: Int? = null,
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
     * Image pixels drawn per style pixel.
     *
     * Null follows the display's own density, which is what stops a 3x screen
     * stretching every tile — neither the map SDKs nor this layer ask for
     * `@2x` tiles, so without it the tile is drawn at a third of the pixels
     * the screen will show it at. A tile covers the same ground whatever this
     * says; only its grain changes, and so does the cost: the image is this
     * squared, and the GPU readback is per pixel.
     *
     * Pass 1 to trade sharpness back for speed on a slow device.
     *
     * Matches `renderScale` on iOS.
     */
    renderScale: Int ? = null,
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
    /**
     * The style is the basemap, not a layer over one.
     *
     * On a map that renders vector styles itself this hands the style over
     * directly and mounts no raster layer at all -- the fast path, and the
     * one an offline package will take. Elsewhere it changes nothing here;
     * the app blanks the map's own basemap (a `None` design) and the opaque
     * raster tiles are the map.
     */
    asBasemap: Boolean = false,
    /**
     * A downloaded [OfflinePackage] to draw from. Source tiles, glyph ranges
     * and the sprite the package holds are read from it and never fetched;
     * anything else is fetched while [online] and refused otherwise. On the
     * direct path the map reads the package through the local tile server.
     */
    offlinePackage: OfflinePackage? = null,
    /**
     * Whether the network may be used for what [offlinePackage] lacks. Off,
     * a source the package does not hold is drawn as absent and the tile is
     * not kept, so it is drawn again once the network is back. Meaningless
     * without a package: a style with no package is always online.
     */
    online: Boolean = true,
    /** Counts of what the package answered and what it could not, as fetches happen. */
    onOfflineStats: ((OfflinePackage.Stats) -> Unit)? = null,
) {
    // Everything downstream is keyed off the group, so asking for a different
    // scale rebuilds the provider and the routes with it. Null is resolved by
    // the provider, from the display's own density.
    val groupId = remember(renderScale) { "vectortile-${UUID.randomUUID()}" }
    val tileServer = remember { TileServerRegistry.get() }

    val direct = if (asBasemap) LocalMapServiceRegistry.current.get(VectorStyleSupportKey) else null
    if (direct != null) {
        DirectVectorStyle(
            styleJson = styleJson,
            support = direct,
            tileServer = tileServer,
            onDiagnostics = onDiagnostics,
            offlinePackage = offlinePackage,
            online = online,
            headers = headers,
        )
        return
    }

    // The fetcher outlives any one fetch and is what `online` switches; the
    // stats callback is delivered on the main thread, where the caller's
    // state lives, from whichever thread fetched.
    val statsListener by rememberUpdatedState(onOfflineStats)
    val mainHandler = remember { android.os.Handler(android.os.Looper.getMainLooper()) }
    val fetcher =
        remember(offlinePackage) {
            offlinePackage?.let { pkg ->
                OfflinePackage.Fetcher(
                    pkg = pkg,
                    upstream = { url -> VectorTileProvider.httpGet(url, headers) },
                    online = online,
                    onStats = { stats -> mainHandler.post { statsListener?.invoke(stats) } },
                )
            }
        }
    LaunchedEffect(fetcher, online) { fetcher?.online = online }

    val preferred = LocalMapServiceRegistry.current.get(RasterTilePreferenceKey)?.preferredTileSize

    @Suppress("NAME_SHADOWING")
    val tileSize = tileSize ?: preferred ?: VectorTileProvider.DEFAULT_TILE_SIZE

    // Parsing a real basemap style is not free, so it happens once and off the
    // main thread; the layer simply does not mount until it is ready.
    var provider by remember { mutableStateOf<VectorTileProvider?>(null) }
    // Bumped when glyphs land. The map only refetches a raster source whose
    // URL changed, so the generation has to reach the template.
    var glyphGeneration by remember { mutableIntStateOf(0) }
    var failure by remember { mutableStateOf<String?>(null) }

    DisposableEffect(groupId, tileSize) {
        onDispose {
            tileServer.unregister("$groupId-geom")
            tileServer.unregister("$groupId-labels")
            provider?.closeAsync()
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
                        renderScale = renderScale,
                        fetchTile = fetcher,
                    )
                }
            }
        created
            .onSuccess {
                // Glyphs arrive after the tiles that need them: a range is a
                // round trip and a tile at low zoom wants dozens, so tiles are
                // drawn with whatever is loaded and refetched once more is.
                it.onGlyphsLoaded = { glyphGeneration++ }
                // Two routes from one provider: the ground and the labels are
                // served as separate tile layers stacked on each other. The
                // user sees one map; the halves render in parallel -- GPU
                // under, CPU over -- and glyphs arriving refresh only the
                // transparent overlay, never the ground.
                tileServer.register("$groupId-geom", it.geometryTiles)
                tileServer.register("$groupId-labels", it.labelTiles)
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
     * The credits the style's sources ask for, carried by the layer itself.
     *
     * The map's attribution overlay is fed from `resolveMapAttributions`,
     * which merges the design's rules with those of every visible raster
     * layer — so a credit attached here appears without the host writing any
     * UI, and disappears when the layer is unmounted. Nothing else about this
     * layer can be wrong while still looking right; the tiles draw perfectly
     * whether or not anyone is credited for them.
     *
     * No zoom or bounds narrowing on the rules. A source's `maxzoom` is where
     * its tiles stop, not where its data stops being on screen: past it the
     * renderer magnifies the deepest tile it has, so the data is still shown
     * and still has to be credited. Crediting a little too often costs a line
     * of text; crediting too rarely breaks a licence.
     */
    val credits =
        remember(current) {
            current.attributions().map { AttributionRule(attribution = it) }
        }

    /**
     * The ground: geometry only, drawn by the GPU, never invalidated by
     * glyphs. One layer, mounted once, at the bottom.
     */
    val ground =
        // `credits` is a key so that swapping the style to one with different
        // sources re-credits the map. Two styles with the same credits compare
        // equal and the layer is left alone, which is the ordinary case -- a
        // recolour keeps its data.
        remember(groupId, tileSize, credits) {
            RasterLayerState(
                id = "$groupId-geom",
                source =
                    RasterLayerSource.UrlTemplate(
                        template = tileServer.urlTemplate("$groupId-geom", tileSize, "static"),
                        tileSize = tileSize,
                        maxZoom = maxZoom,
                        attributionRules = credits,
                        scheme = TileScheme.XYZ,
                    ),
                opacity = opacity.coerceIn(0.0f, 1.0f),
                visible = visible,
                zIndex = 0,
            )
        }

    /**
     * The label overlays currently mounted: normally one, briefly two.
     *
     * Changing a raster layer's source URL is implemented as remove-then-add,
     * so the layer vanishes for as long as the new source takes its first
     * tiles. Adding the replacement alongside and dropping the old one a
     * moment later hands over instead — and because the ground is its own
     * layer underneath, the worst a handover can now cost is a moment of
     * doubled labels, never a bare map.
     */
    fun labelStateFor(generation: Int) =
        RasterLayerState(
            id = "$groupId-labels-g$generation",
            source =
                RasterLayerSource.UrlTemplate(
                    template = tileServer.urlTemplate("$groupId-labels", tileSize, "g$generation"),
                    tileSize = tileSize,
                    maxZoom = maxZoom,
                    // Both halves carry the credit. `resolveMapAttributions`
                    // ends in `distinct()`, so it is printed once; putting it
                    // on both means hiding either half cannot silence it.
                    attributionRules = credits,
                    scheme = TileScheme.XYZ,
                ),
            opacity = opacity.coerceIn(0.0f, 1.0f),
            visible = visible,
            // Above the ground, and above the generation it replaces.
            zIndex = 1000 + generation,
        )

    val mounted =
        remember(groupId, tileSize) {
            mutableStateListOf(labelStateFor(0))
        }

    LaunchedEffect(glyphGeneration) {
        if (glyphGeneration == 0) return@LaunchedEffect
        mounted.add(labelStateFor(glyphGeneration))
        // Long enough for the replacement's tiles to arrive. There is no
        // per-source "loaded" signal to wait on, and holding the old layer a
        // little too long only costs one extra layer for that moment.
        kotlinx.coroutines.delay(HANDOVER_MS)
        while (mounted.size > 1) {
            mounted.removeAt(0)
        }
    }

    LaunchedEffect(opacity, visible) {
        ground.opacity = opacity.coerceIn(0.0f, 1.0f)
        ground.visible = visible
        mounted.forEach {
            it.opacity = opacity.coerceIn(0.0f, 1.0f)
            it.visible = visible
        }
    }

    // Referencing `current` keeps the provider alive for as long as the layer is
    // mounted, and makes the dependency explicit rather than incidental.
    LaunchedEffect(current) { }

    RasterLayer(state = ground)
    mounted.forEach { RasterLayer(state = it) }
}

/**
 * The direct path: the style is served as a document and the map draws it.
 *
 * Two effects rather than one so that a style change does not bounce the map
 * through the previous design on its way to the new style: the document
 * (keyed by content) is swapped, the support is told once more, and only a
 * real unmount clears it.
 */
@Composable
private fun DirectVectorStyle(
    styleJson: String,
    support: VectorStyleSupport,
    tileServer: com.mapconductor.core.tileserver.LocalTileServer,
    onDiagnostics: ((List<String>) -> Unit)?,
    offlinePackage: OfflinePackage?,
    online: Boolean,
    headers: Map<String, String>,
) {
    // With a package, the map reads its tiles, glyphs and sprite from the
    // local server, which answers from the package and -- online -- fetches
    // upstream for the rest. The route carries the mode: a map remembers a
    // tile it was told does not exist, so going back online has to change
    // the URLs to be noticed.
    val filesRoute =
        remember(offlinePackage, online) {
            offlinePackage?.let { "vectortile-package-${it.manifest.styleDigest.take(12)}-${if (online) "online" else "offline"}" }
        }
    DisposableEffect(filesRoute, offlinePackage) {
        if (filesRoute != null && offlinePackage != null) {
            tileServer.registerFiles(
                routeId = filesRoute,
                directory = offlinePackage.directory,
                fallback =
                    if (online) {
                        { relative -> offlinePackage.upstreamUrl(relative)?.let { VectorTileProvider.httpGet(it, headers) } }
                    } else {
                        null
                    },
            )
        }
        onDispose { if (filesRoute != null) tileServer.unregisterFiles(filesRoute) }
    }
    val served =
        remember(styleJson, filesRoute) {
            if (filesRoute != null && offlinePackage != null) offlinePackage.styleServedBy(tileServer.filesUrl(filesRoute)) else styleJson
        }

    // The id is the content and nothing else -- not the layer instance. The
    // map only re-reads a style whose URL changed, so a changed style must
    // change the URL; but a provider that rebuilds its view on a design
    // change (MapTiler) remounts this layer with it, and if the remount
    // minted a fresh URL it would be a fresh design, another rebuild, and
    // so on -- the map never comes up. Same content, same URL, and the
    // remount is a no-op. Two layers showing one style share the document,
    // which is harmless: one basemap is all a map has.
    val documentId = remember(served) { "vectortile-style-${served.hashCode().toUInt()}" }

    DisposableEffect(support) {
        onDispose { support.clearStyle() }
    }

    DisposableEffect(documentId, support) {
        tileServer.registerDocument(documentId, "application/json", served.toByteArray())
        support.showStyle(
            styleUrl = tileServer.documentUrl(documentId),
            attributionRules = styleAttributions(styleJson).map { AttributionRule(attribution = it) },
        )
        onDiagnostics?.invoke(
            listOf(
                if (offlinePackage != null) {
                    "direct: the map reads the package itself (${if (online) "online" else "offline"})"
                } else {
                    "direct: the map draws the style itself"
                },
            ),
        )
        onDispose { tileServer.unregisterDocument(documentId) }
    }
}

/**
 * The `attribution` of every source in a style, in style order, without
 * duplicates. A style that will not parse credits nothing here -- the map
 * will refuse it anyway, more visibly.
 */
private fun styleAttributions(styleJson: String): List<String> =
    runCatching {
        val sources = JSONObject(styleJson).optJSONObject("sources") ?: return emptyList()
        sources
            .keys()
            .asSequence()
            .mapNotNull { sources.optJSONObject(it)?.optString("attribution")?.takeIf { a -> a.isNotBlank() } }
            .distinct()
            .toList()
    }.getOrDefault(emptyList())

/**
 * How long the layer being replaced stays up.
 *
 * No signal says a raster source has drawn its first tiles, so this is a
 * window rather than a wait. Too short brings the flash back; too long leaves
 * two layers stacked, which costs a moment of overdraw and nothing else.
 */
private const val HANDOVER_MS = 900L
