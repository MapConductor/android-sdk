#include <jni.h>
#include <string>
#include <memory>
#include <android/log.h>
#include "native_marker_index.h"

#define LOG_TAG "MapConductorNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Global reference to store native instances
static std::unordered_map<jlong, std::unique_ptr<NativeMarkerIndex>> g_nativeInstances;
static jlong g_nextHandle = 1;

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_mapconductor_core_marker_NativeMarkerIndex_nativeCreate(JNIEnv *env, jclass clazz, jint baseHexSideLength, jdouble zoom) {
    try {
        auto instance = std::make_unique<NativeMarkerIndex>(baseHexSideLength, zoom);
        jlong handle = g_nextHandle++;
        g_nativeInstances[handle] = std::move(instance);
        LOGI("Created native marker index with handle: %lld", handle);
        return handle;
    } catch (const std::exception& e) {
        LOGE("Failed to create native marker index: %s", e.what());
        return 0;
    }
}

JNIEXPORT void JNICALL
Java_com_mapconductor_core_marker_NativeMarkerIndex_nativeDestroy(JNIEnv *env, jclass clazz, jlong handle) {
    auto it = g_nativeInstances.find(handle);
    if (it != g_nativeInstances.end()) {
        g_nativeInstances.erase(it);
        LOGI("Destroyed native marker index with handle: %lld", handle);
    }
}

JNIEXPORT void JNICALL
Java_com_mapconductor_core_marker_NativeMarkerIndex_nativeRegisterMarker(JNIEnv *env, jclass clazz, jlong handle, jstring jId, jdouble latitude, jdouble longitude, jboolean clickable) {
    auto it = g_nativeInstances.find(handle);
    if (it == g_nativeInstances.end()) {
        LOGE("Invalid handle: %lld", handle);
        return;
    }
    
    const char* idCStr = env->GetStringUTFChars(jId, nullptr);
    std::string id(idCStr);
    env->ReleaseStringUTFChars(jId, idCStr);
    
    GeoPoint position(latitude, longitude);
    it->second->registerMarker(id, position, clickable == JNI_TRUE);
}

JNIEXPORT void JNICALL
Java_com_mapconductor_core_marker_NativeMarkerIndex_nativeUpdateMarker(JNIEnv *env, jclass clazz, jlong handle, jstring jId, jdouble latitude, jdouble longitude, jboolean clickable) {
    auto it = g_nativeInstances.find(handle);
    if (it == g_nativeInstances.end()) {
        LOGE("Invalid handle: %lld", handle);
        return;
    }
    
    const char* idCStr = env->GetStringUTFChars(jId, nullptr);
    std::string id(idCStr);
    env->ReleaseStringUTFChars(jId, idCStr);
    
    GeoPoint position(latitude, longitude);
    it->second->updateMarker(id, position, clickable == JNI_TRUE);
}

