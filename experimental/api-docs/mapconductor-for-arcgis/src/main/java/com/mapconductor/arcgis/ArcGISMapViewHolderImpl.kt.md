Of course! Here is the high-quality SDK documentation for the provided code snippet, formatted in Markdown.

***

## WrapSceneView

A custom `FrameLayout` that serves as a wrapper for the ArcGIS `SceneView`. This class is designed to integrate the `SceneView` into a view hierarchy and manage its lifecycle.

The class provides standard Android `View` constructors and delegates lifecycle events from a `LifecycleOwner` (like an `Activity` or `Fragment`) to the underlying `SceneView`.

### Lifecycle Management

The following methods must be called from the corresponding lifecycle callbacks of the hosting `Activity` or `Fragment` to ensure the proper functioning of the `SceneView`.

#### onCreate(owner)

Forwards the `onCreate` lifecycle event to the `SceneView`. This should be called within the `onCreate` method of the hosting `Activity` or `Fragment`.

**Signature**
```kotlin
fun onCreate(owner: LifecycleOwner)
```

**Description**
Initializes the `SceneView` and prepares it for use.

**Parameters**
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `owner` | `LifecycleOwner` | The lifecycle owner (e.g., `Activity`, `Fragment`) whose state is being managed. |

---

#### onPause(owner)

Forwards the `onPause` lifecycle event to the `SceneView`. This should be called within the `onPause` method of the hosting `Activity` or `Fragment`.

**Signature**
```kotlin
fun onPause(owner: LifecycleOwner)
```

**Description**
Pauses the `SceneView`, stopping rendering and other active processes to conserve resources when the view is not in the foreground.

**Parameters**
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `owner` | `LifecycleOwner` | The lifecycle owner whose state is being managed. |

---

#### onResume(owner)

Forwards the `onResume` lifecycle event to the `SceneView`. This should be called within the `onResume` method of the hosting `Activity` or `Fragment`.

**Signature**
```kotlin
fun onResume(owner: LifecycleOwner)
```

**Description**
Resumes the `SceneView` after it has been paused, restarting rendering and other processes.

**Parameters**
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `owner` | `LifecycleOwner` | The lifecycle owner whose state is being managed. |

---

#### onStop(owner)

Forwards the `onStop` lifecycle event to the `SceneView`. This should be called within the `onStop` method of the hosting `Activity` or `Fragment`.

**Signature**
```kotlin
fun onStop(owner: LifecycleOwner)
```

**Description**
Stops the `SceneView` when it is no longer visible to the user.

**Parameters**
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `owner` | `LifecycleOwner` | The lifecycle owner whose state is being managed. |

---

#### onDestroy(owner)

Forwards the `onDestroy` lifecycle event to the `SceneView`. This should be called within the `onDestroy` method of the hosting `Activity` or `Fragment`.

**Signature**
```kotlin
fun onDestroy(owner: LifecycleOwner)
```

**Description**
Cleans up and releases all resources used by the `SceneView`. This is a final cleanup step.

**Parameters**
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `owner` | `LifecycleOwner` | The lifecycle owner whose state is being managed. |

***

## ArcGISMapViewHolder

An adapter class that implements the `MapViewHolderInterface`. It acts as a bridge between a generic map interface and the specific ArcGIS `SceneView` implementation, providing methods for coordinate transformations.

### toScreenOffset(position)

Converts a geographic coordinate (`GeoPointInterface`) to a screen coordinate (`Offset`).

**Signature**
```kotlin
fun toScreenOffset(position: GeoPointInterface): Offset?
```

**Description**
This function takes a geographic point and projects it onto the screen space of the `SceneView`, returning the corresponding pixel coordinates.

**Parameters**
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `position` | `GeoPointInterface` | The geographic point (latitude, longitude, and optional altitude) to convert. |

