---
title: "Marker Animation（マーカーアニメーション）"
---

`MarkerAnimation` は、マーカーに対してアニメーションを適用するための設定をまとめた型です。位置の変化やフェードイン/フェードアウトなど、さまざまなアニメーションを表現できます。

## MarkerState と組み合わせて利用

```kotlin
val state = MarkerState(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    icon = DefaultIcon(label = "Animated"),
)

val animation = MarkerAnimation.FadeIn(durationMillis = 500)

state.setAnimation(animation)
```

`MarkerAnimation` の具体的な種類やパラメータは、KDoc や実装クラスを参照してください。

