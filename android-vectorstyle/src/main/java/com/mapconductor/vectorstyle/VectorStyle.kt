package com.mapconductor.vectorstyle

import com.mapconductor.core.map.AttributionRule
import com.mapconductor.core.map.MapStyleHost
import com.mapconductor.core.map.MapStyleInstallation
import com.mapconductor.core.map.MapViewStyle
import com.mapconductor.core.map.VectorStyleMutationSupportKey
import com.mapconductor.core.map.VectorStyleSupportKey
import java.net.HttpURLConnection
import java.net.URL
import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import android.util.Log

/**
 * A vector style, with adjustments, as the map's appearance.
 *
 * ```kotlin
 * MapLibreMapView(
 *     state = state,
 *     style = VectorStyle(
 *         rules = StyleRules.build {
 *             all { color = Color.Black }
 *             role(LayerRole.LABEL) { visible = false }
 *             role(LayerRole.ROAD) { color = Color.White }
 *         },
 *     ),
 * )
 * ```
 *
 * What happens underneath depends on the backend, and the app does not have
 * to know:
 *
 * - **A map that reads vector styles** (MapLibre, Mapbox, MapTiler) is given
 *   the document and told the per-layer differences. Changing the rules
 *   later sends new differences and **reloads nothing** — which is the whole
 *   reason the adjustments are compiled into deltas as well as a document.
 * - **A map that cannot** (Google Maps, MapKit, HERE, ArcGIS...) is handed
 *   raster tiles drawn from the adjusted document, if the app passed a
 *   [rasteriser]. Changing the rules re-rasterises and refetches nothing.
 *
 * Either way the same rules produce the same map, because both paths start
 * from one compilation of one style.
 *
 * ## What it reports
 *
 * Quietly doing nothing is the failure this design is built against, so
 * [onDiagnostics] always hears how many layers each rule matched, which
 * matched none, what this backend would not take, and what the rasteriser
 * will not draw. A rule written for the wrong tile schema produces a
 * perfectly good map with nothing changed on it; nothing else says so.
 */
