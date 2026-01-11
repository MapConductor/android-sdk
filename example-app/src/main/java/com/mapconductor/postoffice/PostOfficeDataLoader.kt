package com.mapconductor.postoffice

import android.content.Context
import com.mapconductor.core.features.GeoPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.zip.ZipInputStream

/**
 * Utility class for loading post office data from zip files in assets.
 * Handles extraction of GeoJSON files and parsing them into PostOffice objects.
 */
class PostOfficeDataLoader(
    private val context: Context,
) {
    /**
     * Load all post offices from zip files in the assets folder.
     * This method extracts each zip file and parses the contained GeoJSON data.
     */
    suspend fun loadAllPostOffices(): List<PostOffice> =
        withContext(Dispatchers.IO) {
            val postOffices = mutableListOf<PostOffice>()

            try {
                val assetManager = context.assets
                val assetFiles = assetManager.list("") ?: emptyArray()

                // Filter for zip files matching the pattern P30-13_*.zip
                val zipFiles = assetFiles.filter { it.startsWith("P30-13_") && it.endsWith(".zip") }

                zipFiles.forEach { zipFileName ->
                    try {
                        val zipPostOffices = loadPostOfficesFromZip(zipFileName)
                        postOffices.addAll(zipPostOffices)
                    } catch (e: Exception) {
                        // Log error but continue with other files
                        println("Error loading $zipFileName: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                println("Error accessing assets: ${e.message}")
            }

            postOffices
        }

    /**
     * Load post offices from a specific zip file.
     */
    private suspend fun loadPostOfficesFromZip(zipFileName: String): List<PostOffice> =
        withContext(Dispatchers.IO) {
            val postOffices = mutableListOf<PostOffice>()

            context.assets.open(zipFileName).use { inputStream ->
                ZipInputStream(inputStream).use { zipStream ->
                    var entry = zipStream.nextEntry

                    while (entry != null) {
                        if (!entry.isDirectory && entry.name.endsWith(".geojson")) {
                            try {
                                val geoJsonContent = readZipEntryContent(zipStream)
                                val parsedPostOffices = parseGeoJsonToPostOffices(geoJsonContent)
                                postOffices.addAll(parsedPostOffices)
                            } catch (e: Exception) {
                                println("Error parsing ${entry.name} in $zipFileName: ${e.message}")
                            }
                        }
                        zipStream.closeEntry()
                        entry = zipStream.nextEntry
                    }
                }
            }

            postOffices
        }

    /**
     * Read the content of a zip entry as a string.
     */
    private fun readZipEntryContent(zipStream: ZipInputStream): String {
        val reader = BufferedReader(InputStreamReader(zipStream, Charsets.UTF_8))
        return reader.readText()
    }

    /**
     * Parse GeoJSON content into PostOffice objects.
     */
    private fun parseGeoJsonToPostOffices(geoJsonContent: String): List<PostOffice> {
        val postOffices = mutableListOf<PostOffice>()

        try {
            val jsonObject = JSONObject(geoJsonContent)
            val features = jsonObject.optJSONArray("features") ?: JSONArray()

            for (i in 0 until features.length()) {
                try {
                    val feature = features.getJSONObject(i)
                    val postOffice = parseFeatureToPostOffice(feature)
                    postOffice?.let { postOffices.add(it) }
                } catch (e: Exception) {
                    // Skip invalid features but continue processing
                    println("Error parsing feature: ${e.message}")
                }
            }
        } catch (e: Exception) {
            println("Error parsing GeoJSON: ${e.message}")
        }

        return postOffices
    }

    /**
     * Parse a single GeoJSON feature into a PostOffice object.
     */
    private fun parseFeatureToPostOffice(feature: JSONObject): PostOffice? {
        return try {
            // Extract geometry
            val geometry = feature.optJSONObject("geometry") ?: return null
            val coordinates = geometry.optJSONArray("coordinates") ?: return null

            if (coordinates.length() < 2) return null

            val longitude = coordinates.getDouble(0)
            val latitude = coordinates.getDouble(1)
            val position = GeoPoint(latitude, longitude)

            // Extract properties
            val properties = feature.optJSONObject("properties") ?: return null
            val name = properties.optString("name", "")
            val address = properties.optString("address", "")

            PostOffice(
                position = position,
                name = name,
                address = address,
            )
        } catch (e: Exception) {
            println("Error parsing feature to PostOffice: ${e.message}")
            null
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
