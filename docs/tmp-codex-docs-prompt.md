# MapConductor Android SDK — 新機能ドキュメント生成タスク

作業ディレクトリ: `/Users/masashi/android-sdk/docs`

以下の5つの新機能について、**日本語のみ**のドキュメントを Astro/Starlight 形式で生成してください。
各機能について `.astro` コンポーネントファイルと `.mdx` ページ（新規または更新）を作成します。

---

## 前提: Astro コンポーネントの書き方

既存ファイル `src/components/api/event-handlers/OnMapClickSignature.astro` の形式:
```astro
---
import { Code } from '@astrojs/starlight/components';
const code = `onMapClick: OnMapEventHandler? = null`;
---
<Code code={code} lang="kotlin" />
```

既存ファイル `src/components/api/event-handlers/OnMapClickExample.astro` の形式:
```astro
---
import { Code } from '@astrojs/starlight/components';
interface Props {
  commentForPrint?: string;
}
const { commentForPrint = "Map clicked at:" } = Astro.props;
const code = `MapView(
    onMapClick = { geoPoint ->
        println("${commentForPrint} \${geoPoint.latitude}, \${geoPoint.longitude}")
    }
) { }`;
---
<Code code={code} lang="kotlin" />
```

既存ファイル `src/components/api/event-handlers/MapViewEventHandlersSignature.astro` を**更新**して `onMapLongClick` を追加してください（既存の内容に `onMapLongClick: OnMapEventHandler? = null,` を追加する）。

MDX ページは `src/content/docs/ja/` 以下にあります。

---

## 機能1: `onMapLongClick` — 全 MapView への長押しイベント追加

### 概要
`onMapLongClick: OnMapEventHandler? = null` が全 MapView（GoogleMapView, ArcGISMapView, ArcGISMapView2D, MapboxMapView, MapLibreMapView, HereMapView）に追加されました。シグネチャは `onMapClick` と同じです。

### 作成するファイル

**`src/components/api/event-handlers/OnMapLongClickSignature.astro`**:
```astro
---
import { Code } from '@astrojs/starlight/components';
const code = `onMapLongClick: OnMapEventHandler? = null`;
---
<Code code={code} lang="kotlin" />
```

**`src/components/api/event-handlers/OnMapLongClickExample.astro`**:
Props: `commentForPrint: string = "Map long-clicked at:"`
コード例:
```kotlin
MapView(
    onMapLongClick = { geoPoint ->
        println("${commentForPrint} ${geoPoint.latitude}, ${geoPoint.longitude}")
    }
) {
    // ...
}
```

### 更新するファイル
`src/content/docs/ja/event/event-handlers.mdx` に `## マップインタラクション` セクションへ `onMapLongClick` のサブセクションを追加:
- 既存の `onMapClick` セクションの直後に追加
- `<OnMapLongClickSignature />` と `<OnMapLongClickExample commentForPrint="長押しした位置:" />` を使う
- import 文も追加する
- 説明: 「ユーザーがマップ（オーバーレイではない部分）を長押ししたときに呼び出されます。」
- イベントデータ: `GeoPointInterface` — 長押しされた地理座標

---

## 機能2: `ArcGISMapView2D` — ArcGIS 2D フラットマップ

### 概要
`ArcGISMapView2D` は ArcGIS の 2D フラット地図ビューです。通常の `ArcGISMapView`（3D SceneView ベース）とは異なり、ArcGIS の `MapView`（2D）を使用します。tilt や 3D 表示はサポートされません（tilt は常に 0 に固定）。

