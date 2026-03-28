# TileServerRegistry

A singleton object that manages the lifecycle of a single `LocalTileServer` instance. It ensures that only one tile server is running within the application, providing a centralized point of access and configuration. This registry is thread-safe.

---

## get

Retrieves the singleton `LocalTileServer` instance. If the server is not already running, this method will start it and return the new instance. If a server is already running, it returns the existing instance.

### Signature

```kotlin
fun get(forceNoStoreCache: Boolean = this.forceNoStoreCache): LocalTileServer
```

### Description

This is the primary method for accessing the `LocalTileServer`. It guarantees that only one server instance exists. The method also allows you to configure the server's caching behavior on retrieval.

### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `forceNoStoreCache` | `Boolean` | **(Optional)** If `true`, configures the tile server to include a `Cache-Control: no-store` header in its responses, preventing downstream clients from caching tiles. Defaults to the last value set by a previous call to `get()` or `setForceNoStoreCache()`. |

### Returns

| Type | Description |
| :--- | :--- |
| `LocalTileServer` | The singleton `LocalTileServer` instance. |

### Example

```kotlin
// Get the tile server instance with default cache settings
val tileServer = TileServerRegistry.get()

// Get the tile server and force no-store caching for its responses
val noCacheTileServer = TileServerRegistry.get(forceNoStoreCache = true)
```

---

## setForceNoStoreCache

Updates the caching behavior for the active `LocalTileServer` instance. This allows you to dynamically enable or disable client-side caching of tiles.

### Signature

```kotlin
fun setForceNoStoreCache(value: Boolean)
```

### Description

This method updates the `forceNoStoreCache` flag for the current `LocalTileServer` instance, if one exists. It also sets the default value for future calls to `get()`.

### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `value` | `Boolean` | If `true`, the server will add a `Cache-Control: no-store` header to its responses. If `false`, it will not. |

### Example

```kotlin
// Disable client-side caching for all subsequent tile requests
TileServerRegistry.setForceNoStoreCache(true)

// Re-enable default client-side caching behavior
TileServerRegistry.setForceNoStoreCache(false)
```

---

## warmup

Pre-initializes the tile server on a background thread and makes a warmup HTTP request.

### Signature

```kotlin
fun warmup()
```

### Description

Call this method early in your application's lifecycle (e.g., in `Application.onCreate()`) to reduce the initial latency when a map layer that uses the tile server is first displayed.

The function performs the following actions on a background thread:
1.  Starts the `LocalTileServer` if it's not already running.
2.  Makes a dummy HTTP request to the server to establish the connection.

This process is executed only once, even if `warmup()` is called multiple times. Any errors during warmup are logged and do not crash the application.

### Example

```kotlin
// In your Application class
import android.app.Application
import com.mapconductor.core.tileserver.TileServerRegistry

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Warm up the tile server to reduce first-load latency for map layers.
        TileServerRegistry.warmup()
    }
}
```