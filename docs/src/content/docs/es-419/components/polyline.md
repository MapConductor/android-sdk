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

Una polilínea es una secuencia de segmentos de línea que conectan múltiples puntos geográficos. Las polilíneas se usan comúnmente para representar rutas, caminos, límites o características lineales en un mapa.

## Funciones Composable

<Tabs>
<TabItem label="Polilínea Básica">

Para un pequeño número de polilíneas, puedes especificar opciones directamente. Especificar un `id` ayuda a prevenir recomposiciones innecesarias.

<PolylineBasicSignature />

</TabItem>
<TabItem label="Polilínea con Estado">

Para un gran número de polilíneas o cuando se mueven polilíneas, se recomienda usar estado. Especificar un `id` ayuda a prevenir recomposiciones innecesarias.

<PolylineStateSignature />

</TabItem>
</Tabs>

## Parámetros

- **`points`**: Lista de coordenadas geográficas que definen el segmento de línea (`List<GeoPoint>`).
- **`id`**: Identificador único opcional para la polilínea (`String?`).
- **`strokeColor`**: Color de la línea (predeterminado: `Color.Black`).
- **`strokeWidth`**: Ancho de la línea (predeterminado: `1.dp`).
- **`geodesic`**: Indica si se debe dibujar la línea utilizando bordes geodésicos que sigan la curvatura de la Tierra (predeterminado: `false`).
- **`extra`**: Datos adicionales adjuntos a la polilínea (`Serializable?`).

## Ejemplos de uso

### Polilínea Básica

<BasicPolylineExample
  centerLat={53.566853}
  centerLng={9.988269}
  zoom={14.0}
  commentForMapViewUsage="Reemplace MapView con su proveedor de mapas elegido, como GoogleMapView, MapboxMapView"
/>

### Polilínea interactiva con marcadores de waypoint

<InteractivePolylineExample />

<video width="720" height="480" controls>
 <source src="/polyline/interactive-polyline-example.webm" type="video/webm" />
 <source src="/polyline/interactive-polyline-example.mp4" type="video/mp4" />
</video>

### Construcción dinámica de polilínea

<DynamicPolylineExample />

<video width="720" height="480" controls>
 <source src="/polyline/dynamic-polyline-example.webm" type="video/webm" />
 <source src="/polyline/dynamic-polyline-example.mp4" type="video/mp4" />
</video>

### Líneas geodésicas vs líneas estándar

<GeodesicPolylineExample />

<video width="720" height="450" controls>
 <source src="/polyline/geodesic-polyline-example.webm" type="video/webm" />
 <source src="/polyline/geodesic-polyline-example.mp4" type="video/mp4" />
</video>

## Manejo de eventos

Las interacciones de polilínea se gestionan mediante el componente de tu proveedor de mapas:

<PolylineEventHandlingExample
  commentForMapViewUsage="Reemplace MapView con su proveedor de mapas elegido, como GoogleMapView, MapboxMapView"
  polylineClickedLabel="Polilínea clickeada:"
  pointsCountLabel="Número de puntos"
  clickLocationLabel="Ubicación del clic"
  extraDataLabel="Datos adicionales"
  extraValue="Ruta interactiva"
/>

## Opciones de estilo

### Variaciones de ancho de línea

<PolylineWidthStyleExamples
  commentForThinLine="Línea fina"
  commentForMediumLine="Línea media"
  commentForThickLine="Línea gruesa"
/>

### Variaciones de color

<PolylineColorStyleExamples
  commentForSolidColors="Colores sólidos"
  commentForSemiTransparent="Semi-transparente"
  commentForCustomColor="Color personalizado"
  commentForMaterialGreen="Verde tipo Material"
/>

## Mejores prácticas

1. **Densidad de puntos**: Equilibrar detalle y rendimiento; demasiados puntos pueden ralentizar el renderizado.
2. **Líneas geodésicas**: Usar geodésicas para rutas de larga distancia para mostrar caminos precisos.
3. **Jerarquía visual**: Usar diferentes colores y anchos para distinguir distintos tipos de rutas.
4. **Retroalimentación interactiva**: Proporcionar feedback visual cuando las polilíneas sean clicables.
5. **Rendimiento**: Considerar usar geometría simplificada para polilíneas complejas en ciertos niveles de zoom.
6. **Contraste de color**: Asegurar que el color de la polilínea destaque sobre el fondo del mapa.
7. **Dirección de ruta**: Considerar añadir flechas o marcadores a lo largo de la ruta para indicar dirección.
8. **Gestión de estado**: Almacenar y actualizar los datos de polilínea de forma eficiente y reactiva.

