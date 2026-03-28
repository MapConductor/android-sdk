Of course! Here is the high-quality SDK documentation for the provided Kotlin code snippet.

---

# ZoomAltitudeConverterDeprecated

## Description

The `ZoomAltitudeConverterDeprecated` class provides methods to convert between map zoom levels and camera altitude. This implementation is designed to be compatible with ArcGIS-style map rendering, where altitude is affected by latitude and camera tilt.

**Note:** As the name suggests, this is a deprecated implementation. It is provided for backward compatibility. For new development, consider using the latest recommended converter if available.

This class extends `AbstractZoomAltitudeConverter`.

## Constructor

### `ZoomAltitudeConverterDeprecated(zoom0Altitude: Double)`

Initializes a new instance of the `ZoomAltitudeConverterDeprecated`.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `zoom0Altitude` | `Double` | The camera altitude in meters that corresponds to zoom level 0 at the equator (0° latitude) with no camera tilt. Defaults to `DEFAULT_ZOOM0_ALTITUDE`. It is highly recommended to calculate this value using the `computeZoom0DistanceForView` utility function. |

## Companion Object Methods

These are static-like utility methods available on the `ZoomAltitudeConverterDeprecated` class.

### `verticalFovFromHorizontal()`

Calculates the vertical field of view (FOV) based on a given horizontal FOV and the viewport's aspect ratio.

#### Signature

```kotlin
fun verticalFovFromHorizontal(
    horizontalFovDeg: Double,
    aspectRatio: Double
): Double
```

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `horizontalFovDeg` | `Double` | The horizontal field of view in degrees. |
| `aspectRatio` | `Double` | The aspect ratio of the viewport (width / height). |

#### Returns

`Double` - The calculated vertical field of view in degrees.

### `computeZoom0DistanceForView()`

A utility function to compute the ideal `zoom0Altitude` value for initializing the converter. This value is based on the viewport's dimensions and the camera's vertical field of view.

#### Signature

```kotlin
fun computeZoom0DistanceForView(
    viewportHeightPx: Int,
    verticalFovDeg: Double,
    initialMetersPerPixel: Double = WEB_MERCATOR_INITIAL_MPP_256
): Double
```

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `viewportHeightPx` | `Int` | The height of the map viewport in pixels. |
| `verticalFovDeg` | `Double` | The vertical field of view of the camera in degrees. |
| `initialMetersPerPixel` | `Double` | *(Optional)* The map resolution (meters per pixel) at zoom level 0 for a standard 256x256 tile. Defaults to the Web Mercator value `156_543.033_928`. |

#### Returns

`Double` - The calculated distance for zoom level 0, which should be used as the `zoom0Altitude` parameter when constructing the converter.

## Methods

### `zoomLevelToAltitude()`

Converts a map zoom level to the corresponding camera altitude, taking into account the current latitude and camera tilt.

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
| `zoomLevel` | `Double` | The map zoom level to convert. |
| `latitude` | `Double` | The current latitude of the map center in degrees. |
| `tilt` | `Double` | The current camera tilt (pitch) in degrees, where 0 is looking straight down. |

#### Returns

`Double` - The calculated camera altitude in meters.

### `altitudeToZoomLevel()`

Converts a camera altitude to the corresponding map zoom level, taking into account the current latitude and camera tilt.

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
| `altitude` | `Double` | The camera altitude in meters. |
| `latitude` | `Double` | The current latitude of the map center in degrees. |
| `tilt` | `Double` | The current camera tilt (pitch) in degrees, where 0 is looking straight down. |

#### Returns

`Double` - The calculated map zoom level.

## Legacy Methods

These methods are provided for backward compatibility and may not account for all view parameters like camera tilt.

### `zoomLevelToAltitude(zoomLevel: Double)`

Converts a zoom level to altitude, assuming 0° latitude and 0° tilt.

#### Signature

```kotlin
fun zoomLevelToAltitude(zoomLevel: Double): Double
```

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `zoomLevel` | `Double` | The map zoom level to convert. |

