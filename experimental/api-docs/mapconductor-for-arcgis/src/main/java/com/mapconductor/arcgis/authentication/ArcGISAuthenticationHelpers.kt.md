Of course! Here is the high-quality SDK documentation for the provided Kotlin code snippets.

***

# ArcGIS Authentication SDK

This document provides detailed documentation for a set of helper functions designed to simplify OAuth authentication with the ArcGIS Maps SDK for Kotlin.

## `arcGISOAuthApplicationInitialize`

Initializes ArcGIS authentication using OAuth Application Credentials. This method performs app-level authentication using a Client ID and Client Secret, which does not require a user login dialog. It is ideal for accessing content that is shared with the organization or publicly. The function is asynchronous and waits for the authentication process to complete.

### Signature

```kotlin
suspend fun arcGISOAuthApplicationInitialize(
    portalUrl: String,
    clientId: String,
    clientSecret: String,
    authenticatorState: AuthenticatorState? = null,
): Boolean
```

### Description

This function configures the `ArcGISEnvironment` to use OAuth with application credentials. It first clears any existing API key, then creates and adds an `OAuthApplicationCredential` to the global credential store. To verify the authentication, it attempts to load a `Portal` object.

An optional `authenticatorState` can be provided to serve as a fallback mechanism, allowing the application to challenge the user for credentials if the application-level authentication is insufficient for accessing a specific resource.

### Parameters

| Parameter          | Type                 | Description                                                                                                                            |
| ------------------ | -------------------- | -------------------------------------------------------------------------------------------------------------------------------------- |
| `portalUrl`        | `String`             | The URL of the ArcGIS Portal (e.g., `"https://www.arcgis.com/"`).                                                                       |
| `clientId`         | `String`             | The OAuth Client ID for your application.                                                                                              |
| `clientSecret`     | `String`             | The OAuth Client Secret for your application.                                                                                          |
| `authenticatorState` | `AuthenticatorState?`| (Optional) An `AuthenticatorState` instance to handle user authentication challenges as a fallback. Defaults to `null`.                |

### Returns

`Boolean` - Returns `true` if the application credential is created and the portal is loaded successfully, indicating successful authentication. Returns `false` otherwise.

### Example

```kotlin
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

// In your ViewModel or another coroutine scope
fun initializeArcGIS() {
    viewModelScope.launch {
        val portalUrl = "https://www.arcgis.com"
        val clientId = "YOUR_CLIENT_ID"
        val clientSecret = "YOUR_CLIENT_SECRET"

        val isAuthenticated = arcGISOAuthApplicationInitialize(
            portalUrl = portalUrl,
            clientId = clientId,
            clientSecret = clientSecret
        )

        if (isAuthenticated) {
            println("ArcGIS Application Authentication Successful!")
            // Now you can load secured layers or portal items
        } else {
            println("ArcGIS Application Authentication Failed.")
        }
    }
}
```

---

## `arcGISOAuthUserInitialize`

Initializes ArcGIS authentication using OAuth User Credentials. This method is used when a user needs to log in to access secured content. It leverages an `AuthenticatorState` to manage and display the login user interface. The function is asynchronous and waits for the user to complete the login process.

### Signature

```kotlin
suspend fun arcGISOAuthUserInitialize(
    authenticatorState: AuthenticatorState,
    portalUrl: String,
    clientId: String,
    redirectUrl: String,
): Boolean
```

### Description

This function configures the `ArcGISEnvironment` for a user-centric OAuth flow. It sets up an `OAuthUserConfiguration` on the provided `authenticatorState` and registers the state as the global challenge handler. When a secured resource is requested, the `authenticatorState` will trigger the display of a login dialog. The function confirms a successful login by attempting to load a `Portal` object, which initiates the authentication challenge.

### Parameters

| Parameter          | Type                 | Description                                                                                                                            |
| ------------------ | -------------------- | -------------------------------------------------------------------------------------------------------------------------------------- |
| `authenticatorState` | `AuthenticatorState` | The state manager that handles the authentication UI and flow. This is typically observed in your UI to show a login prompt.          |
| `portalUrl`        | `String`             | The URL of the ArcGIS Portal (e.g., `"https://www.arcgis.com/"`).                                                                       |
| `clientId`         | `String`             | The OAuth Client ID for your application.                                                                                              |
| `redirectUrl`      | `String`             | The OAuth Redirect URI configured for your application (e.g., `"urn:ietf:wg:oauth:2.0:oob"`).                                          |

