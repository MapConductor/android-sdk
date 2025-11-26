---
title: Google Maps セットアップ
---

このセクションでは、Google Maps SDK と MapConductor を統合するためのセットアップ手順について説明します。

> **重要**: MapConductor は既存の地図 SDK 上に統一された API レイヤーを提供します。MapConductor の Google Maps 統合を使用する前に、Google Maps SDK を独立してセットアップする必要があります。

## 前提条件

- Android 開発環境
- Google Cloud Console アカウント
- Google Cloud プロジェクトで Maps SDK for Android が有効化されていること

## セットアップ手順

### 1. Google Cloud Console の設定

1. [Google Cloud Console](https://console.cloud.google.com/) にアクセスします
2. 新しいプロジェクトを作成するか、既存のものを選択します
3. **Maps SDK for Android** API を有効にします
4. **認証情報** に移動して API キーを作成します
5. API キーをアプリのパッケージ名と SHA-1 証明書フィンガープリントに制限します

### 2. Gradle の設定

まず、プロジェクトのルート `build.gradle.kts` に Secrets Gradle Plugin を追加します:

```kotlin
// Root build.gradle.kts
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
    // Google Maps SDK (version managed via libs.versions.toml)
    implementation(libs.play.services.maps)

    // MapConductor BOM for version management (v1.1.1)
    implementation(platform("com.mapconductor:mapconductor-bom:1.1.1"))

    // MapConductor modules (versions managed by BOM)
    implementation("com.mapconductor:core")
    implementation("com.mapconductor:for-googlemaps")
}
```

Secrets Gradle Plugin は自動的に `secrets.properties` ファイルを読み取り、ビルド時に値を `AndroidManifest.xml` に注入します。

### 3. Android Manifest の設定

`AndroidManifest.xml` に Google Maps API キーのプレースホルダーを追加します:

```xml
<application>
    <!-- Google Maps API Key -->
    <meta-data
        android:name="com.google.android.geo.API_KEY"
        android:value="${GOOGLE_MAPS_API_KEY}" />

    <!-- Add location permissions -->
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
</application>
```

### 4. API キーの設定

`secrets.properties` ファイルに実際の API キーを追加します（存在しない場合は作成します）:

```properties
# secrets.properties
GOOGLE_MAPS_API_KEY=your_actual_google_maps_api_key_here
```

**重要**:

- `secrets.properties` ファイルをバージョン管理にコミットしないでください。`.gitignore` に追加してください。
- Secrets Gradle Plugin は、このファイルの実際の値で `AndroidManifest.xml` の `${GOOGLE_MAPS_API_KEY}` を自動的に置き換えます。
- CI/CD ビルドの場合は、環境変数または他の安全な方法を使用してこれらの値を提供できます。

### 5. ProGuard / R8 の設定（該当する場合）

ProGuard/R8 を使用している場合は、`proguard-rules.pro` にこれらのルールを追加します:

```proguard
# Google Play Services
-keep class com.google.android.gms.maps.** { *; }
-keep interface com.google.android.gms.maps.** { *; }
-dontwarn com.google.android.gms.**
```

## 検証

Google Maps のセットアップを確認するには:

1. アプリをビルドして実行します
2. エラーなしでマップが表示されることを確認します
3. 位置情報のパーミッションが機能していることを確認します
4. 基本的なマップ操作（ズーム、パン）をテストします

```kotlin
@Composable
fun TestGoogleMaps() {
    val mapState = rememberGoogleMapViewState()

    GoogleMapsView(state = mapState) {
        // If this displays correctly, your setup is working
    }
}
```
![TestGoogleMapsの実行結果](/img/examples/basic-setupGoogleMaps.jpg)

## トラブルシューティング

### よくある問題

**マップが表示されない（グレーの画面）**

- `secrets.properties` の API キーが正しいことを確認してください
- Google Cloud Console で Maps SDK for Android が有効になっていることを確認してください
- API キーの制限がアプリのパッケージ名と SHA-1 と一致していることを確認してください

**ログに API キーエラーが表示される**

- API キーの値を再確認してください
- キーの有効期限が切れていないことを確認してください
- Google Cloud Console で API キーの制限を確認してください

**ビルドエラー**

- Play Services Maps の依存関係がプロジェクト設定と一致していることを確認してください
- `secrets.properties` ファイルが存在し、適切にフォーマットされていることを確認してください
- `secrets.properties` がローカルビルドで誤って無視されていないことを確認してください

## 次のステップ

Google Maps SDK が適切に設定されたら、Map View コンポーネントのドキュメントに記載されているように MapConductor の `GoogleMapsView` コンポーネントを使用できます。
