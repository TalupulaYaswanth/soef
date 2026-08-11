package com.audiophile.dsp.audio

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.util.Log

/**
 * BroadcastReceiver that captures system-wide audio session registration intents
 * emitted by media playback applications (e.g. Spotify, YouTube, Poweramp, Apple Music).
 */
class AudioSessionReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AudioSessionReceiver"
        const val ACTION_OPEN_AUDIO_EFFECT = "android.media.action.OPEN_AUDIO_EFFECT_SESSION"
        const val ACTION_CLOSE_AUDIO_EFFECT = "android.media.action.CLOSE_AUDIO_EFFECT_SESSION"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val action = intent.action ?: return
        val sessionId = intent.getIntExtra(AudioEffect.EXTRA_AUDIO_SESSION, AudioEffect.ERROR_BAD_VALUE)
        val packageName = intent.getStringExtra(AudioEffect.EXTRA_PACKAGE_NAME) ?: "Unknown"

        Log.d(TAG, "Audio Effect Session Broadcast received: Action=$action, SessionId=$sessionId, Package=$packageName")

        if (sessionId == AudioEffect.ERROR_BAD_VALUE || sessionId < 0) {
            return
        }

        when (action) {
            ACTION_OPEN_AUDIO_EFFECT -> {
                Log.i(TAG, "Opening DSP for package $packageName on session $sessionId")
                val serviceIntent = Intent(context, DspEqualizerService::class.java).apply {
                    this.action = DspEqualizerService.ACTION_ATTACH_SESSION
                    putExtra(DspEqualizerService.EXTRA_SESSION_ID, sessionId)
                }
                context.startService(serviceIntent)
            }
            ACTION_CLOSE_AUDIO_EFFECT -> {
                Log.i(TAG, "Closing DSP session $sessionId for package $packageName")
                val serviceIntent = Intent(context, DspEqualizerService::class.java).apply {
                    this.action = DspEqualizerService.ACTION_DETACH_SESSION
                    putExtra(DspEqualizerService.EXTRA_SESSION_ID, sessionId)
                }
                context.startService(serviceIntent)
            }
        }
    }
}
