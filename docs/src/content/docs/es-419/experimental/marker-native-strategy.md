---
title: "Marker Native Strategy (Experimental)"
---

El módulo `mapconductor-marker-native-strategy` proporciona una gestión de marcadores de alto rendimiento usando indexación espacial nativa en C++. Este módulo experimental mejora drásticamente el rendimiento en aplicaciones con un número muy grande de marcadores (10.000+).

> **⚠️ Módulo altamente experimental**: requiere soporte de librerías nativas. Úsalo en producción con extrema cautela.

## Descripción general

La estrategia nativa de marcadores sustituye la indexación espacial en Java por implementaciones optimizadas en C++, ofreciendo:

- **Reducción de memoria (~90 %)**: comparado con la gestión estándar de marcadores.
- **Consultas espaciales nativas**: indexación espacial en C++ para máximo rendimiento.
- **Culling eficiente por viewport**: solo se renderizan marcadores dentro del área visible.
- **Procesamiento en paralelo**: operaciones de marcadores multi‑hilo.
- **Mínima sobrecarga en Java**: la lógica nativa actúa como fuente de verdad.

## Características de rendimiento

### Uso de memoria

- **MarkerManager estándar**: ~1 MB por cada 1.000 marcadores.  
- **NativeMarkerManager**: ~100 KB por cada 1.000 marcadores.  
- **Almacenamiento optimizado**: evita duplicar entidades.

### Rendimiento de consultas

- **Consulta espacial estándar**: O(log n) con sobrecarga de Java.  
- **Consulta espacial nativa**: O(log n) con optimización en C++.  
- **Conjuntos grandes**: mejora de rendimiento de 10x–100x para 100.000+ marcadores.

## Instalación

Añade el módulo de estrategia nativa a tu `build.gradle`:

```kotlin
dependencies {
    implementation "com.mapconductor:marker-native-strategy"

    // Requerido: módulo BOM
    implementation "com.mapconductor:mapconductor-bom:$version"
    // Requerido: módulo core
    implementation "com.mapconductor:core"

    // Elige tu proveedor de mapas
    implementation "com.mapconductor:for-googlemaps"
}
```

### Configuración de librerías nativas

El módulo requiere librerías nativas C++. Asegúrate de que tu app soporta los ABIs necesarios:

```kotlin
android {
    defaultConfig {
        ndk {
            abiFilters 'arm64-v8a', 'armeabi-v7a', 'x86', 'x86_64'
        }
    }
}
```

## Cuándo usar marker-native-strategy

Utiliza `mapconductor-marker-native-strategy` cuando:

- Renderizas decenas de miles de marcadores de forma simultánea.  
- El rendimiento de CPU/memoria con estrategias en Kotlin/Java no es suficiente.  
- Necesitas consultas espaciales muy frecuentes (p. ej., actualizaciones en tiempo real).

Para conjuntos de datos más pequeños, suele ser suficiente con `mapconductor-marker-strategy` u otras aproximaciones de alto nivel.
