---
title: HERE Maps セットアップ
---

# HERE Maps セットアップ

このページでは、HERE Maps SDK と MapConductor を組み合わせて利用するためのセットアップ手順について説明します。

## 前提条件

- HERE 開発者アカウントを作成済み
- 必要な API キーまたは認証情報を取得済み

## Gradle への依存関係追加

```kotlin
dependencies {
    implementation(platform("com.mapconductor:mapconductor-bom:1.1.1"))
    implementation("com.mapconductor:core")
    implementation("com.mapconductor:for-here")
    // HERE SDK for Android の依存関係も追加
}
```

HERE SDK 本体の依存関係は、HERE の公式ドキュメントに従って追加してください。

## MapConductor での利用例

```kotlin
@Composable
fun HereMapsExample() {
    val camera = MapCameraPositionImpl(
        position = GeoPointImpl.fromLatLong(52.5309, 13.3847),
        zoom = 13.0,
    )

    val mapViewState = rememberHereMapViewState(
        cameraPosition = camera,
    )

    HereMapView(
        state = mapViewState,
        onMapClick = { point ->
            println("Clicked: ${point.latitude}, ${point.longitude}")
        }
    ) {
        Marker(
            position = GeoPointImpl.fromLatLong(52.5309, 13.3847),
            icon = DefaultIcon(label = "Berlin"),
        )
    }
}
```
![簡単なHereの例](/img/basic-here.jpg)

HERE 固有の機能（交通情報レイヤーやオフラインマップなど）と併用する場合は、HERE SDK のドキュメントも参照してください。

