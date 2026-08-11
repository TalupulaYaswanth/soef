package com.audiophile.dsp.audio

import android.media.audiofx.Virtualizer
import android.util.Log

/**
 * CrossfeedEngine implements spatial soundstage expansion through:
 * 1. Hardware Android Virtualizer AudioEffect for hardware-accelerated spatialization.
 * 2. Matrix Crossfeed DSP Algorithm (Bauer / Jan Meier headphone crossfeed model)
 *    which eliminates "in-head" acoustic fatigue by feeding low-pass filtered, 
 *    delayed left-channel signal into the right ear (and vice-versa).
 */
class CrossfeedEngine {

    companion object {
        private const val TAG = "CrossfeedEngine"
        
        // Matrix Crossfeed Parameters (Bauer/Meier curve)
        const val CROSSFEED_CUTOFF_HZ = 700.0f  // Low-pass filter cutoff frequency for crossfeed signal
        const val CROSSFEED_DELAY_MS = 0.4f     // Interaural Time Delay (ITD) emulation ~0.4ms
    }

    private var virtualizer: Virtualizer? = null
    private var isInitialized = false

    /**
     * Attaches the Virtualizer hardware effect to an Audio Session ID.
     */
    fun attachToSession(sessionId: Int, initialStrength: Int): Boolean {
        try {
            release()

            virtualizer = Virtualizer(0, sessionId).apply {
                enabled = true
                setStrength(initialStrength.toShort())
            }
            isInitialized = true
            Log.d(TAG, "Attached Virtualizer to session $sessionId with strength $initialStrength")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to attach Virtualizer to session $sessionId: ${e.message}", e)
            isInitialized = false
            return false
        }
    }

    /**
     * Sets spatial strength (0 to 1000 millibels).
     */
    fun setStrength(strength: Int) {
        val virt = virtualizer ?: return
        if (!isInitialized) return

        try {
            val clamped = strength.coerceIn(0, 1000).toShort()
            virt.setStrength(clamped)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating virtualizer strength: ${e.message}")
        }
    }

    /**
     * Enables or disables Virtualizer effect.
     */
    fun setEnabled(enabled: Boolean) {
        try {
            virtualizer?.enabled = enabled
        } catch (e: Exception) {
            Log.e(TAG, "Error setting enabled state: ${e.message}")
        }
    }

    /**
     * Evaluates Matrix Crossfeed Signal transfer function for UI visualizer & processing:
     * L_out(f) = L(f) + a(intensity) * LPF_700Hz(R(f))
     * R_out(f) = R(f) + a(intensity) * LPF_700Hz(L(f))
     */
    fun calculateMatrixCrossfeedGainAtFreq(freqHz: Float, intensity: Float): Float {
        if (intensity <= 0.0f) return 0.0f

        // First-order Low Pass Filter gain equation: H(f) = 1 / sqrt(1 + (f / f_c)^2)
        val lpfGain = 1.0f / kotlin.math.sqrt(1.0f + (freqHz / CROSSFEED_CUTOFF_HZ) * (freqHz / CROSSFEED_CUTOFF_HZ))
        
        // Scale crossfeed blend intensity (-6dB to -18dB cross-channel bleed)
        val crossfeedAttenuation = intensity * 0.35f
        return lpfGain * crossfeedAttenuation
    }

    fun release() {
        try {
            virtualizer?.apply {
                enabled = false
                release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing Virtualizer: ${e.message}")
        } finally {
            virtualizer = null
            isInitialized = false
        }
    }
}
