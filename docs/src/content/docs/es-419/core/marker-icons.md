---
title: "Marker Icons"
---

MapConductor proporciona varios tipos de iconos de marcador para personalizar la apariencia de los marcadores en el mapa. Todos los iconos implementan la interfaz `MarkerIcon` y pueden usarse con cualquier proveedor de mapas.

## Interfaz MarkerIcon

Interfaz base para todos los iconos de marcador:

```kotlin
interface MarkerIcon {
    // Propiedades comunes para todas las implementaciones de iconos
}
```

## Tipos de icono

### DefaultIcon (ColorDefaultIcon)

Icono estándar de marcador coloreado con apariencia y texto personalizables.

> **Nota**: `DefaultIcon` es un alias de `ColorDefaultIcon`.

```kotlin
DefaultIcon(
    scale: Float = 1.0f,
    label: String? = null,
    fillColor: Color = Color.Red,
    strokeColor: Color = Color.Black,
    strokeWidth: Dp = 1.dp,
    labelTextColor: Color = Color.White,
    labelStrokeColor: Color? = null,
    debug: Boolean = false
)
```

#### Parámetros

- **`scale`**: factor de escala del icono (1.0 = tamaño normal, 0.5 = mitad de tamaño, 2.0 = el doble).
- **`label`**: texto mostrado en el marcador (opcional).
- **`fillColor`**: color de fondo del marcador.
- **`strokeColor`**: color del borde del marcador.
- **`strokeWidth`**: grosor del borde.
- **`labelTextColor`**: color del texto de la etiqueta.
- **`labelStrokeColor`**: color de contorno opcional para el texto.
- **`debug`**: muestra información de depuración si está activado.

#### Ejemplos de uso

```kotlin
// Sustituye MapView por el proveedor de mapas que prefieras, como GoogleMapView o MapboxMapView
MapView(state = mapViewState) {
    // Marcador rojo básico
    Marker(
        position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        icon = DefaultIcon()
    )

    // Marcador personalizado con etiqueta
    Marker(
        position = GeoPointImpl.fromLatLong(37.7849, -122.4094),
        icon = DefaultIcon(
            label = "A",
            fillColor = Color.Blue,
            strokeColor = Color.White,
            strokeWidth = 2.dp
        )
    )
}
```

### DrawableDefaultIcon

Icono basado en un `Drawable` con soporte para trazo opcional.

```kotlin
DrawableDefaultIcon(
    backgroundDrawable: Drawable,
    scale: Float = 1.0f,
    strokeColor: Color? = null,
    strokeWidth: Dp = 1.dp
)
```

Se utiliza cuando quieres reutilizar drawables existentes (por ejemplo, recursos de tu app) como fondo de tu marcador.

### ImageIcon

Icono de marcador que renderiza directamente un `Drawable` en la posición del marcador.

```kotlin
ImageIcon(
    drawable: Drawable,
    anchor: Offset = Offset(0.5f, 0.5f),
    debug: Boolean = false
)
```

- **`anchor`**: punto de anclaje relativo dentro de la imagen (0.5, 0.5 = centro).

## Relación con mapconductor-icons

Cuando utilizas el módulo experimental `mapconductor-icons`, puedes combinar `MarkerIcon` con iconos composables como `CircleIcon` o estilos de info bubble para conseguir marcadores más ricos visualmente.

Consulta también [Marker](/es-419/components/marker) para ver cómo se usan estos iconos en el mapa.
