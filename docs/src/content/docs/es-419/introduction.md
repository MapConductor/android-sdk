---
title: Introducción
---

# Documentación de MapConductor Android SDK

MapConductor es una biblioteca de mapas unificada que proporciona una API común para varios proveedores de mapas, incluyendo Google Maps, Mapbox, HERE, ArcGIS y MapLibre. Esta documentación cubre los componentes públicos del SDK distribuidos a través de Maven para la versión **v1.1.0**.

## Descripción general

MapConductor te permite trabajar con diferentes proveedores de mapas utilizando una única API. El SDK se encarga de las implementaciones específicas de cada proveedor y ofrece a tu aplicación una interfaz consistente.

### Proveedores de mapas compatibles

- **Google Maps**: `GoogleMapViewStateImpl` / `GoogleMapsView`
- **Mapbox**: `MapboxViewStateImpl` / `MapboxMapView`
- **HERE Maps**: `HereViewStateImpl` / `HereMapView`
- **ArcGIS**: `ArcGISMapViewStateImpl` / `ArcGISMapView`
- **MapLibre**: `MapLibreViewStateImpl` / `MapLibreMapView`

### Clases principales

El SDK proporciona clases geográficas fundamentales:

- **GeoPoint**: Representa coordenadas geográficas (latitud, longitud, altitud).
- **GeoRectBounds**: Define áreas geográficas rectangulares usando las esquinas suroeste/noreste.
- **MapCameraPosition**: Representa la posición de la cámara (objetivo, zoom, orientación, inclinación, márgenes).

### Componentes clave

El SDK ofrece los siguientes componentes principales:

1. **Componentes de vista de mapa**: Componentes específicos por proveedor (GoogleMapsView, MapboxMapView, HereMapView, ArcGISMapView, MapLibreMapView).
2. **Marker**: Marcadores de punto con iconos e interacciones personalizables.
3. **Circle**: Superposiciones circulares con opciones de estilo.
4. **Polyline**: Segmentos de línea que conectan múltiples puntos.
5. **Polygon**: Formas rellenas con trazo y relleno configurables.
6. **GroundImage**: Superposición de imágenes posicionadas geográficamente (solo Google Maps).

## Primeros pasos

Para usar MapConductor en tu proyecto, sigue estos pasos.

### 1. Instalación

Consulta la página de [Comenzar](/es-419/get-started/) para ver toda la información sobre dependencias y versiones.

### 2. Configuración de cada SDK de mapas

> **Importante**: MapConductor proporciona una capa de API unificada sobre los SDK de mapas existentes. Debes configurar cada SDK de mapas de forma independiente antes de usar la integración de MapConductor.

Cada proveedor de mapas requiere su propia configuración de SDK: claves de API, permisos y ajustes específicos:

- **[Configuración de Google Maps](/es-419/setup/google-maps)** – Configura el SDK de Google Maps con claves de API y permisos.
- **[Configuración de Mapbox](/es-419/setup/mapbox)** – Configura los tokens de acceso de Mapbox y el estilo de mapa.
- **[Configuración de HERE Maps](/es-419/setup/here-maps)** – Configura el SDK de HERE con claves de API y licencias.
- **[Configuración de ArcGIS](/es-419/setup/arcgis)** – Configura el SDK de ArcGIS con claves de API y licencias.
- **[Configuración de MapLibre](/es-419/setup/maplibre/)** – Configura los mosaicos y la información de estilo.

Solo necesitas configurar los SDK que vayas a utilizar en tu aplicación.

### 3. Uso básico

Ejemplo de creación de una vista de mapa con un marcador y un círculo:

```kotlin
@Composable
fun BasicMapExample(modifier: Modifier = Modifier) {
    val sanFrancisco = GeoPointImpl.fromLatLong(37.7749, -122.4194)
    val camera = MapCameraPositionImpl(
        position = sanFrancisco,
        zoom = 13.0,
    )
    // Sustituye por el proveedor de mapas que vayas a usar
    // - Google Maps -> rememberGoogleMapViewState
    // - Mapbox -> rememberMapboxViewState
    // ... etc.
    val mapViewState = rememberGoogleMapViewState(
        cameraPosition = camera,
    )

    // Cambia también la vista de mapa según el proveedor elegido
    // - Google Maps -> GoogleMapsView
    // - Mapbox -> MapboxMapView
    // ... etc.
    GoogleMapsView(
        modifier = modifier,
        state = mapViewState,
        onMapClick = { geoPoint ->
            println("Map clicked at: ${geoPoint.latitude}, ${geoPoint.longitude}")
        },
        onMarkerClick = { markerState ->
            println("Marker clicked: ${markerState.extra}")
        }
    ) {
        // Añadir un marcador
        Marker(
            position = sanFrancisco,
            icon = DefaultIcon(label = "SF"),
            extra = "San Francisco marker"
        )

        // Añadir un círculo
        Circle(
            center = sanFrancisco,
            radiusMeters = 1000.0,
            strokeColor = Color.Blue,
            fillColor = Color.Blue.copy(alpha = 0.3f)
        )
    }
}
```

![un marcador con un círculo dibujado en el mapa](/img/introduction/basic-googlemaps-example.jpg)

### 4. Cambio de proveedor de mapas

Para cambiar de proveedor de mapas, basta con cambiar la implementación de `MapViewState`:

```kotlin
// Google Maps
val googleMapState = rememberGoogleMapViewState()

// Mapbox
val mapboxState = rememberMapboxMapViewState()

// HERE Maps
val hereState = rememberHereMapViewState()

// ArcGIS
val arcgisState = rememberArcGISMapViewState()

// MapLibre
val mapLibreState = rememberMapLibreMapViewState()
```

El resto de tu código permanece igual: todos los componentes funcionan de forma consistente entre proveedores.

## Novedades de la versión v1.1.1

En comparación con la versión 1.1.0, los cambios principales de la v1.1.1 incluyen:

- Se corrigió un error donde la posición de visualización de InfoBubble no se recalculaba al mover el área visible del mapa

## Novedades de la versión v1.1.0

En comparación con la versión 1.0.0, los cambios principales de la v1.1.0 incluyen:

- Manejo unificado de eventos de movimiento de cámara (`onCameraMoveStart`, `onCameraMove`, `onCameraMoveEnd`) en todos los proveedores
- Mejoras en la gestión de la posición de la cámara de `MapViewState` y su integración con `VisibleRegion`
- Interfaces de control de marcadores refactorizadas para integraciones de proveedores más claras
- Ejemplos ampliados en la app de ejemplo para demostrar flujos avanzados de cámara y `VisibleRegion`

## Documentación relacionada

- [Comenzar](/es-419/get-started/)
- [Módulos](/es-419/modules/)
- [Compatibilidad de versiones del SDK](/es-419/sdk-version-compatibility/)
- [Compatibilidad de proveedores](/es-419/provider-compatibility/)

