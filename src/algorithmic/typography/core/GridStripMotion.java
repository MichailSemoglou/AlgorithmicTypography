/**
 * GridStripMotion — Grid-level animation layer that translates entire rows
 * or columns as rigid strips driven by a wave function.
 *
 * <p>Instead of the wave function modulating colour or brightness per cell,
 * {@code GridStripMotion} drives the <em>physical translation</em> of entire
 * rows and columns.  All cells in row N share the same Y displacement:
 * {@code wave(t + N × phaseStep) × amplitude × tileHeight}.  The effect
 * reads as a travelling wave in a bead curtain, a flag rippling across a
 * letterform, or a seismic shudder running through a word.</p>
 *
 * <p>Because strip displacement is a transform applied before any
 * per-cell {@link CellMotion}, the two systems compose naturally: a row
 * can simultaneously slide (strip motion) and have its individual glyphs
 * orbit or ripple (cell motion).</p>
 *
 * <h2>Axis modes</h2>
 * <ul>
 *   <li>{@link #ROW} — each row translates along Y (default)</li>
 *   <li>{@link #COLUMN} — each column translates along X</li>
 *   <li>{@link #BOTH} — simultaneous XY travel, creating a 2-D wave surface</li>
 * </ul>
 *
 * <h2>Quick start</h2>
 * <pre>
 * // Via config.json (recommended)
 * // "gridStripMotion": { "axis": "row", "amplitude": 0.3, "phaseStep": 0.4 }
 *
 * // Or programmatically
 * GridStripMotion strip = new GridStripMotion();
 * strip.setAxis(GridStripMotion.ROW);
 * strip.setAmplitude(0.3f);
 * strip.setPhaseStep(0.4f);
 * at.setGridStripMotion(strip);
 * </pre>
 *
 * <h2>Wave type overrides</h2>
 * <pre>
 * // Use the global waveType for rows, but PERLIN for columns
 * strip.setRowWaveType("SINE");
 * strip.setColumnWaveType("PERLIN");
 * </pre>
 *
 * <h2>Counter-travelling strips</h2>
 * <pre>
 * // Rows and columns travelling in opposite temporal directions
 * strip.setAxis(GridStripMotion.BOTH);
 * strip.setRowSpeed(1.0f);
 * strip.setColumnSpeed(-1.0f);
 * </pre>
 *
 * @author Michail Semoglou
 * @version 0.3.0
 * @since 0.2.6
 * @see CellMotion
 */

package algorithmic.typography.core;

import processing.core.PVector;

/**
 * Grid-level wave displacement applied to entire rows and / or columns.
 */
public class GridStripMotion {

  // ── Axis constants ───────────────────────────────────────────

  /** Each row translates along the Y axis. */
  public static final int ROW    = 0;

  /** Each column translates along the X axis. */
  public static final int COLUMN = 1;

  /** Both rows and columns are simultaneously displaced (2-D wave surface). */
  public static final int BOTH   = 2;

  // ── Parameters ───────────────────────────────────────────────

  /** Active axis mode ({@link #ROW}, {@link #COLUMN}, or {@link #BOTH}). Default: ROW. */
  private int axis = ROW;

  /**
   * Maximum pixel displacement per strip, expressed as a fraction of the
   * tile dimension in the axis direction (0 = no displacement, 1 = full tile).
   * Default: 0.3 (30 % of tile height for ROW axis).
   */
  private float amplitude = 0.3f;

  /**
   * Phase offset between adjacent strips in radians.
   * Larger values increase the number of wave cycles visible across the grid.
   * Default: 0.4 rad.
   */
  private float phaseStep = 0.4f;

  /**
   * Speed multiplier for the row wave.
   * Set a negative value to reverse the direction of travel.
   * Default: 1.0.
   */
  private float rowSpeed = 1.0f;

  /**
   * Speed multiplier for the column wave.
   * Set a negative value opposite to rowSpeed for counter-travelling strips.
   * Default: 1.0.
   */
  private float columnSpeed = 1.0f;

  /**
   * Wave shape used for row displacement.
   * Accepted values: "SINE" (default), "SQUARE", "TRIANGLE", "SAWTOOTH", "TANGENT", "PERLIN".
   * When null or unrecognised, falls back to SINE.
   */
  private String rowWaveType = "SINE";

