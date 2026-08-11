package com.audiophile.dsp.audio

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.util.Log

/**
 * BroadcastReceiver that captures system-wide audio session registration intents
 * as well as real-time song metadata broadcasts emitted by music players.
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
        Log.d(TAG, "Broadcast received: Action=$action")

        when {
            action == ACTION_OPEN_AUDIO_EFFECT -> {
                val sessionId = intent.getIntExtra(AudioEffect.EXTRA_AUDIO_SESSION, AudioEffect.ERROR_BAD_VALUE)
                val packageName = intent.getStringExtra(AudioEffect.EXTRA_PACKAGE_NAME) ?: "Unknown"
                if (sessionId >= 0) {
                    Log.i(TAG, "Opening DSP session $sessionId for package $packageName")
                    val serviceIntent = Intent(context, DspEqualizerService::class.java).apply {
                        this.action = DspEqualizerService.ACTION_ATTACH_SESSION
                        putExtra(DspEqualizerService.EXTRA_SESSION_ID, sessionId)
                    }
                    context.startService(serviceIntent)
                }
            }
            action == ACTION_CLOSE_AUDIO_EFFECT -> {
                val sessionId = intent.getIntExtra(AudioEffect.EXTRA_AUDIO_SESSION, AudioEffect.ERROR_BAD_VALUE)
                if (sessionId > 0) {
                    Log.i(TAG, "Closing DSP session $sessionId")
                    val serviceIntent = Intent(context, DspEqualizerService::class.java).apply {
                        this.action = DspEqualizerService.ACTION_DETACH_SESSION
                        putExtra(DspEqualizerService.EXTRA_SESSION_ID, sessionId)
                    }
                    context.startService(serviceIntent)
                }
            }
            action.endsWith(".metachanged") || action.contains("metachanged") -> {
                val track = intent.getStringExtra("track") ?: intent.getStringExtra("trackName") ?: intent.getStringExtra("title")
                val artist = intent.getStringExtra("artist") ?: intent.getStringExtra("artistName")
                val album = intent.getStringExtra("album") ?: intent.getStringExtra("albumName")

                Log.i(TAG, "Song metadata broadcast: Track=$track, Artist=$artist, Album=$album")
                val serviceIntent = Intent(context, DspEqualizerService::class.java).apply {
                    this.action = DspEqualizerService.ACTION_UPDATE_METADATA
                    putExtra(DspEqualizerService.EXTRA_TRACK_NAME, track)
                    putExtra(DspEqualizerService.EXTRA_ARTIST_NAME, artist)
                    putExtra(DspEqualizerService.EXTRA_ALBUM_NAME, album)
                }
                context.startService(serviceIntent)
            }
        }
    }
}
