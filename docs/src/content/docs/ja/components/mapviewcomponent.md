---
title: "MapViewComponent"
---

MapConductor は、アプリケーションで地図を表示するための基盤となる、プロバイダ固有の地図ビューコンポーネントを提供します。各地図プロバイダには独自の実装がありますが、一貫した API インターフェースを維持しています。

## プロバイダ固有のコンポーネント

MapConductor は複数の地図プロバイダをサポートしており、それぞれに専用のコンポーネントがあります:

### GoogleMapsView
Google Maps 統合用:
```kotlin
GoogleMapsView(
    state: GoogleMapViewStateImpl,
    modifier: Modifier = Modifier,
    markerRenderingStrategy: MarkerRenderingStrategy<GoogleMapActualMarker>? = null,
    onMapViewInitialized: OnMapViewInitializedHandler? = null,
    onMapLoaded: OnMapLoadedHandler? = null,
    onMapClick: OnMapEventHandler? = null,
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
    content: (@Composable MapViewScope.() -> Unit)? = null
)
```

### MapboxMapView
Mapbox 統合用:
```kotlin
MapboxMapView(
    state: MapboxViewStateImpl,
    modifier: Modifier = Modifier,
    markerRenderingStrategy: MarkerRenderingStrategy<MapboxActualMarker>? = null,
    onMapViewInitialized: OnMapViewInitializedHandler? = null,
    onMapLoaded: OnMapLoadedHandler? = null,
    onMapClick: OnMapEventHandler? = null,
    onMarkerClick: OnMarkerEventHandler? = null,
    onMarkerDragStart: OnMarkerEventHandler? = null,
    onMarkerDrag: OnMarkerEventHandler? = null,
    onMarkerDragEnd: OnMarkerEventHandler? = null,
    onMarkerAnimateStart: OnMarkerEventHandler? = null,
    onMarkerAnimateEnd: OnMarkerEventHandler? = null,
    onCircleClick: OnCircleEventHandler? = null,
    onPolylineClick: OnPolylineEventHandler? = null,
    onPolygonClick: OnPolygonEventHandler? = null,
    content: (@Composable MapViewScope.() -> Unit)? = null
)
```

### HereMapView
HERE Maps 統合用:
```kotlin
HereMapView(
    state: HereViewStateImpl,
    modifier: Modifier = Modifier,
    markerRenderingStrategy: MarkerRenderingStrategy<HereActualMarker>? = null,
    onMapViewInitialized: OnMapViewInitializedHandler? = null,
    onMapLoaded: OnMapLoadedHandler? = null,
    onMapClick: OnMapEventHandler? = null,
    onMarkerClick: OnMarkerEventHandler? = null,
    onMarkerDragStart: OnMarkerEventHandler? = null,
    onMarkerDrag: OnMarkerEventHandler? = null,
    onMarkerDragEnd: OnMarkerEventHandler? = null,
    onMarkerAnimateStart: OnMarkerEventHandler? = null,
    onMarkerAnimateEnd: OnMarkerEventHandler? = null,
    onCircleClick: OnCircleEventHandler? = null,
    onPolylineClick: OnPolylineEventHandler? = null,
    onPolygonClick: OnPolygonEventHandler? = null,
    content: (@Composable MapViewScope.() -> Unit)? = null
)
```

### ArcGISMapView
ArcGIS 統合用:
```kotlin
ArcGISMapView(
    state: ArcGISMapViewStateImpl,
    modifier: Modifier = Modifier,
    markerRenderingStrategy: MarkerRenderingStrategy<ArcGISActualMarker>? = null,
    onMapViewInitialized: OnMapViewInitializedHandler? = null,
    onMapLoaded: OnMapLoadedHandler? = null,
    onMapClick: OnMapEventHandler? = null,
    onMarkerClick: OnMarkerEventHandler? = null,
    onMarkerDragStart: OnMarkerEventHandler? = null,
    onMarkerDrag: OnMarkerEventHandler? = null,
    onMarkerDragEnd: OnMarkerEventHandler? = null,
    onMarkerAnimateStart: OnMarkerEventHandler? = null,
    onMarkerAnimateEnd: OnMarkerEventHandler? = null,
    onCircleClick: OnCircleEventHandler? = null,
    onPolylineClick: OnPolylineEventHandler? = null,
    onPolygonClick: OnPolygonEventHandler? = null,
    content: (@Composable MapViewScope.() -> Unit)? = null
)
```

