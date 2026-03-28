Of course! Here is a high-quality SDK document for the provided `GeoRectBounds` class.

---

# GeoRectBounds

## Class: GeoRectBounds

### Description

Represents a rectangular geographical area defined by a southwest and a northeast `GeoPoint`. This class is designed to correctly handle bounding boxes that cross the antimeridian (180th meridian).

An empty `GeoRectBounds` can be created by providing no arguments to the constructor. The bounds can then be defined by adding points using the `extend` method.

### Signature

```kotlin
class GeoRectBounds(
    southWest: GeoPoint? = null,
    northEast: GeoPoint? = null
)
```

### Parameters

| Parameter   | Type       | Description                                        |
| :---------- | :--------- | :------------------------------------------------- |
| `southWest` | `GeoPoint?` | The southwest corner of the bounding box. Optional. |
| `northEast` | `GeoPoint?` | The northeast corner of the bounding box. Optional. |

---

## Properties

### isEmpty

#### Signature

```kotlin
val isEmpty: Boolean
```

#### Description

Returns `true` if the bounds have not been initialized with at least one point, `false` otherwise.

---

### southWest

#### Signature

```kotlin
val southWest: GeoPoint?
```

#### Description

The southwest corner `GeoPoint` of the bounding box. Returns `null` if the bounds are empty.

---

### northEast

#### Signature

```kotlin
val northEast: GeoPoint?
```

#### Description

The northeast corner `GeoPoint` of the bounding box. Returns `null` if the bounds are empty.

---

### center

#### Signature

```kotlin
val center: GeoPoint?
```

#### Description

Calculates the geographical center of the bounding box. Correctly handles bounds that cross the antimeridian. Returns `null` if the bounds are empty.

---

## Methods

### extend

#### Signature

```kotlin
fun extend(point: GeoPointInterface)
```

#### Description

Expands the bounding box to include the given geographical point. If the bounds are empty, they will be initialized to the location of this point. This method modifies the current `GeoRectBounds` instance.

#### Parameters

| Parameter | Type                | Description                               |
| :-------- | :------------------ | :---------------------------------------- |
| `point`   | `GeoPointInterface` | The geographical point to include in the bounds. |

---

### contains

#### Signature

```kotlin
fun contains(point: GeoPointInterface): Boolean
```

#### Description

Checks if the given geographical point is within this bounding box (inclusive). This check correctly handles bounds that cross the antimeridian.

#### Parameters

| Parameter | Type                | Description                                  |
| :-------- | :------------------ | :------------------------------------------- |
| `point`   | `GeoPointInterface` | The point to check for containment.          |

#### Returns

`Boolean` - `true` if the point is inside the bounds, `false` otherwise. Returns `false` if the bounds are empty.

---

### union

#### Signature

```kotlin
fun union(other: GeoRectBounds): GeoRectBounds
```

#### Description

Computes the union of this bounding box and another `GeoRectBounds`. It returns a new `GeoRectBounds` that encompasses both of the original bounds.

#### Parameters

| Parameter | Type            | Description                               |
| :-------- | :-------------- | :---------------------------------------- |
| `other`   | `GeoRectBounds` | The other bounding box to combine with this one. |

#### Returns

`GeoRectBounds` - A new `GeoRectBounds` instance representing the union of the two bounds.

---

### toSpan

#### Signature

```kotlin
fun toSpan(): GeoPoint?
```

#### Description

Calculates the latitudinal and longitudinal span of the bounding box.

#### Returns

`GeoPoint?` - A `GeoPoint` where the `latitude` represents the latitudinal span in degrees and the `longitude` represents the longitudinal span in degrees. Returns `null` if the bounds are empty.

---

### toUrlValue

#### Signature

```kotlin
fun toUrlValue(precision: Int = 6): String
```

#### Description

Formats the bounding box coordinates into a single comma-separated string, suitable for use in URL parameters. The format is `south,west,north,east`.

#### Parameters

| Parameter   | Type  | Description                                                              |
| :---------- | :---- | :----------------------------------------------------------------------- |
| `precision` | `Int` | The number of decimal places to use for formatting the coordinates. Defaults to `6`. |

#### Returns

