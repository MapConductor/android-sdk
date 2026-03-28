# MarkerEventControllerInterface<ActualMarker>

The `MarkerEventControllerInterface` provides a generic contract for managing and handling events associated with a map marker.

## Signature

```java
interface MarkerEventControllerInterface<ActualMarker>
```

## Description

This interface defines a standardized way to control marker-related events, abstracting the specific implementation details of the underlying map provider. By using a generic type `ActualMarker`, it allows for a consistent event handling architecture regardless of whether the map is powered by Google Maps, Mapbox, or another provider.

Implementations of this interface are responsible for attaching and detaching event listeners to the native marker object.

## Type Parameters

| Parameter      | Description                                                                                                                            |
| :------------- | :------------------------------------------------------------------------------------------------------------------------------------- |
| `ActualMarker` | The concrete class of the marker object from the underlying map provider's SDK (e.g., `com.google.android.gms.maps.model.Marker`). |

## Example

While the interface itself cannot be instantiated, here is a conceptual example of how it might be implemented for a specific map provider like Google Maps.

```java
import com.google.android.gms.maps.model.Marker;
import com.mapconductor.core.marker.MarkerEventControllerInterface;

// A concrete implementation for Google Maps markers.
public class GoogleMapMarkerEventController implements MarkerEventControllerInterface<Marker> {

    private final Marker googleMapMarker;

    public GoogleMapMarkerEventController(Marker googleMapMarker) {
        this.googleMapMarker = googleMapMarker;
    }

    // In a real-world scenario, this class would contain methods
    // to add and remove listeners for clicks, drags, etc.,
    // interacting with the 'googleMapMarker' instance.
    
    public void setOnClickListener(OnMarkerClickListener listener) {
        // Implementation to set a click listener on the Google Map Marker.
    }
    
    public void setOnDragListener(OnMarkerDragListener listener) {
        // Implementation to set a drag listener.
    }
}
```