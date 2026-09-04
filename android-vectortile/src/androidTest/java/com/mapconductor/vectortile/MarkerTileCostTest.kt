package com.mapconductor.vectortile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.marker.MarkerEntity
import com.mapconductor.core.marker.MarkerManager
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.marker.MarkerTileRenderer
import com.mapconductor.core.tileserver.TileRequest
import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Measures where the time actually goes when markers are baked into raster
 * tiles, so the question "would the Rust renderer make this faster?" can be
 * answered with numbers rather than intuition.
 *
 * Nothing in this suite asserts a threshold. It reports, because there is no
 * prior measurement of this path to regress against — which is itself the
 * finding that prompted it.
 */
@RunWith(AndroidJUnit4::class)
class MarkerTileCostTest {

    private val tileSize = 512

    /** Markers scattered over greater Tokyo, deterministic so runs compare. */
    private fun manager(count: Int): MarkerManager<Unit> {
        val manager = MarkerManager.defaultManager<Unit>(minMarkerCount = 1)
        var seed = 12345L
        fun next(): Double {
            // Deterministic LCG rather than Random, so a slow run can be
            // compared against a fast one marker for marker.
            seed = (seed * 6364136223846793005L + 1442695040888963407L)
            return ((seed ushr 11).toDouble() / (1L shl 53).toDouble())
        }
        repeat(count) {
            val lat = 35.5 + next() * 0.4
            val lng = 139.5 + next() * 0.5
            manager.registerEntity(
                MarkerEntity(
                    marker = null,
                    state = MarkerState(position = GeoPoint(lat, lng)),
                    visible = true,
                    isRendered = true,
                    tiling = true,
                ),
            )
        }
        return manager
    }

    private fun renderer(manager: MarkerManager<Unit>) =
        MarkerTileRenderer(
            markerManager = manager,
            tileSize = tileSize,
            cacheSizeBytes = 8 * 1024 * 1024,
        )

    private fun median(values: List<Double>): Double =
        values.sorted()[values.size / 2]

    /** Times one call, having thrown away the tile cache first. */
    private fun timeUncached(
        manager: MarkerManager<Unit>,
        request: TileRequest,
        rounds: Int = 5,
    ): Pair<Double, ByteArray?> {
        var bytes: ByteArray? = null
        val samples = ArrayList<Double>(rounds)
        repeat(rounds) {
            // A fresh renderer each round: the cache would otherwise answer
            // every call after the first, which is not what is being measured.
            val fresh = renderer(manager)
            val started = System.nanoTime()
            bytes = fresh.renderTile(request)
            samples.add((System.nanoTime() - started) / 1_000_000.0)
        }
        return median(samples) to bytes
    }

    @Test
    fun whereTheTimeGoesRenderingAMarkerTile() {
        for (markerCount in listOf(2_000, 20_000)) {
            val manager = manager(markerCount)

            // z=12 covers Tokyo in a handful of tiles: a realistic dense tile.
            // z=6 puts the entire dataset on one tile, which is the case the
            // renderer has no declutter for.
            for ((z, x, y) in listOf(Triple(12, 3637, 1612), Triple(6, 56, 25))) {
                val request = TileRequest(x = x, y = y, z = z)
                val (renderMs, bytes) = timeUncached(manager, request)
                if (bytes == null) {
                    println("MARKERTILE n=$markerCount z=$z EMPTY")
                    continue
                }

                // Re-encode the tile the renderer actually produced, so the
                // encoder comparison runs on real marker-tile content rather
                // than on noise — a mistake already made once in this project.
                val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    .copy(Bitmap.Config.ARGB_8888, false)

                val compressSamples = ArrayList<Double>(5)
                repeat(5) {
                    val stream = java.io.ByteArrayOutputStream(bytes.size)
                    val started = System.nanoTime()
                    decoded.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    compressSamples.add((System.nanoTime() - started) / 1_000_000.0)
                }

                val buffer = ByteBuffer
                    .allocateDirect(decoded.width * decoded.height * 4)
                    .order(ByteOrder.nativeOrder())
                decoded.copyPixelsToBuffer(buffer)
                val rustSamples = ArrayList<Double>(5)
                var rustBytes = 0
                repeat(5) {
                    buffer.rewind()
                    val started = System.nanoTime()
                    rustBytes = NativeRenderer.nativeEncodePng(
                        buffer, decoded.width, decoded.height,
                    ).size
                    rustSamples.add((System.nanoTime() - started) / 1_000_000.0)
                }

                val compressMs = median(compressSamples)
                val rustMs = median(rustSamples)
                println(
                    "MARKERTILE n=%d z=%d render=%.1fms png=%d compress=%.1fms rust=%.1fms rustBytes=%d encodeShare=%.0f%%"
                        .format(
                            markerCount, z, renderMs, bytes.size,
                            compressMs, rustMs, rustBytes,
                            compressMs / renderMs * 100,
                        ),
                )
                decoded.recycle()
            }
        }
    }

