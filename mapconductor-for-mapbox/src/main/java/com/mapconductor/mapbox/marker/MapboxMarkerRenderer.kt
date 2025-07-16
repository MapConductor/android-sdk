package com.mapconductor.mapbox.marker

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.geocell.HexGeocell
import com.mapconductor.core.marker.AbstractMarkerRenderer
import com.mapconductor.core.marker.BitmapIcon
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.core.marker.MarkerIcon
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.marker.MarkerOverlayManager
import com.mapconductor.core.marker.MarkerOverlayManagerImpl
import com.mapconductor.core.marker.MarkerRendererFactory
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.UpdateParams
import com.mapconductor.mapbox.MapboxMapViewHolder
import com.mapconductor.mapbox.toPoint
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class DefaultMapboxMarkerRenderer : MarkerRendererFactory<Feature> {
    override fun create(
        hexGeocell: HexGeocell,
        onIconAdd: suspend (List<Pair<MarkerState, BitmapIcon>>) -> List<Feature?>,
        onIconRemove: suspend (List<MarkerEntity<Feature>>) -> Unit,
        onIconChange: suspend (List<UpdateParams<Feature>>) -> List<Feature>,
        onAnimate: suspend (MarkerEntity<Feature>) -> Unit,
        onPostProcess: (suspend () -> Unit)?,
    ): MarkerOverlayManager<Feature> =
        MarkerOverlayManagerImpl(
            markerManager = MarkerManager(hexGeocell),
            onRemove = onIconRemove,
            onAdd = onIconAdd,
            onChange = onIconChange,
            onPostProcess = onPostProcess,
            onAnimate = onAnimate,
        )
}

class MapboxMarkerRenderer(
    override val holder: MapboxMapViewHolder,
    override val coroutine: CoroutineScope,
    private val markerLayer: MarkerLayer,
    private val dragLayer: MarkerDragLayer,
) : AbstractMarkerRenderer<Feature>() {
    private val iconRefCounter: MutableMap<String, Int> = mutableMapOf()

    object Prop {
        const val ICON_ID = "icon_id"
        const val DEFAULT_MARKER_ID = "default"
        const val SCALE = "scale"
        const val ICON_ANCHOR = "icon-offset"
    }

    override fun init(markerOverlayManager: MarkerOverlayManager<Feature>) {
        super.init(markerOverlayManager)
        holder.map.getStyle { style ->
            style.addImage(Prop.DEFAULT_MARKER_ID, defaultIcon.bitmap)
        }
    }

    fun drawMarkerLayer() {
        val entities = markerOverlayManager.markerManager.allEntities()
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
        val entities = markerOverlayManager.markerManager.allEntities()
        val feature =
            Feature.fromGeometry(
                position.toPoint(),
                markerEntity.marker.properties(),
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

    override suspend fun addIcons(newMarkers: List<Pair<MarkerState, BitmapIcon>>): List<Feature> {
        val style =
            suspendCoroutine { continuation ->
                holder.map.getStyle { style ->
                    continuation.resumeWith(Result.success(style))
                }
            }

        newMarkers.forEach { (state, bitmapIcon) ->
            val iconKey = state.icon.hashCode().toString()
            if (!iconRefCounter.contains(iconKey)) {
                style.addImage(iconKey, bitmapIcon.bitmap)
                iconRefCounter[iconKey] = 0
            }
        }

        return newMarkers.map { (state, _) ->
            val featureId = state.id
            val position = state.position.toPoint()
            val properties =
                JsonObject().apply {
                    if (state.icon != null) {
                        state.icon?.let { icon ->
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
                    addProperty(Prop.SCALE, 1.0f)// state.icon?.scale ?: 1.0)
                }
            Feature.fromGeometry(position, properties, featureId)
        }
    }

    override suspend fun removeIcons(removeEntities: List<MarkerEntity<Feature>>) {
        val style =
            suspendCoroutine { continuation ->
                holder.map.getStyle { style ->
                    continuation.resumeWith(Result.success(style))
                }
            }

        removeEntities.forEach { entity ->
            entity.state.icon?.let { icon ->
                val iconKey = icon.hashCode().toString()
                val cnt = iconRefCounter.getOrDefault(iconKey, 1) - 1
                if (cnt == 0) {
                    iconRefCounter.remove(iconKey)
                    style.removeStyleImage(iconKey)
                } else {
                    iconRefCounter[iconKey] = cnt
                }
            }
        }
    }

    override suspend fun changeIcons(changes: List<UpdateParams<Feature>>): List<Feature> {
        val style =
            suspendCoroutine { continuation ->
                holder.map.getStyle { style ->
                    continuation.resumeWith(Result.success(style))
                }
            }

        return changes.map { params ->
            val prevFinger = params.prevEntity.fingerPrint
            val currFinger = params.entity.fingerPrint
            val prevProperties = params.prevEntity.marker.properties()

            val properties =
                JsonObject().apply {
                    addProperty(Prop.SCALE, params.entity.state.icon?.scale ?: 1.0f)
                    if (currFinger.icon == prevFinger.icon) {
                        addProperty(
                            Prop.ICON_ID,
                            prevProperties?.get(Prop.ICON_ID)?.asString ?: Prop.DEFAULT_MARKER_ID,
                        )

                        add(Prop.ICON_ANCHOR,
                            prevProperties?.get(Prop.ICON_ANCHOR) ?: getDefaultIconOffsetProperty(),
                        )
                    } else {
                        val iconKey = prevFinger.icon.toString()
                        val cnt = iconRefCounter.getOrDefault(iconKey, 1) - 1
                        if (cnt == 0) {
                            iconRefCounter.remove(iconKey)
                            style.removeStyleImage(iconKey)
                        } else {
                            iconRefCounter[iconKey] = cnt
                        }

                        if (currFinger.icon == null) {
                            addProperty(Prop.ICON_ID, Prop.DEFAULT_MARKER_ID)
                            add(Prop.ICON_ANCHOR, getDefaultIconOffsetProperty())
                        } else {
                            params.entity.state.icon?.let { icon ->
                                // icon id
                                val iconKey = icon.hashCode().toString()
                                if (iconRefCounter.contains(iconKey)) {
                                    iconRefCounter[iconKey] = (iconRefCounter[iconKey] ?: 0) + 1
                                } else {
                                    style.addImage(iconKey, params.bitmapIcon.bitmap)
                                    iconRefCounter[iconKey] = 1
                                }
                                addProperty(Prop.ICON_ID, iconKey)
                                add(Prop.ICON_ANCHOR, createIconOffset(icon))
                            }
                        }
                    }
                }

            val position =
                params.entity.state.position
                    .toPoint()
            val featureId = params.entity.state.id
            Feature.fromGeometry(position, properties, featureId)
        }
    }

    private fun getDefaultIconOffsetProperty(): JsonArray {
        return createIconOffset(defaultIcon)
    }

    private fun createIconOffset(icon: BitmapIcon): JsonArray {
        return JsonArray().apply {
            add(-(icon.size.width * icon.anchor.x) / ResourceProvider.getDensity())
            add(-(icon.size.height * icon.anchor.y) / ResourceProvider.getDensity())
        }
    }
    private fun createIconOffset(icon: MarkerIcon): JsonArray {
        return createIconOffset(icon.toBitmapIcon())
    }
}
