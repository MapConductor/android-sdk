---
title: "InfoBubble"
---

InfoBubble は、地図上のマーカーに吹き出し形式でカスタムコンテンツを表示するコンポーネントです。地図インターフェースを乱雑にすることなく、マーカーに関する詳細情報を表示する方法を提供します。

## 概要

InfoBubble は、特定のマーカーの上に表示される浮動オーバーレイを作成し、カスタマイズ可能なスタイルとコンテンツを持ちます。バブルは自動的にマーカーに対して相対的に配置され、地図がパンまたはズームされたときにマーカーの位置に追従します。

## Composable 関数

```kotlin
@Composable
fun MapViewScope.InfoBubble(
    marker: MarkerState,
    bubbleColor: Color = Color.White,
    borderColor: Color = Color.Black,
    contentPadding: Dp = 8.dp,
    cornerRadius: Dp = 4.dp,
    tailSize: Dp = 8.dp,
    content: @Composable () -> Unit
)
```

## パラメータ

- **`marker`**: バブルが付加される `MarkerState`
- **`bubbleColor`**: バブルの背景色（デフォルト: White）
- **`borderColor`**: バブルの境界線の色（デフォルト: Black）
- **`contentPadding`**: コンテンツ周りの内部パディング（デフォルト: 8dp）
- **`cornerRadius`**: バブルの角丸の半径（デフォルト: 4dp）
- **`tailSize`**: マーカーを指す吹き出しの尾のサイズ（デフォルト: 8dp）
- **`content`**: バブル内に表示する Composable コンテンツ

## 基本的な使用方法

### シンプルなテキストバブル

```kotlin
@Composable
fun SimpleInfoBubbleExample() {
    val mapViewState = rememberGoogleMapViewState()
    var selectedMarker by remember { mutableStateOf<MarkerState?>(null) }

    // MapView を GoogleMapsView、MapboxMapView などのマップ地図SDKに置き換えてください
    MapView(
        state = mapViewState,
        onMapClick = { selectedMarker = null },
        onMarkerClick = { markerState -> selectedMarker = markerState }
    ) {
        val markerState = MarkerState(
            position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            icon = DefaultIcon(fillColor = Color.Blue, label = "SF"),
            extra = "San Francisco - The Golden Gate City",
            id = "sf-marker"
        )

        Marker(markerState)

        // 選択されたマーカーの情報バブルを表示
        selectedMarker?.let { marker ->
            InfoBubble(marker = marker) {
                Text(
                    text = marker.extra as? String ?: "No information",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}
```

### カスタムスタイルのバブル

```kotlin
@Composable
fun StyledInfoBubbleExample() {
    val mapViewState = rememberGoogleMapViewState()
    var selectedMarker by remember { mutableStateOf<MarkerState?>(null) }
    val isDarkTheme = isSystemInDarkTheme()

    // MapView を GoogleMapsView、MapboxMapView などのマップ地図SDKに置き換えてください
    MapView(
        state = mapViewState,
        onMapClick = { selectedMarker = null },
        onMarkerClick = { markerState -> selectedMarker = markerState }
    ) {
        val markerState = MarkerState(
            position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            icon = DefaultIcon(fillColor = Color.Green, label = "POI"),
            extra = "Point of Interest",
            id = "poi-marker"
        )

        Marker(markerState)

        selectedMarker?.let { marker ->
            InfoBubble(
                marker = marker,
                bubbleColor = if (isDarkTheme) Color.Black else Color.White,
                borderColor = if (isDarkTheme) Color.Gray else Color.Black,
                contentPadding = 12.dp,
                cornerRadius = 8.dp,
                tailSize = 10.dp
            ) {
                Text(
                    text = marker.extra as? String ?: "",
                    color = if (isDarkTheme) Color.White else Color.Black,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
```

## 高度な使用方法

### リッチコンテンツバブル

