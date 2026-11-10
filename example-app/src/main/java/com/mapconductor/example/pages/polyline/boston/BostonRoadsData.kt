package com.mapconductor.example.pages.polyline.boston

import androidx.compose.ui.graphics.Color
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.spherical.Spherical
import com.mapconductor.example.pages.polygon.boston.LoadPhase
import com.mapconductor.example.pages.polygon.boston.decodeMcpg
import android.content.Context

/**
 * The roads the Boston roads page draws: every road of the city, from Overture
 * Maps `transportation/segment` (road subtype), cut at the city boundary. 54,645
 * polylines, 234,765 points, about 1 MB.
 *
 * Bundled as assets/boston/boston-city-roads.bin. The React sample's
 * `examples/basic/tools/boston/` builds it (`pack.py roads`, see its README for
 * the source, licence and why not Analyze Boston's street centerlines). The file
 * carries no ids: this is a polyline demo, and a road's id on the map is its
 * index in the file.
 *
 * The format is the polygons' MCPG v2 (see BostonData.kt) with two more header
 * bits: 16, the road class (u8, after the name), and 32, lines: each record's
 * one "ring" is an open polyline, every vertex stored, never closed by the
 * reader. Coordinates are 1e-5 degree; points almost on a straight line (0.5 m)
 * are left out, the ends kept.
 */
const val ROADS_FILE = "boston/boston-city-roads.bin"

const val ROADS_ATTRIBUTION =
    "© OpenStreetMap contributors, TomTom — via Overture Maps Foundation (ODbL 1.0)"

/**
 * A road's class (the file's index, grouped from Overture's segment classes) and
 * how the page draws it: bigger roads wider, brighter and on top. The same table
 * on the web and iOS.
 */
enum class RoadClass(
    val label: String,
    val color: Color,
    /** dp. */
    val width: Float,
    val zIndex: Int,
) {
    HIGHWAY("Highway", Color(0xFFE8590C), 5f, 6),
    PRIMARY("Primary", Color(0xFFF08C00), 4f, 5),
    SECONDARY("Secondary", Color(0xFFFAB005), 3.5f, 4),
    TERTIARY("Tertiary", Color(0xFFFFD43B), 3f, 3),
    LOCAL("Local", Color(0xFF868E96), 2f, 2),
    SERVICE("Service", Color(0xFFADB5BD), 1.5f, 1),
    PATH("Path", Color(0xFF74B816), 1f, 0),
}

class Road(
    val id: String,
    val roadClass: RoadClass,
    val name: String?,
    /** `[lng0, lat0, lng1, lat1, ...]`, open. */
    val path: DoubleArray,
) {
    val points: List<GeoPoint> get() = List(path.size / 2) { GeoPoint(path[it * 2 + 1], path[it * 2]) }
}

/** Decodes the roads file: MCPG v2 with the road class and lines bits; road `i` gets the id "road-<i>". */
fun decodeRoads(bytes: ByteArray): List<Road> =
    decodeMcpg(bytes, "road").map { shape ->
        Road(
            id = shape.id,
            roadClass = RoadClass.entries[shape.roadClass ?: RoadClass.LOCAL.ordinal],
            name = shape.name,
            path = shape.rings.single(),
        )
    }

fun loadBostonRoads(
    context: Context,
    onProgress: (LoadPhase) -> Unit = {},
): List<Road> {
    val length = runCatching { context.assets.openFd(ROADS_FILE).use { it.length } }.getOrNull()
    onProgress(LoadPhase.Read(0, length))
    val bytes = context.assets.open(ROADS_FILE).use { it.readBytes() }
    onProgress(LoadPhase.Read(bytes.size.toLong(), length))
    onProgress(LoadPhase.Decode)
    return decodeRoads(bytes)
}

/** The road's length in metres: the geodesic length of its points. */
fun lengthMetres(road: Road): Double = Spherical.computeLength(road.points)
