package com.mapconductor.mapbox.marker

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.calculateZIndex
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.marker.AbstractMarkerOverlayRenderer
import com.mapconductor.core.marker.BitmapIcon
import com.mapconductor.core.marker.DefaultMarkerIcon
import com.mapconductor.core.marker.MarkerEntityInterface
import com.mapconductor.core.marker.MarkerIconInterface
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.marker.MarkerOverlayRendererInterface
import com.mapconductor.mapbox.MapboxActualMarker
import com.mapconductor.mapbox.MapboxMapViewHolder
import com.mapconductor.mapbox.toPoint
import android.graphics.Bitmap
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield

class MapboxMarkerOverlayRenderer(
    holder: MapboxMapViewHolder,
    val markerManager: MarkerManager<MapboxActualMarker>,
    val markerLayer: MarkerLayer,
    val dragLayer: MarkerDragLayer,
    coroutine: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : AbstractMarkerOverlayRenderer<
        MapboxMapViewHolder,
        MapboxActualMarker,
    >(
        holder = holder,
        coroutine = coroutine,
    ) {
    private val iconRefCounter: MutableMap<String, Int> = mutableMapOf()
    private val pendingStyleImageRemovals: MutableMap<String, Long> = mutableMapOf()
    private val iconBitmapCache: MutableMap<String, Bitmap> = mutableMapOf()
    private val defaultMarkerIcon: BitmapIcon = DefaultMarkerIcon().toBitmapIcon()

    object Prop {
        const val ICON_ID = "icon_id"
        const val DEFAULT_MARKER_ID = "default"
        const val SCALE = "scale"
        const val ICON_ANCHOR = "icon-offset"
        const val Z_INDEX = "zIndex"
    }

    init {
        holder.map.getStyle { style ->
            style.addImage(Prop.DEFAULT_MARKER_ID, defaultMarkerIcon.bitmap)
        }
    }

    private suspend fun getStyle(): com.mapbox.maps.Style =
        suspendCoroutine { continuation ->
            holder.map.getStyle { style ->
                continuation.resumeWith(Result.success(style))
            }
        }

    private fun decrementIconRef(iconKey: String) {
        if (iconKey == Prop.DEFAULT_MARKER_ID) return
        val next = (iconRefCounter[iconKey] ?: 0) - 1
        if (next <= 0) {
            iconRefCounter.remove(iconKey)
            // Mapbox style rendering can lag behind GeoJSON updates; keep images around briefly
            // after last use to avoid "Required image ... is missing" warnings during fast zoom.
            pendingStyleImageRemovals[iconKey] = System.currentTimeMillis() + STYLE_IMAGE_REMOVAL_GRACE_MS
        } else {
            iconRefCounter[iconKey] = next
        }
    }

    private fun incrementIconRef(iconKey: String) {
        if (iconKey == Prop.DEFAULT_MARKER_ID) return
        pendingStyleImageRemovals.remove(iconKey)
        iconRefCounter[iconKey] = (iconRefCounter[iconKey] ?: 0) + 1
    }

    fun onStyleImageMissing(imageId: String) {
        coroutine.launch(Dispatchers.Main) {
            val style = holder.map.style ?: return@launch
            if (imageId == Prop.DEFAULT_MARKER_ID) {
                try {
                    style.addImage(Prop.DEFAULT_MARKER_ID, defaultMarkerIcon.bitmap)
                } catch (_: Exception) {
                }
                return@launch
            }

            pendingStyleImageRemovals.remove(imageId)

            val cached = iconBitmapCache[imageId]
            if (cached != null) {
                try {
                    style.addImage(imageId, cached)
                } catch (_: Exception) {
                }
                return@launch
            }

            // Fallback: regenerate from the marker state if we can find it.
            val icon =
                markerManager
                    .allEntities()
                    .firstOrNull { it.state.icon?.hashCode()?.toString() == imageId }
                    ?.state
                    ?.icon
            if (icon != null) {
                try {
                    style.addImage(imageId, icon.toBitmapIcon().bitmap)
                } catch (_: Exception) {
                }
            }
        }
    }

    // Ensure default and custom marker images exist on the given style (used after style reload)
    fun ensureStyleImages(style: com.mapbox.maps.Style) {
        try {
            style.addImage(Prop.DEFAULT_MARKER_ID, defaultMarkerIcon.bitmap)
        } catch (_: Exception) {
            // Image may already exist; ignore
        }

        // Re-add custom icon images for existing markers
        try {
            markerManager
                .allEntities()
                .forEach { entity ->
                    entity.state.icon?.let { icon ->
                        val iconKey = icon.hashCode().toString()
                        // Recreate bitmap from icon definition
                        val bmp = icon.toBitmapIcon().bitmap
                        try {
                            style.addImage(iconKey, bmp)
                        } catch (_: Exception) {
                        }
                    }
                }
        } catch (_: Exception) {
            // Style might be in transition; ignore quietly
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
        markerEntity: MarkerEntityInterface<Feature>,
        position: GeoPoint,
    ) {
        val entities = markerManager.allEntities()
        val props = (markerEntity.marker?.properties() ?: JsonObject()).deepCopy()
        props.addProperty(
            Prop.Z_INDEX,
            markerEntity.state.zIndex ?: calculateZIndex(position),
        )
        val feature =
            Feature.fromGeometry(
                position.toPoint(),
                props,
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

    override suspend fun onAdd(data: List<MarkerOverlayRendererInterface.AddParamsInterface>): List<Feature> =
        withContext(Dispatchers.Main) {
            val style = getStyle()

            data.forEach {
                it.state.icon?.let { icon ->
                    val iconKey =
                        icon
                            .hashCode()
                            .toString()
                    // Ensure the image exists in this style; the ref counter only tracks usage.
                    try {
                        style.addImage(iconKey, it.bitmapIcon.bitmap)
                    } catch (_: Exception) {
                    }
                    iconBitmapCache[iconKey] = it.bitmapIcon.bitmap
                    if (!iconRefCounter.contains(iconKey)) iconRefCounter[iconKey] = 0
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
                                incrementIconRef(iconKey)
                                addProperty(Prop.ICON_ID, iconKey)
                                // icon offset property
                                add(Prop.ICON_ANCHOR, createIconOffset(icon))
                            }
                        } else {
                            addProperty(Prop.ICON_ID, Prop.DEFAULT_MARKER_ID)
                            add(Prop.ICON_ANCHOR, getDefaultIconOffsetProperty())
                        }
                        addProperty(Prop.SCALE, it.state.icon?.scale ?: 1.0)
                        addProperty(Prop.Z_INDEX, it.state.zIndex ?: calculateZIndex(it.state.position))
                    }
                Feature.fromGeometry(position, properties, featureId)
            }
        }

    override suspend fun onRemove(data: List<MarkerEntityInterface<MapboxActualMarker>>) {
        withContext(Dispatchers.Main) {
            data.forEach { entity ->
                val iconKey =
                    entity.marker
                        ?.properties()
                        ?.get(Prop.ICON_ID)
                        ?.asString
                        ?: entity.state.icon?.hashCode()?.toString()
                if (iconKey != null) {
                    // Defer style image removal until after the GeoJSON source is updated.
                    decrementIconRef(iconKey)
                }
            }
        }
    }

    override suspend fun onPostProcess() {
        withContext(Dispatchers.Main) {
            // Update the source first, then remove unused images.
            // Removing images too early can produce "[maps-core/style]: Required image ... is missing".
            markerLayer.draw(markerManager.allEntities())
            yield()

            val style = holder.map.style ?: return@withContext
            val now = System.currentTimeMillis()
            val expired =
                pendingStyleImageRemovals
                    .asSequence()
                    .filter { (_, deadline) -> deadline <= now }
                    .map { (key, _) -> key }
                    .toList()

            expired.forEach { iconKey ->
                if (iconRefCounter.containsKey(iconKey)) {
                    pendingStyleImageRemovals.remove(iconKey)
                    return@forEach
                }
                try {
                    style.removeStyleImage(iconKey)
                } catch (_: Exception) {
                } finally {
                    pendingStyleImageRemovals.remove(iconKey)
                    iconBitmapCache.remove(iconKey)
                }
            }
        }
    }

    override suspend fun onChange(
        data: List<MarkerOverlayRendererInterface.ChangeParamsInterface<MapboxActualMarker>>,
    ): List<MapboxActualMarker?> =
        withContext(Dispatchers.Main) {
            val style = getStyle()

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
                    addProperty(
                        Prop.Z_INDEX,
                        params.current.state.zIndex ?: calculateZIndex(params.current.state.position),
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
                        val prevIconKey =
                            prevProperties?.get(Prop.ICON_ID)?.asString
                                ?: params.prev.state.icon?.hashCode()?.toString()
                                ?: Prop.DEFAULT_MARKER_ID
                        decrementIconRef(prevIconKey)

                        if (params.current.state.icon == null) {
                            addProperty(Prop.ICON_ID, Prop.DEFAULT_MARKER_ID)
                            add(Prop.ICON_ANCHOR, getDefaultIconOffsetProperty())
                        } else {
                            params.current.state.icon?.let { icon ->
                                // icon id
                                val iconKey = icon.hashCode().toString()
                                // Ensure the image exists in this style (it may have been reloaded).
                                try {
                                    style.addImage(iconKey, params.bitmapIcon.bitmap)
                                } catch (_: Exception) {
                                }
                                iconBitmapCache[iconKey] = params.bitmapIcon.bitmap
                                if (!iconRefCounter.contains(iconKey)) iconRefCounter[iconKey] = 0
                                incrementIconRef(iconKey)
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
        }

    private fun getDefaultIconOffsetProperty(): JsonArray = createIconOffset(defaultMarkerIcon)

    private fun createIconOffset(icon: BitmapIcon): JsonArray =
        JsonArray().apply {
            add(-(icon.size.width * icon.anchor.x) / ResourceProvider.getDensity())
            add(-(icon.size.height * icon.anchor.y) / ResourceProvider.getDensity())
        }

    private fun createIconOffset(icon: MarkerIconInterface): JsonArray = createIconOffset(icon.toBitmapIcon())
}

private const val STYLE_IMAGE_REMOVAL_GRACE_MS: Long = 1500L
