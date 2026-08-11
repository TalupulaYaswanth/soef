package com.audiophile.dsp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.audiophile.dsp.ui.theme.DarkCardBorder
import com.audiophile.dsp.ui.theme.DarkSurface
import com.audiophile.dsp.ui.theme.GoldAccent
import com.audiophile.dsp.ui.theme.NeonCyan
import com.audiophile.dsp.ui.theme.TextMuted
import com.audiophile.dsp.ui.theme.TextPrimary
import com.audiophile.dsp.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun CrossfeedDial(
    crossfeedIntensity: Float,
    onCrossfeedChanged: (Float) -> Unit,
    virtualizerStrength: Int,
    onVirtualizerChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkSurface, RoundedCornerShape(16.dp))
            .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text(
                    text = "Soundstage Virtualizer & Crossfeed",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Bauer Matrix L/R cross-bleed & spatial room acoustic expansion",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
            Text(
                text = "${(crossfeedIntensity * 100).toInt()}%",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = GoldAccent
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Matrix Crossfeed Slider
        Text(
            text = "Matrix Crossfeed Intensity (Headphone Fatigue Reduction)",
            fontSize = 12.sp,
            color = TextSecondary
        )
        Slider(
            value = crossfeedIntensity,
            onValueChange = onCrossfeedChanged,
            valueRange = 0.0f..1.0f,
            colors = SliderDefaults.colors(
                thumbColor = GoldAccent,
                activeTrackColor = GoldAccent,
                inactiveTrackColor = DarkCardBorder
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Hardware Spatial Virtualizer
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Spatial Hardware Depth: $virtualizerStrength mB",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
        Slider(
            value = virtualizerStrength.toFloat(),
            onValueChange = { onVirtualizerChanged(it.toInt()) },
            valueRange = 0.0f..1000.0f,
            colors = SliderDefaults.colors(
                thumbColor = NeonCyan,
                activeTrackColor = NeonCyan,
                inactiveTrackColor = DarkCardBorder
            )
        )
    }
}
