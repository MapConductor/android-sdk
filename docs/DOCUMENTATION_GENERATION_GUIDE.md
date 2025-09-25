# MapConductor Documentation Generation Guide

このファイルは、MapConductor Android SDKドキュメントを自動生成するためのLLM指示書です。次回以降、同様の品質でドキュメントを生成する際に使用してください。

## プロジェクト概要

MapConductor Android SDKは、Google Maps、Mapbox、HERE、ArcGISなど複数の地図プロバイダーに対して統一APIを提供するKotlin/Jetpack Composeライブラリです。

### 重要な設計原則
- **統一API**: 全プロバイダーで一貫したインターフェース
- **部分ラッピング**: 完全にラップせず、ネイティブアクセスも可能
- **Jetpack Compose統合**: リアクティブな状態管理
- **Maven配布**: example-appのみ公開、SDKはMaven経由

## ドキュメント構造

### ディレクトリ構成
```
docs/
├── book.toml                 # mdbook設定（英語）
├── src/                     # 英語ドキュメント
│   ├── SUMMARY.md           # ナビゲーション構造
│   ├── introduction.md     # 概要とクイックスタート
│   ├── installation.md     # インストールとバージョン情報
│   ├── provider-compatibility.md # プロバイダー互換性
│   ├── setup/              # SDK固有のセットアップ
│   ├── components/         # コアコンポーネント
│   ├── states/             # 状態管理クラス
│   ├── core/               # 基本クラスとユーティリティ
│   ├── experimental/       # 実験的モジュール
│   ├── examples/           # 使用例
│   └── api/                # API リファレンス
└── build/               # 英語版生成サイト
```

### ナビゲーション順序（SUMMARY.md）
1. **Introduction** - 概要とクイックスタート
2. **Installation and Versions** - インストール手順とバージョン情報
3. **Provider Compatibility** - プロバイダー互換性マトリックス
4. **SDK Setup** - プロバイダー固有のセットアップ
5. **Core Components** - マップコンポーネント
6. **State Classes** - 状態管理
7. **Core Classes** - 基本クラスとユーティリティ
8. **Experimental Modules** - 実験的モジュール
9. **Examples** - 実践例
10. **API Reference** - 詳細リファレンス

## コンテンツ生成指針

### 1. コード例の統一パターン

#### MapViewプレースホルダー
すべてのコード例でMapViewコンポーネントには以下のコメントを追加：

```kotlin
// Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
MapView(state = mapViewState) {
    // コンポーネント
}
```

#### プロバイダー固有の例
各プロバイダーの具体例も含める：

```kotlin
// Google Maps
GoogleMapsView(state = googleMapState) { }

// Mapbox
MapboxMapView(state = mapboxState) { }

// HERE Maps
HereMapView(state = hereState) { }

// ArcGIS
ArcGISMapView(state = arcgisState) { }
```

### 2. 必須セクション構造

各ドキュメントページには以下のセクションを含める：

1. **概要** - クラス/コンポーネントの目的と役割
2. **インターフェース/実装** - APIの定義とクラス構造
3. **基本的な使用法** - シンプルな例
4. **プロパティ/メソッド** - 詳細な説明とパラメーター
5. **カメラパラメーターとの関係** - 該当する場合のみ（位置、ズーム、ベアリング、チルト、パディングとの相互作用）
6. **実践例** - 実用的なCompose使用例
7. **ベストプラクティス** - 推奨事項
8. **注意事項** - 制限や考慮点

### 3. コードスタイル規約

#### Kotlin
- Jetpack Compose慣例に従う
- `remember`、`LaunchedEffect`等の適切な使用
- 型安全性の重視
- null安全性の考慮

#### コメント
- 英語でのドキュメント
- 実用的な説明
- TODO/FIXMEは含めない

### 4. 特別な注意事項

#### MapViewHolder
- ネイティブアクセスの説明
- プロバイダー固有のtypealias
- null安全性の強調
- 初期化タイミングの注意

