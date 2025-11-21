---
title: MapLibre セットアップ
---

# MapLibre セットアップ

このページでは、MapLibre と MapConductor を組み合わせて利用するためのセットアップ手順について説明します。

## 前提条件

- MapLibre を利用可能な環境（タイルサーバーやスタイルJSONなど）が用意されている

## Gradle への依存関係追加

```kotlin
dependencies {
    implementation(platform("com.mapconductor:mapconductor-bom:1.1.0"))
    implementation("com.mapconductor:core")
    implementation("com.mapconductor:for-maplibre")
    // MapLibre Native / GL for Android の依存関係も追加
}
```

MapLibre 本体の依存関係やタイルサーバーの設定は、MapLibre のドキュメントに従ってください。

## MapConductor での利用例

```kotlin
@Composable
fun MapLibreExample() {
    val camera = MapCameraPositionImpl(
        position = GeoPointImpl.fromLatLong(51.5074, -0.1278),
        zoom = 12.0,
    )

    val mapViewState = rememberMapLibreMapViewState(
        cameraPosition = camera,
        // 必要に応じてスタイルなどを指定
    )

    MapLibreMapView(
        state = mapViewState,
        onMapClick = { point ->
            println("Clicked: ${point.latitude}, ${point.longitude}")
        }
    ) {
        Marker(
            position = GeoPointImpl.fromLatLong(51.5074, -0.1278),
            icon = DefaultIcon(label = "London"),
        )
    }
}
```

オープンなタイルソースや独自スタイルを組み合わせることで、柔軟な地図表現が可能です。

