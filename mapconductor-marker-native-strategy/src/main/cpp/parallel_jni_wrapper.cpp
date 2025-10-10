#include <jni.h>
#include <string>
#include <memory>
#include <unordered_map>
#include <android/log.h>
#include "parallel_marker_strategy.h"
#include "marker_manager.h"
#include "native_marker_index.h"

#define LOG_TAG "MapConductorParallel"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

using namespace mapconductor::marker;
using MarkerGeoPoint = mapconductor::marker::GeoPoint;
using MarkerGeoRectBounds = mapconductor::marker::GeoRectBounds;

// Global storage for strategy instances
static std::unordered_map<jlong, std::unique_ptr<ParallelMarkerRenderingStrategy<void*>>> g_parallelStrategies;
static std::unordered_map<jlong, std::shared_ptr<MarkerManager<void*>>> g_markerManagers;
static jlong g_nextParallelHandle = 1000;

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_mapconductor_marker_nativestrategy_NativeParallelMarkerStrategy_nativeCreateStrategy(
    JNIEnv *env, jclass clazz, jdouble expandMargin, jboolean addOnlyMode, jint minBatchSize) {
    try {
        auto semaphore = std::make_shared<std::mutex>();
        auto strategy = std::make_unique<ParallelMarkerRenderingStrategy<void*>>(
            semaphore, expandMargin, addOnlyMode == JNI_TRUE, static_cast<size_t>(minBatchSize)
        );
        
        jlong handle = g_nextParallelHandle++;
        g_parallelStrategies[handle] = std::move(strategy);
        
        // Also create a marker manager for this strategy
        auto markerManager = std::make_shared<MarkerManager<void*>>();
        g_markerManagers[handle] = markerManager;
        
        LOGI("Created parallel strategy with handle: %ld", (long)handle);
        return handle;
    } catch (const std::exception& e) {
        LOGE("Failed to create parallel strategy: %s", e.what());
        return 0;
    }
}

JNIEXPORT void JNICALL
Java_com_mapconductor_marker_nativestrategy_NativeParallelMarkerStrategy_nativeDestroyStrategy(
    JNIEnv *env, jclass clazz, jlong handle) {
    auto strategyIt = g_parallelStrategies.find(handle);
    if (strategyIt != g_parallelStrategies.end()) {
        g_parallelStrategies.erase(strategyIt);
    }
    
    auto managerIt = g_markerManagers.find(handle);
    if (managerIt != g_markerManagers.end()) {
        g_markerManagers.erase(managerIt);
    }
    
    LOGI("Destroyed parallel strategy with handle: %ld", (long)handle);
}

JNIEXPORT void JNICALL
Java_com_mapconductor_marker_nativestrategy_NativeParallelMarkerStrategy_nativeAddMarker(
    JNIEnv *env, jclass clazz, jlong handle, jstring jId, jdouble latitude, jdouble longitude) {
    auto managerIt = g_markerManagers.find(handle);
    if (managerIt == g_markerManagers.end()) {
        LOGE("Invalid handle for addMarker: %ld", (long)handle);
        return;
    }
    
    const char* idCStr = env->GetStringUTFChars(jId, nullptr);
    std::string id(idCStr);
    env->ReleaseStringUTFChars(jId, idCStr);
    
    MarkerGeoPoint position(latitude, longitude);
    MarkerState state(id, position);
    
    // Add marker to the manager
    auto entity = std::make_shared<MarkerEntity<void*>>(state);
    managerIt->second->registerEntity(entity);
}

JNIEXPORT void JNICALL
Java_com_mapconductor_marker_nativestrategy_NativeParallelMarkerStrategy_nativeRemoveMarker(
    JNIEnv *env, jclass clazz, jlong handle, jstring jId) {
    auto managerIt = g_markerManagers.find(handle);
    if (managerIt == g_markerManagers.end()) {
        LOGE("Invalid handle for removeMarker: %ld", (long)handle);
        return;
    }
    
    const char* idCStr = env->GetStringUTFChars(jId, nullptr);
    std::string id(idCStr);
    env->ReleaseStringUTFChars(jId, idCStr);
    
    managerIt->second->removeEntity(id);
}

