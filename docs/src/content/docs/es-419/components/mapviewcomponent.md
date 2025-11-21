---
title: "Componentes MapView"
---

MapConductor proporciona un componente de vista de mapa para cada proveedor. Aunque las implementaciones internas difieren, la API expuesta es muy similar.

## Componentes por proveedor

- `GoogleMapsView` (Google Maps)
- `MapboxMapView` (Mapbox)
- `HereMapView` (HERE Maps)
- `ArcGISMapView` (ArcGIS)
- `MapLibreMapView` (MapLibre)

Todos aceptan un `state` específico de proveedor y exponen manejadores de eventos comunes como `onMapClick`, `onMarkerClick`, etc.

## Ejemplo con GoogleMapsView

```kotlin
GoogleMapsView(
    state = googleMapViewState,
    onMapLoaded = { println("Map loaded") },
    onMapClick = { geoPoint ->
        println("Map clicked at: ${geoPoint.latitude}, ${geoPoint.longitude}")
    },
    onMarkerClick = { markerState ->
        println("Marker clicked: ${markerState.extra}")
    }
) {
    Marker(
        position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        icon = DefaultIcon(label = "SF")
    )
}
```

Para una referencia completa de eventos y tipos, consulta [API / Initialization](/es-419/api/initialization) y [API / Event Handlers](/es-419/api/event-handlers).

