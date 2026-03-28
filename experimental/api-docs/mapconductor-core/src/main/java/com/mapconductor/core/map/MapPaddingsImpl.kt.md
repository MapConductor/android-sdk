# MapPaddings

The `MapPaddings` class and `MapPaddingsInterface` define a structure for specifying padding on the four sides of a map view. This is useful for creating a visible region within the map's viewport that is not obscured by other UI elements.

## MapPaddingsInterface

An interface that defines the contract for a map padding object.

### Signature

```kotlin
interface MapPaddingsInterface
```

### Description

Provides a standardized way to access padding values. Any class that represents map paddings can implement this interface.

### Properties

| Property | Type     | Description                               |
|----------|----------|-------------------------------------------|
| `top`    | `Double` | The padding from the top edge in pixels.  |
| `left`   | `Double` | The padding from the left edge in pixels. |
| `bottom` | `Double` | The padding from the bottom edge in pixels.|
| `right`  | `Double` | The padding from the right edge in pixels.|

---

## MapPaddings Class

An open class that provides a concrete implementation of `MapPaddingsInterface`.

### Signature

```kotlin
open class MapPaddings : MapPaddingsInterface
```

### Description

Represents the padding values for the four edges of a map view in pixels. It allows developers to define an inset area where map content like markers or routes can be displayed without being covered by UI components. This class is `open`, so it can be subclassed if needed.

### Constructor

#### `MapPaddings()`

Creates a new `MapPaddings` instance with specified padding values.

##### Signature

```kotlin
@JvmOverloads
constructor(
    top: Double = 0.0,
    left: Double = 0.0,
    bottom: Double = 0.0,
    right: Double = 0.0,
)
```

##### Description

Initializes a `MapPaddings` object. All parameters have a default value of `0.0`. The `@JvmOverloads` annotation enables this constructor to be called from Java with any number of arguments from left to right, with the remaining parameters taking their default values.

##### Parameters

| Parameter | Type     | Description                               | Default |
|-----------|----------|-------------------------------------------|---------|
| `top`     | `Double` | The padding from the top edge in pixels.  | `0.0`   |
| `left`    | `Double` | The padding from the left edge in pixels. | `0.0`   |
| `bottom`  | `Double` | The padding from the bottom edge in pixels.| `0.0`   |
| `right`   | `Double` | The padding from the right edge in pixels.| `0.0`   |

### Companion Object

#### `Zeros`

A static instance representing no padding.

##### Signature

```kotlin
val Zeros: MapPaddings
```

##### Description

Provides a convenient, pre-defined `MapPaddings` object where all padding values (`top`, `left`, `bottom`, `right`) are `0.0`.

#### `from()`

A factory method to create a `MapPaddings` instance from a `MapPaddingsInterface`.

##### Signature

```kotlin
fun from(paddings: MapPaddingsInterface): MapPaddings
```

##### Description

Creates a `MapPaddings` instance from any object that implements the `MapPaddingsInterface`. This method includes an optimization: if the provided `paddings` object is already an instance of `MapPaddings`, it is returned directly to avoid unnecessary object creation.

##### Parameters

| Parameter  | Type                   | Description                                          |
|------------|------------------------|------------------------------------------------------|
| `paddings` | `MapPaddingsInterface` | An object conforming to the `MapPaddingsInterface`.  |

##### Returns

| Type          | Description                                                              |
|---------------|--------------------------------------------------------------------------|
| `MapPaddings` | A `MapPaddings` instance with values copied from the `paddings` parameter. |

### Example

```kotlin
// Example of a custom class implementing the interface
data class CustomPaddings(
    override val top: Double,
    override val left: Double,
    override val bottom: Double,
    override val right: Double
) : MapPaddingsInterface

fun main() {
    // 1. Create an instance with all custom values
    val customPaddings = MapPaddings(top = 100.0, left = 20.0, bottom = 50.0, right = 20.0)
    println("Custom Paddings: top=${customPaddings.top}, right=${customPaddings.right}")
    // Output: Custom Paddings: top=100.0, right=20.0

    // 2. Create an instance using default values for bottom and right
    val partialPaddings = MapPaddings(top = 150.0, left = 25.0)
    println("Partial Paddings: bottom=${partialPaddings.bottom}, right=${partialPaddings.right}")
    // Output: Partial Paddings: bottom=0.0, right=0.0

    // 3. Use the predefined Zeros constant for no padding
    val noPaddings = MapPaddings.Zeros
    println("Zero Paddings: top=${noPaddings.top}, left=${noPaddings.left}")
    // Output: Zero Paddings: top=0.0, left=0.0

    // 4. Use the `from` factory method with a custom implementation
    val myPaddings = CustomPaddings(top = 200.0, left = 0.0, bottom = 0.0, right = 0.0)
    val mapPaddingsFromInterface = MapPaddings.from(myPaddings)
    println("From Interface: top=${mapPaddingsFromInterface.top}")
    // Output: From Interface: top=200.0

    // 5. The `from` method returns the same instance if it's already a MapPaddings
    val originalPaddings = MapPaddings(50.0, 50.0, 50.0, 50.0)
    val fromPaddings = MapPaddings.from(originalPaddings)
    println("Is same instance: ${originalPaddings === fromPaddings}")
    // Output: Is same instance: true
}
```