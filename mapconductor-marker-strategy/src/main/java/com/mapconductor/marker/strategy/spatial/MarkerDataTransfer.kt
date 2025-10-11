package com.mapconductor.marker.strategy.spatial

import android.os.Parcel
import android.os.Parcelable

/**
 * Data transfer object for passing marker information between processes.
 * Contains only essential data needed for spatial calculations.
 */
internal data class MarkerDataDTO(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val clickable: Boolean = true,
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

    companion object CREATOR : Parcelable.Creator<MarkerDataDTO> {
        override fun createFromParcel(parcel: Parcel): MarkerDataDTO = MarkerDataDTO(parcel)

        override fun newArray(size: Int): Array<MarkerDataDTO?> = arrayOfNulls(size)
    }
}

/**
 * Data transfer object for camera position information.
 */
data class CameraPositionDTO(
    val centerLatitude: Double,
    val centerLongitude: Double,
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
        parcel.writeDouble(centerLatitude)
        parcel.writeDouble(centerLongitude)
        parcel.writeDouble(zoom)
        parcel.writeDouble(bearing)
        parcel.writeDouble(tilt)
        parcel.writeDouble(boundsMinLat)
        parcel.writeDouble(boundsMaxLat)
        parcel.writeDouble(boundsMinLng)
        parcel.writeDouble(boundsMaxLng)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<CameraPositionDTO> {
        override fun createFromParcel(parcel: Parcel): CameraPositionDTO = CameraPositionDTO(parcel)

        override fun newArray(size: Int): Array<CameraPositionDTO?> = arrayOfNulls(size)
    }
}

/**
 * Data transfer object for spatial calculation results.
 */
data class SpatialResultDTO(
    val markersToAdd: List<String>,
    val markersToRemove: List<String>,
    val markersToUpdate: List<String>,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.createStringArrayList() ?: emptyList(),
        parcel.createStringArrayList() ?: emptyList(),
        parcel.createStringArrayList() ?: emptyList(),
    )

    override fun writeToParcel(
        parcel: Parcel,
        flags: Int,
    ) {
        parcel.writeStringList(markersToAdd)
        parcel.writeStringList(markersToRemove)
        parcel.writeStringList(markersToUpdate)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<SpatialResultDTO> {
        override fun createFromParcel(parcel: Parcel): SpatialResultDTO = SpatialResultDTO(parcel)

        override fun newArray(size: Int): Array<SpatialResultDTO?> = arrayOfNulls(size)
    }
}

/**
 * Configuration for spatial rendering strategy in background process.
 */
data class SpatialConfigDTO(
    val expandMargin: Double,
    val addOnlyMode: Boolean,
    val baseHexSideLength: Int = 1000,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readDouble(),
        parcel.readByte() != 0.toByte(),
        parcel.readInt(),
    )

    override fun writeToParcel(
        parcel: Parcel,
        flags: Int,
    ) {
        parcel.writeDouble(expandMargin)
        parcel.writeByte(if (addOnlyMode) 1 else 0)
        parcel.writeInt(baseHexSideLength)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<SpatialConfigDTO> {
        override fun createFromParcel(parcel: Parcel): SpatialConfigDTO = SpatialConfigDTO(parcel)

        override fun newArray(size: Int): Array<SpatialConfigDTO?> = arrayOfNulls(size)
    }
}
