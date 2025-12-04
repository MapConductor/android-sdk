---
title: "Polyline"
---

import { Tabs, TabItem } from '@astrojs/starlight/components';
import BasicPolylineExample from '~/components/components/polyline/BasicPolylineExample.astro';
import InteractivePolylineExample from '~/components/components/polyline/InteractivePolylineExample.astro';
import DynamicPolylineExample from '~/components/components/polyline/DynamicPolylineExample.astro';
import GeodesicPolylineExample from '~/components/components/polyline/GeodesicPolylineExample.astro';
import PolylineBasicSignature from '~/components/components/polyline/PolylineBasicSignature.astro';
import PolylineStateSignature from '~/components/components/polyline/PolylineStateSignature.astro';
import PolylineEventHandlingExample from '~/components/components/polyline/PolylineEventHandlingExample.astro';
import PolylineWidthStyleExamples from '~/components/components/polyline/PolylineWidthStyleExamples.astro';
import PolylineColorStyleExamples from '~/components/components/polyline/PolylineColorStyleExamples.astro';

Polylines are sequences of line segments that connect multiple geographic points. They are commonly used for routes, paths, boundaries, or any linear features on the map.

## Composable Functions

<Tabs>
<TabItem label="Basic Polyline">

For a small number of polylines, you can specify options directly. Specifying an `id` helps prevent unnecessary recomposition.

<PolylineBasicSignature />

</TabItem>
<TabItem label="Polyline with State">

For a large number of polylines or when moving polylines, using state is recommended. Specifying an `id` helps prevent unnecessary recomposition.

<PolylineStateSignature />

</TabItem>
</Tabs>

## Parameters

- **`points`**: List of geographic coordinates defining the line segment (`List<GeoPoint>`)
- **`id`**: Optional unique identifier for the polyline (`String?`)
- **`strokeColor`**: Color of the line (default: `Color.Black`)
- **`strokeWidth`**: Width of the line (default: `1.dp`)
- **`geodesic`**: Whether to draw the line using geodesic edges that follow the Earth's curvature (default: `false`)
- **`extra`**: Additional data attached to the polyline (`Serializable?`)

## Usage Examples

### Basic Polyline

<BasicPolylineExample
  centerLat={53.566853}
  centerLng={9.988269}
  zoom={14.0}
  commentForMapViewUsage="Replace MapView with your chosen map provider, such as GoogleMapView, MapboxMapView"
/>

### Interactive Polyline with Waypoint Markers

<InteractivePolylineExample />

<video width="720" height="480" controls>
 <source src="/polyline/interactive-polyline-example.webm" type="video/webm" />
 <source src="/polyline/interactive-polyline-example.mp4" type="video/mp4" />
</video>

### Dynamic Polyline Construction

<DynamicPolylineExample />

<video width="720" height="480" controls>
 <source src="/polyline/dynamic-polyline-example.webm" type="video/webm" />
 <source src="/polyline/dynamic-polyline-example.mp4" type="video/mp4" />
</video>

### Geodesic vs Standard Lines

<GeodesicPolylineExample />

<video width="720" height="450" controls>
 <source src="/polyline/geodesic-polyline-example.webm" type="video/webm" />
 <source src="/polyline/geodesic-polyline-example.mp4" type="video/mp4" />
</video>

## Event Handling

Polyline interactions are handled by your map provider component:

<PolylineEventHandlingExample />

## Styling Options

### Line Width Variations

<PolylineWidthStyleExamples />

### Color Variations

<PolylineColorStyleExamples />

## Best Practices

1. **Point Density**: Balance detail and performance - too many points can slow down rendering
2. **Geodesic Lines**: Use geodesic for long-distance routes to display accurate paths
3. **Visual Hierarchy**: Use different colors and widths to distinguish different types of routes
4. **Interactive Feedback**: Provide visual feedback when polylines are clickable
5. **Performance**: Consider using simplified geometry for complex polylines at specific zoom levels
6. **Color Contrast**: Ensure the polyline color stands out against the map background
7. **Route Direction**: Consider adding arrows or markers along the route to indicate direction
8. **State Management**: Efficiently store and reactively update polyline data as needed

