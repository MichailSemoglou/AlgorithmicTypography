/**
 * GlyphCurvatureField — projects letterform outline curvature onto a 2-D scalar field.
 *
 * @author Michail Semoglou
 * @version 0.3.0
 * @since 0.3.0
 */

package algorithmic.typography.render;

import processing.core.*;
import algorithmic.typography.core.AmplitudeField;
import java.util.ArrayList;
import java.util.List;

/**
 * GlyphCurvatureField samples the local curvature at every vertex of a glyph's
 * flattened outline and exposes those values as a continuous 2-D scalar field via
 * Gaussian kernel summation.
 *
 * <p>Every point on a glyph's outline has a measurable local curvature: tight near
 * serif terminals, apexes, and bowl junctions; near-zero along straight stems.  This
 * class stores those values and lets a {@link algorithmic.typography.core.WaveEngine}
 * consult them per-cell so that cells near geometrically tense letterform regions
 * animate with higher amplitude, while cells over calm straight strokes stay quiet.
 * The result is an animation that is a geometric portrait of the typeface — the wave
 * emerges from the type's own structure rather than being imposed on top of it.</p>
 *
 * <h2>Usage</h2>
 * <pre>
 * GlyphExtractor ge = new GlyphExtractor(this, "Garamond", 72);
 * GlyphCurvatureField field = GlyphCurvatureField.from(ge, 'O', 600);
 * field.setIntensity(0.8f);
 * at.getWaveEngine().setAmplitudeField(field);
 * // Or via the convenience shorthand:
 * at.setCurvatureField(field);
 * </pre>
 *
 * <p>Characters without strong curvature variation (I, H, L) produce a nearly flat
 * field; characters rich in curved strokes and counter-form openings (O, B, G, &amp;)
 * produce the most expressive modulation.</p>
 *
 * @author Michail Semoglou
 * @version 0.3.0
 * @since 0.3.0
 * @see algorithmic.typography.core.WaveEngine#setAmplitudeField(GlyphCurvatureField)
 */
public class GlyphCurvatureField implements AmplitudeField {

  // Normalised sample positions in [0,1] × [0,1] relative to glyph bounding box
  private float[] nx;
  private float[] ny;
  // Curvature magnitudes normalised to [0,1]
  private float[] cv;

  /**
   * Blend intensity: 0 = field has no effect on amplitude (default amplitude used),
   * 1 = field completely replaces the per-cell amplitude computation. Default: 1.0.
   */
  private float intensity = 1.0f;

  /**
   * Gaussian kernel sigma in normalised [0,1] units. Controls how far each curvature
   * sample radiates outward. Smaller values produce sharp, localised hotspots; larger
   * values spread the influence across broader regions. Default: 0.12.
   */
  private float falloff = 0.12f;

  // Private — construct via from()
  private GlyphCurvatureField() {}

  // ─────────────────────────────────────────────────────────────────────────────
  // Factories
  // ─────────────────────────────────────────────────────────────────────────────

  /**
   * Computes a curvature field from the outline of a single character.
   *
   * <p>The outline is flattened to polylines by the extractor, then the Menger
   * curvature is estimated at each vertex as
   * {@code |cross(a,b)| / (|a|·|b|·|c|)} where {@code a} and {@code b} are the
   * two edge vectors meeting at the vertex.  All curvature values are normalised
   * to {@code [0,1]} before storage.</p>
   *
   * @param extractor the GlyphExtractor configured for the desired font and flatness
   * @param c         the character whose outline drives the field
   * @param fontSize  render size in pixels (must be positive)
   * @return a ready-to-use {@code GlyphCurvatureField}
   */
  public static GlyphCurvatureField from(GlyphExtractor extractor, char c, float fontSize) {
    GlyphCurvatureField field = new GlyphCurvatureField();
    field.compute(extractor.getContours(c, fontSize).toArray(new PVector[0][]));
    return field;
  }

