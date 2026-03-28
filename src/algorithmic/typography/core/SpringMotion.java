/**
 * SpringMotion - Glyphs pulled toward a drifting target with spring damping.
 *
 * <p>Each glyph behaves as a mass on a spring: it is continuously pulled
 * toward a slowly moving target position.  The spring equation is:</p>
 * <pre>
 *   acceleration = -stiffness * (position - target) - damping * velocity
 * </pre>
 *
 * <p>This produces the characteristic spring behaviour — overshoot,
 * ringing, and eventual settling — whose feel is controlled by the
 * {@code stiffness} and {@code damping} parameters:</p>
 * <ul>
 *   <li><b>Under-damped</b> (damping low): bouncy, oscillates before settling</li>
 *   <li><b>Critically damped</b>: reaches target as fast as possible, no overshoot</li>
 *   <li><b>Over-damped</b> (damping high): slow, sluggish approach</li>
 * </ul>
 *
 * <p>The target itself drifts sinusoidally so the spring never fully
 * settles — the glyph perpetually chases it.  A per-cell phase offset
 * ensures neighbouring glyphs are all at different stages of their chase,
 * creating organic, wave-like ripples across the grid.</p>
 *
 * <h2>Example</h2>
 * <pre>
 * // Default: gentle spring, moderate damping
 * SpringMotion spring = new SpringMotion(10, 1.0f);
 *
 * // Stiff, under-damped (bouncy)
 * SpringMotion bouncy = new SpringMotion(12, 1.0f, 0.8f, 0.05f);
 *
 * // Soft, over-damped (sluggish)
 * SpringMotion slow = new SpringMotion(8, 0.6f, 0.15f, 0.25f);
 * </pre>
 *
 * @author Michail Semoglou
 * @version 0.3.0
 * @since 0.2.1
 * @see CellMotion
 * @see CircularMotion
 * @see PerlinMotion
 */

package algorithmic.typography.core;

import java.util.HashMap;
import processing.core.PApplet;
import processing.core.PVector;

/**
 * Spring-damped motion that pulls each glyph toward a drifting target.
 */
public class SpringMotion extends CellMotion {

  // ── Spring parameters ─────────────────────────────────────────

  /**
   * Spring stiffness (k).  Higher values make the spring stiffer and
   * the glyph track the target more tightly.
   * Typical range: 0.05 – 1.0.
   */
  private float stiffness;

  /**
   * Damping coefficient.  Higher values absorb energy faster and
   * reduce overshoot.  Typical range: 0.05 – 0.5.
   */
  private float damping;

  /**
   * How far the drifting target wanders from the cell centre (pixels).
   * Defaults to {@code radius}.
   */
  private float targetAmplitude;

  /**
   * How fast the target drifts.  1.0 = normal; lower = lazier target.
   */
  private float targetSpeed;

  /**
   * Per-cell phase spread.  Higher values make neighbouring glyphs
   * more out-of-phase with each other.
   */
  private float phaseSpread = 0.8f;

  // ── Per-cell physics state ────────────────────────────────────

  /**
   * Stores [posX, posY, velX, velY, lastFrame] for every (col, row) pair.
   * Keyed by {@code ((long) col << 32) | (row & 0xFFFFFFFFL)}.
   */
  private final HashMap<Long, float[]> cellState = new HashMap<>();

  // ── Constructors ──────────────────────────────────────────────

  /**
   * Full constructor.
   *
   * @param radius          max displacement / bounding radius in pixels
   * @param targetSpeed     drift speed of the target (1.0 = normal)
   * @param stiffness       spring constant k  (0.05 – 1.0)
   * @param damping         damping coefficient (0.05 – 0.5)
   */
  public SpringMotion(float radius, float targetSpeed,
                      float stiffness, float damping) {
    this.radius          = radius;
    this.speed           = targetSpeed;
    this.targetSpeed     = targetSpeed;
    this.stiffness       = stiffness;
    this.damping         = damping;
    this.targetAmplitude = radius;
  }

  /**
   * Creates a SpringMotion with default stiffness (0.35) and
   * damping (0.12) — lightly bouncy feel.
   *
   * @param radius      max displacement in pixels
   * @param targetSpeed drift speed of the target (1.0 = normal)
   */
  public SpringMotion(float radius, float targetSpeed) {
    this(radius, targetSpeed, 0.35f, 0.12f);
  }

