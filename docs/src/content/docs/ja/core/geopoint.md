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

// MapView を GoogleMapsView、MapboxMapView などの選択した地図SDKに置き換えてください
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

## 距離と方位角の計算

### 2点間の距離

```kotlin
fun GeoPoint.distanceTo(other: GeoPoint): Double

// 使用例
val sf = GeoPointImpl.fromLatLong(37.7749, -122.4194)
val nyc = GeoPointImpl.fromLatLong(40.7128, -74.0060)

val distanceMeters = sf.distanceTo(nyc)
val distanceKm = distanceMeters / 1000.0

println("SF から NYC までの距離: ${distanceKm}km")
```

### 方位角（方向）

```kotlin
fun GeoPoint.headingTo(other: GeoPoint): Double

// 使用例
val bearing = sf.headingTo(nyc) // 度数で方位角を返す（0-360）
println("SF から NYC への方位角: ${bearing}°")
```

### オフセットポイント

```kotlin
fun GeoPoint.offset(distance: Double, heading: Double): GeoPointImpl

// 使用例
val sf = GeoPointImpl.fromLatLong(37.7749, -122.4194)
val pointNorth = sf.offset(1000.0, 0.0) // SF の 1km 北
val pointEast = sf.offset(1000.0, 90.0)  // SF の 1km 東
```

## 補間

### 球面補間

地球の曲率を考慮します（地理的計算に推奨）:

```kotlin
fun GeoPoint.interpolateTo(other: GeoPoint, fraction: Double): GeoPointImpl

// 使用例
val sf = GeoPointImpl.fromLatLong(37.7749, -122.4194)
val nyc = GeoPointImpl.fromLatLong(40.7128, -74.0060)

val halfway = sf.interpolateTo(nyc, 0.5) // 地球の曲率を考慮した中間点
val quarterWay = sf.interpolateTo(nyc, 0.25)
```

### 線形補間

地球の曲率を無視します（高速だが長距離では精度が低い）:

```kotlin
fun GeoPoint.linearInterpolateTo(other: GeoPoint, fraction: Double): GeoPointImpl

// 使用例
val linearMidpoint = sf.linearInterpolateTo(nyc, 0.5)
```

## 実用例

### ルートウェイポイント

```kotlin
@Composable
fun RouteWithWaypoints() {
    val start = GeoPointImpl.fromLatLong(37.7749, -122.4194)
    val end = GeoPointImpl.fromLatLong(37.7849, -122.4094)

    // ルート上にウェイポイントを作成
    val waypoints = (0..10).map { i ->
        start.interpolateTo(end, i / 10.0)
    }

    // MapView を GoogleMapsView、MapboxMapView などの選択した地図SDKに置き換えてください
    MapView(state = mapViewState) {
        // ルートを描画
        Polyline(
            points = waypoints,
            strokeColor = Color.Blue,
            strokeWidth = 3.dp
        )

        // ウェイポイントマーカー
        waypoints.forEachIndexed { index, point ->
            Marker(
                position = point,
                icon = DefaultIcon(
                    label = "$index",
                    scale = 0.7f
                )
            )
        }
    }
}
```

### 近接検出

```kotlin
@Composable
fun ProximityExample() {
    val userLocation = GeoPointImpl.fromLatLong(37.7749, -122.4194)
    val poi = GeoPointImpl.fromLatLong(37.7759, -122.4184)

    val distance = userLocation.distanceTo(poi)
    val isNearby = distance < 100.0 // 100メートル以内

    // MapView を GoogleMapsView、MapboxMapView などの選択した地図SDKに置き換えてください
    MapView(state = mapViewState) {
        Marker(
            position = userLocation,
            icon = DefaultIcon(
                fillColor = Color.Blue,
                label = "あなた"
            )
        )

        Marker(
            position = poi,
            icon = DefaultIcon(
                fillColor = if (isNearby) Color.Green else Color.Red,
                label = "POI"
            )
        )

        // 近接サークル
        Circle(
            center = poi,
            radiusMeters = 100.0,
            strokeColor = Color.Green.copy(alpha = 0.5f),
            fillColor = Color.Green.copy(alpha = 0.1f)
        )
    }
}
```

### 動的位置更新

```kotlin
@Composable
fun MovingMarkerExample() {
    val path = listOf(
        GeoPointImpl.fromLatLong(37.7749, -122.4194),
        GeoPointImpl.fromLatLong(37.7759, -122.4184),
        GeoPointImpl.fromLatLong(37.7769, -122.4174)
    )

    var currentPosition by remember { mutableStateOf(path.first()) }
    var pathIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(2000)
            pathIndex = (pathIndex + 1) % path.size
            currentPosition = path[pathIndex]
        }
    }

    // MapView を GoogleMapsView、MapboxMapView などの選択した地図SDKに置き換えてください
    MapView(state = mapViewState) {
        // 移動マーカー
        Marker(
            position = currentPosition,
            icon = DefaultIcon(
                fillColor = Color.Red,
                label = "📍"
            )
        )

        // パスプレビュー
        Polyline(
            points = path,
            strokeColor = Color.Gray,
            strokeWidth = 2.dp
        )
    }
}
```

## 等価性とハッシング

`GeoPointImpl` は浮動小数点精度の問題を処理するために許容値ベースの等価性を使用します:

```kotlin
val point1 = GeoPointImpl.fromLatLong(37.7749, -122.4194)
val point2 = GeoPointImpl.fromLatLong(37.7749000001, -122.4194000001)

println(point1 == point2) // true - 許容範囲内
```

許容値は `1e-7` 度に設定されており、赤道で約 1 センチメートルに相当します。

## URL フォーマット

```kotlin
fun toUrlValue(precision: Int = 6): String

// 使用例
val point = GeoPointImpl.fromLatLong(37.7749, -122.4194)
val urlString = point.toUrlValue() // "37.774900,-122.419400"
val preciseString = point.toUrlValue(precision = 8) // より多くの小数点以下の桁数
```

## ベストプラクティス

1. **ファクトリーメソッドを使用**: パラメータの順序を明確にするため `fromLatLong()` を優先
2. **入力を検証**: ユーザー入力を受け入れる際は座標が有効範囲内にあることを確認
3. **地球の曲率を考慮**: 地理的精度のため球面補間を使用
4. **パフォーマンス**: 精度があまり重要でない頻繁な計算には線形補間を使用
5. **高度**: 3D アプリケーションには高度を含め、2D マッピングには 0.0 を使用
6. **不変性**: GeoPointImpl は不変です - 異なる座標には新しいインスタンスを作成
