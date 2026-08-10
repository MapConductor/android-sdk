package com.mapconductor.openmobilemaps

import io.openmobilemaps.mapscore.shared.map.LayerInterface
import io.openmobilemaps.mapscore.shared.map.MapInterface
import io.openmobilemaps.mapscore.shared.map.layers.icon.IconLayerInterface
import io.openmobilemaps.mapscore.shared.map.layers.line.LineLayerInterface
import io.openmobilemaps.mapscore.shared.map.layers.polygon.PolygonLayerInterface

/**
 * このドライバーが地図に載せるレイヤ一式と、その重ね順。
 *
 * ## なぜレイヤを 1 箇所で持つのか
 *
 * Open Mobile Maps は「オーバーレイ 1 つ = ネイティブオブジェクト 1 つ」ではなく
 * 「**種別ごとに 1 枚のレイヤ**があり、そこへ要素のリストを流し込む」形の SDK である
 * （MapLibre の GeoJSON ソースに近い）。レンダラが個別にレイヤを作ると重ね順が
 * 生成順に依存して不定になるので、ここでまとめて作り、索引を割り当てる。
 *
 * ## 重ね順（下から）
 *
 * ```
 * 地図デザイン → ラスターレイヤ・グラウンドイメージ → ポリゴン塗り → ポリゴン輪郭
 *   → 円の塗り → 円の輪郭 → ポリライン → マーカー
 * ```
 *
 * 他プロバイダのクリックのカスケード（marker → circle → groundImage → polyline →
 * polygon → map）と**上下が逆に見える**が、これは正しい。カスケードは「上にあるものから
 * 順に当たり判定する」ので、描画の重ね順を上から読むとカスケード順に一致する。
 *
 * ## この SDK のレイヤ API には罠が 2 つある
 *
 * **1. `insertLayerAt(layer, index)` は挿入ではなく上書きである。**
 * `MapScene` はレイヤを「索引 → レイヤ」の map で持っていて、`insertLayerAt` は
 * その索引に居たレイヤを `onRemoved()` して置き換える。地図デザインを索引 0 に
 * 入れると、索引 0 に居た**ポリゴン塗りレイヤが黙って外れる**。
 * 症状は「円は塗れるのにポリゴンだけ塗れない」で、例外もログも出ない。
 * だから索引は [DESIGN_INDEX] / [BELOW_INDEX_FIRST] / [FIXED_INDEX_FIRST] に
 * 分けて**衝突させない**。
 *
 * **2. `asLayerInterface()` は呼ぶたびに別のオブジェクトを返す。**
 * `insertLayerBelow(x, polygonFillLayer.asLayerInterface())` は「そんなレイヤは無い」で
 * GL スレッドから落ちる（`RuntimeException: MapScene does not contain below layer`）。
 * `removeLayer` も黙って何も外さない。載せたときの値を持っておいて、それを渡すこと。
 */
class OpenMobileMapsLayers {
    val polygonFillLayer: PolygonLayerInterface = PolygonLayerInterface.create()
    val polygonOutlineLayer: LineLayerInterface = LineLayerInterface.create()
    val circleFillLayer: PolygonLayerInterface = PolygonLayerInterface.create()
    val circleOutlineLayer: LineLayerInterface = LineLayerInterface.create()
    val polylineLayer: LineLayerInterface = LineLayerInterface.create()
    val iconLayer: IconLayerInterface = IconLayerInterface.create()

    /** 地図デザインのレイヤ。差し替え時に外すため、**載せた値そのもの**を持っておく。 */
    private var designLayer: LayerInterface? = null

    /** 地図デザインとオーバーレイ群の間に入っているレイヤ（ラスター / グラウンドイメージ）と、その索引。 */
    private val belowOverlayIndices = mutableMapOf<LayerInterface, Int>()

    /** 外したレイヤの索引。使い回さないと、タイルの張り替えを繰り返すうちに索引が尽きる。 */
    private val freeBelowIndices = sortedSetOf<Int>()
    private var nextBelowIndex = BELOW_INDEX_FIRST

    private val fixedLayers: List<LayerInterface> by lazy {
        listOf(
            polygonFillLayer.asLayerInterface(),
            polygonOutlineLayer.asLayerInterface(),
            circleFillLayer.asLayerInterface(),
            circleOutlineLayer.asLayerInterface(),
            polylineLayer.asLayerInterface(),
            iconLayer.asLayerInterface(),
        )
    }

    /**
     * 固定レイヤを地図へ載せる。
     *
     * ## クリックは SDK に取らせない
     *
     * どのレイヤも `setLayerClickable(false)` にする。当たり判定はコアの Manager が
     * 地理座標で行う（そうしないとプロバイダごとに結果が変わる）ので、SDK 側の
     * ヒットテストが先に食べてしまうと `clickable = false` の透過もカスケードの順序も
     * 効かなくなる。**ここを消すと「ポリゴンをタップしても下のマーカーに当たらない」
     * という形で壊れる。**
     */
    fun attachTo(map: MapInterface) {
        polygonFillLayer.setLayerClickable(false)
        circleFillLayer.setLayerClickable(false)
        polygonOutlineLayer.setLayerClickable(false)
        circleOutlineLayer.setLayerClickable(false)
        polylineLayer.setLayerClickable(false)
        iconLayer.setLayerClickable(false)

        fixedLayers.forEachIndexed { offset, layer ->
            map.insertLayerAt(layer, FIXED_INDEX_FIRST + offset)
        }
    }

    /** 地図デザインのレイヤを差し替える。 */
    fun setDesignLayer(
        map: MapInterface,
        layer: LayerInterface?,
    ) {
        designLayer?.let { map.removeLayer(it) }
        designLayer = layer
        layer ?: return
        map.insertLayerAt(layer, DESIGN_INDEX)
    }

    /**
     * ラスターレイヤ／グラウンドイメージを、オーバーレイ群より下・地図デザインより上へ入れる。
     *
     * @return 外すときに [remove] へ渡す値。**別途 `asLayerInterface()` を呼び直さないこと。**
     */
    fun insertBelowOverlays(
        map: MapInterface,
        layer: LayerInterface,
    ): LayerInterface {
        val index = freeBelowIndices.pollFirst() ?: nextBelowIndex++
        belowOverlayIndices[layer] = index
        map.insertLayerAt(layer, index)
        return layer
    }

    fun remove(
        map: MapInterface,
        layer: LayerInterface,
    ) {
        belowOverlayIndices.remove(layer)?.let { freeBelowIndices.add(it) }
        map.removeLayer(layer)
    }

    fun clearAll() {
        polygonFillLayer.clear()
        polygonOutlineLayer.clear()
        circleFillLayer.clear()
        circleOutlineLayer.clear()
        polylineLayer.clear()
        iconLayer.clear()
    }

    private companion object {
        /** 地図デザイン。一番下。 */
        const val DESIGN_INDEX = 0

        /** ラスターレイヤ・グラウンドイメージ。地図デザインとオーバーレイ群の間。 */
        const val BELOW_INDEX_FIRST = 1

        /** 固定のオーバーレイ 6 種。下の枠を使い切らないよう十分離す。 */
        const val FIXED_INDEX_FIRST = 1_000
    }
}
