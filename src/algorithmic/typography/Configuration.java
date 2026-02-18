/**
 * Configuration class for AlgorithmicTypography.
 *
 * This class encapsulates all configurable parameters for the algorithmic
 * typography system. It provides validation and default values for all
 * settings.
 *
 * @author Michail Semoglou
 * @version 1.0.0
 * @since 1.0.0
 */

package algorithmic.typography;

import processing.data.*;

/**
 * Configuration holds all parameters for the AlgorithmicTypography system.
 * 
 * <p>The configuration is organized into logical groups:</p>
 * <ul>
 *   <li>Canvas settings (width, height)</li>
 *   <li>Animation settings (duration, FPS, timing)</li>
 *   <li>Grid settings (tile counts for initial and changed states)</li>
 *   <li>Color settings (hue, saturation, brightness and wave amplitude ranges)</li>
 *   <li>Wave settings (speed and multiplier ranges)</li>
 * </ul>
 * 
 * <p>Default values are provided for all settings, ensuring the system
 * can operate even without explicit configuration.</p>
 */
public class Configuration {
  
  // Canvas configuration
  /** Width of the canvas in pixels. Default: 1080 */
  private int canvasWidth = 1080;
  
  /** Height of the canvas in pixels. Default: 1080 */
  private int canvasHeight = 1080;
  
  // Animation configuration
  /** Duration of the animation in seconds. Default: 18 */
  private int animationDuration = 18;
  
  /** Frames per second for the animation. Default: 30 */
  private int animationFPS = 30;
  
  /** Time in milliseconds when grid layout changes (stage 1 → stage 2). Default: 6000 */
  private int changeTime = 6000;
  
  /** Time in milliseconds for the second grid change (stage 2 → stage 3). Default: 12000 */
  private int secondChangeTime = 12000;
  
  /** Duration of fade transitions between stages in milliseconds. Default: 2000 */
  private int fadeDuration = 2000;
  
  /** Character to display in the grid. Default: "A" */
  private String character = "A";
  
  /** Scale factor for text size relative to tile size (0.0 - 1.0). Default: 0.8 */
  private float textScale = 0.8f;
  
  /** Whether to automatically save frames. Default: false */
  private boolean saveFrames = false;
  
  /** Speed multiplier for wave animation. Default: 1.0 */
  private float waveSpeed = 1.0f;
  
  /** Wave propagation angle in degrees (0-360). 0=horizontal, 90=vertical, 45=diagonal. Default: 45 */
  private float waveAngle = 45.0f;
  
  /** Minimum value for wave multiplier. Default: 0.0 */
  private float waveMultiplierMin = 0.0f;
  
  /** Maximum value for wave multiplier. Default: 2.0 */
  private float waveMultiplierMax = 2.0f;
  
  // Grid configuration
  /** Number of horizontal tiles in initial state. Default: 16 */
  private int initialTilesX = 16;
  
  /** Number of vertical tiles in initial state. Default: 16 */
  private int initialTilesY = 16;
  
  /** Number of horizontal tiles after change time. Default: 8 */
  private int changedTilesX = 8;
  
  /** Number of vertical tiles after change time. Default: 8 */
  private int changedTilesY = 8;
  
  /** Number of horizontal tiles in the final stage. Default: 4 */
  private int finalTilesX = 4;
  
  /** Number of vertical tiles in the final stage. Default: 4 */
  private int finalTilesY = 4;
  
  // Color configuration (HSB)
  /** Minimum hue value (0-360). Default: 0 */
  private float hueMin = 0.0f;
  
  /** Maximum hue value (0-360). Default: 0 (no hue variation) */
  private float hueMax = 0.0f;
  
  /** Minimum saturation value (0-255). Default: 0 (greyscale) */
  private float saturationMin = 0.0f;
  
  /** Maximum saturation value (0-255). Default: 0 (greyscale) */
  private float saturationMax = 0.0f;
  
  /** Minimum brightness value (0-255). Default: 50 */
  private float brightnessMin = 50.0f;
  
  /** Maximum brightness value (0-255). Default: 255 */
  private float brightnessMax = 255.0f;
  
  /** Minimum wave amplitude value. Default: -200 */
  private float waveAmplitudeMin = -200.0f;
  
  /** Maximum wave amplitude value. Default: 200 */
  private float waveAmplitudeMax = 200.0f;
  
  /**
   * Creates a new Configuration with default values.
   */
  public Configuration() {
    // Default constructor uses default field values
  }
  
  // ============================================================
  // Getters
  // ============================================================
  
  /** @return the canvas width in pixels */
  public int getCanvasWidth() { return canvasWidth; }
  
  /** @return the canvas height in pixels */
  public int getCanvasHeight() { return canvasHeight; }
  
  /** @return the animation duration in seconds */
  public int getAnimationDuration() { return animationDuration; }
  
  /** @return the animation frames per second */
  public int getAnimationFPS() { return animationFPS; }
  
  /** @return the time in milliseconds when grid layout changes */
  public int getChangeTime() { return changeTime; }
  
  /** @return the time in milliseconds for the second grid change (0 = disabled) */
  public int getSecondChangeTime() { return secondChangeTime; }
  
  /** @return the fade transition duration in milliseconds */
  public int getFadeDuration() { return fadeDuration; }
  
  /** @return the character(s) displayed in the grid */
  public String getCharacter() { return character; }
  
  /** @return the text scale factor (0.0 to 1.0) */
  public float getTextScale() { return textScale; }
  
  /** @return true if frame saving is enabled */
  public boolean isSaveFrames() { return saveFrames; }
  
  /** @return the wave animation speed multiplier */
  public float getWaveSpeed() { return waveSpeed; }
  
