package com.mapconductor.vectortile

import com.mapconductor.core.tileserver.TilePngEncoder
import java.io.Closeable
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import android.util.Log

/**
 * Rasterises triangulated tiles on the GPU.
 *
 * **The GL calls run on one dedicated thread**, because an EGL context belongs
 * to the thread that made it current. The tile server calls `renderTile` from
 * a pool of eight workers, so those calls are marshalled onto this thread and
 * the caller blocks. That is the right shape for the drawing itself: it takes
 * about 15 ms and the GPU is a single resource anyway.
 *
 * Everything after the readback is deliberately *not* on that thread. Labels
 * are drawn on the CPU over the pixels and the PNG is encoded from them, and
 * both used to run on the GL thread, where they serialised: measured on a
 * Pixel 5a they are 30 to 130 ms a tile, so eight workers spent their time
 * queueing behind one another for work that has nothing to do with the GPU.
 *
 * Creating the context is the expensive part (~15 ms), so it is created once
 * on first use and kept.
 */
internal class GpuTileRasterizer(
    private val tileSize: Int,
) : Closeable {
    private val thread =
        Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable, "vectortile-gl").apply { isDaemon = true }
        }

    /** Direct buffers for the encoder, one per worker that might be encoding. */
    private val encodeBuffers = java.util.concurrent.ArrayBlockingQueue<ByteBuffer>(8)

    private var egl: EglOffscreen? = null
    private var renderer: SolidBatchRenderer? = null
    private var pixels: ByteBuffer? = null

    @Volatile
    private var closed = false

    companion object {
        private const val TAG = "VectorTileGpu"

        /**
         * Creates a rasteriser, or null when this device cannot give us an
         * off-screen GL context.
         *
         * Probing here rather than at first tile means the provider can fall
         * back to the CPU before anything is served, instead of failing tiles
         * one by one.
         */
        fun createOrNull(tileSize: Int): GpuTileRasterizer? {
            val candidate = GpuTileRasterizer(tileSize)
            val ready =
                try {
                    candidate.thread.submit(Callable { candidate.initialise() }).get()
                } catch (error: Exception) {
                    Log.w(TAG, "no GL context available", error)
                    false
                }
            if (ready) return candidate
            candidate.close()
            return null
        }
    }

    /** Runs on the GL thread. */
    private fun initialise(): Boolean =
        try {
            val context = EglOffscreen.create()
            context.prepare(tileSize)
            val batch = SolidBatchRenderer()
            batch.initialise()
            egl = context
            renderer = batch
            pixels =
                ByteBuffer
                    .allocateDirect(tileSize * tileSize * 4)
                    .order(ByteOrder.nativeOrder())
            true
        } catch (error: Throwable) {
            Log.w(TAG, "GL initialisation failed", error)
            false
        }

    /** Describes the context in use, for diagnostics. */
    fun describe(): String =
        try {
            thread
                .submit(
                    Callable {
                        val context = egl
                        if (context == null) "unavailable" else "ES${context.esVersion} MSAA x${context.activeSamples}"
                    },
                ).get()
        } catch (error: Exception) {
            "unavailable"
        }

    /**
     * Draws [tile] and returns PNG bytes, or null if the GPU path failed.
     *
     * Encoding happens in Rust against the readback buffer directly: the
     * platform encoder costs more than the entire rest of the GPU path.
     *
     * @param decorate given the readback in RGBA, before it is encoded. Used
     *   to draw labels, which the tessellator does not produce — the GL thread
     *   owns the buffer, so anything touching it has to run here.
     */
    fun renderPng(
        tile: TessellatedTile,
        decorate: ((ByteArray) -> Unit)? = null,
    ): ByteArray? {
        if (closed) return null
        val rgba =
            try {
                thread.submit(Callable { drawOnGlThread(tile) }).get()
            } catch (error: ExecutionException) {
                Log.w(TAG, "GPU render failed", error.cause ?: error)
                null
            } catch (error: Exception) {
                Log.w(TAG, "GPU render failed", error)
                null
            } ?: return null

        val labelsStarted = System.nanoTime()
        decorate?.invoke(rgba)
        val labelMs = (System.nanoTime() - labelsStarted) / 1_000_000

        val encodeStarted = System.nanoTime()
        val png = encode(rgba)
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(
                TAG,
                "phases decode=${tile.decodeMs.toInt()}ms " +
                    "tessellate=${tile.tessellateMs.toInt()}ms " +
                    "(loop=${tile.featureLoopMs.toInt()} of which fills=${tile.fillMs.toInt()} " +
                    "lines=${tile.lineMs.toInt()}; filters=${tile.filterMs.toInt()}) " +
                    "labels=${labelMs}ms encode=${(System.nanoTime() - encodeStarted) / 1_000_000}ms",
            )
        }
        return png
    }

    /**
     * Encodes off the GL thread, through a borrowed direct buffer.
     *
     * The encoder needs a direct buffer and the pixels arrive as a byte array,
     * so there is a copy either way; borrowing avoids a megabyte of fresh
     * direct memory per tile.
     */
    private fun encode(rgba: ByteArray): ByteArray? {
        val buffer = encodeBuffers.poll() ?: ByteBuffer.allocateDirect(tileSize * tileSize * 4)
        return try {
            buffer.clear()
            buffer.put(rgba)
            buffer.rewind()
            TilePngEncoder.encode(buffer, tileSize, tileSize, premultiplied = false)
        } finally {
            encodeBuffers.offer(buffer)
        }
    }

    /** Runs on the GL thread. */
    private fun drawOnGlThread(tile: TessellatedTile): ByteArray? {
        val context = egl ?: return null
        val batch = renderer ?: return null
        val buffer = pixels ?: return null

        batch.upload(tile.vertices)
        context.bindDrawTarget()
        val background = tile.background
        if (background != null) {
            batch.clear(background[0], background[1], background[2], background[3])
        } else {
            batch.clear(0f, 0f, 0f, 0f)
        }
        batch.draw(tile.batches, tile.extent)
        context.readPixels(buffer)
        buffer.rewind()

        // Copied out so the shared readback buffer is free for the next tile
        // the moment this one leaves the GL thread.
        val pixels = ByteArray(buffer.remaining())
        buffer.get(pixels)
        buffer.rewind()
        return pixels
    }

    override fun close() {
        if (closed) return
        closed = true
        try {
            thread
                .submit {
                    renderer?.close()
                    egl?.close()
                    renderer = null
                    egl = null
                }.get()
        } catch (_: Exception) {
            // Already gone; nothing useful to do while tearing down.
        }
        thread.shutdown()
    }
}
