package com.mapconductor.example.pages.polygon.boston

import androidx.compose.ui.graphics.Color
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.spherical.Spherical
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import android.content.Context

/**
 * The polygons the Boston page draws.
 *
 * - [DOWNTOWN]: Overture building footprints of downtown in three nested areas.
 * - [CITY_BUILDINGS] / [CITY_PARCELS]: the whole city: Overture buildings, and
 *   Analyze Boston Parcels 2025 joined with the FY2026 Property Assessment land use.
 *
 * All three are bundled (assets/boston). The React sample's
 * `examples/basic/tools/boston/` builds them from the official sources (see its
 * README). The files carry no ids: this is a polygon demo, and a polygon's id on
 * the map is its index in the file.
 *
 * The binary layout is the same as the React sample's (bostonData.ts), MCPG v2:
 *   'MCPG' u32 version=2 u32 count u8 fields u8 digits   (little-endian)
 *     fields: 1 = height, 2 = land use, 4 = area, 8 = name, 16 = road class (what the file carries),
 *             32 = lines: the shapes are polylines, not polygons (the roads file, see BostonRoadsData)
 *     digits: coordinates are integers of 10^-digits degree (buildings 6, parcels 5)
 *   per polygon, only the fields present, in this order:
 *     height    varint, 0 = unknown, else the height in 0.1 m plus 1
 *     land use  u8, the index in [LandUse] (family … unknown)
 *     area      u8, the smallest downtown area (0, 1 or 2) the building fits in
 *     name      varint byte length (0 = none), UTF-8
 *     road class u8, the index in RoadClass (highway … path)
 *     varint ringCount, per ring: varint n, n x (svarint dLat, svarint dLng)
 *   varint is LEB128 (7 bits at a time, low first); svarint is a zigzag varint.
 *   Coordinates are integers of 10^-digits degree, each the difference from the
 *   previous vertex across the whole file. A ring's closing vertex (the same as its first)
 *   is not stored: n counts the others, and the reader closes the ring again.
 *   With the lines bit a "ring" is an open polyline: every vertex is stored and
 *   the reader does not close it.
 */
enum class Dataset(
    val label: String,
    /** The file in assets. */
    val file: String,
    val attribution: String,
) {
    DOWNTOWN("Downtown", "boston/boston-downtown.bin", OVERTURE_ATTRIBUTION),
    CITY_BUILDINGS("City buildings", "boston/boston-city-buildings.bin", OVERTURE_ATTRIBUTION),
    CITY_PARCELS(
        "City parcels",
        "boston/boston-city-parcels.bin",
        "City of Boston, Parcels 2025 and FY2026 Property Assessment (Analyze Boston) (ODC PDDL)",
    ),
}

private const val OVERTURE_ATTRIBUTION =
    "© OpenStreetMap contributors, Esri Community Maps contributors, USGS, Microsoft — via Overture Maps Foundation (ODbL 1.0)"

/**
 * Land use of a parcel (Property Assessment `LU`, grouped) and how it is drawn; the same as the React sample.
 * The order is the file's: a land use is stored as its index here.
 */
enum class LandUse(
    val key: String,
    val label: String,
    val fill: Color,
) {
    FAMILY("family", "1–3 family", Color(0x8CFACC15)),
    APARTMENT("apartment", "Apartments", Color(0x8CF97316)),
    CONDO("condo", "Condominium", Color(0x80EC4899)),
    MIXED("mixed", "Residential + commercial", Color(0x80A855F7)),
    COMMERCIAL("commercial", "Commercial", Color(0x8CEF4444)),
    INDUSTRIAL("industrial", "Industrial", Color(0x9964748B)),
    EXEMPT("exempt", "Exempt (public, institutions)", Color(0x803B82F6)),
    VACANT("vacant", "Vacant land", Color(0x7322C55E)),
    UNKNOWN("unknown", "No assessment (roads, islands)", Color(0x5994A3B8)),
}

/** Where loading has got to, for the overlay. */
sealed class LoadPhase {
    data class Read(
        val loaded: Long,
        val total: Long?,
    ) : LoadPhase()

    data object Decode : LoadPhase()

    data object Build : LoadPhase()

    data object Draw : LoadPhase()
}

class BostonData(
    val attribution: String,
    val buildings: List<Building>,
)

