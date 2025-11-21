---
title: "PolygonState（ポリゴン状態）"
---

`PolygonState` は、ポリゴンの外周や穴（holes）、スタイル設定を管理するための状態クラスです。

## 典型的なプロパティ

- **`points: List<GeoPoint>`**: 外周の座標
- **`holes: List<List<GeoPoint>>`**: 内側の穴ポリゴン
- **`strokeColor` / `strokeWidth`**: 枠線の色と太さ
- **`fillColor`**: 塗りつぶし色
- **`clickable`**: クリック可否
- **`extra`**: 付帯データ

利用例は [Polygon コンポーネント](/ja/components/polygon) を参照してください。

