package com.mapconductor.example.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mapconductor.arcgis.ArcGISMapViewState
import com.mapconductor.core.map.MapViewState
import com.mapconductor.example.R
import com.mapconductor.googlemaps.GoogleMapViewState
import com.mapconductor.here.HereMapViewState
import com.mapconductor.mapbox.MapboxMapViewState

@Composable
fun HeaderCard(
    googleMapState: GoogleMapViewState,
    mapboxMapState: MapboxMapViewState,
    hereMapState: HereMapViewState,
    arcGISMapState: ArcGISMapViewState,
    modifier: Modifier = Modifier,
    onToggleSidebar: () -> Unit,
    onSdkSelectChange: (selected: MapViewState<*>) -> Unit,
) {




}
