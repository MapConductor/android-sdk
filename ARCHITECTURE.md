# MapConductor Android SDK Architecture

## Overview

MapConductor is a **unified mapping library** for Android that provides a single, consistent API for integrating multiple map providers. It enables developers to write mapping code once and deploy across different backends (Google Maps, Mapbox, HERE, ArcGIS) without provider-specific implementation details.

**Key Characteristics:**
- **Language**: Kotlin 2.2.21
- **UI Framework**: Jetpack Compose 1.8.1
- **Pattern**: Layered Multi-Provider Architecture
- **State Management**: Kotlin StateFlow (Reactive)
- **Supported Providers**: Google Maps, Mapbox, HERE Maps, ArcGIS, MapLibre
- **Android SDK**: 26-35 (minSdk=26, targetSdk=35)

---

## High-Level Architecture

```mermaid
graph TB
    subgraph Application Layer
        APP[Example Apps<br/>simple-map-app<br/>example-app]
    end

    subgraph Provider Implementation Layer
        GOOGLE[GoogleMapView<br/>GoogleMapViewControllerInterface]
        MAPBOX[MapboxMapView<br/>MapboxMapViewControllerInterface]
        HERE[HereMapView<br/>HereMapViewControllerInterface]
        ARCGIS[ArcGISMapView<br/>ArcGISMapViewControllerInterface]
        MAPLIBRE[MapLibreMapView<br/>MapLibreMapViewController]
    end

    subgraph Core Abstraction Layer
        CORE[mapconductor-core]
        MAPBASE[MapViewBase]
        CONTROLLER[MapViewControllerInterface]
        MANAGERS[Overlay Managers]
        SPATIAL[Spatial Indexing<br/>HexGeocellInterface, KDTree]
        PROJECTION[Projections<br/>WebMercator, WGS84]
        GEOMETRY[Spherical Geometry]
    end

    subgraph Native SDK Layer
        GMAPS[Google Maps SDK]
        MBOX[Mapbox SDK]
        HMAP[HERE SDK]
        AGIS[ArcGIS SDK]
        MLIBRE[MapLibre SDK]
    end

    APP --> GOOGLE
    APP --> MAPBOX
    APP --> HERE
    APP --> ARCGIS
    APP --> MAPLIBRE

    GOOGLE --> MAPBASE
    MAPBOX --> MAPBASE
    HERE --> MAPBASE
    ARCGIS --> MAPBASE
    MAPLIBRE --> MAPBASE

    MAPBASE --> CONTROLLER
    MAPBASE --> MANAGERS
    CONTROLLER --> SPATIAL
    CONTROLLER --> PROJECTION
    CONTROLLER --> GEOMETRY

    GOOGLE --> GMAPS
    MAPBOX --> MBOX
    HERE --> HMAP
    ARCGIS --> AGIS
    MAPLIBRE --> MLIBRE

    style APP fill:#e1f5ff
    style CORE fill:#fff4e1
    style GOOGLE fill:#e8f5e9
    style MAPBOX fill:#e8f5e9
    style HERE fill:#e8f5e9
    style ARCGIS fill:#e8f5e9
    style MAPLIBRE fill:#e8f5e9
    style GMAPS fill:#f3e5f5
    style MBOX fill:#f3e5f5
    style HMAP fill:#f3e5f5
    style AGIS fill:#f3e5f5
    style MLIBRE fill:#f3e5f5
```

---

## Module Structure

