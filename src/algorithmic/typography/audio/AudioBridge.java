/**
 * AudioBridge - Audio-reactive typography for live performances.
 *
 * Connects audio input to typography parameters using Processing's
 * Sound library. FFT analysis extracts bass, mids, and treble
 * frequencies that drive wave amplitude, color saturation, and
 * grid dynamics.
 *
 * Requires: Sound library (bundled with Processing 4)
 *   Sketch > Import Library > Sound
 *
 * @author Michail Semoglou
 * @version 1.0.0
 * @since 1.0.0
 */

package algorithmic.typography.audio;

import processing.core.*;
import algorithmic.typography.*;
import algorithmic.typography.core.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * AudioBridge connects audio input to typography animation.
 *
 * <p>Uses the Processing Sound library for real-time audio analysis.
 * All audio objects are loaded via reflection so the library compiles
 * without Sound on the classpath.</p>
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Real-time FFT analysis (bass, mid, treble bands)</li>
 *   <li>Beat detection for rhythmic typography</li>
 *   <li>Smooth parameter mapping with attack/decay</li>
 *   <li>Amplitude monitoring for overall level</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>
 * AudioBridge audio = new AudioBridge(this, config);
 * audio.useMicrophone();
 * audio.mapBassTo(config::setWaveSpeed, 2, 10);
 * audio.mapTrebleTo(config::setBrightnessMax, 180, 255);
 * </pre>
 */
public class AudioBridge {

  private final PApplet parent;
  private final ObservableConfiguration config;

  // Sound library objects (via reflection to avoid compile dependency)
  private Object audioIn;       // processing.sound.AudioIn
  private Object fft;           // processing.sound.FFT
  private Object amplitude;     // processing.sound.Amplitude

  // FFT settings
  private static final int FFT_BANDS = 512;

  // Audio analysis
  private boolean isAudioInitialized = false;
  private float bassLevel = 0;
  private float midLevel = 0;
  private float trebleLevel = 0;
  private float overallLevel = 0;

  // Auto-calibrating peak tracking (normalises raw FFT to 0-1)
  private float bassPeak = 0.001f;
  private float midPeak  = 0.001f;
  private float treblePeak = 0.001f;
  private static final float PEAK_DECAY = 0.995f;

  // Smoothing
  private float attack = 0.3f;   // How fast parameters react
  private float decay  = 0.1f;   // How fast parameters fade

  // Parameter mappings
  private final Map<String, ParameterMapping> mappings = new HashMap<>();

  // Beat detection
  private boolean beatDetected = false;
  private float beatThreshold = 0.5f;
  private int lastBeatTime = 0;
  private int beatCooldown = 200; // ms

  /**
   * Creates an AudioBridge.
   *
   * @param parent the parent PApplet
   * @param config the configuration to modify
   */
  public AudioBridge(PApplet parent, ObservableConfiguration config) {
    this.parent = parent;
    this.config = config;
  }

  /**
   * Initializes audio from microphone input.
   *
   * @return true if initialized successfully
   */
  public boolean useMicrophone() {
    return initializeAudio();
  }

  /**
   * Initializes audio from line input.
   * (Sound library treats line-in the same as mic.)
   *
   * @return true if initialized successfully
   */
  public boolean useLineIn() {
    return initializeAudio();
  }

