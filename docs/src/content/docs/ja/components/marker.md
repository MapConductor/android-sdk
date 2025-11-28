---
title: "Marker（マーカー）"
---

Marker は、特定の地理位置に配置できるポイント型の注釈です。カスタムアイコン、インタラクション、アニメーションに対応しています。

## Composable 関数

### 基本的な Marker

```kotlin
@Composable
fun MapViewScope.Marker(
    position: GeoPoint,
    clickable: Boolean = true,
    draggable: Boolean = false,
    icon: MarkerIcon? = null,
    extra: Serializable? = null,
    id: String? = null
)
```

### State を利用した Marker

```kotlin
@Composable
fun MapViewScope.Marker(state: MarkerState)
```

## パラメータ

- **`position`**: マーカーの位置を表す地理座標（`GeoPoint`）
- **`clickable`**: マーカーがクリックに反応するかどうか（デフォルト: `true`）
- **`draggable`**: マーカーをドラッグ可能にするかどうか（デフォルト: `false`）
- **`icon`**: マーカー用のカスタムアイコン（`MarkerIcon?`）
- **`extra`**: マーカーに紐づける任意の追加データ（`Serializable?`）
- **`id`**: マーカーの一意な識別子（`String?`、未指定の場合は自動生成）

## アイコンの種類

### DefaultIcon

外観をカスタマイズできる標準的なマーカーアイコン:

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

### DrawableDefaultIcon

Drawable リソースを背景として利用するマーカー:

```kotlin
DrawableDefaultIcon(
    backgroundDrawable: Drawable,
    scale: Float = 1.0f,
    strokeColor: Color? = null,
    strokeWidth: Dp = 1.dp
)
```

### ImageIcon

任意の Drawable 画像を使ったマーカー:

```kotlin
ImageIcon(
    drawable: Drawable,
    anchor: Offset = Offset(0.5f, 0.5f),
    debug: Boolean = false
)
```

## 利用例

### 基本的なマーカー

```kotlin
// MapView には GoogleMapView や MapboxMapView など、利用する地図SDKのコンポーネントを指定します
MapView(state = mapViewState) {
    Marker(
        position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        extra = "San Francisco",
        id = "san-francisco-marker"
    )
}
```

### カスタムアイコン付きマーカー

```kotlin
MapView(state = mapViewState) {
    Marker(
        position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        icon = DefaultIcon(
            scale = 1.5f,
            label = "SF",
            fillColor = Color.Blue,
            strokeColor = Color.White,
            strokeWidth = 2.dp
        ),
        extra = "San Francisco with custom icon",
        id = "custom-sf-marker"
    )
}
```

### ドラッグ可能なマーカー

```kotlin
@Composable
fun DraggableMarkerExample() {
    var markerPosition by remember {
        mutableStateOf(GeoPointImpl.fromLatLong(37.7749, -122.4194))
    }

    MapView(
        state = mapViewState,
        onMarkerDrag = { markerState ->
            markerPosition = markerState.position
        }
    ) {
        Marker(
            position = markerPosition,
            draggable = true,
            icon = DefaultIcon(
                label = "Drag me",
                fillColor = Color.Green
            )
        )
    }
}
```

### 異なるアイコンを持つ複数マーカー

```kotlin
MapView(state = mapViewState) {
    // スケール違いの DefaultIcon
    Marker(
        position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        icon = DefaultIcon(scale = 0.7f, label = "Small")
    )

    Marker(
        position = GeoPointImpl.fromLatLong(37.7849, -122.4094),
        icon = DefaultIcon(scale = 1.0f, label = "Normal")
    )

    Marker(
        position = GeoPointImpl.fromLatLong(37.7949, -122.3994),
        icon = DefaultIcon(scale = 1.4f, label = "Large")
    )

    // カスタムカラーのマーカー
    Marker(
        position = GeoPointImpl.fromLatLong(37.7649, -122.4294),
        icon = DefaultIcon(
            fillColor = Color.Yellow,
            strokeColor = Color.Black,
            strokeWidth = 2.dp,
            label = "Custom"
        )
    )

    // Drawable を使ったマーカー
    val context = LocalContext.current
    AppCompatResources.getDrawable(context, R.drawable.custom_icon)?.let { drawable ->
        Marker(
            position = GeoPointImpl.fromLatLong(37.7549, -122.4394),
            icon = DrawableDefaultIcon(
                backgroundDrawable = drawable,
                scale = 1.2f
            )
        )
    }
}
```

### InfoBubble と組み合わせたマーカー

`InfoBubble` コンポーネントと組み合わせることで、マーカーをクリックした際に詳細情報を表示できます。詳細は [InfoBubble コンポーネント](/ja/components/infobubble) を参照してください。

