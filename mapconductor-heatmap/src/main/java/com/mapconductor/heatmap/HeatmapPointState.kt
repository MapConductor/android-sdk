package com.mapconductor.heatmap

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.mapconductor.core.ComponentState
import com.mapconductor.core.features.GeoPointInterface
import java.io.Serializable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

class HeatmapPointState(
    position: GeoPointInterface,
    weight: Double = 1.0,
    id: String? = null,
    extra: Serializable? = null,
) : ComponentState {
    override val id =
        (
            id ?: heatmapPointId(
                listOf(
                    position.hashCode(),
                    extra?.hashCode() ?: 0,
                ),
            )
        ).toString()

    var position by mutableStateOf(position)
    var weight by mutableStateOf(weight)
    var extra by mutableStateOf(extra)

    private fun heatmapPointId(hashCodes: List<Int>): Int =
        hashCodes.reduce { result, hashCode ->
            31 * result + hashCode
        }

    fun fingerPrint(): HeatmapPointFingerPrint =
        HeatmapPointFingerPrint(
            id = id.hashCode(),
            position = position.hashCode(),
            weight = weight.hashCode(),
            extra = extra?.hashCode() ?: 0,
        )

    fun asFlow(): Flow<HeatmapPointFingerPrint> = snapshotFlow { fingerPrint() }.distinctUntilChanged()
}

data class HeatmapPointFingerPrint(
    val id: Int,
    val position: Int,
    val weight: Int,
    val extra: Int,
)