### Composable シグネチャ（`android-for-arcgis` モジュール）
```kotlin
@Composable
fun ArcGISMapView2D(
    state: ArcGISMapViewState,
    modifier: Modifier = Modifier,
    markerTiling: MarkerTilingOptions? = null,
    sdkInitialize: (suspend (android.content.Context) -> Boolean)? = null,
    onMapLoaded: OnMapLoadedHandler? = null,
    onCameraMoveStart: OnCameraMoveHandler? = null,
    onCameraMove: OnCameraMoveHandler? = null,
    onCameraMoveEnd: OnCameraMoveHandler? = null,
    onMapClick: OnMapEventHandler? = null,
    onMapLongClick: OnMapEventHandler? = null,
    content: (@Composable ArcGISMapViewScope.() -> Unit)? = null,
)
```

通常の `ArcGISMapView` との違い:
- 3D SceneView の代わりに 2D MapView を使用
- tilt は常に 0（3D 傾斜なし）
- 建物の 3D 表示なし
- 軽量で 2D マップに特化
- `ArcGISMapViewState` は共用（`rememberArcGISMapViewState()` で生成）

### 作成するファイル

**`src/components/api/mapviewholder/ArcGISMapView2DSignature.astro`**:
`ArcGISMapView2D` の Composable シグネチャをコード表示

**`src/components/api/mapviewholder/ArcGISMapView2DBasicExample.astro`**:
Props: なし
基本的な使用例:
```kotlin
val state = rememberArcGISMapViewState(
    mapDesign = ArcGISDesign.Streets,
    cameraPosition = MapCameraPosition(
        position = GeoPoint(35.68, 139.76),
        zoom = 12.0,
    )
)

ArcGISMapView2D(
    state = state,
    modifier = Modifier.fillMaxSize(),
    onMapLoaded = {
        println("2D マップ読み込み完了")
    },
    onMapClick = { geoPoint ->
        println("タップ: ${geoPoint.latitude}, ${geoPoint.longitude}")
    }
) {
    // マーカーや他のオーバーレイを配置できます
}
```

**`src/components/api/mapviewholder/ArcGISMapView2DVsArcGISMapView.astro`**:
Props: なし
2D と 3D の比較コード例:
```kotlin
// 3D ビュー（デフォルト ArcGISMapView）
ArcGISMapView(
    state = state,
    modifier = Modifier.fillMaxSize(),
) { }

// 2D フラットビュー（ArcGISMapView2D）
ArcGISMapView2D(
    state = state,
    modifier = Modifier.fillMaxSize(),
) { }
```

### 新規作成: `src/content/docs/ja/mapviewholder/arcgis-2d.mdx`

frontmatter:
```yaml
title: ArcGISMapView2D（2D フラットビュー）
head:
  - tag: meta
    attrs:
      name: description
      content: MapConductor Android SDKのArcGISMapView2D — ArcGISの2DフラットマップビューComposable
```

内容:
- 概要: ArcGIS 2D フラットマップビュー。3D SceneView ではなく 2D MapView を使用
- Composable シグネチャセクション
- `ArcGISMapView` との違いセクション（tilt固定・建物3D表示なし・軽量）
- 基本的な使い方セクション
- パラメータ説明（state, modifier, markerTiling, sdkInitialize, onMapLoaded, onCameraMoveStart/Move/End, onMapClick, onMapLongClick, content）
- 注意事項: tilt は常に 0 に固定。MapCameraPosition に tilt を指定しても無視される。

---

## 機能3: `InfoBubble` マーカー不要オーバーロードと `InfoBubbleCustom`

### 概要
`InfoBubble` に `MarkerState` なしで地図上の任意座標に配置できる新オーバーロードが追加されました。また `InfoBubbleCustom` で完全カスタム形状のバブルが作れます。

### 新しい API シグネチャ

```kotlin
// マーカー不要の新オーバーロード
@Composable
fun MapViewScope.InfoBubble(
    position: GeoPoint,
    bubbleColor: Color = Color.White,
    borderColor: Color = Color.Black,
    contentPadding: Dp = 8.dp,
    cornerRadius: Dp = 4.dp,
    tailSize: Dp = 8.dp,
    content: @Composable () -> Unit,
)

// カスタム形状バブル
@Composable
fun MapViewScope.InfoBubbleCustom(
    marker: MarkerState,
    tailOffset: Offset,
    content: @Composable () -> Unit,
)
```

