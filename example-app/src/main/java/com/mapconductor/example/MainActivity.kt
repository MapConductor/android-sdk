package com.mapconductor.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mapconductor.core.GeoPointBase
import com.mapconductor.core.MapCameraPositionBase
import com.mapconductor.core.MapViewStateImpl
import com.mapconductor.example.ui.IconSelectMenu
import com.mapconductor.example.ui.theme.OverlayTestTheme
import com.mapconductor.googlemaps.GoogleMapView
import com.mapconductor.googlemaps.GoogleMapViewState
import com.mapconductor.here.HereMapView
import com.mapconductor.here.HereMapViewState
import com.mapconductor.mapbox.MapboxMapView
import com.mapconductor.mapbox.MapboxViewState

class MainActivity : ComponentActivity() {
    private val appViewModel : AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
//            val gMapState = rememberGMapViewState()
//            val hereMapState = rememberHereMapViewState()
//            val mBoxViewHolder = rememberMBoxMapViewState()

            OverlayTestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column (
                        modifier = Modifier.padding(innerPadding)
                    ){
                        IconSelectMenu(viewModel = appViewModel)
                        MapAppView(
                            viewModel = appViewModel,
                            modifier = Modifier,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MapAppView(
    modifier: Modifier = Modifier,
    viewModel: AppViewModelImpl = viewModel<AppViewModel>(),
) {
    val state by viewModel.mapViewState.collectAsState()
    Column (
        modifier = modifier,
    ) {
        if (state != null) {
            val camera by state!!.mapCameraPosition.collectAsState()
            Row {
                Button(onClick = {
                    state!!.moveCameraTo(
                        dstPosition = MapCameraPositionBase(
                            target = GeoPointBase(
                                latitude = 40.689184289566214,
                                longitude =  -74.04454331830473,
                            ),
                            tilt = 82.0,
                            zoom = 17.0,
                        ),
                        durationMs = 3000,
                    )
                }) {
                    Text("Fly to!")
                }
                Column {
                    Text("LatLng: ${camera?.target?.toUrlValue() ?: "(-, -)"}")
                    Text("Zoom: ${camera?.zoom}")
                    Text("bearing: ${camera?.bearing}")
                    Text("tilt: ${camera?.tilt}")
                }
            }


            Box (
                modifier = Modifier.fillMaxSize(),
            ){
                MapViewContainer(state)
            }
        } else {
            Text(
                text = "Loading...",
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }
}

@Composable
fun MapViewContainer(state: MapViewStateImpl?) {
    when (state) {
        is GoogleMapViewState -> GoogleMapView(state)
        is HereMapViewState -> HereMapView(state)
        is MapboxViewState -> MapboxMapView(state)
        else -> throw IllegalStateException("unknown state")
    }
}
