#ifndef REMOTE_SPATIAL_MARKER_STRATEGY_H
#define REMOTE_SPATIAL_MARKER_STRATEGY_H

#include <vector>
#include <string>
#include <memory>
#include <mutex>
#include <shared_mutex>
#include <unordered_map>
#include <unordered_set>
#include <queue>
#include <atomic>
#include <thread>
#include <condition_variable>
#include "marker_types.h"
#include "native_marker_index.h"
#include "spatial_utils.h"

namespace mapconductor {
namespace native {

// Data transfer objects for service communication
struct MarkerDataDTO {
    std::string id;
    double latitude;
    double longitude;
    bool clickable;
    
    MarkerDataDTO() = default;
    MarkerDataDTO(const std::string& id, double lat, double lng, bool click)
        : id(id), latitude(lat), longitude(lng), clickable(click) {}
};

struct SpatialConfigDTO {
    double expandMargin;
    bool addOnlyMode;
    
    SpatialConfigDTO() : expandMargin(0.3), addOnlyMode(false) {}
    SpatialConfigDTO(double margin, bool addOnly) : expandMargin(margin), addOnlyMode(addOnly) {}
};

struct SpatialResultDTO {
    std::vector<std::string> markersToAdd;
    std::vector<std::string> markersToRemove;
    std::vector<std::string> errors;
};

// Use the existing GeoRectBounds from spatial_utils.h
using GeoRectBounds = ::GeoRectBounds;

struct CameraPosition {
    double latitude;
    double longitude;
    double zoom;
    double bearing;
    double tilt;
    GeoRectBounds visibleBounds;
    
    CameraPosition() = default;
    CameraPosition(double lat, double lng, double z, double b, double t, const GeoRectBounds& bounds)
        : latitude(lat), longitude(lng), zoom(z), bearing(b), tilt(t), visibleBounds(bounds) {}
};

// Copyable performance statistics for returning from functions
struct PerformanceStatsSnapshot {
    uint64_t totalCameraChanges;
    uint64_t totalMarkersProcessed;
    uint64_t totalSpatialQueries;
    uint64_t totalBatchUpdates;
    double averageQueryTimeMs;
    double averageBatchProcessTimeMs;
    size_t currentMarkerCount;
    size_t renderedMarkerCount;
    
    PerformanceStatsSnapshot() 
        : totalCameraChanges(0), totalMarkersProcessed(0), totalSpatialQueries(0), 
          totalBatchUpdates(0), averageQueryTimeMs(0.0), averageBatchProcessTimeMs(0.0),
          currentMarkerCount(0), renderedMarkerCount(0) {}
};

// Performance statistics for monitoring (with atomic fields)
struct PerformanceStats {
    std::atomic<uint64_t> totalCameraChanges{0};
    std::atomic<uint64_t> totalMarkersProcessed{0};
    std::atomic<uint64_t> totalSpatialQueries{0};
    std::atomic<uint64_t> totalBatchUpdates{0};
    std::atomic<double> averageQueryTimeMs{0.0};
    std::atomic<double> averageBatchProcessTimeMs{0.0};
    std::atomic<size_t> currentMarkerCount{0};
    std::atomic<size_t> renderedMarkerCount{0};
    
    // Default constructor
    PerformanceStats() = default;
    
    // Copy constructor is deleted due to atomic members
    PerformanceStats(const PerformanceStats&) = delete;
    PerformanceStats& operator=(const PerformanceStats&) = delete;
    
    void reset() {
        totalCameraChanges = 0;
        totalMarkersProcessed = 0;
        totalSpatialQueries = 0;
        totalBatchUpdates = 0;
        averageQueryTimeMs = 0.0;
        averageBatchProcessTimeMs = 0.0;
        currentMarkerCount = 0;
        renderedMarkerCount = 0;
    }
};

/**
 * High-performance C++ implementation of RemoteSpatialMarkerRenderingStrategy.
 * 
 * This implementation provides:
 * - Native spatial indexing using optimized C++ data structures
 * - Batch processing for marker updates to reduce JNI overhead
 * - Background thread processing for IPC communication simulation
 * - Lock-free operations where possible for maximum performance
 * - Memory pool allocation to reduce allocation overhead
 * - Vectorized spatial calculations for better performance
 */
class RemoteSpatialMarkerStrategy {
private:
    std::string sessionId;
    double expandMargin;
    bool addOnlyMode;
    
