#include "parallel_marker_strategy.h"
#include "spatial_utils_extended.h"
#include <algorithm>
#include <future>
#include <chrono>
#include <queue>
#include <cmath>

namespace mapconductor {
namespace marker {

template<typename ActualMarker>
void ParallelMarkerRenderingStrategy<ActualMarker>::onCameraChanged(
    const MapCameraPosition& cameraPosition,
    std::shared_ptr<MarkerManager<ActualMarker>> markerManager,
    std::shared_ptr<MarkerOverlayRenderer<ActualMarker>> renderer
) {
    if (!cameraPosition.visibleRegion) {
        return;
    }

    std::lock_guard<std::mutex> lock(*this->semaphore_);

    // Expand bounds for better performance and smoother experience
    auto expandedBounds = expandBounds(cameraPosition.visibleRegion->bounds, expandMargin_);

    // Get all entities
    auto allMarkers = markerManager->allEntities();
    const size_t totalMarkers = allMarkers.size();

    // If dataset is small, use sequential processing to avoid thread overhead
    if (totalMarkers < minBatchSize_) {
        processSequentially(allMarkers, expandedBounds, renderer);
        return;
    }

    // Calculate optimal chunk size and number of threads to use
    const size_t numThreads = std::min(
        static_cast<size_t>(std::thread::hardware_concurrency()),
        (totalMarkers + minBatchSize_ - 1) / minBatchSize_
    );
    const size_t chunkSize = calculateChunkSize(totalMarkers, numThreads);

    // Create futures for parallel processing
    std::vector<std::future<VisibilityResult<ActualMarker>>> futures;
    futures.reserve(numThreads);

    // Submit chunks to thread pool
    for (size_t i = 0; i < totalMarkers; i += chunkSize) {
        const size_t endIdx = std::min(i + chunkSize, totalMarkers);

        auto future = threadPool_->enqueue([this, &allMarkers, i, endIdx, expandedBounds]() {
            return processMarkerChunk(allMarkers, i, endIdx, expandedBounds, addOnlyMode_);
        });

        futures.push_back(std::move(future));
    }

    // Collect results from all threads
    std::vector<std::shared_ptr<MarkerEntity<ActualMarker>>> markersToRender;
    std::vector<std::shared_ptr<MarkerEntity<ActualMarker>>> markersToRemove;

    for (auto& future : futures) {
        try {
            auto result = future.get();

            // Merge results efficiently
            markersToRender.insert(
                markersToRender.end(),
                std::make_move_iterator(result.markersToRender.begin()),
                std::make_move_iterator(result.markersToRender.end())
            );

            markersToRemove.insert(
                markersToRemove.end(),
                std::make_move_iterator(result.markersToRemove.begin()),
                std::make_move_iterator(result.markersToRemove.end())
            );
        } catch (const std::exception& e) {
            // Log error and continue with other results
            // In production, use proper logging framework
        }
    }

    // Process rendering operations
    processRenderingOperations(markersToRender, markersToRemove, renderer);
}

template<typename ActualMarker>
VisibilityResult<ActualMarker> ParallelMarkerRenderingStrategy<ActualMarker>::processMarkerChunk(
    const std::vector<std::shared_ptr<MarkerEntity<ActualMarker>>>& markers,
    size_t startIdx,
    size_t endIdx,
    const GeoRectBounds& expandedBounds,
    bool addOnlyMode
) {
    VisibilityResult<ActualMarker> result;

    // Reserve space to minimize allocations
    const size_t chunkSize = endIdx - startIdx;
    result.markersToRender.reserve(chunkSize / 4); // Estimate 25% visible
    result.markersToRemove.reserve(chunkSize / 8); // Estimate 12.5% to remove

    for (size_t i = startIdx; i < endIdx; ++i) {
        auto& entity = markers[i];
        bool isInViewport = expandedBounds.contains(entity->state.position);

        if (isInViewport && !entity->isRendered) {
            // Marker entered viewport, need to render
            result.markersToRender.push_back(entity);
            entity->visible = true;
        } else if (!isInViewport && entity->isRendered && !addOnlyMode) {
            // Marker left viewport, need to remove from rendering (only in add/remove mode)
            result.markersToRemove.push_back(entity);
            entity->visible = false;
        } else if (isInViewport) {
            // Marker is in viewport and already rendered
            entity->visible = true;
        } else {
            // Marker is outside viewport and not rendered
            entity->visible = false;
        }
    }

    return result;
}

template<typename ActualMarker>
size_t ParallelMarkerRenderingStrategy<ActualMarker>::calculateChunkSize(
    size_t totalMarkers,
    size_t numThreads
) const {
    // Calculate base chunk size
    size_t baseChunkSize = (totalMarkers + numThreads - 1) / numThreads;

    // Ensure minimum chunk size to avoid excessive thread overhead
    const size_t minChunkSize = std::max(minBatchSize_, static_cast<size_t>(100));

    // Ensure maximum chunk size to maintain good parallelization
    const size_t maxChunkSize = std::max(totalMarkers / 2, static_cast<size_t>(1000));

    return std::max(minChunkSize, std::min(baseChunkSize, maxChunkSize));
}

template<typename ActualMarker>
void ParallelMarkerRenderingStrategy<ActualMarker>::processSequentially(
    const std::vector<std::shared_ptr<MarkerEntity<ActualMarker>>>& allMarkers,
    const GeoRectBounds& expandedBounds,
    std::shared_ptr<MarkerOverlayRenderer<ActualMarker>> renderer
) {
    std::vector<std::shared_ptr<MarkerEntity<ActualMarker>>> markersToRender;
    std::vector<std::shared_ptr<MarkerEntity<ActualMarker>>> markersToRemove;

    for (auto& entity : allMarkers) {
        bool isInViewport = expandedBounds.contains(entity->state.position);

        if (isInViewport && !entity->isRendered) {
            markersToRender.push_back(entity);
            entity->visible = true;
        } else if (!isInViewport && entity->isRendered && !addOnlyMode_) {
            markersToRemove.push_back(entity);
            entity->visible = false;
        } else if (isInViewport) {
            entity->visible = true;
        } else {
            entity->visible = false;
        }
    }

    processRenderingOperations(markersToRender, markersToRemove, renderer);
}

template<typename ActualMarker>
void ParallelMarkerRenderingStrategy<ActualMarker>::processRenderingOperations(
    const std::vector<std::shared_ptr<MarkerEntity<ActualMarker>>>& markersToRender,
    const std::vector<std::shared_ptr<MarkerEntity<ActualMarker>>>& markersToRemove,
    std::shared_ptr<MarkerOverlayRenderer<ActualMarker>> renderer
) {
    // Remove markers that left the viewport
    if (!markersToRemove.empty()) {
        renderer->onRemove(markersToRemove);
        for (auto& entity : markersToRemove) {
            entity->isRendered = false;
            entity->marker = nullptr;
        }
    }

    // Add markers that entered the viewport
    if (!markersToRender.empty()) {
        DefaultIcon defaultIcon;
        std::vector<AddParams> addParams;
        addParams.reserve(markersToRender.size());

        for (auto& entity : markersToRender) {
            BitmapIcon bitmapIcon = entity->state.icon ?
                entity->state.icon->toBitmapIcon() :
                defaultIcon.toBitmapIcon();
            addParams.emplace_back(entity->state, bitmapIcon);
        }

        auto actualMarkers = renderer->onAdd(addParams);

        for (size_t i = 0; i < actualMarkers.size() && i < markersToRender.size(); ++i) {
            if (actualMarkers[i]) {
                markersToRender[i]->marker = actualMarkers[i];
                markersToRender[i]->isRendered = true;
            }
        }
    }

    if (!markersToRender.empty() || !markersToRemove.empty()) {
        renderer->onPostProcess();
    }
}

// Explicit template instantiation for common marker types
template class ParallelMarkerRenderingStrategy<void*>;

} // namespace marker
} // namespace mapconductor