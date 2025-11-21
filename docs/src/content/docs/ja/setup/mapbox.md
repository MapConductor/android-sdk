---
title: Mapbox セットアップ
---

# Mapbox セットアップ

このページでは、Mapbox と MapConductor を組み合わせて利用するためのセットアップ手順について説明します。

## 前提条件

- Mapbox アカウントを作成済み
- アクセストークンを取得済み
- Mapbox スタイル（マップスタイルURL）が用意されている

## Gradle への依存関係追加

```kotlin
dependencies {
    implementation(platform("com.mapconductor:mapconductor-bom:1.1.0"))
    implementation("com.mapconductor:core")
    implementation("com.mapconductor:for-mapbox")
    // Mapbox Maps SDK for Android の依存関係も追加
}
```

Mapbox SDK 本体の依存関係は、Mapbox の公式ドキュメントに従って追加してください。

## 初期化とスタイル設定

Mapbox はアプリケーション起動時にアクセストークンを設定する必要があります。`Application` クラスなどで初期化を行います。

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Mapbox の初期化（実際のコードは SDK バージョンによって異なります）
    }
}
```

## MapConductor での利用例

```kotlin
@Composable
fun MapboxExample() {
    val camera = MapCameraPositionImpl(
        position = GeoPointImpl.fromLatLong(40.7128, -74.0060),
        zoom = 12.0,
    )

    val mapViewState = rememberMapboxMapViewState(
        cameraPosition = camera,
        // 必要に応じてスタイルURLなどを指定
    )

    MapboxMapView(
        state = mapViewState,
        onMapClick = { point ->
            println("Clicked: ${point.latitude}, ${point.longitude}")
        }
    ) {
        Marker(
            position = GeoPointImpl.fromLatLong(40.7128, -74.0060),
            icon = DefaultIcon(label = "NYC"),
        )
    }
}
```

Mapbox 固有の機能（スタイルレイヤーの操作など）が必要な場合は、Mapbox SDK の機能と併用してください。

