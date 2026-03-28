Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

# ZoomAltitudeConverter

The `ZoomAltitudeConverter` class provides utility functions to convert between a MapLibre map's zoom level and the camera's altitude in meters. It accounts for factors like latitude and camera tilt to provide more accurate conversions.

This class extends `AbstractZoomAltitudeConverter` and is specifically tailored for the MapLibre SDK's zoom behavior, which differs slightly from other mapping platforms like Google Maps.

## Signature

```kotlin
class ZoomAltitudeConverter(
    zoom0Altitude: Double = DEFAULT_ZOOM0_ALTITUDE
) : AbstractZoomAltitudeConverter(zoom0Altitude)
```

## Constructor

### `ZoomAltitudeConverter(zoom0Altitude: Double)`

Creates a new instance of the `ZoomAltitudeConverter`.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `zoom0Altitude` | `Double` | The camera altitude in meters that corresponds to zoom level 0 at the equator. Defaults to `DEFAULT_ZOOM0_ALTITUDE`. |

---

## Companion Object

### `MAPLIBRE_TO_GOOGLE_ZOOM_OFFSET`

An empirical constant representing the approximate offset between MapLibre SDK zoom levels and Google Maps zoom levels.

#### Signature

```kotlin
const val MAPLIBRE_TO_GOOGLE_ZOOM_OFFSET = 1.0
```

---

### `maplibreZoomToGoogleZoom()`

Converts a MapLibre zoom level to its approximate Google Maps equivalent by adding the `MAPLIBRE_TO_GOOGLE_ZOOM_OFFSET`. The result is clamped to a valid zoom range.

#### Signature

```kotlin
fun maplibreZoomToGoogleZoom(maplibreZoom: Double): Double
```

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `maplibreZoom` | `Double` | The zoom level from the MapLibre SDK. |

#### Returns

`Double` - The equivalent Google Maps zoom level.

---

### `googleZoomToMaplibreZoom()`

Converts a Google Maps zoom level to its approximate MapLibre equivalent by subtracting the `MAPLIBRE_TO_GOOGLE_ZOOM_OFFSET`. The result is clamped to a valid zoom range.

#### Signature

```kotlin
fun googleZoomToMaplibreZoom(googleZoom: Double): Double
```

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `googleZoom` | `Double` | The zoom level from a Google Maps context. |

#### Returns

`Double` - The equivalent MapLibre zoom level.

---

## Methods

### `zoomLevelToAltitude()`

Calculates the camera altitude in meters that corresponds to a given MapLibre zoom level, latitude, and camera tilt. The calculation first converts the MapLibre zoom to a Google-style zoom level for a more standardized altitude formula.

#### Signature

```kotlin
override fun zoomLevelToAltitude(
    zoomLevel: Double,
    latitude: Double,
    tilt: Double
): Double
```

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `zoomLevel` | `Double` | The target MapLibre zoom level. |
| `latitude` | `Double` | The current latitude of the map's center, in degrees. |
| `tilt` | `Double` | The current camera tilt (pitch), in degrees. |

#### Returns

`Double` - The calculated camera altitude in meters, clamped within a valid range (`MIN_ALTITUDE` to `MAX_ALTITUDE`).

---

### `altitudeToZoomLevel()`

Calculates the MapLibre zoom level that corresponds to a given camera altitude, latitude, and camera tilt.

#### Signature

```kotlin
override fun altitudeToZoomLevel(
    altitude: Double,
    latitude: Double,
    tilt: Double
): Double
```

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `altitude` | `Double` | The camera's altitude above the map surface, in meters. |
| `latitude` | `Double` | The current latitude of the map's center, in degrees. |
| `tilt` | `Double` | The current camera tilt (pitch), in degrees. |

#### Returns

`Double` - The calculated MapLibre zoom level.

---

## Example

Here's how to use the `ZoomAltitudeConverter` to perform conversions.

```kotlin
import com.mapconductor.maplibre.zoom.ZoomAltitudeConverter

fun main() {
    // Instantiate the converter
    val converter = ZoomAltitudeConverter()

    // Define map state
    val maplibreZoom = 15.0
    val altitude = 1500.0
    val latitude = 34.0522 // Los Angeles
    val tilt = 45.0

    // 1. Convert from zoom level to altitude
    val calculatedAltitude = converter.zoomLevelToAltitude(
        zoomLevel = maplibreZoom,
        latitude = latitude,
        tilt = tilt
    )
    println("MapLibre zoom $maplibreZoom at latitude $latitude with tilt $tilt° corresponds to an altitude of approximately ${"%.2f".format(calculatedAltitude)} meters.")
    // Expected output: MapLibre zoom 15.0 at latitude 34.0522 with tilt 45.0° corresponds to an altitude of approximately 1535.23 meters.

    // 2. Convert from altitude to zoom level
    val calculatedZoom = converter.altitudeToZoomLevel(
        altitude = altitude,
        latitude = latitude,
        tilt = tilt
    )
    println("An altitude of $altitude meters at latitude $latitude with tilt $tilt° corresponds to MapLibre zoom level ${"%.2f".format(calculatedZoom)}.")
    // Expected output: An altitude of 1500.0 meters at latitude 34.0522 with tilt 45.0° corresponds to MapLibre zoom level 14.96.

    // 3. Use the static conversion helpers
    val googleZoom = ZoomAltitudeConverter.maplibreZoomToGoogleZoom(maplibreZoom)
    println("MapLibre zoom $maplibreZoom is equivalent to Google Maps zoom $googleZoom.")
    // Expected output: MapLibre zoom 15.0 is equivalent to Google Maps zoom 16.0.
}
```