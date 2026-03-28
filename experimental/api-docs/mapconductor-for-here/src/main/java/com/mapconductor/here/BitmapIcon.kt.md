Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

# BitmapIcon Extensions for HERE SDK

This document provides details on a set of `internal` Kotlin extension functions designed to facilitate the conversion of a custom `BitmapIcon` object into corresponding types required by the HERE SDK. These helpers streamline the process of creating map markers with custom visuals and anchor points.

**Note:** As these functions are marked `internal`, they are intended for use only within the `com.mapconductor.here` module.

---

## `toMapImage()`

Converts a `BitmapIcon` into a `MapImage` object, which is used by the HERE SDK to render images on the map.

### Signature

```kotlin
internal fun BitmapIcon.toMapImage(): MapImage
```

### Description

This extension function transforms a `BitmapIcon` instance into a `MapImage`. It extracts the raw pixel data as a byte array, along with the bitmap's width and height. The image format is consistently set to `ImageFormat.PNG`. The resulting `MapImage` is essential for creating visual representations of map markers.

### Returns

| Type | Description |
| :--- | :--- |
| `MapImage` | A `MapImage` object containing the icon's visual data, ready to be used for creating a `MapMarker`. |

### Example

```kotlin
import com.here.sdk.mapview.MapImage
import com.mapconductor.core.marker.BitmapIcon

// Assume 'customIcon' is an instance of your BitmapIcon class
val customIcon: BitmapIcon = createCustomBitmapIcon()

// Convert the BitmapIcon to a HERE SDK MapImage
val mapImage: MapImage = customIcon.toMapImage()

// 'mapImage' can now be used to create a MapMarker
// val mapMarker = MapMarker(geoCoordinates, mapImage)
```

---

## `toAnchor2D()`

Converts the anchor point of a `BitmapIcon` into an `Anchor2D` object for precise marker placement.

### Signature

```kotlin
internal fun BitmapIcon.toAnchor2D(): Anchor2D
```

### Description

This extension function extracts the normalized anchor coordinates (`x`, `y`) from a `BitmapIcon` and uses them to create an `Anchor2D` object. The anchor point determines which pixel of the marker image is placed exactly at the marker's geographical coordinates. The coordinates are normalized, where `(0.0, 0.0)` is the top-left corner and `(1.0, 1.0)` is the bottom-right corner.

### Returns

| Type | Description |
| :--- | :--- |
| `Anchor2D` | An `Anchor2D` object representing the attachment point for the marker image. |

### Example

```kotlin
import com.here.sdk.core.Anchor2D
import com.mapconductor.core.marker.BitmapIcon

// Assume 'customIcon' is an instance of your BitmapIcon class
// with its anchor property set.
val customIcon: BitmapIcon = createCustomBitmapIconWithAnchor()

// Convert the icon's anchor to a HERE SDK Anchor2D
val markerAnchor: Anchor2D = customIcon.toAnchor2D()

// 'markerAnchor' can now be applied to a MapMarker to set its anchor point
// val mapMarker = MapMarker(geoCoordinates, mapImage, markerAnchor)
```