`InfoBubble(position: GeoPoint)` の動作:
- マーカーに紐付けず、`GeoPoint` で指定した座標に吹き出しを表示
- `position` が変更されるとバブルが移動する
- 地図のパン/ズームに追従する

`InfoBubbleCustom` の動作:
- `tailOffset` でバブルのどの点（0..1）が接続点かを指定
  - 例: 右側中央 = `Offset(1f, 0.5f)`、下中央 = `Offset(0.5f, 1f)`
- `content` で吹き出し全体（尾を含む）を自由に描画

### 作成するファイル

**`src/components/components/infobubble/StandaloneInfoBubbleSignature.astro`**:
`InfoBubble(position: GeoPoint, ...)` シグネチャ

**`src/components/components/infobubble/StandaloneInfoBubbleExample.astro`**:
Props: `latitude: number = 35.6812`, `longitude: number = 139.7671`, `commentForLabel: string = "ここに吹き出し"`
```kotlin
MapView(...) {
    InfoBubble(
        position = GeoPoint(${latitude}, ${longitude}),
        bubbleColor = Color.White,
        borderColor = Color.Blue,
    ) {
        Text("${commentForLabel}")
    }
}
```

**`src/components/components/infobubble/InfoBubbleCustomSignature.astro`**:
`InfoBubbleCustom` シグネチャ

**`src/components/components/infobubble/InfoBubbleCustomExample.astro`**:
Props: `commentForTailOffset: string = "右側中央を接続点にする"`, `commentForMarker: string = "マーカー"`
右側に尾を持つカスタムバブルの例:
```kotlin
InfoBubbleCustom(
    marker = markerState, // ${commentForMarker}
    tailOffset = Offset(1f, 0.5f), // ${commentForTailOffset}
) {
    // カスタム形状の吹き出しを描画
    Box(
        modifier = Modifier
            .background(Color.Yellow, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Text("カスタムバブル")
    }
}
```

### 更新するファイル
`src/content/docs/ja/components/infobubble.mdx` に以下を追加:
- 既存の「基本的な使用方法」セクションの前に「マーカー不要の配置」セクションを追加
- `InfoBubbleCustom` の使い方セクションを末尾付近に追加
- 対応する import 文を追加

---

## 機能4: `MapCameraPosition.tilt` マイナス値 — 前方を見るカメラ

### 概要
`MapCameraPosition.tilt` にマイナス値を指定すると、水平線より上方を向く「仰角ビュー」になります。

- **正の tilt（0〜90）**: 真上（0）から水平（90）の間で地面を見下ろす（既存の動作）
- **負の tilt（-1〜-90）**: 水平線より abs(tilt) 度上方を向く（前方を見る）

各 SDK の実装:
- **ArcGIS**: ネイティブに対応（ArcGIS のカメラ仕様と同一）
- **Google Maps, MapLibre, Mapbox, HERE**: 上向きピッチを直接表現できないため、カメラ位置を固定し bearing 方向の前方へターゲットを `altitude * tan(|tilt|)` メートル移動してシミュレート

### 作成するファイル

**`src/components/core/mapcameraposition/NegativeTiltExample.astro`**:
Props: `latitude: number = 35.68`, `longitude: number = 139.76`, `commentForLookUp: string = "前方（水平線上方）を見る"`, `commentForNormal: string = "通常の俯瞰ビュー"`, `commentForArcGISNote: string = "ArcGIS はネイティブ対応"`
```kotlin
// ${commentForNormal}（従来の動作）
MapCameraPosition(
    position = GeoPoint(${latitude}, ${longitude}),
    zoom = 15.0,
    tilt = 60.0,   // 地面を60度の角度で見下ろす
)

// ${commentForLookUp}
MapCameraPosition(
    position = GeoPoint(${latitude}, ${longitude}),
    zoom = 15.0,
    tilt = -30.0,  // 水平線より30度上方を見る
    bearing = 45.0,
)
```