```mermaid
graph LR
    subgraph Core Modules
        CORE[mapconductor-core<br/>Abstractions]
        BOM[mapconductor-bom<br/>Version Management]
        ICONS[mapconductor-icons<br/>Icon Components]
        STRATEGY[mapconductor-marker-strategy<br/>Marker Strategy]
        NATIVE[mapconductor-marker-native-strategy<br/>Native Strategy]
    end

    subgraph Provider Modules
        GOOGLE[mapconductor-for-googlemaps]
        MAPBOX[mapconductor-for-mapbox]
        HERE[mapconductor-for-here]
        ARCGIS[mapconductor-for-arcgis]
        MAPLIBRE[mapconductor-for-maplibre]
    end

    subgraph Application Modules
        EXAMPLE[example-app<br/>Demo App]
        SIMPLE[simple-map-app<br/>Simple Demo]
    end

    BOM -.manages versions.-> CORE
    BOM -.manages versions.-> GOOGLE
    BOM -.manages versions.-> MAPBOX
    BOM -.manages versions.-> HERE
    BOM -.manages versions.-> ARCGIS

    GOOGLE --> CORE
    MAPBOX --> CORE
    HERE --> CORE
    ARCGIS --> CORE
    MAPLIBRE --> CORE

    GOOGLE --> ICONS
    MAPBOX --> ICONS
    HERE --> ICONS
    ARCGIS --> ICONS

    GOOGLE --> STRATEGY
    MAPBOX --> STRATEGY
    HERE --> STRATEGY

    STRATEGY --> NATIVE

    EXAMPLE --> GOOGLE
    EXAMPLE --> MAPBOX
    EXAMPLE --> HERE
    EXAMPLE --> ARCGIS

    SIMPLE --> GOOGLE

    style CORE fill:#4fc3f7
    style BOM fill:#81c784
    style ICONS fill:#ffb74d
    style STRATEGY fill:#ba68c8
    style NATIVE fill:#f06292
```

---

## Core Component Architecture

```mermaid
graph TB
    subgraph MapViewBase Component
        MVB[MapViewBase<br/>Generic Compose Component]
        STATE[MapViewStateInterface<br/>StateFlow Management]
        CAMERA[MapCameraPositionBase<br/>Camera State]
        HOLDER[MapViewHolderInterface<br/>View Instance]
    end

    subgraph Controller Layer
        MVC[MapViewControllerInterface<br/>Abstract Interface]
        OVERLAY[OverlayControllerInterface<br/>Overlay Management]
        RENDERER[OverlayRendererInterface<br/>Rendering Interface]
    end

    subgraph Overlay Managers
        MARKER_MGR[MarkerManager<br/>Spatial Indexing]
        CIRCLE_MGR[CircleManagerInterface]
        POLYLINE_MGR[PolylineManagerInterface]
        POLYGON_MGR[PolygonManagerInterface]
        GROUND_MGR[GroundImageManagerInterface]
    end

    subgraph Overlay Controllers
        MARKER_CTRL[AbstractMarkerController]
        CIRCLE_CTRL[CircleController]
        POLYLINE_CTRL[PolylineController]
        POLYGON_CTRL[PolygonController]
        GROUND_CTRL[GroundImageController]
    end

    subgraph Overlay Renderers
        MARKER_RENDER[AbstractMarkerOverlayRenderer]
        CIRCLE_RENDER[CircleOverlayRendererInterface]
        POLYLINE_RENDER[PolylineOverlayRendererInterface]
        POLYGON_RENDER[PolygonOverlayRendererInterface]
        GROUND_RENDER[GroundImageOverlayRendererInterface]
    end

    MVB --> STATE
    MVB --> CAMERA
    MVB --> HOLDER
    MVB --> MVC

    MVC --> OVERLAY
    MVC --> RENDERER

    OVERLAY --> MARKER_MGR
    OVERLAY --> CIRCLE_MGR
    OVERLAY --> POLYLINE_MGR
    OVERLAY --> POLYGON_MGR
    OVERLAY --> GROUND_MGR

    MARKER_MGR --> MARKER_CTRL
    CIRCLE_MGR --> CIRCLE_CTRL
    POLYLINE_MGR --> POLYLINE_CTRL
    POLYGON_MGR --> POLYGON_CTRL
    GROUND_MGR --> GROUND_CTRL

    MARKER_CTRL --> MARKER_RENDER
    CIRCLE_CTRL --> CIRCLE_RENDER
    POLYLINE_CTRL --> POLYLINE_RENDER
    POLYGON_CTRL --> POLYGON_RENDER
    GROUND_CTRL --> GROUND_RENDER

    style MVB fill:#4fc3f7
    style MVC fill:#81c784
    style MARKER_MGR fill:#ffb74d
    style MARKER_CTRL fill:#ba68c8
    style MARKER_RENDER fill:#f06292
```

---

## Provider Implementation Pattern

