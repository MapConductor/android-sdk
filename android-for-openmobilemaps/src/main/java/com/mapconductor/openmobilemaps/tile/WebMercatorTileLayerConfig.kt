package com.mapconductor.openmobilemaps.tile

import com.mapconductor.core.raster.TileScheme
import com.mapconductor.openmobilemaps.zoom.ZoomAltitudeConverter
import io.openmobilemaps.mapscore.shared.map.coordinates.Coord
import io.openmobilemaps.mapscore.shared.map.coordinates.CoordinateSystemIdentifiers
import io.openmobilemaps.mapscore.shared.map.coordinates.RectCoord
import io.openmobilemaps.mapscore.shared.map.layers.tiled.Tiled2dMapLayerConfig
import io.openmobilemaps.mapscore.shared.map.layers.tiled.Tiled2dMapVectorSettings
import io.openmobilemaps.mapscore.shared.map.layers.tiled.Tiled2dMapZoomInfo
import io.openmobilemaps.mapscore.shared.map.layers.tiled.Tiled2dMapZoomLevelInfo
import kotlin.math.pow

/**
 * XYZ / TMS のラスタータイル 1 組ぶんの設定。
 *
 * ## SDK 同梱の [io.openmobilemaps.mapscore.shared.map.layers.tiled.DefaultTiled2dMapLayerConfigs] を使わない理由
 *
 * 同梱の web メルカトル設定はズームレベルの縮尺が SDK 独自の基準（レベル 0 で
 * 1:500'000'000）で刻まれている。これは MapConductor の統一ズームの基準
 * （[ZoomAltitudeConverter.SCALE_AT_ZOOM_0] = 1:986'097'222）と約 2 倍ずれるので、
 * そのまま使うと**統一ズーム Z のときに 1 段低いレベルのタイルが選ばれ、常にぼやける**。
 * 自前の設定にして、レベル L の縮尺を統一ズーム L と厳密に一致させる。
 *
 * ## 端末密度ぶんはここで足す
 *
 * 統一ズーム Z のとき、Google 基準では 256dip 幅のタイルが 1 枚ぶんになる。物理ピクセルでは
 * `256 x density` なので、[tileSize] の画像を 1:1 で貼るには
 * `density x 256 / tileSize` ぶん深いレベルを取りに行く必要がある。その係数を縮尺に掛ける
 * （512px タイル・density 2.0 なら等倍で、Z と同じレベルが選ばれる）。
 *
 * @param urlTemplate `{z}` `{x}` `{y}` を含む URL。
 * @param scheme [TileScheme.TMS] なら y を反転する。
 * @param density `Resources.getDisplayMetrics().density`。
 */
class WebMercatorTileLayerConfig(
    private val layerName: String,
    private val urlTemplate: String,
    private val tileSize: Int,
    private val density: Float,
    private val minZoomLevel: Int = 0,
    private val maxZoomLevel: Int = 22,
    private val scheme: TileScheme = TileScheme.XYZ,
) : Tiled2dMapLayerConfig() {
    override fun getCoordinateSystemIdentifier(): Int = CoordinateSystemIdentifiers.EPSG3857()

    override fun getLayerName(): String = layerName

    override fun getTileUrl(
        x: Int,
        y: Int,
        t: Int,
        zoom: Int,
    ): String {
        val resolvedY = if (scheme == TileScheme.TMS) (1 shl zoom) - 1 - y else y
        return urlTemplate
            .replace("{z}", zoom.toString())
            .replace("{x}", x.toString())
            .replace("{y}", resolvedY.toString())
    }

    override fun getZoomLevelInfos(): ArrayList<Tiled2dMapZoomLevelInfo> =
        ArrayList<Tiled2dMapZoomLevelInfo>().also { out ->
            for (level in minZoomLevel..maxZoomLevel) {
                val tilesPerAxis = 1 shl level
                out.add(
                    Tiled2dMapZoomLevelInfo(
                        zoomScaleForLevel(level, tileSize, density),
                        (WORLD_WIDTH_METERS / tilesPerAxis).toFloat(),
                        tilesPerAxis,
                        tilesPerAxis,
                        1,
                        level,
                        webMercatorBounds(),
                    ),
                )
            }
        }

    override fun getVirtualZoomLevelInfos(): ArrayList<Tiled2dMapZoomLevelInfo> = ArrayList()

    override fun getZoomInfo(): Tiled2dMapZoomInfo =
        Tiled2dMapZoomInfo(
            // 縮尺はすでに端末密度ぶんを織り込んであるので、SDK 側では調整させない。
            1.0f,
            2,
            0,
            false,
            false,
            true,
            true,
        )

    override fun getVectorSettings(): Tiled2dMapVectorSettings? = null

    override fun getBounds(): RectCoord? = webMercatorBounds()

    private companion object {
        const val WORLD_WIDTH_METERS = 40_075_016.685_578_49
        const val HALF_WORLD = WORLD_WIDTH_METERS / 2.0

        fun webMercatorBounds(): RectCoord =
            RectCoord(
                Coord(CoordinateSystemIdentifiers.EPSG3857(), -HALF_WORLD, HALF_WORLD, 0.0),
                Coord(CoordinateSystemIdentifiers.EPSG3857(), HALF_WORLD, -HALF_WORLD, 0.0),
            )
    }
}

/** 統一ズームが基準にしているタイルの一辺（Google 準拠の 256dip）。 */
private const val UNIFIED_TILE_SIZE = 256

/**
 * タイルレベル [level] を選ばせたい縮尺。
 *
 * `density = 1` かつ 256px タイルなら統一ズーム [level] の縮尺そのもの
 * （＝ Google と同じレベルが選ばれる）。端末密度とタイルの大きさぶんだけ
 * 深い／浅いレベルへずらす。
 *
 * SDK に触らない純粋な計算にしてあるのは、素の JVM テストで検証できるようにするため
 * （[Tiled2dMapZoomLevelInfo] の生成は [CoordinateSystemIdentifiers] を通るので JNI に入る）。
 */
internal fun zoomScaleForLevel(
    level: Int,
    tileSize: Int,
    density: Float,
): Double = ZoomAltitudeConverter.SCALE_AT_ZOOM_0 * density * UNIFIED_TILE_SIZE / tileSize / 2.0.pow(level)
