/**
 * OrbitalMotion - Glyphs orbit their immediate neighbours, forming typographic constellations.
 *
 * <p>Each cell is assigned a set of virtual anchor points derived from its
 * nearest grid neighbours. The glyph in the cell then orbits the closest anchor
 * at a configurable radius and angular speed. A per-cell phase offset ensures
 * adjacent glyphs are out of sync, producing a living, constellation-like pattern
 * across the whole grid.</p>
 *
 * <p>The orbit radius and angular speed are configurable independently, and a
 * {@code phaseSpread} parameter controls how staggered neighbouring orbits are.
 * Setting {@code phaseSpread} to zero makes all glyphs orbit in lock-step.</p>
 *
 * <h2>Usage</h2>
 * <pre>
 * OrbitalMotion orbit = new OrbitalMotion(10, 0.6f);
 * config.setCellMotion(orbit);
 * </pre>
 *
 * <h2>Feel guide</h2>
 * <pre>
 * // Slow, wide orbits — planetary feel
 * OrbitalMotion slow = new OrbitalMotion(18, 0.3f);
 * slow.setPhaseSpread(1.4f);
 *
 * // Fast, tight, chaotic
 * OrbitalMotion fast = new OrbitalMotion(6, 2.5f);
 * fast.setPhaseSpread(0.4f);
 * </pre>
 *
 * @author Michail Semoglou
 * @version 0.3.0
 * @since 0.2.3
 * @see CellMotion
 * @see RippleMotion
 * @see FlowFieldMotion
 * @see CircularMotion
 */

package algorithmic.typography.core;

import processing.core.PApplet;
import processing.core.PVector;

/**
 * Glyphs orbit neighbouring cells, forming typographic constellations.
 */
public class OrbitalMotion extends CellMotion {

  // ── Orbital parameters ────────────────────────────────────────

  /**
   * Per-cell phase spread. Higher values stagger neighbours more.
   * 0 = all in lock-step; default: 1.1.
   */
  private float phaseSpread = 1.1f;

  /**
   * Secondary wobble amplitude as a fraction of {@code radius}.
   * Adds a slow radial oscillation so orbits breathe slightly.
   * Default: 0.25 (25 % of radius).
   */
  private float wobble = 0.25f;

  /**
   * Number of neighbours each glyph orbits. Currently only the nearest
   * neighbour (1) is used, but this is exposed for future multi-anchor modes.
   */
  private int anchorCount = 1;

  // ── Constructors ─────────────────────────────────────────────

  /** Default: 10 px radius, speed 0.6. */
  public OrbitalMotion() {
    this(10, 0.6f);
  }

  /**
   * Creates an OrbitalMotion with a given radius and speed.
   *
   * @param radius orbit radius in pixels (max displacement)
   * @param speed  angular speed multiplier (1.0 = normal)
   */
  public OrbitalMotion(float radius, float speed) {
    this.radius = radius;
    this.speed  = speed;
  }

  // ── Core ─────────────────────────────────────────────────────

  @Override
  public PVector getOffset(int col, int row, int frameCount) {
    // Per-cell phase: mixture of column and row so the diagonal is also staggered
    float phase = (col * 1.618f + row * 2.399f) * phaseSpread;

    // Angular position around the orbit
    float angle = frameCount * speed * 0.025f + phase;

    // Radial wobble: slow breathing of the orbit size
    float r = radius * (1.0f + wobble * PApplet.sin(frameCount * speed * 0.009f + phase * 0.7f));

    // The orbit "anchor" is a point offset from the cell centre toward the
    // nearest logical neighbour. We pick the neighbour direction using the
    // per-cell hash so different cells choose different anchor directions,
    // creating the constellation effect without needing to read actual
    // neighbour positions.
    float anchorAngle = (col * 2.7f + row * 1.43f) % (2 * PApplet.PI);
    float anchorDist  = r * 0.35f; // orbit around a point slightly off-centre

    float ax = PApplet.cos(anchorAngle) * anchorDist;
    float ay = PApplet.sin(anchorAngle) * anchorDist;

    float x = ax + PApplet.cos(angle) * r;
    float y = ay + PApplet.sin(angle) * r;

    // Clamp to radius
    PVector out = new PVector(x, y);
    if (out.mag() > radius * (1 + wobble)) {
      out.setMag(radius * (1 + wobble));
    }
    return out;
  }

  // ── Configuration ─────────────────────────────────────────────

  /**
   * Sets the per-cell phase spread (how staggered neighbouring orbits are).
   * @param spread phase spread (default 1.1)
   */
  public void setPhaseSpread(float spread) { this.phaseSpread = spread; }

  /** Returns the current phase spread. */
  public float getPhaseSpread() { return phaseSpread; }

  /**
   * Sets the radial wobble amplitude as a fraction of radius.
   * 0 = perfectly circular orbits. Default: 0.25.
   * @param wobble wobble fraction [0, 1]
   */
  public void setWobble(float wobble) { this.wobble = Math.max(0, Math.min(1, wobble)); }

  /** Returns the current wobble amplitude fraction. */
  public float getWobble() { return wobble; }
}
