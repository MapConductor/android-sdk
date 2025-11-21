---
title: "MarkerState"
---

`MarkerState` gestiona la posición, apariencia y configuración de interacción de un marcador. Ofrece un modelo de estado reactivo adecuado para Compose y mecanismos eficientes de detección de cambios.

## Constructor

```kotlin
MarkerState(
    position: GeoPoint,
    id: String? = null,
    extra: Serializable? = null,
    icon: MarkerIcon? = null,
    animation: MarkerAnimation? = null,
    clickable: Boolean = true,
    draggable: Boolean = false
)
```

## Propiedades

### Propiedades básicas

- **`id: String`**: Identificador único del marcador (se genera automáticamente si no se proporciona).
- **`position: GeoPoint`**: Posición actual del marcador.
- **`extra: Serializable?`**: Datos adicionales asociados al marcador.

### Propiedades visuales

- **`icon: MarkerIcon?`**: Icono del marcador (usa el icono por defecto si es `null`).
- **`clickable: Boolean`**: Indica si el marcador responde a eventos de clic.
- **`draggable: Boolean`**: Indica si el usuario puede arrastrar el marcador.

### Propiedades de interacción

- **`isDragging: Boolean`** (solo lectura): Indica si el marcador está siendo arrastrado.
- **`internalPosition: GeoPoint`** (solo lectura): Posición interna usada para el renderizado (tiene en cuenta estados intermedios como el arrastre).

## Métodos principales

### Animación

```kotlin
fun setAnimation(animation: MarkerAnimation?)
```
Establece o limpia la animación del marcador.

```kotlin
internal fun getAnimation(): MarkerAnimation?
```
Obtiene la animación actual (uso interno).

### Copia de estado

```kotlin
fun copy(
    id: String? = this.id,
    position: GeoPoint = this.position,
    extra: Serializable? = this.extra,
    icon: MarkerIcon? = this.icon,
    clickable: Boolean? = this.clickable,
    draggable: Boolean? = this.draggable
): MarkerState
```

Crea un nuevo `MarkerState` basado en el actual, modificando solo las propiedades necesarias.

### Actualizaciones reactivas

```kotlin
fun asFlow(): Flow<MarkerFingerPrint>
```
Devuelve un flujo que emite valores cuando el estado del marcador cambia.

```kotlin
fun fingerPrint(): MarkerFingerPrint
```
Genera una huella (fingerprint) para la detección eficiente de cambios.

## Ejemplos de uso

### MarkerState básico

```kotlin
val markerState = MarkerState(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    extra = "San Francisco marker"
)

MapView(state = mapViewState) {
    Marker(markerState)
}
```

### MarkerState personalizado

```kotlin
val customMarkerState = MarkerState(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    icon = DefaultIcon(
        fillColor = Color.Blue,
        label = "SF",
        scale = 1.2f
    ),
    clickable = true,
    draggable = true,
    extra = "Draggable San Francisco marker"
)

MapView(
    state = mapViewState,
    onMarkerClick = { markerState ->
        println("Clicked marker: ${markerState.extra}")
    },
    onMarkerDrag = { markerState ->
        println("Marker dragged to: ${markerState.position}")
    }
) {
    Marker(customMarkerState)
}
```

### Actualizar MarkerState de forma dinámica

```kotlin
@Composable
fun DynamicMarkerExample() {
    var markerState by remember {
        mutableStateOf(
            MarkerState(
                position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
                icon = DefaultIcon(fillColor = Color.Red, label = "1"),
                extra = "Counter: 1"
            )
        )
    }

    var counter by remember { mutableStateOf(1) }

    Column {
        Button(
            onClick = {
                counter++
                markerState = markerState.copy(
                    icon = DefaultIcon(
                        fillColor = Color.Blue,
                        label = counter.toString()
                    ),
                    extra = "Counter: $counter"
                )
            }
        ) {
            Text("Update Marker ($counter)")
        }

        MapView(state = mapViewState) {
            Marker(markerState)
        }
    }
}
```

### MarkerState con animación de posición

```kotlin
@Composable
fun AnimatedMarkerExample() {
    val startPosition = GeoPointImpl.fromLatLong(37.7749, -122.4194)
    val endPosition = GeoPointImpl.fromLatLong(37.7849, -122.4094)

    var markerState by remember {
        mutableStateOf(
            MarkerState(
                position = startPosition,
                icon = DefaultIcon(fillColor = Color.Green, label = "Moving"),
                extra = "Animated marker"
            )
        )
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(2000)
            markerState = markerState.copy(
                position = if (markerState.position == startPosition) endPosition else startPosition
            )
        }
    }

    MapView(state = mapViewState) {
        Marker(markerState)
    }
}
```

