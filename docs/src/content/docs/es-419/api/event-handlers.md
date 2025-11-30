---
title: "Manejadores de eventos"
---

MapConductor proporciona un sistema completo de manejo de eventos para las interacciones de usuario con el mapa y sus componentes. Todos los eventos se gestionan a través del componente `MapViewContainer` y se pasan como parámetros al composable `MapView`.

```kotlin
MapView(
    ...,
    onMapLoaded: OnMapLoadedHandler? = null,
    onMapClick: OnMapEventHandler? = null,
    onCameraMoveStart: OnCameraMoveHandler? = null,
    onCameraMove: OnCameraMoveHandler? = null,
    onCameraMoveEnd: OnCameraMoveHandler? = null,
    onMarkerClick: OnMarkerEventHandler? = null,
    onMarkerDragStart: OnMarkerEventHandler? = null,
    onMarkerDrag: OnMarkerEventHandler? = null,
    onMarkerDragEnd: OnMarkerEventHandler? = null,
    onMarkerAnimateStart: OnMarkerEventHandler? = null,
    onMarkerAnimateEnd: OnMarkerEventHandler? = null,
    onCircleClick: OnCircleEventHandler? = null,
    onPolylineClick: OnPolylineEventHandler? = null,
    onPolygonClick: OnPolygonEventHandler? = null,
    onGroundImageClick: OnGroundImageEventHandler? = null,
) { }
```

## Eventos del mapa

### Inicialización del mapa

- Se llama cuando el mapa ha terminado de cargarse y está listo para recibir interacciones.

    ```kotlin
    onMapLoaded: OnMapLoadedHandler? = null
    ```

- Ejemplo:
    ```kotlin
    MapView(
        ...
        onMapLoaded = {
            println("Map loaded")
        },
    ) {
        ...
    }
    ```

### Interacción con el mapa

- Se llama cuando el usuario toca el mapa (pero no sobre un overlay).

    ```kotlin
    onMapClick: OnMapEventHandler? = null
    ```

**Datos del evento**: `GeoPoint` – coordenadas geográficas donde se hizo tap.

- Ejemplo:
    ```kotlin
    MapView(
        ...
        onMapClick = { geoPoint ->
            println("Map clicked at: ${geoPoint.latitude}, ${geoPoint.longitude}")
        }
    ) {
        ...
    }
    ```

## Eventos de marcadores

Todos los eventos de marcador reciben un objeto `MarkerState` con el estado actual del marcador.

### Eventos de clic

- Se llama cuando el usuario toca un marcador.

    ```kotlin
    onMarkerClick: OnMarkerEventHandler? = null
    ```

**Datos del evento**: `MarkerState` – estado del marcador clicado.

### Eventos de arrastre

- `onMarkerDragStart`: se llama cuando el usuario comienza a arrastrar un marcador.
- `onMarkerDrag`: se llama mientras el marcador se está arrastrando.
- `onMarkerDragEnd`: se llama cuando el usuario suelta el marcador.

Ejemplo:

```kotlin
MapView(
    ...,
    onMarkerDragStart = { markerState ->
        println("Drag started at: ${markerState.position}")
    },
    onMarkerDrag = { markerState ->
        println("Dragging: ${markerState.position}")
    },
    onMarkerDragEnd = { markerState ->
        println("Drag ended at: ${markerState.position}")
    },
) { ... }
```

### Eventos de animación de marcadores

- `onMarkerAnimateStart`: se llama cuando comienza una animación de marcador.
- `onMarkerAnimateEnd`: se llama cuando termina una animación de marcador.

```kotlin
MapView(
    ...,
    onMarkerAnimateStart = { markerState ->
        println("Animation started for: ${markerState.id}")
    },
    onMarkerAnimateEnd = { markerState ->
        println("Animation ended for: ${markerState.id}")
    },
) { ... }
```

## Eventos de overlays

MapConductor también expone eventos para overlays como círculos, polilíneas, polígonos y ground images.

### Círculos

```kotlin
onCircleClick: OnCircleEventHandler? = null
```

**Datos del evento**: `CircleEvent` – incluye el `CircleState` y el punto clicado.

### Polilíneas

```kotlin
onPolylineClick: OnPolylineEventHandler? = null
```

**Datos del evento**: `PolylineEvent` – incluye el `PolylineState` y el punto clicado.

### Polígonos

```kotlin
onPolygonClick: OnPolygonEventHandler? = null
```

**Datos del evento**: `PolygonEvent` – incluye el `PolygonState` y el punto clicado.

### GroundImage

```kotlin
onGroundImageClick: OnGroundImageEventHandler? = null
```

**Datos del evento**: `GroundImageEvent` – incluye el `GroundImageState` y la posición clicada (cuando está disponible). Solo está soportado en Google Maps.

## Eventos de cámara

Los eventos de movimiento de cámara permiten reaccionar a cambios de posición y zoom.

```kotlin
onCameraMoveStart: OnCameraMoveHandler? = null,
onCameraMove: OnCameraMoveHandler? = null,
onCameraMoveEnd: OnCameraMoveHandler? = null,
```

- `onCameraMoveStart`: se llama cuando la cámara empieza a moverse (por gesto del usuario o animación).
- `onCameraMove`: se llama mientras la cámara se está moviendo.
- `onCameraMoveEnd`: se llama cuando la cámara deja de moverse.

Estos eventos están disponibles en los proveedores que exponen callbacks de movimiento de cámara.
