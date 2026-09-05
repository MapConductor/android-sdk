package com.mapconductor.vectortile

import java.io.Closeable
import java.util.concurrent.atomic.AtomicLong

/**
 * Renders MapLibre vector styles to raster PNG tiles.
 *
 * Rendering is split in two so that **no network I/O happens inside the native
 * library**: [plan] says which source tiles are needed, the caller fetches them
 * with its own HTTP stack — keeping auth header injection, certificate pinning
 * and OkHttp's cache — and passes the bytes to [render].
 *
 * Instances hold a native allocation; call [close] when done.
 */
class VectorTileRenderer private constructor(
    handle: Long,
) : Closeable {
    private val handle = AtomicLong(handle)

    companion object {
        const val DEFAULT_TILE_SIZE: Int = 512

        /** @throws IllegalArgumentException if the style cannot be parsed. */
        @JvmStatic
        fun create(styleJson: String): VectorTileRenderer = VectorTileRenderer(NativeRenderer.nativeNew(styleJson))
    }

    private fun requireHandle(): Long {
        val value = handle.get()
        check(value != 0L) { "VectorTileRenderer has been closed" }
        return value
    }

    /**
     * Source tiles needed to draw `z/x/y`, as JSON. Fetch them in order and
     * pass the bytes to [render] positionally.
     */
    fun plan(
        z: Int,
        x: Int,
        y: Int,
    ): String = NativeRenderer.nativePlan(requireHandle(), z, x, y)

    /**
     * Rasterises `z/x/y` to PNG bytes.
     *
     * @param tiles one entry per [plan] request, in the same order; null where
     *   the fetch 404'd or came back empty.
     */
    fun render(
        z: Int,
        x: Int,
        y: Int,
        tileSize: Int = DEFAULT_TILE_SIZE,
        tiles: List<ByteArray?>,
    ): ByteArray {
        // The native side takes one concatenated buffer plus a length table,
        // which avoids marshalling an array-of-arrays across JNI.
        val lengths = IntArray(tiles.size) { tiles[it]?.size ?: 0 }
        val data = ByteArray(lengths.sum())
        var offset = 0
        for (tile in tiles) {
            if (tile == null) continue
            tile.copyInto(data, offset)
            offset += tile.size
        }
        return NativeRenderer.nativeRender(requireHandle(), z, x, y, tileSize, data, lengths)
    }

    /**
     * Replaces the style. Fetched vector tiles stay valid — the geometry is
     * unchanged, only the paint applied to it — so recolouring needs no refetch.
     */
    fun setStyle(styleJson: String) {
        NativeRenderer.nativeSetStyle(requireHandle(), styleJson)
    }

    /**
     * Triangulates `z/x/y` for the GPU renderer. See `TessellatedTile` for the
     * packed layout.
     */
    internal fun tessellate(
        z: Int,
        x: Int,
        y: Int,
        tileSize: Int,
        data: ByteArray,
        lengths: IntArray,
    ): FloatArray = NativeRenderer.nativeTessellate(requireHandle(), z, x, y, tileSize, data, lengths)

    /** JSON array of layer `type` values in this style that will not be drawn. */
    fun unsupportedLayerTypes(): String = NativeRenderer.nativeUnsupportedLayerTypes(requireHandle())

    /**
     * Reasons the current style may not render as intended, as a JSON array of
     * strings: unsupported layer types, sources that cannot be fetched, layers
     * pointing at undefined sources, Mapbox `imports`.
     *
     * Worth surfacing — the failure mode that matters is a blank tile, and a
     * style this renderer cannot use should say so.
     */
    fun diagnostics(): String = NativeRenderer.nativeDiagnostics(requireHandle())

    /** Releases the native renderer. Safe to call more than once. */
    override fun close() {
        // getAndSet keeps a double close from freeing the same pointer twice.
        val value = handle.getAndSet(0L)
        if (value != 0L) NativeRenderer.nativeFree(value)
    }
}
