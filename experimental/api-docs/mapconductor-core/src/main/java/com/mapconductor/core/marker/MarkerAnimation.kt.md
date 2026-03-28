# MarkerAnimation

An enum defining the types of animations that can be applied to a marker.

## Signature

```kotlin
enum class MarkerAnimation
```

## Description

The `MarkerAnimation` enum specifies the animation to be played when a marker is added to the map or when its state is updated. These animations provide visual feedback to the user, enhancing the user experience by drawing attention to marker placement or changes.

## Enum Values

| Value    | Description                                                                                                |
| :------- | :--------------------------------------------------------------------------------------------------------- |
| `Drop`   | The marker appears to drop from the top of the screen to its final position on the map.                      |
| `Bounce` | The marker plays a continuous bouncing animation at its position. This is useful for highlighting a marker. |

## Example

The following example demonstrates how to set a `Drop` animation for a new marker using a hypothetical `MarkerOptions` builder.

```kotlin
// Create a new marker with a drop animation
val markerOptions = MarkerOptions()
    .position(LatLng(40.7128, -74.0060))
    .title("New York City")
    .animation(MarkerAnimation.Drop) // Apply the drop animation

// Add the configured marker to the map
val marker = map.addMarker(markerOptions)

// To make a marker bounce later, you might do something like this:
marker.setAnimation(MarkerAnimation.Bounce)
```