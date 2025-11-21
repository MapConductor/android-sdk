---
title: "InfoBubble（情報バブル）"
---

`InfoBubble` はマーカーや任意の位置に紐づけて表示できる情報ウィンドウです。タイトルや本文、カスタムコンテンツを表示できます。

## 基本的な使い方

```kotlin
@Composable
fun MapViewScope.InfoBubble(
    target: GeoPoint,
    modifier: Modifier = Modifier,
    anchor: Offset = Offset(0.5f, 1.0f),
    onDismissRequest: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
)
```

- **`target`**: バブルを表示する地理座標
- **`anchor`**: バブルのアンカー位置
- **`onDismissRequest`**: バブルを閉じる処理を呼び出すコールバック

## マーカーと組み合わせた例

```kotlin
@Composable
fun MarkerWithInfoBubbleExample() {
    var selectedMarker by remember { mutableStateOf<MarkerState?>(null) }

    MapView(
        state = mapViewState,
        onMarkerClick = { markerState ->
            selectedMarker = markerState
        }
    ) {
        val sanFrancisco = GeoPointImpl.fromLatLong(37.7749, -122.4194)
        val markerState = MarkerState(position = sanFrancisco, extra = "San Francisco")

        Marker(markerState)

        selectedMarker?.let { state ->
            InfoBubble(target = state.position, onDismissRequest = { selectedMarker = null }) {
                Text("Marker: ${state.extra}")
            }
        }
    }
}
```

