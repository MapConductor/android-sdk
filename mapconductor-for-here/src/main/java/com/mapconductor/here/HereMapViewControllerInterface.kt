import com.mapconductor.core.circle.CircleCapableInterface
import com.mapconductor.core.controller.MapViewControllerInterface
import com.mapconductor.core.groundimage.GroundImageCapableInterface
import com.mapconductor.core.marker.MarkerCapableInterface
import com.mapconductor.core.polygon.PolygonCapableInterface
import com.mapconductor.core.polyline.PolylineCapableInterface
import com.mapconductor.core.raster.RasterLayerCapableInterface
import com.mapconductor.here.HereMapDesignType

typealias HereMapDesignTypeChangeHandler = (HereMapDesignType) -> Unit

interface HereMapViewControllerInterface :
    MapViewControllerInterface,
    MarkerCapableInterface,
    PolygonCapableInterface,
    PolylineCapableInterface,
    CircleCapableInterface,
    GroundImageCapableInterface,
    RasterLayerCapableInterface {
    fun setMapDesignType(value: HereMapDesignType)

    fun setMapDesignTypeChangeListener(listener: HereMapDesignTypeChangeHandler)
}
