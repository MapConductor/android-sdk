package com.mapconductor.arcgis.authentication

import com.arcgismaps.ArcGISEnvironment
import com.arcgismaps.httpcore.authentication.OAuthApplicationCredential
import com.arcgismaps.httpcore.authentication.OAuthUserConfiguration
import com.arcgismaps.portal.Portal
import com.arcgismaps.toolkit.authentication.AuthenticatorState
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * ArcGIS OAuth Application Credential認証を初期化します。
 *
 * Client SecretとClient IDでアプリケーションレベルの認証を行います。
 * ログインダイアログは表示されません。組織で共有されているコンテンツにアクセスできます。
 * 認証が完了するまで待機します。
 *
 * @param portalUrl Portalの URL (例: "https://www.arcgis.com/", "https://your-org.maps.arcgis.com/")
 * @param clientId OAuth Client ID
 * @param clientSecret OAuth Client Secret
 * @param authenticatorState (オプション) ユーザー認証へのフォールバック用AuthenticatorState
 * @return 認証が成功した場合true、失敗した場合false
 */
suspend fun arcGISOAuthApplicationInitialize(
    portalUrl: String,
    clientId: String,
    clientSecret: String,
    authenticatorState: AuthenticatorState? = null,
): Boolean =
    withContext(Dispatchers.IO) {
        try {
            // API Keyをクリア（OAuth認証を使用する場合は不要）
            ArcGISEnvironment.apiKey = null

            // AuthenticatorStateをチャレンジハンドラーとして設定（オプション）
            authenticatorState?.let {
                ArcGISEnvironment.authenticationManager.arcGISAuthenticationChallengeHandler = it
                ArcGISEnvironment.authenticationManager.networkAuthenticationChallengeHandler = it
            }

            // Application Credentialで認証
            val credResult =
                OAuthApplicationCredential.create(
                    portalUrl = portalUrl,
                    clientId = clientId,
                    clientSecret = clientSecret,
                    tokenExpirationInterval = 0,
                )

            if (credResult.isFailure) {
                Log.e("ArcGIS", "OAuth Application authentication failed: ${credResult.exceptionOrNull()?.message}")
                return@withContext false
            }

            credResult.getOrNull()?.let { credential ->
                ArcGISEnvironment.authenticationManager.arcGISCredentialStore.add(credential)
                Log.d("ArcGIS", "OAuth Application credential added")
            }

            // 認証が有効かどうかを確認するために、Portalにアクセスを試みる
            val portal = Portal(portalUrl, Portal.Connection.Authenticated)
            val loadResult = portal.load()

            loadResult
                .onSuccess {
                    Log.d("ArcGIS", "Portal loaded successfully - authentication completed")
                }.onFailure { error ->
                    Log.e("ArcGIS", "Failed to load portal: ${error.message}", error)
                }

            loadResult.isSuccess
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            Log.e("ArcGIS", "Error configuring OAuth Application authentication", e)
            false
        }
    }

/**
 * ArcGIS OAuth User Credential認証を初期化します。
 *
 * ユーザーログインが必要な場合に使用します。
 * AuthenticatorStateを通じてログインダイアログが表示されます。
 * 認証が完了するまで待機します。
 *
 * @param authenticatorState 認証状態を管理するAuthenticatorState
 * @param portalUrl Portalの URL
 * @param clientId OAuth Client ID
 * @param redirectUrl OAuth Redirect URI (例: "urn:ietf:wg:oauth:2.0:oob")
 * @return 認証が成功した場合true、失敗した場合false
 */
suspend fun arcGISOAuthUserInitialize(
    authenticatorState: AuthenticatorState,
    portalUrl: String,
    clientId: String,
    redirectUrl: String,
): Boolean =
    withContext(Dispatchers.IO) {
        try {
            // API Keyをクリア（OAuth認証を使用する場合は不要）
            ArcGISEnvironment.apiKey = null

            // OAuthUserConfigurationを設定
            authenticatorState.oAuthUserConfiguration =
                OAuthUserConfiguration(
                    portalUrl = portalUrl,
                    clientId = clientId,
                    redirectUrl = redirectUrl,
                )

            // AuthenticatorStateをチャレンジハンドラーとして設定
            ArcGISEnvironment.authenticationManager.arcGISAuthenticationChallengeHandler = authenticatorState
            ArcGISEnvironment.authenticationManager.networkAuthenticationChallengeHandler = authenticatorState

            Log.d("ArcGIS", "OAuth User authentication configured")

            // 認証が有効かどうかを確認するために、Portalにアクセスを試みる
            // これによりログインダイアログが表示され、認証が完了するまで待機します
            val portal = Portal(portalUrl, Portal.Connection.Authenticated)
            val loadResult = portal.load()

            loadResult
                .onSuccess {
                    Log.d("ArcGIS", "Portal loaded successfully - authentication completed")
                }.onFailure { error ->
                    Log.e("ArcGIS", "Failed to load portal: ${error.message}", error)
                }

            loadResult.isSuccess
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            Log.e("ArcGIS", "Error configuring OAuth User authentication", e)
            false
        }
    }

/**
 * ハイブリッド認証を初期化します。
 *
 * Application Credentialで認証を試み、失敗した場合は自動的にユーザーログインにフォールバックします。
 * 認証が完了するまで待機します。
 *
 * @param portalUrl Portalの URL
 * @param clientId OAuth Client ID
 * @param clientSecret OAuth Client Secret
 * @param redirectUrl OAuth Redirect URI
 * @param authenticatorState 認証状態を管理するAuthenticatorState
 * @return 認証が成功した場合true、失敗した場合false
 */
suspend fun ArcGISOAuthHybridInitialize(
    authenticatorState: AuthenticatorState,
    portalUrl: String,
    redirectUrl: String,
    clientId: String,
    clientSecret: String? = null,
): Boolean =
    withContext(Dispatchers.IO) {
        try {
            // API Keyをクリア（OAuth認証を使用する場合は不要）
            ArcGISEnvironment.apiKey = null

            // まず、User認証を設定（フォールバック用）
            authenticatorState.oAuthUserConfiguration =
                OAuthUserConfiguration(
                    portalUrl = portalUrl,
                    clientId = clientId,
                    redirectUrl = redirectUrl,
                )

            // AuthenticatorStateをチャレンジハンドラーとして設定
            ArcGISEnvironment.authenticationManager.arcGISAuthenticationChallengeHandler = authenticatorState
            ArcGISEnvironment.authenticationManager.networkAuthenticationChallengeHandler = authenticatorState

            // Application Credentialで認証を試みる
            if (clientSecret != null) {
                val appCredResult =
                    OAuthApplicationCredential.create(
                        portalUrl = portalUrl,
                        clientId = clientId,
                        clientSecret = clientSecret,
                        tokenExpirationInterval = 0,
                    )

                if (appCredResult.isSuccess) {
                    appCredResult.getOrNull()?.let { credential ->
                        ArcGISEnvironment.authenticationManager.arcGISCredentialStore.add(credential)
                    }
                }
            }

            // 認証が有効かどうかを確認するために、Portalにアクセスを試みる
            val portal = Portal(portalUrl, Portal.Connection.Authenticated)
            val loadResult = portal.load()

            loadResult
                .onSuccess {
                    Log.d("ArcGIS", "Portal loaded successfully - authentication completed")
                }.onFailure { error ->
                    Log.e("ArcGIS", "Failed to load portal: ${error.message}", error)
                }

            loadResult.isSuccess
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            Log.e("ArcGIS", "Error during hybrid authentication", e)
            false
        }
    }
