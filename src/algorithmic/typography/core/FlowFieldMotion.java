/**
 * FlowFieldMotion - Glyphs drift as particles in a slowly evolving Perlin-noise vector field.
 *
 * <p>A 2-D Perlin-noise field is evaluated at each cell's world position. The
 * resulting angle drives the displacement direction; the magnitude tapers toward
 * the cell edges so glyphs never stray outside their tile. The field itself
 * evolves over time, producing smooth, slowly shifting currents across the grid.</p>
 *
 * <p>Unlike {@code PerlinMotion}, which gives each glyph an independent noise path,
 * {@code FlowFieldMotion} creates a <em>spatially coherent</em> field — adjacent
 * cells flow together, forming visible currents and eddies that travel across the
 * whole grid.</p>
 *
 * <h2>Usage</h2>
 * <pre>
 * FlowFieldMotion flow = new FlowFieldMotion();
 * config.setCellMotion(flow);
 * </pre>
 *
 * <h2>Feel guide</h2>
 * <pre>
 * // Slow, wide eddies
 * FlowFieldMotion slow = new FlowFieldMotion();
 * slow.setFieldScale(0.004f);   slow.setEvolutionRate(0.003f);
 *
 * // Fast turbulence
 * FlowFieldMotion fast = new FlowFieldMotion();
 * fast.setFieldScale(0.012f);   fast.setEvolutionRate(0.012f);
 * </pre>
 *
 * @author Michail Semoglou
 * @version 0.2.3
 * @since 0.2.3
 * @see CellMotion
 * @see RippleMotion
 * @see OrbitalMotion
 * @see PerlinMotion
 */

package algorithmic.typography.core;

import processing.core.PVector;

/**
 * Perlin-noise vector field that glyphs follow as drifting particles.
 */
public class FlowFieldMotion extends CellMotion {

  // ── Field parameters ─────────────────────────────────────────

  /**
   * Spatial scale of the noise field. Lower values produce larger, smoother
   * structures; higher values produce smaller, more turbulent eddies.
   * Default: 0.007.
   */
  private float fieldScale = 0.007f;

  /**
   * How fast the noise field evolves over time (temporal scale per frame).
   * Default: 0.005.
   */
  private float evolutionRate = 0.005f;

  /**
   * Offset added to the noise Z axis so this field is statistically independent
   * from other Perlin-based effects in the same sketch.
   * Default: 100.
   */
  private float zOffset = 100.0f;

  // ── Constructors ─────────────────────────────────────────────

  /** Default: 10 px radius, moderate field scale, slow evolution. */
  public FlowFieldMotion() {
    this.radius = 10;
  }

  /**
   * Creates a FlowFieldMotion with custom parameters.
   *
   * @param radius        maximum displacement radius in pixels
   * @param fieldScale    spatial scale of the noise field
   * @param evolutionRate temporal evolution speed per frame
   */
  public FlowFieldMotion(float radius, float fieldScale, float evolutionRate) {
    this.radius        = radius;
    this.fieldScale    = fieldScale;
    this.evolutionRate = evolutionRate;
  }

  // ── Core ─────────────────────────────────────────────────────

  @Override
  public PVector getOffset(int col, int row, int frameCount) {
    float t = frameCount * evolutionRate * speed;

    // Sample two staggered noise channels to get an angle in [0, 2π]
    float nx = noise3D(col * fieldScale, row * fieldScale * 0.8f, t + zOffset);
    float ny = noise3D(col * fieldScale * 0.8f, row * fieldScale, t + zOffset + 31.41f);

    // Map [0,1] → [0, 2π] for each channel, then use as angle components
    float angle = nx * 2 * (float) Math.PI * 2; // full two-cycle sweep
    float mag   = (ny * 0.5f + 0.5f) * radius;   // 0..radius

    float x = (float) Math.cos(angle) * mag;
    float y = (float) Math.sin(angle) * mag;
    return new PVector(x, y);
  }

  // ── Noise ─────────────────────────────────────────────────────

  /** Value noise lattice with 3-D coordinates (x, y, z). */
  private static float noise3D(float x, float y, float z) {
    int xi = fastFloor(x);
    int yi = fastFloor(y);
    int zi = fastFloor(z);
    float xf = x - xi;
    float yf = y - yi;
    float zf = z - zi;
    float u  = fade(xf);
    float v  = fade(yf);
    float w  = fade(zf);

    float n000 = lattice(xi,     yi,     zi);
    float n100 = lattice(xi + 1, yi,     zi);
    float n010 = lattice(xi,     yi + 1, zi);
    float n110 = lattice(xi + 1, yi + 1, zi);
    float n001 = lattice(xi,     yi,     zi + 1);
    float n101 = lattice(xi + 1, yi,     zi + 1);
    float n011 = lattice(xi,     yi + 1, zi + 1);
    float n111 = lattice(xi + 1, yi + 1, zi + 1);

    float nx00 = lerp(n000, n100, u);
    float nx10 = lerp(n010, n110, u);
    float nx01 = lerp(n001, n101, u);
    float nx11 = lerp(n011, n111, u);
    float nxy0 = lerp(nx00, nx10, v);
    float nxy1 = lerp(nx01, nx11, v);
    return lerp(nxy0, nxy1, w);
  }

  private static float fade(float t) { return t * t * (3 - 2 * t); }

  private static float lattice(int x, int y, int z) {
    int n = x + y * 57 + z * 131;
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

  // ── Configuration ─────────────────────────────────────────────

  /**
   * Sets the spatial scale of the noise field.
   * Lower = larger, smoother structures. Default: 0.007.
   * @param scale field scale (must be &gt; 0)
   */
  public void setFieldScale(float scale) { this.fieldScale = Math.max(0.0001f, scale); }

  /**
   * Sets how fast the noise field evolves each frame.
   * @param rate evolution rate per frame. Default: 0.005.
   */
  public void setEvolutionRate(float rate) { this.evolutionRate = Math.max(0, rate); }

  /**
   * Sets the Z-axis noise seed offset (differentiates this field from other uses
   * of noise in the same sketch). Default: 100.
   * @param z seed offset
   */
  public void setSeedOffset(float z) { this.zOffset = z; }
}
