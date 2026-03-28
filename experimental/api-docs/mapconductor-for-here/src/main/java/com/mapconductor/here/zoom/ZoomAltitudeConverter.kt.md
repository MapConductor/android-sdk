# SDK Documentation: ZoomAltitudeConverter

This document provides a detailed reference for the `ZoomAltitudeConverter` class and its associated functions, designed for converting between map zoom levels and camera altitudes within the HERE Maps SDK context.

## `ZoomAltitudeConverter`

### Description

The `ZoomAltitudeConverter` class provides a concrete implementation for converting between camera altitude and map zoom level, specifically tailored for the HERE Maps SDK. It extends `AbstractZoomAltitudeConverter` and factors in variables like latitude and camera tilt to provide accurate conversions, accounting for the map's projection.

### Constructor

#### Signature

```kotlin
ZoomAltitudeConverter(
    zoom0Altitude: Double = DEFAULT_ZOOM0_ALTITUDE
)
```

#### Description

Creates an instance of the `ZoomAltitudeConverter`.

#### Parameters

| Parameter       | Type   | Description                                                                                             |
| :-------------- | :----- | :------------------------------------------------------------------------------------------------------ |
| `zoom0Altitude` | Double | Optional. The camera altitude in meters that corresponds to zoom level 0 at the equator. Defaults to `DEFAULT_ZOOM0_ALTITUDE`. |

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

Calculates the camera altitude in meters required to display a specific map zoom level. The calculation is sensitive to the current latitude and camera tilt angle.

#### Parameters

| Parameter   | Type   | Description                                                              |
| :---------- | :----- | :----------------------------------------------------------------------- |
| `zoomLevel` | Double | The target HERE Maps zoom level.                                         |
| `latitude`  | Double | The current latitude of the map's center, in degrees.                    |
| `tilt`      | Double | The current camera tilt angle in degrees, where 0 is looking straight down. |

#### Returns

`Double` - The calculated camera altitude in meters, clamped within a valid range.

#### Example

```kotlin
val converter = ZoomAltitudeConverter()
val targetZoom = 15.5
val currentLatitude = 48.8584 // Paris
val currentTilt = 30.0

val requiredAltitude = converter.zoomLevelToAltitude(targetZoom, currentLatitude, currentTilt)
println("To achieve zoom level $targetZoom, set altitude to $requiredAltitude meters.")
// Example output: To achieve zoom level 15.5, set altitude to 1098.5 meters.
```

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

Calculates the map zoom level that corresponds to a given camera altitude. This is the inverse operation of `zoomLevelToAltitude`.

#### Parameters

| Parameter  | Type   | Description                                                              |
| :--------- | :----- | :----------------------------------------------------------------------- |
| `altitude` | Double | The current camera altitude above the map, in meters.                    |
| `latitude` | Double | The current latitude of the map's center, in degrees.                    |
| `tilt`     | Double | The current camera tilt angle in degrees, where 0 is looking straight down. |

#### Returns

`Double` - The calculated HERE Maps zoom level, clamped within a valid range.

#### Example

```kotlin
val converter = ZoomAltitudeConverter()
val currentAltitude = 1100.0 // meters
val currentLatitude = 48.8584 // Paris
val currentTilt = 30.0

val zoomLevel = converter.altitudeToZoomLevel(currentAltitude, currentLatitude, currentTilt)
println("At an altitude of $currentAltitude meters, the zoom level is approximately $zoomLevel.")
// Example output: At an altitude of 1100.0 meters, the zoom level is approximately 15.49.
```

---

## Companion Object

The `ZoomAltitudeConverter` class contains a companion object with utility functions and constants for zoom level conversions between different map provider standards.

### Companion Object Functions

#### `hereZoomToGoogleZoom`

##### Signature

```kotlin
fun hereZoomToGoogleZoom(
    hereZoom: Double,
    latitude: Double
): Double
```

##### Description

Converts a HERE Maps zoom level to an equivalent "Google-like" (Web Mercator) zoom level. The conversion is latitude-dependent to account for differences in map projection scaling between the two systems.

##### Parameters

| Parameter  | Type   | Description                                           |
| :--------- | :----- | :---------------------------------------------------- |
| `hereZoom` | Double | The zoom level from the HERE Maps SDK.                |
| `latitude` | Double | The current latitude of the map's center, in degrees. |

##### Returns

`Double` - The equivalent Google-like zoom level.

##### Example

```kotlin
val hereZoom = 14.0
val latitude = 60.1699 // Helsinki
val googleZoom = ZoomAltitudeConverter.hereZoomToGoogleZoom(hereZoom, latitude)
println("HERE zoom $hereZoom at latitude $latitude is equivalent to Google zoom $googleZoom.")
// Example output: HERE zoom 14.0 at latitude 60.1699 is equivalent to Google zoom 13.0.
```

---

#### `googleZoomToHereZoom`

##### Signature

```kotlin
fun googleZoomToHereZoom(
    googleZoom: Double,
    latitude: Double
): Double
```

##### Description

Converts a "Google-like" (Web Mercator) zoom level to an equivalent HERE Maps zoom level. This is the inverse operation of `hereZoomToGoogleZoom`.

##### Parameters

| Parameter    | Type   | Description                                           |
| :----------- | :----- | :---------------------------------------------------- |
| `googleZoom` | Double | The Google-like (Web Mercator) zoom level.            |
| `latitude`   | Double | The current latitude of the map's center, in degrees. |

##### Returns

`Double` - The equivalent HERE Maps zoom level.

##### Example

```kotlin
val googleZoom = 13.0
val latitude = 60.1699 // Helsinki
val hereZoom = ZoomAltitudeConverter.googleZoomToHereZoom(googleZoom, latitude)
println("Google zoom $googleZoom at latitude $latitude is equivalent to HERE zoom $hereZoom.")
// Example output: Google zoom 13.0 at latitude 60.1699 is equivalent to HERE zoom 14.0.
```

---

### Companion Object Properties

#### `HERE_ZOOM_TO_GOOGLE_ZOOM_AT_EQUATOR`

##### Signature

```kotlin
const val HERE_ZOOM_TO_GOOGLE_ZOOM_AT_EQUATOR: Double
```

##### Description

An empirical constant representing the offset between HERE Maps zoom and "Google-like" zoom at the equator (latitude ≈ 0). The relationship is approximately:
`GoogleZoom ≈ HereZoom + HERE_ZOOM_TO_GOOGLE_ZOOM_AT_EQUATOR`