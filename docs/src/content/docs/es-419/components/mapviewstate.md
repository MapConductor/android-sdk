---
title: "MapViewState"
---

import { Tabs, TabItem } from '@astrojs/starlight/components';
import CameraControlExample from '~/components/components/mapviewstate/CameraControlExample.astro';
import MapDesignSwitchExample from '~/components/components/mapviewstate/MapDesignSwitchExample.astro';
import MoveCameraToGeoPointSignature from '~/components/components/mapviewstate/MoveCameraToGeoPointSignature.astro';
import MoveCameraToCameraPositionSignature from '~/components/components/mapviewstate/MoveCameraToCameraPositionSignature.astro';
import MapViewStateEventHandlingExample from '~/components/components/mapviewstate/MapViewStateEventHandlingExample.astro';

`MapViewState` es un componente principal que gestiona la inicialización del mapa, la posición de la cámara y el estado general del mapa.
Cada SDK de mapa tiene su propia implementación, pero MapConductor proporciona una API unificada en todos los proveedores.

## Implementaciones de SDK de mapa

MapConductor admite 5 SDKs de mapa, cada uno con su propia implementación de `MapViewState`:

- `GoogleMapViewStateImpl` - Google Maps
- `MapboxViewStateImpl` - Mapbox Map
- `HereViewStateImpl` - HERE Map
- `ArcGISMapViewStateImpl` - ArcGIS Map
- `MapLibreViewStateImpl` - MapLibre Map

## Propiedades principales

### Estado de inicialización

- **`isInitialized: StateFlow<InitState>`**: Rastrea el estado de inicialización del mapa.
  - `NotStarted`: La inicialización del mapa no ha comenzado.
  - `Initializing`: El mapa se está inicializando actualmente.
  - `Initialized`: El mapa está listo para usar.
  - `Failed`: La inicialización falló.

### Gestión de cámara

- **`cameraPosition: MapCameraPositionImpl`**: Posición de cámara actual e inicial cuando se carga el mapa.

### Diseño de mapa

- **`mapDesignType: ActualMapDesignType`**: Estilo/diseño del mapa (específico del proveedor).

### Movimiento de cámara

<Tabs>
<TabItem label="Movimiento simple">
- `moveCameraTo(GeoPointImpl, Long?)`

    <MoveCameraToGeoPointSignature />

    - Mueve la cámara a la posición especificada. Los ángulos de zoom e inclinación se mantienen desde el momento en que se llama a `moveCameraTo`.
    - Cuando se especifica `durationMills` en milisegundos, la cámara se mueve con animación.
</TabItem>
<TabItem label="Movimiento con opciones de cámara">
- `moveCameraTo(MapCameraPositionImpl, Long?)`

    <MoveCameraToCameraPositionSignature />

    - Mueve la cámara usando `MapCameraPositionImpl`. Se pueden especificar zoom e inclinación al mismo tiempo. Las propiedades que se omiten mantienen sus valores desde el momento en que se llamó a `moveCameraTo`.
    - Cuando se especifica `durationMills` en milisegundos, la cámara se mueve con animación.
</TabItem>
</Tabs>

## Ejemplos de uso

### Control de cámara

<CameraControlExample />

### Cambio de diseño de mapa

<MapDesignSwitchExample />

## Manejo de eventos

`MapViewState` funciona con el componente del proveedor de mapas elegido para proporcionar un manejo de eventos completo:

<MapViewStateEventHandlingExample
  commentForMapViewUsage="Reemplace MapView con su proveedor de mapas elegido, como GoogleMapView, MapboxMapView"
  commentForMapContent="Contenido del mapa"
  mapLoadedMessage="Mapa cargado exitosamente"
  mapClickPrefix="Mapa clickeado en"
/>

## Mejores prácticas

1. **Inicialización del estado**: Inicializar MapViewState con posición de cámara y límites apropiados.
2. **Animación de cámara**: Usar duraciones de animación razonables para una experiencia de usuario fluida (500 ms - 3000 ms).
3. **Estabilidad del estado**: Usar `remember` para mantener el estado entre recomposiciones.
4. **Actualizaciones reactivas**: Usar StateFlow para actualizaciones reactivas del estado de inicialización.
5. **Manejo de errores**: Verificar el estado de inicialización antes de realizar operaciones de cámara.
6. **Gestión de memoria**: Limpiar escuchadores cuando el mapa ya no sea necesario.

