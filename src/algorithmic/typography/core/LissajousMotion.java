/**
 * LissajousMotion - figure-8 and knot-shaped orbits.
 *
 * <p>A Lissajous figure traces the path:</p>
 * <pre>
 *   x(t) = radius  * sin(freqX * t + phaseX)
 *   y(t) = radiusY * sin(freqY * t)
 * </pre>
 *
 * <p>The frequency ratio {@code freqX : freqY} determines the
 * shape of the orbit:</p>
 * <ul>
 *   <li>1 : 2 → figure-8 (default)</li>
 *   <li>3 : 2 → three-lobed knot</li>
 *   <li>1 : 1 with {@code phaseX = PI/2} → circle</li>
 * </ul>
 *
 * <p>Each cell receives an independent phase offset derived from
 * its column and row so neighbouring glyphs are spread around the
 * figure rather than all moving in lock-step.</p>
 *
 * <h2>Example</h2>
 * <pre>
 * // Classic figure-8 (default)
 * LissajousMotion fig8 = new LissajousMotion(10, 1.0f);
 *
 * // Three-lobed knot, faster, taller than wide
 * LissajousMotion knot = new LissajousMotion(10, 16, 1.5f, 3, 2);
 * </pre>
 *
 * @author Michail Semoglou
 * @version 0.3.0
 * @since 0.2.1
 * @see CellMotion
 */

package algorithmic.typography.core;

import processing.core.PApplet;
import processing.core.PVector;

/**
 * Lissajous figure-8 and knot-shaped orbit within a grid cell.
 */
public class LissajousMotion extends CellMotion {

  /** Vertical amplitude (pixels). Defaults to match {@code radius}. */
  private float radiusY;

  /** Horizontal frequency multiplier (integer ratios produce closed curves). */
  private float freqX;

  /** Vertical frequency multiplier. */
  private float freqY;

  /**
   * Phase offset applied to the x channel.
   * {@code PI/2} gives the classic sideways figure-8.
   */
  private float phaseX;

  /** Per-cell phase spread. Higher = more variation between neighbours. */
  private float phaseSpread = 0.8f;

  // ── Constructors ─────────────────────────────────────────────

  /**
   * Creates a LissajousMotion with independent x/y amplitudes
   * and custom frequency ratio.
   *
   * @param radiusX  horizontal amplitude in pixels
   * @param radiusY  vertical amplitude in pixels
   * @param speed    animation speed (1.0 = normal)
   * @param freqX    horizontal frequency multiplier
   * @param freqY    vertical frequency multiplier
   */
  public LissajousMotion(float radiusX, float radiusY,
                         float speed, float freqX, float freqY) {
    this.radius  = radiusX;
    this.radiusY = radiusY;
    this.speed   = speed;
    this.freqX   = freqX;
    this.freqY   = freqY;
    this.phaseX  = PApplet.HALF_PI;   // classic figure-8 orientation
  }

  /**
   * Creates a LissajousMotion with equal x/y amplitudes and
   * the 1:2 figure-8 ratio.
   *
   * @param radius amplitude in pixels (x and y)
   * @param speed  animation speed (1.0 = normal)
   */
  public LissajousMotion(float radius, float speed) {
    this(radius, radius, speed, 1, 2);
  }

  /**
   * Default: 8 px radius, speed 1, figure-8 (1:2 ratio).
   */
  public LissajousMotion() {
    this(8, 1.0f);
  }

  // ── Core ─────────────────────────────────────────────────────

  @Override
  public PVector getOffset(int col, int row, int frameCount) {
    float cellPhase = (col * 0.7f + row * 1.3f) * phaseSpread;
    float t = frameCount * speed * 0.03f + cellPhase;

    float x = PApplet.sin(freqX * t + phaseX) * radius;
    float y = PApplet.sin(freqY * t) * radiusY;
    return new PVector(x, y);
  }

  // ── Configuration ────────────────────────────────────────────

  /**
   * Sets the vertical amplitude independently of the horizontal one.
   *
   * @param ry vertical radius in pixels
   */
  public void setRadiusY(float ry) { this.radiusY = Math.max(0, ry); }

  /** Returns the vertical amplitude.
   *  @return vertical radius */
  public float getRadiusY() { return radiusY; }

  /**
   * Sets the horizontal frequency multiplier.
   *
   * @param fx horizontal frequency (must be &gt; 0)
   */
  public void setFreqX(float fx) { this.freqX = Math.max(0.001f, fx); }

  /** Returns the horizontal frequency multiplier.
   *  @return freqX */
  public float getFreqX() { return freqX; }

  /**
   * Sets the vertical frequency multiplier.
   *
   * @param fy vertical frequency (must be &gt; 0)
   */
  public void setFreqY(float fy) { this.freqY = Math.max(0.001f, fy); }

  /** Returns the vertical frequency multiplier.
   *  @return freqY */
  public float getFreqY() { return freqY; }

  /**
   * Sets the x-channel phase offset.
   * Use {@code PApplet.HALF_PI} for the classic figure-8,
   * {@code 0} for a diagonal line at rest.
   *
   * @param phase angle in radians
   */
  public void setPhaseX(float phase) { this.phaseX = phase; }

  /** Returns the x-channel phase offset.
   *  @return phaseX in radians */
  public float getPhaseX() { return phaseX; }

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
