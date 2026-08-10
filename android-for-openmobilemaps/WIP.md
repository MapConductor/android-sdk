# 作業中（ビルド対象から外してあります）

Open Mobile Maps SDK 4.0.0 用ドライバー。**まだコンパイルが通りません。**
`projects.properties` の `modules=` から外してあるので、リポジトリ全体のビルドには
影響しません。再開するときに戻してください。

## 済み

- `io.openmobilemaps:mapscore:4.0.0` を version catalog に追加（Maven Central）
- Kotlin 2.4.10 / compileSdk 36 への引き上げ（**別コミット**。全モジュール検証済み）
- `OpenMobileMapsMapViewHolder` … 実装点 A。SDK の
  `screenPosFromCoord` / `coordFromScreenPosition` で**同期投影を両方向**実装済み
- `OpenMobileMapsMapView` … `AndroidView` で SDK の `MapView` を載せ、
  EPSG:4326 で `setupMap` し `registerLifecycle` する

## 残り

- `OpenMobileMapsMapViewController` … カメラ・イベント転送・capability 宣言
  （雛形のままで、削除した代役 SDK を参照しているので通らない）
- 6 レンダラ … icon / polygon / line / raster は SDK にそのままある。
  circle と groundImage は polygon / raster に載せる
- example-app への組み込み
- 実機検証（15 項目）

## SDK 調査で分かっていること

| 必要なもの | Open Mobile Maps の API |
|---|---|
| 同期投影（両方向） | `MapCameraInterface.screenPosFromCoord` / `coordFromScreenPosition` |
| カメラ | `moveToCenterPositionZoom` / `setZoom` / `setRotation` / `moveToBoundingBox` |
| 可視範囲 | `getVisibleRect`（4 隅逆投影でも可） |
| マーカー | `IconLayerInterface` + `IconFactory.createIconWithAnchor`。`IconType.INVARIANT` で画面固定サイズ |
| ポリゴン | `PolygonLayerInterface` + `PolygonCoord`（**holes をネイティブに持てる**） |
| ポリライン | `LineLayerInterface` + `LineFactory` |
| ラスター | `Tiled2dMapRasterLayerInterface` |
| 座標 | `Coord(EPSG4326, x=経度, y=緯度, z)` |

### tilt が唯一の難所

2D カメラに `setTilt` は無い。`MapCamera3dInterface` の
`setPoseCamera(yaw, pitch, roll, fov, near, far)` か `setCustomViewMatrix` を使う。
行列を直接渡せるので、tilt < 0 を数学的に計算する方針とはむしろ相性がよい。
