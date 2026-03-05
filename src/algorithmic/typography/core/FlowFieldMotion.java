/**
 * FlowFieldMotion - Glyphs drift as particles in a slowly evolving curl-noise vector field.
 *
 * <p>Each cell's displacement is derived from the mathematical curl of a Perlin
 * scalar potential, producing a <em>divergence-free</em> (solenoidal) vector field.
 * Unlike directly mapping noise to an angle, curl noise creates genuine eddies —
 * closed circulation zones that glyphs orbit rather than converge into or scatter
 * from. The field is sampled independently per octave (for multi-scale structure)
 * and each cell receives a deterministic per-cell phase seed, so neighbouring cells
 * evolve through the field at offset phases rather than in synchronised lockstep.</p>
 *
 * <p>Unlike {@code PerlinMotion}, which gives each glyph a fully independent noise
 * path, {@code FlowFieldMotion} creates a <em>spatially coherent</em> field —
 * adjacent cells share the same underlying current while retaining individual
 * phase independence.</p>
 *
 * <h2>Usage</h2>
 * <pre>
 * FlowFieldMotion flow = new FlowFieldMotion();
 * config.setCellMotion(flow);
 * </pre>
 *
 * <h2>Feel guide</h2>
 * <pre>
 * // Slow, wide eddies — visible circulation zones sweep the grid
 * FlowFieldMotion slow = new FlowFieldMotion();
 * slow.setFieldScale(0.004f);  slow.setEvolutionRate(0.003f);
 * slow.setPhaseRange(10.0f);   // strong cell-to-cell independence
 *
 * // Fast multi-scale turbulence
 * FlowFieldMotion fast = new FlowFieldMotion();
 * fast.setFieldScale(0.012f);  fast.setEvolutionRate(0.012f);
 * fast.setOctaves(3);          fast.setPersistence(0.5f);
 * </pre>
 *
 * @author Michail Semoglou
 * @version 0.2.4
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

  /**
   * Number of octave layers summed to form the curl field. Higher values add
   * finer-grained turbulence on top of the large-scale current.
   * Default: 2. Range: 1–4.
   */
  private int octaves = 2;

  /**
   * Frequency multiplier from one octave to the next (standard lacunarity).
   * Default: 2.0.
   */
  private float lacunarity = 2.0f;

  /**
   * Amplitude multiplier from one octave to the next.
   * Lower values reduce the influence of finer octaves. Default: 0.45.
   */
  private float persistence = 0.45f;

  /**
   * Maximum Z-axis spread used for per-cell phase seeding.
   * Each (col, row) pair is hashed to a deterministic offset in [0, phaseRange],
   * so neighbouring cells evolve through the field at different phases —
   * eliminating the whole-grid "block" synchrony.
   * Default: 8.0. Set to 0 to restore fully synchronised behaviour.
   */
  private float phaseRange = 8.0f;

  /**
   * Finite-difference step used when numerically deriving the curl.
   * In noise-coordinate space; independent of fieldScale.
   * Default: 0.3.
   */
  private float curlEpsilon = 0.3f;

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

    // Per-cell deterministic phase: each (col,row) gets a unique Z-axis offset
    // so cells evolve through the field at different phases — no block lockstep.
    float cellZ = cellPhase(col, row) * phaseRange;
    float tz    = t + zOffset + cellZ;

    // Multi-octave curl noise.
    // Curl is derived from the gradient of a scalar potential P(x,y,t):
    //   curlX =  ∂P/∂y,  curlY = -∂P/∂x
    // Approximated numerically with a symmetric finite difference.
    // This produces a divergence-free (solenoidal) field — glyphs orbit
    // eddies rather than converging to or diverging from fixed points.
    float cx = 0, cy = 0;
    float freq     = fieldScale;
    float ampScale = 1.0f;
    float totalAmp = 0.0f;

    for (int o = 0; o < octaves; o++) {
      float px = col * freq;
      float py = row * freq;
      float oz = tz + o * 17.3f; // separate Z slice per octave

      float dPdY =  noise3D(px,              py + curlEpsilon, oz)
                  - noise3D(px,              py - curlEpsilon, oz);
      float dPdX =  noise3D(px + curlEpsilon, py,              oz)
                  - noise3D(px - curlEpsilon, py,              oz);

      cx       +=  dPdY  * ampScale;
      cy       += -dPdX  * ampScale;
      totalAmp += ampScale;

      freq     *= lacunarity;
      ampScale *= persistence;
    }

    // Normalise by total amplitude weight then scale to radius.
    // Raw diffs are in [-1,1]; result maps to [-radius, radius] per axis.
    cx = (cx / totalAmp) * radius;
    cy = (cy / totalAmp) * radius;

    return new PVector(cx, cy);
  }

  /** Deterministic per-cell phase in [0, 1] derived from a fast integer hash. */
  private static float cellPhase(int col, int row) {
    int h = col * 1664525 + row * 1013904223 + 22695477;
    h ^= (h >>> 16);
    h *= 0x45d9f3b;
    h ^= (h >>> 16);
    return ((h & 0x7FFFFFFF) % 10000) / 10000.0f;
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

  /** Returns the spatial scale of the noise field. @return fieldScale */
  public float getFieldScale()    { return fieldScale; }

  /** Returns the temporal evolution rate. @return evolutionRate */
  public float getEvolutionRate() { return evolutionRate; }

  /**
   * Sets the number of octave layers (1–4). More octaves add finer detail
   * but increase computation slightly. Default: 2.
   * @param n number of octaves
   */
  public void setOctaves(int n) { this.octaves = Math.max(1, Math.min(4, n)); }

  /**
   * Sets the amplitude decay per octave (persistence).
   * 1.0 = all octaves equal weight; 0.0 = only first octave. Default: 0.45.
   * @param p persistence factor in [0, 1]
   */
  public void setPersistence(float p) { this.persistence = Math.max(0, Math.min(1, p)); }

  /**
   * Sets the maximum per-cell Z-axis phase spread.
   * Larger values increase the independence between neighbouring cells.
   * 0 disables per-cell phase (restores fully synchronised field). Default: 8.0.
   * @param range phase range (≥ 0)
   */
  public void setPhaseRange(float range) { this.phaseRange = Math.max(0, range); }

  /** Returns the number of octave layers. @return octaves */
  public int getOctaves()       { return octaves; }

  /** Returns the persistence (amplitude decay per octave). @return persistence */
  public float getPersistence() { return persistence; }

  /** Returns the maximum per-cell phase spread. @return phaseRange */
  public float getPhaseRange()  { return phaseRange; }
}
