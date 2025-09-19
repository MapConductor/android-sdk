package com.mapconductor.example.pages.marker.postoffice

import com.mapconductor.core.features.GeoPointImpl
import java.io.Serializable

data class PostOffice(
    val position: GeoPointImpl,
    val name: String,
    val address: String,
) : Serializable
