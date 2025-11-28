---
title: "MapViewHolder"
---

`MapViewHolder` は、MapConductor の統一 API でカバーされていない特定の地図SDK機能が必要な高度なユースケースのために、ネイティブ地図 SDK インスタンスへのアクセスを提供します。MapConductor は共通のインターフェースを提供しますが、すべてのネイティブ機能を完全にラップするわけではなく、MapViewHolder がこのギャップを埋めます。

## 概要

MapConductor は地図SDK間で統一された API を提供することを目指していますが、完全な機能パリティは必ずしも可能ではありません。MapViewHolder を使用すると、開発者は地図SDK固有の機能が必要な場合に、基盤となるネイティブ地図インスタンスにアクセスできます。

```kotlin
interface MapViewHolder<ActualMapViewType, ActualMapType> {
    val mapView: ActualMapViewType
    val map: ActualMapType
}
```

## MapViewHolder へのアクセス

各地図SDKの `MapViewState` 実装は、その特定の `MapViewHolder` へのアクセスを提供します:

```kotlin
// Google Maps
val googleMapState = GoogleMapViewStateImpl()
val googleHolder: GoogleMapViewHolder? = googleMapState.getMapViewHolder()

// Mapbox
val mapboxState = MapboxViewStateImpl()
val mapboxHolder: MapboxMapViewHolder? = mapboxState.getMapViewHolder()

// HERE Maps
val hereState = HereViewStateImpl()
val hereHolder: HereViewHolder? = hereState.getMapViewHolder()

// ArcGIS
val arcgisState = ArcGISMapViewStateImpl()
val arcgisHolder: ArcGISMapViewHolder? = arcgisState.getMapViewHolder()
```

## 地図SDK固有の実装

### Google Maps

```kotlin
typealias GoogleMapViewHolder = MapViewHolder<MapView, GoogleMap>

// ネイティブ Google Maps API にアクセス
googleHolder?.let { holder ->
    val nativeMapView: MapView = holder.mapView
    val googleMap: GoogleMap = holder.map

    // Google Maps 固有の機能を使用
    googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.style_json))
    googleMap.setOnPoiClickListener { poi ->
        // 関心点のクリックを処理
    }

    nativeMapView.onResume()
    nativeMapView.onPause()
}
```

### Mapbox

```kotlin
typealias MapboxMapViewHolder = MapViewHolder<MapView, MapboxMap>

// ネイティブ Mapbox API にアクセス
mapboxHolder?.let { holder ->
    val mapboxMapView: MapView = holder.mapView
    val mapboxMap: MapboxMap = holder.map

    // Mapbox 固有の機能を使用
    mapboxMap.getStyle { style ->
        style.addSource(GeoJsonSource("custom-source", geoJsonData))
        style.addLayer(FillLayer("custom-layer", "custom-source"))
    }

    // Mapbox プラグインにアクセス
    val locationComponent = mapboxMap.locationComponent
    locationComponent.activateLocationComponent(context)
}
```

### HERE Maps

```kotlin
typealias HereViewHolder = MapViewHolder<MapView, MapScene>

// ネイティブ HERE SDK API にアクセス
hereHolder?.let { holder ->
    val hereMapView: MapView = holder.mapView
    val mapScene: MapScene = holder.map

    // HERE 固有の機能を使用
    val searchEngine = SearchEngine()
    val textQuery = TextQuery("coffee shops", geoCoordinates)

    searchEngine.search(textQuery) { searchError, searchResults ->
        // HERE 検索結果を処理
    }

    // HERE ルーティング
    val routingEngine = RoutingEngine()
    // ルーティングを設定して使用
}
```

### ArcGIS

