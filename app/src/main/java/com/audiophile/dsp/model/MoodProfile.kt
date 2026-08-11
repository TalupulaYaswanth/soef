package com.audiophile.dsp.model

/**
 * Empirical Audio Research-Backed Mood & Listener Arousal DSP Profiles.
 * Maps psychological emotional states directly to 10-Band EQ gain vectors,
 * anti-sibilance notch filtering, dynamic peak limiting, and matrix crossfeed.
 */
data class MoodProfile(
    val id: String,
    val name: String,
    val emoji: String,
    val psychologicalEffect: String,
    val targetEqGainsDb: FloatArray,
    val masterGainDb: Float,
    val crossfeedIntensity: Float,
    val virtualizerStrength: Int,
    val isAntiSibilanceEnabled: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MoodProfile

        if (id != other.id) return false
        if (name != other.name) return false
        if (emoji != other.emoji) return false
        if (psychologicalEffect != other.psychologicalEffect) return false
        if (!targetEqGainsDb.contentEquals(other.targetEqGainsDb)) return false
        if (masterGainDb != other.masterGainDb) return false
        if (crossfeedIntensity != other.crossfeedIntensity) return false
        if (virtualizerStrength != other.virtualizerStrength) return false
        if (isAntiSibilanceEnabled != other.isAntiSibilanceEnabled) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + emoji.hashCode()
        result = 31 * result + psychologicalEffect.hashCode()
        result = 31 * result + targetEqGainsDb.contentHashCode()
        result = 31 * result + masterGainDb.hashCode()
        result = 31 * result + crossfeedIntensity.hashCode()
        result = 31 * result + virtualizerStrength.hashCode()
        result = 31 * result + isAntiSibilanceEnabled.hashCode()
        return result
    }
}

object MoodProfiles {
    val ENERGY_WORKOUT = MoodProfile(
        id = "energy_workout",
        name = "Energy / Workout",
        emoji = "⚡",
        psychologicalEffect = "V-Shaped Boost (+Bass, +Treble). High-impact energy, punchy transients, and drive.",
        targetEqGainsDb = floatArrayOf(4.5f, 3.5f, 2.0f, 0.5f, -0.5f, 0.0f, 1.5f, 3.0f, 4.0f, 3.5f),
        masterGainDb = 1.5f,
        crossfeedIntensity = 0.25f,
        virtualizerStrength = 350,
        isAntiSibilanceEnabled = false
    )

    val RELAX_UNWIND = MoodProfile(
        id = "relax_unwind",
        name = "Relax / Unwind",
        emoji = "🌿",
        psychologicalEffect = "Warm Profile (+Low Bass, High-Cut at 10kHz). Soothing warmth, removal of harshness, spatial depth.",
        targetEqGainsDb = floatArrayOf(3.5f, 3.0f, 2.0f, 1.0f, 0.5f, 0.0f, -1.0f, -2.5f, -4.0f, -5.0f),
        masterGainDb = 0.5f,
        crossfeedIntensity = 0.65f,
        virtualizerStrength = 650,
        isAntiSibilanceEnabled = true
    )

    val FOCUS_PRODUCTIVITY = MoodProfile(
        id = "focus_productivity",
        name = "Focus / Productivity",
        emoji = "🎯",
        psychologicalEffect = "Mid-Range Vocal Clarity (1kHz–3kHz boost). Clear instrument separation & zero listening fatigue.",
        targetEqGainsDb = floatArrayOf(0.5f, 0.0f, 0.5f, 1.5f, 3.5f, 3.0f, 2.5f, 1.5f, 0.0f, -1.0f),
        masterGainDb = 0.0f,
        crossfeedIntensity = 0.75f,
        virtualizerStrength = 750,
        isAntiSibilanceEnabled = false
    )

    val MELANCHOLY_COMFORT = MoodProfile(
        id = "melancholy_comfort",
        name = "Melancholy / Comfort",
        emoji = "🌧️",
        psychologicalEffect = "Tube Warmth & Anti-Sibilance (4kHz–8kHz notch). Vintage soft texture, intimate & non-intrusive.",
        targetEqGainsDb = floatArrayOf(2.5f, 2.0f, 1.5f, 1.0f, 0.0f, -0.5f, -2.0f, -4.0f, -3.0f, -2.0f),
        masterGainDb = 1.0f,
        crossfeedIntensity = 0.50f,
        virtualizerStrength = 500,
        isAntiSibilanceEnabled = true
    )

    val ALL_MOODS = listOf(
        ENERGY_WORKOUT,
        RELAX_UNWIND,
        FOCUS_PRODUCTIVITY,
        MELANCHOLY_COMFORT
    )

    fun getById(id: String): MoodProfile? {
        return ALL_MOODS.firstOrNull { it.id.equals(id, ignoreCase = true) }
    }
}
