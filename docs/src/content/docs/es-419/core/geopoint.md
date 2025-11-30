---
title: "GeoPoint"
---

`GeoPoint` representa una coordenada geográfica con latitud, longitud y altitud opcional. Es la unidad básica que se utiliza para posicionar marcadores, círculos y otros overlays en el mapa.

## Interfaz e implementación

### Interfaz GeoPoint

```kotlin
interface GeoPoint {
    val latitude: Double
    val longitude: Double
    val altitude: Double?
}
```

### GeoPointImpl

La implementación principal proporciona coordenadas geográficas inmutables:

```kotlin
data class GeoPointImpl(
    override val latitude: Double,
    override val longitude: Double,
    override val altitude: Double = 0.0
) : GeoPoint
```

## Formas de creación

### Métodos de fábrica

```kotlin
// Creación estándar: latitud primero (patrón habitual)
GeoPointImpl.fromLatLong(37.7749, -122.4194)

// Alternativa: longitud primero
GeoPointImpl.fromLongLat(-122.4194, 37.7749)

// A partir de un GeoPoint existente
GeoPointImpl.from(existingGeoPoint)

// Uso directo del constructor
GeoPointImpl(latitude = 37.7749, longitude = -122.4194, altitude = 100.0)
```

### Ejemplos de uso

```kotlin
// Posicionamiento básico de un marcador
val sanFrancisco = GeoPointImpl.fromLatLong(37.7749, -122.4194)

// Sustituye MapView por el proveedor de mapas que prefieras, como GoogleMapView o MapboxMapView
MapView(state = mapViewState) {
    Marker(
        position = sanFrancisco,
        icon = DefaultIcon(label = "SF")
    )
}
```

```kotlin
// Con altitud para posicionamiento 3D
val mountEverest = GeoPointImpl(
    latitude = 27.9881,
    longitude = 86.9250,
    altitude = 8848.0 // metros
)
```

## Sistema de coordenadas

### Latitud
- **Rango**: de -90.0 a 90.0 grados  
- **-90.0**: Polo Sur  
- **0.0**: Ecuador  
- **90.0**: Polo Norte

### Longitud
- **Rango**: de -180.0 a 180.0 grados  
- **Negativa**: hemisferio occidental  
- **Positiva**: hemisferio oriental

### Altitud
- Expresada normalmente en metros sobre el nivel del mar  
- Opcional en la mayoría de los casos de uso

`GeoPoint` asume el sistema WGS84 (latitud/longitud). Incluso cuando internamente se utilizan otras proyecciones (por ejemplo, Web Mercator), la API pública siempre expone posiciones como `GeoPoint`.

Para operaciones como cálculos de distancia o conversiones relacionadas, consulta también [Spherical Utilities](/es-419/core/spherical-utilities).
