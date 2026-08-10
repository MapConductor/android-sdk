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
 * 生成順に依存して不定になるので、ここでまとめて作り、[attachTo] で順に載せる。
 *
 * ## 重ね順（下から）
 *
 * ```
 * 地図デザイン → ラスターレイヤ → グラウンドイメージ → ポリゴン塗り → ポリゴン輪郭
 *   → 円の塗り → 円の輪郭 → ポリライン → マーカー
 * ```
 *
 * 他プロバイダのクリックのカスケード（marker → circle → groundImage → polyline →
 * polygon → map）と**上下が逆に見える**が、これは正しい。カスケードは「上にあるものから
 * 順に当たり判定する」ので、描画の重ね順を上から読むとカスケード順に一致する。
 */
class OpenMobileMapsLayers {
    val polygonFillLayer: PolygonLayerInterface = PolygonLayerInterface.create()
    val polygonOutlineLayer: LineLayerInterface = LineLayerInterface.create()
    val circleFillLayer: PolygonLayerInterface = PolygonLayerInterface.create()
    val circleOutlineLayer: LineLayerInterface = LineLayerInterface.create()
    val polylineLayer: LineLayerInterface = LineLayerInterface.create()
    val iconLayer: IconLayerInterface = IconLayerInterface.create()

    /**
     * 地図デザインのレイヤ。差し替えのために参照を持っておく。
     *
     * これより下には何も入らない（[insertBelowOverlays] は必ずこれの上へ入れる）。
     */
    private var designLayer: LayerInterface? = null

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

        fixedLayers.forEach { map.addLayer(it) }
    }

    /** 地図デザインのレイヤを差し替える。 */
    fun setDesignLayer(
        map: MapInterface,
        layer: LayerInterface?,
    ) {
        designLayer?.let { map.removeLayer(it) }
        designLayer = layer
        layer ?: return
        map.insertLayerAt(layer, 0)
    }

    /**
     * ラスターレイヤ／グラウンドイメージを、オーバーレイ群より下・地図デザインより上へ入れる。
     */
    fun insertBelowOverlays(
        map: MapInterface,
        layer: LayerInterface,
    ) {
        map.insertLayerBelow(layer, polygonFillLayer.asLayerInterface())
    }

    fun remove(
        map: MapInterface,
        layer: LayerInterface,
    ) {
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
}
