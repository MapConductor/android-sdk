package com.mapconductor.core.map

import androidx.compose.runtime.saveable.Saver
import com.mapconductor.core.features.GeoPoint
import android.os.Bundle

/**
 * Base class for MapView state savers
 * @param T MapViewStateInterface type
 */
abstract class BaseMapViewSaver<T : MapViewStateInterface<*>> {
    /**
     * Save map design type to bundle
     */
    protected abstract fun saveMapDesign(
        state: T,
        bundle: Bundle,
    )

    /**
     * Create state instance from restored data
     */
    protected abstract fun createState(
        stateId: String,
        mapDesignBundle: Bundle?,
        cameraPosition: MapCameraPosition,
    ): T

    /**
     * Get paddings for restored camera position (can be overridden by subclasses)
     */
    protected open fun getCameraPaddings(): MapPaddingsInterface? = null

    /**
     * Create the actual Saver instance
     */
    fun createSaver(): Saver<T, Bundle> =
        Saver(
            save = { state ->
                val cameraStateBundle = createCameraBundle(state)
                val mapDesignBundle =
                    Bundle().apply {
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
            },
        )

    /**
     * Extract state ID from the state object
     */
    protected abstract fun getStateId(state: T): String

    private fun createCameraBundle(state: T): Bundle =
        state.cameraPosition.let {
            Bundle().apply {
                putDouble("zoom", it.zoom)
                putDouble("tilt", it.tilt)
                putDouble("bearing", it.bearing)
                putDouble("latitude", it.position.latitude)
                putDouble("longitude", it.position.longitude)
            }
        }

    private fun createCameraPositionFromBundle(cameraBundle: Bundle?): MapCameraPosition =
        MapCameraPosition(
            position =
                GeoPoint.fromLatLong(
                    latitude = cameraBundle?.getDouble("latitude") ?: 0.0,
                    longitude = cameraBundle?.getDouble("longitude") ?: 0.0,
                ),
            zoom = cameraBundle?.getDouble("zoom") ?: 0.0,
            bearing = cameraBundle?.getDouble("bearing") ?: 0.0,
            tilt = cameraBundle?.getDouble("tilt") ?: 0.0,
            paddings = getCameraPaddings(),
        )
}
