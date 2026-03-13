/**
 * GravityMotion - Glyphs fall under gravity and bounce within their cells.
 *
 * <p>Each glyph behaves as a point mass inside a rectangular cell:</p>
 * <ul>
 *   <li>Gravity continuously accelerates it downward (+Y in Processing).</li>
 *   <li>It bounces off all four cell walls with a configurable
 *       {@code restitution} coefficient (energy retained per bounce).</li>
 *   <li>A gentle, sinusoidally driven lateral force keeps glyphs from
 *       settling into a purely vertical bounce.</li>
 *   <li>A small air-drag term prevents lateral velocity from growing
 *       without bound.</li>
 * </ul>
 *
 * <p>Each cell is initialised at a phase-staggered height so that
 * neighbouring glyphs are always at different stages of their fall —
 * the initial velocity is computed via energy conservation
 * ({@code v = √(2g·h)}) so the stagger is physically consistent from
 * the very first frame.  The result is a rippling, cascading fall
 * across the grid rather than every glyph dropping in lockstep.</p>
 *
 * <h2>Feel guide</h2>
 * <pre>
 * // Default — moderate gravity, satisfying bounce
 * GravityMotion gm = new GravityMotion();
 *
 * // Heavy, low restitution (falls fast, barely bounces)
 * GravityMotion heavy = new GravityMotion(12, 1.0f, 0.5f, 0.3f);
 *
 * // Floaty, super-bouncy
 * GravityMotion floaty = new GravityMotion(10, 0.4f, 0.95f, 0.1f);
 * </pre>
 *
 * @author Michail Semoglou
 * @version 0.2.5
 * @since 0.2.1
 * @see CellMotion
 * @see SpringMotion
 */

package algorithmic.typography.core;

import java.util.HashMap;
import processing.core.PApplet;
import processing.core.PVector;

/**
 * Gravity and bounce motion within a grid cell.
 */
public class GravityMotion extends CellMotion {

  // ── Physics parameters ────────────────────────────────────────

  /**
   * Downward acceleration in pixels/frame² (before speed scaling).
   * Higher = heavier/faster fall.  Typical range: 0.1 – 0.8.
   */
  private float gravity;

  /**
   * Fraction of speed retained after each wall bounce (0 = dead stop,
   * 1 = perfectly elastic).  Typical range: 0.5 – 0.95.
   */
  private float restitution;

  /**
   * Amplitude of the sinusoidal lateral force applied each frame.
   * Keeps bounces from being purely vertical.  Typical range: 0.02 – 0.2.
   */
  private float lateralStrength;

  /**
   * Air-drag multiplier applied to velX every frame.
   * Values close to 1 = low drag; lower values kill lateral motion quickly.
   */
  private float airDrag = 0.97f;

  /**
   * Per-cell phase spread used for both the initial height stagger
   * and the ongoing lateral force.  Higher = more variation between
   * neighbouring glyphs.
   */
  private float phaseSpread = 1.0f;

  // ── Per-cell physics state ────────────────────────────────────

  /**
   * Stores [posX, posY, velX, velY, lastFrame] for every (col, row) pair.
   * Keyed by {@code ((long) col << 32) | (row & 0xFFFFFFFFL)}.
   */
  private final HashMap<Long, float[]> cellState = new HashMap<>();

  // ── Constructors ──────────────────────────────────────────────

  /**
   * Full constructor.
   *
   * @param radius         max displacement / cell half-size in pixels
   * @param gravity        downward acceleration in px/frame² (pre-speed)
   * @param restitution    bounce energy retention (0 – 1)
   * @param lateralStrength sinusoidal lateral force amplitude
   */
  public GravityMotion(float radius, float gravity,
                       float restitution, float lateralStrength) {
    this.radius          = radius;
    this.gravity         = gravity;
    this.restitution     = restitution;
    this.lateralStrength = lateralStrength;
  }

  /**
   * Creates a GravityMotion with default restitution (0.72) and
   * lateral strength (0.06).
   *
   * @param radius  cell half-size in pixels
   * @param gravity downward acceleration in px/frame² (pre-speed)
   */
  public GravityMotion(float radius, float gravity) {
    this(radius, gravity, 0.72f, 0.06f);
  }

  /**
   * Default: 10 px radius, gravity 0.18, restitution 0.72,
   * lateral strength 0.06.
   */
  public GravityMotion() {
    this(10, 0.18f);
  }

  // ── Core ──────────────────────────────────────────────────────

