---
title: "MapViewState"
---

`MapViewState` es el componente central que gestiona la inicialización del mapa, la posición de la cámara y el estado global del mapa. Cada proveedor tiene su propia implementación manteniendo una interfaz coherente.

## Implementaciones por proveedor

MapConductor admite varios proveedores, cada uno con su propia implementación de `MapViewState`:

- `GoogleMapViewStateImpl` – Google Maps
- `MapboxViewStateImpl` – Mapbox Maps
- `HereViewStateImpl` – HERE Maps
- `ArcGISMapViewStateImpl` – ArcGIS Maps
- `MapLibreViewStateImpl` – MapLibre Maps

## Propiedades principales

### Estado de inicialización

- **`isInitialized: StateFlow<InitState>`**: rastrea el estado de inicialización del mapa.
  - `NotStarted`: la inicialización aún no ha comenzado.
  - `Initializing`: el mapa se está inicializando.
  - `Initialized`: el mapa está listo para usarse.
  - `Failed`: la inicialización ha fallado.

### Gestión de la cámara

- **`cameraPosition: StateFlow<MapCameraPositionImpl?>`**: posición actual de la cámara.
- **`initCameraPosition: MapCameraPositionImpl`**: posición inicial de la cámara al cargar el mapa.

### Diseño del mapa

- **`mapDesignType: ActualMapDesignType`**: estilo/diseño del mapa (específico del proveedor).

## Métodos principales

### Inicialización

```kotlin
fun initAsync(init: suspend () -> Boolean)
```
Inicializa el mapa de forma asíncrona. Devuelve `true` si tiene éxito.

```kotlin
fun resetInitState()
```
Restablece el estado de inicialización a `NotStarted`.

### Movimiento de cámara

```kotlin
fun moveCameraTo(
    cameraPosition: MapCameraPositionImpl,
    durationMills: Long? = 0,
    listener: MoveCameraCallback? = null
)
```
Mueve la cámara a una posición específica, opcionalmente con animación.

```kotlin
fun moveCameraTo(
    position: GeoPointImpl,
    durationMills: Long? = 0,
    listener: MoveCameraCallback? = null
)
```
Mueve la cámara para centrar un punto geográfico.

## Ejemplo de uso

### Configuración básica

```kotlin
@Composable
fun MapExample() {
    // Crear el estado del mapa (elige el proveedor)
    val mapViewState = rememberGoogleMapViewState()

    // Observar el estado de inicialización
    val initState by mapViewState.isInitialized.collectAsState()

    when (initState) {
        InitState.NotStarted -> {
            LaunchedEffect(Unit) {
                mapViewState.initAsync {
                    // Realiza aquí cualquier inicialización adicional
                    true
                }
            }
        }
        InitState.Initializing -> {
            LoadingScreen(message = "Inicializando mapa...")
        }
        InitState.Initialized -> {
            GoogleMapView(state = mapViewState) {
                // Contenido del mapa
            }
        }
        InitState.Failed -> {
            ErrorScreen(onRetry = { mapViewState.resetInitState() })
        }
    }
}
```

### Control de cámara

```kotlin
@Composable
fun CameraControlExample() {
    val london = GeoPointImpl.fromLatLong(51.4985, 0.0)
    val camera = MapCameraPositionImpl(
        position = london,
        zoom = 8.0,
    )
    val mapViewState = rememberGoogleMapViewState(cameraPosition = camera)
    val scope = rememberCoroutineScope()

    Column {
        Row {
            Button(onClick = {
                scope.launch {
                    mapViewState.moveCameraTo(
                        cameraPosition = MapCameraPositionImpl(
                            position = london,
                            zoom = 12.0
                        ),
                        durationMills = 1000L
                    )
                }
            }) {
                Text("Zoom to London")
            }
        }

        GoogleMapView(state = mapViewState) {
            // Contenido del mapa
        }
    }
}
```

