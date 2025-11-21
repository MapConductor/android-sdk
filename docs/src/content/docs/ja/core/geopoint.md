---
title: "GeoPoint（地理座標）"
---

`GeoPoint` は、緯度・経度・高度を表す地理座標クラスです。MapConductor 内のほぼすべての位置情報は `GeoPoint` を通じて表現されます。

## 基本構造

`GeoPoint` は次の情報を持ちます。

- 緯度（latitude）
- 経度（longitude）
- 高度（altitude、任意）

具体的な実装として、`GeoPointImpl` を使用します。

```kotlin
data class GeoPointImpl(
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double? = null
) : GeoPoint
```

## 生成方法

### 緯度・経度から生成

```kotlin
val point = GeoPointImpl.fromLatLong(
    latitude = 37.7749,
    longitude = -122.4194
)
```

### 高度を含めて生成

```kotlin
val pointWithAltitude = GeoPointImpl(
    latitude = 37.7749,
    longitude = -122.4194,
    altitudeMeters = 30.0
)
```

## 主な用途

`GeoPoint` は、MapConductor のさまざまなコンポーネントで利用されます。

- `Marker` や `Circle`、`Polyline` などの位置指定
- `MapCameraPosition` のターゲット位置
- イベントハンドラ（クリック位置など）から返される座標情報

## 座標系

`GeoPoint` は WGS84（緯度・経度）を前提としています。内部的に投影変換（Web Mercator など）が必要な場合でも、API レベルでは `GeoPoint` を使って扱います。

投影変換や距離計算については、[Spherical Utilities](/ja/core/spherical-utilities) も併せて参照してください。

