package com.mapconductor.openmobilemaps

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.mapconductor.core.circle.CircleEntity
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.conformance.MapDriverConformance
import com.mapconductor.core.controller.OverlayKind
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MutableMapServiceRegistry
import com.mapconductor.core.polygon.PolygonEntity
import com.mapconductor.core.polygon.PolygonState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 雛形ドライバーの適合テスト。
 *
 * ## これが CI に入っていることが重要
 *
 * 雛形は放っておくと腐る。`projects.properties` の `modules=` に
 * `android-for-openmobilemaps` を入れてあるので、コアの契約が変わればこのテストが
 * 落ちるか、少なくともコンパイルが通らなくなる。**雛形が現実と合っていることを
 * 保証する仕組みはこれしかない。**
 *
 * ## ここで確かめられないこと
 *
 * マーカーの描画は `BitmapIcon`（`android.graphics.Bitmap`）を通るので素の JVM では
 * 動かない。マーカーのヒットテストとドラッグは実機で確かめること。
 */
class OpenMobileMapsDriverConformanceTest {
    private fun newDriver(): OpenMobileMapsMapViewController {
        val map = OpenMobileMapsMap()
        map.viewportSize = Size(1000f, 1000f)
        val holder = OpenMobileMapsMapViewHolder(OpenMobileMapsMapSurface(map), map)
        return OpenMobileMapsMapViewController(
            holder = holder,
            markerController = OpenMobileMapsMarkerController(OpenMobileMapsMarkerRenderer(holder, TestScope)),
            polylineController = OpenMobileMapsPolylineController(OpenMobileMapsPolylineRenderer(holder)),
            polygonController = OpenMobileMapsPolygonController(OpenMobileMapsPolygonRenderer(holder)),
            circleController = OpenMobileMapsCircleController(OpenMobileMapsCircleRenderer(holder)),
            groundImageController = OpenMobileMapsGroundImageController(OpenMobileMapsGroundImageRenderer(holder)),
            rasterLayerController =
                OpenMobileMapsRasterLayerController(OpenMobileMapsRasterLayerRenderer(holder, TestScope)),
            mainCoroutine = TestScope,
            defaultCoroutine = TestScope,
        ).also {
            // SDK のイベント配線。本番では Composable が呼ぶ。
            it.installListeners()
        }
    }

    private fun registeredControllers(driver: OpenMobileMapsMapViewController): List<Any> =
        listOf(
            driver.markerController,
            driver.polylineController,
            driver.polygonController,
            driver.circleController,
            driver.groundImageController,
            driver.rasterLayerController,
        )

    // ── 適合チェック ────────────────────────────────────────────────────

    @Test
    fun `ズームの往復換算が壊れていない`() {
        MapDriverConformance.checkZoomConverter(OpenMobileMapsMapViewController.ZOOM_CONVERTER)
    }

    @Test
    fun `6 種別すべてがスロットに参加している`() {
        // ここが落ちるのは「コントローラを作ったが SlottedOverlayController を
        // 実装し忘れた」とき。表示されない・更新されない・タップに反応しない、が
        // まとめてこの 1 本で出る。
        MapDriverConformance.checkOverlaySlots(registeredControllers(newDriver()))
    }

    @Test
    fun `カスケードの探索順が正準どおり`() {
        MapDriverConformance.checkCascadeOrder()
    }

    @Test
    fun `capability の宣言に理由がついている`() {
        val registry = MutableMapServiceRegistry()
        newDriver().declareCapabilities(registry)
        MapDriverConformance.checkCapabilityDeclarations(registry)
    }

