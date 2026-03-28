Excellent! Here is the high-quality SDK documentation for the provided Kotlin code snippet.

---

### `debounceBatch<T>`

**Signature**
```kotlin
@OptIn(FlowPreview::class)
fun <T> Flow<T>.debounceBatch(
    window: Duration,
    maxSize: Int
): Flow<List<T>>
```

**Description**

Collects items from the source `Flow` and emits them as batches (lists). This operator is an extension function on `Flow<T>`.

A batch is emitted when either of these two conditions is met:
1.  A time window of `window` duration passes without any new items being received.
2.  The number of collected items in the current batch reaches `maxSize`.

This is particularly useful for improving efficiency by grouping a burst of frequent emissions into a single, larger chunk before performing an operation like a network request or a database write.

If the source flow completes, any remaining buffered items are emitted as a final batch before the downstream flow completes.

**Parameters**

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `window` | `Duration` | The duration of inactivity after which the current batch of items is emitted. |
| `maxSize` | `Int` | The maximum number of items to collect in a batch. When the batch size reaches this limit, it is emitted immediately. Must be greater than 0. |

**Returns**

A `Flow<List<T>>` that emits lists of items, where each list represents a batch collected from the source flow.

**Example**

The following example demonstrates how `debounceBatch` groups items based on both the time `window` and the `maxSize`.

```kotlin
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

// The debounceBatch function is assumed to be defined in this scope.
// Note: The original function is internal, but we use it here for demonstration.

@OptIn(FlowPreview::class)
fun main() = runBlocking {
    val sourceFlow = flow {
        // --- Batch 1: Triggered by time window ---
        println("-> Emitting 1")
        emit(1)
        delay(50)
        println("-> Emitting 2")
        emit(2)
        // Wait for 500ms, which is longer than the 300ms window.
        // This inactivity triggers the emission of the first batch.
        delay(500)

        // --- Batch 2: Triggered by maxSize ---
        println("-> Emitting 3")
        emit(3)
        delay(50)
        println("-> Emitting 4")
        emit(4)
        delay(50)
        // The third emission in this burst fills the batch to maxSize.
        // This triggers an immediate emission of the second batch.
        println("-> Emitting 5")
        emit(5)

        // --- Batch 3: Triggered by flow completion ---
        println("-> Emitting 6")
        emit(6)
        // A short delay before the flow completes.
        delay(50)
    }

    println("Collecting batches with window=300ms, maxSize=3...")
    sourceFlow
        .debounceBatch(window = 300.milliseconds, maxSize = 3)
        .collect { batch ->
            println("<- Collected Batch: $batch")
        }
    println("Collection complete.")
}

/*
Expected Output:

Collecting batches with window=300ms, maxSize=3...
-> Emitting 1
-> Emitting 2
<- Collected Batch: [1, 2]
-> Emitting 3
-> Emitting 4
-> Emitting 5
<- Collected Batch: [3, 4, 5]
-> Emitting 6
<- Collected Batch: [6]
Collection complete.
*/
```