    /** How much the tile cache is worth, and therefore what the web SW path loses by lacking one. */
    @Test
    fun whatTheTileCacheIsWorth() {
        val manager = manager(20_000)
        val live = renderer(manager)
        val request = TileRequest(x = 3637, y = 1612, z = 12)

        val coldStarted = System.nanoTime()
        live.renderTile(request)
        val coldMs = (System.nanoTime() - coldStarted) / 1_000_000.0

        val warmSamples = ArrayList<Double>(10)
        repeat(10) {
            val started = System.nanoTime()
            live.renderTile(request)
            warmSamples.add((System.nanoTime() - started) / 1_000_000.0)
        }
        println("MARKERTILE_CACHE cold=%.1fms warm=%.3fms".format(coldMs, median(warmSamples)))
    }
    /**
     * Splits the low-zoom case into its parts.
     *
     * z=6 with 20k markers takes seconds per tile, and the encoder is 1% of
     * that, so the cost is somewhere in query/prepare/draw. Which one decides
     * whether a GPU rasteriser would help at all.
     */
    @Test
    fun whatMakesTheLowZoomTileSlow() {
        val manager = manager(20_000)
        val bounds = com.mapconductor.core.features.GeoRectBounds().apply {
            extend(GeoPoint(35.4, 139.4))
            extend(GeoPoint(36.0, 140.1))
        }

        val querySamples = ArrayList<Double>(5)
        var found = 0
        repeat(5) {
            val started = System.nanoTime()
            found = manager.findMarkersInBounds(bounds).filter { it.tiling }.size
            querySamples.add((System.nanoTime() - started) / 1_000_000.0)
        }

        val entities = manager.findMarkersInBounds(bounds).filter { it.tiling }

        // A stand-in for the default pin at this device's density. The real one
        // cannot be constructed from here (its Compose-typed constructor does
        // not resolve across the test APK), and it does not need to be: the
        // renderer's icon lookup is a BitmapIconCache hit, so what is being
        // timed is the blit, and the blit only cares about the pixel size.
        val iconPx = com.mapconductor.core.ResourceProvider.dpToPxForBitmap(32.0).toInt()
        val icon = Bitmap.createBitmap(iconPx, iconPx, Bitmap.Config.ARGB_8888).apply {
            eraseColor(android.graphics.Color.argb(220, 40, 90, 200))
        }

        // The prepare pass, minus the cached icon lookup: projection per marker.
        val prepareSamples = ArrayList<Double>(5)
        repeat(5) {
            val started = System.nanoTime()
            var sink = 0.0
            for (entity in entities) {
                sink += entity.state.position.latitude + entity.state.position.longitude
            }
            prepareSamples.add((System.nanoTime() - started) / 1_000_000.0)
            check(sink != 0.0)
        }

        // The draw loop on its own: one scaled, filtered blit per marker.
        val canvas = Bitmap.createBitmap(700, 700, Bitmap.Config.ARGB_8888)
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            isFilterBitmap = true
            isDither = true
        }
        val drawSamples = ArrayList<Double>(5)
        repeat(5) {
            val target = android.graphics.Canvas(canvas)
            val started = System.nanoTime()
            for (index in entities.indices) {
                val left = (index % 600).toFloat()
                val top = ((index / 600) % 600).toFloat()
                target.drawBitmap(
                    icon,
                    null,
                    android.graphics.RectF(left, top, left + iconPx, top + iconPx),
                    paint,
                )
            }
            drawSamples.add((System.nanoTime() - started) / 1_000_000.0)
        }
        canvas.recycle()
        icon.recycle()

