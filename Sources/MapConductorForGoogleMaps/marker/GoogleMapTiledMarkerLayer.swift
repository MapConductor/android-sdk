import CoreGraphics
import GoogleMaps
import UIKit

internal final class GoogleMapTiledMarkerLayer: GMSTileLayer {
    internal struct RenderMarker {
        let id: String
        let latitude: Double
        let longitude: Double
        let visible: Bool
        let image: CGImage
        let anchorX: CGFloat
        let anchorY: CGFloat
        let drawWidth: Double
        let drawHeight: Double
    }

    private let tileSizePx: Int
    private let renderScale: Int
    private let lock = NSLock()

    private var markersById: [String: RenderMarker] = [:]
    private var indexedZoom: Int = -1
    private var tileToMarkerIds: [UInt64: [String]] = [:]

    init(
        mapView: GMSMapView,
        tileSizePx: Int = 256,
        zIndex: Int32 = 0
    ) {
        self.tileSizePx = max(1, tileSizePx)
        self.renderScale = min(2, max(1, Int(round(UIScreen.main.scale))))
        super.init()
        tileSize = self.tileSizePx
        self.zIndex = zIndex
        map = mapView
    }

    func setMarkers(
        _ markers: [String: RenderMarker],
        zoom: Int
    ) {
        lock.lock()
        markersById = markers
        rebuildIndexLocked(zoom: zoom)
        lock.unlock()
        clearTileCache()
    }

    func setZoom(_ zoom: Int) {
        lock.lock()
        if zoom == indexedZoom {
            lock.unlock()
            return
        }
        rebuildIndexLocked(zoom: zoom)
        lock.unlock()
        clearTileCache()
    }

    func remove() {
        map = nil
    }

    override func tileFor(x: UInt, y: UInt, zoom: UInt) -> UIImage? {
        let requestedZoom = Int(zoom)
        let worldTileCount = 1 << requestedZoom
        if Int(y) < 0 || Int(y) >= worldTileCount { return nil }

        let normalizedX = normalizeTileX(Int(x), worldTileCount: worldTileCount)
        let key = tileKey(x: normalizedX, y: Int(y))

        lock.lock()
        let zoomIndex = (requestedZoom == indexedZoom) ? tileToMarkerIds : [:]
        let ids = zoomIndex[key] ?? []
        let markers = markersById
        lock.unlock()

        if ids.isEmpty { return nil }

        let renderTileSize = tileSizePx * renderScale
        let format = UIGraphicsImageRendererFormat.default()
        format.scale = 1
        format.opaque = false

        let renderImage = UIGraphicsImageRenderer(size: CGSize(width: renderTileSize, height: renderTileSize), format: format).image { ctx in
            let context = ctx.cgContext
            context.setAllowsAntialiasing(true)
            context.setShouldAntialias(true)
            context.interpolationQuality = .high
            if renderScale != 1 {
                context.scaleBy(x: CGFloat(renderScale), y: CGFloat(renderScale))
            }

            let worldPixelSize = Double(worldTileCount * tileSizePx)
            let tileOriginX = Double(normalizedX * tileSizePx)
            let tileOriginY = Double(Int(y) * tileSizePx)

            for id in ids {
                guard let marker = markers[id] else { continue }
                if !marker.visible { continue }
                let pixel = mercatorPixel(latitude: marker.latitude, longitude: marker.longitude, worldPixelSize: worldPixelSize)
                let localX = pixel.x - tileOriginX
                let localY = pixel.y - tileOriginY

                let left = Double(localX) - Double(marker.anchorX) * marker.drawWidth
                let top = Double(localY) - Double(marker.anchorY) * marker.drawHeight
                let rect = CGRect(x: left, y: top, width: marker.drawWidth, height: marker.drawHeight)
                context.draw(marker.image, in: rect)
            }
        }

        if renderScale == 1 { return renderImage }

        let finalImage = UIGraphicsImageRenderer(size: CGSize(width: tileSizePx, height: tileSizePx), format: format).image { ctx in
            ctx.cgContext.interpolationQuality = .high
            renderImage.draw(in: CGRect(x: 0, y: 0, width: tileSizePx, height: tileSizePx))
        }
        return finalImage
    }

    private struct Pixel {
        let x: Double
        let y: Double
    }

    private func mercatorPixel(
        latitude: Double,
        longitude: Double,
        worldPixelSize: Double
    ) -> Pixel {
        let clampedLatitude = min(85.05112878, max(-85.05112878, latitude))
        let sinLatitude = min(0.9999, max(-0.9999, sin(clampedLatitude * .pi / 180.0)))
        let x = (longitude + 180.0) / 360.0
        let y = 0.5 - log((1.0 + sinLatitude) / (1.0 - sinLatitude)) / (4.0 * .pi)

        let pixelX = normalizePixel(x * worldPixelSize, worldPixelSize: worldPixelSize)
        let pixelY = min(worldPixelSize - 1.0, max(0.0, y * worldPixelSize))
        return Pixel(x: pixelX, y: pixelY)
    }

    private func normalizePixel(_ pixel: Double, worldPixelSize: Double) -> Double {
        let wrapped = pixel.truncatingRemainder(dividingBy: worldPixelSize)
        return wrapped < 0 ? wrapped + worldPixelSize : wrapped
    }

    private func normalizeTileX(_ x: Int, worldTileCount: Int) -> Int {
        let wrapped = x % worldTileCount
        return wrapped < 0 ? wrapped + worldTileCount : wrapped
    }

    private func tileKey(x: Int, y: Int) -> UInt64 {
        (UInt64(bitPattern: Int64(x)) << 32) ^ (UInt64(bitPattern: Int64(y)) & 0xffffffff)
    }

    private func rebuildIndexLocked(zoom: Int) {
        if markersById.isEmpty {
            indexedZoom = zoom
            tileToMarkerIds = [:]
            return
        }

        let worldTileCount = 1 << zoom
        let worldPixelSize = Double(worldTileCount * tileSizePx)
        var tiles: [UInt64: [String]] = [:]
        tiles.reserveCapacity(min(4096, markersById.count))

        for marker in markersById.values {
            if !marker.visible { continue }
            let pixel = mercatorPixel(latitude: marker.latitude, longitude: marker.longitude, worldPixelSize: worldPixelSize)
            let left = pixel.x - Double(marker.anchorX) * marker.drawWidth
            let top = pixel.y - Double(marker.anchorY) * marker.drawHeight
            let right = left + marker.drawWidth
            let bottom = top + marker.drawHeight

            let minTileX = Int(floor(left / Double(tileSizePx)))
            let maxTileX = Int(floor((right - 1.0) / Double(tileSizePx)))
            let minTileY = Int(floor(top / Double(tileSizePx)))
            let maxTileY = Int(floor((bottom - 1.0) / Double(tileSizePx)))

            if minTileY >= worldTileCount || maxTileY < 0 { continue }
            for tileY in minTileY...maxTileY {
                if tileY < 0 || tileY >= worldTileCount { continue }
                for tileX in minTileX...maxTileX {
                    let normalizedX = normalizeTileX(tileX, worldTileCount: worldTileCount)
                    let key = tileKey(x: normalizedX, y: tileY)
                    if tiles[key] == nil { tiles[key] = [] }
                    tiles[key]?.append(marker.id)
                }
            }
        }

        indexedZoom = zoom
        tileToMarkerIds = tiles
    }
}

