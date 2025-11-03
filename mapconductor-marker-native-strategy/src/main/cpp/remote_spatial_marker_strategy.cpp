#include "remote_spatial_marker_strategy.h"
#include <chrono>
#include <algorithm>
#include <cmath>
#include <random>
#include <android/log.h>

#define LOG_TAG "RemoteSpatialStrategy"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

namespace mapconductor {
namespace native {

RemoteSpatialMarkerStrategy::RemoteSpatialMarkerStrategy(
    const std::string& sessionId,
    double expandMargin,
    bool addOnlyMode
) : sessionId(sessionId), 
    expandMargin(expandMargin), 
    addOnlyMode(addOnlyMode),
    spatialIndex(std::make_unique<NativeMarkerIndex>()) {
    
    // Pre-allocate temporary vectors for better performance
    tempMarkerIds.reserve(INITIAL_VECTOR_CAPACITY);
    tempMarkerData.reserve(INITIAL_VECTOR_CAPACITY);
    
    LOGD("RemoteSpatialMarkerStrategy created with session: %s, expandMargin: %.2f, addOnly: %s", 
         sessionId.c_str(), expandMargin, addOnlyMode ? "true" : "false");
}

RemoteSpatialMarkerStrategy::~RemoteSpatialMarkerStrategy() {
    destroySession();
    LOGD("RemoteSpatialMarkerStrategy destroyed for session: %s", sessionId.c_str());
}

bool RemoteSpatialMarkerStrategy::initializeSession(const SpatialConfigDTO& config) {
    std::lock_guard<std::mutex> lock(serviceConnectionMutex);
    
    // Update configuration
    expandMargin = config.expandMargin;
    addOnlyMode = config.addOnlyMode;
    
    // Initialize spatial index
    if (!spatialIndex) {
        spatialIndex = std::make_unique<NativeMarkerIndex>();
    }
    
    // Start batch processor
    startBatchProcessor();
    
    // Simulate service connection
    serviceConnected.store(true);
    
    LOGD("Session initialized: %s with expandMargin: %.2f, addOnly: %s", 
         sessionId.c_str(), expandMargin, addOnlyMode ? "true" : "false");
    
    return true;
}

void RemoteSpatialMarkerStrategy::destroySession() {
    std::lock_guard<std::mutex> lock(serviceConnectionMutex);
    
    // Stop batch processor
    stopBatchProcessor();
    
    // Clear all markers
    {
        std::unique_lock<std::mutex> markersLock(markersMutex);
        allMarkers.clear();
        renderedMarkers.clear();
    }
    
    // Clear spatial index
    if (spatialIndex) {
        spatialIndex->clear();
    }
    
    serviceConnected.store(false);
    
    LOGD("Session destroyed: %s", sessionId.c_str());
}

bool RemoteSpatialMarkerStrategy::addMarkers(const std::vector<MarkerDataDTO>& markers) {
    auto startTime = std::chrono::high_resolution_clock::now();
    
    try {
        {
            // Update logical store under lock
            std::unique_lock<std::mutex> lock(markersMutex);
            for (const auto& markerDTO : markers) {
                allMarkers[markerDTO.id] = markerDTO;
            }
            stats.currentMarkerCount.store(allMarkers.size());
            stats.totalMarkersProcessed += markers.size();
        }

        // Update spatial index outside of markersMutex to avoid lock-order inversion
        if (spatialIndex) {
            for (const auto& markerDTO : markers) {
                GeoPoint position(markerDTO.latitude, markerDTO.longitude);
                spatialIndex->registerMarker(markerDTO.id, position, markerDTO.clickable);
            }
        }
        
        auto endTime = std::chrono::high_resolution_clock::now();
        auto duration = std::chrono::duration_cast<std::chrono::microseconds>(endTime - startTime);
        
        LOGD("Added %zu markers in %.2f ms", markers.size(), duration.count() / 1000.0);
        
        return true;
    } catch (const std::exception& e) {
        LOGE("Failed to add markers: %s", e.what());
        return false;
    }
}

bool RemoteSpatialMarkerStrategy::updateMarker(const MarkerDataDTO& markerDTO) {
    try {
        bool existed = false;
        {
            std::unique_lock<std::mutex> lock(markersMutex);
            auto it = allMarkers.find(markerDTO.id);
            existed = (it != allMarkers.end());
            allMarkers[markerDTO.id] = markerDTO; // upsert
            stats.currentMarkerCount.store(allMarkers.size());
            stats.totalMarkersProcessed++;
        }

        // Update spatial index outside markersMutex
        if (spatialIndex) {
            GeoPoint position(markerDTO.latitude, markerDTO.longitude);
            if (existed) {
                spatialIndex->updateMarker(markerDTO.id, position, markerDTO.clickable);
            } else {
                spatialIndex->registerMarker(markerDTO.id, position, markerDTO.clickable);
            }
        }
        return true;
    } catch (const std::exception& e) {
        LOGE("Failed to update marker %s: %s", markerDTO.id.c_str(), e.what());
        return false;
    }
}

bool RemoteSpatialMarkerStrategy::removeMarker(const std::string& markerId) {
    try {
        bool existed = false;
        {
            std::unique_lock<std::mutex> lock(markersMutex);
            auto it = allMarkers.find(markerId);
            if (it != allMarkers.end()) {
                existed = true;
                // Remove from rendered set and logical collection
                renderedMarkers.erase(markerId);
                allMarkers.erase(it);
                stats.currentMarkerCount.store(allMarkers.size());
                stats.renderedMarkerCount.store(renderedMarkers.size());
            }
        }

        if (!existed) {
            LOGE("Marker not found for removal: %s", markerId.c_str());
            return false;
        }

        // Update spatial index outside markersMutex
        if (spatialIndex) {
            spatialIndex->removeMarker(markerId);
        }
        return true;
    } catch (const std::exception& e) {
        LOGE("Failed to remove marker %s: %s", markerId.c_str(), e.what());
        return false;
    }
}

SpatialResultDTO RemoteSpatialMarkerStrategy::processCameraChange(const CameraPosition& cameraPosition) {
    auto startTime = std::chrono::high_resolution_clock::now();
    
    SpatialResultDTO result;
    
    try {
        // Expand bounds for better performance
        GeoRectBounds expandedBounds = expandBounds(cameraPosition.visibleBounds, expandMargin);
        
        // Find markers in the expanded viewport using spatial index
        std::vector<std::string> markersInBounds = findMarkersInBounds(expandedBounds);
        
        std::lock_guard<std::mutex> lock(markersMutex);
        
        // Determine which markers should be added (in viewport but not rendered)
        for (const std::string& markerId : markersInBounds) {
            if (renderedMarkers.find(markerId) == renderedMarkers.end()) {
                result.markersToAdd.push_back(markerId);
            }
        }
        
        // Determine which markers should be removed (rendered but not in viewport)
        if (!addOnlyMode) {
            std::unordered_set<std::string> markersInBoundsSet(markersInBounds.begin(), markersInBounds.end());
            
            for (const std::string& renderedMarkerId : renderedMarkers) {
                if (markersInBoundsSet.find(renderedMarkerId) == markersInBoundsSet.end()) {
                    result.markersToRemove.push_back(renderedMarkerId);
                }
            }
        }
        
        // Update rendered markers tracking
        for (const std::string& markerId : result.markersToAdd) {
            const_cast<std::unordered_set<std::string>&>(renderedMarkers).insert(markerId);
        }
        
        for (const std::string& markerId : result.markersToRemove) {
            const_cast<std::unordered_set<std::string>&>(renderedMarkers).erase(markerId);
        }
        
        stats.renderedMarkerCount.store(renderedMarkers.size());
        stats.totalCameraChanges++;
        stats.totalSpatialQueries++;
        
        auto endTime = std::chrono::high_resolution_clock::now();
        auto duration = std::chrono::duration_cast<std::chrono::microseconds>(endTime - startTime);
        recordQueryTime(duration.count() / 1000.0);
        
        LOGD("Camera change processed: +%zu -%zu markers in %.2f ms", 
             result.markersToAdd.size(), result.markersToRemove.size(), duration.count() / 1000.0);
        
    } catch (const std::exception& e) {
        LOGE("Failed to process camera change: %s", e.what());
        result.errors.push_back(std::string("Camera change processing failed: ") + e.what());
    }
    
    return result;
}

std::vector<std::string> RemoteSpatialMarkerStrategy::findMarkersInBounds(const GeoRectBounds& bounds) const {
    prepareTemporaryVectors(1000); // Pre-allocate for expected results
    
    try {
        if (spatialIndex) {
            // Convert bounds to the format expected by spatial index
            // Note: spatial_utils.h might use different bounds format
            return spatialIndex->findMarkersInBounds(bounds);
        } else {
            // Fallback to linear search
            std::lock_guard<std::mutex> lock(markersMutex);
            
            tempMarkerIds.clear();
            tempMarkerIds.reserve(allMarkers.size() / 4); // Estimate 25% of markers in viewport
            
            for (const auto& pair : allMarkers) {
                if (isMarkerInBounds(pair.second, bounds)) {
                    tempMarkerIds.push_back(pair.first);
                }
            }
            
            return tempMarkerIds;
        }
    } catch (const std::exception& e) {
        LOGE("Failed to find markers in bounds: %s", e.what());
        clearTemporaryVectors();
        return {};
    }
}

std::string RemoteSpatialMarkerStrategy::findNearestMarker(double latitude, double longitude) const {
    try {
        if (spatialIndex) {
            GeoPoint position(latitude, longitude);
            return spatialIndex->findNearestMarker(position);
        } else {
            // Fallback to linear search
            std::lock_guard<std::mutex> lock(markersMutex);
            
            std::string nearestId;
            double minDistance = std::numeric_limits<double>::max();
            
            for (const auto& pair : allMarkers) {
                const MarkerDataDTO& marker = pair.second;
                // Simple distance calculation (squared for performance)
                double dlat = latitude - marker.latitude;
                double dlng = longitude - marker.longitude;
                double distance = dlat * dlat + dlng * dlng;
                
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestId = pair.first;
                }
            }
            
            return nearestId;
        }
    } catch (const std::exception& e) {
        LOGE("Failed to find nearest marker: %s", e.what());
        return "";
    }
}

void RemoteSpatialMarkerStrategy::addToBatch(const MarkerDataDTO& marker) {
    std::lock_guard<std::mutex> lock(batchMutex);
    
    pendingUpdates.push(marker);
    
    // If queue is getting large, force immediate processing
    if (pendingUpdates.size() >= MAX_BATCH_SIZE) {
        batchCondition.notify_one();
    }
}

void RemoteSpatialMarkerStrategy::processPendingUpdates() {
    auto startTime = std::chrono::high_resolution_clock::now();
    
    std::vector<MarkerDataDTO> batch;
    
    {
        std::lock_guard<std::mutex> lock(batchMutex);
        
        if (pendingUpdates.empty()) {
            return;
        }
        
        // Collect up to MAX_BATCH_SIZE updates
        size_t batchSize = std::min(pendingUpdates.size(), MAX_BATCH_SIZE);
        batch.reserve(batchSize);
        
        for (size_t i = 0; i < batchSize; ++i) {
            batch.push_back(pendingUpdates.front());
            pendingUpdates.pop();
        }
    }
    
    // Process the batch
    if (!batch.empty()) {
        for (const auto& marker : batch) {
            updateMarker(marker);
        }
        
        stats.totalBatchUpdates++;
        
        auto endTime = std::chrono::high_resolution_clock::now();
        auto duration = std::chrono::duration_cast<std::chrono::microseconds>(endTime - startTime);
        recordBatchProcessTime(duration.count() / 1000.0);
        
        LOGD("Processed batch of %zu markers in %.2f ms", batch.size(), duration.count() / 1000.0);
    }
}

PerformanceStatsSnapshot RemoteSpatialMarkerStrategy::getPerformanceStats() const {
    PerformanceStatsSnapshot result;
    result.totalCameraChanges = stats.totalCameraChanges.load();
    result.totalMarkersProcessed = stats.totalMarkersProcessed.load();
    result.totalSpatialQueries = stats.totalSpatialQueries.load();
    result.totalBatchUpdates = stats.totalBatchUpdates.load();
    result.averageQueryTimeMs = stats.averageQueryTimeMs.load();
    result.averageBatchProcessTimeMs = stats.averageBatchProcessTimeMs.load();
    result.currentMarkerCount = stats.currentMarkerCount.load();
    result.renderedMarkerCount = stats.renderedMarkerCount.load();
    return result;
}

void RemoteSpatialMarkerStrategy::resetPerformanceStats() {
    stats.reset();
}

size_t RemoteSpatialMarkerStrategy::getMarkerCount() const {
    return stats.currentMarkerCount.load();
}

size_t RemoteSpatialMarkerStrategy::getRenderedMarkerCount() const {
    return stats.renderedMarkerCount.load();
}

void RemoteSpatialMarkerStrategy::startBatchProcessor() {
    if (!batchProcessorRunning.load()) {
        batchProcessorRunning.store(true);
        batchProcessorThread = std::thread(&RemoteSpatialMarkerStrategy::batchProcessorLoop, this);
        LOGD("Batch processor started");
    }
}

void RemoteSpatialMarkerStrategy::stopBatchProcessor() {
    if (batchProcessorRunning.load()) {
        batchProcessorRunning.store(false);
        batchCondition.notify_all();
        
        if (batchProcessorThread.joinable()) {
            batchProcessorThread.join();
        }
        
        // Process any remaining updates
        processPendingUpdates();
        
        LOGD("Batch processor stopped");
    }
}

void RemoteSpatialMarkerStrategy::batchProcessorLoop() {
    while (batchProcessorRunning.load()) {
        std::unique_lock<std::mutex> lock(batchMutex);
        
        // Wait for batch delay or notification
        batchCondition.wait_for(lock, std::chrono::milliseconds(BATCH_DELAY_MS), 
            [this] { return !batchProcessorRunning.load() || pendingUpdates.size() >= MAX_BATCH_SIZE; });
        
        lock.unlock();
        
        if (batchProcessorRunning.load()) {
            processPendingUpdates();
        }
    }
}

GeoRectBounds RemoteSpatialMarkerStrategy::expandBounds(const GeoRectBounds& bounds, double margin) const {
    double latDelta = (bounds.maxLat - bounds.minLat) * margin;
    double lngDelta = (bounds.maxLng - bounds.minLng) * margin;
    
    return GeoRectBounds(
        bounds.minLat - latDelta,
        bounds.maxLat + latDelta,
        bounds.minLng - lngDelta,
        bounds.maxLng + lngDelta
    );
}

bool RemoteSpatialMarkerStrategy::isMarkerInBounds(const MarkerDataDTO& marker, const GeoRectBounds& bounds) const {
    return marker.latitude >= bounds.minLat && marker.latitude <= bounds.maxLat &&
           marker.longitude >= bounds.minLng && marker.longitude <= bounds.maxLng;
}

void RemoteSpatialMarkerStrategy::recordQueryTime(double timeMs) const {
    // Simple moving average
    double currentAvg = stats.averageQueryTimeMs.load();
    double newAvg = (currentAvg * 0.9) + (timeMs * 0.1);
    stats.averageQueryTimeMs.store(newAvg);
}

void RemoteSpatialMarkerStrategy::recordBatchProcessTime(double timeMs) const {
    // Simple moving average
    double currentAvg = stats.averageBatchProcessTimeMs.load();
    double newAvg = (currentAvg * 0.9) + (timeMs * 0.1);
    stats.averageBatchProcessTimeMs.store(newAvg);
}

void RemoteSpatialMarkerStrategy::prepareTemporaryVectors(size_t expectedSize) const {
    if (tempMarkerIds.capacity() < expectedSize) {
        tempMarkerIds.reserve(expectedSize);
    }
    if (tempMarkerData.capacity() < expectedSize) {
        tempMarkerData.reserve(expectedSize);
    }
}

void RemoteSpatialMarkerStrategy::clearTemporaryVectors() const {
    tempMarkerIds.clear();
    tempMarkerData.clear();
}

// Factory functions
std::unique_ptr<RemoteSpatialMarkerStrategy> createRemoteSpatialStrategy(
    const std::string& sessionId,
    double expandMargin,
    bool addOnlyMode
) {
    return std::make_unique<RemoteSpatialMarkerStrategy>(sessionId, expandMargin, addOnlyMode);
}

std::unique_ptr<RemoteSpatialMarkerStrategy> createHighPerformanceRemoteStrategy(
    const std::string& sessionId,
    double expandMargin,
    bool addOnlyMode
) {
    auto strategy = std::make_unique<RemoteSpatialMarkerStrategy>(sessionId, expandMargin, addOnlyMode);
    // Additional optimizations for high performance could be added here
    return strategy;
}

std::unique_ptr<RemoteSpatialMarkerStrategy> createLargeDatasetRemoteStrategy(
    const std::string& sessionId,
    double expandMargin,
    bool addOnlyMode
) {
    auto strategy = std::make_unique<RemoteSpatialMarkerStrategy>(sessionId, expandMargin, addOnlyMode);
    // Additional optimizations for large datasets could be added here
    return strategy;
}

} // namespace native
} // namespace mapconductor
