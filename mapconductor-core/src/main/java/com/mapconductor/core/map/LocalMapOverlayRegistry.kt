package com.mapconductor.core.map

import androidx.compose.runtime.compositionLocalOf

val LocalMapOverlayRegistry =
    compositionLocalOf<MapOverlayRegistry> {
        error("Map overlays must be registered under the <MapView />")
    }

val LocalMapViewController =
    compositionLocalOf<com.mapconductor.core.controller.MapViewController> {
        error("Map controller must be available under the <MapView />")
    }
