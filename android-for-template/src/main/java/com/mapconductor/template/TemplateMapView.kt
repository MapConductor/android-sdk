package com.mapconductor.template

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.mapconductor.core.map.MutableMapServiceRegistry
import kotlinx.coroutines.MainScope
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapCameraPositionInterface
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.map.MapViewStateInterface
import java.util.UUID

/**
 * アプリ側の状態。
 *
 * カメラの保持・[MapViewStateInterface.moveCameraTo]・`fitBounds` はコアの
 * [MapViewState] が持つ。ドライバーが書くのは
 *  - `id`
 *  - `mapDesignType`
 *  - 共変の [getMapViewHolder]（**1 行だが消さないこと**。消すとアプリ側の
 *    `state.getMapViewHolder()?.map` が静的型を失いソース非互換になる）
 * の 3 つだけ。
 */
class TemplateMapViewState(
    mapDesignType: TemplateMapDesignType,
    override val id: String,
    cameraPosition: MapCameraPosition = MapCameraPosition.Default,
) : MapViewState<TemplateMapDesignType>(cameraPosition),
    MapViewStateInterface<TemplateMapDesignType> {
    private var controller: TemplateMapViewController? = null
    private var current: TemplateMapDesignType = mapDesignType

    override var mapDesignType: TemplateMapDesignType
        get() = current
        set(value) {
            current = value
        }

    internal fun setController(controller: TemplateMapViewController) {
        this.controller = controller
        attachController(controller)
    }

    /** 戻り型をこのプロバイダのホルダーへ絞る（アプリが `?.map` を取れる形を保つため）。 */
    override fun getMapViewHolder(): TemplateMapViewHolder? = super.getMapViewHolder() as? TemplateMapViewHolder

    internal fun updateCameraPosition(cameraPosition: MapCameraPosition) {
        setCameraPositionInternal(cameraPosition)
    }
}

/** 地図デザイン。SDK のスタイル指定にあたる。 */
interface TemplateMapDesignType {
    val id: String
    val background: Color
}

/** 既定のデザイン。 */
data class TemplateDesign(
    override val id: String = "plain",
    override val background: Color = Color(0xFFE8EEF2),
) : TemplateMapDesignType

@Composable
fun rememberTemplateMapViewState(
    mapDesign: TemplateMapDesignType = TemplateDesign(),
    cameraPosition: MapCameraPositionInterface = MapCameraPosition.Default,
): TemplateMapViewState {
    val stateId by rememberSaveable { mutableStateOf(UUID.randomUUID().toString()) }
    return remember(stateId) {
        TemplateMapViewState(
            id = stateId,
            mapDesignType = mapDesign,
            // MapCameraPosition.from(...) は各プロバイダがローカル拡張として持っている
            // （コアには無い。todo の申し送り参照）。雛形は素直に組み立てる。
            cameraPosition =
                MapCameraPosition(
                    position = GeoPoint.from(cameraPosition.position),
                    zoom = cameraPosition.zoom,
                    bearing = cameraPosition.bearing,
                    tilt = cameraPosition.tilt,
                ),
        )
    }
}

/**
 * 地図の Composable。
 *
 * 本物のドライバーは `AndroidView` で SDK の `MapView` を載せる。雛形は
 * [TemplateDisplayList] を Canvas に描くだけで、描画そのものは本題ではない。
 * 見るべきは
 *  - ビューポートの大きさを地図へ渡すこと（投影に要る）
 *  - タップを SDK のイベントとして [TemplateMapViewController.installListeners] の
 *    受け口へ流すこと
 * の 2 点。
 */
@Composable
fun TemplateMapView(
    state: TemplateMapViewState,
    modifier: Modifier = Modifier,
) {
    val map = remember(state.id) { TemplateMap() }
    val revision = remember(state.id) { mutableIntStateOf(0) }

    val controller =
        remember(state.id) {
            createTemplateViewController(map).also {
                map.onInvalidate = { revision.intValue++ }
                state.setController(it)
            }
        }

    TemplateMapCanvas(
        map = map,
        design = state.mapDesignType,
        revision = revision.intValue,
        controller = controller,
        modifier = modifier,
    )
}

/**
 * コントローラ一式を組み立てる。
 *
 * Compose を通さないホスト（React Native の `reactnative-for-template`）からも
 * 同じ地図を使えるように、`@Composable` の外へ出してある。本物のドライバーでは
 * `createMapLibreViewController` などがこれに当たる。
 *
 * @param serviceRegistry 拡張モジュール（マーカークラスタリング等）が capability を
 *   引くレジストリ。雛形はマーカーのレンダリング差し替えに対応していないので何も
 *   登録しないが、対応する場合は `MarkerRenderingSupportKey` をここで登録する
 *   （`android-for-maplibre` の同名関数を参照）。**登録を忘れると
 *   クラスタリングが黙って何も描画しない。**
 */
fun createTemplateViewController(
    map: TemplateMap,
    @Suppress("UNUSED_PARAMETER") serviceRegistry: MutableMapServiceRegistry? = null,
): TemplateMapViewController {
    val holder = TemplateMapViewHolder(TemplateMapSurface(map), map)
    val markerRenderer = TemplateMarkerRenderer(holder, MainScope())
    return TemplateMapViewController(
        holder = holder,
        markerController = TemplateMarkerController(markerRenderer),
        polylineController = TemplatePolylineController(TemplatePolylineRenderer(holder)),
        polygonController = TemplatePolygonController(TemplatePolygonRenderer(holder)),
        circleController = TemplateCircleController(TemplateCircleRenderer(holder)),
        groundImageController = TemplateGroundImageController(TemplateGroundImageRenderer(holder)),
        rasterLayerController =
            TemplateRasterLayerController(TemplateRasterLayerRenderer(holder, MainScope())),
    ).also { it.installListeners() }
}

/**
 * 描画だけを担う Composable。
 *
 * 本物のドライバーではここが `AndroidView { SDK の MapView }` になる。
 * `TemplateMapView` と RN のホストの両方から使う。
 */
@Composable
fun TemplateMapCanvas(
    map: TemplateMap,
    design: TemplateMapDesignType,
    revision: Int,
    controller: TemplateMapViewController,
    modifier: Modifier = Modifier,
) {
    Canvas(
        modifier =
            modifier.pointerInput(controller) {
                detectTapGestures(
                    onTap = { offset -> map.unproject(offset)?.let { map.onMapClick?.invoke(it) } },
                    onLongPress = { offset ->
                        map.unproject(offset)?.let { map.onMapLongClick?.invoke(it) }
                    },
                )
            },
    ) {
        map.viewportSize = Size(size.width, size.height)
        @Suppress("UNUSED_EXPRESSION")
        revision // 再描画のトリガ
        drawRect(design.background)
        map.displayList.markers.values.forEach { marker ->
            if (!marker.visible) return@forEach
            map.project(marker.position)?.let { drawCircle(Color.Red, radius = 12f, center = it) }
        }
        map.displayList.circles.values.forEach { circle ->
            map.project(circle.center)?.let { drawCircle(Color.Blue.copy(alpha = 0.3f), 40f, it) }
        }
    }
}
