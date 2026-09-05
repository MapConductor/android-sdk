package com.mapconductor.vectortile

import com.mapconductor.core.tileserver.TilePngEncoder
import android.util.Log
import java.io.Closeable
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors

/**
 * Rasterises triangulated tiles on the GPU.
 *
 * **Everything runs on one dedicated thread**, because an EGL context belongs
 * to the thread that made it current. The tile server calls `renderTile` from
 * a pool of eight workers, so requests are marshalled onto this thread and the
 * caller blocks — which is also the right shape for the work: drawing a tile
 * takes about 2 ms, so serialising it costs nothing, and the GPU is a single
 * resource anyway.
 *
 * Creating the context is the expensive part (~15 ms), so it is created once
 * on first use and kept.
 */
internal class GpuTileRasterizer(private val tileSize: Int) : Closeable {

    private val thread = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "vectortile-gl").apply { isDaemon = true }
    }

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
            val ready = try {
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
    private fun initialise(): Boolean = try {
        val context = EglOffscreen.create()
        context.prepare(tileSize)
        val batch = SolidBatchRenderer()
        batch.initialise()
        egl = context
        renderer = batch
        pixels = ByteBuffer
            .allocateDirect(tileSize * tileSize * 4)
            .order(ByteOrder.nativeOrder())
        true
    } catch (error: Throwable) {
        Log.w(TAG, "GL initialisation failed", error)
        false
    }

    /** Describes the context in use, for diagnostics. */
    fun describe(): String = try {
        thread.submit(Callable {
            val context = egl
            if (context == null) "unavailable" else "ES${context.esVersion} MSAA x${context.activeSamples}"
        }).get()
    } catch (error: Exception) {
        "unavailable"
    }

    /**
     * Draws [tile] and returns PNG bytes, or null if the GPU path failed.
     *
     * Encoding happens in Rust against the readback buffer directly: the
     * platform encoder costs more than the entire rest of the GPU path.
     */
    fun renderPng(tile: TessellatedTile): ByteArray? {
        if (closed) return null
        return try {
            thread.submit(Callable { drawOnGlThread(tile) }).get()
        } catch (error: ExecutionException) {
            Log.w(TAG, "GPU render failed", error.cause ?: error)
            null
        } catch (error: Exception) {
            Log.w(TAG, "GPU render failed", error)
            null
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
        // The core's encoder, not this module's: one copy of it per app.
        // glReadPixels leaves straight alpha here — the blend keeps destination
        // alpha saturated — so there is nothing to un-premultiply.
        return TilePngEncoder.encode(buffer, tileSize, tileSize, premultiplied = false)
    }

    override fun close() {
        if (closed) return
        closed = true
        try {
            thread.submit {
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
