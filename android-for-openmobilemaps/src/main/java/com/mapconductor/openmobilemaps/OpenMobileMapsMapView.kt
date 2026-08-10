package com.mapconductor.openmobilemaps

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import io.openmobilemaps.mapscore.map.view.MapView
import io.openmobilemaps.mapscore.shared.map.MapConfig
import io.openmobilemaps.mapscore.shared.map.coordinates.CoordinateSystemFactory
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
class OpenMobileMapsMapViewState(
    mapDesignType: OpenMobileMapsMapDesignType,
    override val id: String,
    cameraPosition: MapCameraPosition = MapCameraPosition.Default,
) : MapViewState<OpenMobileMapsMapDesignType>(cameraPosition),
    MapViewStateInterface<OpenMobileMapsMapDesignType> {
    private var controller: OpenMobileMapsMapViewController? = null
    private var current: OpenMobileMapsMapDesignType = mapDesignType

    override var mapDesignType: OpenMobileMapsMapDesignType
        get() = current
        set(value) {
            current = value
        }

    internal fun setController(controller: OpenMobileMapsMapViewController) {
        this.controller = controller
        attachController(controller)
    }

    /** 戻り型をこのプロバイダのホルダーへ絞る（アプリが `?.map` を取れる形を保つため）。 */
    override fun getMapViewHolder(): OpenMobileMapsMapViewHolder? =
        super.getMapViewHolder() as? OpenMobileMapsMapViewHolder

    internal fun updateCameraPosition(cameraPosition: MapCameraPosition) {
        setCameraPositionInternal(cameraPosition)
    }
}

/** 地図デザイン。SDK のスタイル指定にあたる。 */
interface OpenMobileMapsMapDesignType {
    val id: String
    val background: Color
}

/** 既定のデザイン。 */
data class OpenMobileMapsDesign(
    override val id: String = "plain",
    override val background: Color = Color(0xFFE8EEF2),
) : OpenMobileMapsMapDesignType

@Composable
fun rememberOpenMobileMapsMapViewState(
    mapDesign: OpenMobileMapsMapDesignType = OpenMobileMapsDesign(),
    cameraPosition: MapCameraPositionInterface = MapCameraPosition.Default,
): OpenMobileMapsMapViewState {
    val stateId by rememberSaveable { mutableStateOf(UUID.randomUUID().toString()) }
    return remember(stateId) {
        OpenMobileMapsMapViewState(
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
 * SDK の [MapView]（`GLSurfaceView` 系）を `AndroidView` で載せる。
 *
 * ## Lifecycle の登録を忘れないこと
 *
 * [MapView.registerLifecycle] を呼ばないとレンダリングループが回らず、
 * **地図が真っ黒のまま**になる。Open Mobile Maps 固有の必須手順。
 */
@Composable
fun OpenMobileMapsMapView(
    state: OpenMobileMapsMapViewState,
    modifier: Modifier = Modifier,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val holderRef = remember(state.id) { mutableStateOf<OpenMobileMapsMapViewHolder?>(null) }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            MapView(context).apply {
                // EPSG:4326（緯度経度）で構成する。SDK 既定の EPSG:3857 のままだと
                // Coord の x/y にメートル値を入れることになり、投影が丸ごとずれる。
                setupMap(
                    MapConfig(CoordinateSystemFactory.getEpsg4326System()),
                    context.resources.displayMetrics.xdpi,
                    false,
                    false,
                )
                registerLifecycle(lifecycleOwner.lifecycle)

                val mapInterface = requireNotNull(mapInterface) { "setupMap 後に mapInterface が null" }
                val holder = OpenMobileMapsMapViewHolder(this, mapInterface)
                holderRef.value = holder
                val controller = OpenMobileMapsMapViewController(holder)
                controller.installListeners()
                state.setController(controller)
                state.setMapViewHolder(holder)
            }
        },
    )

    DisposableEffect(state.id) {
        onDispose {
            state.destroyController()
            holderRef.value = null
        }
    }
}
