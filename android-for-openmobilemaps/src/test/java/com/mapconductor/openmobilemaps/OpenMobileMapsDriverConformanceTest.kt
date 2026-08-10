package com.mapconductor.openmobilemaps

import com.mapconductor.core.conformance.MapDriverConformance
import com.mapconductor.core.controller.OverlayKind
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MutableMapServiceRegistry
import com.mapconductor.core.raster.TileScheme
import com.mapconductor.core.spherical.Spherical
import com.mapconductor.openmobilemaps.tile.WebMercatorTileLayerConfig
import com.mapconductor.openmobilemaps.tile.zoomScaleForLevel
import com.mapconductor.openmobilemaps.zoom.ZoomAltitudeConverter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

/**
 * ドライバーの適合テスト。
 *
 * ## ここで確かめられないことのほうが多い
 *
 * `android-for-template` の適合テストはドライバーをまるごと組み立てて投影もカスケードも
 * 検証できるが、それは雛形の「地図」が純粋な Kotlin だから。Open Mobile Maps は
 * `PolygonLayerInterface.create()` の時点で JNI に入るので、**コントローラもレンダラも
 * 素の JVM では作れない**。
 *
 * したがってここで押さえるのは「地図SDKに触らない純粋な計算」だけ:
 *
 *  - ズームの往復換算（この SDK 固有の、縮尺の分母 ⇄ 統一ズーム）
 *  - tilt 擬似表現の往復
 *  - タイル URL とズームレベルの刻み
 *  - capability の宣言に理由がついているか
 *  - カスケードの探索順（コアの定数）
 *
 * **描画・タップ・ドラッグ・InfoBubble の追従は実機で確かめること。**
 * `android-for-openmobilemaps/README.md` の確認手順を参照。
 */
class OpenMobileMapsDriverConformanceTest {
    private val converter = ZoomAltitudeConverter()

    // ── ズーム ──────────────────────────────────────────────────────────

    @Test
    fun `統一ズームと SDK の縮尺が往復する`() {
        listOf(0.0, 1.0, 5.5, 10.0, 15.25, 19.0, 22.0).forEach { zoom ->
            val native = converter.toNativeZoom(zoom)
            val roundTrip = converter.toUnifiedZoom(native)
            assertTrue(
                "統一ズーム $zoom -> 縮尺 $native -> $roundTrip で往復しない",
                abs(roundTrip - zoom) < 1e-9,
            )
        }
    }

    @Test
    fun `ズームが上がると縮尺の分母は下がる`() {
        // 「ズームインすると縮尺が細かくなる」。符号を取り違えると地図が逆に動く。
        var previous = Double.MAX_VALUE
        (0..22).forEach { zoom ->
            val native = converter.toNativeZoom(zoom.toDouble())
            assertTrue("縮尺が単調減少していない（zoom=$zoom）", native < previous)
            previous = native
        }
    }

    @Test
    fun `統一ズーム 0 の縮尺が導出値どおり`() {
        // 156543.033928 x 160 / 0.0254。ここがずれると Google Maps と大きさが揃わない。
        assertEquals(986_097_222.0, converter.toNativeZoom(0.0), 1.0)
        // 1 段ズームインで縮尺はちょうど半分。
        assertEquals(
            converter.toNativeZoom(0.0) / 2.0,
            converter.toNativeZoom(1.0),
            1e-6,
        )
    }

    // ── tilt 擬似表現 ───────────────────────────────────────────────────

    @Test
    fun `tilt が 0 以上ならカメラを動かさない`() {
        val position =
            MapCameraPosition(
                position = GeoPoint.fromLatLong(35.681, 139.767),
                zoom = 14.0,
                bearing = 30.0,
                tilt = 45.0,
            )
        val (center, zoom) = OpenMobileMapsTiltEmulation.shiftedCamera(position)
        assertEquals(35.681, center.latitude, 1e-12)
        assertEquals(139.767, center.longitude, 1e-12)
        assertEquals(14.0, zoom, 1e-12)
    }

