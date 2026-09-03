package com.mapconductor.vectortile

import android.opengl.GLES20
import java.io.Closeable
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * Draws batches of solid-coloured triangles in tile-local coordinates.
 *
 * This is the shape a vector tile actually reduces to on a GPU: every fill and
 * every line becomes triangles, grouped by paint colour, one draw call per
 * style layer. Anti-aliasing comes from MSAA on the framebuffer rather than
 * from per-vertex coverage, which keeps the shader trivial and is enough to
 * establish whether the GPU path is worth pursuing.
 */
internal class SolidBatchRenderer : Closeable {

    private var program = 0
    private var positionAttribute = 0
    private var colorUniform = 0
    private var extentUniform = 0
    private var vertexBuffer = 0

    private companion object {
        // Positions arrive in tile units (0..extent) and are mapped to clip
        // space here, so the CPU never has to rescale the geometry.
        //
        // Tile y grows downward, and the natural mapping would put y=0 at the
        // top of clip space. It does not, deliberately: `glReadPixels` returns
        // rows bottom-up while `Bitmap.copyPixelsFromBuffer` reads them
        // top-down, so rendering upside down here is what makes the readback
        // come out the right way up — without an extra CPU flip per tile.
        const val VERTEX_SHADER = """
            attribute vec2 aPosition;
            uniform float uExtent;
            void main() {
                vec2 unit = aPosition / uExtent;
                gl_Position = vec4(unit.x * 2.0 - 1.0, unit.y * 2.0 - 1.0, 0.0, 1.0);
            }
        """

        const val FRAGMENT_SHADER = """
            precision mediump float;
            uniform vec4 uColor;
            void main() { gl_FragColor = uColor; }
        """
    }

    /** One draw call: a colour and a run of triangle vertices. */
    data class Batch(val color: FloatArray, val vertexOffset: Int, val vertexCount: Int)

    fun initialise() {
        program = link(VERTEX_SHADER, FRAGMENT_SHADER)
        positionAttribute = GLES20.glGetAttribLocation(program, "aPosition")
        colorUniform = GLES20.glGetUniformLocation(program, "uColor")
        extentUniform = GLES20.glGetUniformLocation(program, "uExtent")

        val ids = IntArray(1)
        GLES20.glGenBuffers(1, ids, 0)
        vertexBuffer = ids[0]
    }

    /** Uploads all geometry for a tile in one go. */
    fun upload(vertices: FloatArray) {
        val buffer: FloatBuffer = ByteBuffer
            .allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        buffer.put(vertices).rewind()

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffer)
        GLES20.glBufferData(
            GLES20.GL_ARRAY_BUFFER, vertices.size * 4, buffer, GLES20.GL_STATIC_DRAW,
        )
    }

    fun clear(r: Float, g: Float, b: Float, a: Float) {
        GLES20.glClearColor(r, g, b, a)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
    }

    fun draw(batches: List<Batch>, extent: Float) {
        GLES20.glUseProgram(program)
        GLES20.glUniform1f(extentUniform, extent)

        GLES20.glEnable(GLES20.GL_BLEND)
        // Separate blend for the alpha channel. Plain glBlendFunc applies
        // SRC_ALPHA to alpha too, so drawing a 0.10-alpha layer DROPS the
        // stored alpha to 0.91 — and Bitmap, which treats its pixels as
        // premultiplied, then un-premultiplies on read: rgb / 0.91, clamped.
        // Stack a few translucent land layers and whole regions blow out to
        // white. With ONE / ONE_MINUS_SRC_ALPHA the destination alpha stays
        // saturated at 1 and the readback is exact.
        GLES20.glBlendFuncSeparate(
            GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA,
            GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA,
        )

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBuffer)
        GLES20.glEnableVertexAttribArray(positionAttribute)
        GLES20.glVertexAttribPointer(positionAttribute, 2, GLES20.GL_FLOAT, false, 8, 0)

        for (batch in batches) {
            GLES20.glUniform4fv(colorUniform, 1, batch.color, 0)
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, batch.vertexOffset, batch.vertexCount)
        }

        GLES20.glDisableVertexAttribArray(positionAttribute)
    }

    private fun link(vertexSource: String, fragmentSource: String): Int {
        val vertex = compile(GLES20.GL_VERTEX_SHADER, vertexSource)
        val fragment = compile(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
        val id = GLES20.glCreateProgram()
        GLES20.glAttachShader(id, vertex)
        GLES20.glAttachShader(id, fragment)
        GLES20.glLinkProgram(id)

        val status = IntArray(1)
        GLES20.glGetProgramiv(id, GLES20.GL_LINK_STATUS, status, 0)
        check(status[0] != 0) { "link failed: ${GLES20.glGetProgramInfoLog(id)}" }

        GLES20.glDeleteShader(vertex)
        GLES20.glDeleteShader(fragment)
        return id
    }

    private fun compile(type: Int, source: String): Int {
        val id = GLES20.glCreateShader(type)
        GLES20.glShaderSource(id, source)
        GLES20.glCompileShader(id)
        val status = IntArray(1)
        GLES20.glGetShaderiv(id, GLES20.GL_COMPILE_STATUS, status, 0)
        check(status[0] != 0) { "compile failed: ${GLES20.glGetShaderInfoLog(id)}" }
        return id
    }

    override fun close() {
        if (vertexBuffer != 0) {
            GLES20.glDeleteBuffers(1, intArrayOf(vertexBuffer), 0)
            vertexBuffer = 0
        }
        if (program != 0) {
            GLES20.glDeleteProgram(program)
            program = 0
        }
    }
}
