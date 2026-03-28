Of course! Here is the high-quality SDK documentation for the provided Kotlin code snippet.

***

## toFixed

These extension functions format a `Double` or `Float` to a string with a specified number of decimal places. The formatting is done by truncating the number, not by rounding.

---

### Double.toFixed()

An extension function that formats a `Double` into a string representation with a fixed number of decimal places.

#### Signature
```kotlin
fun Double.toFixed(decimals: Int = 0): String
```

#### Description
This function converts a `Double` to a string with a specified number of digits after the decimal point. It uses `RoundingMode.DOWN`, which means it always truncates the number and never rounds up. The resulting string will not use scientific notation.

#### Parameters
| Parameter  | Type  | Description                                          | Default |
|------------|-------|------------------------------------------------------|---------|
| `decimals` | `Int` | The number of decimal places to preserve in the output. | `0`     |

#### Returns
`String` - The formatted string representation of the `Double`.

#### Example
```kotlin
import com.mapconductor.core.toFixed

fun main() {
    val pi = 3.14159265359
    
    // Format to 4 decimal places
    val formattedPi = pi.toFixed(4)
    println(formattedPi) // Output: "3.1415"
    
    // Format to 2 decimal places
    val price = 19.999
    val formattedPrice = price.toFixed(2)
    println(formattedPrice) // Output: "19.99"
    
    // Use the default value (0 decimals)
    val integerPart = 123.456
    val formattedInteger = integerPart.toFixed()
    println(formattedInteger) // Output: "123"
}
```

---

### Float.toFixed()

An extension function that formats a `Float` into a string representation with a fixed number of decimal places.

#### Signature
```kotlin
fun Float.toFixed(decimals: Int = 0): String
```

#### Description
This function converts a `Float` to a string with a specified number of digits after the decimal point. It uses `RoundingMode.DOWN`, which means it always truncates the number and never rounds up. The resulting string will not use scientific notation.

#### Parameters
| Parameter  | Type  | Description                                          | Default |
|------------|-------|------------------------------------------------------|---------|
| `decimals` | `Int` | The number of decimal places to preserve in the output. | `0`     |

#### Returns
`String` - The formatted string representation of the `Float`.

#### Example
```kotlin
import com.mapconductor.core.toFixed

fun main() {
    val value = 123.4567f
    
    // Format to 3 decimal places
    val formattedValue = value.toFixed(3)
    println(formattedValue) // Output: "123.456"
    
    // Format to 1 decimal place
    val temperature = 98.76f
    val formattedTemp = temperature.toFixed(1)
    println(formattedTemp) // Output: "98.7"
    
    // Use the default value (0 decimals)
    val anotherValue = 45.9f
    val formattedAnother = anotherValue.toFixed()
    println(formattedAnother) // Output: "45"
}
```