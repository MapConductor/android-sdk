---
title: "Inicialización"
---

En esta sección se explica cómo inicializar y configurar correctamente MapConductor para distintos SDK de mapas.

## Inicialización básica

### Dependencias Gradle

Añade MapConductor a tu `build.gradle.kts`:

```kotlin
dependencies {
    implementation "com.mapconductor:mapconductor-bom:$version"
    implementation "com.mapconductor:core"

    // Elige tus SDK de mapas
    implementation "com.mapconductor:for-googlemaps"
    implementation "com.mapconductor:for-mapbox"
    implementation "com.mapconductor:for-here"
    implementation "com.mapconductor:for-arcgis"
}
```

### Configuración de los SDK de mapas

Cada SDK de mapas requiere una configuración específica y claves de API.

#### Google Maps

```kotlin
// En tu Activity o Fragment
@Composable
fun GoogleMapsExample() {
    val mapViewState = rememberGoogleMapViewState()

    // Sustituye MapView por el proveedor de mapas que prefieras, como GoogleMapView o MapboxMapView
MapView(state = mapViewState) {
        // Contenido del mapa
    }
}
```

#### Mapbox

```kotlin
@Composable
fun MapboxExample() {
    val mapViewState = rememberMapboxMapViewState()

    // Sustituye MapView por el proveedor de mapas que prefieras, como GoogleMapView o MapboxMapView
MapView(state = mapViewState) {
        // Contenido del mapa
    }
}
```

#### HERE Maps

```kotlin
@Composable
fun HereExample() {
    val mapViewState = rememberHereMapViewState()

    // Sustituye MapView por el proveedor de mapas que prefieras, como GoogleMapView o MapboxMapView
MapView(state = mapViewState) {
        // Contenido del mapa
    }
}
```

#### ArcGIS

```kotlin
@Composable
fun ArcGISExample() {
    val mapViewState = rememberArcGISMapViewState()

    // Sustituye MapView por el proveedor de mapas que prefieras, como GoogleMapView o MapboxMapView
MapView(state = mapViewState) {
        // Contenido del mapa
    }
}
```

## Inicialización avanzada

### Configuración personalizada del mapa

```kotlin
@Composable
fun CustomMapConfiguration() {
    val mapViewState = remember {
        GoogleMapViewStateImpl().apply {
            // Establecer la posición inicial de la cámara
            initCameraPosition = MapCameraPositionImpl(
                target = GeoPointImpl.fromLatLong(37.7749, -122.4194),
                zoom = 12f,
                bearing = 45f,
                tilt = 30f
            )

            // Configurar el diseño/estilo del mapa
            mapDesignType = GoogleMapDesignType.SATELLITE
        }
    }

    // Sustituye MapView por el proveedor de mapas que prefieras, como GoogleMapView o MapboxMapView
MapView(
        state = mapViewState,
        onMapViewInitialized = {
            println("Map view initialized")
        },
        onMapLoaded = {
            println("Map loaded and ready")
        }
    ) {
        // Contenido del mapa
    }
}
```

### Manejo de estados de carga y error

```kotlin
@Composable
fun ErrorScreen(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Error al cargar el mapa")
        Button(onClick = onRetry) {
            Text("Reintentar")
        }
    }
}

@Composable
fun LoadingScreen(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Text(message)
        }
    }
}
```

## Buenas prácticas

1. **Usar remember**: envuelve siempre la creación de `MapViewState` con `remember`.
2. **Manejar estados**: gestiona correctamente todos los estados de inicialización.
3. **Recuperación de errores**: implementa lógica de reintento cuando falle la inicialización.
4. **Gestión de recursos**: delega la gestión del ciclo de vida al SDK de mapas.
5. **Claves de API**: asegúrate de configurar correctamente las claves de API de cada SDK.
6. **Rendimiento**: considera la inicialización diferida para mejorar el tiempo de arranque de la app.
7. **Pruebas**: prueba con distintos SDK de mapas para garantizar la compatibilidad.

