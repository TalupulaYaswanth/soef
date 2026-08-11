package com.audiophile.dsp.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.ViewModel
import com.audiophile.dsp.audio.DspEqualizerService
import com.audiophile.dsp.model.DacProfiles
import com.audiophile.dsp.model.DspState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel : ViewModel() {

    companion object {
        private const val TAG = "MainViewModel"
    }

    private var dspService: DspEqualizerService? = null
    private var isBound = false

    private val _uiState = MutableStateFlow(DspState())
    val uiState: StateFlow<DspState> = _uiState.asStateFlow()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.i(TAG, "Service connected to MainViewModel")
            val binder = service as DspEqualizerService.DspBinder
            dspService = binder.getService()
            isBound = true

            // Sync state with Service
            dspService?.dspState?.value?.let { state ->
                _uiState.value = state
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.w(TAG, "Service disconnected")
            dspService = null
            isBound = false
        }
    }

    fun bindService(context: Context) {
        val intent = Intent(context, DspEqualizerService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun unbindService(context: Context) {
        if (isBound) {
            context.unbindService(serviceConnection)
            isBound = false
        }
    }

    fun toggleEnabled(enabled: Boolean) {
        updateState { it.copy(isEnabled = enabled) }
    }

    fun setBandGain(index: Int, gainDb: Float) {
        val gains = _uiState.value.bandGainsDb.clone()
        if (index in gains.indices) {
            gains[index] = gainDb
            updateState { it.copy(bandGainsDb = gains, selectedProfile = "Custom") }
        }
    }

    fun setMasterGain(gainDb: Float) {
        updateState { it.copy(masterGainDb = gainDb) }
    }

    fun setCrossfeedIntensity(intensity: Float) {
        updateState { it.copy(crossfeedIntensity = intensity) }
    }

    fun setVirtualizerStrength(strength: Int) {
        updateState { it.copy(virtualizerStrength = strength) }
    }

    fun selectPreset(presetName: String) {
        val profile = DacProfiles.getByName(presetName)
        updateState {
            it.copy(
                selectedProfile = profile.name,
                bandGainsDb = profile.bandGainsDb.clone(),
                masterGainDb = profile.masterGainDb,
                crossfeedIntensity = profile.crossfeedIntensity,
                virtualizerStrength = profile.virtualizerStrength
            )
        }
    }

    fun toggleLimiter(enabled: Boolean) {
        updateState { it.copy(limiterEnabled = enabled) }
    }

    private fun updateState(transform: (DspState) -> DspState) {
        val newState = transform(_uiState.value)
        _uiState.value = newState
        dspService?.updateState(transform)
    }
}
