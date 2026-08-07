#!/usr/bin/env bash
#
# Record the documentation-site sample videos from simple-map-app.
#
# One take per page on mapconductor.com whose doc.video block has no youtubeId
# yet. Each take launches the matching DocDemo by slug, drives it with input
# events, and lands as new-design/public/assets/videos/<slug>.mp4 ready to be
# uploaded to YouTube.
#
#   ./scripts/record-doc-videos.sh                 # all six
#   ./scripts/record-doc-videos.sh raster-layer    # just one, for a retake
#
# The device has to be awake, unlocked and in landscape — the coordinates below
# are for a 2944x1840 screen and are not derived from the layout, so a different
# device needs them re-measured (a screencap and a look is how these were got).
#
# adb screenrecord captures the whole screen including the status bar; the
# ffmpeg pass at the end crops it off and trims the launch, so what is uploaded
# starts on the demo rather than on a half-drawn map.
set -euo pipefail

DEVICE="${DEVICE:-HA26Z6JL}"
PKG=com.mapconductor.simplemapapp
OUT="${OUT:-$(cd "$(dirname "$0")/../../new-design/public/assets/videos" && pwd)}"
WORK="${TMPDIR:-/tmp}/mc-doc-videos"
mkdir -p "$WORK" "$OUT"

# Screen geometry, in device pixels.
W=2944
H=1840
STATUS_BAR=76        # cropped away; it carries the clock and the battery icon
BTN_Y=1721           # centre line of the bottom control row
BTN1=1472            # a single full-width button
BTN2L=740            # left of two
BTN2R=2196           # right of two
BTN3L=497            # left of three
BTN3M=1472
BTN3R=2443
MAP_Y=900            # a safe point inside the map area, for drags

adb() { command adb -s "$DEVICE" "$@"; }
tap() { adb shell input tap "$1" "$2"; sleep "${3:-2}"; }
drag() { adb shell input swipe "$1" "$2" "$3" "$4" "${5:-600}"; sleep "${6:-2}"; }

launch() {
  adb shell am force-stop "$PKG"
  adb shell am start -n "$PKG/.MainActivity" --es demo "$1" >/dev/null
  # Long enough for the tiles — and for Google Play services on the two demos
  # that use them — to be there before the recorder starts.
  sleep "${2:-10}"
}

start_rec() {
  adb shell rm -f /sdcard/mc-rec.mp4 || true
  adb shell screenrecord --size 1920x1200 --bit-rate 12000000 --time-limit 180 /sdcard/mc-rec.mp4 &
  REC_PID=$!
  sleep 2   # screenrecord takes about a second to actually start writing
}

stop_rec() {
  local slug=$1
  # SIGINT rather than kill: screenrecord only finalises the MP4 container on a
  # clean stop, and a half-written file has no moov atom and will not play.
  adb shell pkill -INT screenrecord || true
  wait "$REC_PID" 2>/dev/null || true
  sleep 3
  adb pull /sdcard/mc-rec.mp4 "$WORK/$slug.raw.mp4" >/dev/null
  adb shell rm -f /sdcard/mc-rec.mp4 || true

  # 1920x1200 is the recording; the status bar is STATUS_BAR device pixels tall,
  # which scales to this much of it.
  local crop_top=$(( STATUS_BAR * 1200 / H ))
  ffmpeg -y -loglevel error -i "$WORK/$slug.raw.mp4" \
    -vf "crop=1920:$((1200 - crop_top)):0:$crop_top" \
    -c:v libx264 -preset slow -crf 20 -pix_fmt yuv420p -movflags +faststart -an \
    "$OUT/$slug.mp4"
  echo "  → $OUT/$slug.mp4 ($(ffprobe -v error -show_entries format=duration -of csv=p=0 "$OUT/$slug.mp4" | cut -d. -f1)s)"
}

# --- one function per page -------------------------------------------------

rec_geopoint_bounds() {
  launch geopoint-bounds 10
  # A warm-up pass, off camera. fitBounds is a jump rather than an animation,
  # so on a cold cache the take would be four seconds of blank tiles where the
  # result is supposed to be; this walks the same path first so the tiles are
  # already in the cache when the recorder starts.
  tap $BTN2L $BTN_Y 8
  tap $BTN2R $BTN_Y 6
  start_rec
  sleep 3
  tap $BTN2L $BTN_Y 6      # Show all
  # Back out animates over 1200 ms and keeps settling after that; a fitBounds
  # issued too soon lands while the animation still owns the camera and is
  # swallowed, leaving the readout saying "fitted" over an unfitted map.
  tap $BTN2R $BTN_Y 8      # Back out
  tap $BTN2L $BTN_Y 6      # Show all, again
  stop_rec geopoint-bounds
}

rec_mapview() {
  # The only take that records the launch itself: what it has to show is the
  # loading state handing over, which is over before a second has passed.
  adb shell am force-stop "$PKG"
  sleep 2
  start_rec
  adb shell am start -n "$PKG/.MainActivity" --es demo mapview >/dev/null
  sleep 12
  tap $BTN1 $BTN_Y 12      # Reload map — the whole sequence again
  stop_rec mapview
}

rec_projection_zoom() {
  launch projection-zoom 14
  start_rec
  sleep 3
  tap $BTN3L $BTN_Y 5      # z 6
  tap $BTN3R $BTN_Y 5      # z 16
  tap $BTN3M $BTN_Y 5      # z 11
  drag 700 $MAP_Y 1100 700 800 4    # drag the MapLibre half; Google follows
  drag 2200 $MAP_Y 1900 1100 800 4  # and the other way round
  stop_rec projection-zoom
}

rec_raster_layer() {
  launch raster-layer 14
  start_rec
  sleep 3
  # The opacity slider spans the control row; 29 → 2646 is 0 → 1.
  drag 2000 1686 700 1686 1500 2    # fade the raster back
  drag 700 1686 2600 1686 1500 2    # and up again
  tap 2853 1676 5                   # relief → standard
  tap 2853 1676 5                   # and back
  stop_rec raster-layer
}

rec_reading_camera() {
  launch reading-camera 10
  start_rec
  sleep 3
  drag 2000 700 900 1000 900 2
  drag 900 1000 2000 600 900 2
  # A pinch needs two pointers, which `input swipe` cannot do; the zoom buttons
  # the map draws are not in this demo, so a double tap is the zoom gesture here.
  adb shell input tap 1472 $MAP_Y
  adb shell input tap 1472 $MAP_Y
  sleep 3
  drag 1400 1200 1700 600 900 3
  stop_rec reading-camera
}

rec_switching_providers() {
  launch switching-providers 14
  start_rec
  sleep 4
  tap $BTN1 $BTN_Y 8       # → Google Maps
  drag 1800 1000 1200 700 800 4
  tap $BTN1 $BTN_Y 8       # → MapLibre, same camera, same overlays
  stop_rec switching-providers
}

ALL=(geopoint-bounds mapview projection-zoom raster-layer reading-camera switching-providers)
if [ $# -gt 0 ]; then
  TARGETS=("$@")
else
  TARGETS=("${ALL[@]}")
fi

for slug in "${TARGETS[@]}"; do
  # Braces matter: macOS still ships bash 3.2, which reads the ellipsis right
  # after a bare $slug as part of the name.
  echo "recording ${slug}…"
  "rec_${slug//-/_}"
done

adb shell am force-stop "$PKG"
echo "done."
