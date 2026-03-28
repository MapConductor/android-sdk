Of course! Here is the high-quality SDK documentation for the provided Kotlin code snippet.

---

# Interface `MapDesignTypeInterface<T>`

## Signature
```kotlin
interface MapDesignTypeInterface<T>
```

## Description
Defines a generic contract for a map design type. Any class implementing this interface represents a specific design characteristic or style that can be applied to a map, such as its visual theme or data layer.

This interface ensures that every design type provides a unique identifier (`id`) and a method to retrieve its core value (`getValue`), both of which are of the generic type `T`.

## Type Parameters

| Parameter | Description |
|-----------|-------------|
| `T`       | The type of the identifier and the value for the design type. This allows for flexibility, such as using `String`, `Int`, or an `Enum`. |

## Properties

### `id`
**Signature:** `val id: T`

A read-only property that serves as the unique identifier for the map design type.

**Returns**
- `T`: The unique identifier.

## Methods

### `getValue()`
**Signature:** `fun getValue(): T`

Retrieves the value associated with the map design type. In many implementations, this may return the same value as the `id`.

**Returns**
- `T`: The value of the design type.

## Example

Here is an example of an `enum` class that implements `MapDesignTypeInterface` to define a set of available map styles.

```kotlin
// Define an enum for map styles that implements the interface with String as the type.
enum class MapStyle(private val styleName: String) : MapDesignType_Interface<String> {
    STANDARD("standard-v1"),
    SATELLITE("satellite-v9"),
    DARK("dark-v10");

    // Implementation of the 'id' property from the interface.
    override val id: String
        get() = this.name // e.g., "STANDARD"

    // Implementation of the 'getValue' function from the interface.
    override fun getValue(): String {
        return this.styleName // e.g., "standard-v1"
    }
}

fun applyMapStyle(style: MapDesignTypeInterface<String>) {
    val styleIdentifier = style.id
    val styleValueForApi = style.getValue()
    println("Applying style with ID: '$styleIdentifier' and API value: '$styleValueForApi'")
    // ... logic to apply the map style using its value
}

fun main() {
    val currentStyle = MapStyle.DARK

    // You can pass the enum instance directly to any function expecting the interface.
    applyMapStyle(currentStyle)
    // Outputs: Applying style with ID: 'DARK' and API value: 'dark-v10'

    // You can also access the properties and methods directly.
    println("Selected Style ID: ${currentStyle.id}")
    // Outputs: Selected Style ID: DARK

    println("Selected Style Value: ${currentStyle.getValue()}")
    // Outputs: Selected Style Value: dark-v10
}
```