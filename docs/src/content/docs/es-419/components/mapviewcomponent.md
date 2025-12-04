---
title: "MapViewComponent"
---

import { Tabs, TabItem } from '@astrojs/starlight/components';
import GoogleMapsExample from '~/components/components/mapviewcomponent/GoogleMapsExample.astro';
import MapboxExample from '~/components/components/mapviewcomponent/MapboxExample.astro';
import AdvancedMapEventHandlingExample from '~/components/components/mapviewcomponent/AdvancedMapEventHandlingExample.astro';
import MapViewComponentSignaturesTabs from '~/components/components/mapviewcomponent/MapViewComponentSignaturesTabs.astro';
import ProviderIndependentMapContentExample from '~/components/components/mapviewcomponent/ProviderIndependentMapContentExample.astro';

MapConductor proporciona componentes de vista de mapa específicos del proveedor que sirven como base para mostrar mapas en tu aplicación. Cada SDK de mapa tiene su propia implementación, pero se mantiene una interfaz de API consistente entre todos los proveedores.

## Componentes específicos del proveedor de mapas

MapConductor admite múltiples SDKs de mapa, cada uno con un componente dedicado.
Normalmente se colocan múltiples objetos (marcadores, polilíneas, etc.) en un mapa, por lo que en lugar de configurar callbacks de eventos en cada objeto,
los callbacks se configuran en el componente específico del proveedor de mapas.

Los callbacks de eventos especiales específicos del proveedor aún no se admiten en v{BOM_MODULE_VERSION}.

<MapViewComponentSignaturesTabs />


## Parámetros comunes

Todos los componentes de vista de mapa comparten los siguientes parámetros:

### Parámetros principales

- **`modifier`**: Modificador de Compose para estilo y diseño.
- **`state`**: Implementación de estado de vista de mapa específica del proveedor.
- **`content`**: Contenido Composable que contiene superposiciones de mapa (marcadores, círculos, etc.).

### Controladores de eventos

- **`onMapLoaded`**: Se llama cuando el mapa ha terminado de cargar.
- **`onMapClick`**: Se llama cuando el usuario toca el mapa.
- **Eventos de cámara**: `onCameraMoveStart`, `onCameraMove`, `onCameraMoveEnd`.
- **Eventos de marcador**: `onMarkerClick`, `onMarkerDragStart`, `onMarkerDrag`, `onMarkerDragEnd`, `onMarkerAnimateStart`, `onMarkerAnimateEnd`.
- **Eventos de superposición**: `onCircleClick`, `onPolylineClick`, `onPolygonClick`, `onGroundImageClick`.

### Parámetros avanzados

- **`markerRenderingStrategy`**: Estrategia de renderizado de marcadores personalizada para optimizar el rendimiento.

## Ejemplos de uso

<Tabs>
<TabItem label="Implementación de Google Maps">
<GoogleMapsExample />
</TabItem>
<TabItem label="Implementación de Mapbox">
<MapboxExample />
</TabItem>
</Tabs>



### Patrón independiente del proveedor de mapas

Aun cuando se requieren componentes específicos de cada SDK, puedes crear contenido independiente del proveedor:

<ProviderIndependentMapContentExample
  markerLatitude={37.7749}
  markerLongitude={-122.4194}
  markerLabel="Punto"
  markerExtra="Marcador común"
  radiusMeters={500.0}
  strokeColorExpr="Color.Green"
  fillColorExpr="Color.Green.copy(alpha = 0.2f)"
  commentForReusableContent="Contenido reutilizable"
  commentForSameContentDifferentSdk="Mismo contenido, diferente SDK de mapa"
/>

### Manejo avanzado de eventos

<AdvancedMapEventHandlingExample />

## Diferencias entre SDKs de mapa

Aun cuando la API es consistente entre los diferentes SDKs de mapa, existen diferencias en las siguientes áreas:

### Características soportadas
- **GroundImage**: Actualmente soportado en Google Maps y ArcGIS.
- **Animación de marcadores**: Disponible en Google Maps y Mapbox.
- **Estilo personalizado**: Cada SDK de mapa tiene opciones de estilo diferentes.

### Características de rendimiento
- **Google Maps**: Excelente para uso general con buen rendimiento de marcadores.
- **Mapbox**: Óptimo para estilos personalizados y conjuntos de datos grandes.
- **HERE Maps**: Optimizado para integración con servicios de localización.
- **ArcGIS**: Óptimo para aplicaciones GIS y empresariales.

### Integración con la plataforma
Cada SDK de mapa puede tener requisitos diferentes respecto a claves de API, permisos y configuración de la plataforma. Consulta la documentación de configuración específica de cada proveedor para más detalles.

## Mejores prácticas

1. **Elegir el SDK adecuado**: Selecciona en función de las necesidades específicas de tu aplicación (estilo, rendimiento, características).
2. **Gestión consistente del estado**: Usa patrones de estado coherentes independientemente del SDK de mapa.
3. **Contenido reutilizable**: Crea contenido Composable independiente del proveedor siempre que sea posible.
4. **Manejo de eventos**: Implementa un manejo de eventos completo para ofrecer una mejor experiencia de usuario.
5. **Manejo de errores**: Maneja siempre los fallos de inicialización y proporciona una UI alternativa.
6. **Rendimiento**: Considera estrategias de renderizado personalizadas para conjuntos grandes de marcadores.
7. **Pruebas**: Prueba tu aplicación con múltiples SDKs de mapa para garantizar la compatibilidad.

