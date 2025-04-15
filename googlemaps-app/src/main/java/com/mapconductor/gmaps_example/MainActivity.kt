package com.mapconductor.gmaps_example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.mapconductor.googlemaps.GMapView
import com.mapconductor.googlemaps.GMapViewState
import com.mapconductor.googlemaps.GeoPoint
import com.mapconductor.googlemaps.MapCameraPosition
import com.mapconductor.googlemaps.rememberGMapViewState
import com.mapconductor.gmaps_example.ui.theme.OverlayTestTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val gMapViewHolder = rememberGMapViewState()

            OverlayTestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MapAppView(
                        state = gMapViewHolder,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}

@Composable
fun MapAppView(
    modifier: Modifier = Modifier,
    state: GMapViewState = rememberGMapViewState(),
) {
    val camera = state.mapCameraPosition.collectAsState()
    Column (
        modifier = modifier,
    ) {
        Text("camera: ${camera.value?.target?.toUrlValue() ?: "(-, -)"}")

        Box (
            modifier = modifier,
        ){
            GMapView(state)


            Button(onClick = {
                state.moveCameraTo(MapCameraPosition(
                    target = GeoPoint.fromLatLong(
                        35.658034,
                        139.701636,
                    ),
                    zoom = 13.0,
                ))

            }) {
                Text("Shibuya")
            }
        }
    }
}