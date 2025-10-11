package com.mapconductor.marker.strategy.spatial;

import com.mapconductor.marker.strategy.spatial.MarkerDataDTO;
import com.mapconductor.marker.strategy.spatial.CameraPositionDTO;
import com.mapconductor.marker.strategy.spatial.SpatialResultDTO;
import com.mapconductor.marker.strategy.spatial.SpatialConfigDTO;

interface ISpatialMarkerService {
    boolean initializeSession(String sessionId, in SpatialConfigDTO config);

    boolean updateMarkers(String sessionId, in List<MarkerDataDTO> markers);

    boolean removeMarkers(String sessionId, in List<String> markerIds);

    SpatialResultDTO calculateChanges(String sessionId, in CameraPositionDTO camera);

    String findNearestMarker(String sessionId, double latitude, double longitude);

    boolean destroySession(String sessionId);

    String getPerformanceStats(String sessionId);
}
