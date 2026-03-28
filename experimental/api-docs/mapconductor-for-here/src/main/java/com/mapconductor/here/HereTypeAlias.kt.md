Of course! Here is the high-quality SDK documentation for the provided code snippet.

# HERE Map Object Type Aliases

This document provides details on the type aliases used to represent native HERE map objects within the MapConductor framework. These aliases provide a consistent abstraction layer, simplifying interaction with the underlying HERE SDK implementation.

---

### `HereActualMarker`

A type alias for the native HERE SDK map marker.

#### Signature
```kotlin
typealias HereActualMarker = com.here.sdk.mapview.MapMarker
```

#### Description
`HereActualMarker` represents a single marker object displayed on the map. It is a direct alias for the `MapMarker` class from the HERE SDK. Any object of this type is the actual, underlying marker instance used by the map view.

**Underlying Type:** `com.here.sdk.mapview.MapMarker`

---

### `HereActualCircle`

A type alias for a circle, which is implemented as a native HERE SDK polygon.

#### Signature
```kotlin
typealias HereActualCircle = com.here.sdk.mapview.MapPolygon
```

#### Description
`HereActualCircle` represents a circle shape drawn on the map. Internally, a circle is rendered as a high-resolution polygon. This alias points to the underlying `MapPolygon` object that constitutes the circle.

**Underlying Type:** `com.here.sdk.mapview.MapPolygon`

---

### `HereActualPolyline`

A type alias for the native HERE SDK map polyline.

#### Signature
```kotlin
typealias HereActualPolyline = com.here.sdk.mapview.MapPolyline
```

#### Description
`HereActualPolyline` represents a polyline (a series of connected line segments) on the map. It is a direct alias for the `MapPolyline` class from the HERE SDK.

**Underlying Type:** `com.here.sdk.mapview.MapPolyline`

---

### `HereActualPolygon`

A type alias for a polygon, which is represented as a list of native HERE SDK polygon objects.

#### Signature
```kotlin
typealias HereActualPolygon = List<com.here.sdk.mapview.MapPolygon>
```

#### Description
`HereActualPolygon` represents a single logical polygon shape on the map. It is defined as a `List` of `MapPolygon` objects to support complex polygons, such as those containing one or more holes (donuts). The first `MapPolygon` in the list typically defines the outer boundary, while subsequent polygons define the inner boundaries (holes).

**Underlying Type:** `List<com.here.sdk.mapview.MapPolygon>`

---

### `HereActualGroundImage`

A type alias for a handle that manages a ground image overlay.

#### Signature
```kotlin
typealias HereActualGroundImage = com.mapconductor.here.groundimage.HereGroundImageHandle
```

#### Description
`HereActualGroundImage` is an alias for a custom `HereGroundImageHandle` class. This class acts as a wrapper and manager for a ground image overlay on the map, abstracting the underlying implementation details of placing and managing the image with the HERE SDK.

**Underlying Type:** `com.mapconductor.here.groundimage.HereGroundImageHandle`