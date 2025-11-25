---
title: インストール
---

# インストールとバージョン

ここでは、MapConductor Android SDK を Gradle プロジェクトに追加する方法と、推奨バージョン設定について説明します。

## 依存関係の追加

MapConductor は Maven Central から `mapconductor-bom` と各モジュールを配布しています。BOM を利用することで、すべての MapConductor モジュールのバージョンを一括で管理できます。

```kotlin
val mapconductorVersion = "1.1.0"

dependencies {
    // BOM を利用してバージョンを統一
    implementation(platform("com.mapconductor:mapconductor-bom:$mapconductorVersion"))

    // コアモジュール
    implementation("com.mapconductor:core")

    // 利用する地図プロバイダモジュールを追加
    implementation("com.mapconductor:for-googlemaps")
    // implementation("com.mapconductor:for-mapbox")
    // implementation("com.mapconductor:for-here")
    // implementation("com.mapconductor:for-arcgis")
    // implementation("com.mapconductor:for-maplibre")
}
```

### コアランタイム

#### `mapconductor-core`

共通の機能と基底クラスを含むコアモジュールです。

```kotlin
implementation("com.mapconductor:core")
```

**必須**: すべての MapConductor 利用時に必要です。
**依存先**: Jetpack Compose, Kotlin Coroutines など。

### 地図プロバイダモジュール

必要に応じて、以下のマッププロバイダモジュールを選択して追加します。

#### `mapconductor-for-googlemaps`

Google Maps 連携モジュール。

```kotlin
implementation("com.mapconductor:for-googlemaps")
```

`GoogleMapsView` と `GoogleMapViewStateImpl` を提供します。Google Maps SDK のセットアップが必要です。

#### `mapconductor-for-mapbox`

Mapbox 連携モジュール。

```kotlin
implementation("com.mapconductor:for-mapbox")
```

`MapboxMapView` と `MapboxViewStateImpl` を提供します。Mapbox SDK のセットアップが必要です。

#### `mapconductor-for-here`

HERE Maps 連携モジュール。

```kotlin
implementation("com.mapconductor:for-here")
```

`HereMapView` と `HereViewStateImpl` を提供します。HERE SDK のセットアップが必要です。

#### `mapconductor-for-arcgis`

ArcGIS 連携モジュール。

```kotlin
implementation("com.mapconductor:for-arcgis")
```

`ArcGISMapView` と `ArcGISMapViewStateImpl` を提供します。ArcGIS SDK のセットアップが必要です。

#### `mapconductor-for-maplibre`

MapLibre 連携モジュール。

```kotlin
implementation("com.mapconductor:for-maplibre")
```

`MapLibreMapView` と `MapLibreViewStateImpl` を提供します。タイルやスタイル情報を MapLibre 用に設定する必要があります。

### 実験的モジュール

> **注意**: これらのモジュールは実験的であり、今後のバージョンで仕様が変更される可能性があります。

#### `mapconductor-icons`

プログラムからスタイルを指定できるカスタムマーカーアイコンを提供します。

```kotlin
implementation("com.mapconductor:icons")
```

`CircleIcon`, `FlagIcon` や InfoBubble 系のアイコンコンポーネントが含まれます。

#### `mapconductor-marker-strategy`

パフォーマンス最適化のための高度なマーカー描画戦略（クラスタリングやサーバーサイド戦略など）を提供します。

```kotlin
implementation("com.mapconductor:marker-strategy")
```

#### `mapconductor-marker-native-strategy`

大量のマーカー描画を高速に処理するためのネイティブ実装戦略を提供します。

```kotlin
implementation("com.mapconductor:marker-native-strategy")
```

## Gradle 設定

### プロジェクトレベル `build.gradle` / `build.gradle.kts`

Kotlin や Compose のバージョンは、example app と同様の設定を推奨します。

```kotlin
buildscript {
    ext {
        compose_version = "1.7.1"
        kotlin_version = "1.9.25"
    }
}
```

### モジュールレベル `build.gradle` / `build.gradle.kts`

```kotlin
android {
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        targetSdk = 35
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = compose_version
    }
}
```

### ProGuard / R8 設定

リリースビルドでは、以下のルールを追加してください。

```proguard
# MapConductor Core
-keep class com.mapconductor.core.** { *; }

# Map Provider Specific
-keep class com.mapconductor.googlemaps.** { *; }
-keep class com.mapconductor.mapbox.** { *; }
-keep class com.mapconductor.here.** { *; }
-keep class com.mapconductor.arcgis.** { *; }
-keep class com.mapconductor.maplibre.** { *; }

# Native Strategy (if using)
-keep class com.mapconductor.marker.nativestrategy.** { *; }
```

## バージョンアップ

### 最新バージョンの確認

最新の MapConductor バージョンは以下から確認できます。

1. GitHub Releases: `android-sdk` のリリースページ
2. Maven Central: `com.mapconductor` を検索
3. Gradle プラグイン: 依存関係更新プラグイン など

### BOM を使ったバージョン更新

新しい MapConductor バージョンへ更新する場合は、BOM のバージョンを変更します。

```kotlin
val mapconductorVersion = "1.1.1"

dependencies {
    implementation(platform("com.mapconductor:mapconductor-bom:$mapconductorVersion"))
    implementation("com.mapconductor:core")
    implementation("com.mapconductor:for-googlemaps")
    // 必要なモジュールを追加
}
```

