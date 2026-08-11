package com.audiophile.dsp.api

import android.util.Log
import com.audiophile.dsp.audio.LlmAudioEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

/**
 * Google Gemini AI API Client for Deep Musical Metadata & Acoustic DSP Analysis.
 * Interacts with Google Gemini (gemini-1.5-flash) REST API to infer optimal
 * 10-Band Parametric EQ curves, Matrix Crossfeed intensity, and Peak Limiter settings.
 */
object GeminiApiClient {

    private const val TAG = "GeminiApiClient"
    private const val GEMINI_API_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"

    data class GeminiAnalysisResult(
        val songTitle: String,
        val artist: String,
        val genre: String,
        val aiReasoning: String,
        val bandGainsDb: FloatArray,
        val masterGainDb: Float,
        val crossfeedIntensity: Float,
        val virtualizerStrength: Int,
        val isAntiSibilanceEnabled: Boolean
    )

    /**
     * Calls Google Gemini AI API to analyze song metadata and generate tailored DSP audio parameters.
     */
    suspend fun analyzeSongWithGemini(
        trackName: String,
        artistName: String,
        apiKey: String?
    ): GeminiAnalysisResult = withContext(Dispatchers.IO) {
        if (apiKey.isNull_or_blank_safe()) {
            Log.w(TAG, "No Gemini API key provided. Falling back to local AI audio engine.")
            return@withContext fallbackLocalAnalysis(trackName, artistName)
        }

        try {
            val url = URL("$GEMINI_API_ENDPOINT?key=$apiKey")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            conn.doOutput = true
            conn.connectTimeout = 8000
            conn.readTimeout = 8000

            val promptText = """
                Act as a Senior Mobile and Audio DSP Engineer. Analyze the song: "$trackName" by "$artistName".
                Provide the optimal 10-Band Parametric Equalizer gain settings in dB for the ISO frequencies: [31Hz, 62Hz, 125Hz, 250Hz, 500Hz, 1kHz, 2kHz, 4kHz, 8kHz, 16kHz].
                Gains must be numbers between -12.0 and 12.0.
                Provide matrix crossfeed intensity (0.0 to 1.0) and spatial virtualizer strength (0 to 1000 mB).
                Return ONLY a JSON object with this format:
                {
                  "genre": "Genre Name",
                  "reasoning": "Brief psychological & acoustic reasoning for these settings",
                  "gains": [31Hz, 62Hz, 125Hz, 250Hz, 500Hz, 1000Hz, 2000Hz, 4000Hz, 8000Hz, 16000Hz],
                  "masterGainDb": 1.0,
                  "crossfeed": 0.5,
                  "virtualizer": 500,
                  "antiSibilance": true
                }
            """.trimIndent()

            val requestJson = JSONObject().apply {
                put("contents", org.json.JSONArray().put(JSONObject().apply {
                    put("parts", org.json.JSONArray().put(JSONObject().apply {
                        put("text", promptText)
                    }))
                }))
            }

            OutputStreamWriter(conn.outputStream, "UTF-8").use { writer ->
                writer.write(requestJson.toString())
                writer.flush()
            }

            val responseCode = conn.responseCode
            if (responseCode == 200) {
                val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                val parsed = parseGeminiResponse(responseText, trackName, artistName)
                if (parsed != null) {
                    return@withContext parsed
                }
            } else {
                Log.e(TAG, "Gemini API HTTP Error $responseCode")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gemini API Connection failed: ${e.message}", e)
        }

        return@withContext fallbackLocalAnalysis(trackName, artistName)
    }

    private fun parseGeminiResponse(
        jsonString: String,
        trackName: String,
        artistName: String
    ): GeminiAnalysisResult? {
        try {
            val root = JSONObject(jsonString)
            val candidates = root.getJSONArray("candidates")
            if (candidates.length() > 0) {
                val content = candidates.getJSONObject(0).getJSONObject("content")
                val parts = content.getJSONArray("parts")
                val text = parts.getJSONObject(0).getString("text")

                // Extract JSON block inside markdown if present
                val cleanedText = text.substringAfter("```json")
                    .substringBefore("```")
                    .trim()

                val json = JSONObject(if (cleanedText.startsWith("{")) cleanedText else text)
                val genre = json.optString("genre", "AI Dynamic Genre")
                val reasoning = json.optString("reasoning", "Gemini AI analyzed track acoustic dynamics.")
                val gainsArray = json.getJSONArray("gains")

                val gains = FloatArray(10)
                for (i in 0 until 10) {
                    gains[i] = gainsArray.getDouble(i).toFloat().coerceIn(-12f, 12f)
                }

                val masterGain = json.optDouble("masterGainDb", 0.5).toFloat()
                val crossfeed = json.optDouble("crossfeed", 0.45).toFloat()
                val virtualizer = json.optInt("virtualizer", 450)
                val antiSibilance = json.optBoolean("antiSibilance", false)

                return GeminiAnalysisResult(
                    songTitle = trackName,
                    artist = artistName,
                    genre = genre,
                    aiReasoning = "✨ Gemini AI: $reasoning",
                    bandGainsDb = gains,
                    masterGainDb = masterGain,
                    crossfeedIntensity = crossfeed,
                    virtualizerStrength = virtualizer,
                    isAntiSibilanceEnabled = antiSibilance
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing Gemini JSON response: ${e.message}")
        }
        return null
    }

    private fun fallbackLocalAnalysis(trackName: String, artistName: String): GeminiAnalysisResult {
        val localRes = LlmAudioEngine.analyzePromptAndInferDsp("$trackName $artistName")
        return GeminiAnalysisResult(
            songTitle = trackName,
            artist = artistName,
            genre = "Audiophile Dynamic",
            aiReasoning = localRes.reasoning,
            bandGainsDb = localRes.bandGainsDb,
            masterGainDb = localRes.masterGainDb,
            crossfeedIntensity = localRes.crossfeedIntensity,
            virtualizerStrength = localRes.virtualizerStrength,
            isAntiSibilanceEnabled = localRes.isAntiSibilanceEnabled
        )
    }

    private fun String?.isNull_or_blank_safe(): Boolean {
        return this == null || this.trim().isEmpty()
    }
}
