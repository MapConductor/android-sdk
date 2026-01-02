package com.mapconductor.tileserver

import fi.iki.elonen.NanoHTTPD
import java.io.ByteArrayInputStream
import java.net.ServerSocket
import java.util.concurrent.ConcurrentHashMap

class LocalTileServer private constructor(
    port: Int,
) : NanoHTTPD(port) {
    private val providers = ConcurrentHashMap<String, TileProvider>()

    val baseUrl: String = "http://127.0.0.1:$port"

    fun register(
        routeId: String,
        provider: TileProvider,
    ) {
        providers[routeId] = provider
    }

    fun unregister(routeId: String) {
        providers.remove(routeId)
    }

    fun urlTemplate(
        routeId: String,
        version: Long,
    ): String = "$baseUrl/tiles/$routeId/$version/{z}/{x}/{y}.png"

    override fun serve(session: IHTTPSession): Response {
        if (session.method != Method.GET) {
            return newFixedLengthResponse(Response.Status.METHOD_NOT_ALLOWED, MIME_PLAINTEXT, "Method not allowed")
        }
        val path = session.uri.trim('/')
        if (path.isEmpty()) {
            return notFound()
        }
        val segments = path.split("/").filter { it.isNotEmpty() }
        if (segments.size < 6 || segments[0] != "tiles") {
            return notFound()
        }
        val routeId = segments[1]
        val z = segments[3].toIntOrNull() ?: return notFound()
        val x = segments[4].toIntOrNull() ?: return notFound()
        val y = segments[5].substringBefore('.').toIntOrNull() ?: return notFound()

        val provider = providers[routeId] ?: return notFound()
        val bytes = provider.renderTile(TileRequest(x = x, y = y, z = z)) ?: return notFound()
        val response =
            newFixedLengthResponse(
                Response.Status.OK,
                "image/png",
                ByteArrayInputStream(bytes),
                bytes.size.toLong(),
            )
        response.addHeader("Cache-Control", "no-store")
        return response
    }

    private fun notFound(): Response = newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not found")

    companion object {
        fun startServer(): LocalTileServer {
            val port = findAvailablePort()
            val server = LocalTileServer(port)
            server.start(SOCKET_READ_TIMEOUT, false)
            return server
        }

        private fun findAvailablePort(): Int =
            ServerSocket(0).use { socket ->
                socket.localPort
            }
    }
}
