---
title: "Marker Icons（マーカーアイコン）"
---

MapConductor は、地図上のマーカーの外観をカスタマイズするためのいくつかのマーカーアイコンタイプを提供します。すべてのマーカーアイコンは `MarkerIcon` インターフェースを実装しており、どの地図SDKでも使用できます。

## MarkerIcon インターフェース

すべてのマーカーアイコンの基本インターフェース:

```kotlin
interface MarkerIcon {
    // すべてのマーカーアイコン実装に共通のプロパティ
}
```

## アイコンタイプ

### DefaultIcon (ColorDefaultIcon)

カスタマイズ可能な外観とテキストラベルを持つ標準的な色付きマーカーアイコンです。

> **注意**: `DefaultIcon` は `ColorDefaultIcon` のエイリアスです。

```kotlin
DefaultIcon(
    scale: Float = 1.0f,
    label: String? = null,
    fillColor: Color = Color.Red,
    strokeColor: Color = Color.Black,
    strokeWidth: Dp = 1.dp,
    labelTextColor: Color = Color.White,
    labelStrokeColor: Color? = null,
    debug: Boolean = false
)
```

#### パラメータ

- **`scale`**: アイコンのサイズ倍率（1.0 = 通常サイズ、0.5 = 半分サイズ、2.0 = 倍サイズ）
- **`label`**: マーカーに表示されるテキスト（オプション）
- **`fillColor`**: マーカーの背景色
- **`strokeColor`**: マーカーのボーダー色
- **`strokeWidth`**: ボーダーの幅
- **`labelTextColor`**: ラベルテキストの色
- **`labelStrokeColor`**: ラベルテキストのオプションのアウトライン色
- **`debug`**: 有効にするとデバッグ情報を表示

#### 使用例

```kotlin
// MapView を GoogleMapsView、MapboxMapView などの選択した地図SDKに置き換えてください
MapView(state = mapViewState) {
    // 基本的な赤いマーカー
    Marker(
        position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        icon = DefaultIcon()
    )

    // ラベル付きのカスタムカラーマーカー
    Marker(
        position = GeoPointImpl.fromLatLong(37.7849, -122.4094),
        icon = DefaultIcon(
            scale = 1.2f,
            label = "SF",
            fillColor = Color.Blue,
            strokeColor = Color.White,
            strokeWidth = 2.dp
        )
    )

    // カスタムスタイリングの小さなマーカー
    Marker(
        position = GeoPointImpl.fromLatLong(37.7649, -122.4294),
        icon = DefaultIcon(
            scale = 0.8f,
            fillColor = Color.Green,
            labelTextColor = Color.Black,
            label = "POI"
        )
    )
}
```

### DrawableDefaultIcon

マーカーの背景としてドローアブルリソースを使用し、オプションでスタイリングオーバーレイを使用します。

```kotlin
DrawableDefaultIcon(
    backgroundDrawable: Drawable,
    scale: Float = 1.0f,
    strokeColor: Color? = null,
    strokeWidth: Dp = 1.dp
)
```

#### パラメータ

- **`backgroundDrawable`**: マーカーの背景として使用されるドローアブルリソース
- **`scale`**: アイコンのサイズ倍率
- **`strokeColor`**: オプションのボーダー色オーバーレイ
- **`strokeWidth`**: ボーダーオーバーレイの幅

#### 使用例

```kotlin
@Composable
fun DrawableIconExamples() {
    val context = LocalContext.current

    // MapView を GoogleMapsView、MapboxMapView などの選択した地図SDKに置き換えてください
    MapView(state = mapViewState) {
        // カスタムドローアブルマーカー
        AppCompatResources.getDrawable(context, R.drawable.custom_marker)?.let { drawable ->
            Marker(
                position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
                icon = DrawableDefaultIcon(
                    backgroundDrawable = drawable,
                    scale = 1.0f
                )
            )
        }

        // カスタムボーダー付きドローアブル
        AppCompatResources.getDrawable(context, R.drawable.pin_icon)?.let { drawable ->
            Marker(
                position = GeoPointImpl.fromLatLong(37.7849, -122.4094),
                icon = DrawableDefaultIcon(
                    backgroundDrawable = drawable,
                    scale = 1.5f,
                    strokeColor = Color.Yellow,
                    strokeWidth = 3.dp
                )
            )
        }
    }
}
```

