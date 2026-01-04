package com.mapconductor.example.pages.marker.postoffice

import com.mapconductor.core.features.GeoPoint
import java.io.Serializable

data class PostOffice(
    val position: GeoPoint,
    val name: String,
    val address: String,
) : Serializable