  /**
   * Wave shape used for column displacement.
   * Accepted values: "SINE" (default), "SQUARE", "TRIANGLE", "SAWTOOTH", "TANGENT", "PERLIN".
   */
  private String columnWaveType = "SINE";

  // ── Constructor ───────────────────────────────────────────────

  /** Creates a {@code GridStripMotion} with default ROW axis and gentle amplitude. */
  public GridStripMotion() {}

  /**
   * Creates a {@code GridStripMotion} with the specified axis mode and amplitude.
   *
   * @param axis      {@link #ROW}, {@link #COLUMN}, or {@link #BOTH}
   * @param amplitude designer-unit strip amplitude (0 – 1, fraction of tile size)
   * @param phaseStep phase offset between adjacent strips in radians
   */
  public GridStripMotion(int axis, float amplitude, float phaseStep) {
    this.axis      = constrain(axis, ROW, BOTH);
    this.amplitude = Math.max(0, amplitude);
    this.phaseStep = phaseStep;
  }

  // ── Core ─────────────────────────────────────────────────────

  /**
   * Computes the strip-level XY displacement for the cell at ({@code col}, {@code row}).
   *
   * <p>All cells sharing the same row receive the same Y offset; all cells
   * sharing the same column receive the same X offset. Per-cell
   * {@link CellMotion} offsets should be added on top of this value.</p>
   *
   * @param col      grid column index
   * @param row      grid row index
   * @param tilesX   total number of grid columns
   * @param tilesY   total number of grid rows
   * @param tileW    pixel width of one tile
   * @param tileH    pixel height of one tile
   * @param frameCount current Processing frameCount
   * @return XY displacement vector (pixels)
   */
  public PVector getStripOffset(int col, int row,
                                float tilesX, float tilesY,
                                float tileW,  float tileH,
                                int frameCount) {
    float dx = 0, dy = 0;

    if (axis == ROW || axis == BOTH) {
      float t = frameCount * rowSpeed * 0.02f + row * phaseStep;
      dy = evaluateWave(rowWaveType, t) * amplitude * tileH;
    }
    if (axis == COLUMN || axis == BOTH) {
      float t = frameCount * columnSpeed * 0.02f + col * phaseStep;
      dx = evaluateWave(columnWaveType, t) * amplitude * tileW;
    }

    return new PVector(dx, dy);
  }

  // ── Wave evaluation ───────────────────────────────────────────

  /**
   * Evaluates a named wave function at time {@code t}.
   * All functions return values in approximately [-1, 1].
   *
   * @param waveType wave type name (case-insensitive)
   * @param t        time/phase argument in radians
   * @return wave value in [-1, 1]
   */
  private float evaluateWave(String waveType, float t) {
    if (waveType == null) return (float) Math.sin(t);
    switch (waveType.toUpperCase().trim()) {
      case "SQUARE":
        return Math.sin(t) >= 0 ? 1f : -1f;
      case "TRIANGLE":
        float tri = (float)(t / Math.PI % 2.0);
        if (tri < 0) tri += 2.0f;
        return tri < 1.0f ? 2f * tri - 1f : 3f - 2f * tri;
      case "SAWTOOTH":
        float saw = (float)(t / (2.0 * Math.PI) % 1.0);
        if (saw < 0) saw += 1.0f;
        return 2f * saw - 1f;
      case "TANGENT":
        // Soft-clipped tangent: tan clamped to [-1, 1] for stability
        return Math.max(-1f, Math.min(1f, (float) Math.tan(t * 0.5)));
      case "PERLIN":
        // Self-contained value noise using the same seed-hash approach as FlowFieldMotion
        return perlinNoise1D(t) * 2f - 1f;
      case "SINE":
      default:
        return (float) Math.sin(t);
    }
  }

  /**
   * Minimal value-noise implementation for the PERLIN wave type.
   * Returns a smooth pseudo-random value in [0, 1].
   */
  private float perlinNoise1D(float t) {
    int ix = (int) Math.floor(t);
    float fx = t - ix;
    float u = fx * fx * (3.0f - 2.0f * fx); // smoothstep
    float a = valueAt(ix);
    float b = valueAt(ix + 1);
    return a + u * (b - a);
  }