    @Test
    fun `tilt が負なら前進し、巻き戻すと元へ戻る`() {
        val origin = GeoPoint.fromLatLong(35.681, 139.767)
        val position =
            MapCameraPosition(position = origin, zoom = 14.0, bearing = 90.0, tilt = -45.0)

        val (shiftedCenter, shiftedZoom) = OpenMobileMapsTiltEmulation.shiftedCamera(position)
        assertTrue(
            "tilt < 0 では中心が進行方向へ前進していなければならない",
            Spherical.computeDistanceBetween(origin, shiftedCenter) > 1.0,
        )
        assertTrue("tilt < 0 ではズームが引かれる", shiftedZoom < 14.0)

        val (restoredCenter, restoredZoom) =
            OpenMobileMapsTiltEmulation.restoreLogicalCamera(shiftedCenter, shiftedZoom, 90.0, -45.0)
        assertEquals(14.0, restoredZoom, 1e-9)
        val forward = Spherical.computeDistanceBetween(origin, shiftedCenter)
        val residual = Spherical.computeDistanceBetween(origin, restoredCenter)
        assertTrue(
            // 完全一致はしない。巻き戻しの視距離を「前進後の緯度」で計算するため
            // （ArcGIS2D も同じ）。前進量に対して十分小さければよい。
            "巻き戻しの誤差が大きすぎる（前進 $forward m に対し残差 $residual m）",
            residual < forward * 0.01,
        )
    }

    // ── タイル設定 ──────────────────────────────────────────────────────

    @Test
    fun `XYZ と TMS で y の向きが変わる`() {
        val xyz = tileConfig(TileScheme.XYZ)
        val tms = tileConfig(TileScheme.TMS)

        assertEquals("https://example.test/3/2/1.png", xyz.getTileUrl(2, 1, 0, 3))
        // z=3 は 8x8 なので TMS の y は 8 - 1 - 1 = 6。
        assertEquals("https://example.test/3/2/6.png", tms.getTileUrl(2, 1, 0, 3))
    }

    @Test
    fun `ズームレベルの縮尺が統一ズームと 1 対 1 になる`() {
        // density = 1.0 / 256px タイルなら、レベル L の縮尺は統一ズーム L の縮尺と一致する。
        // ここがずれると常に 1 段ぼけた（あるいは無駄に細かい）タイルが選ばれる。
        (0..22).forEach { level ->
            val scale = zoomScaleForLevel(level, tileSize = 256, density = 1.0f)
            assertEquals(
                "レベル $level の縮尺が統一ズームと一致しない",
                converter.toNativeZoom(level.toDouble()),
                scale,
                scale * 1e-9,
            )
        }
    }

    @Test
    fun `512px タイルと端末密度でレベルがずれる`() {
        // 512px タイルは 256px の 2 枚ぶんを覆うので、同じ画面には 1 段浅いレベルでよい。
        val small = zoomScaleForLevel(10, tileSize = 256, density = 1.0f)
        val large = zoomScaleForLevel(10, tileSize = 512, density = 1.0f)
        assertEquals(large * 2.0, small, small * 1e-9)

        // 高密度端末は同じ画面により細かいタイルが要る（＝縮尺の分母が大きい側へ寄る）。
        val retina = zoomScaleForLevel(10, tileSize = 256, density = 3.0f)
        assertEquals(small * 3.0, retina, retina * 1e-9)
    }

    // ── 契約 ────────────────────────────────────────────────────────────

    @Test
    fun `capability の宣言に理由がついている`() {
        val registry = MutableMapServiceRegistry()
        OpenMobileMapsCapabilities.declare(registry)
        MapDriverConformance.checkCapabilityDeclarations(registry)
    }

    @Test
    fun `カスケードの探索順が正準どおり`() {
        MapDriverConformance.checkCascadeOrder()
    }

    @Test
    fun `OverlayKind は 6 種別`() {
        assertEquals(6, OverlayKind.entries.size)
    }

    private fun tileConfig(scheme: TileScheme) =
        WebMercatorTileLayerConfig(
            layerName = "test",
            urlTemplate = "https://example.test/{z}/{x}/{y}.png",
            tileSize = 256,
            density = 1.0f,
            scheme = scheme,
        )
}