### ImageDefaultIcon

正確なアンカー位置制御を持つカスタム画像ドローアブルを使用します。

```kotlin
ImageDefaultIcon(
    drawable: Drawable,
    anchor: Offset = Offset(0.5f, 0.5f),
    debug: Boolean = false
)
```

#### パラメータ

- **`drawable`**: 表示する画像ドローアブル
- **`anchor`**: 画像に対する相対的なアンカーポイント（0.0-1.0 の範囲）
  - `Offset(0.5f, 0.5f)`: 中心アンカー（デフォルト）
  - `Offset(0.5f, 1.0f)`: 下中心アンカー
  - `Offset(0.0f, 0.0f)`: 左上アンカー
  - `Offset(1.0f, 1.0f)`: 右下アンカー
- **`debug`**: デバッグアンカー可視化を表示

#### 使用例

```kotlin
@Composable
fun ImageIconExamples() {
    val context = LocalContext.current

    // MapView を GoogleMapsView、MapboxMapView などの選択した地図SDKに置き換えてください
    MapView(state = mapViewState) {
        // 下中心アンカーの気象観測所アイコン
        AppCompatResources.getDrawable(context, R.drawable.weather_station)?.let { icon ->
            Marker(
                position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
                icon = ImageDefaultIcon(
                    drawable = icon,
                    anchor = Offset(0.5f, 1.0f), // 下中心
                    debug = false
                )
            )
        }

        // 中心アンカーの方向矢印
        AppCompatResources.getDrawable(context, R.drawable.direction_arrow)?.let { icon ->
            Marker(
                position = GeoPointImpl.fromLatLong(37.7849, -122.4094),
                icon = ImageDefaultIcon(
                    drawable = icon,
                    anchor = Offset(0.5f, 0.5f), // 中心
                    debug = true // アンカーポイントを表示
                )
            )
        }
    }
}
```

## 高度な使用法

### 動的アイコン選択

```kotlin
@Composable
fun DynamicIconExample() {
    val iconType by remember { mutableStateOf("default") }
    val context = LocalContext.current

    // MapView を GoogleMapsView、MapboxMapView などの選択した地図SDKに置き換えてください
    MapView(state = mapViewState) {
        val icon = when (iconType) {
            "default" -> DefaultIcon(
                fillColor = Color.Blue,
                label = "D"
            )
            "drawable" -> AppCompatResources.getDrawable(context, R.drawable.custom_pin)?.let {
                DrawableDefaultIcon(backgroundDrawable = it)
            }
            "image" -> AppCompatResources.getDrawable(context, R.drawable.location_icon)?.let {
                ImageDefaultIcon(
                    drawable = it,
                    anchor = Offset(0.5f, 1.0f)
                )
            }
            else -> DefaultIcon()
        }

        Marker(
            position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            icon = icon
        )
    }
}
```

### アイコンファクトリーパターン

