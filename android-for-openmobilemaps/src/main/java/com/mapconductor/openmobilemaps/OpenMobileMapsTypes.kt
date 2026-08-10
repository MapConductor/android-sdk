package com.mapconductor.openmobilemaps

import androidx.compose.ui.graphics.Color
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface
import io.openmobilemaps.mapscore.shared.graphics.common.Color as OmmColor
import io.openmobilemaps.mapscore.shared.map.LayerInterface
import io.openmobilemaps.mapscore.shared.map.coordinates.Coord
import io.openmobilemaps.mapscore.shared.map.coordinates.CoordinateSystemIdentifiers
import io.openmobilemaps.mapscore.shared.map.coordinates.PolygonCoord
import io.openmobilemaps.mapscore.shared.map.layers.icon.IconInfoInterface
import io.openmobilemaps.mapscore.shared.map.layers.line.LineInfoInterface
import io.openmobilemaps.mapscore.shared.map.layers.polygon.PolygonInfo
import io.openmobilemaps.mapscore.shared.map.layers.polygon.TexturedPolygonLayerInterface
import io.openmobilemaps.mapscore.shared.map.layers.tiled.raster.Tiled2dMapRasterLayerInterface

/*
 * 「地図に載っている実体」の型と、MapConductor ⇄ SDK の値の変換。
 *
 * ## ActualXxx はコアが id で引き当てるための持ち手にすぎない
 *
 * コアの Manager は `ActualXxx` の中身を一切見ない。レンダラが `onAdd` で作って返し、
 * `onChange` / `onRemove` で受け取る。したがって「SDK の描画オブジェクトそのもの」を
 * そのまま入れるのが一番素直である。Open Mobile Maps はレイヤが
 * `IconInfoInterface` / `LineInfoInterface` / `PolygonInfo` のリストを持つ形なので、
 * その要素をそのまま Actual として扱う。
 */

/** 地図上のマーカー 1 つ。SDK の [IconInfoInterface] そのもの。 */
typealias OpenMobileMapsActualMarker = IconInfoInterface

/**
 * 地図上のポリライン 1 本。
 *
 * 1 本のポリラインが**複数の [LineInfoInterface] になる**ことがある。geodesic な線を
 * 密度化したうえで子午線で分割すると、±180° を跨ぐ線が 2 本以上のセグメントに割れるため。
 */
typealias OpenMobileMapsActualPolyline = List<LineInfoInterface>

/** 塗りと輪郭の組。Open Mobile Maps のポリゴンレイヤは輪郭線を描かないので線レイヤと併用する。 */
data class OpenMobileMapsActualPolygon(
    val fills: List<PolygonInfo>,
    val outlines: List<LineInfoInterface>,
)

/** 円。SDK に円の概念が無いのでポリゴン + 輪郭線として描く。 */
data class OpenMobileMapsActualCircle(
    val fills: List<PolygonInfo>,
    val outlines: List<LineInfoInterface>,
)

/**
 * グラウンドイメージ。SDK の「テクスチャ付きポリゴンレイヤ」1 枚に対応する。
 *
 * [layerInterface] を持っているのは、`asLayerInterface()` が**呼ぶたびに別のオブジェクトを
 * 返す**ため（[OpenMobileMapsLayers] のコメント参照）。載せたときの値をそのまま持っておかないと
 * 地図から外せない。
 */
data class OpenMobileMapsActualGroundImage(
    val layer: TexturedPolygonLayerInterface,
    val layerInterface: LayerInterface,
)

/** ラスターレイヤ。[layerInterface] を持つ理由は [OpenMobileMapsActualGroundImage] と同じ。 */
data class OpenMobileMapsActualRasterLayer(
    val layer: Tiled2dMapRasterLayerInterface,
    val layerInterface: LayerInterface,
)

// ── 座標 ──────────────────────────────────────────────────────────────────

/**
 * MapConductor の座標 → SDK の [Coord]。
 *
 * EPSG:4326 では **x が経度・y が緯度**。MapConductor は latitude / longitude の順なので、
 * 取り違えても例外にならず「座標が入れ替わるだけ」になり、原因が非常に追いにくい。
 * 変換をこの 1 箇所に閉じ込めているのはそのため。
 */
internal fun GeoPointInterface.toOmmCoord(): Coord =
    Coord(CoordinateSystemIdentifiers.EPSG4326(), longitude, latitude, altitude ?: 0.0)

/** SDK の [Coord] → MapConductor の座標。 */
internal fun Coord.toGeoPoint(): GeoPoint = GeoPoint(latitude = y, longitude = x, altitude = z)

/** 点列 → SDK の座標列。SDK の API はどれも [ArrayList] を要求する（[List] では通らない）。 */
internal fun List<GeoPointInterface>.toOmmCoords(): ArrayList<Coord> =
    ArrayList<Coord>(size).also { out -> forEach { out.add(it.toOmmCoord()) } }

/**
 * 外周リングと穴リング → SDK の [PolygonCoord]。
 *
 * Open Mobile Maps は**穴をネイティブに持てる**（`PolygonCoord.holes`）数少ない SDK。
 * 穴をブリッジで外周に繋ぐ細工（[com.mapconductor.core.polygon.bridgeHolesIntoSingleRing]）は要らない。
 */
internal fun polygonCoordOf(
    outer: List<GeoPointInterface>,
    holes: List<List<GeoPointInterface>>,
): PolygonCoord =
    PolygonCoord(
        outer.toOmmCoords(),
        ArrayList<ArrayList<Coord>>(holes.size).also { out -> holes.forEach { out.add(it.toOmmCoords()) } },
    )

// ── 色 ────────────────────────────────────────────────────────────────────

/**
 * Compose の色 → SDK の色。
 *
 * SDK 側は 0..1 の float 4 つ。**アルファ済み（premultiplied）ではない**ので、
 * そのまま渡してよい。
 */
internal fun Color.toOmmColor(): OmmColor = OmmColor(red, green, blue, alpha)

/** 完全透明。「塗らない」の意味で使う。 */
internal fun transparentOmmColor(): OmmColor = OmmColor(0f, 0f, 0f, 0f)
