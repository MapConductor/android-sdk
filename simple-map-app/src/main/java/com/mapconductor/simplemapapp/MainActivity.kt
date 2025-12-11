package com.mapconductor.simplemapapp

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.arcgismaps.mapping.ArcGISScene
import com.arcgismaps.mapping.PortalItem
import com.arcgismaps.mapping.layers.ArcGISMapImageLayer
import com.arcgismaps.portal.Portal
import com.arcgismaps.portal.PortalItemType
import com.arcgismaps.toolkit.authentication.Authenticator
import com.arcgismaps.toolkit.authentication.AuthenticatorState
import com.mapconductor.arcgis.authentication.ArcGISOAuthHybridInitialize
import com.mapconductor.arcgis.map.ArcGISMapView
import com.mapconductor.arcgis.map.rememberArcGISMapViewState
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.map.MapCameraPositionImpl
import com.mapconductor.simplemapapp.ui.theme.MapConductorSDKTheme
import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MapConductorSDKTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BasicMapExample(
                        modifier =
                            Modifier
                                .padding(innerPadding)
                                .fillMaxSize(),
                    )
                }
            }
        }
    }
}

@Composable
fun BasicMapExample(modifier: Modifier = Modifier) {
    val mapViewState = rememberArcGISMapViewState()

    // 認証状態と CoroutineScope（await ライクに逐次処理）
    val authenticatorState = remember { AuthenticatorState() }
    val scope = rememberCoroutineScope()

    ArcGISMapView(
        state = mapViewState,
        modifier = modifier.fillMaxSize(),
        sdkInitialize = { context ->
            ArcGISOAuthHybridInitialize(
                authenticatorState = authenticatorState,
                portalUrl = "https://mkgeeklab.maps.arcgis.com/",
                redirectUrl = "urn:ietf:wg:oauth:2.0:oob",
                clientId = "9QsChEh9QuTV5jLk",
                clientSecret = "34a5c93cfcb1462cb409f37fba775a39",
            )
        },
        onMapLoaded = {
            scope.launch {
                val holder = mapViewState.getMapViewHolder() ?: return@launch

                // await 風に直列実行
                val portal = Portal("https://mkgeeklab.maps.arcgis.com/", Portal.Connection.Authenticated)
                portal.load().getOrThrow()

                val item = PortalItem(portal, "e0ce06974e3d4c79b37e30d224c585d3")
                item.load().getOrThrow()

                when (item.type) {
                    is PortalItemType.WebScene -> {
                        ArcGISScene(item).also {
                            it.load().getOrThrow() // WebSceneの読み込み完了を待つ
                            holder.map.scene = it
                        }
                    }
                    is PortalItemType.WebMap -> error("SceneView は WebScene のみ対応")
                    else -> error("Unknown item type: ${item.type}")
                }

                // 追加レイヤ例（任意）
                val trafficServerUrl = "https://traffic.arcgis.com/arcgis/rest/services/World/Traffic/MapServer"
                ArcGISMapImageLayer(trafficServerUrl).also {
                    it.opacity = 0.6f
                    holder.map.scene!!.operationalLayers.add(0, it)
                }
            }
        }
    ) {}

    // 認証UI（ユーザーログイン用）。ハイブリッド認証のフォールバックで使用されます。
    Authenticator(authenticatorState = authenticatorState, modifier = modifier)
}


