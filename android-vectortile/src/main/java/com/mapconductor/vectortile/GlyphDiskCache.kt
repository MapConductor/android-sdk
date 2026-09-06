package com.mapconductor.vectortile

import java.io.File
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicLong
import android.util.Log

/**
 * Keeps glyph ranges on disk so a launch does not start with a mute map.
 *
 * The store the renderer draws from lives in memory, so every process start
 * began with no letters at all, and a tile drawn then has no place names on
 * it. They arrive with the ranges, and a screen of Japanese wants a lot of
 * ranges: CJK is spread thinly across 256-codepoint blocks, so one view of
 * Tokyo asked for 69 of them, at 0.4 to 1.3 seconds each. Measured on a Pixel
 * 5a, the map was up and unlabelled for 6.2 seconds.
 *
 * Ranges themselves never change — a `{fontstack}/{range}.pbf` is the same
 * file today and next month — so they are worth keeping. The second launch
 * reads them back in a few milliseconds and the first tile is drawn with its
 * labels already on it.
 *
 * Unlike [TileDiskCache] this is not content-addressed by anything the caller
 * has to reconstruct: a warm start does not know which URLs it wants until it
 * has drawn a tile, and by then it is too late. Every file is simply read back
 * and handed to the renderer, which takes the fontstack and range from inside
 * the PBF.
 */
internal class GlyphDiskCache(
    private val directory: File,
    private val budgetBytes: Long,
) {
    private val writesSinceSweep = AtomicLong(0)

    companion object {
        private const val TAG = "VectorTileGlyphs"
        private const val SWEEP_EVERY = 16L
        private const val SUFFIX = ".pbf"
    }

    private fun fileFor(url: String): File {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(url.toByteArray()).joinToString("") { "%02x".format(it) }
        return File(directory, "$digest$SUFFIX")
    }

    fun get(url: String): ByteArray? =
        try {
            val file = fileFor(url)
            if (file.isFile) {
                file.setLastModified(System.currentTimeMillis())
                file.readBytes().takeIf { it.isNotEmpty() }
            } else {
                null
            }
        } catch (error: Exception) {
            Log.w(TAG, "read failed for $url", error)
            null
        }

    fun put(
        url: String,
        bytes: ByteArray,
    ) {
        try {
            if (!directory.isDirectory && !directory.mkdirs()) return
            // Write-then-rename, so a warm start never reads a half-written
            // range and decides the font is broken.
            val temp = File.createTempFile("glyph", ".tmp", directory)
            temp.writeBytes(bytes)
            if (!temp.renameTo(fileFor(url))) {
                temp.delete()
                return
            }
            if (writesSinceSweep.incrementAndGet() % SWEEP_EVERY == 0L) sweep()
        } catch (error: Exception) {
            Log.w(TAG, "write failed for $url", error)
        }
    }

    /**
     * Hands every stored range to [load], newest last.
     *
     * Order matters only for what survives a partial read: the ranges touched
     * most recently are the ones this map is most likely to want again.
     */
    fun warm(load: (ByteArray) -> Unit): Int {
        val files =
            try {
                directory.listFiles()?.filter { it.isFile && it.name.endsWith(SUFFIX) }
            } catch (error: Exception) {
                Log.w(TAG, "listing failed", error)
                null
            } ?: return 0

        var loaded = 0
        for (file in files.sortedBy { it.lastModified() }) {
            val bytes =
                runCatching { file.readBytes() }
                    .onFailure { Log.w(TAG, "read failed for ${file.name}", it) }
                    .getOrNull()
            if (bytes == null || bytes.isEmpty()) continue
            runCatching { load(bytes) }
                .onSuccess { loaded++ }
                .onFailure { Log.w(TAG, "range would not parse: ${file.name}", it) }
        }
        return loaded
    }

    /** Drops the least recently used ranges until the budget is met. */
    private fun sweep() {
        try {
            val files = directory.listFiles()?.filter { it.isFile } ?: return
            var total = files.sumOf { it.length() }
            if (total <= budgetBytes) return
            for (file in files.sortedBy { it.lastModified() }) {
                if (total <= budgetBytes) break
                val length = file.length()
                if (file.delete()) total -= length
            }
        } catch (error: Exception) {
            Log.w(TAG, "sweep failed", error)
        }
    }

    fun clear() {
        directory.listFiles()?.forEach { it.delete() }
    }
}
