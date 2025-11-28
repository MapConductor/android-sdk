---
title: "CircleState（円の状態）"
---

`CircleState` は、地図上の円形オーバーレイの構成と動作を管理します。中心位置、半径、スタイル、インタラクション設定のためのリアクティブなプロパティを提供します。

## コンストラクタ

```kotlin
CircleState(
    center: GeoPoint,
    radiusMeters: Double,
    clickable: Boolean = true,
    strokeColor: Color = Color.Red,
    strokeWidth: Dp = 1.dp,
    fillColor: Color = Color(red = 255, green = 255, blue = 255, alpha = 127),
    id: String? = null,
    zIndex: Int? = null,
    extra: Serializable? = null
)
```

## プロパティ

### 基本プロパティ
- **`id: String`**: 一意の識別子（指定されていない場合は自動生成）
- **`center: GeoPoint`**: 円の地理的中心
- **`radiusMeters: Double`**: 半径（メートル単位）
- **`clickable: Boolean`**: 円がクリックイベントに応答するかどうか
- **`extra: Serializable?`**: 円に付加される追加データ

### ビジュアルプロパティ
- **`strokeColor: Color`**: 枠線の色
- **`strokeWidth: Dp`**: 枠線の太さ
- **`fillColor: Color`**: 内部の塗りつぶし色
- **`zIndex: Int?`**: 描画順序（高い値が上に描画される）

## メソッド

```kotlin
fun copy(...): CircleState // 変更されたコピーを作成
fun fingerPrint(): CircleFingerPrint // 変更検知
fun asFlow(): Flow<CircleFingerPrint> // リアクティブ更新
```

## 使用例

### 基本的な円

```kotlin
val circleState = CircleState(
    center = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    radiusMeters = 1000.0,
    strokeColor = Color.Blue,
    fillColor = Color.Blue.copy(alpha = 0.3f)
)

// MapViewは、GoogleMapView、MapboxMapViewなど、選択した地図SDKーに置き換えてください
MapView(state = mapViewState) {
    Circle(circleState)
}
```

### インタラクティブな円

```kotlin
@Composable
fun InteractiveCircleExample() {
    var circleState by remember {
        mutableStateOf(
            CircleState(
                center = GeoPointImpl.fromLatLong(37.7749, -122.4194),
                radiusMeters = 500.0,
                strokeColor = Color.Red,
                fillColor = Color.Red.copy(alpha = 0.2f),
                clickable = true
            )
        )
    }

    Column {
        Slider(
            value = circleState.radiusMeters.toFloat(),
            onValueChange = {
                circleState = circleState.copy(radiusMeters = it.toDouble())
            },
            valueRange = 100f..2000f
        )

        // MapViewは、GoogleMapView、MapboxMapViewなど、選択した地図SDKーに置き換えてください
MapView(
            state = mapViewState,
            onCircleClick = { event ->
                println("Circle clicked at: ${event.clicked}")
            }
        ) {
            Circle(circleState)
        }
    }
}
```

### Z-Indexを使用した複数の円

```kotlin
val circles = listOf(
    CircleState(
        center = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        radiusMeters = 1000.0,
        fillColor = Color.Red.copy(alpha = 0.3f),
        zIndex = 1
    ),
    CircleState(
        center = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        radiusMeters = 500.0,
        fillColor = Color.Blue.copy(alpha = 0.5f),
        zIndex = 2
    )
)

// MapViewは、GoogleMapView、MapboxMapViewなど、選択した地図SDKーに置き換えてください
MapView(state = mapViewState) {
    circles.forEach { circleState ->
        Circle(circleState)
    }
}
```

## イベント処理

円のイベントは状態とクリック位置の両方を提供します：

```kotlin
data class CircleEvent(
    val state: CircleState,
    val clicked: GeoPoint
)

typealias OnCircleEventHandler = (CircleEvent) -> Unit
```

## ベストプラクティス

1. **半径の単位**: 一貫性のため、半径は常にメートル単位で指定してください
2. **色のアルファ値**: 地図の視認性を高めるためにアルファ透過を使用してください
3. **Z-Index**: z-indexを使用して重なり合う円を制御してください
4. **パフォーマンス**: 大きな円を多数作成することは避けてください
5. **リアクティビティ**: 効率的な再レンダリングのためにプロパティを直接更新してください
