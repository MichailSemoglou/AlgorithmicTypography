/**
 * PerlinVertexMotion — Per-vertex Perlin noise displacement applied directly
 * to extracted glyph outline vertices.
 *
 * <p>Unlike {@link CellMotion}, which translates the entire glyph as a rigid
 * body, {@code PerlinVertexMotion} displaces each outline vertex independently
 * using spatially coherent 3-D Perlin noise.  The result is an organic,
 * hand-drawn distortion that follows the letterform's own silhouette — tight
 * curves flutter more visibly than long straight stems, and the deformation
 * breathes with time.</p>
 *
 * <h2>Usage</h2>
 * <pre>
 * GlyphExtractor ge = new GlyphExtractor(this, "Helvetica", 72);
 * PVector[] outline = ge.getOuterContour('A', 200);
 *
 * PerlinVertexMotion pvm = new PerlinVertexMotion(this);
 * pvm.setAmplitude(6.0f);
 * pvm.setSpatialScale(0.03f);
 * pvm.setTimeSpeed(0.015f);
 *
 * // In draw():
 * PVector[] deformed = pvm.deform(outline, frameCount);
 * beginShape();
 * for (PVector v : deformed) vertex(v.x, v.y);
 * endShape(CLOSE);
 * </pre>
 *
 * <h2>Typical parameter ranges</h2>
 * <pre>
 * // Subtle   — barely noticeable jitter
 * pvm.setAmplitude(2.0f);   pvm.setSpatialScale(0.05f);  pvm.setTimeSpeed(0.008f);
 *
 * // Organic  — fluid, calligraphic distortion (default)
 * pvm.setAmplitude(6.0f);   pvm.setSpatialScale(0.03f);  pvm.setTimeSpeed(0.015f);
 *
 * // Chaotic  — aggressive, glitch-like deformation
 * pvm.setAmplitude(14.0f);  pvm.setSpatialScale(0.012f); pvm.setTimeSpeed(0.04f);
 * </pre>
 *
 * @author Michail Semoglou
 * @version 0.2.6
 * @since 0.2.6
 * @see algorithmic.typography.render.GlyphExtractor
 */

package algorithmic.typography.core;

import processing.core.PApplet;
import processing.core.PVector;

/**
 * Per-vertex Perlin noise deformation for extracted glyph outline vertices.
 */
public class PerlinVertexMotion {

  // ── Reference ─────────────────────────────────────────────────

  private final PApplet sketch;

  // ── Parameters ────────────────────────────────────────────────

  /**
   * Maximum pixel displacement applied to each vertex.
   * This is the peak-to-peak amplitude of the Perlin field projected onto
   * the vertex normal.  Higher values = more dramatic distortion.
   * Default: 6.0 px.
   */
  private float amplitude = 6.0f;

  /**
   * Spatial frequency of the noise field.
   * Smaller values produce large, smooth waves; larger values produce
   * fine-grain turbulence that follows every bump and corner of the letterform.
   * Default: 0.03.
   */
  private float spatialScale = 0.03f;

  /**
   * Rate at which the noise field evolves over time (per frame).
   * Increase for faster, more agitated motion; reduce for slow, meditative drift.
   * Default: 0.015.
   */
  private float timeSpeed = 0.015f;

  /**
   * Noise field seed offset; shifts the noise pattern so that multiple
   * {@code PerlinVertexMotion} instances applied to different glyphs do not
   * produce identical deformations.
   * Default: 13.7 (arbitrary, aesthetically varied).
   */
  private float seed = 13.7f;

  // ── Constructor ───────────────────────────────────────────────

  /**
   * Creates a {@code PerlinVertexMotion} with default parameters.
   *
   * @param sketch the host Processing sketch (required for {@code noise()})
   */
  public PerlinVertexMotion(PApplet sketch) {
    if (sketch == null) throw new IllegalArgumentException(
        "PerlinVertexMotion requires a non-null PApplet reference");
    this.sketch = sketch;
  }

  /**
   * Creates a {@code PerlinVertexMotion} with fully specified parameters.
   *
   * @param sketch       the host Processing sketch
   * @param amplitude    maximum vertex displacement in pixels
   * @param spatialScale spatial frequency of the noise field
   * @param timeSpeed    noise evolution rate per frame
   */
  public PerlinVertexMotion(PApplet sketch, float amplitude,
                             float spatialScale, float timeSpeed) {
    this(sketch);
    this.amplitude    = Math.max(0, amplitude);
    this.spatialScale = Math.max(0.0001f, spatialScale);
    this.timeSpeed    = timeSpeed;
  }

