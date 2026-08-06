package com.mapconductor.example

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color

/**
 * タイル要求に**実際に載っていたヘッダ**を記録する計測用サーバ。
 *
 * `RasterLayerState` の `userAgent` / `extraHeaders` が本当に送信されているかは、
 * 送り出す側のコードを読んでも分からない（プロバイダのネイティブ SDK が握っている）。
 * 受け取る側を自分で立てて、届いたヘッダをそのまま読むのが唯一の確実な確認方法。
 *
 * 端末内 127.0.0.1 に立てるので、実機でもエミュレータでも外部依存なしに動く。
 * ios-sdk の `HeaderRecordingTileServer` と対になっている。
 */
class HeaderRecordingTileServer private constructor(
    private val serverSocket: ServerSocket,
) {
    /** 記録した 1 リクエスト分。ヘッダ名は小文字で正規化して入れる。 */
    data class Record(
        val path: String,
        val headers: Map<String, String>,
    )

    private val lock = Any()
    private val records = mutableListOf<Record>()
    private val running = AtomicBoolean(true)

    /** 応答する PNG。毎回生成すると重いので 1 回だけ作る。 */
    private val tilePng: ByteArray = makeTilePng()

    val baseUrl: String = "http://127.0.0.1:${serverSocket.localPort}"

    val requestCount: Int
        get() = synchronized(lock) { records.size }

    /**
     * これまでに一度でも観測したヘッダ値。
     *
     * 直近だけを見ると取りこぼす: ベースマップのスタイル要求とラスタタイル要求が
     * 混ざって飛ぶので、「最後の 1 本」がこちらの狙ったタイルとは限らない。
     */
    fun anyHeader(name: String): String? {
        val key = name.lowercase()
        return synchronized(lock) { records.asReversed().firstNotNullOfOrNull { it.headers[key] } }
    }

    fun urlTemplate(): String = "$baseUrl/tiles/{z}/{x}/{y}.png"

    fun stop() {
        running.set(false)
        runCatching { serverSocket.close() }
    }

    private fun acceptLoop() {
        while (running.get()) {
            val socket =
                try {
                    serverSocket.accept()
                } catch (e: Exception) {
                    return
                }
            thread(isDaemon = true) { handle(socket) }
        }
    }

    private fun handle(socket: Socket) {
        socket.use { s ->
            runCatching {
                val request = readRequest(s.getInputStream()) ?: return
                synchronized(lock) { records.add(request) }
                writeResponse(s.getOutputStream())
            }
        }
    }

    /**
     * リクエスト行とヘッダだけ読む。
     *
     * バイト列を自前で改行分割する。文字列にしてから分割する実装は CRLF の扱いを
     * 間違えやすく、ios-sdk では実際にそれでヘッダが 1 つも取れていなかった。
     */
    private fun readRequest(input: InputStream): Record? {
        val head = ByteArrayOutputStream()
        var matched = 0
        while (matched < 4) {
            val b = input.read()
            if (b < 0) return null
            head.write(b)
            matched =
                when {
                    matched == 0 && b == 13 -> 1
                    matched == 1 && b == 10 -> 2
                    matched == 2 && b == 13 -> 3
                    matched == 3 && b == 10 -> 4
                    b == 13 -> 1
                    else -> 0
                }
            if (head.size() > 64 * 1024) return null
        }

        val lines = head.toString("UTF-8").split("\r\n")
        val requestLine = lines.firstOrNull()?.trim().orEmpty()
        val path = requestLine.split(" ").getOrNull(1).orEmpty()

        val headers = mutableMapOf<String, String>()
        for (line in lines.drop(1)) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue
            val index = trimmed.indexOf(':')
            if (index <= 0) continue
            val key = trimmed.substring(0, index).trim().lowercase()
            val value = trimmed.substring(index + 1).trim()
            if (key.isNotEmpty() && value.isNotEmpty()) headers[key] = value
        }

        return Record(path = path, headers = headers)
    }

    private fun writeResponse(output: OutputStream) {
        val head =
            buildString {
                append("HTTP/1.1 200 OK\r\n")
                append("Content-Type: image/png\r\n")
                append("Content-Length: ${tilePng.size}\r\n")
                // 計測なので毎回ネットワークまで来てほしい。キャッシュされるとヘッダが観測できない。
                append("Cache-Control: no-store\r\n")
                append("Connection: close\r\n\r\n")
            }
        output.write(head.toByteArray(Charsets.UTF_8))
        output.write(tilePng)
        output.flush()
    }

    /** 目視でも「敷かれている」と分かるよう、半透明のマゼンタで塗った 256px タイルを返す。 */
    private fun makeTilePng(): ByteArray {
        val bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
        Canvas(bitmap).drawColor(Color.argb(90, 255, 0, 255))
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    companion object {
        fun start(): HeaderRecordingTileServer {
            val socket = ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"))
            val server = HeaderRecordingTileServer(socket)
            thread(isDaemon = true) { server.acceptLoop() }
            return server
        }
    }
}
