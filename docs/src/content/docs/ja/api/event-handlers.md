---
title: "イベントハンドラ"
---

MapConductor は、マップとそのコンポーネントに対するユーザーインタラクションのための包括的なイベント処理を提供します。すべてのイベントは `MapViewContainer` コンポーネントを通じて処理されます。
すべてのイベントリスナーは MapView コンポーネントを通じて渡されます。

```kotlin
MapView(
    ...,
    onMapLoaded: OnMapLoadedHandler? = null,
    onMapClick: OnMapEventHandler? = null,
    onCameraMoveStart: OnCameraMoveHandler? = null,
    onCameraMove: OnCameraMoveHandler? = null,
    onCameraMoveEnd: OnCameraMoveHandler? = null,
    onMarkerClick: OnMarkerEventHandler? = null,
    onMarkerDragStart: OnMarkerEventHandler? = null,
    onMarkerDrag: OnMarkerEventHandler? = null,
    onMarkerDragEnd: OnMarkerEventHandler? = null,
    onMarkerAnimateStart: OnMarkerEventHandler? = null,
    onMarkerAnimateEnd: OnMarkerEventHandler? = null,
    onCircleClick: OnCircleEventHandler? = null,
    onPolylineClick: OnPolylineEventHandler? = null,
    onPolygonClick: OnPolygonEventHandler? = null,
    onGroundImageClick: OnGroundImageEventHandler? = null,
) { }
```

## マップイベント

### マップ初期化

- マップの読み込みが完了し、インタラクションの準備ができたときに呼び出されます。

    ```kotlin
    onMapLoaded: OnMapLoadedHandler? = null
    ```

-  例
    ```kotlin
    MapView(
        ...
        onMapLoaded: {
            println("Map loaded")
        },
    ) {
        ...
    }
    ```

### マップインタラクション

- ユーザーがマップ（オーバーレイではない部分）をタップしたときに呼び出されます。

    ```kotlin
    onMapClick: OnMapEventHandler? = null
    ```
**イベントデータ**: `GeoPoint` - タップされた地理座標


-  例
    ```kotlin
    MapView(
        ...
        onMapClick = { geoPoint ->
            println("Map clicked at: ${geoPoint.latitude}, ${geoPoint.longitude}")
        }
    ) {
        ...
    }
    ```

## マーカーイベント

すべてのマーカーイベントは、マーカーの現在の状態を含む `MarkerState` オブジェクトを受け取ります。

### クリックイベント

- マーカーがタップされたときに呼び出されます。
    ```kotlin
    onMarkerClick: OnMarkerEventHandler? = null
    ```

-  例
    ```kotlin
    MapView(
        ...
        onMarkerClick = { markerState ->
            println("Marker clicked at: ${geoPoint.latitude}, ${geoPoint.longitude}")
        },
    ) {
        ...
    }
    ```

### ドラッグイベント

- **`onMarkerDragStart`**: ドラッグ開始時に呼び出されます
- **`onMarkerDrag`**: ドラッグ中に継続的に呼び出されます
- **`onMarkerDragEnd`**: ドラッグ終了時に呼び出されます

    ```kotlin
    onMarkerDragStart: OnMarkerEventHandler? = null
    onMarkerDrag: OnMarkerEventHandler? = null
    onMarkerDragEnd: OnMarkerEventHandler? = null
    ```

    ```kotlin
    MapView(
        ...
            onMarkerDragStart = { draggedMarker ->
                println("Drag started: ${draggedMarker.id}")
            },
            onMarkerDrag = { draggedMarker ->
                if (draggedMarker.id == markerState.id) {
                    markerState = markerState.copy(position = draggedMarker.position)
                }
            },
            onMarkerDragEnd = { draggedMarker ->
                println("Drag ended: ${draggedMarker.id}")
            },
    ) { }
    ```

### アニメーションイベント

- マーカーアニメーションの開始時と終了時に呼び出されます。
    ```kotlin
    onMarkerAnimateStart: OnMarkerEventHandler? = null
    onMarkerAnimateEnd: OnMarkerEventHandler? = null
    ```


