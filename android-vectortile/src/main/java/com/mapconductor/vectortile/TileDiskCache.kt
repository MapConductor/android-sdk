package com.mapconductor.vectortile

import java.io.File
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicLong
import android.util.Log

/**
 * Stores rendered PNG tiles on disk, keyed by style and coordinates.
 *
 * This does not make the first view of an area faster — nothing can, the tiles
 * have to be fetched and rasterised once. What it removes is paying that cost
 * *again* on the next app launch. Within a single session the map SDK already
 * caches raster tiles itself (measured: 9 renders for 7 distinct tiles across a
 * pan away and back), so this is specifically about surviving process death.
 *
 * Entries are content-addressed, so a restyle simply misses rather than needing
 * explicit invalidation.
 */
internal class TileDiskCache(
    private val directory: File,
    private val budgetBytes: Long,
) {
    private val sizeBytes = AtomicLong(-1)

    /** Writes since the last sweep; eviction is amortised rather than per-write. */
    private val writesSinceSweep = AtomicLong(0)

    companion object {
        private const val TAG = "VectorTileCache"
        private const val SWEEP_EVERY = 32L

        /** Stable short digest of arbitrary text, for use in a file name. */
        fun digest(vararg parts: String): String {
            val md = MessageDigest.getInstance("SHA-256")
            parts.forEach { md.update(it.toByteArray()) }
            return md.digest().joinToString("") { "%02x".format(it) }.take(32)
        }
    }

    private fun fileFor(key: String) = File(directory, "$key.png")

    fun get(key: String): ByteArray? {
        val file = fileFor(key)
        return try {
            if (!file.isFile) return null
            val bytes = file.readBytes()
            if (bytes.isEmpty()) return null
            // Touch so the sweep below evicts genuinely cold entries.
            file.setLastModified(System.currentTimeMillis())
            bytes
        } catch (error: Exception) {
            Log.w(TAG, "read failed for $key", error)
            null
        }
    }

    fun put(
        key: String,
        bytes: ByteArray,
    ) {
        try {
            if (!directory.isDirectory && !directory.mkdirs()) return
            // Write-then-rename: two threads rendering the same tile must not
            // leave a half-written file behind for a third to read.
            val temp = File.createTempFile("tile", ".tmp", directory)
            temp.writeBytes(bytes)
            if (!temp.renameTo(fileFor(key))) {
                temp.delete()
                return
            }
            sizeBytes.addAndGet(bytes.size.toLong())
            if (writesSinceSweep.incrementAndGet() % SWEEP_EVERY == 0L) sweep()
        } catch (error: Exception) {
            Log.w(TAG, "write failed for $key", error)
        }
    }

    /** Drops the least recently used files until the budget is met. */
    private fun sweep() {
        try {
            val files = directory.listFiles()?.filter { it.isFile } ?: return
            var total = files.sumOf { it.length() }
            sizeBytes.set(total)
            if (total <= budgetBytes) return

            for (file in files.sortedBy { it.lastModified() }) {
                if (total <= budgetBytes) break
                val length = file.length()
                if (file.delete()) total -= length
            }
            sizeBytes.set(total)
        } catch (error: Exception) {
            Log.w(TAG, "sweep failed", error)
        }
    }

    fun clear() {
        directory.listFiles()?.forEach { it.delete() }
        sizeBytes.set(0)
    }
}
