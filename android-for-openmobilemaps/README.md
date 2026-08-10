# 地図SDKドライバーの書き方（Android）

このモジュールは **動く最小のドライバー** です。読んで、コピーして、`OpenMobileMaps` を
あなたの地図SDK名に置き換えるところから始めてください。

`OpenMobileMapsMap` が「あなたが使う地図SDK」の代役です。**置き換えるのはそこだけ**で、
まわりのホルダー・コントローラ・レンダラの**形はそのまま**使えます。

---

## 1. 何を書くのか

Step 0〜7 の共通化で、ドライバーが書く実装点は **90〜100 → 約 45** に減りました。
そのうち 29 は「地図SDK固有の翻訳」で、これは減らせません。

| # | 実装点 | 個数 | ファイル |
|---|---|---|---|
| A | ホルダー — `mapView` / `map` / 投影 2 つ | 4 | `OpenMobileMapsMap.kt` |
| B | コントローラ — `holder` / カメラ読み書き / `fitBounds` | 5 | `OpenMobileMapsMapViewController.kt` |
| C | 地図デザイン型 | 3 | `OpenMobileMapsMapView.kt` |
| D | **レンダラ 6 種 × onAdd/onChange/onRemove/onPostProcess** | 25 | `OpenMobileMapsOverlays.kt` |
| E | SDK イベントの転送 | 6 | `installListeners()` |
| F | ドラッグ中のパン抑止 | 1 | SDK 次第 |
| G | State サブクラス | 3 | `OpenMobileMapsMapView.kt` |
| H | capability の宣言 | 1 | `declareCapabilities()` |

## 2. 何を書かなくてよいのか

以下はすべてコアが持っています。**書き始める前にこの一覧を読んでください。**
過去のプロバイダはこれらを各自で書いており、それが重複の正体でした。

- **クリックのカスケード** — `marker → circle → groundImage → polyline → polygon → map`。
  `BaseMapViewController.dispatchTap(position)` を呼ぶだけ。
- **オーバーレイの当たり判定** — 各 `Manager` が持っています（測地線ポリゴンの
  巻き数判定、穴の除外、球面距離、線分への近接）。
- **`clickable = false` の透過** — 握り潰しではなく次の層へ流します。
- **マーカーのヒットテスト** — `AbstractMarkerController.find`。アイコン矩形 +
  tapTolerance を画面座標で判定します。
- **`compositionXxx` / `updateXxx` / `hasXxx`**（Capable ファサード）— コントローラを
  `registerOverlayController` するだけで既定が働きます。
- **マーカーのドラッグの保持とリスナー転送** — `DefaultMarkerEventController`。
- **VisibleRegion の組み立て** — `holder.buildVisibleRegion()` が 4 隅を逆投影します。
- **ズームの往復換算** — `WebMercatorZoomAltitudeConverter`。

## 3. 手順

1. `android-for-openmobilemaps` をコピーして `android-for-<sdk>` にする
2. `OpenMobileMapsMap.kt` の `OpenMobileMapsMap` を実際の SDK の地図型に置き換える
3. `OpenMobileMapsOverlays.kt` の 6 レンダラを SDK のオブジェクト生成に書き換える（**ここが本体**）
4. `OpenMobileMapsMapViewController.kt` のカメラ換算とイベント転送を SDK に合わせる
5. `OpenMobileMapsMapView.kt` の Canvas を `AndroidView { SDKのMapView }` に置き換える
6. `OpenMobileMapsDriverConformanceTest.kt` をそのまま動かす
7. **実機で確かめる**（§6）

`projects.properties` の `modules=` に追加すると CI のビルド・lint・テストに乗ります。

---

## 4. つまずくところ（実際に作り込んだ不具合）

### 4-1. コントローラを `registerOverlayController` し忘れる

`compositionXxx` も `hasXxx` もクリックカスケードも**黙って**効かなくなります。
「追加したのに表示されない」の大半はこれです。

### 4-2. `SlottedOverlayController` を実装し忘れる

`kind` と `resolveTap` は**抽象**なので、コアのコントローラを継承していれば
コンパイラが守ってくれます。しかし**継承せずに自前で `OverlayControllerInterface` を
実装した**とき（複数のレンダラを束ねる「コンダクタ」を作りたくなったとき）に漏れます。

実際に `MapLibrePolygonConductor` / `MapboxPolygonConductor` がこれで、
`hasPolygon` が常に false になり**ポリゴン単体の状態更新が黙って捨てられて**いました。
ビルドも API チェックも既存のユニットテストも緑のままでした。

→ `MapDriverConformance.checkOverlaySlots()` が 1 本で捕まえます。必ず入れてください。

### 4-3. `find` で `clickable` を見る