```kotlin
data class LocationInfo(
    val name: String,
    val description: String,
    val rating: Float,
    val imageUrl: String? = null
) : java.io.Serializable

@Composable
fun RichContentBubbleExample() {
    val mapViewState = rememberGoogleMapViewState()
    var selectedMarker by remember { mutableStateOf<MarkerState?>(null) }

    // MapView を GoogleMapsView、MapboxMapView などのマップ地図SDKに置き換えてください
    MapView(
        state = mapViewState,
        onMapClick = { selectedMarker = null },
        onMarkerClick = { markerState -> selectedMarker = markerState }
    ) {
        val locationInfo = LocationInfo(
            name = "Golden Gate Park",
            description = "A large urban park with gardens, museums, and recreational areas.",
            rating = 4.5f
        )

        val markerState = MarkerState(
            position = GeoPointImpl.fromLatLong(37.7694, -122.4862),
            icon = DefaultIcon(fillColor = Color.Green, label = "🌳"),
            extra = locationInfo,
            id = "park-marker"
        )

        Marker(markerState)

        selectedMarker?.let { marker ->
            val info = marker.extra as? LocationInfo
            info?.let {
                InfoBubble(
                    marker = marker,
                    bubbleColor = Color.White,
                    borderColor = Color.Gray,
                    contentPadding = 16.dp,
                    cornerRadius = 12.dp
                ) {
                    Column(
                        modifier = Modifier.width(200.dp)
                    ) {
                        Text(
                            text = info.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = info.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            repeat(5) { index ->
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (index < info.rating.toInt()) Color.Yellow else Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = " ${info.rating}/5",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
```

### アクション付きのインタラクティブバブル

```kotlin
@Composable
fun InteractiveBubbleExample() {
    val mapViewState = rememberGoogleMapViewState()
    var selectedMarker by remember { mutableStateOf<MarkerState?>(null) }
    val context = LocalContext.current

    // MapView を GoogleMapsView、MapboxMapView などのマップ地図SDKに置き換えてください
    MapView(
        state = mapViewState,
        onMapClick = { selectedMarker = null },
        onMarkerClick = { markerState -> selectedMarker = markerState }
    ) {
        val storeInfo = StoreInfo(
            name = "Coffee Shop",
            address = "123 Main St, San Francisco",
            phone = "+1 (555) 123-4567",
            type = "coffee"
        )

        val markerState = MarkerState(
            position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            icon = DefaultIcon(fillColor = Color(0xFF8B4513), label = "☕"),
            extra = storeInfo,
            id = "coffee-shop-marker"
        )

        Marker(markerState)

        selectedMarker?.let { marker ->
            val store = marker.extra as? StoreInfo
            store?.let {
                InfoBubble(
                    marker = marker,
                    bubbleColor = Color.White,
                    borderColor = Color.Black,
                    contentPadding = 12.dp,
                    cornerRadius = 8.dp
                ) {
                    Column(
                        modifier = Modifier.width(250.dp)
                    ) {
                        Text(
                            text = store.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = store.address,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = {
                                    // 通話アクションを処理
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:${store.phone}")
                                    }
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = "Call")
                                Text("Call", modifier = Modifier.padding(start = 4.dp))
                            }

                            Button(
                                onClick = {
                                    // ルート案内アクションを処理
                                    val position = marker.position
                                    val gmmIntentUri = Uri.parse("google.navigation:q=${position.latitude},${position.longitude}")
                                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                    mapIntent.setPackage("com.google.android.apps.maps")
                                    context.startActivity(mapIntent)
                                },
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green)
                            ) {
                                Icon(Icons.Default.Directions, contentDescription = "Directions")
                                Text("Go", modifier = Modifier.padding(start = 4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

data class StoreInfo(
    val name: String,
    val address: String,
    val phone: String,
    val type: String
) : java.io.Serializable
```

### 複数のバブル管理

