package com.audiophile.dsp

import android.app.Application
import android.content.Intent
import android.os.Build
import com.audiophile.dsp.audio.DspEqualizerService

class AudioDspApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startDspService()
    }

    fun startDspService() {
        val intent = Intent(this, DspEqualizerService::class.java).apply {
            action = DspEqualizerService.ACTION_START_SERVICE
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}
