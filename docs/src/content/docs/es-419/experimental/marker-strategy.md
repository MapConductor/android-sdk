---
title: "Marker Strategy (Experimental)"
---

El módulo `mapconductor-marker-strategy` proporciona estrategias avanzadas de renderizado de marcadores para optimizar el rendimiento y la experiencia de usuario con grandes volúmenes de datos. Este módulo experimental ofrece distintos enfoques de renderizado adaptados a diferentes casos de uso y requisitos de rendimiento.

> **⚠️ Módulo experimental**: este módulo es experimental y sus APIs pueden cambiar. Úsalo en producción con cautela.

## Descripción general

El módulo de estrategias de marcadores ofrece capacidades más sofisticadas que el renderizado básico de marcadores:
- **Renderizado basado en viewport**: solo se renderizan los marcadores visibles en la región actual del mapa.
- **Alta/baja dinámica**: añade y elimina marcadores de forma eficiente al mover la cámara.
- **Optimización espacial**: indexación espacial avanzada para grandes conjuntos de datos.
- **Integración con datos remotos**: soporte para datos de marcadores provenientes del servidor.
- **Clustering**: agrupa marcadores cercanos para mejorar rendimiento y claridad visual.

## Instalación

Añade el módulo de estrategia de marcadores a tu `build.gradle`:

```kotlin
dependencies {
    implementation "com.mapconductor:marker-strategy"

    // Requerido: módulo BOM
    implementation "com.mapconductor:mapconductor-bom:$version"
    // Requerido: módulo core
    implementation "com.mapconductor:core"

    // Elige tu proveedor de mapas
    implementation "com.mapconductor:for-googlemaps"
}
```

## Estrategias principales

### DefaultMarkerStrategy

Estrategia óptima para proveedores como Google Maps y ArcGIS, que gestionan bien operaciones de alta/baja de marcadores:

```kotlin
import com.mapconductor.marker.strategy.DefaultMarkerStrategy

val defaultStrategy = DefaultMarkerStrategy<GoogleMapActualMarker>(
    expandMargin = 0.2,  // Expande el viewport un 20 %
    semaphore = Semaphore(1),
    geocell = HexGeocellImpl.defaultGeocell()
)
```

#### Características clave

- **Alta/baja dinámica**: añade marcadores que entran en el viewport y elimina los que salen.
- **Expansión de viewport**: precarga marcadores ligeramente fuera del área visible.
- **Uso eficiente de memoria**: mantiene en memoria principalmente los marcadores visibles.
- **Desplazamiento fluido**: reduce el efecto de “pop-in/pop-out” al mover el mapa.

### SimpleMarkerStrategy

Estrategia ligera para conjuntos de datos más pequeños o proveedores con características de rendimiento distintas. Proporciona una lógica más simple de actualización de marcadores con menos sobrecarga.

## Cuándo usar marker-strategy

Usa `mapconductor-marker-strategy` cuando:

- Muestras cientos o miles de marcadores y el rendimiento empieza a degradarse.
- Necesitas controlar explícitamente qué se carga en función del viewport.
- Quieres integrar datos de servidor paginados o por tiles.

Para escenarios más simples (pocos marcadores), puede bastar con el renderizado directo sin estrategias avanzadas.
