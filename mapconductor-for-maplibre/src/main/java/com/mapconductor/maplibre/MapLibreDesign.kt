package com.mapconductor.maplibre

import com.mapconductor.core.map.MapDesignTypeInterface

interface MapLibreMapDesignTypeInterface : MapDesignTypeInterface<String> {
    val styleJsonURL: String
}

data class MapLibreDesign(
    override val id: String,
    override val styleJsonURL: String,
) : MapLibreMapDesignTypeInterface {
    override fun getValue(): String = "mapDesign_id=$id,style=$styleJsonURL"

    companion object {
        val DemoTiles =
            MapLibreDesign(
                id = "demo",
                styleJsonURL = "https://demotiles.maplibre.org/style.json",
            )
        val MapTilerTonerJa =
            MapLibreDesign(
                id = "maptiler-toner-ja",
                styleJsonURL = "https://tile.openstreetmap.jp/styles/maptiler-toner-ja/style.json",
            )
        val MapTilerTonerEn =
            MapLibreDesign(
                id = "maptiler-toner-en",
                styleJsonURL = "https://tile.openstreetmap.jp/styles/maptiler-toner-en/style.json",
            )
        val OsmBright =
            MapLibreDesign(
                id = "osm-bright",
                styleJsonURL = "https://tile.openstreetmap.jp/styles/osm-bright/style.json",
            )
        val OsmBrightEn =
            MapLibreDesign(
                id = "osm-bright-en",
                styleJsonURL = "https://tile.openstreetmap.jp/styles/osm-bright-en/style.json",
            )
        val OsmBrightJa =
            MapLibreDesign(
                id = "osm-bright-ja",
                styleJsonURL = "https://tile.openstreetmap.jp/styles/osm-bright-ja/style.json",
            )
        val MapTilerBasicEn =
            MapLibreDesign(
                id = "maptiler-basic-en",
                styleJsonURL = "https://tile.openstreetmap.jp/styles/maptiler-basic-en/style.json",
            )
        val OpenMapTiles =
            MapLibreDesign(
                id = "openmaptiles",
                styleJsonURL = "https://tile.openstreetmap.jp/styles/openmaptiles/style.json",
            )
        val MapTilerBasicJa =
            MapLibreDesign(
                id = "maptiler-basic-ja",
                styleJsonURL = "https://tile.openstreetmap.jp/styles/maptiler-basic-ja/style.json",
            )
    }
}
