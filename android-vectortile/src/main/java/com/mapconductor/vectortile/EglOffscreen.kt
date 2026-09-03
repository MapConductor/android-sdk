package com.mapconductor.vectortile

import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.opengl.GLES20
import android.opengl.GLES30
import java.io.Closeable
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * A headless OpenGL ES context rendering into an off-screen framebuffer.
 *
 * There is no window and no `SurfaceView`: tiles are produced on a worker
 * thread and handed back as bytes, so the context is created against a 1x1
 * pbuffer purely to have something current, and all drawing goes to an FBO
 * whose size is chosen per tile.
 *
 * The earlier `tile_renderer` prototype did this with GLFW, which does not
 * exist on Android — hence EGL directly.
 */
internal class EglOffscreen private constructor(
    private val display: EGLDisplay,
    private val context: EGLContext,
    private val surface: EGLSurface,
    /** 3 when an ES3 context was obtained, otherwise 2. */
    val esVersion: Int,
) : Closeable {

    private var framebuffer = 0
    private var colorTexture = 0
    private var depthBuffer = 0
    private var msaaFramebuffer = 0
    private var msaaColor = 0
    private var width = 0
    private var height = 0
    private var samples = 0

    /** Multisample count actually in use; 0 when unavailable. */
    val activeSamples: Int get() = samples

    companion object {
        /**
         * Creates a context, preferring ES3.
         *
         * ES3 matters for two things here: multisampled renderbuffers with
         * `glBlitFramebuffer`, and instancing. ES2 works but loses MSAA unless
         * the driver exposes `EXT_multisampled_render_to_texture`.
         */
        fun create(preferEs3: Boolean = true): EglOffscreen {
            val display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
            check(display != EGL14.EGL_NO_DISPLAY) { "no EGL display" }

            val version = IntArray(2)
            check(EGL14.eglInitialize(display, version, 0, version, 1)) { "eglInitialize failed" }

            for (clientVersion in if (preferEs3) intArrayOf(3, 2) else intArrayOf(2)) {
                val renderable =
                    if (clientVersion == 3) EGLExt.EGL_OPENGL_ES3_BIT else EGL14.EGL_OPENGL_ES2_BIT
                val config = chooseConfig(display, renderable) ?: continue

                val context = EGL14.eglCreateContext(
                    display,
                    config,
                    EGL14.EGL_NO_CONTEXT,
                    intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, clientVersion, EGL14.EGL_NONE),
                    0,
                )
                if (context == EGL14.EGL_NO_CONTEXT) continue

                // A minimal pbuffer: nothing is drawn to it, but a context
                // needs a current surface before any GL call is legal.
                val surface = EGL14.eglCreatePbufferSurface(
                    display,
                    config,
                    intArrayOf(EGL14.EGL_WIDTH, 1, EGL14.EGL_HEIGHT, 1, EGL14.EGL_NONE),
                    0,
                )
                if (surface == EGL14.EGL_NO_SURFACE) {
                    EGL14.eglDestroyContext(display, context)
                    continue
                }
                check(EGL14.eglMakeCurrent(display, surface, surface, context)) {
                    "eglMakeCurrent failed"
                }
                return EglOffscreen(display, context, surface, clientVersion)
            }
            error("could not create an ES3 or ES2 context")
        }

        private fun chooseConfig(display: EGLDisplay, renderableType: Int): EGLConfig? {
            val attributes = intArrayOf(
                EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT,
                EGL14.EGL_RENDERABLE_TYPE, renderableType,
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_NONE,
            )
            val configs = arrayOfNulls<EGLConfig>(1)
            val count = IntArray(1)
            if (!EGL14.eglChooseConfig(display, attributes, 0, configs, 0, 1, count, 0)) return null
            if (count[0] < 1) return null
            return configs[0]
        }
    }

    /** ES3's `EGL_OPENGL_ES3_BIT_KHR`, which EGL14 does not expose. */
    private object EGLExt {
        const val EGL_OPENGL_ES3_BIT = 0x0040
    }

    /**
     * Prepares an FBO of the given size, reusing it when the size is unchanged.
     *
     * @param requestedSamples MSAA samples to attempt. Vector tiles look bad
     *   without anti-aliasing, and MSAA is the cheap way to get it on a GPU;
     *   it is silently dropped when unsupported.
     */
    fun prepare(size: Int, requestedSamples: Int = 4) {
        if (size == width && size == height && requestedSamples == samples) return
        releaseFramebuffers()

        width = size
        height = size

        val ids = IntArray(1)
        GLES20.glGenTextures(1, ids, 0)
        colorTexture = ids[0]
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, colorTexture)
        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, size, size, 0,
            GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null,
        )
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

        GLES20.glGenFramebuffers(1, ids, 0)
        framebuffer = ids[0]
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebuffer)
        GLES20.glFramebufferTexture2D(
            GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
            GLES20.GL_TEXTURE_2D, colorTexture, 0,
        )
        check(
            GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) ==
                GLES20.GL_FRAMEBUFFER_COMPLETE,
        ) { "resolve framebuffer incomplete" }

        samples = 0
        if (esVersion >= 3 && requestedSamples > 1) {
            val maxSamples = IntArray(1)
            GLES30.glGetIntegerv(GLES30.GL_MAX_SAMPLES, maxSamples, 0)
            val use = minOf(requestedSamples, maxSamples[0])
            if (use > 1) {
                GLES20.glGenRenderbuffers(1, ids, 0)
                msaaColor = ids[0]
                GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, msaaColor)
                GLES30.glRenderbufferStorageMultisample(
                    GLES20.GL_RENDERBUFFER, use, GLES30.GL_RGBA8, size, size,
                )
                GLES20.glGenFramebuffers(1, ids, 0)
                msaaFramebuffer = ids[0]
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, msaaFramebuffer)
                GLES20.glFramebufferRenderbuffer(
                    GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                    GLES20.GL_RENDERBUFFER, msaaColor,
                )
                if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) ==
                    GLES20.GL_FRAMEBUFFER_COMPLETE
                ) {
                    samples = use
                } else {
                    releaseMsaa()
                }
            }
        }
        bindDrawTarget()
    }

    /** Binds whichever framebuffer drawing should go to. */
    fun bindDrawTarget() {
        GLES20.glBindFramebuffer(
            GLES20.GL_FRAMEBUFFER,
            if (samples > 0) msaaFramebuffer else framebuffer,
        )
        GLES20.glViewport(0, 0, width, height)
    }

    /** Resolves MSAA into the readable framebuffer, if MSAA is in use. */
    fun resolve() {
        if (samples == 0) return
        GLES30.glBindFramebuffer(GLES30.GL_READ_FRAMEBUFFER, msaaFramebuffer)
        GLES30.glBindFramebuffer(GLES30.GL_DRAW_FRAMEBUFFER, framebuffer)
        GLES30.glBlitFramebuffer(
            0, 0, width, height, 0, 0, width, height,
            GLES20.GL_COLOR_BUFFER_BIT, GLES20.GL_NEAREST,
        )
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebuffer)
    }

    /**
     * Reads the framebuffer back to the CPU.
     *
     * This is the step that decides whether GPU rasterisation is worth it at
     * all: it stalls the pipeline, and on some drivers it is slow enough to
     * erase any gain from drawing faster. Hence it is measured separately.
     */
    fun readPixels(into: ByteBuffer? = null): ByteBuffer {
        resolve()
        val buffer = into ?: ByteBuffer
            .allocateDirect(width * height * 4)
            .order(ByteOrder.nativeOrder())
        buffer.rewind()
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebuffer)
        GLES20.glReadPixels(
            0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer,
        )
        buffer.rewind()
        return buffer
    }

    /** Blocks until every issued command has completed. */
    fun finish() = GLES20.glFinish()

    private fun releaseMsaa() {
        if (msaaFramebuffer != 0) {
            GLES20.glDeleteFramebuffers(1, intArrayOf(msaaFramebuffer), 0)
            msaaFramebuffer = 0
        }
        if (msaaColor != 0) {
            GLES20.glDeleteRenderbuffers(1, intArrayOf(msaaColor), 0)
            msaaColor = 0
        }
        samples = 0
    }

    private fun releaseFramebuffers() {
        releaseMsaa()
        if (framebuffer != 0) {
            GLES20.glDeleteFramebuffers(1, intArrayOf(framebuffer), 0)
            framebuffer = 0
        }
        if (colorTexture != 0) {
            GLES20.glDeleteTextures(1, intArrayOf(colorTexture), 0)
            colorTexture = 0
        }
        if (depthBuffer != 0) {
            GLES20.glDeleteRenderbuffers(1, intArrayOf(depthBuffer), 0)
            depthBuffer = 0
        }
        width = 0
        height = 0
    }

    override fun close() {
        releaseFramebuffers()
        EGL14.eglMakeCurrent(
            display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT,
        )
        EGL14.eglDestroySurface(display, surface)
        EGL14.eglDestroyContext(display, context)
        EGL14.eglTerminate(display)
    }
}
