---
title: "Spherical Utilities（球面ユーティリティ）"
---

球面ユーティリティは、球面地球モデルを使用した正確な地理計算を提供します。これらの関数は、距離測定、パス計算、地理的計算に不可欠です。

## 概要

`Spherical` オブジェクトには、地球の表面上の距離、方位角、位置を計算するためのユーティリティ関数が含まれています。WGS84 楕円体に基づく地球の半径を使用した球面地球モデルを使用します。

```kotlin
import com.mapconductor.core.spherical.Spherical
```

## コア定数

- **地球の半径**: 6,378,137 メートル（WGS84 楕円体の長半径）
- **座標系**: WGS84 地理座標（緯度/経度）

## 距離計算

### 2点間の距離

```kotlin
fun computeDistanceBetween(from: GeoPoint, to: GeoPoint): Double
```

ハバーサイン公式を使用して 2 点間の最短距離を計算します:

```kotlin
val sanFrancisco = GeoPointImpl.fromLatLong(37.7749, -122.4194)
val newYork = GeoPointImpl.fromLatLong(40.7128, -74.0060)

val distanceMeters = Spherical.computeDistanceBetween(sanFrancisco, newYork)
val distanceKm = distanceMeters / 1000.0
val distanceMiles = distanceMeters / 1609.344

println("距離: ${distanceKm.toInt()} km (${distanceMiles.toInt()} マイル)")
```

### パスの長さ

```kotlin
fun computeLength(path: List<GeoPoint>): Double
```

複数の点で構成されるパスの総長を計算します:

```kotlin
val routePoints = listOf(
    GeoPointImpl.fromLatLong(37.7749, -122.4194), // サンフランシスコ
    GeoPointImpl.fromLatLong(37.7849, -122.4094), // ノースビーチ
    GeoPointImpl.fromLatLong(37.7949, -122.3994), // ロシアンヒル
    GeoPointImpl.fromLatLong(37.8049, -122.3894)  // フィッシャーマンズワーフ
)

val totalDistance = Spherical.computeLength(routePoints)
println("ルート長: ${(totalDistance / 1000).toInt()} km")
```

## 方向と方位角

### 方位角計算

```kotlin
fun computeHeading(from: GeoPoint, to: GeoPoint): Double
```

ある点から別の点への初期方位角（方向）を計算します:

```kotlin
val start = GeoPointImpl.fromLatLong(37.7749, -122.4194)
val destination = GeoPointImpl.fromLatLong(40.7128, -74.0060)

val bearing = Spherical.computeHeading(start, destination)
println("サンフランシスコからニューヨークに到達するには ${bearing.toInt()}° に進む")

// コンパス方向に変換
val compassDirection = when {
    bearing >= -22.5 && bearing < 22.5 -> "北"
    bearing >= 22.5 && bearing < 67.5 -> "北東"
    bearing >= 67.5 && bearing < 112.5 -> "東"
    bearing >= 112.5 && bearing < 157.5 -> "南東"
    bearing >= 157.5 || bearing < -157.5 -> "南"
    bearing >= -157.5 && bearing < -112.5 -> "南西"
    bearing >= -112.5 && bearing < -67.5 -> "西"
    else -> "北西"
}
```

## 位置計算

### 点からのオフセット

```kotlin
fun computeOffset(origin: GeoPoint, distance: Double, heading: Double): GeoPointImpl
```

原点からの距離と方向を指定して新しい位置を計算します:

```kotlin
val origin = GeoPointImpl.fromLatLong(37.7749, -122.4194)

// 原点周辺の点
val north1km = Spherical.computeOffset(origin, 1000.0, 0.0)   // 原点の北 1km
val east1km = Spherical.computeOffset(origin, 1000.0, 90.0)   // 原点の東 1km
val south1km = Spherical.computeOffset(origin, 1000.0, 180.0) // 原点の南 1km
val west1km = Spherical.computeOffset(origin, 1000.0, 270.0)  // 原点の西 1km

// 原点周辺の正方形を作成
val squarePoints = listOf(north1km, east1km, south1km, west1km, north1km)
```

### 逆計算

```kotlin
fun computeOffsetOrigin(to: GeoPoint, distance: Double, heading: Double): GeoPointImpl?
```

目的地、距離、元の方位角を指定して原点を計算します:

```kotlin
val destination = GeoPointImpl.fromLatLong(37.7849, -122.4094)
val distance = 1000.0  // 1km
val originalHeading = 45.0  // 北東

val origin = Spherical.computeOffsetOrigin(destination, distance, originalHeading)
origin?.let { point ->
    println("開始地点: ${point.latitude}, ${point.longitude}")
}
```

