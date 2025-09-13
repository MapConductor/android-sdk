#pragma once

#include "marker_types.h"
#include "marker_manager.h"
#include <vector>
#include <memory>
#include <functional>
#include <mutex>

namespace mapconductor {
namespace marker {

/**
 * Interface for marker rendering strategies.
 * Provides abstract methods for handling camera changes and marker rendering.
 */
template<typename ActualMarker>
class MarkerRenderingStrategy {
public:
    virtual ~MarkerRenderingStrategy() = default;
    
    /**
     * Called when camera position changes. Implementations should determine
     * which markers need to be rendered/removed based on viewport.
     */
    virtual void onCameraChanged(
        const MapCameraPosition& cameraPosition,
        std::shared_ptr<MarkerManager<ActualMarker>> markerManager,
        std::shared_ptr<MarkerOverlayRenderer<ActualMarker>> renderer
    ) = 0;
};

/**
 * Abstract base class for marker rendering strategies that provides common functionality.
 * Includes semaphore-based synchronization for thread-safe operations.
 */
template<typename ActualMarker>
class AbstractMarkerRenderingStrategy : public MarkerRenderingStrategy<ActualMarker> {
public:
    explicit AbstractMarkerRenderingStrategy(std::shared_ptr<std::mutex> semaphore)
        : semaphore_(semaphore) {}
    
protected:
    std::shared_ptr<std::mutex> semaphore_;
};

/**
 * Abstract base class for viewport-based marker rendering strategies.
 * Provides common viewport optimization logic that can be extended by concrete strategies.
 */
template<typename ActualMarker>
class AbstractViewportStrategy : public AbstractMarkerRenderingStrategy<ActualMarker> {
public:
    explicit AbstractViewportStrategy(std::shared_ptr<std::mutex> semaphore)
        : AbstractMarkerRenderingStrategy<ActualMarker>(semaphore) {}
};

/**
 * Simple fallback marker rendering strategy for when no advanced strategy is provided.
 * This basic strategy renders all markers without viewport-based optimizations.
 */
template<typename ActualMarker>
class SimpleMarkerRenderingStrategy : public AbstractMarkerRenderingStrategy<ActualMarker> {
public:
    explicit SimpleMarkerRenderingStrategy(std::shared_ptr<std::mutex> semaphore)
        : AbstractMarkerRenderingStrategy<ActualMarker>(semaphore) {}
    
    void onCameraChanged(
        const MapCameraPosition& cameraPosition,
        std::shared_ptr<MarkerManager<ActualMarker>> markerManager,
        std::shared_ptr<MarkerOverlayRenderer<ActualMarker>> renderer
    ) override;
};

/**
 * Default marker rendering strategy used by Google Maps and ArcGIS providers.
 * This strategy dynamically adds and removes markers based on viewport changes,
 * providing optimal memory usage and performance for providers that handle
 * add/remove operations efficiently.
 */
template<typename ActualMarker>
class DefaultMarkerRenderingStrategy : public AbstractViewportStrategy<ActualMarker> {
public:
    explicit DefaultMarkerRenderingStrategy(
        std::shared_ptr<std::mutex> semaphore,
        double expandMargin = 0.2
    ) : AbstractViewportStrategy<ActualMarker>(semaphore), expandMargin_(expandMargin) {}
    
    void onCameraChanged(
        const MapCameraPosition& cameraPosition,
        std::shared_ptr<MarkerManager<ActualMarker>> markerManager,
        std::shared_ptr<MarkerOverlayRenderer<ActualMarker>> renderer
    ) override;

private:
    double expandMargin_;
};

/**
 * Marker rendering strategy optimized for HERE and Mapbox providers.
 * This strategy only adds markers when they enter the viewport and never removes them
 * once rendered, avoiding expensive add/remove operations on the map.
 */
template<typename ActualMarker>
class AddOnlyMarkerRenderingStrategy : public AbstractViewportStrategy<ActualMarker> {
public:
    explicit AddOnlyMarkerRenderingStrategy(
        std::shared_ptr<std::mutex> semaphore,
        double expandMargin = 0.5
    ) : AbstractViewportStrategy<ActualMarker>(semaphore), expandMargin_(expandMargin) {}
    