  /**
   * Initializes the Processing Sound library.
   */
  private boolean initializeAudio() {
    try {
      Class<?> audioInClass = Class.forName("processing.sound.AudioIn");
      Class<?> fftClass     = Class.forName("processing.sound.FFT");
      Class<?> ampClass     = Class.forName("processing.sound.Amplitude");

      // AudioIn(PApplet parent) — find constructor by assignability
      java.lang.reflect.Constructor<?> aiCtor = findConstructor(audioInClass, parent);
      audioIn = aiCtor.newInstance(parent);

      // AudioIn.start()
      audioIn.getClass().getMethod("start").invoke(audioIn);

      // FFT(PApplet parent, int bands)
      java.lang.reflect.Constructor<?> fftCtor = findConstructor(fftClass, parent, FFT_BANDS);
      fft = fftCtor.newInstance(parent, FFT_BANDS);

      // FFT.input(SoundObject source)  — SoundObject is the base type
      invokeInput(fft, audioIn);

      // Amplitude(PApplet parent)
      java.lang.reflect.Constructor<?> ampCtor = findConstructor(ampClass, parent);
      amplitude = ampCtor.newInstance(parent);
      invokeInput(amplitude, audioIn);

      isAudioInitialized = true;
      parent.println("AudioBridge initialised (Sound library, " + FFT_BANDS + " bands)");
      return true;

    } catch (ClassNotFoundException e) {
      parent.println("AudioBridge: Sound library not found");
      parent.println("  Install: Sketch > Import Library > Manage Libraries > Sound");
      return false;
    } catch (Exception e) {
      parent.println("AudioBridge init error: " + e.getClass().getSimpleName()
                      + " - " + e.getMessage());
      return false;
    }
  }

  // ---- Reflection helpers ------------------------------------------------

  /**
   * Finds a constructor matching (PApplet) by assignability.
   */
  private java.lang.reflect.Constructor<?> findConstructor(
      Class<?> cls, PApplet p) throws NoSuchMethodException {
    for (java.lang.reflect.Constructor<?> c : cls.getConstructors()) {
      Class<?>[] params = c.getParameterTypes();
      if (params.length == 1 && params[0].isAssignableFrom(p.getClass())) {
        return c;
      }
    }
    throw new NoSuchMethodException(
        "No compatible " + cls.getSimpleName() + "(PApplet) constructor");
  }

  /**
   * Finds a constructor matching (PApplet, int) by assignability.
   */
  private java.lang.reflect.Constructor<?> findConstructor(
      Class<?> cls, PApplet p, int bands) throws NoSuchMethodException {
    for (java.lang.reflect.Constructor<?> c : cls.getConstructors()) {
      Class<?>[] params = c.getParameterTypes();
      if (params.length == 2
          && params[0].isAssignableFrom(p.getClass())
          && (params[1] == int.class || params[1] == Integer.class)) {
        return c;
      }
    }
    throw new NoSuchMethodException(
        "No compatible " + cls.getSimpleName() + "(PApplet, int) constructor");
  }

  /**
   * Calls {@code target.input(source)} finding the method by assignability.
   */
  private void invokeInput(Object target, Object source) throws Exception {
    for (java.lang.reflect.Method m : target.getClass().getMethods()) {
      if (m.getName().equals("input") && m.getParameterCount() == 1
          && m.getParameterTypes()[0].isAssignableFrom(source.getClass())) {
        m.invoke(target, source);
        return;
      }
    }
    throw new NoSuchMethodException(
        "No compatible input() on " + target.getClass().getSimpleName());
  }

  // ---- Update loop -------------------------------------------------------

  // Log the first update error once for diagnostics, then go silent
  private boolean updateErrorLogged = false;

  /**
   * Updates audio analysis. Call this in draw().
   */
  public void update() {
    if (!isAudioInitialized) return;

    try {
      // Run FFT analysis
      fft.getClass().getMethod("analyze").invoke(fft);

      // Read the spectrum into a float[]
      float[] spectrum = (float[]) fft.getClass().getField("spectrum")
                                      .get(fft);

      // Analyze bands from the spectrum
      analyzeBands(spectrum);

      // Overall level from Amplitude analyser
      overallLevel = smooth(overallLevel,
          (float) amplitude.getClass().getMethod("analyze").invoke(amplitude));

      // Beat detection
      detectBeat();

      // Apply mappings
      applyMappings();

    } catch (Exception e) {
      if (!updateErrorLogged) {
        parent.println("AudioBridge update error: "
            + e.getClass().getSimpleName() + " - " + e.getMessage());
        updateErrorLogged = true;
      }
    }
  }

