---
title: Configuración de Mapbox
---

# Configuración de Mapbox

En esta página se explica cómo configurar Mapbox para usarlo con MapConductor.

## Requisitos previos

- Cuenta de Mapbox creada.
- Token de acceso obtenido.
- URL de estilo de mapa disponible.

## Añadir dependencias en Gradle

```kotlin
dependencies {
    implementation(platform("com.mapconductor:mapconductor-bom:{BOM_MODULE_VERSION}"))
    implementation("com.mapconductor:core")
    implementation("com.mapconductor:for-mapbox")
    // Añade también las dependencias de Mapbox Maps SDK for Android
}
```

Consulta la documentación oficial de Mapbox para añadir las dependencias del SDK.

## Inicialización y estilo

Mapbox requiere configurar el token de acceso al inicio de la aplicación. Normalmente se hace en la clase `Application`:

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicialización de Mapbox (el código exacto depende de la versión del SDK)
    }
}
```

## Ejemplo de uso con MapConductor

```kotlin
@Composable
fun MapboxExample() {
    val camera = MapCameraPositionImpl(
        position = GeoPointImpl.fromLatLong(40.7128, -74.0060),
        zoom = 6.0,
    )

    val mapViewState = rememberMapboxMapViewState(
        cameraPosition = camera,
        // Configura aquí la URL de estilo si es necesario
    )

    MapboxMapView(
        state = mapViewState,
        onMapClick = { point ->
            println("Clicked: ${point.latitude}, ${point.longitude}")
        }
    ) {
        Marker(
            position = GeoPointImpl.fromLatLong(40.7128, -74.0060),
            icon = DefaultIcon(label = "NYC"),
        )
    }
}
```
![Ejemplo sencillo de Mapbox](/img/basic-mapbox.jpg)

Para funcionalidades avanzadas específicas de Mapbox (como manipular capas de estilo), combínalas con las APIs nativas del SDK.

