# The JNI entry points are resolved by name from native code, so R8 must not
# rename or strip them.
-keepclasseswithmembernames class com.mapconductor.vectortile.NativeRenderer {
    native <methods>;
}
