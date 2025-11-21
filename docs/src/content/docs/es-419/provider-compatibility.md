---
title: Compatibilidad de proveedores
---

# Compatibilidad de proveedores

En esta página se describen las funciones que MapConductor admite en cada proveedor de mapas y las diferencias de comportamiento más relevantes.

## Resumen de compatibilidad

MapConductor es compatible con Google Maps, Mapbox, HERE, ArcGIS y MapLibre. Las funciones principales del mapa (marcadores, círculos, polilíneas, polígonos, etc.) están diseñadas para usarse mediante una API lo más común posible entre proveedores.

Algunas funciones son específicas de ciertos proveedores, por ejemplo, `GroundImage` solo está disponible en Google Maps.

## Funciones principales y soporte

Ejemplos de compatibilidad por tipo de función:

- **Mapa**: compatible en todos los proveedores.
- **Marcador**: compatible en todos los proveedores (aunque la apariencia y animaciones pueden variar entre SDKs).
- **Círculo / polilínea / polígono**: compatibles en todos los proveedores.
- **GroundImage**: actualmente solo Google Maps.

Consulta también la documentación de cada componente, como [Marker](/es-419/components/marker) y [Polyline](/es-419/components/polyline), para ver detalles adicionales.

## Eventos e interacciones

Los eventos de interacción (como movimientos de cámara o toques sobre el mapa) se exponen a través de interfaces unificadas, pero pueden existir diferencias entre proveedores:

- Algunos SDK no ofrecen ciertos eventos.
- El momento o la frecuencia con la que se disparan los eventos puede variar.

MapConductor intenta ofrecer un comportamiento coherente entre proveedores, pero para casos sensibles (como tracking preciso), se recomienda probar el comportamiento real en cada SDK.

## Rendimiento y limitaciones

El rendimiento al mostrar grandes cantidades de marcadores o polígonos puede variar según el proveedor de mapas. Aunque MapConductor aplica optimizaciones en la medida de lo posible, ten en cuenta:

- Las capacidades del dispositivo (CPU, GPU, memoria) influyen en el rendimiento.
- Algunos proveedores imponen límites en el número de objetos o capas.

Para optimizaciones avanzadas, consulta también [Experimental / Marker Strategy](/es-419/experimental/marker-strategy).