  /** @return the wave propagation angle in degrees (0-360) */
  public float getWaveAngle() { return waveAngle; }
  
  /** @return the minimum wave multiplier value */
  public float getWaveMultiplierMin() { return waveMultiplierMin; }
  
  /** @return the maximum wave multiplier value */
  public float getWaveMultiplierMax() { return waveMultiplierMax; }
  
  /** @return the initial horizontal tile count */
  public int getInitialTilesX() { return initialTilesX; }
  
  /** @return the initial vertical tile count */
  public int getInitialTilesY() { return initialTilesY; }
  
  /** @return the changed horizontal tile count */
  public int getChangedTilesX() { return changedTilesX; }
  
  /** @return the changed vertical tile count */
  public int getChangedTilesY() { return changedTilesY; }
  
  /** @return the final horizontal tile count */
  public int getFinalTilesX() { return finalTilesX; }
  
  /** @return the final vertical tile count */
  public int getFinalTilesY() { return finalTilesY; }
  
  /** @return the minimum hue value (0-360) */
  public float getHueMin() { return hueMin; }
  
  /** @return the maximum hue value (0-360) */
  public float getHueMax() { return hueMax; }
  
  /** @return the minimum saturation value (0-255) */
  public float getSaturationMin() { return saturationMin; }
  
  /** @return the maximum saturation value (0-255) */
  public float getSaturationMax() { return saturationMax; }
  
  /** @return the minimum brightness value (0-255) */
  public float getBrightnessMin() { return brightnessMin; }
  
  /** @return the maximum brightness value (0-255) */
  public float getBrightnessMax() { return brightnessMax; }
  
  /** @return the minimum wave amplitude value */
  public float getWaveAmplitudeMin() { return waveAmplitudeMin; }
  
  /** @return the maximum wave amplitude value */
  public float getWaveAmplitudeMax() { return waveAmplitudeMax; }
  
  // ============================================================
  // Setters (with validation)
  // ============================================================
  
  /**
   * Called when a configuration value changes via a setter.
   * Override in subclasses to react to changes.
   *
   * @param key the name of the changed parameter
   * @param value the new value
   */
  protected void onChange(String key, Object value) {
    // Default: no-op. Overridden by ObservableConfiguration.
  }
  
  /**
   * Sets the canvas width.
   *
   * @param canvasWidth canvas width in pixels (must be positive)
   * @return this instance for method chaining
   * @throws IllegalArgumentException if width is not positive
   */
  public Configuration setCanvasWidth(int canvasWidth) {
    if (canvasWidth <= 0) throw new IllegalArgumentException("Canvas width must be positive. Got: " + canvasWidth);
    this.canvasWidth = canvasWidth;
    onChange("canvasWidth", canvasWidth);
    return this;
  }
  
  /**
   * Sets the canvas height.
   *
   * @param canvasHeight canvas height in pixels (must be positive)
   * @return this instance for method chaining
   * @throws IllegalArgumentException if height is not positive
   */
  public Configuration setCanvasHeight(int canvasHeight) {
    if (canvasHeight <= 0) throw new IllegalArgumentException("Canvas height must be positive. Got: " + canvasHeight);
    this.canvasHeight = canvasHeight;
    onChange("canvasHeight", canvasHeight);
    return this;
  }
  
  /**
   * Sets the canvas dimensions.
   *
   * @param width canvas width in pixels (must be positive)
   * @param height canvas height in pixels (must be positive)
   * @return this instance for method chaining
   * @throws IllegalArgumentException if width or height is not positive
   */
  public Configuration setCanvasSize(int width, int height) {
    setCanvasWidth(width);
    setCanvasHeight(height);
    return this;
  }
  
  /**
   * Sets the animation duration.
   *
   * @param animationDuration duration in seconds (must be positive)
   * @return this instance for method chaining
   * @throws IllegalArgumentException if duration is not positive
   */
  public Configuration setAnimationDuration(int animationDuration) {
    if (animationDuration <= 0) throw new IllegalArgumentException("Animation duration must be positive. Got: " + animationDuration);
    this.animationDuration = animationDuration;
    onChange("animationDuration", animationDuration);
    return this;
  }
  
  /**
   * Sets the animation frame rate.
   *
   * @param animationFPS frames per second (must be positive)
   * @return this instance for method chaining
   * @throws IllegalArgumentException if FPS is not positive
   */
  public Configuration setAnimationFPS(int animationFPS) {
    if (animationFPS <= 0) throw new IllegalArgumentException("Animation FPS must be positive. Got: " + animationFPS);
    this.animationFPS = animationFPS;
    onChange("animationFPS", animationFPS);
    return this;
  }
  
  /**
   * Sets the time when grid layout changes.
   *
   * @param changeTime time in milliseconds (must be non-negative)
   * @return this instance for method chaining
   * @throws IllegalArgumentException if change time is negative
   */
  public Configuration setChangeTime(int changeTime) {
    if (changeTime < 0) throw new IllegalArgumentException("Change time must be non-negative. Got: " + changeTime);
    this.changeTime = changeTime;
    onChange("changeTime", changeTime);
    return this;
  }
  
  /**
   * Sets the time for the second grid change (stage 2 → stage 3).
   * Set to 0 to disable the third stage.
   *
   * @param secondChangeTime time in milliseconds (must be non-negative)
   * @return this instance for method chaining
   * @throws IllegalArgumentException if time is negative
   */
  public Configuration setSecondChangeTime(int secondChangeTime) {
    if (secondChangeTime < 0) throw new IllegalArgumentException("Second change time must be non-negative. Got: " + secondChangeTime);
    this.secondChangeTime = secondChangeTime;
    onChange("secondChangeTime", secondChangeTime);
    return this;
  }
  
