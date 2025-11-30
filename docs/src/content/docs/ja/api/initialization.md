---
title: "初期化"
---

このセクションでは、異なる地図SDKに対して MapConductor を適切に初期化および設定する方法について説明します。

## 基本的な初期化

### Gradle 依存関係

`build.gradle.kts` に MapConductor を追加します:

```kotlin
dependencies {
    implementation "com.mapconductor:mapconductor-bom:${BOM_MODULE_VERSION}"
    implementation "com.mapconductor:core"

    // 地図SDKを選択
    implementation "com.mapconductor:for-googlemaps"
    implementation "com.mapconductor:for-mapbox"
    implementation "com.mapconductor:for-here"
    implementation "com.mapconductor:for-arcgis"
    implementation "com.mapconductor:for-maplibre"
}
```

### 地図SDKのセットアップ

各地図SDKには、特定のセットアップと API キーが必要です。

#### Google Maps

```kotlin
@Composable
fun GoogleMapsExample() {
    val mapViewState = rememberGoogleMapViewState()

    GoogleMapView(state = mapViewState) {
        // マップコンテンツ
    }
}
```

#### Mapbox

```kotlin
@Composable
fun MapboxExample() {
    val mapViewState = rememberMapboxMapViewState()

    MapboxMapView(state = mapViewState) {
        // マップコンテンツ
    }
}
```

#### HERE Maps

```kotlin
@Composable
fun HereExample() {
    val mapViewState = rememberHereMapViewState()

    HereMapView(state = mapViewState) {
        // マップコンテンツ
    }
}
```

#### ArcGIS

```kotlin
@Composable
fun ArcGISExample() {
    val mapViewState = rememberArcGISMapViewState()

    ArcGISMapView(state = mapViewState) {
        // マップコンテンツ
    }
}
```

## 初期化

```kotlin
@Composable
fun CustomMapConfiguration() {
    val mapViewState = remember {
        GoogleMapViewStateImpl().apply {
            // 初期カメラ位置を設定
            initCameraPosition = MapCameraPositionImpl(
                target = GeoPointImpl.fromLatLong(37.7749, -122.4194),
                zoom = 12f,
                bearing = 45f,
                tilt = 30f
            )

            // マップデザイン/スタイルを設定
            mapDesignType = GoogleMapDesignType.SATELLITE
        }
    }

    // GoogleMapView、MapboxMapView など、選択した地図SDKに置き換えてください
    GoogleMapView(
        state = mapViewState,
        onMapLoaded = {
            println("Map loaded and ready")
        }
    ) {
        // マップコンテンツ
    }
}
```