**`src/components/core/mapcameraposition/NegativeTiltSdkNote.astro`**:
Props: なし
SDK ごとの対応状況を箇条書きで表示するコンポーネント（Markdown テキスト、コードブロックなし）:
ArcGIS: ネイティブ対応、Google Maps / Mapbox / MapLibre / HERE: ターゲット移動によるシミュレーション（完全に同一にはならない）

### 更新するファイル
`src/content/docs/ja/core/mapcameraposition.mdx` の「傾き（3D 視点）」セクション内 `<TiltExamples ... />` の直後に「負の tilt 値 — 前方を見る」サブセクションを追加:
- `<NegativeTiltExample />` と `<NegativeTiltSdkNote />` を使う
- import 文も追加

---

## 機能5: GeoJSON レイヤーモジュール (`android-geojson-layer`)

### 概要
`android-geojson-layer` は独立したモジュールで、地図実装に依存しない GeoJSON データの表示機能を提供します。タイルベースのラスターレイヤーとして動作するため、あらゆる地図 SDK で使用できます。

### 主要 API

#### `GeoJSONLayer` Composable
```kotlin
@Composable
fun MapViewScope.GeoJSONLayer(
    state: GeoJSONLayerState = remember { GeoJSONLayerState() },
    features: List<GeoJSONFeature> = emptyList(),
    tileSize: Int = 512,
    disableTileServerCache: Boolean = false,
    content: @Composable () -> Unit = {},
)
```

#### `GeoJSONLayerState`
```kotlin
class GeoJSONLayerState(
    opacity: Float = 1.0f,
    strokeColor: Int = Color.argb(255, 30, 136, 229),  // 青系
    fillColor: Int = Color.argb(128, 30, 136, 229),    // 半透明青系
    strokeWidth: Float = 2f,
    pointRadius: Float = 8f,
    visible: Boolean = true,
    minZoom: Int = 0,
    maxZoom: Int = 22,
    val onClick: ((feature: GeoJSONFeature, position: GeoPoint) -> Unit)? = null,
)
```
メソッド:
- `processClick(geoPoint: GeoPoint): Boolean` — マップのクリックハンドラから呼び出してフィーチャのヒットテストを行う。フィーチャが見つかれば `onClick` を呼び出し `true` を返す

#### `GeoJSONFeature`（静的/一括データ用）
```kotlin
data class GeoJSONFeature(
    val id: String? = null,
    val geometry: GeoJSONGeometry,
    val properties: Map<String, Any?> = emptyMap(),
    val strokeColor: Int? = null,   // nullの場合は GeoJSONLayerState のデフォルトを使用
    val fillColor: Int? = null,
    val strokeWidth: Float? = null,
    val pointRadius: Float? = null,
    val visible: Boolean = true,
)
```

#### `GeoJSONFeatureState`（Compose のリアクティブ状態用）
`GeoJSONFeatureState` は `GeoJSONLayer` の `content` ブロック内に配置する Composable 対応の状態クラス。大量データには `GeoJSONFeature`（`features` パラメータ）を使う方が効率的。

#### `GeoJSONGeometry` sealed class
```kotlin
sealed class GeoJSONGeometry {
    data class Point(val longitude: Double, val latitude: Double)
    data class MultiPoint(val points: List<Point>)
    data class LineString(val coordinates: List<LonLat>)
    data class MultiLineString(val lines: List<List<LonLat>>)
    data class Polygon(val rings: List<List<LonLat>>)  // rings[0]=外周, rings[1..]=穴
    data class MultiPolygon(val polygons: List<List<List<LonLat>>>)
    data class GeometryCollection(val geometries: List<GeoJSONGeometry>)
    object Empty
}

data class LonLat(val longitude: Double, val latitude: Double)
```