fun loadBostonData(
    context: Context,
    dataset: Dataset,
    onProgress: (LoadPhase) -> Unit = {},
): BostonData {
    // openFd works only on an uncompressed asset; without it the progress has no total.
    val length = runCatching { context.assets.openFd(dataset.file).use { it.length } }.getOrNull()
    val bytes = readWithProgress(context.assets.open(dataset.file), length, onProgress)
    onProgress(LoadPhase.Decode)
    return BostonData(dataset.attribution, decodeMcpg(bytes, dataset.name.lowercase()))
}

private fun readWithProgress(
    input: java.io.InputStream,
    total: Long?,
    onProgress: (LoadPhase) -> Unit,
): ByteArray =
    input.use { stream ->
        val out = ByteArrayOutputStream(total?.toInt() ?: 1 shl 20)
        val chunk = ByteArray(256 * 1024)
        var loaded = 0L
        onProgress(LoadPhase.Read(loaded, total))
        while (true) {
            val n = stream.read(chunk)
            if (n < 0) break
            out.write(chunk, 0, n)
            loaded += n
            onProgress(LoadPhase.Read(loaded, total))
        }
        out.toByteArray()
    }

private const val FIELD_HEIGHT = 1
private const val FIELD_USE = 2
private const val FIELD_AREA = 4
private const val FIELD_NAME = 8
private const val FIELD_CLASS = 16
private const val FIELD_LINES = 32

/** Decodes an MCPG v2 file (see the top of this file); shape `i` gets the id "<idPrefix>-<i>". */
internal fun decodeMcpg(
    bytes: ByteArray,
    idPrefix: String,
): List<Building> {
    val header = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
    val magic = String(bytes, 0, 4, Charsets.US_ASCII)
    require(magic == "MCPG" && header.getInt(4) == 2) { "not an MCPG v2 file" }
    val count = header.getInt(8)
    val fields = bytes[12].toInt() and 0xFF
    val scale = Math.pow(10.0, (bytes[13].toInt() and 0xFF).toDouble())
    var offset = 14
    val lines = (fields and FIELD_LINES) != 0

    fun varint(): Int {
        var value = 0
        var shift = 0
        while (true) {
            val b = bytes[offset++].toInt() and 0xFF
            value = value or ((b and 0x7F) shl shift)
            if (b < 0x80) return value
            shift += 7
        }
    }

    fun svarint(): Int {
        val v = varint()
        return (v ushr 1) xor -(v and 1)
    }

    var lat = 0
    var lng = 0
    return List(count) { i ->
        var height: Double? = null
        var use: LandUse? = null
        var level = 0
        var name: String? = null
        var roadClass: Int? = null
        if ((fields and FIELD_HEIGHT) != 0) height = varint().takeIf { it > 0 }?.let { (it - 1) / 10.0 }
        if ((fields and FIELD_USE) != 0) use = LandUse.entries[bytes[offset++].toInt() and 0xFF]
        if ((fields and FIELD_AREA) != 0) level = bytes[offset++].toInt() and 0xFF
        if ((fields and FIELD_NAME) != 0) {
            val length = varint()
            if (length > 0) name = String(bytes, offset, length, Charsets.UTF_8)
            offset += length
        }
        if ((fields and FIELD_CLASS) != 0) roadClass = bytes[offset++].toInt() and 0xFF
        val rings =
            List(varint()) {
                val n = varint()
                // The closing vertex is not stored; it is the first one again. A line is open.
                val ring = DoubleArray((if (lines) n else n + 1) * 2)
                for (v in 0 until n) {
                    lat += svarint()
                    lng += svarint()
                    ring[v * 2] = lng / scale
                    ring[v * 2 + 1] = lat / scale
                }
                if (!lines) {
                    ring[n * 2] = ring[0]
                    ring[n * 2 + 1] = ring[1]
                }
                ring
            }
        Building(id = "$idPrefix-$i", level = level, height = height, name = name, rings = rings, use = use, roadClass = roadClass)
    }
}

/** The area of [building] in square metres: its outline less its holes. */
fun areaSquareMetres(building: Building): Double {
    val rings = building.rings.map { ring -> List(ring.size / 2) { GeoPoint(ring[it * 2 + 1], ring[it * 2]) } }
    return Spherical.computeArea(rings.first()) - rings.drop(1).sumOf { Spherical.computeArea(it) }
}
