package com.audiophile.dsp.audio

import android.media.audiofx.DynamicsProcessing
import android.util.Log
import com.audiophile.dsp.model.DspState
import com.audiophile.dsp.model.ISO_FREQUENCIES_HZ

/**
 * DynamicsEngine encapsulates Android's native DynamicsProcessing AudioEffect API.
 * It provides hardware/kernel level 10-Band Parametric Equalization and Peak Limiting
 * across system-wide audio output sessions without requiring root access.
 */
class DynamicsEngine {

    companion object {
        private const val TAG = "DynamicsEngine"
        const val BAND_COUNT = 10
        const val CHANNEL_COUNT = 2 // Stereo L + R
    }

    private var dynamicsProcessing: DynamicsProcessing? = null
    private var currentSessionId: Int = 0
    private var isInitialized = false

    /**
     * Initializes or re-binds the DynamicsProcessing engine to a specific Audio Session ID.
     * Session ID 0 represents global system-wide output.
     */
    fun attachToSession(sessionId: Int, state: DspState): Boolean {
        try {
            release()
            
            this.currentSessionId = sessionId
            val config = buildDynamicsConfig(state)

            // Priority = 0, Audio Session ID
            dynamicsProcessing = DynamicsProcessing(0, sessionId, config).apply {
                enabled = state.isEnabled
            }

            isInitialized = true
            Log.d(TAG, "Successfully attached DynamicsProcessing to Audio Session ID: $sessionId")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to attach DynamicsProcessing to session $sessionId: ${e.message}", e)
            isInitialized = false
            return false
        }
    }

    /**
     * Constructs a multi-stage DynamicsProcessing Configuration consisting of:
     * 1. PreEQ (10-Band ISO Parametric Equalizer)
     * 2. Limiter (Zero Clipping Safeguard Stage)
     */
    private fun buildDynamicsConfig(state: DspState): DynamicsProcessing.Config {
        // PreEQ Config
        val eqConfig = DynamicsProcessing.Eq(
            /* inUse */ true,
            /* enabled */ true,
            /* bandCount */ BAND_COUNT
        )

        for (bandIdx in 0 until BAND_COUNT) {
            val freq = ISO_FREQUENCIES_HZ[bandIdx].toFloat()
            val gainDb = state.bandGainsDb.getOrElse(bandIdx) { 0f } + state.masterGainDb
            val band = DynamicsProcessing.EqBand(
                /* inUse */ true,
                /* cutoffFrequency */ freq
            ).apply {
                gain = gainDb
            }
            eqConfig.setBand(bandIdx, band)
        }

        // Limiter Config
        val limiterConfig = DynamicsProcessing.Limiter(
            /* inUse */ state.limiterEnabled,
            /* enabled */ state.limiterEnabled,
            /* linkChannels */ true,
            /* channelIndex */ 0,
            /* attackTime */ 1.0f,    // 1ms fast attack
            /* releaseTime */ 50.0f,   // 50ms smooth release
            /* ratio */ state.limiterRatio, // Hard limit 10:1 ratio
            /* threshold */ state.limiterThresholdDb, // -0.5 dBFS
            /* postGain */ 0.0f
        )

        // Builder setup: PreEQ inUse, PreEQ bandCount, MBC inUse, MBC bandCount, PostEQ inUse, PostEQ bandCount, Limiter inUse
        val builder = DynamicsProcessing.Config.Builder(
            DynamicsProcessing.CONFIG_PREFERRED_VARIANT_MULTIBAND_DEFAULT,
            CHANNEL_COUNT,
            /* PreEQ */ true, BAND_COUNT,
            /* MBC */ false, 0,
            /* PostEQ */ false, 0,
            /* Limiter */ state.limiterEnabled
        )

        // Apply channel 0 & 1 (Left and Right Stereo)
        for (ch in 0 until CHANNEL_COUNT) {
            builder.setPreEqByChannelIndex(ch, eqConfig)
            if (state.limiterEnabled) {
                builder.setLimiterByChannelIndex(ch, limiterConfig)
            }
        }

        return builder.build()
    }

    /**
     * Updates 10-Band EQ Gains dynamically in real time without audio pops.
     */
    fun updateBandGains(gainsDb: FloatArray, masterGainDb: Float) {
        val dp = dynamicsProcessing ?: return
        if (!isInitialized) return

        try {
            for (bandIdx in 0 until BAND_COUNT) {
                val gainDb = gainsDb.getOrElse(bandIdx) { 0f } + masterGainDb
                for (ch in 0 until CHANNEL_COUNT) {
                    dp.setPreEqBandAllChannelsTo(bandIdx, DynamicsProcessing.EqBand(true, ISO_FREQUENCIES_HZ[bandIdx].toFloat()).apply {
                        gain = gainDb
                    })
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating band gains: ${e.message}")
        }
    }

    /**
     * Enables or disables the DSP processing engine.
     */
    fun setEnabled(enabled: Boolean) {
        try {
            dynamicsProcessing?.enabled = enabled
        } catch (e: Exception) {
            Log.e(TAG, "Error setting enabled state: ${e.message}")
        }
    }

    /**
     * Updates Limiter Threshold & Ratio dynamically.
     */
    fun updateLimiter(enabled: Boolean, thresholdDb: Float, ratio: Float) {
        val dp = dynamicsProcessing ?: return
        if (!isInitialized) return

        try {
            val limiterConfig = DynamicsProcessing.Limiter(
                enabled, enabled, true, 0, 1.0f, 50.0f, ratio, thresholdDb, 0.0f
            )
            for (ch in 0 until CHANNEL_COUNT) {
                dp.setLimiterByChannelIndex(ch, limiterConfig)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating limiter: ${e.message}")
        }
    }

    /**
     * Releases DSP hardware & system resources safely.
     */
    fun release() {
        try {
            dynamicsProcessing?.apply {
                enabled = false
                release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing DynamicsProcessing: ${e.message}")
        } finally {
            dynamicsProcessing = null
            isInitialized = false
        }
    }

    fun isAttached(): Boolean = isInitialized
}
