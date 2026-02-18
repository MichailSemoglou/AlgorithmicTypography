/**
 * PerlinMotion - Perlin-noise driven movement within a grid cell.
 *
 * <p>Each glyph wanders organically inside its cell using two
 * independent Perlin noise channels (one for x, one for y).
 * The noise seed is offset per cell so every glyph follows
 * its own unique path.</p>
 *
 * <h2>Example</h2>
 * <pre>
 * PerlinMotion perlin = new PerlinMotion(10, 0.8);
 *
 * // in draw():
 * PVector off = perlin.getOffset(col, row, frameCount);
 * text(ch, cx + off.x, cy + off.y);
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
 * Perlin-noise organic motion within a grid cell.
 */
public class PerlinMotion extends CellMotion {

  /** Noise time-increment per frame (lower = smoother). */
  private float noiseScale = 0.012f;

  /** Spatial seed offset between neighbouring cells. */
  private float cellSeedSpread = 100.0f;

  // ── Constructors ─────────────────────────────────────────────

  /**
   * Creates a PerlinMotion.
   *
   * @param radius max displacement in pixels
   * @param speed  time speed (1.0 = normal)
   */
  public PerlinMotion(float radius, float speed) {
    this.radius = radius;
    this.speed  = speed;
  }

  /** Default: 10 px radius, speed 1. */
  public PerlinMotion() {
    this(10, 1);
  }

  // ── Core ─────────────────────────────────────────────────────

  @Override
  public PVector getOffset(int col, int row, int frameCount) {
    float t = frameCount * noiseScale * speed;

    // Two independent noise channels offset per cell
    float seedX = col * cellSeedSpread + row * cellSeedSpread * 0.37f;
    float seedY = col * cellSeedSpread * 0.61f + row * cellSeedSpread;

    // noise() returns 0-1; centre on 0 and scale to [-radius, radius]
    float nx = (float)(noise2D(seedX + t, seedY) - 0.5) * 2.0f * radius;
    float ny = (float)(noise2D(seedX, seedY + t) - 0.5) * 2.0f * radius;

    return new PVector(nx, ny);
  }

  /**
   * Simple 2D Perlin-style noise (value noise using smoothed lattice).
   * Self-contained so we don't need a PApplet reference.
   */
  private static float noise2D(float x, float y) {
    // Use a seeded hash for lattice values
    int xi = fastFloor(x);
    int yi = fastFloor(y);
    float xf = x - xi;
    float yf = y - yi;

    // Smoothstep
    float u = xf * xf * (3 - 2 * xf);
    float v = yf * yf * (3 - 2 * yf);

    float n00 = lattice(xi,     yi);
    float n10 = lattice(xi + 1, yi);
    float n01 = lattice(xi,     yi + 1);
    float n11 = lattice(xi + 1, yi + 1);

    float nx0 = lerp(n00, n10, u);
    float nx1 = lerp(n01, n11, u);
    return lerp(nx0, nx1, v);
  }

  private static float lattice(int x, int y) {
    // Hash to pseudo-random 0-1
    int n = x + y * 57;
    n = (n << 13) ^ n;
    return (1.0f - ((n * (n * n * 15731 + 789221) + 1376312589) & 0x7fffffff) / 1073741824.0f) * 0.5f + 0.5f;
  }

  private static int fastFloor(float x) {
    int xi = (int) x;
    return x < xi ? xi - 1 : xi;
  }

  private static float lerp(float a, float b, float t) {
    return a + t * (b - a);
  }

  // ── Configuration ────────────────────────────────────────────

  /**
   * Sets the noise time scale (lower = smoother, slower movement).
   *
   * @param scale noise increment per frame (default 0.012)
   */
  public void setNoiseScale(float scale) { this.noiseScale = Math.max(0.001f, scale); }

  /**
   * Sets the seed offset between neighbouring cells.
   * Higher values make adjacent cells more independent.
   *
   * @param spread cell seed spread (default 100)
   */
  public void setCellSeedSpread(float spread) { this.cellSeedSpread = spread; }
}
