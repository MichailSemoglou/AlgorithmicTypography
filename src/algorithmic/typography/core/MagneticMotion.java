/**
 * MagneticMotion - Glyphs are repelled from or attracted toward the mouse pointer.
 *
 * <p>Each glyph behaves as a point charge in a magnetic field centred on the
 * current mouse cursor.  The field strength falls off with distance according
 * to an inverse-distance law with a configurable {@code falloff} term that
 * prevents the force from becoming infinite when the cursor sits exactly on a
 * cell centre:</p>
 *
 * <pre>
 *   force = strength / (distance + falloff)
 * </pre>
 *
 * <p>The resulting displacement is clamped to {@code radius} and smoothed
 * over time with a simple exponential-moving-average lerp, so glyphs
 * glide fluidly to their new positions rather than snapping.</p>
 *
 * <h2>Usage</h2>
 * <pre>
 * // Repel (default) — glyphs flee the cursor
 * MagneticMotion mag = new MagneticMotion(this);
 * mag.setTileGrid(width, height, tilesX, tilesY);
 * config.setCellMotion(mag);
 *
 * // Attract — glyphs follow the cursor
 * MagneticMotion attract = new MagneticMotion(this, 1800, 80, false);
 * attract.setTileGrid(width, height, tilesX, tilesY);
 * config.setCellMotion(attract);
 * </pre>
 *
 * <h2>Feel guide</h2>
 * <pre>
 * // Gentle ripple — glyphs drift away slowly
 * MagneticMotion gentle = new MagneticMotion(this);
 * gentle.setStrength(600);  gentle.setFalloff(120);  gentle.setSmoothing(0.06f);
 *
 * // Snap-repel — glyphs jump away and snap back instantly
 * MagneticMotion snap = new MagneticMotion(this);
 * snap.setStrength(3000);  snap.setFalloff(40);  snap.setSmoothing(0.35f);
 *
 * // Rubber band attract — glyphs cluster on the cursor
 * MagneticMotion rubber = new MagneticMotion(this, 2000, 60, false);
 * rubber.setSmoothing(0.10f);
 * </pre>
 *
 * @author Michail Semoglou
 * @version 0.2.6
 * @since 0.2.1
 * @see CellMotion
 * @see GravityMotion
 * @see SpringMotion
 */

package algorithmic.typography.core;

import java.util.HashMap;
import processing.core.PApplet;
import processing.core.PVector;

/**
 * Mouse-driven repel / attract motion within a grid cell.
 */
public class MagneticMotion extends CellMotion {

  // ── Named intensity presets ───────────────────────────────────

  /** Gentle drift: glyphs drift away slowly with a wide, soft field. */
  public static final int GENTLE   = 0;

  /** Moderate effect: balanced strength and falloff (default feel). */
  public static final int MODERATE = 1;

  /** Strong push: glyphs are pushed forcefully with a tighter field. */
  public static final int STRONG   = 2;

  /** Snap repel: glyphs jump away instantly; ideal for sharp, reactive visuals. */
  public static final int SNAPPING = 3;

  // ── Reference to host sketch ──────────────────────────────────

  /** Processing sketch used to read {@code mouseX} / {@code mouseY}. */
  private final PApplet sketch;

  // ── Grid geometry ──────────────────────────────────────────────

  /**
   * Width of a single grid tile in pixels.
   * Used to convert (col, row) indices into world-space coordinates.
   * Set via {@link #setTileSize(float, float)} or
   * {@link #setTileGrid(float, float, int, int)}.
   */
  private float tileWidth = 45.0f;

  /**
   * Height of a single grid tile in pixels.
   */
  private float tileHeight = 45.0f;

  // ── Field parameters ──────────────────────────────────────────

  /**
   * Overall field strength.  Higher values push / pull glyphs further
   * from / toward the cursor.  Typical range: 400 – 4000.
   */
  private float strength = 1800.0f;

  /**
   * Distance (pixels) at which the force is exactly half its
   * zero-distance value.  Also prevents division-by-zero when the
   * cursor overlaps a cell centre.  Typical range: 30 – 200.
   */
  private float falloff = 80.0f;

  /**
   * Lerp factor applied each frame to smooth the glyph's position.
   * 0 = frozen, 1 = instantaneous snap.  Typical range: 0.05 – 0.4.
   */
  private float smoothing = 0.12f;

  /**
   * Polarity of the magnetic field.
   * {@code true} = attract (glyphs move toward the cursor);
   * {@code false} = repel (glyphs flee the cursor, default).
   */
  private boolean attract = false;

  // ── Per-cell state ────────────────────────────────────────────

