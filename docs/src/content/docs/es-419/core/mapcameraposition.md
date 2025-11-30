---
title: "MapCameraPosition"
---

`MapCameraPosition` representa la posición, orientación y área visible de la cámara en el mapa. Define hacia dónde mira la cámara, cuánto mapa se ve y desde qué perspectiva.

## Interfaz e implementación

### Interfaz MapCameraPosition

```kotlin
interface MapCameraPosition {
    val position: GeoPoint
    val zoom: Double
    val bearing: Double
    val tilt: Double
    val paddings: MapPaddings?
    val visibleRegion: VisibleRegion?
}
```

### MapCameraPositionImpl

La implementación principal proporciona datos inmutables de la posición de la cámara:

```kotlin
data class MapCameraPositionImpl(
    override val position: GeoPointImpl,
    override val zoom: Double = 0.0,
    override val bearing: Double = 0.0,
    override val tilt: Double = 0.0,
    override val paddings: MapPaddings? = MapPaddingsImpl.Zeros,
    override val visibleRegion: VisibleRegion? = null
) : MapCameraPosition
```

## Propiedades

### Posición de la cámara

- **`position: GeoPoint`**: punto geográfico central de la vista de la cámara.
- **`zoom: Double`**: nivel de zoom (aproximadamente alineado con la escala de Google Maps).
- **`bearing: Double`**: rumbo en grados (0 = norte, 90 = este).
- **`tilt: Double`**: ángulo de inclinación en grados (0 = vista cenital, 90 = horizontal).

### Configuración de vista

- **`paddings: MapPaddings?`**: márgenes del viewport que afectan a la región visible.
- **`visibleRegion: VisibleRegion?`**: límites geográficos que realmente se ven en pantalla.

## Formas de creación

### Posición por defecto

```kotlin
// Posición de cámara por defecto en el origen
val defaultPosition = MapCameraPositionImpl.Default
```

### Posición personalizada

```kotlin
// Posición básica de cámara
val sanFrancisco = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    zoom = 15.0
)

// Cámara con rumbo e inclinación
val aerialView = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    zoom = 18.0,
    bearing = 45.0,  // dirección noreste
    tilt = 60.0      // vista en ángulo
)

// Cámara con rellenos para dejar espacio a la UI
val paddedView = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    zoom = 14.0,
    paddings = MapPaddingsImpl(
        left = 0,
        top = 200,
        right = 0,
        bottom = 0
    )
)
```

### Uso con MapViewState

```kotlin
val camera = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    zoom = 14.0,
    tilt = 45.0,
    bearing = 90.0
)

val mapViewState = rememberGoogleMapViewState(
    cameraPosition = camera
)
```

## Buenas prácticas

- Ajusta el zoom en función del tipo de contenido que quieras mostrar (ciudad, país, mundo, etc.).
- Usa `paddings` cuando tengas overlays de UI (como paneles o barras) que no deban tapar elementos importantes del mapa.
- Combina `bearing` y `tilt` para crear vistas 3D atractivas, pero vuelve a valores neutros cuando la legibilidad sea prioritaria.
