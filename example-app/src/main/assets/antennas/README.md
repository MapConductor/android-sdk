# OpenCellID cell positions (MCC 310–314)

Cell tower data from [OpenCellID](https://opencellid.org) · [CC BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0/)

Derived from user-supplied 310.csv.gz through 314.csv.gz. All 723,800 rows are preserved as separate cells (not unique physical towers). Coordinates are rounded to five decimals and stored as signed E5 integers; range is float32. No token is shipped.

Circles automatically use the shared ShapeTiler and RasterLayer infrastructure above 2,000 circles. No tiling parameter is passed by the example.

Tiled circles share the tile source's states and ID lookup with the hit manager; they do not retain a CircleEntity or Fingerprint per cell. Native changes use revision numbers and attribute bit flags. Legacy fingerprint snapshots remain available for native-provider compatibility. Cheap circle projections are recomputed for requested tiles instead of retaining a full population at every zoom.

Carrier counts and MCC counts are in us-antennas.json. Classification uses the MCC/MNC pair.
Assignment source: https://imsiadmin.com/assignments/hni/ (2026-11-10). Classification is by MCC/MNC assignment, not tower ownership. CDMA SID is not interpreted as MNC. Supplied files do not represent a complete physical tower inventory.

Each cell is drawn as a fixed 500 m radius circle to indicate its estimated position, not signal coverage. The CSV range is retained in the asset but is not used as a display radius. Geodesic=true uses spherical distance. All cells for the selected carriers are rendered without sampling or a count limit; deselecting all carriers renders no circles. Carrier colors: blue, pink, red, gray.

Binary: MCCE + u32 little-endian count, then 16-byte records: i32 latitude_e5, i32 longitude_e5, f32 range_m, u32 (carrier_index * 1000 + MNC). Stable row index identifies each cell. Carrier indices: AT&T=0, T-Mobile/Sprint=1, Verizon=2, Others=3.

Reproduce with pack-opencellid.py 310.csv.gz 311.csv.gz 312.csv.gz 313.csv.gz 314.csv.gz --output OUTPUT.bin [--output OUTPUT.bin ...]. pack.py is the older FCC importer and is not used for this dataset. Modifications and binary data remain under CC BY-SA 4.0; retain the attribution above when redistributing.

CircleState.extra holds only the source row ID. A shared click handler resolves details from the source table. Subpixel circles sharing a world pixel use the topmost visible circle as their display representative; their alpha no longer accumulates at that scale. Source states and hit testing remain intact, and zooming in restores every circle.