  /**
   * Sets the duration of fade transitions between stages.
   * Set to 0 for instant (snap) transitions.
   *
   * @param fadeDuration duration in milliseconds (must be non-negative)
   * @return this instance for method chaining
   * @throws IllegalArgumentException if duration is negative
   */
  public Configuration setFadeDuration(int fadeDuration) {
    if (fadeDuration < 0) throw new IllegalArgumentException("Fade duration must be non-negative. Got: " + fadeDuration);
    this.fadeDuration = fadeDuration;
    onChange("fadeDuration", fadeDuration);
    return this;
  }
  
  /**
   * Sets the character(s) displayed in the grid.
   *
   * @param character the character(s) to display (must not be null or empty)
   * @return this instance for method chaining
   * @throws IllegalArgumentException if character is null or empty
   */
  public Configuration setCharacter(String character) {
    if (character == null || character.isEmpty()) throw new IllegalArgumentException("Character cannot be null or empty");
    this.character = character;
    onChange("character", character);
    return this;
  }
  
  /**
   * Sets the text scale factor.
   *
   * @param textScale scale relative to tile size (0.0 exclusive to 1.0 inclusive)
   * @return this instance for method chaining
   * @throws IllegalArgumentException if scale is out of range
   */
  public Configuration setTextScale(float textScale) {
    if (textScale <= 0 || textScale > 1) throw new IllegalArgumentException("Text scale must be between 0 (exclusive) and 1 (inclusive). Got: " + textScale);
    this.textScale = textScale;
    onChange("textScale", textScale);
    return this;
  }
  
  /**
   * Sets whether frames should be saved.
   *
   * @param saveFrames true to enable frame saving
   * @return this instance for method chaining
   */
  public Configuration setSaveFrames(boolean saveFrames) {
    this.saveFrames = saveFrames;
    onChange("saveFrames", saveFrames);
    return this;
  }
  
  /**
   * Sets the wave animation speed.
   *
   * @param waveSpeed speed multiplier
   * @return this instance for method chaining
   */
  public Configuration setWaveSpeed(float waveSpeed) {
    this.waveSpeed = waveSpeed;
    onChange("waveSpeed", waveSpeed);
    return this;
  }
  
  /**
   * Sets the wave propagation angle.
   * 0 = horizontal (left to right), 90 = vertical (top to bottom),
   * 45 = diagonal (default behavior).
   *
   * @param waveAngle angle in degrees (0-360)
   * @return this instance for method chaining
   */
  public Configuration setWaveAngle(float waveAngle) {
    this.waveAngle = waveAngle % 360.0f;
    onChange("waveAngle", this.waveAngle);
    return this;
  }
  
  /**
   * Sets the minimum wave multiplier.
   *
   * @param waveMultiplierMin minimum multiplier value
   * @return this instance for method chaining
   */
  public Configuration setWaveMultiplierMin(float waveMultiplierMin) {
    this.waveMultiplierMin = waveMultiplierMin;
    onChange("waveMultiplierMin", waveMultiplierMin);
    return this;
  }
  
  /**
   * Sets the maximum wave multiplier.
   *
   * @param waveMultiplierMax maximum multiplier value
   * @return this instance for method chaining
   */
  public Configuration setWaveMultiplierMax(float waveMultiplierMax) {
    this.waveMultiplierMax = waveMultiplierMax;
    onChange("waveMultiplierMax", waveMultiplierMax);
    return this;
  }
  
  /**
   * Sets the wave multiplier range.
   *
   * @param min minimum wave multiplier
   * @param max maximum wave multiplier
   * @return this instance for method chaining
   */
  public Configuration setWaveMultiplierRange(float min, float max) {
    setWaveMultiplierMin(min);
    setWaveMultiplierMax(max);
    return this;
  }
  
  /**
   * Sets the initial horizontal tile count.
   *
   * @param initialTilesX horizontal tile count (must be positive)
   * @return this instance for method chaining
   * @throws IllegalArgumentException if tile count is not positive
   */
  public Configuration setInitialTilesX(int initialTilesX) {
    if (initialTilesX <= 0) throw new IllegalArgumentException("Initial tiles X must be positive. Got: " + initialTilesX);
    this.initialTilesX = initialTilesX;
    onChange("initialTilesX", initialTilesX);
    return this;
  }
  
  /**
   * Sets the initial vertical tile count.
   *
   * @param initialTilesY vertical tile count (must be positive)
   * @return this instance for method chaining
   * @throws IllegalArgumentException if tile count is not positive
   */
  public Configuration setInitialTilesY(int initialTilesY) {
    if (initialTilesY <= 0) throw new IllegalArgumentException("Initial tiles Y must be positive. Got: " + initialTilesY);
    this.initialTilesY = initialTilesY;
    onChange("initialTilesY", initialTilesY);
    return this;
  }
  
  /**
   * Sets the changed horizontal tile count.
   *
   * @param changedTilesX horizontal tile count (must be positive)
   * @return this instance for method chaining
   * @throws IllegalArgumentException if tile count is not positive
   */
  public Configuration setChangedTilesX(int changedTilesX) {
    if (changedTilesX <= 0) throw new IllegalArgumentException("Changed tiles X must be positive. Got: " + changedTilesX);
    this.changedTilesX = changedTilesX;
    onChange("changedTilesX", changedTilesX);
    return this;
  }
  
  /**
   * Sets the changed vertical tile count.
   *
   * @param changedTilesY vertical tile count (must be positive)
   * @return this instance for method chaining
   * @throws IllegalArgumentException if tile count is not positive
   */
  public Configuration setChangedTilesY(int changedTilesY) {
    if (changedTilesY <= 0) throw new IllegalArgumentException("Changed tiles Y must be positive. Got: " + changedTilesY);
    this.changedTilesY = changedTilesY;
    onChange("changedTilesY", changedTilesY);
    return this;
  }
  
