package com.mapconductor.vectortile

import java.io.Closeable
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Thin JNI surface over the Rust renderer.
 *
 * Every call is one-way: Kotlin hands over coordinates and bytes and gets bytes
 * back. Rust never calls into the JVM, so no worker thread has to attach to it
 * — the one place JNI overhead would actually be felt.
 */
internal object NativeRenderer {
    init {
        System.loadLibrary("mvt_render_jni")
    }

    /**
     * Returns an opaque handle; throws IllegalArgumentException on a bad style.
     *
     * [displayTileSize] is how many dp one tile covers where the map shows it
     * -- not the pixel count a render is asked for. A label pass draws at
     * twice the pixels, and a 256dp tile is still 256dp however many pixels it
     * carries. It fixes the size the style draws at and the zoom its
     * expressions are read at.
     */
    external fun nativeNew(
        styleJson: String,
        displayTileSize: Int,
    ): Long

    external fun nativeFree(handle: Long)

    external fun nativeSetStyle(
        handle: Long,
        styleJson: String,
    )

    /** JSON array of `{ sourceId, url, z, x, y, scale, offsetX, offsetY }`. */
    external fun nativePlan(
        handle: Long,
        z: Int,
        x: Int,
        y: Int,
    ): String

    /**
     * @param data every fetched source tile concatenated in plan order
     * @param lengths each tile's byte length, 0 where the fetch produced nothing
     */
    external fun nativeRender(
        handle: Long,
        z: Int,
        x: Int,
        y: Int,
        tileSize: Int,
        data: ByteArray,
        lengths: IntArray,
        geometryOnly: Int,
    ): ByteArray

    /** The style's `glyphs` URL template, or empty when it has none. */
    external fun nativeGlyphsUrlTemplate(handle: Long): String

    /**
     * JSON array of glyph URLs this tile's labels need and the renderer does
     * not have.
     *
     * Answered from the tile rather than the style: ranges follow the text, and
     * a CJK font has 82 of them where a tile of Tokyo uses four.
     */
    external fun nativeNeededGlyphs(
        handle: Long,
        z: Int,
        x: Int,
        y: Int,
        data: ByteArray,
        lengths: IntArray,
    ): String

    /** Hands over one fetched range; returns how many glyphs it added. */
    external fun nativeSpriteUrls(
        handle: Long,
        pixelRatio: Int,
    ): String

    external fun nativeNeedsSprite(handle: Long): Int

    external fun nativeNeedsCpu(
        handle: Long,
        z: Int,
        data: ByteArray,
        lengths: IntArray,
    ): Int

    external fun nativeAddSprite(
        handle: Long,
        indexJson: String,
        png: ByteArray,
    ): Int

    external fun nativeAddGlyphs(
        handle: Long,
        pbf: ByteArray,
    ): Int

    /**
     * Draws this tile's labels onto an RGBA buffer, in place. Returns how many
     * were placed.
     */
    external fun nativeDrawLabels(
        handle: Long,
        z: Int,
        x: Int,
        y: Int,
        tileSize: Int,
        rgba: ByteArray,
        data: ByteArray,
        lengths: IntArray,
    ): Int

    /** Draws a transparent label tile into Rust-owned direct memory. */
    external fun nativeRenderLabels(
        handle: Long,
        z: Int,
        x: Int,
        y: Int,
        tileSize: Int,
        data: ByteArray,
        lengths: IntArray,
        placedOut: IntArray,
    ): ByteBuffer?

    external fun nativeFreeLabelPixels(buffer: ByteBuffer)

    /** JSON array of layer `type` values the renderer will not draw. */
    external fun nativeUnsupportedLayerTypes(handle: Long): String

    /** JSON array of the credits the style's sources ask to be shown. */
    external fun nativeAttributions(handle: Long): String

    /** JSON array of reasons the style may not render as intended. */
    external fun nativeDiagnostics(handle: Long): String

    /**
     * Triangulates a tile for the GPU renderer. Returns one packed direct buffer; see
     * [TessellatedTile] for the layout.
     */
    external fun nativeTessellate(
        handle: Long,
        z: Int,
        x: Int,
        y: Int,
        tileSize: Int,
        data: ByteArray,
        lengths: IntArray,
    ): ByteBuffer

    /** Releases the native allocation backing [buffer]. */
    external fun nativeFreeTessellation(buffer: ByteBuffer)
}

/**
 * A tile triangulated by [NativeRenderer.nativeTessellate].
 *
 * One packed native buffer carries everything, so a tile crosses JNI without
 * copying its vertex data into a Java array:
 *
 * ```text
 * [0]              extent
 * [1]              background present (1) or not (0)
 * [2..6]           background r, g, b, a
 * [6]              batch count B
 * [7..13]          timings ms: copy-in, decode, tessellate,
 *                  filter-compile, fill, line
 * [13 .. 13+B*2]   per batch: firstVertex, vertexCount
 * [13+B*2 ..]      vertices: x, y, r, g, b, a
 * ```
 *
 * Colour is per vertex, not per batch: a style layer may paint each feature
 * differently — the MapLibre demo style gives every country its own fill —
 * and one colour per draw call disagreed with the CPU renderer on 37% of a
 * tile.
 */
internal class TessellatedTile(
    private val storage: ByteBuffer,
) : Closeable {
    /** Direct view passed to OpenGL; its storage remains owned by Rust. */
    val packed: FloatBuffer = storage.order(ByteOrder.nativeOrder()).asFloatBuffer()
    private val closed = AtomicBoolean(false)
    val extent: Float = packed[0]
    val background: FloatArray? =
        if (packed[1] != 0f) floatArrayOf(packed[2], packed[3], packed[4], packed[5]) else null
    val batches: List<SolidBatchRenderer.Batch>
    val vertexOffset: Int
    val vertexFloatCount: Int

    /** Milliseconds spent inside the native call, by phase. */
    val decodeMs: Float = packed[7]
    val tessellateMs: Float = packed[8]
    val filterMs: Float = packed[9]
    val fillMs: Float = packed[10]
    val lineMs: Float = packed[11]
    val featureLoopMs: Float = packed[12]

    init {
        val batchCount = packed[6].toInt()
        var cursor = 13
        batches =
            (0 until batchCount).map {
                val batch =
                    SolidBatchRenderer.Batch(
                        vertexOffset = packed[cursor].toInt(),
                        vertexCount = packed[cursor + 1].toInt(),
                    )
                cursor += 2
                batch
            }
        vertexOffset = cursor
        vertexFloatCount = packed.capacity() - cursor
    }

    val triangleCount: Int get() = vertexFloatCount / (SolidBatchRenderer.VERTEX_STRIDE * 3)

    override fun close() {
        if (closed.compareAndSet(false, true)) NativeRenderer.nativeFreeTessellation(storage)
    }
}

/** Rust-owned premultiplied RGBA for a transparent label tile. */
internal class NativeLabelPixels(
    val buffer: ByteBuffer,
    val placed: Int,
) : Closeable {
    private val closed = AtomicBoolean(false)

    override fun close() {
        if (closed.compareAndSet(false, true)) NativeRenderer.nativeFreeLabelPixels(buffer)
    }
}
