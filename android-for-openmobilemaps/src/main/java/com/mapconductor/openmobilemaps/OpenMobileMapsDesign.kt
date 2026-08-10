package com.mapconductor.openmobilemaps

import com.mapconductor.core.map.AttributionRule
import com.mapconductor.core.map.MapDesignTypeInterface

/**
 * 地図デザイン。
 *
 * ## この SDK は「素の地図」を持たない
 *
 * MapLibre や Mapbox と違い、Open Mobile Maps には既定の地図が無い。地図の中身は
 * すべてアプリが載せるレイヤであり、地図デザイン ＝ **どのタイルを一番下に敷くか** になる。
 *
 * 現状はラスタータイル（XYZ）のみを扱う。ベクタータイル
 * （[io.openmobilemaps.mapscore.map.layers.TiledVectorLayer]）にも対応できるが、
 * ラベルの描画に距離場フォントのアセットをモジュールへ同梱する必要があり、
 * デザインの差し替えとは別の作業になるのでここには含めていない。
 */
interface OpenMobileMapsMapDesignTypeInterface : MapDesignTypeInterface<String> {
    /** `{z}` `{x}` `{y}` を含むタイル URL。 */
    val tileUrlTemplate: String

    /** タイル画像の一辺（px）。OSM 系は 256、@2x 系は 512。 */
    val tileSize: Int
        get() = 256
}

data class OpenMobileMapsDesign(
    override val id: String,
    override val tileUrlTemplate: String,
    override val tileSize: Int = 256,
    override val attributionRules: List<AttributionRule> = emptyList(),
) : OpenMobileMapsMapDesignTypeInterface {
    override fun getValue(): String = "mapDesign_id=$id,tiles=$tileUrlTemplate"

    companion object {
        val OpenStreetMap =
            OpenMobileMapsDesign(
                id = "osm",
                tileUrlTemplate = "https://tile.openstreetmap.org/{z}/{x}/{y}.png",
            )

        val OpenStreetMapJapan =
            OpenMobileMapsDesign(
                id = "osm-japan",
                tileUrlTemplate = "https://tile.openstreetmap.jp/{z}/{x}/{y}.png",
            )

        val OpenTopoMap =
            OpenMobileMapsDesign(
                id = "opentopomap",
                tileUrlTemplate = "https://a.tile.opentopomap.org/{z}/{x}/{y}.png",
            )
    }
}
