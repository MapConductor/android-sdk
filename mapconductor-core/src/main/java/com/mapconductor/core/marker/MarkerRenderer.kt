package com.mapconductor.core.marker

import androidx.compose.ui.geometry.Offset
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.IGeoPoint
import com.mapconductor.core.geocell.HexGeocell
import com.mapconductor.core.map.MapViewHolder
import com.mapconductor.core.marker.MarkerRenderer.UpdateParams
import com.mapconductor.core.spherical.haversineDistance
import com.mapconductor.settings.Settings
import kotlin.math.min
import kotlin.math.pow
import android.os.SystemClock
import android.view.animation.BounceInterpolator
import android.view.animation.LinearInterpolator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach

interface MarkerRendererFactory<ActualMarker> {
    fun create(
        hexGeocell: HexGeocell,
        onIconAdd: suspend (List<Pair<MarkerState, BitmapIcon>>) -> List<ActualMarker?>,
        onIconRemove: suspend (List<MarkerEntity<ActualMarker>>) -> Unit,
        onIconChange: suspend (List<UpdateParams<ActualMarker>>) -> List<ActualMarker>,
        onAnimate: suspend (MarkerEntity<ActualMarker>) -> Unit,
        onPostProcess: (suspend () -> Unit)? = null,
    ): MarkerOverlayManager<ActualMarker>
}

interface MarkerRenderer<ActualMarker> {

    interface UpdateParams<ActualMarker> {
        val entity: MarkerEntity<ActualMarker>
        val bitmapIcon: BitmapIcon
        val prevEntity: MarkerEntity<ActualMarker>
    }

    fun init(markerOverlayManager: MarkerOverlayManager<ActualMarker>)

    suspend fun addIcons(newMarkers: List<Pair<MarkerState, BitmapIcon>>): List<ActualMarker?>

    suspend fun removeIcons(removeEntities: List<MarkerEntity<ActualMarker>>)

    suspend fun changeIcons(changes: List<UpdateParams<ActualMarker>>): List<ActualMarker>

    fun animate(entity: MarkerEntity<ActualMarker>)

    fun setOnMarkerAnimationStart(listener: OnMarkerEventHandler?)

    fun setOnMarkerAnimationEnd(listener: OnMarkerEventHandler?)

    fun setDraggingState(
        markerState: MarkerState,
        dragging: Boolean,
    )

    fun findNearestMarker(
        position: IGeoPoint,
        tolerance: Double,
        zoom: Double,
    ): MarkerEntity<ActualMarker>?
}

abstract class AbstractMarkerRenderer<ActualMarker> : MarkerRenderer<ActualMarker> {
    protected val defaultIcon: BitmapIcon
    protected var markerAnimationStartHandler: ((state: MarkerState) -> Unit)? = null
    protected var markerAnimationEndHandler: ((state: MarkerState) -> Unit)? = null

    protected lateinit var markerOverlayManager: MarkerOverlayManager<ActualMarker>

    abstract val holder: MapViewHolder<*, *>
    abstract val coroutine: CoroutineScope

    override fun setOnMarkerAnimationStart(listener: OnMarkerEventHandler?) {
        this.markerAnimationStartHandler = listener
    }

    override fun setOnMarkerAnimationEnd(listener: OnMarkerEventHandler?) {
        this.markerAnimationEndHandler = listener
    }

    init {
        defaultIcon = DefaultIcon().toBitmapIcon()
    }
    override fun findNearestMarker(
        position: IGeoPoint,
        tolerance: Double,
        zoom: Double,
    ): MarkerEntity<ActualMarker>? {
//        val acceptDPI = tolerance.value * ResourceProvider.density

//        clearPolyline()
//
//        // 検索範囲の詳細分析
//        val searchAnalysis = analyzeSearchRange(position, zoom, acceptDPI.toDouble())
//
//        // 可視化レイヤーを選択
//        drawSearchOutline(searchAnalysis)

        return findMarkerFromPoint(
            position = position,
            zoom = zoom,
            tolerance = tolerance,
        )
    }

    protected abstract fun setMarkerPosition(
        markerEntity: MarkerEntity<ActualMarker>,
        position: GeoPoint,
    )

    override fun init(markerManager: MarkerOverlayManager<ActualMarker>) {
        this.markerOverlayManager = markerManager
    }

