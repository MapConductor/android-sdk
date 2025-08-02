// Example of how to use the circle functionality in HereMap

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.features.GeoPoint

// Example usage of the circle feature
suspend fun addCircleExample(hereMapViewController: HereMapViewController) {
    // Create a circle centered at Tokyo with a 1km radius
    val tokyoCircle = CircleState(
        center = GeoPoint(35.6762, 139.6503), // Tokyo coordinates
        radius = 1000.0, // 1000 meters = 1km
        strokeColor = Color.Red,
        strokeWidth = 2.dp,
        fillColor = Color.Red.copy(alpha = 0.3f),
        id = "tokyo-circle"
    )
    
    // Add the circle to the map
    hereMapViewController.addCircles(listOf(tokyoCircle))
    
    // You can also update the circle later
    tokyoCircle.radiusMeters = 2000.0 // Change radius to 2km
    tokyoCircle.fillColor = Color.Blue.copy(alpha = 0.3f) // Change color to blue
    hereMapViewController.updateCircle(tokyoCircle)
}

// The circle will be rendered as a polygon with 64 points approximating a circle
// It uses the Here SDK's MapPolygon internally but provides a simple circle API

// Key features:
// - Circles are converted to polygons with 64 points for smooth appearance
// - Radius is specified in meters
// - Supports stroke color, fill color, stroke width
// - Circles automatically adapt to the map projection
// - Click events can be handled (if implemented in your UI layer)