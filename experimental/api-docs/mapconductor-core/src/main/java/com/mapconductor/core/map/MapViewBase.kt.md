Excellent. Here is the high-quality SDK documentation for the provided `MapViewBase` composable.

***

# MapViewBase

## Signature
```kotlin
@Composable
fun <
    SpecificState : MapViewStateInterface<*>,
    SpecificController : MapViewControllerInterface,
    ActualMapView : View,
    ActualMap : Any,
    SpecificScope : MapViewScope,
    SpecificHolder : MapViewHolderInterface<ActualMapView, ActualMap>,
> MapViewBase(
    state: SpecificState,
    cameraState: MutableState<MapCameraPositionInterface?>,
    modifier: Modifier = Modifier,
    viewProvider: () -> ActualMapView,
    scope: SpecificScope,
    registry: MapOverlayRegistry,
    serviceRegistry: MapServiceRegistry = EmptyMapServiceRegistry,
    sdkInitialize: suspend () -> Boolean = { true },
    holderProvider: suspend (mapView: ActualMapView) -> SpecificHolder,
    controllerProvider: suspend (holder: SpecificHolder) -> SpecificController,
    onMapLoaded: OnMapLoadedHandler? = null,
    customDisposableEffect: (@Composable (InitState, Ref<SpecificHolder>) -> Unit)? = null,
    content: (@Composable SpecificScope.() -> Unit)? = null,
)
```

## Description

`MapViewBase` is a fundamental, low-level composable designed to integrate a native Android map view into a Jetpack Compose application. It serves as a generic foundation for creating specific map implementations (e.g., for Google Maps, Mapbox, etc.).

This component manages the entire lifecycle of the underlying map view, including:
- Asynchronous initialization of the map SDK.
- Creation and management of the native `View`.
- Setup of a map controller to abstract map interactions.
- A declarative API for adding and updating map overlays (like markers, polylines, and polygons) as composable content.

It uses a `SubcomposeLayout` to correctly layer Compose-based UI (such as info bubbles) on top of the native `AndroidView`, ensuring that overlays are rendered only after the map's size is determined. `CompositionLocalProvider` is used to provide the map controller and overlay collectors to the child `content` composables.

## Parameters

| Parameter | Type | Description |
| --- | --- | --- |
| `state` | `SpecificState` | The state object for the map, which must implement `MapViewStateInterface`. It typically contains properties like the camera position. |
| `cameraState` | `MutableState<MapCameraPositionInterface?>` | A mutable state holding the current camera position. This is used for observing and controlling the map's camera from outside the composable. |
| `modifier` | `Modifier` | The `Modifier` to be applied to the root layout of the map component. |
| `viewProvider` | `() -> ActualMapView` | A factory lambda that creates and returns an instance of the native Android map view (e.g., `com.google.android.gms.maps.MapView`). |
| `scope` | `SpecificScope` | An instance of a `MapViewScope` implementation. It is responsible for managing the state collectors for all map overlays (markers, polylines, etc.). |
| `registry` | `MapOverlayRegistry` | The registry that contains the rendering logic for different types of map overlays. It maps overlay state to the corresponding controller actions. |
| `serviceRegistry` | `MapServiceRegistry` | A registry for providing optional map-related services. Defaults to an empty registry if not provided. |
| `sdkInitialize` | `suspend () -> Boolean` | An asynchronous lambda that performs one-time initialization for the map SDK (e.g., setting an API key). It should return `true` on success and `false` on failure. |
| `holderProvider` | `suspend (ActualMapView) -> SpecificHolder` | An asynchronous factory lambda that takes the created native map view and returns a `MapViewHolderInterface`. The holder is responsible for managing the map object's lifecycle (e.g., calling `getMapAsync` in Google Maps). |
| `controllerProvider` | `suspend (SpecificHolder) -> SpecificController` | An asynchronous factory lambda that takes the initialized holder and returns a `MapViewControllerInterface`. The controller provides a unified API to manipulate the map (e.g., add markers, move camera). |
| `onMapLoaded` | `OnMapLoadedHandler?` | An optional callback that is invoked when the map controller is fully initialized and the map is ready to be interacted with. It receives the map's state as an argument. |
| `customDisposableEffect` | `(@Composable (InitState, Ref<SpecificHolder>) -> Unit)?` | An optional composable lambda for executing custom side-effects tied to the map's lifecycle. This is useful for advanced, provider-specific setup or cleanup logic. |
| `content` | `(@Composable SpecificScope.() -> Unit)?` | The composable content to be displayed on the map. This is where you declaratively add map overlays like `Marker`, `Polyline`, and `Circle`. These composables receive the `MapViewScope` as their receiver. |

## Returns

This is a composable function and does not have a direct return value. It renders a UI component that displays the native map and its associated overlay content.

## Example

`MapViewBase` is a generic component. The following example shows a conceptual implementation for a hypothetical "MyMap" SDK to illustrate how you would use it.

```kotlin
// Assume these are your concrete implementations for a specific map SDK
class MyMapViewState : MapViewStateInterface<MyMap> { /* ... */ }
class MyMapViewController : MapViewControllerInterface { /* ... */ }
class MyMapView(context: Context) : FrameLayout(context) { /* Native Map View */ }
class MyMap { /* The actual map object from the SDK */ }
class MyMapViewHolder(mapView: MyMapView) : MapViewHolderInterface<MyMapView, MyMap> { /* ... */ }
class MyMapViewScope : MapViewScope() { /* ... */ }

@Composable
fun MyMapViewComponent(modifier: Modifier = Modifier) {
    // 1. Remember the state and scope for your map implementation
    val state = remember { MyMapViewState() }
    val cameraState = remember { mutableStateOf<MapCameraPositionInterface?>(null) }
    val scope = remember { MyMapViewScope() }
    val context = LocalContext.current

    // 2. Provide the concrete implementations to MapViewBase
    MapViewBase(
        state = state,
        cameraState = cameraState,
        modifier = modifier,
        scope = scope,
        registry = scope.buildRegistry(), // Build registry from the scope
        
        // Provide a factory for the native map view
        viewProvider = { MyMapView(context) },
        
        // Initialize the SDK (e.g., with an API key)
        sdkInitialize = {
            MyMapSDK.initialize("YOUR_API_KEY")
            true // Return true on success
        },
        
        // Create a holder that manages the map object lifecycle
        holderProvider = { nativeView ->
            MyMapViewHolder(nativeView).apply {
                // This is where you'd typically call something like getMapAsync
                awaitMap() 
            }
        },
        
        // Create a controller to interact with the map
        controllerProvider = { holder ->
            MyMapViewController(holder.getMap())
        },
        
        // Callback for when the map is ready
        onMapLoaded = { mapState ->
            Log.d("MyMap", "Map is fully loaded and ready!")
        }
    ) {
        // 3. Add map overlays declaratively inside the content lambda
        Marker(
            position = GeoPoint(35.681236, 139.767125), // Tokyo Station
            title = "Tokyo Station",
            snippet = "The heart of Tokyo's train network."
        )
        
        Polyline(
            points = listOf(
                GeoPoint(35.681236, 139.767125),
                GeoPoint(35.658581, 139.745433) // Tokyo Tower
            ),
            color = Color.Blue,
            width = 5f
        )
    }
}
```