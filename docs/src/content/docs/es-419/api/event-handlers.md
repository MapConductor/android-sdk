---
title: "Event Handlers"
---

MapConductor expone varios manejadores de eventos para reaccionar a interacciones del usuario y cambios en el mapa.

## Eventos del mapa

- `onMapViewInitialized`
- `onMapLoaded`
- `onMapClick`

```kotlin
GoogleMapsView(
    state = mapViewState,
    onMapClick = { geoPoint ->
        println("Clicked at: ${geoPoint.latitude}, ${geoPoint.longitude}")
    }
) { /* contenido */ }
```

## Eventos de marcadores

- `onMarkerClick`
- `onMarkerDragStart` / `onMarkerDrag` / `onMarkerDragEnd`
- `onMarkerAnimateStart` / `onMarkerAnimateEnd`

## Eventos de overlays

- `onCircleClick`
- `onPolylineClick`
- `onPolygonClick`
- `onGroundImageClick` (Google Maps)

## Eventos de cámara

En algunos proveedores (como MapLibre) también dispones de:

- `onCameraMoveStart`
- `onCameraMove`
- `onCameraMoveEnd`

