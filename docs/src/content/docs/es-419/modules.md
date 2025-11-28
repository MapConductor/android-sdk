---
title: Módulos
---

# Arquitectura de módulos

MapConductor Android SDK se compone de varios módulos para que puedas usar solo lo que necesitas. En esta página se describen los roles y dependencias de cada módulo.

## BOM y gestión de versiones

Todos los módulos se gestionan a través de `mapconductor-bom`. Usar el BOM evita inconsistencias de versión entre módulos.

```kotlin
val mapconductorVersion = "1.1.1"

dependencies {
    implementation(platform("com.mapconductor:mapconductor-bom:$mapconductorVersion"))
    implementation("com.mapconductor:core")
    implementation("com.mapconductor:for-googlemaps")
    // Añade otros módulos según necesidad
}
```

## Runtime principal

### `mapconductor-core`

Módulo central que proporciona abstracciones compartidas y utilidades:

- Clases básicas de geolocalización y cámara
- Gestión de estado de MapView y eventos
- Interfaces comunes para componentes como marcadores, círculos y polilíneas

Este módulo es obligatorio para la mayoría de los casos de uso.

## Módulos de proveedor de mapas

Cada proveedor de mapas dispone de un módulo que implementa `mapconductor-core`:

- `mapconductor-for-googlemaps`
- `mapconductor-for-mapbox`
- `mapconductor-for-here`
- `mapconductor-for-arcgis`
- `mapconductor-for-maplibre`

Estos módulos proporcionan vistas de mapa y clases de estado específicas por proveedor, y actúan como puente entre el SDK nativo y la API unificada de MapConductor.

## Módulos de soporte

### `mapconductor-icons`

Módulo que proporciona iconos personalizados de marcador, como iconos circulares o de bandera, para representar información en el mapa de forma visualmente clara.

## Módulos experimentales

> **Nota**: Los módulos experimentales están sujetos a cambios. Consulta las notas de la versión si los usas en producción.

### `mapconductor-marker-strategy`

Define estrategias para manejar de forma eficiente un gran número de marcadores, incluyendo clustering y otras optimizaciones de rendimiento.

### `mapconductor-marker-native-strategy`

Ofrece estrategias de renderizado de marcadores aceleradas de forma nativa, pensadas para escenarios con muchos marcadores.

## App de ejemplo

El repositorio incluye una aplicación de ejemplo que demuestra las capacidades principales de MapConductor:

- `example-app`: ejemplo de cómo usar MapConductor con varios proveedores.

Consulta [Comenzar](/es-419/get-started/) y el README del repositorio para saber cómo compilar y ejecutar la app de ejemplo.

