Here is the high-quality SDK documentation for the provided code snippet.

***

# StaticHolder

An abstract generic class that provides a basic in-memory key-value store. It is designed to be extended by other classes that need to manage a collection of objects indexed by string identifiers.

### Signature

```kotlin
abstract class StaticHolder<ValueType>
```

### Description

`StaticHolder` serves as a base for creating simple caches or registries. It uses a `MutableMap` internally to store values of a generic type `ValueType` against a `String` key. It provides fundamental methods for adding, retrieving, checking, and removing items from the store.

## Methods

### has

Checks if a value with the specified ID exists in the store.

**Signature**
```kotlin
fun has(id: String): Boolean
```

**Parameters**

| Parameter | Type   | Description                  |
| :-------- | :----- | :--------------------------- |
| `id`      | `String` | The unique identifier for the value. |

**Returns**

`Boolean` — `true` if a value with the given `id` is found, `false` otherwise.

---

### get

Retrieves the value associated with the specified ID from the store.

**Signature**
```kotlin
fun get(id: String): ValueType?
```

**Parameters**

| Parameter | Type   | Description                  |
| :-------- | :----- | :--------------------------- |
| `id`      | `String` | The unique identifier for the value. |

**Returns**

`ValueType?` — The value associated with the `id`, or `null` if the `id` does not exist.

---

### set

Adds a new value to the store or updates the existing value for the specified ID.

**Signature**
```kotlin
fun set(id: String, viewHolder: ValueType)
```

**Parameters**

| Parameter    | Type        | Description                          |
| :----------- | :---------- | :----------------------------------- |
| `id`         | `String`    | The unique identifier for the value. |
| `viewHolder` | `ValueType` | The value to be stored.              |

---

### remove

Removes the value associated with the specified ID from the store.

**Signature**
```kotlin
fun remove(id: String)
```

**Parameters**

| Parameter | Type   | Description                  |
| :-------- | :----- | :--------------------------- |
| `id`      | `String` | The unique identifier for the value. |

---

### clearAll

Removes all values from the store, leaving it empty.

**Signature**
```kotlin
fun clearAll()
```

***

# MapViewHolderStoreBaseAsync

An abstract base class for managing a store of map view holders. It extends `StaticHolder` to provide storage functionality and introduces an asynchronous method to get or create map view holders.

### Signature

```kotlin
abstract class MapViewHolderStoreBaseAsync<
    TMapView,
    TMap,
    TOptions,
> : StaticHolder<MapViewHolderInterface<TMapView, TMap>>()
```

### Description

`MapViewHolderStoreBaseAsync` is designed to manage the lifecycle of map views, which can be resource-intensive to create. By extending `StaticHolder<MapViewHolderInterface<TMapView, TMap>>`, it inherits methods for storing, retrieving, and removing map view holders.

The key feature of this class is the `getOrCreate` abstract method, which ensures that map view holders are created asynchronously and only when necessary, promoting efficient resource usage.

### Generic Type Parameters

| Parameter  | Description                                                              |
| :--------- | :----------------------------------------------------------------------- |
| `TMapView` | The type of the native map view UI component (e.g., `MapView`).          |
| `TMap`     | The type of the underlying map object (e.g., `GoogleMap`, `MapboxMap`).    |
| `TOptions` | The type of the configuration options used to create a new map instance. |

### Inherited Methods

This class inherits all public methods from `StaticHolder<MapViewHolderInterface<TMapView, TMap>>`:

*   `has(id: String): Boolean`
*   `get(id: String): MapViewHolderInterface<TMapView, TMap>?`
*   `set(id: String, viewHolder: MapViewHolderInterface<TMapView, TMap>)`
*   `remove(id: String)`
*   `clearAll()`

## Abstract Methods

### getOrCreate

Asynchronously gets an existing `MapViewHolderInterface` from the store or creates a new one if it doesn't exist.

**Signature**
```kotlin
abstract suspend fun getOrCreate(
    context: Context,
    id: String,
    options: TOptions,
): MapViewHolderInterface<TMapView, TMap>
```

**Description**

This function first attempts to retrieve a `MapViewHolderInterface` using the provided `id`. If no holder is found, it proceeds to create a new one using the given `context` and `options`. The newly created holder is then stored and returned. This "get-or-create" pattern is ideal for managing shared map resources efficiently. As a `suspend` function, it must be called from a coroutine or another `suspend` function.

**Parameters**

| Parameter | Type      | Description                                                  |
| :-------- | :-------- | :----------------------------------------------------------- |
| `context` | `Context` | The Android `Context` required to create a new map view.     |
| `id`      | `String`  | The unique identifier for the map view holder.               |
| `options` | `TOptions`  | The configuration options to use if a new holder is created. |

**Returns**

`MapViewHolderInterface<TMapView, TMap>` — The existing or newly created map view holder.

### Example

Below is an example of how you might implement `MapViewHolderStoreBaseAsync` for a specific map provider.

```kotlin
// Assume these types are defined for a specific map SDK
class MyMapView
class MyMap
class MyMapOptions(val initialZoom: Double)
interface MapViewHolderInterface<TMapView, TMap> // From your library

// 1. Define a concrete MapViewHolder
class MyMapViewHolder(
    // ... constructor params
) : MapViewHolderInterface<MyMapView, MyMap> {
    // ... implementation
}

// 2. Implement the abstract store
class MyMapViewHolderStore : MapViewHolderStoreBaseAsync<MyMapView, MyMap, MyMapOptions>() {
    override suspend fun getOrCreate(
        context: Context,
        id: String,
        options: MyMapOptions,
    ): MapViewHolderInterface<MyMapView, MyMap> {
        // Try to get an existing holder first
        get(id)?.let {
            println("Returning existing map holder for id: $id")
            return it
        }

        // If it doesn't exist, create a new one (potentially a long-running task)
        println("Creating new map holder for id: $id with zoom: ${options.initialZoom}")
        val newHolder = withContext(Dispatchers.IO) {
            // Simulate heavy creation logic
            delay(500)
            MyMapViewHolder(/*...pass context and options...*/)
        }

        // Store the new holder before returning it
        set(id, newHolder)
        return newHolder
    }
}

// 3. Usage in your application
suspend fun setupMap(context: Context) {
    val mapStore = MyMapViewHolderStore()
    val mapId = "main_map"
    val mapOptions = MyMapOptions(initialZoom = 12.0)

    // First call: creates and returns a new holder
    val mapHolder1 = mapStore.getOrCreate(context, mapId, mapOptions)

    // Second call: returns the existing holder without creating a new one
    val mapHolder2 = mapStore.getOrCreate(context, mapId, mapOptions)

    // Check if they are the same instance
    println(mapHolder1 === mapHolder2) // Prints: true
}
```