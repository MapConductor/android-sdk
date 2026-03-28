Of course! Here is the high-quality SDK documentation for the provided code snippet.

# Mapbox Type Aliases

This document outlines the type aliases used within the MapConductor Mapbox provider. These aliases serve as an abstraction layer, mapping generic MapConductor concepts to the specific underlying types required by the Mapbox SDK. This allows for a consistent internal API while interacting with Mapbox-specific objects.

---

### `MapboxActualMarker`

A type alias representing the underlying Mapbox object for a single marker.

**Signature**
```kotlin
typealias MapboxActualMarker = Feature
```

**Description**
This alias maps to a `com.mapbox.geojson.Feature`. In the Mapbox SDK, individual markers are typically represented as `Feature` objects with a `Point` geometry and associated properties for styling and metadata. This alias standardizes the representation of a marker within the MapConductor framework.

**Underlying Type**: `com.mapbox.geojson.Feature`

---

### `MapboxActualCircle`

A type alias representing the underlying Mapbox object for a circle.

**Signature**
```kotlin
typealias MapboxActualCircle = Feature
```

**Description**
Similar to `MapboxActualMarker`, this alias also maps to a `com.mapbox.geojson.Feature`. Circles on a Mapbox map are often implemented as styled `Feature` objects that have a `Point` geometry. The visual representation as a circle is achieved through the style layer properties (e.g., `circle-radius`, `circle-color`).

**Underlying Type**: `com.mapbox.geojson.Feature`

---

### `MapboxActualPolyline`

A type alias representing the underlying Mapbox object(s) for a polyline.

**Signature**
```kotlin
typealias MapboxActualPolyline = List<Feature>
```

**Description**
This alias maps to a `List<Feature>`. A polyline can be composed of one or more `Feature` objects. For instance, a simple line is a single `Feature` with a `LineString` geometry, while a more complex polyline with different colored segments or custom start/end caps might be represented as a list of features.

**Underlying Type**: `List<com.mapbox.geojson.Feature>`

---

### `MapboxActualPolygon`

A type alias representing the underlying Mapbox object(s) for a polygon.

**Signature**
```kotlin
typealias MapboxActualPolygon = List<Feature>
```

**Description**
This alias maps to a `List<Feature>`. A polygon is typically represented by a `Feature` with a `Polygon` geometry. Using a `List` allows for more complex representations, such as separating the polygon's fill and its outline (stroke) into two distinct `Feature` objects, each with its own styling.

**Underlying Type**: `List<com.mapbox.geojson.Feature>`

---

### `MapboxActualGroundImage`

A type alias for a handle that manages a ground image overlay.

**Signature**
```kotlin
typealias MapboxActualGroundImage = com.mapconductor.mapbox.groundimage.MapboxGroundImageHandle
```

**Description**
This alias refers to a custom `MapboxGroundImageHandle` class. This handle acts as a wrapper and manager for a ground image (also known as a ground overlay) on the map. It likely encapsulates the logic for adding, removing, and updating the image source and its geographic coordinates.

**Underlying Type**: `com.mapconductor.mapbox.groundimage.MapboxGroundImageHandle`