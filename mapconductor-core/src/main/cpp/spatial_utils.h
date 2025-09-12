#pragma once

#include <cmath>
#include <vector>
#include <string>

struct GeoPoint {
    double latitude;
    double longitude;
    
    GeoPoint() : latitude(0.0), longitude(0.0) {}
    GeoPoint(double lat, double lng) : latitude(lat), longitude(lng) {}
};

struct HexCoord {
    int q;
    int r;
    int depth;
    
    HexCoord() : q(0), r(0), depth(0) {}
    HexCoord(int q, int r, int depth = 0) : q(q), r(r), depth(depth) {}
    
    int s() const { return -q - r; }
    
    std::string toString() const {
        return "H" + std::to_string(q) + "_" + std::to_string(r) + "_" + std::to_string(depth);
    }
};

struct Offset {
    double x;
    double y;
    
    Offset() : x(0.0), y(0.0) {}
    Offset(double x, double y) : x(x), y(y) {}
};

struct MarkerPoint {
    std::string id;
    GeoPoint position;
    bool clickable;
    
    MarkerPoint() : clickable(true) {}
    MarkerPoint(const std::string& id, const GeoPoint& pos, bool clickable = true)
        : id(id), position(pos), clickable(clickable) {}
};

struct HexCell {
    HexCoord coord;
    GeoPoint centerLatLng;
    Offset centerXY;
    std::string id;
    
    HexCell() {}
    HexCell(const HexCoord& coord, const GeoPoint& center, const Offset& xy, const std::string& id)
        : coord(coord), centerLatLng(center), centerXY(xy), id(id) {}
};

struct HexCellWithDistance {
    HexCell cell;
    double distanceMeters;
    
    HexCellWithDistance() : distanceMeters(0.0) {}
    HexCellWithDistance(const HexCell& cell, double distance)
        : cell(cell), distanceMeters(distance) {}
};

struct GeoRectBounds {
    double minLat;
    double maxLat;
    double minLng;
    double maxLng;
    
    GeoRectBounds() : minLat(0.0), maxLat(0.0), minLng(0.0), maxLng(0.0) {}
    GeoRectBounds(double minLat, double maxLat, double minLng, double maxLng)
        : minLat(minLat), maxLat(maxLat), minLng(minLng), maxLng(maxLng) {}
    
    bool isEmpty() const {
        return minLat >= maxLat || minLng >= maxLng;
    }
    
    GeoPoint center() const {
        return GeoPoint((minLat + maxLat) / 2.0, (minLng + maxLng) / 2.0);
    }
    
    bool contains(const GeoPoint& point) const {
        return point.latitude >= minLat && point.latitude <= maxLat &&
               point.longitude >= minLng && point.longitude <= maxLng;
    }
};

// Utility functions
double haversineDistance(const GeoPoint& p1, const GeoPoint& p2);
HexCoord cubeRound(double q, double r);
int hexDistance(const HexCoord& a, const HexCoord& b);
std::vector<HexCoord> hexRange(const HexCoord& center, int radius);
std::vector<HexCoord> hexRing(const HexCoord& center, int radius);