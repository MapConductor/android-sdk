# HereViewScope

### Signature
```kotlin
class HereViewScope : MapViewScope()
```

### Description
The `HereViewScope` class provides a specific receiver scope for the `hereMap` builder function. It establishes a context dedicated to configuring a HERE Map instance.

This class inherits from `MapViewScope`, making all common map configuration functions available within its scope. Its primary purpose is to enable HERE Maps-specific extensions and ensure type-safe construction of the map UI, preventing the mixing of components from different map providers.

You will typically interact with this class implicitly when using the `hereMap` builder lambda.

### Example
The following example demonstrates how `HereViewScope` is used as the receiver in the `hereMap` builder to configure a map.

```kotlin
// A hypothetical MapConductor composable
@Composable
fun MyMapScreen() {
    // The lambda block for hereMap operates within the HereViewScope
    hereMap(
        modifier = Modifier.fillMaxSize(),
        // `this` inside the lambda is an instance of HereViewScope
    ) {
        // Functions from the parent MapViewScope are available
        addMarker(
            position = GeoPosition(52.5200, 13.4050),
            title = "Berlin"
        )

        // You can also use HERE Maps-specific extensions (if available)
        // enableTrafficFlow(true) 
    }
}
```