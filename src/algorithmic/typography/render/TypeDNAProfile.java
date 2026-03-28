/**
 * TypeDNAProfile — typographic fingerprint derived from glyph outline geometry.
 *
 * <p>A {@code TypeDNAProfile} aggregates four measurements extracted from the
 * letterforms of a font by {@link GlyphExtractor#buildTypeDNAProfile(char[], float)}:
 * <ul>
 *   <li><b>stressAxis</b> — dominant stroke-stress angle in degrees [0, 180)</li>
 *   <li><b>opticalCentroid</b> — ink-density-weighted perceptual centre of mass</li>
 *   <li><b>counterRatio</b> — fraction of the bounding box occupied by counter-forms [0, 1]</li>
 *   <li><b>strokeWeight</b> — estimated dominant stroke width in pixels</li>
 * </ul>
 *
 * <p>Apply a profile to an animation with
 * {@link algorithmic.typography.AlgorithmicTypography#applyTypeDNA(TypeDNAProfile)};
 * serialise / deserialise with {@link #toJSON()} and {@link #fromJSON(processing.data.JSONObject)}.</p>
 *
 * <pre>
 * TypeDNAProfile p = glyph.buildTypeDNAProfile(new char[]{'A','B','O','H','R'}, 600);
 * at.applyTypeDNA(p);
 * p.toJSON().save(sketchPath("data/myFont.dna.json"));
 * </pre>
 *
 * @author Michail Semoglou
 * @version 0.3.0
 * @since 0.2.5
 */

package algorithmic.typography.render;

import processing.core.PVector;
import processing.data.JSONObject;

public class TypeDNAProfile {

  private final float   stressAxis;
  private final PVector opticalCentroid;
  private final float   counterRatio;
  private final float   strokeWeight;

  /**
   * Constructs a TypeDNAProfile with all four measurements.
   *
   * @param stressAxis       dominant stroke-stress angle in degrees [0, 180)
   * @param opticalCentroid  ink-density-weighted perceptual centre of mass
   * @param counterRatio     counter area / bounding-box area [0, 1]
   * @param strokeWeight     estimated dominant stroke width in pixels
   */
  public TypeDNAProfile(float stressAxis, PVector opticalCentroid,
                        float counterRatio, float strokeWeight) {
    this.stressAxis      = stressAxis;
    this.opticalCentroid = opticalCentroid != null ? opticalCentroid.copy() : new PVector();
    this.counterRatio    = counterRatio;
    this.strokeWeight    = strokeWeight;
  }

  // ── Getters ──────────────────────────────────────────────────

  /**
   * Returns the dominant stroke-stress angle in degrees [0, 180).
   * 90° = vertical stress (transitional / modern serifs); near 0° = oblique stress
   * (old-style serifs); near 0° in monolinear faces.
   */
  public float getStressAxis() { return stressAxis; }

  /**
   * Returns the ink-density-weighted perceptual centre of mass.
   * Use as a pivot for rotation, orbit origins, or magnetic field anchors.
   */
  public PVector getOpticalCentroid() { return opticalCentroid.copy(); }

  /**
   * Returns the ratio of counter-form area to bounding-box area [0, 1].
   * 0 = no counters (I, L); higher values = more open letterforms (O, B).
   */
  public float getCounterRatio() { return counterRatio; }

  /**
   * Returns the estimated dominant stroke width in pixels.
   * Light faces have small values; bold/heavy faces have larger values.
   */
  public float getStrokeWeight() { return strokeWeight; }

  // ── Serialisation ─────────────────────────────────────────────

  /**
   * Serialises this profile to a {@link JSONObject}.
   *
   * <pre>
   * profile.toJSON().save(sketchPath("data/myFont.dna.json"));
   * </pre>
   *
   * @return a JSON representation of this profile
   */
  public JSONObject toJSON() {
    JSONObject json = new JSONObject();
    json.setFloat("stressAxis",    stressAxis);
    json.setFloat("counterRatio",  counterRatio);
    json.setFloat("strokeWeight",  strokeWeight);
    JSONObject oc = new JSONObject();
    oc.setFloat("x", opticalCentroid.x);
    oc.setFloat("y", opticalCentroid.y);
    json.setJSONObject("opticalCentroid", oc);
    return json;
  }

  /**
   * Reconstructs a TypeDNAProfile from a {@link JSONObject} created by {@link #toJSON()}.
   *
   * <pre>
   * JSONObject j = loadJSONObject("data/myFont.dna.json");
   * TypeDNAProfile p = TypeDNAProfile.fromJSON(j);
   * at.applyTypeDNA(p);
   * </pre>
   *
   * @param json the JSONObject to read from
   * @return the reconstructed profile
   */
  public static TypeDNAProfile fromJSON(JSONObject json) {
    float stress = json.getFloat("stressAxis", 90f);
    float ratio  = json.getFloat("counterRatio", 0f);
    float weight = json.getFloat("strokeWeight", 0f);
    JSONObject oc = json.hasKey("opticalCentroid") ? json.getJSONObject("opticalCentroid") : null;
    PVector centroid = (oc != null)
        ? new PVector(oc.getFloat("x", 0), oc.getFloat("y", 0))
        : new PVector();
    return new TypeDNAProfile(stress, centroid, ratio, weight);
  }

  @Override
  public String toString() {
    return String.format(
        "TypeDNAProfile{stressAxis=%.1f°, centroid=(%.1f,%.1f), counterRatio=%.3f, strokeWeight=%.1fpx}",
        stressAxis, opticalCentroid.x, opticalCentroid.y, counterRatio, strokeWeight);
  }
}
