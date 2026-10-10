#!/usr/bin/env bash
set -euo pipefail
root="$(cd "$(dirname "$0")/../.." && pwd)"
sdk="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-$HOME/Library/Android/sdk}}"
checkout="$root/example-app/build/omm-source"
ndk="$sdk/ndk/${NDK_VERSION:-27.1.12297006}"
cmake="$sdk/cmake/${CMAKE_VERSION:-3.22.1}/bin"
if [[ ! -d "$checkout/.git" ]]; then
    git clone https://github.com/openmobilemaps/maps-core.git "$checkout"
    git -C "$checkout" checkout 82c1fac
fi
if [[ "$(git -C "$checkout" rev-parse --short=7 HEAD)" != 82c1fac ]]; then
    echo "Expected OMM commit 82c1fac in $checkout" >&2; exit 1
fi
git -C "$checkout" submodule update --init --recursive
patch="$root/example-app/omm-native/flat-raster-seams.patch"
if ! git -C "$checkout" apply --reverse --check "$patch" 2>/dev/null; then
    git -C "$checkout" apply --check "$patch"
    git -C "$checkout" apply "$patch"
fi
native_build="$root/example-app/build/omm-native"
"$cmake/cmake" -S "$checkout/android" -B "$native_build" -G Ninja \
    -DCMAKE_TOOLCHAIN_FILE="$ndk/build/cmake/android.toolchain.cmake" \
    -DANDROID_ABI=arm64-v8a -DANDROID_PLATFORM=android-28 -DANDROID_STL=c++_shared \
    -DANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES=ON -DCMAKE_BUILD_TYPE=Release \
    -DCMAKE_MAKE_PROGRAM="$cmake/ninja"
"$cmake/cmake" --build "$native_build" --target mapscore -j 8
dest="$root/example-app/src/main/jniLibs/arm64-v8a"
mkdir -p "$dest"
cp "$native_build/libmapscore.so" "$dest/libmapscore.so"
host="$(uname -s | tr '[:upper:]' '[:lower:]')-x86_64"
"$ndk/toolchains/llvm/prebuilt/$host/bin/llvm-strip" --strip-unneeded "$dest/libmapscore.so"
