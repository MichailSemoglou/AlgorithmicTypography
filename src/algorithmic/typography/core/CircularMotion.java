/**
 * CircularMotion - Clockwise or counter-clockwise orbital movement.
 *
 * <p>Each cell's glyph orbits around the cell centre on a circle
 * of configurable {@code radius}.  A per-cell phase offset is
 * derived from the column and row so neighbouring glyphs don't
 * all rotate in lock-step.</p>
 *
 * <h2>Example</h2>
 * <pre>
 * // Clockwise, 8 px radius, normal speed
 * CircularMotion cw = new CircularMotion(8, 1.0, true);
 *
 * // Counter-clockwise, smaller orbit, faster
 * CircularMotion ccw = new CircularMotion(5, 2.0, false);
 * </pre>
 *
 * @author Michail Semoglou
 * @version 1.0.0
 * @since 1.0.0
 */

package algorithmic.typography.core;

import processing.core.PApplet;
import processing.core.PVector;

/**
 * Circular (CW / CCW) motion within a grid cell.
 */
public class CircularMotion extends CellMotion {

  /** {@code true} for clockwise, {@code false} for counter-clockwise. */
  private boolean clockwise;

  /** Per-cell phase spread. Higher = more variation between neighbours. */
  private float phaseSpread = 0.8f;

  // ── Constructors ─────────────────────────────────────────────

  /**
   * Creates a CircularMotion.
   *
   * @param radius    max displacement in pixels
   * @param speed     rotation speed (1.0 = normal)
   * @param clockwise {@code true} for CW, {@code false} for CCW
   */
  public CircularMotion(float radius, float speed, boolean clockwise) {
    this.radius    = radius;
    this.speed     = speed;
    this.clockwise = clockwise;
  }

  /** Default: 8 px, speed 1, clockwise. */
  public CircularMotion() {
    this(8, 1, true);
  }

  // ── Core ─────────────────────────────────────────────────────

  @Override
  public PVector getOffset(int col, int row, int frameCount) {
    float phase = (col * 0.7f + row * 1.3f) * phaseSpread;
    float dir   = clockwise ? 1.0f : -1.0f;
    float angle = dir * frameCount * speed * 0.03f + phase;

    float x = PApplet.cos(angle) * radius;
    float y = PApplet.sin(angle) * radius;
    return new PVector(x, y);
  }

  // ── Configuration ────────────────────────────────────────────

  /** Set rotation direction. */
  public void setClockwise(boolean cw) { this.clockwise = cw; }

  /** Returns whether the rotation direction is clockwise.
   *  @return {@code true} if clockwise */
  public boolean isClockwise() { return clockwise; }

  /**
   * Sets the phase offset spread between neighbouring cells.
   * Higher values make adjacent glyphs more out-of-phase.
   *
   * @param spread phase spread (default 0.8)
   */
  public void setPhaseSpread(float spread) { this.phaseSpread = spread; }
}
