package com.mapconductor.example

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mapconductor.StarbucksHI_list
import com.mapconductor.arcgis.ArcGISMapView
import com.mapconductor.arcgis.MapCameraPosition
import com.mapconductor.arcgis.rememberArcGISMapViewState
import android.os.Bundle

class MainActivity : ComponentActivity() {
    private val appViewModel: AppViewModelImpl by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
//            ArcGISScreen()
            DemoAppScreen(appViewModel)
        }
    }
}

@Composable
fun ArcGISScreen() {
    val mapState =
        rememberArcGISMapViewState(
            cameraPosition =
                MapCameraPosition(
                    position = StarbucksHI_list[0].position,
                    zoom = 13.0,
                ),
        )

    ArcGISMapView(
        state = mapState,
        modifier = Modifier.fillMaxSize(),
    ) {
    }
}