  // ── Core ──────────────────────────────────────────────────────

  /**
   * Applies Perlin noise displacement to each vertex in {@code vertices}
   * and returns the deformed array.
   *
   * <p>The original array is <em>not</em> modified — a new array is
   * allocated on each call.  This makes {@code deform()} safe to call
   * every frame with the same base outline.</p>
   *
   * <p>Each vertex is displaced by a vector derived from the noise field
   * sampled at {@code (x × spatialScale, y × spatialScale, t)} where
   * {@code x} and {@code y} are the vertex's world-space coordinates and
   * {@code t = frameCount × timeSpeed + seed}.  Two independent noise
   * samples (offset by a large constant on the Z axis) provide independent
   * X and Y components of the displacement.</p>
   *
   * @param vertices   the base outline vertices (not modified)
   * @param frameCount current Processing frameCount
   * @return a new array of Perlin-displaced vertices
   */
  public PVector[] deform(PVector[] vertices, int frameCount) {
    if (vertices == null || vertices.length == 0) return new PVector[0];

    PVector[] result = new PVector[vertices.length];
    float t = frameCount * timeSpeed + seed;

    for (int i = 0; i < vertices.length; i++) {
      float nx = vertices[i].x * spatialScale;
      float ny = vertices[i].y * spatialScale;

      // Two independent noise samples for X and Y displacement
      float noiseX = sketch.noise(nx, ny, t) - 0.5f;          // [-0.5, 0.5]
      float noiseY = sketch.noise(nx + 31.41f, ny + 27.18f, t) - 0.5f;

      result[i] = new PVector(
          vertices[i].x + noiseX * amplitude * 2f,
          vertices[i].y + noiseY * amplitude * 2f
      );
    }
    return result;
  }

  /**
   * Deforms a 2-D contour list (e.g., from {@code GlyphExtractor.getContours()})
   * in one call, applying the same noise field to every contour independently.
   *
   * @param contours   list of per-contour vertex arrays (not modified)
   * @param frameCount current Processing frameCount
   * @return a new list of deformed vertex arrays
   */
  public PVector[][] deformContours(PVector[][] contours, int frameCount) {
    if (contours == null) return new PVector[0][];
    PVector[][] result = new PVector[contours.length][];
    for (int i = 0; i < contours.length; i++) {
      result[i] = deform(contours[i], frameCount);
    }
    return result;
  }

  // ── Getters / setters ─────────────────────────────────────────

  /**
   * Sets the maximum vertex displacement amplitude in pixels.
   *
   * @param amplitude peak displacement (must be ≥ 0)
   * @return this for chaining
   */
  public PerlinVertexMotion setAmplitude(float amplitude) {
    this.amplitude = Math.max(0, amplitude);
    return this;
  }

  /** Returns the current amplitude. @return amplitude in pixels */
  public float getAmplitude() { return amplitude; }

  /**
   * Sets the spatial frequency of the noise field.
   * Smaller = smoother, larger = finer turbulence.
   *
   * @param spatialScale spatial scale (must be &gt; 0)
   * @return this for chaining
   */
  public PerlinVertexMotion setSpatialScale(float spatialScale) {
    this.spatialScale = Math.max(0.0001f, spatialScale);
    return this;
  }

  /** Returns the spatial scale. @return spatialScale */
  public float getSpatialScale() { return spatialScale; }

  /**
   * Sets how fast the noise field evolves each frame.
   * Values around 0.01 produce slow drift; values around 0.05 become
   * visibly chaotic.
   *
   * @param timeSpeed evolution rate per frame
   * @return this for chaining
   */
  public PerlinVertexMotion setTimeSpeed(float timeSpeed) {
    this.timeSpeed = timeSpeed;
    return this;
  }

  /** Returns the time evolution speed. @return timeSpeed */
  public float getTimeSpeed() { return timeSpeed; }

  /**
   * Sets the noise field seed offset.
   * Use different floats for each {@code PerlinVertexMotion} instance to avoid
   * identical deformation on different glyphs rendered simultaneously.
   *
   * @param seed arbitrary float seed value
   * @return this for chaining
   */
  public PerlinVertexMotion setSeed(float seed) {
    this.seed = seed;
    return this;
  }

  /** Returns the current seed. @return seed */
  public float getSeed() { return seed; }
}