  /**
   * Averages the curvature fields of multiple characters into a single composite field.
   *
   * <p>Useful when the grid displays several different glyphs and the animation should
   * reflect the overall geometric character of the typeface rather than a single letter.
   * All contours are collected into a shared bounding box before normalisation.</p>
   *
   * @param extractor the GlyphExtractor
   * @param chars     the characters to sample (must not be null or empty)
   * @param fontSize  render size in pixels (must be positive)
   * @return a composite {@code GlyphCurvatureField}
   */
  public static GlyphCurvatureField from(GlyphExtractor extractor, char[] chars, float fontSize) {
    if (chars == null || chars.length == 0) {
      GlyphCurvatureField empty = new GlyphCurvatureField();
      empty.nx = new float[0]; empty.ny = new float[0]; empty.cv = new float[0];
      return empty;
    }

    // Collect all vertices across all glyphs into one list
    List<float[]> rawCurvatures = new ArrayList<>();
    // First pass: collect all vertices for bounding-box computation
    List<PVector> allPts = new ArrayList<>();
    for (char c : chars) {
      List<PVector[]> contourList = extractor.getContours(c, fontSize);
      if (contourList == null) continue;
      PVector[][] contours = contourList.toArray(new PVector[0][]);
      for (PVector[] contour : contours) {
        if (contour == null) continue;
        for (PVector p : contour) allPts.add(p);
      }
    }
    if (allPts.isEmpty()) {
      GlyphCurvatureField empty = new GlyphCurvatureField();
      empty.nx = new float[0]; empty.ny = new float[0]; empty.cv = new float[0];
      return empty;
    }
    float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
    float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
    for (PVector p : allPts) {
      if (p.x < minX) minX = p.x; if (p.y < minY) minY = p.y;
      if (p.x > maxX) maxX = p.x; if (p.y > maxY) maxY = p.y;
    }
    float w = maxX - minX;
    float h = maxY - minY;
    if (w <= 0 || h <= 0) {
      GlyphCurvatureField empty = new GlyphCurvatureField();
      empty.nx = new float[0]; empty.ny = new float[0]; empty.cv = new float[0];
      return empty;
    }

    // Second pass: compute curvature for each character's contours
    List<float[]> samples = new ArrayList<>();
    for (char c : chars) {
      List<PVector[]> contourList = extractor.getContours(c, fontSize);
      PVector[][] contours = contourList == null ? new PVector[0][] : contourList.toArray(new PVector[0][]);
      collectSamples(contours, samples, minX, minY, w, h);
    }

    GlyphCurvatureField field = new GlyphCurvatureField();
    field.storeSamples(samples);
    return field;
  }

  // ─────────────────────────────────────────────────────────────────────────────
  // Internal computation
  // ─────────────────────────────────────────────────────────────────────────────

  /** Computes the field from a set of pre-extracted contours. */
  private void compute(PVector[][] contours) {
    if (contours == null || contours.length == 0) {
      nx = new float[0]; ny = new float[0]; cv = new float[0];
      return;
    }

    // Determine bounding box of all vertices
    float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
    float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
    for (PVector[] contour : contours) {
      if (contour == null) continue;
      for (PVector p : contour) {
        if (p.x < minX) minX = p.x; if (p.y < minY) minY = p.y;
        if (p.x > maxX) maxX = p.x; if (p.y > maxY) maxY = p.y;
      }
    }

    float w = maxX - minX;
    float h = maxY - minY;
    if (w <= 0 || h <= 0) { nx = new float[0]; ny = new float[0]; cv = new float[0]; return; }

    List<float[]> samples = new ArrayList<>();
    collectSamples(contours, samples, minX, minY, w, h);
    storeSamples(samples);
  }

  /**
   * Collects Menger-curvature samples from a contour array, normalising positions
   * to {@code [0,1]} using the supplied bounding-box origin and size.
   *
   * <p>Menger curvature at vertex {@code i}:
   * {@code κ = |cross(a,b)| / (|a|·|b|·|c|)} where
   * {@code a = curr − prev}, {@code b = next − curr}, {@code c = next − prev}.</p>
   */
  private static void collectSamples(PVector[][] contours, List<float[]> out,
                                     float minX, float minY, float w, float h) {
    if (contours == null) return;
    for (PVector[] contour : contours) {
      if (contour == null || contour.length < 3) continue;
      int n = contour.length;
      for (int i = 0; i < n; i++) {
        PVector prev = contour[(i - 1 + n) % n];
        PVector curr = contour[i];
        PVector next = contour[(i + 1) % n];

        float ax = curr.x - prev.x, ay = curr.y - prev.y;
        float bx = next.x - curr.x, by = next.y - curr.y;
        float cross = Math.abs(ax * by - ay * bx);
        float la    = (float) Math.sqrt(ax * ax + ay * ay);
        float lb    = (float) Math.sqrt(bx * bx + by * by);
        float cx2   = next.x - prev.x, cy2 = next.y - prev.y;
        float lc    = (float) Math.sqrt(cx2 * cx2 + cy2 * cy2);
        float denom = la * lb * lc;
        float curv  = denom < 1e-6f ? 0f : cross / denom;

        float snx = w > 0f ? (curr.x - minX) / w : 0.5f;
        float sny = h > 0f ? (curr.y - minY) / h : 0.5f;
        out.add(new float[]{snx, sny, curv});
      }
    }
  }

