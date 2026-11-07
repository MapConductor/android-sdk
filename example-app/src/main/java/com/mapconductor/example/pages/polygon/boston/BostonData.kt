package com.mapconductor.example.pages.polygon.boston

import android.content.Context
import org.json.JSONObject
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * The polygons the Boston page draws.
 *
 * - [DOWNTOWN]: the bundled Overture extract (assets, JSON), three nested areas.
 * - [CITY_BUILDINGS] / [CITY_PARCELS]: the whole city. Those files are too large
 *   for the APK; push them to the app's external files directory:
 *   `adb push boston-parcels.bin /sdcard/Android/data/com.mapconductor.example/files/boston/`
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
    CITY_PARCELS("City parcels", "boston/boston-parcels.bin", "City of Boston, Parcels 2025 (Analyze Boston) (ODC PDDL)"),
}

class BostonData(
    val attribution: String,
    val buildings: List<Building>,
)

private const val DOWNTOWN_ASSET = "boston/boston-buildings.json"

fun loadBostonData(
    context: Context,
    dataset: Dataset,
): BostonData =
    when (val file = dataset.file) {
        null -> loadDowntown(context)
        else -> {
            val path = File(context.getExternalFilesDir(null), file)
            require(path.exists()) { "Missing ${path.path}; push it with adb (see BostonData.kt)" }
            BostonData(dataset.attribution, decode(path.readBytes(), dataset))
        }
    }

private fun loadDowntown(context: Context): BostonData {
    val json = JSONObject(context.assets.open(DOWNTOWN_ASSET).bufferedReader().use { it.readText() })
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
        // A building's label is its Overture id; a parcel's is its parcel id, shown as the name.
        // Parts of a multipolygon share the label, so a repeat gets the index appended.
        var id = label.ifEmpty { "${dataset.name}-$i" }
        if (!seen.add(id)) id = "$id#$i".also { seen.add(it) }
        Building(
            id = id,
            level = 0,
            height = if (height == 0xFFFF) null else height / 10.0,
            name = if (dataset == Dataset.CITY_PARCELS && label.isNotEmpty()) "Parcel $label" else null,
            rings = rings,
        )
    }
}
