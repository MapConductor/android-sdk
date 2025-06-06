package com.mapconductor.arcgis

import com.mapconductor.core.map.StaticHolder

object ArcGISViewControllerStore :
    StaticHolder<ArcGISMapViewController>() {

    fun hasCache(id: String): Boolean = this.has(id)

    fun getOrCreate(
        id: String,
        holder: ArcGISMapViewHolder,
    ): ArcGISMapViewController {
        val existing = this.get(id)
        if (existing != null) return existing

        val controller = ArcGISMapViewController(
                holder = holder,
            )
        this.set(id, controller)
        return controller
    }
}
