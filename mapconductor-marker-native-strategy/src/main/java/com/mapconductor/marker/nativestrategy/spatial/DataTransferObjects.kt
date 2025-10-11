package com.mapconductor.marker.nativestrategy.spatial

import android.os.Parcel
import android.os.Parcelable

/**
 * Data transfer objects for communication between Kotlin and native C++ code.
 * These classes mirror the C++ structs defined in remote_spatial_marker_strategy.h
 */

internal data class NativeMarkerDataDTO(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val clickable: Boolean,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readByte() != 0.toByte(),
    )

    override fun writeToParcel(
        parcel: Parcel,
        flags: Int,
    ) {
        parcel.writeString(id)
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeByte(if (clickable) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<NativeMarkerDataDTO> {
        override fun createFromParcel(parcel: Parcel): NativeMarkerDataDTO = NativeMarkerDataDTO(parcel)

        override fun newArray(size: Int): Array<NativeMarkerDataDTO?> = arrayOfNulls(size)
    }
}

data class NativeSpatialConfigDTO(
    val expandMargin: Double,
    val addOnlyMode: Boolean,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readDouble(),
        parcel.readByte() != 0.toByte(),
    )

    override fun writeToParcel(
        parcel: Parcel,
        flags: Int,
    ) {
        parcel.writeDouble(expandMargin)
        parcel.writeByte(if (addOnlyMode) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<NativeSpatialConfigDTO> {
        override fun createFromParcel(parcel: Parcel): NativeSpatialConfigDTO = NativeSpatialConfigDTO(parcel)

        override fun newArray(size: Int): Array<NativeSpatialConfigDTO?> = arrayOfNulls(size)
    }
}

data class NativeSpatialResultDTO(
    val markersToAdd: Array<String> = emptyArray(),
    val markersToRemove: Array<String> = emptyArray(),
    val errors: Array<String> = emptyArray(),
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.createStringArray() ?: emptyArray(),
        parcel.createStringArray() ?: emptyArray(),
        parcel.createStringArray() ?: emptyArray(),
    )

    override fun writeToParcel(
        parcel: Parcel,
        flags: Int,
    ) {
        parcel.writeStringArray(markersToAdd)
        parcel.writeStringArray(markersToRemove)
        parcel.writeStringArray(errors)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<NativeSpatialResultDTO> {
        override fun createFromParcel(parcel: Parcel): NativeSpatialResultDTO = NativeSpatialResultDTO(parcel)

        override fun newArray(size: Int): Array<NativeSpatialResultDTO?> = arrayOfNulls(size)
    }

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

data class NativeCameraPositionDTO(
    val latitude: Double,
    val longitude: Double,
    val zoom: Double,
    val bearing: Double,
    val tilt: Double,
    val boundsMinLat: Double,
    val boundsMaxLat: Double,
    val boundsMinLng: Double,
    val boundsMaxLng: Double,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readDouble(),
    )

    override fun writeToParcel(
        parcel: Parcel,
        flags: Int,
    ) {
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeDouble(zoom)
        parcel.writeDouble(bearing)
        parcel.writeDouble(tilt)
        parcel.writeDouble(boundsMinLat)
        parcel.writeDouble(boundsMaxLat)
        parcel.writeDouble(boundsMinLng)
        parcel.writeDouble(boundsMaxLng)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<NativeCameraPositionDTO> {
        override fun createFromParcel(parcel: Parcel): NativeCameraPositionDTO = NativeCameraPositionDTO(parcel)

        override fun newArray(size: Int): Array<NativeCameraPositionDTO?> = arrayOfNulls(size)
    }
}

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
