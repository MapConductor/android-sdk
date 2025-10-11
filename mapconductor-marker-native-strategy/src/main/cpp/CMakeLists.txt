cmake_minimum_required(VERSION 3.18.1)

project("mapconductor-native")

# Set C++ standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Add optimization flags
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -O3 -DNDEBUG")

# SIMD optimization flags
if(CMAKE_ANDROID_ARCH_ABI STREQUAL "arm64-v8a")
    # ARM NEON optimization (ARM64 has NEON by default)
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3 -ffast-math")
elseif(CMAKE_ANDROID_ARCH_ABI STREQUAL "armeabi-v7a")
    # ARM NEON optimization for 32-bit ARM
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -mfpu=neon")
elseif(CMAKE_ANDROID_ARCH_ABI STREQUAL "x86_64")
    # x86_64 AVX2 optimization (for emulator)
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -mavx2 -mfma")
endif()

# Add 16KB page alignment support for Android 15+ (API 35+)
# Required for devices with 16KB page sizes
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,-z,max-page-size=16384")
set(CMAKE_EXE_LINKER_FLAGS "${CMAKE_EXE_LINKER_FLAGS} -Wl,-z,max-page-size=16384")

# Find required packages
find_library(log-lib log)
find_library(gles3-lib GLESv3)
find_library(egl-lib EGL)

# Add our native library
add_library(
    mapconductor-native
    SHARED
    native_marker_index.cpp
    hex_geocell_native.cpp
    spatial_utils.cpp
    spatial_utils_extended.cpp
    marker_rendering_strategy.cpp
    parallel_marker_strategy.cpp
    jni_wrapper.cpp
    parallel_jni_wrapper.cpp
    remote_spatial_jni_wrapper.cpp
    remote_spatial_marker_strategy.cpp
)

# Include header directories
target_include_directories(
    mapconductor-native
    PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}
)

# Link libraries
target_link_libraries(
    mapconductor-native
    ${log-lib}
    ${gles3-lib}
    ${egl-lib}
)