**Returns**
`Offset?` — The corresponding screen `Offset` (x, y coordinates), or `null` if the conversion is not possible (e.g., the point is not visible on the screen).

---

### fromScreenOffset(offset)

Asynchronously converts a screen coordinate (`Offset`) to a geographic coordinate (`GeoPoint`).

**Signature**
```kotlin
suspend fun fromScreenOffset(offset: Offset): GeoPoint?
```

**Description**
This suspend function takes a screen pixel coordinate and performs a reverse projection to find the corresponding geographic coordinate on the 3D scene. Because this can be a computationally intensive operation, it is performed asynchronously.

**Parameters**
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `offset` | `Offset` | The screen offset (x, y pixel coordinates) to convert. |

**Returns**
`GeoPoint?` — The corresponding `GeoPoint`, or `null` if the screen coordinate does not map to a location on the scene.

---

### fromScreenOffsetSync(offset)

Synchronously converts a screen coordinate (`Offset`) to a geographic coordinate (`GeoPoint`).

**Signature**
```kotlin
fun fromScreenOffsetSync(offset: Offset): GeoPoint?
```

**Description**
This function is a synchronous wrapper around `fromScreenOffset`. It blocks the calling thread until the conversion is complete. It is useful when you need the result immediately and are not on the main thread. **Warning:** Calling this on the main UI thread will cause it to freeze and may lead to an "Application Not Responding" (ANR) error.

**Parameters**
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `offset` | `Offset` | The screen offset (x, y pixel coordinates) to convert. |

**Returns**
`GeoPoint?` — The corresponding `GeoPoint`, or `null` if the screen coordinate does not map to a location on the scene.

### Example

```kotlin
// Assuming 'mapViewHolder' is an instance of ArcGISMapViewHolder
// and this code is run within a coroutine scope.

// 1. Convert a geographic point to a screen offset
val geoPoint = GeoPoint(latitude = 34.0562, longitude = -117.1956) // A point in Redlands, CA
val screenOffset = mapViewHolder.toScreenOffset(geoPoint)

screenOffset?.let {
    println("Screen coordinates: x=${it.x}, y=${it.y}")
}

// 2. Convert a screen offset back to a geographic point
val someScreenOffset = Offset(500f, 800f)
val newGeoPoint = mapViewHolder.fromScreenOffset(someScreenOffset)

newGeoPoint?.let {
    println("Geographic coordinates: lat=${it.latitude}, lon=${it.longitude}")
}
```

***

## getArcGisApiKey()

An internal extension function on `Context` that retrieves the ArcGIS API key from the application's manifest metadata.

**Signature**
```kotlin
internal fun Context.getArcGisApiKey(): String?
```

**Description**
This utility function simplifies retrieving the ArcGIS API key required for using ArcGIS services. It looks for a `<meta-data>` tag with the name `ARCGIS_API_KEY` within the `<application>` tag of your `AndroidManifest.xml`.

**Returns**
`String?` — The ArcGIS API key if found in the manifest's metadata, otherwise `null`.

### Example

To use this function, you must first add your API key to the `AndroidManifest.xml` file.

**1. Add API Key to `AndroidManifest.xml`**

Place the following `<meta-data>` tag inside the `<application>` tag.

```xml
<!-- AndroidManifest.xml -->
<application
    ... >

    <meta-data
        android:name="ARCGIS_API_KEY"
        android:value="YOUR_API_KEY" />

    ...
</application>
```

**2. Retrieve the Key in Code**

You can then call the function on a `Context` instance.

```kotlin
// Inside an Activity, Fragment, or any class with access to a Context
val apiKey = context.getArcGisApiKey()

if (apiKey != null) {
    // Use the API key to set up ArcGIS services
    ArcGISRuntimeEnvironment.setApiKey(apiKey)
} else {
    // Handle the case where the API key is not found
    Log.e("ArcGISSetup", "ARCGIS_API_KEY not found in AndroidManifest.xml")
}
```