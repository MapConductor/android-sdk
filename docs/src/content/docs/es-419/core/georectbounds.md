---
title: "GeoRectBounds"
---

`GeoRectBounds` representa un rectángulo geográfico mediante dos puntos: suroeste (`southWest`) y noreste (`northEast`). Se utiliza para definir regiones visibles y áreas de superposición.

## Estructura básica

```kotlin
data class GeoRectBoundsImpl(
    val southWest: GeoPoint,
    val northEast: GeoPoint
) : GeoRectBounds
```

## Ejemplo como área de GroundImage

```kotlin
val bounds = GeoRectBoundsImpl(
    southWest = GeoPointImpl.fromLatLong(37.77, -122.43),
    northEast = GeoPointImpl.fromLatLong(37.79, -122.41),
)

MapView(state = mapViewState) {
    GroundImage(
        image = painterResource(id = R.drawable.overlay_image),
        bounds = bounds
    )
}
```

