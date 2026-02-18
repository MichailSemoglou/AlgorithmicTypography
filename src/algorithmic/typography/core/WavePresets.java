/**
 * Built-in wave function presets for AlgorithmicTypography.
 *
 * <p>Provides the standard mathematical wave shapes (sine, tangent, square,
 * triangle, sawtooth) as ready-to-use {@link WaveFunction} implementations,
 * plus a Perlin-noise factory that requires a {@code PApplet} reference.</p>
 *
 * <h2>Quick start</h2>
 * <pre>
 * // Via the convenience method on the main class
 * at.setWaveType(WavePresets.Type.SINE);
 *
 * // Or directly via the wave engine
 * at.setWaveFunction(WavePresets.square());
 *
 * // Perlin noise (needs PApplet for noise())
 * at.setWaveFunction(WavePresets.perlin(this));
 * </pre>
 *
 * @author Michail Semoglou
 * @version 1.0.0
 * @since 1.0.0
 */
package algorithmic.typography.core;

import processing.core.PApplet;
import algorithmic.typography.Configuration;

public final class WavePresets {

  // ── Enum ────────────────────────────────────────────────────────────

  /** Standard mathematical wave types. */
  public enum Type {
    /** Smooth sinusoidal oscillation. */
    SINE,
    /** Sharp, angular tangent-based oscillation. */
    TANGENT,
    /** Binary on / off square wave. */
    SQUARE,
    /** Linear ramp up then down. */
    TRIANGLE,
    /** Linear ramp with sharp drop-off. */
    SAWTOOTH
  }

  private WavePresets() { } // utility class — no instantiation

  // ── Factory ─────────────────────────────────────────────────────────

  /**
   * Returns the built-in {@link WaveFunction} for the given type.
   *
   * @param type the wave type
   * @return a new WaveFunction instance
   */
  public static WaveFunction get(Type type) {
    switch (type) {
      case SINE:     return sine();
      case TANGENT:  return tangent();
      case SQUARE:   return square();
      case TRIANGLE: return triangle();
      case SAWTOOTH: return sawtooth();
      default:       return sine();
    }
  }

  // ── Standard presets ────────────────────────────────────────────────

  /**
   * Smooth sinusoidal wave — the gentlest, most organic of the
   * mathematical shapes.
   *
   * @return a sine WaveFunction
   */
  public static WaveFunction sine() {
    return new WaveFunction() {
      @Override
      public float calculate(int frameCount, float x, float y,
                             float time, Configuration config) {
        float phase = computePhase(frameCount, x, y, config);
        return PApplet.map(PApplet.sin(phase), -1, 1,
            config.getBrightnessMin(), config.getBrightnessMax());
      }
      @Override public String getName()        { return "Sine"; }
      @Override public String getDescription() { return "Smooth sinusoidal oscillation"; }
    };
  }

  /**
   * Tangent-based wave — creates sharp, angular transitions and
   * is equivalent to the library's built-in default engine.
   *
   * @return a tangent WaveFunction
   */
  public static WaveFunction tangent() {
    return new WaveFunction() {
      @Override
      public float calculate(int frameCount, float x, float y,
                             float time, Configuration config) {
        float phase = computePhase(frameCount, x, y, config);
        float raw = PApplet.map(PApplet.tan(phase), -1, 1,
            config.getBrightnessMin(), config.getBrightnessMax());
        return PApplet.constrain(raw,
            config.getBrightnessMin(), config.getBrightnessMax());
      }
      @Override public String getName()        { return "Tangent"; }
      @Override public String getDescription() { return "Sharp, angular tangent oscillation"; }
    };
  }

  /**
   * Square wave — binary on / off, creating bold checkerboard-like
   * patterns across the grid.
   *
   * @return a square WaveFunction
   */
  public static WaveFunction square() {
    return new WaveFunction() {
      @Override
      public float calculate(int frameCount, float x, float y,
                             float time, Configuration config) {
        float phase = computePhase(frameCount, x, y, config);
        return PApplet.sin(phase) >= 0
            ? config.getBrightnessMax()
            : config.getBrightnessMin();
      }
      @Override public String getName()        { return "Square"; }
      @Override public String getDescription() { return "Binary on/off square wave"; }
    };
  }

