# AbstractZoomAltitudeConverter

The `AbstractZoomAltitudeConverter` is an abstract base class designed to provide a standardized interface for converting between map zoom levels and camera altitude in meters. Concrete implementations of this class define the specific mathematical model for the conversion, which can vary based on the map projection and desired behavior.

This class is intended to be extended, not instantiated directly.

## Constructor

### Signature
```kotlin
abstract class AbstractZoomAltitudeConverter(
    protected val zoom0Altitude: Double
)
```

### Description
Creates a new instance of an `AbstractZoomAltitudeConverter`.

### Parameters
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `zoom0Altitude` | `Double` | The camera altitude in meters that corresponds to zoom level 0.0. This value serves as the baseline for all conversion calculations. |

---

## Abstract Methods

### zoomLevelToAltitude

#### Signature
```kotlin
abstract fun zoomLevelToAltitude(
    zoomLevel: Double,
    latitude: Double,
    tilt: Double
): Double
```

#### Description
Converts a given map zoom level to the corresponding camera altitude in meters. The calculation takes into account the camera's latitude and tilt to provide a more accurate altitude for a consistent viewing experience across different perspectives.

#### Parameters
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `zoomLevel` | `Double` | The map zoom level to convert. |
| `latitude` | `Double` | The current latitude of the camera in degrees. |
| `tilt` | `Double` | The current tilt of the camera in degrees from nadir (0 = looking straight down). |

#### Returns
| Type | Description |
| :--- | :--- |
| `Double` | The calculated camera altitude in meters. |

---

### altitudeToZoomLevel

#### Signature
```kotlin
abstract fun altitudeToZoomLevel(
    altitude: Double,
    latitude: Double,
    tilt: Double
): Double
```

#### Description
Converts a given camera altitude in meters to the corresponding map zoom level. This is the inverse operation of `zoomLevelToAltitude`. The calculation also accounts for the camera's latitude and tilt.

#### Parameters
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `altitude` | `Double` | The camera altitude in meters to convert. |
| `latitude` | `Double` | The current latitude of the camera in degrees. |
| `tilt` | `Double` | The current tilt of the camera in degrees from nadir. |

#### Returns
| Type | Description |
| :--- | :--- |
| `Double` | The calculated map zoom level. |

---

## Companion Object Constants

These constants provide default values and constraints used within conversion calculations.

| Constant | Value | Description |
| :--- | :--- | :--- |
| `DEFAULT_ZOOM0_ALTITUDE` | `171_319_879.0` | The default altitude in meters for zoom level 0, calibrated to approximate Google Maps' visible regions. |
| `ZOOM_FACTOR` | `2.0` | The multiplier used for zoom level calculations. Each integer zoom level change typically halves or doubles the view. |
| `MIN_ZOOM_LEVEL` | `0.0` | The minimum allowed zoom level. |
| `MAX_ZOOM_LEVEL` | `22.0` | The maximum allowed zoom level. |
| `MIN_ALTITUDE` | `100.0` | The minimum allowed camera altitude in meters. |
| `MAX_ALTITUDE` | `50_000_000.0` | The maximum allowed camera altitude in meters. |
| `MIN_COS_LAT` | `0.01` | The minimum cosine of latitude, used to prevent division by zero near the poles. |
| `MIN_COS_TILT` | `0.05` | The minimum cosine of tilt, used to prevent division by zero at extreme tilt angles. |
| `WEB_MERCATOR_INITIAL_MPP_256` | `156_543.033_928` | The initial meters-per-pixel resolution for a 256px tile at zoom level 0 in the Web Mercator projection. |

---

## Example

Since `AbstractZoomAltitudeConverter` is an abstract class, you must create a concrete implementation to use it. The following example demonstrates how to create a simple linear converter.

```kotlin
import kotlin.math.cos
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.max

// A simple, hypothetical implementation of the abstract class.
class LinearZoomAltitudeConverter(
    zoom0Altitude: Double = DEFAULT_ZOOM0_ALTITUDE
) : AbstractZoomAltitudeConverter(zoom0Altitude) {

    override fun zoomLevelToAltitude(
        zoomLevel: Double,
        latitude: Double,
        tilt: Double
    ): Double {
        // A simplified model that ignores latitude and tilt for this example.
        // A real implementation would have a more complex formula.
        val altitude = zoom0Altitude / ZOOM_FACTOR.pow(zoomLevel)
        return altitude.coerceIn(MIN_ALTITUDE, MAX_ALTITUDE)
    }

    override fun altitudeToZoomLevel(
        altitude: Double,
        latitude: Double,
        tilt: Double
    ): Double {
        // The inverse of the simplified model above.
        if (altitude <= 0) return MAX_ZOOM_LEVEL
        val zoomLevel = log2(zoom0Altitude / altitude)
        return zoomLevel.coerceIn(MIN_ZOOM_LEVEL, MAX_ZOOM_LEVEL)
    }
}

// Usage of the concrete implementation
fun main() {
    val converter = LinearZoomAltitudeConverter()

    val zoomLevel = 10.0
    val latitude = 40.7128 // New York City
    val tilt = 0.0

    // Convert zoom level to altitude
    val altitude = converter.zoomLevelToAltitude(zoomLevel, latitude, tilt)
    println("Zoom level $zoomLevel corresponds to an altitude of ${altitude.toInt()} meters.")
    // Output: Zoom level 10.0 corresponds to an altitude of 167304 meters.

    // Convert altitude back to zoom level
    val newZoomLevel = converter.altitudeToZoomLevel(altitude, latitude, tilt)
    println("An altitude of ${altitude.toInt()} meters corresponds to zoom level $newZoomLevel.")
    // Output: An altitude of 167304 meters corresponds to zoom level 10.0.
}
```