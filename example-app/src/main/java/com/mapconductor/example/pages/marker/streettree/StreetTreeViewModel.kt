package com.mapconductor.example.pages.marker.streettree

import androidx.lifecycle.ViewModel
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.core.marker.ImageIcon
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.MarkerTilingOptions
import com.mapconductor.maplibre.MapLibreDesign
import com.mapconductor.maplibre.MapLibreViewStateInterface
import com.mapconductor.streettree.LabelSide
import com.mapconductor.streettree.StreetTree
import com.mapconductor.streettree.StreetTreeData
import com.mapconductor.streettree.StreetTreeDataLoader
import com.mapconductor.streettree.StreetTreeIcons
import com.mapconductor.streettree.StreetTreeLabels
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Tokyo's 144,183 street trees, one colour per species.
 *
 * The set is six times the post office sample's and packed into one
 * metropolitan area rather than spread over a country, so it is what the marker
 * tiling path looks like under real pressure.
 */
class StreetTreeViewModel(
    private val dataLoader: StreetTreeDataLoader,
    private val coroutine: CoroutineScope = CoroutineScope(Dispatchers.Default),
    /** `"緯度,経度,ズーム"`。継ぎ目の不具合などを特定の場所で再現するための入口。 */
    initCamera: String? = null,
) : ViewModel() {
    val initCameraPosition =
        initCamera
            ?.split(",")
            ?.mapNotNull { it.trim().toDoubleOrNull() }
            ?.takeIf { it.size == 3 }
            .let { parts ->
                MapCameraPosition(
                    position =
                        GeoPoint.fromLatLong(
                            latitude = parts?.get(0) ?: 35.6812,
                            longitude = parts?.get(1) ?: 139.7671,
                        ),
                    zoom = parts?.get(2) ?: 11.0,
                    bearing = 0.0,
                    tilt = 0.0,
                    paddings = null,
                )
            }

    private val _markerList = MutableStateFlow<List<MarkerState>>(emptyList())
    val markerList: StateFlow<List<MarkerState>> = _markerList.asStateFlow()

    private var treeData: StreetTreeData? = null

    /** 樹種ごとの点。ラベルを外すときはこれに戻す。 */
    private var dotIcons: List<ImageIcon> = emptyList()

    /** 樹種 × 向きのラベル付きアイコン。画像を持たないので軽い。 */
    private val labelIcons = HashMap<String, ImageIcon>()

    /** いまラベルを付けている木と向き。 */
    private var labelled: Map<Int, LabelSide> = emptyMap()

    /** 道路ごとに最後に選んだ向き。パンしても同じ通りのラベルが上下・左右で入れ替わらないように。 */
    private val roadSides = java.util.concurrent.ConcurrentHashMap<String, LabelSide>()

    private var labelJob: Job? = null

    /** 木を読み終える前に止まったカメラ。読み終えたらここでラベルを付ける。 */
    private var lastCamera: MapCameraPosition? = null

    private val _selectedMarker = MutableStateFlow<MarkerState?>(null)
    val selectedMarker: StateFlow<MarkerState?> = _selectedMarker.asStateFlow()

    private val _mapViewState = MutableStateFlow<MapViewStateInterface<*>?>(null)
    val mapViewState: StateFlow<MapViewStateInterface<*>?> = _mapViewState.asStateFlow()

    private val _isMapLoaded = MutableStateFlow(false)
    val isMapLoaded: StateFlow<Boolean> = _isMapLoaded.asStateFlow()

    private val _isDataLoading = MutableStateFlow(false)
    val isDataLoading: StateFlow<Boolean> = _isDataLoading.asStateFlow()

    val markerTiling: MarkerTilingOptions =
        MarkerTilingOptions.Default.copy(
            // Trees are planted a few metres apart along a road, so below street
            // level most of them sit on top of one another. Thinning them to one
            // per icon-width halves the tile bytes and shows the same map.
            declutterPx = 14,
            iconScaleCallback = { _, zoom ->
                when {
                    zoom > 15.0 -> 1.4
                    zoom > 13.0 -> 1.0
                    zoom > 11.0 -> 0.7
                    else -> 0.5
                }
            },
        )

    fun loadTrees() {
        if (_markerList.value.isNotEmpty()) return
        coroutine.launch {
            _isDataLoading.value = true
            val data = dataLoader.load()
            val icons = StreetTreeIcons.palette(data.species.size, sizeDp = DOT_SIZE_DP)
            treeData = data
            dotIcons = icons
            _markerList.value =
                data.trees.mapIndexed { index, tree ->
                    MarkerState(
                        position = tree.position,
                        id = index.toString(),
                        icon = icons[tree.speciesIndex],
                        extra = tree,
                        onClick = ::onMarkerClick,
                    )
                }
            _isDataLoading.value = false
            lastCamera?.let(::onCameraMoveEnd)
        }
    }

    /**
     * zoom 16 以上で止まったら、画面内の木に樹種名を付ける。
     *
     * どの木にどちら向きで付けるかは [StreetTreeLabels] が決める。付け替えはマーカーの一覧を
     * 差し替えず、変わった木のアイコンだけを書き換える -- 14 万件の一覧を作り直すと、
     * そのたびに全件の同期が走る。
     */
    fun onCameraMoveEnd(camera: MapCameraPosition) {
        lastCamera = camera
        val data = treeData ?: return
        val markers = _markerList.value
        if (markers.size != data.trees.size) return
        labelJob?.cancel()
        if (camera.zoom < LABEL_MIN_ZOOM) {
            setLabels(emptyMap(), data, markers)
            return
        }
        val bounds = camera.visibleRegion?.bounds ?: return
        labelJob =
            coroutine.launch {
                val sides =
                    StreetTreeLabels.choose(
                        trees = data.trees,
                        bounds = bounds,
                        zoom = camera.zoom,
                        scale = LABEL_ICON_SCALE,
                        dotSizeDp = DOT_SIZE_DP,
                        labelSizeDp = StreetTreeIcons::labelSizeDp,
                        previousSides = HashMap(roadSides),
                    )
                for ((index, side) in sides) roadSides[data.trees[index].roadName] = side
                setLabels(sides, data, markers)
            }
    }

    private fun setLabels(
        sides: Map<Int, LabelSide>,
        data: StreetTreeData,
        markers: List<MarkerState>,
    ) {
        for (index in labelled.keys) {
            if (index !in sides) markers[index].icon = dotIcons[data.trees[index].speciesIndex]
        }
        for ((index, side) in sides) {
            if (labelled[index] == side) continue
            val tree = data.trees[index]
            markers[index].icon =
                labelIcons.getOrPut("${tree.speciesIndex}/$side") {
                    StreetTreeIcons.labelled(dotIcons[tree.speciesIndex], tree.species, side)
                }
        }
        labelled = sides
    }

    fun onMapViewChanged(mapViewState: MapViewStateInterface<*>) {
        _mapViewState.value = mapViewState
        applyTreeStyle(mapViewState)
    }

    /**
     * Swaps in a basemap stripped down to what a tree map needs: land, water,
     * the major roads and the place names. The default style draws 51
     * transportation line layers, and at this density they compete with the
     * trees for every pixel — the map ends up being about the roads.
     *
     * Cast because the design type is provider-specific by construction: a
     * MapLibre design carries a style URL, a Google one carries a map type id.
     * The page runs on whatever backend is selected, so it only reaches for
     * this when it is on MapLibre.
     */
    private fun applyTreeStyle(mapViewState: MapViewStateInterface<*>) {
        @Suppress("UNCHECKED_CAST")
        val state = mapViewState as? MapLibreViewStateInterface ?: return
        state.mapDesignType =
            MapLibreDesign(
                id = "street-tree",
                styleJsonURL = "asset://street-tree-style.json",
            )
    }

    fun onMapLoaded(mapViewState: MapViewStateInterface<*>) {
        _isMapLoaded.value = true
        loadTrees()
    }

    fun onMarkerClick(clicked: MarkerState) {
        _selectedMarker.value = clicked
    }

    fun onMapClick(clicked: GeoPoint) {
        _selectedMarker.value = null
    }

    val StreetTree.label: String get() = species

    companion object {
        /** ラベルを出すズーム。 */
        const val LABEL_MIN_ZOOM = 16.0

        const val DOT_SIZE_DP = 10f

        /** zoom > 15 の iconScaleCallback と同じ値。ラベルの重なりはこの大きさで測る。 */
        const val LABEL_ICON_SCALE = 1.4f
    }
}
