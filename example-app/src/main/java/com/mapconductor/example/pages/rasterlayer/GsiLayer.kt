package com.mapconductor.example.pages.rasterlayer

import com.mapconductor.core.map.AttributionRule
import com.mapconductor.core.raster.RasterLayerSource

data class GsiLayer(
    val id: String,
    val displayName: String,
    val source: RasterLayerSource,
)

object DefaultGsiLayers {
    val nasa =
        GsiLayer(
            id = "nasa",
            displayName = "Relief map",
            source =
                RasterLayerSource.UrlTemplate(
                    template =
                        "https://gibs.earthdata.nasa.gov/wmts/epsg3857/best/" +
                            "MODIS_Terra_CorrectedReflectance_TrueColor/default/2024-01-01/" +
                            "GoogleMapsCompatible_Level9/{z}/{y}/{x}.jpg",
                    tileSize = 256,
                    minZoom = 5,
                    maxZoom = 15,
                    attributionRules =
                        listOf(
                            AttributionRule(
                                attribution = "NASA Global Imagery Browse Services（GIBS / EOSDIS）",
                            ),
                        ),
                ),
        )

    val standard =
        GsiLayer(
            id = "standard",
            displayName = "Standard map (電子国土基本図)",
            source =
                RasterLayerSource.UrlTemplate(
                    template = "https://cyberjapandata.gsi.go.jp/xyz/std/{z}/{x}/{y}.png",
                    tileSize = 256,
                    minZoom = 5,
                    maxZoom = 18,
                    attributionRules =
                        listOf(
                            AttributionRule(
                                attribution =
                                    "<a href=\"https://maps.gsi.go.jp/development/ichiran.html\">" +
                                        "地理院タイル</a>",
                            ),
                            AttributionRule(
                                attribution =
                                    "The bathymetric contours are derived from those contained within the " +
                                        "GEBCO Digital Atlas, published by the BODC on behalf of IOC and IHO " +
                                        "(2003) (<a href=\"https://www.gebco.net\">https://www.gebco.net</a>)",
                                minZoom = 5,
                                maxZoom = 8,
                            ),
                            AttributionRule(
                                attribution = "海上保安庁許可第292502号（水路業務法第25条に基づく類似刊行物）",
                                minZoom = 5,
                                maxZoom = 8,
                            ),
                            AttributionRule(
                                attribution =
                                    "Shoreline data is derived from: United States. National Imagery and " +
                                        "Mapping Agency. &quot;Vector Map Level 0 (VMAP0).&quot; Bethesda, MD: " +
                                        "Denver, CO: The Agency; USGS Information Services, 1997.",
                                minZoom = 5,
                                maxZoom = 8,
                            ),
                        ),
                ),
        )

    val all = listOf(nasa, standard)
}
