Of course! Here is the high-quality SDK documentation for the provided Kotlin code snippet.

---

# ZoomAltitudeConverter

## Class: `ZoomAltitudeConverter`

A concrete implementation of `AbstractZoomAltitudeConverter` for the Mapbox SDK. This class provides methods to convert between Mapbox zoom levels and camera altitude in meters. The conversion logic accounts for the map's latitude and the camera's tilt angle to provide more accurate results, especially in a 3D perspective view.

It uses an empirical offset to reconcile the difference between Mapbox's zoom level system and the more common Web Mercator scale math (often associated with Google Maps).

```kotlin
class ZoomAltitudeConverter(
    zoom0Altitude: Double = DEFAULT_ZOOM0_ALTITUDE
) : AbstractZoomAltitudeConverter(zoom0Altitude)
```

### Constructor

#### Signature

```kotlin
ZoomAltitudeConverter(zoom0Altitude: Double = DEFAULT_ZOOM0_ALTITUDE)
```

#### Description

Creates a new instance of the `ZoomAltitudeConverter`.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `zoom0Altitude` | `Double` | **Optional**. The camera altitude in meters that corresponds to zoom level 0 at the equator. Defaults to `DEFAULT_ZOOM0_ALTITUDE`. |

---

## Companion Object Methods

### `mapboxZoomToGoogleZoom`

#### Signature

```kotlin
fun mapboxZoomToGoogleZoom(mapboxZoom: Double): Double
```

#### Description

Converts a Mapbox SDK zoom level to an equivalent Google Maps-style zoom level. This is achieved by adding a constant offset (`MAPBOX_TO_GOOGLE_ZOOM_OFFSET`). The result is clamped within the valid zoom range.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `mapboxZoom` | `Double` | The zoom level from the Mapbox SDK. |

#### Returns

`Double` - The equivalent Google Maps-style zoom level.

---

### `googleZoomToMapboxZoom`

#### Signature

```kotlin
fun googleZoomToMapboxZoom(googleZoom: Double): Double
```

#### Description

Converts a Google Maps-style zoom level to an equivalent Mapbox SDK zoom level. This is achieved by subtracting a constant offset (`MAPBOX_TO_GOOGLE_ZOOM_OFFSET`). The result is clamped within the valid zoom range.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `googleZoom` | `Double` | The Google Maps-style zoom level. |

#### Returns

`Double` - The equivalent Mapbox SDK zoom level.

---

## Methods

### `zoomLevelToAltitude`

#### Signature

```kotlin
override fun zoomLevelToAltitude(
    zoomLevel: Double,
    latitude: Double,
    tilt: Double
): Double
```

#### Description

Calculates the camera altitude in meters that corresponds to a given Mapbox zoom level. The calculation is adjusted based on the provided latitude and camera tilt angle for greater accuracy.

The conversion process is as follows:
1.  The Mapbox zoom level is converted to a Google-style zoom level.
2.  The altitude is calculated using the Web Mercator projection scale formula, factoring in latitude and tilt.
3.  The final altitude is clamped to a valid range.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `zoomLevel` | `Double` | The target Mapbox zoom level. |
| `latitude` | `Double` | The current latitude of the map's center, in degrees. |
| `tilt` | `Double` | The current camera tilt (pitch) angle, in degrees. |

#### Returns

`Double` - The calculated camera altitude in meters.

---

### `altitudeToZoomLevel`

#### Signature

```kotlin
override fun altitudeToZoomLevel(
    altitude: Double,
    latitude: Double,
    tilt: Double
): Double
```

#### Description

Calculates the Mapbox zoom level that corresponds to a given camera altitude in meters. This is the inverse operation of `zoomLevelToAltitude`. The calculation is adjusted based on the provided latitude and camera tilt angle.

The conversion process is as follows:
1.  The altitude is clamped to a valid range.
2.  A Google-style zoom level is calculated using the inverse of the Web Mercator projection scale formula, factoring in latitude and tilt.
3.  The Google-style zoom level is converted to a Mapbox zoom level.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `altitude` | `Double` | The current camera altitude in meters. |
| `latitude` | `Double` | The current latitude of the map's center, in degrees. |
| `tilt` | `Double` | The current camera tilt (pitch) angle, in degrees. |

#### Returns

`Double` - The calculated Mapbox zoom level.

---

## Example

Here's how to use `ZoomAltitudeConverter` to convert between zoom levels and altitude.

```kotlin
import com.mapconductor.mapbox.zoom.ZoomAltitudeConverter

fun main() {
    // Use the default zoom0Altitude
    val converter = ZoomAltitudeConverter()

    // --- Scenario 1: Calculate altitude from a known zoom level ---
    val mapboxZoom = 14.5
    val latitude = 34.0522 // Los Angeles
    val tilt = 45.0 // 45-degree camera tilt

    val calculatedAltitude = converter.zoomLevelToAltitude(
        zoomLevel = mapboxZoom,
        latitude = latitude,
        tilt = tilt
    )

    println("For Mapbox zoom $mapboxZoom at latitude $latitude with a $tilt-degree tilt:")
    println("Calculated camera altitude is approximately ${"%.2f".format(calculatedAltitude)} meters.")
    // Expected output:
    // For Mapbox zoom 14.5 at latitude 34.0522 with a 45.0-degree tilt:
    // Calculated camera altitude is approximately 2353.09 meters.


    // --- Scenario 2: Calculate zoom level from a known altitude ---
    val cameraAltitude = 5000.0 // 5000 meters

    val calculatedZoom = converter.altitudeToZoomLevel(
        altitude = cameraAltitude,
        latitude = latitude,
        tilt = tilt
    )

    println("\nFor a camera altitude of $cameraAltitude meters at latitude $latitude with a $tilt-degree tilt:")
    println("Calculated Mapbox zoom level is approximately ${"%.2f".format(calculatedZoom)}.")
    // Expected output:
    // For a camera altitude of 5000.0 meters at latitude 34.0522 with a 45.0-degree tilt:
    // Calculated Mapbox zoom level is approximately 13.40.
}
```