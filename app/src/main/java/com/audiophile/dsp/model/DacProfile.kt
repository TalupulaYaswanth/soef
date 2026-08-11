package com.audiophile.dsp.model

data class DacProfile(
    val name: String,
    val description: String,
    val bandGainsDb: FloatArray,
    val masterGainDb: Float,
    val crossfeedIntensity: Float,
    val virtualizerStrength: Int
)

object DacProfiles {
    val PRESETS = listOf(
        DacProfile(
            name = "ESS Sabre Reference",
            description = "Hyper-detailed, transparent soundstage with sub-bass extension & pristine treble clarity.",
            bandGainsDb = floatArrayOf(2.5f, 1.5f, 0.5f, 0.0f, -0.5f, 0.5f, 1.5f, 2.0f, 3.0f, 2.5f),
            masterGainDb = 1.0f,
            crossfeedIntensity = 0.35f,
            virtualizerStrength = 300
        ),
        DacProfile(
            name = "Warm Tube Amp",
            description = "Analogue tube warmth, lush midrange harmonics, smooth highs & full-bodied lower bass.",
            bandGainsDb = floatArrayOf(4.0f, 3.5f, 2.5f, 1.0f, 0.5f, 0.0f, -1.0f, -1.5f, -2.0f, -3.0f),
            masterGainDb = 1.5f,
            crossfeedIntensity = 0.50f,
            virtualizerStrength = 450
        ),
        DacProfile(
            name = "Spatial Soundstage",
            description = "Maximized binaural crossfeed & spatial virtualizer matrix for deep holographic width.",
            bandGainsDb = floatArrayOf(1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 2.0f, 2.5f, 2.0f, 1.5f, 1.0f),
            masterGainDb = 0.0f,
            crossfeedIntensity = 0.85f,
            virtualizerStrength = 850
        ),
        DacProfile(
            name = "Flat Audiophile",
            description = "Bit-perfect uncolored frequency output for studio monitoring.",
            bandGainsDb = floatArrayOf(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f),
            masterGainDb = 0.0f,
            crossfeedIntensity = 0.0f,
            virtualizerStrength = 0
        ),
        DacProfile(
            name = "Bass Cannon DAC",
            description = "Deep visceral sub-bass impact backed by high-threshold peak limiter clipping defense.",
            bandGainsDb = floatArrayOf(7.5f, 6.0f, 4.5f, 2.0f, 0.0f, -1.0f, 0.0f, 1.0f, 2.0f, 1.5f),
            masterGainDb = 2.0f,
            crossfeedIntensity = 0.25f,
            virtualizerStrength = 400
        )
    )

    fun getByName(name: String): DacProfile {
        return PRESETS.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: PRESETS[0]
    }
}
