---
title: "Marker Animation"
---

`MarkerAnimation` proporciona transiciones suaves y efectos visuales para los marcadores del mapa. Las animaciones pueden aplicarse a la apariencia del marcador, a cambios de posición y a eventos de ciclo de vida.

## Interfaz MarkerAnimation

```kotlin
interface MarkerAnimation {
    val duration: Long
    val interpolator: TimeInterpolator?

    // Métodos del ciclo de vida de la animación
    fun onStart()
    fun onUpdate(progress: Float)
    fun onEnd()
}
```

## Tipos de animación

### Animaciones de posición

Animan suavemente los cambios de posición de un marcador al actualizar sus coordenadas.

```kotlin
class PositionAnimation(
    val fromPosition: GeoPoint,
    val toPosition: GeoPoint,
    override val duration: Long = 1000,
    override val interpolator: TimeInterpolator? = AccelerateDecelerateInterpolator()
) : MarkerAnimation
```

#### Ejemplo de uso

```kotlin
@Composable
fun AnimatedMarkerExample() {
    var markerState by remember {
        mutableStateOf(
            MarkerState(
                position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
                icon = DefaultIcon(label = "Animated"),
            )
        )
    }

    val targetPosition = GeoPointImpl.fromLatLong(37.7849, -122.4094)

    val animation = PositionAnimation(
        fromPosition = markerState.position,
        toPosition = targetPosition,
        duration = 1000L
    )

    LaunchedEffect(Unit) {
        markerState.setAnimation(animation)
    }

    MapView(state = mapViewState) {
        Marker(markerState)
    }
}
```

### Animaciones de aparición/desaparición

Dependiendo de la implementación concreta, también puedes animar la opacidad o escala del marcador (por ejemplo, “fade in/out”, “pop-in”, etc.).

## Uso con MarkerState

```kotlin
val state = MarkerState(
    position = GeoPointImpl.fromLatLong(37.7749, -122.4194),
    icon = DefaultIcon(label = "Animated"),
)

val animation = MarkerAnimation.FadeIn(durationMillis = 500)

state.setAnimation(animation)
```

Consulta las clases concretas de `MarkerAnimation` para ver las opciones disponibles y combinarlas según tus necesidades de UX.
