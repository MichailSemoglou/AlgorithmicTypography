/**
 * TemporalTrail - Delay and trail effects for typography animation.
 *
 * <p>Maintains a circular buffer of historical grid states (brightness,
 * hue, saturation) and composites them into a single output frame with
 * configurable fade, delay, and temporal displacement.  The trail can
 * react to frame rate (slower FPS → longer trails) or audio input via
 * {@link algorithmic.typography.audio.AudioBridge}.</p>
 *
 * <h2>Features</h2>
 * <ul>
 *   <li>Fixed-length or adaptive trail buffer</li>
 *   <li>Per-cell temporal offset for wave-like displacement</li>
 *   <li>Frame-rate reactive mode (stutters extend the ghost)</li>
 *   <li>Audio-reactive trail length (uses overall level)</li>
 *   <li>Compositing: additive, max, or average blending</li>
 * </ul>
 *
 * <h2>Quick start</h2>
 * <pre>
 * TemporalTrail trail = new TemporalTrail(cols, rows, 12);
 * // inside draw():
 * trail.capture(waveEngine, config, frameCount);
 * float[][] frame = trail.composite();
 * // use frame[i][j] as brightness in your grid
 * </pre>
 *
 * @author Michail Semoglou
 * @version 1.0.0
 * @since 1.0.0
 */

package algorithmic.typography.core;

import processing.core.*;
import algorithmic.typography.Configuration;

/**
 * TemporalTrail captures and composites a history of grid values.
 */
public class TemporalTrail {

  // ── Blend modes ──────────────────────────────────────────────
  /** Additive blending (default). */
  public static final int BLEND_ADD     = 0;
  /** Per-cell maximum. */
  public static final int BLEND_MAX     = 1;
  /** Per-cell average. */
  public static final int BLEND_AVERAGE = 2;

  // ── Ring buffer ──────────────────────────────────────────────
  private final int cols;
  private final int rows;
  private final int maxLength;

  // buffer[frame][row * cols + col]  — brightness values 0–255
  private final float[][] buffer;
  // Optional hue buffer
  private final float[][] hueBuffer;
  // Optional saturation buffer
  private final float[][] satBuffer;

  private int head = 0;       // write position
  private int filled = 0;     // how many slots contain data

  // ── Configuration ────────────────────────────────────────────
  private int    trailLength;       // active trail length ≤ maxLength
  private float  fadeDecay = 0.7f;  // per-frame brightness multiplier (0–1)
  private int    blendMode = BLEND_ADD;

  // Per-cell temporal offset (wave displacement)
  private boolean useTemporalWave = false;
  private float   temporalWaveAmp = 3.0f;   // max frame offset
  private float   temporalWaveFreq = 0.3f;  // spatial frequency

  // Framerate reactive
  private boolean framerateReactive = false;
  private float   targetFPS = 60.0f;
  private float   framerateSmooth = 60.0f;  // EMA of frame rate

  // Audio reactive  (caller feeds level each frame)
  private boolean audioReactive = false;
  private float   audioLevel = 0.0f;        // 0-1 fed externally
  private int     audioMinTrail = 2;
  private int     audioMaxTrail;

  // ───────────────────────────────────────────────────────────────
  //  Construction
  // ───────────────────────────────────────────────────────────────

  /**
   * Creates a TemporalTrail buffer.
   *
   * @param cols        grid columns
   * @param rows        grid rows
   * @param maxLength   maximum trail length (ring buffer size)
   */
  public TemporalTrail(int cols, int rows, int maxLength) {
    this.cols = cols;
    this.rows = rows;
    this.maxLength = Math.max(1, maxLength);
    this.trailLength = this.maxLength;
    this.audioMaxTrail = this.maxLength;

    int cells = cols * rows;
    buffer    = new float[this.maxLength][cells];
    hueBuffer = new float[this.maxLength][cells];
    satBuffer = new float[this.maxLength][cells];
  }

  // ───────────────────────────────────────────────────────────────
  //  Capture
  // ───────────────────────────────────────────────────────────────