```kotlin
object MarkerIconFactory {
    fun createLocationIcon(color: Color, label: String? = null): MarkerIcon {
        return DefaultIcon(
            fillColor = color,
            strokeColor = Color.White,
            strokeWidth = 2.dp,
            label = label,
            scale = 1.0f
        )
    }

    fun createCustomIcon(context: Context, drawableRes: Int, scale: Float = 1.0f): MarkerIcon? {
        return AppCompatResources.getDrawable(context, drawableRes)?.let {
            DrawableDefaultIcon(
                backgroundDrawable = it,
                scale = scale
            )
        }
    }

    fun createDirectionalIcon(context: Context, direction: Float): MarkerIcon? {
        return AppCompatResources.getDrawable(context, R.drawable.arrow)?.let { drawable ->
            // 必要に応じてドローアブルに回転を適用
            val rotatedDrawable = drawable.mutate()
            rotatedDrawable.setColorFilter(null)

            ImageDefaultIcon(
                drawable = rotatedDrawable,
                anchor = Offset(0.5f, 0.5f)
            )
        }
    }
}
```

## ベストプラクティス

### パフォーマンスの考慮事項

1. **アイコンインスタンスを再利用**: アイコンを一度作成し、複数のマーカーで再利用
2. **ドローアブルリソースを最適化**: 適切なサイズのドローアブルリソースを使用
3. **カスタムドローアブルを制限**: 一意のドローアブルが多すぎるとメモリ使用量に影響

```kotlin
// 良い例: アイコンインスタンスを再利用
val blueIcon = DefaultIcon(fillColor = Color.Blue)
val redIcon = DefaultIcon(fillColor = Color.Red)

markers.forEach { markerData ->
    val icon = if (markerData.isSelected) blueIcon else redIcon
    Marker(position = markerData.position, icon = icon)
}
```

### 視覚的一貫性

1. **一貫したスケーリング**: アプリケーション全体で一貫したスケール値を使用
2. **カラースキーム**: マーカータイプに対して一貫したカラーパレットを維持
3. **アンカーポイント**: アイコンタイプに適したアンカーを使用（ピンには下中心、円には中心）

### アクセシビリティ

1. **意味のあるラベル**: `DefaultIcon` を使用する際は記述的なラベルを提供
2. **カラーコントラスト**: 塗りつぶしとストローク色の間の良好なコントラストを確保
3. **代替インジケータ**: 情報を伝えるために色だけに頼らない

## 一般的なユースケース

### ステータスインジケータ

```kotlin
enum class MarkerStatus { ACTIVE, INACTIVE, WARNING, ERROR }

fun createStatusIcon(status: MarkerStatus): MarkerIcon = DefaultIcon(
    fillColor = when (status) {
        MarkerStatus.ACTIVE -> Color.Green
        MarkerStatus.INACTIVE -> Color.Gray
        MarkerStatus.WARNING -> Color.Yellow
        MarkerStatus.ERROR -> Color.Red
    },
    strokeColor = Color.White,
    strokeWidth = 2.dp,
    scale = 1.0f
)
```

### カテゴリーマーカー

```kotlin
sealed class PoiCategory(val icon: MarkerIcon) {
    object Restaurant : PoiCategory(DefaultIcon(fillColor = Color(0xFF4CAF50), label = "🍽️"))
    object Hotel : PoiCategory(DefaultIcon(fillColor = Color(0xFF2196F3), label = "🏨"))
    object GasStation : PoiCategory(DefaultIcon(fillColor = Color(0xFFFF9800), label = "⛽"))
    object Hospital : PoiCategory(DefaultIcon(fillColor = Color(0xFFF44336), label = "🏥"))
}
```

## トラブルシューティング

### 一般的な問題

1. **アイコンが表示されない**: ドローアブルリソースが存在しアクセス可能であることを確認
2. **位置が正しくない**: カスタムアイコンのアンカーポイントを確認
3. **パフォーマンス問題**: 一意のアイコンインスタンスの数を削減
4. **スケールの問題**: スケール値が正で妥当であることを確認（0.1 - 3.0）

### デバッグモード

アンカーポイントとアイコン境界を可視化するためにデバッグモードを有効にします:

```kotlin
Marker(
    position = position,
    icon = ImageDefaultIcon(
        drawable = customDrawable,
        anchor = Offset(0.5f, 1.0f),
        debug = true // アンカーポイントと境界を表示
    )
)
```
