---
title: "MarkerState（マーカー状態）"
---

`MarkerState` はマーカーの位置や見た目、インタラクション設定を管理するための状態クラスです。Compose らしいリアクティブな更新と、効率的な差分検知の仕組みを提供します。

## コンストラクタ

```kotlin
MarkerState(
    position: GeoPoint,
    id: String? = null,
    extra: Serializable? = null,
    icon: MarkerIcon? = null,
    animation: MarkerAnimation? = null,
    clickable: Boolean = true,
    draggable: Boolean = false
)
```

## プロパティ

### 基本プロパティ

- **`id: String`**: マーカーの一意な ID（未指定の場合は自動生成）
- **`position: GeoPoint`**: 現在のマーカー位置
- **`extra: Serializable?`**: 追加情報など、任意の付帯データ

### 表示に関するプロパティ

- **`icon: MarkerIcon?`**: マーカーのアイコン（未指定の場合はデフォルトアイコン）
- **`clickable: Boolean`**: マーカーがクリックイベントに反応するかどうか
- **`draggable: Boolean`**: マーカーをユーザーがドラッグできるかどうか

### インタラクション関連のプロパティ

- **`isDragging: Boolean`**（読み取り専用）: 現在ドラッグ中かどうか
- **`internalPosition: GeoPoint`**（読み取り専用）: 描画用に使用される内部位置（ドラッグ中の一時位置などを考慮）

## 主なメソッド

### アニメーション

```kotlin
fun setAnimation(animation: MarkerAnimation?)
```
マーカーのアニメーションを設定または解除します。

```kotlin
internal fun getAnimation(): MarkerAnimation?
```
現在のアニメーションを取得します（内部利用向け）。

### 状態のコピー

```kotlin
fun copy(
    id: String? = this.id,
    position: GeoPoint = this.position,
    extra: Serializable? = this.extra,
    icon: MarkerIcon? = this.icon,
    clickable: Boolean? = this.clickable,
    draggable: Boolean? = this.draggable
): MarkerState
```

既存の状態を元に、一部プロパティだけを変えた新しい `MarkerState` を生成します。

### リアクティブ更新

```kotlin
fun asFlow(): Flow<MarkerFingerPrint>
```
状態が変化したときにフローとして通知を受け取るための API です。

```kotlin
fun fingerPrint(): MarkerFingerPrint
```
差分検知のためのフィンガープリント（スナップショット）を生成します。

## 利用例

### 基本的な MarkerState

```kotlin
val markerState = MarkerState(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    extra = "San Francisco marker"
)

MapView(state = mapViewState) {
    Marker(markerState)
}
```

### カスタマイズした MarkerState

```kotlin
val customMarkerState = MarkerState(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    icon = DefaultIcon(
        fillColor = Color.Blue,
        label = "SF",
        scale = 1.2f
    ),
    clickable = true,
    draggable = true,
    extra = "Draggable San Francisco marker"
)

MapView(
    state = mapViewState,
    onMarkerClick = { markerState ->
        println("Clicked marker: ${markerState.extra}")
    },
    onMarkerDrag = { markerState ->
        println("Marker dragged to: ${markerState.position}")
    }
) {
    Marker(customMarkerState)
}
```

### 動的に MarkerState を更新する

```kotlin
@Composable
fun DynamicMarkerExample() {
    var markerState by remember {
        mutableStateOf(
            MarkerState(
                position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
                icon = DefaultIcon(fillColor = Color.Red, label = "1"),
                extra = "Counter: 1"
            )
        )
    }

    var counter by remember { mutableStateOf(1) }

    Column {
        Button(
            onClick = {
                counter++
                markerState = markerState.copy(
                    icon = DefaultIcon(
                        fillColor = Color.Blue,
                        label = counter.toString()
                    ),
                    extra = "Counter: $counter"
                )
            }
        ) {
            Text("Update Marker ($counter)")
        }

        MapView(state = mapViewState) {
            Marker(markerState)
        }
    }
}
```

### 位置をアニメーションさせる MarkerState

```kotlin
@Composable
fun AnimatedMarkerExample() {
    val startPosition = GeoPointImpl.fromLatLong(37.7749, -122.4194)
    val endPosition = GeoPointImpl.fromLatLong(37.7849, -122.4094)

    var markerState by remember {
        mutableStateOf(
            MarkerState(
                position = startPosition,
                icon = DefaultIcon(fillColor = Color.Green, label = "Moving"),
                extra = "Animated marker"
            )
        )
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(2000)
            markerState = markerState.copy(
                position = if (markerState.position == startPosition) endPosition else startPosition
            )
        }
    }

    MapView(state = mapViewState) {
        Marker(markerState)
    }
}
```

