# セットアップ

1. リポジトリをクローン
```
git clone https://github.com/MapConductor/android-sdk.git
```

2. https://github.com/MapConductor/map-sdk-credentials/ から`secrets.properties` をプロジェクトルートに追加保存する

3. Android Studioでビルド

# コーディングスタイル

KtLintに従います。ローカルで実行する場合は、下記コマンドを実行します（可能ならば自動修正されます）。

```
./gradlew allLintChecks
```

# 基本実装状況 (Android)

|                 | Google Maps | Mapbox   | Here     | ArcGIS         |
|-----------------|-------------|----------|----------|----------------|
| Map             | &#x2611;    | &#x2611; | &#x2611; | &#x2611;       |
| Marker          | &#x2611;    | &#x2611; | &#x2611; | &#x2611;       |
| Circle          | &#x2611;    | &#x2611; | &#x2610; | &#x2610; (wip) |
| Polyline        | &#x2611;    | &#x2611; | &#x2611; | &#x2611;       |
| Polygon         | &#x2610;    | &#x2610; | &#x2610; | &#x2610;       |
| GroundOverlay   | &#x2610;    | &#x2610; | &#x2610; | &#x2610;       |
| RasterTileLayer | &#x2610;    | &#x2610; | &#x2610; | &#x2610;       |
| VectorTileLayer | &#x2610;    | &#x2610; | &#x2610; | &#x2610;       |
