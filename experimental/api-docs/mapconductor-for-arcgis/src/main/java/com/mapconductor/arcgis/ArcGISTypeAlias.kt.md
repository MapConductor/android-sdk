Of course! Here is the high-quality SDK documentation for the provided code snippet.

# Type Aliases

This document outlines the core type aliases used within the `com.mapconductor.arcgis` package. These aliases provide a consistent and abstracted interface over the underlying ArcGIS Maps SDK for Kotlin types, simplifying development and improving code readability. They map common map concepts (like markers, polylines, etc.) to their specific ArcGIS implementation classes.

---

### `ArcGISActualMarker`

**Signature**
```kotlin
typealias ArcGISActualMarker = com.arcgismaps.mapping.view.Graphic
```

**Description**
An alias for the `com.arcgismaps.mapping.view.Graphic` class. This type is used to represent a marker or a point graphic on the map. Using this alias helps abstract the specific ArcGIS implementation for markers, providing a more consistent API within the MapConductor SDK.

---

### `ArcGISActualCircle`

**Signature**
```kotlin
typealias ArcGISActualCircle = com.arcgismaps.mapping.view.Graphic
```

**Description**
An alias for the `com.arcgismaps.mapping.view.Graphic` class. This type represents a circle shape drawn on the map. In the ArcGIS SDK, shapes like circles are typically implemented as `Graphic` objects with a specific geometry. This alias provides a standardized "Circle" type within the MapConductor SDK.

---

### `ArcGISActualPolyline`

**Signature**
```kotlin
typealias ArcGISActualPolyline = com.arcgismaps.mapping.view.Graphic
```

**Description**
An alias for the `com.arcgismaps.mapping.view.Graphic` class. It is used to represent a polyline, which consists of a series of connected line segments, on the map.

---

### `ArcGISActualPolygon`

**Signature**
```kotlin
typealias ArcGISActualPolygon = com.arcgismaps.mapping.view.Graphic
```

**Description**
An alias for the `com.arcgismaps.mapping.view.Graphic` class. It is used to represent a polygon, which is a closed shape defined by a series of connected vertices, on the map.

---

### `ArcGISActualGroundImage`

**Signature**
```kotlin
typealias ArcGISActualGroundImage = com.mapconductor.arcgis.groundimage.ArcGISGroundImageHandle
```

**Description**
An alias for the `com.mapconductor.arcgis.groundimage.ArcGISGroundImageHandle` class. This type represents a handle or a wrapper object for managing a ground image overlay on the map. It abstracts the specific implementation details required to display and manage ground imagery within the ArcGIS environment.