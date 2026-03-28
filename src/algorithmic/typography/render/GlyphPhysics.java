/**
 * GlyphPhysics - Particle-based physics for glyph vertices.
 *
 * <p>Treats each vertex of a glyph outline as a particle with position,
 * velocity, and acceleration.  Supports attraction/repulsion interactions
 * with the mouse or arbitrary attractor points, spring-back forces to
 * the original letterform, and simple damping.</p>
 *
 * <h2>Features</h2>
 * <ul>
 *   <li>Convert glyph contour points into live particles</li>
 *   <li>Mouse attraction / repulsion with configurable radius and strength</li>
 *   <li>Multiple named attractors for programmatic control</li>
 *   <li>Spring force pulls particles back to their home position</li>
 *   <li>Render as points, lines, or filled PShape</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre>
 * GlyphPhysics physics = new GlyphPhysics(this, glyphExtractor);
 * physics.setChar('A', 400);
 * physics.setMouseAttraction(-2.0);   // repel from cursor
 *
 * void draw() {
 *   physics.update();
 *   physics.display();
 * }
 * </pre>
 *
 * @author Michail Semoglou
 * @version 0.3.0
 * @since 1.0.0
 */

package algorithmic.typography.render;

import processing.core.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlyphPhysics {

  private final PApplet parent;
  private final GlyphExtractor extractor;

  // ── Particle arrays ──────────────────────────────────────────
  private PVector[] home;       // original glyph positions
  private PVector[] pos;        // current positions
  private PVector[] vel;        // velocities
  private PVector[] acc;        // per-frame acceleration accumulator
  private int count;

  // ── Forces ───────────────────────────────────────────────────
  private float springK        = 0.05f;   // spring stiffness
  private float damping        = 0.85f;   // velocity damping (0-1)
  private float mouseStrength  = 0.0f;    // +attract / -repel
  private float mouseRadius    = 150.0f;  // interaction radius
  private float maxSpeed       = 12.0f;   // velocity clamp

  // Named attractors: name → {x, y, strength, radius}
  private final Map<String, float[]> attractors = new HashMap<>();

  // ── Display ──────────────────────────────────────────────────
  /** Point size or stroke weight for rendering. */
  private float pointSize = 3.0f;
  private int   fillColor;
  private boolean useHSB = false;

  // ── Counter-form colour (for Filled mode) ────────────────────
  private boolean[] contourIsInner;   // true = hole / counter-form
  private int       counterColor = 0; // default: black

  // ── Contour structure (for shape/line rendering) ─────────────
  private List<int[]> contourRanges;  // [startIdx, length] per contour

  // ── Offset for positioning ───────────────────────────────────
  private float offsetX = 0, offsetY = 0;

  /** Total dot count distributed by arc length across all contours. */
  private static final int DEFAULT_DISTRIBUTE_PTS = 600;

  // ───────────────────────────────────────────────────────────────
  //  Construction
  // ───────────────────────────────────────────────────────────────

  /**
   * Creates a GlyphPhysics system.
   *
   * @param parent   the parent PApplet
   * @param extractor a GlyphExtractor (font already set)
   */
  public GlyphPhysics(PApplet parent, GlyphExtractor extractor) {
    this.parent = parent;
    this.extractor = extractor;
    this.fillColor = parent.color(255);
  }

  // ───────────────────────────────────────────────────────────────
  //  Loading glyphs
  // ───────────────────────────────────────────────────────────────

  /**
   * Loads a single character and centres it on screen.
   *
   * @param ch       the character
   * @param fontSize font size in points
   */
  public void setChar(char ch, float fontSize) {
    List<PVector[]> raw = extractor.getContours(ch, fontSize);
    float[] b = extractor.getBounds(ch, fontSize);
    offsetX = parent.width  / 2f - b[0] - b[2] / 2f;
    offsetY = parent.height / 2f - b[1] - b[3] / 2f;
    // Classify inner/outer on raw font geometry (same strategy as
    // GlyphExtractor.getInnerContours: all-but-largest-area = inner).
    contourIsInner = classifyContours(raw);
    loadContours(redistributeContours(raw, DEFAULT_DISTRIBUTE_PTS));
  }

  /**
   * Loads a string and centres it on screen.
   *
   * @param text     the text
   * @param fontSize font size in points
   */
  public void setText(String text, float fontSize) {
    List<PVector[]> raw = extractor.getContours(text, fontSize);
    float[] b = extractor.getBounds(text, fontSize);
    offsetX = parent.width  / 2f - b[0] - b[2] / 2f;
    offsetY = parent.height / 2f - b[1] - b[3] / 2f;
    contourIsInner = classifyContours(raw);
    loadContours(redistributeContours(raw, DEFAULT_DISTRIBUTE_PTS));
  }

  /**
   * Distributes {@code totalPts} across contours proportionally by arc length,
   * resampling each to its share so particles are evenly spaced along the outline.
   */
  private List<PVector[]> redistributeContours(List<PVector[]> contours, int totalPts) {
    if (contours.isEmpty()) return contours;
    float[] lengths = new float[contours.size()];
    float totalLen  = 0;
    for (int i = 0; i < contours.size(); i++) {
      PVector[] c = contours.get(i);
      float len = 0;
      for (int j = 1; j < c.length; j++) len += PVector.dist(c[j - 1], c[j]);
      if (c.length > 1) len += PVector.dist(c[c.length - 1], c[0]); // closing edge
      lengths[i] = len;
      totalLen  += len;
    }
    List<PVector[]> result = new ArrayList<>();
    for (int i = 0; i < contours.size(); i++) {
      // Close the contour explicitly so the last→first edge is covered.
      PVector[] raw    = contours.get(i);
      PVector[] closed = java.util.Arrays.copyOf(raw, raw.length + 1);
      closed[raw.length] = raw[0].copy();
      int n = Math.max(3, Math.round(totalPts * lengths[i] / Math.max(totalLen, 1f)));
      result.add(extractor.resample(closed, n));
    }
    return result;
  }

  /**
   * Loads raw contours (advanced use).
   */
  private void loadContours(List<PVector[]> contours) {
    // Count total points
    count = 0;
    for (PVector[] c : contours) count += c.length;

    home = new PVector[count];
    pos  = new PVector[count];
    vel  = new PVector[count];
    acc  = new PVector[count];
    contourRanges = new ArrayList<>();
    // contourIsInner is already set from raw font geometry in setChar()/setText()
    // before this method is called.

    int idx = 0;
    for (PVector[] c : contours) {
      contourRanges.add(new int[]{idx, c.length});
      for (PVector v : c) {
        home[idx] = new PVector(v.x + offsetX, v.y + offsetY);
        pos[idx]  = home[idx].copy();
        vel[idx]  = new PVector();
        acc[idx]  = new PVector();
        idx++;
      }
    }
  }

  /**
   * Classifies raw (un-resampled) font contours as outer boundary or
   * counter-form (hole) using winding direction — the same convention that
   * {@code GlyphExtractor.shapeToProcessing()} relies on after AWT {@code Area}
   * normalisation:
   * <ul>
   *   <li>CCW traversal (negative signed area in y-down screen coords) = outer</li>
   *   <li>CW traversal (positive signed area) = counter-form / hole</li>
   * </ul>
   * This correctly handles glyphs with multiple separate outer forms such as
   * '%' (slash + two rings), '&', '@', etc. — where a simple largest-area
   * rule would mis-classify the smaller outer forms as holes.
   */
  private boolean[] classifyContours(List<PVector[]> contours) {
    boolean[] result = new boolean[contours.size()];
    for (int ci = 0; ci < contours.size(); ci++) {
      result[ci] = signedArea(contours.get(ci)) > 0; // CW = hole/counter-form
    }
    return result;
  }

  // ───────────────────────────────────────────────────────────────
  //  Configuration
  // ───────────────────────────────────────────────────────────────

  /** Spring stiffness (0 = no return, 1 = instant snap). Default 0.05. */
  public void setSpring(float k) { this.springK = k; }

  /** Velocity damping (0 = instant stop, 1 = no friction). Default 0.85. */
  public void setDamping(float d) { this.damping = d; }

  /**
   * Mouse interaction strength.
   * Positive = attract toward cursor, negative = repel.
   * Zero = disabled.  Default 0.
   */
  public void setMouseAttraction(float strength) { this.mouseStrength = strength; }

  /** Interaction radius for mouse force. Default 150. */
  public void setMouseRadius(float r) { this.mouseRadius = Math.max(1, r); }

  /** Maximum particle speed. Default 12. */
  public void setMaxSpeed(float s) { this.maxSpeed = s; }

  /** Point / stroke rendering size. Default 3. */
  public void setPointSize(float s) { this.pointSize = s; }

  /** Set a flat fill colour (RGB). */
  public void setColor(int c) { this.fillColor = c; this.useHSB = false; }

  /** Enable rainbow HSB colouring based on particle index. */
  public void setRainbow(boolean on) { this.useHSB = on; }

  /**
   * Set the fill colour for counter-form contours in Filled mode (non-rainbow).
   * Defaults to black ({@code color(0)}), which renders counters as transparent
   * holes when drawn over a black background.
   *
   * @param c the colour (use Processing's {@code color(r, g, b)})
   */
  public void setCounterColor(int c) { this.counterColor = c; }

  // ───────────────────────────────────────────────────────────────
  //  Attractors
  // ───────────────────────────────────────────────────────────────

  /**
   * Adds or updates a named attractor point.
   *
   * @param name     unique name for the attractor
   * @param x        x position
   * @param y        y position
   * @param strength positive = attract, negative = repel
   * @param radius   influence radius
   */
  public void setAttractor(String name, float x, float y, float strength, float radius) {
    attractors.put(name, new float[]{x, y, strength, radius});
  }

  /**
   * Removes a named attractor.
   *
   * @param name the attractor to remove
   */
  public void removeAttractor(String name) { attractors.remove(name); }

  /** Removes all attractors. */
  public void clearAttractors() { attractors.clear(); }

  // ───────────────────────────────────────────────────────────────
  //  Simulation
  // ───────────────────────────────────────────────────────────────

  /**
   * Advances the physics simulation by one frame.
   * Call once per {@code draw()}.
   */
  public void update() {
    if (pos == null) return;

    float mx = parent.mouseX;
    float my = parent.mouseY;

    for (int i = 0; i < count; i++) {
      acc[i].set(0, 0);

      // Spring back to home
      PVector spring = PVector.sub(home[i], pos[i]);
      spring.mult(springK);
      acc[i].add(spring);

      // Mouse force
      if (mouseStrength != 0) {
        applyAttractorForce(i, mx, my, mouseStrength, mouseRadius);
      }

      // Named attractors
      for (float[] a : attractors.values()) {
        applyAttractorForce(i, a[0], a[1], a[2], a[3]);
      }

      // Integrate
      vel[i].add(acc[i]);
      vel[i].mult(damping);
      vel[i].limit(maxSpeed);
      pos[i].add(vel[i]);
    }
  }

  private void applyAttractorForce(int i, float ax, float ay, float strength, float radius) {
    float dx = ax - pos[i].x;
    float dy = ay - pos[i].y;
    float distSq = dx * dx + dy * dy;
    float rSq = radius * radius;

    if (distSq < rSq && distSq > 1) {
      float dist = PApplet.sqrt(distSq);
      float force = strength * (1.0f - dist / radius);
      acc[i].x += (dx / dist) * force;
      acc[i].y += (dy / dist) * force;
    }
  }

  /**
   * Resets all particles to their home (glyph) positions.
   */
  public void reset() {
    if (pos == null) return;
    for (int i = 0; i < count; i++) {
      pos[i].set(home[i]);
      vel[i].set(0, 0);
    }
  }

  // ───────────────────────────────────────────────────────────────
  //  Rendering
  // ───────────────────────────────────────────────────────────────

  /**
   * Draws particles as points.
   */
  public void displayPoints() {
    if (pos == null) return;
    parent.noStroke();

    if (useHSB) {
      parent.colorMode(PApplet.HSB, 360, 255, 255);
      for (int i = 0; i < count; i++) {
        float hue = PApplet.map(i, 0, count, 0, 360);
        parent.fill(hue, 220, 255);
        parent.ellipse(pos[i].x, pos[i].y, pointSize, pointSize);
      }
      parent.colorMode(PApplet.RGB, 255);
    } else {
      parent.fill(fillColor);
      for (int i = 0; i < count; i++) {
        parent.ellipse(pos[i].x, pos[i].y, pointSize, pointSize);
      }
    }
  }

  /**
   * Draws particles connected by per-contour lines.
   */
  public void displayLines() {
    if (pos == null || contourRanges == null) return;
    parent.noFill();
    parent.strokeWeight(pointSize * 0.5f);

    if (useHSB) parent.colorMode(PApplet.HSB, 360, 255, 255);

    for (int c = 0; c < contourRanges.size(); c++) {
      int start = contourRanges.get(c)[0];
      int len   = contourRanges.get(c)[1];

      if (useHSB) {
        float hue = PApplet.map(c, 0, contourRanges.size(), 0, 360);
        parent.stroke(hue, 220, 255);
      } else {
        parent.stroke(fillColor);
      }

      parent.beginShape();
      for (int i = start; i < start + len; i++) {
        parent.vertex(pos[i].x, pos[i].y);
      }
      parent.endShape(PApplet.CLOSE);
    }

    if (useHSB) parent.colorMode(PApplet.RGB, 255);
    parent.noStroke();
  }

  /**
   * Draws particles as filled contour shapes.
   *
   * <p>Outer letterform contours and counter-form (hole) contours are coloured
   * independently:</p>
   * <ul>
   *   <li><b>Rainbow mode</b> — outer contours draw from the warm end of the
   *       spectrum (hues 0°–180°); counter-forms draw from the cool end
   *       (180°–360°), so stems and enclosed spaces are always visually
   *       distinct.</li>
   *   <li><b>Flat mode</b> — outer contours use {@link #setColor(int) fillColor};
   *       counter-forms use {@link #setCounterColor(int) counterColor} (default
   *       black, which reads as a transparent hole over a black background).</li>
   * </ul>
   */
  public void displayFilled() {
    if (pos == null || contourRanges == null) return;
    parent.noStroke();

    // Pre-count outer and inner contours for proportional rainbow hue spread.
    int outerCount = 0, innerCount = 0;
    if (contourIsInner != null) {
      for (boolean b : contourIsInner) { if (b) innerCount++; else outerCount++; }
    } else {
      outerCount = contourRanges.size();
    }

    if (useHSB) parent.colorMode(PApplet.HSB, 360, 255, 255);

    // Two-pass rendering: outers first, then inners painted on top.
    // This guarantees counter-form colours are always visible regardless of
    // the order the font's path iterator emits sub-paths.
    for (int pass = 0; pass < 2; pass++) {
      boolean paintInners = (pass == 1);
      int passIdx = 0; // colour index within this pass

      for (int c = 0; c < contourRanges.size(); c++) {
        boolean inner = contourIsInner != null && contourIsInner[c];
        if (inner != paintInners) continue; // handle in the other pass

        int start = contourRanges.get(c)[0];
        int len   = contourRanges.get(c)[1];

        if (useHSB) {
          if (inner) {
            // Cool half of the spectrum: cyan → blue → magenta
            float hue = innerCount > 1
                ? PApplet.map(passIdx, 0, innerCount, 180, 360)
                : 210f;
            parent.fill(hue % 360, 200, 220);
          } else {
            // Warm half: red → orange → yellow → green → cyan
            float hue = outerCount > 1
                ? PApplet.map(passIdx, 0, outerCount, 0, 180)
                : 30f;
            parent.fill(hue, 220, 255);
          }
        } else {
          parent.fill(inner ? counterColor : fillColor);
        }

        parent.beginShape();
        for (int i = start; i < start + len; i++) {
          parent.vertex(pos[i].x, pos[i].y);
        }
        parent.endShape(PApplet.CLOSE);
        passIdx++;
      }
    }

    if (useHSB) parent.colorMode(PApplet.RGB, 255);
  }

  /**
   * Convenience: calls {@link #displayPoints()}.
   */
  public void display() { displayPoints(); }

  // ───────────────────────────────────────────────────────────────
  //  Accessors
  // ───────────────────────────────────────────────────────────────

  /** Number of particles. */
  public int getCount() { return count; }

  /** Current particle positions (live reference). */
  public PVector[] getPositions() { return pos; }

  /** Home positions (live reference). */
  public PVector[] getHomePositions() { return home; }

  /** Current velocities (live reference). */
  public PVector[] getVelocities() { return vel; }

  // ───────────────────────────────────────────────────────────────
  //  Internal helpers
  // ───────────────────────────────────────────────────────────────

  /**
   * Shoelace signed area in y-down screen coordinates.
   * Positive = CW traversal = counter-form / hole.
   * Negative or zero = CCW traversal = outer boundary.
   */
  private float signedArea(PVector[] pts) {
    double sum = 0;
    int n = pts.length;
    for (int i = 0; i < n; i++) {
      PVector a = pts[i], b = pts[(i + 1) % n];
      sum += (double) a.x * b.y - (double) b.x * a.y;
    }
    return (float) (sum * 0.5);
  }
}
