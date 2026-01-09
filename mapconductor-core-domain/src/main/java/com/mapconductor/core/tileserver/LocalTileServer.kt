package com.mapconductor.core.tileserver

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

class LocalTileServer private constructor(
    private val serverSocket: ServerSocket,
) {
    private val providers = ConcurrentHashMap<String, TileProviderInterface>()
    private val running = AtomicBoolean(false)
    private val acceptThread = Thread { acceptLoop() }

    val baseUrl: String = "http://127.0.0.1:${serverSocket.localPort}"

    fun register(
        routeId: String,
        provider: TileProviderInterface,
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

    fun start() {
        if (running.compareAndSet(false, true)) {
            acceptThread.isDaemon = true
            acceptThread.start()
        }
    }

    fun stop() {
        if (running.compareAndSet(true, false)) {
            serverSocket.close()
        }
    }

    private fun acceptLoop() {
        while (running.get()) {
            val socket =
                try {
                    serverSocket.accept()
                } catch (_: Exception) {
                    if (running.get()) {
                        continue
                    }
                    return
                }
            Thread { handleClient(socket) }.apply { isDaemon = true }.start()
        }
    }

    private fun handleClient(socket: Socket) {
        socket.use { client ->
            try {
                client.soTimeout = 5000
                val reader = BufferedReader(InputStreamReader(client.getInputStream()))
                var handled = 0
                while (handled < MAX_KEEP_ALIVE_REQUESTS) {
                    val request = readRequest(reader) ?: break
                    if (!request.valid) {
                        writeResponse(client, "400 Bad Request", "text/plain", "Bad request".toByteArray())
                        break
                    }

                    val method = request.method
                    val path = request.path.substringBefore('?').trim('/')
                    val keepAlive = shouldKeepAlive(request)

                    if (method != "GET") {
                        writeResponse(
                            client,
                            "405 Method Not Allowed",
                            "text/plain",
                            "Method not allowed".toByteArray(),
                            keepAlive = false,
                            extraHeaders = mapOf("Allow" to "GET", "Cache-Control" to "no-store"),
                        )
                        break
                    }

                    val tileResponse = resolveTile(path)
                    if (tileResponse == null) {
                        writeResponse(
                            client,
                            "404 Not Found",
                            "text/plain",
                            "Not found".toByteArray(),
                            keepAlive = keepAlive,
                            extraHeaders = mapOf("Cache-Control" to "no-store"),
                        )
                    } else {
                        writeResponse(
                            client,
                            "200 OK",
                            "image/png",
                            tileResponse.body,
                            keepAlive = keepAlive,
                            extraHeaders = mapOf("Cache-Control" to tileResponse.cacheControl),
                        )
                    }

                    handled += 1
                    if (!keepAlive) {
                        break
                    }
                }
            } catch (_: Exception) {
                // Ignore per-connection errors to avoid crashing the server.
            }
        }
    }

    private fun readRequest(reader: BufferedReader): Request? {
        var requestLine: String? = null
        while (requestLine == null) {
            val line = reader.readLine() ?: return null
            if (line.isNotEmpty()) {
                requestLine = line
            }
        }

        val parts = requestLine.split(" ")
        val valid = parts.size >= 2
        val method = parts.getOrNull(0) ?: ""
        val path = parts.getOrNull(1) ?: ""
        val httpVersion = parts.getOrNull(2) ?: "HTTP/1.0"

        val headers = HashMap<String, String>()
        while (true) {
            val line = reader.readLine() ?: break
            if (line.isEmpty()) {
                break
            }
            val index = line.indexOf(':')
            if (index <= 0) continue
            val key = line.substring(0, index).trim().lowercase()
            val value = line.substring(index + 1).trim()
            headers[key] = value
        }

        return Request(
            method = method,
            path = path,
            httpVersion = httpVersion,
            headers = headers,
            valid = valid,
        )
    }

    private fun shouldKeepAlive(request: Request): Boolean {
        val connection = request.headers["connection"]?.lowercase()
        return when (request.httpVersion) {
            "HTTP/1.1" -> connection != "close"
            "HTTP/1.0" -> connection == "keep-alive"
            else -> false
        }
    }

    private fun resolveTile(path: String): TileResponse? {
        if (path.isEmpty()) {
            return null
        }
        val segments = path.split("/").filter { it.isNotEmpty() }
        if (segments.size < 6 || segments[0] != "tiles") {
            return null
        }
        val routeId = segments[1]
        val version = segments[2].toLongOrNull()
        val z = segments[3].toIntOrNull() ?: return null
        val x = segments[4].toIntOrNull() ?: return null
        val y = segments[5].substringBefore('.').toIntOrNull() ?: return null

        val provider = providers[routeId] ?: return null
        val bytes = provider.renderTile(TileRequest(x = x, y = y, z = z)) ?: return null
        val cacheControl = if (version != null) LONG_CACHE_CONTROL else "no-store"
        return TileResponse(bytes, cacheControl)
    }

    private fun writeResponse(
        client: Socket,
        status: String,
        contentType: String,
        body: ByteArray,
        keepAlive: Boolean = false,
        extraHeaders: Map<String, String> = emptyMap(),
    ) {
        try {
            val output = client.getOutputStream()
            val headers = StringBuilder()
            headers.append("HTTP/1.1 ").append(status).append("\r\n")
            headers.append("Content-Type: ").append(contentType).append("\r\n")
            headers.append("Content-Length: ").append(body.size).append("\r\n")
            headers.append("Connection: ").append(if (keepAlive) "keep-alive" else "close").append("\r\n")
            for ((key, value) in extraHeaders) {
                headers
                    .append(key)
                    .append(": ")
                    .append(value)
                    .append("\r\n")
            }
            headers.append("\r\n")
            output.write(headers.toString().toByteArray())
            output.write(body)
            output.flush()
        } catch (_: Exception) {
            // Client closed the connection early; ignore.
        }
    }

    private data class Request(
        val method: String,
        val path: String,
        val httpVersion: String,
        val headers: Map<String, String>,
        val valid: Boolean,
    )

    private data class TileResponse(
        val body: ByteArray,
        val cacheControl: String,
    )

    companion object {
        private const val MAX_KEEP_ALIVE_REQUESTS = 10
        private const val LONG_CACHE_CONTROL = "public, max-age=31536000, immutable"

        fun startServer(): LocalTileServer {
            val socket = ServerSocket(0)
            val server = LocalTileServer(socket)
            server.start()
            return server
        }
    }
}
