package com.audiophile.dsp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.audiophile.dsp.ui.theme.DarkCardBorder
import com.audiophile.dsp.ui.theme.DarkSurface
import com.audiophile.dsp.ui.theme.GoldAccent
import com.audiophile.dsp.ui.theme.NeonCyan

@Composable
fun SpectrumGraph(
    bandGainsDb: FloatArray,
    masterGainDb: Float,
    crossfeedIntensity: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerY = height / 2f

            // Draw horizontal dB grid lines (-12dB, 0dB, +12dB)
            val lineYMinus12 = height * 0.9f
            val lineYZero = centerY
            val lineYPlus12 = height * 0.1f

            val gridColor = Color(0xFF222836)
            drawLine(gridColor, Offset(0f, lineYMinus12), Offset(width, lineYMinus12), strokeWidth = 1f)
            drawLine(Color(0xFF3B4457), Offset(0f, lineYZero), Offset(width, lineYZero), strokeWidth = 1.5f)
            drawLine(gridColor, Offset(0f, lineYPlus12), Offset(width, lineYPlus12), strokeWidth = 1f)

            if (bandGainsDb.isEmpty()) return@Canvas

            // Compute Points for EQ Curve
            val stepX = width / (bandGainsDb.size - 1)
            val points = mutableListOf<Offset>()

            for (i in bandGainsDb.indices) {
                val gain = bandGainsDb[i] + masterGainDb
                val clampedGain = gain.coerceIn(-12f, 12f)
                val normY = (clampedGain + 12f) / 24f
                val y = height - (normY * height)
                val x = i * stepX
                points.add(Offset(x, y))
            }

            // Smooth cubic bezier curve for frequency response
            val path = Path()
            val fillPath = Path()

            path.moveTo(points[0].x, points[0].y)
            fillPath.moveTo(points[0].x, height)
            fillPath.lineTo(points[0].x, points[0].y)

            for (i in 0 until points.size - 1) {
                val p1 = points[i]
                val p2 = points[i + 1]
                val controlX = (p1.x + p2.x) / 2f
                path.cubicTo(controlX, p1.y, controlX, p2.y, p2.x, p2.y)
                fillPath.cubicTo(controlX, p1.y, controlX, p2.y, p2.x, p2.y)
            }

            fillPath.lineTo(points.last().x, height)
            fillPath.close()

            // Draw gradient fill under EQ curve
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        NeonCyan.copy(alpha = 0.35f),
                        NeonCyan.copy(alpha = 0.02f)
                    )
                )
            )

            // Draw smooth primary EQ line
            drawPath(
                path = path,
                color = NeonCyan,
                style = Stroke(width = 4f)
            )

            // Draw Crossfeed Curve Overlay if intensity > 0
            if (crossfeedIntensity > 0f) {
                val cfPath = Path()
                for (i in bandGainsDb.indices) {
                    val freqFactor = 1.0f / (1.0f + (i * 0.6f))
                    val cfGain = crossfeedIntensity * 4.0f * freqFactor
                    val normY = (cfGain + 12f) / 24f
                    val y = height - (normY * height)
                    val x = i * stepX
                    if (i == 0) cfPath.moveTo(x, y) else cfPath.lineTo(x, y)
                }
                drawPath(
                    path = cfPath,
                    color = GoldAccent.copy(alpha = 0.7f),
                    style = Stroke(width = 2.5f)
                )
            }

            // Draw control nodes
            points.forEach { pt ->
                drawCircle(
                    color = NeonCyan,
                    radius = 5f,
                    center = pt
                )
                drawCircle(
                    color = DarkSurface,
                    radius = 2.5f,
                    center = pt
                )
            }
        }
    }
}
