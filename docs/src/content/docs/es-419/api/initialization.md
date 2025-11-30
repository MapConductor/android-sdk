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
    implementation "com.mapconductor:for-maplibre"
}
```

### Configuración de los SDK de mapas

Cada SDK de mapas requiere una configuración específica y claves de API.

#### Google Maps

```kotlin
@Composable
fun GoogleMapsExample() {
    val mapViewState = rememberGoogleMapViewState()

    GoogleMapView(state = mapViewState) {
        // Contenido del mapa
    }
}
```

#### Mapbox

```kotlin
@Composable
fun MapboxExample() {
    val mapViewState = rememberMapboxMapViewState()

    MapboxMapView(state = mapViewState) {
        // Contenido del mapa
    }
}
```

#### HERE Maps

```kotlin
@Composable
fun HereExample() {
    val mapViewState = rememberHereMapViewState()

    HereMapView(state = mapViewState) {
        // Contenido del mapa
    }
}
```

#### ArcGIS

```kotlin
@Composable
fun ArcGISExample() {
    val mapViewState = rememberArcGISMapViewState()

    ArcGISMapView(state = mapViewState) {
        // Contenido del mapa
    }
}
```

#### MapLibre

```kotlin
@Composable
fun GoogleMapsExample() {
    val mapViewState = rememberMapLibreViewState()

    MapLibreView(state = mapViewState) {
        // Contenido del mapa
    }
}
```

## Inicialización

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
    GoogleMapView(
        state = mapViewState,
        onMapLoaded = {
            println("Map loaded and ready")
        }
    ) {
        // Contenido del mapa
    }
}
```
