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
 * @version 1.0.0
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

  // ── Contour structure (for shape/line rendering) ─────────────
  private List<int[]> contourRanges;  // [startIdx, length] per contour
  
  // ── Offset for positioning ───────────────────────────────────
  private float offsetX = 0, offsetY = 0;

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
    List<PVector[]> contours = extractor.getContours(ch, fontSize);
    float[] b = extractor.getBounds(ch, fontSize);
    offsetX = parent.width  / 2f - b[0] - b[2] / 2f;
    offsetY = parent.height / 2f - b[1] - b[3] / 2f;
    loadContours(contours);
  }

  /**
   * Loads a string and centres it on screen.
   *
   * @param text     the text
   * @param fontSize font size in points
   */
  public void setText(String text, float fontSize) {
    List<PVector[]> contours = extractor.getContours(text, fontSize);
    float[] b = extractor.getBounds(text, fontSize);
    offsetX = parent.width  / 2f - b[0] - b[2] / 2f;
    offsetY = parent.height / 2f - b[1] - b[3] / 2f;
    loadContours(contours);
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
   */
  public void displayFilled() {
    if (pos == null || contourRanges == null) return;
    parent.noStroke();

    if (useHSB) parent.colorMode(PApplet.HSB, 360, 255, 255);

    for (int c = 0; c < contourRanges.size(); c++) {
      int start = contourRanges.get(c)[0];
      int len   = contourRanges.get(c)[1];

      if (useHSB) {
        float hue = PApplet.map(c, 0, contourRanges.size(), 0, 360);
        parent.fill(hue, 200, 255);
      } else {
        parent.fill(fillColor);
      }

      parent.beginShape();
      for (int i = start; i < start + len; i++) {
        parent.vertex(pos[i].x, pos[i].y);
      }
      parent.endShape(PApplet.CLOSE);
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
}
