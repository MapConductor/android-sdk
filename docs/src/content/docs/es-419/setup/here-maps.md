---
title: Configuración de HERE Maps
---

# Configuración de HERE Maps

En esta página se explica cómo configurar HERE Maps SDK para usarlo con MapConductor.

## Requisitos previos

- Cuenta de desarrollador de HERE creada.
- Claves de API o credenciales necesarias obtenidas.

## Añadir dependencias en Gradle

```kotlin
dependencies {
    implementation(platform("com.mapconductor:mapconductor-bom:1.1.1"))
    implementation("com.mapconductor:core")
    implementation("com.mapconductor:for-here")
    // Añade también las dependencias de HERE SDK for Android
}
```

Sigue la documentación oficial de HERE para añadir las dependencias del SDK.

## Ejemplo de uso con MapConductor

```kotlin
@Composable
fun HereMapsExample() {
    val camera = MapCameraPositionImpl(
        position = GeoPointImpl.fromLatLong(52.5309, 13.3847),
        zoom = 13.0,
    )

    val mapViewState = rememberHereMapViewState(
        cameraPosition = camera,
    )

    HereMapView(
        state = mapViewState,
        onMapClick = { point ->
            println("Clicked: ${point.latitude}, ${point.longitude}")
        }
    ) {
        Marker(
            position = GeoPointImpl.fromLatLong(52.5309, 13.3847),
            icon = DefaultIcon(label = "Berlin"),
        )
    }
}
```
![Ejemplo sencillo de HERE](/img/basic-here.jpg)

Si necesitas funcionalidades específicas de HERE (como mapas offline o información de tráfico), combínalas con las APIs nativas del SDK.