## オーバーレイイベント

### 円イベント

- 円がタップされたときに呼び出されます。

    ```kotlin
    onCircleClick: OnCircleEventHandler? = null
    ```

    **イベントデータ**: `CircleEvent`

    ```kotlin
    data class CircleEvent(
        val state: CircleState,
        val clicked: GeoPoint
    )
    ```

- 例
    ```

    MapView(
        ...
            onCircleClick = { event ->
                println("A circle clicked at: ${event.clicked.latitude}, ${event.clicked.longitude}")

                event.state.fillColor = Color.Red.copy(
                    opacity = 0.7f,
                )
            },
    ) { }
    ```

### ポリラインイベント

- ポリラインがタップされたときに呼び出されます。

    ```kotlin
    onPolylineClick: OnPolylineEventHandler? = null
    ```

    **イベントデータ**: `PolylineEvent`
    ```kotlin
    data class PolylineEvent(
        val state: PolylineState,
        val clicked: GeoPoint
    )
    ```

- 例
    ```
    MapView(
        ...
            onPolylineClick = { event ->
                println("A circle clicked at: ${event.clicked.latitude}, ${event.clicked.longitude}")

                event.state.fillColor = Color.Red.copy(
                    opacity = 0.7f,
                )
            },
    ) { }
    ```

### ポリゴンイベント

```kotlin
onPolygonClick: OnPolygonEventHandler? = null
```

**イベントデータ**: `PolygonEvent`
```kotlin
data class PolygonEvent(
    val state: PolygonState,
    val clicked: GeoPoint?
)
```

### グラウンドイメージイベント

```kotlin
onGroundImageClick: OnGroundImageEventHandler? = null
```

**イベントデータ**: `GroundImageEvent`
```kotlin
data class GroundImageEvent(
    val state: GroundImageState,
    val clicked: GeoPointImpl?
)
```

## 使用例

### 基本的なイベント処理

```kotlin
// GoogleMapView、MapboxMapView など、選択した地図SDKに置き換えてください
MapView(
    state = mapViewState,
    onMapClick = { geoPoint ->
        println("Map clicked at: ${geoPoint.latitude}, ${geoPoint.longitude}")
    },
    onMarkerClick = { markerState ->
        println("Marker clicked: ${markerState.extra}")
    },
    onCircleClick = { circleEvent ->
        println("Circle clicked: ${circleEvent.state.extra}")
    }
) {
    // マップコンテンツ
}
```

### 高度なイベント処理

```kotlin
@Composable
fun AdvancedEventHandlingExample() {
    var selectedItem by remember { mutableStateOf<String?>(null) }
    var draggedMarker by remember { mutableStateOf<MarkerState?>(null) }

    // GoogleMapView、MapboxMapView など、選択した地図SDKに置き換えてください
    MapView(
        state = mapViewState,
        onMapClick = { geoPoint ->
            selectedItem = null
            println("Map clicked at: $geoPoint")
        },
        onMarkerClick = { markerState ->
            selectedItem = "Marker: ${markerState.extra}"
        },
        onMarkerDragStart = { markerState ->
            draggedMarker = markerState
            println("Started dragging: ${markerState.id}")
        },
        onMarkerDrag = { markerState ->
            draggedMarker = markerState
            println("Dragging to: ${markerState.position}")
        },
        onMarkerDragEnd = { markerState ->
            draggedMarker = null
            println("Finished dragging: ${markerState.id}")
        },
        onCircleClick = { circleEvent ->
            selectedItem = "Circle: ${circleEvent.state.extra}"
        },
        onPolylineClick = { polylineEvent ->
            selectedItem = "Polyline: ${polylineEvent.state.extra}"
        },
        onPolygonClick = { polygonEvent ->
            selectedItem = "Polygon: ${polygonEvent.state.extra}"
        }
    ) {
        // マップコンポーネント
    }

    // 選択されたアイテム情報を表示
    selectedItem?.let { item ->
        Card(modifier = Modifier.padding(16.dp)) {
            Text(item, modifier = Modifier.padding(16.dp))
        }
    }
}
```

