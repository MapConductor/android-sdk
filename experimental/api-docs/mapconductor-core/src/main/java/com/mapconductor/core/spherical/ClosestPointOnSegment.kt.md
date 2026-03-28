Of course! Here is the high-quality SDK documentation for the provided code snippet.

---

### `closestPointOnSegment`

<br/>

#### Signature

```kotlin
fun closestPointOnSegment(
    startPoint: Offset,
    endPoint: Offset,
    testPoint: Offset
): Offset
```

#### Description

Calculates the point on a 2D line segment that is closest to a given test point.

This function operates by projecting the `testPoint` onto the infinite line defined by `startPoint` and `endPoint`. The resulting projection is then clamped to the boundaries of the line segment itself. This ensures that if the projection falls outside the segment, the function returns the nearest endpoint (`startPoint` or `endPoint`). If the `startPoint` and `endPoint` are identical, the `startPoint` is returned.

#### Parameters

| Parameter | Type | Description |
| :--- | :--- | :--- |
| `startPoint` | `Offset` | The starting point of the line segment. |
| `endPoint` | `Offset` | The ending point of the line segment. |
| `testPoint` | `Offset` | The point from which to find the closest point on the segment. |

#### Returns

An `Offset` object representing the coordinates of the point on the segment `[startPoint, endPoint]` that is nearest to `testPoint`.

#### Example

The following example demonstrates how to find the closest point on a horizontal line segment from a point located above it.

```kotlin
import androidx.compose.ui.geometry.Offset

fun main() {
    // Define a line segment from (0, 0) to (10, 0)
    val start = Offset(0f, 0f)
    val end = Offset(10f, 0f)

    // 1. Test with a point whose projection is within the segment
    val testPointInside = Offset(5f, 5f)
    val closestPoint1 = closestPointOnSegment(start, end, testPointInside)
    
    println("Segment: [$start, $end]")
    println("Test Point: $testPointInside")
    // The closest point is the perpendicular projection onto the segment.
    println("Closest Point: $closestPoint1") // Expected: Offset(5.0, 0.0)
    println("---")

    // 2. Test with a point whose projection is outside the segment
    val testPointOutside = Offset(15f, 3f)
    val closestPoint2 = closestPointOnSegment(start, end, testPointOutside)

    println("Segment: [$start, $end]")
    println("Test Point: $testPointOutside")
    // The projection (15, 0) is outside the segment, so the result is clamped to the endpoint.
    println("Closest Point: $closestPoint2") // Expected: Offset(10.0, 0.0)
}

/*
Output:
Segment: [Offset(0.0, 0.0), Offset(10.0, 0.0)]
Test Point: Offset(5.0, 5.0)
Closest Point: Offset(5.0, 0.0)
---
Segment: [Offset(0.0, 0.0), Offset(10.0, 0.0)]
Test Point: Offset(15.0, 3.0)
Closest Point: Offset(10.0, 0.0)
*/
```