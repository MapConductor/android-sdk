---
title: "MapViewState"
---

import { Tabs, TabItem } from '@astrojs/starlight/components';
import CameraControlExample from '~/components/components/mapviewstate/CameraControlExample.astro';
import MapDesignSwitchExample from '~/components/components/mapviewstate/MapDesignSwitchExample.astro';
import MoveCameraToGeoPointSignature from '~/components/components/mapviewstate/MoveCameraToGeoPointSignature.astro';
import MoveCameraToCameraPositionSignature from '~/components/components/mapviewstate/MoveCameraToCameraPositionSignature.astro';
import MapViewStateEventHandlingExample from '~/components/components/mapviewstate/MapViewStateEventHandlingExample.astro';

`MapViewState` is a core component that manages map initialization, camera position, and overall map state.
Each map SDK has its own implementation, but MapConductor provides a unified API across all providers.

## Map SDK Implementations

MapConductor supports 5 map SDKs, each with its own `MapViewState` implementation:

- `GoogleMapViewStateImpl` - Google Maps
- `MapboxViewStateImpl` - Mapbox Map
- `HereViewStateImpl` - HERE Map
- `ArcGISMapViewStateImpl` - ArcGIS Map
- `MapLibreViewStateImpl` - MapLibre Map

## Core Properties

### Initialization State

- **`isInitialized: StateFlow<InitState>`**: Tracks the map's initialization state
  - `NotStarted`: Map initialization has not started
  - `Initializing`: Map is currently initializing
  - `Initialized`: Map is ready to use
  - `Failed`: Initialization failed

### Camera Management

- **`cameraPosition: MapCameraPositionImpl`**: Current camera position and initial camera position when the map loads

### Map Design

- **`mapDesignType: ActualMapDesignType`**: Map style/design (provider-specific)

### Camera Movement

<Tabs>
<TabItem label="Simple Movement">
- `moveCameraTo(GeoPointImpl, Long?)`

    <MoveCameraToGeoPointSignature />

    - Moves the camera to the specified position. Zoom and tilt angles are maintained from the time `moveCameraTo` is called.
    - When `durationMills` is specified in milliseconds, the camera moves with animation.
</TabItem>
<TabItem label="Movement with Camera Options">
- `moveCameraTo(MapCameraPositionImpl, Long?)`

    <MoveCameraToCameraPositionSignature />

    - Moves the camera using a `MapCameraPositionImpl`. Zoom and tilt can be specified at the same time. Properties that are omitted maintain their values from when `moveCameraTo` is called.
    - When `durationMills` is specified in milliseconds, the camera moves with animation.
</TabItem>
</Tabs>

## Usage Examples

### Camera Control

<CameraControlExample />

### Map Design Switching

<MapDesignSwitchExample />

## Event Handling

`MapViewState` works with your chosen map provider component to provide comprehensive event handling:

<MapViewStateEventHandlingExample />

## Best Practices

1. **State Initialization**: Initialize MapViewState with appropriate camera position and bounds
2. **Camera Animation**: Use reasonable animation durations for smooth user experience (500ms - 3000ms)
3. **State Stability**: Use `remember` to maintain state across recompositions
4. **Reactive Updates**: Use StateFlow for reactive updates to initialization state
5. **Error Handling**: Check initialization state before performing camera operations
6. **Memory Management**: Clean up listeners when the map is no longer needed

