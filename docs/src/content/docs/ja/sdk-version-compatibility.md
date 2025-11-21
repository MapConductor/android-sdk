---
title: SDK バージョン互換性
---

# SDK バージョン互換性

このページでは、MapConductor と各地図 SDK、ならびに Android / Kotlin / Compose のバージョン互換性について説明します。

## 対応バージョンの概要

MapConductor v1.1.0 は、以下の環境を前提に設計されています。

- **Android**: 最低 SDK バージョン 26、ターゲット SDK 35
- **Kotlin**: 1.9.25
- **Jetpack Compose**: 1.7.1
- **Java**: 17

各モジュールは、これらのバージョンを前提としてテストされています。異なるバージョンを利用する場合は、互換性に注意してください。

## Gradle 設定の一例

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
}
```

## MapConductor と地図 SDK

MapConductor は各地図 SDK の上に構築されており、以下のような組み合わせでの動作を想定しています（実際の最小・推奨バージョンは、各地図 SDK の公式ドキュメントも合わせて参照してください）。

- Google Maps Android SDK（最新の安定版）
- Mapbox Maps SDK for Android
- HERE SDK for Android
- ArcGIS Maps SDK for Kotlin / Android
- MapLibre Native / MapLibre GL for Android

MapConductor 側では、コンパイルやランタイムでの互換性が保てるよう、バージョンの組み合わせを確認しながら開発しています。

## 依存関係の更新ポリシー

- 重大なセキュリティアップデートや非互換な変更がある場合、マイナー/メジャーリリースで追随
- 大きな API 変更が必要な場合は、リリースノートで明示

最新の情報は、GitHub のリリースノートや Maven Central のバージョン履歴を確認してください。

