package com.mapconductor.arcgis

import com.arcgismaps.mapping.view.SceneView
import com.mapconductor.core.map.MapViewHolder
import com.mapconductor.core.map.StaticHolder
import android.content.Context

typealias ArcGISMapViewHolder = MapViewHolder<WrapSceneView, SceneView>

object ArcGISViewControllerStore :
    StaticHolder<ArcGISMapViewController>() {
    fun hasCache(id: String): Boolean = this.has(id)

    fun getOrCreate(
        context: Context,
        id: String,
        options: ArcGISMapViewInitOptions,
    ): ArcGISMapViewController {
        val existing = this.get(id)
        if (existing != null) return existing

        val holder =
            ArcGISMapViewHolderImpl.create(
                context = context.applicationContext,
                options = options,
            )

        val controller =
            ArcGISMapViewController(
                holder = holder,
            )
        this.set(id, controller)
        return controller
    }
}