  /**
   * Captures the current grid state from the WaveEngine.
   *
   * @param engine     the wave engine
   * @param config     the configuration
   * @param frameCount current Processing frameCount
   */
  public void capture(WaveEngine engine, Configuration config, int frameCount) {
    int cells = cols * rows;
    float[] frame = buffer[head];
    float[] hFrame = hueBuffer[head];
    float[] sFrame = satBuffer[head];

    for (int j = 0; j < rows; j++) {
      for (int i = 0; i < cols; i++) {
        int idx = j * cols + i;
        frame[idx] = engine.calculateColorCustom(frameCount, i, j, cols, rows);

        boolean hasHue = config.getHueMin() != config.getHueMax();
        if (hasHue) {
          hFrame[idx] = engine.calculateHue(frameCount, i, j, cols, rows);
          sFrame[idx] = engine.calculateSaturation(frameCount, i, j, cols, rows);
        }
      }
    }

    head = (head + 1) % maxLength;
    if (filled < maxLength) filled++;
  }

  /**
   * Captures arbitrary float values (advanced use).
   *
   * @param values  flat array of size cols*rows
   */
  public void captureRaw(float[] values) {
    int cells = cols * rows;
    float[] frame = buffer[head];
    System.arraycopy(values, 0, frame, 0, Math.min(values.length, cells));

    head = (head + 1) % maxLength;
    if (filled < maxLength) filled++;
  }

  // ───────────────────────────────────────────────────────────────
  //  Compositing
  // ───────────────────────────────────────────────────────────────

  /**
   * Composites the trail into a single 2-D grid of brightness values.
   *
   * @return float[row][col] brightness values (0–255)
   */
  public float[][] composite() {
    int len = effectiveTrailLength();
    float[][] result = new float[rows][cols];

    if (filled == 0) return result;

    int cells = cols * rows;
    float[] accum = new float[cells];
    int[] counts = new int[cells];

    for (int t = 0; t < len && t < filled; t++) {
      float weight = (float) Math.pow(fadeDecay, t);

      for (int c = 0; c < cells; c++) {
        int offset = useTemporalWave ? temporalOffset(c, t) : 0;
        int effectiveT = t + offset;
        if (effectiveT < 0 || effectiveT >= filled) continue;
        int srcIdx = ((head - 1 - effectiveT) + maxLength * 2) % maxLength;

        float val = buffer[srcIdx][c] * weight;

        switch (blendMode) {
          case BLEND_MAX:
            accum[c] = Math.max(accum[c], val);
            break;
          case BLEND_AVERAGE:
            accum[c] += val;
            counts[c]++;
            break;
          case BLEND_ADD:
          default:
            accum[c] += val;
            break;
        }
      }
    }

    for (int j = 0; j < rows; j++) {
      for (int i = 0; i < cols; i++) {
        int idx = j * cols + i;
        float v = accum[idx];
        if (blendMode == BLEND_AVERAGE && counts[idx] > 0) {
          v /= counts[idx];
        }
        result[j][i] = Math.min(255, Math.max(0, v));
      }
    }
    return result;
  }

  /**
   * Composites the trail into flat arrays of hue, saturation, brightness.
   * Useful when the source uses HSB colour mode.
   *
   * @return float[3][rows*cols] — [0]=hue(0-360), [1]=sat(0-255), [2]=bri(0-255)
   */
  public float[][] compositeHSB() {
    int len = effectiveTrailLength();
    int cells = cols * rows;
    float[] hAccum = new float[cells];
    float[] sAccum = new float[cells];
    float[] bAccum = new float[cells];
    float[] wSum   = new float[cells];

    for (int t = 0; t < len && t < filled; t++) {
      float weight = (float) Math.pow(fadeDecay, t);

      for (int c = 0; c < cells; c++) {
        int offset = useTemporalWave ? temporalOffset(c, t) : 0;
        int effectiveT = t + offset;
        if (effectiveT < 0 || effectiveT >= filled) continue;
        int srcIdx = ((head - 1 - effectiveT) + maxLength * 2) % maxLength;

        hAccum[c] += hueBuffer[srcIdx][c] * weight;
        sAccum[c] += satBuffer[srcIdx][c] * weight;
        bAccum[c] += buffer[srcIdx][c]    * weight;
        wSum[c]   += weight;
      }
    }

    float[][] result = new float[3][cells];
    for (int c = 0; c < cells; c++) {
      float w = Math.max(0.001f, wSum[c]);
      result[0][c] = hAccum[c] / w;                            // hue (weighted avg)
      result[1][c] = sAccum[c] / w;                            // saturation
      result[2][c] = Math.min(255, Math.max(0, bAccum[c]));    // brightness (summed)
    }
    return result;
  }

