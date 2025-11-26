---
title: ArcGIS Maps セットアップ
---

このセクションでは、ArcGIS Maps SDK と MapConductor を統合するためのセットアップ手順について説明します。

> **重要**: MapConductor は既存の地図 SDK 上に統一された API レイヤーを提供します。MapConductor の ArcGIS 統合を使用する前に、ArcGIS Maps SDK を独立してセットアップする必要があります。

## 前提条件

- Android 開発環境
- ArcGIS 開発者アカウント
- ArcGIS API キー

## セットアップ手順

### 1. ArcGIS Developer Dashboard の設定

1. [ArcGIS Developer アカウント](https://developers.arcgis.com/)にサインアップします
2. [ArcGIS Developer Dashboard](https://developers.arcgis.com/dashboard)にアクセスします
3. 新しいアプリケーションを作成するか、既存のものを選択します
4. 適切なスコープで API キーを生成します
5. アプリで使用するために API キーをメモします

### 2. Gradle の設定

Esri リポジトリが設定されていることを確認します（このリポジトリの `settings.gradle.kts` には既に含まれています）:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        // Esri public repository for ArcGIS Maps SDK
        maven {
            url = uri("https://esri.jfrog.io/artifactory/arcgis")
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
    // ArcGIS Maps SDK (version managed via libs.versions.toml)
    implementation(platform(libs.arcgis.maps.kotlin.toolkit.bom))
    implementation(libs.arcgis.maps.kotlin)
    implementation(libs.arcgis.maps.kotlin.toolkit.geoview.compose)
    implementation(libs.arcgis.maps.kotlin.toolkit.authentication)

    // MapConductor BOM for version management (v1.1.1)
    implementation(platform("com.mapconductor:mapconductor-bom:1.1.1"))

    // MapConductor modules (versions managed by BOM)
    implementation("com.mapconductor:core")
    implementation("com.mapconductor:for-arcgis")
}
```

Secrets Gradle Plugin は `secrets.properties` ファイルを読み取り、ビルド時に値を `AndroidManifest.xml` に注入できます。

### 3. Android Manifest の設定

`AndroidManifest.xml` に ArcGIS API キーのプレースホルダーを追加します:

```xml
<application>
    <!-- ArcGIS API Key -->
    <meta-data
        android:name="ARCGIS_API_KEY"
        android:value="${ARCGIS_API_KEY}" />

    <!-- Add internet and location permissions -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
</application>
```

### 4. API キーの設定

`secrets.properties` ファイルに実際の API キーを追加します:

```properties
# secrets.properties
ARCGIS_API_KEY=your_actual_arcgis_api_key_here
```

**重要**:

- `secrets.properties` ファイルをバージョン管理にコミットしないでください。`.gitignore` に追加してください。
- Secrets Gradle Plugin は、このファイルの実際の値で `AndroidManifest.xml` の `${ARCGIS_API_KEY}` を自動的に置き換えます。

### 5. ライセンスの設定

ArcGIS SDK は適切なライセンス設定が必要です。アプリケーションでライセンスを設定します:

```kotlin
// In your Application class or MainActivity
ArcGISEnvironment.setApiKey("your_api_key_here")

// For development/testing
// ArcGISEnvironment.setLicense("your_license_key_here") // Optional for basic use
```

本番アプリの場合は、[Named User License](https://developers.arcgis.com/documentation/security-and-authentication/licensing/) または [License Key](https://developers.arcgis.com/documentation/security-and-authentication/licensing/) の使用を検討してください。

## 検証

ArcGIS のセットアップを確認するには:

1. アプリをビルドして実行します
2. ArcGIS マップが正しく表示されることを確認します
3. GIS 固有の機能をテストします（使用している場合）
4. 認証が機能していることを確認します

```kotlin
@Composable
fun TestArcGIS() {
    val mapState = rememberArcGISMapViewState()

    ArcGISMapView(state = mapState) {
        // If this displays correctly, your setup is working
    }
}
```
![TestArcGISの実行結果](/img/examples/basic-setupArcgis.jpg)

## トラブルシューティング

### よくある問題

**マップが読み込まれない**

- `secrets.properties` の API キーが正しいことを確認してください
- API キーが ArcGIS Dashboard で適切なスコープを持っていることを確認してください

**ライセンスエラー**

- マップを使用する前に `ArcGISEnvironment.setApiKey()` が呼び出されていることを確認してください
- 本番アプリの場合は、Esri のドキュメントに従って適切なライセンスを設定してください

**ビルドエラー**

- ArcGIS SDK の座標がプロジェクト設定と一致していることを確認してください
- BOM がバージョン管理に使用されていることを確認してください
- Compose ツールキットの依存関係が含まれていることを確認してください

## 次のステップ

ArcGIS Maps SDK が適切に設定されたら、統一 API を使用して MapConductor の `ArcGISMapView` コンポーネントを使用できます。