```mermaid
graph TB
    subgraph Core Abstractions
        MAPBASE[MapViewBase]
        CONTROLLER[MapViewControllerInterface]
        MARKER_CTRL[AbstractMarkerController]
        MARKER_RENDER[AbstractMarkerOverlayRenderer]
    end

    subgraph Google Maps Implementation
        GOOGLE_VIEW[GoogleMapView]
        GOOGLE_CTRL[GoogleMapViewControllerInterface]
        GOOGLE_MARKER_CTRL[GoogleMarkerController]
        GOOGLE_MARKER_RENDER[GoogleMarkerOverlayRenderer]
    end

    subgraph Mapbox Implementation
        MAPBOX_VIEW[MapboxMapView]
        MAPBOX_CTRL[MapboxMapViewControllerInterface]
        MAPBOX_MARKER_CTRL[MapboxMarkerController]
        MAPBOX_MARKER_RENDER[MapboxMarkerOverlayRenderer]
    end

    subgraph HERE Implementation
        HERE_VIEW[HereMapView]
        HERE_CTRL[HereMapViewControllerInterface]
        HERE_MARKER_CTRL[HereMarkerController]
        HERE_MARKER_RENDER[HereMarkerOverlayRenderer]
    end

    MAPBASE -.implements.-> GOOGLE_VIEW
    MAPBASE -.implements.-> MAPBOX_VIEW
    MAPBASE -.implements.-> HERE_VIEW

    CONTROLLER -.implements.-> GOOGLE_CTRL
    CONTROLLER -.implements.-> MAPBOX_CTRL
    CONTROLLER -.implements.-> HERE_CTRL

    MARKER_CTRL -.extends.-> GOOGLE_MARKER_CTRL
    MARKER_CTRL -.extends.-> MAPBOX_MARKER_CTRL
    MARKER_CTRL -.extends.-> HERE_MARKER_CTRL

    MARKER_RENDER -.extends.-> GOOGLE_MARKER_RENDER
    MARKER_RENDER -.extends.-> MAPBOX_MARKER_RENDER
    MARKER_RENDER -.extends.-> HERE_MARKER_RENDER

    GOOGLE_VIEW --> GOOGLE_CTRL
    MAPBOX_VIEW --> MAPBOX_CTRL
    HERE_VIEW --> HERE_CTRL

    GOOGLE_CTRL --> GOOGLE_MARKER_CTRL
    MAPBOX_CTRL --> MAPBOX_MARKER_CTRL
    HERE_CTRL --> HERE_MARKER_CTRL

    GOOGLE_MARKER_CTRL --> GOOGLE_MARKER_RENDER
    MAPBOX_MARKER_CTRL --> MAPBOX_MARKER_RENDER
    HERE_MARKER_CTRL --> HERE_MARKER_RENDER

    style MAPBASE fill:#fff4e1
    style CONTROLLER fill:#fff4e1
    style MARKER_CTRL fill:#fff4e1
    style MARKER_RENDER fill:#fff4e1
    style GOOGLE_VIEW fill:#e8f5e9
    style MAPBOX_VIEW fill:#e8f5e9
    style HERE_VIEW fill:#e8f5e9
```

---

## Overlay Management System

```mermaid
graph TB
    subgraph Application
        APP[Application Code]
    end

    subgraph Compose Layer
        MAPVIEW[MapViewBase Composable]
        COLLECTOR[Overlay Collectors]
    end

    subgraph Manager Layer
        MARKER_MGR[MarkerManager<br/>Spatial Indexing]
        CIRCLE_MGR[CircleManagerInterface]
        POLYLINE_MGR[PolylineManagerInterface]
        POLYGON_MGR[PolygonManagerInterface]
        GROUND_MGR[GroundImageManagerInterface]
    end

    subgraph Spatial Indexing
        HEX[HexGeocellInterface<br/>Hexagonal Cells]
        KDTREE[KDTree<br/>Spatial Search]
        REGISTRY[HexCellRegistry]
    end

    subgraph Controller Layer
        CONTROLLERS[Provider-Specific<br/>Overlay Controllers]
    end

    subgraph Renderer Layer
        RENDERERS[Provider-Specific<br/>Overlay Renderers]
    end

    subgraph Native Layer
        NATIVE[Native Map SDK<br/>Markers, Shapes, etc.]
    end

    APP -->|Define Overlays| MAPVIEW
    MAPVIEW --> COLLECTOR
    COLLECTOR --> MARKER_MGR
    COLLECTOR --> CIRCLE_MGR
    COLLECTOR --> POLYLINE_MGR
    COLLECTOR --> POLYGON_MGR
    COLLECTOR --> GROUND_MGR

    MARKER_MGR -->|Lazy Init| HEX
    HEX --> KDTREE
    HEX --> REGISTRY

    MARKER_MGR --> CONTROLLERS
    CIRCLE_MGR --> CONTROLLERS
    POLYLINE_MGR --> CONTROLLERS
    POLYGON_MGR --> CONTROLLERS
    GROUND_MGR --> CONTROLLERS

    CONTROLLERS --> RENDERERS
    RENDERERS --> NATIVE

    style APP fill:#e1f5ff
    style MAPVIEW fill:#fff4e1
    style MARKER_MGR fill:#ffb74d
    style HEX fill:#ba68c8
    style CONTROLLERS fill:#81c784
    style RENDERERS fill:#f06292
    style NATIVE fill:#f3e5f5
```