```kotlin
@Composable
fun MultipleBubblesExample() {
    val mapViewState = rememberGoogleMapViewState()
    var selectedMarkers by remember { mutableStateOf(setOf<String>()) }

    val markerData = remember {
        listOf(
            Triple(GeoPointImpl.fromLatLong(37.7749, -122.4194), "Restaurant A", Color.Red),
            Triple(GeoPointImpl.fromLatLong(37.7849, -122.4094), "Hotel B", Color.Blue),
            Triple(GeoPointImpl.fromLatLong(37.7649, -122.4294), "Shop C", Color.Green)
        )
    }

    val markerStates = remember {
        markerData.mapIndexed { index, (position, name, color) ->
            MarkerState(
                id = "marker_$index",
                position = position,
                icon = DefaultIcon(fillColor = color, label = "${index + 1}"),
                extra = name
            )
        }
    }

    // MapView を GoogleMapsView、MapboxMapView などのマップ地図SDKに置き換えてください
    MapView(
        state = mapViewState,
        onMapClick = {
            selectedMarkers = emptySet() // すべての選択をクリア
        },
        onMarkerClick = { markerState ->
            selectedMarkers = if (selectedMarkers.contains(markerState.id)) {
                selectedMarkers - markerState.id // 選択解除
            } else {
                selectedMarkers + markerState.id // 選択
            }
        }
    ) {
        markerStates.forEach { markerState ->
            Marker(markerState)

            // マーカーが選択されている場合、バブルを表示
            if (selectedMarkers.contains(markerState.id)) {
                InfoBubble(
                    marker = markerState,
                    bubbleColor = Color.White,
                    borderColor = Color.Black
                ) {
                    Column {
                        Text(
                            text = markerState.extra as String,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tap to close",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}
```

## 配置と動作

### 自動配置

InfoBubble は関連するマーカーの上に自動的に配置されます:

- バブルの尾がマーカーの中心を指します
- バブルは地図のビューポート内で可視状態を維持するように位置を調整します
- 地図がパンまたはズームされると、バブルはマーカーに追従します

### カスタム配置

InfoBubble は自動的に配置を処理しますが、マーカーアイコンのアンカーを通じて影響を与えることができます:

```kotlin
// 下中央アンカーを持つマーカー - バブルは上に表示されます
val markerWithBottomAnchor = MarkerState(
    position = position,
    icon = ImageDefaultIcon(
        drawable = customIcon,
        anchor = Offset(0.5f, 1.0f) // 下中央
    )
)

// 中央アンカーを持つマーカー - バブルは中央の上に表示されます
val markerWithCenterAnchor = MarkerState(
    position = position,
    icon = ImageDefaultIcon(
        drawable = customIcon,
        anchor = Offset(0.5f, 0.5f) // 中央
    )
)
```

## ライフサイクル管理

InfoBubble は自動的にライフサイクルを管理します:

```kotlin
// コンポーネントが構成されるとバブルが表示されます
selectedMarker?.let { marker ->
    InfoBubble(marker = marker) {
        // コンテンツ
    }
}

// コンポーネントがコンポジションから削除されるとバブルが消えます
// これは selectedMarker が null になると自動的に発生します
```

### 手動ライフサイクル制御

```kotlin
@Composable
fun ManualLifecycleExample() {
    var showBubble by remember { mutableStateOf(false) }
    val markerState = remember { /* マーカーの状態 */ }

    LaunchedEffect(showBubble) {
        if (showBubble) {
            delay(3000) // 3秒間表示
            showBubble = false
        }
    }

    // MapView を GoogleMapsView、MapboxMapView などのマップ地図SDKに置き換えてください
    MapView(state = mapViewState) {
        Marker(markerState)

        if (showBubble) {
            InfoBubble(marker = markerState) {
                Text("Auto-hiding bubble")
            }
        }
    }
}
```

## スタイルとテーマ

### ダークモードサポート

```kotlin
@Composable
fun DarkModeInfoBubble(marker: MarkerState) {
    val isDarkTheme = isSystemInDarkTheme()

    InfoBubble(
        marker = marker,
        bubbleColor = if (isDarkTheme) Color(0xFF2D2D2D) else Color.White,
        borderColor = if (isDarkTheme) Color(0xFF555555) else Color.Black,
        contentPadding = 12.dp,
        cornerRadius = 8.dp
    ) {
        Text(
            text = marker.extra as? String ?: "",
            color = if (isDarkTheme) Color.White else Color.Black,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
```

### カスタムテーマ

