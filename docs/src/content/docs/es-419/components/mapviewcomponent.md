---
title: "Componentes MapView"
---

MapConductor proporciona componentes de vista de mapa específicos por proveedor que sirven como base para mostrar mapas en tu aplicación. Cada proveedor tiene su propia implementación, pero la API se mantiene coherente.

## Componentes por proveedor

MapConductor admite varios proveedores de mapas, cada uno con su componente dedicado:

- `GoogleMapView` (Google Maps)
- `MapboxMapView` (Mapbox)
- `HereMapView` (HERE Maps)
- `ArcGISMapView` (ArcGIS)
- `MapLibreMapView` (MapLibre)

En lugar de registrar callbacks de eventos en cada overlay individual (marcadores, polilíneas, etc.), los manejadores se pasan al componente de mapa correspondiente.

### GoogleMapView
Para integración con Google Maps:
```kotlin
GoogleMapView(
    state: GoogleMapViewStateImpl,
    modifier: Modifier = Modifier,
    markerRenderingStrategy: MarkerRenderingStrategy<GoogleMapActualMarker>? = null,
    onMapViewInitialized: OnMapViewInitializedHandler? = null,
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
    content: (@Composable MapViewScope.() -> Unit)? = null
)
```

### MapboxMapView
Para integración con Mapbox:
```kotlin
MapboxMapView(
    state: MapboxViewStateImpl,
    modifier: Modifier = Modifier,
    markerRenderingStrategy: MarkerRenderingStrategy<MapboxActualMarker>? = null,
    onMapViewInitialized: OnMapViewInitializedHandler? = null,
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
    content: (@Composable MapViewScope.() -> Unit)? = null
)
```

### HereMapView
Para integración con HERE Maps:
```kotlin
HereMapView(
    state: HereViewStateImpl,
    modifier: Modifier = Modifier,
    markerRenderingStrategy: MarkerRenderingStrategy<HereActualMarker>? = null,
    onMapViewInitialized: OnMapViewInitializedHandler? = null,
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
    content: (@Composable MapViewScope.() -> Unit)? = null
)
```

Componentes equivalentes existen para ArcGIS y MapLibre (`ArcGISMapView`, `MapLibreMapView`) con firmas similares.

## Ejemplo con GoogleMapView

```kotlin
GoogleMapView(
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
