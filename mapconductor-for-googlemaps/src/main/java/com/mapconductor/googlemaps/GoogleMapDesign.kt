package com.mapconductor.googlemaps

import com.google.android.gms.maps.GoogleMap.MAP_TYPE_HYBRID
import com.google.android.gms.maps.GoogleMap.MAP_TYPE_NONE
import com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL
import com.google.android.gms.maps.GoogleMap.MAP_TYPE_SATELLITE
import com.google.android.gms.maps.GoogleMap.MAP_TYPE_TERRAIN
import com.mapconductor.core.map.MapDesignTypeInterface

typealias GoogleMapDesignType = MapDesignTypeInterface<Int>

sealed class GoogleMapDesign(
    override val id: Int,
) : GoogleMapDesignType {
    object Normal : GoogleMapDesign(MAP_TYPE_NORMAL)

    object Satellite : GoogleMapDesign(MAP_TYPE_SATELLITE)

    object Hybrid : GoogleMapDesign(MAP_TYPE_HYBRID)

    object Terrain : GoogleMapDesign(MAP_TYPE_TERRAIN)

    object None : GoogleMapDesign(MAP_TYPE_NONE)

    override fun getValue(): Int = id

    companion object {
        fun Create(id: Int): GoogleMapDesign =
            when (id) {
                Normal.id -> Normal
                Satellite.id -> Satellite
                Hybrid.id -> Hybrid
                Terrain.id -> Terrain
                else -> None
            }

        fun toMapDesignType(id: Int): GoogleMapDesignType =
            when (id) {
                MAP_TYPE_NORMAL -> Normal
                MAP_TYPE_SATELLITE -> Satellite
                MAP_TYPE_HYBRID -> Hybrid
                MAP_TYPE_TERRAIN -> Terrain
                else -> None
            }
    }
}