---

## Spatial Indexing for Markers

```mermaid
graph TB
    subgraph MarkerManager
        ADD[Add Marker Request]
        THRESHOLD{More than<br/>100 markers?}
        SPATIAL[Use Spatial Index]
        BRUTE[Use Brute Force]
    end

    subgraph HexGeocellInterface System
        HEX[HexGeocellInterface]
        CALC[Calculate Cell ID<br/>from GeoPointInterface]
        REGISTRY[HexCellRegistry<br/>Cell → Markers Map]
    end

    subgraph Spatial Queries
        NEAREST[Find Nearest]
        WITHIN[Find Within Bounds]
        KDTREE[KDTree Search]
    end

    ADD --> THRESHOLD
    THRESHOLD -->|Yes| SPATIAL
    THRESHOLD -->|No| BRUTE

    SPATIAL --> HEX
    HEX --> CALC
    CALC --> REGISTRY

    REGISTRY --> NEAREST
    REGISTRY --> WITHIN
    NEAREST --> KDTREE
    WITHIN --> KDTREE

    style ADD fill:#e1f5ff
    style SPATIAL fill:#c8e6c9
    style BRUTE fill:#ffcdd2
    style HEX fill:#ffb74d
    style KDTREE fill:#ba68c8
```

---

## Reactive State Management

```mermaid
sequenceDiagram
    participant App as Application
    participant Compose as MapViewBase
    participant State as MapViewStateInterface
    participant Flow as StateFlow
    participant Controller as MapViewControllerInterface
    participant Native as Native Map SDK

    App->>Compose: @Composable MapView()
    Compose->>State: Initialize State
    State->>Flow: Create StateFlow

    App->>Compose: Update Camera Position
    Compose->>State: Emit Camera Change
    State->>Flow: StateFlow.emit()
    Flow->>Compose: Trigger Recomposition
    Compose->>Controller: Update Camera
    Controller->>Native: Native API Call

    Native-->>Controller: Camera Changed Event
    Controller-->>Flow: Update StateFlow
    Flow-->>Compose: Trigger Recomposition
    Compose-->>App: UI Updated

    Note over Flow,Compose: Debounced 100ms
    Note over State,Flow: Reactive Updates
```

---

## ProjectionInterface & Coordinate Systems

```mermaid
graph TB
    subgraph Application Data
        GEOPOINT[GeoPointInterface<br/>lat, lon]
        BOUNDS[GeoRectBounds<br/>southwest, northeast]
    end

    subgraph ProjectionInterface Layer
        PROJ_IFACE[ProjectionInterface Interface]
        WEBMERC[WebMercator<br/>EPSG:3857]
        WGS84[WGS84<br/>EPSG:4326]
    end

    subgraph Spherical Geometry
        CALC[GeographicLibCalculator]
        DISTANCE[Calculate Distance]
        POSITION[Calculate Position at Distance]
        NEAREST[Find Nearest Point]
        EXPAND[Expand Bounds]
        ONLINE[Is Point on Geodesic Line]
    end

    subgraph Earth Model
        EARTH[Earth Constants<br/>Radius, Circumference]
    end

    GEOPOINT --> PROJ_IFACE
    BOUNDS --> PROJ_IFACE

    PROJ_IFACE --> WEBMERC
    PROJ_IFACE --> WGS84

    WEBMERC --> CALC
    WGS84 --> CALC

    CALC --> DISTANCE
    CALC --> POSITION
    CALC --> NEAREST
    CALC --> EXPAND
    CALC --> ONLINE

    CALC --> EARTH

    style GEOPOINT fill:#4fc3f7
    style PROJ_IFACE fill:#fff4e1
    style WEBMERC fill:#81c784
    style WGS84 fill:#ffb74d
    style CALC fill:#ba68c8
    style EARTH fill:#f06292
```

