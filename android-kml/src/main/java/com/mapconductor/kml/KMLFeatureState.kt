package com.mapconductor.kml

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.mapconductor.core.ComponentState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

class KMLFeatureState(
    featureId: String? = null,
    geometry: KMLGeometry,
    properties: Map<String, Any?> = emptyMap(),
    strokeColor: Int? = null,
    fillColor: Int? = null,
    strokeWidth: Float? = null,
    pointRadius: Float? = null,
    visible: Boolean = true,
) : ComponentState {
    override val id: String = featureId ?: buildDefaultId(geometry, properties)

    var geometry by mutableStateOf(geometry)
    var properties by mutableStateOf(properties)
    var strokeColor by mutableStateOf(strokeColor)
    var fillColor by mutableStateOf(fillColor)
    var strokeWidth by mutableStateOf(strokeWidth)
    var pointRadius by mutableStateOf(pointRadius)
    var visible by mutableStateOf(visible)

    fun fingerPrint(): KMLFeatureFingerPrint =
        KMLFeatureFingerPrint(
            id = id.hashCode(),
            geometry = geometry.hashCode(),
            properties = properties.hashCode(),
            style = styleHashCode(),
            visible = visible.hashCode(),
        )

    fun asFlow(): Flow<KMLFeatureFingerPrint> = snapshotFlow { fingerPrint() }.distinctUntilChanged()

    private fun styleHashCode(): Int {
        var h = strokeColor.hashCode()
        h = 31 * h + fillColor.hashCode()
        h = 31 * h + strokeWidth.hashCode()
        h = 31 * h + pointRadius.hashCode()
        return h
    }

    companion object {
        private fun buildDefaultId(
            geometry: KMLGeometry,
            properties: Map<String, Any?>,
        ): String {
            var h = geometry.hashCode()
            h = 31 * h + properties.hashCode()
            return h.toString()
        }
    }
}

data class KMLFeatureFingerPrint(
    val id: Int,
    val geometry: Int,
    val properties: Int,
    val style: Int,
    val visible: Int,
)