## 補間

### 大円補間

```kotlin
fun interpolate(from: GeoPoint, to: GeoPoint, fraction: Double): GeoPointImpl
```

2 点間の大円パスに沿って補間します:

```kotlin
val start = GeoPointImpl.fromLatLong(37.7749, -122.4194)
val end = GeoPointImpl.fromLatLong(40.7128, -74.0060)

// 大円ルート沿いのウェイポイントを作成
val waypoints = (0..10).map { i ->
    val fraction = i / 10.0
    Spherical.interpolate(start, end, fraction)
}

// ルートアニメーションで使用
@Composable
fun AnimatedRoute() {
    var currentWaypoint by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (currentWaypoint < waypoints.size - 1) {
            delay(1000)
            currentWaypoint++
        }
    }

    // MapView を GoogleMapView、MapboxMapView などの選択した地図SDKに置き換えてください
    MapView(state = mapViewState) {
        // ルートを表示
        Polyline(
            points = waypoints,
            strokeColor = Color.Blue,
            strokeWidth = 3.dp
        )

        // 移動マーカー
        if (currentWaypoint < waypoints.size) {
            Marker(
                position = waypoints[currentWaypoint],
                icon = DefaultIcon(fillColor = Color.Red)
            )
        }
    }
}
```

### 線形補間

```kotlin
fun linearInterpolate(from: GeoPoint, to: GeoPoint, fraction: Double): GeoPointImpl
```

地球の曲率を考慮しない高速線形補間:

```kotlin
val start = GeoPointImpl.fromLatLong(37.7749, -122.4194)
val end = GeoPointImpl.fromLatLong(37.7849, -122.4094)  // 短距離

// 短距離の場合、線形補間はより高速で十分に正確です
val midpoint = Spherical.linearInterpolate(start, end, 0.5)
```

## 面積計算

### ポリゴン面積

```kotlin
fun computeArea(path: List<GeoPoint>): Double
fun computeSignedArea(path: List<GeoPoint>): Double
```

閉じたポリゴンの面積を計算します:

```kotlin
val polygonPoints = listOf(
    GeoPointImpl.fromLatLong(37.7749, -122.4194),
    GeoPointImpl.fromLatLong(37.7849, -122.4094),
    GeoPointImpl.fromLatLong(37.7849, -122.4294),
    GeoPointImpl.fromLatLong(37.7749, -122.4294),
    GeoPointImpl.fromLatLong(37.7749, -122.4194)  // ポリゴンを閉じる
)

val area = Spherical.computeArea(polygonPoints)
val areaKm2 = area / 1_000_000  // 平方キロメートルに変換

println("ポリゴン面積: ${areaKm2.toInt()} km²")

// 符号付き面積は方向を教えてくれます
val signedArea = Spherical.computeSignedArea(polygonPoints)
val orientation = if (signedArea > 0) "反時計回り" else "時計回り"
println("ポリゴンの方向: $orientation")
```

## 実用例

### 近接検出

```kotlin
@Composable
fun ProximityAlert() {
    val targetLocation = GeoPointImpl.fromLatLong(37.7749, -122.4194)
    val alertRadius = 500.0  // 500 メートル

    var userLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var isNearTarget by remember { mutableStateOf(false) }

    // ユーザー位置が変わったときに近接状態を更新
    LaunchedEffect(userLocation) {
        userLocation?.let { location ->
            val distance = Spherical.computeDistanceBetween(location, targetLocation)
            isNearTarget = distance <= alertRadius
        }
    }

    // MapView を GoogleMapView、MapboxMapView などの選択した地図SDKに置き換えてください
    MapView(state = mapViewState) {
        // ターゲット位置
        Marker(
            position = targetLocation,
            icon = DefaultIcon(
                fillColor = if (isNearTarget) Color.Green else Color.Red,
                label = "ターゲット"
            )
        )

        // アラート半径
        Circle(
            center = targetLocation,
            radiusMeters = alertRadius,
            strokeColor = Color.Blue,
            fillColor = Color.Blue.copy(alpha = 0.2f)
        )

        // ユーザー位置（利用可能な場合）
        userLocation?.let { location ->
            Marker(
                position = location,
                icon = DefaultIcon(fillColor = Color.Blue, label = "あなた")
            )
        }
    }

    if (isNearTarget) {
        Text(
            text = "ターゲットの近くにいます！",
            color = Color.Green,
            fontWeight = FontWeight.Bold
        )
    }
}
```

### ルート進捗追跡