---

## Marker Rendering Strategy

```mermaid
graph TB
    subgraph Application
        APP[Define Marker]
        MARKER_STATE[MarkerState]
        ICON[MarkerIconInterface]
        ANIM[MarkerAnimation]
    end

    subgraph Strategy Layer
        STRATEGY[MarkerRenderingStrategyInterface]
        NATIVE_STRAT[Native Strategy]
        CUSTOM_STRAT[Custom Strategy]
    end

    subgraph Icon System
        DEFAULT_ICON[DefaultIcon]
        IMAGE_ICON[ImageIcon]
        BITMAP_CACHE[BitmapIconCache]
    end

    subgraph Renderer Layer
        RENDERER[MarkerOverlayRendererInterface]
    end

    subgraph Provider Layer
        PROVIDER[Provider-Specific<br/>Marker Controller]
        NATIVE_API[Native Map API]
    end

    APP --> MARKER_STATE
    MARKER_STATE --> ICON
    MARKER_STATE --> ANIM

    MARKER_STATE --> STRATEGY
    STRATEGY --> NATIVE_STRAT
    STRATEGY --> CUSTOM_STRAT

    ICON --> DEFAULT_ICON
    ICON --> IMAGE_ICON
    IMAGE_ICON --> BITMAP_CACHE

    STRATEGY --> RENDERER
    DEFAULT_ICON --> RENDERER
    IMAGE_ICON --> RENDERER

    RENDERER --> PROVIDER
    PROVIDER --> NATIVE_API

    style APP fill:#e1f5ff
    style STRATEGY fill:#fff4e1
    style ICON fill:#ffb74d
    style RENDERER fill:#81c784
    style PROVIDER fill:#ba68c8
    style NATIVE_API fill:#f3e5f5
```

---

## Directory Structure

```
android-sdk/
├── mapconductor-core/                     # Core abstractions (335 files)
│   └── src/main/java/com/mapconductor/core/
│       ├── controller/                    # Controllers & interfaces
│       │   ├── MapViewControllerInterface.kt       # Abstract map controller
│       │   ├── BaseMapViewController.kt   # Base implementation
│       │   ├── OverlayControllerInterface.kt       # Overlay management
│       │   └── OverlayRendererInterface.kt         # Rendering interface
│       ├── map/                           # Map components
│       │   ├── MapViewBase.kt             # Generic Compose component
│       │   ├── MapViewStateInterface.kt            # State management
│       │   ├── MapCameraPositionBase.kt   # Camera abstraction
│       │   └── MapViewHolderInterface.kt           # View holder
│       ├── marker/                        # Marker system
│       │   ├── Marker.kt                  # MarkerState definition
│       │   ├── MarkerManager.kt           # Spatial indexing manager
│       │   ├── AbstractMarkerController.kt
│       │   ├── AbstractMarkerOverlayRenderer.kt
│       │   ├── MarkerRenderingStrategyInterface.kt
│       │   ├── MarkerAnimation.kt
│       │   └── MarkerIconInterface.kt
│       ├── circle/                        # Circle overlays
│       ├── polyline/                      # Polyline overlays
│       ├── polygon/                       # Polygon overlays
│       ├── groundimage/                   # Ground image overlays
│       ├── geocell/                       # Spatial indexing
│       │   ├── HexGeocellInterface.kt              # Hexagonal geocell interface
│       │   ├── HexGeocell.kt          # Implementation
│       │   ├── HexCellRegistry.kt         # Cell registry
│       │   └── KDTree.kt                  # KD-tree
│       ├── projection/                    # Coordinate projections
│       │   ├── ProjectionInterface.kt              # ProjectionInterface interface
│       │   ├── WebMercator.kt             # EPSG:3857
│       │   ├── WGS84.kt                   # EPSG:4326
│       │   └── Earth.kt                   # Earth constants
│       ├── spherical/                     # Spherical geometry
│       │   ├── Spherical.kt
│       │   ├── CalculatePositionAtDistance.kt
│       │   ├── ExpandBounds.kt
│       │   ├── GeoNearest.kt
│       │   └── GeographicLibCalculator.kt
│       ├── features/                      # Core data types
│       │   ├── GeoPointInterface.kt                # Geographic point
│       │   └── GeoRectBounds.kt           # Geographic bounds
│       ├── info/                          # Info bubble/window
│       │   ├── DrawInfoBubble.kt
│       │   ├── InfoBubbleCompose.kt
│       │   └── InfoWindowOverlay.kt
│       └── utils/                         # Utilities
│           ├── BitmapIconCache.kt
│           ├── ResourceProvider.kt
│           ├── MyLogger.kt
│           └── Utils.kt
│
├── mapconductor-for-googlemaps/           # Google Maps implementation
│   └── src/main/java/com/mapconductor/googlemaps/
│       ├── GoogleMapView.kt               # Main Compose component
│       ├── GoogleMapViewControllerInterface.kt     # Controller interface
│       ├── GoogleMapViewController.kt # Implementation
│       ├── GoogleMapViewState.kt      # State implementation
│       ├── marker/
│       │   ├── GoogleMarkerController.kt
│       │   └── GoogleMarkerOverlayRenderer.kt
│       ├── circle/
│       ├── polyline/
│       ├── polygon/
│       ├── groundimage/
│       ├── GoogleMapDesign.kt             # Style configuration
│       └── GoogleTypeAlias.kt             # Type aliases
│
├── mapconductor-for-mapbox/               # Mapbox implementation
├── mapconductor-for-here/                 # HERE Maps implementation
├── mapconductor-for-arcgis/               # ArcGIS implementation
├── mapconductor-for-maplibre/             # MapLibre implementation
│
├── mapconductor-icons/                    # Icon components
├── mapconductor-marker-strategy/          # Marker strategy abstraction
├── mapconductor-marker-native-strategy/   # Native marker strategy
├── mapconductor-bom/                      # Bill of Materials
│
├── example-app/                           # Demo application
│   └── src/main/java/com/mapconductor/example/
│       ├── MainActivity.kt
│       ├── DemoAppScreen.kt
│       └── pages/                         # Demo pages
│
├── simple-map-app/                        # Simple demo
│
├── docs/                                  # Documentation site
│   └── (Astro-based documentation)
│
├── gradle/                                # Gradle wrapper
├── build.gradle.kts                       # Root build config
└── settings.gradle.kts                    # Module settings
```

