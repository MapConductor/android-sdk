---
title: "GeoPoint"
---

`GeoPoint` es la clase que representa coordenadas geográficas (latitud, longitud y altitud). La mayoría de las posiciones en MapConductor se expresan mediante `GeoPoint`.

## Estructura básica

Un `GeoPoint` contiene:

- Latitud (`latitude`)
- Longitud (`longitude`)
- Altitud (`altitude`, opcional)

La implementación concreta suele ser `GeoPointImpl`:

```kotlin
data class GeoPointImpl(
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double? = null
) : GeoPoint
```

## Cómo crear un GeoPoint

### A partir de latitud y longitud

```kotlin
val point = GeoPointImpl.fromLatLong(
    latitude = 37.7749,
    longitude = -122.4194
)
```

### Incluyendo altitud

```kotlin
val pointWithAltitude = GeoPointImpl(
    latitude = 37.7749,
    longitude = -122.4194,
    altitudeMeters = 30.0
)
```

## Usos principales

`GeoPoint` se utiliza en muchos componentes de MapConductor:

- Posición de `Marker`, `Circle`, `Polyline`, etc.
- Objetivo de la cámara en `MapCameraPosition`.
- Coordenadas devueltas por los eventos de mapa (por ejemplo, posición de clic).

## Sistema de coordenadas

`GeoPoint` asume el sistema WGS84 (latitud/longitud). Incluso cuando internamente se realizan conversiones de proyección (por ejemplo, Web Mercator), la API pública siempre expone posiciones como `GeoPoint`.

Para operaciones como cálculos de distancia o conversiones relacionadas, consulta también [Spherical Utilities](/es-419/core/spherical-utilities).

