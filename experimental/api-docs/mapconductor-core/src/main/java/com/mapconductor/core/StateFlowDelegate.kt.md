# StateFlowDelegate<T>

A property delegate that backs a `var` property with a `kotlinx.coroutines.flow.MutableStateFlow`, allowing property changes to be observed reactively.

## Signature

```kotlin
class StateFlowDelegate<T>(
    initialValue: T,
) : ReadWriteProperty<Any?, T>
```

## Description

`StateFlowDelegate` is a property delegate that seamlessly integrates a standard mutable property (`var`) with the power of Kotlin's `StateFlow`. When you delegate a property to `StateFlowDelegate`, any assignment to that property automatically updates the value of an internal `MutableStateFlow` and emits the new value to all its collectors.

This class implements the `ReadWriteProperty` interface, allowing it to be used with the `by` keyword. The `getValue` and `setValue` methods are invoked by the Kotlin compiler upon property access and assignment.

This delegate is ideal for state management in application architectures like MVVM or MVI, where you need to expose observable state from a ViewModel or a repository to the UI layer or other consumers.

## Constructor

### `StateFlowDelegate(initialValue: T)`

Creates a new instance of the delegate, initializing the internal `StateFlow` with the provided value.

#### Parameters

| Parameter      | Type | Description                        |
|----------------|------|------------------------------------|
| `initialValue` | `T`  | The initial value of the property. |

## Methods

### `asStateFlow()`

Exposes the underlying `MutableStateFlow` that backs the property. This allows consumers to observe changes to the property's value by collecting from the returned flow.

While the method returns a `MutableStateFlow`, it is a common and recommended practice for consumers to treat it as a read-only `StateFlow` to maintain unidirectional data flow and prevent external modifications.

#### Signature

```kotlin
fun asStateFlow(): MutableStateFlow<T>
```

#### Returns

| Type                  | Description                               |
|-----------------------|-------------------------------------------|
| `MutableStateFlow<T>` | The backing `MutableStateFlow` instance.  |

## Example

The following example demonstrates how to use `StateFlowDelegate` in a `UserViewModel` to manage and observe a user's status.

```kotlin
import com.mapconductor.core.StateFlowDelegate
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class UserViewModel {
    // 1. Create a private instance of the delegate with an initial value.
    private val statusDelegate = StateFlowDelegate("Offline")

    // 2. Delegate the public 'status' property to the instance.
    //    Reads and writes to 'status' will now go through the delegate.
    var status: String by statusDelegate

    // 3. Expose the underlying StateFlow for observers.
    //    This allows other parts of the app to react to status changes.
    val statusFlow: StateFlow<String> = statusDelegate.asStateFlow()
}

fun main() = runBlocking {
    val viewModel = UserViewModel()

    println("Initial status: ${viewModel.status}")

    // Launch a coroutine to collect updates from statusFlow.
    // The initial value is collected immediately.
    val collectorJob = launch {
        viewModel.statusFlow.collect { newStatus ->
            println("Status updated via flow: $newStatus")
        }
    }

    // Wait a bit, then change the property's value.
    // This will trigger the collector in the coroutine.
    delay(100)
    println("\n> Setting status to 'Connecting...'")
    viewModel.status = "Connecting..."

    delay(100)
    println("\n> Setting status to 'Online'")
    viewModel.status = "Online"
    
    // The property can be read directly at any time.
    println("Current status property: ${viewModel.status}")

    // Clean up the coroutine
    delay(50)
    collectorJob.cancel()
}
```

### Expected Output

```
Initial status: Offline
Status updated via flow: Offline

> Setting status to 'Connecting...'
Status updated via flow: Connecting...

> Setting status to 'Online'
Status updated via flow: Online
Current status property: Online
```