package com.mapconductor.maplibre

import com.mapconductor.core.map.MapDesignType

interface MapLibreMapDesignType : MapDesignType<String> {
    val styleJsonURL: String
}

data class MapLibreMapDesign(
    override val id: String,
    override val styleJsonURL: String,
): MapLibreMapDesignType {
    override fun getValue(): String = "mapDesign_id=${id},style=${styleJsonURL}"

    companion object {
        val DemoTiles = MapLibreMapDesign(
            id = "demo",
            styleJsonURL = "https://demotiles.maplibre.org/style.json",
        )
    }
}
