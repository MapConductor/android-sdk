Of course! Here is the high-quality SDK documentation for the provided Kotlin code snippet.

***

### `MapLibreMapDesignTypeInterface`

An interface that defines the contract for a MapLibre map design.

#### Signature

```kotlin
interface MapLibreMapDesignTypeInterface : MapDesignTypeInterface<String>
```

#### Description

This interface establishes the core requirements for any class representing a MapLibre map style. It ensures that a map design provides a URL to its style JSON file, which is essential for rendering the map. It extends the base `MapDesignTypeInterface<String>`.

#### Properties

| Property       | Type     | Description                                          |
|----------------|----------|------------------------------------------------------|
| `styleJsonURL` | `String` | The URL pointing to the MapLibre style JSON file.    |

***

### `MapLibreDesign`

A data class that represents a specific map design for a MapLibre-based map, including pre-defined common styles.

#### Signature

```kotlin
data class MapLibreDesign(
    override val id: String,
    override val styleJsonURL: String,
) : MapLibreMapDesignTypeInterface
```

#### Description

The `MapLibreDesign` class is a concrete implementation of `MapLibreMapDesignTypeInterface`. It encapsulates the essential properties of a map style: a unique identifier (`id`) and the URL to its style definition file (`styleJsonURL`). This class is used to configure the visual appearance of the map.

It also provides a `companion object` with a set of pre-configured, static instances for common map styles, making it easy to switch between them.

#### Parameters

These parameters are used in the constructor to create a new `MapLibreDesign` instance.

| Parameter      | Type     | Description                                               |
|----------------|----------|-----------------------------------------------------------|
| `id`           | `String` | A unique identifier for the map design.                   |
| `styleJsonURL` | `String` | The URL pointing to the MapLibre style JSON configuration. |

### Methods

#### `getValue()`

Returns a serialized string representation of the map design.

**Signature**
```kotlin
fun getValue(): String
```

**Description**
This method combines the `id` and `styleJsonURL` into a single string. This format can be useful for logging, caching, or other internal identification purposes.

**Returns**

| Type     | Description                                                    |
|----------|----------------------------------------------------------------|
| `String` | A string formatted as `"mapDesign_id=<id>,style=<styleJsonURL>"`. |

### Pre-defined Styles

The `MapLibreDesign` companion object provides several static properties for commonly used map styles. You can use these directly without needing to instantiate `MapLibreDesign` manually.

| Property            | Description                                                              |
|---------------------|--------------------------------------------------------------------------|
| `DemoTiles`         | The default demo style from MapLibre.org.                                |
| `MapTilerTonerJa`   | A high-contrast, black-and-white "toner" style with Japanese labels.     |
| `MapTilerTonerEn`   | A high-contrast, black-and-white "toner" style with English labels.      |
| `OsmBright`         | A colorful, general-purpose style based on OpenStreetMap data.           |
| `OsmBrightEn`       | The "OSM Bright" style with English labels.                              |
| `OsmBrightJa`       | The "OSM Bright" style with Japanese labels.                             |
| `MapTilerBasicEn`   | A basic, general-purpose map style with English labels.                  |
| `OpenMapTiles`      | The default vector tile style from OpenMapTiles.                         |
| `MapTilerBasicJa`   | A basic, general-purpose map style with Japanese labels.                 |

### Example

Here’s how to use both pre-defined and custom `MapLibreDesign` instances.

```kotlin
import com.mapconductor.maplibre.MapLibreDesign

fun main() {
    // 1. Using a pre-defined map style
    val tonerMapStyle = MapLibreDesign.MapTilerTonerEn
    println("Using pre-defined style: ${tonerMapStyle.id}")
    // Output: Using pre-defined style: maptiler-toner-en
    
    println("Style URL: ${tonerMapStyle.styleJsonURL}")
    // Output: Style URL: https://tile.openstreetmap.jp/styles/maptiler-toner-en/style.json

    // 2. Creating a custom map design instance
    val customMapStyle = MapLibreDesign(
        id = "my-custom-dark-mode",
        styleJsonURL = "https://example.com/styles/dark-v10.json"
    )
    println("\nCustom style ID: ${customMapStyle.id}")
    // Output: Custom style ID: my-custom-dark-mode

    // 3. Getting the serialized value from the custom style
    val serializedValue = customMapStyle.getValue()
    println("Serialized value: $serializedValue")
    // Output: Serialized value: mapDesign_id=my-custom-dark-mode,style=https://example.com/styles/dark-v10.json
}
```