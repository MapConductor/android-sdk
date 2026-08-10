package com.mapconductor.openmobilemaps

import androidx.compose.ui.geometry.Offset
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.map.MapViewHolderInterface
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
 * ## 座標系の変換方向に注意
 *
 * 地図は EPSG:3857（Web メルカトル、単位はメートル）で構成してある。タイルがどれも
 * 3857 なのと、[com.mapconductor.openmobilemaps.zoom.ZoomAltitudeConverter] の縮尺の
 * 導出が「地図単位 = メルカトルメートル」を前提にしているため。
 *
 * - **入力**（`toScreenOffset`）: EPSG:4326 の [Coord] をそのまま渡してよい。
 *   SDK が `CoordinateConversionHelper` で地図の系へ変換する。
 * - **出力**（`fromScreenOffsetSync`）: SDK は**地図の系（3857）**で返す。
 *   ここで 4326 へ戻さないと、緯度経度のつもりでメートル値を扱うことになる。
 *   症状は「タップ位置が地球の裏側になる」で、非常に分かりやすく壊れる。
 */
class OpenMobileMapsMapViewHolder(
    override val mapView: OpenMobileMapsMapSurface,
    override val map: MapInterface,
) : MapViewHolderInterface<OpenMobileMapsMapSurface, MapInterface> {
    /** 地理座標 → 画面座標。 */
    override fun toScreenOffset(position: GeoPointInterface): Offset? {
        val screen =
            runCatching { map.getCamera().screenPosFromCoord(position.toOmmCoord()) }.getOrNull() ?: return null
        return Offset(screen.x, screen.y)
    }

    /** 画面座標 → 地理座標。 */
    override fun fromScreenOffsetSync(offset: Offset): GeoPoint? {
        val coord =
            runCatching { map.getCamera().coordFromScreenPosition(Vec2F(offset.x, offset.y)) }.getOrNull()
                ?: return null
        return toWgs84(coord)?.toGeoPoint()
    }

    /*
     * ビューポートの大きさはコアの [com.mapconductor.core.map.viewportSizePx] が
     * [mapView] から解決する（拡張関数なので override はできない）。
     *
     * tilt を掛けているとき、内側の [io.openmobilemaps.mapscore.map.view.MapView] は
     * [OpenMobileMapsMapSurface] より広く、投影は内側の座標系で返る。そのぶん
     * `visibleRegion` と InfoBubble の位置はずれる。android-for-arcgis の 2D も
     * 同じ割り切りで、tilt = 0 のときは厳密に一致する。
     */

    /** 地図の座標系（EPSG:3857）→ EPSG:4326。 */
    internal fun toWgs84(coord: Coord): Coord? {
        if (coord.systemIdentifier == CoordinateSystemIdentifiers.EPSG4326()) return coord
        return runCatching {
            map.getCoordinateConverterHelper().convert(CoordinateSystemIdentifiers.EPSG4326(), coord)
        }.getOrNull()
    }
}
