/**
 * RippleMotion - Concentric displacement waves radiating from a click point.
 *
 * <p>When the user clicks (or when a wave is triggered programmatically via
 * {@link #trigger(float, float)}), a ring ripple expands outward from that
 * origin. Glyphs within the ring are displaced radially as it passes them and
 * fall back toward their cell centre as the ring moves on and decays.</p>
 *
 * <p>Multiple ripples can coexist — each call to {@link #trigger} spawns an
 * independent expanding ring. Old rings are automatically pruned once their
 * amplitude drops below a threshold.</p>
 *
 * <h2>Usage</h2>
 * <pre>
 * RippleMotion ripple = new RippleMotion();
 * ripple.setTileGrid(width, height, tilesX, tilesY);
 * config.setCellMotion(ripple);
 *
 * // in mousePressed():
 * ripple.trigger(mouseX, mouseY);
 * </pre>
 *
 * <h2>Feel guide</h2>
 * <pre>
 * // Fast, tight ripple
 * RippleMotion r = new RippleMotion();
 * r.setExpandSpeed(320);   r.setWaveWidth(60);   r.setDecayRate(0.96f);
 *
 * // Slow, wide swell
 * r.setExpandSpeed(120);   r.setWaveWidth(180);  r.setDecayRate(0.985f);
 * </pre>
 *
 * @author Michail Semoglou
 * @version 0.3.0
 * @since 0.2.3
 * @see CellMotion
 * @see FlowFieldMotion
 * @see OrbitalMotion
 */

package algorithmic.typography.core;

import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Click-triggered concentric displacement rings that expand and decay.
 */
public class RippleMotion extends CellMotion {
  // ── Named intensity presets ──────────────────────────────────

  /** Slow, wide swell — glyphs rock gently as the ring passes. */
  public static final int GENTLE   = 0;

  /** Balanced ripple — noticeable but not overwhelming (default). */
  public static final int MODERATE = 1;

  /** Fast, tight wave — sharp punchy displacement per ring. */
  public static final int STRONG   = 2;
  // ── Grid geometry ─────────────────────────────────────────────

  /** Width of one grid tile in pixels. */
  private float tileWidth  = 45.0f;

  /** Height of one grid tile in pixels. */
  private float tileHeight = 45.0f;

  // ── Ring parameters ───────────────────────────────────────────

  /**
   * How fast the ring front expands, in pixels per second equivalent.
   * Because {@code getOffset} is called every frame, this is scaled by
   * {@code 1/60f} internally so the parameter reads naturally in px/s.
   * Default: 200 px/s.
   */
  private float expandSpeed = 200.0f;

  /**
   * Width of the displacement band around the ring front.
   * Glyphs within this distance of the leading edge are displaced.
   * Default: 80 px.
   */
  private float waveWidth = 80.0f;

  /**
   * Per-frame amplitude decay multiplier. 1.0 = no decay; 0.95 = fast decay.
   * Default: 0.975.
   */
  private float decayRate = 0.975f;

  /** Displacement amplitude at full strength (pixels). Default: radius. */
  private float amplitude = -1; // -1 means "use radius"

  // ── Active ripples ────────────────────────────────────────────

  private final List<Ripple> ripples = new ArrayList<>();

  // ── Internal ring state ───────────────────────────────────────

  private static class Ripple {
    float ox, oy;       // origin in screen pixels
    float currentRadius;
    float amp;

    Ripple(float ox, float oy, float amp) {
      this.ox = ox;
      this.oy = oy;
      this.currentRadius = 0;
      this.amp = amp;
    }
  }

  // ── Constructors ─────────────────────────────────────────────

  /** Default constructor — trigger via {@link #trigger(float, float)}. */
  public RippleMotion() {}

  /**
   * Creates a RippleMotion with custom ring parameters.
   *
   * @param expandSpeed ring expansion speed (px/s equivalent)
   * @param waveWidth   width of the displacement band around the ring front
   * @param decayRate   per-frame amplitude multiplier (< 1 = decaying)
   */
  public RippleMotion(float expandSpeed, float waveWidth, float decayRate) {
    this.expandSpeed = expandSpeed;
    this.waveWidth   = waveWidth;
    this.decayRate   = decayRate;
  }

  // ── Trigger ───────────────────────────────────────────────────

  /**
   * Spawns a new ripple wave originating from the given screen position.
   * Call this from {@code mousePressed()} or anywhere an event should trigger a ring.
   *
   * @param screenX origin X in screen pixels
   * @param screenY origin Y in screen pixels
   */
  public void trigger(float screenX, float screenY) {
    float amp = (amplitude < 0) ? radius : amplitude;
    ripples.add(new Ripple(screenX, screenY, amp));
  }

  // ── Core ─────────────────────────────────────────────────────

