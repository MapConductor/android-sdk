package com.mapconductor.openmobilemaps

import com.mapconductor.core.map.MapDesignType

typealias OpenMobileMapDesignUrlCallback = (Int, Int, Int) -> String
interface OpenMobileMapDesignType : MapDesignType<String>

data class OpenMobileMapDesign(
    override val id: String,
    val urlCallback: OpenMobileMapDesignUrlCallback,
) : OpenMobileMapDesignType {
    override fun getValue(): String = id

    companion object {
        val OpenStreetMap = OpenMobileMapDesign(
            id = "openstreetmap",
            urlCallback = { x: Int, y: Int, zoom: Int ->
                return@OpenMobileMapDesign "https://a.tile.openstreetmap.org/$zoom/$x/$y.png"
            }
        )
    }
}
