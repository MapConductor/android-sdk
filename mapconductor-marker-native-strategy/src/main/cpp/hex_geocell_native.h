#pragma once

#include "spatial_utils.h"
#include <memory>

class WebMercatorProjection {
public:
    Offset project(const GeoPoint& point) const;
    GeoPoint unproject(const Offset& offset) const;
};

class HexGeocellNative {
private:
    std::unique_ptr<WebMercatorProjection> projection;
    int baseHexSideLength;
    
    double adjustedHexSideLength(double lat, double zoom) const;
    Offset hexCenterXY(const HexCoord& coord, double hexSideLength) const;
    HexCoord pixelToHex(const Offset& offset, double hexSideLength) const;
    
public:
    HexGeocellNative(int baseHexSideLength = 1000);
    ~HexGeocellNative();
    
    HexCoord latLngToHexCoord(const GeoPoint& position, double zoom) const;
    HexCell latLngToHexCell(const GeoPoint& position, double zoom) const;
    GeoPoint hexToLatLngCenter(const HexCoord& coord, double latHint, double zoom) const;
    std::string hexToCellId(const HexCoord& coord, double zoom) const;
};