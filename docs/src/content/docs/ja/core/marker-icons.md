---
title: "Marker Icons（マーカーアイコン）"
---

MapConductor では、マーカーアイコンを `MarkerIcon` 抽象型として扱います。標準的な `DefaultIcon` に加えて、`DrawableDefaultIcon` や `ImageIcon` などの実装が提供されています。

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

マーカーアイコンの詳細な利用方法は、[Marker コンポーネント](/ja/components/marker) も参照してください。

