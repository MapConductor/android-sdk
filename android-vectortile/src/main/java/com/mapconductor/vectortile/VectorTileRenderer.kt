package com.mapconductor.vectortile

import org.json.JSONArray
import java.io.Closeable
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

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

    /**
     * Keeps [close] from freeing the renderer while a native call is inside it.
     *
     * Tiles are drawn on the tile server's worker threads while the thread
     * that owns the layer decides to drop it -- a provider switch, a page
     * leaving, a style change. Freeing the handle then leaves those workers
     * holding a pointer into freed memory, and whatever is allocated next
     * writes over it: the process died inside `nativeRender` with a fault
     * address made of style-layer name bytes, which is what the freed block
     * held by the time it was read.
     *
     * Calls take the read side, so tiles still render in parallel. Only
     * [close] takes the write side, and that is what makes it wait for the
     * calls already inside the library -- bounded by one tile, since nothing
     * here loops. A call that arrives after the free finds the handle cleared
     * and throws instead of reading freed memory.
     */
    private val liveCalls = ReentrantReadWriteLock()

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
         * 14: drawn for the screen size the host shows a tile at, and read
         *     from the source level that size belongs to.
         * 15: the whole background stack, not its last layer.
         * 16: magnified geometry lands inside its tile on the GPU path.
         * 17: labels judged against a tile of margin, so neighbours agree.
         * 18: glyph pixels align across tile boundaries.
         * 19: label bounds include glyph bearings and SDF raster extents.
         */
        const val OUTPUT_VERSION: Int = 19

        /** @throws IllegalArgumentException if the style cannot be parsed. */
        @JvmStatic
        fun create(
            styleJson: String,
            displayTileSize: Int,
        ): VectorTileRenderer = VectorTileRenderer(NativeRenderer.nativeNew(styleJson, displayTileSize))
    }

    /**
     * Runs one native call with the renderer held alive for its duration.
     *
     * The handle is read inside the lock. Reading it outside would race
     * [close] again, which is the bug this exists to stop.
     */
    private inline fun <T> withRenderer(block: (Long) -> T): T =
        liveCalls.read {
            val value = handle.get()
            check(value != 0L) { "VectorTileRenderer has been closed" }
            block(value)
        }

    /**
     * Source tiles needed to draw `z/x/y`, as JSON. Fetch them in order and
     * pass the bytes to [render] positionally.
     */
    fun plan(
        z: Int,
        x: Int,
        y: Int,
    ): String = withRenderer { handle -> NativeRenderer.nativePlan(handle, z, x, y) }

    /** The style's `glyphs` URL template, or null when it has none. */
    fun glyphsUrlTemplate(): String? =
        withRenderer { handle -> NativeRenderer.nativeGlyphsUrlTemplate(handle) }.takeIf { it.isNotEmpty() }

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
        val urls = JSONArray(withRenderer { handle -> NativeRenderer.nativeNeededGlyphs(handle, z, x, y, data, lengths) })
        return (0 until urls.length()).map { urls.getString(it) }
    }

    /**
     * Hands one fetched range to the renderer, which keeps it for every later
     * tile. Returns how many glyphs it gained.
     */
    fun addGlyphs(pbf: ByteArray): Int = withRenderer { handle -> NativeRenderer.nativeAddGlyphs(handle, pbf) }

    /**
     * The style's sprite pair — index then image — or an empty list when the
     * style names no sprite.
     *
     * Unlike glyphs these are known from the style alone, so they can be
     * fetched before a single tile has been drawn.
     */
    fun spriteUrls(pixelRatio: Int = 1): List<String> {
        val urls = JSONArray(withRenderer { handle -> NativeRenderer.nativeSpriteUrls(handle, pixelRatio) })
        return (0 until urls.length()).map { urls.getString(it) }
    }

    /**
     * Whether a tile at this zoom has to be rasterised on the CPU.
     *
     * The GPU path draws flat-coloured triangles; a patterned fill needs an
     * image repeated across the polygon. Asked of the tile rather than of the
     * style, because a style that paints prisons with a hatch says nothing
     * about whether this tile has a prison in it.
     */
    fun needsCpu(
        z: Int,
        tiles: List<ByteArray?>,
    ): Boolean {
        val (data, lengths) = pack(tiles)
        return withRenderer { handle -> NativeRenderer.nativeNeedsCpu(handle, z, data, lengths) } != 0
    }

    /** Whether the style names a sprite the renderer has not been given. */
    fun needsSprite(): Boolean = withRenderer { handle -> NativeRenderer.nativeNeedsSprite(handle) } != 0

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
    ): Int = withRenderer { handle -> NativeRenderer.nativeAddSprite(handle, indexJson, png) }

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
        return withRenderer { handle -> NativeRenderer.nativeDrawLabels(handle, z, x, y, tileSize, rgba, data, lengths) }
    }

    /** Draws a standalone transparent label tile without an RGBA JNI copy. */
    internal fun renderLabels(
        z: Int,
        x: Int,
        y: Int,
        tileSize: Int,
        tiles: List<ByteArray?>,
    ): NativeLabelPixels? {
        val (data, lengths) = pack(tiles)
        val placed = IntArray(1)
        val pixels =
            withRenderer { handle -> NativeRenderer.nativeRenderLabels(
                handle, z, x, y, tileSize, data, lengths, placed,
            ) } ?: return null
        return NativeLabelPixels(pixels, placed[0])
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
        geometryOnly: Boolean = false,
    ): ByteArray {
        val (data, lengths) = pack(tiles)
        return withRenderer { handle -> NativeRenderer.nativeRender(
            handle,
            z,
            x,
            y,
            tileSize,
            data,
            lengths,
            if (geometryOnly) 1 else 0,
        ) }
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
        withRenderer { handle -> NativeRenderer.nativeSetStyle(handle, styleJson) }
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
    ): ByteBuffer = withRenderer { handle -> NativeRenderer.nativeTessellate(handle, z, x, y, tileSize, data, lengths) }

    /** JSON array of layer `type` values in this style that will not be drawn. */
    fun unsupportedLayerTypes(): String = withRenderer { handle -> NativeRenderer.nativeUnsupportedLayerTypes(handle) }

    /**
     * JSON array of the credits this style's sources ask to be shown.
     *
     * The host must display these: a style is data under someone's licence,
     * and the sample basemap draws OpenStreetMap, whose licence requires the
     * credit. May contain HTML — the text is normally a link to the licence.
     */
    fun attributions(): String = withRenderer { handle -> NativeRenderer.nativeAttributions(handle) }

    /**
     * Reasons the current style may not render as intended, as a JSON array of
     * strings: unsupported layer types, sources that cannot be fetched, layers
     * pointing at undefined sources, Mapbox `imports`.
     *
     * Worth surfacing — the failure mode that matters is a blank tile, and a
     * style this renderer cannot use should say so.
     */
    fun diagnostics(): String = withRenderer { handle -> NativeRenderer.nativeDiagnostics(handle) }

    /** Releases the native renderer. Safe to call more than once. */
    override fun close() {
        liveCalls.write {
            // getAndSet keeps a double close from freeing the same pointer twice.
            val value = handle.getAndSet(0L)
            if (value != 0L) NativeRenderer.nativeFree(value)
        }
    }
}