`find` は**ドラッグの開始判定にも使われます**。ここで `clickable` を見ると
`clickable = false` かつ `draggable = true` のマーカーがドラッグできなくなります。
クリックの可否は配送側（`dispatchClick` / `clickableOnly`）で判断してください。

### 4-4. `Unknown` を「非対応」と読む

capability の宣言が無い状態は「まだ宣言していない」であって「使えない」ではありません
（地図の初期化途中もここに入ります）。

**`Unsupported` は「その機能が動かない」ときだけ**にしてください。
ホルダーに同期変換が無くても別経路でオーバーレイを配置できているなら `Degraded` です。
`Unsupported` にするとコアが**動いている機能を止めます**（android-for-longdo で
一度そうしかけました）。

### 4-5. 地図クリックとオーバーレイクリックの二重配送

WebView ブリッジ系で起きがちです。「まず地図クリックを通知し、そのうえで各オーバーレイに
配送する」と、ポリゴンをタップしたとき `onMapClick` と `polygon.onClick` の**両方**が
飛びます。`dispatchTap` を使えば必ずどれか 1 つだけになります。

### 4-6. ネイティブのマーカークリックを外せない SDK

原則は「すべてのクリックを地図クリックで受け、コアがヒットテストする」です。
ただし例外があります（実機計測で確認済み）。

| SDK | 挙動 |
|---|---|
| Google Maps | `Marker` / `MarkerOptions` に `clickable` が無い。タップしても `OnMapClickListener` は発火しない |
| TomTom（マーカー） | 同上。`MarkerClickListener` は `void` を返し、消費の可否を選べない |
| TomTom（polygon/polyline/circle） | `isClickable = false` は**透過ではなく握り潰し**。どのリスナーも発火しない |

この 2 つは `dispatchNativeMarkerClick(tag)` を使ってください。**管理外のマーカー**
（アプリが `state.getMapViewHolder()?.map` 経由で直接追加したもの）を横取りしない
tag 判定もそこに入っています。

---

## 5. 適合テスト

```kotlin
MapDriverConformance.checkZoomConverter(MyZoomConverter())
MapDriverConformance.checkOverlaySlots(registeredControllers)
MapDriverConformance.checkCascadeOrder()
MapDriverConformance.checkCapabilityDeclarations(registry)
MapDriverConformance.checkProjectionRoundTrip(::toScreen, ::fromScreen, samples)
```

`OpenMobileMapsDriverConformanceTest.kt` に実例があります。素の JUnit で動きます
（`Resources.getSystem()` にはユニットテスト用のフォールバックを入れてあるので、
ポリラインの当たり判定も JVM で回せます）。

**マーカーの描画だけは JVM で回せません**（`Bitmap` を通るため）。
マーカーのヒットテストとドラッグは実機で確かめてください。

---

## 6. 実機で確かめること

```bash
adb shell am start -n com.mapconductor.example/.MainActivity \
  --es page polygon-click --es provider <あなたのキー>
```

| ページ | 回帰を示す症状 |
|---|---|
| `marker-postoffice` | 情報ウィンドウが出ない（タイル描画マーカーのヒットテスト） |
| `circle` / `groundImage` | 内側で反応しない、外側で反応する |
| `polyline-click` | マーカーがタップ点に置かれる（**線上の最近傍点**でなければならない） |
| `polygon-click` | Inside/Outside の判定、座標が `-180..180` に収まるか |
| `polygon-hole` | 穴の中が Outside になるか。マーカードラッグ |
| `map-visibleregion` | 高緯度でのズレ（ズーム換算の緯度クランプ） |

### マーカードラッグの偽陽性の罠

前後の比較では検出できません。指を離すとマーカーは最終位置へスナップするので、
「ドラッグ中だけ指に追従しない」という症状は前後比較では見えません。
**指が下りている間**のフレームを見てください。あわせて「離した後に地図がパンできるか」
（スクロール抑止の復元）も確認してください。

タイル描画のプロバイダは初期表示に時間がかかります（TomTom は約 15 秒）。
7 秒で撮ると「マーカーが出ない」に見えます。

---

## 7. 触ってはいけないもの

- **`MapViewState` の共変 `getMapViewHolder()` を消さないこと。** 1 行ですが、消すと
  アプリ側の `state.getMapViewHolder()?.map` が静的型を失いソース非互換になります。
- **`MapViewControllerInterface` にカメラの getter を足さないこと。** 理由はその
  インターフェースのコメントに書いてあります（push 型設計・実測で 65% の無駄）。
- **アプリ向けの公開 API を変えないこと。** `./gradlew apiCheck` が門番です。
  ドライバー実装点は `@InternalMapConductorApi` が付いており、スナップショットから
  除外されるので自由に変えられます。