  private float valueAt(int i) {
    int h = i * 1619 + 31337;
    h = (h ^ (h >> 14)) * 0x3f159b;
    h = (h ^ (h >> 16));
    return ((h & 0x7fffffff) / (float) 0x7fffffff);
  }

  // ── Helpers ───────────────────────────────────────────────────

  private int constrain(int v, int lo, int hi) {
    return v < lo ? lo : (v > hi ? hi : v);
  }

  // ── Getters / setters ─────────────────────────────────────────

  /**
   * Sets the axis mode.
   *
   * @param axis {@link #ROW}, {@link #COLUMN}, or {@link #BOTH}
   * @return this for chaining
   */
  public GridStripMotion setAxis(int axis) {
    this.axis = constrain(axis, ROW, BOTH);
    return this;
  }

  /** Returns the current axis mode. @return axis constant */
  public int getAxis() { return axis; }

  /**
   * Sets the strip amplitude as a fraction of the tile dimension
   * in the displacement direction (0 = no movement, 1 = full tile width/height).
   *
   * @param amplitude designer-unit amplitude (0 – 1)
   * @return this for chaining
   */
  public GridStripMotion setAmplitude(float amplitude) {
    this.amplitude = Math.max(0, amplitude);
    return this;
  }

  /** Returns the strip amplitude (0 – 1). @return amplitude */
  public float getAmplitude() { return amplitude; }

  /**
   * Sets the phase offset between adjacent strips in radians.
   * Controls how many wave cycles are visible across the grid.
   *
   * @param phaseStep phase step in radians per strip
   * @return this for chaining
   */
  public GridStripMotion setPhaseStep(float phaseStep) {
    this.phaseStep = phaseStep;
    return this;
  }

  /** Returns the inter-strip phase offset in radians. @return phaseStep */
  public float getPhaseStep() { return phaseStep; }

  /**
   * Sets the row wave speed multiplier.
   * Negative values reverse the direction of wave travel.
   *
   * @param rowSpeed row wave speed
   * @return this for chaining
   */
  public GridStripMotion setRowSpeed(float rowSpeed) {
    this.rowSpeed = rowSpeed;
    return this;
  }

  /** Returns the row wave speed. @return rowSpeed */
  public float getRowSpeed() { return rowSpeed; }

  /**
   * Sets the column wave speed multiplier.
   *
   * @param columnSpeed column wave speed
   * @return this for chaining
   */
  public GridStripMotion setColumnSpeed(float columnSpeed) {
    this.columnSpeed = columnSpeed;
    return this;
  }

  /** Returns the column wave speed. @return columnSpeed */
  public float getColumnSpeed() { return columnSpeed; }

  /**
   * Sets a uniform speed for both row and column waves.
   *
   * @param speed speed multiplier applied to both axes
   * @return this for chaining
   */
  public GridStripMotion setSpeed(float speed) {
    this.rowSpeed    = speed;
    this.columnSpeed = speed;
    return this;
  }

  /**
   * Sets the wave shape for row displacement.
   * Accepted values (case-insensitive): SINE, SQUARE, TRIANGLE, SAWTOOTH, TANGENT, PERLIN.
   *
   * @param waveType wave type name
   * @return this for chaining
   */
  public GridStripMotion setRowWaveType(String waveType) {
    this.rowWaveType = (waveType != null) ? waveType.toUpperCase().trim() : "SINE";
    return this;
  }

  /** Returns the row wave type name. @return rowWaveType */
  public String getRowWaveType() { return rowWaveType; }

  /**
   * Sets the wave shape for column displacement.
   * Accepted values (case-insensitive): SINE, SQUARE, TRIANGLE, SAWTOOTH, TANGENT, PERLIN.
   *
   * @param waveType wave type name
   * @return this for chaining
   */
  public GridStripMotion setColumnWaveType(String waveType) {
    this.columnWaveType = (waveType != null) ? waveType.toUpperCase().trim() : "SINE";
    return this;
  }

  /** Returns the column wave type name. @return columnWaveType */
  public String getColumnWaveType() { return columnWaveType; }

  /**
   * Sets the same wave type for both row and column axes.
   *
   * @param waveType wave type name
   * @return this for chaining
   */
  public GridStripMotion setWaveType(String waveType) {
    setRowWaveType(waveType);
    setColumnWaveType(waveType);
    return this;
  }
}
