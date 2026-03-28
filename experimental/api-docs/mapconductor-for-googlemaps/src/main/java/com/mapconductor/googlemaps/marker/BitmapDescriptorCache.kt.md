# BitmapDescriptorCache

A utility object that provides a cache for `BitmapDescriptor` instances. This helps improve performance and reduce memory churn by avoiding the recreation of `BitmapDescriptor` objects for identical bitmaps. It uses the `hashCode` of the input `Bitmap` as the key for efficient cache lookups.

This object is implemented as a thread-safe singleton using `ConcurrentHashMap`.

---

## Methods

### fromBitmap

Retrieves a `BitmapDescriptor` for the given `Bitmap`. It first checks the internal cache for an existing instance corresponding to the bitmap's hash code. If a cached instance is found, it is returned immediately. Otherwise, a new `BitmapDescriptor` is created, stored in the cache, and then returned.

#### Signature
```kotlin
fun fromBitmap(bitmap: Bitmap): BitmapDescriptor
```

#### Parameters
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `bitmap` | `Bitmap` | The `Bitmap` object to convert into a `BitmapDescriptor`. |

#### Returns
`BitmapDescriptor` - The cached or newly created `BitmapDescriptor` instance.

#### Example
```kotlin
// Assume 'myCustomBitmap' is a valid Bitmap object
val descriptor1 = BitmapDescriptorCache.fromBitmap(myCustomBitmap)

// This call will hit the cache and return the same instance as descriptor1
// without creating a new object.
val descriptor2 = BitmapDescriptorCache.fromBitmap(myCustomBitmap)

// Use the descriptor to add a marker to the map
googleMap.addMarker(
    MarkerOptions()
        .position(CLEVELAND)
        .title("Cached Marker")
        .icon(descriptor1)
)
```

---

### clearCache

Removes all entries from the `BitmapDescriptor` cache. This can be useful in low-memory situations or when you need to ensure that all descriptors are recreated on their next request.

#### Signature
```kotlin
fun clearCache()
```

#### Example
```kotlin
// Clear all cached BitmapDescriptors to free up memory.
BitmapDescriptorCache.clearCache()
```

---

### getCacheSize

Returns the current number of `BitmapDescriptor` instances stored in the cache. This method is primarily intended for debugging and monitoring purposes.

#### Signature
```kotlin
fun getCacheSize(): Int
```

#### Returns
`Int` - The total number of items currently in the cache.

#### Example
```kotlin
// Get the number of items in the cache
val currentCacheSize = BitmapDescriptorCache.getCacheSize()
Log.d("CacheInfo", "Current BitmapDescriptorCache size: $currentCacheSize")
```