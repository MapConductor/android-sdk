Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

# ArcGISDesign

The `ArcGISDesign` class and its companion object provide a structured way to define and manage map styles for an ArcGIS map. It encapsulates predefined basemap styles from both ArcGIS and OpenStreetMap (OSM), and allows for customization with elevation data.

## `ArcGISDesign` Class

A data class that represents a specific ArcGIS or OSM basemap style. It holds the unique identifier for the style and a list of associated elevation sources.

### Signature

```kotlin
data class ArcGISDesign(
    override val id: String,
    override val elevationSources: List<String> = emptyList<String>(),
) : ArcGISDesignTypeInterface
```

### Properties

| Property          | Type                | Description                                                                 |
| :---------------- | :------------------ | :-------------------------------------------------------------------------- |
| `id`              | `String`            | The unique string identifier for the map design (e.g., `"arc_gis_streets"`). |
| `elevationSources`| `List<String>`      | A list of URLs for elevation data sources to be applied to the map.        |

### Methods

#### `withElevationSources`

Creates a new `ArcGISDesign` instance by copying the current design and applying a new list of elevation sources. This method follows the immutable object pattern.

**Signature**
```kotlin
fun withElevationSources(sources: List<String>): ArcGISDesign
```

**Parameters**
| Parameter | Type           | Description                                    |
| :-------- | :------------- | :--------------------------------------------- |
| `sources` | `List<String>` | A list of URLs for the new elevation sources. |

**Returns**
| Type            | Description                                                              |
| :-------------- | :----------------------------------------------------------------------- |
| `ArcGISDesign`  | A new `ArcGISDesign` object with the updated `elevationSources`.         |

#### `getValue`

Retrieves the unique identifier of the map design.

**Signature**
```kotlin
override fun getValue(): String
```

**Returns**
| Type       | Description                               |
| :--------- | :---------------------------------------- |
| `String`   | The `id` of the `ArcGISDesign` instance.  |

---

## `ArcGISDesign.Companion` Object

The companion object for `ArcGISDesign` serves as a factory and utility provider. It contains a comprehensive list of predefined map designs and helper functions to create designs and convert them to ArcGIS-native types.

### Predefined Designs

The companion object includes numerous predefined static properties for common ArcGIS and OSM basemap styles. These provide a convenient and type-safe way to select a map style.

**Examples of Predefined Designs:**
*   `ArcGISDesign.Streets`
*   `ArcGISDesign.Imagery`
*   `ArcGISDesign.NavigationNight`
*   `ArcGISDesign.Topographic`
*   `ArcGISDesign.OsmStandard`
*   `ArcGISDesign.OsmStreets`

### Companion Object Functions

#### `Create`

A factory function that retrieves a predefined `ArcGISDesign` instance based on its unique string ID.

**Signature**
```kotlin
fun Create(
    id: String,
    sources: List<String> = emptyList<String>(),
): ArcGISDesign
```

**Description**
This function looks up a predefined `ArcGISDesign` from the provided `id`. If a matching design is found, it is returned. Note that the `sources` parameter is currently not used to modify the returned predefined instance. Throws a `Throwable` if the `id` does not correspond to any known design.

**Parameters**
| Parameter | Type           | Description                                                              |
| :-------- | :------------- | :----------------------------------------------------------------------- |
| `id`      | `String`       | The unique identifier of the desired map design.                         |
| `sources` | `List<String>` | An optional list of elevation sources. Defaults to an empty list.        |

**Returns**
| Type           | Description                                      |
| :------------- | :----------------------------------------------- |
| `ArcGISDesign` | The corresponding predefined `ArcGISDesign` instance. |

#### `toBasemapStyle`

Converts an `ArcGISDesignTypeInterface` implementation (like `ArcGISDesign`) into the corresponding `BasemapStyle` enum required by the ArcGIS Maps SDK for Kotlin.

**Signature**
```kotlin
fun toBasemapStyle(designType: ArcGISDesignTypeInterface): BasemapStyle
```

**Description**
This utility function acts as a bridge between the `ArcGISDesign` system and the underlying ArcGIS SDK, translating the design's ID into the appropriate `BasemapStyle` enum value. Throws a `Throwable` if the design type's ID is not recognized.

**Parameters**
| Parameter    | Type                        | Description                               |
| :----------- | :-------------------------- | :---------------------------------------- |
| `designType` | `ArcGISDesignTypeInterface` | The map design instance to convert.       |

**Returns**
| Type           | Description                                                              |
| :------------- | :----------------------------------------------------------------------- |
| `BasemapStyle` | The equivalent `BasemapStyle` enum from the `com.arcgismaps.mapping` package. |

---

## `ArcGISDesignTypeInterface` Interface

An interface that defines the basic contract for an ArcGIS map design type.

### Signature
```kotlin
interface ArcGISDesignTypeInterface : MapDesignTypeInterface<String> {
    val elevationSources: List<String>
}
```

### Properties
| Property          | Type           | Description                               |
| :---------------- | :------------- | :---------------------------------------- |
| `elevationSources`| `List<String>` | A list of URLs for elevation data sources. |

---

## Example

Here's how you can use `ArcGISDesign` to configure a map.

```kotlin
import com.arcgismaps.mapping.Basemap
import com.arcgismaps.mapping.BasemapStyle
import com.mapconductor.arcgis.map.ArcGISDesign

fun main() {
    // 1. Use a predefined map design directly
    val streetDesign = ArcGISDesign.Streets
    println("Selected design ID: ${streetDesign.getValue()}")

    // 2. Add elevation sources to an existing design
    val elevationSourceUrl = "https://elevation3d.arcgis.com/arcgis/rest/services/WorldElevation3D/Terrain3D/ImageServer"
    val streetDesignWithElevation = streetDesign.withElevationSources(listOf(elevationSourceUrl))
    println("Elevation sources: ${streetDesignWithElevation.elevationSources}")

    // 3. Create a design from a string ID using the factory function
    try {
        val imageryDesign = ArcGISDesign.Create("arc_gis_imagery")
        println("Successfully created design from ID: ${imageryDesign.id}")
    } catch (e: Throwable) {
        println(e.message)
    }

    // 4. Convert an ArcGISDesign to the ArcGIS SDK's BasemapStyle enum
    // This is typically done when creating a Basemap object for the map view.
    val basemapStyle: BasemapStyle = ArcGISDesign.toBasemapStyle(streetDesign)
    val basemap = Basemap(basemapStyle)
    println("Converted to ArcGIS BasemapStyle: $basemapStyle")

    // Example of using the created basemap (conceptual)
    // mapView.map = ArcGISMap(basemap)
}
```

### Output of the Example:
```
Selected design ID: arc_gis_streets
Elevation sources: [https://elevation3d.arcgis.com/arcgis/rest/services/WorldElevation3D/Terrain3D/ImageServer]
Successfully created design from ID: arc_gis_imagery
Converted to ArcGIS BasemapStyle: ArcGISStreets
```