JNIEXPORT jboolean JNICALL
Java_com_mapconductor_core_marker_NativeMarkerIndex_nativeRemoveMarker(JNIEnv *env, jclass clazz, jlong handle, jstring jId) {
    auto it = g_nativeInstances.find(handle);
    if (it == g_nativeInstances.end()) {
        LOGE("Invalid handle: %lld", handle);
        return JNI_FALSE;
    }
    
    const char* idCStr = env->GetStringUTFChars(jId, nullptr);
    std::string id(idCStr);
    env->ReleaseStringUTFChars(jId, idCStr);
    
    bool removed = it->second->removeMarker(id);
    return removed ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com_mapconductor_core_marker_NativeMarkerIndex_nativeHasMarker(JNIEnv *env, jclass clazz, jlong handle, jstring jId) {
    auto it = g_nativeInstances.find(handle);
    if (it == g_nativeInstances.end()) {
        LOGE("Invalid handle: %lld", handle);
        return JNI_FALSE;
    }
    
    const char* idCStr = env->GetStringUTFChars(jId, nullptr);
    std::string id(idCStr);
    env->ReleaseStringUTFChars(jId, idCStr);
    
    bool exists = it->second->hasMarker(id);
    return exists ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jstring JNICALL
Java_com_mapconductor_core_marker_NativeMarkerIndex_nativeFindNearest(JNIEnv *env, jclass clazz, jlong handle, jdouble latitude, jdouble longitude) {
    auto it = g_nativeInstances.find(handle);
    if (it == g_nativeInstances.end()) {
        LOGE("Invalid handle: %lld", handle);
        return nullptr;
    }
    
    GeoPoint position(latitude, longitude);
    std::string nearestId = it->second->findNearestMarker(position);
    
    if (nearestId.empty()) {
        return nullptr;
    }
    
    return env->NewStringUTF(nearestId.c_str());
}

JNIEXPORT jobjectArray JNICALL
Java_com_mapconductor_core_marker_NativeMarkerIndex_nativeFindMarkersInBounds(JNIEnv *env, jclass clazz, jlong handle, jdouble minLat, jdouble maxLat, jdouble minLng, jdouble maxLng) {
    auto it = g_nativeInstances.find(handle);
    if (it == g_nativeInstances.end()) {
        LOGE("Invalid handle: %lld", handle);
        return nullptr;
    }
    
    GeoRectBounds bounds(minLat, maxLat, minLng, maxLng);
    std::vector<std::string> markerIds = it->second->findMarkersInBounds(bounds);
    
    jclass stringClass = env->FindClass("java/lang/String");
    jobjectArray result = env->NewObjectArray(markerIds.size(), stringClass, nullptr);
    
    for (size_t i = 0; i < markerIds.size(); ++i) {
        jstring jStr = env->NewStringUTF(markerIds[i].c_str());
        env->SetObjectArrayElement(result, i, jStr);
        env->DeleteLocalRef(jStr);
    }
    
    return result;
}

JNIEXPORT jobjectArray JNICALL
Java_com_mapconductor_core_marker_NativeMarkerIndex_nativeFindByIdPrefix(JNIEnv *env, jclass clazz, jlong handle, jstring jPrefix) {
    auto it = g_nativeInstances.find(handle);
    if (it == g_nativeInstances.end()) {
        LOGE("Invalid handle: %lld", handle);
        return nullptr;
    }
    
    const char* prefixCStr = env->GetStringUTFChars(jPrefix, nullptr);
    std::string prefix(prefixCStr);
    env->ReleaseStringUTFChars(jPrefix, prefixCStr);
    
    std::vector<std::string> markerIds = it->second->findByIdPrefix(prefix);
    
    jclass stringClass = env->FindClass("java/lang/String");
    jobjectArray result = env->NewObjectArray(markerIds.size(), stringClass, nullptr);
    
    for (size_t i = 0; i < markerIds.size(); ++i) {
        jstring jStr = env->NewStringUTF(markerIds[i].c_str());
        env->SetObjectArrayElement(result, i, jStr);
        env->DeleteLocalRef(jStr);
    }
    
    return result;
}

JNIEXPORT void JNICALL
Java_com_mapconductor_core_marker_NativeMarkerIndex_nativeClear(JNIEnv *env, jclass clazz, jlong handle) {
    auto it = g_nativeInstances.find(handle);
    if (it == g_nativeInstances.end()) {
        LOGE("Invalid handle: %lld", handle);
        return;
    }
    
    it->second->clear();
}

JNIEXPORT jlong JNICALL
Java_com_mapconductor_core_marker_NativeMarkerIndex_nativeMarkerCount(JNIEnv *env, jclass clazz, jlong handle) {
    auto it = g_nativeInstances.find(handle);
    if (it == g_nativeInstances.end()) {
        LOGE("Invalid handle: %lld", handle);
        return 0;
    }
    
    return static_cast<jlong>(it->second->markerCount());
}

JNIEXPORT jdouble JNICALL
Java_com_mapconductor_core_marker_NativeMarkerIndex_nativeMetersPerPixel(JNIEnv *env, jclass clazz, jlong handle, jdouble latitude, jdouble longitude, jdouble zoom, jdouble pixels, jint tileSize) {
    auto it = g_nativeInstances.find(handle);
    if (it == g_nativeInstances.end()) {
        LOGE("Invalid handle: %lld", handle);
        return 0.0;
    }
    
    GeoPoint position(latitude, longitude);
    return it->second->metersPerPixel(position, zoom, pixels, tileSize);
}

} // extern "C"