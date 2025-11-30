---
title: "Zoom Levels"
---

MapConductor utiliza niveles de zoom para controlar la escala y el nivel de detalle del mapa. El sistema de zoom sigue aproximadamente las convenciones de Google Maps, aunque puede variar ligeramente entre proveedores debido a diferencias en sus implementaciones internas.

## Entender los niveles de zoom

Los niveles de zoom en MapConductor se representan como valores `Double`, normalmente en el rango de 0 a 21, donde:

- **Valores más altos** = Más zoom (más detalle, área más pequeña).
- **Valores más bajos** = Menos zoom (menos detalle, área más grande).
- **Valores fraccionarios** = Interpolación suave entre niveles enteros.

```kotlin
// Ejemplos de diferentes niveles de zoom
val worldView = 2.0        // Ver continentes
val countryView = 6.0      // Ver países completos
val cityView = 10.0        // Ver ciudades
val streetView = 15.0      // Ver calles
val buildingView = 18.0    // Ver edificios individuales
```

## Referencia de niveles de zoom

### Escala global (0–5)

```kotlin
// Vista del mundo y continentes
val worldLevel = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(0.0, 0.0),
    zoom = 2.0  // Muestra continentes y océanos
)

val continentLevel = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(39.8283, -98.5795), // Centro aproximado de EE. UU.
    zoom = 4.0  // Muestra un continente completo
)
```

### Escala de país / región (6–9)

```kotlin
val countryLevel = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(35.0, 135.0),
    zoom = 6.0  // Ver un país entero
)

val regionLevel = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(40.4168, -3.7038), // Madrid
    zoom = 8.0  // Ver una región/provincia
)
```

### Escala de ciudad / barrio (10–15)

```kotlin
val cityLevel = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194), // San Francisco
    zoom = 12.0  // Ver una ciudad
)

val neighborhoodLevel = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    zoom = 15.0  // Ver barrios y calles
)
```

### Escala de edificio / detalle (16+)

```kotlin
val buildingLevel = MapCameraPositionImpl(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    zoom = 18.0  // Ver edificios individuales
)
```

## Notas por proveedor

- Los valores de zoom exactos y el rango máximo pueden variar según el SDK de mapas (Google Maps, Mapbox, etc.).
- MapConductor normaliza en la medida de lo posible, pero siempre conviene probar en los proveedores que uses para ajustar los niveles a tus necesidades.

En general, usa el parámetro `zoom` de `MapCameraPosition` como una guía relativa de escala, y ajusta los valores para que coincidan con la experiencia de usuario deseada en cada proveedor.
