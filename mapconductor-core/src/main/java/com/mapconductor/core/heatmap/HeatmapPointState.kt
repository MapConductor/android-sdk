package com.mapconductor.core.heatmap

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.mapconductor.core.features.GeoPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

class HeatmapPointState(
    position: GeoPoint,
    weight: Double = 1.0,
    id: String? = null,
) {
    val id =
        (
            id ?: pointId(
                listOf(
                    position.hashCode(),
                    weight.hashCode(),
                ),
            )
        ).toString()

    private fun pointId(hashCodes: List<Int>): Int =
        hashCodes.reduce { result, hashCode ->
            31 * result + hashCode
        }

    private val currentPosition = mutableStateOf(position)
    var position: GeoPoint
        get() = currentPosition.value
        set(value) {
            currentPosition.value = value
        }

    var weight by mutableStateOf(weight)

    fun copy(
        id: String? = this.id,
        position: GeoPoint = this.position,
        weight: Double = this.weight,
    ): HeatmapPointState =
        HeatmapPointState(
            id = id,
            position = position,
            weight = weight,
        )

    override fun equals(other: Any?): Boolean {
        val otherState = other as? HeatmapPointState ?: return false
        return hashCode() == otherState.hashCode()
    }

    override fun hashCode(): Int {
        var result = weight.hashCode()
        result = 31 * result + currentPosition.value.latitude.hashCode()
        result = 31 * result + currentPosition.value.longitude.hashCode()
        result = 31 * result + currentPosition.value.altitude.hashCode()
        return result
    }

    fun fingerPrint(): HeatmapPointFingerPrint =
        HeatmapPointFingerPrint(
            id.hashCode(),
            weight.hashCode(),
            currentPosition.value.latitude.hashCode(),
            currentPosition.value.longitude.hashCode(),
            currentPosition.value.altitude.hashCode(),
        )

    fun asFlow(): Flow<HeatmapPointFingerPrint> = snapshotFlow { fingerPrint() }.distinctUntilChanged()
}

data class HeatmapPointFingerPrint(
    val id: Int,
    val weight: Int,
    val latitude: Int,
    val longitude: Int,
    val altitude: Int,
)
