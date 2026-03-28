# TileRequest

A data class that encapsulates the coordinates for a map tile request.

## Signature

```kotlin
data class TileRequest(
    val x: Int,
    val y: Int,
    val z: Int
)
```

## Description

The `TileRequest` class represents a request for a specific map tile using the standard XYZ (or "Slippy Map") tiling scheme. It is a simple data holder for the three essential coordinates: `x`, `y`, and `z` (zoom level).

Instances of this class are typically used to query a tile server or a tile cache for a specific map tile image.

## Parameters

This table describes the constructor parameters for the `TileRequest` class.

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `x` | `Int` | The x-coordinate of the tile at the specified zoom level. |
| `y` | `Int` | The y-coordinate of the tile at the specified zoom level. |
| `z` | `Int` | The zoom level for the tile. |

## Example

Here is an example of how to create and use a `TileRequest` object.

```kotlin
// Create a request for a tile at zoom level 12,
// with x-coordinate 1234 and y-coordinate 5678.
val tileRequest = TileRequest(x = 1234, y = 5678, z = 12)

// You can then access the properties of the request.
println("Requesting tile at Z/X/Y: ${tileRequest.z}/${tileRequest.x}/${tileRequest.y}")

// Expected Output:
// Requesting tile at Z/X/Y: 12/1234/5678
```