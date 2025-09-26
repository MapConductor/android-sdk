#include <jni.h>
#include <string>
#include <vector>
#include <memory>
#include <unordered_map>
#include <mutex>
#include <android/log.h>
#include "remote_spatial_marker_strategy.h"

#define LOG_TAG "RemoteSpatialJNI"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

using namespace mapconductor::native;

// Global storage for strategy instances
static std::unordered_map<jlong, std::unique_ptr<RemoteSpatialMarkerStrategy>> strategies;
static std::mutex strategiesMutex;
static jlong nextStrategyId = 1;

// Helper functions for JNI type conversions
std::string jstringToString(JNIEnv* env, jstring jstr) {
    if (!jstr) return "";

    const char* chars = env->GetStringUTFChars(jstr, nullptr);
    std::string result(chars);
    env->ReleaseStringUTFChars(jstr, chars);
    return result;
}

jstring stringToJstring(JNIEnv* env, const std::string& str) {
    return env->NewStringUTF(str.c_str());
}

MarkerDataDTO jobjectToMarkerDataDTO(JNIEnv* env, jobject markerObj) {
    jclass markerClass = env->GetObjectClass(markerObj);

    jfieldID idField = env->GetFieldID(markerClass, "id", "Ljava/lang/String;");
    jfieldID latField = env->GetFieldID(markerClass, "latitude", "D");
    jfieldID lngField = env->GetFieldID(markerClass, "longitude", "D");
    jfieldID clickableField = env->GetFieldID(markerClass, "clickable", "Z");

    jstring jid = (jstring)env->GetObjectField(markerObj, idField);
    jdouble lat = env->GetDoubleField(markerObj, latField);
    jdouble lng = env->GetDoubleField(markerObj, lngField);
    jboolean clickable = env->GetBooleanField(markerObj, clickableField);

    std::string id = jstringToString(env, jid);
    env->DeleteLocalRef(jid);
    env->DeleteLocalRef(markerClass);

    return MarkerDataDTO(id, lat, lng, clickable);
}

jobject createMarkerDataDTO(JNIEnv* env, const MarkerDataDTO& marker) {
    jclass markerClass = env->FindClass("com/mapconductor/marker/nativestrategy/spatial/NativeMarkerDataDTO");
    jmethodID constructor = env->GetMethodID(markerClass, "<init>", "(Ljava/lang/String;DDZ)V");

    jstring jid = stringToJstring(env, marker.id);
    jobject result = env->NewObject(markerClass, constructor, jid, marker.latitude, marker.longitude, marker.clickable);

    env->DeleteLocalRef(jid);
    env->DeleteLocalRef(markerClass);
    return result;
}

CameraPosition jobjectToCameraPosition(JNIEnv* env, jobject cameraObj) {
    jclass cameraClass = env->GetObjectClass(cameraObj);

    jfieldID latField = env->GetFieldID(cameraClass, "latitude", "D");
    jfieldID lngField = env->GetFieldID(cameraClass, "longitude", "D");
    jfieldID zoomField = env->GetFieldID(cameraClass, "zoom", "D");
    jfieldID bearingField = env->GetFieldID(cameraClass, "bearing", "D");
    jfieldID tiltField = env->GetFieldID(cameraClass, "tilt", "D");
    jfieldID boundsField = env->GetFieldID(cameraClass, "visibleBounds", "Lcom/mapconductor/marker/strategy/spatial/GeoRectBounds;");

    jdouble lat = env->GetDoubleField(cameraObj, latField);
    jdouble lng = env->GetDoubleField(cameraObj, lngField);
    jdouble zoom = env->GetDoubleField(cameraObj, zoomField);
    jdouble bearing = env->GetDoubleField(cameraObj, bearingField);
    jdouble tilt = env->GetDoubleField(cameraObj, tiltField);
    jobject boundsObj = env->GetObjectField(cameraObj, boundsField);

    // Extract bounds
    jclass boundsClass = env->GetObjectClass(boundsObj);
    jfieldID southField = env->GetFieldID(boundsClass, "south", "D");
    jfieldID northField = env->GetFieldID(boundsClass, "north", "D");
    jfieldID westField = env->GetFieldID(boundsClass, "west", "D");
    jfieldID eastField = env->GetFieldID(boundsClass, "east", "D");

    jdouble south = env->GetDoubleField(boundsObj, southField);
    jdouble north = env->GetDoubleField(boundsObj, northField);
    jdouble west = env->GetDoubleField(boundsObj, westField);
    jdouble east = env->GetDoubleField(boundsObj, eastField);

    GeoRectBounds bounds(south, north, west, east); // minLat, maxLat, minLng, maxLng

    env->DeleteLocalRef(boundsObj);
    env->DeleteLocalRef(boundsClass);
    env->DeleteLocalRef(cameraClass);

    return CameraPosition(lat, lng, zoom, bearing, tilt, bounds);
}

