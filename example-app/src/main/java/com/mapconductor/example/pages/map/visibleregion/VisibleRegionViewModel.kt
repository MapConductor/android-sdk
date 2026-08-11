package com.mapconductor.example.pages.map.visibleregion

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.mapconductor.core.map.MapCameraPositionInterface
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.map.VisibleRegion

interface VisibleRegionViewModelInterface {
    val mapViewState: State<MapViewStateInterface<*>?>
    val currentCameraPosition: State<MapCameraPositionInterface?>
    val currentVisibleRegion: State<VisibleRegion?>

    fun onMapViewStateChanged(mapViewState: MapViewStateInterface<*>)

    fun onMapLoaded(mapViewState: MapViewStateInterface<*>)

    fun onCameraChanged(cameraPosition: MapCameraPositionInterface)
}

/**
 * 表示領域サンプルの状態。
 *
 * 表示に使う値は [VisibleRegionMapComponent] が `MapCameraPosition` から直接読むので、
 * ここは「いまどのカメラか」を持つだけ。react-sdk の `VisibleRegionPage.tsx` が
 * `useState<MapCameraPosition>` 1 本で済ませているのと同じ形にしてある。
 */
class VisibleRegionViewModel :
    ViewModel(),
    VisibleRegionViewModelInterface {
    private val _mapViewState = mutableStateOf<MapViewStateInterface<*>?>(null)
    override val mapViewState: State<MapViewStateInterface<*>?> = _mapViewState

    private val _currentCameraPosition = mutableStateOf<MapCameraPositionInterface?>(null)
    override val currentCameraPosition: State<MapCameraPositionInterface?> = _currentCameraPosition

    private val _currentVisibleRegion = mutableStateOf<VisibleRegion?>(null)
    override val currentVisibleRegion: State<VisibleRegion?> = _currentVisibleRegion

    override fun onMapViewStateChanged(mapViewState: MapViewStateInterface<*>) {
        _mapViewState.value = mapViewState
    }

    override fun onMapLoaded(mapViewState: MapViewStateInterface<*>) {
        // 何もしない。カメライベントで更新される。
    }

    override fun onCameraChanged(cameraPosition: MapCameraPositionInterface) {
        _currentCameraPosition.value = cameraPosition
        _currentVisibleRegion.value = cameraPosition.visibleRegion
    }
}
