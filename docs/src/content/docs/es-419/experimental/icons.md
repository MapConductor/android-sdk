---
title: "Icons (Experimental)"
---

El módulo `mapconductor-icons` proporciona iconos de marcador dibujados de forma personalizada con estilos configurables desde código. Este módulo experimental ofrece iconos de tipo vectorial que puedes personalizar en colores, tamaños y otras propiedades en tiempo de ejecución.

> **⚠️ Módulo experimental**: Este módulo es experimental y puede cambiar de forma significativa en futuras versiones. Úsalo en producción con cautela.

## Descripción general

El módulo de iconos crea marcadores de alta calidad usando operaciones de dibujo sobre `Canvas`, ofreciendo:
- **Gráficos vectoriales escalables**: los iconos escalan suavemente a cualquier tamaño.
- **Personalización en tiempo de ejecución**: cambia colores, tamaños y propiedades dinámicamente.
- **Caché optimizado**: caching automático en bitmaps para mejorar el rendimiento.
- **Apariencia consistente**: mismo estilo visual en todos los proveedores de mapas.

## Instalación

Añade el módulo de iconos a tu `build.gradle`:

```kotlin
dependencies {
    implementation "com.mapconductor:mapconductor-icons"

    // Requerido: módulo BOM
    implementation "com.mapconductor:mapconductor-bom:$version"
    // Requerido: módulo core
    implementation "com.mapconductor:core"

    // Elige tu proveedor de mapas
    implementation "com.mapconductor:for-googlemaps"
}
```

## Iconos disponibles

### CircleIcon

Icono de marcador circular con relleno y borde personalizables:

```kotlin
import com.mapconductor.icons.CircleIcon

// Icono circular básico
val basicCircle = CircleIcon()

// Icono circular personalizado
val customCircle = CircleIcon(
    fillColor = Color.Blue,
    strokeColor = Color.White,
    strokeWidth = 2.dp,
    scale = 1.2f,
    iconSize = 32.dp
)
```

#### Propiedades de CircleIcon

- **`fillColor: Color`**: color interior del círculo (por defecto `Color.Red`).
- **`strokeColor: Color`**: color del borde (por defecto `Color.White`).
- **`strokeWidth: Dp`**: grosor del borde (por defecto, según configuración).
- **`scale: Float`**: factor de tamaño (por defecto `1.0f`).
- **`iconSize: Dp`**: tamaño base del icono (por defecto, según configuración).
- **`debug: Boolean`**: muestra contornos de depuración si es `true`.

### FlagIcon

Icono de marcador en forma de bandera con mástil y bandera personalizables:

```kotlin
import com.mapconductor.icons.FlagIcon

// Icono de bandera básico
val basicFlag = FlagIcon()

// Icono de bandera personalizado
val customFlag = FlagIcon(
    fillColor = Color.Green,
    strokeColor = Color.Black,
    strokeWidth = 1.5.dp,
    label = "A",
    labelTextColor = Color.White
)
```

### InfoBubble Icons

El módulo también incluye iconos inspirados en “info bubbles” para destacar puntos de interés. Estos iconos pueden combinarse con los componentes de `InfoBubble` para crear marcadores ricos visualmente.

Consulta el código fuente del módulo y los ejemplos del repositorio para ver todas las variantes disponibles.