jobject createSpatialResultDTO(JNIEnv* env, const SpatialResultDTO& result) {
    jclass resultClass = env->FindClass("com/mapconductor/marker/nativestrategy/spatial/NativeSpatialResultDTO");
    jmethodID constructor = env->GetMethodID(resultClass, "<init>", "()V");
    jobject resultObj = env->NewObject(resultClass, constructor);

    // Create string arrays
    jclass stringClass = env->FindClass("java/lang/String");

    jobjectArray markersToAdd = env->NewObjectArray(result.markersToAdd.size(), stringClass, nullptr);
    for (size_t i = 0; i < result.markersToAdd.size(); ++i) {
        jstring str = stringToJstring(env, result.markersToAdd[i]);
        env->SetObjectArrayElement(markersToAdd, i, str);
        env->DeleteLocalRef(str);
    }

    jobjectArray markersToRemove = env->NewObjectArray(result.markersToRemove.size(), stringClass, nullptr);
    for (size_t i = 0; i < result.markersToRemove.size(); ++i) {
        jstring str = stringToJstring(env, result.markersToRemove[i]);
        env->SetObjectArrayElement(markersToRemove, i, str);
        env->DeleteLocalRef(str);
    }

    jobjectArray errors = env->NewObjectArray(result.errors.size(), stringClass, nullptr);
    for (size_t i = 0; i < result.errors.size(); ++i) {
        jstring str = stringToJstring(env, result.errors[i]);
        env->SetObjectArrayElement(errors, i, str);
        env->DeleteLocalRef(str);
    }

    // Set fields
    jfieldID addField = env->GetFieldID(resultClass, "markersToAdd", "[Ljava/lang/String;");
    jfieldID removeField = env->GetFieldID(resultClass, "markersToRemove", "[Ljava/lang/String;");
    jfieldID errorsField = env->GetFieldID(resultClass, "errors", "[Ljava/lang/String;");

    env->SetObjectField(resultObj, addField, markersToAdd);
    env->SetObjectField(resultObj, removeField, markersToRemove);
    env->SetObjectField(resultObj, errorsField, errors);

    env->DeleteLocalRef(markersToAdd);
    env->DeleteLocalRef(markersToRemove);
    env->DeleteLocalRef(errors);
    env->DeleteLocalRef(stringClass);
    env->DeleteLocalRef(resultClass);

    return resultObj;
}

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_mapconductor_marker_nativestrategy_spatial_NativeRemoteSpatialMarkerStrategy_nativeCreate(
    JNIEnv* env, jclass clazz, jstring sessionId, jdouble expandMargin, jboolean addOnlyMode) {

    try {
        std::string sessionIdStr = jstringToString(env, sessionId);

        auto strategy = std::make_unique<RemoteSpatialMarkerStrategy>(
            sessionIdStr, expandMargin, addOnlyMode);

        std::lock_guard<std::mutex> lock(strategiesMutex);
        jlong id = nextStrategyId++;
        strategies[id] = std::move(strategy);

        LOGD("Created RemoteSpatialMarkerStrategy with ID: %lld", id);
        return id;

    } catch (const std::exception& e) {
        LOGE("Failed to create strategy: %s", e.what());
        return 0;
    }
}

JNIEXPORT jboolean JNICALL
Java_com_mapconductor_marker_nativestrategy_spatial_NativeRemoteSpatialMarkerStrategy_nativeInitializeSession(
    JNIEnv* env, jclass clazz, jlong strategyId, jdouble expandMargin, jboolean addOnlyMode) {

    try {
        std::lock_guard<std::mutex> lock(strategiesMutex);
        auto it = strategies.find(strategyId);
        if (it != strategies.end()) {
            SpatialConfigDTO config(expandMargin, addOnlyMode);
            return it->second->initializeSession(config);
        }
        return false;
    } catch (const std::exception& e) {
        LOGE("Failed to initialize session: %s", e.what());
        return false;
    }
}

JNIEXPORT void JNICALL
Java_com_mapconductor_marker_nativestrategy_spatial_NativeRemoteSpatialMarkerStrategy_nativeDestroySession(
    JNIEnv* env, jclass clazz, jlong strategyId) {

    try {
        std::lock_guard<std::mutex> lock(strategiesMutex);
        auto it = strategies.find(strategyId);
        if (it != strategies.end()) {
            it->second->destroySession();
            strategies.erase(it);
            LOGD("Destroyed strategy with ID: %lld", strategyId);
        }
    } catch (const std::exception& e) {
        LOGE("Failed to destroy session: %s", e.what());
    }
}

