# Audiophile System-Wide DSP Equalizer & Sound Enhancer

A high-performance Android System-Wide DSP Equalizer, DAC/Amp Sound Enhancer, Spatial Crossfeed Virtualizer, and Peak Limiter app written in **Kotlin** and **Jetpack Compose**.

---

## Technical Features

1. **Rootless System-Wide Audio DSP**:
   - Uses native `android.media.audiofx.DynamicsProcessing` (API 28+ / Android 9.0+) targeting Session ID `0` and catching dynamic session IDs via `AudioEffect.ACTION_OPEN_AUDIO_EFFECT_SESSION`.
   - Runs as a persistent **Foreground Service** with `mediaPlayback` type.

2. **10-Band Parametric Equalizer & Zero-Clipping Limiter**:
   - **10 ISO Frequencies**: 31Hz, 62Hz, 125Hz, 250Hz, 500Hz, 1kHz, 2kHz, 4kHz, 8kHz, 16kHz.
   - **Peak Limiter Safeguard**: `-0.5 dBFS` threshold, 1.0ms attack, 50.0ms release, 10:1 ratio.

3. **Spatial Virtualizer & Matrix Crossfeed Algorithm**:
   - **Bauer Matrix Crossfeed Model**: Interchannel low-pass filtered crossfeed under 700Hz with ~0.4ms time delay to eliminate headphone fatigue.
   - **Hardware Virtualizer**: Android `Virtualizer` effect (0 to 1000 mB depth).

4. **Jetpack Compose Modern Audiophile UI**:
   - Dark DAC/Amp visual layout with live spectrum curve visualizer (`SpectrumGraph.kt`).
   - DAC sound tuning presets (`ESS Sabre Reference`, `Warm Tube Amp`, `Spatial Soundstage`, `Flat Audiophile`, `Bass Cannon`).
   - Real-time **CLIPPING** indicator LED.

---

## How to Build & Run

### Method 1: Android Studio (Recommended)
1. Open Android Studio.
2. Select **Open** -> `c:\Users\V\OneDrive\ドキュメント\Rockstar Games\sound_eff_project`.
3. Android Studio uses its built-in JDK 17 (JBR) to sync and build automatically.
4. Press **Run (Shift + F10)**.

### Method 2: VS Code / Terminal
1. Open project folder in VS Code.
2. If your default system Java is Java 25, ensure JDK 17 or JDK 21 is set for Gradle by adding `org.gradle.java.home=C:\\path\\to\\jdk17` in `gradle.properties`.
3. Run build command:
   ```powershell
   java -classpath "gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain assembleDebug
   ```
4. Install to phone:
   ```powershell
   & "C:\Users\V\AppData\Local\Android\Sdk\platform-tools\adb.exe" install -r app/build/outputs/apk/debug/app-debug.apk
   ```
