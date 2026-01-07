package com.mapconductor.arcgis.map

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.LifecycleOwner
import com.arcgismaps.mapping.view.SceneView
import com.arcgismaps.mapping.view.ScreenCoordinate
import com.mapconductor.arcgis.toGeoPoint
import com.mapconductor.arcgis.toPoint
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.map.MapViewHolderInterface
import android.content.Context
import android.content.pm.PackageManager
import android.util.AttributeSet
import android.widget.FrameLayout
import kotlinx.coroutines.runBlocking

class WrapSceneView : FrameLayout {
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

class ArcGISMapViewHolder(
    override val mapView: WrapSceneView,
    override val map: SceneView,
) : MapViewHolderInterface<WrapSceneView, SceneView> {
    override fun toScreenOffset(position: GeoPointInterface): Offset? {
        val result =
            mapView.sceneView.locationToScreen(
                point = GeoPoint.from(position).toPoint(map.scene?.spatialReference),
            )
        return result?.let {
            Offset(it.screenPoint.x.toFloat(), it.screenPoint.y.toFloat())
        }
    }

    override suspend fun fromScreenOffset(offset: Offset): GeoPoint? {
        val result =
            mapView.sceneView.screenToLocation(
                screenCoordinate =
                    ScreenCoordinate(
                        x = offset.x.toDouble(),
                        y = offset.y.toDouble(),
                    ),
            )
        return result.getOrNull()?.toGeoPoint()
    }

    override fun fromScreenOffsetSync(offset: Offset): GeoPoint? =
        runBlocking {
            fromScreenOffset(offset)
        }
}

internal fun Context.getArcGisApiKey(): String? =
    packageManager
        .getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        .metaData
        ?.getString("ARCGIS_API_KEY")
