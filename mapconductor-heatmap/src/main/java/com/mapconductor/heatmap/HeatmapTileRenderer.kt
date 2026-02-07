package com.mapconductor.heatmap

import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.tileserver.TileProviderInterface
import com.mapconductor.core.tileserver.TileRequest
import java.util.Arrays
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.Adler32
import java.util.zip.CRC32
import java.util.zip.Deflater
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import android.graphics.Color
import android.util.Log
import android.util.LruCache

class HeatmapTileRenderer(
    val tileSize: Int = DEFAULT_TILE_SIZE,
    cacheSizeKb: Int = DEFAULT_CACHE_SIZE_KB,
    maxConcurrentRenders: Int = DEFAULT_MAX_CONCURRENT_RENDERS,
    private val pngCompressionLevel: Int = DEFAULT_PNG_COMPRESSION_LEVEL,
) : TileProviderInterface {
    @Volatile
    private var didWarmUp: Boolean = false

    private val cacheLock = Any()
    private val cache =
        object : LruCache<String, ByteArray>(cacheSizeKb) {
            override fun sizeOf(
                key: String,
                value: ByteArray,
            ): Int = (value.size / 1024).coerceAtLeast(1)
        }

    private val emptyTileMarker = ByteArray(1)
    private val transparentTileBytes: ByteArray by lazy(LazyThreadSafetyMode.PUBLICATION) {
        val colors = IntArray(tileSize * tileSize) { Color.TRANSPARENT }
        encodePngRgba(
            colors = colors,
            width = tileSize,
            height = tileSize,
            buffers = PngBuffers(),
            compressionLevel = pngCompressionLevel,
        )
    }
    private val kernelCache = ConcurrentHashMap<Int, FloatArray>()
    private val inFlight = ConcurrentHashMap<String, CompletableFuture<ByteArray?>>()
    private val renderQueue =
        ArrayBlockingQueue<RenderJob>(
            MAX_RENDER_QUEUE_SIZE,
            // fair =
            false,
        )
    private val workerCount = maxConcurrentRenders.coerceIn(1, MAX_MAX_CONCURRENT_RENDERS)

    @Volatile
    private var cameraZoomQuantized: Double? = null

    @Volatile
    private var cameraZoomKey: Int? = null

    @Volatile
    private var cacheEpoch: Long = 0L

    @Volatile
    private var state =
        TileState(
            points = emptyList(),
            index = null,
            bounds = null,
            radiusPx = DEFAULT_RADIUS_PX,
            colorMap = IntArray(COLOR_MAP_SIZE) { Color.TRANSPARENT },
            maxIntensities = DoubleArray(MAX_ZOOM_LEVEL),
        )

    init {
        repeat(workerCount) { index ->
            Thread({ renderLoop() }, "HeatmapTileRenderer-$index").apply {
                isDaemon = true
                start()
            }
        }
    }

    fun update(
        points: List<HeatmapPoint>,
        radiusPx: Int,
        gradient: HeatmapGradient,
        maxIntensity: Double?,
    ) {
        val safeRadius = radiusPx.coerceAtLeast(1)
        val weightedPoints = buildWeightedPoints(points)
        val bounds = if (weightedPoints.isEmpty()) null else calculateBounds(weightedPoints)
        val index =
            if (weightedPoints.size < INDEX_BUILD_THRESHOLD) null else buildPointIndex(weightedPoints)
        val colorMap = buildColorMap(gradient)
        val maxIntensities =
            if (bounds == null) {
                DoubleArray(MAX_ZOOM_LEVEL)
            } else {
                getMaxIntensities(weightedPoints, bounds, safeRadius, maxIntensity)
            }
        if (!didWarmUp) {
            didWarmUp = true
            warmUp(colorMap)
        }
        state =
            TileState(
                points = weightedPoints,
                index = index,
                bounds = bounds,
                radiusPx = safeRadius,
                colorMap = colorMap,
                maxIntensities = maxIntensities,
            )
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            val b = bounds
            val idx = index
            Log.d(
                TAG,
                "update pointsIn=${points.size} weighted=${weightedPoints.size} radiusPx=$safeRadius " +
                    "bounds=" +
                    (if (b == null) "null" else "(${b.minX},${b.minY})-(${b.maxX},${b.maxY})") +
                    " index=" +
                    (
                        if (idx ==
                            null
                        ) {
                            "null"
                        } else {
                            "grid=${idx.gridSize} nonEmpty=${idx.nonEmptyBuckets} maxBucket=${idx.maxBucketSize}"
                        }
                    ),
            )
        }
        synchronized(cacheLock) {
            cacheEpoch += 1
            cache.evictAll()
        }
    }

    private fun warmUp(colorMap: IntArray) {
        // Warm up the critical hot paths to reduce first-tile spikes (ART JIT / profiles).
        try {
            val radius = 1
            val tileSize = 8
            val gridDim = tileSize + radius * 2
            val kernel = resolveKernel(radius)
            val input = FloatArray(gridDim * gridDim)
            val intermediate = FloatArray(gridDim * gridDim)
            val output = FloatArray(tileSize * tileSize)
            val center = gridDim * (gridDim / 2) + (gridDim / 2)
            input[center] = 1.0f
            val nonZeroInput = intArrayOf(center)
            val nonZeroIntermediate = IntArray(gridDim * gridDim)
            convolveSparseToOutput(
                input = input,
                intermediate = intermediate,
                output = output,
                kernel = kernel,
                gridDim = gridDim,
                radius = radius,
                tileSize = tileSize,
                nonZeroInput = nonZeroInput,
                nonZeroInputCount = nonZeroInput.size,
                nonZeroIntermediate = nonZeroIntermediate,
                nonZeroIntermediateCountOut = {},
            )
            encodePngFromIntensity(
                intensity = output,
                colorMap = colorMap,
                maxIntensity = 1.0,
                width = tileSize,
                height = tileSize,
                buffers = PngBuffers(),
                compressionLevel = pngCompressionLevel,
            )
        } catch (_: Exception) {
            // Ignore warm-up failures; rendering will proceed normally.
        }
    }

    fun updateCameraZoom(zoom: Double) {
        val nextKey = (zoom * CAMERA_ZOOM_KEY_SCALE).roundToInt()
        val prevKey = cameraZoomKey
        if (prevKey == nextKey && cameraZoomQuantized != null) return
        cameraZoomKey = nextKey
        cameraZoomQuantized = nextKey.toDouble() / CAMERA_ZOOM_KEY_SCALE
    }

    override fun renderTile(request: TileRequest): ByteArray? {
        val epoch = cacheEpoch
        val zoomKey = cameraZoomKey ?: (request.z * CAMERA_ZOOM_KEY_SCALE)
        val key = "$epoch:$zoomKey:${request.z}/${request.x}/${request.y}"
        synchronized(cacheLock) {
            cache.get(key)?.let { cached ->
                return if (cached === emptyTileMarker) transparentTileBytes else cached
            }
        }

        val future = CompletableFuture<ByteArray?>()
        val existing = inFlight.putIfAbsent(key, future)
        if (existing != null) {
            return existing.join()
        }
        val tileStateSnapshot = state
        val job =
            RenderJob(
                key = key,
                epoch = epoch,
                enqueuedAtNs = System.nanoTime(),
                request = request,
                state = tileStateSnapshot,
                future = future,
            )
        try {
            // Backpressure instead of dropping tiles (dropping triggers parent-tile fallback seams).
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
                val workStartNs = System.nanoTime()
                val queueWaitMs = (workStartNs - job.enqueuedAtNs) / 1_000_000.0
                val timings = phaseTimings().also { it.reset() }
                synchronized(cacheLock) {
                    cache.get(job.key)?.let { cached ->
                        job.future.complete(if (cached === emptyTileMarker) transparentTileBytes else cached)
                        continue
                    }
                }

                val renderStartNs = System.nanoTime()
                val bytes = renderTileInternal(job.request, job.state, timings)
                val renderMs = (System.nanoTime() - renderStartNs) / 1_000_000.0
                val responseBytes = bytes ?: transparentTileBytes

                synchronized(cacheLock) {
                    if (cacheEpoch == job.epoch) {
                        cache.put(job.key, bytes ?: emptyTileMarker)
                    }
                }

                val totalMs = queueWaitMs + renderMs
                val qw = (queueWaitMs * 10.0).roundToInt() / 10.0
                val rm = (renderMs * 10.0).roundToInt() / 10.0
                val tm = (totalMs * 10.0).roundToInt() / 10.0
                val isSlow = totalMs >= SLOW_TILE_LOG_THRESHOLD_MS
                if (isSlow) {
                    val phaseMsg =
                        """
                        effZoom=${timings.effectiveZoom}
                        radius=${timings.radius}
                        gridDim=${timings.gridDim}
                        index=${timings.usedIndex}
                        idxGrid=${timings.indexGridSize}
                        idxNonEmpty=${timings.indexNonEmptyBuckets}
                        idxMaxBucket=${timings.indexMaxBucketSize}
                        xRanges=${timings.xRanges}
                        cells=${timings.cellsVisited}
                        cand=${timings.candidatesVisited}
                        binned=${timings.pointsBinned}
                        setup=${timings.setupMs}ms
                        bin=${timings.binMs}ms
                        conv=${timings.convolveMs}ms
                        mapPng=${timings.pngMs}ms
                        pngLevel=${timings.pngLevel}
                        """.trimIndent()
                    val msg =
                        """
                        Slow tile breakdown
                        z=${job.request.z}
                        x=${job.request.x}
                        y=${job.request.y}
                        queueWait=${qw}ms
                        render=${rm}ms
                        total=${tm}ms
                        points=${job.state.points.size}
                        tileSize=$tileSize
                        isEmptyTile=${bytes == null}$phaseMsg
                        """.trimIndent()
                    Log.w(TAG, msg)
                }
                job.future.complete(responseBytes)
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
        timings: PhaseTimings?,
    ): ByteArray? {
        val setupStartNs = if (timings == null) 0L else System.nanoTime()
        val bounds = tileState.bounds ?: return null
        if (tileState.points.isEmpty()) return null

        val zoom = request.z.toDouble()
        val effectiveZoom = cameraZoomQuantized ?: zoom
        val zoomScale = 2.0.pow(effectiveZoom - zoom)
        val radius = (tileState.radiusPx / zoomScale).roundToInt().coerceAtLeast(1)
        val kernel = resolveKernel(radius)
        val tileWidth = WORLD_WIDTH / 2.0.pow(zoom)
        val padding = tileWidth * radius / tileSize
        val tileWidthPadded = tileWidth + 2 * padding
        val gridDim = tileSize + radius * 2
        val bucketWidth = tileWidthPadded / gridDim

        if (timings != null) {
            timings.effectiveZoom = ((effectiveZoom * 10.0).roundToInt() / 10.0)
            timings.radius = radius
            timings.gridDim = gridDim
        }

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

        val buffers = buffers()
        buffers.ensure(gridDim = gridDim, tileSize = tileSize)
        val gridLen = gridDim * gridDim
        val tileLen = tileSize * tileSize
        Arrays.fill(buffers.intensity, 0, gridLen, 0.0f)
        Arrays.fill(buffers.intermediate, 0, gridLen, 0.0f)
        Arrays.fill(buffers.output, 0, tileLen, 0.0f)
        buffers.nonZeroIntensityCount = 0
        buffers.nonZeroIntermediateCount = 0

        var hasPoints = false
        if (timings != null) {
            timings.setupMs = msSince(setupStartNs)
        }

        val binStartNs = if (timings == null) 0L else System.nanoTime()
        val trackSelectionStats = timings != null
        var candidatesVisited = 0
        var cellsVisited = 0
        var pointsBinned = 0

        fun addPoint(
            adjustedWorldX: Double,
            worldY: Double,
            weight: Double,
        ) {
            val bucketX = ((adjustedWorldX - minX) / bucketWidth).toInt()
            val bucketY = ((worldY - minY) / bucketWidth).toInt()
            if (bucketX !in 0 until gridDim || bucketY !in 0 until gridDim) return
            val idx = bucketY * gridDim + bucketX
            val prev = buffers.intensity[idx]
            if (prev == 0.0f) {
                buffers.nonZeroIntensity[buffers.nonZeroIntensityCount++] = idx
            }
            buffers.intensity[idx] = prev + weight.toFloat()
            hasPoints = true
            if (trackSelectionStats) {
                pointsBinned += 1
            }
        }

        val pointIndex = tileState.index
        if (pointIndex == null) {
            timings?.let {
                it.usedIndex = false
                it.indexGridSize = 0
                it.indexNonEmptyBuckets = 0
                it.indexMaxBucketSize = 0
                it.xRanges = 0
            }
            tileState.points.forEach { point ->
                if (trackSelectionStats) {
                    candidatesVisited += 1
                }
                if (point.y < minY || point.y > maxY) return@forEach
                if (point.x >= minX && point.x <= maxX) {
                    addPoint(point.x, point.y, point.intensity)
                } else if (minX < 0.0 && point.x >= minX + WORLD_WIDTH) {
                    addPoint(point.x - WORLD_WIDTH, point.y, point.intensity)
                } else if (maxX > WORLD_WIDTH && point.x <= maxX - WORLD_WIDTH) {
                    addPoint(point.x + WORLD_WIDTH, point.y, point.intensity)
                }
            }
        } else {
            val gridSize = pointIndex.gridSize
            val heads = pointIndex.heads
            val next = pointIndex.next
            timings?.let {
                it.usedIndex = true
                it.indexGridSize = gridSize
                it.indexNonEmptyBuckets = pointIndex.nonEmptyBuckets
                it.indexMaxBucketSize = pointIndex.maxBucketSize
            }
            val yMin = minY.coerceAtLeast(0.0)
            val yMax = maxY.coerceAtMost(WORLD_WIDTH)
            if (yMin <= yMax) {
                val cyStart = (yMin * gridSize).toInt().coerceIn(0, gridSize - 1)
                val cyEnd = ((yMax * gridSize).toInt()).coerceIn(0, gridSize - 1)

                val xRanges = buildTileXRanges(minX, maxX)
                timings?.let { it.xRanges = xRanges.size }
                xRanges.forEach { range ->
                    val min = range.min.coerceAtLeast(0.0)
                    val max = range.max.coerceAtMost(WORLD_WIDTH)
                    if (min > max) return@forEach
                    val cxStart = (min * gridSize).toInt().coerceIn(0, gridSize - 1)
                    val cxEnd = ((max * gridSize).toInt()).coerceIn(0, gridSize - 1)
                    for (cy in cyStart..cyEnd) {
                        val row = cy * gridSize
                        for (cx in cxStart..cxEnd) {
                            if (trackSelectionStats) {
                                cellsVisited += 1
                            }
                            var i = heads[row + cx]
                            while (i != -1) {
                                if (trackSelectionStats) {
                                    candidatesVisited += 1
                                }
                                val point = tileState.points[i]
                                if (point.y >= minY && point.y <= maxY) {
                                    val xAdj = point.x + range.offset
                                    if (xAdj >= minX && xAdj <= maxX) {
                                        addPoint(xAdj, point.y, point.intensity)
                                    }
                                }
                                i = next[i]
                            }
                        }
                    }
                }
            }
        }

        if (!hasPoints) return null
        if (timings != null) {
            timings.binMs = msSince(binStartNs)
            timings.cellsVisited = cellsVisited
            timings.candidatesVisited = candidatesVisited
            timings.pointsBinned = pointsBinned
        }

        val convolveStartNs = if (timings == null) 0L else System.nanoTime()
        convolveSparseToOutput(
            input = buffers.intensity,
            intermediate = buffers.intermediate,
            output = buffers.output,
            kernel = kernel,
            gridDim = gridDim,
            radius = radius,
            tileSize = tileSize,
            nonZeroInput = buffers.nonZeroIntensity,
            nonZeroInputCount = buffers.nonZeroIntensityCount,
            nonZeroIntermediate = buffers.nonZeroIntermediate,
            nonZeroIntermediateCountOut = { buffers.nonZeroIntermediateCount = it },
        )
        if (timings != null) {
            timings.convolveMs = msSince(convolveStartNs)
        }
        val intensityZoom = effectiveZoom.toInt().coerceIn(0, tileState.maxIntensities.lastIndex)
        val maxIntensity = tileState.maxIntensities[intensityZoom]
        if (maxIntensity <= 0.0) return null

        val pngStartNs = if (timings == null) 0L else System.nanoTime()
        // Deflate can become CPU-heavy when the tile contains lots of non-zero signal
        // (many different colors => poor compression); fall back to level 0 for latency.
        val effectivePngCompressionLevel =
            if (radius >= PNG_COMPLEX_TILE_RADIUS_THRESHOLD_PX ||
                buffers.nonZeroIntensityCount >= PNG_COMPLEX_TILE_POINT_THRESHOLD
            ) {
                0
            } else {
                pngCompressionLevel
            }
        if (timings != null) {
            timings.pngLevel = effectivePngCompressionLevel
        }
        val out =
            encodePngFromIntensity(
                intensity = buffers.output,
                colorMap = tileState.colorMap,
                maxIntensity = maxIntensity,
                width = tileSize,
                height = tileSize,
                buffers = buffers.png,
                compressionLevel = effectivePngCompressionLevel,
            )
        if (timings != null) {
            timings.pngMs = msSince(pngStartNs)
        }
        return out
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

    private fun toWorldPoint(position: GeoPointInterface): WorldPoint {
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

    private fun resolveKernel(radius: Int): FloatArray {
        if (radius <= 0) return floatArrayOf(1.0f)
        val cached = kernelCache[radius]
        if (cached != null) return cached
        val built = generateKernel(radius, radius / 3.0)
        kernelCache[radius] = built
        return built
    }

    private fun generateKernel(
        radius: Int,
        sd: Double,
    ): FloatArray {
        val kernel = FloatArray(radius * 2 + 1)
        for (i in -radius..radius) {
            kernel[i + radius] = exp(-i * i / (2 * sd * sd)).toFloat()
        }
        return kernel
    }

    private fun convolveSparseToOutput(
        input: FloatArray,
        intermediate: FloatArray,
        output: FloatArray,
        kernel: FloatArray,
        gridDim: Int,
        radius: Int,
        tileSize: Int,
        nonZeroInput: IntArray,
        nonZeroInputCount: Int,
        nonZeroIntermediate: IntArray,
        nonZeroIntermediateCountOut: (Int) -> Unit,
    ) {
        val lowerLimit = radius
        val upperLimit = radius + tileSize - 1

        // Horizontal spread into `intermediate` (row-major).
        var nonZeroIntermediateCount = 0
        var i = 0
        while (i < nonZeroInputCount) {
            val idx = nonZeroInput[i]
            val y = idx / gridDim
            val x = idx - y * gridDim
            val value = input[idx]
            val rowBase = y * gridDim
            val xStart = lowerLimit.coerceAtLeast(x - radius)
            val xEndExclusive = (upperLimit.coerceAtMost(x + radius)) + 1
            var x2 = xStart
            while (x2 < xEndExclusive) {
                val j = rowBase + x2
                val prev = intermediate[j]
                if (prev == 0.0f) {
                    nonZeroIntermediate[nonZeroIntermediateCount++] = j
                }
                intermediate[j] = prev + value * kernel[x2 - x + radius]
                x2 += 1
            }
            i += 1
        }
        nonZeroIntermediateCountOut(nonZeroIntermediateCount)

        // Vertical spread into `output` (tileSize x tileSize, row-major).
        i = 0
        while (i < nonZeroIntermediateCount) {
            val idx = nonZeroIntermediate[i]
            val y = idx / gridDim
            val x = idx - y * gridDim
            val value = intermediate[idx]
            val yStart = lowerLimit.coerceAtLeast(y - radius)
            val yEndExclusive = (upperLimit.coerceAtMost(y + radius)) + 1
            val xOut = x - radius
            var y2 = yStart
            while (y2 < yEndExclusive) {
                output[(y2 - radius) * tileSize + xOut] += value * kernel[y2 - y + radius]
                y2 += 1
            }
            i += 1
        }
    }

    private fun colorizeToColors(
        input: FloatArray,
        colors: IntArray,
        colorMap: IntArray,
        max: Double,
    ) {
        val lastIndex = colorMap.size - 1
        val maxColor = colorMap[lastIndex]
        val scaling = (lastIndex.toFloat() / max.toFloat())
        var i = 0
        val n = input.size
        while (i < n) {
            val value = input[i]
            if (value == 0.0f) {
                colors[i] = Color.TRANSPARENT
            } else {
                val colorIndex = (value * scaling).toInt()
                colors[i] = if (colorIndex <= lastIndex) colorMap[colorIndex] else maxColor
            }
            i += 1
        }
    }

    private fun encodePngFromIntensity(
        intensity: FloatArray,
        colorMap: IntArray,
        maxIntensity: Double,
        width: Int,
        height: Int,
        buffers: PngBuffers,
        compressionLevel: Int,
    ): ByteArray {
        buffers.ensureRow(width)
        buffers.ensureOutCapacity(width, height)
        buffers.out.reset()

        val out = buffers.out
        writePngSignature(out)
        writeIhdr(buffers.ihdr, width, height)
        val crc32 = buffers.crc32
        writePngChunk(out, PNG_IHDR, buffers.ihdr, 0, buffers.ihdr.size, crc32)

        // Stream zlib output directly into a single IDAT chunk.
        val idatLenPos = out.position()
        out.writeInt32BE(0) // placeholder length
        out.writeBytes(PNG_IDAT)
        crc32.reset()
        crc32.update(PNG_IDAT)
        val idatDataStart = out.position()

        val row = buffers.row
        val lastIndex = colorMap.size - 1
        val maxColor = colorMap[lastIndex]
        val scaling = (lastIndex.toFloat() / maxIntensity.toFloat())

        var srcIndex = 0
        if (compressionLevel == 0) {
            // Bypass Deflater; write a zlib stream with stored (uncompressed) DEFLATE blocks.
            val adler32 = buffers.adler32
            adler32.reset()
            writeIdatData(out, crc32, ZLIB_HEADER_NO_COMPRESSION, 0, ZLIB_HEADER_NO_COMPRESSION.size)
            for (y in 0 until height) {
                val rowLen = fillRowRgba(row, intensity, colorMap, maxColor, scaling, srcIndex, width)
                srcIndex += width
                adler32.update(row, 0, rowLen)
                writeZlibStoredBlock(out, crc32, buffers.zlibBlockHeader, row, 0, rowLen)
            }
            // Final empty block with BFINAL=1 and Adler32 checksum.
            writeIdatData(out, crc32, ZLIB_FINAL_EMPTY_BLOCK, 0, ZLIB_FINAL_EMPTY_BLOCK.size)
            writeInt32BE(buffers.adlerBuf, 0, adler32.value.toInt())
            writeIdatData(out, crc32, buffers.adlerBuf, 0, 4)
        } else {
            val deflater = buffers.deflater
            deflater.reset()
            deflater.setLevel(compressionLevel.coerceIn(0, 9))
            for (y in 0 until height) {
                val rowLen = fillRowRgba(row, intensity, colorMap, maxColor, scaling, srcIndex, width)
                srcIndex += width
                deflater.setInput(row, 0, rowLen)
                while (!deflater.needsInput()) {
                    val n = deflater.deflate(buffers.deflateBuf)
                    if (n > 0) {
                        writeIdatData(out, crc32, buffers.deflateBuf, 0, n)
                    }
                }
            }
            deflater.finish()
            while (!deflater.finished()) {
                val n = deflater.deflate(buffers.deflateBuf)
                if (n > 0) {
                    writeIdatData(out, crc32, buffers.deflateBuf, 0, n)
                }
            }
        }

        val idatLen = out.position() - idatDataStart
        out.setInt32BE(idatLenPos, idatLen)
        out.writeInt32BE(crc32.value.toInt())

        writePngChunk(out, PNG_IEND, EMPTY_BYTES, 0, 0, crc32)
        return out.toByteArray()
    }

    private fun fillRowRgba(
        row: ByteArray,
        intensity: FloatArray,
        colorMap: IntArray,
        maxColor: Int,
        scaling: Float,
        srcIndexStart: Int,
        width: Int,
    ): Int {
        row[0] = 0 // filter type 0 (None)
        var p = 1
        val rowEnd = srcIndexStart + width
        var srcIndex = srcIndexStart
        val lastIndex = colorMap.size - 1
        while (srcIndex < rowEnd) {
            val value = intensity[srcIndex]
            if (value == 0.0f) {
                var run = 1
                while (srcIndex + run < rowEnd && intensity[srcIndex + run] == 0.0f) {
                    run += 1
                }
                val end = p + run * 4
                Arrays.fill(row, p, end, 0)
                p = end
                srcIndex += run
                continue
            }
            val colorIndex = (value * scaling).toInt()
            val c = if (colorIndex <= lastIndex) colorMap[colorIndex] else maxColor
            row[p++] = ((c ushr 16) and 0xff).toByte() // r
            row[p++] = ((c ushr 8) and 0xff).toByte() // g
            row[p++] = (c and 0xff).toByte() // b
            row[p++] = ((c ushr 24) and 0xff).toByte() // a
            srcIndex += 1
        }
        return p
    }

    private fun writeIdatData(
        out: ByteArrayBuilder,
        crc32: CRC32,
        data: ByteArray,
        offset: Int,
        len: Int,
    ) {
        if (len <= 0) return
        out.writeBytes(data, offset, len)
        crc32.update(data, offset, len)
    }

    private fun writeZlibStoredBlock(
        out: ByteArrayBuilder,
        crc32: CRC32,
        header: ByteArray,
        data: ByteArray,
        offset: Int,
        len: Int,
    ) {
        // One stored (uncompressed) DEFLATE block. This is valid as long as len <= 65535.
        val safeLen = len.coerceIn(0, 65535)
        header[0] = 0x00 // BFINAL=0, BTYPE=00
        header[1] = (safeLen and 0xff).toByte()
        header[2] = ((safeLen ushr 8) and 0xff).toByte()
        val nlen = safeLen.inv() and 0xFFFF
        header[3] = (nlen and 0xff).toByte()
        header[4] = ((nlen ushr 8) and 0xff).toByte()
        writeIdatData(out, crc32, header, 0, 5)
        writeIdatData(out, crc32, data, offset, safeLen)
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
        val index: PointIndex?,
        val bounds: Bounds?,
        val radiusPx: Int,
        val colorMap: IntArray,
        val maxIntensities: DoubleArray,
    )

    private data class PointIndex(
        val gridSize: Int,
        val heads: IntArray,
        val next: IntArray,
        val nonEmptyBuckets: Int,
        val maxBucketSize: Int,
    )

    companion object {
        // 256 is the de-facto standard tile size across map SDKs; some (e.g. ArcGIS WebTiledLayer)
        // behave inconsistently when given 512 here, which can lead to mismatched (z,x,y) requests.
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
        private const val DEFAULT_MAX_CONCURRENT_RENDERS = 2
        private const val MAX_MAX_CONCURRENT_RENDERS = 8
        private const val MAX_RENDER_QUEUE_SIZE = 2048
        private const val DEFAULT_INDEX_GRID_SIZE = 128
        const val DEFAULT_PNG_COMPRESSION_LEVEL = 1
        private const val INDEX_BUILD_THRESHOLD = 1024
        private const val CAMERA_ZOOM_KEY_SCALE = 4
        private const val TAG = "HeatmapTileRenderer"
        private const val SLOW_TILE_LOG_THRESHOLD_MS = 250.0
        private const val PNG_COMPLEX_TILE_POINT_THRESHOLD = 128
        private const val PNG_COMPLEX_TILE_RADIUS_THRESHOLD_PX = 8
        private val PNG_SIGNATURE =
            byteArrayOf(
                0x89.toByte(),
                0x50,
                0x4E,
                0x47,
                0x0D,
                0x0A,
                0x1A,
                0x0A,
            )
        private val PNG_IHDR = byteArrayOf(0x49, 0x48, 0x44, 0x52) // IHDR
        private val PNG_IDAT = byteArrayOf(0x49, 0x44, 0x41, 0x54) // IDAT
        private val PNG_IEND = byteArrayOf(0x49, 0x45, 0x4E, 0x44) // IEND
        private val ZLIB_HEADER_NO_COMPRESSION = byteArrayOf(0x78.toByte(), 0x01)
        private val ZLIB_FINAL_EMPTY_BLOCK = byteArrayOf(0x01, 0x00, 0x00, 0xFF.toByte(), 0xFF.toByte())
        private val EMPTY_BYTES = ByteArray(0)
    }

    private data class RenderJob(
        val key: String,
        val epoch: Long,
        val enqueuedAtNs: Long,
        val request: TileRequest,
        val state: TileState,
        val future: CompletableFuture<ByteArray?>,
    )

    private data class XRange(
        val min: Double,
        val max: Double,
        val offset: Double,
    )

    private fun buildTileXRanges(
        minX: Double,
        maxX: Double,
    ): List<XRange> {
        if (minX <= 0.0 && maxX >= WORLD_WIDTH) {
            return listOf(XRange(min = 0.0, max = WORLD_WIDTH, offset = 0.0))
        }
        if (minX < 0.0) {
            return listOf(
                XRange(min = 0.0, max = maxX, offset = 0.0),
                XRange(min = minX + WORLD_WIDTH, max = WORLD_WIDTH, offset = -WORLD_WIDTH),
            )
        }
        if (maxX > WORLD_WIDTH) {
            return listOf(
                XRange(min = minX, max = WORLD_WIDTH, offset = 0.0),
                XRange(min = 0.0, max = maxX - WORLD_WIDTH, offset = WORLD_WIDTH),
            )
        }
        return listOf(XRange(min = minX, max = maxX, offset = 0.0))
    }

    private fun buildPointIndex(points: List<WeightedPoint>): PointIndex {
        val gridSize = DEFAULT_INDEX_GRID_SIZE
        val heads = IntArray(gridSize * gridSize) { -1 }
        val next = IntArray(points.size) { -1 }
        val counts = IntArray(gridSize * gridSize)
        var nonEmptyBuckets = 0
        var maxBucketSize = 0
        for (i in points.indices) {
            val p = points[i]
            val cx = (p.x * gridSize).toInt().coerceIn(0, gridSize - 1)
            val cy = (p.y * gridSize).toInt().coerceIn(0, gridSize - 1)
            val idx = cy * gridSize + cx
            next[i] = heads[idx]
            heads[idx] = i
            val c = counts[idx] + 1
            if (c == 1) nonEmptyBuckets += 1
            counts[idx] = c
            if (c > maxBucketSize) maxBucketSize = c
        }
        return PointIndex(
            gridSize = gridSize,
            heads = heads,
            next = next,
            nonEmptyBuckets = nonEmptyBuckets,
            maxBucketSize = maxBucketSize,
        )
    }

    private class RenderBuffers {
        var gridDim: Int = 0
        private var gridDimCapacity: Int = 0
        var tileSize: Int = 0
        var intensity: FloatArray = FloatArray(0)
        var intermediate: FloatArray = FloatArray(0)
        var output: FloatArray = FloatArray(0)
        var colors: IntArray = IntArray(0)
        var png: PngBuffers = PngBuffers()
        var nonZeroIntensity: IntArray = IntArray(0)
        var nonZeroIntermediate: IntArray = IntArray(0)
        var nonZeroIntensityCount: Int = 0
        var nonZeroIntermediateCount: Int = 0

        fun ensure(
            gridDim: Int,
            tileSize: Int,
        ) {
            this.gridDim = gridDim
            if (gridDimCapacity < gridDim) {
                gridDimCapacity = gridDim
                this.gridDim = gridDim
                intensity = FloatArray(gridDimCapacity * gridDimCapacity)
                intermediate = FloatArray(gridDimCapacity * gridDimCapacity)
                nonZeroIntensity = IntArray(gridDimCapacity * gridDimCapacity)
                nonZeroIntermediate = IntArray(gridDimCapacity * gridDimCapacity)
            }
            if (this.tileSize != tileSize) {
                this.tileSize = tileSize
                output = FloatArray(tileSize * tileSize)
                colors = IntArray(tileSize * tileSize)
            }
        }
    }

    private class ByteArrayBuilder(
        initialCapacity: Int,
    ) {
        private var buf: ByteArray = ByteArray(initialCapacity.coerceAtLeast(16))
        private var count: Int = 0

        fun position(): Int = count

        fun reset() {
            count = 0
        }

        fun ensureCapacity(minCapacity: Int) {
            if (buf.size >= minCapacity) return
            var newCap = buf.size
            while (newCap < minCapacity) {
                newCap = (newCap * 2).coerceAtLeast(16)
            }
            buf = buf.copyOf(newCap)
        }

        fun setInt32BE(
            offset: Int,
            value: Int,
        ) {
            if (offset < 0 || offset + 4 > count) {
                throw IndexOutOfBoundsException("offset=$offset count=$count")
            }
            buf[offset] = ((value ushr 24) and 0xff).toByte()
            buf[offset + 1] = ((value ushr 16) and 0xff).toByte()
            buf[offset + 2] = ((value ushr 8) and 0xff).toByte()
            buf[offset + 3] = (value and 0xff).toByte()
        }

        fun writeByte(value: Int) {
            ensureCapacity(count + 1)
            buf[count++] = value.toByte()
        }

        fun writeInt32BE(value: Int) {
            ensureCapacity(count + 4)
            buf[count++] = ((value ushr 24) and 0xff).toByte()
            buf[count++] = ((value ushr 16) and 0xff).toByte()
            buf[count++] = ((value ushr 8) and 0xff).toByte()
            buf[count++] = (value and 0xff).toByte()
        }

        fun writeBytes(bytes: ByteArray) {
            writeBytes(bytes, 0, bytes.size)
        }

        fun writeBytes(
            bytes: ByteArray,
            offset: Int,
            len: Int,
        ) {
            if (len <= 0) return
            ensureCapacity(count + len)
            System.arraycopy(bytes, offset, buf, count, len)
            count += len
        }

        fun toByteArray(): ByteArray = buf.copyOf(count)
    }

    private class PngBuffers {
        var row: ByteArray = ByteArray(0)
        var zlibBlockHeader: ByteArray = ByteArray(5)
        var adlerBuf: ByteArray = ByteArray(4)
        var deflateBuf: ByteArray = ByteArray(128 * 1024)
        var deflater: Deflater = Deflater(DEFAULT_PNG_COMPRESSION_LEVEL)
        var crc32: CRC32 = CRC32()
        var adler32: Adler32 = Adler32()
        var ihdr: ByteArray = ByteArray(13)
        var out: ByteArrayBuilder = ByteArrayBuilder(512 * 1024)

        fun ensureRow(width: Int) {
            val needed = 1 + width * 4
            if (row.size != needed) {
                row = ByteArray(needed)
            }
        }

        fun ensureOutCapacity(
            width: Int,
            height: Int,
        ) {
            // Rough estimate: signature + IHDR/IEND overhead + zlib stream ~ raw bytes (level 0).
            val raw = height * (1 + width * 4)
            val estimated = 8 + 64 + raw + raw / 64
            out.ensureCapacity(estimated)
        }
    }

    private fun encodePngRgba(
        colors: IntArray,
        width: Int,
        height: Int,
        buffers: PngBuffers,
        compressionLevel: Int,
    ): ByteArray {
        buffers.ensureRow(width)
        buffers.ensureOutCapacity(width, height)
        buffers.out.reset()

        val deflater = buffers.deflater
        deflater.reset()
        deflater.setLevel(compressionLevel.coerceIn(0, 9))

        val out = buffers.out
        writePngSignature(out)
        writeIhdr(buffers.ihdr, width, height)
        val crc32 = buffers.crc32
        writePngChunk(out, PNG_IHDR, buffers.ihdr, 0, buffers.ihdr.size, crc32)

        // Stream zlib output directly into a single IDAT chunk to avoid large intermediate buffers/copies.
        val idatLenPos = out.position()
        out.writeInt32BE(0) // placeholder length
        out.writeBytes(PNG_IDAT)
        crc32.reset()
        crc32.update(PNG_IDAT)
        val idatDataStart = out.position()

        var srcIndex = 0
        for (y in 0 until height) {
            val row = buffers.row
            row[0] = 0 // filter type 0 (None)
            var p = 1
            val rowEnd = srcIndex + width
            while (srcIndex < rowEnd) {
                val c = colors[srcIndex]
                val a = (c ushr 24) and 0xff
                val r = (c ushr 16) and 0xff
                val g = (c ushr 8) and 0xff
                val b = c and 0xff
                row[p++] = r.toByte()
                row[p++] = g.toByte()
                row[p++] = b.toByte()
                row[p++] = a.toByte()
                srcIndex += 1
            }
            deflater.setInput(row, 0, p)
            while (!deflater.needsInput()) {
                val n = deflater.deflate(buffers.deflateBuf)
                if (n > 0) {
                    out.writeBytes(buffers.deflateBuf, 0, n)
                    crc32.update(buffers.deflateBuf, 0, n)
                }
            }
        }
        deflater.finish()
        while (!deflater.finished()) {
            val n = deflater.deflate(buffers.deflateBuf)
            if (n > 0) {
                out.writeBytes(buffers.deflateBuf, 0, n)
                crc32.update(buffers.deflateBuf, 0, n)
            }
        }

        val idatLen = out.position() - idatDataStart
        out.setInt32BE(idatLenPos, idatLen)
        out.writeInt32BE(crc32.value.toInt())

        writePngChunk(out, PNG_IEND, EMPTY_BYTES, 0, 0, crc32)
        return out.toByteArray()
    }

    private fun writePngSignature(out: ByteArrayBuilder) {
        out.writeBytes(PNG_SIGNATURE)
    }

    private fun writeIhdr(
        out: ByteArray,
        width: Int,
        height: Int,
    ) {
        writeInt32BE(out, 0, width)
        writeInt32BE(out, 4, height)
        out[8] = 8 // bit depth
        out[9] = 6 // color type: RGBA
        out[10] = 0 // compression method
        out[11] = 0 // filter method
        out[12] = 0 // interlace method
    }

    private fun writePngChunk(
        out: ByteArrayBuilder,
        type: ByteArray,
        data: ByteArray,
        offset: Int,
        len: Int,
        crc32: CRC32,
    ) {
        out.writeInt32BE(len)
        out.writeBytes(type)
        if (len > 0) {
            out.writeBytes(data, offset, len)
        }
        crc32.reset()
        crc32.update(type)
        if (len > 0) {
            crc32.update(data, offset, len)
        }
        out.writeInt32BE(crc32.value.toInt())
    }

    private fun writeInt32BE(
        buf: ByteArray,
        offset: Int,
        value: Int,
    ) {
        buf[offset] = ((value ushr 24) and 0xff).toByte()
        buf[offset + 1] = ((value ushr 16) and 0xff).toByte()
        buf[offset + 2] = ((value ushr 8) and 0xff).toByte()
        buf[offset + 3] = (value and 0xff).toByte()
    }

    private val threadLocalBuffers = ThreadLocal<RenderBuffers>()

    private class PhaseTimings {
        var effectiveZoom: Double = 0.0
        var radius: Int = 0
        var gridDim: Int = 0
        var usedIndex: Boolean = false
        var indexGridSize: Int = 0
        var indexNonEmptyBuckets: Int = 0
        var indexMaxBucketSize: Int = 0
        var xRanges: Int = 0
        var cellsVisited: Int = 0
        var candidatesVisited: Int = 0
        var pointsBinned: Int = 0
        var setupMs: Double = 0.0
        var binMs: Double = 0.0
        var convolveMs: Double = 0.0
        var colorizeMs: Double = 0.0
        var pngMs: Double = 0.0
        var pngLevel: Int = 0

        fun reset() {
            effectiveZoom = 0.0
            radius = 0
            gridDim = 0
            usedIndex = false
            indexGridSize = 0
            indexNonEmptyBuckets = 0
            indexMaxBucketSize = 0
            xRanges = 0
            cellsVisited = 0
            candidatesVisited = 0
            pointsBinned = 0
            setupMs = 0.0
            binMs = 0.0
            convolveMs = 0.0
            colorizeMs = 0.0
            pngMs = 0.0
            pngLevel = 0
        }
    }

    private val threadLocalPhaseTimings = ThreadLocal<PhaseTimings>()

    private fun phaseTimings(): PhaseTimings {
        val existing = threadLocalPhaseTimings.get()
        if (existing != null) return existing
        val created = PhaseTimings()
        threadLocalPhaseTimings.set(created)
        return created
    }

    private fun msSince(startNs: Long): Double {
        val ms = (System.nanoTime() - startNs) / 1_000_000.0
        return (ms * 10.0).roundToInt() / 10.0
    }

    private fun buffers(): RenderBuffers {
        val existing = threadLocalBuffers.get()
        if (existing != null) return existing
        val created = RenderBuffers()
        threadLocalBuffers.set(created)
        return created
    }
}
