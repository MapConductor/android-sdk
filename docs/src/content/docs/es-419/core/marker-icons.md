---
title: "Marker Icons"
---

Los iconos de marcador se representan mediante el tipo `MarkerIcon`. MapConductor incluye varias implementaciones listas para usar.

## DefaultIcon

```kotlin
DefaultIcon(
    scale: Float = 1.0f,
    label: String? = null,
    fillColor: Color = Color.Red,
    strokeColor: Color = Color.Black,
    strokeWidth: Dp = 1.dp,
    labelTextColor: Color = Color.White,
    labelStrokeColor: Color? = null,
    debug: Boolean = false
)
```

## DrawableDefaultIcon

```kotlin
DrawableDefaultIcon(
    backgroundDrawable: Drawable,
    scale: Float = 1.0f,
    strokeColor: Color? = null,
    strokeWidth: Dp = 1.dp
)
```

## ImageIcon

```kotlin
ImageIcon(
    drawable: Drawable,
    anchor: Offset = Offset(0.5f, 0.5f),
    debug: Boolean = false
)
```

Consulta [Marker](/es-419/components/marker) para ver cómo se usan estos iconos en el mapa.