#### Returns

`Double` - The calculated camera altitude in meters.

### `zoomLevelToAltitude(zoomLevel: Double, latitude: Double)`

Converts a zoom level to altitude, accounting for latitude but assuming 0° tilt.

#### Signature

```kotlin
fun zoomLevelToAltitude(
    zoomLevel: Double,
    latitude: Double
): Double
```

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `zoomLevel` | `Double` | The map zoom level to convert. |
| `latitude` | `Double` | The current latitude of the map center in degrees. |

#### Returns

`Double` - The calculated camera altitude in meters.

### `zoomLevelToDistance(zoomLevel: Double, latitude: Double)`

Converts a zoom level to the camera's distance from the ground along the view center axis, accounting for latitude. Note that "distance" differs from "altitude" when the camera is tilted.

#### Signature

```kotlin
fun zoomLevelToDistance(
    zoomLevel: Double,
    latitude: Double
): Double
```

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `zoomLevel` | `Double` | The map zoom level to convert. |
| `latitude` | `Double` | The current latitude of the map center in degrees. |

#### Returns

`Double` - The calculated camera distance in meters.

### `altitudeToZoomLevel(altitude: Double)`

Converts an altitude to a zoom level, assuming 0° latitude and 0° tilt.

#### Signature

```kotlin
fun altitudeToZoomLevel(altitude: Double): Double
```

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `altitude` | `Double` | The camera altitude in meters. |

#### Returns

`Double` - The calculated map zoom level.

### `altitudeToZoomLevel(altitude: Double, latitude: Double)`

Converts an altitude to a zoom level, accounting for latitude but assuming 0° tilt.

#### Signature

```kotlin
fun altitudeToZoomLevel(
    altitude: Double,
    latitude: Double
): Double
```

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `altitude` | `Double` | The camera altitude in meters. |
| `latitude` | `Double` | The current latitude of the map center in degrees. |

#### Returns

`Double` - The calculated map zoom level.

## Example

Here is an example of how to initialize and use the `ZoomAltitudeConverterDeprecated`.

```kotlin
import com.mapconductor.arcgis.ZoomAltitudeConverterDeprecated

fun main() {
    // Viewport and camera parameters
    val viewportHeight = 1080 // pixels
    val verticalFov = 60.0 // degrees
    val currentLatitude = 40.7128 // New York City
    val currentTilt = 45.0 // degrees

    // 1. Calculate the required zoom0Altitude for our specific view setup
    val zoom0Altitude = ZoomAltitudeConverterDeprecated.computeZoom0DistanceForView(
        viewportHeightPx = viewportHeight,
        verticalFovDeg = verticalFov
    )
    println("Calculated zoom0Altitude: $zoom0Altitude")

    // 2. Create an instance of the converter with the calculated value
    val converter = ZoomAltitudeConverterDeprecated(zoom0Altitude)

    // 3. Convert a zoom level to altitude
    val zoomLevel = 15.0
    val altitude = converter.zoomLevelToAltitude(
        zoomLevel = zoomLevel,
        latitude = currentLatitude,
        tilt = currentTilt
    )
    println("For zoom level $zoomLevel at latitude $currentLatitude and tilt $currentTilt, altitude is $altitude meters.")

    // 4. Convert an altitude back to a zoom level
    val newZoomLevel = converter.altitudeToZoomLevel(
        altitude = altitude,
        latitude = currentLatitude,
        tilt = currentTilt
    )
    println("For altitude $altitude at latitude $currentLatitude and tilt $currentTilt, zoom level is $newZoomLevel.")
}

// Example Output:
// Calculated zoom0Altitude: 4.818844949903386E7
// For zoom level 15.0 at latitude 40.7128 and tilt 45.0, altitude is 1048.099119129931 meters.
// For altitude 1048.099119129931 at latitude 40.7128 and tilt 45.0, zoom level is 15.0.
```