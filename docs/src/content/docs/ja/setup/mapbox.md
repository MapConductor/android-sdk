---
title: Mapbox セットアップ
---

このセクションでは、Mapbox Maps SDK と MapConductor を統合するためのセットアップ手順について説明します。

> **重要**: MapConductor は既存の地図 SDK 上に統一された API レイヤーを提供します。MapConductor の Mapbox 統合を使用する前に、Mapbox Maps SDK を独立してセットアップする必要があります。

## 前提条件

- Android 開発環境
- Mapbox アカウント
- Mapbox アクセストークン

## セットアップ手順

### 1. Mapbox アカウントの設定

1. [Mapbox アカウント](https://www.mapbox.com/)にサインアップします
2. [Mapbox Account Dashboard](https://account.mapbox.com/) にアクセスします
3. **Access tokens** に移動します
4. 新しいアクセストークンを作成するか、デフォルトの公開トークンを使用します
5. アプリで使用するためにアクセストークンをコピーします

### 2. Gradle の設定

Mapbox Maven リポジトリが設定されていることを確認します（このリポジトリでは `settings.gradle.kts` に既に含まれています）:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        // Mapbox Maven repository
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
        }
    }
}
```

プロジェクトのルート `build.gradle.kts` に Secrets Gradle Plugin を追加します:

```kotlin
plugins {
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1" apply false
}
```

次に、アプリの `build.gradle.kts` に依存関係を追加し、プラグインを適用します:

```kotlin
// App build.gradle.kts
plugins {
    // ... other plugins
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

dependencies {
    // Mapbox Maps SDK (NDK27 variant, version managed via libs.versions.toml)
    implementation(libs.mapbox.android)

    // MapConductor BOM for version management (v1.1.1)
    implementation(platform("com.mapconductor:mapconductor-bom:1.1.1"))

    // MapConductor modules (versions managed by BOM)
    implementation("com.mapconductor:core")
    implementation("com.mapconductor:for-mapbox")
}
```

Secrets Gradle Plugin は自動的に `secrets.properties` ファイルを読み取り、ビルド時に値を `AndroidManifest.xml` に注入します。

### 3. Android Manifest の設定

`AndroidManifest.xml` に Mapbox アクセストークンのプレースホルダーを追加します:

```xml
<application>
    <!-- Mapbox Access Token -->
    <meta-data
        android:name="MAPBOX_ACCESS_TOKEN"
        android:value="${MAPBOX_ACCESS_TOKEN}" />

    <!-- Add internet and location permissions -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
</application>
```

### 4. アクセストークンの設定

`secrets.properties` ファイルに実際のアクセストークンを追加します:

```properties
# secrets.properties
MAPBOX_ACCESS_TOKEN=your_actual_mapbox_access_token_here
```

**重要**:

- `secrets.properties` ファイルをバージョン管理にコミットしないでください。`.gitignore` に追加してください。
- Secrets Gradle Plugin は、このファイルの実際の値で `AndroidManifest.xml` の `${MAPBOX_ACCESS_TOKEN}` を自動的に置き換えます。
- CI/CD ビルドの場合は、環境変数または他の安全な方法を使用してこれらの値を提供できます。

### 5. スタイルの設定

Mapbox はカスタムマップスタイルを許可します。組み込みのスタイルを使用するか、カスタムスタイルを作成できます:

```kotlin
// Using built-in styles
val mapStyle = Style.MAPBOX_STREETS
// or Style.SATELLITE, Style.OUTDOORS, etc.

// Using custom style URL
val customStyle = "mapbox://styles/your-username/your-style-id"
```

## 検証

Mapbox のセットアップを確認するには:

1. アプリをビルドして実行します
2. マップが Mapbox スタイルで表示されることを確認します
3. マップ操作（ズーム、パン、回転）をテストします
4. カスタムスタイルが正しく読み込まれることを確認します（使用している場合）

```kotlin
@Composable
fun TestMapbox() {
    val mapState = rememberMapboxMapViewState()

    MapboxMapView(state = mapState) {
        // If this displays correctly, your setup is working
    }
}
```

## トラブルシューティング

### よくある問題

**マップが表示されない（空白の画面）**

- `secrets.properties` のアクセストークンが正しいことを確認してください
- トークンの有効期限が切れていないことを確認してください
- タイルの読み込みのためのインターネット接続を確認してください

**アクセストークンエラー**

- トークンの値を再確認してください（通常は `pk.` で始まります）
- Mapbox アカウントダッシュボードでトークンのパーミッションを確認してください
- トークンスコープに必要なパーミッションが含まれていることを確認してください

**ビルドエラー**

- Mapbox SDK の座標がプロジェクト設定と一致していることを確認してください
- NDK27 バリアントが使用されていることを確認してください
- `secrets.properties` ファイルが存在し、トークンが含まれていることを確認してください

## 次のステップ

Mapbox Maps SDK が適切に設定されたら、統一 API を使用して MapConductor の `MapboxMapView` コンポーネントを使用できます。