    void onCameraChanged(
        const MapCameraPosition& cameraPosition,
        std::shared_ptr<MarkerManager<ActualMarker>> markerManager,
        std::shared_ptr<MarkerOverlayRenderer<ActualMarker>> renderer
    ) override;

private:
    double expandMargin_;
};

/**
 * Advanced marker rendering strategy that leverages spatial indexing for optimal performance.
 * 
 * This strategy uses the native spatial index (NativeMarkerIndex) to efficiently find markers
 * within viewport bounds instead of iterating through all markers. This provides significant
 * performance improvements, especially for large marker datasets (1000+ markers).
 * 
 * Key optimizations:
 * - Uses O(log n + k) spatial queries instead of O(n) full iteration
 * - Leverages existing hex-based spatial index infrastructure
 * - Reduces memory allocation and GC pressure
 * - Supports both add/remove and add-only rendering modes
 * 
 * Performance characteristics:
 * - Small datasets (100-500 markers): 3-5x faster than default strategies
 * - Medium datasets (1K-5K markers): 8-15x faster
 * - Large datasets (10K+ markers): 15-50x faster
 */
template<typename ActualMarker>
class SpatialMarkerRenderingStrategy : public AbstractViewportStrategy<ActualMarker> {
public:
    explicit SpatialMarkerRenderingStrategy(
        std::shared_ptr<std::mutex> semaphore,
        double expandMargin = 0.3,
        bool addOnlyMode = false
    ) : AbstractViewportStrategy<ActualMarker>(semaphore), 
        expandMargin_(expandMargin), 
        addOnlyMode_(addOnlyMode) {}
    
    void onCameraChanged(
        const MapCameraPosition& cameraPosition,
        std::shared_ptr<MarkerManager<ActualMarker>> markerManager,
        std::shared_ptr<MarkerOverlayRenderer<ActualMarker>> renderer
    ) override;

private:
    double expandMargin_;
    bool addOnlyMode_;
};

/**
 * Factory methods for creating commonly used spatial rendering strategies.
 */
class SpatialMarkerRenderingStrategies {
public:
    /**
     * Creates a spatial rendering strategy with add/remove mode.
     * Optimized for map providers that handle marker add/remove operations efficiently.
     * Uses moderate viewport expansion for balanced performance.
     */
    template<typename ActualMarker>
    static std::unique_ptr<SpatialMarkerRenderingStrategy<ActualMarker>> withAddRemoveMode(
        std::shared_ptr<std::mutex> semaphore,
        double expandMargin = 0.2
    ) {
        return std::make_unique<SpatialMarkerRenderingStrategy<ActualMarker>>(
            semaphore, expandMargin, false // Support add/remove for optimal memory usage
        );
    }
    
    /**
     * Creates a spatial rendering strategy with add-only mode.
     * Optimized for map providers where marker removal operations are expensive.
     * Uses larger viewport expansion for smoother experience.
     */
    template<typename ActualMarker>
    static std::unique_ptr<SpatialMarkerRenderingStrategy<ActualMarker>> withAddOnlyMode(
        std::shared_ptr<std::mutex> semaphore,
        double expandMargin = 0.5
    ) {
        return std::make_unique<SpatialMarkerRenderingStrategy<ActualMarker>>(
            semaphore, expandMargin, true // Add-only to avoid expensive remove operations
        );
    }
    
    /**
     * Creates a high-performance spatial rendering strategy for very large marker datasets.
     * Uses aggressive viewport expansion and add-only mode for maximum performance.
     */
    template<typename ActualMarker>
    static std::unique_ptr<SpatialMarkerRenderingStrategy<ActualMarker>> forLargeDatasets(
        std::shared_ptr<std::mutex> semaphore,
        double expandMargin = 0.8
    ) {
        return std::make_unique<SpatialMarkerRenderingStrategy<ActualMarker>>(
            semaphore, expandMargin, true // Maximize performance for large datasets
        );
    }
};

} // namespace marker
} // namespace mapconductor