  /** Normalises curvature values to {@code [0,1]} and packs samples into arrays. */
  private void storeSamples(List<float[]> samples) {
    if (samples.isEmpty()) { nx = new float[0]; ny = new float[0]; cv = new float[0]; return; }

    float maxC = 0f;
    for (float[] s : samples) if (s[2] > maxC) maxC = s[2];

    nx = new float[samples.size()];
    ny = new float[samples.size()];
    cv = new float[samples.size()];
    for (int i = 0; i < samples.size(); i++) {
      nx[i] = samples.get(i)[0];
      ny[i] = samples.get(i)[1];
      cv[i] = maxC > 0f ? samples.get(i)[2] / maxC : 0f;
    }
  }

  // ─────────────────────────────────────────────────────────────────────────────
  // Field query
  // ─────────────────────────────────────────────────────────────────────────────

  /**
   * Samples the curvature field at a normalised grid position.
   *
   * <p>Both {@code qx} and {@code qy} are in {@code [0,1]}, where
   * {@code (0,0)} is the top-left corner of the grid and {@code (1,1)} is the
   * bottom-right.  The query is answered by summing contributions from all stored
   * curvature samples, each weighted by a Gaussian kernel with sigma = {@link #falloff}.
   * The result is normalised to {@code [0,1]}.</p>
   *
   * @param qx normalised x position (0–1)
   * @param qy normalised y position (0–1)
   * @return field value in [0, 1] (0.5 when no samples are stored)
   */
  public float sampleAt(float qx, float qy) {
    if (nx == null || nx.length == 0) return 0.5f;

    float inv2s2 = 0.5f / (falloff * falloff);
    float sum    = 0f;
    float wsum   = 0f;
    for (int i = 0; i < nx.length; i++) {
      float dx   = qx - nx[i];
      float dy   = qy - ny[i];
      float kern = (float) Math.exp(-(dx * dx + dy * dy) * inv2s2);
      sum  += cv[i] * kern;
      wsum += kern;
    }
    return wsum < 1e-12f ? 0.5f : PApplet.constrain(sum / wsum, 0f, 1f);
  }

  // ─────────────────────────────────────────────────────────────────────────────
  // Configuration
  // ─────────────────────────────────────────────────────────────────────────────

  /**
   * Sets the blend intensity between the standard per-cell amplitude and the field value.
   * {@code 0} = field has no effect (default amplitude used throughout);
   * {@code 1} = the field value completely replaces the default amplitude.
   * Intermediate values blend proportionally.
   *
   * @param intensity blend factor, clamped to [0, 1]
   * @return this instance for method chaining
   */
  public GlyphCurvatureField setIntensity(float intensity) {
    this.intensity = PApplet.constrain(intensity, 0f, 1f);
    return this;
  }

  /**
   * Returns the current blend intensity.
   *
   * @return intensity in [0, 1]
   */
  public float getIntensity() { return intensity; }

  /**
   * Sets the Gaussian kernel sigma in normalised [0,1] units.
   * Smaller values (e.g. 0.05) produce sharp, localised curvature hotspots.
   * Larger values (e.g. 0.25) spread each sample's influence across broader regions,
   * producing a smoother, more diffuse field.
   *
   * @param falloff sigma (must be positive)
   * @return this instance for method chaining
   */
  public GlyphCurvatureField setFalloff(float falloff) {
    if (falloff > 0f) this.falloff = falloff;
    return this;
  }

  /**
   * Returns the current Gaussian kernel sigma.
   *
   * @return falloff in normalised units
   */
  public float getFalloff() { return falloff; }

  /**
   * Returns the number of curvature samples stored in this field.
   * Equivalent to the total vertex count across all sampled glyph contours.
   *
   * @return sample count
   */
  public int getSampleCount() { return nx == null ? 0 : nx.length; }
}