  /**
   * Sets the final horizontal tile count (stage 3).
   *
   * @param finalTilesX horizontal tile count (must be positive)
   * @return this instance for method chaining
   * @throws IllegalArgumentException if tile count is not positive
   */
  public Configuration setFinalTilesX(int finalTilesX) {
    if (finalTilesX <= 0) throw new IllegalArgumentException("Final tiles X must be positive. Got: " + finalTilesX);
    this.finalTilesX = finalTilesX;
    onChange("finalTilesX", finalTilesX);
    return this;
  }
  
  /**
   * Sets the final vertical tile count (stage 3).
   *
   * @param finalTilesY vertical tile count (must be positive)
   * @return this instance for method chaining
   * @throws IllegalArgumentException if tile count is not positive
   */
  public Configuration setFinalTilesY(int finalTilesY) {
    if (finalTilesY <= 0) throw new IllegalArgumentException("Final tiles Y must be positive. Got: " + finalTilesY);
    this.finalTilesY = finalTilesY;
    onChange("finalTilesY", finalTilesY);
    return this;
  }
  
  /**
   * Sets the grid tile counts for both initial and changed states.
   *
   * @param initialX initial horizontal tile count (must be positive)
   * @param initialY initial vertical tile count (must be positive)
   * @param changedX changed horizontal tile count (must be positive)
   * @param changedY changed vertical tile count (must be positive)
   * @return this instance for method chaining
   * @throws IllegalArgumentException if any tile count is not positive
   */
  public Configuration setGridSize(int initialX, int initialY, int changedX, int changedY) {
    setInitialTilesX(initialX);
    setInitialTilesY(initialY);
    setChangedTilesX(changedX);
    setChangedTilesY(changedY);
    return this;
  }
  
  /**
   * Sets the grid tile counts for all three stages.
   *
   * @param initialX initial horizontal tile count (must be positive)
   * @param initialY initial vertical tile count (must be positive)
   * @param changedX changed horizontal tile count (must be positive)
   * @param changedY changed vertical tile count (must be positive)
   * @param finalX final horizontal tile count (must be positive)
   * @param finalY final vertical tile count (must be positive)
   * @return this instance for method chaining
   * @throws IllegalArgumentException if any tile count is not positive
   */
  public Configuration setGridSize(int initialX, int initialY, int changedX, int changedY, int finalX, int finalY) {
    setGridSize(initialX, initialY, changedX, changedY);
    setFinalTilesX(finalX);
    setFinalTilesY(finalY);
    return this;
  }
  
  /**
   * Sets initial grid size (changed tiles default to half of initial).
   *
   * @param initialX initial horizontal tile count (must be positive)
   * @param initialY initial vertical tile count (must be positive)
   * @return this instance for method chaining
   * @throws IllegalArgumentException if any tile count is not positive
   */
  public Configuration setGridSize(int initialX, int initialY) {
    return setGridSize(initialX, initialY, Math.max(1, initialX / 2), Math.max(1, initialY / 2));
  }
  
  /**
   * Sets the minimum saturation value.
   *
   * @param saturationMin minimum saturation (0-255)
   * @return this instance for method chaining
   * @throws IllegalArgumentException if value is out of range
   */
  public Configuration setSaturationMin(float saturationMin) {
    if (saturationMin < 0 || saturationMin > 255) throw new IllegalArgumentException("Saturation min must be between 0 and 255. Got: " + saturationMin);
    this.saturationMin = saturationMin;
    onChange("saturationMin", saturationMin);
    return this;
  }
  
  /**
   * Sets the maximum saturation value.
   *
   * @param saturationMax maximum saturation (0-255)
   * @return this instance for method chaining
   * @throws IllegalArgumentException if value is out of range
   */
  public Configuration setSaturationMax(float saturationMax) {
    if (saturationMax < 0 || saturationMax > 255) throw new IllegalArgumentException("Saturation max must be between 0 and 255. Got: " + saturationMax);
    this.saturationMax = saturationMax;
    onChange("saturationMax", saturationMax);
    return this;
  }
  
  /**
   * Sets the saturation range.
   *
   * @param min minimum saturation (0-255)
   * @param max maximum saturation (0-255)
   * @return this instance for method chaining
   * @throws IllegalArgumentException if values are out of range
   */
  public Configuration setSaturationRange(float min, float max) {
    setSaturationMin(min);
    setSaturationMax(max);
    return this;
  }
  
  /**
   * Sets the minimum brightness value.
   *
   * @param brightnessMin minimum brightness (0-255)
   * @return this instance for method chaining
   * @throws IllegalArgumentException if value is out of range
   */
  public Configuration setBrightnessMin(float brightnessMin) {
    if (brightnessMin < 0 || brightnessMin > 255) throw new IllegalArgumentException("Brightness min must be between 0 and 255. Got: " + brightnessMin);
    this.brightnessMin = brightnessMin;
    onChange("brightnessMin", brightnessMin);
    return this;
  }
  
  /**
   * Sets the maximum brightness value.
   *
   * @param brightnessMax maximum brightness (0-255)
   * @return this instance for method chaining
   * @throws IllegalArgumentException if value is out of range
   */
  public Configuration setBrightnessMax(float brightnessMax) {
    if (brightnessMax < 0 || brightnessMax > 255) throw new IllegalArgumentException("Brightness max must be between 0 and 255. Got: " + brightnessMax);
    this.brightnessMax = brightnessMax;
    onChange("brightnessMax", brightnessMax);
    return this;
  }
  