### 条件付きイベント処理

```kotlin
@Composable
fun ConditionalEventExample() {
    var editMode by remember { mutableStateOf(false) }
    var selectedMarkers by remember { mutableStateOf<Set<String>>(emptySet()) }

    // GoogleMapView、MapboxMapView など、選択した地図SDKに置き換えてください
    MapView(
        state = mapViewState,
        onMapClick = { geoPoint ->
            if (editMode) {
                // 編集モードで新しいマーカーを追加
                addNewMarker(geoPoint)
            } else {
                // 表示モードで選択をクリア
                selectedMarkers = emptySet()
            }
        },
        onMarkerClick = { markerState ->
            if (editMode) {
                // 編集モードで選択を切り替え
                selectedMarkers = if (markerState.id in selectedMarkers) {
                    selectedMarkers - markerState.id
                } else {
                    selectedMarkers + markerState.id
                }
            } else {
                // 表示モードで情報を表示
                showMarkerInfo(markerState)
            }
        },
        onMarkerDragStart = { markerState ->
            if (!editMode) {
                // 表示モードではドラッグを防止
                return@MapViewContainer
            }
        }
    ) {
        // マップコンテンツ
    }
}
```

### イベントデバウンス

```kotlin
@Composable
fun DebouncedEventExample() {
    var searchLocation by remember { mutableStateOf<GeoPoint?>(null) }

    // 過剰な API 呼び出しを避けるために検索をデバウンス
    LaunchedEffect(searchLocation) {
        searchLocation?.let { location ->
            delay(500) // 500ms 待機
            performLocationSearch(location)
        }
    }

    // GoogleMapView、MapboxMapView など、選択した地図SDKに置き換えてください
    MapView(
        state = mapViewState,
        onMapClick = { geoPoint ->
            searchLocation = geoPoint
        }
    ) {
        searchLocation?.let { location ->
            Marker(
                position = location,
                icon = DefaultIcon(
                    fillColor = Color.Blue,
                    label = "🔍"
                )
            )
        }
    }
}
```

### マルチタッチジェスチャーイベント

```kotlin
@Composable
fun GestureEventExample() {
    var gestureInfo by remember { mutableStateOf("No gesture") }

    // GoogleMapView、MapboxMapView など、選択した地図SDKに置き換えてください
    MapView(
        state = mapViewState,
        onMapClick = { geoPoint ->
            gestureInfo = "Single tap at: $geoPoint"
        },
        // 注: 長押しやその他のジェスチャーは、
        // 基盤となるマップ地図SDKのジェスチャーシステムを通じて処理されます
    ) {
        // ジェスチャー情報を表示
        Text(
            text = gestureInfo,
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.7f))
                .padding(8.dp),
            color = Color.White
        )
    }
}
```

## イベントハンドラの型

### OnMapEventHandler

- 定義
    ```kotlin
    typealias OnMapEventHandler = (GeoPoint) -> Unit
    ```

```
typealias OnMarkerEventHandler = (MarkerState) -> Unit
typealias OnCircleEventHandler = (CircleEvent) -> Unit
typealias OnPolylineEventHandler = (PolylineEvent) -> Unit
typealias OnPolygonEventHandler = (PolygonEvent) -> Unit
typealias OnGroundImageEventHandler = (GroundImageEvent) -> Unit
```

## ベストプラクティス

1. **パフォーマンス**: イベントハンドラ内で重い計算を避ける
2. **状態更新**: イベントハンドラを使用してアプリケーション状態を更新する
3. **ユーザーフィードバック**: ユーザーインタラクションに対して即座に視覚的なフィードバックを提供する
4. **エラー処理**: null 値やエッジケースを適切に処理する
5. **デバウンス**: ドラッグなどの高頻度イベントをデバウンスしてパフォーマンスを向上させる
6. **条件付きロジック**: アプリケーション状態を使用してイベント動作を決定する
7. **イベント伝播**: 一部のイベントがバブルアップまたは消費される可能性があることに注意する