class VectorStyle(
    /** Which style to adjust. Defaults to whatever the map is already drawing. */
    val document: VectorStyleSource = VectorStyleSource.CurrentDesign,
    val rules: StyleRules = StyleRules.NONE,
    /**
     * Draws the style for a backend that cannot read one.
     *
     * `com.mapconductor:vectortile` supplies one. Left out, a map without
     * vector style support is told so in the diagnostics rather than left
     * blank — there is nothing sensible this can do on its own.
     *
     * **Hold it, do not rebuild it.** Two styles count as the same one only
     * if they name the same rasteriser *instance*, so constructing it inline
     * makes every recomposition a different style — the map tears the old
     * one down and installs a new one each time, and the result is tiles
     * fetched forever with nothing drawn. `remember { VectorTileRasteriser() }`.
     */
    val rasteriser: VectorStyleRasteriser? = null,
    val onUnsupported: UnsupportedPolicy = UnsupportedPolicy.REPORT,
    val onDiagnostics: ((List<String>) -> Unit)? = null,
) : MapViewStyle {
    override val key: String = "${document.key}|${rules.json.hashCode().toUInt()}"

    /** Whether [next] differs from this only in its adjustments. */
    internal fun sameStyleAs(next: VectorStyle): Boolean =
        next.document == document &&
            next.rasteriser === rasteriser &&
            next.onUnsupported == onUnsupported

    override fun install(host: MapStyleHost): MapStyleInstallation = Installation(this, host).also { it.start() }

    /**
     * One style's life on one map.
     *
     * Resolving the document can mean a network round trip, so the work
     * starts on a worker and this is handed back at once; everything after
     * that checks [disposed] before touching the map, because a view can be
     * gone before its style arrives.
     */
    private class Installation(
        private var style: VectorStyle,
        private val host: MapStyleHost,
    ) : MapStyleInstallation {
        private val disposed = AtomicBoolean(false)

        /**
         * The id the document is served under, named after its content, or
         * null while nothing is served.
         *
         * **Not a fresh id per installation**, which is the shape this used
         * to have and which deadlocks a provider that rebuilds its view on a
         * design change. MapTiler on Android keys its whole view on the
         * design id: serving a document sets the design, that rebuilds the
         * view, the rebuild reinstalls the style, a new id gives a new URL,
         * which is a new design -- and the map never finishes coming up.
         *
         * Content-derived, the second install serves the same URL and
         * `showStyle` sees the design it already set and does nothing. It
         * also makes the other half right: when the adjusted document *is*
         * what the map gets (a vector renderer that cannot be patched), a
         * rules change produces different content, so the URL changes and the
         * map reloads -- which under one fixed id it would not have done.
         */
        private var servedId: String? = null

        /** The style as its author wrote it, kept so later rules compile against it. */
        private var base: String? = null
        private var rasterisation: VectorStyleRasterisation? = null
        private var styleLoaded: MapStyleInstallation? = null

        fun start() {
            workers.execute {
                val base =
                    runCatching { resolve(style.document) }
                        .getOrElse { error ->
                            report(listOf("the style could not be read: ${error.message}"))
                            return@execute
                        }
                if (disposed.get()) return@execute
                this.base = base
                apply(style, firstTime = true)
            }
        }

        override fun update(next: com.mapconductor.core.map.MapViewStyle): Boolean {
            val next = next as? VectorStyle ?: return false
            // A different document is a different style, whatever else
            // matches: it has to be fetched and the map has to be told.
            if (!style.sameStyleAs(next)) return false
            val base = base ?: return false
            style = next
            workers.execute { if (!disposed.get()) apply(next, firstTime = false) }
            return true
        }

        override fun dispose() {
            if (!disposed.compareAndSet(false, true)) return
            styleLoaded?.dispose()
            styleLoaded = null
            // Order matters: the mutations have to come off while the
            // document they were applied to is still the one loaded.
            host.serviceRegistry.get(VectorStyleMutationSupportKey)?.clear()
            servedId?.let { id ->
                host.serviceRegistry.get(VectorStyleSupportKey)?.clearStyle()
                host.tileServer.unregisterDocument(id)
                servedId = null
            }
            rasterisation?.dispose()
            rasterisation = null
        }

        /** Compiles on a worker, then routes on the main thread. */
        private fun apply(
            style: VectorStyle,
            firstTime: Boolean,
        ) {
            val base = base ?: return
            val compiled =
                runCatching { VectorStyleRules.compile(base, style.rules.json) }
                    .getOrElse { error ->
                        report(listOf("the rules could not be applied: ${error.message}"))
                        return
                    }
            if (disposed.get()) return
            report(compiled.diagnostics)
            onMain { route(style, compiled, firstTime, base) }
        }

        /**
         * Hands the compiled result to the map. **Main thread only.**
         *
         * Every branch below touches the renderer, and a renderer is a UI
         * object on all three platforms. MapLibre Android says so out loud --
         * `CalledFromWorkerThreadException: Method invoked from wrong thread
         * is getLayer` -- and because `applyStyleMutations` catches what a
         * mutation throws, all 506 of them came back as adjustments this map
         * "cannot" make. The map still looked right, because the provider
         * puts the whole set on again from the style-loaded callback, which
         * does run here. So the symptom was a diagnostic that contradicted
         * the picture, which is the hardest kind to believe.
         */
        private fun route(
            style: VectorStyle,
            compiled: CompiledStyle,
            firstTime: Boolean,
            base: String,
        ) {
            if (disposed.get()) return
            val registry = host.serviceRegistry
            val vector = registry.get(VectorStyleSupportKey)
            val mutation = registry.get(VectorStyleMutationSupportKey)

            when {
                // The map draws styles itself and can be told differences:
                // give it the author's document and the deltas on top. The
                // compiled document is deliberately *not* what it is given --
                // the next rule change produces deltas against the original,
                // and a map holding an already-adjusted document would take
                // them to mean something else.
                vector != null && mutation != null -> {
                    if (firstTime) serveDocument(vector, base)
                    val unapplied = mutation.apply(compiled.mutations)
                    if (unapplied.isNotEmpty()) handleUnapplied(unapplied, vector, compiled)
                }
                // It draws styles but cannot be patched: the adjusted
                // document is the only way, and every rule change reloads.
                vector != null -> serveDocument(vector, compiled.styleJson)
                // It cannot read a style at all.
                style.rasteriser != null -> {
                    val current = rasterisation
                    if (current == null) {
                        rasterisation = style.rasteriser.install(host, compiled.styleJson, compiled.affects)
                    } else {
                        current.restyle(compiled.styleJson, compiled.affects)
                    }
                }
                else ->
                    report(
                        listOf(
                            "this map cannot draw a vector style, and no rasteriser was given; " +
                                "pass `rasteriser = VectorTileRasteriser()` from com.mapconductor:vectortile",
                        ),
                    )
            }

            // Whatever was just applied is lost the moment the map loads a
            // style again -- an app switching basemap, a provider rebuilding.
            // The mutation store puts its own set back; the document has to
            // be re-served here.
            if (firstTime && styleLoaded == null) {
                styleLoaded = host.onStyleLoaded { if (!disposed.get()) onStyleReloaded() }
            }
        }

        private fun onStyleReloaded() {
            // The store re-applies the mutations by itself. Nothing else to
            // do unless this was the side that served the document, in which
            // case the map has just loaded something else and the style is
            // no longer installed -- the app chose that, so it stands.
            if (servedId != null) {
                Log.d(TAG, "the map loaded another style; the adjustments went with it")
            }
        }

        private fun serveDocument(
            vector: com.mapconductor.core.map.VectorStyleSupport,
            json: String,
        ) {
            val id = "vectorstyle-${contentId(json)}"
            val previous = servedId
            host.tileServer.registerDocument(id, "application/json", json.toByteArray())
            servedId = id
            vector.showStyle(
                styleUrl = host.tileServer.documentUrl(id),
                attributionRules = attributionsOf(json).map { AttributionRule(attribution = it) },
            )
            // The one it replaces, dropped only after the new one is up so
            // the map is never pointed at an id that is no longer registered.
            if (previous != null && previous != id) host.tileServer.unregisterDocument(previous)
        }

        private fun handleUnapplied(
            unapplied: List<com.mapconductor.core.map.StyleMutation>,
            vector: com.mapconductor.core.map.VectorStyleSupport,
            compiled: CompiledStyle,
        ) {
            // Named by property, not by layer. A backend with no property
            // for a spec key fails that key on every layer that uses it, so
            // a list of layers was hundreds of names for one cause -- and
            // never said which property, which is the only part an app can
            // act on.
            val properties = unapplied.map { it.propertyName }.distinct().sorted()
            val shown = properties.take(5)
            val more = if (properties.size > shown.size) ", ..." else ""
            val layers = unapplied.mapNotNull { it.layerId }.distinct().size
            val message =
                "${unapplied.size} adjustments were not applied: this map cannot set these " +
                    "properties by name (${shown.joinToString(", ")}$more) across $layers layers"
            when (style.onUnsupported) {
                UnsupportedPolicy.REPORT -> report(listOf(message))
                UnsupportedPolicy.RELOAD -> {
                    report(listOf("$message; handing the map the adjusted document instead"))
                    host.serviceRegistry.get(VectorStyleMutationSupportKey)?.clear()
                    serveDocument(vector, compiled.styleJson)
                }
            }
        }

        private fun resolve(source: VectorStyleSource): String =
            when (source) {
                is VectorStyleSource.Text -> source.json
                is VectorStyleSource.Url -> fetch(source.url, source.headers)
                VectorStyleSource.CurrentDesign -> {
                    val url =
                        host.vectorStyleUrl
                            ?: throw IllegalStateException(
                                "this map is not drawing a vector style, so there is nothing to adjust; " +
                                    "give `document` a style of your own",
                            )
                    fetch(url, emptyMap())
                }
            }

        private fun report(diagnostics: List<String>) {
            if (diagnostics.isEmpty()) return
            host.report(diagnostics)
            style.onDiagnostics?.invoke(diagnostics)
        }

        private companion object {
            const val TAG = "VectorStyle"

            /**
             * A short, stable name for a document's content.
             *
             * FNV-1a rather than the platform's digest API: this only has to
             * name content inside one process, and the three platforms carry
             * the same ten lines instead of three different hashing APIs.
             */
            fun contentId(text: String): String {
                var hash = -0x340d631b7bdddcdbL // FNV-1a 64-bit offset basis
                for (byte in text.toByteArray()) {
                    hash = hash xor (byte.toLong() and 0xff)
                    hash *= 0x100000001b3L // FNV prime
                }
                return hash.toULong().toString(16)
            }

            /**
             * Where a style is fetched and compiled.
             *
             * Daemon threads, and few: a map has one style, and the work is
             * a round trip plus a parse. Shared across installations so a
             * view churning through styles does not churn through threads.
             */
            val workers =
                Executors.newFixedThreadPool(2) { runnable ->
                    Thread(runnable, "mc-vectorstyle").apply { isDaemon = true }
                }

            private val main = Handler(Looper.getMainLooper())

            /** Runs now when already on the main thread, and posts otherwise. */
            private fun onMain(block: () -> Unit) {
                if (Looper.myLooper() == Looper.getMainLooper()) block() else main.post(block)
            }

            fun fetch(
                url: String,
                headers: Map<String, String>,
            ): String {
                val connection = URL(url).openConnection() as HttpURLConnection
                return try {
                    connection.connectTimeout = 15_000
                    connection.readTimeout = 15_000
                    headers.forEach(connection::setRequestProperty)
                    if (connection.responseCode !in 200..299) {
                        throw IllegalStateException("HTTP ${connection.responseCode} for $url")
                    }
                    connection.inputStream.use { it.readBytes().decodeToString() }
                } finally {
                    connection.disconnect()
                }
            }

            /**
             * The credits the style's sources ask for.
             *
             * Carried onto the design so the map's attribution overlay shows
             * them for as long as the style is up. Nothing else about this
             * can be wrong while still looking right: the map draws
             * perfectly whether or not anyone is credited for the data.
             */
            fun attributionsOf(json: String): List<String> =
                runCatching {
                    val sources = org.json.JSONObject(json).optJSONObject("sources") ?: return emptyList()
                    sources
                        .keys()
                        .asSequence()
                        .mapNotNull { sources.optJSONObject(it)?.optString("attribution") }
                        .filter { it.isNotEmpty() }
                        .distinct()
                        .toList()
                }.getOrDefault(emptyList())
        }
    }
}