  /**
   * Sets the brightness range.
   *
   * @param min minimum brightness (0-255)
   * @param max maximum brightness (0-255)
   * @return this instance for method chaining
   * @throws IllegalArgumentException if values are out of range
   */
  public Configuration setBrightnessRange(float min, float max) {
    setBrightnessMin(min);
    setBrightnessMax(max);
    return this;
  }
  
  /**
   * Sets the minimum hue value.
   *
   * @param hueMin minimum hue (0-360)
   * @return this instance for method chaining
   * @throws IllegalArgumentException if value is out of range
   */
  public Configuration setHueMin(float hueMin) {
    if (hueMin < 0 || hueMin > 360) throw new IllegalArgumentException("Hue min must be between 0 and 360. Got: " + hueMin);
    this.hueMin = hueMin;
    onChange("hueMin", hueMin);
    return this;
  }
  
  /**
   * Sets the maximum hue value.
   *
   * @param hueMax maximum hue (0-360)
   * @return this instance for method chaining
   * @throws IllegalArgumentException if value is out of range
   */
  public Configuration setHueMax(float hueMax) {
    if (hueMax < 0 || hueMax > 360) throw new IllegalArgumentException("Hue max must be between 0 and 360. Got: " + hueMax);
    this.hueMax = hueMax;
    onChange("hueMax", hueMax);
    return this;
  }
  
  /**
   * Sets the hue range.
   *
   * @param min minimum hue (0-360)
   * @param max maximum hue (0-360)
   * @return this instance for method chaining
   * @throws IllegalArgumentException if values are out of range
   */
  public Configuration setHueRange(float min, float max) {
    setHueMin(min);
    setHueMax(max);
    return this;
  }
  
  /**
   * Sets the minimum wave amplitude.
   *
   * @param waveAmplitudeMin minimum amplitude value
   * @return this instance for method chaining
   */
  public Configuration setWaveAmplitudeMin(float waveAmplitudeMin) {
    this.waveAmplitudeMin = waveAmplitudeMin;
    onChange("waveAmplitudeMin", waveAmplitudeMin);
    return this;
  }
  
  /**
   * Sets the maximum wave amplitude.
   *
   * @param waveAmplitudeMax maximum amplitude value
   * @return this instance for method chaining
   */
  public Configuration setWaveAmplitudeMax(float waveAmplitudeMax) {
    this.waveAmplitudeMax = waveAmplitudeMax;
    onChange("waveAmplitudeMax", waveAmplitudeMax);
    return this;
  }
  
  /**
   * Sets the wave amplitude range.
   *
   * @param min minimum amplitude value
   * @param max maximum amplitude value
   * @return this instance for method chaining
   */
  public Configuration setWaveAmplitudeRange(float min, float max) {
    setWaveAmplitudeMin(min);
    setWaveAmplitudeMax(max);
    return this;
  }
  
  /**
   * Loads configuration from a JSON object.
   * 
   * This method parses a JSONObject and applies its values to this
   * configuration. It validates values and provides meaningful error
   * messages for invalid data.
   * 
   * @param json the JSONObject containing configuration data
   * @throws IllegalArgumentException if json is null or contains invalid values
   */
  public void loadFromJSON(JSONObject json) {
    if (json == null) {
      throw new IllegalArgumentException("JSON object cannot be null");
    }
    
    // Load canvas configuration
    if (json.hasKey("canvas")) {
      JSONObject canvas = json.getJSONObject("canvas");
      canvasWidth = getPositiveInt(canvas, "width", canvasWidth, "Canvas width");
      canvasHeight = getPositiveInt(canvas, "height", canvasHeight, "Canvas height");
    }
    
    // Load animation configuration
    if (json.hasKey("animation")) {
      JSONObject animation = json.getJSONObject("animation");
      animationDuration = getPositiveInt(animation, "duration", animationDuration, "Animation duration");
      animationFPS = getPositiveInt(animation, "fps", animationFPS, "Animation FPS");
      changeTime = getNonNegativeInt(animation, "changeTime", changeTime, "Change time");
      secondChangeTime = getNonNegativeInt(animation, "secondChangeTime", secondChangeTime, "Second change time");
      fadeDuration = getNonNegativeInt(animation, "fadeDuration", fadeDuration, "Fade duration");
      
      if (animation.hasKey("character")) {
        String ch = animation.getString("character");
        if (ch != null && !ch.isEmpty()) {
          character = ch;
        }
      }
      
      textScale = getFloatInRange(animation, "textScale", textScale, 0.0f, 1.0f, "Text scale");
      saveFrames = animation.getBoolean("saveFrames", saveFrames);
      waveSpeed = getFloat(animation, "waveSpeed", waveSpeed, "Wave speed");
      waveAngle = getFloat(animation, "waveAngle", waveAngle, "Wave angle");
      waveMultiplierMin = getFloat(animation, "waveMultiplierMin", waveMultiplierMin, "Wave multiplier min");
      waveMultiplierMax = getFloat(animation, "waveMultiplierMax", waveMultiplierMax, "Wave multiplier max");
      
      // Validate wave multiplier range
      if (waveMultiplierMin > waveMultiplierMax) {
        System.err.println("Warning: waveMultiplierMin > waveMultiplierMax, swapping values");
        float temp = waveMultiplierMin;
        waveMultiplierMin = waveMultiplierMax;
        waveMultiplierMax = temp;
      }
    }
    
    // Load grid configuration
    if (json.hasKey("grid")) {
      JSONObject grid = json.getJSONObject("grid");
      initialTilesX = getPositiveInt(grid, "initialTilesX", initialTilesX, "Initial tiles X");
      initialTilesY = getPositiveInt(grid, "initialTilesY", initialTilesY, "Initial tiles Y");
      changedTilesX = getPositiveInt(grid, "changedTilesX", changedTilesX, "Changed tiles X");
      changedTilesY = getPositiveInt(grid, "changedTilesY", changedTilesY, "Changed tiles Y");
      finalTilesX = getPositiveInt(grid, "finalTilesX", finalTilesX, "Final tiles X");
      finalTilesY = getPositiveInt(grid, "finalTilesY", finalTilesY, "Final tiles Y");
    }
    
    // Load color configuration (HSB)
    if (json.hasKey("colors")) {
      JSONObject colors = json.getJSONObject("colors");
      hueMin = getFloatInRange(colors, "hueMin", hueMin, 0.0f, 360.0f, "Hue min");
      hueMax = getFloatInRange(colors, "hueMax", hueMax, 0.0f, 360.0f, "Hue max");
      saturationMin = getFloatInRange(colors, "saturationMin", saturationMin, 0.0f, 255.0f, "Saturation min");
      saturationMax = getFloatInRange(colors, "saturationMax", saturationMax, 0.0f, 255.0f, "Saturation max");
      brightnessMin = getFloatInRange(colors, "brightnessMin", brightnessMin, 0.0f, 255.0f, "Brightness min");
      brightnessMax = getFloatInRange(colors, "brightnessMax", brightnessMax, 0.0f, 255.0f, "Brightness max");
      waveAmplitudeMin = getFloat(colors, "waveAmplitudeMin", waveAmplitudeMin, "Wave amplitude min");
      waveAmplitudeMax = getFloat(colors, "waveAmplitudeMax", waveAmplitudeMax, "Wave amplitude max");
      
      // Validate ranges
      if (saturationMin > saturationMax) {
        System.err.println("Warning: saturationMin > saturationMax, swapping values");
        float temp = saturationMin;
        saturationMin = saturationMax;
        saturationMax = temp;
      }
      if (brightnessMin > brightnessMax) {
        System.err.println("Warning: brightnessMin > brightnessMax, swapping values");
        float temp = brightnessMin;
        brightnessMin = brightnessMax;
        brightnessMax = temp;
      }
    }
    
    // Validate overall configuration
    validate();
  }
  
