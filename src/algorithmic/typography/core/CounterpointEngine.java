/**
 * CounterpointEngine — assigns independent wave systems to a glyph's outer form
 * and counter-forms, animating positive and negative space as deliberate visual dialogue.
 *
 * @author Michail Semoglou
 * @version 0.3.0
 * @since 0.3.0
 */

package algorithmic.typography.core;

/**
 * CounterpointEngine brings the metaphor of musical counterpoint to typography.
 *
 * <p>In music, counterpoint is the technique of combining two melodic lines that
 * proceed in deliberate dialogue.  This class applies the same idea to type: the
 * outer letterform — stems, bowls, serifs — animates on one wave; the enclosed
 * white spaces (the counters in O, B, P, R, and related forms) animate on a
 * separate, complementary wave at a different speed, angle, or of a different type.
 * Positive and negative space become first-class animated entities, visible as
 * distinct rhythmic voices.</p>
 *
 * <p>No other generative-typography library currently treats counter-forms as
 * independent animated objects.</p>
 *
 * <h2>Usage</h2>
 * <pre>
 * // Two independent wave configurations
 * Configuration mainCfg    = new Configuration();
 * mainCfg.setWaveSpeed(1.0f).setWaveAngle(45).setWaveType("SINE");
 * mainCfg.setBrightnessRange(60, 255);
 *
 * Configuration counterCfg = new Configuration();
 * counterCfg.setWaveSpeed(1.8f).setWaveAngle(135).setWaveType("TRIANGLE");
 * counterCfg.setBrightnessRange(180, 255);
 *
 * WaveEngine mainWave    = new WaveEngine(mainCfg);
 * WaveEngine counterWave = new WaveEngine(counterCfg);
 *
 * CounterpointEngine engine = new CounterpointEngine(mainWave, counterWave);
 * at.setCounterpointEngine(engine);
 * </pre>
 *
 * <p>Characters with no counter-forms (I, H, L, V, etc.) display only the main wave;
 * the {@code CounterpointEngine} has no visible effect for those glyphs.</p>
 *
 * @author Michail Semoglou
 * @version 0.3.0
 * @since 0.3.0
 * @see algorithmic.typography.AlgorithmicTypography#setCounterpointEngine(CounterpointEngine)
 */
public class CounterpointEngine {

  private final WaveEngine mainWave;
  private final WaveEngine counterWave;

  // ─────────────────────────────────────────────────────────────────────────────
  // Construction
  // ─────────────────────────────────────────────────────────────────────────────

  /**
   * Creates a CounterpointEngine with independent wave engines for the outer letterform
   * and the inner counter-form regions.
   *
   * @param mainWave    wave engine for the outer positive form (must not be null)
   * @param counterWave wave engine for the inner counter-forms (must not be null)
   * @throws IllegalArgumentException if either engine is null
   */
  public CounterpointEngine(WaveEngine mainWave, WaveEngine counterWave) {
    if (mainWave == null || counterWave == null) {
      throw new IllegalArgumentException(
          "CounterpointEngine: neither WaveEngine may be null");
    }
    this.mainWave    = mainWave;
    this.counterWave = counterWave;
  }

  // ─────────────────────────────────────────────────────────────────────────────
  // Accessors
  // ─────────────────────────────────────────────────────────────────────────────

  /**
   * Returns the wave engine driving the outer (positive) letterform.
   *
   * @return main wave engine
   */
  public WaveEngine getMainWave() { return mainWave; }

  /**
   * Returns the wave engine driving the inner counter-forms.
   *
   * @return counter wave engine
   */
  public WaveEngine getCounterWave() { return counterWave; }

  // ─────────────────────────────────────────────────────────────────────────────
  // Per-frame colour queries
  // ─────────────────────────────────────────────────────────────────────────────

  /**
   * Returns the brightness value for the outer letterform at this grid cell.
   *
   * @param frameCount current Processing frameCount
   * @param x          grid column index
   * @param y          grid row index
   * @param tilesX     total grid columns
   * @param tilesY     total grid rows
   * @return brightness in the range configured on the main wave engine
   */
  public float getMainBrightness(int frameCount, int x, int y,
                                  float tilesX, float tilesY) {
    return mainWave.calculateColorCustom(frameCount, x, y, tilesX, tilesY);
  }

  /**
   * Returns the brightness value for the counter-form regions at this grid cell.
   *
   * @param frameCount current Processing frameCount
   * @param x          grid column index
   * @param y          grid row index
   * @param tilesX     total grid columns
   * @param tilesY     total grid rows
   * @return brightness in the range configured on the counter wave engine
   */
  public float getCounterBrightness(int frameCount, int x, int y,
                                     float tilesX, float tilesY) {
    return counterWave.calculateColorCustom(frameCount, x, y, tilesX, tilesY);
  }

  /**
   * Returns the hue for the outer letterform at this grid cell, derived from the
   * main wave engine's Configuration.  When {@code hueMin == hueMax} that fixed value
   * is returned.
   *
   * @param frameCount current Processing frameCount
   * @param x          column
   * @param y          row
   * @param tilesX     total columns
   * @param tilesY     total rows
   * @return hue value from the main wave engine
   */
  public float getMainHue(int frameCount, int x, int y,
                           float tilesX, float tilesY) {
    return mainWave.calculateHue(frameCount, x, y, tilesX, tilesY);
  }

  /**
   * Returns the hue for the counter-form regions at this grid cell, derived from the
   * counter wave engine's Configuration.
   *
   * @param frameCount current Processing frameCount
   * @param x          column
   * @param y          row
   * @param tilesX     total columns
   * @param tilesY     total rows
   * @return hue value from the counter wave engine
   */
  public float getCounterHue(int frameCount, int x, int y,
                              float tilesX, float tilesY) {
    return counterWave.calculateHue(frameCount, x, y, tilesX, tilesY);
  }

  /**
   * Returns the saturation for the outer form from the main wave engine.
   *
   * @param frameCount current Processing frameCount
   * @param x column  @param y row  @param tilesX total columns  @param tilesY total rows
   * @return saturation from main wave
   */
  public float getMainSaturation(int frameCount, int x, int y,
                                  float tilesX, float tilesY) {
    return mainWave.calculateSaturation(frameCount, x, y, tilesX, tilesY);
  }

  /**
   * Returns the saturation for the counter-form regions from the counter wave engine.
   *
   * @param frameCount current Processing frameCount
   * @param x column  @param y row  @param tilesX total columns  @param tilesY total rows
   * @return saturation from counter wave
   */
  public float getCounterSaturation(int frameCount, int x, int y,
                                     float tilesX, float tilesY) {
    return counterWave.calculateSaturation(frameCount, x, y, tilesX, tilesY);
  }

  // ─────────────────────────────────────────────────────────────────────────────
  // Frame update
  // ─────────────────────────────────────────────────────────────────────────────

  /**
   * Updates both wave engines for the current frame.
   *
   * <p>Each engine uses its own Configuration's {@code waveSpeed} so they can run
   * at independent tempos without any extra bookkeeping in the sketch.</p>
   *
   * @param frameCount current Processing frameCount
   */
  public void update(int frameCount) {
    mainWave.update(frameCount, mainWave.getConfig().getWaveSpeed());
    counterWave.update(frameCount, counterWave.getConfig().getWaveSpeed());
  }
}
