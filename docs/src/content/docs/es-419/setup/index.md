---
title: Descripción general de la configuración
---

# Descripción general de la configuración

En esta página se ofrece una visión general de los pasos necesarios para usar MapConductor con cada proveedor de mapas. Los detalles específicos se describen en las páginas de cada proveedor.

## Requisitos comunes

Antes de usar MapConductor, asegúrate de cumplir estos requisitos:

- Proyecto Android creado (con Gradle configurado).
- Kotlin y Jetpack Compose habilitados en el proyecto.
- Claves de API y credenciales necesarias para cada SDK de mapas.

Puedes encontrar un ejemplo de configuración de Gradle en la sección de [Comenzar](/es-419/get-started/).

## Configuración por proveedor

Las instrucciones para cada proveedor se detallan en:

- [Configuración de Google Maps](/es-419/setup/google-maps)
- [Configuración de Mapbox](/es-419/setup/mapbox)
- [Configuración de HERE Maps](/es-419/setup/here-maps)
- [Configuración de ArcGIS](/es-419/setup/arcgis)
- [Configuración de MapLibre](/es-419/setup/maplibre)

Solo necesitas configurar los proveedores que uses en tu aplicación.

## Inicialización de MapConductor

Una vez inicializado cada SDK de mapas, en MapConductor solo tienes que utilizar el `MapViewState` correspondiente y el componente de vista de mapa adecuado.

```kotlin
@Composable
fun MyMap() {
    val cameraPosition = MapCameraPositionImpl(
        position = GeoPointImpl.fromLatLong(35.0, 135.0),
        zoom = 12.0,
    )

    val mapViewState = rememberGoogleMapViewState(
        cameraPosition = cameraPosition,
    )

    GoogleMapsView(state = mapViewState) {
        // Añade Marker / Circle / Polyline según necesites
    }
}
```

Para cambiar a otro proveedor, sustituye `rememberGoogleMapViewState` y `GoogleMapsView` por las versiones correspondientes, como `rememberMapboxMapViewState` y `MapboxMapView`.

