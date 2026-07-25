package com.mapconductor.kml

import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.tileserver.TileProviderInterface
import com.mapconductor.core.tileserver.TileRequest
import java.nio.ByteBuffer
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.ln
import kotlin.math.sin
import kotlin.math.sinh
import kotlin.math.sqrt
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.LruCache

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
        staticFeatures.forEach { if (it.visible) rendered.add(buildRenderFeature(it, layerStyle, styleProvider)) }
        dynamicFeatures.forEach { if (it.visible) rendered.add(buildRenderFeature(it, layerStyle, styleProvider)) }
        val index = if (rendered.size >= INDEX_THRESHOLD) buildIndex(rendered) else null
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
        val bitmap = getBitmap()
        bitmap.eraseColor(android.graphics.Color.TRANSPARENT)
        val canvas = Canvas(bitmap)

        val worldSize = tileSize.toDouble() * worldTileCount
        val originX = x.toDouble() * tileSize
        val originY = y.toDouble() * tileSize

        for (idx in candidates) {
            val feature = tileState.features[idx]
            if (!feature.bounds.intersects(tileMinX, tileMinY, tileMaxX, tileMaxY)) continue
            if (
                renderFeature(
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
        return bitmapToPng(bitmap)
    }

    private fun renderFeature(
        canvas: Canvas,
        feature: RenderFeature,
        zoom: Int,
        worldSize: Double,
        originX: Double,
        originY: Double,
        tileMinX: Double,
        tileMinY: Double,
        tileMaxX: Double,
        tileMaxY: Double,
    ): Boolean =
        renderGeometry(
            canvas,
            feature,
            feature.worldGeometry,
            zoom,
            worldSize,
            originX,
            originY,
            tileMinX,
            tileMinY,
            tileMaxX,
            tileMaxY,
        )

    private fun renderGeometry(
        canvas: Canvas,
        feature: RenderFeature,
        geometry: WorldGeometry,
        zoom: Int,
        worldSize: Double,
        originX: Double,
        originY: Double,
        tileMinX: Double,
        tileMinY: Double,
        tileMaxX: Double,
        tileMaxY: Double,
    ): Boolean =
        when (geometry) {
            is WorldGeometry.Point -> {
                val px = toPixel(geometry.wx, worldSize, originX)
                val py = toPixel(geometry.wy, worldSize, originY)
                canvas.drawCircle(px, py, feature.pointRadius, feature.fillPaint)
                feature.strokePaint?.let { canvas.drawCircle(px, py, feature.pointRadius, it) }
                true
            }

            is WorldGeometry.Points -> {
                val points = geometry.points
                var i = 0
                while (i < points.size) {
                    val px = toPixel(points[i], worldSize, originX)
                    val py = toPixel(points[i + 1], worldSize, originY)
                    canvas.drawCircle(px, py, feature.pointRadius, feature.fillPaint)
                    feature.strokePaint?.let { canvas.drawCircle(px, py, feature.pointRadius, it) }
                    i += 2
                }
                points.isNotEmpty()
            }

            is WorldGeometry.Line -> {
                val path =
                    buildLinePath(
                        geometry.rings,
                        zoom,
                        worldSize,
                        originX,
                        originY,
                        tileMinX,
                        tileMinY,
                        tileMaxX,
                        tileMaxY,
                        feature.strokePaint?.strokeWidth ?: feature.fillPaint.strokeWidth,
                    )
                if (!path.isEmpty) {
                    canvas.drawPath(path, feature.strokePaint ?: feature.fillPaint)
                    true
                } else {
                    false
                }
            }

            is WorldGeometry.Polygon -> {
                val path = buildPolygonPath(geometry.rings, zoom, worldSize, originX, originY)
                if (!path.isEmpty) {
                    canvas.drawPath(path, feature.fillPaint)
                    feature.strokePaint?.let { canvas.drawPath(path, it) }
                    true
                } else {
                    false
                }
            }

            is WorldGeometry.Collection -> {
                var drew = false
                for (part in geometry.parts) {
                    if (
                        renderGeometry(
                            canvas,
                            feature,
                            part,
                            zoom,
                            worldSize,
                            originX,
                            originY,
                            tileMinX,
                            tileMinY,
                            tileMaxX,
                            tileMaxY,
                        )
                    ) {
                        drew = true
                    }
                }
                drew
            }

            WorldGeometry.Empty -> false
        }

    private fun buildLinePath(
        rings: List<WorldRing>,
        zoom: Int,
        worldSize: Double,
        originX: Double,
        originY: Double,
        tileMinX: Double,
        tileMinY: Double,
        tileMaxX: Double,
        tileMaxY: Double,
        strokeWidth: Float,
    ): Path {
        val path = getPath()
        path.rewind()
        val margin = ((tileMaxX - tileMinX) * 0.25) + (strokeWidth.toDouble() / worldSize)
        val minX = tileMinX - margin
        val minY = tileMinY - margin
        val maxX = tileMaxX + margin
        val maxY = tileMaxY + margin
        for (ring in rings) {
            val coords = ring.coordsForZoom(zoom, tileSize)
            if (coords.size < 4) continue
            var needsMove = true
            var i = 2
            while (i < coords.size) {
                val ax = coords[i - 2]
                val ay = coords[i - 1]
                val bx = coords[i]
                val by = coords[i + 1]
                if (!segmentOutside(ax, ay, bx, by, minX, minY, maxX, maxY)) {
                    if (needsMove) {
                        path.moveTo(toPixel(ax, worldSize, originX), toPixel(ay, worldSize, originY))
                        needsMove = false
                    }
                    path.lineTo(toPixel(bx, worldSize, originX), toPixel(by, worldSize, originY))
                } else {
                    needsMove = true
                }
                i += 2
            }
        }
        return path
    }

    private fun buildPolygonPath(
        rings: List<WorldRing>,
        zoom: Int,
        worldSize: Double,
        originX: Double,
        originY: Double,
    ): Path {
        val path = getPath()
        path.rewind()
        path.fillType = Path.FillType.EVEN_ODD
        for (ring in rings) {
            val coords = ring.coordsForZoom(zoom, tileSize)
            if (coords.size < 6) continue
            path.moveTo(toPixel(coords[0], worldSize, originX), toPixel(coords[1], worldSize, originY))
            var i = 2
            while (i < coords.size) {
                path.lineTo(toPixel(coords[i], worldSize, originX), toPixel(coords[i + 1], worldSize, originY))
                i += 2
            }
            path.close()
        }
        return path
    }

    private fun lonToWorld(lon: Double): Double = lon / 360.0 + 0.5

    private fun worldToLon(wx: Double): Double = (wx - 0.5) * 360.0

    private fun latToWorld(lat: Double): Double {
        val siny = sin(lat * PI / 180.0).coerceIn(-0.9999, 0.9999)
        return 0.5 - ln((1.0 + siny) / (1.0 - siny)) / (4.0 * PI)
    }

    private fun worldToLat(wy: Double): Double = atan(sinh(PI * (1.0 - 2.0 * wy))) * 180.0 / PI

    private fun toPixel(
        world: Double,
        worldSize: Double,
        origin: Double,
    ): Float = ((world * worldSize) - origin).toFloat()

    private fun segmentOutside(
        ax: Double,
        ay: Double,
        bx: Double,
        by: Double,
        minX: Double,
        minY: Double,
        maxX: Double,
        maxY: Double,
    ): Boolean =
        (ax < minX && bx < minX) ||
            (ax > maxX && bx > maxX) ||
            (ay < minY && by < minY) ||
            (ay > maxY && by > maxY)

    private fun buildRenderFeature(
        feature: KMLFeature,
        layerStyle: LayerStyle,
        styleProvider: KMLStyleProviderInterface,
    ): RenderFeature {
        val style = styleProvider.getStyle(feature, layerStyle)
        return buildRenderFeatureFromStyle(
            feature,
            feature.geometry,
            style.strokeColor,
            style.fillColor,
            style.strokeWidth,
            style.pointRadius,
        )
    }

    private fun buildRenderFeature(
        state: KMLFeatureState,
        layerStyle: LayerStyle,
        styleProvider: KMLStyleProviderInterface,
    ): RenderFeature {
        val source =
            KMLFeature(
                id = state.id,
                geometry = state.geometry,
                properties = state.properties,
                strokeColor = state.strokeColor,
                fillColor = state.fillColor,
                strokeWidth = state.strokeWidth,
                pointRadius = state.pointRadius,
                visible = state.visible,
            )
        val style = styleProvider.getStyle(source, layerStyle)
        return buildRenderFeatureFromStyle(
            source,
            source.geometry,
            style.strokeColor,
            style.fillColor,
            style.strokeWidth,
            style.pointRadius,
        )
    }

    private fun buildRenderFeatureFromStyle(
        source: KMLFeature,
        geometry: KMLGeometry,
        strokeColor: Int,
        fillColor: Int,
        strokeWidth: Float,
        pointRadius: Float,
    ): RenderFeature {
        val fillPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = fillColor
            }
        val strokePaint =
            if (android.graphics.Color.alpha(strokeColor) > 0 && strokeWidth > 0f) {
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    color = strokeColor
                    this.strokeWidth = strokeWidth
                    strokeJoin = Paint.Join.ROUND
                    strokeCap = Paint.Cap.ROUND
                }
            } else {
                null
            }

        val worldGeometry = toWorldGeometry(geometry)
        val bounds = computeBounds(worldGeometry)
        // Strip the geometry from source: worldGeometry already holds all coordinates in world
        // space for rendering and hit-testing. Keeping lat/lon coords here doubles memory usage,
        // which causes OOM on large datasets with multiple renderers.
        return RenderFeature(
            source = source.copy(geometry = KMLGeometry.Empty),
            worldGeometry = worldGeometry,
            bounds = bounds,
            fillPaint = fillPaint,
            strokePaint = strokePaint,
            pointRadius = pointRadius,
        )
    }

    private fun toWorldGeometry(geometry: KMLGeometry): WorldGeometry =
        when (geometry) {
            is KMLGeometry.Point ->
                WorldGeometry.Point(
                    wx = lonToWorld(geometry.longitude),
                    wy = latToWorld(geometry.latitude),
                )

            is KMLGeometry.MultiPoint ->
                WorldGeometry.Points(
                    points = flatPoints(geometry.points),
                )

            is KMLGeometry.LineString ->
                WorldGeometry.Line(
                    rings = listOf(WorldRing(flatCoordinates(geometry.coordinates))),
                )

            is KMLGeometry.MultiLineString ->
                WorldGeometry.Line(
                    rings =
                        geometry.lines.map { line ->
                            WorldRing(flatCoordinates(line))
                        },
                )

            is KMLGeometry.Polygon ->
                WorldGeometry.Polygon(
                    rings =
                        geometry.rings.map { ring ->
                            WorldRing(flatCoordinates(ring))
                        },
                )

            is KMLGeometry.MultiPolygon ->
                WorldGeometry.Collection(
                    parts =
                        geometry.polygons.map { poly ->
                            WorldGeometry.Polygon(
                                rings =
                                    poly.map { ring ->
                                        WorldRing(flatCoordinates(ring))
                                    },
                            )
                        },
                )

            is KMLGeometry.GeometryCollection ->
                WorldGeometry.Collection(
                    parts = geometry.geometries.map { toWorldGeometry(it) },
                )

            KMLGeometry.Empty -> WorldGeometry.Empty
        }

    private fun flatPoints(points: List<KMLGeometry.Point>): DoubleArray {
        val coords = DoubleArray(points.size * 2)
        var i = 0
        for (point in points) {
            coords[i++] = lonToWorld(point.longitude)
            coords[i++] = latToWorld(point.latitude)
        }
        return coords
    }

    private fun flatCoordinates(points: List<LonLat>): DoubleArray {
        val coords = DoubleArray(points.size * 2)
        var i = 0
        for (point in points) {
            coords[i++] = lonToWorld(point.longitude)
            coords[i++] = latToWorld(point.latitude)
        }
        return coords
    }

    private fun computeBounds(geometry: WorldGeometry): WorldBounds =
        when (geometry) {
            is WorldGeometry.Point -> WorldBounds(geometry.wx, geometry.wx, geometry.wy, geometry.wy)
            is WorldGeometry.Points -> boundsOfCoords(geometry.points)
            is WorldGeometry.Line -> boundsOfRings(geometry.rings)
            is WorldGeometry.Polygon -> boundsOfRings(geometry.rings)
            is WorldGeometry.Collection -> {
                if (geometry.parts.isEmpty()) {
                    WorldBounds(0.0, 1.0, 0.0, 1.0)
                } else {
                    val childBounds = geometry.parts.map { computeBounds(it) }
                    WorldBounds(
                        minX = childBounds.minOf { it.minX },
                        maxX = childBounds.maxOf { it.maxX },
                        minY = childBounds.minOf { it.minY },
                        maxY = childBounds.maxOf { it.maxY },
                    )
                }
            }
            WorldGeometry.Empty -> WorldBounds(0.0, 1.0, 0.0, 1.0)
        }

    private fun boundsOfCoords(coords: DoubleArray): WorldBounds {
        if (coords.isEmpty()) return WorldBounds(0.0, 1.0, 0.0, 1.0)
        var minX = coords[0]
        var maxX = coords[0]
        var minY = coords[1]
        var maxY = coords[1]
        var i = 2
        while (i < coords.size) {
            val x = coords[i]
            val y = coords[i + 1]
            if (x < minX) minX = x
            if (x > maxX) maxX = x
            if (y < minY) minY = y
            if (y > maxY) maxY = y
            i += 2
        }
        return WorldBounds(minX, maxX, minY, maxY)
    }

    private fun boundsOfRings(rings: List<WorldRing>): WorldBounds {
        var minX = Double.MAX_VALUE
        var maxX = -Double.MAX_VALUE
        var minY = Double.MAX_VALUE
        var maxY = -Double.MAX_VALUE
        for (ring in rings) {
            val coords = ring.coords
            var i = 0
            while (i < coords.size) {
                val x = coords[i]
                val y = coords[i + 1]
                if (x < minX) minX = x
                if (x > maxX) maxX = x
                if (y < minY) minY = y
                if (y > maxY) maxY = y
                i += 2
            }
        }
        return if (minX <= maxX) {
            WorldBounds(minX, maxX, minY, maxY)
        } else {
            WorldBounds(0.0, 1.0, 0.0, 1.0)
        }
    }

    private fun buildIndex(features: List<RenderFeature>): SpatialIndex {
        val grid = Array(INDEX_GRID_SIZE * INDEX_GRID_SIZE) { mutableListOf<Int>() }
        for (i in features.indices) {
            val b = features[i].bounds
            val x0 = (b.minX * INDEX_GRID_SIZE).toInt().coerceIn(0, INDEX_GRID_SIZE - 1)
            val x1 = (b.maxX * INDEX_GRID_SIZE).toInt().coerceIn(0, INDEX_GRID_SIZE - 1)
            val y0 = (b.minY * INDEX_GRID_SIZE).toInt().coerceIn(0, INDEX_GRID_SIZE - 1)
            val y1 = (b.maxY * INDEX_GRID_SIZE).toInt().coerceIn(0, INDEX_GRID_SIZE - 1)
            for (cy in y0..y1) {
                for (cx in x0..x1) {
                    grid[cy * INDEX_GRID_SIZE + cx].add(i)
                }
            }
        }
        return SpatialIndex(grid, features.size)
    }

    private fun bitmapToPng(bitmap: Bitmap): ByteArray {
        val byteCount = bitmap.byteCount
        val buffer = getPixelBuffer(byteCount)
        buffer.clear()
        bitmap.copyPixelsToBuffer(buffer)
        // copyPixelsToBuffer yields the bitmap's native ARGB_8888 layout, which is
        // R,G,B,A byte order with premultiplied alpha; PNG requires straight alpha,
        // so un-premultiply translucent pixels.
        val source = buffer.array()
        val rgba = getRgbaBuffer(byteCount)
        var i = 0
        while (i < byteCount) {
            when (val a = source[i + 3].toInt() and 0xff) {
                255 -> {
                    rgba[i] = source[i]
                    rgba[i + 1] = source[i + 1]
                    rgba[i + 2] = source[i + 2]
                    rgba[i + 3] = source[i + 3]
                }
                0 -> {
                    rgba[i] = 0
                    rgba[i + 1] = 0
                    rgba[i + 2] = 0
                    rgba[i + 3] = 0
                }
                else -> {
                    val half = a / 2
                    rgba[i] = ((((source[i].toInt() and 0xff) * 255 + half) / a).coerceAtMost(255)).toByte()
                    rgba[i + 1] = ((((source[i + 1].toInt() and 0xff) * 255 + half) / a).coerceAtMost(255)).toByte()
                    rgba[i + 2] = ((((source[i + 2].toInt() and 0xff) * 255 + half) / a).coerceAtMost(255)).toByte()
                    rgba[i + 3] = source[i + 3]
                }
            }
            i += 4
        }
        return FastPngEncoder.encode(bitmap.width, bitmap.height, rgba)
    }

    private val threadLocalBitmap = ThreadLocal<Bitmap>()
    private val threadLocalPath = ThreadLocal<Path>()
    private val threadLocalPixelBuffer = ThreadLocal<ByteBuffer>()
    private val threadLocalRgba = ThreadLocal<ByteArray>()

    private fun getBitmap(): Bitmap {
        val existing = threadLocalBitmap.get()
        if (existing != null &&
            !existing.isRecycled &&
            existing.width == tileSize &&
            existing.height == tileSize
        ) {
            return existing
        }
        val bm = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888)
        threadLocalBitmap.set(bm)
        return bm
    }

    private fun getPath(): Path {
        val existing = threadLocalPath.get()
        if (existing != null) return existing
        val path = Path()
        threadLocalPath.set(path)
        return path
    }

    private fun getPixelBuffer(byteCount: Int): ByteBuffer {
        val existing = threadLocalPixelBuffer.get()
        if (existing != null && existing.capacity() >= byteCount && existing.hasArray()) {
            return existing
        }
        val buffer = ByteBuffer.allocate(byteCount)
        threadLocalPixelBuffer.set(buffer)
        return buffer
    }

    private fun getRgbaBuffer(byteCount: Int): ByteArray {
        val existing = threadLocalRgba.get()
        if (existing != null && existing.size >= byteCount) return existing
        val buffer = ByteArray(byteCount)
        threadLocalRgba.set(buffer)
        return buffer
    }

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

    private data class WorldBounds(
        val minX: Double,
        val maxX: Double,
        val minY: Double,
        val maxY: Double,
    ) {
        fun intersects(
            x1: Double,
            y1: Double,
            x2: Double,
            y2: Double,
        ): Boolean = minX <= x2 && maxX >= x1 && minY <= y2 && maxY >= y1
    }

    private sealed class WorldGeometry {
        data class Point(
            val wx: Double,
            val wy: Double,
        ) : WorldGeometry()

        data class Points(
            val points: DoubleArray,
        ) : WorldGeometry()

        data class Line(
            val rings: List<WorldRing>,
        ) : WorldGeometry()

        data class Polygon(
            val rings: List<WorldRing>,
        ) : WorldGeometry()

        data class Collection(
            val parts: List<WorldGeometry>,
        ) : WorldGeometry()

        object Empty : WorldGeometry()
    }

    private class WorldRing(
        val coords: DoubleArray,
    ) {
        private val simplifiedByZoom =
            java.util.concurrent.atomic
                .AtomicReferenceArray<DoubleArray>(MAX_SIMPLIFY_ZOOM + 1)

        fun coordsForZoom(
            zoom: Int,
            tileSize: Int,
        ): DoubleArray {
            if (coords.size < 6) return coords
            val cacheIndex = zoom.coerceIn(0, MAX_SIMPLIFY_ZOOM)
            simplifiedByZoom.get(cacheIndex)?.let { return it }
            val tolerance = 0.5 / (tileSize.toDouble() * (1 shl cacheIndex))
            val simplified = simplifyRadial(coords, tolerance)
            return if (simplifiedByZoom.compareAndSet(cacheIndex, null, simplified)) {
                simplified
            } else {
                simplifiedByZoom.get(cacheIndex)
            }
        }
    }

    private data class RenderFeature(
        val source: KMLFeature,
        val worldGeometry: WorldGeometry,
        val bounds: WorldBounds,
        val fillPaint: Paint,
        val strokePaint: Paint?,
        val pointRadius: Float,
    )

    private class SpatialIndex(
        private val grid: Array<MutableList<Int>>,
        private val featureCount: Int,
    ) {
        fun query(
            x1: Double,
            y1: Double,
            x2: Double,
            y2: Double,
        ): List<Int> {
            val cx0 = (x1 * INDEX_GRID_SIZE).toInt().coerceIn(0, INDEX_GRID_SIZE - 1)
            val cx1 = (x2 * INDEX_GRID_SIZE).toInt().coerceIn(0, INDEX_GRID_SIZE - 1)
            val cy0 = (y1 * INDEX_GRID_SIZE).toInt().coerceIn(0, INDEX_GRID_SIZE - 1)
            val cy1 = (y2 * INDEX_GRID_SIZE).toInt().coerceIn(0, INDEX_GRID_SIZE - 1)
            // BitSet uses 1 bit per feature instead of ~32 bytes per entry in HashSet,
            // which prevents OOM when many features fall into the same tile.
            val seen = java.util.BitSet(featureCount)
            val result = ArrayList<Int>()
            for (cy in cy0..cy1) {
                for (cx in cx0..cx1) {
                    for (idx in grid[cy * INDEX_GRID_SIZE + cx]) {
                        if (!seen.get(idx)) {
                            seen.set(idx)
                            result.add(idx)
                        }
                    }
                }
            }
            return result
        }
    }

    private data class TileState(
        val features: List<RenderFeature>,
        val index: SpatialIndex?,
    )

    private data class RenderJob(
        val key: String,
        val epoch: Long,
        val request: TileRequest,
        val state: TileState,
        val future: CompletableFuture<ByteArray?>,
    )

    // ── Hit-testing ──────────────────────────────────────────────────────────

    /**
     * Returns the topmost [KMLFeature] whose geometry contains or is near
     * [longitude]/[latitude], or null if nothing is hit.
     * Call this from a map-level onClick handler on the main thread.
     */
    fun hitTest(
        longitude: Double,
        latitude: Double,
        lineTolSq: Double? = null,
        pointTolSq: Double? = null,
    ): KMLHitTestResult? {
        val wx = lonToWorld(longitude)
        val wy = latToWorld(latitude)
        val currentState = state
        val lineTolerance = if (lineTolSq != null) sqrt(lineTolSq) else HIT_LINE_TOLERANCE
        val pointTolerance = if (pointTolSq != null) sqrt(pointTolSq) else HIT_POINT_TOLERANCE
        val tolerance = maxOf(lineTolerance, pointTolerance)
        val candidates =
            currentState.index
                ?.query(wx - tolerance, wy - tolerance, wx + tolerance, wy + tolerance)
                ?: currentState.features.indices.toList()

        for (idx in candidates.asReversed()) {
            val feature = currentState.features[idx]
            if (!feature.bounds.intersects(wx - tolerance, wy - tolerance, wx + tolerance, wy + tolerance)) continue
            val hit = hitTestGeometry(wx, wy, feature.worldGeometry, lineTolSq, pointTolSq)
            if (hit != null) {
                return KMLHitTestResult(
                    feature = feature.source,
                    position =
                        GeoPoint.fromLongLat(
                            longitude = worldToLon(hit.wx),
                            latitude = worldToLat(hit.wy),
                        ),
                )
            }
        }
        return null
    }

    fun hitTestFeature(
        longitude: Double,
        latitude: Double,
        lineTolSq: Double? = null,
        pointTolSq: Double? = null,
    ): KMLFeature? = hitTest(longitude, latitude, lineTolSq, pointTolSq)?.feature

    private data class GeometryHit(
        val wx: Double,
        val wy: Double,
        val distanceSq: Double,
    )

    private fun hitTestGeometry(
        wx: Double,
        wy: Double,
        geometry: WorldGeometry,
        lineTolSq: Double? = null,
        pointTolSq: Double? = null,
    ): GeometryHit? =
        when (geometry) {
            is WorldGeometry.Point -> {
                val distanceSq = distanceSq(wx, wy, geometry.wx, geometry.wy)
                if (distanceSq <= (pointTolSq ?: HIT_POINT_TOLERANCE_SQ)) {
                    GeometryHit(geometry.wx, geometry.wy, distanceSq)
                } else {
                    null
                }
            }

            is WorldGeometry.Points ->
                hitTestPoints(wx, wy, geometry.points, pointTolSq)

            is WorldGeometry.Line ->
                hitTestRings(wx, wy, geometry.rings, lineTolSq)

            is WorldGeometry.Polygon -> {
                if (lineTolSq != null) {
                    hitTestRings(wx, wy, geometry.rings, lineTolSq)
                } else {
                    val rings = geometry.rings
                    if (rings.isNotEmpty() &&
                        pointInRing(wx, wy, rings[0].coords) &&
                        rings.drop(1).none { hole -> pointInRing(wx, wy, hole.coords) }
                    ) {
                        GeometryHit(wx, wy, 0.0)
                    } else {
                        null
                    }
                }
            }

            is WorldGeometry.Collection -> {
                var best: GeometryHit? = null
                for (part in geometry.parts) {
                    val hit = hitTestGeometry(wx, wy, part, lineTolSq, pointTolSq)
                    if (hit != null && (best == null || hit.distanceSq < best.distanceSq)) best = hit
                }
                best
            }

            WorldGeometry.Empty -> null
        }

    private fun hitTestPoints(
        wx: Double,
        wy: Double,
        coords: DoubleArray,
        pointTolSq: Double? = null,
    ): GeometryHit? {
        val tolSq = pointTolSq ?: HIT_POINT_TOLERANCE_SQ
        var best: GeometryHit? = null
        var i = 0
        while (i < coords.size) {
            val distanceSq = distanceSq(wx, wy, coords[i], coords[i + 1])
            if (distanceSq <= tolSq && (best == null || distanceSq < best.distanceSq)) {
                best = GeometryHit(coords[i], coords[i + 1], distanceSq)
            }
            i += 2
        }
        return best
    }

    private fun hitTestRings(
        wx: Double,
        wy: Double,
        rings: List<WorldRing>,
        lineTolSq: Double? = null,
    ): GeometryHit? {
        var best: GeometryHit? = null
        for (ring in rings) {
            val hit = hitTestLine(wx, wy, ring.coords, lineTolSq)
            if (hit != null && (best == null || hit.distanceSq < best.distanceSq)) best = hit
        }
        return best
    }

    private fun hitTestLine(
        wx: Double,
        wy: Double,
        coords: DoubleArray,
        lineTolSq: Double? = null,
    ): GeometryHit? {
        val tolSq = lineTolSq ?: HIT_LINE_TOLERANCE_SQ
        var best: GeometryHit? = null
        var i = 2
        while (i < coords.size) {
            val hit = closestPointOnSegment(wx, wy, coords[i - 2], coords[i - 1], coords[i], coords[i + 1])
            if (hit.distanceSq <= tolSq && (best == null || hit.distanceSq < best.distanceSq)) {
                best = hit
            }
            i += 2
        }
        return best
    }

    private fun pointInRing(
        wx: Double,
        wy: Double,
        ring: DoubleArray,
    ): Boolean {
        var inside = false
        var j = ring.size - 2
        var i = 0
        while (i < ring.size) {
            val xi = ring[i]
            val yi = ring[i + 1]
            val xj = ring[j]
            val yj = ring[j + 1]
            if (((yi > wy) != (yj > wy)) && (wx < (xj - xi) * (wy - yi) / (yj - yi) + xi)) {
                inside = !inside
            }
            j = i
            i += 2
        }
        return inside
    }

    private fun closestPointOnSegment(
        px: Double,
        py: Double,
        ax: Double,
        ay: Double,
        bx: Double,
        by: Double,
    ): GeometryHit {
        val dx = bx - ax
        val dy = by - ay
        if (dx == 0.0 && dy == 0.0) return GeometryHit(ax, ay, distanceSq(px, py, ax, ay))
        val t = ((px - ax) * dx + (py - ay) * dy) / (dx * dx + dy * dy)
        val tc = t.coerceIn(0.0, 1.0)
        val wx = ax + tc * dx
        val wy = ay + tc * dy
        return GeometryHit(wx, wy, distanceSq(px, py, wx, wy))
    }

    companion object {
        private const val DEFAULT_CACHE_SIZE_KB = 8 * 1024
        private val DEFAULT_MAX_CONCURRENT_RENDERS =
            (Runtime.getRuntime().availableProcessors() - 1).coerceIn(2, 6)
        private const val MAX_CONCURRENT_RENDERS = 6
        private const val MAX_QUEUE_SIZE = 512
        private const val INDEX_THRESHOLD = 256
        private const val INDEX_GRID_SIZE = 64
        private const val MAX_SIMPLIFY_ZOOM = 22

        // World-coordinate hit tolerances (~0.0002 ≈ 72m at equator, ~3-5px at zoom 14)
        private const val HIT_LINE_TOLERANCE = 0.0002
        private const val HIT_LINE_TOLERANCE_SQ = HIT_LINE_TOLERANCE * HIT_LINE_TOLERANCE
        private const val HIT_POINT_TOLERANCE = 0.0004
        private const val HIT_POINT_TOLERANCE_SQ = HIT_POINT_TOLERANCE * HIT_POINT_TOLERANCE

        private fun distanceSq(
            ax: Double,
            ay: Double,
            bx: Double,
            by: Double,
        ): Double {
            val dx = ax - bx
            val dy = ay - by
            return dx * dx + dy * dy
        }

        private fun simplifyRadial(
            coords: DoubleArray,
            tolerance: Double,
        ): DoubleArray {
            if (coords.size <= 4 || tolerance <= 0.0) return coords
            val toleranceSq = tolerance * tolerance
            val output = DoubleArray(coords.size)
            var out = 0
            var lastX = coords[0]
            var lastY = coords[1]
            output[out++] = lastX
            output[out++] = lastY

            var i = 2
            while (i < coords.size - 2) {
                val x = coords[i]
                val y = coords[i + 1]
                if (distanceSq(lastX, lastY, x, y) > toleranceSq) {
                    output[out++] = x
                    output[out++] = y
                    lastX = x
                    lastY = y
                }
                i += 2
            }

            val endX = coords[coords.size - 2]
            val endY = coords[coords.size - 1]
            if (out < 2 || output[out - 2] != endX || output[out - 1] != endY) {
                output[out++] = endX
                output[out++] = endY
            }
            return if (out == coords.size) coords else output.copyOf(out)
        }
    }
}
