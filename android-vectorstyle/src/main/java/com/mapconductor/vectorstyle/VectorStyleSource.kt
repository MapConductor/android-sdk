package com.mapconductor.vectorstyle

import com.mapconductor.core.map.MapStyleHost

/**
 * Which style the rules are applied to.
 *
 * A rule cannot be matched against anything until the style document is in
 * hand: "roads" means whichever layers *this* style draws roads with, and
 * only the document says. So every path here ends in text, and the one that
 * has to go and get it says so.
 */
sealed interface VectorStyleSource {
    /** What identifies this source, for deciding whether a style changed. */
    val key: String

    /** The document itself. The app already has it. */
    data class Text(
        val json: String,
    ) : VectorStyleSource {
        override val key: String get() = "text:${json.hashCode().toUInt()}"
    }

    /**
     * A `style.json` to fetch.
     *
     * Fetched by this module rather than by the map, because the rules have
     * to be compiled against it before the map is given anything.
     */
    data class Url(
        val url: String,
        /** Sent with the request — an API key, usually. */
        val headers: Map<String, String> = emptyMap(),
    ) : VectorStyleSource {
        override val key: String get() = "url:$url"
    }

    /**
     * Whatever the map is already drawing.
     *
     * For adjusting a basemap the app did not supply: a MapLibre, Mapbox or
     * MapTiler design. The module reads the design's style URL from
     * [MapStyleHost.vectorStyleUrl] and fetches it; a backend that has no
     * such thing cannot be adjusted this way and says so.
     */
    data object CurrentDesign : VectorStyleSource {
        override val key: String get() = "current"
    }
}

/**
 * What to do about an adjustment the map would not take.
 *
 * Only MapLibre on iOS reaches this today: it exposes paint properties as
 * typed expressions rather than by name, so a raw style-spec key it has no
 * mapping for cannot be set.
 */
enum class UnsupportedPolicy {
    /**
     * Say so and leave the rest applied. The default, because the
     * alternative makes the map reload — and not reloading is the point.
     */
    REPORT,

    /**
     * Hand the map the adjusted document instead, so everything applies.
     *
     * Correct, and expensive: the renderer drops its tiles, re-reads the
     * document and the app's overlays are rebuilt.
     */
    RELOAD,
}

/**
 * Draws a style the map cannot read itself.
 *
 * The seam between this module and the rasteriser. Declared here and
 * implemented in `com.mapconductor:vectortile`, which is what keeps an app
 * using only MapLibre from linking a megabyte of renderer it will never
 * call. An app that also targets Google Maps, MapKit, HERE or ArcGIS passes
 * one in; one that does not, does not, and is told so rather than shown a
 * blank map.
 */
interface VectorStyleRasteriser {
    /**
     * Starts drawing [styleJson] as raster tiles on [host].
     *
     * @param affects which half of the split tile layers the adjustments
     *   reached, so a change to labels alone need not redraw the ground.
     */
    fun install(
        host: MapStyleHost,
        styleJson: String,
        affects: StyleAffects,
    ): VectorStyleRasterisation
}

/** A rasteriser's work in progress. */
interface VectorStyleRasterisation {
    /**
     * Draws a different style with the same tiles.
     *
     * The geometry has not changed, only the paint over it, so this costs a
     * re-rasterise and no network.
     */
    fun restyle(
        styleJson: String,
        affects: StyleAffects,
    )

    fun dispose()
}
