package com.mapconductor.example

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mapconductor.example.pages.rasterlayer.probeExtraHeaders
import com.mapconductor.example.pages.rasterlayer.probeTemplate
import com.mapconductor.example.pages.rasterlayer.probeUserAgent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import android.content.Intent

/**
 * `RasterLayerState` の `userAgent` / `extraHeaders` が**実際にタイル要求に載るか**を
 * プロバイダごとに実機で確かめる。
 *
 * 送り出す側のコードを読んでも分からない: タイルを取りに行くのはプロバイダの
 * ネイティブ SDK で、こちらが渡したヘッダを使うかどうかは SDK 次第。プロセス内に
 * 受け側（[HeaderRecordingTileServer]）を立てて、届いたヘッダをそのまま読む。
 *
 * 対応できないプロバイダがあるのは想定内で、その場合は「非対応であること」を
 * 固定するテストとして残す。落とすためではなく、SDK 更新でフックが増えたときに
 * 気づくため。ios-sdk の `RasterHeaderUITests` と対になっている。
 */
@RunWith(AndroidJUnit4::class)
class RasterHeaderInstrumentedTest {
    private data class Observed(
        val count: Int,
        val userAgent: String?,
        val custom: String?,
    )

    /**
     * 実アプリの計測ページをそのプロバイダで開き、届いたヘッダを読む。
     *
     * `createComposeRule()` は使えない。あれはフレームクロックをテスト側が握るので、
     * `waitForIdle` の類を呼ばないと**コンポジションが進まない**。かといって
     * `waitUntil` を呼ぶと、地図ビューは描画ループを回し続けるので永久に idle にならず
     * `AppNotIdleException` になる。どちらにも倒れないよう、実アプリの Activity を
     * そのまま起動する（ios-sdk がサンプルアプリを起動しているのと同じ形）。
     *
     * Android の計装テストはアプリと同じプロセスで動くので、テスト側で立てたサーバを
     * ページへ直接渡せる。
     */
    private fun observe(providerKey: String): Observed {
        val server = HeaderRecordingTileServer.start()
        probeTemplate = server.urlTemplate()
        probeUserAgent = EXPECTED_USER_AGENT
        probeExtraHeaders = mapOf(TEST_HEADER_NAME to EXPECTED_HEADER_VALUE)

        val intent =
            Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java).apply {
                putExtra("page", "raster-header")
                putExtra("provider", providerKey)
            }

        try {
            ActivityScenario.launch<MainActivity>(intent).use {
                // タイル要求が 1 本でも届くまで待つ。届かないままなら
                // ヘッダの合否以前の問題なので、専用のメッセージで落とす。
                val deadline = System.currentTimeMillis() + TILE_TIMEOUT_MS
                while (System.currentTimeMillis() < deadline && server.requestCount == 0) {
                    Thread.sleep(300)
                }

                // 最初の 1 本が届いた直後は、後続のタイルがまだ飛んでいる途中のことがある。
                // 少し待って観測を安定させる。
                Thread.sleep(3_000)

                return Observed(
                    count = server.requestCount,
                    userAgent = server.anyHeader("User-Agent"),
                    custom = server.anyHeader(TEST_HEADER_NAME),
                )
            }
        } finally {
            server.stop()
            probeTemplate = null
            probeExtraHeaders = null
        }
    }

    /** ヘッダが載っていることを要求する（対応済みプロバイダ用）。 */
    private fun assertHeadersSent(providerKey: String) {
        val result = observe(providerKey)
        assertTrue("$providerKey: タイルを取りに来ていない", result.count > 0)
        assertEquals(
            "$providerKey: userAgent がタイル要求に載っていない",
            EXPECTED_USER_AGENT,
            result.userAgent,
        )
        assertEquals(
            "$providerKey: extraHeaders がタイル要求に載っていない",
            EXPECTED_HEADER_VALUE,
            result.custom,
        )
    }

    /**
     * どちらも載らない。**非対応であること**を固定する。
     *
     * 落とすためではなく、載るようになったら気づくために書いてある。
     */
    private fun assertHeadersIgnored(providerKey: String) {
        val result = observe(providerKey)
        assertTrue("$providerKey: タイルを取りに来ていない", result.count > 0)
        assertNotEquals(
            "$providerKey: userAgent が載るようになった。実装を足して assertHeadersSent へ移すこと",
            EXPECTED_USER_AGENT,
            result.userAgent,
        )
        assertNull(
            "$providerKey: extraHeaders が載るようになった。実装を足して assertHeadersSent へ移すこと",
            result.custom,
        )
    }

    // MARK: 対応済み

    @Test
    fun mapLibreSendsRasterHeaders() {
        assertHeadersSent("maplibre")
    }

    @Test
    fun googleMapsSendsRasterHeaders() {
        assertHeadersSent("googlemap")
    }

    @Test
    fun hereSendsRasterHeaders() {
        assertHeadersSent("here")
    }

    // MARK: 非対応（ネイティブ SDK にリクエスト書き換えの口が無い）

    @Test
    fun mapboxIgnoresRasterHeaders() {
        assertHeadersIgnored("mapbox")
    }

    @Test
    fun arcGISIgnoresRasterHeaders() {
        assertHeadersIgnored("arcgis")
    }

    @Test
    fun tomTomIgnoresRasterHeaders() {
        assertHeadersIgnored("tomtom")
    }

    /**
     * MapTiler Android は WebView(MapLibre GL JS) ベースで、タイル取得は WebView 内の
     * fetch が行う。計測サーバ（`http://127.0.0.1`）へは WebView から到達できないので、
     * **この方法では観測できない**。非対応であることは実装側の stub + ログで担保する。
     *
     * 「到達できないこと」を書き留めておくためのテスト。到達できるようになったら
     * 落ちるので、そのときはヘッダの検証に切り替えること。観測できないまま
     * `assertHeadersIgnored` を通すと、非対応を確かめたつもりで何も確かめていない。
     */
    @Test
    fun mapTilerCannotBeMeasuredThroughWebView() {
        val result = observe("maptiler")
        assertEquals(
            "maptiler: 計測サーバに到達した。観測できるならヘッダの検証に切り替えること",
            0,
            result.count,
        )
    }

    @Test
    fun longdoIgnoresRasterHeaders() {
        assertHeadersIgnored("longdo")
    }

    private companion object {
        const val EXPECTED_USER_AGENT = "MapConductorRasterHeaderProbe/1.0"
        const val TEST_HEADER_NAME = "X-MapConductor-Test"
        const val EXPECTED_HEADER_VALUE = "mapconductor-probe"
        const val TILE_TIMEOUT_MS = 60_000L
    }
}
