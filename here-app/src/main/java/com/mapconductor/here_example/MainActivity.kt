package com.mapconductor.here_example

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
import com.mapconductor.here.MBoxMapView
import com.mapconductor.here.HereMapViewState
import com.mapconductor.here.GeoPoint
import com.mapconductor.here.MapCameraPosition
import com.mapconductor.here.rememberHereMapViewState
import com.mapconductor.here_example.ui.theme.ExampleTheme
import java.math.BigDecimal
import java.math.RoundingMode

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val gMapViewHolder = rememberHereMapViewState()

            ExampleTheme {
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
    state: HereMapViewState = rememberHereMapViewState(),
) {
    val camera = state.mapCameraPosition.collectAsState()
    Column (
        modifier = modifier,
    ) {
        Text("camera: ${camera.value?.target?.toUrlValue() ?: "(-, -)"}")


        Box (
            modifier = modifier,
        ){
            MBoxMapView(state)


            Button(onClick = {
                state.moveCameraTo(MapCameraPosition(
                    target = GeoPoint.fromLatLong(
                        35.658034,
                        139.701636,
                    ),
                    zoom = 14.0,
                ))

            }) {
                Text("Shibuya")
            }
        }
    }
}