```kotlin
data class BubbleTheme(
    val backgroundColor: Color,
    val borderColor: Color,
    val textColor: Color,
    val contentPadding: Dp,
    val cornerRadius: Dp,
    val tailSize: Dp
) : java.io.Serializable

val DefaultBubbleTheme = BubbleTheme(
    backgroundColor = Color.White,
    borderColor = Color.Black,
    textColor = Color.Black,
    contentPadding = 8.dp,
    cornerRadius = 4.dp,
    tailSize = 8.dp
)

val DarkBubbleTheme = BubbleTheme(
    backgroundColor = Color(0xFF2D2D2D),
    borderColor = Color(0xFF555555),
    textColor = Color.White,
    contentPadding = 12.dp,
    cornerRadius = 8.dp,
    tailSize = 10.dp
)

@Composable
fun ThemedInfoBubble(
    marker: MarkerState,
    theme: BubbleTheme = DefaultBubbleTheme,
    content: @Composable () -> Unit
) {
    InfoBubble(
        marker = marker,
        bubbleColor = theme.backgroundColor,
        borderColor = theme.borderColor,
        contentPadding = theme.contentPadding,
        cornerRadius = theme.cornerRadius,
        tailSize = theme.tailSize,
        content = content
    )
}
```

## パフォーマンスの考慮事項

### 効率的な更新

```kotlin
// 良い例: マーカーに安定したキーを使用
val markerStates = remember(markersData) {
    markersData.map { data ->
        MarkerState(
            id = "marker_${data.id}", // 安定した ID
            position = data.position,
            extra = data
        )
    }
}

// 避けるべき: リコンポジションごとに新しいマーカー状態を作成
val markerStates = markersData.map { data ->
    MarkerState(position = data.position, extra = data) // 毎回新しいインスタンス
}
```

### メモリ管理

```kotlin
// 画面を離れるときに選択されたマーカーをクリア
DisposableEffect(Unit) {
    onDispose {
        selectedMarker = null // InfoBubble をクリア
    }
}
```

## ベストプラクティス

### デザインガイドライン

1. **簡潔なコンテンツ**: InfoBubble はユーザーを圧倒することなく、本質的な情報を提供するべきです
2. **適切なサイズ**: モバイルデバイスでの可読性を維持するために、バブルの幅を制限してください
3. **明確なアクション**: ボタンを含める場合は、その目的を明確にしてください
4. **タッチターゲット**: インタラクティブな要素が最小タッチターゲットサイズを満たしていることを確認してください

### ユーザーエクスペリエンス

1. **閉じる動作**: ユーザーが地図やマーカーをタップしてバブルを閉じることができるようにしてください
2. **読み込み状態**: ネットワークリクエストが必要なコンテンツについては、読み込みインジケーターを表示してください
3. **エラー処理**: 不足または無効なデータを適切に処理してください
4. **アクセシビリティ**: スクリーンリーダーにコンテンツの説明を提供してください

### 実装のヒント

```kotlin
// 良い例: 安定したマーカー参照
val markerState = remember(markerId) {
    MarkerState(id = markerId, position = position)
}

// 良い例: 効率的なコンテンツ更新
LaunchedEffect(selectedMarkerId) {
    if (selectedMarkerId != null) {
        // 必要に応じて追加データを読み込む
    }
}

// 避けるべき: バブルコンテンツ内での重い計算
InfoBubble(marker = marker) {
    // ここで高コストの操作を避ける
    val processedData = remember(marker.extra) {
        processData(marker.extra) // remember に移動
    }
    DisplayContent(processedData)
}
```

## トラブルシューティング

### よくある問題

1. **バブルが表示されない**: マーカーが適切に構成されており、InfoBubble が MapViewScope 内にあることを確認してください
2. **バブルが閉じない**: 条件付きレンダリングが状態の変化に適切に反応しているか確認してください
3. **パフォーマンスの低下**: 同時に表示するバブルの数を制限し、コンテンツのコンポジションを最適化してください
4. **レイアウトの問題**: 適切なサイズ制約を使用し、さまざまな画面サイズでテストしてください

### デバッグモード

```kotlin
// InfoBubble の配置のデバッグログを有効にする
InfoBubble(
    marker = marker,
    bubbleColor = Color.Yellow.copy(alpha = 0.8f), // デバッグ用にハイライト
    borderColor = Color.Red
) {
    Column {
        Text("Marker ID: ${marker.id}")
        Text("Position: ${marker.position}")
        // 実際のコンテンツ
    }
}
```
