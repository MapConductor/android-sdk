#include "native_marker_index.h"
#include <algorithm>
#include <cmath>
#include <limits>
#include <thread>

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
    std::unique_lock<std::shared_mutex> lock(indexMutex);
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
    std::unique_lock<std::shared_mutex> lock(indexMutex);
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
    std::shared_lock<std::shared_mutex> lock(indexMutex);
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
    std::shared_lock<std::shared_mutex> lock(indexMutex);
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
    // Build a snapshot to avoid holding iterators into the map across threads
    std::vector<std::pair<std::string, GeoPoint>> items;
    items.reserve(markers.size());
    for (const auto& kv : markers) {
        if (kv.second.clickable) {
            items.emplace_back(kv.first, kv.second.position);
        }
    }

    if (items.empty()) return std::string();

    // Decide parallelism based on data size
    unsigned hw = std::thread::hardware_concurrency();
    if (hw == 0) hw = 2;
    unsigned maxThreads = std::min<unsigned>(hw, 4); // cap
    unsigned threadCount = std::min<unsigned>(maxThreads, static_cast<unsigned>(items.size()));

    // Small inputs: single-thread linear scan
    if (threadCount <= 1 || items.size() < 1024) {
        std::string bestId;
        double bestDist = std::numeric_limits<double>::max();
        for (const auto& it : items) {
            double d = haversineDistance(position, it.second);
            if (d < bestDist) {
                bestDist = d;
                bestId = it.first;
            }
        }
        return bestId;
    }

    // Parallel reduce
    struct LocalBest { std::string id; double dist; };
    std::vector<LocalBest> locals(threadCount, LocalBest{"", std::numeric_limits<double>::max()});
    std::vector<std::thread> threads;
    threads.reserve(threadCount);

    size_t n = items.size();
    size_t base = n / threadCount;
    size_t rem = n % threadCount;
    size_t start = 0;
    for (unsigned i = 0; i < threadCount; ++i) {
        size_t span = base + (i < rem ? 1 : 0);
        size_t s = start;
        size_t e = s + span; // [s, e)
        start = e;
        threads.emplace_back([&, i, s, e]() {
            double best = std::numeric_limits<double>::max();
            std::string id;
            for (size_t k = s; k < e; ++k) {
                const auto& it = items[k];
                double d = haversineDistance(position, it.second);
                if (d < best) {
                    best = d;
                    id = it.first;
                }
            }
            locals[i].dist = best;
            locals[i].id = id;
        });
    }

    for (auto& t : threads) t.join();

    // Reduce global best
    std::string bestId;
    double bestDist = std::numeric_limits<double>::max();
    for (const auto& lb : locals) {
        if (!lb.id.empty() && lb.dist < bestDist) {
            bestDist = lb.dist;
            bestId = lb.id;
        }
    }
    return bestId;
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

    // Hold a shared lock for the duration to block writers while allowing safe reads.
    std::shared_lock<std::shared_mutex> outerLock(indexMutex);

    // More precise search radius calculation
    GeoPoint center = bounds.center();
    double latRadiusMeters = (bounds.maxLat - bounds.minLat) * 111000.0 / 2.0;
    double lngRadiusMeters = (bounds.maxLng - bounds.minLng) * 111000.0 * std::cos(center.latitude * PI / 180.0) / 2.0;
    double searchRadiusMeters = std::sqrt(latRadiusMeters * latRadiusMeters + lngRadiusMeters * lngRadiusMeters);

    // Limit search radius to prevent excessive hex cell generation
    searchRadiusMeters = std::min(searchRadiusMeters, 50000.0); // Max 50km search

    HexCoord centerCoord = geocell->latLngToHexCoord(center, zoom);

    // Efficient hex radius calculation
    int hexRadius = std::max(1, static_cast<int>(std::ceil(searchRadiusMeters / 1000.0)));
    hexRadius = std::min(hexRadius, 20); // bound to prevent explosion

    // Total number of dq rows we will process
    const int dqMin = -hexRadius;
    const int dqMax = hexRadius;
    const int dqCount = dqMax - dqMin + 1;

    // Decide parallelism
    unsigned hw = std::thread::hardware_concurrency();
    if (hw == 0) hw = 2;
    unsigned maxThreads = std::min<unsigned>(hw, 4); // cap to avoid overhead
    unsigned threadCount = std::min<unsigned>(maxThreads, static_cast<unsigned>(dqCount));

    // If small work, run single-threaded to avoid overhead
    if (threadCount <= 1 || dqCount < 6) {
        std::vector<std::string> result;
        result.reserve(100);
        for (int dq = dqMin; dq <= dqMax; ++dq) {
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

    // Parallel execution: split dq rows into contiguous segments
    std::vector<std::vector<std::string>> partials(threadCount);
    std::vector<std::thread> threads;
    threads.reserve(threadCount);

    auto worker = [&](int dqStart, int dqEnd, std::vector<std::string>& out) {
        out.reserve(64);
        for (int dq = dqStart; dq <= dqEnd; ++dq) {
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
                            out.push_back(markerId);
                        }
                    }
                }
            }
        }
    };

    int rowsPerThread = dqCount / static_cast<int>(threadCount);
    int remainder = dqCount % static_cast<int>(threadCount);
    int current = dqMin;
    for (unsigned i = 0; i < threadCount; ++i) {
        int span = rowsPerThread + (i < static_cast<unsigned>(remainder) ? 1 : 0);
        int start = current;
        int end = start + span - 1;
        current = end + 1;
        threads.emplace_back(worker, start, end, std::ref(partials[i]));
    }

    for (auto& t : threads) t.join();

    // Merge results
    std::vector<std::string> result;
    size_t total = 0;
    for (const auto& v : partials) total += v.size();
    result.reserve(total);
    for (auto& v : partials) {
        result.insert(result.end(), v.begin(), v.end());
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
