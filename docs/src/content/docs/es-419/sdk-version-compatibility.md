---
title: Compatibilidad de versiones del SDK
---

# Compatibilidad de versiones del SDK

En esta página se describe la compatibilidad entre MapConductor, los distintos SDK de mapas y el entorno Android/Kotlin/Compose.

## Resumen de versiones compatibles

MapConductor v{BOM_MODULE_VERSION} está diseñado para el siguiente entorno:

- **Android**: minSdk {ANDROID_MIN_SDK_VERSION}, targetSdk {ANDROID_TARGET_SDK_VERSION}.
- **Kotlin**: {KOTLIN_VERSION}.
- **Jetpack Compose**: {JETPACK_COMPOSE_VERSION}.
- **Java**: {JAVA_VERSION}.

Los módulos se prueban con estas versiones como base. Si usas versiones diferentes, verifica cuidadosamente la compatibilidad.

## Ejemplo de configuración de Gradle

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
