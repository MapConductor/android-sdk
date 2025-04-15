package com.mapconductor.googlemaps

import android.content.Context
import android.view.ViewGroup
import androidx.compose.runtime.LaunchedEffect
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

//class MapViewHolder private constructor(context: Context) {
//    private var destroyed = false
//    var googleMap: GoogleMap? = null
//
//    val mapView: MapView = MapView(context).apply {
//        onCreate(null)
//    }
//
//    internal suspend fun initAsync(timeoutMillis: Long = 10_000): Unit = withTimeout(timeoutMillis) {
//        suspendCoroutine { contract ->
//            try {
//                mapView.getMapAsync { googleMap ->
//                    this@MapViewHolder.googleMap = googleMap
//                    contract.resume(Unit)
//                }
//            } catch (e: Exception) {
//                contract.resumeWithException(e)
//            }
//        }
//    }
//
//    fun getMap(): GoogleMap {
//        return googleMap ?: throw IllegalStateException("GoogleMap is not initialized yet.")
//    }
//
//    fun destroy() {
//        if (destroyed) return
//
//        destroyed = true
//        mapView.onPause()
//        mapView.onDestroy()
//    }
//
//    companion object {
//        suspend fun create(context: Context): MapViewHolder {
//            val viewHolder = MapViewHolder(context)
//            viewHolder.initAsync()
//            return viewHolder
//        }
//    }
//}

class MapViewHolder private constructor(
    val mapView: MapView
) {
    lateinit var map: GoogleMap

    companion object {
        suspend fun create(context: Context): MapViewHolder {
            val mapView = MapView(context).apply { onCreate(null) }

            val holder = MapViewHolder(mapView)

            suspendCancellableCoroutine<Unit> { cont ->
                mapView.getMapAsync {
                    holder.map = it
                    cont.resume(Unit) {}
                }
            }

            return holder
        }
    }

    fun attachTo(container: ViewGroup) {
        if (mapView.parent === container) return
        (mapView.parent as? ViewGroup)?.removeView(mapView)
        container.addView(mapView)
    }

    fun destroy() {
        mapView.onPause()
        mapView.onDestroy()
    }
}