  private int temporalOffset(int cellIdx, int t) {
    int col = cellIdx % cols;
    int row = cellIdx / cols;
    float wave = PApplet.sin((col + row) * temporalWaveFreq + t * 0.2f);
    return Math.round(wave * temporalWaveAmp);
  }

  // ───────────────────────────────────────────────────────────────
  //  Reactive trail length
  // ───────────────────────────────────────────────────────────────

  private int effectiveTrailLength() {
    int len = trailLength;

    if (framerateReactive) {
      // Slow frame rate → longer trail (more ghosting)
      float ratio = targetFPS / Math.max(1, framerateSmooth);
      len = Math.round(len * PApplet.constrain(ratio, 1.0f, 3.0f));
    }

    if (audioReactive) {
      len = Math.round(PApplet.lerp(audioMinTrail, audioMaxTrail, audioLevel));
    }

    return Math.min(len, Math.min(filled, maxLength));
  }

  // ───────────────────────────────────────────────────────────────
  //  Configuration setters
  // ───────────────────────────────────────────────────────────────

  /** Active trail length (capped to maxLength). */
  public void setTrailLength(int n) { this.trailLength = PApplet.constrain(n, 1, maxLength); }

  /** Per-frame brightness decay (0 = instant fade, 1 = no fade). Default 0.7. */
  public void setFadeDecay(float d) { this.fadeDecay = PApplet.constrain(d, 0, 1); }

  /** Blend mode: BLEND_ADD, BLEND_MAX, or BLEND_AVERAGE. */
  public void setBlendMode(int mode) { this.blendMode = mode; }

  /**
   * Enables per-cell temporal wave displacement.
   *
   * @param amp   max frame offset per cell
   * @param freq  spatial frequency across the grid
   */
  public void setTemporalWave(float amp, float freq) {
    this.useTemporalWave = true;
    this.temporalWaveAmp = amp;
    this.temporalWaveFreq = freq;
  }

  /** Disables temporal wave displacement. */
  public void disableTemporalWave() { this.useTemporalWave = false; }

  /**
   * Enables frame-rate reactive trail length.
   * When FPS drops below target, the trail grows proportionally.
   *
   * @param targetFPS the ideal frame rate (usually 60)
   */
  public void setFramerateReactive(boolean on, float targetFPS) {
    this.framerateReactive = on;
    this.targetFPS = targetFPS;
  }

  /**
   * Feed current frame rate (call once per draw() when framerate-reactive).
   *
   * @param fps the current {@code frameRate} from Processing
   */
  public void feedFramerate(float fps) {
    framerateSmooth = framerateSmooth * 0.9f + fps * 0.1f;
  }

  /**
   * Enables audio-reactive trail length.
   *
   * @param minTrail  trail length at silence (0 level)
   * @param maxTrail  trail length at full volume (1.0 level)
   */
  public void setAudioReactive(int minTrail, int maxTrail) {
    this.audioReactive = true;
    this.audioMinTrail = Math.max(1, minTrail);
    this.audioMaxTrail = Math.min(maxTrail, maxLength);
  }

  /** Disables audio-reactive trail length. */
  public void disableAudioReactive() { this.audioReactive = false; }

  /**
   * Feed current audio level (call once per draw() when audio-reactive).
   *
   * @param level normalised audio level 0.0 – 1.0
   */
  public void feedAudioLevel(float level) {
    this.audioLevel = PApplet.constrain(level, 0, 1);
  }

  /** Resets the buffer, discarding all history. */
  public void clear() {
    head = 0;
    filled = 0;
  }

  // ───────────────────────────────────────────────────────────────
  //  Accessors
  // ───────────────────────────────────────────────────────────────

  /** Number of buffered frames currently stored. */
  public int getFilledFrames() { return filled; }

  /** Grid columns. */
  public int getCols() { return cols; }

  /** Grid rows. */
  public int getRows() { return rows; }

  /** Maximum buffer size. */
  public int getMaxLength() { return maxLength; }
}
