---
title: "Icons (Experimental)"
---

`mapconductor-icons` モジュールは、プログラムによるスタイリングが可能なカスタム描画マーカーアイコンを提供します。この実験的モジュールは、実行時に色、サイズ、その他のプロパティをカスタマイズできるベクタースタイルのアイコンを提供します。

> **⚠️ 実験的モジュール**: このモジュールは実験的であり、将来のバージョンで大幅に変更される可能性があります。本番環境での使用には注意してください。

## 概要

icons モジュールは、Canvas 描画操作を使用して高品質なマーカーアイコンを作成し、以下を提供します:
- **スケーラブルベクターグラフィックス**: アイコンはあらゆるサイズで滑らかにスケールします
- **実行時カスタマイズ**: 色、サイズ、プロパティを動的に変更できます
- **最適化されたキャッシング**: パフォーマンスのための自動ビットマップキャッシング
- **一貫した外観**: すべての地図SDKで同じビジュアルスタイル

## インストール

`build.gradle` に icons モジュールを追加します:

```kotlin
dependencies {
    implementation "com.mapconductor:mapconductor-icons"

    // 必須: Bom モジュール
    implementation "com.mapconductor:mapconductor-bom:$version"
    // 必須: Core モジュール
    implementation "com.mapconductor:core"

    // 地図SDKを選択
    implementation "com.mapconductor:for-googlemaps"
}
```

## 利用可能なアイコン

### CircleIcon

カスタマイズ可能な塗りつぶしとストロークを持つシンプルな円形マーカーアイコン:

```kotlin
import com.mapconductor.icons.CircleIcon

// 基本的な円形アイコン
val basicCircle = CircleIcon()

// カスタマイズされた円形アイコン
val customCircle = CircleIcon(
    fillColor = Color.Blue,
    strokeColor = Color.White,
    strokeWidth = 2.dp,
    scale = 1.2f,
    iconSize = 32.dp
)
```

#### CircleIcon のプロパティ

- **`fillColor: Color`**: 円の内部色（デフォルト: `Color.Red`）
- **`strokeColor: Color`**: 境界線の色（デフォルト: `Color.White`）
- **`strokeWidth: Dp`**: 境界線の太さ（デフォルト: Settings から）
- **`scale: Float`**: サイズ倍率（デフォルト: `1.0f`）
- **`iconSize: Dp`**: アイコンの基本サイズ（デフォルト: Settings から）
- **`debug: Boolean`**: デバッグアウトラインを表示（デフォルト: `false`）

### FlagIcon

ポールとカスタマイズ可能なフラグを持つフラグスタイルのマーカーアイコン:

```kotlin
import com.mapconductor.icons.FlagIcon

// 基本的なフラグアイコン
val basicFlag = FlagIcon()

// カスタマイズされたフラグアイコン
val customFlag = FlagIcon(
    fillColor = Color.Green,
    strokeColor = Color.Black,
    strokeWidth = 1.5.dp,
    scale = 1.0f,
    iconSize = 40.dp
)
```

#### FlagIcon のプロパティ

- **`fillColor: Color`**: フラグとポールの色（デフォルト: `Color.Red`）
- **`strokeColor: Color`**: アウトラインの色（デフォルト: `Color.White`）
- **`strokeWidth: Dp`**: アウトラインの太さ（デフォルト: Settings から）
- **`scale: Float`**: サイズ倍率（デフォルト: `1.0f`）
- **`iconSize: Dp`**: アイコンの基本サイズ（デフォルト: Settings から）
- **`debug: Boolean`**: デバッグアウトラインを表示（デフォルト: `false`）

## 基本的な使用方法

### シンプルなアイコン使用

```kotlin
@Composable
fun BasicIconExample() {
    val circleIcon = CircleIcon(
        fillColor = Color.Blue,
        strokeColor = Color.White
    )

    val flagIcon = FlagIcon(
        fillColor = Color.Red,
        strokeColor = Color.Black
    )

    // GoogleMapsView、MapboxMapView などの選択した地図SDKに置き換えてください
    MapView(state = mapViewState) {
        Marker(
            position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            icon = circleIcon
        )

        Marker(
            position = GeoPointImpl.fromLatLong(37.7849, -122.4094),
            icon = flagIcon
        )
    }
}
```

### 動的アイコンカスタマイズ

