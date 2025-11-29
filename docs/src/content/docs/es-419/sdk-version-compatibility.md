---
title: Compatibilidad de versiones del SDK
---

# Compatibilidad de versiones del SDK

En esta página se describe la compatibilidad entre MapConductor, los distintos SDK de mapas y el entorno Android/Kotlin/Compose.

## Resumen de versiones compatibles

MapConductor v1.1.1 está diseñado para el siguiente entorno:

- **Android**: minSdk 26, targetSdk 35.
- **Kotlin**: 1.9.25.
- **Jetpack Compose**: 1.7.1.
- **Java**: 17.

Los módulos se prueban con estas versiones como base. Si usas versiones diferentes, verifica cuidadosamente la compatibilidad.

## Ejemplo de configuración de Gradle

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
}
```

## MapConductor y los SDK de mapas

MapConductor se ejecuta sobre distintos SDK de mapas y está pensado para funcionar con combinaciones como:

- Google Maps Android SDK (versión estable reciente).
- Mapbox Maps SDK for Android.
- HERE SDK for Android.
- ArcGIS Maps SDK for Kotlin / Android.
- MapLibre Native / MapLibre GL para Android.

En el desarrollo de MapConductor se verifican combinaciones de versiones habituales para mantener la compatibilidad tanto en compilación como en tiempo de ejecución. Aun así, revisa siempre los requisitos y notas de versión de cada SDK de mapas.

## Política de actualización de dependencias

- Actualizaciones importantes de seguridad o cambios críticos se incorporan en versiones menores o mayores.
- Los cambios que requieran refactorización de API se documentan en las notas de la versión.

Para obtener la información más reciente, revisa las notas de lanzamiento en GitHub y el historial de versiones en Maven Central.