#### MapCameraPosition と VisibleRegion
- カメラパラメーター（位置、ズーム、ベアリング、チルト、パディング）の相互作用
- VisibleRegionの各プロパティ（bounds, nearLeft, nearRight, farLeft, farRight）の詳細説明
- corner points と bounds の違いと使い分け
- 3D tilt や bearing rotation での可視領域の変化

#### ズームレベル
- Google Maps「おおよそ」準拠と明記
- プロバイダー間の違いを説明
- 実用的な範囲の例示

#### 地理計算
- Sphericalユーティリティの実例
- 精度vs性能のトレードオフ
- WGS84座標系の説明

#### 実験的モジュール
- ⚠️ 実験的警告の必須表示
- プロダクション使用時の注意事項
- モジュール固有の制限事項
- インストール手順とネイティブライブラリ要件

#### プロバイダー互換性
- 機能対応表の維持（予定機能から3Dモデルレンダリングは除外）
- 非対応機能の代替手段
- ランタイム検出パターン
- Graceful Degradation実装例

#### State変換とコンストラクタ一貫性
- プロバイダー間でのStateクラスコンストラクタの一貫性維持
- PolygonState → PolylineState変換時のextraプロパティ引き継ぎ
- 拡張関数でのパラメータ漏れ防止
- State変換処理でのnull安全性確保

#### バージョン管理
- 現在のバージョン情報（1.0.0）
- 統一バージョニング戦略の詳細説明：
  - メジャーバージョンの全モジュール統一
  - コアモジュール: 地図SDK重要更新でマイナーインクリメント、修正でパッチインクリメント
  - 実験的モジュール: 新機能・アイコン追加でメジャーインクリメント、修正でパッチインクリメント
- 互換性マトリックス
- アップグレード手順

## 生成手順

### ステップ1: 既存構造の確認
```bash
# 現在のドキュメント構造を確認
cat docs/src/SUMMARY.md

# 既存ファイルのサンプル読み込み
head -50 docs/src/components/marker.md
```

### ステップ2: ソースコード分析
```bash
# 対象クラスの検索
find . -name "*.kt" -exec grep -l "ClassName" {} \;

# インターフェース/実装の確認
grep -n "interface\|class\|data class" target_file.kt
```

### ステップ3: ドキュメント生成
1. **ソースコード読み込み** - 対象クラスの実装を理解
2. **example-app参照** - 実際の使用パターンを確認
3. **既存ドキュメント参照** - 一貫したスタイルの維持
4. **コンテンツ作成** - 上記指針に従った内容生成

### ステップ4: 品質保証
1. **コード例の動作確認** - 構文エラーの回避
2. **リンク整合性** - 内部リンクの確認
3. **スタイル統一** - 既存ドキュメントとの一貫性
4. **mdbook ビルド** - 生成エラーの確認

## 自動化用のプロンプトテンプレート

### 基本コンポーネントドキュメント生成

```
MapConductor Android SDKの[対象コンポーネント名]について、以下の要件でドキュメントを生成してください：

1. **ソースコード分析**
   - [対象パス]のコードを読み込み、APIを理解
   - example-appでの使用例を確認

2. **ドキュメント生成**
   - docs/src/[カテゴリ]/[ファイル名].mdに作成
   - 既存ドキュメントのスタイルを参考
   - MapViewプレースホルダーパターンを使用
   - プロバイダー固有の例も含める

3. **必須セクション**
   - 概要とインターフェース定義
   - 基本的な使用法
   - 詳細なプロパティ/メソッド説明
   - 実践的なCompose例（3-5個）
   - ベストプラクティスと注意事項

4. **更新作業**
   - SUMMARY.mdのナビゲーション更新
   - 必要に応じてintroduction.mdも更新
   - mdbook buildで静的サイト生成

5. **品質基準**
   - 英語ドキュメント
   - Jetpack Compose慣例準拠
   - null安全性の考慮
   - 実用的で動作する例のみ

[対象コンポーネント名]は統一APIの[役割]を担い、[特別な考慮事項があれば記載]です。
```