  /**
   * Stores [smoothedOffX, smoothedOffY, lastFrame] for every (col, row) pair.
   * Keyed by {@code ((long) col << 32) | (row & 0xFFFFFFFFL)}.
   */
  private final HashMap<Long, float[]> cellState = new HashMap<>();

  // ── Constructors ──────────────────────────────────────────────

  /**
   * Full constructor.
   *
   * @param sketch   the host Processing sketch (required for mouse access)
   * @param strength overall field strength (typical range 400 – 4000)
   * @param falloff  half-force distance in pixels (typical range 30 – 200)
   * @param attract  {@code true} to attract, {@code false} to repel
   */
  public MagneticMotion(PApplet sketch, float strength,
                        float falloff, boolean attract) {
    this.sketch   = sketch;
    this.strength = strength;
    this.falloff  = falloff;
    this.attract  = attract;
    this.radius   = 20.0f;
  }

  /**
   * Creates a repelling MagneticMotion with default strength and falloff.
   *
   * @param sketch the host Processing sketch
   */
  public MagneticMotion(PApplet sketch) {
    this(sketch, 1800.0f, 80.0f, false);
  }

  // ── Grid helper ───────────────────────────────────────────────

  /**
   * Sets the pixel dimensions of a single grid tile.
   * This is the raw form — use {@link #setTileGrid(float, float, int, int)}
   * to derive tile size from canvas dimensions and tile counts.
   *
   * @param tileWidth  pixel width of one tile
   * @param tileHeight pixel height of one tile
   */
  public void setTileSize(float tileWidth, float tileHeight) {
    this.tileWidth  = Math.max(1, tileWidth);
    this.tileHeight = Math.max(1, tileHeight);
  }

  /**
   * Convenience method: computes and stores tile dimensions from the
   * canvas size and the number of tiles.  Call this once in
   * {@code setup()} after {@code size()}.
   *
   * <pre>
   * mag.setTileGrid(width, height, 24, 24);
   * </pre>
   *
   * @param canvasWidth  sketch width in pixels
   * @param canvasHeight sketch height in pixels
   * @param tilesX       number of columns in the grid
   * @param tilesY       number of rows in the grid
   */
  public void setTileGrid(float canvasWidth, float canvasHeight,
                          int tilesX, int tilesY) {
    this.tileWidth  = canvasWidth  / Math.max(1, tilesX);
    this.tileHeight = canvasHeight / Math.max(1, tilesY);
  }

  // ── Core ──────────────────────────────────────────────────────

  /**
   * {@inheritDoc}
   *
   * <p>Computes the magnetic displacement for the cell at
   * ({@code col}, {@code row}) based on the current mouse position and
   * exponentially smooths the result toward the target offset.</p>
   */
  @Override
  public PVector getOffset(int col, int row, int frameCount) {
    long key = ((long) col << 32) | (row & 0xFFFFFFFFL);

    // Retrieve or initialise per-cell state: [offX, offY, lastFrame]
    float[] s = cellState.computeIfAbsent(key, k -> new float[]{0, 0, -1});

    // Same-frame guard — return cached result if already updated this frame
    if ((int) s[2] == frameCount) {
      return new PVector(s[0], s[1]);
    }
    s[2] = frameCount;

    // ── World position of this cell's centre ──────────────────────
    float worldX = col * tileWidth  + tileWidth  * 0.5f;
    float worldY = row * tileHeight + tileHeight * 0.5f;

    // ── Vector from cell centre to mouse ─────────────────────────
    float dx   = sketch.mouseX - worldX;
    float dy   = sketch.mouseY - worldY;
    float dist = PApplet.sqrt(dx * dx + dy * dy);

    // ── Magnetic force (inverse-distance with falloff softening) ──
    float targetX = 0, targetY = 0;
    if (dist > 0.001f) {
      float force = strength / (dist + falloff);
      force = Math.min(force, radius);    // clamp to radius

      float nx = dx / dist;              // unit vector toward mouse
      float ny = dy / dist;

      if (attract) {
        targetX =  nx * force;           // toward cursor
        targetY =  ny * force;
      } else {
        targetX = -nx * force;           // away from cursor
        targetY = -ny * force;
      }
    }

    // ── Exponential smoothing ─────────────────────────────────────
    float lerpFactor = PApplet.constrain(smoothing * speed, 0.001f, 1.0f);
    s[0] += (targetX - s[0]) * lerpFactor;
    s[1] += (targetY - s[1]) * lerpFactor;

    return new PVector(s[0], s[1]);
  }

  // ── Polarity toggle ───────────────────────────────────────────

  /**
   * Flips the magnetic polarity between attract and repel.
   * Glyphs will smoothly transition to the new direction.
   */
  public void togglePolarity() { this.attract = !this.attract; }