@Composable
fun BasicMapExampleBackup(modifier: Modifier = Modifier) {
    // 地図のカメラ位置
    val mapViewState =
        rememberArcGISMapViewState(
            cameraPosition =
                MapCameraPositionImpl(
                    position = GeoPointImpl.fromLatLong(35.6812, 139.7671),
                    zoom = 12.0,
                ),
        )

    // 認証状態と CoroutineScope（await風の逐次処理で使う）
    val authenticatorState = remember { AuthenticatorState() }
    val scope = rememberCoroutineScope()

    ArcGISMapView(
        state = mapViewState,
        modifier = modifier.fillMaxSize(),
        sdkInitialize = { context ->
            ArcGISOAuthHybridInitialize(
                authenticatorState = authenticatorState,
                portalUrl = "https://mkgeeklab.maps.arcgis.com/",
                redirectUrl = "urn:ietf:wg:oauth:2.0:oob",
                clientId = "9QsChEh9QuTV5jLk",
                clientSecret = "34a5c93cfcb1462cb409f37fba775a39",
            )
        },
        onMapLoaded = {
            // ここで await 風に順次処理
            scope.launch {
                val holder = mapViewState.getMapViewHolder() ?: return@launch
                runCatching {
                    Log.d("ArcGIS", "Loading portal and scene...")
                    val portal = Portal("https://mkgeeklab.maps.arcgis.com/", Portal.Connection.Authenticated)
                    portal.load().getOrThrow()

                    val portalItem = PortalItem(portal, "e0ce06974e3d4c79b37e30d224c585d3")
                    portalItem.load().getOrThrow()

                    when (portalItem.type) {
                        is PortalItemType.WebScene -> {
                            holder.map.scene = ArcGISScene(portalItem)
                        }
                        is PortalItemType.WebMap -> {
                            error("SceneView only supports WebScene. Use a WebScene item ID.")
                        }
                        else -> error("Unknown item type: ${portalItem.type}")
                    }

                    // 追加レイヤ（任意）
                    val trafficLayer = ArcGISMapImageLayer(
                        "https://traffic.arcgis.com/arcgis/rest/services/World/Traffic/MapServer",
                    )
                    //holder.map.scene!!.operationalLayers.add(trafficLayer)
                }.onFailure { e ->
                    Log.e("ArcGIS", "Post-load sequential setup failed", e)
                }
            }
        },
    ) {}

    // 認証UI（ユーザーログイン用）。ハイブリッド認証のフォールバックで使用されます。
    Authenticator(
        authenticatorState = authenticatorState,
        modifier = modifier,
    )
}

/*
 * ===============================
 * 他の認証パターンの例
 * ===============================
 *
 * パターンA: OAuth Application Credential認証のみ（ログインダイアログなし）
 * 組織で共有されているコンテンツにアクセス可能
 *
 * ArcGISMapView(
 *     state = mapViewState,
 *     sdkInitialize = { context ->
 *         arcGISOAuthApplicationInitialize(
 *             portalUrl = "https://mkgeeklab.maps.arcgis.com/",
 *             clientId = "9QsChEh9QuTV5jLk",
 *             clientSecret = "34a5c93cfcb1462cb409f37fba775a39"
 *         )
 *     },
 *     onMapLoaded = { arcGisMapViewHolder = mapViewState.getMapViewHolder() }
 * ) {}
 *
 * ===============================
 *
 * パターンB: OAuth User Credential認証（ログインダイアログあり）
 * ユーザーのプライベートコンテンツにアクセス可能
 * ※ Authenticator(authenticatorState)をScaffold内に追加する必要があります
 *
 * ArcGISMapView(
 *     state = mapViewState,
 *     sdkInitialize = { context ->
 *         arcGISOAuthUserInitialize(
 *             authenticatorState = authenticatorState,
 *             portalUrl = "https://mkgeeklab.maps.arcgis.com/",
 *             clientId = "9QsChEh9QuTV5jLk",
 *             redirectUrl = "urn:ietf:wg:oauth:2.0:oob"
 *         )
 *     },
 *     onMapLoaded = { arcGisMapViewHolder = mapViewState.getMapViewHolder() }
 * ) {}
 *
 * ===============================
 *
 * パターンC: デフォルトのAPI Key認証（従来通り）
 * AndroidManifest.xmlのARCGIS_API_KEYを使用
 *
 * ArcGISMapView(
 *     state = mapViewState,
 *     // sdkInitializeを指定しない = デフォルトのAPI Key認証
 *     onMapLoaded = { arcGisMapViewHolder = mapViewState.getMapViewHolder() }
 * ) {}
 *
 * ===============================
 *
 * パターンD: 完全カスタム認証
 * 任意の認証ロジックを実装可能
 *
 * ArcGISMapView(
 *     state = mapViewState,
 *     sdkInitialize = { context ->
 *         // カスタム認証ロジック
 *         myCustomAuthenticationLogic(context)
 *     },
 *     onMapLoaded = { arcGisMapViewHolder = mapViewState.getMapViewHolder() }
 * ) {}
 */