JNIEXPORT jboolean JNICALL
Java_com_mapconductor_marker_nativestrategy_spatial_NativeRemoteSpatialMarkerStrategy_nativeAddMarkers(
    JNIEnv* env, jclass clazz, jlong strategyId, jobjectArray markersArray) {

    try {
        std::lock_guard<std::mutex> lock(strategiesMutex);
        auto it = strategies.find(strategyId);
        if (it == strategies.end()) return false;

        jsize length = env->GetArrayLength(markersArray);
        std::vector<MarkerDataDTO> markers;
        markers.reserve(length);

        for (jsize i = 0; i < length; ++i) {
            jobject markerObj = env->GetObjectArrayElement(markersArray, i);
            markers.push_back(jobjectToMarkerDataDTO(env, markerObj));
            env->DeleteLocalRef(markerObj);
        }

        return it->second->addMarkers(markers);

    } catch (const std::exception& e) {
        LOGE("Failed to add markers: %s", e.what());
        return false;
    }
}

JNIEXPORT jboolean JNICALL
Java_com_mapconductor_marker_nativestrategy_spatial_NativeRemoteSpatialMarkerStrategy_nativeUpdateMarker(
    JNIEnv* env, jclass clazz, jlong strategyId, jobject markerObj) {

    try {
        std::lock_guard<std::mutex> lock(strategiesMutex);
        auto it = strategies.find(strategyId);
        if (it == strategies.end()) return false;

        MarkerDataDTO marker = jobjectToMarkerDataDTO(env, markerObj);
        return it->second->updateMarker(marker);

    } catch (const std::exception& e) {
        LOGE("Failed to update marker: %s", e.what());
        return false;
    }
}

JNIEXPORT jboolean JNICALL
Java_com_mapconductor_marker_nativestrategy_spatial_NativeRemoteSpatialMarkerStrategy_nativeRemoveMarker(
    JNIEnv* env, jclass clazz, jlong strategyId, jstring markerId) {

    try {
        std::lock_guard<std::mutex> lock(strategiesMutex);
        auto it = strategies.find(strategyId);
        if (it == strategies.end()) return false;

        std::string markerIdStr = jstringToString(env, markerId);
        return it->second->removeMarker(markerIdStr);

    } catch (const std::exception& e) {
        LOGE("Failed to remove marker: %s", e.what());
        return false;
    }
}

JNIEXPORT jobject JNICALL
Java_com_mapconductor_marker_nativestrategy_spatial_NativeRemoteSpatialMarkerStrategy_nativeProcessCameraChange(
    JNIEnv* env, jclass clazz, jlong strategyId, jobject cameraObj) {

    try {
        std::lock_guard<std::mutex> lock(strategiesMutex);
        auto it = strategies.find(strategyId);
        if (it == strategies.end()) return nullptr;

        CameraPosition camera = jobjectToCameraPosition(env, cameraObj);
        SpatialResultDTO result = it->second->processCameraChange(camera);

        return createSpatialResultDTO(env, result);

    } catch (const std::exception& e) {
        LOGE("Failed to process camera change: %s", e.what());
        return nullptr;
    }
}

JNIEXPORT jobjectArray JNICALL
Java_com_mapconductor_marker_nativestrategy_spatial_NativeRemoteSpatialMarkerStrategy_nativeFindMarkersInBounds(
    JNIEnv* env, jclass clazz, jlong strategyId, jobject boundsObj) {

    try {
        std::lock_guard<std::mutex> lock(strategiesMutex);
        auto it = strategies.find(strategyId);
        if (it == strategies.end()) return nullptr;

        // Extract bounds
        jclass boundsClass = env->GetObjectClass(boundsObj);
        jfieldID southField = env->GetFieldID(boundsClass, "south", "D");
        jfieldID northField = env->GetFieldID(boundsClass, "north", "D");
        jfieldID westField = env->GetFieldID(boundsClass, "west", "D");
        jfieldID eastField = env->GetFieldID(boundsClass, "east", "D");

        jdouble south = env->GetDoubleField(boundsObj, southField);
        jdouble north = env->GetDoubleField(boundsObj, northField);
        jdouble west = env->GetDoubleField(boundsObj, westField);
        jdouble east = env->GetDoubleField(boundsObj, eastField);

        GeoRectBounds bounds(south, north, west, east); // minLat, maxLat, minLng, maxLng
        std::vector<std::string> markerIds = it->second->findMarkersInBounds(bounds);

        // Create string array
        jclass stringClass = env->FindClass("java/lang/String");
        jobjectArray result = env->NewObjectArray(markerIds.size(), stringClass, nullptr);

        for (size_t i = 0; i < markerIds.size(); ++i) {
            jstring str = stringToJstring(env, markerIds[i]);
            env->SetObjectArrayElement(result, i, str);
            env->DeleteLocalRef(str);
        }

        env->DeleteLocalRef(stringClass);
        env->DeleteLocalRef(boundsClass);
        return result;

    } catch (const std::exception& e) {
        LOGE("Failed to find markers in bounds: %s", e.what());
        return nullptr;
    }
}