  /**
   * {@inheritDoc}
   *
   * <p>Advances the gravity/bounce physics for the cell identified by
   * ({@code col}, {@code row}) by one integration step and returns the
   * current displacement from the cell centre.</p>
   */
  @Override
  public PVector getOffset(int col, int row, int frameCount) {
    long key = ((long) col << 32) | (row & 0xFFFFFFFFL);

    // Retrieve or initialise per-cell state: [posX, posY, velX, velY, lastFrame]
    final int c = col, r = row;
    float[] s = cellState.computeIfAbsent(key, k -> initState(c, r));

    // Guard against double-integration within the same frame
    if ((int) s[4] == frameCount) {
      return new PVector(s[0], s[1]);
    }
    s[4] = frameCount;

    float dt    = speed;                    // speed scales the time step
    float phase = (col * 0.7f + row * 1.3f) * phaseSpread;

    // ── Lateral sinusoidal force ──────────────────────────────────
    float lateralForce = PApplet.sin(frameCount * 0.04f * speed + phase) * lateralStrength;
    s[2] += lateralForce;
    s[2] *= airDrag;                        // air drag on velX

    // ── Gravity ───────────────────────────────────────────────────
    s[3] += gravity * dt;

    // ── Integrate position ────────────────────────────────────────
    s[0] += s[2] * dt;
    s[1] += s[3] * dt;

    // ── Wall bounce — X ───────────────────────────────────────────
    if (s[0] > radius) {
      s[0] = radius;
      s[2] = -Math.abs(s[2]) * restitution;
    } else if (s[0] < -radius) {
      s[0] = -radius;
      s[2] =  Math.abs(s[2]) * restitution;
    }

    // ── Wall bounce — Y ───────────────────────────────────────────
    if (s[1] > radius) {
      // Bottom wall — main bounce
      s[1] = radius;
      s[3] = -Math.abs(s[3]) * restitution;
      // Damp lateral velocity on landing to simulate surface friction
      s[2] *= 0.85f;
    } else if (s[1] < -radius) {
      // Top wall (can only reach here at very high upward velocity)
      s[1] = -radius;
      s[3] =  Math.abs(s[3]) * restitution;
    }

    return new PVector(s[0], s[1]);
  }

  /**
   * Initialises the physics state for a new cell.
   * Starting Y position is staggered using a per-cell phase offset;
   * the matching downward velocity is derived via energy conservation
   * so the grid appears staggered from the very first frame.
   */
  private float[] initState(int col, int row) {
    float phase = (col * 0.7f + row * 1.3f) * phaseSpread;
    double frac = ((phase % (2 * Math.PI)) + 2 * Math.PI) % (2 * Math.PI)
                  / (2 * Math.PI);          // 0.0 – 1.0

    // Spread initial Y from top (-radius) to bottom (+radius)
    float initY   = (float)(-radius + frac * 2.0 * radius);

    // Velocity consistent with free-fall from the top: v = √(2·g·h)
    float h       = initY + radius;         // distance fallen from top
    float initVelY = (float) Math.sqrt(2.0 * gravity * Math.max(0, h));

    // Slight lateral stagger so cells don't start on the same axis
    float initVelX = (float) Math.sin(phase * 2.7) * lateralStrength * 2.0f;

    // [posX, posY, velX, velY, lastFrame]
    return new float[]{ 0, initY, initVelX, initVelY, -1 };
  }

  /** Resets all per-cell physics state (positions and velocities to zero). */
  public void reset() {
    cellState.clear();
  }

  /**
   * Applies an upward velocity impulse to every glyph that has been
   * initialised, making them "jump" from wherever they currently are.
   * A small lateral impulse proportional to the glyph's current
   * horizontal position is also added for natural-looking variety.
   *
   * <p>If no cells have been rendered yet this is a no-op — glyphs
   * will be initialised staggered from the top on the first frame.</p>
   *
   * @param impulseY upward speed in pixels/frame (positive = jump higher)
   */
  public void kick(float impulseY) {
    for (float[] s : cellState.values()) {
      s[3] -= impulseY;   // negative Y = upward in Processing
      // Lateral variety: push left/right based on current X position
      s[2] += (s[0] > 0 ? 1.0f : -1.0f) * lateralStrength * 4.0f;
    }
  }

  // ── Configuration ─────────────────────────────────────────────

  /**
   * Sets the gravitational acceleration (pixels/frame², before speed scaling).
   *
   * @param g gravity (typical range 0.1 – 0.8)
   */
  public void setGravity(float g) { this.gravity = Math.max(0.001f, g); }

  /** Returns the gravitational acceleration.
   *  @return gravity */
  public float getGravity() { return gravity; }

  /**
   * Sets the bounce restitution coefficient.
   * 0 = no bounce (dead stop), 1 = perfectly elastic.
   *
   * @param r restitution (0.0 – 1.0)
   */
  public void setRestitution(float r) {
    this.restitution = Math.max(0, Math.min(1, r));
  }

  /** Returns the restitution coefficient.
   *  @return restitution */
  public float getRestitution() { return restitution; }

  /**
   * Sets the sinusoidal lateral force amplitude.
   * Higher values cause wider horizontal wandering between bounces.
   *
   * @param ls lateral strength (typical range 0.02 – 0.2)
   */
  public void setLateralStrength(float ls) {
    this.lateralStrength = Math.max(0, ls);
  }

  /** Returns the lateral force amplitude.
   *  @return lateralStrength */
  public float getLateralStrength() { return lateralStrength; }

  /**
   * Sets the air-drag multiplier applied to velX each frame.
   * Values close to 1.0 = low drag; lower values damp lateral motion quickly.
   *
   * @param drag air drag multiplier (0.0 – 1.0, default 0.97)
   */
  public void setAirDrag(float drag) {
    this.airDrag = Math.max(0, Math.min(1, drag));
  }

  /** Returns the air-drag multiplier.
   *  @return airDrag */
  public float getAirDrag() { return airDrag; }

  /**
   * Sets the inter-cell phase spread used for height staggering
   * and lateral oscillation.  Higher = more variation between neighbours.
   *
   * @param spread phase spread (default 1.0)
   */
  public void setPhaseSpread(float spread) { this.phaseSpread = spread; }

  /** Returns the inter-cell phase spread.
   *  @return phaseSpread */
  public float getPhaseSpread() { return phaseSpread; }
}