        println(
            "MARKERTILE_PHASES markers=%d query=%.1fms prepare=%.1fms draw=%.1fms iconPx=%dx%d"
                .format(
                    found, median(querySamples), median(prepareSamples), median(drawSamples),
                    iconPx, iconPx,
                ),
        )
    }

    /**
     * Why one blit costs 0.46 ms inside the renderer but 0.01 ms in isolation.
     *
     * The two differ in three ways — canvas size, whether the destination lands
     * on whole pixels, and how much the marks overlap — so this varies them one
     * at a time instead of guessing.
     */
    @Test
    fun whatMakesTheBlitSlow() {
        val iconPx = com.mapconductor.core.ResourceProvider.dpToPxForBitmap(32.0).toInt()
        val icon = Bitmap.createBitmap(iconPx, iconPx, Bitmap.Config.ARGB_8888).apply {
            eraseColor(android.graphics.Color.argb(220, 40, 90, 200))
        }
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            isFilterBitmap = true
            isDither = true
        }
        val count = 20_000

        fun run(canvasPx: Int, fractional: Boolean, spreadPx: Int): Double {
            val surface = Bitmap.createBitmap(canvasPx, canvasPx, Bitmap.Config.ARGB_8888)
            val samples = ArrayList<Double>(3)
            repeat(3) {
                val target = android.graphics.Canvas(surface)
                val started = System.nanoTime()
                for (index in 0 until count) {
                    val base = (index % spreadPx).toFloat()
                    val top = ((index / spreadPx) % spreadPx).toFloat()
                    // 0.37 is arbitrary; the point is only that it is not a whole pixel.
                    val offset = if (fractional) 0.37f else 0f
                    val left = base + offset
                    val up = top + offset
                    target.drawBitmap(
                        icon, null,
                        android.graphics.RectF(left, up, left + iconPx, up + iconPx),
                        paint,
                    )
                }
                samples.add((System.nanoTime() - started) / 1_000_000.0)
            }
            surface.recycle()
            return median(samples)
        }

        val small = run(700, fractional = false, spreadPx = 600)
        val big = run(1600, fractional = false, spreadPx = 600)
        val bigFractional = run(1600, fractional = true, spreadPx = 600)
        val bigFractionalStacked = run(1600, fractional = true, spreadPx = 40)
        val bigStacked = run(1600, fractional = false, spreadPx = 40)
        icon.recycle()

        println(
            ("MARKERTILE_BLIT n=%d icon=%d small=%.0fms big=%.0fms " +
                "bigFractional=%.0fms bigStacked=%.0fms bigFractionalStacked=%.0fms")
                .format(count, iconPx, small, big, bigFractional, bigStacked, bigFractionalStacked),
        )
    }

    /** Two candidate fixes for the sub-pixel blit cost, measured against it. */
    @Test
    fun candidateFixesForTheBlit() {
        val iconPx = com.mapconductor.core.ResourceProvider.dpToPxForBitmap(32.0).toInt()
        val icon = Bitmap.createBitmap(iconPx, iconPx, Bitmap.Config.ARGB_8888).apply {
            eraseColor(android.graphics.Color.argb(220, 40, 90, 200))
        }
        val count = 20_000

        fun run(filter: Boolean, snap: Boolean): Double {
            val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                isFilterBitmap = filter
                isDither = true
            }
            val surface = Bitmap.createBitmap(1600, 1600, Bitmap.Config.ARGB_8888)
            val samples = ArrayList<Double>(3)
            repeat(3) {
                val target = android.graphics.Canvas(surface)
                val started = System.nanoTime()
                for (index in 0 until count) {
                    val leftRaw = (index % 600) + 0.37f
                    val topRaw = ((index / 600) % 600) + 0.37f
                    val left = if (snap) kotlin.math.round(leftRaw) else leftRaw
                    val up = if (snap) kotlin.math.round(topRaw) else topRaw
                    target.drawBitmap(
                        icon, null,
                        android.graphics.RectF(left, up, left + iconPx, up + iconPx),
                        paint,
                    )
                }
                samples.add((System.nanoTime() - started) / 1_000_000.0)
            }
            surface.recycle()
            return median(samples)
        }

        println(
            "MARKERTILE_FIX baseline=%.0fms noFilter=%.0fms snapped=%.0fms snappedNoFilter=%.0fms"
                .format(
                    run(filter = true, snap = false),
                    run(filter = false, snap = false),
                    run(filter = true, snap = true),
                    run(filter = false, snap = true),
                ),
        )
        icon.recycle()
    }

    /**
     * Markers land where their coordinates say they do.
     *
     * The draw rounds its destination to whole pixels, which is worth 20x but
     * would be worth nothing if it put the markers somewhere else. This pins
     * the placement independently of the timing work.
     */
    @Test
    fun markersLandWhereTheirCoordinatesSay() {
        val z = 12
        val tileX = 3638
        val tileY = 1612

        // Positions are derived from the tile rather than written down, so the
        // test cannot drift a marker into the neighbouring tile — which is
        // exactly what hand-computed coordinates did on the first attempt.
        val worldTilesForPlacement = 1 shl z
        fun atFraction(fx: Double, fy: Double): GeoPoint {
            val worldX = tileX + fx
            val worldY = tileY + fy
            val longitude = worldX / worldTilesForPlacement * 360.0 - 180.0
            val n = Math.PI * (1.0 - 2.0 * worldY / worldTilesForPlacement)
            val latitude = Math.toDegrees(kotlin.math.atan(kotlin.math.sinh(n)))
            return GeoPoint(latitude, longitude)
        }
        val positions = listOf(
            atFraction(0.25, 0.25),
            atFraction(0.50, 0.60),
            atFraction(0.75, 0.40),
        )
        val manager = MarkerManager.defaultManager<Unit>(minMarkerCount = 1)
        positions.forEach { position ->
            manager.registerEntity(
                MarkerEntity(
                    marker = null,
                    state = MarkerState(position = position),
                    visible = true,
                    isRendered = true,
                    tiling = true,
                ),
            )
        }

        val bytes = checkNotNull(renderer(manager).renderTile(TileRequest(x = tileX, y = tileY, z = z)))
        val tile = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        val worldTiles = 1 shl z

        fun tilePixel(position: GeoPoint): Pair<Int, Int> {
            val worldX = (position.longitude + 180.0) / 360.0 * worldTiles
            val latRad = Math.toRadians(position.latitude)
            val worldY = (
                1.0 - kotlin.math.ln(
                    kotlin.math.tan(latRad) + 1.0 / kotlin.math.cos(latRad),
                ) / Math.PI
            ) / 2.0 * worldTiles
            return Math.round((worldX - tileX) * tile.width).toInt() to
                Math.round((worldY - tileY) * tile.height).toInt()
        }

        /** Is anything drawn within `radius` px of (x, y)? */
        fun painted(x: Int, y: Int, radius: Int): Boolean {
            for (dy in -radius..radius) {
                for (dx in -radius..radius) {
                    val px = x + dx
                    val py = y + dy
                    if (px !in 0 until tile.width || py !in 0 until tile.height) continue
                    if (tile.getPixel(px, py) ushr 24 != 0) return true
                }
            }
            return false
        }

        for (position in positions) {
            val (x, y) = tilePixel(position)
            println("MARKERTILE_PLACE tile=${tile.width}x${tile.height} pos=$position -> $x,$y painted=${painted(x, y, 6)}")
            // The default pin is anchored near its tip, so the icon body sits
            // above the coordinate; a small box around it is the honest test.
            org.junit.Assert.assertTrue(
                "nothing drawn at $position -> $x,$y (tile ${tile.width})",
                painted(x, y, radius = 6),
            )
        }

        // Somewhere no marker is: the tile is not simply filled in.
        val (firstX, firstY) = tilePixel(positions[0])
        val emptyX = (firstX + tile.width / 3) % tile.width
        val emptyY = (firstY + tile.height / 3) % tile.height
        org.junit.Assert.assertFalse(
            "unexpected paint at $emptyX,$emptyY",
            painted(emptyX, emptyY, radius = 2),
        )
        tile.recycle()
    }
}
