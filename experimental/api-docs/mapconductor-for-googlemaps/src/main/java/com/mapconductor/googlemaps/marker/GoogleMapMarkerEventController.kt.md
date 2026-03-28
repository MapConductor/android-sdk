# SDK Documentation

This document provides a detailed overview of the Marker Event Controllers for Google Maps, including an interface and its implementations. These components are responsible for managing and dispatching marker-related events.

## `GoogleMapMarkerEventControllerInterface`

An interface that defines the contract for controlling and handling events related to Google Map markers. It extends the core `MarkerEventControllerInterface` and provides methods to set event listeners and programmatically dispatch events.

---

### `getEntity`

Retrieves a marker entity by its unique identifier.

#### Signature

```kotlin
fun getEntity(id: String): MarkerEntityInterface<GoogleMapActualMarker>?
```

#### Description

This function allows you to fetch a specific `MarkerEntityInterface` instance from the controller's internal manager using its ID. This is useful when you need to interact with a specific marker that is already on the map.

#### Parameters

| Parameter | Type   | Description                               |
| :-------- | :----- | :---------------------------------------- |
| `id`      | String | The unique identifier of the marker entity. |

#### Returns

| Type                                              | Description                                                    |
| :------------------------------------------------ | :------------------------------------------------------------- |
| `MarkerEntityInterface<GoogleMapActualMarker>?` | The corresponding marker entity, or `null` if no marker with the specified `id` is found. |

---

### `dispatchClick`

Programmatically dispatches a click event for a marker.

#### Signature

```kotlin
fun dispatchClick(state: MarkerState)
```

#### Description

This function manually triggers the `onMarkerClick` event handler, if one is set. It simulates a user clicking on a marker.

#### Parameters

| Parameter | Type          | Description                                                |
| :-------- | :------------ | :--------------------------------------------------------- |
| `state`   | `MarkerState` | The state of the marker at the time the event is dispatched. |

---

### `dispatchDragStart`

Programmatically dispatches a drag start event for a marker.

#### Signature

```kotlin
fun dispatchDragStart(state: MarkerState)
```

#### Description

This function manually triggers the `onMarkerDragStart` event handler, simulating the start of a drag operation on a marker.

#### Parameters

| Parameter | Type          | Description                                                |
| :-------- | :------------ | :--------------------------------------------------------- |
| `state`   | `MarkerState` | The state of the marker at the time the event is dispatched. |

---

### `dispatchDrag`

Programmatically dispatches a drag event for a marker.

#### Signature

```kotlin
fun dispatchDrag(state: MarkerState)
```

#### Description

This function manually triggers the `onMarkerDrag` event handler, simulating an ongoing drag operation.

#### Parameters

| Parameter | Type          | Description                                                |
| :-------- | :------------ | :--------------------------------------------------------- |
| `state`   | `MarkerState` | The state of the marker at the time the event is dispatched. |

---

### `dispatchDragEnd`

Programmatically dispatches a drag end event for a marker.

#### Signature

```kotlin
fun dispatchDragEnd(state: MarkerState)
```

#### Description

This function manually triggers the `onMarkerDragEnd` event handler, simulating the completion of a drag operation.

#### Parameters

| Parameter | Type          | Description                                                |
| :-------- | :------------ | :--------------------------------------------------------- |
| `state`   | `MarkerState` | The state of the marker at the time the event is dispatched. |

---

### `setClickListener`

Sets or clears the event handler for marker click events.

#### Signature

```kotlin
fun setClickListener(listener: OnMarkerEventHandler?)
```

#### Parameters

| Parameter  | Type                   | Description                                                                                             |
| :--------- | :--------------------- | :------------------------------------------------------------------------------------------------------ |
| `listener` | `OnMarkerEventHandler?` | The handler to be invoked on a marker click. The handler receives a `MarkerState` object. Pass `null` to remove the current listener. |

---

### `setDragStartListener`

Sets or clears the event handler for marker drag start events.

#### Signature

