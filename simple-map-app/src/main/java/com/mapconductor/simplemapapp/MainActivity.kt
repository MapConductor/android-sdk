package com.mapconductor.simplemapapp

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.arcgismaps.mapping.ArcGISScene
import com.arcgismaps.mapping.PortalItem
import com.arcgismaps.portal.Portal
import com.arcgismaps.portal.PortalItemType
import com.arcgismaps.toolkit.authentication.AuthenticatorState
import com.mapconductor.arcgis.ArcGISMapViewHolder
import com.mapconductor.arcgis.authentication.arcGISOAuthHybridInitialize
import com.mapconductor.arcgis.map.ArcGISMapView
import com.mapconductor.arcgis.map.rememberArcGISMapViewState
import com.mapconductor.core.features.GeoPointImpl
import com.mapconductor.core.map.MapCameraPositionImpl
import com.mapconductor.simplemapapp.ui.theme.MapConductorSDKTheme
import android.os.Bundle
import android.util.Log

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MapConductorSDKTheme {
                // AuthenticatorStateを設定（ユーザー認証にフォールバック用）
                val authenticatorState = remember { AuthenticatorState() }

                // 認証処理をLaunchedEffect内で実行
                LaunchedEffect(Unit) {
                    arcGISOAuthHybridInitialize(
                        portalUrl = "https://mkgeeklab.maps.arcgis.com/",
                        clientId = "9QsChEh9QuTV5jLk",
                        clientSecret = "34a5c93cfcb1462cb409f37fba775a39",
                        redirectUrl = "urn:ietf:wg:oauth:2.0:oob",
                        authenticatorState = authenticatorState
                    )
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BasicMapExample(
                        authenticatorState = authenticatorState,
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
fun BasicMapExample(
    authenticatorState: AuthenticatorState,
    modifier: Modifier = Modifier
) {
    // 地図のカメラ位置
    val mapViewState = rememberArcGISMapViewState(
        cameraPosition = MapCameraPositionImpl(
            position = GeoPointImpl.fromLatLong(35.6812, 139.7671),
            zoom = 12.0
        ),
    )

    // AuthenticatorStateを使って認証状態を監視（オプション）
    LaunchedEffect(authenticatorState) {
        Log.d("ArcGIS", "AuthenticatorState initialized")
    }

    // ViewHolderの保持
    var arcGisMapViewHolder by remember { mutableStateOf<ArcGISMapViewHolder?>(null) }

    LaunchedEffect(arcGisMapViewHolder) {
        arcGisMapViewHolder?.let { holder ->
            try {
                // Don't delete the trafficLayer lines in the following comments. It works.
                //            val trafficLayer =
                //                ArcGISMapImageLayer("https://traffic.arcgis.com/arcgis/rest/services/World/Traffic/MapServer")
                //            holder.map.scene!!.operationalLayers.add(trafficLayer)

                Log.d("ArcGIS", "Loading portal and scene...")

                // Portal.Connection.Authenticatedを指定して認証を要求
                val portal = Portal("https://mkgeeklab.maps.arcgis.com/", Portal.Connection.Authenticated)

                // Portalを読み込む
                portal.load().onSuccess {
                    Log.d("ArcGIS", "Portal loaded successfully, loading item...")

                    val portalItem = PortalItem(portal, "e0ce06974e3d4c79b37e30d224c585d3")

                    // PortalItemを読み込んでタイプを確認
                    portalItem.load().onSuccess {
                        Log.d("ArcGIS", "PortalItem loaded. Type: ${portalItem.type}")

                        when (portalItem.type) {
                            is PortalItemType.WebScene -> {
                                Log.d("ArcGIS", "Loading as WebScene (3D)...")
                                val webScene = ArcGISScene(portalItem)
                                holder.map.scene = webScene
                                Log.d("ArcGIS", "WebScene set to map view")
                            }
                            is PortalItemType.WebMap -> {
                                Log.e("ArcGIS", "This is a WebMap (2D), but SceneView only supports WebScene (3D)")
                                Log.e("ArcGIS", "Please use a WebScene item ID instead")
                            }
                            else -> {
                                Log.e("ArcGIS", "Unknown item type: ${portalItem.type}")
                            }
                        }
                    }.onFailure { error ->
                        Log.e("ArcGIS", "Failed to load portal item: ${error.message}", error)
                    }
                }.onFailure { error ->
                    Log.e("ArcGIS", "Failed to load portal: ${error.message}", error)
                }
            } catch (e: Exception) {
                Log.e("ArcGIS", "Error loading portal/scene", e)
            }
        }
    }

    ArcGISMapView(
        state = mapViewState,
        modifier = modifier.fillMaxSize(),
        // OAuth認証は既にLaunchedEffectで実行済みなので、SDK初期化はスキップ
        sdkInitialize = { context -> true },
        onMapLoaded = {
            // ViewHolderの取得
            arcGisMapViewHolder = mapViewState.getMapViewHolder()
        }
    ) {}
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
