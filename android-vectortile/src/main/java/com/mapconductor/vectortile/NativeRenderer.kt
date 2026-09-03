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
}
