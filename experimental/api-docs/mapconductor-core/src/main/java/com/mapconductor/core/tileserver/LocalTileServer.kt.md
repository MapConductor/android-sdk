# SDK Documentation: LocalTileServer

The `LocalTileServer` class provides a lightweight, in-process HTTP server for serving map tiles. It runs locally on `127.0.0.1` with an automatically assigned port, making it ideal for dynamically generating and displaying tile data in map SDKs without needing a backend deployment or exposing a public endpoint.

Tiles are generated on-the-fly by registering one or more `TileProviderInterface` implementations, each associated with a unique `routeId`.

## Companion Object

### startServer

Creates, configures, and starts a new `LocalTileServer` instance. This is the recommended factory method for instantiating the server.

**Signature**
```kotlin
fun startServer(forceNoStoreCache: Boolean = false): LocalTileServer
```

**Description**
This function initializes a `ServerSocket` on an available port, creates a `LocalTileServer` instance, and starts its request-handling thread.

**Parameters**

| Parameter           | Type      | Description                                                                                                                            |
| ------------------- | --------- | -------------------------------------------------------------------------------------------------------------------------------------- |
| `forceNoStoreCache` | `Boolean` | If `true`, all tile responses will include `Cache-Control: no-store` headers, preventing clients from caching tiles. Defaults to `false`. |

**Returns**

`LocalTileServer` - A running and configured instance of the tile server.

**Example**
```kotlin
// Start the server with default caching behavior
val tileServer = LocalTileServer.startServer()

// Start the server and force clients not to cache tiles
val noCacheTileServer = LocalTileServer.startServer(forceNoStoreCache = true)
```

---

## Properties

### baseUrl

The base URL of the running server.

**Signature**
```kotlin
val baseUrl: String
```

**Description**
This read-only property provides the root URL for the local server, in the format `http://127.0.0.1:<port>`. The port is chosen automatically by the system when the server is started. This URL is the foundation for all tile request URLs.

**Example**
```kotlin
val server = LocalTileServer.startServer()
println("Server running at: ${server.baseUrl}") 
// Output: Server running at: http://127.0.0.1:54321 (port will vary)
```

---

## Methods

### setForceNoStoreCache

Updates the server's caching behavior at runtime.

**Signature**
```kotlin
fun setForceNoStoreCache(value: Boolean)
```

**Description**
This method allows you to dynamically change whether the server instructs clients to cache tiles. When set to `true`, all subsequent tile responses will be sent with `Cache-Control: no-store` headers. When `false`, responses will include headers that encourage long-term caching.

**Parameters**

| Parameter | Type      | Description                                                                                                                            |
| --------- | --------- | -------------------------------------------------------------------------------------------------------------------------------------- |
| `value`   | `Boolean` | If `true`, disables client-side caching for new requests. If `false`, enables long-term caching (the default behavior is set at creation). |

**Example**
```kotlin
val server = LocalTileServer.startServer()

// Initially, tiles are cacheable.
// ...

// Now, force all new tile responses to be non-cacheable.
server.setForceNoStoreCache(true)
```

### register

Registers a tile provider for a specific route.

**Signature**
```kotlin
fun register(routeId: String, provider: TileProviderInterface)
```

**Description**
This method associates a `TileProviderInterface` implementation with a unique `routeId`. When the server receives a tile request URL containing this `routeId`, it will invoke the corresponding provider's `renderTile` method to generate the tile image.

**Parameters**

| Parameter  | Type                    | Description                                                                                             |
| ---------- | ----------------------- | ------------------------------------------------------------------------------------------------------- |
| `routeId`  | `String`                | A unique string to identify the tile source (e.g., "weather-radar", "traffic-flow").                    |
| `provider` | `TileProviderInterface` | An object that implements the logic for rendering a tile based on its coordinates (`x`, `y`, `z`). |

**Example**
```kotlin
// Assume MyRadarProvider implements TileProviderInterface
val radarProvider = MyRadarProvider()
val server = LocalTileServer.startServer()

server.register("radar", radarProvider)

// The server will now handle requests to URLs like /tiles/radar/...
```

### unregister

Removes a registered tile provider.

**Signature**
```kotlin
fun unregister(routeId: String)
```