  // ── Configuration ─────────────────────────────────────────────

  /**
   * Sets the overall field strength.
   * Higher values produce larger displacements.
   *
   * @param strength field strength (typical range 400 – 4000)
   */
  public void setStrength(float strength) {
    this.strength = Math.max(0, strength);
  }

  /** Returns the field strength.
   *  @return strength */
  public float getStrength() { return strength; }

  /**
   * Sets the falloff distance.
   * Larger values create a wider, gentler field;
   * smaller values concentrate force near the cursor.
   *
   * @param falloff half-force distance in pixels (must be &gt; 0)
   */
  public void setFalloff(float falloff) {
    this.falloff = Math.max(0.001f, falloff);
  }

  /** Returns the falloff distance.
   *  @return falloff in pixels */
  public float getFalloff() { return falloff; }

  /**
   * Sets the position-smoothing factor (lerp per frame).
   * Lower values = slower, more fluid response;
   * higher values = snappier, more direct tracking.
   *
   * @param smoothing lerp factor (0.0 – 1.0)
   */
  public void setSmoothing(float smoothing) {
    this.smoothing = PApplet.constrain(smoothing, 0.001f, 1.0f);
  }

  /** Returns the smoothing factor.
   *  @return smoothing (0.0 – 1.0) */
  public float getSmoothing() { return smoothing; }

  /**
   * Sets the magnetic polarity.
   *
   * @param attract {@code true} = attract, {@code false} = repel
   */
  public void setAttract(boolean attract) { this.attract = attract; }

  /**
   * Returns the current polarity.
   *
   * @return {@code true} if attracting, {@code false} if repelling
   */
  public boolean isAttract() { return attract; }

  /** Returns the current tile width.
   *  @return tile width in pixels */
  public float getTileWidth()  { return tileWidth; }

  /** Returns the current tile height.
   *  @return tile height in pixels */
  public float getTileHeight() { return tileHeight; }

  // ── Preset convenience ────────────────────────────────────────

  /**
   * Applies a named intensity preset, adjusting strength, falloff,
   * smoothing, and radius to a coordinated combination.
   *
   * <p>Presets:
   * <table>
   *   <tr><th>Name</th><th>Constant</th><th>strength</th><th>falloff</th><th>smoothing</th><th>radius</th></tr>
   *   <tr><td>Gentle  </td><td>{@link #GENTLE}  </td><td>600 </td><td>120</td><td>0.06</td><td>20</td></tr>
   *   <tr><td>Moderate</td><td>{@link #MODERATE}</td><td>1800</td><td> 80</td><td>0.12</td><td>20</td></tr>
   *   <tr><td>Strong  </td><td>{@link #STRONG}  </td><td>3000</td><td> 50</td><td>0.20</td><td>30</td></tr>
   *   <tr><td>Snapping</td><td>{@link #SNAPPING}</td><td>4000</td><td> 30</td><td>0.40</td><td>40</td></tr>
   * </table></p>
   *
   * @param preset one of {@link #GENTLE}, {@link #MODERATE}, {@link #STRONG}, {@link #SNAPPING}
   * @return this instance for fluent chaining
   */
  public MagneticMotion setPreset(int preset) {
    switch (preset) {
      case GENTLE:
        this.strength  = 600.0f;
        this.falloff   = 120.0f;
        this.smoothing = 0.06f;
        this.radius    = 20.0f;
        break;
      case STRONG:
        this.strength  = 3000.0f;
        this.falloff   = 50.0f;
        this.smoothing = 0.20f;
        this.radius    = 30.0f;
        break;
      case SNAPPING:
        this.strength  = 4000.0f;
        this.falloff   = 30.0f;
        this.smoothing = 0.40f;
        this.radius    = 40.0f;
        break;
      default: // MODERATE
        this.strength  = 1800.0f;
        this.falloff   = 80.0f;
        this.smoothing = 0.12f;
        this.radius    = 20.0f;
        break;
    }
    return this;
  }

  /**
   * Sets the field strength using a normalised 0-1 designer value,
   * linearly mapped to the physical range 400–4000.
   *
   * <pre>
   * mag.setStrengthNormalized(0.0f);  // strength = 400  (very gentle)
   * mag.setStrengthNormalized(0.5f);  // strength = 2200 (balanced)
   * mag.setStrengthNormalized(1.0f);  // strength = 4000 (maximum)
   * </pre>
   *
   * @param t normalised strength in [0, 1]
   * @return this instance for fluent chaining
   */
  public MagneticMotion setStrengthNormalized(float t) {
    float clamped = PApplet.constrain(t, 0.0f, 1.0f);
    this.strength = 400.0f + clamped * 3600.0f;
    return this;
  }
}
