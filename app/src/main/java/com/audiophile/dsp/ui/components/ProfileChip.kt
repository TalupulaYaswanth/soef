package com.audiophile.dsp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.audiophile.dsp.ui.theme.DarkCardBorder
import com.audiophile.dsp.ui.theme.DarkSurface
import com.audiophile.dsp.ui.theme.NeonCyan
import com.audiophile.dsp.ui.theme.TextMuted
import com.audiophile.dsp.ui.theme.TextPrimary

@Composable
fun ProfileChip(
    name: String,
    isSelected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else DarkSurface)
            .border(
                width = 1.dp,
                color = if (isSelected) NeonCyan else DarkCardBorder,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onSelected() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = name,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) NeonCyan else TextMuted
        )
    }
}
