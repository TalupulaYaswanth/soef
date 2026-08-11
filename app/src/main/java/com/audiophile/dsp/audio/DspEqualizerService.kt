package com.audiophile.dsp.audio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.audiophile.dsp.MainActivity
import com.audiophile.dsp.R
import com.audiophile.dsp.model.DspState
import com.audiophile.dsp.model.MoodProfiles
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DspEqualizerService : Service() {

    companion object {
        private const val TAG = "DspEqualizerService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "dsp_equalizer_service_channel"

        const val ACTION_START_SERVICE = "com.audiophile.dsp.ACTION_START_SERVICE"
        const val ACTION_STOP_SERVICE = "com.audiophile.dsp.ACTION_STOP_SERVICE"
        const val ACTION_ATTACH_SESSION = "com.audiophile.dsp.ACTION_ATTACH_SESSION"
        const val ACTION_DETACH_SESSION = "com.audiophile.dsp.ACTION_DETACH_SESSION"
        const val ACTION_UPDATE_METADATA = "com.audiophile.dsp.ACTION_UPDATE_METADATA"

        const val EXTRA_SESSION_ID = "extra_session_id"
        const val EXTRA_TRACK_NAME = "extra_track_name"
        const val EXTRA_ARTIST_NAME = "extra_artist_name"
        const val EXTRA_ALBUM_NAME = "extra_album_name"
    }

    inner class DspBinder : Binder() {
        fun getService(): DspEqualizerService = this@DspEqualizerService
    }

    private val binder = DspBinder()
    private val dynamicsEngine = DynamicsEngine()
    private val crossfeedEngine = CrossfeedEngine()

    private val activeSessionIds = mutableSetOf<Int>()

    private val _dspState = MutableStateFlow(DspState())
    val dspState: StateFlow<DspState> = _dspState.asStateFlow()

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "DspEqualizerService onCreate initialized")
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        // Initial default binding to Session 0 (Global System Audio)
        attachSession(0)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && intent.action != null) {
            val action = intent.action!!
            when (action) {
                ACTION_START_SERVICE -> {
                    Log.i(TAG, "Service started explicitly")
                }
                ACTION_STOP_SERVICE -> {
                    Log.i(TAG, "Service stop requested")
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
                ACTION_ATTACH_SESSION -> {
                    val sessionId = intent.getIntExtra(EXTRA_SESSION_ID, -1)
                    if (sessionId >= 0) {
                        attachSession(sessionId)
                    } else {
                        Log.w(TAG, "Invalid session id: $sessionId")
                    }
                }
                ACTION_DETACH_SESSION -> {
                    val sessionId = intent.getIntExtra(EXTRA_SESSION_ID, -1)
                    if (sessionId > 0) {
                        detachSession(sessionId)
                    } else {
                        Log.w(TAG, "Invalid session id: $sessionId")
                    }
                }
                ACTION_UPDATE_METADATA -> {
                    val track = intent.getStringExtra(EXTRA_TRACK_NAME)
                    val artist = intent.getStringExtra(EXTRA_ARTIST_NAME)
                    val album = intent.getStringExtra(EXTRA_ALBUM_NAME)
                    handleMetadataUpdate(track, artist, album)
                }
                else -> {
                    Log.d(TAG, "Unhandled action: $action")
                }
            }
        }
        return START_STICKY
    }

    private fun handleMetadataUpdate(track: String?, artist: String?, album: String?) {
        val metadata = TrackAudioAnalyzer.analyzeTrack(track, artist, album)
        Log.i(TAG, "Detected Track Metadata: ${metadata.trackName} - ${metadata.artistName} [Genre: ${metadata.detectedGenre}]")

        val currentState = _dspState.value
        if (currentState.isAutoSongMoodEnabled) {
            val mood = MoodProfiles.getById(metadata.autoInferredMoodId)
            if (mood != null) {
                updateState {
                    it.copy(
                        activeMoodId = mood.id,
                        selectedProfile = "Auto-Song: ${metadata.detectedGenre}",
                        bandGainsDb = mood.targetEqGainsDb.clone(),
                        masterGainDb = mood.masterGainDb,
                        crossfeedIntensity = mood.crossfeedIntensity,
                        virtualizerStrength = mood.virtualizerStrength,
                        isAntiSibilanceEnabled = mood.isAntiSibilanceEnabled
                    )
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    fun updateState(transform: (DspState) -> DspState) {
        val newState = transform(_dspState.value)
        _dspState.update { newState }
        applyStateToEngines(newState)
    }

    private fun attachSession(sessionId: Int) {
        if (!activeSessionIds.contains(sessionId)) {
            activeSessionIds.add(sessionId)
            val currentState = _dspState.value

            Log.i(TAG, "Attaching audio engines to Session ID: $sessionId")
            dynamicsEngine.attachToSession(sessionId, currentState)
            crossfeedEngine.attachToSession(sessionId, currentState.virtualizerStrength)
        }
    }

    private fun detachSession(sessionId: Int) {
        if (sessionId != 0 && activeSessionIds.contains(sessionId)) {
            activeSessionIds.remove(sessionId)
            Log.i(TAG, "Detached Session ID: $sessionId")
        }
    }

    private fun applyStateToEngines(state: DspState) {
        dynamicsEngine.setEnabled(state.isEnabled)
        crossfeedEngine.setEnabled(state.isEnabled)

        if (state.isEnabled) {
            dynamicsEngine.updateBandGains(state.bandGainsDb, state.masterGainDb)
            dynamicsEngine.updateLimiter(state.limiterEnabled, state.limiterThresholdDb, state.limiterRatio)
            crossfeedEngine.setStrength(state.virtualizerStrength)
        }

        // Check clipping condition
        val maxGain = state.bandGainsDb.maxOrNull() ?: 0f
        val totalPeak = maxGain + state.masterGainDb
        val isClipping = totalPeak > 6.0f // Peak threshold indicator
        if (isClipping != state.isClipping) {
            _dspState.update { it.copy(isClipping = isClipping) }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Audiophile DSP Equalizer Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps DAC/Amp audio equalizer and spatial crossfeed active system-wide."
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Audiophile DSP Engine Active")
            .setContentText("10-Band Parametric EQ & Matrix Crossfeed Enabled")
            .setSmallIcon(android.R.drawable.sym_def_app_icon)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "DspEqualizerService onDestroy")
        dynamicsEngine.release()
        crossfeedEngine.release()
        serviceJob.cancel()
    }
}
