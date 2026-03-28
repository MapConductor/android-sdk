# MarkerCollector

### Signature

```kotlin
class MarkerCollector(
    updateDebounce: Duration = Settings.Default.composeEventDebounce,
    scope: CoroutineScope = CoroutineScope(Dispatchers.Main.immediate),
) : ChildCollector<MarkerState>
```

### Description

The `MarkerCollector` is a specialized class responsible for collecting and managing a group of `MarkerState` objects. It is designed to be used within a map component to handle the state of multiple markers efficiently.

It functions as a `ChildCollector<MarkerState>` by delegating its implementation to `ChildCollectorImpl`. This allows it to collect marker states declared as its children and expose them as a debounced flow. This is particularly useful for optimizing performance by batching rapid updates to marker properties or positions, reducing the number of recompositions or map redraws.

### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `updateDebounce` | `Duration` | The time duration to wait after the last change before emitting an update. This helps to batch rapid updates and improve performance. Defaults to `Settings.Default.composeEventDebounce`. |
| `scope` | `CoroutineScope` | The coroutine scope in which the collection and debouncing operations will run. Defaults to a scope on the main thread (`CoroutineScope(Dispatchers.Main.immediate)`). |

### Example

The `MarkerCollector` is typically instantiated and provided to a map component. Markers are then declaratively added within the map's content, and their states are automatically managed by the collector.

```kotlin
import androidx.compose.runtime.Composable
import com.mapconductor.core.marker.MarkerCollector
import kotlin.time.Duration.Companion.milliseconds

// 1. Instantiate the MarkerCollector, optionally overriding default parameters.
val customMarkerCollector = MarkerCollector(
    updateDebounce = 200.milliseconds
)

// A hypothetical Map composable that uses the collector.
@Composable
fun MyMapComponent(markerCollector: MarkerCollector) {
    // The map would internally use the collector to listen for marker state changes.
    // ...
}

// 2. Use the collector in your UI.
@Composable
fun MyMapScreen() {
    // The collector is passed to the map component.
    MyMapComponent(markerCollector = customMarkerCollector) {
        // In a real-world scenario, you would declare Marker composables here.
        // Their state would be implicitly registered with the `customMarkerCollector`.
        
        /*
        Example with hypothetical Marker composable:
        
        Marker(
            position = LatLng(34.0522, -118.2437),
            title = "Los Angeles"
        )
        Marker(
            position = LatLng(40.7128, -74.0060),
            title = "New York City"
        )
        */
    }
}
```