#### `GeoJSONParser`
```kotlin
object GeoJSONParser {
    // InputStream 全体をパースして List<GeoJSONFeature> を返す
    fun parseStream(inputStream: InputStream): List<GeoJSONFeature>
    
    // 1フィーチャずつコールバック — 大容量ファイル（10MB+）向け
    fun streamParse(inputStream: InputStream, onFeature: (GeoJSONFeature) -> Unit)
}
```

### インストール（Gradle）
```kotlin
dependencies {
    implementation(platform("com.mapconductor:mapconductor-bom:{BOM_MODULE_VERSION}"))
    implementation("com.mapconductor:core")
    implementation("com.mapconductor:geojson-layer")

    // 使用する地図 SDK を選択
    implementation("com.mapconductor:for-googlemaps")
}
```

### 作成するファイル

**`src/components/experimental/geojson/GeoJSONLayerInstall.astro`**:
上記 Gradle 依存関係コードを表示

**`src/components/experimental/geojson/GeoJSONLayerSignature.astro`**:
`GeoJSONLayer` Composable シグネチャを表示

**`src/components/experimental/geojson/GeoJSONLayerStateSignature.astro`**:
`GeoJSONLayerState` コンストラクタと `processClick` メソッドを表示

**`src/components/experimental/geojson/GeoJSONFeatureSignature.astro`**:
`GeoJSONFeature` data class を表示

**`src/components/experimental/geojson/GeoJSONGeometryTypes.astro`**:
`GeoJSONGeometry` sealed class 全体を表示

**`src/components/experimental/geojson/GeoJSONParserSignature.astro`**:
`GeoJSONParser` オブジェクトのメソッドシグネチャを表示

**`src/components/experimental/geojson/GeoJSONBasicExample.astro`**:
Props: なし
ポイント・ラインストリング・ポリゴンを含む基本的な使用例:
```kotlin
val layerState = remember { GeoJSONLayerState() }
val features = remember {
    listOf(
        GeoJSONFeature(
            geometry = GeoJSONGeometry.Point(longitude = 139.76, latitude = 35.68),
            properties = mapOf("name" to "東京駅"),
        ),
        GeoJSONFeature(
            geometry = GeoJSONGeometry.LineString(
                coordinates = listOf(
                    LonLat(139.76, 35.68),
                    LonLat(139.77, 35.69),
                )
            ),
        ),
        GeoJSONFeature(
            geometry = GeoJSONGeometry.Polygon(
                rings = listOf(
                    listOf(
                        LonLat(139.75, 35.67),
                        LonLat(139.77, 35.67),
                        LonLat(139.77, 35.69),
                        LonLat(139.75, 35.69),
                        LonLat(139.75, 35.67), // 始点と終点を同じにする
                    )
                )
            ),
        ),
    )
}

MapView(...) {
    GeoJSONLayer(
        state = layerState,
        features = features,
    )
}
```

**`src/components/experimental/geojson/GeoJSONParserExample.astro`**:
Props: `commentForLargeFile: string = "大容量 GeoJSON ファイルのパース"`, `commentForStream: string = "1フィーチャずつ処理（メモリ効率が良い）"`
```kotlin
// ${commentForLargeFile}
val features = GeoJSONParser.parseStream(assets.open("data.geojson"))

// ${commentForStream}
GeoJSONParser.streamParse(assets.open("large-data.geojson")) { feature ->
    // 1件ずつ処理
}
```

**`src/components/experimental/geojson/GeoJSONClickExample.astro`**:
Props: `commentForHitTest: string = "タップ位置のフィーチャを検索"`, `commentForResult: string = "フィーチャが見つかった場合"`
```kotlin
val layerState = remember {
    GeoJSONLayerState(
        onClick = { feature, position ->
            println("${commentForResult}: ${feature.properties["name"]}")
        }
    )
}

MapView(
    onMapClick = { geoPoint ->
        // ${commentForHitTest}
        val handled = layerState.processClick(geoPoint)
        if (!handled) {
            // GeoJSON フィーチャ以外のタップ処理
        }
    }
) {
    GeoJSONLayer(state = layerState, features = features)
}
```