### MapLibreMapView
MapLibre 統合用:
```kotlin
MapLibreMapView(
    state: MapLibreViewStateImpl,
    modifier: Modifier = Modifier,
    markerRenderingStrategy: MarkerRenderingStrategy<MapLibreActualMarker>? = null,
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
    onPolylineClick: OnPolylineEventHandler? = null,
    onCircleClick: OnCircleEventHandler? = null,
    onPolygonClick: OnPolygonEventHandler? = null,
    content: (@Composable MapLibreMapViewScope.() -> Unit)? = null
)
```

## 共通パラメータ

すべての地図ビューコンポーネントは以下のパラメータを共有しています:

### コアパラメータ

- **`modifier`**: スタイリングとレイアウトのための Compose modifier
- **`state`**: プロバイダ固有の地図ビュー状態実装
- **`content`**: 地図オーバーレイ（マーカー、円など）を含む Composable コンテンツ

### イベントハンドラ

- **`onMapViewInitialized`**: 地図ビューが最初に初期化されたときに呼び出されます
- **`onMapLoaded`**: 地図の読み込みが完了したときに呼び出されます
- **`onMapClick`**: ユーザーが地図をタップしたときに呼び出されます
- **カメライベント**: `onCameraMoveStart`、`onCameraMove`、`onCameraMoveEnd`
- **マーカーイベント**: `onMarkerClick`、`onMarkerDragStart`、`onMarkerDrag`、`onMarkerDragEnd`、`onMarkerAnimateStart`、`onMarkerAnimateEnd`
- **オーバーレイイベント**: `onCircleClick`、`onPolylineClick`、`onPolygonClick`、`onGroundImageClick`

### 高度なパラメータ

- **`markerRenderingStrategy`**: パフォーマンス最適化のためのカスタムマーカーレンダリング戦略

## 使用例

### 基本的な Google Maps の実装

```kotlin
@Composable
fun GoogleMapsExample() {
    val mapViewState = rememberGoogleMapViewState()

    GoogleMapsView(
        state = mapViewState,
        onMapClick = { geoPoint ->
            println("Map clicked at: ${geoPoint.latitude}, ${geoPoint.longitude}")
        },
        onMarkerClick = { markerState ->
            println("Marker clicked: ${markerState.extra}")
        }
    ) {
        Marker(
            position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            icon = DefaultIcon(label = "SF"),
            extra = "San Francisco"
        )

        Circle(
            center = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            radiusMeters = 1000.0,
            strokeColor = Color.Blue,
            fillColor = Color.Blue.copy(alpha = 0.3f)
        )
    }
}
```
![](/img/introduction/basic-googlemaps-example.jpg)

### 基本的な Mapbox の実装

```kotlin
@Composable
fun MapboxExample() {
    val mapViewState = remember { MapboxViewStateImpl() }

    MapboxMapView(
        state = mapViewState,
        onMapClick = { geoPoint ->
            println("Map clicked at: ${geoPoint.latitude}, ${geoPoint.longitude}")
        }
    ) {
        Marker(
            position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            icon = DefaultIcon(label = "MB"),
            extra = "Mapbox marker"
        )

        Polyline(
            points = listOf(
                GeoPointImpl.fromLatLong(37.7749, -122.4194),
                GeoPointImpl.fromLatLong(37.7849, -122.4094),
                GeoPointImpl.fromLatLong(37.7949, -122.3994)
            ),
            strokeColor = Color.Red,
            strokeWidth = 3.dp
        )
    }
}
```
![](/img/introduction/basic-mapbox-example.jpg)

### プロバイダに依存しないパターン

各プロバイダには特定のコンポーネントが必要ですが、プロバイダに依存しないコンテンツを作成できます:

```kotlin
@Composable
fun MapContent() {
    Marker(
        position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        icon = DefaultIcon(label = "Point"),
        extra = "Common marker"
    )

    Circle(
        center = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        radiusMeters = 500.0,
        strokeColor = Color.Green,
        fillColor = Color.Green.copy(alpha = 0.2f)
    )
}

@Composable
fun GoogleMapsScreen() {
    val state = rememberGoogleMapViewState()
    GoogleMapsView(state = state) {
        MapContent() // 再利用可能なコンテンツ
    }
}

@Composable
fun MapboxScreen() {
    val state = remember { MapboxViewStateImpl() }
    MapboxMapView(state = state) {
        MapContent() // 同じコンテンツ、異なるプロバイダ
    }
}
```

### 高度なイベント処理

```kotlin
@Composable
fun AdvancedMapExample() {
    val mapViewState = rememberGoogleMapViewState()
    var selectedMarker by remember { mutableStateOf<MarkerState?>(null) }

    GoogleMapsView(
        state = mapViewState,
        onMapViewInitialized = {
            println("Map initialized successfully")
        },
        onMapLoaded = {
            println("Map loaded and ready")
        },
        onMapClick = { geoPoint ->
            selectedMarker = null // 地図クリックで選択解除
        },
        onMarkerClick = { markerState ->
            selectedMarker = markerState
        },
        onMarkerDragStart = { markerState ->
            println("Started dragging marker: ${markerState.id}")
        },
        onMarkerDrag = { markerState ->
            println("Dragging marker to: ${markerState.position}")
        },
        onMarkerDragEnd = { markerState ->
            println("Finished dragging marker: ${markerState.id}")
        }
    ) {
        // インタラクティブなマーカーを持つ地図コンテンツ
        Marker(
            position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            icon = DefaultIcon(
                fillColor = if (selectedMarker?.id == "marker1") Color.Yellow else Color.Blue,
                label = "1"
            ),
            draggable = true,
            extra = "Draggable marker 1"
        )

        Marker(
            position = GeoPointImpl.fromLatLong(37.7849, -122.4094),
            icon = DefaultIcon(
                fillColor = if (selectedMarker?.id == "marker2") Color.Yellow else Color.Red,
                label = "2"
            ),
            extra = "Clickable marker 2"
        )

        // 選択されたマーカーの情報を表示
        selectedMarker?.let { marker ->
            // ここで InfoBubble やその他の UI を表示できます
        }
    }
}
```

## プロバイダの違い

API は全プロバイダで一貫していますが、以下の点で違いがあります:

### サポートされる機能
- **GroundImage**: 現在 Google Maps と ArcGIS でサポートされています
- **マーカーアニメーション**: Google Maps と Mapbox で利用可能
- **カスタムスタイリング**: 各プロバイダには異なる地図スタイルオプションがあります

### パフォーマンス特性
- **Google Maps**: 一般的な使用に優れており、優れたマーカーパフォーマンス
- **Mapbox**: カスタムスタイリングと大規模データセットに最適
- **HERE Maps**: ロケーションサービス統合に最適化
- **ArcGIS**: GIS およびエンタープライズアプリケーションに最適

### プラットフォーム統合
各プロバイダには、API キー、パーミッション、プラットフォームセットアップに関して異なる要件がある場合があります。プロバイダ固有のセットアップについては、初期化ドキュメントを参照してください。

## ベストプラクティス

1. **適切なプロバイダを選択**: アプリの特定のニーズに基づいて選択してください（スタイリング、パフォーマンス、機能）
2. **一貫した状態管理**: プロバイダに関係なく、同じ状態パターンを使用してください
3. **再利用可能なコンテンツ**: 可能な限りプロバイダに依存しない Composable コンテンツを作成してください
4. **イベント処理**: より良いユーザーエクスペリエンスのために包括的なイベント処理を実装してください
5. **エラー処理**: 常に初期化の失敗を処理し、フォールバック UI を提供してください
6. **パフォーマンス**: 大量のマーカーに対してはカスタムレンダリング戦略の使用を検討してください
7. **テスト**: 互換性を確保するために、複数のプロバイダでアプリケーションをテストしてください