---

## Technology Stack

### Core Technologies

| Category | Technology | Version | Purpose |
|----------|-----------|---------|---------|
| **Language** | Kotlin | 2.2.21 | Primary language |
| **JVM Target** | Java | 17 | JVM compatibility |
| **UI Framework** | Jetpack Compose | 1.8.1 | Modern UI |
| **Compose BOM** | androidx.compose | 2025.05.00 | Dependency management |
| **Android SDK** | Android | 26-35 | Platform support |

### Map Provider SDKs

| Provider | Package | Version |
|----------|---------|---------|
| **Google Maps** | com.google.android.gms:play-services-maps | 19.2.0 |
| **Mapbox** | com.mapbox.maps:android-ndk27 | 11.14.3 |
| **MapLibre** | org.maplibre.gl:android-sdk | 12.0.0 |
| **ArcGIS** | com.esri:arcgis-maps-kotlin | 200.7.0 |
| **HERE Maps** | - | Local libs |

### Build & Distribution

| Tool | Version | Purpose |
|------|---------|---------|
| **Gradle** | 8.10.1 | Build system |
| **KtLint** | 13.1.0 | Code style |
| **Maven Central** | NMCP | Primary distribution |
| **GitHub Packages** | - | Secondary distribution |

### Libraries & Utilities

| Library | Package | Version | Purpose |
|---------|---------|---------|---------|
| **AndroidX Activity** | androidx.activity.compose | - | Activity integration |
| **AndroidX Lifecycle** | androidx.lifecycle.* | - | Lifecycle management |
| **AndroidX Core KTX** | androidx.core-ktx | - | Kotlin extensions |
| **Material Design 3** | androidx.compose.material3 | - | Material components |
| **GeographicLib** | net.sf.geographiclib | 2.1 | Geodesic calculations |

### Testing

| Framework | Version | Purpose |
|-----------|---------|---------|
| **JUnit** | 4.13.2 | Unit testing |
| **Espresso** | 3.6.1 | UI testing |
| **AndroidX Test** | - | Test infrastructure |

### Documentation

| Tool | Purpose |
|------|---------|
| **Astro** | Static site generator |
| **Starlight** | Documentation theme |

