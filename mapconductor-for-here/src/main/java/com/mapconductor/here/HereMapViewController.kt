import com.mapconductor.core.circle.CircleCapable
import com.mapconductor.core.controller.MapViewController
import com.mapconductor.core.marker.MarkerCapable
import com.mapconductor.core.polygon.PolygonCapable
import com.mapconductor.core.polyline.PolylineCapable
import com.mapconductor.here.HereMapDesignType

typealias HereMapDesignTypeChangeHandler = (HereMapDesignType) -> Unit

interface HereMapViewController :
    MapViewController,
    MarkerCapable,
    PolygonCapable,
    PolylineCapable,
    CircleCapable {
    fun setMapDesignType(value: HereMapDesignType)

    fun setMapDesignTypeChangeListener(listener: HereMapDesignTypeChangeHandler)
}
