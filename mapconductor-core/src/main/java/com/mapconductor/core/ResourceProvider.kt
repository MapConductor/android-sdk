package com.mapconductor.core

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.util.DisplayMetrics
import android.util.TypedValue
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

    fun getDisplayMetrics(): DisplayMetrics = Resources.getSystem().displayMetrics

    fun getSystemConfiguration(): Configuration = Resources.getSystem().configuration

    fun init(context: Context) {
        appContext = context.applicationContext
        _initialized.value = true
    }

    fun getDensity(): Float = getDisplayMetrics().density

    fun dpToPx(dp: Float): Double = dpToPx(dp.toDouble())

    fun dpToPx(dp: Dp): Double = dpToPx(dp.value.toDouble())

    fun dpToPx(dp: Double): Double =
        TypedValue
            .applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp.toFloat(),
                getDisplayMetrics(),
            ).toDouble()

    fun pxToSp(px: Double): Double {
        val displayMetrics = getDisplayMetrics()
        val scaledDensity =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                // Android 14以降の推奨方法
                displayMetrics.density * getSystemConfiguration().fontScale
            } else {
                // 従来の方法（API 33以下）
                @Suppress("DEPRECATION")
                displayMetrics.scaledDensity
            }
        return px / scaledDensity
    }

    fun spToPx(sp: Float): Double = spToPx(sp.toDouble())

    fun spToPx(sp: TextUnit): Double = spToPx(sp.value.toDouble())

    fun spToPx(sp: Double): Double =
        TypedValue
            .applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                sp.toFloat(),
                getDisplayMetrics(),
            ).toDouble()

    fun getFontScale(): Float = getSystemConfiguration().fontScale

    /**
     * 効果的なスケール密度を現代的な方法で計算
     */
    fun getEffectiveScaledDensity(): Float {
        val displayMetrics = getDisplayMetrics()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Android 14以降：density × fontScale で計算
            displayMetrics.density * getSystemConfiguration().fontScale
        } else {
            // Android 13以下：従来の scaledDensity を使用
            @Suppress("DEPRECATION")
            displayMetrics.scaledDensity
        }
    }
}
