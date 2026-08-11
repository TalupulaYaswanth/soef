package com.audiophile.dsp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.audiophile.dsp.ui.theme.DarkCardBorder
import com.audiophile.dsp.ui.theme.DarkSurface
import com.audiophile.dsp.ui.theme.NeonCyan
import com.audiophile.dsp.ui.theme.TextMuted
import com.audiophile.dsp.ui.theme.TextPrimary
import com.audiophile.dsp.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun BandSlider(
    frequencyLabel: String,
    gainDb: Float,
    onGainChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(58.dp)
            .background(DarkSurface, RoundedCornerShape(12.dp))
            .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
            .padding(vertical = 10.dp, horizontal = 4.dp)
    ) {
        // Gain dB Text Value
        val formattedGain = if (gainDb > 0) "+%.1fdB".format(Locale.US, gainDb) else "%.1fdB".format(Locale.US, gainDb)
        Text(
            text = formattedGain,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (gainDb != 0f) NeonCyan else TextSecondary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Vertical Slider Rotated -90 degrees
        Slider(
            value = gainDb,
            onValueChange = onGainChanged,
            valueRange = -12.0f..12.0f,
            colors = SliderDefaults.colors(
                thumbColor = NeonCyan,
                activeTrackColor = NeonCyan,
                inactiveTrackColor = DarkCardBorder
            ),
            modifier = Modifier
                .height(130.dp)
                .rotate(-90f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Frequency Label (31Hz ... 16kHz)
        Text(
            text = frequencyLabel,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}