```kotlin
@Composable
fun DynamicIconExample() {
    var iconColor by remember { mutableStateOf(Color.Red) }
    var iconSize by remember { mutableStateOf(32.dp) }

    val dynamicIcon = CircleIcon(
        fillColor = iconColor,
        strokeColor = Color.White,
        iconSize = iconSize
    )

    Column {
        // カラーピッカー
        Row {
            Button(onClick = { iconColor = Color.Red }) { Text("Red") }
            Button(onClick = { iconColor = Color.Blue }) { Text("Blue") }
            Button(onClick = { iconColor = Color.Green }) { Text("Green") }
        }

        // サイズスライダー
        Slider(
            value = iconSize.value,
            onValueChange = { iconSize = it.dp },
            valueRange = 16f..64f
        )
        Text("Size: ${iconSize.value.toInt()}dp")

        // GoogleMapsView、MapboxMapView などの選択した地図SDKに置き換えてください
        MapView(state = mapViewState) {
            Marker(
                position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
                icon = dynamicIcon
            )
        }
    }
}
```

## 高度な使用方法

### カテゴリベースのアイコン

```kotlin
@Composable
fun CategoryIconExample() {
    data class POI(
        val name: String,
        val category: String,
        val position: GeoPoint
    ) : java.io.Serializable

    val pois = listOf(
        POI("Restaurant", "food", GeoPointImpl.fromLatLong(37.7749, -122.4194)),
        POI("Hotel", "lodging", GeoPointImpl.fromLatLong(37.7849, -122.4094)),
        POI("Gas Station", "fuel", GeoPointImpl.fromLatLong(37.7649, -122.4294))
    )

    fun getIconForCategory(category: String) = when (category) {
        "food" -> CircleIcon(fillColor = Color.Red, strokeColor = Color.White)
        "lodging" -> FlagIcon(fillColor = Color.Blue, strokeColor = Color.White)
        "fuel" -> CircleIcon(fillColor = Color.Yellow, strokeColor = Color.Black)
        else -> CircleIcon(fillColor = Color.Gray, strokeColor = Color.White)
    }

    // GoogleMapsView、MapboxMapView などの選択した地図SDKに置き換えてください
    MapView(state = mapViewState) {
        pois.forEach { poi ->
            Marker(
                position = poi.position,
                icon = getIconForCategory(poi.category),
                extra = poi.name
            )
        }
    }
}
```

### アイコンテーマ

```kotlin
object IconTheme {
    data class Theme(
        val primaryColor: Color,
        val secondaryColor: Color,
        val strokeColor: Color,
        val strokeWidth: Dp
    ) : java.io.Serializable

    val light = Theme(
        primaryColor = Color(0xFF2196F3),
        secondaryColor = Color(0xFFFFFFFF),
        strokeColor = Color(0xFF000000),
        strokeWidth = 1.dp
    )

    val dark = Theme(
        primaryColor = Color(0xFF1976D2),
        secondaryColor = Color(0xFF424242),
        strokeColor = Color(0xFFFFFFFF),
        strokeWidth = 1.dp
    )
}

@Composable
fun ThemedIconExample() {
    val isDarkTheme = isSystemInDarkTheme()
    val theme = if (isDarkTheme) IconTheme.dark else IconTheme.light

    val themedCircle = CircleIcon(
        fillColor = theme.primaryColor,
        strokeColor = theme.strokeColor,
        strokeWidth = theme.strokeWidth
    )

    val themedFlag = FlagIcon(
        fillColor = theme.primaryColor,
        strokeColor = theme.strokeColor,
        strokeWidth = theme.strokeWidth
    )

    // GoogleMapsView、MapboxMapView などの選択した地図SDKに置き換えてください
    MapView(state = mapViewState) {
        Marker(
            position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            icon = themedCircle
        )

        Marker(
            position = GeoPointImpl.fromLatLong(37.7849, -122.4094),
            icon = themedFlag
        )
    }
}
```

### アイコンアニメーション

```kotlin
@Composable
fun AnimatedIconExample() {
    var scale by remember { mutableStateOf(1.0f) }
    var color by remember { mutableStateOf(Color.Red) }

    // スケールをアニメート
    LaunchedEffect(Unit) {
        while (true) {
            animate(
                initialValue = 1.0f,
                targetValue = 1.5f,
                animationSpec = tween(1000)
            ) { value, _ -> scale = value }

            animate(
                initialValue = 1.5f,
                targetValue = 1.0f,
                animationSpec = tween(1000)
            ) { value, _ -> scale = value }
        }
    }

    // 色をアニメート
    LaunchedEffect(Unit) {
        val colors = listOf(Color.Red, Color.Blue, Color.Green, Color.Yellow)
        var index = 0
        while (true) {
            delay(2000)
            index = (index + 1) % colors.size
            color = colors[index]
        }
    }

    val animatedIcon = CircleIcon(
        fillColor = color,
        strokeColor = Color.White,
        scale = scale
    )

    // GoogleMapsView、MapboxMapView などの選択した地図SDKに置き換えてください
    MapView(state = mapViewState) {
        Marker(
            position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            icon = animatedIcon
        )
    }
}
```

## アイコンのプロパティと動作

### アンカーポイント

アイコンには、地理座標に対してどのように配置されるかを決定する特定のアンカーポイントがあります:

- **CircleIcon**: 左中央にアンカー (0.0, 0.5)
- **FlagIcon**: ポールの基部近くにアンカー (0.176, 0.91)

### 情報ウィンドウアンカー

情報ウィンドウ（地図SDKがサポートしている場合）は異なるポイントにアンカーされます:

- **CircleIcon**: 円の中心 (0.5, 0.5)
- **FlagIcon**: フラグの上部 (0.5, 0.0)

### パフォーマンスに関する考慮事項

#### ビットマップキャッシング

アイコンは、プロパティハッシュに基づいてレンダリングされたビットマップを自動的にキャッシュします:

```kotlin
// これらは同じキャッシュされたビットマップを共有します
val icon1 = CircleIcon(fillColor = Color.Red, strokeColor = Color.White)
val icon2 = CircleIcon(fillColor = Color.Red, strokeColor = Color.White)

// これは新しいキャッシュされたビットマップを作成します
val icon3 = CircleIcon(fillColor = Color.Blue, strokeColor = Color.White)
```

#### メモリ管理

- キャッシュされたビットマップは自動的に管理されます
- 同一のプロパティを持つアイコンはビットマップインスタンスを共有します
- 大きなアイコンはより多くのメモリを使用します - 適切なサイズを使用してください

## アイコンのデバッグ

デバッグモードを有効にして、アイコンの境界とアンカーポイントを視覚化します:

```kotlin
@Composable
fun DebugIconExample() {
    val debugIcon = CircleIcon(
        fillColor = Color.Red,
        strokeColor = Color.White,
        debug = true  // デバッグアウトラインと十字線を表示
    )

    // GoogleMapsView、MapboxMapView などの選択した地図SDKに置き換えてください
    MapView(state = mapViewState) {
        Marker(
            position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            icon = debugIcon
        )
    }
}
```

デバッグモードは以下を表示します:
- アイコンの境界矩形（黒いアウトライン）
- 中心の十字線（黒い線）
- 実際に描画されたコンテンツ

## カスタムアイコン開発

### AbstractMarkerIcon の拡張

カスタムアイコンを作成するには、`AbstractMarkerIcon` を拡張します:

```kotlin
class CustomIcon(
    private val fillColor: Color = Color.Blue,
    override val scale: Float = 1.0f,
    override val iconSize: Dp = 32.dp,
    override val debug: Boolean = false
) : AbstractMarkerIcon() {

    override val anchor: Offset = Offset(0.5f, 0.5f)
    override val infoAnchor: Offset = Offset(0.5f, 0.0f)

    override fun toBitmapIcon(): BitmapIcon {
        val id = "custom_icon_${hashCode()}".hashCode()
        BitmapIconCache.get(id)?.let { return it }

        val canvasSize = ResourceProvider.dpToPx(iconSize.value * scale)
        val bitmap = createBitmap(canvasSize.toInt(), canvasSize.toInt())
        val canvas = Canvas(bitmap)

        // カスタム描画コードをここに記述
        val paint = Paint().apply {
            color = fillColor.toArgb()
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        canvas.drawRect(0f, 0f, canvasSize.toFloat(), canvasSize.toFloat(), paint)

        val result = BitmapIcon(
            bitmap = bitmap,
            anchor = anchor,
            size = Size(canvasSize.toFloat(), canvasSize.toFloat())
        )
        BitmapIconCache.put(id, result)
        return result
    }
}
```

## ベストプラクティス

1. **一貫したサイジング**: 類似したマーカータイプには一貫したアイコンサイズを使用する
2. **色のアクセシビリティ**: 塗りつぶしとストロークの色の間に十分なコントラストを確保する
3. **パフォーマンス**: キャッシングの恩恵を受けるために、同一のアイコンインスタンスを再利用する
4. **適切なスケール**: アイコンサイズを選択する際は、地図のズームレベルを考慮する
5. **地図SDK間でのテスト**: すべてのターゲット地図SDKでアイコンの外観を確認する

## 制限事項

1. **限定的なアイコンセット**: 現在、CircleIcon と FlagIcon のみが利用可能
2. **静的な形状**: アイコンはプログラムで描画され、ベクターファイルからではありません
3. **地図SDK間の違い**: 地図SDK間でわずかなレンダリングの違いが発生する可能性があります
4. **メモリ使用量**: 大きなアイコンや多くのユニークなアイコンバリエーションは、より多くのメモリを消費します

## 移行と互換性

このモジュールは実験的であり、API が変更される可能性があります。移行時には:

1. 特定のユースケースで十分にテストする
2. 多数のユニークなアイコンを使用する場合はメモリ使用量を監視する
3. 重要な機能にはフォールバックオプションを用意する
4. モジュールの改善に役立つように問題を報告する

icons モジュールは、地図SDK間で統一された MapConductor API を維持しながら、カスタムマーカースタイリングの基盤を提供します。
