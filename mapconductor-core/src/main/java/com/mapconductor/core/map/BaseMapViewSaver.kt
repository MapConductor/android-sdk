package com.mapconductor.core.map

import android.os.Bundle
import androidx.compose.runtime.saveable.Saver
import com.mapconductor.core.features.GeoPoint

/**
 * Base class for MapView state savers
 * @param T MapViewState type
 */
abstract class BaseMapViewSaver<T : Any> {

    /**
     * Extract camera position from the state, handling null cases
     */
    protected abstract fun extractCameraPosition(state: T): MapCameraPosition?

    /**
     * Save map design type to bundle
     */
    protected abstract fun saveMapDesign(state: T, bundle: Bundle)

    /**
     * Create state instance from restored data
     */
    protected abstract fun createState(
        stateId: String,
        mapDesignBundle: Bundle?,
        cameraPosition: MapCameraPosition
    ): T

    /**
     * Get default camera position for fallback
     */
    protected open fun getDefaultCameraPosition(): MapCameraPosition = MapCameraPosition.Default

    /**
     * Get paddings for restored camera position (can be overridden by subclasses)
     */
    protected open fun getCameraPaddings(): MapPaddings? = null

    /**
     * Create the actual Saver instance
     */
    fun createSaver(): Saver<T, Bundle> = Saver(
        save = { state ->
            val cameraStateBundle = createCameraBundle(state)
            val mapDesignBundle = Bundle().apply {
                saveMapDesign(state, this)
            }

            Bundle().apply {
                putString("stateId", getStateId(state))
                putBundle("mapDesign", mapDesignBundle)
                putBundle("camera", cameraStateBundle)
            }
        },
        restore = { storedData ->
            val cameraBundle = storedData.getBundle("camera")
            val mapDesignBundle = storedData.getBundle("mapDesign")
            val stateId = storedData.getString("stateId")!!

            val cameraPosition = createCameraPositionFromBundle(cameraBundle)

            createState(stateId, mapDesignBundle, cameraPosition)
        }
    )

    /**
     * Extract state ID from the state object
     */
    protected abstract fun getStateId(state: T): String

    private fun createCameraBundle(state: T): Bundle? {
        val cameraState = extractCameraPosition(state)
        val defaultCamera = getDefaultCameraPosition()

        return cameraState?.let {
            Bundle().apply {
                putDouble("zoom", it.zoom)
                putDouble("tilt", it.tilt)
                putDouble("bearing", it.bearing)
                putDouble("latitude", it.position.latitude)
                putDouble("longitude", it.position.longitude)
            }
        } ?: Bundle().apply {
            putDouble("zoom", defaultCamera.zoom)
            putDouble("tilt", defaultCamera.tilt)
            putDouble("bearing", defaultCamera.bearing)
            putDouble("latitude", defaultCamera.position.latitude)
            putDouble("longitude", defaultCamera.position.longitude)
        }
    }

    private fun createCameraPositionFromBundle(cameraBundle: Bundle?): MapCameraPosition {
        return MapCameraPosition(
            position = GeoPoint.fromLatLong(
                latitude = cameraBundle?.getDouble("latitude") ?: 0.0,
                longitude = cameraBundle?.getDouble("longitude") ?: 0.0,
            ),
            zoom = cameraBundle?.getDouble("zoom") ?: 0.0,
            bearing = cameraBundle?.getDouble("bearing") ?: 0.0,
            tilt = cameraBundle?.getDouble("tilt") ?: 0.0,
            paddings = getCameraPaddings(),
        )
    }
}
