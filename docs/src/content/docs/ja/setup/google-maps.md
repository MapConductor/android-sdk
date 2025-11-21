---
title: Google Maps セットアップ
---

# Google Maps セットアップ

このページでは、Google Maps Android SDK と MapConductor を組み合わせて利用するためのセットアップ手順について説明します。

## 前提条件

- Google Cloud プロジェクトが作成済み
- Maps SDK for Android が有効化されている
- Android 用の API キーを発行済み

## Gradle への依存関係追加

```kotlin
dependencies {
    implementation(platform("com.mapconductor:mapconductor-bom:1.1.0"))
    implementation("com.mapconductor:core")
    implementation("com.mapconductor:for-googlemaps")
    // Google Maps Android SDK 本体の依存関係も追加
}
```

Google Maps SDK 本体の依存関係は、Google の公式ドキュメントに従って追加してください。

## AndroidManifest の設定

`AndroidManifest.xml` に API キーと必要な権限を追加します。

```xml
<manifest>
    <application>
        <meta-data
            android:name="com.google.android.geo.API_KEY"
            android:value="@string/google_maps_key" />
    </application>
</manifest>
```

位置情報を利用する場合は、`ACCESS_FINE_LOCATION` などのパーミッションも追加してください。

## MapConductor での利用例

```kotlin
@Composable
fun GoogleMapsExample() {
    val camera = MapCameraPositionImpl(
        position = GeoPointImpl.fromLatLong(35.6812, 139.7671),
        zoom = 14.0,
    )

    val mapViewState = rememberGoogleMapViewState(
        cameraPosition = camera,
    )

    GoogleMapsView(
        state = mapViewState,
        onMapClick = { point ->
            println("Clicked: ${point.latitude}, ${point.longitude}")
        }
    ) {
        Marker(
            position = GeoPointImpl.fromLatLong(35.6812, 139.7671),
            icon = DefaultIcon(label = "Tokyo"),
        )
    }
}
```

より詳しい API の使い方は、[MapView コンポーネント](/ja/components/mapviewcomponent) と [Marker コンポーネント](/ja/components/marker) を参照してください。

