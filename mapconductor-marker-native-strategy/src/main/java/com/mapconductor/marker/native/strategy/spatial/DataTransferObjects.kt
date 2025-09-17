package com.mapconductor.marker.strategy.strategy.spatial

/**
 * Data transfer objects for communication between Kotlin and native C++ code.
 * These classes mirror the C++ structs defined in remote_spatial_marker_strategy.h
 */

data class NativeMarkerDataDTO(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val clickable: Boolean,
)

data class NativeSpatialConfigDTO(
    val expandMargin: Double,
    val addOnlyMode: Boolean,
)

data class NativeSpatialResultDTO(
    val markersToAdd: Array<String> = emptyArray(),
    val markersToRemove: Array<String> = emptyArray(),
    val errors: Array<String> = emptyArray(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NativeSpatialResultDTO

        if (!markersToAdd.contentEquals(other.markersToAdd)) return false
        if (!markersToRemove.contentEquals(other.markersToRemove)) return false
        if (!errors.contentEquals(other.errors)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = markersToAdd.contentHashCode()
        result = 31 * result + markersToRemove.contentHashCode()
        result = 31 * result + errors.contentHashCode()
        return result
    }
}

data class NativeGeoRectBounds(
    val south: Double,
    val north: Double,
    val west: Double,
    val east: Double,
)

data class CameraPosition(
    val latitude: Double,
    val longitude: Double,
    val zoom: Double,
    val bearing: Double,
    val tilt: Double,
    val visibleBounds: NativeGeoRectBounds,
)

data class PerformanceStats(
    val totalCameraChanges: Long,
    val totalMarkersProcessed: Long,
    val totalSpatialQueries: Long,
    val totalBatchUpdates: Long,
    val averageQueryTimeMs: Double,
    val averageBatchProcessTimeMs: Double,
    val currentMarkerCount: Long,
    val renderedMarkerCount: Long,
) {
    companion object {
        fun parseFromString(statsString: String): PerformanceStats {
            val lines = statsString.split("\n")
            val values =
                lines.associate { line ->
                    val parts = line.split(": ")
                    parts[0] to parts[1]
                }

            return PerformanceStats(
                totalCameraChanges = values["totalCameraChanges"]?.toLongOrNull() ?: 0L,
                totalMarkersProcessed = values["totalMarkersProcessed"]?.toLongOrNull() ?: 0L,
                totalSpatialQueries = values["totalSpatialQueries"]?.toLongOrNull() ?: 0L,
                totalBatchUpdates = values["totalBatchUpdates"]?.toLongOrNull() ?: 0L,
                averageQueryTimeMs = values["averageQueryTimeMs"]?.toDoubleOrNull() ?: 0.0,
                averageBatchProcessTimeMs = values["averageBatchProcessTimeMs"]?.toDoubleOrNull() ?: 0.0,
                currentMarkerCount = values["currentMarkerCount"]?.toLongOrNull() ?: 0L,
                renderedMarkerCount = values["renderedMarkerCount"]?.toLongOrNull() ?: 0L,
            )
        }
    }
}
