package com.mapconductor.marker.nativestrategy.spatial;

import com.mapconductor.marker.nativestrategy.spatial.NativeMarkerDataDTO;
import com.mapconductor.marker.nativestrategy.spatial.NativeCameraPositionDTO;
import com.mapconductor.marker.nativestrategy.spatial.NativeSpatialResultDTO;
import com.mapconductor.marker.nativestrategy.spatial.NativeSpatialConfigDTO;

interface INativeSpatialMarkerService {
    boolean initializeSession(String sessionId, in NativeSpatialConfigDTO config);

    boolean addMarkers(String sessionId, in List<NativeMarkerDataDTO> markers);

    boolean updateMarker(String sessionId, in NativeMarkerDataDTO marker);

    boolean removeMarkers(String sessionId, in List<String> markerIds);

    NativeSpatialResultDTO processCameraChange(String sessionId, in NativeCameraPositionDTO camera);

    String findNearestMarker(String sessionId, double latitude, double longitude);

    boolean destroySession(String sessionId);

    String getPerformanceStats(String sessionId);
}

