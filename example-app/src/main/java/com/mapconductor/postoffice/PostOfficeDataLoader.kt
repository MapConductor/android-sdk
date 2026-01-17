package com.mapconductor.postoffice

import android.content.Context
import android.os.SystemClock
import android.util.Log
import android.util.JsonReader
import android.util.JsonToken
import com.mapconductor.core.features.GeoPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.Reader
import java.util.zip.ZipInputStream

/**
 * Utility class for loading post office data from zip files in assets.
 * Handles extraction of GeoJSON files and parsing them into PostOffice objects.
 */
class PostOfficeDataLoader(
    private val context: Context,
) {
    private val tag: String = "PostOfficeDataLoader"

    /**
     * Load all post offices from zip files in the assets folder.
     * This method extracts each zip file and parses the contained GeoJSON data.
     */
    suspend fun loadAllPostOffices(): List<PostOffice> =
        withContext(Dispatchers.IO) {
            val start = SystemClock.elapsedRealtime()
            val postOffices = mutableListOf<PostOffice>()

            try {
                val assetManager = context.assets
                val listStart = SystemClock.elapsedRealtime()
                val assetFiles = assetManager.list("") ?: emptyArray()
                Log.i(tag, "assets.list took ${SystemClock.elapsedRealtime() - listStart}ms | count=${assetFiles.size}")

                // Filter for zip files matching the pattern P30-13_*.zip
                val zipFiles = assetFiles.filter { it.startsWith("P30-13_") && it.endsWith(".zip") }
                Log.i(tag, "zipFiles=${zipFiles.size}")

                zipFiles.forEach { zipFileName ->
                    try {
                        val zipStart = SystemClock.elapsedRealtime()
                        val zipPostOffices = loadPostOfficesFromZip(zipFileName)
                        postOffices.addAll(zipPostOffices)
                        Log.i(
                            tag,
                            "loadZip done in ${SystemClock.elapsedRealtime() - zipStart}ms | file=$zipFileName count=${zipPostOffices.size} total=${postOffices.size}",
                        )
                    } catch (e: Exception) {
                        // Log error but continue with other files
                        Log.w(tag, "Error loading $zipFileName", e)
                    }
                }
            } catch (e: Exception) {
                Log.w(tag, "Error accessing assets", e)
            }

            Log.i(tag, "loadAllPostOffices took ${SystemClock.elapsedRealtime() - start}ms | total=${postOffices.size}")
            postOffices
        }

    /**
     * Load post offices from a specific zip file.
     */
    private suspend fun loadPostOfficesFromZip(zipFileName: String): List<PostOffice> =
        withContext(Dispatchers.IO) {
            val start = SystemClock.elapsedRealtime()
            val postOffices = mutableListOf<PostOffice>()

            context.assets.open(zipFileName).use { inputStream ->
                ZipInputStream(inputStream).use { zipStream ->
                    var entry = zipStream.nextEntry
                    var entryCount = 0
                    var geojsonCount = 0

                    while (entry != null) {
                        entryCount++
                        if (!entry.isDirectory && entry.name.endsWith(".geojson")) {
                            try {
                                geojsonCount++
                                val parseStart = SystemClock.elapsedRealtime()
                                val parsedPostOffices = parseGeoJsonToPostOffices(InputStreamReader(zipStream, Charsets.UTF_8))
                                val parseMs = SystemClock.elapsedRealtime() - parseStart
                                postOffices.addAll(parsedPostOffices)
                                Log.i(
                                    tag,
                                    "entry parsed | file=$zipFileName entry=${entry.name} parseMs=$parseMs count=${parsedPostOffices.size}",
                                )
                            } catch (e: Exception) {
                                Log.w(tag, "Error parsing ${entry.name} in $zipFileName", e)
                            }
                        }
                        zipStream.closeEntry()
                        entry = zipStream.nextEntry
                    }
                    Log.i(
                        tag,
                        "loadZip summary | file=$zipFileName entries=$entryCount geojson=$geojsonCount count=${postOffices.size} took ${SystemClock.elapsedRealtime() - start}ms",
                    )
                }
            }

            postOffices
        }

    /**
     * Parse GeoJSON content into PostOffice objects.
     */
    private fun parseGeoJsonToPostOffices(reader: Reader): List<PostOffice> {
        val postOffices = mutableListOf<PostOffice>()

        // IMPORTANT: Do not let the JsonReader close the underlying ZipInputStream.
        // Closing ZipInputStream mid-iteration will break closeEntry()/nextEntry.
        val nonClosingReader = NonClosingReader(reader)
        val json = JsonReader(BufferedReader(nonClosingReader))
        try {
            json.beginObject()
            while (json.hasNext()) {
                when (json.nextName()) {
                    "features" -> {
                        json.beginArray()
                        while (json.hasNext()) {
                            parseFeatureToPostOffice(json)?.let { postOffices.add(it) }
                        }
                        json.endArray()
                    }
                    else -> json.skipValue()
                }
            }
            json.endObject()
        } finally {
            // Safe: NonClosingReader prevents ZipInputStream from being closed here.
            try {
                json.close()
            } catch (_: Exception) {
                // Ignore; best-effort cleanup.
            }
        }

        return postOffices
    }

    /**
     * Parse a single GeoJSON feature into a PostOffice object.
     */
    private fun parseFeatureToPostOffice(json: JsonReader): PostOffice? {
        var latitude: Double? = null
        var longitude: Double? = null
        var name: String = ""
        var address: String = ""

        json.beginObject()
        while (json.hasNext()) {
            when (json.nextName()) {
                "geometry" -> {
                    val coords = parseGeometryCoordinates(json)
                    if (coords != null) {
                        longitude = coords.first
                        latitude = coords.second
                    }
                }
                "properties" -> {
                    json.beginObject()
                    while (json.hasNext()) {
                        when (json.nextName()) {
                            "name" -> name = if (json.peek() == JsonToken.NULL) { json.nextNull(); "" } else json.nextString()
                            "address" -> address = if (json.peek() == JsonToken.NULL) { json.nextNull(); "" } else json.nextString()
                            else -> json.skipValue()
                        }
                    }
                    json.endObject()
                }
                else -> json.skipValue()
            }
        }
        json.endObject()

        val lat = latitude ?: return null
        val lng = longitude ?: return null
        return PostOffice(position = GeoPoint(lat, lng), name = name, address = address)
    }

    private fun parseGeometryCoordinates(json: JsonReader): Pair<Double, Double>? {
        var longitude: Double? = null
        var latitude: Double? = null

        json.beginObject()
        while (json.hasNext()) {
            when (json.nextName()) {
                "coordinates" -> {
                    json.beginArray()
                    if (json.hasNext()) longitude = json.nextDouble() else json.skipValue()
                    if (json.hasNext()) latitude = json.nextDouble() else json.skipValue()
                    while (json.hasNext()) json.skipValue()
                    json.endArray()
                }
                else -> json.skipValue()
            }
        }
        json.endObject()

        val lng = longitude ?: return null
        val lat = latitude ?: return null
        return Pair(lng, lat)
    }

    private class NonClosingReader(
        private val delegate: Reader,
    ) : Reader() {
        override fun read(
            cbuf: CharArray,
            off: Int,
            len: Int,
        ): Int = delegate.read(cbuf, off, len)

        override fun close() {
            // no-op
        }
    }

    /**
     * Load post offices from a specific zip file by name.
     * Useful for loading data from individual regions.
     */
    suspend fun loadPostOfficesFromSpecificZip(zipFileName: String): List<PostOffice> =
        withContext(Dispatchers.IO) {
            try {
                loadPostOfficesFromZip(zipFileName)
            } catch (e: Exception) {
                println("Error loading from $zipFileName: ${e.message}")
                emptyList()
            }
        }

    /**
     * Get the list of available zip files in assets.
     */
    suspend fun getAvailableZipFiles(): List<String> =
        withContext(Dispatchers.IO) {
            try {
                val assetManager = context.assets
                val assetFiles = assetManager.list("") ?: emptyArray()
                assetFiles.filter { it.startsWith("P30-13_") && it.endsWith(".zip") }.sorted()
            } catch (e: Exception) {
                println("Error getting zip files: ${e.message}")
                emptyList()
            }
        }
}