  /**
   * Default: 10 px radius, speed 1, stiffness 0.35, damping 0.12.
   */
  public SpringMotion() {
    this(10, 1.0f);
  }

  // ── Core ──────────────────────────────────────────────────────

  /**
   * {@inheritDoc}
   *
   * <p>Advances the spring physics for the cell identified by
   * ({@code col}, {@code row}) by one integration step and returns
   * the current displacement.</p>
   */
  @Override
  public PVector getOffset(int col, int row, int frameCount) {
    long key = ((long) col << 32) | (row & 0xFFFFFFFFL);

    // Retrieve or initialise per-cell state: [posX, posY, velX, velY, lastFrame]
    float[] s = cellState.computeIfAbsent(key, k -> new float[5]);

    // Guard against double-integration when called multiple times per frame
    if ((int) s[4] == frameCount) {
      return new PVector(s[0], s[1]);
    }
    s[4] = frameCount;

    // ── Compute drifting target position ─────────────────────────
    float phase = (col * 0.7f + row * 1.3f) * phaseSpread;
    float t      = frameCount * 0.02f * targetSpeed;
    float targetX = PApplet.sin(t + phase)            * targetAmplitude;
    float targetY = PApplet.cos(t + phase * 0.73f)    * targetAmplitude;

    // ── Spring-damper integration (semi-implicit Euler, dt = 1 frame) ──
    // Fixed timestep calibrated so speed=1 at ~60 fps feels responsive
    float dt = 0.9f;

    float ax = -stiffness * (s[0] - targetX) - damping * s[2];
    float ay = -stiffness * (s[1] - targetY) - damping * s[3];

    s[2] += ax * dt;   // velX
    s[3] += ay * dt;   // velY
    s[0] += s[2] * dt; // posX
    s[1] += s[3] * dt; // posY

    // ── Soft radius clamp ─────────────────────────────────────────
    float dist = PApplet.sqrt(s[0] * s[0] + s[1] * s[1]);
    if (dist > radius) {
      float scale = radius / dist;
      s[0] *= scale;
      s[1] *= scale;
      s[2] *= scale;
      s[3] *= scale;
    }

    return new PVector(s[0], s[1]);
  }

  /** Resets all per-cell spring state (positions and velocities to zero). */
  public void reset() {
    cellState.clear();
  }

  // ── Configuration ─────────────────────────────────────────────

  /**
   * Sets the spring stiffness.  Higher = tighter tracking, faster response.
   *
   * @param k stiffness (typical range 0.05 – 1.0)
   */
  public void setStiffness(float k) { this.stiffness = Math.max(0.001f, k); }

  /** Returns the spring stiffness.
   *  @return stiffness */
  public float getStiffness() { return stiffness; }

  /**
   * Sets the damping coefficient.  Higher = less overshoot, slower settling.
   *
   * @param d damping (typical range 0.05 – 0.5)
   */
  public void setDamping(float d) { this.damping = Math.max(0.0f, d); }

  /** Returns the damping coefficient.
   *  @return damping */
  public float getDamping() { return damping; }

  /**
   * Sets how far the target drifts from the cell centre.
   * Defaults to the same value as {@code radius}.
   *
   * @param amplitude target amplitude in pixels
   */
  public void setTargetAmplitude(float amplitude) {
    this.targetAmplitude = Math.max(0, amplitude);
  }

  /** Returns the target drift amplitude.
   *  @return targetAmplitude */
  public float getTargetAmplitude() { return targetAmplitude; }

  /**
   * Sets the drift speed of the target.
   *
   * @param s speed multiplier (1.0 = normal)
   */
  public void setTargetSpeed(float s) {
    this.targetSpeed = s;
    this.speed       = s;
  }

  /** Returns the target drift speed.
   *  @return targetSpeed */
  public float getTargetSpeed() { return targetSpeed; }

  /**
   * Sets the inter-cell phase spread.
   * Higher values make adjacent glyphs more out-of-phase.
   *
   * @param spread phase spread (default 0.8)
   */
  public void setPhaseSpread(float spread) { this.phaseSpread = spread; }

  /** Returns the inter-cell phase spread.
   *  @return phaseSpread */
  public float getPhaseSpread() { return phaseSpread; }
}