`String` - A string representation of the bounds. For empty bounds, it returns `"1.0,180.0,-1.0,-180.0"`.

---

### expandedByDegrees

#### Signature

```kotlin
fun expandedByDegrees(latPad: Double, lonPad: Double): GeoRectBounds
```

#### Description

Creates a new `GeoRectBounds` instance that is expanded from the original by a specified amount in every direction. The padding values are added to the north and east boundaries and subtracted from the south and west boundaries. This method correctly handles expansion across the antimeridian.

#### Parameters

| Parameter | Type     | Description                                        |
| :-------- | :------- | :------------------------------------------------- |
| `latPad`  | `Double` | The number of degrees to expand latitude-wise (north and south). |
| `lonPad`  | `Double` | The number of degrees to expand longitude-wise (east and west).  |

#### Returns

`GeoRectBounds` - A new, expanded `GeoRectBounds` instance.

---

### intersects

#### Signature

```kotlin
fun intersects(other: GeoRectBounds): Boolean
```

#### Description

Determines if this bounding box has any overlap with another `GeoRectBounds`. The check is inclusive of the boundaries and correctly handles cases where one or both bounds cross the antimeridian.

#### Parameters

| Parameter | Type            | Description                               |
| :-------- | :-------------- | :---------------------------------------- |
| `other`   | `GeoRectBounds` | The other bounding box to check for intersection. |

#### Returns

`Boolean` - `true` if the two bounding boxes intersect, `false` otherwise. Returns `false` if either of the bounds is empty.

---

## Example

```kotlin
// Assuming GeoPoint and GeoPointInterface are defined
// data class GeoPoint(val latitude: Double, val longitude: Double) : GeoPointInterface

fun main() {
    // 1. Create an empty bounds and extend it with points
    val bounds = GeoRectBounds()
    println("Is bounds empty initially? ${bounds.isEmpty}") // true

    bounds.extend(GeoPoint(34.0, -118.0)) // Los Angeles
    bounds.extend(GeoPoint(40.7, -74.0))  // New York

    println("Is bounds empty after extend? ${bounds.isEmpty}") // false
    println("Bounds after adding LA and NYC: $bounds")
    // Output: Bounds after adding LA and NYC: ((34.0, -118.0), (40.7, -74.0))

    // 2. Check if a point is contained within the bounds
    val chicago = GeoPoint(41.8, -87.6)
    val tokyo = GeoPoint(35.6, 139.6)
    println("Does bounds contain Chicago? ${bounds.contains(chicago)}") // true
    println("Does bounds contain Tokyo? ${bounds.contains(tokyo)}")   // false

    // 3. Get the center of the bounds
    val centerPoint = bounds.center
    println("Center of the bounds: $centerPoint")

    // 4. Create another bounds and find the union
    val europeBounds = GeoRectBounds(
        southWest = GeoPoint(48.8, 2.3),  // Paris
        northEast = GeoPoint(52.5, 13.4) // Berlin
    )
    val combinedBounds = bounds.union(europeBounds)
    println("Combined bounds of US and Europe: $combinedBounds")

    // 5. Create bounds that cross the antimeridian
    val pacificBounds = GeoRectBounds()
    pacificBounds.extend(GeoPoint(21.3, -157.8)) // Honolulu
    pacificBounds.extend(GeoPoint(-33.8, 151.2)) // Sydney
    println("Pacific bounds (crosses antimeridian): $pacificBounds")
    // Note: west longitude > east longitude indicates antimeridian crossing
    // Output: Pacific bounds (crosses antimeridian): ((-33.8, 151.2), (21.3, -157.8))

    // 6. Check for intersection
    println("Do US and Europe bounds intersect? ${bounds.intersects(europeBounds)}") // false
    val hawaiiBounds = GeoRectBounds(southWest = GeoPoint(19.0, -160.0), northEast = GeoPoint(22.0, -154.0))
    println("Do Pacific and Hawaii bounds intersect? ${pacificBounds.intersects(hawaiiBounds)}") // true

    // 7. Get URL-friendly string
    println("URL value for US bounds: ${bounds.toUrlValue(precision = 2)}")
    // Output: URL value for US bounds: 34.00,-118.00,40.70,-74.00
}
```