/**
 * AmplitudeField — interface for spatial fields that modulate per-cell wave amplitude.
 *
 * @author Michail Semoglou
 * @version 0.3.0
 * @since 0.3.0
 */

package algorithmic.typography.core;

/**
 * A 2-D scalar field that modulates the per-cell wave amplitude inside a
 * {@link WaveEngine}.
 *
 * <p>Implementing classes map a normalised grid position {@code (nx, ny) ∈ [0,1]²}
 * to a field value in {@code [0,1]}.  The value is blended with the engine's
 * default amplitude according to {@link #getIntensity()}:</p>
 * <pre>
 * effective amplitude = lerp(defaultAmplitude, fieldValue, intensity)
 * </pre>
 *
 * <p>The primary implementation shipped with this library is
 * {@link algorithmic.typography.render.GlyphCurvatureField}, which derives the
 * field from a letterform's outline curvature.  Custom implementations are
 * supported — any numeric mapping from normalised grid coordinates to amplitude
 * can be plugged in.</p>
 *
 * @author Michail Semoglou
 * @version 0.3.0
 * @since 0.3.0
 * @see WaveEngine#setAmplitudeField(AmplitudeField)
 */
public interface AmplitudeField {

  /**
   * Returns the field value at a normalised grid position.
   *
   * @param nx normalised column in [0, 1] (0 = leftmost column, 1 = rightmost)
   * @param ny normalised row in [0, 1] (0 = top row, 1 = bottom row)
   * @return scalar field value in [0, 1]
   */
  float sampleAt(float nx, float ny);

  /**
   * Returns the blend intensity between the default per-cell amplitude and the
   * field value.  {@code 0} means the field has no effect; {@code 1} means the
   * field completely replaces the default amplitude.
   *
   * @return intensity in [0, 1]
   */
  float getIntensity();
}
