package com.mapconductor.core.geocell

import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.core.projection.ProjectionInterface

interface HexGeocellInterface {
    val projection: ProjectionInterface
    val baseHexSideLength: Int

    fun latLngToHexCoord(
        position: GeoPointInterface,
        zoom: Double,
    ): HexCoord

    fun latLngToHexCell(
        position: GeoPointInterface,
        zoom: Double,
    ): HexCell

    fun hexToLatLngCenter(
        coord: HexCoord,
        latHint: Double,
        zoom: Double,
    ): GeoPointInterface

    fun hexToCellId(
        coord: HexCoord,
        zoom: Double,
    ): String

    fun hexToPolygonLatLng(
        coord: HexCoord,
        latHint: Double,
        zoom: Double,
    ): List<GeoPointInterface>

    fun enclosingCellOf(
        points: List<MarkerState>,
        zoom: Double,
    ): HexCell

    fun hexCellsForPointsWithId(
        points: List<MarkerState>,
        zoom: Double,
    ): Set<IdentifiedHexCell>

    fun hexDistance(
        a: HexCoord,
        b: HexCoord,
    ): Int

    fun hexRange(
        center: HexCoord,
        radius: Int,
    ): List<HexCoord>
}
