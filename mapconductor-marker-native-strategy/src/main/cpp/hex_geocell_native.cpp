#include "hex_geocell_native.h"
#include <cmath>
#include <algorithm>

constexpr double PI = 3.14159265358979323846;
constexpr double WEB_MERCATOR_MAX = 20037508.342789244;

Offset WebMercatorProjection::project(const GeoPoint& point) const {
    double x = point.longitude * WEB_MERCATOR_MAX / 180.0;
    double y = std::log(std::tan((90.0 + point.latitude) * PI / 360.0)) / (PI / 180.0);
    y = y * WEB_MERCATOR_MAX / 180.0;
    return Offset(x, y);
}

GeoPoint WebMercatorProjection::unproject(const Offset& offset) const {
    double lng = (offset.x / WEB_MERCATOR_MAX) * 180.0;
    double lat = (offset.y / WEB_MERCATOR_MAX) * 180.0;
    lat = 180.0 / PI * (2.0 * std::atan(std::exp(lat * PI / 180.0)) - PI / 2.0);
    return GeoPoint(lat, lng);
}

HexGeocellNative::HexGeocellNative(int baseHexSideLength) 
    : baseHexSideLength(baseHexSideLength)
    , projection(std::make_unique<WebMercatorProjection>()) {
}

HexGeocellNative::~HexGeocellNative() = default;

double HexGeocellNative::adjustedHexSideLength(double lat, double zoom) const {
    double scale = 1.0 / std::pow(2.0, zoom);
    double latScale = std::max(0.01, std::cos(lat * PI / 180.0));
    return baseHexSideLength * scale / latScale;
}

Offset HexGeocellNative::hexCenterXY(const HexCoord& coord, double hexSideLength) const {
    double x = hexSideLength * (3.0 / 2.0 * coord.q);
    double y = hexSideLength * (std::sqrt(3.0) * (coord.r + coord.q / 2.0));
    return Offset(x, y);
}

HexCoord HexGeocellNative::pixelToHex(const Offset& offset, double hexSideLength) const {
    double q = (2.0 / 3.0 * offset.x / hexSideLength);
    double r = (-1.0 / 3.0 * offset.x + std::sqrt(3.0) / 3.0 * offset.y) / hexSideLength;
    return cubeRound(q, r);
}

HexCoord HexGeocellNative::latLngToHexCoord(const GeoPoint& position, double zoom) const {
    double hexSideLength = adjustedHexSideLength(position.latitude, zoom);
    Offset offset = projection->project(position);
    return pixelToHex(offset, hexSideLength);
}

HexCell HexGeocellNative::latLngToHexCell(const GeoPoint& position, double zoom) const {
    HexCoord coord = latLngToHexCoord(position, zoom);
    std::string id = hexToCellId(coord, zoom);
    GeoPoint centerLatLng = hexToLatLngCenter(coord, position.latitude, zoom);
    Offset centerXY = projection->project(centerLatLng);
    return HexCell(coord, centerLatLng, centerXY, id);
}

GeoPoint HexGeocellNative::hexToLatLngCenter(const HexCoord& coord, double latHint, double zoom) const {
    double hexSideLength = adjustedHexSideLength(latHint, zoom);
    Offset center = hexCenterXY(coord, hexSideLength);
    return projection->unproject(center);
}

std::string HexGeocellNative::hexToCellId(const HexCoord& coord, double zoom) const {
    return "H" + std::to_string(coord.q) + "_" + std::to_string(coord.r) + "_Z" + std::to_string(static_cast<int>(zoom));
}