package com.mapconductor.heatmap

import com.mapconductor.core.features.GeoPoint
import com.mapconductor.tileserver.TileProvider
import com.mapconductor.tileserver.TileRequest
import java.io.ByteArrayOutputStream
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import android.graphics.Bitmap
import android.graphics.Color
import android.util.LruCache

class HeatmapTileRenderer(
    val tileSize: Int = DEFAULT_TILE_SIZE,
    cacheSizeKb: Int = DEFAULT_CACHE_SIZE_KB,
) : TileProvider {
    private val cacheLock = Any()
    private val cache =
        object : LruCache<String, ByteArray>(cacheSizeKb) {
            override fun sizeOf(
                key: String,
                value: ByteArray,
            ): Int = (value.size / 1024).coerceAtLeast(1)
        }

    private val emptyTileMarker = ByteArray(1)
    private val kernelCache = ConcurrentHashMap<Int, DoubleArray>()

    @Volatile
    private var cameraZoom: Double? = null

    @Volatile
    private var cameraZoomKey: Int? = null

    @Volatile
    private var state =
        TileState(
            points = emptyList(),
            bounds = null,
            radiusPx = DEFAULT_RADIUS_PX,
            colorMap = IntArray(COLOR_MAP_SIZE) { Color.TRANSPARENT },
            maxIntensities = DoubleArray(MAX_ZOOM_LEVEL),
        )

    fun update(
        points: List<HeatmapPoint>,
        radiusPx: Int,
        gradient: HeatmapGradient,
        maxIntensity: Double?,
    ) {
        val safeRadius = radiusPx.coerceAtLeast(1)
        val weightedPoints = buildWeightedPoints(points)
        val bounds = if (weightedPoints.isEmpty()) null else calculateBounds(weightedPoints)
        val colorMap = buildColorMap(gradient)
        val maxIntensities =
            if (bounds == null) {
                DoubleArray(MAX_ZOOM_LEVEL)
            } else {
                getMaxIntensities(weightedPoints, bounds, safeRadius, maxIntensity)
            }
        state =
            TileState(
                points = weightedPoints,
                bounds = bounds,
                radiusPx = safeRadius,
                colorMap = colorMap,
                maxIntensities = maxIntensities,
            )
        synchronized(cacheLock) {
            cache.evictAll()
        }
    }

    fun updateCameraZoom(zoom: Double) {
        val nextKey = (zoom * 100).roundToInt()
        val prevKey = cameraZoomKey
        cameraZoom = zoom
        if (prevKey != nextKey) {
            cameraZoomKey = nextKey
            synchronized(cacheLock) {
                cache.evictAll()
            }
        }
    }

    override fun renderTile(request: TileRequest): ByteArray? {
        val key = "${request.z}/${request.x}/${request.y}"
        synchronized(cacheLock) {
            cache.get(key)?.let { cached ->
                return if (cached === emptyTileMarker) null else cached
            }
        }
        val bytes = renderTileInternal(request, state)
        synchronized(cacheLock) {
            cache.put(key, bytes ?: emptyTileMarker)
        }
        return bytes
    }

    private fun renderTileInternal(
        request: TileRequest,
        tileState: TileState,
    ): ByteArray? {
        val bounds = tileState.bounds ?: return null
        if (tileState.points.isEmpty()) return null

        val zoom = request.z.toDouble()
        val zoomScale = 2.0.pow((cameraZoom ?: zoom) - zoom)
        val radius = (tileState.radiusPx / zoomScale).roundToInt().coerceAtLeast(1)
        val kernel = resolveKernel(radius)
        val tileWidth = WORLD_WIDTH / 2.0.pow(zoom)
        val padding = tileWidth * radius / tileSize
        val tileWidthPadded = tileWidth + 2 * padding
        val gridDim = tileSize + radius * 2
        val bucketWidth = tileWidthPadded / gridDim

        val minX = request.x * tileWidth - padding
        val maxX = (request.x + 1) * tileWidth + padding
        val minY = request.y * tileWidth - padding
        val maxY = (request.y + 1) * tileWidth + padding

        val tileBounds = Bounds(minX, maxX, minY, maxY)
        val paddedBounds =
            Bounds(
                minX = bounds.minX - padding,
                maxX = bounds.maxX + padding,
                minY = bounds.minY - padding,
                maxY = bounds.maxY + padding,
            )
        if (!tileBounds.intersects(paddedBounds)) return null

        val intensity = Array(gridDim) { DoubleArray(gridDim) }
        var hasPoints = false

        var overlapMinX = 0.0
        var overlapMaxX = 0.0
        var xOffset = 0.0
        if (minX < 0.0) {
            overlapMinX = minX + WORLD_WIDTH
            overlapMaxX = WORLD_WIDTH
            xOffset = -WORLD_WIDTH
        } else if (maxX > WORLD_WIDTH) {
            overlapMinX = 0.0
            overlapMaxX = maxX - WORLD_WIDTH
            xOffset = WORLD_WIDTH
        }

        fun addPoint(
            worldX: Double,
            worldY: Double,
            weight: Double,
        ) {
            val bucketX = ((worldX - minX) / bucketWidth).toInt()
            val bucketY = ((worldY - minY) / bucketWidth).toInt()
            if (bucketX !in 0 until gridDim || bucketY !in 0 until gridDim) return
            intensity[bucketX][bucketY] += weight
        }

        tileState.points.forEach { point ->
            if (point.y < minY || point.y > maxY) return@forEach
            var added = false
            if (point.x >= minX && point.x <= maxX) {
                addPoint(point.x, point.y, point.intensity)
                added = true
            }
            if (xOffset != 0.0 && point.x >= overlapMinX && point.x <= overlapMaxX) {
                addPoint(point.x + xOffset, point.y, point.intensity)
                added = true
            }
            if (added) hasPoints = true
        }

        if (!hasPoints) return null

        val convolved = convolve(intensity, kernel)
        val intensityZoom = (cameraZoom ?: zoom).toInt().coerceIn(0, tileState.maxIntensities.lastIndex)
        val maxIntensity = tileState.maxIntensities[intensityZoom]
        if (maxIntensity <= 0.0) return null

        val bitmap = colorize(convolved, tileState.colorMap, maxIntensity)
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        bitmap.recycle()
        return stream.toByteArray()
    }

    private fun buildWeightedPoints(points: List<HeatmapPoint>): List<WeightedPoint> {
        if (points.isEmpty()) return emptyList()
        val weightedPoints = ArrayList<WeightedPoint>(points.size)
        points.forEach { point ->
            val weight =
                if (point.weight.isNaN()) {
                    DEFAULT_INTENSITY
                } else if (point.weight >= 0.0) {
                    point.weight
                } else {
                    DEFAULT_INTENSITY
                }
            val world = toWorldPoint(point.position)
            weightedPoints.add(WeightedPoint(world.x, world.y, weight))
        }
        return weightedPoints
    }

    private fun toWorldPoint(position: GeoPoint): WorldPoint {
        val x = position.longitude / 360.0 + 0.5
        val siny = sin(Math.toRadians(position.latitude)).coerceIn(-0.9999, 0.9999)
        val y = 0.5 * ln((1 + siny) / (1 - siny)) / -(2 * PI) + 0.5
        return WorldPoint(x, y)
    }

    private fun calculateBounds(points: List<WeightedPoint>): Bounds {
        var minX = points[0].x
        var maxX = points[0].x
        var minY = points[0].y
        var maxY = points[0].y
        points.forEach { point ->
            if (point.x < minX) minX = point.x
            if (point.x > maxX) maxX = point.x
            if (point.y < minY) minY = point.y
            if (point.y > maxY) maxY = point.y
        }
        return Bounds(minX, maxX, minY, maxY)
    }

    private fun resolveKernel(radius: Int): DoubleArray {
        if (radius <= 0) return doubleArrayOf(1.0)
        val cached = kernelCache[radius]
        if (cached != null) return cached
        val built = generateKernel(radius, radius / 3.0)
        kernelCache[radius] = built
        return built
    }

    private fun generateKernel(
        radius: Int,
        sd: Double,
    ): DoubleArray {
        val kernel = DoubleArray(radius * 2 + 1)
        for (i in -radius..radius) {
            kernel[i + radius] = exp(-i * i / (2 * sd * sd))
        }
        return kernel
    }

    private fun convolve(
        grid: Array<DoubleArray>,
        kernel: DoubleArray,
    ): Array<DoubleArray> {
        val radius = kernel.size / 2
        val dimOld = grid.size
        val dim = dimOld - 2 * radius
        val lowerLimit = radius
        val upperLimit = radius + dim - 1
        val intermediate = Array(dimOld) { DoubleArray(dimOld) }

        for (x in 0 until dimOld) {
            for (y in 0 until dimOld) {
                val value = grid[x][y]
                if (value == 0.0) continue
                val xUpperLimit = (upperLimit.coerceAtMost(x + radius)) + 1
                val initial = lowerLimit.coerceAtLeast(x - radius)
                for (x2 in initial until xUpperLimit) {
                    intermediate[x2][y] += value * kernel[x2 - (x - radius)]
                }
            }
        }

        val outputGrid = Array(dim) { DoubleArray(dim) }
        for (x in lowerLimit..upperLimit) {
            for (y in 0 until dimOld) {
                val value = intermediate[x][y]
                if (value == 0.0) continue
                val yUpperLimit = (upperLimit.coerceAtMost(y + radius)) + 1
                val initial = lowerLimit.coerceAtLeast(y - radius)
                for (y2 in initial until yUpperLimit) {
                    outputGrid[x - radius][y2 - radius] += value * kernel[y2 - (y - radius)]
                }
            }
        }
        return outputGrid
    }

    private fun colorize(
        grid: Array<DoubleArray>,
        colorMap: IntArray,
        max: Double,
    ): Bitmap {
        val maxColor = colorMap[colorMap.size - 1]
        val colorMapScaling = (colorMap.size - 1) / max
        val dim = grid.size
        val colors = IntArray(dim * dim)
        for (i in 0 until dim) {
            for (j in 0 until dim) {
                val value = grid[j][i]
                val index = i * dim + j
                if (value != 0.0) {
                    val colorIndex = (value * colorMapScaling).toInt()
                    colors[index] =
                        if (colorIndex < colorMap.size) {
                            colorMap[colorIndex]
                        } else {
                            maxColor
                        }
                } else {
                    colors[index] = Color.TRANSPARENT
                }
            }
        }
        val bitmap = Bitmap.createBitmap(dim, dim, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(colors, 0, dim, 0, 0, dim, dim)
        return bitmap
    }

    private fun buildColorMap(gradient: HeatmapGradient): IntArray {
        val colors = gradient.stops.map { it.color }.toIntArray()
        val startPoints = gradient.stops.map { it.position.toFloat() }.toFloatArray()
        return generateColorMap(colors, startPoints, COLOR_MAP_SIZE)
    }

    private fun generateColorMap(
        colors: IntArray,
        startPoints: FloatArray,
        mapSize: Int,
    ): IntArray {
        require(colors.isNotEmpty()) { "Heatmap gradient requires at least one color." }
        val colorIntervals = HashMap<Int, ColorInterval>()
        if (startPoints[0] != 0f) {
            val initialColor =
                Color.argb(
                    0,
                    Color.red(colors[0]),
                    Color.green(colors[0]),
                    Color.blue(colors[0]),
                )
            colorIntervals[0] =
                ColorInterval(
                    color1 = initialColor,
                    color2 = colors[0],
                    duration = mapSize * startPoints[0],
                )
        }
        for (i in 1 until colors.size) {
            colorIntervals[(mapSize * startPoints[i - 1]).toInt()] =
                ColorInterval(
                    color1 = colors[i - 1],
                    color2 = colors[i],
                    duration = mapSize * (startPoints[i] - startPoints[i - 1]),
                )
        }
        if (startPoints[startPoints.size - 1] != 1f) {
            val last = startPoints.size - 1
            colorIntervals[(mapSize * startPoints[last]).toInt()] =
                ColorInterval(
                    color1 = colors[last],
                    color2 = colors[last],
                    duration = mapSize * (1 - startPoints[last]),
                )
        }

        val colorMap = IntArray(mapSize)
        var interval = colorIntervals[0] ?: ColorInterval(colors[0], colors[0], 1f)
        var start = 0
        for (i in 0 until mapSize) {
            colorIntervals[i]?.let {
                interval = it
                start = i
            }
            val ratio =
                if (interval.duration == 0f) {
                    0f
                } else {
                    (i - start) / interval.duration
                }
            colorMap[i] = interpolateColor(interval.color1, interval.color2, ratio)
        }
        return colorMap
    }

    private fun interpolateColor(
        color1: Int,
        color2: Int,
        ratio: Float,
    ): Int {
        val alpha = ((Color.alpha(color2) - Color.alpha(color1)) * ratio + Color.alpha(color1)).roundToInt()
        val hsv1 = FloatArray(3)
        val hsv2 = FloatArray(3)
        Color.RGBToHSV(Color.red(color1), Color.green(color1), Color.blue(color1), hsv1)
        Color.RGBToHSV(Color.red(color2), Color.green(color2), Color.blue(color2), hsv2)

        if (hsv1[0] - hsv2[0] > 180) {
            hsv2[0] += 360
        } else if (hsv2[0] - hsv1[0] > 180) {
            hsv1[0] += 360
        }

        val result = FloatArray(3)
        for (i in 0..2) {
            result[i] = (hsv2[i] - hsv1[i]) * ratio + hsv1[i]
        }
        return Color.HSVToColor(alpha, result)
    }

    private fun getMaxIntensities(
        points: List<WeightedPoint>,
        bounds: Bounds,
        radius: Int,
        customMaxIntensity: Double?,
    ): DoubleArray {
        val maxIntensityArray = DoubleArray(MAX_ZOOM_LEVEL)
        if (customMaxIntensity != null && customMaxIntensity != 0.0) {
            maxIntensityArray.fill(customMaxIntensity)
            return maxIntensityArray
        }
        for (i in DEFAULT_MIN_ZOOM until DEFAULT_MAX_ZOOM) {
            val screenDim = (SCREEN_SIZE * 2.0.pow(i - 3)).roundToInt()
            maxIntensityArray[i] = getMaxValue(points, bounds, radius, screenDim)
            if (i == DEFAULT_MIN_ZOOM) {
                for (j in 0 until i) {
                    maxIntensityArray[j] = maxIntensityArray[i]
                }
            }
        }
        for (i in DEFAULT_MAX_ZOOM until MAX_ZOOM_LEVEL) {
            maxIntensityArray[i] = maxIntensityArray[DEFAULT_MAX_ZOOM - 1]
        }
        return maxIntensityArray
    }

    private fun getMaxValue(
        points: List<WeightedPoint>,
        bounds: Bounds,
        radius: Int,
        screenDim: Int,
    ): Double {
        val minX = bounds.minX
        val maxX = bounds.maxX
        val minY = bounds.minY
        val maxY = bounds.maxY
        val boundsDim = (maxX - minX).coerceAtLeast(maxY - minY)
        if (boundsDim == 0.0) {
            return points.maxOfOrNull { it.intensity } ?: 0.0
        }
        val nBuckets = (screenDim / (2.0 * radius) + 0.5).toInt().coerceAtLeast(1)
        val scale = nBuckets / boundsDim
        val buckets = HashMap<Int, HashMap<Int, Double>>()
        var max = 0.0
        points.forEach { point ->
            val xBucket = ((point.x - minX) * scale).toInt()
            val yBucket = ((point.y - minY) * scale).toInt()
            val column = buckets.getOrPut(xBucket) { HashMap() }
            val nextValue = (column[yBucket] ?: 0.0) + point.intensity
            column[yBucket] = nextValue
            if (nextValue > max) max = nextValue
        }
        return max
    }

    private data class WorldPoint(
        val x: Double,
        val y: Double,
    )

    private data class WeightedPoint(
        val x: Double,
        val y: Double,
        val intensity: Double,
    )

    private data class Bounds(
        val minX: Double,
        val maxX: Double,
        val minY: Double,
        val maxY: Double,
    ) {
        fun intersects(other: Bounds): Boolean =
            minX <= other.maxX &&
                maxX >= other.minX &&
                minY <= other.maxY &&
                maxY >= other.minY
    }

    private data class ColorInterval(
        val color1: Int,
        val color2: Int,
        val duration: Float,
    )

    private data class TileState(
        val points: List<WeightedPoint>,
        val bounds: Bounds?,
        val radiusPx: Int,
        val colorMap: IntArray,
        val maxIntensities: DoubleArray,
    )

    companion object {
        const val DEFAULT_TILE_SIZE = 512
        private const val DEFAULT_CACHE_SIZE_KB = 8 * 1024
        private const val DEFAULT_RADIUS_PX = 20
        private const val DEFAULT_INTENSITY = 1.0
        private const val WORLD_WIDTH = 1.0
        private const val SCREEN_SIZE = 1280
        private const val DEFAULT_MIN_ZOOM = 5
        private const val DEFAULT_MAX_ZOOM = 11
        private const val MAX_ZOOM_LEVEL = 22
        private const val COLOR_MAP_SIZE = 1000
    }
}
