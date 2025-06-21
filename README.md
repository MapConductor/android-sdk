# セットアップ

- https://github.com/MapConductor/map-sdk-credentials/ から`secrets.properties` をプロジェクトルートに追加保存する

# KtLint

```
./gradlew allLintChecks
```

# マーカーに対するプロパティの追加方法
1. [MarkerState](./mapconductor-core/src/main/java/com/mapconductor/core/marker/Marker.kt)にプロパティの追加

```kotlin
class MarkerState(
    val id: String = UUID.randomUUID().toString(),
    ...
    animation: MarkerAnimation? = null,
    ...
) {

    var animation by mutableStateOf(animation)

}
```

2. [MapViewBase.kt](mapconductor-core/src/main/java/com/mapconductor/core/map/MapViewBase.kt)に監視するプロパティの追加
```kotlin
markers.value.forEach { markerState ->
    LaunchedEffect(
        markerState.icon,
        markerState.draggable,
        markerState.internalPosition,
        markerState.animation,   // <---- 追加
    ) {
        controller.updateMarker(markerState)
    }
}
```

3. [MarkerOverlayManagerImpl](mapconductor-core/src/main/java/com/mapconductor/core/controller/MarkerOverlayManagerImpl.kt)でDependencyInjectionする
```kotlin
class MarkerOverlayManagerImpl<
    // Actual marker instance type
    ActualMarker : Any,
>(
    val markerManager: MarkerManager<ActualMarker>,
    ...,
    val onAnimation: (params: MarkerModifyParams<ActualMarker>) -> Unit,    // <--- 追加
    ...,
) : MarkerOverlayManager {
```

4. [MarkerOverlayManagerImpl](mapconductor-core/src/main/java/com/mapconductor/core/controller/MarkerOverlayManagerImpl.kt)でコールバックを実行する
```kotlin
results.forEach { param ->
    // Execute the animation property
    param.state.animation?.let {
        coroutine.launch {
            onAnimation(param)
        }
    }
}
```

5.
