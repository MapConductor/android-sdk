Of course! Here is the high-quality SDK documentation for the provided code snippet.

# MapboxInitSDK()

## Signature

```kotlin
fun MapboxInitSDK(context: Context)
```

## Description

Initializes the Mapbox SDK with the required access token. This function must be called once before using any other Mapbox components, such as maps or navigation services.

The function retrieves your Mapbox access token from the application's `AndroidManifest.xml` metadata. It then sets this token globally for the SDK to use. It is highly recommended to call this function in your `Application` class's `onCreate()` method to ensure the SDK is initialized as early as possible.

## Parameters

| Parameter | Type    | Description                                                                                                                            |
| :-------- | :------ | :------------------------------------------------------------------------------------------------------------------------------------- |
| `context` | `Context` | The application context. It is used to access the app's metadata to find the `MAPBOX_ACCESS_TOKEN`. Using an `ApplicationContext` is recommended. |

## Returns

This function does not return any value.

## Throws

| Type        | Condition                                                                                             |
| :---------- | :---------------------------------------------------------------------------------------------------- |
| `Exception` | Thrown if the `<meta-data android:name="MAPBOX_ACCESS_TOKEN" ... />` is not found in `AndroidManifest.xml`. |

## Example

To use this function, you must first add your access token to your `AndroidManifest.xml` file and then call `MapboxInitSDK()` from your `Application` class.

**1. Add Access Token to `AndroidManifest.xml`**

Place the `<meta-data>` tag containing your access token inside the `<application>` tag.

```xml
<!-- AndroidManifest.xml -->

<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.myapp">

    <application
        android:name=".MyApplication"
        ...>

        <!-- Add your Mapbox access token here -->
        <meta-data
            android:name="MAPBOX_ACCESS_TOKEN"
            android:value="YOUR_MAPBOX_ACCESS_TOKEN" />

        ...

    </application>
</manifest>
```

**2. Call `MapboxInitSDK` in your Application Class**

Create a custom `Application` class and call the function within the `onCreate()` method.

```kotlin
// MyApplication.kt

import android.app.Application
import com.mapconductor.mapbox.MapboxInitSDK

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize the Mapbox SDK with the access token
        // This should be done once when the application starts.
        try {
            MapboxInitSDK(this)
        } catch (e: Exception) {
            // Handle the exception, e.g., log an error or disable map features
            e.printStackTrace()
        }
    }
}
```