**`src/components/experimental/geojson/GeoJSONFeatureStateExample.astro`**:
Props: `commentForDynamic: string = "動的に変化するフィーチャ（Compose state）"`, `commentForContent: string = "content ブロック内に GeoJSONFeatureState を配置"`
リアクティブな `GeoJSONFeatureState` の使用例:
```kotlin
val featureState = remember {
    GeoJSONFeatureState(
        geometry = GeoJSONGeometry.Point(longitude = 139.76, latitude = 35.68),
        properties = mapOf("name" to "動的ポイント"),
    )
}

// ${commentForDynamic}
LaunchedEffect(userLocation) {
    featureState.geometry = GeoJSONGeometry.Point(
        longitude = userLocation.longitude,
        latitude = userLocation.latitude,
    )
}

MapView(...) {
    GeoJSONLayer(state = layerState) {
        // ${commentForContent}
        GeoJSONFeatureCompose(state = featureState)
    }
}
```

**`src/components/experimental/geojson/GeoJSONStyleExample.astro`**:
Props: なし
スタイルカスタマイズ例:
```kotlin
val layerState = remember {
    GeoJSONLayerState(
        strokeColor = Color.rgb(255, 87, 34),      // オレンジ
        fillColor = Color.argb(100, 255, 87, 34),  // 半透明オレンジ
        strokeWidth = 3f,
        pointRadius = 12f,
        opacity = 0.9f,
    )
}
```

### 新規作成: `src/content/docs/ja/experimental/geojson-layer.mdx`

frontmatter:
```yaml
title: GeoJSON レイヤー
head:
  - tag: meta
    attrs:
      name: description
      content: MapConductor Android SDKのGeoJSONレイヤー — 任意の地図プロバイダでGeoJSONデータを表示するモジュール
```

ページ構成:
1. 概要（地図実装非依存のタイルベース GeoJSON レンダリング）
2. インストール（`<GeoJSONLayerInstall />`）
3. 基本的な使い方（`<GeoJSONBasicExample />`）
4. API リファレンス
   - `GeoJSONLayer` Composable（`<GeoJSONLayerSignature />`）
   - `GeoJSONLayerState`（`<GeoJSONLayerStateSignature />`）
   - `GeoJSONLayerState` パラメータ説明
   - `GeoJSONFeature`（`<GeoJSONFeatureSignature />`）
5. ジオメトリタイプ（`<GeoJSONGeometryTypes />`）
6. 大容量ファイルのパース（`<GeoJSONParserSignature />` + `<GeoJSONParserExample />`）
7. クリックイベント / ヒットテスト（`<GeoJSONClickExample />`）
8. リアクティブフィーチャ（GeoJSONFeatureState）（`<GeoJSONFeatureStateExample />`）
9. スタイルカスタマイズ（`<GeoJSONStyleExample />`）

### 更新するファイル
`src/content/docs/ja/modules.mdx` に `geojson-layer` モジュールのエントリを追加（既存の `heatmap` エントリを参考にして）

---

## 注意事項

- 全テキストは**日本語**で書く（コードコメントも日本語可）
- `.astro` ファイルはすべて `src/components/` 以下の適切なサブディレクトリに作成
- `.mdx` ファイルはすべて `src/content/docs/ja/` 以下
- MDX の import 文は必ず `~/components/...` 形式（`@` ではなく `~`）
- コードブロックは `<Code code={code} lang="kotlin" />` 形式（直接 `\`\`\`` でもよい）
- `GeoJSONFeatureCompose` は `content` ブロック内に配置する Composable（使用例で参照可）
- 既存ページを更新する場合は既存の内容を削除せず、新しいセクションを追加する
