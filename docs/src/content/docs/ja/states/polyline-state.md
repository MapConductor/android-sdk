---
title: "PolylineState（ポリライン状態）"
---

`PolylineState` は、ポリラインの座標リストやスタイルを管理する状態クラスです。

## 典型的なプロパティ

- **`points: List<GeoPoint>`**: 線分を構成する座標
- **`color: Color`**: 線の色
- **`width: Dp`**: 線の太さ
- **`clickable: Boolean`**: クリック可否
- **`extra: Serializable?`**: 付帯データ

`copy` / `fingerPrint` / `asFlow` などの API は `CircleState` / `MarkerState` と同様のパターンで提供されます。

