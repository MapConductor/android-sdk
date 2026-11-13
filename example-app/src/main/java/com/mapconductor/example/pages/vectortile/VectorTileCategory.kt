package com.mapconductor.example.pages.vectortile

import androidx.compose.ui.graphics.Color
import com.mapconductor.vectorstyle.LayerRole

/**
 * ベクタータイルのサンプルで触れる「地図の上のもの」と、それが指すスタイルの
 * 中の role。
 *
 * 1 つに role が複数あるのは、**つまみは地図の上のもの、role はスタイルの中の
 * もの**だから。人が「陸」と呼ぶのは地図が乗っている背景（[LayerRole.BACKGROUND]）
 * ＋その上の土地利用（[LayerRole.LAND]）＋敷地（[LayerRole.LANDUSE]）で、真ん中
 * だけ消したり塗ったりしても地面の色は変わらない。「水域」も面と川筋の両方。
 *
 * レイヤー id ではなく role で指すので、別のスタイルに差し替えても同じ言葉が
 * 通る。ios-sdk の `VectorTileMapPage` と react-sdk の `VectorTileLayerPage`
 * も同じ並び・同じ既定色。
 */
data class VectorTileCategory(
    val label: String,
    val key: String,
    val roles: List<String>,
    /**
     * 色を選べるか。
     *
     * ラベルだけ false。tint は当たったレイヤーを全部 1 色へ寄せるので、文字に
     * やると公園の緑も川の青も、ページを開いただけで潰れる。消すほうは要るので
     * スイッチだけ出す。
     */
    val tinted: Boolean = true,
) {
    companion object {
        val all =
            listOf(
                VectorTileCategory("Water", "water", listOf(LayerRole.WATER, LayerRole.WATERWAY)),
                VectorTileCategory(
                    "Land",
                    "land",
                    listOf(LayerRole.BACKGROUND, LayerRole.LAND, LayerRole.LANDUSE),
                ),
                // 縁取りは road でもあるので、これで一緒に拾える。
                VectorTileCategory("Roads", "road", listOf(LayerRole.ROAD)),
                VectorTileCategory("Buildings", "building", listOf(LayerRole.BUILDING)),
                // 地名・POI・路線記号・一方通行の矢印まで、記号レイヤーすべて。
                // ラスタ経路ではこれが透明なオーバーレイなので、外すとその半分が
                // 空になる。
                VectorTileCategory("Labels", "label", listOf(LayerRole.LABEL), tinted = false),
            )

        val defaultPalette: Map<String, Color> =
            mapOf(
                "water" to Color(0xFF3A7CA5),
                "land" to Color(0xFFF2EFE6),
                "road" to Color(0xFFFFFFFF),
                "building" to Color(0xFFD9D0C9),
            )

        /** 見本帳。サンプルに要るだけの色数。 */
        val swatches =
            listOf(
                Color(0xFFFFFFFF),
                Color(0xFFD9D0C9),
                Color(0xFF9E9E9E),
                Color(0xFF424242),
                Color(0xFF000000),
                Color(0xFF3A7CA5),
                Color(0xFF2E7D32),
                Color(0xFFF2EFE6),
                Color(0xFFE65100),
                Color(0xFF8E24AA),
            )
    }
}
