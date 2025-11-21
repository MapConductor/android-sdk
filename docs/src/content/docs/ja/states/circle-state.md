---
title: "CircleState（円の状態）"
---

`CircleState` は、円オーバーレイの中心位置や半径、スタイル設定を管理する状態クラスです。

## コンストラクタ

```kotlin
CircleState(
    center: GeoPoint,
    radiusMeters: Double,
    clickable: Boolean = true,
    strokeColor: Color = Color.Red,
    strokeWidth: Dp = 1.dp,
    fillColor: Color = Color(red = 255, green = 255, blue = 255, alpha = 127),
    id: String? = null,
    zIndex: Int? = null,
    extra: Serializable? = null
)
```

## 主なプロパティ

- **`center`**: 円の中心座標
- **`radiusMeters`**: 半径（メートル）
- **`strokeColor` / `strokeWidth`**: 枠線の色と太さ
- **`fillColor`**: 塗りつぶし色
- **`clickable`**: クリック可否
- **`extra`**: 付帯データ

`CircleState` は `copy` / `fingerPrint` / `asFlow` などのメソッドを備え、リアクティブな更新と効率的な差分検知をサポートします。

利用例は [Circle コンポーネント](/ja/components/circle) を参照してください。