  /**
   * Validates the current configuration values.
   * 
   * @throws IllegalStateException if configuration is invalid
   */
  public void validate() {
    if (canvasWidth <= 0 || canvasHeight <= 0) {
      throw new IllegalStateException("Canvas dimensions must be positive. Got: width=" + canvasWidth + ", height=" + canvasHeight);
    }
    if (animationDuration <= 0) {
      throw new IllegalStateException("Animation duration must be positive. Got: " + animationDuration);
    }
    if (animationFPS <= 0) {
      throw new IllegalStateException("Animation FPS must be positive. Got: " + animationFPS);
    }
    if (initialTilesX <= 0 || initialTilesY <= 0 || changedTilesX <= 0 || changedTilesY <= 0 || finalTilesX <= 0 || finalTilesY <= 0) {
      throw new IllegalStateException("Tile counts must be positive. Got: initialTilesX=" + initialTilesX + 
          ", initialTilesY=" + initialTilesY + ", changedTilesX=" + changedTilesX + ", changedTilesY=" + changedTilesY +
          ", finalTilesX=" + finalTilesX + ", finalTilesY=" + finalTilesY);
    }
    if (fadeDuration < 0) {
      throw new IllegalStateException("Fade duration must be non-negative. Got: " + fadeDuration);
    }
    if (secondChangeTime < 0) {
      throw new IllegalStateException("Second change time must be non-negative. Got: " + secondChangeTime);
    }
    if (secondChangeTime > 0 && secondChangeTime <= changeTime) {
      throw new IllegalStateException("Second change time must be greater than first change time. Got: changeTime=" + changeTime + ", secondChangeTime=" + secondChangeTime);
    }
    if (textScale <= 0 || textScale > 1) {
      throw new IllegalStateException("Text scale must be between 0 (exclusive) and 1 (inclusive). Got: " + textScale);
    }
    if (hueMin < 0 || hueMin > 360 || hueMax < 0 || hueMax > 360) {
      throw new IllegalStateException("Hue values must be between 0 and 360. Got: hueMin=" + 
          hueMin + ", hueMax=" + hueMax);
    }
    if (saturationMin < 0 || saturationMin > 255 || saturationMax < 0 || saturationMax > 255) {
      throw new IllegalStateException("Saturation values must be between 0 and 255. Got: saturationMin=" + 
          saturationMin + ", saturationMax=" + saturationMax);
    }
    if (brightnessMin < 0 || brightnessMin > 255 || brightnessMax < 0 || brightnessMax > 255) {
      throw new IllegalStateException("Brightness values must be between 0 and 255. Got: brightnessMin=" + 
          brightnessMin + ", brightnessMax=" + brightnessMax);
    }
  }
  
  /**
   * Gets a positive integer from a JSON object.
   */
  private int getPositiveInt(JSONObject json, String key, int defaultValue, String description) {
    if (!json.hasKey(key)) {
      return defaultValue;
    }
    int value = json.getInt(key, defaultValue);
    if (value <= 0) {
      System.err.println("Warning: " + description + " must be positive, using default: " + defaultValue);
      return defaultValue;
    }
    return value;
  }
  
  /**
   * Gets a non-negative integer from a JSON object.
   */
  private int getNonNegativeInt(JSONObject json, String key, int defaultValue, String description) {
    if (!json.hasKey(key)) {
      return defaultValue;
    }
    int value = json.getInt(key, defaultValue);
    if (value < 0) {
      System.err.println("Warning: " + description + " must be non-negative, using default: " + defaultValue);
      return defaultValue;
    }
    return value;
  }
  
