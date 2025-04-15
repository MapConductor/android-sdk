package com.mapconductor.googlemaps

import android.content.Context
import android.view.ViewGroup
import androidx.compose.runtime.LaunchedEffect
import androidx.core.app.ComponentActivity
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
object MapViewHolderStore {
    private val holders = mutableMapOf<String, MapViewHolder>()

    suspend fun getOrCreate(id: String, context: Context): MapViewHolder {
        val existing = holders[id]
        if (existing != null) return existing

        val newHolder = MapViewHolder.create(context.applicationContext)
        holders[id] = newHolder
        return newHolder
    }

    fun has(id: String): Boolean {
        return holders.containsKey(id)
    }

    fun get(id: String): MapViewHolder? = holders[id]

    fun clear(id: String) {
        holders.remove(id)?.destroy()
    }

    fun clearAll() {
        holders.values.forEach { it.destroy() }
        holders.clear()
    }
}
