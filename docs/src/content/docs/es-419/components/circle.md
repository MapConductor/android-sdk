---
title: "Circle"
---

import CircleBasicSignature from '~/components/components/circle/CircleBasicSignature.astro';
import CircleStateSignature from '~/components/components/circle/CircleStateSignature.astro';
import BasicCircleExample from '~/components/components/circle/BasicCircleExample.astro';
import InteractiveCircleExample from '~/components/components/circle/InteractiveCircleExample.astro';
import MultipleCirclesExample from '~/components/components/circle/MultipleCirclesExample.astro';
import DynamicCircleExample from '~/components/components/circle/DynamicCircleExample.astro';
import OverlappingCirclesExample from '~/components/components/circle/OverlappingCirclesExample.astro';
import CircleEventHandlingExample from '~/components/components/circle/CircleEventHandlingExample.astro';
import CircleStrokeStylesExample from '~/components/components/circle/CircleStrokeStylesExample.astro';
import CircleFillStylesExample from '~/components/components/circle/CircleFillStylesExample.astro';
import CircleIdUsageExample from '~/components/components/circle/CircleIdUsageExample.astro';

Los círculos son superposiciones circulares que se dibujan sobre el mapa con radio, trazo y relleno personalizables. Son útiles para representar áreas, rangos o zonas.

## Funciones composable

### Circle básico

<CircleBasicSignature />

### Circle con estado

<CircleStateSignature />

## Parámetros

- **`center`**: Centro geográfico del círculo (`GeoPoint`).
- **`radiusMeters`**: Radio en metros (`Double`).
- **`geodesic`**: Indica si se debe dibujar el círculo usando bordes geodésicos que sigan la curvatura de la Tierra (por defecto `false`).
- **`strokeColor`**: Color del borde del círculo (por defecto `Color.Red`).
- **`strokeWidth`**: Grosor de la línea de borde (por defecto `2.dp`).
- **`fillColor`**: Color de relleno del interior del círculo (por defecto blanco semitransparente).
- **`extra`**: Datos adicionales asociados al círculo (`Serializable?`).
- **`id`**: Identificador único del círculo (`String?`).

## Ejemplos de uso

### Circle básico

<BasicCircleExample
  commentForMapViewUsage="Sustituye MapView por el proveedor de mapas que prefieras, como GoogleMapView o MapboxMapView"
/>

### Circle interactivo con marcadores

A partir del ejemplo de la app de muestra, así puedes crear un círculo interactivo con marcadores arrastrables:

<InteractiveCircleExample
  commentForMapViewUsage="Sustituye MapView por el proveedor de mapas que prefieras, como GoogleMapView o MapboxMapView"
  commentForLatOffsetNote="Cálculo simplificado: para producción conviene considerar la curvatura de la Tierra"
/>

### Varios círculos con estilos diferentes

<MultipleCirclesExample
  commentForMapViewUsage="Sustituye MapView por el proveedor de mapas que prefieras, como GoogleMapView o MapboxMapView"
/>

### Actualizaciones dinámicas de círculos

<DynamicCircleExample
  commentForMapViewUsage="Sustituye MapView por el proveedor de mapas que prefieras, como GoogleMapView o MapboxMapView"
/>

### Círculos superpuestos con Z-Index

<OverlappingCirclesExample
  commentForMapViewUsage="Sustituye MapView por el proveedor de mapas que prefieras, como GoogleMapView o MapboxMapView"
/>

## Manejo de eventos

Las interacciones con círculos se gestionan desde el componente de mapa:

<CircleEventHandlingExample
  commentForMapViewUsage="Sustituye MapView por el proveedor de mapas que prefieras, como GoogleMapView o MapboxMapView"
/>

## Opciones de estilo

### Estilos de trazo

<CircleStrokeStylesExample />

### Estilos de relleno

<CircleFillStylesExample />

## Identificación de círculos

### Uso de la propiedad ID

La propiedad `id` proporciona un identificador único para cada círculo y permite un seguimiento y gestión eficientes:

<CircleIdUsageExample />
