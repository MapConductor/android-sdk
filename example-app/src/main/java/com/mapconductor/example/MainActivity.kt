package com.mapconductor.example

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import android.os.Bundle

class MainActivity : ComponentActivity() {
    private val appViewModel: AppViewModelImpl by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DemoAppScreen(appViewModel)
        }
    }
}
