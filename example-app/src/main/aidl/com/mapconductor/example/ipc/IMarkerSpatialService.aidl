package com.mapconductor.example.ipc;

import com.mapconductor.example.ipc.SpatialConfigDTO;
import com.mapconductor.example.ipc.MarkerDataDTO;
import com.mapconductor.example.ipc.CameraPositionDTO;
import com.mapconductor.example.ipc.SpatialResultDTO;

/**
 * AIDL interface for background marker spatial calculations.
 * This service runs in a separate process and handles heavy spatial computations.
 */
interface IMarkerSpatialService {
    /**
     * Initialize a new spatial index session.
     */
    boolean initializeSession(String sessionId, in SpatialConfigDTO config);
    
    /**
     * Add or update markers in the spatial index.
     */
    boolean updateMarkers(String sessionId, in List<MarkerDataDTO> markers);
    
    /**
     * Remove markers from the spatial index.
     */
    boolean removeMarkers(String sessionId, in List<String> markerIds);
    
    /**
     * Perform spatial calculation for camera position change.
     * Returns which markers should be added/removed/updated for rendering.
     */
    SpatialResultDTO calculateSpatialChanges(String sessionId, in CameraPositionDTO camera);
    
    /**
     * Find the nearest marker to a given position.
     */
    String findNearestMarker(String sessionId, double latitude, double longitude);
    
    /**
     * Clear all markers and destroy the session.
     */
    boolean destroySession(String sessionId);
    
    /**
     * Get performance statistics for debugging.
     */
    String getPerformanceStats(String sessionId);
}