package com.audiophile.dsp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.audiophile.dsp.model.DacProfiles
import com.audiophile.dsp.model.ISO_BAND_LABELS
import com.audiophile.dsp.model.MoodProfiles
import com.audiophile.dsp.ui.MainViewModel
import com.audiophile.dsp.ui.components.BandSlider
import com.audiophile.dsp.ui.components.CrossfeedDial
import com.audiophile.dsp.ui.components.ProfileChip
import com.audiophile.dsp.ui.components.SpectrumGraph
import com.audiophile.dsp.ui.theme.AudiophileDSPTheme
import com.audiophile.dsp.ui.theme.DarkBackground
import com.audiophile.dsp.ui.theme.DarkCardBorder
import com.audiophile.dsp.ui.theme.DarkSurface
import com.audiophile.dsp.ui.theme.DangerRed
import com.audiophile.dsp.ui.theme.GoldAccent
import com.audiophile.dsp.ui.theme.NeonCyan
import com.audiophile.dsp.ui.theme.TextMuted
import com.audiophile.dsp.ui.theme.TextPrimary
import com.audiophile.dsp.ui.theme.TextSecondary
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Permission handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkNotificationPermission()
        viewModel.bindService(this)

        setContent {
            AudiophileDSPTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    AudiophileDspScreen(viewModel = viewModel)
                }
            }
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.unbindService(this)
    }
}

@Composable
fun AudiophileDspScreen(viewModel: MainViewModel) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // --- Top Bar Header ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(NeonCyan.copy(alpha = 0.15f))
                        .padding(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "DSP Equalizer",
                        tint = NeonCyan
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "AUDIOPHILE DSP",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Portable DAC/Amp Emulator",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }

            // Power Switch
            Switch(
                checked = state.isEnabled,
                onCheckedChange = { viewModel.toggleEnabled(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = DarkBackground,
                    checkedTrackColor = NeonCyan,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = DarkSurface
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Real-time Spectrum Response Graph ---
        SpectrumGraph(
            bandGainsDb = state.bandGainsDb,
            masterGainDb = state.masterGainDb,
            crossfeedIntensity = state.crossfeedIntensity
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- NEURO-MOOD AUTO-DSP SECTION (Empirical Research Integration) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface, RoundedCornerShape(16.dp))
                .border(1.dp, GoldAccent.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "Neuro-Mood DSP",
                        tint = GoldAccent
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Neuro-Mood Dynamic Soundstage",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Arousal-level EQ, sibilance filter & fatigue control",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                Switch(
                    checked = state.isAutoSongMoodEnabled,
                    onCheckedChange = { viewModel.toggleAutoSongMood(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = DarkBackground,
                        checkedTrackColor = GoldAccent,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = DarkSurface
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Interactive Mood Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MoodProfiles.ALL_MOODS.forEach { mood ->
                    val isSelected = state.activeMoodId == mood.id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) GoldAccent.copy(alpha = 0.25f) else DarkBackground)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) GoldAccent else DarkCardBorder,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { viewModel.selectMood(mood.id) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "${mood.emoji} ${mood.name}",
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) GoldAccent else TextSecondary
                        )
                    }
                }
            }

            // Active Mood Psychological Description
            val activeMoodObj = MoodProfiles.ALL_MOODS.firstOrNull { it.id == state.activeMoodId }
            if (activeMoodObj != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkBackground, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "🧠 ${activeMoodObj.psychologicalEffect}",
                        fontSize = 11.sp,
                        color = GoldAccent
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- DAC Profile Preset Selector Bar ---
        Text(
            text = "DAC TUNING PRESETS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 1.2.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DacProfiles.PRESETS.forEach { profile ->
                ProfileChip(
                    name = profile.name,
                    isSelected = state.selectedProfile.equals(profile.name, ignoreCase = true) && state.activeMoodId == null,
                    onSelected = { viewModel.selectPreset(profile.name) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- 10-Band Parametric Equalizer ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "10-BAND PARAMETRIC EQ",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.2.sp
            )
            Text(
                text = "ISO Center Freq (-12dB to +12dB)",
                fontSize = 11.sp,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal scrollable band sliders
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ISO_BAND_LABELS.forEachIndexed { index, label ->
                val gain = state.bandGainsDb.getOrElse(index) { 0f }
                BandSlider(
                    frequencyLabel = label,
                    gainDb = gain,
                    onGainChanged = { newGain ->
                        viewModel.setBandGain(index, newGain)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- Master Boost & Dynamic Limiter Safeguard ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface, RoundedCornerShape(16.dp))
                .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Limiter Safeguard",
                        tint = if (state.limiterEnabled) NeonCyan else TextMuted
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Pre-Amp & Peak Limiter Safeguard",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Prevents digital audio clipping & DAC distortion",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                // Clipping Badge Indicator
                if (state.isClipping) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DangerRed.copy(alpha = 0.2f))
                            .border(1.dp, DangerRed, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = "Clipping Warning", tint = DangerRed)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("CLIPPING", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DangerRed)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Master Gain Slider
            val formattedMaster = if (state.masterGainDb > 0) "+%.1fdB".format(Locale.US, state.masterGainDb) else "%.1fdB".format(Locale.US, state.masterGainDb)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Master Boost / Pre-Amp", fontSize = 12.sp, color = TextSecondary)
                Text(text = formattedMaster, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
            }
            Slider(
                value = state.masterGainDb,
                onValueChange = { viewModel.setMasterGain(it) },
                valueRange = -6.0f..6.0f,
                colors = SliderDefaults.colors(
                    thumbColor = NeonCyan,
                    activeTrackColor = NeonCyan,
                    inactiveTrackColor = DarkCardBorder
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Soundstage Virtualizer & Matrix Crossfeed Dial ---
        CrossfeedDial(
            crossfeedIntensity = state.crossfeedIntensity,
            onCrossfeedChanged = { viewModel.setCrossfeedIntensity(it) },
            virtualizerStrength = state.virtualizerStrength,
            onVirtualizerChanged = { viewModel.setVirtualizerStrength(it) }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}
