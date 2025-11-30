---
title: Tutorial
---

import { Tabs, TabItem } from '@astrojs/starlight/components';

# Tutorial de MapConductor

En este tutorial aprenderás a usar MapConductor Android SDK para mostrar un mapa, añadir marcadores y figuras, y manejar interacciones de usuario.

## Qué aprenderás

- Cómo instalar y configurar el SDK de MapConductor.
- Cómo mostrar un mapa.
- Cómo añadir marcadores, círculos y polilíneas.
- Cómo manejar eventos de tap/clic.
- Cómo controlar la posición de la cámara.
- Cómo cambiar entre distintos SDK de mapas.

## Requisitos previos

- Android Studio instalado.
- Conocimientos básicos de Jetpack Compose.
- Conocimientos básicos de Kotlin.

## Paso 1: Configuración del proyecto

### 1-1. Añadir dependencias

Añade las dependencias de MapConductor en `build.gradle.kts` o `build.gradle` del módulo:

<Tabs>
<TabItem label="Kotlin (build.gradle.kts)">

```kotlin
dependencies {
    val mapconductorVersion = "{BOM_MODULE_VERSION}"

    // Usar BOM para unificar versiones
    implementation(platform("com.mapconductor:mapconductor-bom:$mapconductorVersion"))

    // Módulo core (obligatorio)
    implementation("com.mapconductor:core")

    // Usar Google Maps
    implementation("com.mapconductor:for-googlemaps")

    // O Mapbox
    // implementation("com.mapconductor:for-mapbox")

    // O HERE Maps
    // implementation("com.mapconductor:for-here")

    // O ArcGIS
    // implementation("com.mapconductor:for-arcgis")

    // O MapLibre
    // implementation("com.mapconductor:for-maplibre")
}
```

</TabItem>
<TabItem label="Groovy (build.gradle)">

```groovy
dependencies {
    def mapconductorVersion = "{BOM_MODULE_VERSION}"

    // Usar BOM para unificar versiones
    implementation platform("com.mapconductor:mapconductor-bom:$mapconductorVersion")

    // Módulo core (obligatorio)
    implementation "com.mapconductor:core"

    // Usar Google Maps
    implementation "com.mapconductor:for-googlemaps"

    // O Mapbox
    // implementation "com.mapconductor:for-mapbox"

    // O HERE Maps
    // implementation "com.mapconductor:for-here"

    // O ArcGIS
    // implementation "com.mapconductor:for-arcgis"

    // O MapLibre
    // implementation "com.mapconductor:for-maplibre"
}
```

</TabItem>
</Tabs>

### 1-2. Configuración de Android

Añade la siguiente configuración en `build.gradle.kts` o `build.gradle`:

<Tabs>
<TabItem label="Kotlin (build.gradle.kts)">

```kotlin
android {
    compileSdk = {ANDROID_TARGET_SDK_VERSION}

    defaultConfig {
        minSdk = {ANDROID_MIN_SDK_VERSION}
        targetSdk = {ANDROID_TARGET_SDK_VERSION}
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_{JAVA_VERSION}
        targetCompatibility = JavaVersion.VERSION_{JAVA_VERSION}
    }

    kotlinOptions {
        jvmTarget = "{JAVA_VERSION}"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "{JETPACK_COMPOSE_VERSION}"
    }
}
```

</TabItem>
<TabItem label="Groovy (build.gradle)">

```groovy
android {
    compileSdk {ANDROID_TARGET_SDK_VERSION}

    defaultConfig {
        minSdk {ANDROID_MIN_SDK_VERSION}
        targetSdk {ANDROID_TARGET_SDK_VERSION}
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_{JAVA_VERSION}
        targetCompatibility JavaVersion.VERSION_{JAVA_VERSION}
    }

    kotlinOptions {
        jvmTarget = '{JAVA_VERSION}'
    }

    buildFeatures {
        compose true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "{JETPACK_COMPOSE_VERSION}"
    }
}
```

</TabItem>
</Tabs>

### 1-3. Configuración de los SDK de mapas

> **Importante**: MapConductor es una capa de API unificada sobre SDKs de mapas existentes. Debes configurar cada SDK de mapas de forma independiente antes de usar la integración de MapConductor.

