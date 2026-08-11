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
 * ## 端末密度で補正してはいけない
 *
 * [tileSize] は**画像のピクセル数ではなく dp** である（`RasterLayerSource.UrlTemplate` の
 * 意味論。MapLibre の `tileSize` も Google のタイル定義も同じ）。したがってレベルの選択は
 * dp だけで決まり、端末密度は関係しない。
 *
 * 一度ここに `density` を掛けていて、**マーカータイルが 1 段深いレベルで選ばれ、
 * PostOffice ページのアイコンが巨大かつぼやける**という形で出た。密度を掛けると
 * 高密度端末だけ挙動が変わるので、他プロバイダと並べても気づきにくい。
 *
 * @param urlTemplate `{z}` `{x}` `{y}` を含む URL。
 * @param tileSize タイル 1 枚の一辺（**dp**）。OSM 系は 256、@2x 系は 512。
 * @param scheme [TileScheme.TMS] なら y を反転する。
 */
class WebMercatorTileLayerConfig(
    private val layerName: String,
    private val urlTemplate: String,
    private val tileSize: Int,
    private val minZoomLevel: Int = 0,
    private val maxZoomLevel: Int = 22,
    private val scheme: TileScheme = TileScheme.XYZ,
    private val numDrawPreviousLayers: Int = 2,
    private val maskTile: Boolean = false,
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
                        zoomScaleForLevel(level, tileSize),
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
            1.0f,
            numDrawPreviousLayers,
            0,
            false,
            maskTile,
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
 * 256dp タイルなら統一ズーム [level] の縮尺そのもの（＝ Google と同じレベルが選ばれる）。
 * 512dp タイルは 1 枚で 2 枚ぶんを覆うので 1 段浅いレベルでよい。
 *
 * **端末密度を掛けないこと。** [tileSize] は画像のピクセル数ではなく dp なので、
 * レベルの選択に密度は関係しない。
 *
 * SDK に触らない純粋な計算にしてあるのは、素の JVM テストで検証できるようにするため
 * （[Tiled2dMapZoomLevelInfo] の生成は [CoordinateSystemIdentifiers] を通るので JNI に入る）。
 */
internal fun zoomScaleForLevel(
    level: Int,
    tileSize: Int,
): Double = ZoomAltitudeConverter.SCALE_AT_ZOOM_0 * UNIFIED_TILE_SIZE / tileSize / 2.0.pow(level)
