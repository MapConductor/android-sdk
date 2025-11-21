---
title: SDK Setup
---

MapConductor provides a unified API on top of multiple map SDKs, but you must configure each provider's SDK independently before using the corresponding MapConductor module.

Choose one or more providers and follow the setup guide:

- **[Google Maps Setup](/setup/google-maps)**  
  Configure the Google Maps SDK, API key, and permissions, then integrate `com.mapconductor:for-googlemaps`.

- **[Mapbox Setup](/setup/mapbox)**  
  Configure the Mapbox Maven repository, access token, styles, and integrate `com.mapconductor:for-mapbox`.

- **[HERE Maps Setup](/setup/here-maps)**  
  Install the HERE SDK AAR, configure access key credentials, and integrate `com.mapconductor:for-here`.

- **[ArcGIS Maps Setup](/setup/arcgis)**
  Configure the Esri repository, ArcGIS API key, licensing, and integrate `com.mapconductor:for-arcgis`.

- **[MapLibre Setup](/setup/maplibre)**
  Configure tile servers, style JSON, and integrate `com.mapconductor:for-maplibre`. Open-source alternative with flexible hosting options.

You only need to configure the SDKs you plan to use in your application.

