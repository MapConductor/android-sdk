package com.mapconductor.kml

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.zip.CRC32
import java.util.zip.Deflater

object FastPngEncoder {
    private val signature = byteArrayOf(-119, 80, 78, 71, 13, 10, 26, 10)

    fun encode(
        width: Int,
        height: Int,
        rgba: ByteArray,
    ): ByteArray {
        require(width > 0) { "width must be positive" }
        require(height > 0) { "height must be positive" }
        require(rgba.size == width * height * 4) { "rgba must contain width * height * 4 bytes" }

        val rowStride = width * 4
        val scanlines = ByteArray((rowStride + 1) * height)
        var src = 0
        var dst = 0
        repeat(height) {
            scanlines[dst++] = 0
            System.arraycopy(rgba, src, scanlines, dst, rowStride)
            src += rowStride
            dst += rowStride
        }

        val deflater = Deflater(Deflater.BEST_SPEED)
        val compressed = ByteArrayOutputStream(scanlines.size / 2)
        try {
            deflater.setInput(scanlines)
            deflater.finish()
            val buffer = ByteArray(16 * 1024)
            while (!deflater.finished()) {
                val count = deflater.deflate(buffer)
                compressed.write(buffer, 0, count)
            }
        } finally {
            deflater.end()
        }

        val out = ByteArrayOutputStream(signature.size + compressed.size() + 64)
        out.write(signature)
        writeChunk(out, "IHDR", ihdr(width, height))
        writeChunk(out, "IDAT", compressed.toByteArray())
        writeChunk(out, "IEND", ByteArray(0))
        return out.toByteArray()
    }

    private fun ihdr(
        width: Int,
        height: Int,
    ): ByteArray =
        ByteBuffer
            .allocate(13)
            .putInt(width)
            .putInt(height)
            .put(8)
            .put(6)
            .put(0)
            .put(0)
            .put(0)
            .array()

    private fun writeChunk(
        out: ByteArrayOutputStream,
        type: String,
        data: ByteArray,
    ) {
        val typeBytes = type.encodeToByteArray()
        out.writeInt(data.size)
        out.write(typeBytes)
        out.write(data)

        val crc = CRC32()
        crc.update(typeBytes)
        crc.update(data)
        out.writeInt(crc.value.toInt())
    }

    private fun ByteArrayOutputStream.writeInt(value: Int) {
        write((value ushr 24) and 0xff)
        write((value ushr 16) and 0xff)
        write((value ushr 8) and 0xff)
        write(value and 0xff)
    }
}
