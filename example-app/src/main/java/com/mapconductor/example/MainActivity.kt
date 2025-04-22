package com.mapconductor.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mapconductor.core.MapViewState
import com.mapconductor.core.Marker
import com.mapconductor.example.ui.IconSelectMenu
import com.mapconductor.example.ui.theme.MapConductorTheme
import com.mapconductor.googlemaps.GeoPoint
import com.mapconductor.googlemaps.GoogleMapView
import com.mapconductor.googlemaps.GoogleMapViewState
import com.mapconductor.here.HereMapView
import com.mapconductor.here.HereMapViewState
import com.mapconductor.mapbox.MapboxMapView
import com.mapconductor.mapbox.MapboxViewState

class MainActivity : ComponentActivity() {
    private val appViewModel : AppViewModelImpl by viewModels {
        AppViewModelFactory(
            application = application,
            lifecycleOwner = this,
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MapConductorTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                IconSelectMenu(viewModel = appViewModel)
                            }
                        )
                    },
                    modifier = Modifier.fillMaxSize(),
                ) { innerPadding ->
                    MapAppView(
                        appViewModel = appViewModel,
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
    appViewModel: AppViewModel = viewModel<AppViewModelImpl>(),
) {
    val state by appViewModel.mapViewState.collectAsState()

    Column (
        modifier = modifier,
    ) {
        if (state != null) {
            val camera by state!!.mapCameraPosition.collectAsState()
            Row {
                Button(onClick = appViewModel::flyTo) {
                    Text("Fly to!")
                }
                Column {
                    Text("LatLng: ${camera?.target?.toUrlValue() ?: "(-, -)"}")
                    Text("Zoom: ${camera?.zoom}")
                    Text("bearing: ${camera?.bearing}")
                    Text("tilt: ${camera?.tilt}")
                }
            }
            MapViewContainer(state = state,)
        } else {
            Text(
                text = "Loading...",
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }
}

@Composable
fun MapViewContainer(
    modifier: Modifier = Modifier,
    state: MapViewState? = null,
) {
    val context = LocalContext.current
    when (state) {
        is GoogleMapViewState -> {
            GoogleMapView(
                modifier = modifier,
                state = state,
            ) {
                Marker(
                    geoPoint = GeoPoint.fromLatLong(
                        latitude = 40.689184289566214,
                        longitude = -74.04454331830473,
                    )
                ) {
                    Toast.makeText(context, "clicked", Toast.LENGTH_SHORT).show()
                }
            }
        }
        is HereMapViewState -> HereMapView(state)
        is MapboxViewState -> MapboxMapView(modifier, state) {
            Marker(
                geoPoint = GeoPoint.fromLatLong(
                    latitude = 40.689184289566214,
                    longitude = -74.04454331830473,
                )
            ) {
                Toast.makeText(context, "clicked", Toast.LENGTH_SHORT).show()
            }
        }
//        is ArcGisMapViewState -> ArcGisMapView(state)
        else -> throw IllegalStateException("unknown state")
    }
}