    // Native spatial index for fast spatial queries
    std::unique_ptr<NativeMarkerIndex> spatialIndex;
    
    // Marker state management - simplified to avoid complex constructor issues
    std::unordered_map<std::string, MarkerDataDTO> allMarkers;
    std::unordered_set<std::string> renderedMarkers;
    mutable std::mutex markersMutex;
    
    // Batch processing for performance
    static constexpr size_t MAX_BATCH_SIZE = 500;
    static constexpr int BATCH_DELAY_MS = 100;
    std::queue<MarkerDataDTO> pendingUpdates;
    std::mutex batchMutex;
    std::condition_variable batchCondition;
    std::atomic<bool> batchProcessorRunning{false};
    std::thread batchProcessorThread;
    
    // Performance monitoring
    mutable PerformanceStats stats;
    
    // Background service simulation
    std::atomic<bool> serviceConnected{false};
    std::mutex serviceConnectionMutex;
    
    // Memory pool for reducing allocations
    static constexpr size_t INITIAL_VECTOR_CAPACITY = 1000;
    mutable std::vector<std::string> tempMarkerIds;
    mutable std::vector<MarkerDataDTO> tempMarkerData;
    
public:
    explicit RemoteSpatialMarkerStrategy(
        const std::string& sessionId,
        double expandMargin = 0.3,
        bool addOnlyMode = false
    );
    
    ~RemoteSpatialMarkerStrategy();
    
    // Core strategy operations
    bool initializeSession(const SpatialConfigDTO& config);
    void destroySession();
    
    // Marker management
    bool addMarkers(const std::vector<MarkerDataDTO>& markers);
    bool updateMarker(const MarkerDataDTO& marker);
    bool removeMarker(const std::string& markerId);
    
    // Spatial queries
    SpatialResultDTO processCameraChange(const CameraPosition& cameraPosition);
    std::vector<std::string> findMarkersInBounds(const GeoRectBounds& bounds) const;
    std::string findNearestMarker(double latitude, double longitude) const;
    
    // Batch processing
    void addToBatch(const MarkerDataDTO& marker);
    void processPendingUpdates();
    
    // Performance and monitoring
    PerformanceStatsSnapshot getPerformanceStats() const;
    void resetPerformanceStats();
    size_t getMarkerCount() const;
    size_t getRenderedMarkerCount() const;
    
    // Service connection simulation
    bool isServiceConnected() const { return serviceConnected.load(); }
    void setServiceConnected(bool connected) { serviceConnected.store(connected); }
    
private:
    // Internal helper methods
    void startBatchProcessor();
    void stopBatchProcessor();
    void batchProcessorLoop();
    
    // Spatial calculation helpers
    GeoRectBounds expandBounds(const GeoRectBounds& bounds, double margin) const;
    bool isMarkerInBounds(const MarkerDataDTO& marker, const GeoRectBounds& bounds) const;
    
    // Performance measurement helpers
    void recordQueryTime(double timeMs) const;
    void recordBatchProcessTime(double timeMs) const;
    
    // Memory management helpers
    void prepareTemporaryVectors(size_t expectedSize) const;
    void clearTemporaryVectors() const;
};

// Factory functions for commonly used configurations
std::unique_ptr<RemoteSpatialMarkerStrategy> createRemoteSpatialStrategy(
    const std::string& sessionId,
    double expandMargin = 0.3,
    bool addOnlyMode = false
);

std::unique_ptr<RemoteSpatialMarkerStrategy> createHighPerformanceRemoteStrategy(
    const std::string& sessionId,
    double expandMargin = 0.5,
    bool addOnlyMode = true
);

std::unique_ptr<RemoteSpatialMarkerStrategy> createLargeDatasetRemoteStrategy(
    const std::string& sessionId,
    double expandMargin = 0.8,
    bool addOnlyMode = true
);

} // namespace native
} // namespace mapconductor

#endif // REMOTE_SPATIAL_MARKER_STRATEGY_H