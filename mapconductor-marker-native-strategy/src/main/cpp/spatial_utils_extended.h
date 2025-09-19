#pragma once

#include "marker_types.h"

namespace mapconductor {
namespace marker {

/**
 * Expand geographic bounds by a specified margin.
 * 
 * @param bounds The original bounds to expand
 * @param margin The expansion margin (e.g., 0.2 = 20% expansion)
 * @return The expanded bounds
 */
GeoRectBounds expandBounds(const GeoRectBounds& bounds, double margin);

} // namespace marker
} // namespace mapconductor