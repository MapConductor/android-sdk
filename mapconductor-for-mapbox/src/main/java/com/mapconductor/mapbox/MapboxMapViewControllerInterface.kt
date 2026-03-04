import com.mapconductor.core.circle.CircleCapableInterface
import com.mapconductor.core.controller.MapViewControllerInterface
import com.mapconductor.core.groundimage.GroundImageCapableInterface
import com.mapconductor.core.marker.MarkerCapableInterface
import com.mapconductor.core.polygon.PolygonCapableInterface
import com.mapconductor.core.polyline.PolylineCapableInterface
import com.mapconductor.core.raster.RasterLayerCapableInterface
import com.mapconductor.mapbox.MapboxDesignType
import com.mapconductor.mapbox.MapboxMapDesignTypeChangeHandler

interface MapboxMapViewControllerInterface :
    MapViewControllerInterface,
    MarkerCapableInterface,
    PolylineCapableInterface,
    PolygonCapableInterface,
    CircleCapableInterface,
    GroundImageCapableInterface,
    RasterLayerCapableInterface {
    fun setMapDesignType(value: MapboxDesignType)

    fun setMapDesignTypeChangeListener(listener: MapboxMapDesignTypeChangeHandler)
}