```kotlin
typealias ArcGISMapViewHolder = MapViewHolder<WrapSceneView, SceneView>

// ネイティブ ArcGIS API にアクセス
arcgisHolder?.let { holder ->
    val wrapSceneView: WrapSceneView = holder.mapView
    val sceneView: SceneView = holder.map

    // ArcGIS 固有の機能を使用
    val portal = Portal("https://www.arcgis.com/")
    val portalItem = PortalItem(portal, "your-webmap-id")
    val webMap = ArcGISMap(portalItem)

    sceneView.map = webMap

    // ArcGIS 分析ツール
    val serviceArea = ServiceAreaTask("https://route-api.arcgis.com/...")
    // 分析を設定して使用
}
```

## 一般的なユースケース

### カスタムスタイリング

```kotlin
@Composable
fun CustomStyledMap() {
    val mapViewState = rememberGoogleMapViewState()

    LaunchedEffect(mapViewState) {
        // 地図の初期化を待つ
        delay(1000)

        mapViewState.getMapViewHolder()?.let { holder ->
            val googleMap = holder.map

            // カスタム地図スタイルを適用
            val styleJson = loadStyleFromAssets(context, "custom_style.json")
            googleMap.setMapStyle(MapStyleOptions(styleJson))

            // 地図 UI を設定
            googleMap.uiSettings.isMyLocationButtonEnabled = false
            googleMap.uiSettings.isCompassEnabled = true
        }
    }

    // MapView を GoogleMapsView、MapboxMapView などの選択した地図SDKに置き換えてください
    GoogleMapsView(state = mapViewState) {
        // MapConductor コンポーネント
    }
}
```

### 高度な分析

```kotlin
@Composable
fun AnalyticsIntegration() {
    val mapViewState = remember { MapboxViewStateImpl() }

    LaunchedEffect(mapViewState) {
        mapViewState.getMapViewHolder()?.let { holder ->
            val mapboxMap = holder.map

            // カスタム地図イベントを追跡
            mapboxMap.addOnMapClickListener { point ->
                Analytics.track("map_click", mapOf(
                    "lat" to point.latitude,
                    "lng" to point.longitude,
                    "zoom" to mapboxMap.cameraPosition.zoom
                ))
                true
            }

            // スタイル変更を追跡
            mapboxMap.addOnStyleLoadedListener {
                Analytics.track("style_loaded", mapOf(
                    "style_id" to mapboxMap.style?.styleURI
                ))
            }
        }
    }

    MapboxMapView(state = mapViewState) {
        // MapConductor コンポーネント
    }
}
```

### パフォーマンス最適化

```kotlin
@Composable
fun PerformanceOptimizedMap() {
    val mapViewState = remember { HereViewStateImpl() }

    LaunchedEffect(mapViewState) {
        mapViewState.getMapViewHolder()?.let { holder ->
            val mapScene = holder.map

            // HERE 固有のパフォーマンス設定
            mapScene.setLayerVisibility(MapScene.Layers.TRAFFIC_FLOW, VisibilityState.VISIBLE)

            // 特定のユースケースのために最適化
            val mapSettings = mapScene.mapSettings
            mapSettings.isTiltGesturesEnabled = false
            mapSettings.isRotateGesturesEnabled = false

            // カスタム詳細レベル設定
            mapScene.setLevelOfDetail(LevelOfDetail.HIGH)
        }
    }

    HereMapView(state = mapViewState) {
        // MapConductor コンポーネント
    }
}
```

### サードパーティサービスとの統合

```kotlin
@Composable
fun ThirdPartyIntegration() {
    val mapViewState = rememberArcGISMapViewState()

    LaunchedEffect(mapViewState) {
        mapViewState.getMapViewHolder()?.let { holder ->
            val sceneView = holder.map

            // ArcGIS Online サービスと統合
            val featureTable = ServiceFeatureTable("https://services.arcgis.com/...")
            val featureLayer = FeatureLayer(featureTable)

            sceneView.map.basemap.baseLayers.add(featureLayer)

            // フィーチャーをクエリ
            val queryParams = QueryParameters().apply {
                whereClause = "STATE_NAME = 'California'"
            }

            featureTable.queryFeaturesAsync(queryParams).addDoneListener {
                val results = it.result
                // フィーチャークエリ結果を処理
            }
        }
    }

    ArcGISMapView(state = mapViewState) {
        // MapConductor コンポーネント
    }
}
```

