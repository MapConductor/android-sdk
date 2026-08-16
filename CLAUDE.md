# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 守っている前提（変えないこと）

- **同一文法・同一動作・同一実装。** 少人数で保守するので「3 プラットフォーム
  （android-sdk / ios-sdk / react-sdk）が同じ構造で書かれている」ことが最優先。
  地図 SDK ごとの差でやむを得ず変える場合も、構造は可能な限り揃える。
- **アプリ開発者向けの公開 API は凍結。** 見た目の変更と、後方互換な省略可能引数の
  追加は可。それ以外は `apiCheck` が落とす（下記）。
- **SDK は下げない。** 4.0 が出ているのに 3.3.0 を使う、はしない。
- **基本的にすべての機能は一度動くことを確認してある。** 壊れて見えたら、まず
  自分の変更を疑う。
- **端末/エミュレータでの確認を必ず行う。** スクリーンショットだけで済ませない。
- **API キーを git 管理下のファイルに書かない。** プレースホルダだけを置き、実値は
  `secrets.properties` / `local.properties`（git 管理外）から流し込む。

## リポジトリ構成

Gradle のマルチモジュール。各モジュールは独立した Maven 成果物として publish する。

```
android-sdk-core/       # プロバイダ非依存のコア（com.mapconductor:core）
android-sdk-compose/    # Jetpack Compose 層（com.mapconductor:compose）
android-for-<sdk>/      # プロバイダ実装（googlemaps / maplibre / mapbox / arcgis /
                        #   here / longdo / maptiler / mappls / tomtom /
                        #   openmobilemaps / template）
android-heatmap/        # 拡張: ヒートマップ
android-marker-clustering/ # 拡張: マーカークラスタリング
android-geojson-layer/  # 拡張: GeoJSON タイルレイヤ
android-kml/            # 拡張: KML
android-icons*/         # アイコン集（jp / us / weather）
gradle/api-surface.gradle.kts  # 公開 API サーフェスの門番
```

`android-for-template` は**実在の地図 SDK ではなく代役を描くだけ**の雛形。
新しいプロバイダを足すときの出発点で、アプリ開発者向けの選択肢ではない。

## ビルドと検証

```bash
# Android Studio 同梱 JDK を明示しないと通らないことがある
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"

./gradlew :android-for-maplibre:compileDebugKotlin
./gradlew apiCheck          # 公開 API の差分検査（後述）
./gradlew apiDump           # 意図した変更ならベースラインを更新
```

### 公開 API サーフェス（apiDump / apiCheck）

`gradle/api-surface.gradle.kts` が release AAR の `classes.jar` を `javap` で読み、
`<module>/api/<module>.api` にテキストで記録する。`binary-compatibility-validator` は
`com.android.library` に対応していない（`apiDump` タスクが 1 つも生成されない）ため
この方式を採っている。

`@InternalMapConductorApi` を付けた宣言は記録しない。**ドライバー実装点には必ず
付けること。** 付け忘れるとアプリ向けの凍結 API に載ってしまう。
iOS の `@_spi(MapConductorDriver)`、React の `/** @internal */` に対応する。

## React Native から使うときの結合（重要）

`react-sdk/reactnative-for-*` はこのリポジトリを **MavenLocal 経由**で参照する。
コピーはしない。

```bash
# android-sdk を変更したら必ず publish してから RN 側を確認する
./gradlew :android-for-maplibre:compileDebugKotlin :android-for-maplibre:publishToMavenLocal
```

publish を忘れると **RN 側は古い実装のまま静かに動く。** 「直したのに変わらない」の
ほとんどはこれ。各 RN パッケージの `android/build.gradle` にある
`com.mapconductor:*` のバージョンと MavenLocal を揃えること。

Kotlin は 2.3.20 に固定してある（上げると pika の上限に当たる）。

## アーキテクチャ

### コントローラは Compose と RN で共有する

プロバイダごとに `createXxxViewController()` というファクトリを持ち、
**Compose 版も RN 版も同じファクトリを通る。**

```
Compose: XxxMapView.kt          ─┐
                                 ├→ createXxxViewController() → XxxViewController
RN:      XxxMapViewWrapper.kt   ─┘
```

新しいプロバイダを足すときも、Compose だけで動く経路を作らないこと。
経路が 2 本あると、片方でしか露見しない不具合が入る（下の「タイル判定」参照）。

### マーカーは RN で Compose 層を通さない

RN のマーカーは意図的に Compose を経由しない（大量マーカーで固まるため）。
Compose ホストは、ヒートマップやマーカークラスタリングなど **Compose が要る拡張**
だけに限定してある。

## 踏んだ落とし穴

- **タイル判定を自分で書かない。**
  `controller.useMarkerLayer = markerTiling != null` と手書きすると、RN の共通基底が
  常に非 null の `MarkerTilingOptions` を渡すので **全マーカーが消える**。
  Compose では各ページが null を渡すので露見しない。
  `MarkerTilingOptions.shouldUseTiles` /
  `shouldUseMarkerLayer(count)`（`options.enabled && count >= options.minMarkerCount`）
  を呼ぶこと。android-for-longdo と android-for-maptiler が同じ穴を順に踏んだ。

- **デザイン ID の受け渡し方はプロバイダごとに違う。**
  `id` / `getValue()` / スタイル URI のどれが 3 プラットフォームで一致するかは
  プロバイダによる（Longdo・MapTiler は `id`、Mapbox はスタイル URI）。
  **web・android・iOS の 3 つを実際に読んでから決めること。** iOS だけ見て web も
  同じと決めつけて、Mapbox のタイルが真っ白になったことがある。

- **`map.animateCamera(update, duration)` は素の ease ではない。**
  MapLibre / Mappls では `Transform.animateCamera` → `NativeMap.flyTo`、つまり
  van Wijk のズームアークに落ちる。ease が要るなら `easeCamera`。
  名前から素の ease だと決めつけると iOS 側の対向実装を誤る。

## 対応するリポジトリ

`ios-sdk`（Swift）と `react-sdk`（TypeScript + RN ブリッジ）が兄弟として並ぶ前提。
RN の `:path` / MavenLocal 参照はこの配置に依存している。