```kotlin
fun setDragStartListener(listener: OnMarkerEventHandler?)
```

#### Parameters

| Parameter  | Type                   | Description                                                                                             |
| :--------- | :--------------------- | :------------------------------------------------------------------------------------------------------ |
| `listener` | `OnMarkerEventHandler?` | The handler to be invoked when a marker drag starts. The handler receives a `MarkerState` object. Pass `null` to remove the current listener. |

---

### `setDragListener`

Sets or clears the event handler for marker drag events.

#### Signature

```kotlin
fun setDragListener(listener: OnMarkerEventHandler?)
```

#### Parameters

| Parameter  | Type                   | Description                                                                                             |
| :--------- | :--------------------- | :------------------------------------------------------------------------------------------------------ |
| `listener` | `OnMarkerEventHandler?` | The handler to be invoked during a marker drag. The handler receives a `MarkerState` object. Pass `null` to remove the current listener. |

---

### `setDragEndListener`

Sets or clears the event handler for marker drag end events.

#### Signature

```kotlin
fun setDragEndListener(listener: OnMarkerEventHandler?)
```

#### Parameters

| Parameter  | Type                   | Description                                                                                             |
| :--------- | :--------------------- | :------------------------------------------------------------------------------------------------------ |
| `listener` | `OnMarkerEventHandler?` | The handler to be invoked when a marker drag ends. The handler receives a `MarkerState` object. Pass `null` to remove the current listener. |

---

### `setAnimateStartListener`

Sets or clears the event handler for marker animation start events.

#### Signature

```kotlin
fun setAnimateStartListener(listener: OnMarkerEventHandler?)
```

#### Parameters

| Parameter  | Type                   | Description                                                                                             |
| :--------- | :--------------------- | :------------------------------------------------------------------------------------------------------ |
| `listener` | `OnMarkerEventHandler?` | The handler to be invoked when a marker animation starts. The handler receives a `MarkerState` object. Pass `null` to remove the current listener. |

---

### `setAnimateEndListener`

Sets or clears the event handler for marker animation end events.

#### Signature

```kotlin
fun setAnimateEndListener(listener: OnMarkerEventHandler?)
```

#### Parameters

| Parameter  | Type                   | Description                                                                                             |
| :--------- | :--------------------- | :------------------------------------------------------------------------------------------------------ |
| `listener` | `OnMarkerEventHandler?` | The handler to be invoked when a marker animation ends. The handler receives a `MarkerState` object. Pass `null` to remove the current listener. |

<br>

## `DefaultGoogleMapMarkerEventController`

A default implementation of `GoogleMapMarkerEventControllerInterface`. It delegates all event handling and listener management calls to an underlying `GoogleMapMarkerController` instance. This class provides a straightforward way to connect marker events to the main marker controller.

### Constructor

#### Signature

```kotlin
DefaultGoogleMapMarkerEventController(
    private val controller: GoogleMapMarkerController
)
```

#### Parameters

| Parameter    | Type                        | Description                                                              |
| :----------- | :-------------------------- | :----------------------------------------------------------------------- |
| `controller` | `GoogleMapMarkerController` | The main marker controller that will manage the markers and their events. |

<br>

## `StrategyGoogleMapMarkerEventController`

An implementation of `GoogleMapMarkerEventControllerInterface` that uses a `StrategyMarkerController`. This class is designed to work within a strategy pattern, where the logic for handling marker events is encapsulated in a separate strategy controller. It delegates all method calls to the provided `StrategyMarkerController`.

### Constructor

#### Signature

```kotlin
StrategyGoogleMapMarkerEventController(
    private val controller: StrategyMarkerController<GoogleMapActualMarker>
)
```

#### Parameters

| Parameter    | Type                                                | Description                                                        |
| :----------- | :-------------------------------------------------- | :----------------------------------------------------------------- |
| `controller` | `StrategyMarkerController<GoogleMapActualMarker>` | The strategy controller responsible for implementing the event logic. |