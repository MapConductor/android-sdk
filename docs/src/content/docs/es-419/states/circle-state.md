---
title: "CircleState"
---

`CircleState` gestiona la configuración y el comportamiento de los círculos dibujados en el mapa.

## Constructor

```kotlin
CircleState(
    center: GeoPoint,
    radius: Double,
    clickable: Boolean = true,
    strokeColor: Color = Color.Red,
    strokeWidth: Dp = 1.dp,
    fillColor: Color = Color(red = 255, green = 255, blue = 255, alpha = 127),
    id: String? = null,
    zIndex: Int? = null,
    extra: Serializable? = null
)
```

Consulta [Circle](/es-419/components/circle) para ver ejemplos de uso y buenas prácticas.

