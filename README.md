# セットアップ

1. リポジトリをクローン
```
git clone https://github.com/MapConductor/android-sdk.git
```

2. https://github.com/MapConductor/map-sdk-credentials/ から`secrets.properties` をプロジェクトルートに追加保存する

3. Android Studioでビルド

# コーディングスタイル

KtLintに従います。ローカルで実行する場合は、下記コマンドを実行します（可能ならば自動修正されます）。

```
./gradlew allLintChecks
```

