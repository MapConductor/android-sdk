#pragma once

#include "marker_types.h"
#include <unordered_map>
#include <vector>
#include <memory>
#include <mutex>

namespace mapconductor {
namespace marker {

/**
 * Manages marker entities and their lifecycle.
 * Provides thread-safe operations for registering, retrieving, and removing markers.
 */
template<typename ActualMarker>
class MarkerManager {
public:
    MarkerManager() = default;
    ~MarkerManager() = default;
    
    /**
     * Register a marker entity with the manager.
     */
    void registerEntity(std::shared_ptr<MarkerEntity<ActualMarker>> entity) {
        std::lock_guard<std::mutex> lock(mutex_);
        entities_[entity->state.id] = entity;
    }
    
    /**
     * Get a marker entity by ID.
     */
    std::shared_ptr<MarkerEntity<ActualMarker>> getEntity(const std::string& id) {
        std::lock_guard<std::mutex> lock(mutex_);
        auto it = entities_.find(id);
        return (it != entities_.end()) ? it->second : nullptr;
    }
    
    /**
     * Check if an entity with the given ID exists.
     */
    bool hasEntity(const std::string& id) const {
        std::lock_guard<std::mutex> lock(mutex_);
        return entities_.find(id) != entities_.end();
    }
    
    /**
     * Remove a marker entity by ID.
     */
    std::shared_ptr<MarkerEntity<ActualMarker>> removeEntity(const std::string& id) {
        std::lock_guard<std::mutex> lock(mutex_);
        auto it = entities_.find(id);
        if (it != entities_.end()) {
            auto entity = it->second;
            entities_.erase(it);
            return entity;
        }
        return nullptr;
    }
    
    /**
     * Get all marker entities.
     */
    std::vector<std::shared_ptr<MarkerEntity<ActualMarker>>> allEntities() const {
        std::lock_guard<std::mutex> lock(mutex_);
        std::vector<std::shared_ptr<MarkerEntity<ActualMarker>>> result;
        result.reserve(entities_.size());
        
        for (const auto& pair : entities_) {
            result.push_back(pair.second);
        }
        return result;
    }
    
    /**
     * Get the number of entities managed.
     */
    size_t size() const {
        std::lock_guard<std::mutex> lock(mutex_);
        return entities_.size();
    }
    
    /**
     * Clear all entities.
     */
    void clear() {
        std::lock_guard<std::mutex> lock(mutex_);
        entities_.clear();
    }

private:
    mutable std::mutex mutex_;
    std::unordered_map<std::string, std::shared_ptr<MarkerEntity<ActualMarker>>> entities_;
};

/**
 * Interface for rendering marker overlays on a map.
 * Provides methods for adding, removing, and updating markers.
 */
template<typename ActualMarker>
class MarkerOverlayRenderer {
public:
    virtual ~MarkerOverlayRenderer() = default;
    
    /**
     * Add new markers to the map.
     * Returns a list of actual marker objects created by the map provider.
     */
    virtual std::vector<std::shared_ptr<ActualMarker>> onAdd(
        const std::vector<AddParams>& params) = 0;
    
    /**
     * Remove markers from the map.
     */
    virtual void onRemove(
        const std::vector<std::shared_ptr<MarkerEntity<ActualMarker>>>& entities) = 0;
    
    /**
     * Update existing markers on the map.
     * Returns a list of updated actual marker objects.
     */
    virtual std::vector<std::shared_ptr<ActualMarker>> onChange(
        const std::vector<ChangeParams<ActualMarker>>& params) = 0;
    
    /**
     * Post-processing after marker operations.
     * Can be used for batch updates or optimization.
     */
    virtual void onPostProcess() = 0;
};

} // namespace marker
} // namespace mapconductor