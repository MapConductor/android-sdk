Of course! Here is the high-quality SDK documentation for the provided Kotlin code snippet.

---

# WGS84

A singleton object that provides methods for Spherical Mercator projection. This projection is widely used in web mapping systems (like Google Maps and OpenStreetMap) to represent the WGS84 geographic coordinate system on a 2D plane.

This object facilitates the conversion between geographic coordinates (`GeoPointInterface`) and 2D world coordinates (`Offset`), which are essential for rendering map data on a screen. The projection calculations are based on a standard 256x256 pixel map tile.

## Methods

### project

```kotlin
fun project(position: GeoPointInterface): Offset
```

Projects a geographic coordinate (`GeoPointInterface`) into a 2D world coordinate (`Offset`) using the Spherical Mercator projection.

#### Parameters

| Parameter  | Type                | Description                                       |
|------------|---------------------|---------------------------------------------------|
| `position` | `GeoPointInterface` | The geographic point (latitude/longitude) to project. |

#### Returns

An `Offset` object representing the x and y coordinates on the 2D projected plane, scaled to a 256x256 world space.

#### Example

```kotlin
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.projection.WGS84
import androidx.compose.ui.geometry.Offset

// Define a geographic point for Lisbon, Portugal
val lisbon = GeoPoint(latitude = 38.7223, longitude = -9.1393)

// Project the geographic point to 2D coordinates
val projectedPoint: Offset = WGS84.project(lisbon)

// The result is the 2D representation of the location
println("Projected coordinates: $projectedPoint")
// Expected output might be similar to: Offset(125.55755, 96.3428)
```

---

### unproject

```kotlin
fun unproject(point: Offset): GeoPointInterface
```

Performs the inverse projection, converting a 2D world coordinate (`Offset`) back into a geographic coordinate (`GeoPointInterface`). This is useful for determining the latitude and longitude corresponding to a specific point on the map, such as a user's tap location.

#### Parameters

| Parameter | Type     | Description                                                              |
|-----------|----------|--------------------------------------------------------------------------|
| `point`   | `Offset` | The 2D world coordinate (x, y) to unproject, based on a 256x256 tile system. |

#### Returns

A `GeoPointInterface` object representing the corresponding latitude and longitude.

#### Example

```kotlin
import com.mapconductor.core.features.GeoPointInterface
import com.mapconductor.core.projection.WGS84
import androidx.compose.ui.geometry.Offset

// A 2D point from a map interaction (e.g., a tap)
val screenPoint = Offset(125.55755f, 96.3428f)

// Convert the 2D point back to its geographic coordinate
val geoPoint: GeoPointInterface = WGS84.unproject(screenPoint)

// The result is the geographic location (latitude/longitude)
println("Unprojected GeoPoint: Lat=${geoPoint.latitude}, Lon=${geoPoint.longitude}")
// Expected output might be similar to: Unprojected GeoPoint: Lat=38.7223..., Lon=-9.1393...
```