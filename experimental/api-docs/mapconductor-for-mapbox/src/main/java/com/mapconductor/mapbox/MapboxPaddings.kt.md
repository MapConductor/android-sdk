Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

# Mapbox Paddings Utilities

This document provides detailed information about the `MapboxPaddings` data class, its associated interface, and utility functions. These components are designed to manage map padding within a Mapbox environment, facilitating conversion between the core `MapPaddingsInterface` and Mapbox's native `EdgeInsets`.

## `IMapboxPaddingsInterface`

### Signature
```kotlin
interface IMapboxPaddingsInterface : MapPaddingsInterface
```

### Description
An interface that extends the core `MapPaddingsInterface` to include Mapbox-specific functionalities. It ensures that any Mapbox padding implementation can be converted to a native Mapbox `EdgeInsets` object.

### Methods

#### `toEdgeInsects()`
Converts the padding data into a Mapbox `EdgeInsets` object.

**Signature**
```kotlin
fun toEdgeInsects(): EdgeInsets
```

**Returns**
| Type | Description |
| :--- | :--- |
| `EdgeInsets` | A Mapbox `EdgeInsets` object representing the same padding values. |

---

## `MapboxPaddings`

### Signature
```kotlin
data class MapboxPaddings(
    override val top: Double,
    override val left: Double,
    override val bottom: Double,
    override val right: Double,
) : MapPaddings(top, left, bottom, right), IMapboxPaddingsInterface
```

### Description
A data class that represents the padding on the four sides of the map. It serves as the concrete implementation of `IMapboxPaddingsInterface` for the Mapbox SDK, holding padding values in pixels.

### Parameters
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `top` | `Double` | The padding from the top edge of the map, in pixels. |
| `left` | `Double` | The padding from the left edge of the map, in pixels. |
| `bottom` | `Double` | The padding from the bottom edge of the map, in pixels. |
| `right` | `Double` | The padding from the right edge of the map, in pixels. |

### Companion Object

#### `Zeros`
A predefined `MapboxPaddings` instance with all padding values set to `0.0`.

**Signature**
```kotlin
val Zeros: MapboxPaddings
```

**Example**
```kotlin
// Use the Zeros constant for no padding
val noPadding = MapboxPaddings.Zeros
```

#### `from()`
A factory method that creates a `MapboxPaddings` instance from any object implementing `MapPaddingsInterface`.

**Signature**
```kotlin
fun from(paddings: MapPaddingsInterface? = null): MapboxPaddings?
```

**Description**
This function safely converts a generic `MapPaddingsInterface` object into a `MapboxPaddings` object. If the input is already a `MapboxPaddings` instance, it is returned directly. If the input is `null`, it returns `null`.

**Parameters**
| Parameter | Type | Description |
| :--- | :--- | :--- |
| `paddings` | `MapPaddingsInterface?` | The padding object to convert. Defaults to `null`. |

**Returns**
| Type | Description |
| :--- | :--- |
| `MapboxPaddings?` | A `MapboxPaddings` instance if the input is not null; otherwise, `null`. |

---

## Extension Functions

### `EdgeInsets.toPaddings()`

### Signature
```kotlin
@Keep
fun EdgeInsets.toPaddings(): MapboxPaddings
```

### Description
An extension function for the Mapbox `EdgeInsets` class. It provides a convenient way to convert a native `EdgeInsets` object into a `MapboxPaddings` object.

### Returns
| Type | Description |
| :--- | :--- |
| `MapboxPaddings` | A new `MapboxPaddings` instance with values copied from the `EdgeInsets` object. |

### Example
```kotlin
import com.mapbox.maps.EdgeInsets
import com.mapconductor.mapbox.toPaddings

// Given a Mapbox EdgeInsets object
val edgeInsets = EdgeInsets(100.0, 50.0, 20.0, 50.0)

// Convert it to a MapboxPaddings object
val mapboxPaddings = edgeInsets.toPaddings()

// Now mapboxPaddings.top is 100.0, mapboxPaddings.left is 50.0, etc.
```