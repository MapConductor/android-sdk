# android-for-openmobilemaps

[Open Mobile Maps](https://github.com/openmobilemaps/maps-core)（`io.openmobilemaps:mapscore`）の
MapConductor ドライバー。

ドライバーの**一般的な**書き方は `android-for-template/README.md` にあります。
ここに書くのは **この SDK 固有の話だけ**です。

---

## 1. この SDK の描画モデル

「オーバーレイ 1 つ = ネイティブオブジェクト 1 つ」ではありません。
**種別ごとに 1 枚のレイヤがあり、そこへ要素のリストを流し込む**形です（MapLibre の
GeoJSON ソースに近い）。したがって各レンダラは

- `onAdd` / `onChange` … 要素（`IconInfoInterface` / `LineInfoInterface` / `PolygonInfo`）を作る
- `onPostProcess` … マネージャの全要素を集めてレイヤへ一括で流す

という形になります。個別の `onRemove` でレイヤを触らないのは、どのみち
`onPostProcess` で全量を流し直すためです。

レイヤの生成と重ね順は `OpenMobileMapsLayers` が一手に持っています。

---

## 2. 踏み抜いた罠（すべて「黙って壊れる」種類）

例外もログも出ないので、**知らないと必ず時間を溶かします**。

### 2-1. `insertLayerAt` は挿入ではなく上書き

`MapScene` はレイヤを「索引 → レイヤ」の map で持っていて、`insertLayerAt(layer, i)` は
索引 `i` に居たレイヤを `onRemoved()` してから置き換えます。

地図デザインを索引 0 に入れた結果、索引 0 に居たポリゴン塗りレイヤが黙って外れていました。
症状は「**円は塗れるのにポリゴンだけ塗れない**」。座標や巻き方向を疑って遠回りしました。

索引は `0` = 地図デザイン / `1..` = ラスター・グラウンドイメージ / `1000..` = 固定オーバーレイ、
と分けて衝突させないこと。

### 2-2. `asLayerInterface()` は呼ぶたびに別のオブジェクトを返す

- `insertLayerBelow(x, polygonFillLayer.asLayerInterface())` は「そんなレイヤは無い」で
  **GL スレッドから落ちます**（`RuntimeException: MapScene does not contain below layer`）
- `removeLayer(x.asLayerInterface())` は**黙って何も外しません**

載せたときの値を持っておいて、それを渡すこと。`OpenMobileMapsActualRasterLayer` /
`OpenMobileMapsActualGroundImage` が `layerInterface` を持っているのはそのためです。

### 2-3. `BitmapTextureHolder` は渡した Bitmap を recycle する

テクスチャへ描き写したあと、**元の Bitmap を `recycle()` します**。
一方 MapConductor の `BitmapIconCache` は同じアイコンに同一の `BitmapIcon`（＝同一の Bitmap）を
配ります。素直に渡すと 1 個目で共有 Bitmap が死に、**2 個目で
`Canvas: trying to use a recycled bitmap` で落ちます**。
マーカーが 1 個のページでは再現しないので気づくのが遅れます。

複製を渡し、できたテクスチャはアイコンごとにキャッシュすること
（`OpenMobileMapsMarkerOverlayRenderer.textureFor`）。

### 2-4. bearing の符号が MapConductor と逆

MapConductor の bearing は Google 準拠で「カメラの向きを北から時計回り」。
SDK の `setRotation` は地図を反時計回りに回す量なので、そのまま渡すと逆に回ります。

単独で見ると「ちゃんと回っている」ので正しく見えます。**MapLibre と並べるまで
180 度ずれに気づけませんでした。**

### 2-5. カメラ通知が多すぎる

`onVisibleBoundsChanged` を描画フレームごとに、しかも同じ値で何度も呼びます。
実測で **1 秒のカメラアニメーション中に 388 回**（大半は前回とまったく同じ値）。

1 回ごとに `readNativeCamera()` を回すと、可視領域の 4 隅の逆投影（JNI）が
メインスレッドで毎秒 1,500 回以上走ります。その結果**他の地図の動きが止まります**
（CameraSync で Google Maps と並べたとき、Google 側のアニメーションが途中で固まる形で表面化）。

フレームごとに 1 回へ畳み、前回と同じなら配らないこと。388 回 → 56 回になります。

---

## 3. この SDK に無くて、こちらで作ったもの

### 3-1. ズームの換算（`zoom/ZoomAltitudeConverter.kt`）

**この SDK のズームは縮尺の分母**（1:500,000,000 の 500,000,000 の側）で、2 の指数ではありません。
他プロバイダのようなオフセットの足し算では変換できず、対数を挟みます。

統一ズーム 0 の縮尺 `986,097,222` は実測合わせではなく `156543.033928 × 160 ÷ 0.0254` の**導出値**です。
`setupMap` に `densityDpi` を渡していることが前提で、SDK のサンプルのように `xdpi` を渡すと
端末ごとに数 % ずれます。

タイルのズームレベルも自前で刻んでいます（`tile/WebMercatorTileLayerConfig.kt`）。
SDK 同梱の web メルカトル設定はレベル 0 が 1:500,000,000 で統一ズームと約 2 倍ずれるため、
そのまま使うと**常に 1 段ぼけたタイル**が選ばれます。

### 3-2. tilt（`OpenMobileMapsTiltEmulation.kt`）

2D カメラにピッチがありません。`MapCamera3dInterface` なら傾けられますが、それは
`setupMap(..., is3D = true)` で**地球儀表示**にしたときだけで、平面地図のまま傾けることはできません。

android-for-arcgis の 2D と**同じ方式・同じ定数**で擬似表現します。遠近感は
`OpenMobileMapsMapSurface` がビューを X 軸まわりに回して作り、tilt < 0 は中心を進行方向へ前進させます。

ビューを回す方式なので、**次の 2 つが必ず対で要ります**。片方だけだと傾けたページで崩れます。

- **投影の畳み込み**。投影は内側の `MapView` の座標系で返るので、
  `OpenMobileMapsMapSurface.fromInnerToSurface` / `fromSurfaceToInner` で入れ物の座標へ畳む。
  畳まないと InfoBubble が画面外へ飛ぶ
- **アイコンの引き伸ばし**。SDK が描いたものは一律に縦が `cos(傾き)` へ潰れる。地面に
  寝ているもの（ポリゴン・ポリライン・タイル）はそれで正しいが、マーカーは正面を向くべきなので、
  先に `1 / cos(傾き)` 伸ばして相殺する（`OpenMobileMapsMarkerOverlayRenderer.verticalStretch`）

### 3-3. カメラアニメーション（`OpenMobileMapsCameraAnimation.kt`）

`moveToCenterPositionZoom(..., animated = true)` は**尺を指定できず、実測で常に約 300ms** で
着地します。アプリが `durationMillis = 1000` と言っても 300ms で終わるため、他プロバイダと
並べると明らかに先に着きます。

SDK のアニメーションは使わず、フレームを刻んで `animated = false` の移動を繰り返します。
中心は**Web メルカトルのメートル空間**で線形補間（緯度経度を直接混ぜると高緯度で速度が
変わって見える）。方位は近い方向へ回ります。イージングは Android 標準の
`AccelerateDecelerateInterpolator` と同じ余弦カーブです。

**指が触れたら止めること**（`OpenMobileMapsTouchListener.onTouchDown`）。
止めないとアニメーションがユーザーの操作と綱引きになります。

---

## 4. 適合テストで確かめられないこと

`PolygonLayerInterface.create()` の時点で JNI に入るので、**コントローラもレンダラも
素の JVM では作れません**。`OpenMobileMapsDriverConformanceTest` が押さえているのは
「地図SDKに触らない純粋な計算」だけです（ズーム換算・tilt の擬似表現・タイル URL・
カメラ補間・capability の宣言）。

描画・タップ・ドラッグ・InfoBubble の追従は**実機で確かめてください**。
上の罠はどれもユニットテストでは絶対に捕まりません。

```bash
adb shell am start -n com.mapconductor.example/.MainActivity \
  --es page map-basic --es provider openmobilemaps
```

**必ず他プロバイダと並べて比べること。** 2-4（bearing）と 2-5（通知過多）は、
単独で見ているかぎり「正しく動いている」ようにしか見えませんでした。

### 2-6. 透過ラスターレイヤは `maskTile = true` にしないと粗い親タイルが透けて残る

`Tiled2dMapSource` は**粗い親レベルのタイルを意図的に保持し続ける**
（`Tiled2dMapSourceImpl.h`: 現行レベル以下のタイルは可視なら残す。さらに
`ALWAYS_KEEP_LEVEL_TARGET_ZOOM_OFFSET = -8` で約 8 段粗いレベルを常に 1 枚保持する）。
`numDrawPreviousLayers = 0` にしても**この保持は止まらない**。

不透明な地図タイルなら現行タイルの下に隠れて無害だが、**透過オーバーレイでは
親タイルが透けて見え続ける**。PostOffice ページで「巨大でぼやけたアイコンが
画面を覆い、ズームしても消えない」という形で出た。ズームのたびに粗い層が
見えるので、「ラスターレイヤが何度も読み込まれる」ようにも見える。

対策は `Tiled2dMapZoomInfo.maskTile = true`（ステンシルマスク。細かいタイルが
読めた領域から親タイルが刻み抜かれる）。あわせて `numDrawPreviousLayers = 0` に
して透過タイルの重ね描き自体もやめる。地図デザイン（不透明）は既定のままでよい。

切り分けの決め手は 2 つ:
- ローカルタイルサーバから**タイル画像を直接取得**して中身が正しいことを確認
  （`adb forward` + `curl`。壊れていたのは描画の側と確定）
- OkHttp のインターセプタで**全タイルの読み込み結果を記録**（全レベル 200 で成功
  していた。DataLoader の `loadTexture` の override はログに出ない。SDK は
  `loadTextureAsync` しか呼ばず、そちらは内部で OkHttp を直接使うため）

---

## 5. 現状の制限

- 地図デザインはラスタータイルのみ。ベクタータイル（`TiledVectorLayer`）にも対応できますが、
  ラベルの描画に距離場フォントのアセットを同梱する必要があり、別作業です
- 傾きはビューを `rotationX` で回して作るので、**マーカー以外の画面固定物は一緒に寝ます**。
  マーカーは縦の引き伸ばしで相殺していますが（`verticalStretch`）、
  同じ相殺を要するものが増えたらそこに足してください
- `MapUISettings` はスクロール／ズームの個別切り替えを持てません
  （SDK にタッチハンドラ全体の on/off しか無いため）。capability で宣言済み
