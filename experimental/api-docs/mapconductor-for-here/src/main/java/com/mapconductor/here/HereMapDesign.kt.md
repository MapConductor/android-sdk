Of course! Here is a high-quality SDK document for the provided Kotlin code snippet.

---

# HereMapDesign

## `HereMapDesign`

A sealed class that encapsulates the various map visual styles (schemes) available in the HERE SDK. Each object within this class represents a specific `MapScheme` and provides a type-safe way to manage and apply map designs.

This class implements the `HereMapDesignType` interface, which standardizes the handling of map designs.

### Available Map Designs

The following singleton objects represent the available map designs.

| Object              | Corresponding `MapScheme`     | Description                                                              |
| ------------------- | ----------------------------- | ------------------------------------------------------------------------ |
| `NormalDay`         | `MapScheme.NORMAL_DAY`        | The standard map style for daytime viewing.                              |
| `NormalNight`       | `MapScheme.NORMAL_NIGHT`      | The standard map style for nighttime viewing.                            |
| `Satellite`         | `MapScheme.SATELLITE`         | A map style that displays satellite imagery.                             |
| `HybridDay`         | `MapScheme.HYBRID_DAY`        | Combines satellite imagery with road network and label overlays for daytime. |
| `HybridNight`       | `MapScheme.HYBRID_NIGHT`      | Combines satellite imagery with road network and label overlays for nighttime. |
| `LiteDay`           | `MapScheme.LITE_DAY`          | A lightweight, performance-optimized map style for daytime.              |
| `LiteNight`         | `MapScheme.LITE_NIGHT`        | A lightweight, performance-optimized map style for nighttime.            |
| `LiteHybridDay`     | `MapScheme.LITE_HYBRID_DAY`   | A lightweight hybrid map style for daytime.                              |
| `LiteHybridNight`   | `MapScheme.LITE_HYBRID_NIGHT` | A lightweight hybrid map style for nighttime.                            |
| `LogisticsDay`      | `MapScheme.LOGISTICS_DAY`     | A map style optimized for logistics and trucking applications for daytime. |
| `LogisticsNight`    | `MapScheme.LOGISTICS_NIGHT`   | A map style optimized for logistics and trucking applications for nighttime. |
| `LogisticsHybridDay`| `MapScheme.LOGISTICS_HYBRID_DAY`| A hybrid map style optimized for logistics applications for daytime.     |
| `RoadNetworkDay`    | `MapScheme.ROAD_NETWORK_DAY`  | A map style that displays only the road network for daytime.             |
| `RoadNetworkNight`  | `MapScheme.ROAD_NETWORK_NIGHT`| A map style that displays only the road network for nighttime.           |

### Methods

#### `getValue()`

Retrieves the underlying HERE SDK `MapScheme` enum associated with the `HereMapDesign` instance.

**Signature**
```kotlin
fun getValue(): MapScheme
```

**Returns**
| Type        | Description                               |
| ----------- | ----------------------------------------- |
| `MapScheme` | The corresponding `MapScheme` enum value. |

---

## Companion Object

Provides factory methods to create `HereMapDesign` instances.

### `CreateById()`

Creates a `HereMapDesign` instance from its corresponding integer ID.

**Signature**
```kotlin
fun CreateById(id: Int): HereMapDesign
```

**Parameters**
| Parameter | Type  | Description                                                              |
| --------- | ----- | ------------------------------------------------------------------------ |
| `id`      | `Int` | The integer identifier of the map scheme, corresponding to `MapScheme.value`. |

**Returns**
| Type            | Description                                                                                             |
| --------------- | ------------------------------------------------------------------------------------------------------- |
| `HereMapDesign` | The `HereMapDesign` object that corresponds to the given ID.                                            |

**Throws**
| Exception                  | Condition                                          |
| -------------------------- | -------------------------------------------------- |
| `IllegalArgumentException` | If the provided `id` does not match any supported map scheme. |

### `Create()`

Creates a `HereMapDesign` instance from a `MapScheme` enum value.

**Signature**
```kotlin
fun Create(id: MapScheme): HereMapDesign
```

**Parameters**
| Parameter | Type        | Description                        |
| --------- | ----------- | ---------------------------------- |
| `id`      | `MapScheme` | The `MapScheme` enum value to use. |

**Returns**
| Type            | Description                                                                                             |
| --------------- | ------------------------------------------------------------------------------------------------------- |
| `HereMapDesign` | The `HereMapDesign` object that corresponds to the given `MapScheme`.                                   |

**Throws**
| Exception                  | Condition                                          |
| -------------------------- | -------------------------------------------------- |
| `IllegalArgumentException` | If the provided `MapScheme` is not supported.      |

---

## `HereMapDesignType`

A type alias for a generic map design interface, specialized for the HERE SDK.

**Signature**
```kotlin
typealias HereMapDesignType = MapDesignTypeInterface<MapScheme>
```

**Description**
This alias simplifies the use of `MapDesignTypeInterface` by fixing its generic type to `MapScheme`, making it specific to the HERE map implementation.

---

## Example

The following examples demonstrate how to use `HereMapDesign` to set a map's visual style.

```kotlin
import com.here.sdk.mapview.MapView
import com.mapconductor.here.HereMapDesign

// Assume 'mapView' is an initialized instance of com.here.sdk.mapview.MapView

// --- Usage 1: Applying a map scheme directly ---
// Use one of the predefined objects to set the map scheme.
val dayDesign = HereMapDesign.NormalDay
mapView.mapScene.loadScene(dayDesign.getValue()) { mapError ->
    if (mapError == null) {
        println("Successfully loaded NormalDay scheme.")
    } else {
        println("Error loading scene: ${mapError.name}")
    }
}

// --- Usage 2: Creating a design from a MapScheme enum ---
// This is useful when you have a MapScheme from another part of your application.
val satelliteScheme = com.here.sdk.mapview.MapScheme.SATELLITE
val satelliteDesign = HereMapDesign.Create(satelliteScheme)
mapView.mapScene.loadScene(satelliteDesign.getValue(), null)


// --- Usage 3: Creating a design from an integer ID ---
// This can be useful for deserialization or when working with legacy code.
val schemeId = com.here.sdk.mapview.MapScheme.HYBRID_NIGHT.value
try {
    val hybridNightDesign = HereMapDesign.CreateById(schemeId)
    mapView.mapScene.loadScene(hybridNightDesign.getValue(), null)
    println("Successfully created design from ID: $schemeId")
} catch (e: IllegalArgumentException) {
    println("Error: ${e.message}")
}
```