---
title: "Spherical Utilities"
---

Las utilidades esféricas proporcionan cálculos geográficos precisos usando un modelo esférico de la Tierra. Estas funciones son esenciales para medir distancias, calcular rutas y realizar otras operaciones geográficas.

## Visión general

El objeto `Spherical` contiene funciones auxiliares para calcular distancias, rumbos y posiciones sobre la superficie terrestre. Usa un modelo esférico con el radio de la Tierra basado en el elipsoide WGS84.

```kotlin
import com.mapconductor.core.spherical.Spherical
```

## Constantes principales

- **Radio de la Tierra**: 6.378.137 metros (semieje mayor del elipsoide WGS84).
- **Sistema de coordenadas**: coordenadas geográficas WGS84 (latitud/longitud).

## Cálculo de distancias

### Distancia entre puntos

```kotlin
fun computeDistanceBetween(from: GeoPoint, to: GeoPoint): Double
```

Calcula la distancia más corta entre dos puntos usando la fórmula de Haversine:

```kotlin
val sanFrancisco = GeoPointImpl.fromLatLong(37.7749, -122.4194)
val newYork = GeoPointImpl.fromLatLong(40.7128, -74.0060)

val distanceMeters = Spherical.computeDistanceBetween(sanFrancisco, newYork)
val distanceKm = distanceMeters / 1000.0
val distanceMiles = distanceMeters / 1609.344

println("Distance: ${distanceKm.toInt()} km (${distanceMiles.toInt()} miles)")
```

### Longitud de un trayecto

```kotlin
fun computeLength(path: List<GeoPoint>): Double
```

Calcula la longitud total de un trayecto compuesto por varios puntos:

```kotlin
val routePoints = listOf(
    GeoPointImpl.fromLatLong(37.7749, -122.4194), // San Francisco
    GeoPointImpl.fromLatLong(37.7849, -122.4094), // North Beach
    GeoPointImpl.fromLatLong(37.7949, -122.3994), // Russian Hill
    GeoPointImpl.fromLatLong(37.8049, -122.3894)  // Fisherman's Wharf
)

val totalDistance = Spherical.computeLength(routePoints)
println("Route length: ${(totalDistance / 1000).toInt()} km")
```

## Otros casos de uso

Las utilidades esféricas también son útiles para:

- Encontrar puntos intermedios o realizar interpolaciones sobre rutas.
- Calcular el rumbo (bearing) entre dos `GeoPoint`.
- Animar la cámara a lo largo de un trayecto.
- Filtrar elementos dentro de un radio o área concreta.

Son especialmente prácticas cuando se trabaja con rutas, animaciones de cámara y lógica de filtrado geográfico basada en distancia.