---

## Key Design Patterns

### 1. Layered Architecture
Separation of concerns across 4 distinct layers:
- Application Layer (example apps)
- Provider Implementation Layer (Google, Mapbox, HERE, ArcGIS)
- Core Abstraction Layer (interfaces, managers, utilities)
- Native SDK Layer (vendor-specific APIs)

### 2. Generic Type System
Extensive use of Kotlin generics for type safety:
```kotlin
class MapViewBase<
    SpecificState,
    SpecificController,
    ActualMapView,
    ActualMap,
    SpecificViewHolder
>
```

### 3. Manager Pattern
Centralized management of overlays:
- `MarkerManager` - Manages all markers with spatial indexing
- `CircleManagerInterface` - Manages circles
- `PolylineManagerInterface` - Manages polylines
- `PolygonManagerInterface` - Manages polygons
- `GroundImageManagerInterface` - Manages ground images

### 4. Renderer Pattern
Abstract renderers with provider-specific implementations:
- `AbstractMarkerOverlayRenderer` → `GoogleMarkerOverlayRenderer`
- `CircleOverlayRendererInterface` → `MapboxCircleOverlayRenderer`
- Pattern ensures consistent rendering interface

### 5. Strategy Pattern
Flexible rendering strategies:
- `MarkerRenderingStrategyInterface` - Abstraction for marker rendering
- `NativeStrategy` - Uses native map markers
- Custom strategies possible for specialized rendering

### 6. Adapter Pattern
Each provider adapts native API to common interface:
- Type aliases hide provider-specific types
- Conversion functions between core and native types
- `GeoPointInterface.kt` in each provider converts coordinates

### 7. Observer Pattern (Reactive)
StateFlow-based reactive architecture:
- Camera position changes trigger recomposition
- Overlay changes propagate via StateFlow
- Debounced updates (100ms) prevent excessive recomposition

### 8. Lazy Initialization
Performance optimization:
- Spatial indexing initialized only when marker count > 100
- Brute-force search for small datasets
- HexGeocellInterface created on-demand

### 9. Composition over Inheritance
Jetpack Compose composables over View inheritance:
- `@Composable` functions for all map views
- CompositionLocal for dependency injection
- Declarative UI construction

---

## Spatial Indexing Algorithm

MapConductor uses a **two-tier spatial indexing system** for efficient marker management:

### Tier 1: Hexagonal Geocells (HexGeocellInterface)
```
Geographic Space → Hexagonal Grid → Cell IDs
- Each cell covers approximately equal area
- Cells indexed by unique ID
- Fast lookup: O(1) cell identification
```

### Tier 2: KD-Tree
```
Within Each Cell → KD-Tree for precise search
- 2D spatial partitioning
- Nearest neighbor search: O(log n)
- Range queries: O(√n + k) where k = results
```

### Performance Characteristics
- **< 100 markers**: Brute force (O(n))
- **≥ 100 markers**: Spatial index (O(log n))
- **Memory overhead**: ~16 bytes per marker for indexing

---

## Reactive State Flow

```mermaid
graph LR
    subgraph User Interaction
        TOUCH[Touch/Gesture]
    end

    subgraph Native Map
        NATIVE[Native Map SDK]
    end

    subgraph MapViewControllerInterface
        CONTROLLER[Controller Event Handler]
    end

    subgraph StateFlow
        FLOW[StateFlow.emit]
    end

    subgraph Compose
        RECOMP[Recomposition]
    end

    subgraph UI
        RENDER[UI Render]
    end

    TOUCH --> NATIVE
    NATIVE --> CONTROLLER
    CONTROLLER --> FLOW
    FLOW -->|Debounced 100ms| RECOMP
    RECOMP --> RENDER

    style TOUCH fill:#e1f5ff
    style NATIVE fill:#f3e5f5
    style CONTROLLER fill:#81c784
    style FLOW fill:#ffb74d
    style RECOMP fill:#ba68c8
    style RENDER fill:#f06292
```

---

## Provider Switching Flow

