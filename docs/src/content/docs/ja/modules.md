---
title: モジュール構成
---

MapConductor は複数の Gradle モジュールに分割されているため、必要なものだけに依存できます。このページでは、v{BOM_MODULE_VERSION} の各 `mapconductor-xxx` モジュールを要約します。

## コアと BOM

### `mapconductor-bom`

すべての MapConductor アーティファクトの Bill of Materials です。

- すべてのモジュール間でバージョンを調整します
- すべてのプロジェクトに推奨されます

```kotlin
implementation(platform("com.mapconductor:mapconductor-bom:{BOM_MODULE_VERSION}"))
```

### `mapconductor-core`

コアランタイムと共有抽象化:

- ジオメトリ型: `GeoPoint`, `GeoRectBounds`
- カメラ型: `MapCameraPosition`, `VisibleRegion`
- マップ抽象化: `MapViewState`, `MapViewController`, `MapViewBase`
- オーバーレイプリミティブ: `MarkerState`, `PolylineState`, `PolygonState`, `CircleState`, `GroundImageState`

他のすべてのモジュールは `mapconductor-core` の上に構築されています。

## 地図SDK統合

地図SDK固有のモジュールは、各 SDK に統一 API を実装します:

### `mapconductor-for-googlemaps`

- `GoogleMapView` Composable
- `GoogleMapViewStateImpl`
- Google Maps 固有のオーバーレイコントローラ

### `mapconductor-for-mapbox`

- `MapboxMapView` Composable
- `MapboxViewStateImpl`

### `mapconductor-for-here`

- `HereMapView` Composable
- `HereViewStateImpl`

### `mapconductor-for-arcgis`

- `ArcGISMapView` Composable
- `ArcGISMapViewStateImpl`

### `mapconductor-for-maplibre`

- `MapLibreMapView` Composable
- `MapLibreViewStateImpl`

各地図SDKモジュールは:

- `MapViewState` とコントローラバインディングを実装します
- 地図SDK固有のカメラと表示可能領域を `MapCameraPosition` にマッピングします
- オーバーレイコントローラ（マーカー、ポリライン、ポリゴン、円、サポートされている場合はグラウンドイメージ）を公開します

## 実験的 / ユーティリティモジュール

### `mapconductor-icons`

純粋な Compose で実装された Composable マーカーアイコン:

- `CircleIcon`, `FlagIcon`
- 情報バブルスタイル（丸、尾付きなど）

地図SDK固有の drawable に依存せずに、地図SDK間で一貫したマーカービジュアルが必要な場合に便利です。

### `mapconductor-marker-strategy`

高レベルのマーカーレンダリング戦略:

- 空間 / クラスタリングのような戦略
- リモート駆動型マーカーセットの抽象化

共有マーカーインターフェースを介して任意の地図SDKモジュールと連携するように設計されています。

### `mapconductor-marker-native-strategy`

非常に大きなマーカーセットのためのネイティブアクセラレーテッド戦略:

- 大規模なパフォーマンスに焦点を当てる
- 通常、`mapconductor-marker-strategy` と組み合わせて使用されます

## 例示アプリケーション

### `example-app`

以下を実証するショーケースアプリケーション:

- 基本的なマップ使用と地図SDKの切り替え
- カメラ処理と表示可能領域（`VisibleRegionPage`, `ZoomCalibrationPage`）
- ポリライン、ポリゴン、円、グラウンドイメージ
- 情報バブルとカスタムマーカーアイコン

### `simple-map-app`

クイック統合テストとデバッグのための最小限の例です。

## モジュールの選択方法

典型的な構成:

```kotlin
// 最小限: Google Maps のみ
implementation(platform("com.mapconductor:mapconductor-bom:{BOM_MODULE_VERSION}"))
implementation("com.mapconductor:core")
implementation("com.mapconductor:for-googlemaps")
```
```
// アイコンと戦略を使用したマルチ地図SDK
implementation(platform("com.mapconductor:mapconductor-bom:{BOM_MODULE_VERSION}"))
implementation("com.mapconductor:core")
implementation("com.mapconductor:for-googlemaps")
implementation("com.mapconductor:for-mapbox")
implementation("com.mapconductor:icons")
implementation("com.mapconductor:marker-strategy")
```

このページを高レベルのマップとして使用してください。各領域（コア、コンポーネント、状態、実験的）の詳細な API 情報は、既存の mdBook セクション（`docs/src/core`, `docs/src/components` など）から移行できます。
