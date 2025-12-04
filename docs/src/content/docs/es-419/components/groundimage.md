---
title: "GroundImage"
---

import GroundImageBasicSignature from '~/components/components/groundimage/GroundImageBasicSignature.astro';
import GroundImageStateSignature from '~/components/components/groundimage/GroundImageStateSignature.astro';
import BasicGroundImageExample from '~/components/components/groundimage/BasicGroundImageExample.astro';
import InteractiveGroundImageExample from '~/components/components/groundimage/InteractiveGroundImageExample.astro';
import MultipleGroundImagesExample from '~/components/components/groundimage/MultipleGroundImagesExample.astro';
import DynamicGroundImageExample from '~/components/components/groundimage/DynamicGroundImageExample.astro';
import GroundImageEventHandlingExample from '~/components/components/groundimage/GroundImageEventHandlingExample.astro';
import GroundImageOpacityStylesExample from '~/components/components/groundimage/GroundImageOpacityStylesExample.astro';

Las ground images son superposiciones de imagen posicionadas geográficamente sobre el mapa. Son útiles para mostrar planos de plantas, imágenes satelitales, overlays meteorológicos o cualquier dato basado en imagen que deba anclarse a coordenadas específicas.

## Funciones composable

### GroundImage básica

<GroundImageBasicSignature />

### GroundImage con estado

<GroundImageStateSignature />

## Parámetros

- **`bounds`**: Límites rectangulares geográficos donde debe colocarse la imagen (`GeoRectBounds`).
- **`image`**: Imagen `Drawable` que se va a mostrar.
- **`opacity`**: Nivel de opacidad entre 0.0 (transparente) y 1.0 (opaco) (por defecto `0.5f`).
- **`id`**: Identificador único opcional para la ground image (`String?`).
- **`extra`**: Datos adicionales asociados a la ground image (`Serializable?`).

## Ejemplos de uso

### GroundImage básica

<BasicGroundImageExample />

### GroundImage interactiva con marcadores de límites

<InteractiveGroundImageExample />

### Varias GroundImages con diferente opacidad

<MultipleGroundImagesExample />

### Carga dinámica de GroundImage

<DynamicGroundImageExample />

## Manejo de eventos

Las interacciones de GroundImage se gestionan mediante el componente de mapa de tu proveedor:

<GroundImageEventHandlingExample />

## Opciones de estilo

### Control de opacidad

<GroundImageOpacityStylesExample />

## Buenas prácticas

1. **Calidad de imagen**: Optimiza el tamaño y resolución de la imagen para evitar un uso excesivo de memoria.
2. **Uso de opacidad**: Usa niveles de opacidad intermedios para permitir que el mapa base siga siendo legible.
3. **Precisión de límites**: Define límites (`bounds`) precisos para asegurar que la imagen se alinee correctamente con el mapa.
4. **Rendimiento**: Evita renderizar demasiadas ground images simultáneamente para no afectar al rendimiento.
5. **Caché**: Considera cachear imágenes que se cargan repetidamente.
6. **Carga dinámica**: Para conjuntos grandes de imágenes, carga dinámicamente solo las imágenes visibles.
7. **Z-Index**: Mantén el orden correcto de capas cuando superpongas múltiples imágenes.

