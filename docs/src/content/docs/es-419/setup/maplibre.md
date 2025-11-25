---
title: Configuración de MapLibre
---

# Configuración de MapLibre

En esta página se explica cómo configurar MapLibre para usarlo con MapConductor.

## Requisitos previos

- Acceso a un servidor de mosaicos y estilos compatible con MapLibre (por ejemplo, archivos de estilo JSON y tiles).

## Añadir dependencias en Gradle

```kotlin
dependencies {
    implementation(platform("com.mapconductor:mapconductor-bom:1.1.1"))
    implementation("com.mapconductor:core")
    implementation("com.mapconductor:for-maplibre")
    // Añade también las dependencias de MapLibre Native / GL para Android
}
```

Consulta la documentación de MapLibre para configurar los tiles y los estilos.

## Ejemplo de uso con MapConductor

```kotlin
@Composable
fun MapLibreExample() {
    val camera = MapCameraPositionImpl(
        position = GeoPointImpl.fromLatLong(51.5074, -0.1278),
        zoom = 6.0,
    )

    val mapViewState = rememberMapLibreMapViewState(
        cameraPosition = camera,
        // Especifica aquí el estilo si es necesario
    )

    MapLibreMapView(
        state = mapViewState,
        onMapClick = { point ->
            println("Clicked: ${point.latitude}, ${point.longitude}")
        }
    ) {
        Marker(
            position = GeoPointImpl.fromLatLong(51.5074, -0.1278),
            icon = DefaultIcon(label = "L"),
        )
    }
}
```

![Ejemplo sencillo de MapLibre](/img/basic-maplibre.jpg)

Al combinar MapLibre con fuentes de datos abiertas o estilos personalizados, puedes crear experiencias de mapa muy flexibles.

