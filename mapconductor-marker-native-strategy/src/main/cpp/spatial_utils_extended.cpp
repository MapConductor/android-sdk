#include "spatial_utils_extended.h"
#include <algorithm>

namespace mapconductor {
namespace marker {

GeoRectBounds expandBounds(const GeoRectBounds& bounds, double margin) {
    // Calculate the expansion deltas
    double latDelta = (bounds.northeast.latitude - bounds.southwest.latitude) * margin;
    double lngDelta = (bounds.northeast.longitude - bounds.southwest.longitude) * margin;
    
    // Expand the bounds
    GeoPoint expandedNortheast(
        bounds.northeast.latitude + latDelta,
        bounds.northeast.longitude + lngDelta
    );
    
    GeoPoint expandedSouthwest(
        bounds.southwest.latitude - latDelta,
        bounds.southwest.longitude - lngDelta
    );
    
    // Clamp latitude values to valid range [-90, 90]
    expandedNortheast.latitude = std::min(90.0, expandedNortheast.latitude);
    expandedSouthwest.latitude = std::max(-90.0, expandedSouthwest.latitude);
    
    // Handle longitude wrapping around [-180, 180]
    if (expandedNortheast.longitude > 180.0) {
        expandedNortheast.longitude = expandedNortheast.longitude - 360.0;
    }
    if (expandedSouthwest.longitude < -180.0) {
        expandedSouthwest.longitude = expandedSouthwest.longitude + 360.0;
    }
    
    return GeoRectBounds(expandedNortheast, expandedSouthwest);
}

} // namespace marker
} // namespace mapconductor