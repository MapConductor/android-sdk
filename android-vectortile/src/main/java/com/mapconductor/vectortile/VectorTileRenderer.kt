package com.mapconductor.vectortile

import org.json.JSONArray
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

        /**
         * Bumped whenever the renderer's output changes for the same style.
         *
         * Callers key their tile caches on this. Without it, a build that
         * started drawing labels kept serving the unlabelled PNGs the previous
         * one had cached — the tiles were correct for the renderer that made
         * them and wrong for the one asking.
         *
         * 1: fills, lines, circles.
         * 2: labels.
         */
        const val OUTPUT_VERSION: Int = 4

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

    /** The style's `glyphs` URL template, or null when it has none. */
    fun glyphsUrlTemplate(): String? =
        NativeRenderer.nativeGlyphsUrlTemplate(requireHandle()).takeIf { it.isNotEmpty() }

    /**
     * Glyph URLs this tile's labels need and the renderer does not hold.
     *
     * Asked after the source tiles are in, because which ranges a tile needs
     * depends on the text in it — a CJK font has 82 ranges where a tile of
     * Tokyo uses four.
     */
    fun neededGlyphs(
        z: Int,
        x: Int,
        y: Int,
        tiles: List<ByteArray?>,
    ): List<String> {
        val (data, lengths) = pack(tiles)
        val urls = JSONArray(NativeRenderer.nativeNeededGlyphs(requireHandle(), z, x, y, data, lengths))
        return (0 until urls.length()).map { urls.getString(it) }
    }

    /**
     * Hands one fetched range to the renderer, which keeps it for every later
     * tile. Returns how many glyphs it gained.
     */
    fun addGlyphs(pbf: ByteArray): Int = NativeRenderer.nativeAddGlyphs(requireHandle(), pbf)

    /**
     * The style's sprite pair — index then image — or an empty list when the
     * style names no sprite.
     *
     * Unlike glyphs these are known from the style alone, so they can be
     * fetched before a single tile has been drawn.
     */
    fun spriteUrls(pixelRatio: Int = 1): List<String> {
        val urls = JSONArray(NativeRenderer.nativeSpriteUrls(requireHandle(), pixelRatio))
        return (0 until urls.length()).map { urls.getString(it) }
    }

    /** Whether the style names a sprite the renderer has not been given. */
    fun needsSprite(): Boolean = NativeRenderer.nativeNeedsSprite(requireHandle()) != 0

    /**
     * Hands over the fetched pair, and returns how many icons the sheet holds.
     *
     * One sheet covers a whole style, so this replaces rather than
     * accumulates — a restyle brings its own.
     *
     * @throws IllegalArgumentException if the pair will not parse
     */
    fun addSprite(
        indexJson: String,
        png: ByteArray,
    ): Int = NativeRenderer.nativeAddSprite(requireHandle(), indexJson, png)

    /**
     * Draws this tile's labels onto pixels something else rasterised.
     *
     * The GPU path tessellates fills and lines and knows nothing about glyphs.
     * The label pass does not care how the pixels underneath were made, so it
     * runs over the readback rather than forcing the whole tile onto the CPU.
     *
     * @param rgba `tileSize * tileSize * 4` bytes, modified in place
     * @return how many labels were placed
     */
    fun drawLabels(
        z: Int,
        x: Int,
        y: Int,
        tileSize: Int,
        rgba: ByteArray,
        tiles: List<ByteArray?>,
    ): Int {
        val (data, lengths) = pack(tiles)
        return NativeRenderer.nativeDrawLabels(requireHandle(), z, x, y, tileSize, rgba, data, lengths)
    }

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
        val (data, lengths) = pack(tiles)
        return NativeRenderer.nativeRender(requireHandle(), z, x, y, tileSize, data, lengths)
    }

    /**
     * One concatenated buffer plus a length table, which is what the native
     * side takes: an array-of-arrays would be marshalled element by element
     * across JNI.
     */
    private fun pack(tiles: List<ByteArray?>): Pair<ByteArray, IntArray> {
        val lengths = IntArray(tiles.size) { tiles[it]?.size ?: 0 }
        val data = ByteArray(lengths.sum())
        var offset = 0
        for (tile in tiles) {
            if (tile == null) continue
            tile.copyInto(data, offset)
            offset += tile.size
        }
        return data to lengths
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
