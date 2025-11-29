---
title: "GeoPoint（地理座標）"
---

`GeoPoint` は、緯度・経度・高度（オプション）を持つ地理座標を表します。マーカー、サークル、その他の地図オーバーレイの配置における基本的な構成要素です。

## インターフェースと実装

### GeoPoint インターフェース

```kotlin
interface GeoPoint {
    val latitude: Double
    val longitude: Double
    val altitude: Double?
}
```

### GeoPointImpl

メイン実装は、不変の地理座標を提供します。

```kotlin
data class GeoPointImpl(
    override val latitude: Double,
    override val longitude: Double,
    override val altitude: Double = 0.0
) : GeoPoint
```

## 生成メソッド

### ファクトリーメソッド

```kotlin
// 標準的な生成 - 緯度が先（一般的なパターン）
GeoPointImpl.fromLatLong(37.7749, -122.4194)

// 代替 - 経度が先
GeoPointImpl.fromLongLat(-122.4194, 37.7749)

// 既存の GeoPoint から生成
GeoPointImpl.from(existingGeoPoint)

// 直接コンストラクタ
GeoPointImpl(latitude = 37.7749, longitude = -122.4194, altitude = 100.0)
```

### 使用例

```kotlin
// 基本的なマーカー配置
val sanFrancisco = GeoPointImpl.fromLatLong(37.7749, -122.4194)

// MapView を GoogleMapView、MapboxMapView などの選択した地図SDKに置き換えてください
MapView(state = mapViewState) {
    Marker(
        position = sanFrancisco,
        icon = DefaultIcon(label = "SF")
    )
}
```

```kotlin
// 3D 配置のための高度を含める
val mountEverest = GeoPointImpl(
    latitude = 27.9881,
    longitude = 86.9250,
    altitude = 8848.0 // メートル
)
```

## 座標系

### 緯度（Latitude）
- **範囲**: -90.0 から 90.0 度
- **-90.0**: 南極
- **0.0**: 赤道
- **90.0**: 北極

### 経度（Longitude）
- **範囲**: -180.0 から 180.0 度
- **-180.0**: 国際日付変更線（西側）
- **0.0**: 本初子午線（グリニッジ）
- **180.0**: 国際日付変更線（東側）

### 高度（Altitude）
- **オプション**: 2D 配置の場合、`null` または `0.0` にできます
- **単位**: 海面からのメートル
- **用途**: 3D 配置、標高データ

## 検証と正規化

### 検証

```kotlin
fun GeoPoint.isValid(): Boolean =
    latitude in -90.0..90.0 && longitude in -180.0..180.0

// 使用例
val point = GeoPointImpl.fromLatLong(37.7749, -122.4194)
if (point.isValid()) {
    // ポイントを使用
}
```

### 正規化

```kotlin
fun GeoPoint.normalize(): GeoPointImpl

// 使用例
val invalidPoint = GeoPointImpl.fromLatLong(100.0, 200.0) // 無効な座標
val validPoint = invalidPoint.normalize() // 有効な範囲にクランプされる
```
