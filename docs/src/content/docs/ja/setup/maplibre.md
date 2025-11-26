---
title: MapLibre セットアップ
---

このセクションでは、MapLibre Native Android と MapConductor を統合するためのセットアップ手順について説明します。

> **重要**: MapConductor は既存の地図 SDK 上に統一された API レイヤーを提供します。MapConductor の MapLibre 統合を使用する前に、MapLibre Native Android SDK を独立してセットアップする必要があります。

## 前提条件

- Android 開発環境
- タイルサーバー URL（例：OpenStreetMap、Maptiler、または自己ホストタイル）
- スタイル JSON URL または設定

## セットアップ手順

### 1. タイルサーバーの選択

MapLibre は、タイルサーバーを必要とするオープンソースのマッピングライブラリです。いくつかのオプションがあります:

**無料/コミュニティオプション:**
- [OpenStreetMap タイル](https://wiki.openstreetmap.org/wiki/Raster_tile_providers) - 無料ですが、帰属表示が必要です
- [Protomaps](https://protomaps.com/) - セルフホスティングオプション付きのオープンデータ

**商用プロバイダー:**
- [Maptiler](https://www.maptiler.com/) - 無料枠あり
- [Stadia Maps](https://stadiamaps.com/) - 無料枠あり
- [MapTiler Cloud](https://cloud.maptiler.com/) - 充実した無料枠

**セルフホスト:**
- [tileserver-gl](https://github.com/maptiler/tileserver-gl) などのツールを使用して独自のタイルサーバーを実行します

### 2. Gradle の設定

アプリの `build.gradle.kts` に依存関係を追加します:

```kotlin
// App build.gradle.kts
dependencies {
    // MapLibre Native Android SDK (version managed via libs.versions.toml)
    implementation(libs.maplibre.android)

    // MapConductor BOM for version management (v1.1.1)
    implementation(platform("com.mapconductor:mapconductor-bom:1.1.1"))

    // MapConductor modules (versions managed by BOM)
    implementation("com.mapconductor:core")
    implementation("com.mapconductor:for-maplibre")
}
```

MapLibre SDK は Maven Central から利用できます。`settings.gradle.kts` に既に設定されているはずです:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
```

### 3. Android Manifest の設定

`AndroidManifest.xml` に必要なパーミッションを追加します:

```xml
<application>
    <!-- Add internet permission for tile loading -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
</application>
```

### 4. スタイルの設定

MapLibre は、マップの見た目を定義するためにスタイル JSON が必要です。ホストされたスタイルを使用するか、カスタムのものを提供できます。

#### オプション A: ホストされたスタイルの使用（推奨）

Maptiler などの商用タイルプロバイダーを使用する場合:

```kotlin
val styleUrl = "https://api.maptiler.com/maps/streets/style.json?key=YOUR_API_KEY"
```

#### オプション B: OpenStreetMap スタイルの使用

無料の OpenStreetMap ベースのスタイルの場合:

```kotlin
// Example using OSM Liberty style
val styleUrl = "https://tiles.openfreemap.org/styles/liberty"
```

#### オプション C: カスタムスタイル JSON

独自のスタイルをインラインで定義するか、アセットから読み込むこともできます:

```kotlin
val customStyle = """
{
  "version": 8,
  "sources": {
    "osm": {
      "type": "raster",
      "tiles": ["https://tile.openstreetmap.org/{z}/{x}/{y}.png"],
      "tileSize": 256,
      "attribution": "© OpenStreetMap contributors"
    }
  },
  "layers": [{
    "id": "osm",
    "type": "raster",
    "source": "osm"
  }]
}
""".trimIndent()
```

### 5. API キーの設定（必要な場合）

タイルプロバイダーが API キーを必要とする場合（例：Maptiler）、`secrets.properties` に追加します:

```properties
# secrets.properties
MAPLIBRE_API_KEY=your_api_key_here
```

ルート `build.gradle.kts` に Secrets Gradle Plugin を設定します:

```kotlin
plugins {
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1" apply false
}
```

アプリの `build.gradle.kts` にプラグインを適用します:

```kotlin
plugins {
    // ... other plugins
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}
```

**重要**:
- `secrets.properties` ファイルをバージョン管理にコミットしないでください。`.gitignore` に追加してください。
- 多くの MapLibre タイルソースは無料ですが、適切な帰属表示が必要です

### 6. スタイル URL の設定

MapConductor の MapLibre 統合を使用する場合、状態を通じてスタイル URL を設定します:

```kotlin
@Composable
fun MapLibreExample() {
    val mapState = rememberMapLibreMapViewState(
        styleUrl = "https://tiles.openfreemap.org/styles/liberty"
    )

    MapLibreMapView(state = mapState) {
        // Your map content
    }
}
```

## 検証

MapLibre のセットアップを確認するには:

1. アプリをビルドして実行します
2. マップタイルが正しく読み込まれることを確認します
3. マップ操作（ズーム、パン、回転）をテストします
4. タイルプロバイダーによって要求される場合、帰属表示が表示されることを確認します

```kotlin
@Composable
fun TestMapLibre() {
    val mapState = rememberMapLibreMapViewState(
        styleUrl = "https://tiles.openfreemap.org/styles/liberty"
    )

    MapLibreMapView(state = mapState) {
        // If this displays correctly, your setup is working
    }
}
```

## トラブルシューティング

### よくある問題

**マップが表示されない（空白の画面）**

- スタイル URL がアクセス可能で正しいことを確認してください
- タイルの読み込みのためのインターネット接続を確認してください
- スタイル JSON が有効であることを確認してください
- ネットワークまたは解析エラーについて Logcat を確認してください

**タイルが読み込まれない**

- タイルサーバー URL が正しいことを確認してください
- 無料タイルサービスのレート制限を確認してください
- 商用プロバイダーを使用している場合は、適切な API キーを確認してください
- ネットワークパーミッションが付与されていることを確認してください

**帰属表示がない**

- ほとんどのタイルプロバイダーは帰属表示の表示を要求します
- タイルプロバイダーの利用規約を確認してください
- UI に適切な帰属表示テキストを追加してください

**ビルドエラー**

- MapLibre SDK のバージョンが互換性があることを確認してください
- Maven Central リポジトリが設定されていることを確認してください
- 他のマップ SDK との依存関係の競合を確認してください

## 帰属表示の要件

多くのタイルサーバーは帰属表示を要求します。一般的な例:

- **OpenStreetMap**: "© OpenStreetMap contributors"
- **Maptiler**: "© MapTiler © OpenStreetMap contributors"
- **Stadia Maps**: 特定の要件を確認してください

タイルプロバイダーの利用規約に従って、アプリの UI に必要な帰属表示を表示するようにしてください。

## パフォーマンスのヒント

- 可能な場合はベクタータイルを使用して、パフォーマンスの向上と小さなダウンロードサイズを実現します
- オフライン使用のためにタイルのキャッシュを検討してください
- ユースケースに適したズームレベルを使用してください
- 従量制接続を使用している場合は、ネットワーク使用量を監視してください

## 次のステップ

MapLibre が適切に設定されたら、統一 API を使用して MapConductor の `MapLibreMapView` コンポーネントを使用できます。
