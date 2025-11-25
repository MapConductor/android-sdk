---
title: ArcGIS セットアップ
---

# ArcGIS セットアップ

このページでは、ArcGIS と MapConductor を組み合わせて利用するためのセットアップ手順について説明します。

## 前提条件

- ArcGIS アカウントまたは開発者ライセンスを取得済み
- 必要な API キーや認証設定が完了している

## Gradle への依存関係追加

```kotlin
dependencies {
    implementation(platform("com.mapconductor:mapconductor-bom:1.1.1"))
    implementation("com.mapconductor:core")
    implementation("com.mapconductor:for-arcgis")
    // ArcGIS Maps SDK for Kotlin / Android の依存関係も追加
}
```

ArcGIS SDK 本体の依存関係やライセンスキーの設定方法は、ArcGIS の公式ドキュメントに従ってください。

## MapConductor での利用例

```kotlin
@Composable
fun ArcGISExample() {
    val camera = MapCameraPositionImpl(
        position = GeoPointImpl.fromLatLong(34.0522, -118.2437),
        zoom = 11.0,
    )

    val mapViewState = rememberArcGISMapViewState(
        cameraPosition = camera,
    )

    ArcGISMapView(
        state = mapViewState,
        onMapClick = { point ->
            println("Clicked: ${point.latitude}, ${point.longitude}")
        }
    ) {
        Marker(
            position = GeoPointImpl.fromLatLong(34.0522, -118.2437),
            icon = DefaultIcon(label = "LA"),
        )
    }
}
```
![簡単なArcGISの例](/img/basic-arcgis.jpg)

ArcGIS 固有のレイヤー機能やジオプロセシング機能と組み合わせることで、より高度な地図アプリケーションを構築できます。

