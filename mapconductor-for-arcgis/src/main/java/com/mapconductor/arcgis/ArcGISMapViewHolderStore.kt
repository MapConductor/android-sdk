package com.mapconductor.arcgis

import com.arcgismaps.mapping.view.SceneView
import com.mapconductor.core.map.MapViewHolder
import com.mapconductor.core.map.MapViewHolderStoreBaseAsync
import android.content.Context

typealias ArcGISMapViewHolder = MapViewHolder<WrapSceneView, SceneView>

object ArcGISMapViewHolderStore :
    MapViewHolderStoreBaseAsync<WrapSceneView, SceneView, ArcGISMapViewInitOptions>() {
    override suspend fun getOrCreate(
        context: Context,
        id: String,
        options: ArcGISMapViewInitOptions,
    ): MapViewHolder<WrapSceneView, SceneView> {
        val existing = this.get(id)
        if (existing != null) return existing

        val newHolder =
            ArcGISMapViewHolderImpl.create(
                context = context.applicationContext,
                options = options,
            )
        this.set(id, newHolder)
        return newHolder
    }
}