Cada SDK requiere sus propias claves de API, permisos y configuración:

- **[Configuración de Google Maps](/es-419/setup/google-maps)** – claves de API y permisos del SDK de Google Maps.
- **[Configuración de Mapbox](/es-419/setup/mapbox)** – tokens de acceso de Mapbox y estilos.
- **[Configuración de HERE Maps](/es-419/setup/here-maps)** – claves de API y licencias de HERE SDK.
- **[Configuración de ArcGIS](/es-419/setup/arcgis)** – claves de API y licencias de ArcGIS SDK.
- **[Configuración de MapLibre](/es-419/setup/maplibre/)** – configuración de mosaicos y estilos.

## Paso 2: Mostrar un mapa

### 2-1. MapView básico

```kotlin
@Composable
fun BasicMapExample(modifier: Modifier = Modifier) {
    val sanFrancisco = GeoPointImpl.fromLatLong(37.7749, -122.4194)
    val camera = MapCameraPositionImpl(
        position = sanFrancisco,
        zoom = 13.0,
    )

    val mapViewState = rememberGoogleMapViewState(
        cameraPosition = camera,
    )

    GoogleMapView(
        modifier = modifier,
        state = mapViewState,
        onMapClick = { geoPoint ->
            println("Map clicked at: ${geoPoint.latitude}, ${geoPoint.longitude}")
        }
    ) {
        // Contenido del mapa
    }
}
```

## Paso 3: Añadir marcadores y figuras

### 3-1. Añadir un marcador

```kotlin
GoogleMapView(
    state = mapViewState
) {
    Marker(
        position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        icon = DefaultIcon(label = "SF"),
        extra = "San Francisco marker"
    )
}
```

### 3-2. Añadir círculos y polilíneas

```kotlin
GoogleMapView(
    state = mapViewState
) {
    // Círculo
    Circle(
        center = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        radiusMeters = 1000.0,
        strokeColor = Color.Blue,
        fillColor = Color.Blue.copy(alpha = 0.3f)
    )

    // Polilínea
    Polyline(
        points = listOf(
            GeoPointImpl.fromLatLong(37.7749, -122.4194),
            GeoPointImpl.fromLatLong(37.7849, -122.4094),
        ),
        strokeColor = Color.Magenta,
        strokeWidth = 3.dp
    )
}
```

## Paso 4: Manejo de interacciones de usuario

### 4-1. Eventos de clic en el mapa

```kotlin
GoogleMapView(
    state = mapViewState,
    onMapClick = { geoPoint ->
        println("Map clicked at: ${geoPoint.latitude}, ${geoPoint.longitude}")
    }
) {
    // Contenido
}
```

### 4-2. Eventos de clic en marcadores

```kotlin
GoogleMapView(
    state = mapViewState,
    onMarkerClick = { markerState ->
        println("Marker clicked: ${markerState.extra}")
    }
) {
    Marker(
        position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        icon = DefaultIcon(label = "SF"),
        extra = "San Francisco marker"
    )
}
```

## Paso 5: Cambiar de SDK de mapas

Para cambiar de Google Maps a Mapbox, solo cambia los tipos de estado y vista:

```kotlin
// Google Maps
val googleMapState = rememberGoogleMapViewState()
GoogleMapView(state = googleMapState) { /* contenido */ }

// Mapbox
val mapboxState = rememberMapboxMapViewState()
MapboxMapView(state = mapboxState) { /* mismo contenido */ }
```

Todos los overlays (`Marker`, `Circle`, `Polyline`, etc.) pueden reutilizarse entre proveedores siempre que intercambies el `MapViewState` y el composable de vista de mapa.

## Siguientes pasos

En este tutorial has visto los conceptos básicos de MapConductor:

- Cómo instalar y configurar dependencias.
- Cómo mostrar un mapa.
- Cómo añadir marcadores y figuras.
- Cómo manejar interacciones de usuario.
- Cómo cambiar entre distintos SDK de mapas.

Para profundizar más, consulta:

- [Módulos](/es-419/modules/)
- [Compatibilidad de proveedores](/es-419/provider-compatibility/)
- [Compatibilidad de versiones del SDK](/es-419/sdk-version-compatibility/)
