Of course! Here is the high-quality SDK documentation for the provided code snippet.

***

## GroundImageEntityInterface<ActualGroundImage>

### Signature
```kotlin
interface GroundImageEntityInterface<ActualGroundImage>
```

### Description
Defines a contract for a ground image entity. This interface provides a standardized way to access the core properties of a ground image, including the image object itself, its current state, and a unique fingerprint derived from that state.

### Properties
| Property      | Type                      | Description                                                                                             |
|---------------|---------------------------|---------------------------------------------------------------------------------------------------------|
| `groundImage` | `ActualGroundImage`       | The underlying ground image object. The specific type is determined by the generic parameter.           |
| `state`       | `GroundImageState`        | Represents the current state and configuration of the ground image (e.g., opacity, visibility, position). |
| `fingerPrint` | `GroundImageFingerPrint`  | A unique identifier derived from the ground image's state, used for tracking changes and comparisons.    |

---

## GroundImageEntity<ActualGroundImage>

### Signature
```kotlin
data class GroundImageEntity<ActualGroundImage>(
    override val groundImage: ActualGroundImage,
    override val state: GroundImageState,
) : GroundImageEntityInterface<ActualGroundImage>
```

### Description
A data class that provides a concrete implementation of `GroundImageEntityInterface`. It encapsulates a ground image object and its associated state. The class automatically generates a `fingerPrint` by calling the `fingerPrint()` method on the provided `state` object.

### Parameters
This table describes the parameters for the `GroundImageEntity` constructor.

| Parameter     | Type                | Description                                                                 |
|---------------|---------------------|-----------------------------------------------------------------------------|
| `groundImage` | `ActualGroundImage` | The underlying ground image object to be encapsulated.                      |
| `state`       | `GroundImageState`  | The state object that describes the ground image's properties and configuration. |

### Properties
This table describes the properties available on an instance of `GroundImageEntity`.

| Property      | Type                      | Description                                                                                             |
|---------------|---------------------------|---------------------------------------------------------------------------------------------------------|
| `groundImage` | `ActualGroundImage`       | The underlying ground image object.                                                                     |
| `state`       | `GroundImageState`        | The state object describing the ground image's properties.                                              |
| `fingerPrint` | `GroundImageFingerPrint`  | A unique fingerprint automatically calculated from the `state` object by calling its `fingerPrint()` method. |

### Example
This example demonstrates how to create and use a `GroundImageEntity`.

```kotlin
// Assume these related data structures are defined elsewhere in the SDK
class SampleMapImage {
    // Represents the actual image data or resource
}

data class GroundImageFingerPrint(val id: String)

data class GroundImageState(
    val opacity: Double,
    val isVisible: Boolean
) {
    // Generates a unique string based on the state's properties
    fun fingerPrint() = GroundImageFingerPrint("opacity=$opacity&isVisible=$isVisible")
}

// 1. Create an instance of the actual image and its state
val myImage = SampleMapImage()
val myImageState = GroundImageState(opacity = 0.85, isVisible = true)

// 2. Create the GroundImageEntity
val groundImageEntity = GroundImageEntity(
    groundImage = myImage,
    state = myImageState
)

// 3. Access its properties
println("Image object: $groundImageEntity.groundImage")
println("Image state: $groundImageEntity.state")

// The fingerprint is automatically generated from the state
// Expected output: Fingerprint: GroundImageFingerPrint(id=opacity=0.85&isVisible=true)
println("Fingerprint: ${groundImageEntity.fingerPrint}")
```