    override fun animate(entity: MarkerEntity<ActualMarker>) {
        when (entity.state.animation) {
            MarkerAnimation.Drop -> animateMarkerDrop(entity)
            MarkerAnimation.Bounce -> animateMarkerBounce(entity)
            else -> throw IllegalArgumentException("No animation is available: ${entity.state.animation}")
        }
    }

    protected fun zoomToMetersPerPixel(zoom: Double): Double {
        val earthCircumference = 40075016.686
        val tileSize = 256
        return earthCircumference / (tileSize * 2.0.pow(zoom))
    }

    override fun setDraggingState(
        markerState: MarkerState,
        dragging: Boolean,
    ) {
        // Since this "isDragging" property is internal accessor,
        // childViewControllers must call this method instead of "isDragging = true/false".
        markerState.isDragging = dragging
    }

    protected fun findMarkerFromPoint(
        position: IGeoPoint,
        zoom: Double,
        tolerance: Double,
    ): MarkerEntity<ActualMarker>? {
        val meterInMapPixel = zoomToMetersPerPixel(zoom)
        val radius = tolerance * meterInMapPixel
        val entity = markerOverlayManager.markerManager.findNearest(position) ?: return null
        val distance = haversineDistance(position, entity.state.position)
        return if (distance <= radius) {
            entity
        } else {
            null
        }
    }

    protected fun animateMarkerDrop(
        markerEntity: MarkerEntity<ActualMarker>, // ラップしたMarkerオブジェクト
        duration: Int = Settings.Default.markerDropAnimateDuration, // アニメションする時間(ms)
    ) {
        // アニメーションの最終的な目標地点(地理座標)
        val target = markerEntity.state.position

        // 線形補間
        val interpolator = LinearInterpolator()

        // 開始地点:x座標はMarkerと同じ、y座標は画面上端。なければreturn
        val startPoint = holder.toScreenOffset(target)?.let { Offset(it.x, 0f) } ?: return

        markerAnimationStartHandler?.invoke(markerEntity.state)

        // ここからアニメ本体
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
            val lat = t * target.latitude + (1f - t) * startLatLng.latitude
            val lng = t * target.longitude + (1f - t) * startLatLng.longitude

            // 現在の座標をマーカーに適用
            val newPosition = GeoPoint.fromLatLong(lat, lng)
            setMarkerPosition(markerEntity, newPosition)
        }.onCompletion {
            // 最終的にマーカー位置を正確な着地点に戻す（補間誤差などを吸収）
            markerEntity.state.position = target
            markerEntity.state.animation = null
            markerAnimationEndHandler?.invoke(markerEntity.state)
        }.launchIn(coroutine)
    }

    protected fun animateMarkerBounce(
        markerEntity: MarkerEntity<ActualMarker>,
        duration: Int = Settings.Default.markerBounceAnimateDuration, // アニメションする時間(ms)
    ) {
        val startTime = SystemClock.uptimeMillis()

        // アニメーションの最終的な目標地点(地理座標)
        val target = markerEntity.state.position

        // 線形補間
        val interpolator = BounceInterpolator()

        // 開始地点:x座標はMarkerと同じ、y座標は画面上端。なければreturn
        val startPoint = holder.toScreenOffset(target)?.let { Offset(it.x, 0f) } ?: return

        markerAnimationStartHandler?.invoke(markerEntity.state)
        flow {
            var t = 0f
            while (t < 1f) {
                val elapsed = SystemClock.uptimeMillis() - startTime
                t = interpolator.getInterpolation(min(1f, elapsed.toFloat() / duration))
                emit(t)
                delay(16L)
            }
        }.onEach { t ->
            val startLatLng = holder.fromScreenOffset(startPoint) ?: return@onEach
            val lng = target.longitude
            val lat = t * target.latitude + (1f - t) * startLatLng.latitude

            // 現在の座標をマーカーに適用
            val newPosition = GeoPoint.fromLatLong(lat, lng)
            setMarkerPosition(markerEntity, newPosition)
        }.onCompletion {
            // 最終的にマーカー位置を正確な着地点に戻す（補間誤差などを吸収）
            markerEntity.state.position = target
            markerEntity.state.animation = null
            markerAnimationEndHandler?.invoke(markerEntity.state)
        }.launchIn(coroutine)
    }
}
