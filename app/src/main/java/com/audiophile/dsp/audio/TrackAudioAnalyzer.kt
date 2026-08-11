package com.audiophile.dsp.audio

import com.audiophile.dsp.model.MoodProfiles
import java.util.Locale

/**
 * TrackAudioAnalyzer inspects song metadata (track title, artist, album, genre cues)
 * and infers the musical genre and optimal research-backed DSP effect profile automatically.
 */
object TrackAudioAnalyzer {

    data class TrackMetadata(
        val trackName: String = "Unknown Track",
        val artistName: String = "Unknown Artist",
        val albumName: String = "Unknown Album",
        val detectedGenre: String = "Universal Audiophile",
        val autoInferredMoodId: String = MoodProfiles.ENERGY_WORKOUT.id
    )

    /**
     * Analyzes track information and classifies genre & target mood DSP profile.
     */
    fun analyzeTrack(track: String?, artist: String?, album: String?): TrackMetadata {
        val name = if (!track.isNull_or_blank_safe()) track!! else "Live Audio Stream"
        val art = if (!artist.isNull_or_blank_safe()) artist!! else "Active Media Player"
        val alb = if (!album.isNull_or_blank_safe()) album!! else "System Output"

        val combinedText = "$name $art $alb".lowercase(Locale.US)

        val (genre, moodId) = when {
            combinedText.contains("edm") || combinedText.contains("dance") || combinedText.contains("house") || combinedText.contains("techno") || combinedText.contains("dubstep") || combinedText.contains("bass") || combinedText.contains("remix") -> {
                "EDM / Electronic" to MoodProfiles.ENERGY_WORKOUT.id
            }
            combinedText.contains("rock") || combinedText.contains("metal") || combinedText.contains("punk") || combinedText.contains("workout") || combinedText.contains("run") -> {
                "Rock / Heavy Impact" to MoodProfiles.ENERGY_WORKOUT.id
            }
            combinedText.contains("hip-hop") || combinedText.contains("hiphop") || combinedText.contains("rap") || combinedText.contains("trap") -> {
                "Hip-Hop / Urban Bass" to MoodProfiles.ENERGY_WORKOUT.id
            }
            combinedText.contains("lofi") || combinedText.contains("lo-fi") || combinedText.contains("chill") || combinedText.contains("rain") || combinedText.contains("sleep") -> {
                "Lo-Fi / Chillout" to MoodProfiles.RELAX_UNWIND.id
            }
            combinedText.contains("jazz") || combinedText.contains("blues") || combinedText.contains("soul") || combinedText.contains("r&b") -> {
                "Smooth Jazz & Soul" to MoodProfiles.RELAX_UNWIND.id
            }
            combinedText.contains("acoustic") || combinedText.contains("unplugged") || combinedText.contains("piano") || combinedText.contains("vocal") -> {
                "Acoustic & Vocal Intimacy" to MoodProfiles.FOCUS_PRODUCTIVITY.id
            }
            combinedText.contains("study") || combinedText.contains("focus") || combinedText.contains("ambient") || combinedText.contains("instrumental") -> {
                "Focus & Ambient" to MoodProfiles.FOCUS_PRODUCTIVITY.id
            }
            combinedText.contains("symphony") || combinedText.contains("classical") || combinedText.contains("orchestra") || combinedText.contains("concerto") -> {
                "Classical & Symphony" to MoodProfiles.MELANCHOLY_COMFORT.id
            }
            combinedText.contains("sad") || combinedText.contains("rainy") || combinedText.contains("ballad") || combinedText.contains("slow") -> {
                "Melancholy / Comfort" to MoodProfiles.MELANCHOLY_COMFORT.id
            }
            else -> {
                "Universal Audiophile" to MoodProfiles.ENERGY_WORKOUT.id
            }
        }

        return TrackMetadata(
            trackName = name,
            artistName = art,
            albumName = alb,
            detectedGenre = genre,
            autoInferredMoodId = moodId
        )
    }

    private fun String?.isNull_or_blank_safe(): Boolean {
        return this == null || this.trim().isEmpty() || this.equals("unknown", ignoreCase = true)
    }
}
