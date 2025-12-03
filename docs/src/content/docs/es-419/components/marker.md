---
title: "Marker"
---

import MarkerBasicSignature from '~/components/components/marker/MarkerBasicSignature.astro';
import MarkerStateSignature from '~/components/components/marker/MarkerStateSignature.astro';
import BasicMarkerExample from '~/components/components/marker/BasicMarkerExample.astro';
import CustomIconMarkerExample from '~/components/components/marker/CustomIconMarkerExample.astro';
import DraggableMarkerExample from '~/components/components/marker/DraggableMarkerExample.astro';
import MultipleMarkersExample from '~/components/components/marker/MultipleMarkersExample.astro';

Los marcadores son anotaciones de punto que se colocan en el mapa en ubicaciones geográficas concretas. Admiten iconos personalizados, interacciones y animaciones.

## Funciones composable

### Marker básico

<MarkerBasicSignature />

### Marker con estado

<MarkerStateSignature />

## Parámetros

- **`position`**: Coordenadas geográficas del marcador (`GeoPoint`).
- **`clickable`**: Indica si el marcador responde a clics (por defecto: `true`).
- **`draggable`**: Indica si el marcador se puede arrastrar (por defecto: `false`).
- **`icon`**: Icono personalizado del marcador (`MarkerIcon?`).
- **`extra`**: Datos adicionales asociados al marcador (`Serializable?`).
- **`id`**: Identificador único del marcador (`String?`, se genera automáticamente si no se proporciona).

## Tipos de icono

### DefaultIcon

Icono estándar de marcador con apariencia configurable:

```kotlin
DefaultIcon(
    scale: Float = 1.0f,
    label: String? = null,
    fillColor: Color = Color.Red,
    strokeColor: Color = Color.Black,
    strokeWidth: Dp = 1.dp,
    labelTextColor: Color = Color.White,
    labelStrokeColor: Color? = null,
    debug: Boolean = false
)
```

### DrawableDefaultIcon

Marcador que utiliza un recurso `Drawable` como fondo:

```kotlin
DrawableDefaultIcon(
    backgroundDrawable: Drawable,
    scale: Float = 1.0f,
    strokeColor: Color? = null,
    strokeWidth: Dp = 1.dp
)
```

### ImageIcon

Marcador que utiliza una imagen `Drawable` personalizada:

```kotlin
ImageIcon(
    drawable: Drawable,
    anchor: Offset = Offset(0.5f, 0.5f),
    debug: Boolean = false
)
```

## Ejemplos de uso

### Marker básico

<BasicMarkerExample
  commentForMapViewUsage="Sustituye MapView por el componente del proveedor que utilices, por ejemplo GoogleMapView o MapboxMapView"
/>

### Marker con icono personalizado

<CustomIconMarkerExample
  commentForMapViewUsage="Sustituye MapView por el componente del proveedor que utilices, por ejemplo GoogleMapView o MapboxMapView"
/>

### Marker arrastrable

<DraggableMarkerExample
  commentForMapViewUsage="Sustituye MapView por el componente del proveedor que utilices, por ejemplo GoogleMapView o MapboxMapView"
/>

### Varios marcadores con distintos iconos

<MultipleMarkersExample
  commentForMapViewUsage="Sustituye MapView por el componente del proveedor que utilices, por ejemplo GoogleMapView o MapboxMapView"
/>