```kotlin
@Composable
fun RouteProgress() {
    val route = listOf(
        GeoPointImpl.fromLatLong(37.7749, -122.4194),
        GeoPointImpl.fromLatLong(37.7849, -122.4094),
        GeoPointImpl.fromLatLong(37.7949, -122.3994)
    )

    var currentPosition by remember { mutableStateOf(route.first()) }
    val totalDistance = Spherical.computeLength(route)

    // ルート沿いの進捗を計算
    fun calculateProgress(position: GeoPoint): Double {
        var distanceToPosition = 0.0
        var minDistanceToRoute = Double.MAX_VALUE
        var bestSegmentProgress = 0.0

        // ルート上の最も近い点を見つける
        for (i in 0 until route.size - 1) {
            val segmentStart = route[i]
            val segmentEnd = route[i + 1]
            val segmentLength = Spherical.computeDistanceBetween(segmentStart, segmentEnd)

            // このセグメント上の最も近い点を見つける
            var closestFraction = 0.0
            var minSegmentDistance = Double.MAX_VALUE

            for (fraction in 0..100) {
                val testFraction = fraction / 100.0
                val testPoint = Spherical.interpolate(segmentStart, segmentEnd, testFraction)
                val distance = Spherical.computeDistanceBetween(position, testPoint)

                if (distance < minSegmentDistance) {
                    minSegmentDistance = distance
                    closestFraction = testFraction
                }
            }

            if (minSegmentDistance < minDistanceToRoute) {
                minDistanceToRoute = minSegmentDistance
                bestSegmentProgress = distanceToPosition + (segmentLength * closestFraction)
            }

            distanceToPosition += segmentLength
        }

        return bestSegmentProgress / totalDistance
    }

    val progress = calculateProgress(currentPosition)

    Column {
        Text("ルート進捗: ${(progress * 100).toInt()}%")
        LinearProgressIndicator(progress = progress.toFloat())

        // MapView を GoogleMapView、MapboxMapView などの選択した地図SDKに置き換えてください
        MapView(state = mapViewState) {
            // ルートを表示
            Polyline(
                points = route,
                strokeColor = Color.Blue,
                strokeWidth = 4.dp
            )

            // 現在位置
            Marker(
                position = currentPosition,
                icon = DefaultIcon(fillColor = Color.Red, label = "現在")
            )
        }
    }
}
```

### ジオフェンシング

```kotlin
@Composable
fun GeofenceExample() {
    val geofenceCenter = GeoPointImpl.fromLatLong(37.7749, -122.4194)
    val geofenceRadius = 1000.0  // 1km

    var userLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var insideGeofence by remember { mutableStateOf(false) }

    // ジオフェンスステータスを確認
    LaunchedEffect(userLocation) {
        userLocation?.let { location ->
            val distance = Spherical.computeDistanceBetween(location, geofenceCenter)
            insideGeofence = distance <= geofenceRadius
        }
    }

    // MapView を GoogleMapView、MapboxMapView などの選択した地図SDKに置き換えてください
    MapView(state = mapViewState) {
        // ジオフェンス境界
        Circle(
            center = geofenceCenter,
            radiusMeters = geofenceRadius,
            strokeColor = if (insideGeofence) Color.Green else Color.Red,
            strokeWidth = 3.dp,
            fillColor = Color.Blue.copy(alpha = 0.1f)
        )

        userLocation?.let { location ->
            Marker(
                position = location,
                icon = DefaultIcon(
                    fillColor = if (insideGeofence) Color.Green else Color.Red,
                    label = if (insideGeofence) "内部" else "外部"
                )
            )
        }
    }
}
```

## パフォーマンスの考慮事項

1. **適切なメソッドを選択**: 短距離と頻繁な計算には `linearInterpolate` を使用
2. **結果をキャッシュ**: 可能な場合は計算された距離と方位角を保存
3. **バッチ計算**: 複数の点をまとめて処理し、関数呼び出しのオーバーヘッドを削減
4. **精度 vs パフォーマンス**: ユースケースに完全な球面精度が必要かどうかを検討

## ベストプラクティス

1. **座標検証**: 計算前に入力座標が有効であることを確認
2. **エラー処理**: `computeOffsetOrigin` からの null 結果を確認
3. **単位の一貫性**: すべての距離はメートル、すべての角度は度数
4. **地球モデル**: これは球面地球モデルを使用し、より正確な楕円体モデルではありません
5. **精度**: ほとんどのマッピングアプリケーションには十分な精度の結果ですが、高精度測量には小さな誤差がある可能性があります
