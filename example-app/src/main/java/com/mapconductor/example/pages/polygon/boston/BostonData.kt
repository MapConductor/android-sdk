package com.mapconductor.example.pages.polygon.boston

import androidx.compose.ui.graphics.Color
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.spherical.Spherical
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import android.content.Context

/**
 * The polygons the Boston page draws.
 *
 * - [DOWNTOWN]: Overture building footprints of downtown in three nested areas,
 *   bundled (assets).
 * - [CITY_BUILDINGS] / [CITY_PARCELS]: the whole city. Those files are too large
 *   for the APK; push them to the app's external files directory:
 *   `adb push boston-city-parcels.bin /sdcard/Android/data/com.mapconductor.example/files/boston/`
 *
 * They are built from the official sources (Overture buildings, Analyze Boston
 * Parcels 2025 joined with the FY2026 Property Assessment land use) and are not
 * committed. The files carry no ids: this is a polygon demo, and a polygon's id
 * on the map is its index in the file.
 *
 * The binary layout is the same as the React sample's (bostonData.ts), MCPG v2:
 *   'MCPG' u32 version=2 u32 count u8 fields   (little-endian)
 *     fields: 1 = height, 2 = land use, 4 = area, 8 = name (what the file carries)
 *   per polygon, only the fields present, in this order:
 *     height    varint, 0 = unknown, else the height in 0.1 m plus 1
 *     land use  u8, the index in [LandUse] (family … unknown)
 *     area      u8, the smallest downtown area (0, 1 or 2) the building fits in
 *     name      varint byte length (0 = none), UTF-8
 *     varint ringCount, per ring: varint n, n x (svarint dLat, svarint dLng)
 *   varint is LEB128 (7 bits at a time, low first); svarint is a zigzag varint.
 *   Coordinates are 1e-6 degree integers, each the difference from the previous
 *   vertex across the whole file. A ring's closing vertex (the same as its first)
 *   is not stored: n counts the others, and the reader closes the ring again.
 */
enum class Dataset(
    val label: String,
    val file: String,
    /** Whether [file] is bundled (assets) or pushed to the external files directory. */
    val bundled: Boolean,
    val attribution: String,
) {
    DOWNTOWN("Downtown", "boston/boston-downtown.bin", true, OVERTURE_ATTRIBUTION),
    CITY_BUILDINGS("City buildings", "boston/boston-city-buildings.bin", false, OVERTURE_ATTRIBUTION),
    CITY_PARCELS(
        "City parcels",
        "boston/boston-city-parcels.bin",
        false,
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
    val bytes =
        if (dataset.bundled) {
            val length = runCatching { context.assets.openFd(dataset.file).use { it.length } }.getOrNull()
            readWithProgress(context.assets.open(dataset.file), length, onProgress)
        } else {
            val path = File(context.getExternalFilesDir(null), dataset.file)
            require(path.exists()) { "Missing ${path.path}; push it with adb (see BostonData.kt)" }
            readWithProgress(path.inputStream(), path.length(), onProgress)
        }
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

/** Decodes an MCPG v2 file (see the top of this file); polygon `i` gets the id "<idPrefix>-<i>". */
internal fun decodeMcpg(
    bytes: ByteArray,
    idPrefix: String,
): List<Building> {
    val header = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
    val magic = String(bytes, 0, 4, Charsets.US_ASCII)
    require(magic == "MCPG" && header.getInt(4) == 2) { "not an MCPG v2 file" }
    val count = header.getInt(8)
    val fields = bytes[12].toInt() and 0xFF
    var offset = 13

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
        if ((fields and FIELD_HEIGHT) != 0) height = varint().takeIf { it > 0 }?.let { (it - 1) / 10.0 }
        if ((fields and FIELD_USE) != 0) use = LandUse.entries[bytes[offset++].toInt() and 0xFF]
        if ((fields and FIELD_AREA) != 0) level = bytes[offset++].toInt() and 0xFF
        if ((fields and FIELD_NAME) != 0) {
            val length = varint()
            if (length > 0) name = String(bytes, offset, length, Charsets.UTF_8)
            offset += length
        }
        val rings =
            List(varint()) {
                val n = varint()
                // The closing vertex is not stored; it is the first one again.
                val ring = DoubleArray((n + 1) * 2)
                for (v in 0 until n) {
                    lat += svarint()
                    lng += svarint()
                    ring[v * 2] = lng / 1e6
                    ring[v * 2 + 1] = lat / 1e6
                }
                ring[n * 2] = ring[0]
                ring[n * 2 + 1] = ring[1]
                ring
            }
        Building(id = "$idPrefix-$i", level = level, height = height, name = name, rings = rings, use = use)
    }
}

/** The area of [building] in square metres: its outline less its holes. */
fun areaSquareMetres(building: Building): Double {
    val rings = building.rings.map { ring -> List(ring.size / 2) { GeoPoint(ring[it * 2 + 1], ring[it * 2]) } }
    return Spherical.computeArea(rings.first()) - rings.drop(1).sumOf { Spherical.computeArea(it) }
}
