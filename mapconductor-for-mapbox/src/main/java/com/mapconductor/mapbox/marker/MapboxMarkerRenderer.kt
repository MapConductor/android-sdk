package com.mapconductor.mapbox.marker

import androidx.compose.ui.geometry.Offset
import com.google.gson.JsonObject
import com.mapbox.geojson.Feature
import com.mapconductor.core.geocell.HexGeocell
import com.mapconductor.core.icons.Default
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
import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap

class DefaultMapboxMarkerRenderer : MarkerRendererFactory<Feature> {
    override fun create(
        hexGeocell: HexGeocell,
        onIconAdd: suspend (List<Pair<MarkerState, BitmapIcon>>) -> List<Feature?>,
        onIconRemove: suspend (List<MarkerEntity<Feature>>) -> Unit,
        onIconChange: suspend (List<UpdateParams<Feature>>) -> List<Feature>,
        onAnimate: suspend (MarkerEntity<Feature>) -> Unit,
        onPostProcess: (suspend () -> Unit)?
    ): MarkerOverlayManager<Feature> {
        return MarkerOverlayManagerImpl(
            markerManager = MarkerManager(hexGeocell),
            onRemove = onIconRemove,
            onAdd = onIconAdd,
            onChange = onIconChange,
            onPostProcess = onPostProcess,
            onAnimate = onAnimate,
        )
    }
}

class MapboxMarkerRenderer(
    private val holder: MapboxMapViewHolder,
): AbstractMarkerRenderer<Feature>() {
    private val iconRefCounter: MutableMap<String, Int> = mutableMapOf()
    private lateinit var defaultIcon: BitmapIcon

    object Prop {
        const val MARKER_ID = "id"
        const val ICON_ID = "icon_id"
        const val DEFAULT_MARKER_ID = "default"
    }

    override fun init(markerManager: MarkerManager<Feature>) {
        holder.map.getStyle { style ->
            defaultIcon = markerManager.createBitmapIcon(MarkerIcon.Default())
            style.addImage(Prop.DEFAULT_MARKER_ID, defaultIcon.bitmap)
        }
    }

    override suspend fun addIcons(newMarkers: List<Pair<MarkerState, BitmapIcon>>): List<Feature> {
        val style = suspendCoroutine { continuation ->
            holder.map.getStyle { style ->
                continuation.resumeWith(Result.success(style))
            }
        }

        newMarkers.forEach { (_, icon) ->
            val iconKey = icon.hashCode().toString()
            if (!iconRefCounter.contains(iconKey)) {
                val adjusted = adjustIconForAnchor(icon)
                style.addImage(iconKey, adjusted.bitmap)
            }
        }

        return newMarkers.map { (state, _) ->
            Feature.fromGeometry(
                state.position.toPoint(),
                JsonObject().apply {
                    addProperty(Prop.MARKER_ID, state.id)
                    if (state.icon != null) {
                        state.icon?.let { icon ->
                            val iconKey = icon.hashCode().toString()
                            iconRefCounter[Prop.ICON_ID] = iconRefCounter.getOrDefault(iconKey, 0) + 1
                        }
                    } else {
                        addProperty(Prop.ICON_ID, Prop.DEFAULT_MARKER_ID)
                    }
                }
            )
        }
    }

    override suspend fun removeIcons(removeEntities: List<MarkerEntity<Feature>>) {
        val style = suspendCoroutine { continuation ->
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
        val style = suspendCoroutine { continuation ->
            holder.map.getStyle { style ->
                continuation.resumeWith(Result.success(style))
            }
        }

        return changes.map { params ->
            val prevFinger = params.prevEntity.fingerPrint
            val currFinger = params.entity.fingerPrint
            val prevProperties = params.prevEntity.marker.properties()

            val properties = JsonObject().apply {
                addProperty(Prop.MARKER_ID, params.entity.state.id)

                if (currFinger.icon == prevFinger.icon) {
                    addProperty(Prop.ICON_ID,
                        prevProperties?.get(Prop.ICON_ID)?.asString ?: Prop.DEFAULT_MARKER_ID)
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
                    } else {
                        params.entity.state.icon?.let { icon ->
                            val iconKey = icon.hashCode().toString()
                            if (iconRefCounter.contains(iconKey)) {
                                iconRefCounter[iconKey] = (iconRefCounter[iconKey] ?: 0) + 1
                            } else {
                                val adjusted = adjustIconForAnchor(params.bitmapIcon)
                                style.addImage(iconKey, adjusted.bitmap)
                                iconRefCounter[iconKey] = 1
                            }
                            addProperty(Prop.ICON_ID, iconKey)
                        }
                    }
                }
            }

            Feature.fromGeometry(params.entity.state.position.toPoint(), properties)
        }
    }

    private fun adjustIconForAnchor(icon: BitmapIcon): BitmapIcon {
        // BaseMapViewControllerのadjustIconForAnchorメソッドと同じ実装
        val offsetX = 0.5 - icon.anchor.x.toDouble()
        val offsetY = 0.5 - icon.anchor.y.toDouble()
        val dx = offsetX * icon.size.width
        val dy = offsetY * icon.size.height
        val width = icon.size.width + kotlin.math.abs(dx)
        val height = icon.size.height + kotlin.math.abs(dy)

        val bitmap = createBitmap(width.toInt(), height.toInt(), Bitmap.Config.ARGB_8888)
        val paint = android.graphics.Paint().apply { isAntiAlias = true }

        android.graphics.Canvas(bitmap).apply {
            drawBitmap(icon.bitmap, kotlin.math.abs(dx).toFloat(), kotlin.math.abs(dy).toFloat(), paint)
        }

        return BitmapIcon(
            bitmap = bitmap,
            anchor = Offset((width * 0.5).toFloat(), height.toFloat()),
            size = androidx.compose.ui.geometry.Size(width.toFloat(), height.toFloat())
        )
    }
}
