package com.mapconductor.openmobilemaps

import android.view.View
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.map.MapViewHolderInterface
import io.openmobilemaps.mapscore.map.view.MapView
import io.openmobilemaps.mapscore.shared.graphics.common.Vec2F
import io.openmobilemaps.mapscore.shared.map.MapInterface
import io.openmobilemaps.mapscore.shared.map.coordinates.Coord
import io.openmobilemaps.mapscore.shared.map.coordinates.CoordinateSystemIdentifiers

/**
 * 実装点 A。**投影をここ以外に書かないこと。**
 *
 * Open Mobile Maps は `MapCameraInterface` に
 * `screenPosFromCoord` / `coordFromScreenPosition` の**同期変換を両方向持っている**ので、
 * InfoBubble・タイル方式マーカーの当たり判定・マーカーアニメ・`buildVisibleRegion` が
 * すべてそのまま動く（ios-for-longdo のように同期変換が無い SDK ではここが nil になる）。
 *
 * ## 座標系
 *
 * SDK の [Coord] は「系 ID + x + y + z」で、EPSG:4326 のとき **x が経度・y が緯度**。
 * MapConductor 側は latitude / longitude なので、順序を取り違えないこと
 * （取り違えても座標が入れ替わるだけで例外は出ないので気づきにくい）。
 */
class OpenMobileMapsMapViewHolder(
    override val mapView: MapView,
    override val map: MapInterface,
) : MapViewHolderInterface<MapView, MapInterface> {
    /** 地理座標 → 画面座標。 */
    override fun toScreenOffset(position: GeoPointInterface): Offset? {
        val camera = runCatching { map.camera }.getOrNull() ?: return null
        val screen = runCatching { camera.screenPosFromCoord(position.toOmmCoord()) }.getOrNull() ?: return null
        return Offset(screen.x, screen.y)
    }

    /** 画面座標 → 地理座標。 */
    override fun fromScreenOffsetSync(offset: Offset): GeoPoint? {
        val camera = runCatching { map.camera }.getOrNull() ?: return null
        val coord = runCatching { camera.coordFromScreenPosition(Vec2F(offset.x, offset.y)) }.getOrNull() ?: return null
        return coord.toGeoPoint()
    }

    /**
     * ビューポートの大きさ。`buildVisibleRegion()` が 4 隅を逆投影するのに使う。
     *
     * 既定実装（`mapView as? View` の幅高さ）で足りるので本来は書かなくてよいが、
     * SDK の [MapView] は `GLSurfaceView` 系でレイアウト前は 0 を返すため、
     * その場合に null を返すことを明示しておく。
     */
    override fun viewportSizePx(): Size? {
        val view = mapView as? View ?: return null
        if (view.width <= 0 || view.height <= 0) return null
        return Size(view.width.toFloat(), view.height.toFloat())
    }
}

/** MapConductor の座標 → SDK の [Coord]（EPSG:4326 は x=経度 / y=緯度）。 */
internal fun GeoPointInterface.toOmmCoord(): Coord =
    Coord(CoordinateSystemIdentifiers.EPSG4326(), longitude, latitude, altitude ?: 0.0)

/** SDK の [Coord] → MapConductor の座標。 */
internal fun Coord.toGeoPoint(): GeoPoint = GeoPoint(latitude = y, longitude = x, altitude = z)
