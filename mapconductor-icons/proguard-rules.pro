# MapConductor Icons ProGuard Rules

# Keep line number information for debugging
-keepattributes SourceFile,LineNumberTable

# Keep all public icon classes
-keep public class com.mapconductor.icons.** { public *; }

# Keep all icon implementations
-keep class com.mapconductor.icons.CircleIcon { *; }
-keep class com.mapconductor.icons.FlagIcon { *; }
-keep class com.mapconductor.icons.RoundInfoBubbleIcon { *; }

# Compose integration
-keep class * extends androidx.compose.runtime.** { *; }

# Fix for Java 11+ StringConcatFactory issue
-dontwarn java.lang.invoke.StringConcatFactory
-keep class java.lang.invoke.StringConcatFactory { *; }
