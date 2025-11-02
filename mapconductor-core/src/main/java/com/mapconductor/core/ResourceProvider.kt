package com.mapconductor.core

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.util.DisplayMetrics
import android.util.TypedValue

data class IconResource(
    val name: String,
    val width: Double,
    val height: Double,
    val anchorX: Double,
    val anchorY: Double,
    internal val resourceId: Int,
)

object ResourceProvider {
    private lateinit var appContext: Context
    private var bitmapDensityOverride: Float? = null

    fun getDisplayMetrics(): DisplayMetrics = Resources.getSystem().displayMetrics

    fun getSystemConfiguration(): Configuration = Resources.getSystem().configuration

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun getDensity(): Float = getDisplayMetrics().density

    /**
     * Set the density used for bitmap creation. This is useful for map providers
     * that automatically scale bitmaps based on their density property.
     *
     * @param density The density to use for bitmap creation, or null to use system density
     */
    fun setBitmapDensity(density: Float?) {
        bitmapDensityOverride = density
    }

    /**
     * Get the density to use for bitmap creation.
     * Returns the override density if set, otherwise returns system density.
     */
    fun getBitmapDensity(): Float = bitmapDensityOverride ?: getDensity()

    fun dpToPx(dp: Float): Double = dpToPx(dp.toDouble())

    fun dpToPx(dp: Dp): Double = dpToPx(dp.value.toDouble())

    fun dpToPx(dp: Double): Double =
        TypedValue
            .applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp.toFloat(),
                getDisplayMetrics(),
            ).toDouble()

    /**
     * Convert dp to px using bitmap density instead of system density.
     * This is used for creating bitmaps that will be used by map providers.
     * Note: Always uses device density for bitmap pixel size, regardless of bitmapDensityOverride.
     * The bitmapDensityOverride is used to set Bitmap.density property after creation.
     */
    fun dpToPxForBitmap(dp: Double): Double = dp * getDensity()

    fun dpToPxForBitmap(dp: Float): Double = dpToPxForBitmap(dp.toDouble())

    fun dpToPxForBitmap(dp: Dp): Double = dpToPxForBitmap(dp.value.toDouble())

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

    fun getOptimalTileSize(): Int {
        val displayMetrics = getDisplayMetrics()
        return if (displayMetrics.density >= 2.0f) 512 else 256
    }
}
