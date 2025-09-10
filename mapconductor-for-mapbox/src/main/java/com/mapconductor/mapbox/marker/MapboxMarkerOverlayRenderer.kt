package com.mapconductor.mapbox.marker

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.marker.AbstractMarkerOverlayRenderer
import com.mapconductor.core.marker.BitmapIcon
import com.mapconductor.core.marker.DefaultIcon
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.core.marker.MarkerIcon
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.marker.MarkerOverlayRenderer
import com.mapconductor.mapbox.MapboxActualMarker
import com.mapconductor.mapbox.MapboxMapViewHolder
import com.mapconductor.mapbox.toPoint
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapboxMarkerOverlayRenderer(
    holder: MapboxMapViewHolder,
    coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
    val markerManager: MarkerManager<MapboxActualMarker>,
    val markerLayer: MarkerLayer =
        MarkerLayer(
            sourceId = "markers-source",
            layerId = "markers-layer",
        ),
    val dragLayer: MarkerDragLayer =
        MarkerDragLayer(
            sourceId = "marker-drag-source",
            layerId = "marker-drag-layer",
        ),
) : AbstractMarkerOverlayRenderer<
        MapboxMapViewHolder,
        MapboxActualMarker,
    >(
        holder = holder,
        coroutine = coroutine,
    ) {
    private val iconRefCounter: MutableMap<String, Int> = mutableMapOf()
    private val defaultIcon: BitmapIcon = DefaultIcon().toBitmapIcon()

    object Prop {
        const val ICON_ID = "icon_id"
        const val DEFAULT_MARKER_ID = "default"
        const val SCALE = "scale"
        const val ICON_ANCHOR = "icon-offset"
    }

    init {
        holder.map.getStyle { style ->
            style.addImage(Prop.DEFAULT_MARKER_ID, defaultIcon.bitmap)
        }
    }

    fun redraw() {
        val entities = markerManager.allEntities()
        coroutine.launch {
            markerLayer.draw(entities)
        }
    }

    fun drawDragLayer() {
        coroutine.launch {
            dragLayer.draw()
        }
    }

    override fun setMarkerPosition(
        markerEntity: MarkerEntity<Feature>,
        position: GeoPoint,
    ) {
        val entities = markerManager.allEntities()
        val feature =
            Feature.fromGeometry(
                position.toPoint(),
                markerEntity.marker?.properties(),
                "marker-${markerEntity.state.id}",
            )
        markerEntity.marker = feature
        val features =
            entities.map {
                if (it.state.id == markerEntity.state.id) {
                    feature
                } else {
                    it.marker
                }
            }
        coroutine.launch {
            markerLayer.source.featureCollection(
                FeatureCollection.fromFeatures(features),
            )
        }
    }

    override suspend fun onAdd(data: List<MarkerOverlayRenderer.AddParams>): List<Feature> {
        return withContext(Dispatchers.Main) {
            val style =
                suspendCoroutine { continuation ->
                    holder.map.getStyle { style ->
                        continuation.resumeWith(Result.success(style))
                    }
                }

            data.forEach {
                val iconKey =
                    it.state.icon
                        .hashCode()
                        .toString()
                if (!iconRefCounter.contains(iconKey)) {
                    style.addImage(iconKey, it.bitmapIcon.bitmap)
                    iconRefCounter[iconKey] = 0
                }
            }

            data.map {
                val featureId = "marker-${it.state.id}"
                val position = GeoPoint.from(it.state.position).toPoint()
                val properties =
                    JsonObject().apply {
                        if (it.state.icon != null) {
                            it.state.icon?.let { icon ->
                                val iconKey = icon.hashCode().toString()
                                iconRefCounter[iconKey] = iconRefCounter.getOrDefault(iconKey, 0) + 1
                                addProperty(Prop.ICON_ID, iconKey)
                                // icon offset property
                                add(Prop.ICON_ANCHOR, createIconOffset(icon))
                            }
                        } else {
                            addProperty(Prop.ICON_ID, Prop.DEFAULT_MARKER_ID)
                            add(Prop.ICON_ANCHOR, getDefaultIconOffsetProperty())
                        }
                        addProperty(Prop.SCALE, it.state.icon?.scale ?: 1.0)
                    }
                Feature.fromGeometry(position, properties, featureId)
            }
        }
    }

    override suspend fun onRemove(data: List<MarkerEntity<MapboxActualMarker>>) {
        withContext(Dispatchers.Main) {
            data.forEach { entity ->
                entity.state.icon?.let { icon ->
                    val iconKey = icon.hashCode().toString()
                    val cnt = iconRefCounter.getOrDefault(iconKey, 1) - 1
                    if (cnt == 0) {
                        iconRefCounter.remove(iconKey)
                        holder.map.style?.removeStyleImage(iconKey)
                    } else {
                        iconRefCounter[iconKey] = cnt
                    }
                }
            }
        }
    }

    override suspend fun onPostProcess() {
        // For Mapbox, we need to update the layer after add/remove operations
        // but only redraw when there were actual changes
        redraw()
    }

    override suspend fun onChange(
        data: List<MarkerOverlayRenderer.ChangeParams<MapboxActualMarker>>,
    ): List<MapboxActualMarker?> =
        data.map { params ->
            val prevFinger = params.prev.fingerPrint
            val currFinger = params.current.fingerPrint
            val prevProperties = params.prev.marker?.properties()

            val properties =
                JsonObject().apply {
                    addProperty(
                        Prop.SCALE,
                        params.current.state.icon
                            ?.scale ?: 1.0f,
                    )
                    if (currFinger.icon == prevFinger.icon) {
                        addProperty(
                            Prop.ICON_ID,
                            prevProperties?.get(Prop.ICON_ID)?.asString ?: Prop.DEFAULT_MARKER_ID,
                        )

                        add(
                            Prop.ICON_ANCHOR,
                            prevProperties?.get(Prop.ICON_ANCHOR) ?: getDefaultIconOffsetProperty(),
                        )
                    } else {
                        val iconKey = prevFinger.icon.toString()
                        val cnt = iconRefCounter.getOrDefault(iconKey, 1) - 1
                        if (cnt == 0) {
                            iconRefCounter.remove(iconKey)
                            holder.map.style?.removeStyleImage(iconKey)
                        } else {
                            iconRefCounter[iconKey] = cnt
                        }

                        if (currFinger.icon == null) {
                            addProperty(Prop.ICON_ID, Prop.DEFAULT_MARKER_ID)
                            add(Prop.ICON_ANCHOR, getDefaultIconOffsetProperty())
                        } else {
                            params.current.state.icon?.let { icon ->
                                // icon id
                                val iconKey = icon.hashCode().toString()
                                if (iconRefCounter.contains(iconKey)) {
                                    iconRefCounter[iconKey] = (iconRefCounter[iconKey] ?: 0) + 1
                                } else {
                                    holder.map.style?.addImage(iconKey, params.bitmapIcon.bitmap)
                                    iconRefCounter[iconKey] = 1
                                }
                                addProperty(Prop.ICON_ID, iconKey)
                                add(Prop.ICON_ANCHOR, createIconOffset(icon))
                            }
                        }
                    }
                }

            val position =
                GeoPoint.from(params.current.state.position).toPoint()
            val featureId = "marker-${params.current.state.id}"
            Feature.fromGeometry(position, properties, featureId)
        }

    private fun getDefaultIconOffsetProperty(): JsonArray = createIconOffset(defaultIcon)

    private fun createIconOffset(icon: BitmapIcon): JsonArray =
        JsonArray().apply {
            add(-(icon.size.width * icon.anchor.x) / ResourceProvider.getDensity())
            add(-(icon.size.height * icon.anchor.y) / ResourceProvider.getDensity())
        }

    private fun createIconOffset(icon: MarkerIcon): JsonArray = createIconOffset(icon.toBitmapIcon())
}
