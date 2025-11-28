---
title: "Marker Animation（マーカーアニメーション）"
---

MarkerAnimation は、地図上のマーカーに対するスムーズな遷移と視覚効果を提供します。アニメーションは、マーカーの外観、位置の変更、ライフサイクルイベントに適用できます。

## MarkerAnimation インターフェース

```kotlin
interface MarkerAnimation {
    val duration: Long
    val interpolator: TimeInterpolator?

    // アニメーションライフサイクルメソッド
    fun onStart()
    fun onUpdate(progress: Float)
    fun onEnd()
}
```

## アニメーションタイプ

### 位置アニメーション

座標を更新する際に、マーカーの位置変更をスムーズにアニメーション化します。

```kotlin
class PositionAnimation(
    val fromPosition: GeoPoint,
    val toPosition: GeoPoint,
    override val duration: Long = 1000,
    override val interpolator: TimeInterpolator? = AccelerateDecelerateInterpolator()
) : MarkerAnimation
```

#### 使用例

```kotlin
@Composable
fun AnimatedMarkerExample() {
    var markerPosition by remember {
        mutableStateOf(GeoPointImpl.fromLatLong(37.7749, -122.4194))
    }
    val markerState = remember { MarkerState(position = markerPosition) }

    // マーカーを新しい位置にアニメーション
    LaunchedEffect(markerPosition) {
        val animation = PositionAnimation(
            fromPosition = markerState.position,
            toPosition = markerPosition,
            duration = 1500
        )
        markerState.setAnimation(animation)
    }

    // MapView を GoogleMapsView、MapboxMapView などの選択した地図SDKに置き換えてください
    MapView(
        state = mapViewState,
        onMapClick = { clickedPosition ->
            markerPosition = clickedPosition // アニメーションをトリガー
        },
        onMarkerAnimateStart = { markerState ->
            println("マーカーのアニメーション開始: ${markerState.id}")
        },
        onMarkerAnimateEnd = { markerState ->
            println("マーカーのアニメーション終了: ${markerState.id}")
        }
    ) {
        Marker(markerState)
    }
}
```

### スケールアニメーション

強調や状態遷移のためにマーカーのサイズ変更をアニメーション化します。

```kotlin
class ScaleAnimation(
    val fromScale: Float,
    val toScale: Float,
    override val duration: Long = 500,
    override val interpolator: TimeInterpolator? = OvershootInterpolator()
) : MarkerAnimation
```

#### 使用例

```kotlin
@Composable
fun ScaleAnimationExample() {
    var isExpanded by remember { mutableStateOf(false) }
    val markerState = remember {
        MarkerState(
            position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            icon = DefaultIcon(fillColor = Color.Blue, scale = 1.0f)
        )
    }

    // スケール変更をアニメーション
    LaunchedEffect(isExpanded) {
        val animation = ScaleAnimation(
            fromScale = if (isExpanded) 1.0f else 1.5f,
            toScale = if (isExpanded) 1.5f else 1.0f,
            duration = 300,
            interpolator = BounceInterpolator()
        )
        markerState.setAnimation(animation)
    }

    // MapView を GoogleMapsView、MapboxMapView などの選択した地図SDKに置き換えてください
    MapView(
        state = mapViewState,
        onMarkerClick = {
            isExpanded = !isExpanded
        }
    ) {
        Marker(markerState)
    }
}
```

### フェードアニメーション

表示・非表示効果のためにマーカーの不透明度を制御します。

```kotlin
class FadeAnimation(
    val fromAlpha: Float,
    val toAlpha: Float,
    override val duration: Long = 800,
    override val interpolator: TimeInterpolator? = AccelerateDecelerateInterpolator()
) : MarkerAnimation
```

#### 使用例

```kotlin
@Composable
fun FadeAnimationExample() {
    var markersVisible by remember { mutableStateOf(true) }
    val markerStates = remember {
        listOf(
            MarkerState(
                position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
                icon = DefaultIcon(fillColor = Color.Red)
            ),
            MarkerState(
                position = GeoPointImpl.fromLatLong(37.7849, -122.4094),
                icon = DefaultIcon(fillColor = Color.Blue)
            )
        )
    }

    // 不透明度の変更をアニメーション
    LaunchedEffect(markersVisible) {
        markerStates.forEach { markerState ->
            val animation = FadeAnimation(
                fromAlpha = if (markersVisible) 0.0f else 1.0f,
                toAlpha = if (markersVisible) 1.0f else 0.0f,
                duration = 600
            )
            markerState.setAnimation(animation)
        }
    }

    Column {
        Button(onClick = { markersVisible = !markersVisible }) {
            Text(if (markersVisible) "マーカーを隠す" else "マーカーを表示")
        }

        // MapView を GoogleMapsView、MapboxMapView などの選択した地図SDKに置き換えてください
        MapView(state = mapViewState) {
            markerStates.forEach { markerState ->
                Marker(markerState)
            }
        }
    }
}
```

### バウンスアニメーション

マーカーが表示されるとき、または操作されるときにバウンス効果を作成します。

