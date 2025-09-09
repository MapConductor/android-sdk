import com.mapconductor.core.circle.CircleCapable
import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.marker.MarkerCapable
import com.mapconductor.core.polygon.PolygonCapable
import com.mapconductor.core.polyline.PolylineCapable
import com.mapconductor.mapbox.MapboxDesignType
import com.mapconductor.mapbox.MapboxMapDesignTypeChangeHandler

interface MapboxMapViewController :
    MapViewController,
    MarkerCapable,
    PolylineCapable,
    PolygonCapable,
    CircleCapable {
    fun setMapDesignType(value: MapboxDesignType)

    fun setMapDesignTypeChangeListener(listener: MapboxMapDesignTypeChangeHandler)
}
