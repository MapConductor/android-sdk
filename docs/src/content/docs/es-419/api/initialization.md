---
title: "Initialization"
---

Esta página describe el flujo básico de inicialización de MapConductor y cómo crear `MapViewState` para cada proveedor.

## Pasos

1. Añadir dependencias en Gradle ([Instalación](/es-419/installation/)).
2. Configurar los SDK de mapas necesarios ([Configuración](/es-419/setup/)).
3. Crear un `MapViewState`.
4. Renderizar el componente `MapView` correspondiente en Compose.

## Ejemplo de MapViewState

```kotlin
val camera = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    zoom = 13.0
)

val mapViewState = rememberGoogleMapViewState(
    cameraPosition = camera
)
```

## Usar el MapView

```kotlin
GoogleMapsView(
    state = mapViewState,
    onMapLoaded = { println("Map loaded") }
) {
    Marker(
        position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        icon = DefaultIcon(label = "SF")
    )
}
```

