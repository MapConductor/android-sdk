package com.mapconductor.here.raster

import com.here.sdk.mapview.datasource.RasterDataSource
import com.here.sdk.mapview.datasource.RasterDataSourceConfiguration
import com.here.sdk.mapview.datasource.TileUrlProviderCallback
import com.here.sdk.mapview.datasource.TilingScheme
import com.mapconductor.core.raster.RasterLayerController
import com.mapconductor.core.raster.RasterLayerManager
import com.mapconductor.core.raster.RasterLayerManagerInterface
import com.mapconductor.here.HereViewHolder
import java.util.concurrent.atomic.AtomicBoolean
import android.util.Log

class HereRasterLayerController(
    rasterLayerManager: RasterLayerManagerInterface<HereRasterLayerHandle> = RasterLayerManager(),
    renderer: HereRasterLayerOverlayRenderer,
) : RasterLayerController<HereRasterLayerHandle>(rasterLayerManager, renderer) {
    companion object {
        private const val TAG = "HereRasterLayer"
        private val networkWarmedUp = AtomicBoolean(false)
    }

    /**
     * Pre-warm HERE SDK's network stack by creating a temporary RasterDataSource.
     * This triggers HERE's internal network reachability checks early, reducing
     * latency when polygon raster layers are first created.
     */
    fun warmupNetworkIfNeeded(holder: HereViewHolder) {
        if (!networkWarmedUp.compareAndSet(false, true)) {
            return
        }
        try {
            Log.d(TAG, "Warming up HERE SDK network stack...")
            val urlProvider = TileUrlProviderCallback { _, _, _ -> "http://127.0.0.1:1/warmup" }
            val provider =
                RasterDataSourceConfiguration.Provider(
                    urlProvider,
                    TilingScheme.QUAD_TREE_MERCATOR,
                    listOf(0, 1, 2),
                )
            val cache =
                RasterDataSourceConfiguration.Cache(
                    holder.mapView.context.cacheDir.absolutePath + "/warmup",
                )
            val config =
                RasterDataSourceConfiguration(
                    "warmup-source",
                    provider,
                    cache,
                )
            // Creating the data source triggers HERE SDK's network initialization
            val dataSource = RasterDataSource(holder.mapView.mapContext, config)
            // Immediately destroy it - we just needed to trigger the network init
            dataSource.destroy()
            Log.d(TAG, "HERE SDK network warmup completed")
        } catch (e: Exception) {
            Log.w(TAG, "HERE SDK network warmup failed", e)
            // Reset so we can try again
            networkWarmedUp.set(false)
        }
    }
}
