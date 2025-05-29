package com.mapconductor.arcgis

import android.content.Context
import com.arcgismaps.mapping.view.SceneView
import com.mapconductor.core.MapViewHolder
import com.mapconductor.core.MapViewHolderStoreBaseAsync

typealias ArcGISMapViewHolder = MapViewHolder<WrapSceneView, SceneView>

object ArcGISMapViewHolderStore :
    MapViewHolderStoreBaseAsync<WrapSceneView, SceneView, ArcGISMapViewInitOptions>() {
    override suspend fun getOrCreate(
        context: Context,
        id: String,
        options: ArcGISMapViewInitOptions
    ): MapViewHolder<WrapSceneView, SceneView> {
        val existing = this.get(id)
        if (existing != null) return existing

        val newHolder = ArcGISMapViewHolderImpl.create(
            context = context.applicationContext,
            options = options,
        )
        this.set(id, newHolder)
        return newHolder
    }
}
