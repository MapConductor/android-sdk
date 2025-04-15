# GeoPoint クラスとそのメンバーを保持
-keep class com.mapconductor.here.GeoPoint { *; }
# GeoPoint のコンパニオンオブジェクト内のメソッドも保持する場合（例: fromLatLng）
# 通常、GeoPoint自体をkeepすれば companion object も保持されることが多いが、明示も可能
-keepclassmembers class com.mapconductor.here.GeoPoint$Companion {
    public static com.mapconductor.here.GeoPoint fromLatLong(double, double);
    public static com.mapconductor.here.GeoPoint fromLongLat(double, double);
}


# MapCameraPosition クラスとそのメンバーを保持
# @JvmOverloads で生成されたコンストラクタも暗黙的に保持される
-keep class com.mapconductor.here.MapCameraPosition { *; }

# データクラスのフィールド名を保持したい場合（例: JSONシリアライズ用）
-keepclassmembers class com.mapconductor.here.MapCameraPosition {
   public com.mapconductor.here.GeoPoint target;
   public double zoom;
   public double bearing;
   public double tilt;
}
# （注意: -keep class ... { *; } は上記 -keepclassmembers ... を包含するので、
#   クラス全体を保持する場合はフィールド個別の keepclassmembers は不要なことが多い）