### 実験的モジュールドキュメント生成

```
MapConductor Android SDKの実験的モジュール[モジュール名]について、以下の要件でドキュメントを生成してください：

1. **実験的モジュール要件**
   - ⚠️ 実験的警告を冒頭に必ず表示
   - プロダクション使用時の注意事項を明記
   - API変更の可能性を言及
   - 安定版との違いを説明

2. **インストールと設定**
   - build.gradleの dependency 追加方法
   - ネイティブライブラリ要件（該当する場合）
   - 最小SDK要件
   - ProGuard設定（必要な場合）

3. **パフォーマンス特性**
   - メモリ使用量の比較
   - 処理性能の特徴
   - 適用場面の推奨

4. **制限事項とトラブルシューティング**
   - プラットフォーム制限
   - 互換性問題
   - メモリリーク防止
   - リソース管理

場所: docs/src/experimental/[モジュール名].md
```

### 日本語ドキュメント生成

```
MapConductor Android SDKの[対象コンポーネント名]について、日本語ドキュメントを生成してください：

1. **言語固有の要件**
   - docs/ja/src/[カテゴリ]/[ファイル名].mdに作成
   - 日本語の自然な表現を使用
   - 技術用語は適切に日本語化（例：「マーカー」「コンポーネント」）
   - コード例のコメントは日本語に翻訳

2. **構造の一貫性**
   - 英語版と同じセクション構造を維持
   - SUMMARY.mdの日本語ナビゲーション更新
   - MapViewプレースホルダーパターンも日本語コメントで

3. **翻訳品質**
   - 技術的正確性を保持
   - 日本の開発者にとって理解しやすい表現
   - カタカナ・漢字・ひらがなの適切な使い分け

4. **ビルドとテスト**
   - ja/ディレクトリでmdbook buildを実行
   - 日本語表示の確認
   - リンクの動作確認

場所: docs/ja/src/[カテゴリ]/[ファイル名].md
```

### ドキュメント品質確認

```
MapConductor Android SDKドキュメントの品質確認を実行してください：

1. **英語版確認**
   - docs/ディレクトリでmdbook buildを実行
   - 全ページのビルド成功を確認
   - ナビゲーションリンクの動作確認
   - コード例の構文チェック

2. **日本語版確認**
   - docs/ja/ディレクトリでmdbook buildを実行
   - 全ページのビルド成功を確認
   - 日本語文字の正しい表示確認
   - 翻訳品質の一貫性確認

3. **多言語対応確認**
   - 両言語版の構造一貫性
   - 相互リンクの動作確認
   - コンテンツの技術的正確性
   - スタイルガイドライン遵守

4. **最終検証**
   - 全モジュールのドキュメント網羅性
   - プロバイダー互換性情報の正確性
   - 実験的モジュール警告の適切な表示
   - バージョン情報の最新性
```

### プロバイダー互換性更新

```
MapConductorの新機能[機能名]のプロバイダー対応状況について、以下を更新してください：

1. **対応表更新**
   - docs/src/provider-compatibility.mdの表を更新
   - docs/ja/src/provider-compatibility.mdの日本語表も更新
   - 各プロバイダーの対応状況（✅/❌/⚠️）
   - 技術的制限の理由を説明

2. **代替手段の提供**
   - 非対応プロバイダーでの代替実装
   - Graceful Degradation パターン
   - ランタイム検出コード例

3. **ベストプラクティス更新**
   - プロバイダー選択ガイダンス
   - 互換性テスト方法
   - マイグレーション戦略

4. **多言語対応**
   - 英語版と日本語版の両方を更新
   - 技術的内容の一貫性を保持
```

## テンプレートファイル

### 基本ドキュメントテンプレート

