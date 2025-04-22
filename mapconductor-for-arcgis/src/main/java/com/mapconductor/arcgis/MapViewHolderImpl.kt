package com.mapconductor.arcgis

import android.content.Context
import android.content.pm.PackageManager
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.LifecycleOwner
import com.arcgismaps.ApiKey
import com.arcgismaps.ArcGISEnvironment
import com.arcgismaps.mapping.ArcGISScene
import com.arcgismaps.mapping.BasemapStyle
import com.arcgismaps.mapping.layers.ArcGISSceneLayer
import com.arcgismaps.mapping.view.SceneView
import com.mapconductor.core.MapViewHolder

//class MapViewHolder private constructor(context: Context) {
//    private var destroyed = false
//    var googleMap: GoogleMap? = null
//
//    val mapView: MapView = MapView(context).apply {
//        onCreate(null)
//    }
//
//    internal suspend fun initAsync(timeoutMillis: Long = 10_000): Unit = withTimeout(timeoutMillis) {
//        suspendCoroutine { contract ->
//            try {
//                mapView.getMapAsync { googleMap ->
//                    this@MapViewHolder.googleMap = googleMap
//                    contract.resume(Unit)
//                }
//            } catch (e: Exception) {
//                contract.resumeWithException(e)
//            }
//        }
//    }
//
//    fun getMap(): GoogleMap {
//        return googleMap ?: throw IllegalStateException("GoogleMap is not initialized yet.")
//    }
//
//    fun destroy() {
//        if (destroyed) return
//
//        destroyed = true
//        mapView.onPause()
//        mapView.onDestroy()
//    }
//
//    companion object {
//        suspend fun create(context: Context): MapViewHolder {
//            val viewHolder = MapViewHolder(context)
//            viewHolder.initAsync()
//            return viewHolder
//        }
//    }
//}
class WrapSceneView: FrameLayout {
    lateinit var sceneView: SceneView

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun onPause(owner: LifecycleOwner) {
        this.sceneView.onPause(owner)
    }
    fun onDestroy(owner: LifecycleOwner) {
        this.sceneView.onDestroy(owner)
    }
}
class MapViewHolderImpl private constructor(
    override val mapView: WrapSceneView,
    val owner: LifecycleOwner,
): MapViewHolder<WrapSceneView, ArcGISScene> {
    override lateinit var map: ArcGISScene

    companion object {
        fun create(
            context: Context,
            owner: LifecycleOwner,
        ): MapViewHolder<WrapSceneView, ArcGISScene> {
            val apiKey = context.applicationContext.getArcGisApiKey()
            if (apiKey == null) throw Exception("<meta-data android:name=\"ARCGIS_API_KEY\" /> is required")
            ArcGISEnvironment.apiKey = ApiKey.create(apiKey)

            val mapView = SceneView(context).apply {
                onCreate(owner)
            }
            val wrapView = WrapSceneView(context).apply {
                addView(mapView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            }
            wrapView.sceneView = mapView

            val holder = MapViewHolderImpl(wrapView, owner)
            val map = ArcGISScene(BasemapStyle.ArcGISTerrain)
//            map.baseSurface.elevationSources.add(
//                ArcGISTiledElevationSource("https://elevation3d.arcgis.com/arcgis/rest/services/WorldElevation3D/Terrain3D/ImageServer")
//            )
            val buildingSceneLayerUrl = "https://your-3d-buildings-service-url/SceneServer" // 例：実際のサービスURLに変更してください
            val buildingSceneLayer = ArcGISSceneLayer(buildingSceneLayerUrl)
//            map.operationalLayers.add(buildingSceneLayer)
            mapView.scene = map
            holder.map = map

            return holder
        }
    }

    override fun attachTo(container: ViewGroup) {
        if (mapView.parent == container) return
        this.detach()
        container.addView(
            mapView,
            500,
            500
        )
    }

    override fun detach() {
        if (mapView.parent == null) return
//        mapView.onPause(this.owner)
        (mapView.parent as ViewGroup).removeView(mapView as View?)
    }

    override fun destroy(owner: LifecycleOwner?) {
        mapView.onPause(owner ?: this.owner)
        mapView.onDestroy(owner ?: this.owner)
    }
}

internal fun Context.getArcGisApiKey(): String? =
    packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        .metaData?.getString("ARCGIS_API_KEY")