  /**
   * Gets a float from a JSON object.
   */
  private float getFloat(JSONObject json, String key, float defaultValue, String description) {
    if (!json.hasKey(key)) {
      return defaultValue;
    }
    return json.getFloat(key, defaultValue);
  }
  
  /**
   * Gets a float from a JSON object, constrained to a range.
   */
  private float getFloatInRange(JSONObject json, String key, float defaultValue, 
                                 float min, float max, String description) {
    if (!json.hasKey(key)) {
      return defaultValue;
    }
    float value = json.getFloat(key, defaultValue);
    if (value < min || value > max) {
      System.err.println("Warning: " + description + " must be between " + min + " and " + max + 
                        ", using default: " + defaultValue);
      return defaultValue;
    }
    return value;
  }
  
  /**
   * Creates a JSON representation of this configuration.
   * 
   * @return a JSONObject containing all configuration values
   */
  public JSONObject toJSON() {
    JSONObject json = new JSONObject();
    
    JSONObject canvas = new JSONObject();
    canvas.setInt("width", canvasWidth);
    canvas.setInt("height", canvasHeight);
    json.setJSONObject("canvas", canvas);
    
    JSONObject animation = new JSONObject();
    animation.setInt("duration", animationDuration);
    animation.setInt("fps", animationFPS);
    animation.setInt("changeTime", changeTime);
    animation.setInt("secondChangeTime", secondChangeTime);
    animation.setInt("fadeDuration", fadeDuration);
    animation.setString("character", character);
    animation.setFloat("textScale", textScale);
    animation.setBoolean("saveFrames", saveFrames);
    animation.setFloat("waveSpeed", waveSpeed);
    animation.setFloat("waveAngle", waveAngle);
    animation.setFloat("waveMultiplierMin", waveMultiplierMin);
    animation.setFloat("waveMultiplierMax", waveMultiplierMax);
    json.setJSONObject("animation", animation);
    
    JSONObject grid = new JSONObject();
    grid.setInt("initialTilesX", initialTilesX);
    grid.setInt("initialTilesY", initialTilesY);
    grid.setInt("changedTilesX", changedTilesX);
    grid.setInt("changedTilesY", changedTilesY);
    grid.setInt("finalTilesX", finalTilesX);
    grid.setInt("finalTilesY", finalTilesY);
    json.setJSONObject("grid", grid);
    
    JSONObject colors = new JSONObject();
    colors.setFloat("hueMin", hueMin);
    colors.setFloat("hueMax", hueMax);
    colors.setFloat("saturationMin", saturationMin);
    colors.setFloat("saturationMax", saturationMax);
    colors.setFloat("brightnessMin", brightnessMin);
    colors.setFloat("brightnessMax", brightnessMax);
    colors.setFloat("waveAmplitudeMin", waveAmplitudeMin);
    colors.setFloat("waveAmplitudeMax", waveAmplitudeMax);
    json.setJSONObject("colors", colors);
    
    return json;
  }
  
  /**
   * Creates a deep copy of this configuration.
   * 
   * @return a new Configuration with the same values
   */
  public Configuration copy() {
    Configuration copy = new Configuration();
    copy.canvasWidth = this.canvasWidth;
    copy.canvasHeight = this.canvasHeight;
    copy.animationDuration = this.animationDuration;
    copy.animationFPS = this.animationFPS;
    copy.changeTime = this.changeTime;
    copy.secondChangeTime = this.secondChangeTime;
    copy.fadeDuration = this.fadeDuration;
    copy.character = this.character;
    copy.textScale = this.textScale;
    copy.saveFrames = this.saveFrames;
    copy.waveSpeed = this.waveSpeed;
    copy.waveAngle = this.waveAngle;
    copy.waveMultiplierMin = this.waveMultiplierMin;
    copy.waveMultiplierMax = this.waveMultiplierMax;
    copy.initialTilesX = this.initialTilesX;
    copy.initialTilesY = this.initialTilesY;
    copy.changedTilesX = this.changedTilesX;
    copy.changedTilesY = this.changedTilesY;
    copy.finalTilesX = this.finalTilesX;
    copy.finalTilesY = this.finalTilesY;
    copy.hueMin = this.hueMin;
    copy.hueMax = this.hueMax;
    copy.saturationMin = this.saturationMin;
    copy.saturationMax = this.saturationMax;
    copy.brightnessMin = this.brightnessMin;
    copy.brightnessMax = this.brightnessMax;
    copy.waveAmplitudeMin = this.waveAmplitudeMin;
    copy.waveAmplitudeMax = this.waveAmplitudeMax;
    return copy;
  }
  
  /**
   * Creates a new Builder for constructing Configuration instances.
   * 
   * <p>The Builder pattern provides a fluent API for creating configurations:</p>
   * <pre>
   * Configuration config = Configuration.builder()
   *     .canvasSize(1920, 1080)
   *     .gridSize(32, 32, 16, 16)
   *     .waveSpeed(3.0f)
   *     .saturation(50, 200)
   *     .build();
   * </pre>
   * 
   * @return a new Builder instance
   */
  public static Builder builder() {
    return new Builder();
  }
  
  /**
   * Builder class for creating Configuration instances with a fluent API.
   * 
   * <p>This builder allows for clean, readable configuration construction,
   * especially useful when setting multiple parameters programmatically.</p>
   */
  public static class Builder {
    private final Configuration config;
    
    /**
     * Creates a new Builder with default configuration values.
     */
    public Builder() {
      config = new Configuration();
    }
    
    /**
     * Sets the canvas dimensions.
     * 
     * @param width canvas width in pixels (must be positive)
     * @param height canvas height in pixels (must be positive)
     * @return this builder for method chaining
     */
    public Builder canvasSize(int width, int height) {
      config.canvasWidth = width;
      config.canvasHeight = height;
      return this;
    }
    
