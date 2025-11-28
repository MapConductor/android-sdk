---
title: "PolylineState（ポリライン状態）"
---

`PolylineState` は、地図上のポリライン（線分）の構成と動作を管理します。点、スタイル、測地線オプションのためのリアクティブなプロパティを提供します。

## コンストラクタ

```kotlin
PolylineState(
    points: List<GeoPoint>,
    id: String? = null,
    strokeColor: Color = Color.Black,
    strokeWidth: Dp = 1.dp,
    geodesic: Boolean = false,
    extra: Serializable? = null
)
```

## プロパティ

### 基本プロパティ
- **`id: String`**: 一意の識別子（指定されていない場合は自動生成）
- **`points: List<GeoPoint>`**: 線を定義する地理座標のリスト
- **`extra: Serializable?`**: ポリラインに付加される追加データ

### ビジュアルプロパティ
- **`strokeColor: Color`**: 線の色
- **`strokeWidth: Dp`**: 線の太さ
- **`geodesic: Boolean`**: 測地線（地球の曲率に従う線）を描画するかどうか

## メソッド

```kotlin
fun copy(...): PolylineState // 変更されたコピーを作成
fun fingerPrint(): PolylineFingerPrint // 変更検知
fun asFlow(): Flow<PolylineFingerPrint> // リアクティブ更新
```

## 使用例

### 基本的なポリライン

```kotlin
val polylineState = PolylineState(
    points = listOf(
        GeoPointImpl.fromLatLong(37.7749, -122.4194),
        GeoPointImpl.fromLatLong(37.7849, -122.4094),
        GeoPointImpl.fromLatLong(37.7949, -122.3994)
    ),
    strokeColor = Color.Blue,
    strokeWidth = 3.dp
)

// MapViewは、GoogleMapView、MapboxMapViewなど、選択した地図SDKーに置き換えてください
MapView(state = mapViewState) {
    Polyline(polylineState)
}
```

### 動的なポリライン

```kotlin
@Composable
fun DynamicPolylineExample() {
    var polylineState by remember {
        mutableStateOf(
            PolylineState(
                points = listOf(
                    GeoPointImpl.fromLatLong(37.7749, -122.4194),
                    GeoPointImpl.fromLatLong(37.7849, -122.4094)
                ),
                strokeColor = Color.Red,
                strokeWidth = 4.dp
            )
        )
    }

    // MapViewは、GoogleMapView、MapboxMapViewなど、選択した地図SDKーに置き換えてください
MapView(
        state = mapViewState,
        onMapClick = { geoPoint ->
            polylineState = polylineState.copy(
                points = polylineState.points + geoPoint
            )
        }
    ) {
        Polyline(polylineState)
    }
}
```

### 測地線ポリライン

```kotlin
val geodesicPolyline = PolylineState(
    points = listOf(
        GeoPointImpl.fromLatLong(37.7749, -122.4194), // サンフランシスコ
        GeoPointImpl.fromLatLong(40.7128, -74.0060)   // ニューヨーク
    ),
    strokeColor = Color.Green,
    strokeWidth = 3.dp,
    geodesic = true, // 地球の曲率に従う
    extra = "Cross-country route"
)
```

### 複数セグメントのルート

```kotlin
@Composable
fun RouteExample() {
    val routeSegments = remember {
        listOf(
            PolylineState(
                points = highway1Points,
                strokeColor = Color.Blue,
                strokeWidth = 6.dp,
                extra = "Highway segment"
            ),
            PolylineState(
                points = localRoadPoints,
                strokeColor = Color.Green,
                strokeWidth = 3.dp,
                extra = "Local road segment"
            ),
            PolylineState(
                points = walkingPathPoints,
                strokeColor = Color.Orange,
                strokeWidth = 2.dp,
                extra = "Walking path"
            )
        )
    }

    // MapViewは、GoogleMapView、MapboxMapViewなど、選択した地図SDKーに置き換えてください
MapView(state = mapViewState) {
        routeSegments.forEach { segment ->
            Polyline(segment)
        }
    }
}
```

## イベント処理

ポリラインイベントは状態とクリック位置の両方を提供します：

```kotlin
data class PolylineEvent(
    val state: PolylineState,
    val clicked: GeoPoint
)

typealias OnPolylineEventHandler = (PolylineEvent) -> Unit
```

## ベストプラクティス

1. **点の密度**: 詳細とパフォーマンスのバランスを取る - 過度な点は避けてください
2. **測地線**: 長距離ルートの場合は正確な経路を示すために測地線を使用してください
3. **色分け**: 異なる色を使用してルートタイプを区別してください
4. **太さの階層**: ストロークの太さを使用して重要度を示してください
5. **パフォーマンス**: 異なるズームレベルで複雑なポリラインの簡略化を検討してください
