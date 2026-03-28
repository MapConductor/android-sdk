### `expandBounds`

#### Signature

```kotlin
fun expandBounds(bounds: GeoRectBounds, margin: Double): GeoRectBounds
```

#### Description

Calculates a new `GeoRectBounds` by expanding the given `bounds` by a specified margin factor. The expansion is proportional to the original dimensions (latitude and longitude span) and is applied outward from the center of the bounds.

If the input `bounds` are empty or if their center or span cannot be determined, the original `bounds` object is returned unmodified.

#### Parameters

| Parameter | Type            | Description                                                                                                                            |
|-----------|-----------------|----------------------------------------------------------------------------------------------------------------------------------------|
| `bounds`  | `GeoRectBounds` | The original rectangular geographic bounds to expand.                                                                                  |
| `margin`  | `Double`        | The proportional factor to expand the bounds. For example, a value of `0.1` increases the total width and height by 10% (5% on each side). |

#### Returns

| Type            | Description                                                                                                                            |
|-----------------|----------------------------------------------------------------------------------------------------------------------------------------|
| `GeoRectBounds` | A new `GeoRectBounds` instance representing the expanded area, or the original `bounds` if they could not be expanded (e.g., if empty). |

#### Example

The following example demonstrates how to expand a `GeoRectBounds` object by a 25% margin.

```kotlin
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoRectBounds
import com.mapconductor.core.spherical.expandBounds

fun main() {
    // 1. Define the initial geographical bounds.
    //    Let's assume the bounds span 2 degrees in latitude and 2 degrees in longitude.
    val initialBounds = GeoRectBounds().apply {
        extend(GeoPoint(latitude = 40.0, longitude = -80.0)) // Southwest corner
        extend(GeoPoint(latitude = 42.0, longitude = -78.0)) // Northeast corner
    }
    println("Original Bounds: $initialBounds")
    // Assuming a toString() output like:
    // > Original Bounds: GeoRectBounds(sw=[lat=40.0, lon=-80.0], ne=[lat=42.0, lon=-78.0])

    // 2. Define the expansion margin (25%).
    val margin = 0.25

    // 3. Call expandBounds to get the new, larger bounds.
    val expandedBounds = expandBounds(initialBounds, margin)

    println("Expanded Bounds (25% margin): $expandedBounds")
    // The original span is 2 degrees lat and 2 degrees lon.
    // The margin adds (2 * 0.25) = 0.5 degrees to the total span of each dimension.
    // This means 0.25 degrees are added to each side (top, bottom, left, right).
    // Expected new SW corner: lat=39.75, lon=-80.25
    // Expected new NE corner: lat=42.25, lon=-77.75
    // > Expanded Bounds (25% margin): GeoRectBounds(sw=[lat=39.75, lon=-80.25], ne=[lat=42.25, lon=-77.75])
}
```