    @Test
    fun `投影が往復する`() {
        val map = OpenMobileMapsMap()
        map.viewportSize = Size(1000f, 800f)
        map.moveCamera(OpenMobileMapsCamera(position = GeoPoint.fromLatLong(35.68, 139.76), zoom = 10.0))
        val holder = OpenMobileMapsMapViewHolder(OpenMobileMapsMapSurface(map), map)
        MapDriverConformance.checkProjectionRoundTrip(
            toScreen = { holder.toScreenOffset(it) },
            fromScreen = { holder.fromScreenOffsetSync(it) },
            samples =
                listOf(
                    GeoPoint.fromLatLong(35.68, 139.76),
                    GeoPoint.fromLatLong(0.0, 0.0),
                    GeoPoint.fromLatLong(-33.86, 151.2),
                ),
        )
    }

    // ── クリックカスケードが実際に効いているか ──────────────────────────

    @Test
    fun `重なった円とポリゴンでは円が勝ち、地図クリックは飛ばない`() {
        val driver = newDriver()
        val point = GeoPoint.fromLatLong(0.0, 0.0)
        var circleClicks = 0
        var polygonClicks = 0
        var mapClicks = 0

        driver.circleController.circleManager.registerEntity(
            CircleEntity(
                circle = OpenMobileMapsActualCircle("c", point, 100_000.0),
                state =
                    CircleState(
                        id = "c",
                        center = point,
                        radiusMeters = 100_000.0,
                        onClick = { circleClicks++ },
                    ),
            ),
        )
        driver.polygonController.polygonManager.registerEntity(
            PolygonEntity(
                polygon = OpenMobileMapsActualPolygon("p", emptyList()),
                state =
                    PolygonState(
                        id = "p",
                        points =
                            listOf(
                                GeoPoint.fromLatLong(-1.0, -1.0),
                                GeoPoint.fromLatLong(-1.0, 1.0),
                                GeoPoint.fromLatLong(1.0, 1.0),
                                GeoPoint.fromLatLong(1.0, -1.0),
                            ),
                        onClick = { polygonClicks++ },
                    ),
            ),
        )
        driver.setMapClickListener { mapClicks++ }

        driver.holder.map.onMapClick
            ?.invoke(point)

        assertEquals("円に配送される", 1, circleClicks)
        assertEquals("二重配送してはいけない", 0, polygonClicks)
        assertEquals("オーバーレイに当たったら地図クリックは飛ばない", 0, mapClicks)
    }

    @Test
    fun `どのオーバーレイにも当たらなければ地図クリックへ落ちる`() {
        val driver = newDriver()
        var mapClicks = 0
        driver.setMapClickListener { mapClicks++ }

        driver.holder.map.onMapClick
            ?.invoke(GeoPoint.fromLatLong(80.0, 170.0))

        assertEquals(1, mapClicks)
    }

    @Test
    fun `clickable が false の円は透過して地図クリックへ落ちる`() {
        val driver = newDriver()
        val point = GeoPoint.fromLatLong(0.0, 0.0)
        var circleClicks = 0
        var mapClicks = 0

        driver.circleController.circleManager.registerEntity(
            CircleEntity(
                circle = OpenMobileMapsActualCircle("c", point, 100_000.0),
                state =
                    CircleState(
                        id = "c",
                        center = point,
                        radiusMeters = 100_000.0,
                        clickable = false,
                        onClick = { circleClicks++ },
                    ),
            ),
        )
        driver.setMapClickListener { mapClicks++ }

        driver.holder.map.onMapClick
            ?.invoke(point)

        assertEquals("clickable=false は握り潰しではなく透過", 0, circleClicks)
        assertEquals(1, mapClicks)
    }

    // ── 雛形そのものの健全性 ────────────────────────────────────────────

    @Test
    fun `投影はビューポート未計測なら null を返す`() {
        val map = OpenMobileMapsMap()
        assertTrue(map.project(GeoPoint.fromLatLong(0.0, 0.0)) == null)
        assertTrue(map.unproject(Offset.Zero) == null)
    }

    @Test
    fun `OverlayKind は 6 種別`() {
        assertEquals(6, OverlayKind.entries.size)
    }

    private companion object {
        /** テストでは即時実行のスコープでよい（コルーチンの並行性はここでは見ない）。 */
        val TestScope =
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Unconfined)
    }
}
