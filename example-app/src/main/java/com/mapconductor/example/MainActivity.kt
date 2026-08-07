package com.mapconductor.example

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mapconductor.example.ui.theme.AppTheme
import android.os.Bundle

class MainActivity : ComponentActivity() {
    companion object {
        /** Gesture preset requested via `--es gestures none|all`, for UI test runs. */
        var gesturesExtra: String? = null

        /**
         * Map provider requested via `--es provider maptiler|longdo|...`, for UI test runs.
         *
         * Matches [com.mapconductor.example.ui.IconItem.key]. Lets an adb-driven run start on a
         * specific provider instead of tapping through the provider menu.
         */
        var providerExtra: String? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gesturesExtra = intent.getStringExtra("gestures")
        providerExtra = intent.getStringExtra("provider")
        enableEdgeToEdge()

        setContent {
            AppTheme {
                DemoAppScreen(
                    initPage = intent.getStringExtra("page") ?: "map-basic",
//                    initPage = "startup",
                )
            }
        }
    }
}
