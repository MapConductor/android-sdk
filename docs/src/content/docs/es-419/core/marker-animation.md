---
title: "Marker Animation"
---

`MarkerAnimation` agrupa la configuración necesaria para animar marcadores, por ejemplo, transiciones de posición o efectos de aparición.

## Uso con MarkerState

```kotlin
val state = MarkerState(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    icon = DefaultIcon(label = "Animated"),
)

val animation = MarkerAnimation.FadeIn(durationMillis = 500)

state.setAnimation(animation)
```

Consulta las clases concretas de `MarkerAnimation` para ver las opciones disponibles.

