cmake_minimum_required(VERSION 3.18.1)

project("mapconductor-native")

# Set C++ standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Add optimization flags
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -O3 -DNDEBUG")

# Add 16KB page alignment support for Android 15+ (API 35+)
# Required for devices with 16KB page sizes
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,-z,max-page-size=16384")
set(CMAKE_EXE_LINKER_FLAGS "${CMAKE_EXE_LINKER_FLAGS} -Wl,-z,max-page-size=16384")

# Find required packages
find_library(log-lib log)

# Add our native library
add_library(
    mapconductor-native
    SHARED
    native_marker_index.cpp
    hex_geocell_native.cpp
    spatial_utils.cpp
    jni_wrapper.cpp
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
)