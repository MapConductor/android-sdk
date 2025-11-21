---
title: "GroundImageState（画像オーバーレイ状態）"
---

`GroundImageState` は、`GroundImage` オーバーレイの表示状態（対象画像、表示範囲、透明度など）を管理するクラスです。

## 主な要素

- **`bounds: GeoRectBounds`**: 画像を貼り付ける地理的範囲
- **`transparency: Float`**: 透明度
- **`extra: Serializable?`**: 付帯データ

Google Maps 専用機能である点に注意してください（他のプロバイダでは無視されるか、未サポートとなります）。

