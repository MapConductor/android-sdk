Of course! Here is the high-quality SDK documentation for the provided code snippet, formatted in Markdown.

# ArcGIS Marker Event Controllers

This document provides detailed documentation for the marker event controller interfaces and their implementations within the ArcGIS module. These controllers are responsible for handling and dispatching user interactions with markers on the map, such as clicks and drags.

**Note:** The components documented here are marked as `internal`, suggesting they are intended for use within the library's ecosystem.

## `ArcGISMarkerEventControllerInterface`

An interface that defines the contract for handling marker-related events in an ArcGIS map context. It extends the generic `MarkerEventControllerInterface` and adds functionalities specific to the ArcGIS environment.

### Methods

#### **find**

Finds a marker entity at a specific geographic position.

**Signature**
```kotlin
fun find(position: GeoPoint): MarkerEntityInterface<ArcGISActualMarker>?
```

**Description**
Searches for a marker entity that corresponds to the given geographic coordinates.

**Parameters**
| Parameter  | Type       | Description                                      |
|------------|------------|--------------------------------------------------|
| `position` | `GeoPoint` | The geographic coordinate to search for a marker. |

**Returns**
| Type                                           | Description                                                              |
|------------------------------------------------|--------------------------------------------------------------------------|
| `MarkerEntityInterface<ArcGISActualMarker>?` | The found marker entity, or `null` if no marker exists at the given position. |

---

#### **getSelectedState**

Retrieves the state of the currently selected marker.

**Signature**
```kotlin
fun getSelectedState(): MarkerState?
```

**Description**
Returns the `MarkerState` of the marker that is currently selected, typically during a drag operation.

**Returns**
| Type          | Description                                                      |
|---------------|------------------------------------------------------------------|
| `MarkerState?`| The state of the selected marker, or `null` if no marker is selected. |

---

#### **startDrag**

Initiates a drag operation for a specific marker entity.

**Signature**
```kotlin
fun startDrag(entity: MarkerEntityInterface<ArcGISActualMarker>)
```

**Description**
Marks the beginning of a drag sequence for the given marker entity. This method sets the provided entity as the currently selected marker for subsequent drag updates.

**Parameters**
| Parameter | Type                                         | Description                          |
|-----------|----------------------------------------------|--------------------------------------|
| `entity`  | `MarkerEntityInterface<ArcGISActualMarker>` | The marker entity to start dragging. |

---

#### **updateDrag**

Updates the position of the marker currently being dragged.

**Signature**
```kotlin
fun updateDrag(point: Point, position: GeoPoint)
```

**Description**
This method is called continuously during a drag gesture to update the visual position of the marker on the map and its underlying geographic coordinates.

**Parameters**
| Parameter  | Type       | Description                                                              |
|------------|------------|--------------------------------------------------------------------------|
| `point`    | `Point`    | The new map coordinate (`com.arcgismaps.geometry.Point`) of the marker.   |
| `position` | `GeoPoint` | The new geographic coordinate (`com.mapconductor.core.features.GeoPoint`). |

---

#### **endDrag**

Finalizes the drag operation for the currently selected marker.

**Signature**
```kotlin
fun endDrag(point: Point, position: GeoPoint)
```

**Description**
This method is called at the end of a drag gesture. It sets the marker's final position and clears the "selected" state, concluding the drag operation.

**Parameters**
| Parameter  | Type       | Description                                                              |
|------------|------------|--------------------------------------------------------------------------|
| `point`    | `Point`    | The final map coordinate (`com.arcgismaps.geometry.Point`) of the marker. |
| `position` | `GeoPoint` | The final geographic coordinate (`com.mapconductor.core.features.GeoPoint`). |

---

#### **dispatchClick**

Dispatches a click event to the registered listener.

**Signature**
```kotlin
fun dispatchClick(state: MarkerState)
```

**Description**
Invokes the `onMarkerClick` handler if a click listener has been set.

**Parameters**
| Parameter | Type          | Description                          |
|-----------|---------------|--------------------------------------|
| `state`   | `MarkerState` | The state of the marker that was clicked. |

---

#### **dispatchDragStart**

Dispatches a drag start event to the registered listener.

**Signature**
```kotlin
fun dispatchDragStart(state: MarkerState)
```

**Description**
Invokes the `onMarkerDragStart` handler if a drag start listener has been set.

**Parameters**
| Parameter | Type          | Description                                       |
|-----------|---------------|---------------------------------------------------|
| `state`   | `MarkerState` | The state of the marker at the start of the drag. |

---

#### **dispatchDrag**

Dispatches a drag event to the registered listener.

**Signature**
```kotlin
fun dispatchDrag(state: MarkerState)
```

**Description**
Invokes the `onMarkerDrag` handler if a drag listener has been set. This is typically called repeatedly as the marker is being moved.

