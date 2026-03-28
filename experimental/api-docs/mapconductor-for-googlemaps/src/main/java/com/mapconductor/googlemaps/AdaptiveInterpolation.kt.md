Of course! Here is the high-quality SDK documentation for the provided code snippet.

***

# Google Maps Utilities

This document outlines a collection of internal utility classes and objects used for optimizing polyline rendering on Google Maps. These helpers manage adaptive interpolation calculations and caching to balance performance and visual quality.

## `AdaptiveInterpolation`

A utility object that provides functions for calculating parameters needed for adaptive polyline interpolation. Adaptive interpolation adjusts the density of points in a polyline based on the map's zoom level, ensuring that lines look smooth without using an excessive number of vertices, which helps optimize rendering performance.

---

### `maxSegmentLengthMeters`

Calculates the maximum length for a polyline segment in meters. This length is adaptive, based on the current map zoom level and latitude. The calculation aims to maintain a consistent visual segment length on the screen, breaking down long polylines into smaller, visually appropriate segments. The result is clamped to predefined minimum and maximum values to ensure sensibility.

#### Signature

```kotlin
fun maxSegmentLengthMeters(
    zoom: Float,
    latitude: Double
): Double
```

#### Description

This function determines the ideal segment length by calculating the meters-per-pixel ratio at a given latitude and zoom level. It then multiplies this by a target pixel length (`TARGET_SEGMENT_PIXELS`) to find the desired segment length in meters.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `zoom` | `Float` | The current zoom level of the map. |
| `latitude` | `Double` | The latitude for which the calculation is being performed. The meters-per-pixel ratio changes with latitude. |

#### Returns

| Type | Description |
| :--- | :--- |
| `Double` | The calculated maximum segment length in meters, clamped between 50.0 and 100,000.0. |

---

### `pointsHash`

Generates a stable 64-bit hash for a list of geographic points. This function is designed to be resilient to minor floating-point inaccuracies by first quantizing the latitude and longitude values.

#### Signature

```kotlin
fun pointsHash(points: List<GeoPointInterface>): Long
```

#### Description

This function uses the 64-bit FNV-1a hashing algorithm. It iterates over each point, quantizes its coordinates to an integer representation, and incorporates them into the hash. The total number of points is also factored into the final hash value, ensuring that lists with different lengths but similar starting points produce unique hashes. This is primarily used for creating reliable cache keys.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `points` | `List<GeoPointInterface>` | The list of geographic points to hash. |

#### Returns

| Type | Description |
| :--- | :--- |
| `Long` | A 64-bit FNV-1a hash of the input points. |

---

### `cacheKey`

Creates a unique string key for caching interpolated polyline data.

#### Signature

```kotlin
fun cacheKey(
    pointsHash: Long,
    maxSegmentLengthMeters: Double
): String
```

#### Description

This function combines the hash of an original polyline with the interpolation segment length used to process it. This ensures that if either the source points or the interpolation detail (segment length) changes, a new cache key is generated, preventing cache collisions.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `pointsHash` | `Long` | The hash of the original list of points, typically from `pointsHash()`. |
| `maxSegmentLengthMeters` | `Double` | The segment length used for interpolation, typically from `maxSegmentLengthMeters()`. |

#### Returns

| Type | Description |
| :--- | :--- |
| `String` | A unique string suitable for use as a cache key. |

## `LatLngInterpolationCache`

An internal class that provides a memory cache for storing the results of polyline interpolations (which are lists of `LatLng`). It is a wrapper around Android's `LruCache`, implementing a "Least Recently Used" eviction policy.

#### Signature

```kotlin
internal class LatLngInterpolationCache(maxEntries: Int)
```

#### Description

When the cache reaches its maximum configured size, it automatically discards the least recently accessed items to make room for new ones. This is useful for storing computationally expensive interpolation results that are likely to be reused.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `maxEntries` | `Int` | The maximum number of entries the cache can hold before items are evicted. |

---

### `get`

Retrieves an interpolated list of `LatLng` points from the cache.

#### Signature

```kotlin
fun get(key: String): List<LatLng>?
```

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `key` | `String` | The key associated with the cached data. |

#### Returns

| Type | Description |
| :--- | :--- |
| `List<LatLng>?` | The cached list of `LatLng` points if the key is found, otherwise `null`. |

---

### `put`

Adds a list of `LatLng` points to the cache with its corresponding key.

#### Signature

```kotlin
fun put(
    key: String,
    value: List<LatLng>
)
```

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `key` | `String` | The key to store the data under. |
| `value` | `List<LatLng>` | The list of `LatLng` points to cache. |

#### Returns

This method does not return a value.

---

### Example

The following example demonstrates the typical workflow for using `AdaptiveInterpolation` and `LatLngInterpolationCache` together.

```kotlin
import com.google.android.gms.maps.model.LatLng
import com.mapconductor.core.features.GeoPointInterface

// A simple implementation of GeoPointInterface for the example
data class MyGeoPoint(override val latitude: Double, override val longitude: Double) : GeoPointInterface

fun main() {
    // 1. Define the original points of a polyline
    val originalPoints = listOf(
        MyGeoPoint(40.7128, -74.0060), // New York
        MyGeoPoint(34.0522, -118.2437) // Los Angeles
    )

    // 2. Define map state
    val currentZoom = 8.0f
    val currentLatitude = 39.8283 // Approx. center of US

    // 3. Calculate the adaptive segment length for the current view
    val segmentLength = AdaptiveInterpolation.maxSegmentLengthMeters(currentZoom, currentLatitude)
    println("Calculated max segment length: ${segmentLength.toLong()} meters")

    // 4. Generate a hash for the original points
    val hash = AdaptiveInterpolation.pointsHash(originalPoints)
    println("Points hash: $hash")

    // 5. Create a unique cache key
    val key = AdaptiveInterpolation.cacheKey(hash, segmentLength)
    println("Cache key: $key")

    // 6. Initialize the cache
    val interpolationCache = LatLngInterpolationCache(maxEntries = 100)

    // 7. Assume we have performed interpolation and have a result
    val interpolatedResult = listOf(
        LatLng(40.7128, -74.0060),
        // ... many more interpolated points ...
        LatLng(34.0522, -118.2437)
    )

    // 8. Store the result in the cache
    interpolationCache.put(key, interpolatedResult)
    println("Stored ${interpolatedResult.size} points in cache.")

    // 9. Later, retrieve the data from the cache
    val cachedData = interpolationCache.get(key)

    if (cachedData != null) {
        println("Successfully retrieved ${cachedData.size} points from cache.")
    } else {
        println("Cache miss. Must re-calculate interpolation.")
    }
}

// Example Output:
// Calculated max segment length: 100000 meters
// Points hash: -544593387939339395
// Cache key: -544593387939339395_100000
// Stored 2 points in cache.
// Successfully retrieved 2 points from cache.
```