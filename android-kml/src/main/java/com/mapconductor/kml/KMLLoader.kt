package com.mapconductor.kml

import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * KML/KMZ を URL から取得し、`<NetworkLink>` が参照する外部ドキュメントまで
 * たどって 1 枚のフィーチャリストへ平坦化するローダ。
 *
 * KML はインターネット上の別の KML/KMZ を参照できる（NetworkLink）ため、取得は
 * キュー + 訪問済みセットのループで回す。参照がどれだけ連鎖しても呼び出しスタックを
 * 消費せず、循環参照があっても各 URL を 1 回しか取得しない。
 *
 * - 取得数は [maxDocuments] 枚まで（ルート含む）。超過分のリンクは読まない。
 * - `visibility` が 0 の NetworkLink は追跡しない。
 * - リンク先の取得・解析失敗はそのリンクだけをスキップし [onDocumentError] へ通知する。
 *   ルート自身の失敗は [load] がそのまま投げる。
 * - `refreshInterval` などの再読込モードは扱わない（一度だけ読む）。
 *
 * ネットワークを使うため、利用アプリには `android.permission.INTERNET` が必要。
 * [fetch] を差し替えると取得手段（テスト用スタブや独自 HTTP スタック）を注入できる。
 */
class KMLLoader(
    private val maxDocuments: Int = DEFAULT_MAX_DOCUMENTS,
    private val onDocumentError: ((url: String, error: Throwable) -> Unit)? = null,
    private val fetch: (url: String) -> InputStream = { url -> defaultFetch(url) },
) {
    /** [url]（http / https / file）の KML/KMZ を読み、NetworkLink の参照先も合流させる。 */
    suspend fun load(url: String): List<KMLFeature> =
        withContext(Dispatchers.IO) {
            collect(rootStream = null, rootUrl = url)
        }

    /**
     * アプリ側で開いた [inputStream]（assets など）を読み、NetworkLink の参照先も合流させる。
     * 相対 href は [baseUrl] に対して解決する。null のときは絶対 URL のリンクだけ追跡する。
     * ストリームは消費されるが close はしない — 呼び出し側が所有する。
     */
    suspend fun load(
        inputStream: InputStream,
        baseUrl: String? = null,
    ): List<KMLFeature> =
        withContext(Dispatchers.IO) {
            collect(rootStream = inputStream, rootUrl = baseUrl)
        }

    private fun collect(
        rootStream: InputStream?,
        rootUrl: String?,
    ): List<KMLFeature> {
        val features = ArrayList<KMLFeature>()
        val queue = ArrayDeque<String>()
        val visited = HashSet<String>()
        var loaded = 0

        fun merge(
            document: KMLDocument,
            baseUrl: String?,
        ) {
            loaded++
            features.addAll(document.features)
            for (link in document.networkLinks) {
                if (!link.visibility) continue
                val resolved = resolveHref(baseUrl, link.href) ?: continue
                if (visited.add(resolved)) queue.addLast(resolved)
            }
        }

        // ルートは呼び出し側の指定そのものなので、失敗はスキップせず投げる。
        rootUrl?.let { visited.add(it) }
        val rootDocument =
            if (rootStream != null) {
                KMLParser.parseDocument(rootStream)
            } else {
                fetch(rootUrl!!).use { KMLParser.parseDocument(it) }
            }
        merge(rootDocument, rootUrl)

        while (queue.isNotEmpty() && loaded < maxDocuments) {
            val url = queue.removeFirst()
            val document =
                try {
                    fetch(url).use { KMLParser.parseDocument(it) }
                } catch (e: Exception) {
                    onDocumentError?.invoke(url, e)
                    continue
                }
            merge(document, url)
        }
        return features
    }

    companion object {
        const val DEFAULT_MAX_DOCUMENTS = 20
        private const val MAX_REDIRECTS = 10

        /** 相対 [href] を [base] に対して解決する。base 不明の相対参照は追跡できず null。 */
        internal fun resolveHref(
            base: String?,
            href: String,
        ): String? {
            if (href.contains("://")) return href
            if (base == null) return null
            return runCatching { URL(URL(base), href).toString() }.getOrNull()
        }

        /**
         * http / https は Cordova 実装と同様にリダイレクトを [MAX_REDIRECTS] 回まで手動で追う
         * （HttpURLConnection の自動追従は http → https のプロトコル跨ぎを拒否するため）。
         * それ以外のスキームは [URL.openStream] に委ねる。
         */
        private fun defaultFetch(urlStr: String): InputStream {
            if (!urlStr.startsWith("http://") && !urlStr.startsWith("https://")) {
                return URL(urlStr).openStream()
            }
            var url = URL(urlStr)
            repeat(MAX_REDIRECTS) {
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 15_000
                connection.readTimeout = 30_000
                connection.instanceFollowRedirects = true
                val status = connection.responseCode
                when (status) {
                    HttpURLConnection.HTTP_OK -> return connection.inputStream
                    in 300..399 -> {
                        val location =
                            connection.getHeaderField("Location")
                                ?: throw IOException("Redirect without Location from $url")
                        val next = URL(url, location)
                        connection.disconnect()
                        url = next
                    }
                    else -> {
                        connection.disconnect()
                        throw IOException("HTTP $status for $url")
                    }
                }
            }
            throw IOException("Too many redirects for $urlStr")
        }
    }
}
