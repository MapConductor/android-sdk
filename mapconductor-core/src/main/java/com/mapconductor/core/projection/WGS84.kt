package com.mapconductor.core.projection

import androidx.compose.ui.geometry.Offset
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface

/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * port from here
 * https://github.com/googlemaps/android-maps-utils/blob/70a77b066b8391da06a2d708792de8337bf5d3b6/library/src/main/java/com/google/maps/android/projection/SphericalMercatorProjection.java
 */
object WGS84 : ProjectionInterface {
    override fun project(position: GeoPointInterface): Offset {
        val x = position.longitude / 360 + .5
        val siny = Math.sin(Math.toRadians(position.latitude))
        val y = 0.5 * Math.log((1 + siny) / (1 - siny)) / -(2 * Math.PI) + .5
        return Offset((x * 256).toFloat(), (y * 256).toFloat())
    }

    override fun unproject(point: Offset): GeoPointInterface {
        val x = point.x / 256 - 0.5
        val lng = x * 360
        val y = .5 - point.y / 256
        val lat = 90 - Math.toDegrees(Math.atan(Math.exp(-y * 2 * Math.PI)) * 2)
        return object : GeoPointInterface {
            override val latitude: Double = lat
            override val longitude: Double = lng
            override val altitude: Double? = null

            override fun wrap(): GeoPointInterface = GeoPoint(latitude, longitude, altitude ?: 0.0).wrap()
        }
    }
}
