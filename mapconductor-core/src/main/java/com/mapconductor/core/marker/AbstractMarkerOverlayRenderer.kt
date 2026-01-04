package com.mapconductor.core.marker

import androidx.compose.ui.geometry.Offset
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapViewHolderInterface
import com.mapconductor.core.projection.Earth
import com.mapconductor.settings.Settings
import kotlin.math.min
import kotlin.math.pow
import android.os.SystemClock
import android.view.animation.BounceInterpolator
import android.view.animation.LinearInterpolator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach

abstract class AbstractMarkerOverlayRenderer<
    MapViewHolderType : MapViewHolderInterface<*, *>,
    ActualMarker,
>(
    val holder: MapViewHolderType,
    val coroutine: CoroutineScope,
    val dropAnimateDuration: Long = Settings.Default.markerDropAnimateDuration,
    val bounceAnimateDuration: Long = Settings.Default.markerBounceAnimateDuration,
) : MarkerOverlayRendererInterface<ActualMarker> {
    override var animateStartListener: OnMarkerEventHandler? = null
    override var animateEndListener: OnMarkerEventHandler? = null

    abstract fun setMarkerPosition(
        markerEntity: MarkerEntityInterface<ActualMarker>,
        position: GeoPoint,
    )

    override suspend fun onAnimate(entity: MarkerEntityInterface<ActualMarker>) {
        val animation = entity.state.getAnimation()
        when (animation) {
            MarkerAnimation.Drop ->
                animateMarkerDrop(
                    entity = entity,
                    duration = dropAnimateDuration,
                )
            MarkerAnimation.Bounce ->
                animateMarkerBounce(
                    entity = entity,
                    duration = bounceAnimateDuration,
                )
            else -> throw IllegalArgumentException("No animation is available: $animation")
        }
    }

    fun zoomToMetersPerPixel(
        zoom: Double,
        tileSize: Int,
    ): Double = Earth.CIRCUMFERENCE_METERS / (tileSize * 2.0.pow(zoom))

    fun animateMarkerDrop(
        entity: MarkerEntityInterface<ActualMarker>,
        duration: Long,
    ) {
        // アニメーションの最終的な目標地点(地理座標)
        val target = entity.state.position

        // 線形補間
        val interpolator = LinearInterpolator()

        // 開始地点:x座標はMarkerと同じ、y座標は画面上端。なければreturn
        val startPoint = holder.toScreenOffset(target)?.let { Offset(it.x, 0f) } ?: return

        animateStartListener?.invoke(entity.state)

        flow {
            val startTime = SystemClock.uptimeMillis()
            var t = 0f
            while (t < 1f) {
                val elapsed = SystemClock.uptimeMillis() - startTime
                t = min(1f, elapsed.toFloat() / duration)
                emit(interpolator.getInterpolation(t))
                delay(16L)
            }
        }.onEach { t: Float ->
            // 開始時の画面座標から緯度経度に戻す(垂直方向アニメーション起点)
            val startLatLng = holder.fromScreenOffset(startPoint)!!

            // 緯度・経度を線形補間
            val interpolatedLatitude = t * target.latitude + (1f - t) * startLatLng.latitude
            val interpolatedLongitude = t * target.longitude + (1f - t) * startLatLng.longitude

            // 現在の座標をマーカーに適用
            val newPosition = GeoPoint.fromLatLong(interpolatedLatitude, interpolatedLongitude)
            setMarkerPosition(entity, newPosition)
        }.onCompletion {
            entity.state.position = target
            entity.state.animate(null)
            animateEndListener?.invoke(entity.state)
        }.launchIn(CoroutineScope(Dispatchers.Main))
    }

    fun animateMarkerBounce(
        entity: MarkerEntityInterface<ActualMarker>,
        duration: Long,
    ) {
        val target = entity.state.position
        val interpolator = BounceInterpolator()
        val startPoint = holder.toScreenOffset(target)?.let { Offset(it.x, 0f) } ?: return

        animateStartListener?.invoke(entity.state)
        flow {
            val startTime = SystemClock.uptimeMillis()
            var t = 0f
            while (t < 1f) {
                val elapsed = SystemClock.uptimeMillis() - startTime
                t = interpolator.getInterpolation(min(1f, elapsed.toFloat() / duration))
                emit(t)
                delay(16L)
            }
        }.onEach { t ->
            val startLatLng = holder.fromScreenOffset(startPoint) ?: return@onEach
            val interpolatedLongitude = t * target.longitude + (1f - t) * startLatLng.longitude
            val interpolatedLatitude = t * target.latitude + (1f - t) * startLatLng.latitude

            // 現在の座標をマーカーに適用
            val newPosition = GeoPoint.fromLatLong(interpolatedLatitude, interpolatedLongitude)
            setMarkerPosition(entity, newPosition)
        }.onCompletion {
            // 最終的にマーカー位置を正確な着地点に戻す（補間誤差などを吸収）
            entity.state.position = target
            entity.state.animate(null)
            animateEndListener?.invoke(entity.state)
        }.launchIn(coroutine)
    }
}
