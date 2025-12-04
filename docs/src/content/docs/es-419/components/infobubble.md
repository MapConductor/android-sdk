---
title: "InfoBubble"
---

import SimpleInfoBubbleExample from '~/components/components/infobubble/SimpleInfoBubbleExample.astro';
import RichContentInfoBubbleExample from '~/components/components/infobubble/RichContentInfoBubbleExample.astro';
import InteractiveBubbleExample from '~/components/components/infobubble/InteractiveBubbleExample.astro';
import InfoBubbleSignature from '~/components/components/infobubble/InfoBubbleSignature.astro';
import MultipleBubblesExample from '~/components/components/infobubble/MultipleBubblesExample.astro';
import CustomPositioningExample from '~/components/components/infobubble/CustomPositioningExample.astro';
import LifecycleManagementExample from '~/components/components/infobubble/LifecycleManagementExample.astro';
import ManualLifecycleExample from '~/components/components/infobubble/ManualLifecycleExample.astro';
import DarkModeInfoBubbleExample from '~/components/components/infobubble/DarkModeInfoBubbleExample.astro';
import CustomThemeExample from '~/components/components/infobubble/CustomThemeExample.astro';
import EfficientUpdatesExample from '~/components/components/infobubble/EfficientUpdatesExample.astro';
import MemoryManagementExample from '~/components/components/infobubble/MemoryManagementExample.astro';
import ImplementationTipsExample from '~/components/components/infobubble/ImplementationTipsExample.astro';
import DebugModeExample from '~/components/components/infobubble/DebugModeExample.astro';

`InfoBubble` es un componente que muestra contenido personalizado en una burbuja asociada a un marcador del mapa. Permite enseñar información detallada sobre un marcador sin saturar la interfaz del mapa.

## Descripción general

InfoBubble aparece sobre un marcador concreto. Como el contenido se proporciona mediante un composable, puedes diseñar libremente el interior de la burbuja para adaptarlo al estilo de tu aplicación.

## Función composable

<InfoBubbleSignature />

## Parámetros

- **`marker`**: `MarkerState` al que se adjunta la burbuja.
- **`bubbleColor`**: Color de fondo de la burbuja (por defecto blanco).
- **`borderColor`**: Color del borde de la burbuja (por defecto negro).
- **`contentPadding`**: Padding interno alrededor del contenido (por defecto 8dp).
- **`cornerRadius`**: Radio de las esquinas redondeadas (por defecto 4dp).
- **`tailSize`**: Tamaño de la “cola” de la burbuja que apunta al marcador (por defecto 8dp).
- **`content`**: Contenido composable que se muestra dentro de la burbuja.

## Uso básico

### Burbuja de texto simple

<SimpleInfoBubbleExample />

### Burbuja con estilo personalizado

<RichContentInfoBubbleExample
  locationName="Golden Gate Park"
  locationDescription="Un gran parque urbano con jardines, museos y zonas recreativas."
  locationRating={4.5}
  latitude={37.7694}
  longitude={-122.4862}
  markerLabel="🌳"
  markerId="park-marker"
  zoomLevel={13.0}
  markerFillColorExpr="Color.Green"
/>

### Burbuja interactiva con acciones

<InteractiveBubbleExample
  storeName="Coffee Shop"
  storeAddress="1-4, Hongo 4-Chome, Bunkyo-Ku"
  storePhone="+81 (555) 123-4567"
  storeType="coffee"
  cameraLatitude={35.70662}
  cameraLongitude={139.76378}
  markerLatitude={35.708106}
  markerLongitude={139.760823}
  markerColor="0xFF8B4513"
  markerLabel="☕"
  markerId="coffee-shop-marker"
  zoomLevel={16.0}
/>

### Gestión de varias burbujas

<MultipleBubblesExample
  commentForMapViewUsage="Sustituye MapView por el proveedor de mapas que prefieras, como GoogleMapView o MapboxMapView"
  commentForTapToClose="Toca para cerrar"
  marker1Latitude={37.7749}
  marker1Longitude={-122.4194}
  marker1Name="Restaurante A"
  marker1ColorExpr="Color.Red"
  marker2Latitude={37.7849}
  marker2Longitude={-122.4094}
  marker2Name="Hotel B"
  marker2ColorExpr="Color.Blue"
  marker3Latitude={37.7649}
  marker3Longitude={-122.4294}
  marker3Name="Tienda C"
  marker3ColorExpr="Color.Green"
/>

<video width="720" height="480" controls>
 <source src="/info-bubble/multiple-bubbles-example.webm" type="video/webm" />
 <source src="/info-bubble/multiple-bubbles-example.mp4" type="video/mp4" />
</video>

### Diseños personalizados

Para implementar un diseño totalmente personalizado puedes crear tu propio componente basado en InfoBubble. El siguiente vídeo muestra un ejemplo de burbuja con la cola en el lado derecho.

<video width="720" height="480" controls>
 <source src="/info-bubble/right-info-bubble-map-example.webm" type="video/webm" />
 <source src="/info-bubble/right-info-bubble-map-example.mp4" type="video/mp4" />
</video>

## Posicionamiento y comportamiento

### Posicionamiento automático

InfoBubble gestiona el posicionamiento de forma automática:

- La cola de la burbuja apunta al centro del marcador.
- La burbuja se mantiene lo más visible posible dentro del viewport del mapa.
- Cuando el usuario desplaza o hace zoom, la burbuja sigue la posición del marcador.

### Posicionamiento personalizado

Aun cuando InfoBubble se encarga del posicionamiento, puedes influir en el resultado ajustando el ancla del icono del marcador:

<CustomPositioningExample />

## Gestión del ciclo de vida

InfoBubble gestiona su ciclo de vida automáticamente:

<LifecycleManagementExample />

### Control manual del ciclo de vida

<ManualLifecycleExample />

## Estilos y temas

### Compatibilidad con modo oscuro

<DarkModeInfoBubbleExample />

### Temas personalizados

<CustomThemeExample />

## Consideraciones de rendimiento

### Actualizaciones eficientes

<EfficientUpdatesExample />

### Gestión de memoria

<MemoryManagementExample />

## Buenas prácticas

### Guías de diseño

1. **Contenido conciso**: Las InfoBubbles deben mostrar la información esencial sin abrumar al usuario.
2. **Tamaño adecuado**: Limita el ancho de la burbuja para mantener una buena legibilidad en dispositivos móviles.
3. **Acciones claras**: Si incluyes botones, deja clara su finalidad.
4. **Zonas táctiles**: Asegúrate de que los elementos interactivos respeten el tamaño mínimo de toque.

### Experiencia de usuario

1. **Comportamiento al cerrar**: Permite cerrar la burbuja al tocar el mapa o el propio marcador.
2. **Estados de carga**: Muestra indicadores de carga cuando el contenido depende de peticiones de red.
3. **Manejo de errores**: Gestiona de forma adecuada datos ausentes o no válidos.
4. **Accesibilidad**: Proporciona descripciones de contenido para lectores de pantalla.

### Consejos de implementación

<ImplementationTipsExample />

## Resolución de problemas

### Problemas habituales

1. **La burbuja no aparece**: Comprueba que el marcador esté correctamente configurado y que InfoBubble esté dentro de `MapViewScope`.
2. **La burbuja no se cierra**: Verifica que el renderizado condicional responda a los cambios de estado.
3. **Rendimiento pobre**: Limita el número de burbujas simultáneas y optimiza la composición del contenido.
4. **Problemas de diseño**: Usa restricciones de tamaño adecuadas y prueba en diferentes tamaños de pantalla.

### Modo de depuración

<DebugModeExample />

