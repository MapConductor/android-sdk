package com.mapconductor.vectortile

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

    /** Returns an opaque handle; throws IllegalArgumentException on a bad style. */
    external fun nativeNew(styleJson: String): Long

    external fun nativeFree(handle: Long)

    external fun nativeSetStyle(handle: Long, styleJson: String)

    /** JSON array of `{ sourceId, url, z, x, y, scale, offsetX, offsetY }`. */
    external fun nativePlan(handle: Long, z: Int, x: Int, y: Int): String

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
    ): ByteArray

    /** JSON array of layer `type` values the renderer will not draw. */
    external fun nativeUnsupportedLayerTypes(handle: Long): String

    /** JSON array of reasons the style may not render as intended. */
    external fun nativeDiagnostics(handle: Long): String

    /**
     * Triangulates a tile for the GPU renderer. Returns one packed array; see
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
    ): FloatArray

    /**
     * Encodes a straight-alpha RGBA8 **direct** buffer as PNG, in Rust.
     *
     * Zero-copy: the GL readback buffer's pointer crosses JNI as-is. On a
     * Pixel 5a this is 7.6 ms against 47.9 ms for `Bitmap.compress`, which is
     * the difference between the GPU path being worth it and not.
     */
    external fun nativeEncodePng(buffer: java.nio.ByteBuffer, width: Int, height: Int): ByteArray
}

/**
 * A tile triangulated by [NativeRenderer.nativeTessellate].
 *
 * One packed `float[]` carries everything, so a tile crosses JNI in a single
 * call with no object marshalling:
 *
 * ```text
 * [0]              extent
 * [1]              background present (1) or not (0)
 * [2..6]           background r, g, b, a
 * [6]              batch count B
 * [7..13]          timings ms: copy-in, decode, tessellate,
 *                  filter-compile, fill, line
 * [13 .. 13+B*6]   per batch: r, g, b, a, firstVertex, vertexCount
 * [13+B*6 ..]      interleaved x, y vertex positions
 * ```
 */
internal class TessellatedTile(packed: FloatArray) {
    val extent: Float = packed[0]
    val background: FloatArray? =
        if (packed[1] != 0f) floatArrayOf(packed[2], packed[3], packed[4], packed[5]) else null
    val batches: List<SolidBatchRenderer.Batch>
    val vertices: FloatArray

    /** Milliseconds spent inside the native call, by phase. */
    val decodeMs: Float = packed[8]
    val tessellateMs: Float = packed[9]

    init {
        val batchCount = packed[6].toInt()
        var cursor = 13
        batches = (0 until batchCount).map {
            val batch = SolidBatchRenderer.Batch(
                color = floatArrayOf(
                    packed[cursor], packed[cursor + 1], packed[cursor + 2], packed[cursor + 3],
                ),
                vertexOffset = packed[cursor + 4].toInt(),
                vertexCount = packed[cursor + 5].toInt(),
            )
            cursor += 6
            batch
        }
        vertices = packed.copyOfRange(cursor, packed.size)
    }

    val triangleCount: Int get() = vertices.size / 6
}
