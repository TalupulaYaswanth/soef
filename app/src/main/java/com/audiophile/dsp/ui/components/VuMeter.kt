package com.audiophile.dsp.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.audiophile.dsp.ui.theme.DarkCardBorder
import com.audiophile.dsp.ui.theme.DarkSurface
import com.audiophile.dsp.ui.theme.DangerRed
import com.audiophile.dsp.ui.theme.NeonCyan
import com.audiophile.dsp.ui.theme.TextMuted

@Composable
fun VuMeter(
    isEnabled: Boolean,
    isClipping: Boolean,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "VuMeterTransition")

    val animLevelL by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "VuLevelL"
    )

    val animLevelR by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.90f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "VuLevelR"
    )

    val levelL = if (isEnabled) animLevelL else 0.05f
    val levelR = if (isEnabled) animLevelR else 0.05f

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .background(DarkSurface, RoundedCornerShape(12.dp))
            .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Channel Labels
        Column {
            Text("L", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
            Spacer(modifier = Modifier.height(6.dp))
            Text("R", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Level Bars Canvas
        Canvas(
            modifier = Modifier
                .weight(1f)
                .height(28.dp)
        ) {
            val barHeight = 8.dp.toPx()
            val totalWidth = size.width
            val segmentCount = 20
            val segmentGap = 2.dp.toPx()
            val segmentWidth = (totalWidth - (segmentCount - 1) * segmentGap) / segmentCount

            // Left Channel
            val activeL = (levelL * segmentCount).toInt()
            for (i in 0 until segmentCount) {
                val x = i * (segmentWidth + segmentGap)
                val isLit = i < activeL
                val color = when {
                    !isLit -> Color(0xFF1B202D)
                    i >= 17 -> DangerRed
                    i >= 13 -> Color(0xFFFFC107)
                    else -> NeonCyan
                }
                drawRect(
                    color = color,
                    topLeft = Offset(x, 2.dp.toPx()),
                    size = Size(segmentWidth, barHeight)
                )
            }

            // Right Channel
            val activeR = (levelR * segmentCount).toInt()
            for (i in 0 until segmentCount) {
                val x = i * (segmentWidth + segmentGap)
                val isLit = i < activeR
                val color = when {
                    !isLit -> Color(0xFF1B202D)
                    i >= 17 -> DangerRed
                    i >= 13 -> Color(0xFFFFC107)
                    else -> NeonCyan
                }
                drawRect(
                    color = color,
                    topLeft = Offset(x, 16.dp.toPx()),
                    size = Size(segmentWidth, barHeight)
                )
            }
        }
    }
}
