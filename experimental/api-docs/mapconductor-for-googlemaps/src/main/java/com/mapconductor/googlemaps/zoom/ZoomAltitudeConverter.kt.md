# class `ZoomAltitudeConverter`

A utility class for converting between Google Maps zoom levels and physical altitude values.

## Description

The `ZoomAltitudeConverter` class provides a bridge between the abstract, screen-pixel-based zoom levels used by Google Maps and a physical altitude value (in meters) used by MapConductor. This conversion is essential for creating a unified camera control system across different map providers.

The conversion is not a simple linear mapping; it is a dynamic calculation that takes into account the camera's geographical latitude and its tilt angle. This ensures that the perceived distance from the map surface remains consistent, accounting for the Earth's curvature (approximated by latitude) and perspective distortion (caused by tilt).

This class inherits from `AbstractZoomAltitudeConverter`.

## Signature

```kotlin
class ZoomAltitudeConverter(
    zoom0Altitude: Double = DEFAULT_ZOOM0_ALTITUDE
) : AbstractZoomAltitudeConverter(zoom0Altitude)
```

### Constructor

Creates a new instance of `ZoomAltitudeConverter`.

#### Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `zoom0Altitude` | `Double` | The reference altitude in meters corresponding to zoom level 0 at the equator. This value is optional and defaults to `DEFAULT_ZOOM0_ALTITUDE`. |

---

## Methods

### `zoomLevelToAltitude`

Converts a Google Maps zoom level to a corresponding physical altitude in meters.

#### Signature

```kotlin
override fun zoomLevelToAltitude(
    zoomLevel: Double,
    latitude: Double,
    tilt: Double
): Double
```

#### Description

This method calculates the physical altitude of the camera based on a given Google Maps zoom level. The calculation is adjusted based on the map's center latitude and the camera's tilt angle to provide a more accurate physical representation of the camera's height above the ground. Input values are clamped to ensure they fall within a valid, practical range.

#### Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `zoomLevel` | `Double` | The Google Maps zoom level to convert. |
| `latitude` | `Double` | The current latitude of the map's center, in degrees. |
| `tilt` | `Double` | The current camera tilt angle, in degrees (e.g., 0 for a top-down view). |

#### Returns

| Type | Description |
|------|-------------|
| `Double` | The calculated altitude in meters, clamped within a valid range. |

---

### `altitudeToZoomLevel`

Converts a physical altitude in meters to the corresponding Google Maps zoom level.

#### Signature

```kotlin
override fun altitudeToZoomLevel(
    altitude: Double,
    latitude: Double,
    tilt: Double
): Double
```

#### Description

This method performs the inverse operation of `zoomLevelToAltitude`. It calculates the appropriate Google Maps zoom level that corresponds to a given physical altitude in meters. The calculation also considers the map's latitude and camera tilt to ensure accuracy. Input values are clamped to ensure they fall within a valid, practical range.

#### Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `altitude` | `Double` | The camera's altitude in meters to convert. |
| `latitude` | `Double` | The current latitude of the map's center, in degrees. |
| `tilt` | `Double` | The current camera tilt angle, in degrees. |

#### Returns

| Type | Description |
|------|-------------|
| `Double` | The calculated Google Maps zoom level, clamped within a valid range. |

---

## Example

The following example demonstrates how to create an instance of `ZoomAltitudeConverter` and use its methods for conversion.

```kotlin
import com.mapconductor.googlemaps.zoom.ZoomAltitudeConverter

fun main() {
    // Instantiate the converter.
    // We can use the default zoom0Altitude or provide a custom one.
    val converter = ZoomAltitudeConverter()

    // --- Scenario 1: Convert Zoom Level to Altitude ---
    val zoomLevel = 15.0
    val latitude = 40.7128 // New York City
    val tilt = 45.0 // A 45-degree camera tilt

    val calculatedAltitude = converter.zoomLevelToAltitude(zoomLevel, latitude, tilt)
    println("Zoom level $zoomLevel at latitude $latitude with tilt $tilt corresponds to an altitude of %.2f meters.".format(calculatedAltitude))
    // Expected output might be similar to:
    // Zoom level 15.0 at latitude 40.7128 with tilt 45.0 corresponds to an altitude of 1234.56 meters.

    // --- Scenario 2: Convert Altitude back to Zoom Level ---
    val altitude = 1234.56 // Use the altitude from the previous calculation

    val calculatedZoomLevel = converter.altitudeToZoomLevel(altitude, latitude, tilt)
    println("Altitude of %.2f meters at latitude $latitude with tilt $tilt corresponds to zoom level %.2f.".format(altitude, calculatedZoomLevel))
    // Expected output should be close to the original zoom level:
    // Altitude of 1234.56 meters at latitude 40.7128 with tilt 45.0 corresponds to zoom level 15.00.
}
```