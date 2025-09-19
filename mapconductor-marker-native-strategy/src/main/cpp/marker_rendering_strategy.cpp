#include "marker_rendering_strategy.h"
#include "marker_manager.h"
#include "spatial_utils_extended.h"
#include <algorithm>
#include <mutex>

namespace mapconductor {
namespace marker {

template<typename ActualMarker>
void SimpleMarkerRenderingStrategy<ActualMarker>::onCameraChanged(
    const MapCameraPosition& cameraPosition,
    std::shared_ptr<MarkerManager<ActualMarker>> markerManager,
    std::shared_ptr<MarkerOverlayRenderer<ActualMarker>> renderer
) {
    std::lock_guard<std::mutex> lock(*this->semaphore_);
    
    // Simple strategy: just render all markers that aren't already rendered
    auto allMarkers = markerManager->allEntities();
    std::vector<std::shared_ptr<MarkerEntity<ActualMarker>>> markersToRender;
    
    for (auto& entity : allMarkers) {
        if (!entity->isRendered) {
            markersToRender.push_back(entity);
        }
    }
    
    if (!markersToRender.empty()) {
        ColorDefaultIcon defaultIcon;
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
                markersToRender[i]->visible = true;
            }
        }
        
        renderer->onPostProcess();
    }
}

template<typename ActualMarker>
void DefaultMarkerRenderingStrategy<ActualMarker>::onCameraChanged(
    const MapCameraPosition& cameraPosition,
    std::shared_ptr<MarkerManager<ActualMarker>> markerManager,
    std::shared_ptr<MarkerOverlayRenderer<ActualMarker>> renderer
) {
    std::lock_guard<std::mutex> lock(*this->semaphore_);
    
    if (!cameraPosition.visibleRegion) {
        return;
    }
    
    // Expand bounds by the specified margin for better performance
    auto expandedBounds = expandBounds(cameraPosition.visibleRegion->bounds, expandMargin_);
    
    // Get all entities and separate them by viewport status
    auto allMarkers = markerManager->allEntities();
    std::vector<std::shared_ptr<MarkerEntity<ActualMarker>>> markersToRender;
    std::vector<std::shared_ptr<MarkerEntity<ActualMarker>>> markersToRemove;
    
    for (auto& entity : allMarkers) {
        bool isInViewport = expandedBounds.contains(entity->state.position);
        
        if (isInViewport && !entity->isRendered) {
            // Marker entered viewport, need to render
            markersToRender.push_back(entity);
            entity->visible = true;
        } else if (!isInViewport && entity->isRendered) {
            // Marker left viewport, need to remove from rendering
            markersToRemove.push_back(entity);
            entity->visible = false;
        } else if (isInViewport) {
            // Marker is in viewport and already rendered
            entity->visible = true;
        } else {
            // Marker is outside viewport and not rendered
            entity->visible = false;
        }
    }
    
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

template<typename ActualMarker>
void AddOnlyMarkerRenderingStrategy<ActualMarker>::onCameraChanged(
    const MapCameraPosition& cameraPosition,
    std::shared_ptr<MarkerManager<ActualMarker>> markerManager,
    std::shared_ptr<MarkerOverlayRenderer<ActualMarker>> renderer
) {
    if (!cameraPosition.visibleRegion) {
        return;
    }
    
    auto viewportBounds = expandBounds(cameraPosition.visibleRegion->bounds, expandMargin_);
    
    // Find markers that need to be added to the viewport
    auto allEntities = markerManager->allEntities();
    std::vector<std::shared_ptr<MarkerEntity<ActualMarker>>> toAdd;
    
    for (auto& entity : allEntities) {
        if (viewportBounds.contains(entity->state.position) && 
            !entity->isRendered && !entity->marker) {
            toAdd.push_back(entity);
        }
    }
    
    if (!toAdd.empty()) {
        std::lock_guard<std::mutex> lock(*this->semaphore_);
        
        DefaultIcon defaultIcon;
        std::vector<AddParams> addParams;
        addParams.reserve(toAdd.size());
        
        for (auto& entity : toAdd) {
            BitmapIcon bitmapIcon = entity->state.icon ? 
                entity->state.icon->toBitmapIcon() : 
                defaultIcon.toBitmapIcon();
            addParams.emplace_back(entity->state, bitmapIcon);
        }
        
        auto newMarkers = renderer->onAdd(addParams);
        
        for (size_t i = 0; i < newMarkers.size() && i < toAdd.size(); ++i) {
            if (newMarkers[i]) {
                toAdd[i]->marker = newMarkers[i];
                toAdd[i]->isRendered = true;
            }
        }
        
        // Post-process for providers that need it (like Mapbox)
        renderer->onPostProcess();
    }
    
    // Update visibility flags for all entities based on viewport
    for (auto& entity : allEntities) {
        entity->visible = viewportBounds.contains(entity->state.position);
    }
}

template<typename ActualMarker>
void SpatialMarkerRenderingStrategy<ActualMarker>::onCameraChanged(
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
    
    // Get all entities and separate them by viewport status (similar to DefaultMarkerRenderingStrategy)
    auto allMarkers = markerManager->allEntities();
    std::vector<std::shared_ptr<MarkerEntity<ActualMarker>>> markersToRender;
    std::vector<std::shared_ptr<MarkerEntity<ActualMarker>>> markersToRemove;
    
    for (auto& entity : allMarkers) {
        bool isInViewport = expandedBounds.contains(entity->state.position);
        
        if (isInViewport && !entity->isRendered) {
            // Marker entered viewport, need to render
            markersToRender.push_back(entity);
            entity->visible = true;
        } else if (!isInViewport && entity->isRendered && !addOnlyMode_) {
            // Marker left viewport, need to remove from rendering (only in add/remove mode)
            markersToRemove.push_back(entity);
            entity->visible = false;
        } else if (isInViewport) {
            // Marker is in viewport and already rendered
            entity->visible = true;
        } else {
            // Marker is outside viewport and not rendered
            entity->visible = false;
        }
    }
    
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
template class SimpleMarkerRenderingStrategy<void*>;
template class DefaultMarkerRenderingStrategy<void*>;
template class AddOnlyMarkerRenderingStrategy<void*>;
template class SpatialMarkerRenderingStrategy<void*>;

} // namespace marker
} // namespace mapconductor