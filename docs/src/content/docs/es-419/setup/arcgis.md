---
title: Configuración de ArcGIS
---

# Configuración de ArcGIS

En esta página se explica cómo configurar ArcGIS para usarlo con MapConductor.

## Requisitos previos

- Cuenta de ArcGIS o licencia de desarrollador disponible.
- Clave de API y configuración de autenticación completadas.

## Añadir dependencias en Gradle

```kotlin
dependencies {
    implementation(platform("com.mapconductor:mapconductor-bom:{BOM_MODULE_VERSION}"))
    implementation("com.mapconductor:core")
    implementation("com.mapconductor:for-arcgis")
    // Añade también las dependencias de ArcGIS Maps SDK for Kotlin / Android
}
```

Sigue la documentación oficial de ArcGIS para configurar las dependencias y las licencias.

## Ejemplo de uso con MapConductor

```kotlin
@Composable
fun ArcGISExample() {
    val camera = MapCameraPositionImpl(
        position = GeoPointImpl.fromLatLong(34.0522, -118.2437),
        zoom = 11.0,
    )

    val mapViewState = rememberArcGISMapViewState(
        cameraPosition = camera,
    )

    ArcGISMapView(
        state = mapViewState,
        onMapClick = { point ->
            println("Clicked: ${point.latitude}, ${point.longitude}")
        }
    ) {
        Marker(
            position = GeoPointImpl.fromLatLong(34.0522, -118.2437),
            icon = DefaultIcon(label = "LA"),
        )
    }
}
```
![Ejemplo sencillo de ArcGIS](/img/basic-arcgis.jpg)

Combina MapConductor con funcionalidades avanzadas de ArcGIS (capas, análisis, etc.) para construir aplicaciones de mapas más complejas.

