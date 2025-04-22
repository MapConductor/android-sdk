package com.mapconductor.arcgis

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.arcgismaps.mapping.ArcGISScene
import com.arcgismaps.mapping.view.SceneView
import com.mapconductor.core.MapViewHolder
import com.mapconductor.core.MapViewHolderStoreBaseSyncWithLifeCycleOwner

internal object MapViewHolderStore :
    MapViewHolderStoreBaseSyncWithLifeCycleOwner<WrapSceneView, ArcGISScene>() {
    override fun getOrCreate(
        context: Context,
        id: String,
        owner: LifecycleOwner,
    ): MapViewHolder<WrapSceneView, ArcGISScene> {
        val existing = this.get(id)
        if (existing != null) return existing

        val newHolder = MapViewHolderImpl.create(
            context = context.applicationContext,
            owner = owner,
        )
        this.set(id, newHolder)
        return newHolder
    }
}