JNIEXPORT jstring JNICALL
Java_com_mapconductor_marker_nativestrategy_spatial_NativeRemoteSpatialMarkerStrategy_nativeFindNearestMarker(
    JNIEnv* env, jclass clazz, jlong strategyId, jdouble latitude, jdouble longitude) {

    try {
        std::lock_guard<std::mutex> lock(strategiesMutex);
        auto it = strategies.find(strategyId);
        if (it == strategies.end()) return nullptr;

        std::string nearestId = it->second->findNearestMarker(latitude, longitude);
        return nearestId.empty() ? nullptr : stringToJstring(env, nearestId);

    } catch (const std::exception& e) {
        LOGE("Failed to find nearest marker: %s", e.what());
        return nullptr;
    }
}

JNIEXPORT void JNICALL
Java_com_mapconductor_marker_nativestrategy_spatial_NativeRemoteSpatialMarkerStrategy_nativeAddToBatch(
    JNIEnv* env, jclass clazz, jlong strategyId, jobject markerObj) {

    try {
        std::lock_guard<std::mutex> lock(strategiesMutex);
        auto it = strategies.find(strategyId);
        if (it == strategies.end()) return;

        MarkerDataDTO marker = jobjectToMarkerDataDTO(env, markerObj);
        it->second->addToBatch(marker);

    } catch (const std::exception& e) {
        LOGE("Failed to add to batch: %s", e.what());
    }
}

JNIEXPORT jlong JNICALL
Java_com_mapconductor_marker_nativestrategy_spatial_NativeRemoteSpatialMarkerStrategy_nativeGetMarkerCount(
    JNIEnv* env, jclass clazz, jlong strategyId) {

    try {
        std::lock_guard<std::mutex> lock(strategiesMutex);
        auto it = strategies.find(strategyId);
        if (it == strategies.end()) return 0;

        return static_cast<jlong>(it->second->getMarkerCount());

    } catch (const std::exception& e) {
        LOGE("Failed to get marker count: %s", e.what());
        return 0;
    }
}

JNIEXPORT jlong JNICALL
Java_com_mapconductor_marker_nativestrategy_spatial_NativeRemoteSpatialMarkerStrategy_nativeGetRenderedMarkerCount(
    JNIEnv* env, jclass clazz, jlong strategyId) {

    try {
        std::lock_guard<std::mutex> lock(strategiesMutex);
        auto it = strategies.find(strategyId);
        if (it == strategies.end()) return 0;

        return static_cast<jlong>(it->second->getRenderedMarkerCount());

    } catch (const std::exception& e) {
        LOGE("Failed to get rendered marker count: %s", e.what());
        return 0;
    }
}

JNIEXPORT jstring JNICALL
Java_com_mapconductor_marker_nativestrategy_spatial_NativeRemoteSpatialMarkerStrategy_nativeGetPerformanceStats(
    JNIEnv* env, jclass clazz, jlong strategyId) {

    try {
        std::lock_guard<std::mutex> lock(strategiesMutex);
        auto it = strategies.find(strategyId);
        if (it == strategies.end()) return nullptr;

        PerformanceStatsSnapshot stats = it->second->getPerformanceStats();

        std::string statsStr =
            "totalCameraChanges: " + std::to_string(stats.totalCameraChanges) + "\n" +
            "totalMarkersProcessed: " + std::to_string(stats.totalMarkersProcessed) + "\n" +
            "totalSpatialQueries: " + std::to_string(stats.totalSpatialQueries) + "\n" +
            "totalBatchUpdates: " + std::to_string(stats.totalBatchUpdates) + "\n" +
            "averageQueryTimeMs: " + std::to_string(stats.averageQueryTimeMs) + "\n" +
            "averageBatchProcessTimeMs: " + std::to_string(stats.averageBatchProcessTimeMs) + "\n" +
            "currentMarkerCount: " + std::to_string(stats.currentMarkerCount) + "\n" +
            "renderedMarkerCount: " + std::to_string(stats.renderedMarkerCount);

        return stringToJstring(env, statsStr);

    } catch (const std::exception& e) {
        LOGE("Failed to get performance stats: %s", e.what());
        return nullptr;
    }
}

} // extern "C"