**Parameters**
| Parameter | Type          | Description                                 |
|-----------|---------------|---------------------------------------------|
| `state`   | `MarkerState` | The current state of the marker being dragged. |

---

#### **dispatchDragEnd**

Dispatches a drag end event to the registered listener.

**Signature**
```kotlin
fun dispatchDragEnd(state: MarkerState)
```

**Description**
Invokes the `onMarkerDragEnd` handler if a drag end listener has been set.

**Parameters**
| Parameter | Type          | Description                                   |
|-----------|---------------|-----------------------------------------------|
| `state`   | `MarkerState` | The final state of the marker after dragging. |

---

#### **setClickListener**

Sets the event handler for marker click events.

**Signature**
```kotlin
fun setClickListener(listener: OnMarkerEventHandler?)
```

**Parameters**
| Parameter  | Type                   | Description                                                              |
|------------|------------------------|--------------------------------------------------------------------------|
| `listener` | `OnMarkerEventHandler?`| The listener to handle click events, or `null` to remove the current listener. |

---

#### **setDragStartListener**

Sets the event handler for marker drag start events.

**Signature**
```kotlin
fun setDragStartListener(listener: OnMarkerEventHandler?)
```

**Parameters**
| Parameter  | Type                   | Description                                                                    |
|------------|------------------------|--------------------------------------------------------------------------------|
| `listener` | `OnMarkerEventHandler?`| The listener to handle drag start events, or `null` to remove the current listener. |

---

#### **setDragListener**

Sets the event handler for marker drag events.

**Signature**
```kotlin
fun setDragListener(listener: OnMarkerEventHandler?)
```

**Parameters**
| Parameter  | Type                   | Description                                                              |
|------------|------------------------|--------------------------------------------------------------------------|
| `listener` | `OnMarkerEventHandler?`| The listener to handle drag events, or `null` to remove the current listener. |

---

#### **setDragEndListener**

Sets the event handler for marker drag end events.

**Signature**
```kotlin
fun setDragEndListener(listener: OnMarkerEventHandler?)
```

**Parameters**
| Parameter  | Type                   | Description                                                                  |
|------------|------------------------|------------------------------------------------------------------------------|
| `listener` | `OnMarkerEventHandler?`| The listener to handle drag end events, or `null` to remove the current listener. |

---

#### **setAnimateStartListener**

Sets the event handler for marker animation start events.

**Signature**
```kotlin
fun setAnimateStartListener(listener: OnMarkerEventHandler?)
```

**Parameters**
| Parameter  | Type                   | Description                                                                        |
|------------|------------------------|------------------------------------------------------------------------------------|
| `listener` | `OnMarkerEventHandler?`| The listener to handle animation start events, or `null` to remove the current listener. |

---

#### **setAnimateEndListener**

Sets the event handler for marker animation end events.

**Signature**
```kotlin
fun setAnimateEndListener(listener: OnMarkerEventHandler?)
```

**Parameters**
| Parameter  | Type                   | Description                                                                      |
|------------|------------------------|----------------------------------------------------------------------------------|
| `listener` | `OnMarkerEventHandler?`| The listener to handle animation end events, or `null` to remove the current listener. |

<br/>

## `DefaultArcGISMarkerEventController`

A default implementation of `ArcGISMarkerEventControllerInterface`. It acts as a proxy, delegating event handling and state management logic to an underlying `ArcGISMarkerController`. This class bridges raw map interactions with the core marker management system.

### Constructor

**Signature**
```kotlin
DefaultArcGISMarkerEventController(
    controller: ArcGISMarkerController
)
```

**Description**
Creates an instance of `DefaultArcGISMarkerEventController`.

**Parameters**
| Parameter    | Type                   | Description                                                              |
|--------------|------------------------|--------------------------------------------------------------------------|
| `controller` | `ArcGISMarkerController` | The main marker controller that manages marker state and dispatches events. |

<br/>

## `StrategyArcGISMarkerEventController`

An alternative implementation of `ArcGISMarkerEventControllerInterface` designed to work with a `StrategyMarkerController`. Unlike `DefaultArcGISMarkerEventController`, this class manages the state of the selected marker internally during a drag operation. It delegates event dispatching and marker lookups to the provided `StrategyMarkerController`.

### Constructor

**Signature**
```kotlin
StrategyArcGISMarkerEventController(
    controller: StrategyMarkerController<ArcGISActualMarker>
)
```

**Description**
Creates an instance of `StrategyArcGISMarkerEventController`.

**Parameters**
| Parameter    | Type                                          | Description                                                                      |
|--------------|-----------------------------------------------|----------------------------------------------------------------------------------|
| `controller` | `StrategyMarkerController<ArcGISActualMarker>`| The strategy-based marker controller for event dispatching and marker lookups. |