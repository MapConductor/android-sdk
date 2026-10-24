package com.mapconductor.example.pages.vectortile

import com.mapconductor.arcgis.ArcGISDesign
import com.mapconductor.arcgis.ArcGISMapViewStateInterface
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.googlemaps.GoogleMapDesign
import com.mapconductor.googlemaps.GoogleMapViewStateInterface
import com.mapconductor.longdo.LongdoDesign
import com.mapconductor.longdo.LongdoViewStateInterface
import com.mapconductor.mapbox.MapboxMapDesign
import com.mapconductor.mapbox.MapboxViewStateInterface
import com.mapconductor.maplibre.MapLibreDesign
import com.mapconductor.maplibre.MapLibreViewStateInterface
import com.mapconductor.maptiler.MapTilerDesign
import com.mapconductor.maptiler.MapTilerViewStateInterface
import com.mapconductor.openmobilemaps.OpenMobileMapsDesign
import com.mapconductor.openmobilemaps.OpenMobileMapsViewStateInterface
import com.mapconductor.tomtom.TomTomMapDesign
import com.mapconductor.tomtom.TomTomMapViewStateInterface

/**
 * Blanks the backend's own basemap, or restores it.
 *
 * Per provider by construction: the design type is provider-specific, and
 * only some of them have a "draw nothing" value. Where there is none the
 * opaque tiles cover it anyway -- the difference is whether the device also
 * fetches a basemap nobody sees. HERE and Mappls have no such design.
 */
fun showProviderBasemap(
    state: MapViewStateInterface<*>,
    visible: Boolean,
) {
    when (state) {
        is MapLibreViewStateInterface ->
            state.mapDesignType =
                if (visible) {
                    MapLibreDesign.OsmBrightJa
                } else {
                    MapLibreDesign(id = "blank", styleJsonURL = "asset://blank-style.json")
                }
        is GoogleMapViewStateInterface ->
            state.mapDesignType = if (visible) GoogleMapDesign.Normal else GoogleMapDesign.None
        is ArcGISMapViewStateInterface ->
            state.mapDesignType = if (visible) ArcGISDesign.OsmStandard else ArcGISDesign.None
        is MapboxViewStateInterface ->
            state.mapDesignType = if (visible) MapboxMapDesign.Standard else MapboxMapDesign.None
        is TomTomMapViewStateInterface ->
            state.mapDesignType = if (visible) TomTomMapDesign.Standard else TomTomMapDesign.None
        is MapTilerViewStateInterface ->
            state.mapDesignType = if (visible) MapTilerDesign.Streets else MapTilerDesign.None
        is LongdoViewStateInterface ->
            state.mapDesignType = if (visible) LongdoDesign.Normal else LongdoDesign.None
        is OpenMobileMapsViewStateInterface ->
            state.mapDesignType =
                if (visible) OpenMobileMapsDesign.OpenStreetMap else OpenMobileMapsDesign.None
        else -> Unit
    }
}