## ライフサイクル管理

### 地図ライフサイクルイベント

```kotlin
@Composable
fun LifecycleAwareMap() {
    val mapViewState = rememberGoogleMapViewState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            mapViewState.getMapViewHolder()?.let { holder ->
                val mapView = holder.mapView

                when (event) {
                    Lifecycle.Event.ON_RESUME -> mapView.onResume()
                    Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                    Lifecycle.Event.ON_START -> mapView.onStart()
                    Lifecycle.Event.ON_STOP -> mapView.onStop()
                    Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                    else -> { }
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    GoogleMapsView(state = mapViewState) {
        // MapConductor コンポーネント
    }
}
```

## エラー処理と安全性

### Null 安全性

```kotlin
fun accessNativeMap(mapViewState: GoogleMapViewStateImpl) {
    val holder = mapViewState.getMapViewHolder()

    if (holder != null) {
        // ネイティブ API に安全にアクセス
        val googleMap = holder.map
        val mapView = holder.mapView

        // ネイティブ機能を使用
        googleMap.setOnMarkerClickListener { marker ->
            // マーカークリックを処理
            true
        }
    } else {
        // 地図がまだ初期化されていないか利用できない
        Log.w("MapAccess", "MapViewHolder が利用できません")
    }
}
```

### 初期化タイミング

```kotlin
@Composable
fun SafeNativeAccess() {
    val mapViewState = remember { MapboxViewStateImpl() }
    var isMapReady by remember { mutableStateOf(false) }

    LaunchedEffect(mapViewState) {
        // 地図が準備できるまでポーリング
        while (!isMapReady) {
            delay(100)
            isMapReady = mapViewState.getMapViewHolder() != null
        }

        // ネイティブ API を使用するのが安全
        mapViewState.getMapViewHolder()?.let { holder ->
            val mapboxMap = holder.map
            // ネイティブ機能を設定
        }
    }

    MapboxMapView(state = mapViewState) {
        // MapConductor コンポーネント
    }
}
```

## ベストプラクティス

1. **初期化チェック**: ネイティブ API にアクセスする前に、`getMapViewHolder()` が非 null を返すことを常に確認
2. **ライフサイクル認識**: ネイティブ API を使用する際は、地図のライフサイクルイベントを適切に処理
3. **エラー処理**: 地図SDK API が例外をスローする可能性があるため、ネイティブ API 呼び出しを try-catch ブロックでラップ
4. **ドキュメント**: ネイティブ API の使用については、各地図SDKの公式ドキュメントを参照
5. **テスト**: ネイティブ API を使用する際は、すべてのターゲット地図SDKで徹底的にテスト
6. **フォールバック**: ネイティブ機能が利用できない場合のフォールバック動作を提供
7. **バージョン互換性**: ネイティブ API の使用が、ターゲットとしている SDK バージョンと互換性があることを確認

## 制限と考慮事項

1. **プラットフォーム依存性**: ネイティブ API の使用は、特定の地図SDKにコードを結び付けます
2. **メンテナンスオーバーヘッド**: 地図SDK API の変更により、ネイティブ API の使用を更新する必要があります
3. **テストの複雑性**: 地図SDK固有のコードパスをカバーするため、より複雑なテストが必要
4. **機能パリティ**: すべての地図SDKが同等のネイティブ機能をサポートしているわけではありません
5. **MapConductor 統合**: ネイティブの変更は MapConductor の状態管理と統合されない可能性があります

MapViewHolder は高度なユースケースのための強力なエスケープハッチですが、MapConductor の統一 API アプローチの利点を維持するために慎重に使用する必要があります。
