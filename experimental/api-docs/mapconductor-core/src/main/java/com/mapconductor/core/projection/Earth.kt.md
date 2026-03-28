# Earth

The `Earth` object provides a collection of fundamental, globally-accepted constants related to the Earth's dimensions. These values are based on the World Geodetic System (WGS 84) ellipsoid model and are provided in meters.

As a Kotlin `object`, it is a singleton, and its properties can be accessed directly without instantiation.

## Signature

```kotlin
object Earth
```

## Properties

The `Earth` object contains the following constant properties:

| Property               | Type   | Description                                                                                             |
| ---------------------- | ------ | ------------------------------------------------------------------------------------------------------- |
| `CIRCUMFERENCE_METERS` | Double | The equatorial circumference of the Earth in meters. Value: `40075016.686`.                               |
| `RADIUS_METERS`        | Double | The equatorial radius of the Earth in meters, as defined by the WGS 84 ellipsoid. Value: `6378137.0`. |

## Example

This example demonstrates how to access the constants from the `Earth` object.

```kotlin
import com.mapconductor.core.projection.Earth

fun main() {
    val radius = Earth.RADIUS_METERS
    val circumference = Earth.CIRCUMFERENCE_METERS

    println("Earth's Radius: $radius meters")
    println("Earth's Circumference: $circumference meters")
}

// Expected Output:
// Earth's Radius: 6378137.0 meters
// Earth's Circumference: 40075016.686 meters
```