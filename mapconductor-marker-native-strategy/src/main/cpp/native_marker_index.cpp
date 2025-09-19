#include "native_marker_index.h"
#include <algorithm>
#include <cmath>
#include <limits>

constexpr double PI = 3.14159265358979323846;
constexpr double EARTH_CIRCUMFERENCE = 40075017.0;

NativeMarkerIndex::NativeMarkerIndex(int baseHexSideLength, double zoom)
    : geocell(std::make_unique<HexGeocellNative>(baseHexSideLength))
    , zoom(zoom) {
}

NativeMarkerIndex::~NativeMarkerIndex() = default;

void NativeMarkerIndex::removeFromCell(const std::string& markerId, const std::string& cellId) {
    auto cellIt = cellToMarkers.find(cellId);
    if (cellIt != cellToMarkers.end()) {
        cellIt->second.erase(markerId);
        if (cellIt->second.empty()) {
            cellToMarkers.erase(cellIt);
        }
    }
}

void NativeMarkerIndex::addToCell(const std::string& markerId, const std::string& cellId) {
    cellToMarkers[cellId].insert(markerId);
}


void NativeMarkerIndex::registerMarker(const std::string& id, const GeoPoint& position, bool clickable) {
    MarkerPoint marker(id, position, clickable);
    
    // Remove from old cell if exists
    auto oldCellIt = markerToCell.find(id);
    if (oldCellIt != markerToCell.end()) {
        removeFromCell(id, oldCellIt->second);
    }
    
    // Add to new cell
    HexCell cell = geocell->latLngToHexCell(position, zoom);
    addToCell(id, cell.id);
    markerToCell[id] = cell.id;
    markers[id] = marker;
}

void NativeMarkerIndex::updateMarker(const std::string& id, const GeoPoint& position, bool clickable) {
    registerMarker(id, position, clickable);
}

bool NativeMarkerIndex::removeMarker(const std::string& id) {
    auto markerIt = markers.find(id);
    if (markerIt == markers.end()) {
        return false;
    }
    
    auto cellIt = markerToCell.find(id);
    if (cellIt != markerToCell.end()) {
        removeFromCell(id, cellIt->second);
        markerToCell.erase(cellIt);
    }
    
    markers.erase(markerIt);
    return true;
}

bool NativeMarkerIndex::hasMarker(const std::string& id) const {
    return markers.find(id) != markers.end();
}

HexCell NativeMarkerIndex::findNearest(const GeoPoint& position) const {
    HexCoord targetCoord = geocell->latLngToHexCoord(position, zoom);
    
    // Start with radius 0 and expand - increased max radius significantly
    for (int radius = 0; radius <= 100; ++radius) {
        std::vector<HexCoord> coords = hexRange(targetCoord, radius);
        
        for (const auto& coord : coords) {
            std::string cellId = geocell->hexToCellId(coord, zoom);
            auto cellIt = cellToMarkers.find(cellId);
            if (cellIt != cellToMarkers.end() && !cellIt->second.empty()) {
                // Check if any markers in this cell are clickable
                bool hasClickableMarker = false;
                for (const std::string& markerId : cellIt->second) {
                    auto markerIt = markers.find(markerId);
                    if (markerIt != markers.end() && markerIt->second.clickable) {
                        hasClickableMarker = true;
                        break;
                    }
                }
                
                if (hasClickableMarker) {
                    GeoPoint centerLatLng = geocell->hexToLatLngCenter(coord, position.latitude, zoom);
                    Offset centerXY(centerLatLng.longitude, centerLatLng.latitude);
                    return HexCell(coord, centerLatLng, centerXY, cellId);
                }
            }
        }
    }
    
    // Return empty cell if nothing found
    return HexCell();
}

std::string NativeMarkerIndex::findNearestMarker(const GeoPoint& position) const {
    // Try optimized ring-by-ring search first
    std::string result = findNearestOptimized(position);
    if (!result.empty()) {
        return result;
    }
    
    // Fallback to original brute force method if optimized search fails
    return findNearestBruteForce(position);
}

std::string NativeMarkerIndex::findNearestOptimized(const GeoPoint& position) const {
    HexCoord targetCoord = geocell->latLngToHexCoord(position, zoom);
    
    std::string bestMarkerId;
    double bestDistance = std::numeric_limits<double>::max();
    bool foundAnyMarker = false;
    
    // Ring-by-ring search with improved early termination
    for (int radius = 0; radius <= 15; ++radius) { // Reduced max radius
        bool foundMarkersThisRadius = false;
        
        // Generate only the current ring
        std::vector<HexCoord> ringCoords = hexRing(targetCoord, radius);
        
        for (const auto& coord : ringCoords) {
            std::string cellId = geocell->hexToCellId(coord, zoom);
            
            auto cellIt = cellToMarkers.find(cellId);
            if (cellIt != cellToMarkers.end()) {
                for (const std::string& markerId : cellIt->second) {
                    auto markerIt = markers.find(markerId);
                    if (markerIt != markers.end() && markerIt->second.clickable) {
                        foundMarkersThisRadius = true;
                        foundAnyMarker = true;
                        double distance = haversineDistance(position, markerIt->second.position);
                        if (distance < bestDistance) {
                            bestDistance = distance;
                            bestMarkerId = markerId;
                        }
                    }
                }
            }
        }
        
        // Better early termination: if we found markers, search 2 more rings then stop
        if (foundAnyMarker && radius >= 2) {
            break;
        }
    }
    
    return bestMarkerId;
}

