package com.audiophile.dsp.model

/**
 * Standard 10 ISO Equalizer Frequencies in Hz:
 * [31Hz, 62Hz, 125Hz, 250Hz, 500Hz, 1000Hz, 2000Hz, 4000Hz, 8000Hz, 16000Hz]
 */
val ISO_FREQUENCIES_HZ = intArrayOf(31, 62, 125, 250, 500, 1000, 2000, 4000, 8000, 16000)

val ISO_BAND_LABELS = arrayOf("31Hz", "62Hz", "125Hz", "250Hz", "500Hz", "1kHz", "2kHz", "4kHz", "8kHz", "16kHz")

/**
 * Data class representing the real-time configuration of the DSP Audio Engine.
 */
data class DspState(
    val isEnabled: Boolean = true,
    val bandGainsDb: FloatArray = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f),
    val masterGainDb: Float = 0.0f,          // Boost/Pre-amp in dB (-12.0dB to +12.0dB)
    val crossfeedIntensity: Float = 0.35f,   // Spatial matrix crossfeed (0.0 to 1.0)
    val virtualizerStrength: Int = 350,      // Hardware virtualizer (0 to 1000)
    val limiterEnabled: Boolean = true,
    val limiterThresholdDb: Float = -0.5f,   // Peak limiter threshold in dBFS
    val limiterRatio: Float = 10.0f,         // Compression ratio for peak limiting
    val selectedProfile: String = "ESS Sabre Reference",
    val activeMoodId: String? = null,
    val lastAiPrompt: String? = null,
    val aiReasoningText: String? = null,
    val geminiApiKey: String? = null,
    val isGeminiAiEnabled: Boolean = false,
    val isGeminiLoading: Boolean = false,
    val isAntiSibilanceEnabled: Boolean = false,
    val isAutoSongMoodEnabled: Boolean = false,
    val isClipping: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DspState

        if (isEnabled != other.isEnabled) return false
        if (!bandGainsDb.contentEquals(other.bandGainsDb)) return false
        if (masterGainDb != other.masterGainDb) return false
        if (crossfeedIntensity != other.crossfeedIntensity) return false
        if (virtualizerStrength != other.virtualizerStrength) return false
        if (limiterEnabled != other.limiterEnabled) return false
        if (limiterThresholdDb != other.limiterThresholdDb) return false
        if (limiterRatio != other.limiterRatio) return false
        if (selectedProfile != other.selectedProfile) return false
        if (activeMoodId != other.activeMoodId) return false
        if (lastAiPrompt != other.lastAiPrompt) return false
        if (aiReasoningText != other.aiReasoningText) return false
        if (geminiApiKey != other.geminiApiKey) return false
        if (isGeminiAiEnabled != other.isGeminiAiEnabled) return false
        if (isGeminiLoading != other.isGeminiLoading) return false
        if (isAntiSibilanceEnabled != other.isAntiSibilanceEnabled) return false
        if (isAutoSongMoodEnabled != other.isAutoSongMoodEnabled) return false
        if (isClipping != other.isClipping) return false

        return true
    }

    override fun hashCode(): Int {
        var result = isEnabled.hashCode()
        result = 31 * result + bandGainsDb.contentHashCode()
        result = 31 * result + masterGainDb.hashCode()
        result = 31 * result + crossfeedIntensity.hashCode()
        result = 31 * result + virtualizerStrength.hashCode()
        result = 31 * result + limiterEnabled.hashCode()
        result = 31 * result + limiterThresholdDb.hashCode()
        result = 31 * result + limiterRatio.hashCode()
        result = 31 * result + selectedProfile.hashCode()
        result = 31 * result + (activeMoodId?.hashCode() ?: 0)
        result = 31 * result + (lastAiPrompt?.hashCode() ?: 0)
        result = 31 * result + (aiReasoningText?.hashCode() ?: 0)
        result = 31 * result + (geminiApiKey?.hashCode() ?: 0)
        result = 31 * result + isGeminiAiEnabled.hashCode()
        result = 31 * result + isGeminiLoading.hashCode()
        result = 31 * result + isAntiSibilanceEnabled.hashCode()
        result = 31 * result + isAutoSongMoodEnabled.hashCode()
        result = 31 * result + isClipping.hashCode()
        return result
    }
}