```kotlin
class BounceAnimation(
    val bounceHeight: Float = 50f,
    override val duration: Long = 1000,
    override val interpolator: TimeInterpolator? = BounceInterpolator()
) : MarkerAnimation
```

#### 使用例

```kotlin
@Composable
fun BounceAnimationExample() {
    var triggerBounce by remember { mutableStateOf(false) }
    val markerState = remember {
        MarkerState(
            position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            icon = DefaultIcon(fillColor = Color.Green, label = "バウンス!")
        )
    }

    // バウンスアニメーションをトリガー
    LaunchedEffect(triggerBounce) {
        if (triggerBounce) {
            val animation = BounceAnimation(
                bounceHeight = 30f,
                duration = 800
            )
            markerState.setAnimation(animation)
            triggerBounce = false
        }
    }

    // MapView を GoogleMapsView、MapboxMapView などの選択した地図SDKに置き換えてください
    MapView(
        state = mapViewState,
        onMarkerClick = {
            triggerBounce = true
        }
    ) {
        Marker(markerState)
    }
}
```

## 複雑なアニメーション

### 連続アニメーション

複雑な効果のために複数のアニメーションを連鎖させます。

```kotlin
class SequentialAnimation(
    private val animations: List<MarkerAnimation>
) : MarkerAnimation {
    private var currentIndex = 0
    private var currentAnimation: MarkerAnimation? = null

    override val duration: Long = animations.sumOf { it.duration }
    override val interpolator: TimeInterpolator? = null

    fun playNext() {
        if (currentIndex < animations.size) {
            currentAnimation = animations[currentIndex++]
            currentAnimation?.onStart()
        }
    }
}
```

#### 使用例

```kotlin
@Composable
fun SequentialAnimationExample() {
    val markerState = remember {
        MarkerState(
            position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
            icon = DefaultIcon(fillColor = Color.Purple)
        )
    }

    LaunchedEffect(Unit) {
        val sequentialAnimation = SequentialAnimation(
            animations = listOf(
                ScaleAnimation(fromScale = 0.5f, toScale = 1.5f, duration = 500),
                FadeAnimation(fromAlpha = 1.0f, toAlpha = 0.3f, duration = 300),
                FadeAnimation(fromAlpha = 0.3f, toAlpha = 1.0f, duration = 300),
                ScaleAnimation(fromScale = 1.5f, toScale = 1.0f, duration = 400)
            )
        )
        markerState.setAnimation(sequentialAnimation)
    }

    // MapView を GoogleMapsView、MapboxMapView などの選択した地図SDKに置き換えてください
    MapView(state = mapViewState) {
        Marker(markerState)
    }
}
```

### パスアニメーション

事前定義されたパスに沿ってマーカーをアニメーション化します。

```kotlin
class PathAnimation(
    val waypoints: List<GeoPoint>,
    override val duration: Long = 3000,
    override val interpolator: TimeInterpolator? = LinearInterpolator()
) : MarkerAnimation {

    fun getPositionAtProgress(progress: Float): GeoPoint {
        // 進捗（0.0 から 1.0）に基づいてパスに沿った位置を計算
        val totalDistance = calculateTotalDistance()
        val targetDistance = totalDistance * progress

        // セグメントを見つけて位置を補間
        return interpolateAlongPath(targetDistance)
    }
}
```

#### 使用例

```kotlin
@Composable
fun PathAnimationExample() {
    val waypoints = remember {
        listOf(
            GeoPointImpl.fromLatLong(37.7749, -122.4194), // スタート
            GeoPointImpl.fromLatLong(37.7849, -122.4094), // ウェイポイント 1
            GeoPointImpl.fromLatLong(37.7949, -122.3994), // ウェイポイント 2
            GeoPointImpl.fromLatLong(37.8049, -122.3894)  // 終点
        )
    }

    val markerState = remember {
        MarkerState(
            position = waypoints.first(),
            icon = DefaultIcon(fillColor = Color.Orange, label = "🚗")
        )
    }

    // パスアニメーションを開始
    LaunchedEffect(Unit) {
        val pathAnimation = PathAnimation(
            waypoints = waypoints,
            duration = 5000 // パスを完了するのに 5 秒
        )
        markerState.setAnimation(pathAnimation)
    }

    // MapView を GoogleMapsView、MapboxMapView などの選択した地図SDKに置き換えてください
    MapView(state = mapViewState) {
        // パスを表示
        Polyline(
            points = waypoints,
            strokeColor = Color.Blue,
            strokeWidth = 3.dp
        )

        // アニメーション化されたマーカー
        Marker(markerState)

        // ウェイポイントマーカー
        waypoints.forEachIndexed { index, point ->
            Marker(
                position = point,
                icon = DefaultIcon(
                    fillColor = Color.Gray,
                    scale = 0.6f,
                    label = "$index"
                )
            )
        }
    }
}
```

## アニメーションイベント処理

### アニメーションコールバック

地図コンポーネントでアニメーションライフサイクルイベントを処理します:

```kotlin
// MapView を GoogleMapsView、MapboxMapView などの選択した地図SDKに置き換えてください
MapView(
    state = mapViewState,
    onMarkerAnimateStart = { markerState ->
        println("マーカーのアニメーション開始: ${markerState.id}")
        // UI 状態の更新、ローディングインジケータの開始など
    },
    onMarkerAnimateEnd = { markerState ->
        println("マーカーのアニメーション完了: ${markerState.id}")
        // リソースのクリーンアップ、最終状態の更新など
    }
) {
    // アニメーション付きマーカー
}
```

### カスタムアニメーション進捗

カスタム動作のためにアニメーション進捗を監視します:

```kotlin
class ProgressTrackingAnimation(
    private val baseAnimation: MarkerAnimation,
    private val onProgress: (Float) -> Unit
) : MarkerAnimation by baseAnimation {

    override fun onUpdate(progress: Float) {
        baseAnimation.onUpdate(progress)
        onProgress(progress)
    }
}

// 使用法
val trackingAnimation = ProgressTrackingAnimation(
    baseAnimation = PositionAnimation(from, to, 2000)
) { progress ->
    // カスタム進捗処理
    println("アニメーションは ${(progress * 100).toInt()}% 完了")
}
```

## パフォーマンスの考慮事項

### アニメーション最適化

1. **同時アニメーションを制限**: 同時アニメーションが多すぎるとパフォーマンスに影響
2. **適切な期間を使用**: 非常に長いアニメーションは遅く感じる可能性があります
3. **効率的なインターポレータを選択**: 一部のインターポレータは計算コストが高い

```kotlin
// 良い例: 適度なアニメーション数と期間
val animations = markerStates.take(10).map { markerState ->
    PositionAnimation(
        fromPosition = markerState.position,
        toPosition = newPosition,
        duration = 800 // 適度な期間
    )
}

// 避けるべき: 長すぎるアニメーションが多すぎる
val badAnimations = markerStates.take(100).map { markerState ->
    PositionAnimation(
        fromPosition = markerState.position,
        toPosition = newPosition,
        duration = 5000 // 長すぎる
    )
}
```

### メモリ管理

```kotlin
// 不要になったらアニメーションをクリア
LaunchedEffect(shouldClearAnimations) {
    if (shouldClearAnimations) {
        markerStates.forEach { it.setAnimation(null) }
    }
}
```

## ベストプラクティス

### アニメーションガイドライン

1. **フィードバックを提供**: アニメーションを使用してユーザーの操作に視覚的なフィードバックを提供
2. **コンテキストを維持**: アニメーションはユーザーが空間的な関係を理解するのに役立つべきです
3. **一貫性を保つ**: アプリケーション全体で類似したアニメーションスタイルを使用
4. **アクセシビリティを考慮**: アニメーションを減らすまたは無効にするオプションを提供

### 一般的なパターン

```kotlin
// 新しいマーカーのエントランスアニメーション
fun createEntranceAnimation(): MarkerAnimation = SequentialAnimation(
    listOf(
        ScaleAnimation(fromScale = 0.0f, toScale = 1.2f, duration = 200),
        ScaleAnimation(fromScale = 1.2f, toScale = 1.0f, duration = 100)
    )
)

// 注目を集めるアニメーション
fun createAttentionAnimation(): MarkerAnimation = SequentialAnimation(
    listOf(
        ScaleAnimation(fromScale = 1.0f, toScale = 1.3f, duration = 150),
        ScaleAnimation(fromScale = 1.3f, toScale = 1.0f, duration = 150),
        ScaleAnimation(fromScale = 1.0f, toScale = 1.3f, duration = 150),
        ScaleAnimation(fromScale = 1.3f, toScale = 1.0f, duration = 150)
    )
)

// スムーズな終了アニメーション
fun createExitAnimation(): MarkerAnimation = SequentialAnimation(
    listOf(
        FadeAnimation(fromAlpha = 1.0f, toAlpha = 0.0f, duration = 300),
        ScaleAnimation(fromScale = 1.0f, toScale = 0.0f, duration = 200)
    )
)
```

## トラブルシューティング

### 一般的な問題

1. **アニメーションが開始しない**: `MarkerState.setAnimation()` が呼び出されていることを確認
2. **ギクシャクした動き**: インターポレータの選択とフレームレートを確認
3. **メモリリーク**: マーカーが削除されたときにアニメーションをクリア
4. **パフォーマンス問題**: 同時アニメーションを制限し、短い期間を使用

### アニメーションのデバッグ

```kotlin
// アニメーションのデバッグを有効化
val debugAnimation = object : MarkerAnimation {
    override val duration = 1000L
    override val interpolator = AccelerateDecelerateInterpolator()

    override fun onStart() {
        Log.d("Animation", "アニメーション開始")
    }

    override fun onUpdate(progress: Float) {
        Log.d("Animation", "進捗: $progress")
    }

    override fun onEnd() {
        Log.d("Animation", "アニメーション完了")
    }
}
```