    /**
     * Sets the animation duration and frame rate.
     * 
     * @param durationSeconds animation duration in seconds
     * @param fps frames per second
     * @return this builder for method chaining
     */
    public Builder animation(int durationSeconds, int fps) {
      config.animationDuration = durationSeconds;
      config.animationFPS = fps;
      return this;
    }
    
    /**
     * Sets the grid tile counts.
     * 
     * @param initialX initial horizontal tiles
     * @param initialY initial vertical tiles
     * @param changedX changed horizontal tiles
     * @param changedY changed vertical tiles
     * @return this builder for method chaining
     */
    public Builder gridSize(int initialX, int initialY, int changedX, int changedY) {
      config.initialTilesX = initialX;
      config.initialTilesY = initialY;
      config.changedTilesX = changedX;
      config.changedTilesY = changedY;
      return this;
    }
    
    /**
     * Sets the grid tile counts for all three stages.
     * 
     * @param initialX initial horizontal tiles
     * @param initialY initial vertical tiles
     * @param changedX changed horizontal tiles
     * @param changedY changed vertical tiles
     * @param finalX final horizontal tiles
     * @param finalY final vertical tiles
     * @return this builder for method chaining
     */
    public Builder gridSize(int initialX, int initialY, int changedX, int changedY, int finalX, int finalY) {
      gridSize(initialX, initialY, changedX, changedY);
      config.finalTilesX = finalX;
      config.finalTilesY = finalY;
      return this;
    }
    
    /**
     * Sets the grid tile counts (square grids, same initial and changed).
     * 
     * @param tiles number of tiles in both dimensions
     * @return this builder for method chaining
     */
    public Builder gridSize(int tiles) {
      return gridSize(tiles, tiles, tiles / 2, tiles / 2);
    }
    
    /**
     * Sets the wave animation speed.
     * 
     * @param speed wave speed multiplier
     * @return this builder for method chaining
     */
    public Builder waveSpeed(float speed) {
      config.waveSpeed = speed;
      return this;
    }
    
    /**
     * Sets the wave propagation angle.
     * 0 = horizontal, 90 = vertical, 45 = diagonal.
     * 
     * @param angle wave angle in degrees (0-360)
     * @return this builder for method chaining
     */
    public Builder waveAngle(float angle) {
      config.waveAngle = angle % 360.0f;
      return this;
    }
    
    /**
     * Sets the wave multiplier range.
     * 
     * @param min minimum wave multiplier
     * @param max maximum wave multiplier
     * @return this builder for method chaining
     */
    public Builder waveMultiplier(float min, float max) {
      config.waveMultiplierMin = min;
      config.waveMultiplierMax = max;
      return this;
    }
    
    /**
     * Sets the saturation range.
     * 
     * @param min minimum saturation (0-255)
     * @param max maximum saturation (0-255)
     * @return this builder for method chaining
     */
    public Builder saturation(float min, float max) {
      config.saturationMin = min;
      config.saturationMax = max;
      return this;
    }
    
    /**
     * Sets the brightness range.
     * 
     * @param min minimum brightness (0-255)
     * @param max maximum brightness (0-255)
     * @return this builder for method chaining
     */
    public Builder brightness(float min, float max) {
      config.brightnessMin = min;
      config.brightnessMax = max;
      return this;
    }
    
    /**
     * Sets the hue range.
     * 
     * @param min minimum hue (0-360)
     * @param max maximum hue (0-360)
     * @return this builder for method chaining
     */
    public Builder hue(float min, float max) {
      config.hueMin = min;
      config.hueMax = max;
      return this;
    }
    
    /**
     * Sets the wave amplitude range.
     * 
     * @param min minimum amplitude
     * @param max maximum amplitude
     * @return this builder for method chaining
     */
    public Builder waveAmplitude(float min, float max) {
      config.waveAmplitudeMin = min;
      config.waveAmplitudeMax = max;
      return this;
    }
    
    /**
     * Sets the display character.
     * 
     * @param character the character(s) to display
     * @return this builder for method chaining
     */
    public Builder character(String character) {
      config.character = character;
      return this;
    }
    
    /**
     * Sets the text scale factor.
     * 
     * @param scale text scale (0.0 to 1.0)
     * @return this builder for method chaining
     */
    public Builder textScale(float scale) {
      config.textScale = scale;
      return this;
    }
    
    /**
     * Sets whether frames should be saved.
     * 
     * @param save true to save frames
     * @return this builder for method chaining
     */
    public Builder saveFrames(boolean save) {
      config.saveFrames = save;
      return this;
    }
    
    /**
     * Sets the grid change time.
     * 
     * @param timeMs time in milliseconds when grid changes
     * @return this builder for method chaining
     */
    public Builder changeTime(int timeMs) {
      config.changeTime = timeMs;
      return this;
    }
    
    /**
     * Sets the second grid change time (stage 2 → stage 3).
     * Set to 0 to disable the third stage.
     * 
     * @param timeMs time in milliseconds for the second grid change
     * @return this builder for method chaining
     */
    public Builder secondChangeTime(int timeMs) {
      config.secondChangeTime = timeMs;
      return this;
    }
    
    /**
     * Sets the fade transition duration.
     * Set to 0 for instant transitions.
     * 
     * @param durationMs fade duration in milliseconds
     * @return this builder for method chaining
     */
    public Builder fadeDuration(int durationMs) {
      config.fadeDuration = durationMs;
      return this;
    }
    
    /**
     * Builds and validates the Configuration instance.
     * 
     * @return a new Configuration with the builder's values
     * @throws IllegalStateException if configuration is invalid
     */
    public Configuration build() {
      config.validate();
      return config;
    }
  }
}
