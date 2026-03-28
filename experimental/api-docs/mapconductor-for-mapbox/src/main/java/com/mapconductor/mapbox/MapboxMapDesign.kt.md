Of course! Here is the high-quality SDK documentation for the provided Kotlin code snippet.

---

# MapboxMapDesign SDK Documentation

This document provides detailed information about the `MapboxMapDesign` sealed class and its related components, which are used to represent and manage Mapbox map styles within the SDK.

## `MapboxDesignType`

### Signature
```kotlin
typealias MapboxDesignType = MapDesignTypeInterface<String>
```

### Description
A type alias for `MapDesignTypeInterface<String>`. It standardizes the representation of a map design type for Mapbox, where the underlying value is a `String` representing the style URL.

---

## `MapboxMapDesign`

### Signature
```kotlin
sealed class MapboxMapDesign(
    override val id: String,
) : MapboxDesignType
```

### Description
A sealed class that represents a specific Mapbox map style. It provides a set of predefined, commonly used Mapbox styles and allows for the use of custom styles through the `Custom` class. Each design corresponds to a Mapbox Style URL.

### Predefined Styles
The `MapboxMapDesign` class includes several predefined objects for standard Mapbox styles.

| Object Name         | Style ID                  | Description                               |
| ------------------- | ------------------------- | ----------------------------------------- |
| `Standard`          | `standard`                | The standard Mapbox map style.            |
| `StandardSatellite` | `standard-satellite`      | The standard satellite style with labels. |
| `Streets`           | `streets-v12`             | The Mapbox Streets style.                 |
| `Outdoors`          | `outdoors-v12`            | The Mapbox Outdoors style.                |
| `Light`             | `light-v11`               | The Mapbox Light style.                   |
| `Dark`              | `dark-v11`                | The Mapbox Dark style.                    |
| `Satellite`         | `satellite-v9`            | The Mapbox Satellite style.               |
| `SatelliteStreets`  | `satellite-streets-v12`   | The Mapbox Satellite Streets style.       |
| `NavigationDay`     | `navigation-day-v1`       | The Mapbox Navigation Day style.          |
| `NavigationNight`   | `navigation-night-v1`     | The Mapbox Navigation Night style.        |

### `Custom` Class

#### Signature
```kotlin
class Custom(layerId: String) : MapboxMapDesign(layerId)
```

#### Description
Represents a custom or non-predefined Mapbox style. Use this class when you need to load a style that is not one of the standard options, such as a style created in your Mapbox account.

#### Parameters
| Parameter | Type     | Description                                                              |
| --------- | -------- | ------------------------------------------------------------------------ |
| `layerId` | `String` | The unique identifier of the custom Mapbox style (e.g., `your-user/ck...`). |

---

## Functions

### `getValue()`

#### Signature
```kotlin
override fun getValue(): String
```

#### Description
Returns the complete Mapbox style URL for the given map design. This URL is formatted as `mapbox://styles/mapbox/{id}` for standard styles or `mapbox://styles/{id}` for custom styles (where `id` includes the user, e.g., `username/style-id`).

#### Returns
| Type     | Description                               |
| -------- | ----------------------------------------- |
| `String` | The fully qualified Mapbox style URL. |

#### Example
```kotlin
val streetsUrl = MapboxMapDesign.Streets.getValue()
// streetsUrl is "mapbox://styles/mapbox/streets-v12"

val customUrl = MapboxMapDesign.Custom("my-user/c1k2e3f4g5").getValue()
// customUrl is "mapbox://styles/mapbox/my-user/c1k2e3f4g5"
```

### `MapboxMapDesign.Create()`

#### Signature
```kotlin
fun Create(layerId: String): MapboxMapDesign
```

#### Description
A factory function that creates a `MapboxMapDesign` instance from a style ID string. If the `layerId` matches one of the predefined styles, it returns the corresponding singleton object. Otherwise, it returns a new `Custom` instance.

#### Parameters
| Parameter | Type     | Description                                                              |
| --------- | -------- | ------------------------------------------------------------------------ |
| `layerId` | `String` | The style ID to create the `MapboxMapDesign` from (e.g., `streets-v12`). |

#### Returns
| Type              | Description                                                                                             |
| ----------------- | ------------------------------------------------------------------------------------------------------- |
| `MapboxMapDesign` | The corresponding predefined `MapboxMapDesign` object or a `Custom` instance if the ID does not match. |

#### Example
```kotlin
// Creates a predefined style object
val streetsDesign = MapboxMapDesign.Create("streets-v12") // Returns MapboxMapDesign.Streets

// Creates a custom style instance
val customDesign = MapboxMapDesign.Create("my-user/c1k2e3f4g5") // Returns Custom("my-user/c1k2e3f4g5")
```

### `Style.toMapDesignType()`

#### Signature
```kotlin
fun Style.toMapDesignType(): MapboxDesignType
```

#### Description
An extension function for the Mapbox `Style` class. It converts a `Style` object into its corresponding `MapboxMapDesign` representation by parsing the `styleURI`. This is useful for determining which `MapboxMapDesign` is currently active on the map.

#### Returns
| Type               | Description                                                                                                                            |
| ------------------ | -------------------------------------------------------------------------------------------------------------------------------------- |
| `MapboxDesignType` | The `MapboxMapDesign` that corresponds to the `Style` object's URI. Returns a `Custom` instance if the URI does not match a predefined style. |

#### Example
```kotlin
// Assuming 'mapboxMap' is an instance of the MapboxMap object
mapboxMap.getStyle { style ->
    // Convert the current map style to a MapboxDesignType
    val currentDesign = style.toMapDesignType()

    when (currentDesign) {
        is MapboxMapDesign.Dark -> {
            // The current map style is Dark
        }
        is MapboxMapDesign.Custom -> {
            // The current map style is a custom one
            println("Custom style ID: ${currentDesign.id}")
        }
        else -> {
            // Handle other styles
        }
    }
}
```