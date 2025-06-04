package com.mapconductor.core.info

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp

@Composable
internal fun DrawInfoBubble(
    modifier: Modifier,
    bubbleColor: Color,
    borderColor: Color,
    contentPadding: Dp,
    cornerRadius: Dp,
    tailSize: Dp,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier.wrapContentSize()) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height
            val tailSizePx = tailSize.toPx()
            val cornerPx = cornerRadius.toPx()

            val path =
                Path().apply {
                    moveTo(2 * cornerPx, 0f)
                    lineTo(width - 2 * cornerPx, 0f)
                    // -- top / right corner --
                    arcTo(
                        rect =
                            Rect(
                                topLeft = Offset(width - 2 * cornerPx, 0f),
                                bottomRight = Offset(width, 2 * cornerPx),
                            ),
                        startAngleDegrees = -90f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false,
                    )
                    lineTo(width, height - tailSizePx - 2 * cornerPx)
                    // -- bottom / right corner --
                    arcTo(
                        rect =
                            Rect(
                                topLeft = Offset(width - 2 * cornerPx, height - tailSizePx - 2 * cornerPx),
                                bottomRight = Offset(width, height - tailSizePx),
                            ),
                        startAngleDegrees = 0f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false,
                    )
                    // -- tail --
                    lineTo(width / 2 + tailSizePx / 2, height - tailSizePx)
                    lineTo(width / 2, height)
                    lineTo(width / 2 - tailSizePx / 2, height - tailSizePx)
                    lineTo(2 * cornerPx, height - tailSizePx)
                    // -- bottom / left
                    arcTo(
                        rect =
                            Rect(
                                topLeft = Offset(0f, height - tailSizePx - 2 * cornerPx),
                                bottomRight = Offset(2 * cornerPx, height - tailSizePx),
                            ),
                        startAngleDegrees = 90f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false,
                    )
                    lineTo(0f, 2 * cornerPx)
                    arcTo(
                        rect =
                            Rect(
                                topLeft = Offset(0f, 0f),
                                bottomRight = Offset(2 * cornerPx, 2 * cornerPx),
                            ),
                        startAngleDegrees = 180f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false,
                    )
                    close()
                }

            drawPath(path, color = bubbleColor, style = Fill)
            drawPath(path, color = borderColor, style = Stroke(width = 2f))
        }
        // 内容
        Box(
            modifier =
                Modifier
                    .padding(
                        start = contentPadding,
                        top = contentPadding,
                        bottom = contentPadding + tailSize,
                        end = contentPadding,
                    ).wrapContentSize()
                    .clip(RoundedCornerShape(cornerRadius)),
        ) {
            content()
        }
    }
}
