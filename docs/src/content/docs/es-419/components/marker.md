---
title: "Marker"
---

import MarkerBasicSignature from '~/components/components/marker/MarkerBasicSignature.astro';
import MarkerStateSignature from '~/components/components/marker/MarkerStateSignature.astro';
import BasicMarkerExample from '~/components/components/marker/BasicMarkerExample.astro';
import CustomIconMarkerExample from '~/components/components/marker/CustomIconMarkerExample.astro';
import DraggableMarkerExample from '~/components/components/marker/DraggableMarkerExample.astro';
import MultipleMarkersExample from '~/components/components/marker/MultipleMarkersExample.astro';
import DefaultIconSignature from '~/components/components/marker/DefaultIconSignature.astro';
import DrawableDefaultIconSignature from '~/components/components/marker/DrawableDefaultIconSignature.astro';
import ImageIconSignature from '~/components/components/marker/ImageIconSignature.astro';

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

<DefaultIconSignature />

### DrawableDefaultIcon

Marcador que utiliza un recurso `Drawable` como fondo:

<DrawableDefaultIconSignature />

### ImageIcon

Marcador que utiliza una imagen `Drawable` personalizada:

<ImageIconSignature />

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
