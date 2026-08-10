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
    /**
     * 地理座標 → 画面座標。
     *
     * SDK が返すのは**内側の [io.openmobilemaps.mapscore.map.view.MapView] の座標**なので、
     * [OpenMobileMapsMapSurface.fromInnerToSurface] で入れ物の座標へ畳んでから返すこと。
     * 傾けているとき内側は拡大・回転しているため、畳まないとオーバーレイが全部ずれる。
     */
    override fun toScreenOffset(position: GeoPointInterface): Offset? {
        val screen =
            runCatching { map.getCamera().screenPosFromCoord(position.toOmmCoord()) }.getOrNull() ?: return null
        return mapView.fromInnerToSurface(Offset(screen.x, screen.y))
    }

    /** 画面座標 → 地理座標。入り口で入れ物の座標を内側の座標へ戻す（[toScreenOffset] の逆）。 */
    override fun fromScreenOffsetSync(offset: Offset): GeoPoint? {
        val inner = mapView.fromSurfaceToInner(offset) ?: return null
        val coord =
            runCatching { map.getCamera().coordFromScreenPosition(Vec2F(inner.x, inner.y)) }.getOrNull()
                ?: return null
        return toWgs84(coord)?.toGeoPoint()
    }

    /*
     * ビューポートの大きさはコアの [com.mapconductor.core.map.viewportSizePx] が
     * [mapView] から解決する（拡張関数なので override はできない）。
     *
     * 返るのは入れ物（[OpenMobileMapsMapSurface]）の大きさ。投影も入れ物の座標系へ
     * 畳んであるので、傾けていても `visibleRegion` の 4 隅は実際に見えている範囲になる。
     */

    /** 地図の座標系（EPSG:3857）→ EPSG:4326。 */
    internal fun toWgs84(coord: Coord): Coord? {
        if (coord.systemIdentifier == CoordinateSystemIdentifiers.EPSG4326()) return coord
        return runCatching {
            map.getCoordinateConverterHelper().convert(CoordinateSystemIdentifiers.EPSG4326(), coord)
        }.getOrNull()
    }
}
