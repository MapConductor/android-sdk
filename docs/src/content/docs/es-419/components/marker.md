---
title: "Marker"
---

Los marcadores son anotaciones de punto que se colocan en el mapa en ubicaciones geográficas concretas. Admiten iconos personalizados, interacciones y animaciones.

## Funciones Composable

### Marker básico

```kotlin
@Composable
fun MapViewScope.Marker(
    position: GeoPoint,
    clickable: Boolean = true,
    draggable: Boolean = false,
    icon: MarkerIcon? = null,
    extra: Serializable? = null,
    id: String? = null
)
```

### Marker con estado

```kotlin
@Composable
fun MapViewScope.Marker(state: MarkerState)
```

## Parámetros

- **`position`**: Coordenadas geográficas del marcador (`GeoPoint`).
- **`clickable`**: Indica si el marcador responde a clics (por defecto: `true`).
- **`draggable`**: Indica si el marcador se puede arrastrar (por defecto: `false`).
- **`icon`**: Icono personalizado del marcador (`MarkerIcon?`).
- **`extra`**: Datos adicionales asociados al marcador (`Serializable?`).
- **`id`**: Identificador único del marcador (`String?`).

## Tipos de icono

### DefaultIcon

Icono estándar de marcador con apariencia configurable:

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

### DrawableDefaultIcon

Marcador que utiliza un recurso `Drawable` como fondo:

```kotlin
DrawableDefaultIcon(
    backgroundDrawable: Drawable,
    scale: Float = 1.0f,
    strokeColor: Color? = null,
    strokeWidth: Dp = 1.dp
)
```

### ImageIcon

Marcador que utiliza una imagen `Drawable` personalizada:

```kotlin
ImageIcon(
    drawable: Drawable,
    anchor: Offset = Offset(0.5f, 0.5f),
    debug: Boolean = false
)
```

## Ejemplos de uso

### Marker básico

```kotlin
// Sustituye MapView por el componente del proveedor que utilices, por ejemplo GoogleMapsView o MapboxMapView
MapView(state = mapViewState) {
    Marker(
        position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        extra = "San Francisco",
        id = "san-francisco-marker"
    )
}
```

### Marker con icono personalizado

```kotlin
MapView(state = mapViewState) {
    Marker(
        position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        icon = DefaultIcon(
            scale = 1.5f,
            label = "SF",
            fillColor = Color.Blue,
            strokeColor = Color.White,
            strokeWidth = 2.dp
        ),
        extra = "San Francisco with custom icon",
        id = "custom-sf-marker"
    )
}
```

### Marker arrastrable

```kotlin
@Composable
fun DraggableMarkerExample() {
    var markerPosition by remember {
        mutableStateOf(GeoPointImpl.fromLatLong(37.7749, -122.4194))
    }

    MapView(
        state = mapViewState,
        onMarkerDrag = { markerState ->
            markerPosition = markerState.position
        }
    ) {
        Marker(
            position = markerPosition,
            draggable = true,
            icon = DefaultIcon(
                label = "Drag me",
                fillColor = Color.Green
            )
        )
    }
}
```

### Varios marcadores con distintos iconos

```kotlin
MapView(state = mapViewState) {
    // Icono por defecto con distintas escalas
    Marker(
        position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
        icon = DefaultIcon(scale = 0.7f, label = "Small")
    )

    Marker(
        position = GeoPointImpl.fromLatLong(37.7849, -122.4094),
        icon = DefaultIcon(scale = 1.0f, label = "Normal")
    )

    Marker(
        position = GeoPointImpl.fromLatLong(37.7949, -122.3994),
        icon = DefaultIcon(scale = 1.4f, label = "Large")
    )

    // Marcador con colores personalizados
    Marker(
        position = GeoPointImpl.fromLatLong(37.7649, -122.4294),
        icon = DefaultIcon(
            fillColor = Color.Yellow,
            strokeColor = Color.Black,
            strokeWidth = 2.dp,
            label = "Custom"
        )
    )

    // Marcador con Drawable
    val context = LocalContext.current
    AppCompatResources.getDrawable(context, R.drawable.custom_icon)?.let { drawable ->
        Marker(
            position = GeoPointImpl.fromLatLong(37.7549, -122.4394),
            icon = DrawableDefaultIcon(
                backgroundDrawable = drawable,
                scale = 1.2f
            )
        )
    }
}
```

### Marker con Info Bubble

Puedes combinar `Marker` con `InfoBubble` para mostrar información detallada al hacer clic en el marcador. Consulta [InfoBubble](/es-419/components/infobubble) para ver un ejemplo completo.