  /**
   * Analyzes frequency bands from the FFT spectrum.
   *
   * <p>Splits the spectrum into bass (bins 1-10), mid (11-100),
   * and treble (101+) and averages each group.  Results are
   * auto-calibrated with peak tracking so the output stays 0-1.</p>
   */
  private void analyzeBands(float[] spectrum) {
    if (spectrum == null || spectrum.length == 0) return;

    // Bass: low bins 1–10  (skip bin 0 which is DC offset)
    int bassEnd   = Math.min(10, spectrum.length);
    int midEnd    = Math.min(100, spectrum.length);
    int trebleEnd = spectrum.length;

    float rawBass   = avgBins(spectrum, 1, bassEnd);
    float rawMid    = avgBins(spectrum, bassEnd, midEnd);
    float rawTreble = avgBins(spectrum, midEnd, trebleEnd);

    bassPeak   = Math.max(bassPeak   * PEAK_DECAY, rawBass);
    midPeak    = Math.max(midPeak    * PEAK_DECAY, rawMid);
    treblePeak = Math.max(treblePeak * PEAK_DECAY, rawTreble);

    bassLevel   = smooth(bassLevel,   (bassPeak   > 0) ? rawBass   / bassPeak   : 0);
    midLevel    = smooth(midLevel,    (midPeak    > 0) ? rawMid    / midPeak    : 0);
    trebleLevel = smooth(trebleLevel, (treblePeak > 0) ? rawTreble / treblePeak : 0);
  }

  /**
   * Averages spectrum bins in range [from, to).
   */
  private float avgBins(float[] spectrum, int from, int to) {
    if (from >= to) return 0;
    float sum = 0;
    for (int i = from; i < to; i++) {
      sum += spectrum[i];
    }
    return sum / (to - from);
  }

  /**
   * Smooths a value with attack/decay.
   */
  private float smooth(float current, float target) {
    if (target > current) {
      return current + (target - current) * attack;
    } else {
      return current + (target - current) * decay;
    }
  }

  /**
   * Detects beats based on bass transients.
   */
  private void detectBeat() {
    int currentTime = parent.millis();

    if (bassLevel > beatThreshold && currentTime - lastBeatTime > beatCooldown) {
      beatDetected = true;
      lastBeatTime = currentTime;
    } else {
      beatDetected = false;
    }
  }

  // ---- Parameter mappings ------------------------------------------------

  /**
   * Maps bass frequency to a parameter.
   *
   * @param setter the parameter setter (e.g., config::setWaveSpeed)
   * @param min minimum value
   * @param max maximum value
   */
  public void mapBassTo(Consumer<Float> setter, float min, float max) {
    mappings.put("bass", new ParameterMapping(setter, min, max));
  }

  /**
   * Maps mid frequency to a parameter.
   *
   * @param setter the parameter setter (e.g., config::setWaveSpeed)
   * @param min minimum mapped value
   * @param max maximum mapped value
   */
  public void mapMidTo(Consumer<Float> setter, float min, float max) {
    mappings.put("mid", new ParameterMapping(setter, min, max));
  }

  /**
   * Maps treble frequency to a parameter.
   *
   * @param setter the parameter setter (e.g., config::setBrightnessMax)
   * @param min minimum mapped value
   * @param max maximum mapped value
   */
  public void mapTrebleTo(Consumer<Float> setter, float min, float max) {
    mappings.put("treble", new ParameterMapping(setter, min, max));
  }

  /**
   * Maps overall level to a parameter.
   *
   * @param setter the parameter setter
   * @param min minimum mapped value
   * @param max maximum mapped value
   */
  public void mapOverallTo(Consumer<Float> setter, float min, float max) {
    mappings.put("overall", new ParameterMapping(setter, min, max));
  }

  /**
   * Applies all parameter mappings.
   */
  private void applyMappings() {
    if (mappings.containsKey("bass")) {
      mappings.get("bass").apply(bassLevel);
    }
    if (mappings.containsKey("mid")) {
      mappings.get("mid").apply(midLevel);
    }
    if (mappings.containsKey("treble")) {
      mappings.get("treble").apply(trebleLevel);
    }
    if (mappings.containsKey("overall")) {
      mappings.get("overall").apply(overallLevel);
    }
  }

  // ---- Getters / setters -------------------------------------------------

  /**
   * Checks if a beat was detected this frame.
   *
   * @return true if a beat was detected
   */
  public boolean isBeat() {
    return beatDetected;
  }