std::string NativeMarkerIndex::findNearestBruteForce(const GeoPoint& position) const {
    std::string bestMarkerId;
    double bestDistance = std::numeric_limits<double>::max();
    
    for (const auto& pair : markers) {
        if (pair.second.clickable) {
            double distance = haversineDistance(position, pair.second.position);
            if (distance < bestDistance) {
                bestDistance = distance;
                bestMarkerId = pair.first;
            }
        }
    }
    
    return bestMarkerId;
}

std::vector<HexCellWithDistance> NativeMarkerIndex::findWithinRadiusWithDistance(const GeoPoint& center, double radiusMeters) const {
    std::vector<HexCellWithDistance> result;
    result.reserve(50); // Pre-allocate reasonable capacity
    
    HexCoord centerCoord = geocell->latLngToHexCoord(center, zoom);
    
    // Limit radius to prevent memory explosion
    int hexRadius = std::min(20, static_cast<int>(std::ceil(radiusMeters / 1000.0)) + 1);
    
    // Direct iteration instead of generating all coordinates at once
    for (int dq = -hexRadius; dq <= hexRadius; ++dq) {
        int minR = std::max(-hexRadius, -dq - hexRadius);
        int maxR = std::min(hexRadius, -dq + hexRadius);
        
        for (int dr = minR; dr <= maxR; ++dr) {
            HexCoord coord(centerCoord.q + dq, centerCoord.r + dr, centerCoord.depth);
            std::string cellId = geocell->hexToCellId(coord, zoom);
            
            auto cellIt = cellToMarkers.find(cellId);
            if (cellIt != cellToMarkers.end() && !cellIt->second.empty()) {
                GeoPoint cellCenter = geocell->hexToLatLngCenter(coord, center.latitude, zoom);
                double distance = haversineDistance(center, cellCenter);
                
                if (distance <= radiusMeters) {
                    Offset centerXY(cellCenter.longitude, cellCenter.latitude);
                    HexCell cell(coord, cellCenter, centerXY, cellId);
                    result.emplace_back(cell, distance);
                }
            }
        }
    }
    
    return result;
}

std::vector<std::string> NativeMarkerIndex::findMarkersInBounds(const GeoRectBounds& bounds) const {
    if (bounds.isEmpty()) {
        return {};
    }
    
    std::vector<std::string> result;
    result.reserve(100); // Pre-allocate reasonable capacity
    
    // More precise search radius calculation
    GeoPoint center = bounds.center();
    double latRadiusMeters = (bounds.maxLat - bounds.minLat) * 111000.0 / 2.0;
    double lngRadiusMeters = (bounds.maxLng - bounds.minLng) * 111000.0 * std::cos(center.latitude * PI / 180.0) / 2.0;
    double searchRadiusMeters = std::sqrt(latRadiusMeters * latRadiusMeters + lngRadiusMeters * lngRadiusMeters);
    
    // Limit search radius to prevent excessive hex cell generation
    searchRadiusMeters = std::min(searchRadiusMeters, 50000.0); // Max 50km search
    
    HexCoord centerCoord = geocell->latLngToHexCoord(center, zoom);
    
    // More efficient hex radius calculation  
    int hexRadius = std::max(1, static_cast<int>(std::ceil(searchRadiusMeters / 1000.0)));
    hexRadius = std::min(hexRadius, 20); // Limit to 20 hex cells radius to prevent memory explosion
    
    // Direct iteration instead of generating all cells at once
    for (int dq = -hexRadius; dq <= hexRadius; ++dq) {
        int minR = std::max(-hexRadius, -dq - hexRadius);
        int maxR = std::min(hexRadius, -dq + hexRadius);
        
        for (int dr = minR; dr <= maxR; ++dr) {
            HexCoord coord(centerCoord.q + dq, centerCoord.r + dr, centerCoord.depth);
            std::string cellId = geocell->hexToCellId(coord, zoom);
            
            auto cellIt = cellToMarkers.find(cellId);
            if (cellIt != cellToMarkers.end()) {
                for (const std::string& markerId : cellIt->second) {
                    auto markerIt = markers.find(markerId);
                    if (markerIt != markers.end() && bounds.contains(markerIt->second.position)) {
                        result.push_back(markerId);
                    }
                }
            }
        }
    }
    
    return result;
}

std::vector<std::string> NativeMarkerIndex::findByIdPrefix(const std::string& prefix) const {
    std::vector<std::string> result;
    
    for (const auto& pair : cellToMarkers) {
        if (pair.first.find(prefix) == 0) {
            // This cell ID starts with the prefix
            for (const std::string& markerId : pair.second) {
                result.push_back(markerId);
            }
        }
    }
    
    return result;
}

void NativeMarkerIndex::clear() {
    markers.clear();
    cellToMarkers.clear();
    markerToCell.clear();
}

size_t NativeMarkerIndex::markerCount() const {
    return markers.size();
}

double NativeMarkerIndex::metersPerPixel(const GeoPoint& position, double zoom, double pixels, int tileSize) const {
    double lat = position.latitude * PI / 180.0;
    double metersPerTile = EARTH_CIRCUMFERENCE * std::cos(lat) / std::pow(2.0, zoom);
    return metersPerTile / (tileSize * pixels);
}