  /**
   * Triangle wave — linear ramp up and down, producing even,
   * gradient-like transitions.
   *
   * @return a triangle WaveFunction
   */
  public static WaveFunction triangle() {
    return new WaveFunction() {
      @Override
      public float calculate(int frameCount, float x, float y,
                             float time, Configuration config) {
        float phase = computePhase(frameCount, x, y, config);
        float t = (phase / PApplet.TWO_PI) % 1.0f;
        if (t < 0) t += 1;
        float tri = t < 0.5f ? (4 * t - 1) : (3 - 4 * t);  // −1 → +1
        return PApplet.map(tri, -1, 1,
            config.getBrightnessMin(), config.getBrightnessMax());
      }
      @Override public String getName()        { return "Triangle"; }
      @Override public String getDescription() { return "Linear ramp up then down"; }
    };
  }

  /**
   * Sawtooth wave — linear ramp followed by a sharp drop, creating
   * directional gradients with hard edges.
   *
   * @return a sawtooth WaveFunction
   */
  public static WaveFunction sawtooth() {
    return new WaveFunction() {
      @Override
      public float calculate(int frameCount, float x, float y,
                             float time, Configuration config) {
        float phase = computePhase(frameCount, x, y, config);
        float t = (phase / PApplet.TWO_PI) % 1.0f;
        if (t < 0) t += 1;
        return PApplet.map(t, 0, 1,
            config.getBrightnessMin(), config.getBrightnessMax());
      }
      @Override public String getName()        { return "Sawtooth"; }
      @Override public String getDescription() { return "Linear ramp with sharp drop"; }
    };
  }

  // ── Perlin noise (needs PApplet) ────────────────────────────────────

  /**
   * Perlin-noise wave with default parameters (scale 3.0, speed 0.8).
   *
   * <p>Requires a {@code PApplet} reference because it calls
   * {@code parent.noise()}.</p>
   *
   * @param parent the Processing sketch
   * @return a Perlin-noise WaveFunction
   */
  public static WaveFunction perlin(PApplet parent) {
    return perlin(parent, 3.0f, 0.8f);
  }

  /**
   * Perlin-noise wave with configurable scale and speed.
   *
   * @param parent the Processing sketch
   * @param scale  spatial noise scale (higher = more detail)
   * @param speed  temporal animation speed
   * @return a Perlin-noise WaveFunction
   */
  public static WaveFunction perlin(PApplet parent, float scale, float speed) {
    return new WaveFunction() {
      @Override
      public float calculate(int frameCount, float x, float y,
                             float time, Configuration config) {
        float nx = x * scale;
        float ny = y * scale;
        float nt = frameCount * 0.01f * speed;
        float n1 = parent.noise(nx, ny, nt);
        float n2 = parent.noise(nx * 2.5f + 100, ny * 2.5f + 100, nt * 1.5f);
        float n  = n1 * 0.7f + n2 * 0.3f;
        return PApplet.map(n, 0.15f, 0.85f,
            config.getBrightnessMin(), config.getBrightnessMax());
      }
      @Override public String getName()        { return "Perlin"; }
      @Override public String getDescription() { return "Organic Perlin noise patterns"; }
    };
  }

  // ── Helpers ─────────────────────────────────────────────────────────

  /**
   * Computes the standard phase value shared by all mathematical presets.
   *
   * <p>Combines frame-based time animation with a diagonal spatial
   * sweep across the normalized grid.</p>
   */
  private static float computePhase(int frameCount, float x, float y,
                                     Configuration config) {
    return frameCount * config.getWaveSpeed() * 0.05f
         + x * PApplet.TWO_PI * 3
         + y * PApplet.TWO_PI * 3;
  }
}
