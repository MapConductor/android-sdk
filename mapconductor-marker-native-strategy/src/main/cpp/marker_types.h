#pragma once

#include <memory>
#include <vector>
#include <string>
#include <functional>

namespace mapconductor {
namespace marker {

/**
 * Geographic coordinates representing a position on Earth.
 */
struct GeoPoint {
    double latitude;
    double longitude;
    
    GeoPoint(double lat, double lng) : latitude(lat), longitude(lng) {}
};

/**
 * Rectangular geographic bounds defined by northeast and southwest corners.
 */
struct GeoRectBounds {
    GeoPoint northeast;
    GeoPoint southwest;
    
    GeoRectBounds(const GeoPoint& ne, const GeoPoint& sw) 
        : northeast(ne), southwest(sw) {}
    
    /**
     * Check if a point is contained within these bounds.
     */
    bool contains(const GeoPoint& point) const {
        return point.latitude >= southwest.latitude && 
               point.latitude <= northeast.latitude &&
               point.longitude >= southwest.longitude && 
               point.longitude <= northeast.longitude;
    }
};

/**
 * Represents the visible region of a map view.
 */
struct VisibleRegion {
    GeoRectBounds bounds;
    
    explicit VisibleRegion(const GeoRectBounds& b) : bounds(b) {}
};

/**
 * Camera position including the visible region.
 */
struct MapCameraPosition {
    std::shared_ptr<VisibleRegion> visibleRegion;
    
    MapCameraPosition(std::shared_ptr<VisibleRegion> region) 
        : visibleRegion(region) {}
};

/**
 * Represents a bitmap icon for markers.
 */
struct BitmapIcon {
    std::vector<uint8_t> data;
    int width;
    int height;
    
    BitmapIcon(const std::vector<uint8_t>& d, int w, int h) 
        : data(d), width(w), height(h) {}
};

/**
 * Base class for marker icons.
 */
class Icon {
public:
    virtual ~Icon() = default;
    virtual BitmapIcon toBitmapIcon() const = 0;
};

/**
 * Default marker icon implementation.
 */
class DefaultIcon : public Icon {
public:
    BitmapIcon toBitmapIcon() const override {
        // Return a simple default icon bitmap
        std::vector<uint8_t> defaultData(64, 0xFF); // Simple white square
        return BitmapIcon(defaultData, 8, 8);
    }
};

/**
 * Color-based default marker icon.
 */
class ColorDefaultIcon : public Icon {
public:
    BitmapIcon toBitmapIcon() const override {
        // Return a colored default icon bitmap
        std::vector<uint8_t> colorData(64, 0x80); // Gray square
        return BitmapIcon(colorData, 8, 8);
    }
};

/**
 * State information for a marker.
 */
struct MarkerState {
    std::string id;
    GeoPoint position;
    std::shared_ptr<Icon> icon;
    bool visible = true;
    
    MarkerState(const std::string& markerId, const GeoPoint& pos, 
                std::shared_ptr<Icon> markerIcon = nullptr)
        : id(markerId), position(pos), icon(markerIcon) {}
    
    /**
     * Generate fingerprint for change detection.
     */
    std::string fingerPrint() const {
        return id + "_" + std::to_string(position.latitude) + "_" + 
               std::to_string(position.longitude);
    }
};

/**
 * Entity representing a marker with its state and rendering information.
 */
template<typename ActualMarker>
struct MarkerEntity {
    MarkerState state;
    std::shared_ptr<ActualMarker> marker;
    bool isRendered = false;
    bool visible = true;
    
    MarkerEntity(const MarkerState& s, std::shared_ptr<ActualMarker> m = nullptr)
        : state(s), marker(m) {}
    
    std::string fingerPrint() const {
        return state.fingerPrint();
    }
};

/**
 * Parameters for adding markers to the renderer.
 */
struct AddParams {
    MarkerState state;
    BitmapIcon bitmapIcon;
    
    AddParams(const MarkerState& s, const BitmapIcon& icon)
        : state(s), bitmapIcon(icon) {}
};

/**
 * Parameters for changing/updating markers in the renderer.
 */
template<typename ActualMarker>
struct ChangeParams {
    std::shared_ptr<MarkerEntity<ActualMarker>> current;
    BitmapIcon bitmapIcon;
    std::shared_ptr<MarkerEntity<ActualMarker>> prev;
    
    ChangeParams(std::shared_ptr<MarkerEntity<ActualMarker>> curr,
                 const BitmapIcon& icon,
                 std::shared_ptr<MarkerEntity<ActualMarker>> previous)
        : current(curr), bitmapIcon(icon), prev(previous) {}
};

} // namespace marker
} // namespace mapconductor