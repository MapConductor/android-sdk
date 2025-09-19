#pragma once

#include "hex_geocell_native.h"
#include "spatial_utils.h"
#include <unordered_map>
#include <unordered_set>
#include <vector>
#include <memory>

class NativeMarkerIndex {
private:
    std::unique_ptr<HexGeocellNative> geocell;
    std::unordered_map<std::string, MarkerPoint> markers;
    std::unordered_map<std::string, std::unordered_set<std::string>> cellToMarkers;
    std::unordered_map<std::string, std::string> markerToCell;
    double zoom;
    
    void removeFromCell(const std::string& markerId, const std::string& cellId);
    void addToCell(const std::string& markerId, const std::string& cellId);
    std::vector<HexCellWithDistance> findWithinRadiusWithDistance(const GeoPoint& center, double radiusMeters) const;
    HexCell findNearest(const GeoPoint& position) const;
    std::string findNearestOptimized(const GeoPoint& position) const;
    std::string findNearestBruteForce(const GeoPoint& position) const;
    
public:
    NativeMarkerIndex(int baseHexSideLength = 1000, double zoom = 20.0);
    ~NativeMarkerIndex();
    
    void registerMarker(const std::string& id, const GeoPoint& position, bool clickable = true);
    void updateMarker(const std::string& id, const GeoPoint& position, bool clickable = true);
    bool removeMarker(const std::string& id);
    bool hasMarker(const std::string& id) const;
    
    std::string findNearestMarker(const GeoPoint& position) const;
    std::vector<std::string> findMarkersInBounds(const GeoRectBounds& bounds) const;
    std::vector<std::string> findByIdPrefix(const std::string& prefix) const;
    
    void clear();
    size_t markerCount() const;
    
    double metersPerPixel(const GeoPoint& position, double zoom, double pixels, int tileSize = 256) const;
};