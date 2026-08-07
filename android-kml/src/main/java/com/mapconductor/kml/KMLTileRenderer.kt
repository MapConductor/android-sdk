package com.mapconductor.kml

import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.tileserver.TileProviderInterface
import com.mapconductor.core.tileserver.TileRequest
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue
import android.graphics.Canvas
import android.util.LruCache

/**
 * KML をタイルへ描くタイルプロバイダ。
 *
 * このクラスが持つのは**元データの保持とタイル要求の段取り**だけで、
 * 実際の計算は責務ごとのファイルにある:
 *
 * | 部品                        | 担当                                      |
 * |-----------------------------|-------------------------------------------|
 * | [KMLWorld]                  | 緯度経度→世界座標、範囲、間引き           |
 * | [KMLRenderFeatureBuilder]   | スタイル解決と描画用フィーチャーの組み立て|
 * | [KMLSpatialIndex]           | タイルにかかるフィーチャーの絞り込み      |
 * | [KMLTilePainter]            | Canvas への描画と PNG 化                  |
 * | [KMLHitTester]              | クリック位置の当たり判定                  |
 *
 * android-geojson-layer と同じ責務分けにしてある（KML は ios / react に
 * 相当物が無く android 単独。todo の「片側欠落」参照）。
 */
class KMLTileRenderer(
    val tileSize: Int = KMLDefaults.DEFAULT_TILE_SIZE,
    cacheSizeKb: Int = DEFAULT_CACHE_SIZE_KB,
    maxConcurrentRenders: Int = DEFAULT_MAX_CONCURRENT_RENDERS,
) : TileProviderInterface {
    private val cacheLock = Any()
    private val cache =
        object : LruCache<String, ByteArray>(cacheSizeKb) {
            override fun sizeOf(
                key: String,
                value: ByteArray,
            ): Int = (value.size / 1024).coerceAtLeast(1)
        }

    private val emptyTileMarker = ByteArray(1)
    private val inFlight = ConcurrentHashMap<String, CompletableFuture<ByteArray?>>()
    private val renderQueue = LinkedBlockingQueue<RenderJob>(MAX_QUEUE_SIZE)
    private val workerCount = maxConcurrentRenders.coerceIn(1, MAX_CONCURRENT_RENDERS)

    private val painter = KMLTilePainter(tileSize)
    private val hitTester = KMLHitTester()

    @Volatile private var cacheEpoch = 0L

    @Volatile private var state = TileState(emptyList(), null)

    init {
        repeat(workerCount) { index ->
            Thread({ renderLoop() }, "KMLTileRenderer-$index").apply {
                isDaemon = true
                start()
            }
        }
    }

    @JvmName("updateDynamic")
    fun update(
        features: List<KMLFeatureState>,
        layerStyle: LayerStyle,
        styleProvider: KMLStyleProviderInterface = DefaultKMLStyleProvider,
    ) {
        update(emptyList(), features, layerStyle, styleProvider)
    }

    @JvmName("updateStatic")
    fun update(
        staticFeatures: List<KMLFeature>,
        layerStyle: LayerStyle,
        styleProvider: KMLStyleProviderInterface = DefaultKMLStyleProvider,
    ) {
        update(staticFeatures, emptyList(), layerStyle, styleProvider)
    }

    fun update(
        staticFeatures: List<KMLFeature>,
        dynamicFeatures: List<KMLFeatureState>,
        layerStyle: LayerStyle,
        styleProvider: KMLStyleProviderInterface = DefaultKMLStyleProvider,
    ) {
        val rendered = ArrayList<RenderFeature>(staticFeatures.size + dynamicFeatures.size)
        staticFeatures.forEach {
            if (it.visible) {
                rendered
                    .add(KMLRenderFeatureBuilder.build(it, layerStyle, styleProvider))
            }
        }
        dynamicFeatures.forEach {
            if (it.visible) {
                rendered
                    .add(KMLRenderFeatureBuilder.build(it, layerStyle, styleProvider))
            }
        }
        val index = if (rendered.size >= KMLSpatialIndex.BUILD_THRESHOLD) KMLSpatialIndex.build(rendered) else null
        state = TileState(rendered, index)
        synchronized(cacheLock) {
            cacheEpoch += 1
            cache.evictAll()
        }
    }

    override fun renderTile(request: TileRequest): ByteArray? {
        val epoch = cacheEpoch
        val key = "$epoch:${request.z}/${request.x}/${request.y}"
        synchronized(cacheLock) {
            cache.get(key)?.let { return if (it === emptyTileMarker) null else it }
        }

        val future = CompletableFuture<ByteArray?>()
        val existing = inFlight.putIfAbsent(key, future)
        if (existing != null) return existing.join()

        val job = RenderJob(key = key, epoch = epoch, request = request, state = state, future = future)
        try {
            renderQueue.put(job)
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
            inFlight.remove(key)
            future.complete(null)
            return null
        }
        return future.join()
    }

    private fun renderLoop() {
        while (true) {
            val job =
                try {
                    renderQueue.take()
                } catch (_: InterruptedException) {
                    Thread.currentThread().interrupt()
                    return
                }
            try {
                synchronized(cacheLock) {
                    cache.get(job.key)?.let {
                        job.future.complete(if (it === emptyTileMarker) null else it)
                        return
                    }
                }
                val bytes = renderTileInternal(job.request, job.state)
                synchronized(cacheLock) {
                    if (cacheEpoch == job.epoch) {
                        cache.put(job.key, bytes ?: emptyTileMarker)
                    }
                }
                job.future.complete(bytes)
            } catch (e: Exception) {
                job.future.completeExceptionally(e)
            } finally {
                inFlight.remove(job.key)
            }
        }
    }

    private fun renderTileInternal(
        request: TileRequest,
        tileState: TileState,
    ): ByteArray? {
        if (tileState.features.isEmpty()) return null

        val z = request.z
        val worldTileCount = 1 shl z
        val x = ((request.x % worldTileCount) + worldTileCount) % worldTileCount
        val y = request.y
        if (y !in 0 until worldTileCount) return null

        val tileMinX = x.toDouble() / worldTileCount
        val tileMaxX = (x + 1).toDouble() / worldTileCount
        val tileMinY = y.toDouble() / worldTileCount
        val tileMaxY = (y + 1).toDouble() / worldTileCount

        val candidates =
            tileState.index?.query(tileMinX, tileMinY, tileMaxX, tileMaxY)
                ?: tileState.features.indices.toList()

        var hasContent = false
        val bitmap = painter.beginTile()
        val canvas = Canvas(bitmap)

        val worldSize = tileSize.toDouble() * worldTileCount
        val originX = x.toDouble() * tileSize
        val originY = y.toDouble() * tileSize

        for (idx in candidates) {
            val feature = tileState.features[idx]
            if (!feature.bounds.intersects(tileMinX, tileMinY, tileMaxX, tileMaxY)) continue
            if (
                painter.drawFeature(
                    canvas = canvas,
                    feature = feature,
                    zoom = z,
                    worldSize = worldSize,
                    originX = originX,
                    originY = originY,
                    tileMinX = tileMinX,
                    tileMinY = tileMinY,
                    tileMaxX = tileMaxX,
                    tileMaxY = tileMaxY,
                )
            ) {
                hasContent = true
            }
        }

        if (!hasContent) return null
        return painter.toPng(bitmap)
    }

    fun hitTest(
        longitude: Double,
        latitude: Double,
        lineTolSq: Double? = null,
        pointTolSq: Double? = null,
    ): KMLHitTestResult? {
        val currentState = state
        return hitTester.hitTest(
            longitude = longitude,
            latitude = latitude,
            features = currentState.features,
            index = currentState.index,
            lineTolSq = lineTolSq,
            pointTolSq = pointTolSq,
        )
    }

    fun hitTestFeature(
        longitude: Double,
        latitude: Double,
        lineTolSq: Double? = null,
        pointTolSq: Double? = null,
    ): KMLFeature? = hitTest(longitude, latitude, lineTolSq, pointTolSq)?.feature

    data class LayerStyle(
        val strokeColor: Int,
        val fillColor: Int,
        val strokeWidth: Float,
        val pointRadius: Float,
    )

    data class KMLHitTestResult(
        val feature: KMLFeature,
        val position: GeoPoint,
    )

    /** 描画中に元データが差し替わっても矛盾しないよう、1 回ぶんをまとめて固めたもの。 */
    private data class TileState(
        val features: List<RenderFeature>,
        val index: KMLSpatialIndex?,
    )

    private data class RenderJob(
        val key: String,
        val epoch: Long,
        val request: TileRequest,
        val state: TileState,
        val future: CompletableFuture<ByteArray?>,
    )

    companion object {
        private const val DEFAULT_CACHE_SIZE_KB = 8 * 1024
        private val DEFAULT_MAX_CONCURRENT_RENDERS =
            (Runtime.getRuntime().availableProcessors() - 1).coerceIn(2, 6)
        private const val MAX_CONCURRENT_RENDERS = 6
        private const val MAX_QUEUE_SIZE = 512
    }
}
