package com.mapconductor.arcgis

import android.content.Context
import android.content.pm.PackageManager
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.lifecycle.LifecycleOwner
import com.arcgismaps.ApiKey
import com.arcgismaps.ArcGISEnvironment
import com.arcgismaps.mapping.ArcGISScene
import com.arcgismaps.mapping.ArcGISTiledElevationSource
import com.arcgismaps.mapping.view.SceneView
import com.mapconductor.core.MapViewHolder

class WrapSceneView: FrameLayout {
    lateinit var sceneView: SceneView

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun onCreate(owner: LifecycleOwner) {
        this.sceneView.onCreate(owner)
    }
    fun onPause(owner: LifecycleOwner) {
        this.sceneView.onPause(owner)
    }
    fun onResume(owner: LifecycleOwner) {
        this.sceneView.onResume(owner)
    }
    fun onStop(owner: LifecycleOwner) {
        this.sceneView.onStop(owner)
    }
    fun onDestroy(owner: LifecycleOwner) {
        this.sceneView.onDestroy(owner)
    }
}
class ArcGISMapViewHolderImpl private constructor(
    override val mapView: WrapSceneView,
): MapViewHolder<WrapSceneView, SceneView> {
    override lateinit var map: SceneView

    companion object {
        fun create(
            context: Context,
            options: ArcGISMapViewInitOptions,
        ): MapViewHolder<WrapSceneView, SceneView> {
            val apiKey = context.applicationContext.getArcGisApiKey()
            if (apiKey == null) throw Exception("<meta-data android:name=\"ARCGIS_API_KEY\" /> is required")
            ArcGISEnvironment.apiKey = ApiKey.create(apiKey)

            val sceneView = SceneView(context)
            val wrapView = WrapSceneView(context).apply {
                addView(sceneView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            }
            wrapView.sceneView = sceneView


            val holder = ArcGISMapViewHolderImpl(wrapView)
            val scene = ArcGISScene(options.basemapStyle)
            options.elevationSources.forEach {
                val source = ArcGISTiledElevationSource(it)
                scene.baseSurface.elevationSources.add(source)
            }

            holder.map = sceneView
            sceneView.scene = scene

            return holder
        }
    }
}

internal fun Context.getArcGisApiKey(): String? =
    packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        .metaData?.getString("ARCGIS_API_KEY")
