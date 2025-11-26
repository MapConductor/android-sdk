---
title: HERE Maps セットアップ
---

# HERE Maps セットアップ

このセクションでは、HERE SDK と MapConductor を統合するためのセットアップ手順について説明します。

> **重要**: MapConductor は既存の地図 SDK 上に統一された API レイヤーを提供します。MapConductor の HERE Maps 統合を使用する前に、HERE SDK を独立してセットアップする必要があります。

## 前提条件

- Android 開発環境
- HERE 開発者アカウント
- HERE API 認証情報

## セットアップ手順

### 1. HERE Developer Portal の設定

1. [HERE Developer アカウント](https://developer.here.com/)にサインアップします
2. [HERE Developer Portal](https://developer.here.com/projects) にアクセスします
3. 新しいプロジェクトを作成します
4. API 認証情報（Access Key ID と Secret）を生成します
5. HERE SDK AAR ファイルをダウンロードします

### 2. HERE SDK のインストール

1. HERE SDK Explore Android（例：このリポジトリで使用されている `4.23.2.0.210004`）をダウンロードします
2. AAR ファイルをプロジェクトの `libs/` ディレクトリに配置します:

   ```text
   libs/heresdk-explore-android-4.23.2.0.210004.aar
   ```

3. AAR ファイル名が Gradle で参照するものと一致していることを確認してください。

### 3. Gradle の設定

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
    // HERE SDK (local AAR)
    implementation(files("${rootProject.projectDir}/libs/heresdk-explore-android-4.23.2.0.210004.aar"))

    // MapConductor BOM for version management (v1.1.1)
    implementation(platform("com.mapconductor:mapconductor-bom:1.1.1"))

    // MapConductor modules (versions managed by BOM)
    implementation("com.mapconductor:core")
    implementation("com.mapconductor:for-here")
}
```

Secrets Gradle Plugin は自動的に `secrets.properties` ファイルを読み取り、`AndroidManifest.xml` に値を注入するために使用できます。

### 4. Android Manifest の設定

`AndroidManifest.xml` に HERE API キーのプレースホルダーを追加します:

```xml
<application>
    <!-- HERE API Credentials -->
    <meta-data
        android:name="HERE_ACCESS_KEY_ID"
        android:value="${HERE_ACCESS_KEY_ID}" />
    <meta-data
        android:name="HERE_ACCESS_KEY_SECRET"
        android:value="${HERE_ACCESS_KEY_SECRET}" />

    <!-- Add internet and location permissions -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
</application>
```

### 5. API 認証情報の設定

`secrets.properties` ファイルに実際の API 認証情報を追加します:

```properties
# secrets.properties
HERE_ACCESS_KEY_ID=your_actual_here_access_key_id
HERE_ACCESS_KEY_SECRET=your_actual_here_access_key_secret
```

**重要**:

- `secrets.properties` ファイルや HERE SDK AAR を公開バージョン管理にコミットしないでください。
- Secrets Gradle Plugin は、このファイルの実際の値で `AndroidManifest.xml` の `${HERE_ACCESS_KEY_ID}` と `${HERE_ACCESS_KEY_SECRET}` を自動的に置き換えます。
- CI/CD ビルドの場合は、環境変数または他の安全な方法を使用してこれらの値を提供できます。

## 検証

HERE Maps のセットアップを確認するには:

1. アプリをビルドして実行します
2. HERE マップが正しく表示されることを確認します
3. マップ操作と HERE 固有の機能をテストします

```kotlin
@Composable
fun TestHERE() {
    val mapState = rememberHereMapViewState()

    HereMapView(state = mapState) {
        // If this displays correctly, your setup is working
    }
}
```
![TestHEREの実行結果](/img/examples/basic-setupHere.jpg)

## トラブルシューティング

### よくある問題

**マップが読み込まれない**

- `secrets.properties` の HERE 認証情報が正しいことを確認してください
- Access Key ID と Secret が適切に設定され、アクティブであることを確認してください

**AAR ファイルが見つからない**

- AAR ファイルが `libs/` ディレクトリにあることを確認してください
- ファイル名が Gradle で参照されているものと完全に一致していることを確認してください

**ビルドエラー**

- HERE SDK のバージョンが Gradle 設定と一致していることを確認してください
- `secrets.properties` に必要なキーが含まれていることを確認してください

## 次のステップ

HERE SDK が適切に設定されたら、統一 API を使用して MapConductor の `HereMapView` コンポーネントを使用できます。
