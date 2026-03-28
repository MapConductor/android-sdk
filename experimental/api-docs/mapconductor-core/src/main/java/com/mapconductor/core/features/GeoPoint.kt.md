This document provides a detailed reference for the `GeoPoint` class and its related utility functions.

# `GeoPoint`

A data class representing a geographical point defined by latitude, longitude, and an optional altitude. It provides methods for coordinate manipulation, validation, and formatting.

Note that `GeoPoint` instances are compared for equality with a small tolerance (`1e-7`) to account for floating-point inaccuracies. This behavior also affects the `hashCode` calculation.

## Constructor

### Signature

```kotlin
data class GeoPoint(
    override val latitude: Double,
    override val longitude: Double,
    override val altitude: Double = 0.0,
) : GeoPointInterface
```

### Description

Creates a new `GeoPoint` instance.

### Parameters

| Parameter   | Type     | Description                                    |
| :---------- | :------- | :--------------------------------------------- |
| `latitude`  | `Double` | The latitude of the point in decimal degrees.  |
| `longitude` | `Double` | The longitude of the point in decimal degrees. |
| `altitude`  | `Double` | The altitude of the point in meters. Defaults to `0.0`. |

## Properties

| Property    | Type     | Description                                    |
| :---------- | :------- | :--------------------------------------------- |
| `latitude`  | `Double` | The latitude of the point in decimal degrees.  |
| `longitude` | `Double` | The longitude of the point in decimal degrees. |
| `altitude`  | `Double` | The altitude of the point in meters.           |

## Methods

### `toUrlValue`

Formats the geographical point into a URL-friendly string.

#### Signature

```kotlin
fun toUrlValue(precision: Int = 6): String
```

#### Description

Converts the `latitude` and `longitude` of the point into a single string with the format `"<latitude>,<longitude>"`.

#### Parameters

| Parameter   | Type  | Description                                                              |
| :---------- | :---- | :----------------------------------------------------------------------- |
| `precision` | `Int` | The number of decimal places to use for formatting the coordinates. Defaults to `6`. |

#### Returns

A `String` representation of the coordinates, suitable for use in URLs.

#### Example

```kotlin
val point = GeoPoint(40.7128, -74.0060)
val urlValue = point.toUrlValue() // "40.712800,-74.006000"

val urlValuePrecise = point.toUrlValue(precision = 8) // "40.71280000,-74.00600000"
```

### `wrap`

Creates a new `GeoPoint` by wrapping the coordinates to handle values outside standard geographical ranges.

#### Signature

```kotlin
override fun wrap(): GeoPointInterface
```

#### Description

This method normalizes coordinates that are out of the standard `[-90, 90]` latitude and `[-180, 180]` longitude bounds.

-   **Latitude**: If the latitude is beyond the poles (e.g., > 90 or < -90), it wraps around. When wrapping over a pole, the longitude is flipped by 180 degrees.
-   **Longitude**: The longitude is normalized to fit within the `[-180, 180]` range.

#### Returns

A new `GeoPointInterface` instance with wrapped coordinates.

#### Example

```kotlin
// Latitude wraps from North Pole to South Pole, longitude is flipped
val point1 = GeoPoint(latitude = 95.0, longitude = 10.0)
val wrappedPoint1 = point1.wrap() // GeoPoint(latitude = -85.0, longitude = -170.0)

// Longitude wraps around the 180th meridian
val point2 = GeoPoint(latitude = 50.0, longitude = 200.0)
val wrappedPoint2 = point2.wrap() // GeoPoint(latitude = 50.0, longitude = -160.0)
```

## Companion Object

### `GeoPoint.fromLatLong`

Creates a `GeoPoint` instance from latitude and longitude values.

#### Signature

```kotlin
fun fromLatLong(latitude: Double, longitude: Double): GeoPoint
```

#### Parameters

| Parameter   | Type     | Description                                   |
| :---------- | :------- | :-------------------------------------------- |
| `latitude`  | `Double` | The latitude of the point in decimal degrees. |
| `longitude` | `Double` | The longitude of the point in decimal degrees.|

#### Returns

A new `GeoPoint` instance.

### `GeoPoint.fromLongLat`

Creates a `GeoPoint` instance from longitude and latitude values.

#### Signature

```kotlin
fun fromLongLat(longitude: Double, latitude: Double): GeoPoint
```

#### Parameters

| Parameter   | Type     | Description                                   |
| :---------- | :------- | :-------------------------------------------- |
| `longitude` | `Double` | The longitude of the point in decimal degrees.|
| `latitude`  | `Double` | The latitude of the point in decimal degrees. |

#### Returns

A new `GeoPoint` instance.

### `GeoPoint.from`

Converts any object implementing `GeoPointInterface` into a `GeoPoint` instance.

#### Signature

```kotlin
fun from(position: GeoPointInterface): GeoPoint
```

#### Description

If the provided `position` is already a `GeoPoint`, it is returned directly. Otherwise, a new `GeoPoint` is created from the `GeoPointInterface`'s properties.

#### Parameters

| Parameter  | Type                | Description                                  |
| :--------- | :------------------ | :------------------------------------------- |
| `position` | `GeoPointInterface` | An object that implements `GeoPointInterface`. |

#### Returns

A `GeoPoint` instance.

---

# Extension Functions

These functions extend the `GeoPointInterface` and can be called on any `GeoPoint` object.

## `normalize`

Creates a new `GeoPoint` by clamping coordinates to valid geographical ranges.

### Signature

```kotlin
fun GeoPointInterface.normalize(): GeoPoint
```

### Description

This function ensures that the coordinates of a geographical point are within standard bounds.
-   **Latitude**: Clamped to the `[-90.0, 90.0]` range.
-   **Longitude**: Normalized to the `[-180.0, 180.0]` range.

Unlike `wrap()`, `normalize()` does not wrap coordinates around the globe but forces them to the nearest valid boundary.

### Returns

A new `GeoPoint` instance with normalized coordinates.

### Example

```kotlin
val point = GeoPoint(latitude = 95.0, longitude = 200.0)
val normalizedPoint = point.normalize() // GeoPoint(latitude = 90.0, longitude = -160.0)
```

## `isValid`

Checks if the coordinates of a `GeoPointInterface` are within valid geographical ranges.

### Signature

```kotlin
fun GeoPointInterface.isValid(): Boolean
```

### Description

Validates that the latitude is within `[-90.0, 90.0]` and the longitude is within `[-180.0, 180.0]`.

### Returns

`true` if the coordinates are valid, `false` otherwise.

### Example

```kotlin
val validPoint = GeoPoint(45.0, 90.0)
println(validPoint.isValid()) // true

val invalidPoint = GeoPoint(95.0, 90.0)
println(invalidPoint.isValid()) // false
```