### Returns

`Boolean` - Returns `true` if the user successfully authenticates and the portal is loaded. Returns `false` if the authentication fails or is canceled.

### Example

```kotlin
import androidx.lifecycle.viewModelScope
import com.arcgismaps.toolkit.authentication.AuthenticatorState
import kotlinx.coroutines.launch

// In your ViewModel
val authenticatorState = AuthenticatorState()

fun initializeArcGISForUser() {
    viewModelScope.launch {
        val portalUrl = "https://www.arcgis.com"
        val clientId = "YOUR_CLIENT_ID"
        val redirectUrl = "urn:ietf:wg:oauth:2.0:oob"

        val isAuthenticated = arcGISOAuthUserInitialize(
            authenticatorState = authenticatorState,
            portalUrl = portalUrl,
            clientId = clientId,
            redirectUrl = redirectUrl
        )

        if (isAuthenticated) {
            println("ArcGIS User Authentication Successful!")
            // The user is logged in.
        } else {
            println("ArcGIS User Authentication Failed.")
        }
    }
}

// In your Composable UI, you would use the Authenticator composable
// Authenticator(authenticatorState = viewModel.authenticatorState) {
//     // Your main app content, e.g., a MapView
// }
```

---

## `ArcGISOAuthHybridInitialize`

Initializes a hybrid authentication flow that first attempts app-level authentication and automatically falls back to user-level authentication if required. This function is asynchronous and waits for an authentication method to succeed.

### Signature

```kotlin
suspend fun ArcGISOAuthHybridInitialize(
    authenticatorState: AuthenticatorState,
    portalUrl: String,
    redirectUrl: String,
    clientId: String,
    clientSecret: String? = null,
): Boolean
```

### Description

This function provides a seamless authentication experience by first trying to authenticate silently with application credentials (if a `clientSecret` is provided). If this method is insufficient to access a resource, or if no `clientSecret` is given, it automatically falls back to the user authentication flow, prompting the user to log in via the `authenticatorState`.

The function configures both authentication methods and then verifies success by attempting to load a `Portal` object.

### Parameters

| Parameter          | Type                 | Description                                                                                                                            |
| ------------------ | -------------------- | -------------------------------------------------------------------------------------------------------------------------------------- |
| `authenticatorState` | `AuthenticatorState` | The state manager that handles the authentication UI for the user login fallback.                                                      |
| `portalUrl`        | `String`             | The URL of the ArcGIS Portal.                                                                                                          |
| `redirectUrl`      | `String`             | The OAuth Redirect URI for the user authentication fallback.                                                                           |
| `clientId`         | `String`             | The OAuth Client ID for your application.                                                                                              |
| `clientSecret`     | `String?`            | (Optional) The OAuth Client Secret. If provided, application-level authentication is attempted first. Defaults to `null`.                |

### Returns

`Boolean` - Returns `true` if authentication succeeds (either via application or user credentials). Returns `false` if all authentication attempts fail.

### Example

```kotlin
import androidx.lifecycle.viewModelScope
import com.arcgismaps.toolkit.authentication.AuthenticatorState
import kotlinx.coroutines.launch

// In your ViewModel
val authenticatorState = AuthenticatorState()

fun initializeHybridAuth() {
    viewModelScope.launch {
        val portalUrl = "https://www.arcgis.com"
        val clientId = "YOUR_CLIENT_ID"
        val clientSecret = "YOUR_CLIENT_SECRET" // Can be null
        val redirectUrl = "urn:ietf:wg:oauth:2.0:oob"

        val isAuthenticated = ArcGISOAuthHybridInitialize(
            authenticatorState = authenticatorState,
            portalUrl = portalUrl,
            redirectUrl = redirectUrl,
            clientId = clientId,
            clientSecret = clientSecret
        )

        if (isAuthenticated) {
            println("ArcGIS Hybrid Authentication Successful!")
            // App is authenticated via app or user credentials.
        } else {
            println("ArcGIS Hybrid Authentication Failed.")
        }
    }
}

// In your Composable UI, you would still need the Authenticator
// to handle the potential user login fallback.
// Authenticator(authenticatorState = viewModel.authenticatorState) {
//     // Your main app content
// }
```