```mermaid
sequenceDiagram
    participant App as Application
    participant State as MapViewStateInterface
    participant Google as GoogleMapView
    participant Mapbox as MapboxMapView
    participant Native as Native SDK

    App->>State: Initialize with "google"
    State->>Google: Create GoogleMapView
    Google->>Native: Initialize Google Maps SDK
    Native-->>Google: Ready
    Google-->>State: Initialized

    Note over App,Native: App decides to switch

    App->>State: Switch to "mapbox"
    State->>Google: Dispose
    Google->>Native: Cleanup Google Maps
    State->>Mapbox: Create MapboxMapView
    Mapbox->>Native: Initialize Mapbox SDK
    Native-->>Mapbox: Ready
    Mapbox-->>State: Initialized

    Note over App,Native: State preserved across switch
```

---

## Usage Example

### Basic Map Setup

```kotlin
import com.mapconductor.googlemaps.GoogleMapView
import com.mapconductor.core.features.GeoPointInterface

@Composable
fun MyMapScreen() {
    GoogleMapView(
        modifier = Modifier.fillMaxSize(),
        cameraPosition = MapCameraPositionInterface(
            center = GeoPointInterface(35.6812, 139.7671), // Tokyo
            zoom = 12.0
        )
    ) {
        // Add markers
        Marker(
            position = GeoPointInterface(35.6812, 139.7671),
            title = "Tokyo Tower"
        )

        // Add circles
        Circle(
            center = GeoPointInterface(35.6812, 139.7671),
            radius = 1000.0, // meters
            fillColor = Color.Blue.copy(alpha = 0.3f),
            strokeColor = Color.Blue
        )

        // Add polylines
        Polyline(
            points = listOf(
                GeoPointInterface(35.6812, 139.7671),
                GeoPointInterface(35.6895, 139.6917)
            ),
            color = Color.Red,
            width = 5.0f
        )
    }
}
```

### Switching Providers

```kotlin
// Just change the import and component name
import com.mapconductor.mapbox.MapboxMapView

@Composable
fun MyMapScreen() {
    MapboxMapView( // Changed from GoogleMapView
        // ... same code, works identically
    )
}
```

---

## Architecture Benefits

### 1. Provider Independence
Write once, switch providers by changing imports. No code changes required.

### 2. Type Safety
Kotlin generics ensure compile-time type checking across all providers.

### 3. Performance Optimization
Spatial indexing (HexGeocellInterface + KDTree) provides O(log n) marker queries.

### 4. Modern Android Development
- Jetpack Compose for declarative UI
- StateFlow for reactive updates
- Kotlin coroutines for async operations

### 5. Extensibility
Easy to add new providers by implementing:
- `MapViewControllerInterface`
- Overlay controllers
- Overlay renderers

### 6. Maintainability
Clear separation of concerns:
- Core abstractions stable
- Provider implementations isolated
- Changes don't ripple across layers

### 7. Testing
- Unit test core logic without map SDK
- Mock providers for integration tests
- UI tests with Espresso

---

## Build & Distribution

### Build Configuration

```kotlin
// build.gradle.kts
android {
    compileSdk = 35
    defaultConfig {
        minSdk = 26
        targetSdk = 35
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}
```

### Publishing Targets

1. **Maven Central** (Primary)
   - Nexus Maven Central Portal (NMCP)
   - Public releases

2. **GitHub Packages** (Secondary)
   - Development builds
   - Pre-release versions

### Module Dependencies

```kotlin
dependencies {
    // Core module
    implementation("com.mapconductor:mapconductor-core:$version")

    // Choose provider
    implementation("com.mapconductor:mapconductor-for-googlemaps:$version")
    // OR
    implementation("com.mapconductor:mapconductor-for-mapbox:$version")

    // Optional: Icons
    implementation("com.mapconductor:mapconductor-icons:$version")

    // OR use BOM for version management
    implementation(platform("com.mapconductor:mapconductor-bom:$version"))
    implementation("com.mapconductor:mapconductor-core")
    implementation("com.mapconductor:mapconductor-for-googlemaps")
}
```

---

## Summary

**MapConductor** is a sophisticated Android SDK that uses **layered architecture** with **generic type system** to provide a unified API for multiple map providers. Its modern tech stack (Kotlin, Jetpack Compose, StateFlow) and performance optimizations (spatial indexing, lazy initialization, debounced updates) make it production-ready for enterprise Android applications.

The architecture elegantly separates:
- **Core abstractions** - Stable, provider-independent API
- **Provider implementations** - Isolated, swappable backends
- **Application layer** - Clean, declarative usage

This design ensures type safety, maintainability, extensibility, and freedom from vendor lock-in while leveraging modern Android development best practices.
