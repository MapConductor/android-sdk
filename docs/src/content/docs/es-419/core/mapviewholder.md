---
title: "MapViewHolder"
---

`MapViewHolder` proporciona acceso a las instancias nativas de los SDK de mapas para casos de uso avanzados en los que la API unificada de MapConductor no cubre alguna funcionalidad específica del proveedor. Aunque MapConductor ofrece una interfaz común, no envuelve absolutamente todas las funciones nativas; `MapViewHolder` sirve como puente para esas necesidades.

## Visión general

MapConductor intenta ofrecer una API unificada entre proveedores, pero no siempre es posible conseguir paridad total de funcionalidades. `MapViewHolder` permite acceder a las instancias nativas subyacentes cuando necesitas características específicas de cada SDK.

```kotlin
interface MapViewHolder<ActualMapViewType, ActualMapType> {
    val mapView: ActualMapViewType
    val map: ActualMapType
}
```

## Acceso a MapViewHolder

Cada implementación de `MapViewState` aporta acceso a su `MapViewHolder` específico:

```kotlin
// Google Maps
val googleMapState = GoogleMapViewStateImpl()
val googleHolder: GoogleMapViewHolder? = googleMapState.getMapViewHolder()

// Mapbox
val mapboxState = MapboxViewStateImpl()
val mapboxHolder: MapboxMapViewHolder? = mapboxState.getMapViewHolder()

// HERE Maps
val hereState = HereViewStateImpl()
val hereHolder: HereViewHolder? = hereState.getMapViewHolder()

// ArcGIS
val arcgisState = ArcGISMapViewStateImpl()
val arcgisHolder: ArcGISMapViewHolder? = arcgisState.getMapViewHolder()
```

## Implementaciones específicas por proveedor

### Google Maps

```kotlin
typealias GoogleMapViewHolder = MapViewHolder<MapView, GoogleMap>

// Acceso a las APIs nativas de Google Maps
googleHolder?.let { holder ->
    val nativeMapView: MapView = holder.mapView
    val googleMap: GoogleMap = holder.map

    // Usar funciones específicas de Google Maps
    googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.style_json))
    googleMap.setOnPoiClickListener { poi ->
        // Manejar clics en puntos de interés (POI)
    }

    nativeMapView.onResume()
    nativeMapView.onPause()
}
```

### Mapbox, HERE, ArcGIS, MapLibre

Cada integración define su propio tipo de `MapViewHolder`, por ejemplo `MapboxMapViewHolder`, `HereViewHolder`, etc., que exponen vistas y objetos de mapa nativos equivalentes.

## Cuándo usar MapViewHolder

En la mayoría de los casos no necesitarás trabajar directamente con `MapViewHolder`; MapConductor ya ofrece la mayoría de las funciones comunes a través de la API unificada. Sin embargo, es útil cuando:

- Necesitas acceder a una API nativa que todavía no está envuelta por MapConductor.
- Quieres integrar extensiones o plugins propios del proveedor.
- Estás depurando problemas específicos del SDK de mapas subyacente.