**Description**
This method detaches a tile provider from its `routeId`. After calling this, any requests for that route will result in a `404 Not Found` error.

**Parameters**

| Parameter | Type     | Description                               |
| --------- | -------- | ----------------------------------------- |
| `routeId` | `String` | The identifier of the route to deregister. |

**Example**
```kotlin
// Unregister the provider previously registered with the "radar" routeId
server.unregister("radar")
```

### urlTemplate

Generates a URL template for a map SDK.

**Signature**
```kotlin
fun urlTemplate(routeId: String, tileSize: Int): String
```

**Description**
Constructs a standard URL template that can be used as a tile source in most map SDKs. The template includes placeholders for the zoom level (`{z}`), x-coordinate (`{x}`), and y-coordinate (`{y}`).

**Parameters**

| Parameter  | Type     | Description                                                                                             |
| ---------- | -------- | ------------------------------------------------------------------------------------------------------- |
| `routeId`  | `String` | The identifier of the registered route for which to generate the template.                              |
| `tileSize` | `Int`    | The tile size in pixels (e.g., 256 or 512). This value is included in the URL path for informational purposes. |

**Returns**

`String` - A URL template string, e.g., `http://127.0.0.1:12345/tiles/my-route/256/{z}/{x}/{y}.png`.

### urlTemplate (with cacheKey)

Generates a URL template with a cache-busting key in the path.

**Signature**
```kotlin
fun urlTemplate(routeId: String, tileSize: Int, cacheKey: String): String
```

**Description**
This overload constructs a URL template that includes an extra `cacheKey` segment in the path. This is a common technique for cache-busting. When the underlying data for a tileset changes, you can generate a new template with a new `cacheKey` (e.g., a timestamp or content hash) to force clients to refetch the tiles.

**Parameters**

| Parameter  | Type     | Description                                                                |
| ---------- | -------- | -------------------------------------------------------------------------- |
| `routeId`  | `String` | The identifier of the registered route.                                    |
| `tileSize` | `Int`    | The tile size in pixels (e.g., 256 or 512).                                |
| `cacheKey` | `String` | A unique string to embed in the URL path for cache-busting purposes.       |

**Returns**

`String` - A URL template string, e.g., `http://127.0.0.1:12345/tiles/my-route/256/v2_20231027/{z}/{x}/{y}.png`.

### urlTemplateWithQueryCacheKey

Generates a URL template with a cache-busting key as a query parameter.

**Signature**
```kotlin
fun urlTemplateWithQueryCacheKey(routeId: String, tileSize: Int, cacheKey: String): String
```

**Description**
This method provides an alternative format for a cache-busting URL template. Instead of adding the `cacheKey` to the URL path, it appends it as a query parameter (e.g., `?v=some-key`). This is useful for compatibility with map SDKs that have strict parsing rules for URL templates and may not support extra path segments.

**Parameters**

| Parameter  | Type     | Description                                                                |
| ---------- | -------- | -------------------------------------------------------------------------- |
| `routeId`  | `String` | The identifier of the registered route.                                    |
| `tileSize` | `Int`    | The tile size in pixels (e.g., 256 or 512).                                |
| `cacheKey` | `String` | A unique string to use as the value of the `v` query parameter.            |

**Returns**

`String` - A URL template string, e.g., `http://127.0.0.1:12345/tiles/my-route/256/{z}/{x}/{y}.png?v=v2_20231027`.

### start

Starts the server's request-handling loop.

**Signature**
```kotlin
fun start()
```

**Description**
This method starts the background thread that listens for and handles incoming HTTP requests. It is called automatically by the `startServer()` factory method, so you typically do not need to call it manually. If the server is already running, this method has no effect.

### stop

Stops the server and releases its resources.

**Signature**
```kotlin
fun stop()
```

**Description**
This method gracefully shuts down the tile server. It closes the underlying `ServerSocket`, which interrupts the request-handling thread and causes it to terminate. Once stopped, a `LocalTileServer` instance cannot be restarted. It's important to call this method to release the network port when the server is no longer needed.

**Example**
```kotlin
val server = LocalTileServer.startServer()
try {
    // Use the server...
    val url = server.urlTemplate("my-route", 256)
    // myMap.addTileSource(url)
} finally {
    // Ensure the server is always stopped
    server.stop()
}
```