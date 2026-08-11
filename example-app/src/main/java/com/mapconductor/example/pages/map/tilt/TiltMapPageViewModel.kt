package com.mapconductor.example.pages.map.tilt

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.marker.ColorDefaultIcon
import com.mapconductor.core.marker.MarkerAnimation
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.spherical.Spherical
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface TiltMapPageViewModelInterface {
    val initCameraPosition: MapCameraPosition
    val markerStates: List<MarkerState>
    val anchorCircleStates: List<CircleState>
    val cameraPosition: StateFlow<MapCameraPosition>
    val disableSlider: StateFlow<Boolean>
    var tilt: Double
    val mapViewState: StateFlow<MapViewStateInterface<*>?>

    fun onMapCameraMoveStart(point: GeoPoint)

    fun onMapCameraMoveEnd(point: GeoPoint)

    fun onMapViewChanged(state: MapViewStateInterface<*>)
}

class DistanceColorPair(
    val distance: Double,
    val color: Color,
)

class TiltMapPageViewModel :
    ViewModel(),
    TiltMapPageViewModelInterface {
    override val initCameraPosition =
        MapCameraPosition(
            position =
                GeoPoint(
                    latitude = 48.858140690309604,
                    longitude = 2.2945027576710344,
                ),
            zoom = 17.0,
            bearing = 270.0,
        )
    private var currentPosition: MapCameraPosition = initCameraPosition

    /**
     * 中心 1 個 + 同心円 5 本（各 8 個、45 度おき）= 41 個。
     * react-sdk の `examples/basic` の Tilt ページと同じ構成にしてある。
     *
     * ## 間隔がリングごとに 60m なのは、この画面のズームに合わせたため
     *
     * react 側はズーム 11 で 3km 間隔だが、こちらはエッフェル塔をズーム 17 で見ている。
     * 同じ 3km だと 1 本目から画面外へ出る。**傾けたときに手前と奥で見え方が変わるのを
     * 確かめるページ**なので、リングが画面に収まっていることが要件。
     */
    override val markerStates: List<MarkerState> = buildTiltRingMarkers(initCameraPosition.position)

    /**
     * 各マーカーの真下に置く小さな円。
     *
     * 円はポリゴン（地面に貼り付く）として描かれ、マーカーはアイコンレイヤ
     * （画面固定サイズ）で描かれる。**傾けたときにピンの先端がこの円から
     * 外れたら、ズレているのはアイコンレイヤの側**だと確定できる。
     */
    override val anchorCircleStates: List<CircleState> =
        markerStates.map { marker ->
            CircleState(
                id = "anchor-${marker.id}",
                center = marker.position,
                radiusMeters = 2.5,
                geodesic = true,
                fillColor = Color(0xFFFF00FF),
                strokeColor = Color.Black,
                strokeWidth = 1.dp,
                clickable = false,
            )
        }

    private var _disableSlider: MutableStateFlow<Boolean> = MutableStateFlow<Boolean>(false)

    override val disableSlider: StateFlow<Boolean> = _disableSlider.asStateFlow()

    private var _cameraPosition: MutableStateFlow<MapCameraPosition> = MutableStateFlow(currentPosition)

    override val cameraPosition: StateFlow<MapCameraPosition> = _cameraPosition.asStateFlow()

    private var _mapViewState: MutableStateFlow<MapViewStateInterface<*>?> = MutableStateFlow(null)
    override val mapViewState: StateFlow<MapViewStateInterface<*>?> = _mapViewState.asStateFlow()

    override fun onMapViewChanged(state: MapViewStateInterface<*>) {
        mapViewState.value?.cameraPosition?.let {
            state.moveCameraTo(it)
        }
        this._mapViewState.value = state
    }

    private var _tilt by mutableStateOf(initCameraPosition.tilt)

    override var tilt: Double
        get(): Double = _tilt

        set(angle) {
            if (_disableSlider.value) return
            _tilt = angle

            currentPosition =
                currentPosition.copy(
                    tilt = angle,
                )
            _cameraPosition.value = currentPosition

            mapViewState.value?.moveCameraTo(currentPosition)
        }

    override fun onMapCameraMoveStart(point: GeoPoint) {
        _disableSlider.value = true
    }

    override fun onMapCameraMoveEnd(point: GeoPoint) {
        _disableSlider.value = false
        _mapViewState.value?.let {
            currentPosition = it.cameraPosition
        }
    }
}

/** 中心の色。 */
private val CENTER_COLOR = Color(0xFF111827)

/** リングごとの色（内側から外側へ）。react-sdk の Tilt ページと同じ並び。 */
private val RING_COLORS =
    listOf(
        Color(0xFFE74C3C),
        Color(0xFFE67E22),
        Color(0xFFF1C40F),
        Color(0xFF2ECC71),
        Color(0xFF3498DB),
    )

private const val RING_SPACING_METERS = 60.0
private const val MARKERS_PER_RING = 8

/**
 * 中心 1 個と、[RING_COLORS] の本数ぶんの同心円上のマーカーを作る。
 *
 * タップするとバウンドする。Android のコアはアニメーションが終わると
 * `animate(null)` を自分で呼ぶので、react 側のようにタイマーで戻す必要はない。
 */
private fun buildTiltRingMarkers(center: GeoPoint): List<MarkerState> =
    buildList {
        add(
            MarkerState(
                id = "tilt-center",
                position = center,
                icon = ColorDefaultIcon(fillColor = CENTER_COLOR),
                onClick = { it.animate(MarkerAnimation.Bounce) },
            ),
        )
        RING_COLORS.forEachIndexed { ringIndex, color ->
            val distance = (ringIndex + 1) * RING_SPACING_METERS
            repeat(MARKERS_PER_RING) { step ->
                val heading = step * (360.0 / MARKERS_PER_RING)
                add(
                    MarkerState(
                        id = "tilt-ring${ringIndex + 1}-${heading.toInt()}",
                        position = Spherical.computeOffset(center, distance, heading),
                        icon = ColorDefaultIcon(fillColor = color),
                        onClick = { it.animate(MarkerAnimation.Bounce) },
                    ),
                )
            }
        }
    }
