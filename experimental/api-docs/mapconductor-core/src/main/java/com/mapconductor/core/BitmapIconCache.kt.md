Excellent. Here is the high-quality SDK documentation for the provided `BitmapIconCache` code snippet.

# BitmapIconCache

The `BitmapIconCache` is a singleton object that provides an in-memory cache for `BitmapIcon` objects. It is designed to optimize memory usage and improve performance by reusing bitmap icons instead of recreating them.

The cache employs a two-level strategy:
1.  **LruCache**: An underlying `LruCache` (Least Recently Used) stores the `BitmapIcon` objects. It automatically evicts the least recently used items when the cache exceeds its designated memory limit (1/8th of the application's maximum memory).
2.  **Reference Counting**: A reference counting mechanism tracks how many components are actively using a specific icon. An icon is only added to the `LruCache` on its first `put` call and is only removed from the cache when its reference count drops to zero. This prevents actively used icons from being evicted from memory.

This object is thread-safe for the operations it exposes, but the underlying `LruCache` has its own synchronization behavior.

---

## `put`

Adds a `BitmapIcon` to the cache or increments its reference count if it already exists.

If the icon with the specified `id` is not already in the cache, it will be added to the `LruCache` and its reference count will be initialized to 1. If the icon already exists, this method simply increments its reference count without re-adding it to the cache.

### Signature

```kotlin
fun put(id: Int, bitmapIcon: BitmapIcon)
```

### Parameters

| Parameter    | Type         | Description                                |
| :----------- | :----------- | :----------------------------------------- |
| `id`         | `Int`        | The unique identifier for the bitmap icon. |
| `bitmapIcon` | `BitmapIcon` | The `BitmapIcon` object to cache.          |

### Example

```kotlin
// Assume createBitmapIcon() is a function that returns a BitmapIcon instance
val newIcon = createBitmapIcon(R.drawable.my_icon)
val iconId = 101

// Add the icon to the cache for the first time
BitmapIconCache.put(iconId, newIcon)
```

---

## `get`

Retrieves a `BitmapIcon` from the cache by its identifier.

### Signature

```kotlin
fun get(id: Int): BitmapIcon?
```

### Parameters

| Parameter | Type  | Description                                  |
| :-------- | :---- | :------------------------------------------- |
| `id`      | `Int` | The unique identifier of the icon to retrieve. |

### Returns

**`BitmapIcon?`**

The cached `BitmapIcon` object if it exists, or `null` if no icon with the specified `id` is found in the cache.

### Example

```kotlin
val iconId = 101
val cachedIcon = BitmapIconCache.get(iconId)

if (cachedIcon != null) {
    // Use the cached icon
    myMarker.setIcon(cachedIcon)
} else {
    // Icon not in cache, create it and put it in the cache
    val newIcon = createBitmapIcon(R.drawable.my_icon)
    BitmapIconCache.put(iconId, newIcon)
    myMarker.setIcon(newIcon)
}
```

---

## `refCountUp`

Manually increments the reference count for a cached icon.

This is useful when a new component begins using an icon that is already in the cache, ensuring it won't be removed prematurely.

### Signature

```kotlin
fun refCountUp(id: Int)
```

### Parameters

| Parameter | Type  | Description                                                        |
| :-------- | :---- | :----------------------------------------------------------------- |
| `id`      | `Int` | The unique identifier of the icon to increment the reference count for. |

### Example

```kotlin
// Another part of the app needs to use the same icon
val iconId = 101
BitmapIconCache.refCountUp(iconId)
```

---

## `refCountDown`

Decrements the reference count for a cached icon. If the reference count drops to zero, the icon is removed from the cache.

This method should be called when a component is finished using an icon, allowing the cache to free up memory if the icon is no longer needed by any other component.

### Signature

```kotlin
fun refCountDown(id: Int)
```

### Parameters

| Parameter | Type  | Description                                                        |
| :-------- | :---- | :----------------------------------------------------------------- |
| `id`      | `Int` | The unique identifier of the icon to decrement the reference count for. |

### Example

```kotlin
// A component that was using an icon is being destroyed
val iconId = 101
BitmapIconCache.refCountDown(iconId)
```

---

## `clear`

Removes all icons from the cache and resets all reference counts.

This is a destructive operation that completely empties the cache. It can be useful in low-memory situations or when a significant part of the application's UI is being torn down.

### Signature

```kotlin
@Keep
fun clear()
```

### Example

```kotlin
// For example, in your Activity's onDestroy or onLowMemory method
override fun onLowMemory() {
    super.onLowMemory()
    // Clear the entire icon cache to free up memory
    BitmapIconCache.clear()
}
```