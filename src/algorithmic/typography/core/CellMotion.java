/**
 * CellMotion - Abstract base for per-cell glyph movement.
 *
 * <p>Subclasses compute an (x, y) offset that displaces a glyph
 * from the centre of its grid cell each frame.  The offset is
 * expressed in pixels and should stay within a configurable
 * {@code radius} so the glyph remains visually inside its tile.</p>
 *
 * <h2>Usage</h2>
 * <pre>
 * CellMotion motion = new CircularMotion(radius, speed, true);
 * // in draw():
 * PVector off = motion.getOffset(col, row, frameCount);
 * text(ch, cx + off.x, cy + off.y);
 * </pre>
 *
 * @author Michail Semoglou
 * @version 1.0.0
 * @since 1.0.0
 * @see CircularMotion
 * @see PerlinMotion
 */

package algorithmic.typography.core;

import processing.core.PVector;

/**
 * Abstract base for glyph-in-cell motion strategies.
 */
public abstract class CellMotion {

  /** Maximum displacement from cell centre (pixels). */
  protected float radius = 8.0f;

  /** Animation speed multiplier. */
  protected float speed = 1.0f;

  /**
   * Computes the (x, y) offset for a cell at the given frame.
   *
   * @param col        grid column index
   * @param row        grid row index
   * @param frameCount current Processing frameCount
   * @return displacement vector (pixels) from cell centre
   */
  public abstract PVector getOffset(int col, int row, int frameCount);

  // ── Configuration ────────────────────────────────────────────

  /**
   * Sets the maximum displacement radius.
   *
   * @param r radius in pixels
   */
  public void setRadius(float r) { this.radius = Math.max(0, r); }

  /** Returns the current displacement radius.
   *  @return the current radius */
  public float getRadius() { return radius; }

  /**
   * Sets the animation speed multiplier.
   *
   * @param s speed (1.0 = normal)
   */
  public void setSpeed(float s) { this.speed = s; }

  /** Returns the current animation speed multiplier.
   *  @return the current speed multiplier */
  public float getSpeed() { return speed; }
}
