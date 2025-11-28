---
title: プロバイダ互換性
---

このページでは、各地図プロバイダがサポートしている MapConductor コンポーネントを示します。MapConductor は統一 API の提供を目指していますが、一部の機能は基盤となる SDK の制限により、すべての地図プロバイダで利用できるわけではありません。

## コンポーネントサポートマトリックス

| コンポーネント      | Google Maps | Mapbox | HERE Maps | ArcGIS | MapLibre |
|--------------------|-------------|--------|-----------|--------|----------|
| **Map View**       | ✅         | ✅     | ✅        | ✅     | ✅       |
| **Marker**         | ✅         | ✅     | ✅        | ✅     | ✅       |
| **Circle**         | ✅         | ✅     | ✅        | ✅     | ✅       |
| **Polyline**       | ✅         | ✅     | ✅        | ✅     | ✅       |
| **Polygon**        | ✅         | ✅     | ✅        | ✅     | ✅       |
| **GroundImage**    | ✅         | ❌     | ❌        | ❌     | ❌       |

### 凡例

- ✅ **完全サポート**: 機能が利用可能でテスト済み
- ❌ **サポートなし**: SDK の制限により機能が利用できません
- ⚠️ **限定サポート**: 機能は利用可能ですが制限があります（現在該当なし）

## コアコンポーネント（全プロバイダ）

### マップビューコンポーネント

すべての地図プロバイダは、以下を含む基本的なマップビュー機能をサポートしています:

- カメラの位置決めと移動
- ユーザーインタラクション（パン、ズーム、回転、傾き - サポートされている場合）
- マップのスタイルと外観
- イベント処理（タップ、ロングプレス、カメラ移動イベント）

v1.1.1 では、以下のカメラコールバックが利用可能です（基盤となる SDK がそれらを公開している場合）:

- `onCameraMoveStart`
- `onCameraMove`
- `onCameraMoveEnd`

### Marker

すべてのプロバイダは、以下を含むマーカー機能をサポートしています:

- カスタムアイコンと色
- クリックイベントとインタラクション
- ドラッグアンドドロップ
- 情報ウィンドウ / 情報バブル（プロバイダがサポートしている場合）

### Circle、Polyline、Polygon

すべてのプロバイダは以下をサポートしています:

- 中心と半径を持つ円オーバーレイ
- 複数のポイントを接続するポリライン
- 塗りつぶしと枠線のスタイリングを持つポリゴン

SDK の実装により、正確な視覚的外観はプロバイダごとに若干異なる場合があります。

## プロバイダ固有の制限

### GroundImage（Google Maps のみ）

GroundImage オーバーレイは Google Maps でのみサポートされています。

- **Google Maps**: ネイティブの `GroundOverlay` API が、地理的境界を持つ画像オーバーレイを直接サポートしています。
- **Mapbox / HERE / ArcGIS / MapLibre**: モバイル SDK には、シンプルで組み込みの同等機能がありません。カスタムレイヤーで同様のビジュアルを実装できますが、MapConductor はまだそれらの統一抽象化を提供していません。

## サポートされていない機能の処理

すべてのプロバイダでサポートされていないコンポーネントを使用する場合は、実行時検出と適切なフォールバックを優先してください。

### 実行時検出の例

```kotlin
@Composable
fun CompatibilityAwareMap() {
    val mapViewState = rememberGoogleMapViewState()
    val supportsGroundImage = mapViewState is GoogleMapViewStateImpl

    // GoogleMapsView、MapboxMapView など、選択した地図プロバイダに置き換えてください
    MapView(state = mapViewState) {
        // 常にサポートされているコンポーネント
        Marker(
            position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            icon = DefaultIcon()
        )

        Circle(
            center = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            radiusMeters = 1000.0,
            fillColor = Color.Blue.copy(alpha = 0.3f)
        )

        // 条件付きでサポートされているコンポーネント
        if (supportsGroundImage) {
            GroundImage(
                bounds = imageBounds,
                image = overlayImage,
                opacity = 0.7f
            )
        }
    }
}
```

## ベストプラクティス

1. **機能検出** – プロバイダ固有の機能を使用する前に、プロバイダの機能を確認してください。
2. **適切な劣化** – 機能がサポートされていない場合は、フォールバックを提供する（またはコントロールを非表示にする）。
3. **ドキュメント** – アプリがサポートするプロバイダと機能のギャップをドキュメント化してください。
4. **テスト** – すべてのターゲットプロバイダでアプリをテストして、視覚的または動作的な違いをキャッチしてください。

MapConductor または基盤となるプロバイダ SDK に新しい機能が追加されると、このページは最新の互換性ステータスを反映するように更新される必要があります。
