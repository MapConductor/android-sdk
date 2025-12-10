package com.mapconductor.arcgis.authentication

import com.arcgismaps.ArcGISEnvironment
import com.arcgismaps.httpcore.authentication.OAuthApplicationCredential
import com.arcgismaps.httpcore.authentication.OAuthUserConfiguration
import com.arcgismaps.toolkit.authentication.AuthenticatorState
import android.util.Log

/**
 * ArcGIS OAuth Application Credential認証を初期化します。
 *
 * Client SecretとClient IDでアプリケーションレベルの認証を行います。
 * ログインダイアログは表示されません。組織で共有されているコンテンツにアクセスできます。
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
    try {
        // API Keyをクリア（OAuth認証を使用する場合は不要）
        ArcGISEnvironment.apiKey = null

        // AuthenticatorStateをチャレンジハンドラーとして設定（オプション）
        authenticatorState?.let {
            ArcGISEnvironment.authenticationManager.arcGISAuthenticationChallengeHandler = it
            ArcGISEnvironment.authenticationManager.networkAuthenticationChallengeHandler = it
        }

        // Application Credentialで認証
        val result =
            OAuthApplicationCredential.create(
                portalUrl = portalUrl,
                clientId = clientId,
                clientSecret = clientSecret,
                tokenExpirationInterval = 0,
            )

        result
            .onSuccess { credential ->
                ArcGISEnvironment.authenticationManager.arcGISCredentialStore.add(credential)
                Log.d("ArcGIS", "OAuth Application authentication configured")
            }.onFailure { error ->
                Log.e("ArcGIS", "OAuth Application authentication failed: ${error.message}", error)
            }

        result.isSuccess
    } catch (e: Exception) {
        Log.e("ArcGIS", "Error configuring OAuth Application authentication", e)
        false
    }

/**
 * ArcGIS OAuth User Credential認証を初期化します。
 *
 * ユーザーログインが必要な場合に使用します。
 * AuthenticatorStateを通じてログインダイアログが表示されます。
 *
 * @param authenticatorState 認証状態を管理するAuthenticatorState
 * @param portalUrl Portalの URL
 * @param clientId OAuth Client ID
 * @param redirectUrl OAuth Redirect URI (例: "urn:ietf:wg:oauth:2.0:oob")
 * @return 常にtrueを返す（実際の認証は非同期で行われる）
 */
suspend fun arcGISOAuthUserInitialize(
    authenticatorState: AuthenticatorState,
    portalUrl: String,
    clientId: String,
    redirectUrl: String,
): Boolean {
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
        return true
    } catch (e: Exception) {
        Log.e("ArcGIS", "Error configuring OAuth User authentication", e)
        return false
    }
}

/**
 * ハイブリッド認証を初期化します。
 *
 * Application Credentialで認証を試み、失敗した場合は自動的にユーザーログインにフォールバックします。
 *
 * @param portalUrl Portalの URL
 * @param clientId OAuth Client ID
 * @param clientSecret OAuth Client Secret
 * @param redirectUrl OAuth Redirect URI
 * @param authenticatorState 認証状態を管理するAuthenticatorState
 * @return Application Credential認証が成功した場合true、失敗した場合はfalse（ユーザー認証へフォールバック）
 */
suspend fun arcGISOAuthHybridInitialize(
    portalUrl: String,
    clientId: String,
    clientSecret: String,
    redirectUrl: String,
    authenticatorState: AuthenticatorState,
): Boolean {
    // まず、User認証を設定（フォールバック用）
    arcGISOAuthUserInitialize(
        authenticatorState = authenticatorState,
        portalUrl = portalUrl,
        clientId = clientId,
        redirectUrl = redirectUrl,
    )

    // Application Credentialで認証を試みる
    return arcGISOAuthApplicationInitialize(
        portalUrl = portalUrl,
        clientId = clientId,
        clientSecret = clientSecret,
        authenticatorState = authenticatorState,
    )
}
