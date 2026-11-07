package com.mapconductor.example.pages.polygon.boston

import androidx.compose.ui.graphics.Color
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import android.content.Context

/**
 * The polygons the Boston page draws.
 *
 * - [DOWNTOWN]: the bundled Overture extract (assets, JSON), three nested areas.
 * - [CITY_BUILDINGS] / [CITY_PARCELS]: the whole city. Those files are too large
 *   for the APK; push them to the app's external files directory:
 *   `adb push boston-parcels.bin /sdcard/Android/data/com.mapconductor.example/files/boston/`
 *
 * They are built from the official sources (Overture buildings, Analyze Boston
 * Parcels 2025 joined with the FY2026 Property Assessment land use) and are not
 * committed. A parcel's label is "<GIS_ID>|<land use>".
 *
 * The binary layout is the same as the React sample's (bostonData.ts):
 *   'MCPG' u32 version=1 u32 count
 *   per polygon: u8 labelLen, label (UTF-8), u16 height (0.1 m, 0xFFFF unknown),
 *                u16 ringCount, per ring: u32 n, n x (i32 dLat, i32 dLng)
 *   Coordinates are 1e-6 degree integers, each the difference from the previous
 *   vertex across the whole file. Little-endian.
 */
enum class Dataset(
    val label: String,
    val file: String?,
    val attribution: String,
) {
    DOWNTOWN("Downtown", null, ""),
    CITY_BUILDINGS(
        "City buildings",
        "boston/boston-buildings-city.bin",
        "© OpenStreetMap contributors, Esri Community Maps contributors, USGS, Microsoft — via Overture Maps Foundation (ODbL 1.0)",
    ),
    CITY_PARCELS(
        "City parcels",
        "boston/boston-parcels.bin",
        "City of Boston, Parcels 2025 and FY2026 Property Assessment (Analyze Boston) (ODC PDDL)",
    ),
}

/** Land use of a parcel (Property Assessment `LU`, grouped) and how it is drawn; the same as the React sample. */
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
    ;

    companion object {
        fun of(key: String?): LandUse = entries.firstOrNull { it.key == key } ?: UNKNOWN
    }
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

private const val DOWNTOWN_ASSET = "boston/boston-buildings.json"

fun loadBostonData(
    context: Context,
    dataset: Dataset,
    onProgress: (LoadPhase) -> Unit = {},
): BostonData =
    when (val file = dataset.file) {
        null -> loadDowntown(context, onProgress)
        else -> {
            val path = File(context.getExternalFilesDir(null), file)
            require(path.exists()) { "Missing ${path.path}; push it with adb (see BostonData.kt)" }
            val bytes = readWithProgress(path.inputStream(), path.length(), onProgress)
            onProgress(LoadPhase.Decode)
            BostonData(dataset.attribution, decode(bytes, dataset))
        }
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

private fun loadDowntown(
    context: Context,
    onProgress: (LoadPhase) -> Unit,
): BostonData {
    val length = runCatching { context.assets.openFd(DOWNTOWN_ASSET).use { it.length } }.getOrNull()
    val bytes = readWithProgress(context.assets.open(DOWNTOWN_ASSET), length, onProgress)
    onProgress(LoadPhase.Decode)
    val json = JSONObject(String(bytes, Charsets.UTF_8))
    val buildings = json.getJSONArray("buildings")
    return BostonData(
        attribution = "${json.getString("attribution")} (${json.getString("license")})",
        buildings =
            List(buildings.length()) { i ->
                val building = buildings.getJSONObject(i)
                val rings = building.getJSONArray("rings")
                Building(
                    id = building.getString("id"),
                    level = building.getInt("l"),
                    height = if (building.isNull("h")) null else building.getDouble("h"),
                    name = building.optString("n").takeIf { it.isNotEmpty() },
                    rings =
                        List(rings.length()) { r ->
                            val ring = rings.getJSONArray(r)
                            DoubleArray(ring.length()) { ring.getDouble(it) }
                        },
                )
            },
    )
}

private fun decode(
    bytes: ByteArray,
    dataset: Dataset,
): List<Building> {
    val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
    val magic = String(bytes, 0, 4, Charsets.US_ASCII)
    require(magic == "MCPG" && buffer.getInt(4) == 1) { "not an MCPG v1 file" }
    val count = buffer.getInt(8)
    buffer.position(12)
    val seen = HashSet<String>(count * 2)
    var lat = 0
    var lng = 0
    return List(count) { i ->
        val labelLength = buffer.get().toInt() and 0xFF
        val label = String(bytes, buffer.position(), labelLength, Charsets.UTF_8)
        buffer.position(buffer.position() + labelLength)
        val height = buffer.getShort().toInt() and 0xFFFF
        val ringCount = buffer.getShort().toInt() and 0xFFFF
        val rings =
            List(ringCount) {
                val n = buffer.getInt()
                val ring = DoubleArray(n * 2)
                for (v in 0 until n) {
                    lat += buffer.getInt()
                    lng += buffer.getInt()
                    ring[v * 2] = lng / 1e6
                    ring[v * 2 + 1] = lat / 1e6
                }
                ring
            }
        // A building's label is its Overture id; a parcel's is "<parcel id>|<land use>".
        // Parts of a multipolygon share the label, so a repeat gets the index appended.
        val parcel = dataset == Dataset.CITY_PARCELS
        val key = if (parcel) label.substringBefore('|') else label
        var id = key.ifEmpty { "${dataset.name}-$i" }
        if (!seen.add(id)) id = "$id#$i".also { seen.add(it) }
        Building(
            id = id,
            level = 0,
            height = if (height == 0xFFFF) null else height / 10.0,
            name = if (parcel && key.isNotEmpty()) "Parcel $key" else null,
            use = if (parcel) LandUse.of(label.substringAfter('|', "")) else null,
            rings = rings,
        )
    }
}