  @Override
  public PVector getOffset(int col, int row, int frameCount) {
    // Advance all ripples
    float dx = 0, dy = 0;
    float expandStep = expandSpeed / 60.0f * speed;

    Iterator<Ripple> it = ripples.iterator();
    while (it.hasNext()) {
      Ripple r = it.next();

      // Convert cell index → world pixel centre
      float cx = (col + 0.5f) * tileWidth;
      float cy = (row + 0.5f) * tileHeight;

      float dist = PApplet.dist(cx, cy, r.ox, r.oy);
      float diff = dist - r.currentRadius;

      // Gaussian bell centred at the ring front
      if (Math.abs(diff) < waveWidth * 2) {
        float bell = (float) Math.exp(-(diff * diff) / (waveWidth * waveWidth));
        // Radial direction away from origin
        float vx = cx - r.ox;
        float vy = cy - r.oy;
        float len = (float) Math.sqrt(vx * vx + vy * vy);
        if (len > 0.001f) {
          vx /= len; vy /= len;
        }
        dx += vx * r.amp * bell;
        dy += vy * r.amp * bell;
      }

      // Only advance ring when col==0,row==0 to avoid multiple advances per frame.
      // We detect this by advancing in the first cell call.
    }

    // Advance rings once per frame on the first cell (0,0)
    if (col == 0 && row == 0) {
      Iterator<Ripple> adv = ripples.iterator();
      while (adv.hasNext()) {
        Ripple r = adv.next();
        r.currentRadius += expandStep;
        r.amp           *= decayRate;
        if (r.amp < 0.5f) adv.remove();
      }
    }

    // Clamp to radius
    PVector out = new PVector(dx, dy);
    if (out.mag() > radius) out.setMag(radius);
    return out;
  }

  // ── Grid helpers ─────────────────────────────────────────────

  /**
   * Convenience method: computes tile size from canvas and grid dimensions.
   *
   * @param canvasW canvas width  (pixels)
   * @param canvasH canvas height (pixels)
   * @param tilesX  number of columns
   * @param tilesY  number of rows
   */
  public void setTileGrid(float canvasW, float canvasH, int tilesX, int tilesY) {
    tileWidth  = canvasW / tilesX;
    tileHeight = canvasH / tilesY;
  }

  /**
   * Sets the tile size directly.
   *
   * @param tw tile width  in pixels
   * @param th tile height in pixels
   */
  public void setTileSize(float tw, float th) {
    tileWidth  = tw;
    tileHeight = th;
  }

  // ── Configuration ─────────────────────────────────────────────

  /**
   * Sets the ring expansion speed (pixels per second equivalent).
   * @param s expansion speed (default 200)
   */
  public void setExpandSpeed(float s) { this.expandSpeed = Math.max(1, s); }

  /**
   * Sets the width of the displacement band around the ring front.
   * @param w wave width in pixels (default 80)
   */
  public void setWaveWidth(float w) { this.waveWidth = Math.max(1, w); }

  /**
   * Sets the per-frame amplitude decay rate.
   * @param d decay [0, 1]; 1 = no decay, lower = faster (default 0.975)
   */
  public void setDecayRate(float d) { this.decayRate = Math.max(0, Math.min(1, d)); }

  /**
   * Overrides the displacement amplitude. By default the inherited {@code radius}
   * field is used.
   * @param amp amplitude in pixels
   */
  public void setAmplitude(float amp) { this.amplitude = amp; }

  /** Clears all active ripples. */
  public void clear() { ripples.clear(); }

  /** Returns the number of currently active ripple rings. */
  public int getRippleCount() { return ripples.size(); }

  /** Returns the ring expansion speed (px/s equivalent). @return expandSpeed */
  public float getExpandSpeed() { return expandSpeed; }

  /** Returns the width of the displacement band around the ring front. @return waveWidth */
  public float getWaveWidth()   { return waveWidth; }

  /** Returns the per-frame amplitude decay rate. @return decayRate */
  public float getDecayRate()   { return decayRate; }

  // ── Preset convenience ────────────────────────────────────────

  /**
   * Applies a named intensity preset, adjusting expandSpeed, waveWidth,
   * and decayRate to a coordinated combination.
   *
   * <p>Presets:
   * <table>
   *   <tr><th>Name</th>    <th>Constant</th>          <th>expandSpeed</th><th>waveWidth</th><th>decayRate</th></tr>
   *   <tr><td>Gentle  </td><td>{@link #GENTLE}  </td> <td>120 </td>       <td>180</td>      <td>0.985</td></tr>
   *   <tr><td>Moderate</td><td>{@link #MODERATE}</td> <td>200 </td>       <td> 80</td>      <td>0.975</td></tr>
   *   <tr><td>Strong  </td><td>{@link #STRONG}  </td> <td>320 </td>       <td> 60</td>      <td>0.960</td></tr>
   * </table></p>
   *
   * @param preset one of {@link #GENTLE}, {@link #MODERATE}, {@link #STRONG}
   * @return this instance for fluent chaining
   */
  public RippleMotion setPreset(int preset) {
    switch (preset) {
      case GENTLE:
        this.expandSpeed = 120.0f;
        this.waveWidth   = 180.0f;
        this.decayRate   = 0.985f;
        break;
      case STRONG:
        this.expandSpeed = 320.0f;
        this.waveWidth   = 60.0f;
        this.decayRate   = 0.960f;
        break;
      default: // MODERATE
        this.expandSpeed = 200.0f;
        this.waveWidth   = 80.0f;
        this.decayRate   = 0.975f;
        break;
    }
    return this;
  }

  /**
   * Sets the ring expansion speed using a normalised 0-1 designer value,
   * linearly mapped to the physical range 60–400 px/s.
   *
   * <pre>
   * ripple.setExpandSpeedNormalized(0.0f);  // expandSpeed = 60   (slow swell)
   * ripple.setExpandSpeedNormalized(0.5f);  // expandSpeed = 230  (balanced)
   * ripple.setExpandSpeedNormalized(1.0f);  // expandSpeed = 400  (fast burst)
   * </pre>
   *
   * @param t normalised speed in [0, 1]
   * @return this instance for fluent chaining
   */
  public RippleMotion setExpandSpeedNormalized(float t) {
    float clamped = PApplet.constrain(t, 0.0f, 1.0f);
    this.expandSpeed = 60.0f + clamped * 340.0f;
    return this;
  }
}
