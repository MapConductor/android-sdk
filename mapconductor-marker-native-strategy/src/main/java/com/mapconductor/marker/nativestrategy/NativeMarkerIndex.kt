package com.mapconductor.marker.nativestrategy

import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.features.GeoRectBounds

internal class NativeMarkerIndex private constructor(
    private val nativeHandle: Long,
) {
    companion object {
        init {
            System.loadLibrary("mapconductor-native")
        }

        fun create(
            baseHexSideLength: Int = 1000,
            zoom: Double = 20.0,
        ): NativeMarkerIndex {
            val handle = nativeCreate(baseHexSideLength, zoom)
            if (handle == 0L) {
                throw RuntimeException("Failed to create native marker index")
            }
            return NativeMarkerIndex(handle)
        }

        @JvmStatic
        private external fun nativeCreate(
            baseHexSideLength: Int,
            zoom: Double,
        ): Long

        @JvmStatic
        private external fun nativeDestroy(handle: Long)

        @JvmStatic
        private external fun nativeRegisterMarker(
            handle: Long,
            id: String,
            latitude: Double,
            longitude: Double,
            clickable: Boolean,
        )

        @JvmStatic
        private external fun nativeUpdateMarker(
            handle: Long,
            id: String,
            latitude: Double,
            longitude: Double,
            clickable: Boolean,
        )

        @JvmStatic
        private external fun nativeRemoveMarker(
            handle: Long,
            id: String,
        ): Boolean

        @JvmStatic
        private external fun nativeHasMarker(
            handle: Long,
            id: String,
        ): Boolean

        @JvmStatic
        private external fun nativeFindNearest(
            handle: Long,
            latitude: Double,
            longitude: Double,
        ): String?

        @JvmStatic
        private external fun nativeFindMarkersInBounds(
            handle: Long,
            minLat: Double,
            maxLat: Double,
            minLng: Double,
            maxLng: Double,
        ): Array<String>

        @JvmStatic
        private external fun nativeFindByIdPrefix(
            handle: Long,
            prefix: String,
        ): Array<String>

        @JvmStatic
        private external fun nativeClear(handle: Long)

        @JvmStatic
        private external fun nativeMarkerCount(handle: Long): Long

        @JvmStatic
        private external fun nativeMetersPerPixel(
            handle: Long,
            latitude: Double,
            longitude: Double,
            zoom: Double,
            pixels: Double,
            tileSize: Int,
        ): Double
    }

    @Volatile
    private var isDestroyed = false

    fun registerMarker(
        id: String,
        position: GeoPointInterface,
        clickable: Boolean = true,
    ) {
        checkNotDestroyed()
        nativeRegisterMarker(nativeHandle, id, position.latitude, position.longitude, clickable)
    }

    fun updateMarker(
        id: String,
        position: GeoPointInterface,
        clickable: Boolean = true,
    ) {
        checkNotDestroyed()
        nativeUpdateMarker(nativeHandle, id, position.latitude, position.longitude, clickable)
    }

    fun removeMarker(id: String): Boolean {
        checkNotDestroyed()
        return nativeRemoveMarker(nativeHandle, id)
    }

    fun hasMarker(id: String): Boolean {
        checkNotDestroyed()
        return nativeHasMarker(nativeHandle, id)
    }

    fun findNearest(position: GeoPointInterface): String? {
        checkNotDestroyed()
        return nativeFindNearest(nativeHandle, position.latitude, position.longitude)
    }

    fun findMarkersInBounds(bounds: GeoRectBounds): List<String> {
        checkNotDestroyed()
        if (bounds.isEmpty) return emptyList()

        return nativeFindMarkersInBounds(
            nativeHandle,
            bounds.southWest!!.latitude,
            bounds.northEast!!.latitude,
            bounds.southWest!!.longitude,
            bounds.northEast!!.longitude,
        ).toList()
    }

    fun findByIdPrefix(prefix: String): List<String> {
        checkNotDestroyed()
        return nativeFindByIdPrefix(nativeHandle, prefix).toList()
    }

    fun clear() {
        checkNotDestroyed()
        nativeClear(nativeHandle)
    }

    fun markerCount(): Long {
        checkNotDestroyed()
        return nativeMarkerCount(nativeHandle)
    }

    fun metersPerPixel(
        position: GeoPointInterface,
        zoom: Double,
        pixels: Double,
        tileSize: Int = 256,
    ): Double {
        checkNotDestroyed()
        return nativeMetersPerPixel(nativeHandle, position.latitude, position.longitude, zoom, pixels, tileSize)
    }

    fun destroy() {
        if (!isDestroyed) {
            isDestroyed = true
            nativeDestroy(nativeHandle)
        }
    }

    private fun checkNotDestroyed() {
        if (isDestroyed) {
            throw IllegalStateException("NativeMarkerIndex has been destroyed")
        }
    }

    protected fun finalize() {
        destroy()
    }
}