  /**
   * Gets current bass level (0.0 - 1.0).
   *
   * @return the bass level
   */
  public float getBass() {
    return bassLevel;
  }

  /**
   * Gets current mid level (0.0 - 1.0).
   *
   * @return the mid level
   */
  public float getMid() {
    return midLevel;
  }

  /**
   * Gets current treble level (0.0 - 1.0).
   *
   * @return the treble level
   */
  public float getTreble() {
    return trebleLevel;
  }

  /**
   * Gets overall audio level (0.0 - 1.0).
   *
   * @return the overall audio level
   */
  public float getLevel() {
    return overallLevel;
  }

  /**
   * Sets attack speed (0.0 - 1.0).
   *
   * @param attack the attack speed (clamped to 0.01 - 1.0)
   */
  public void setAttack(float attack) {
    this.attack = PApplet.constrain(attack, 0.01f, 1.0f);
  }

  /**
   * Sets decay speed (0.0 - 1.0).
   *
   * @param decay the decay speed (clamped to 0.01 - 1.0)
   */
  public void setDecay(float decay) {
    this.decay = PApplet.constrain(decay, 0.01f, 1.0f);
  }

  /**
   * Sets beat detection threshold.
   *
   * @param threshold the beat detection threshold
   */
  public void setBeatThreshold(float threshold) {
    this.beatThreshold = threshold;
  }

  // ---- Debug overlay -----------------------------------------------------

  /**
   * Displays audio visualization overlay.
   *
   * @param x the x position of the debug overlay
   * @param y the y position of the debug overlay
   */
  public void displayDebug(float x, float y) {
    if (!isAudioInitialized) return;

    float barWidth = 25;
    float maxHeight = 80;
    float panelW = 145;
    float panelH = 140;

    // Background panel
    parent.pushStyle();
    parent.fill(0, 200);
    parent.noStroke();
    parent.rect(x, y, panelW, panelH);

    // Title
    parent.fill(255);
    parent.textAlign(PApplet.LEFT, PApplet.TOP);
    parent.textSize(12);
    parent.text("Audio", x + 5, y + 4);

    // Numeric levels
    parent.textSize(10);
    parent.text("B " + PApplet.nf(bassLevel, 1, 2), x + 5,  y + 120);
    parent.text("M " + PApplet.nf(midLevel, 1, 2),  x + 50, y + 120);
    parent.text("T " + PApplet.nf(trebleLevel, 1, 2), x + 95, y + 120);

    // Bar base Y
    float baseY = y + 115;

    // Bass bar
    parent.fill(255, 80, 80);
    parent.rect(x + 5, baseY, barWidth, -bassLevel * maxHeight);

    // Mid bar
    parent.fill(80, 255, 80);
    parent.rect(x + 50, baseY, barWidth, -midLevel * maxHeight);

    // Treble bar
    parent.fill(80, 80, 255);
    parent.rect(x + 95, baseY, barWidth, -trebleLevel * maxHeight);

    // Beat indicator
    if (beatDetected) {
      parent.fill(255, 255, 0);
      parent.ellipse(x + panelW - 12, y + 12, 10, 10);
    }
    parent.popStyle();
  }

  // ---- Lifecycle ---------------------------------------------------------

  /**
   * Stops audio processing.
   */
  public void stop() {
    if (audioIn != null) {
      try {
        audioIn.getClass().getMethod("stop").invoke(audioIn);
      } catch (Exception e) {
        // Ignore
      }
    }
    isAudioInitialized = false;
  }

  // ---- Internal ----------------------------------------------------------

  /**
   * Internal class for parameter mappings.
   */
  private static class ParameterMapping {
    private final Consumer<Float> setter;
    private final float min;
    private final float max;

    ParameterMapping(Consumer<Float> setter, float min, float max) {
      this.setter = setter;
      this.min = min;
      this.max = max;
    }

    void apply(float normalizedValue) {
      float clamped = Math.max(0f, Math.min(1f, normalizedValue));
      float value = min + clamped * (max - min);
      setter.accept(value);
    }
  }
}
