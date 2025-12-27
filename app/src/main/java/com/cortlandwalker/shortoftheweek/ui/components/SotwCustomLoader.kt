package com.cortlandwalker.shortoftheweek.ui.components

import android.content.res.Configuration
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cortlandwalker.shortoftheweek.R
import kotlin.math.pow

@Composable
fun SotwCustomLoader(
    modifier: Modifier = Modifier,
    spinDurationMillis: Int = 2400,
    radius: Dp = 92.dp,
    blockSize: Dp = 44.dp,
    blockCornerRadius: Dp = 10.dp,
    ringLineWidth: Dp = 28.dp,
    // Negative lift moves it closer to center, 0 centers it on the ring line
    blockLift: Dp = 0.dp
) {
    val isDark = isSystemInDarkTheme()
    val logoRes = if (isDark) R.drawable.sotw_full_logo_white_copy else R.drawable.sotw_full_logo
    // Gray ring color
    val ringStrokeColor = if (isDark) Color.White else Color.LightGray

    val infiniteTransition = rememberInfiniteTransition(label = "SotwLoaderSpin")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(spinDurationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Rotation"
    )

    val sotwColors = remember {
        listOf(
            Color(0xFF29C7C7), // Teal
            Color(0xFFF5C72B), // Yellow
            Color(0xFF7DCA30), // Green
            Color(0xFFF72B61), // Magenta
            Color(0xFFFDA3A8)  // Light Pink
        )
    }

    val spinningBlocks = remember {
        listOf(
            sotwColors[3], // Magenta
            sotwColors[4], // Pink
            sotwColors[0], // Teal
            sotwColors[2], // Green
            sotwColors[3], // Magenta
            sotwColors[2], // Green
            sotwColors[1], // Yellow
            sotwColors[0]  // Teal
        )
    }

    // Total canvas size needs to accommodate the radius + half the block size on either side
    val totalSize = (radius * 2) + blockSize + ringLineWidth

    Box(
        modifier = modifier
            .size(totalSize)
            .background(if (isSystemInDarkTheme()) Color(0xFF212121) else Color.White),
        contentAlignment = Alignment.Center
    ) {
        // 1. Static Ring (Canvas)
        Canvas(modifier = Modifier.matchParentSize()) {
            val center = this.center
            val rPx = radius.toPx()
            val strokeWidthPx = ringLineWidth.toPx()

            drawCircle(
                color = ringStrokeColor,
                radius = rPx,
                center = center,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
        }

        // 2. Center Logo
        Image(
            painter = painterResource(id = logoRes),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .matchParentSize()
                .padding(64.dp)
        )

        // 3. Spinning Blocks (Single Optimized Canvas)
        Canvas(modifier = Modifier.matchParentSize()) {
            val center = this.center
            val rPx = radius.toPx()

            // To sit "ON" the ring, the offset should simply be the radius.
            // (Previous implementation added ringWidth/2 which pushed it outside).
            val offsetRadiusPx = rPx + blockLift.toPx()

            val blockSizePx = blockSize.toPx()
            val cornerRadiusPx = blockCornerRadius.toPx()
            val blockHalf = blockSizePx / 2f

            // Pre-calculate block positions
            val totalBlocks = spinningBlocks.size
            val angleStep = 360f / totalBlocks

            // Trail settings
            val trailCount = 4
            val trailAngleStep = 6f
            val trailScaleStep = 0.05f

            // Rotate the entire canvas context by the animated angle
            rotate(degrees = angle, pivot = center) {

                spinningBlocks.forEachIndexed { index, color ->
                    val baseAngle = index * angleStep

                    // Draw Trails first (bottom layer)
                    for (i in trailCount downTo 1) {
                        val trailAngle = baseAngle - (i * trailAngleStep)
                        val fade = 0.18f * 0.65.pow((i - 1).toDouble()).toFloat()
                        val scale = 1f - (i * trailScaleStep)

                        // We use rotate() again for individual block placement relative to 12 o'clock
                        rotate(degrees = trailAngle, pivot = center) {
                            val scaledSize = blockSizePx * scale
                            val scaledHalf = scaledSize / 2f

                            // Draw Rounded Rect at top-center position (0, -radius)
                            drawRoundRect(
                                color = color.copy(alpha = fade),
                                topLeft = Offset(center.x - scaledHalf, center.y - offsetRadiusPx - scaledHalf),
                                size = Size(scaledSize, scaledSize),
                                cornerRadius = CornerRadius(cornerRadiusPx * scale, cornerRadiusPx * scale)
                            )
                        }
                    }

                    // Draw Main Block
                    rotate(degrees = baseAngle, pivot = center) {
                        // Shadow (Optional simple fake shadow)
                        drawRoundRect(
                            color = color.copy(alpha = 0.2f),
                            topLeft = Offset(center.x - blockHalf + 4f, center.y - offsetRadiusPx - blockHalf + 4f),
                            size = Size(blockSizePx, blockSizePx),
                            cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
                        )

                        // Main Body
                        drawRoundRect(
                            color = color,
                            topLeft = Offset(center.x - blockHalf, center.y - offsetRadiusPx - blockHalf),
                            size = Size(blockSizePx, blockSizePx),
                            cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Light mode", showBackground = true)
@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun LoaderPreview() {
    val backgroundColor = if (isSystemInDarkTheme()) Color(0xFF212121) else Color.White
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        SotwCustomLoader()
    }
}