```markdown
# [コンポーネント名]

[コンポーネントの概要と目的]

## Interface and Implementation

### [インターフェース名]

```kotlin
interface ComponentName {
    // プロパティとメソッド
}
```

### [実装クラス名]

```kotlin
data class ComponentNameImpl(
    // 実装詳細
) : ComponentName
```

## Basic Usage

```kotlin
@Composable
fun BasicExample() {
    // Replace MapView with your chosen map provider, such as GoogleMapsView, MapboxMapView
    MapView(state = mapViewState) {
        ComponentName(
            // 基本的な使用法
        )
    }
}
```

## Properties and Methods

### [プロパティ名]
- **説明**: [詳細説明]
- **型**: [型情報]
- **デフォルト**: [デフォルト値]

## Practical Examples

### [使用例1のタイトル]

```kotlin
@Composable
fun Example1() {
    // 実践的な例
}
```

### [使用例2のタイトル]

```kotlin
@Composable
fun Example2() {
    // 実践的な例
}
```

## Best Practices

1. **項目1**: 説明
2. **項目2**: 説明

## Common Pitfalls

1. **注意点1**: 説明
2. **注意点2**: 説明
```

## チェックリスト

生成前の確認事項：
- [ ] 対象クラス/コンポーネントのソースコード確認済み
- [ ] example-appでの使用例確認済み
- [ ] 既存ドキュメントのスタイル把握済み
- [ ] 必要なディレクトリが存在する

生成後の確認事項：
- [ ] コード例に構文エラーがない
- [ ] MapViewプレースホルダーコメントが含まれている
- [ ] プロバイダー固有の例も含まれている
- [ ] SUMMARY.mdが更新されている
- [ ] mdbook buildが成功する
- [ ] リンクが正しく機能する

## 更新履歴

- 2024-09-24: 初版作成
  - 基本ドキュメント構造
  - Core Classes（GeoPoint、GeoRectBounds）
  - MapViewプレースホルダーパターン

- 2024-09-24: 大幅拡張
  - Core Classes拡張（MapCameraPosition、Spherical Utilities、MapViewHolder、Zoom Levels）
  - Experimental Modules追加（Icons、Marker Strategy、Marker Native Strategy）
  - Provider Compatibility追加（機能対応表、代替手段）
  - Installation and Versions追加（バージョン管理、インストール手順）
  - ナビゲーション構造の最終化（10セクション構成）

- 2024-09-24: 日本語ドキュメント対応
  - 日本語ドキュメント構造の設計（docs/ja/）
  - 日本語版book.toml設定
  - 日本語版コアページ作成（introduction、installation、provider-compatibility）
  - 多言語対応プロンプトテンプレート追加
  - 日本語固有の翻訳ガイドライン追加
  - ドキュメント品質確認プロセスの追加
  - 完全な多言語対応ワークフローの確立

- 2024-09-24: コンテンツ精製とバージョニング戦略明確化
  - Provider Compatibilityから3Dモデルレンダリングを削除（英語・日本語版）
  - Installation and Versionsにバージョニング戦略の詳細説明を追加
  - 統一バージョニングアプローチの文書化（メジャー統一、モジュール別インクリメント規則）
  - ドキュメント生成ガイドにバージョニング要件を追加

- 2025-01-XX: VisibleRegion クラス詳細ドキュメント化
  - MapCameraPositionドキュメントにVisibleRegionの詳細説明を追加
  - カメラパラメーター（位置、ズーム、ベアリング、チルト、パディング）との関係性を詳述
  - corner points vs bounds の使い分けガイドライン追加
  - ドキュメント生成ガイドにMapCameraPosition/VisibleRegion特有の注意事項を追加

- 2025-01-XX: State変換とプロバイダー一貫性強化
  - MapboxPolygonConductor.ktでのPolylineState変換エラー修正
  - State変換処理でのextraプロパティ引き継ぎ要件を明確化
  - プロバイダー間のコンストラクタ一貫性に関するガイドライン追加
  - PolygonState → PolylineState変換パターンの標準化

---

この指示書に従うことで、一貫した品質のMapConductorドキュメントを効率的に生成できます。
