package com.mapconductor.core

import androidx.compose.ui.unit.Dp
import android.content.Context
import android.content.res.Resources
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class IconResource(
    val name: String,
    val width: Double,
    val height: Double,
    val anchorX: Double,
    val anchorY: Double,
    internal val resourceId: Int,
)

object ResourceProvider {
    private val _initialized: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val initialized = _initialized.asStateFlow()


    private lateinit var appContext: Context
    val density =
        Resources
            .getSystem()
            .displayMetrics.density
            .toDouble()

    fun init(context: Context) {
        appContext = context.applicationContext
        _initialized.value = true
    }

    fun dpToPx(value: Float): Double = value * density
    fun dpToPx(value: Double): Double = value * density
    fun dpToPx(value: Dp): Double = value.value * density
}
