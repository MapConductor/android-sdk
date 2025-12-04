---
title: "Polygon"
---

import { Tabs, TabItem } from '@astrojs/starlight/components';
import BasicPolygonExample from '~/components/components/polygon/BasicPolygonExample.astro';
import InteractivePolygonExample from '~/components/components/polygon/InteractivePolygonExample.astro';
import MultiplePolygonsExample from '~/components/components/polygon/MultiplePolygonsExample.astro';
import DynamicPolygonExample from '~/components/components/polygon/DynamicPolygonExample.astro';
import ComplexPolygonExample from '~/components/components/polygon/ComplexPolygonExample.astro';
import PolygonEventHandlingExample from '~/components/components/polygon/PolygonEventHandlingExample.astro';
import PolygonBasicSignature from '~/components/components/polygon/PolygonBasicSignature.astro';
import PolygonStateSignature from '~/components/components/polygon/PolygonStateSignature.astro';
import PolygonFillStyleExamples from '~/components/components/polygon/PolygonFillStyleExamples.astro';
import PolygonStrokeStyleExamples from '~/components/components/polygon/PolygonStrokeStyleExamples.astro';

Un polígono es una forma cerrada que define un área con propiedades de trazo y relleno personalizables. Los polígonos son útiles para representar zonas, regiones, límites o características basadas en áreas.

## Funciones Composable

<Tabs>
<TabItem label="Polígono Básico">

<PolygonBasicSignature />
</TabItem>
<TabItem label="Polígono con Estado">

<PolygonStateSignature />
</TabItem>
</Tabs>

## Parámetros

- **`points`**: Lista de coordenadas geográficas que definen los vértices del polígono (`List<GeoPoint>`).
- **`id`**: Identificador único opcional para el polígono (`String?`).
- **`strokeColor`**: Color del límite del polígono (predeterminado: `Color.Black`).
- **`strokeWidth`**: Ancho de la línea de límite (predeterminado: `1.dp`).
- **`fillColor`**: Color de relleno del interior del polígono (predeterminado: `Color.Transparent`).
- **`geodesic`**: Si se deben dibujar bordes geodésicos (predeterminado: `false`).
- **`extra`**: Datos adicionales adjuntos al polígono (`Serializable?`).

## Ejemplos de uso

### Polígono Básico

<BasicPolygonExample />

### Polígono interactivo con marcadores de vértice

<InteractivePolygonExample />

### Múltiples polígonos con diferentes estilos

<MultiplePolygonsExample />

### Forma de polígono compleja

<ComplexPolygonExample />

### Creación dinámica de polígonos

<DynamicPolygonExample />

## Manejo de eventos

Las interacciones de polígono se gestionan mediante el componente del proveedor de mapas:

<PolygonEventHandlingExample />

## Opciones de estilo

### Variaciones de relleno

<PolygonFillStyleExamples
  commentForSolidFill="Relleno sólido"
  commentForSemiTransparentFill="Relleno semi-transparente"
  commentForNoFill="Sin relleno (solo contorno)"
  commentForGradientEffect="Efecto similar a degradado usando múltiples polígonos"
/>

### Variaciones de trazo

<PolygonStrokeStyleExamples
  commentForThinBorder="Borde fino"
  commentForThickBorder="Borde grueso"
  commentForNoBorder="Sin borde"
/>

## Mejores prácticas

1. **Cerrar el polígono**: Asegúrate siempre de que el último punto sea igual al primero para cerrar correctamente el polígono.
2. **Orden de vértices**: Usa un orden de vértices consistente (sentido horario o antihorario) para resultados predecibles.
3. **Rendimiento**: Evita polígonos excesivamente complejos con cientos de vértices.
4. **Claridad visual**: Usa colores y niveles de transparencia apropiados para buena visibilidad.
5. **Retroalimentación interactiva**: Proporciona feedback visual cuando los polígonos sean clicables.
6. **Manejo de agujeros**: Simula agujeros usando polígonos superpuestos con colores diferentes.
7. **Bordes geodésicos**: Usa bordes geodésicos para polígonos grandes para considerar la curvatura de la Tierra.
8. **Gestión de estado**: Gestiona eficientemente los datos de vértices del polígono y maneja actualizaciones de forma reactiva.
9. **Validación**: Valida la geometría del polígono para asegurar que forme una figura válida.
10. **Manejo de errores**: Maneja casos límite como polígonos con menos de 3 vértices.