JNIEXPORT void JNICALL
Java_com_mapconductor_marker_nativestrategy_NativeParallelMarkerStrategy_nativeClearMarkers(
    JNIEnv *env, jclass clazz, jlong handle) {
    auto managerIt = g_markerManagers.find(handle);
    if (managerIt == g_markerManagers.end()) {
        LOGE("Invalid handle for clearMarkers: %ld", (long)handle);
        return;
    }
    
    managerIt->second->clear();
}

JNIEXPORT jlong JNICALL
Java_com_mapconductor_marker_nativestrategy_NativeParallelMarkerStrategy_nativeGetMarkerCount(
    JNIEnv *env, jclass clazz, jlong handle) {
    auto managerIt = g_markerManagers.find(handle);
    if (managerIt == g_markerManagers.end()) {
        LOGE("Invalid handle for getMarkerCount: %ld", (long)handle);
        return 0;
    }
    
    return static_cast<jlong>(managerIt->second->allEntities().size());
}

JNIEXPORT jobjectArray JNICALL
Java_com_mapconductor_marker_nativestrategy_NativeParallelMarkerStrategy_nativeProcessCameraChange(
    JNIEnv *env, jclass clazz, jlong handle, jdouble minLat, jdouble maxLat, jdouble minLng, jdouble maxLng) {
    auto strategyIt = g_parallelStrategies.find(handle);
    auto managerIt = g_markerManagers.find(handle);
    
    if (strategyIt == g_parallelStrategies.end() || managerIt == g_markerManagers.end()) {
        LOGE("Invalid handle for processCameraChange: %ld", (long)handle);
        return nullptr;
    }
    
    // Create camera position from bounds
    MarkerGeoRectBounds geoBounds(MarkerGeoPoint(maxLat, maxLng), MarkerGeoPoint(minLat, minLng));
    auto visibleRegion = std::make_shared<VisibleRegion>(geoBounds);
    MapCameraPosition cameraPosition(visibleRegion);
    
    // Create a simple renderer that just collects marker IDs
    struct SimpleRenderer : public MarkerOverlayRenderer<void*> {
        std::vector<std::string> addedMarkerIds;
        std::vector<std::string> removedMarkerIds;
        
        std::vector<std::shared_ptr<void*>> onAdd(const std::vector<AddParams>& params) override {
            std::vector<std::shared_ptr<void*>> result;
            for (const auto& param : params) {
                addedMarkerIds.push_back(param.state.id);
                result.push_back(std::make_shared<void*>(nullptr)); // Dummy marker
            }
            return result;
        }
        
        void onRemove(const std::vector<std::shared_ptr<MarkerEntity<void*>>>& entities) override {
            for (const auto& entity : entities) {
                removedMarkerIds.push_back(entity->state.id);
            }
        }
        
        std::vector<std::shared_ptr<void*>> onChange(const std::vector<ChangeParams<void*>>& params) override {
            // Not implemented for this example
            return std::vector<std::shared_ptr<void*>>();
        }
        
        void onPostProcess() override {
            // Nothing to do
        }
    };
    
    auto renderer = std::make_shared<SimpleRenderer>();
    
    // Process camera change
    strategyIt->second->onCameraChanged(cameraPosition, managerIt->second, renderer);
    
    // Return the added marker IDs as a string array
    jclass stringClass = env->FindClass("java/lang/String");
    jobjectArray result = env->NewObjectArray(renderer->addedMarkerIds.size(), stringClass, nullptr);
    
    for (size_t i = 0; i < renderer->addedMarkerIds.size(); ++i) {
        jstring jStr = env->NewStringUTF(renderer->addedMarkerIds[i].c_str());
        env->SetObjectArrayElement(result, i, jStr);
        env->DeleteLocalRef(jStr);
    }
    
    return result;
}

} // extern "C"
