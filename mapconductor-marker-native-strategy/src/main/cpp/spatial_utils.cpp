#include "spatial_utils.h"
#include <cmath>
#include <algorithm>

constexpr double PI = 3.14159265358979323846;
constexpr double EARTH_RADIUS_METERS = 6371000.0;

double haversineDistance(const GeoPoint& p1, const GeoPoint& p2) {
    double lat1Rad = p1.latitude * PI / 180.0;
    double lat2Rad = p2.latitude * PI / 180.0;
    double deltaLatRad = (p2.latitude - p1.latitude) * PI / 180.0;
    double deltaLngRad = (p2.longitude - p1.longitude) * PI / 180.0;
    
    double a = std::sin(deltaLatRad / 2.0) * std::sin(deltaLatRad / 2.0) +
               std::cos(lat1Rad) * std::cos(lat2Rad) *
               std::sin(deltaLngRad / 2.0) * std::sin(deltaLngRad / 2.0);
    
    double c = 2.0 * std::atan2(std::sqrt(a), std::sqrt(1.0 - a));
    
    return EARTH_RADIUS_METERS * c;
}

HexCoord cubeRound(double q, double r) {
    double s = -q - r;
    
    int rq = static_cast<int>(std::round(q));
    int rr = static_cast<int>(std::round(r));
    int rs = static_cast<int>(std::round(s));
    
    double qDiff = std::abs(rq - q);
    double rDiff = std::abs(rr - r);
    double sDiff = std::abs(rs - s);
    
    if (qDiff > rDiff && qDiff > sDiff) {
        rq = -rr - rs;
    } else if (rDiff > sDiff) {
        rr = -rq - rs;
    } else {
        rs = -rq - rr;
    }
    
    return HexCoord(rq, rr);
}

int hexDistance(const HexCoord& a, const HexCoord& b) {
    return (std::abs(a.q - b.q) + std::abs(a.q + a.r - b.q - b.r) + std::abs(a.r - b.r)) / 2;
}

std::vector<HexCoord> hexRange(const HexCoord& center, int radius) {
    std::vector<HexCoord> results;
    results.reserve((3 * radius * (radius + 1)) + 1);
    
    for (int dq = -radius; dq <= radius; ++dq) {
        int minR = std::max(-radius, -dq - radius);
        int maxR = std::min(radius, -dq + radius);
        
        for (int dr = minR; dr <= maxR; ++dr) {
            results.emplace_back(center.q + dq, center.r + dr, center.depth);
        }
    }
    
    return results;
}

std::vector<HexCoord> hexRing(const HexCoord& center, int radius) {
    std::vector<HexCoord> results;
    
    if (radius == 0) {
        results.push_back(center);
        return results;
    }
    
    // Reserve space for exactly 6 * radius cells
    results.reserve(6 * radius);
    
    // Start at one corner of the ring
    HexCoord current = HexCoord(center.q - radius, center.r + radius, center.depth);
    
    // The six directions in axial coordinates
    const int directions[6][2] = {
        {1, 0},   // E
        {1, -1},  // SE  
        {0, -1},  // SW
        {-1, 0},  // W
        {-1, 1},  // NW
        {0, 1}    // NE
    };
    
    // Walk around the ring
    for (int direction = 0; direction < 6; ++direction) {
        for (int step = 0; step < radius; ++step) {
            results.push_back(current);
            current.q += directions[direction][0];
            current.r += directions[direction][1];
        }
    }
    
    return results;
}