---
title: モジュール構成
---

# モジュール構成

MapConductor Android SDK は複数のモジュールから構成されており、必要な機能だけを選択して利用できます。このページでは、各モジュールの役割と依存関係の概要を説明します。

## BOM とバージョン管理

すべてのモジュールは `mapconductor-bom` でバージョンを一括管理できます。BOM を利用することで、異なるモジュール間でバージョンの不整合が起きるのを防げます。

```kotlin
val mapconductorVersion = "1.1.0"

dependencies {
    implementation(platform("com.mapconductor:mapconductor-bom:$mapconductorVersion"))
    implementation("com.mapconductor:core")
    implementation("com.mapconductor:for-googlemaps")
    // 必要に応じて他のモジュールを追加
}
```

## コアランタイム

### `mapconductor-core`

共通の抽象クラスやユーティリティを提供する中核モジュールです。

- 地理座標やカメラ位置などのコアクラス
- MapView の状態管理とイベントハンドリング
- マーカーや円などのオーバーレイコンポーネントの共通インターフェース

ほとんどの機能で必須となるモジュールです。

## 地図プロバイダモジュール

各地図プロバイダごとに、`mapconductor-core` を実装するモジュールが用意されています。

- `mapconductor-for-googlemaps`
- `mapconductor-for-mapbox`
- `mapconductor-for-here`
- `mapconductor-for-arcgis`
- `mapconductor-for-maplibre`

これらのモジュールは、共通 API に準拠したマップビューやステートクラスを提供し、Google Maps や Mapbox などの実際の SDK と橋渡しをします。

## サポートモジュール

### `mapconductor-icons`

カスタムマーカーアイコンを提供するモジュールです。円形アイコンやフラグアイコンなど、地図上の情報を視覚的に表現するためのコンポーネントが含まれています。

## 実験的モジュール

> **注意**: 実験的モジュールは API が変更される可能性があります。本番環境で利用する場合はリリースノートを確認してください。

### `mapconductor-marker-strategy`

大量のマーカーを効率的に扱うための戦略パターンを提供します。クラスタリングやネイティブレンダリングなど、パフォーマンスを重視した実装が含まれます。

### `mapconductor-marker-native-strategy`

ネイティブ側の最適化を活用したマーカー描画戦略を提供します。大量のマーカーを表示するユースケースを想定しています。

## 例示アプリ

リポジトリには、MapConductor の主要機能を確認できる例示アプリが含まれています。

- `example-app`: 複数のプロバイダで MapConductor を利用するサンプル

セットアップ方法やビルド手順は、[インストール](/ja/installation/) および README を参照してください。

