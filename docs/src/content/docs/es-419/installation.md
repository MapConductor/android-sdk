---
title: Instalación
---

# Instalación y versiones

En esta página se explica cómo añadir MapConductor Android SDK a un proyecto Gradle y qué configuración de versiones se recomienda.

## Añadir dependencias

MapConductor se publica en Maven Central como `mapconductor-bom` junto con varios módulos. Usar el BOM te permite gestionar de forma centralizada la versión de todos los módulos de MapConductor.

```kotlin
val mapconductorVersion = "1.1.0"

dependencies {
    // Usar el BOM para unificar versiones
    implementation(platform("com.mapconductor:mapconductor-bom:$mapconductorVersion"))

    // Módulo principal
    implementation("com.mapconductor:core")

    // Añadir los módulos de proveedor de mapas que necesites
    implementation("com.mapconductor:for-googlemaps")
    // implementation("com.mapconductor:for-mapbox")
    // implementation("com.mapconductor:for-here")
    // implementation("com.mapconductor:for-arcgis")
    // implementation("com.mapconductor:for-maplibre")
}
```

### Runtime principal

#### `mapconductor-core`

Módulo principal que contiene clases base y funcionalidad compartida.

```kotlin
implementation("com.mapconductor:core")
```

**Requerido para**: Cualquier uso de MapConductor.  
**Depende de**: Jetpack Compose, Kotlin Coroutines, etc.

### Módulos de proveedor de mapas

En función de tus necesidades, añade uno o varios de los siguientes módulos:

#### `mapconductor-for-googlemaps`

Integración con Google Maps.

```kotlin
implementation("com.mapconductor:for-googlemaps")
```

Proporciona `GoogleMapsView` y `GoogleMapViewStateImpl`. Requiere configurar el SDK de Google Maps.

#### `mapconductor-for-mapbox`

Integración con Mapbox.

```kotlin
implementation("com.mapconductor:for-mapbox")
```

Proporciona `MapboxMapView` y `MapboxViewStateImpl`. Requiere configurar el SDK de Mapbox.

#### `mapconductor-for-here`

Integración con HERE Maps.

```kotlin
implementation("com.mapconductor:for-here")
```

Proporciona `HereMapView` y `HereViewStateImpl`. Requiere configurar el SDK de HERE.

#### `mapconductor-for-arcgis`

Integración con ArcGIS.

```kotlin
implementation("com.mapconductor:for-arcgis")
```

Proporciona `ArcGISMapView` y `ArcGISMapViewStateImpl`. Requiere configurar el SDK de ArcGIS.

#### `mapconductor-for-maplibre`

Integración con MapLibre.

```kotlin
implementation("com.mapconductor:for-maplibre")
```

Proporciona `MapLibreMapView` y `MapLibreViewStateImpl`. Requiere configurar MapLibre (mosaicos, estilos, etc.).

### Módulos experimentales

> **Experimental**: Estos módulos son experimentales y su API puede cambiar en futuras versiones.

#### `mapconductor-icons`

Ofrece iconos de marcadores personalizados con estilo configurable desde el código.

```kotlin
implementation("com.mapconductor:icons")
```

Incluye componentes como `CircleIcon`, `FlagIcon` y distintos iconos de info bubble.

#### `mapconductor-marker-strategy`

Proporciona estrategias avanzadas de renderizado de marcadores para optimizar el rendimiento (por ejemplo, clustering o estrategias del lado del servidor).

```kotlin
implementation("com.mapconductor:marker-strategy")
```

#### `mapconductor-marker-native-strategy`

Incluye estrategias aceleradas de forma nativa para representar un gran número de marcadores.

```kotlin
implementation("com.mapconductor:marker-native-strategy")
```

## Configuración de Gradle

### `build.gradle` / `build.gradle.kts` a nivel de proyecto

Se recomienda configurar las versiones de Kotlin y Compose siguiendo el ejemplo de la app de ejemplo.

```kotlin
buildscript {
    ext {
        compose_version = "1.7.1"
        kotlin_version = "1.9.25"
    }
}
```

### `build.gradle` / `build.gradle.kts` a nivel de módulo

```kotlin
android {
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        targetSdk = 35
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = compose_version
    }
}
```

### Configuración de ProGuard / R8

Para builds de release, añade las siguientes reglas:

```proguard
# MapConductor Core
-keep class com.mapconductor.core.** { *; }

# Map Provider Specific
-keep class com.mapconductor.googlemaps.** { *; }
-keep class com.mapconductor.mapbox.** { *; }
-keep class com.mapconductor.here.** { *; }
-keep class com.mapconductor.arcgis.** { *; }
-keep class com.mapconductor.maplibre.** { *; }

# Native Strategy (if using)
-keep class com.mapconductor.marker.nativestrategy.** { *; }
```

## Actualización de versiones

### Comprobar la última versión

Puedes comprobar la última versión de MapConductor en:

1. GitHub Releases: página de lanzamientos de `android-sdk`.
2. Maven Central: buscando `com.mapconductor`.
3. Herramientas de Gradle: plugins de actualización de dependencias, etc.

### Actualizar con el BOM

Para actualizar a una nueva versión de MapConductor usando el BOM:

```kotlin
val mapconductorVersion = "1.1.0"

dependencies {
    implementation(platform("com.mapconductor:mapconductor-bom:$mapconductorVersion"))
    implementation("com.mapconductor:core")
    implementation("com.mapconductor:for-googlemaps")
    // ... otros módulos según necesidad
}
```

