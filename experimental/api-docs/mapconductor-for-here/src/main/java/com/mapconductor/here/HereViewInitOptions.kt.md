# HereViewInitOptions

The `HereViewInitOptions` data class is used to specify the initial configuration options for a HERE map view.

## Signature

```kotlin
data class HereViewInitOptions(
    val scheme: MapScheme = MapScheme.NORMAL_DAY
)
```

## Description

This data class holds various settings for initializing a map view component. An instance of this class can be passed during the map view's creation to customize its initial appearance and behavior.

## Parameters

| Parameter | Type        | Description                                                                                                                                                           |
| :-------- | :---------- | :-------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `scheme`  | `MapScheme` | The visual map scheme to apply upon initialization. This determines the map's visual style, such as day, night, or satellite. Defaults to `MapScheme.NORMAL_DAY`. |

## Example

The following examples demonstrate how to create an instance of `HereViewInitOptions`.

### Example 1: Using Default Options

To initialize the map view with the default settings, you can create an instance without providing any arguments.

```kotlin
import com.mapconductor.here.HereViewInitOptions

// Creates options with the default map scheme (NORMAL_DAY)
val defaultOptions = HereViewInitOptions()
```

### Example 2: Specifying a Custom Map Scheme

To initialize the map with a different visual style, such as the night mode scheme, specify the `scheme` parameter during instantiation.

```kotlin
import com.mapconductor.here.HereViewInitOptions
import com.here.sdk.mapview.MapScheme

// Creates options to set the initial map scheme to night mode
val nightModeOptions = HereViewInitOptions(scheme = MapScheme.NORMAL_NIGHT)
```