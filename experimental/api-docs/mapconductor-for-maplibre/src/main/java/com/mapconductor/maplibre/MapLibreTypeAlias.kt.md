Of course! Here is the high-quality SDK documentation for the provided Kotlin code snippet.

# Type Aliases: `com.mapconductor.maplibre`

This document provides details on the core type aliases used within the `com.mapconductor.maplibre` package. These aliases serve as abstractions for the underlying MapLibre objects, providing a clear and consistent API for representing common map elements like markers, polylines, and polygons.

---

## `MapLibreActualMarker`

A type alias representing a single marker on the map.

### Signature

```kotlin
typealias MapLibreActualMarker = org.maplibre.geojson.Feature
```

### Description

`MapLibreActualMarker` is an alias for a MapLibre `Feature` object. It is used throughout the SDK to represent a single point of interest on the map, typically visualized with an icon. The underlying `Feature` is expected to contain a `Point` geometry.

**Underlying Type:** `org.maplibre.geojson.Feature`

---

## `MapLibreActualPolyline`

A type alias representing a polyline on the map.

### Signature

```kotlin
typealias MapLibreActualPolyline = List<Feature>
```

### Description

`MapLibreActualPolyline` represents a polyline, which is a series of connected line segments. It is defined as a `List` of `Feature` objects. This structure allows a single conceptual polyline to be composed of multiple underlying features, which can be useful for advanced styling of the line, its vertices, or its endpoints.

**Underlying Type:** `List<org.maplibre.geojson.Feature>`

---

## `MapLibreActualCircle`

A type alias representing a circle on the map.

### Signature

```kotlin
typealias MapLibreActualCircle = org.maplibre.geojson.Feature
```

### Description

`MapLibreActualCircle` is an alias for a single MapLibre `Feature` that is styled to appear as a circle. This is typically achieved by using a `Feature` with a `Point` geometry and applying MapLibre style layer properties like `circle-radius` and `circle-color`.

**Underlying Type:** `org.maplibre.geojson.Feature`

---

## `MapLibreActualPolygon`

A type alias representing a polygon on the map.

### Signature

```kotlin
typealias MapLibreActualPolygon = List<Feature>
```

### Description

`MapLibreActualPolygon` represents a closed shape on the map. It is defined as a `List` of `Feature` objects. This allows a complex polygon's visual components, such as its fill and stroke (outline), to be managed as separate `Feature` objects, enabling more granular control over styling.

**Underlying Type:** `List<org.maplibre.geojson.Feature>`

---

## `MapLibreActualGroundImage`

A type alias representing a handle to a ground image overlay.

### Signature

```kotlin
typealias MapLibreActualGroundImage = com.mapconductor.maplibre.groundimage.MapLibreGroundImageHandle
```

### Description

`MapLibreActualGroundImage` is an alias for a `MapLibreGroundImageHandle`. This handle is an object used to manage a ground image overlay that has been added to the map. It allows you to reference and interact with the image after its creation.

**Underlying Type:** `com.mapconductor.maplibre.groundimage.MapLibreGroundImageHandle`