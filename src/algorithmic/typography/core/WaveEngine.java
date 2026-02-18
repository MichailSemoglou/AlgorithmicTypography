/**
 * WaveEngine - Core mathematical wave function calculations.
 * 
 * This class handles all wave-based mathematics for the typography system,
 * separating calculation logic from rendering. It supports pluggable
 * wave functions and provides normalized output values.
 * 
 * @author Michail Semoglou
 * @version 1.0.0
 */

package algorithmic.typography.core;

import processing.core.*;
import algorithmic.typography.Configuration;

/**
 * WaveEngine performs mathematical calculations for wave-based animations.
 * 
 * <p>The engine uses a combination of sine and tangent functions to create
 * dynamic patterns. It supports custom wave functions through the
 * {@link WaveFunction} interface.</p>
 * 
 * <p>All calculations are normalized to 0.0-1.0 range for consistent
 * color mapping and transformations.</p>
 */
public class WaveEngine {
  
  private final Configuration config;
  private WaveFunction customWaveFunction = null;
  
  // Pre-calculated values for performance
  private float waveMultiplier;
  private float frameRadians;
  
  // Auto-update tracking
  private boolean autoUpdate = true;
  private int lastUpdateFrame = -1;
  private float lastWaveSpeed = -1;
  
  /**
   * Creates a new WaveEngine with the specified configuration.
   * 
   * @param config the configuration containing wave parameters
   */
  public WaveEngine(Configuration config) {
    this.config = config;
  }
  
  /**
   * Updates wave calculations for the current frame.
   * 
   * Call this once per frame before calculating grid values, or rely on
   * auto-update which automatically updates before the first calculation
   * each frame.
   * 
   * @param frameCount the current frame number
   * @param waveSpeed the speed multiplier for wave animation
   */
  public void update(int frameCount, float waveSpeed) {
    frameRadians = PApplet.radians(frameCount);
    waveMultiplier = PApplet.map(PApplet.sin(frameRadians), -1, 1, 
                                  config.getWaveMultiplierMin(), config.getWaveMultiplierMax());
    lastUpdateFrame = frameCount;
    lastWaveSpeed = waveSpeed;
  }
  
  /**
   * Ensures wave calculations are updated for the current frame.
   * 
   * <p>When auto-update is enabled (default), this is called automatically
   * by calculation methods. It prevents redundant updates within the same frame.</p>
   * 
   * @param frameCount the current frame number
   * @param waveSpeed the speed multiplier for wave animation
   */
  private void ensureUpdated(int frameCount, float waveSpeed) {
    if (autoUpdate && (frameCount != lastUpdateFrame || waveSpeed != lastWaveSpeed)) {
      update(frameCount, waveSpeed);
    }
  }
  
  /**
   * Enables or disables auto-update mode.
   * 
   * <p>When enabled (default), calculation methods automatically call update()
   * if needed. Disable for manual control over update timing.</p>
   * 
   * @param enabled true to enable auto-update
   */
  public void setAutoUpdate(boolean enabled) {
    this.autoUpdate = enabled;
  }
  
  /**
   * Checks if auto-update mode is enabled.
   * 
   * @return true if auto-update is enabled
   */
  public boolean isAutoUpdate() {
    return autoUpdate;
  }
  
  /**
   * Calculates the amplitude value for a grid position.
   * 
   * This uses tangent functions based on grid coordinates.
   * 
   * @param x the x grid coordinate
   * @param y the y grid coordinate
   * @return normalized amplitude value (0.0 to 1.0)
   */
  public float calculateAmplitude(int x, int y) {
    float a = PApplet.map(PApplet.tan(PApplet.radians(x + y)), -1, 1, 
                          config.getWaveAmplitudeMin(), config.getWaveAmplitudeMax());
    return PApplet.norm(a, config.getWaveAmplitudeMin(), config.getWaveAmplitudeMax());
  }
  
  /**
   * Calculates the brightness value for a grid position (B in HSB).
   * 
   * This combines frame-based animation with position-based waves.
   * 
   * @param frameCount the current frame number
   * @param x the x grid coordinate
   * @param y the y grid coordinate
   * @param amplitude the pre-calculated amplitude value
   * @return brightness value in range (brightnessMin to brightnessMax)
   */
  public float calculateColor(int frameCount, int x, int y, float amplitude) {
    ensureUpdated(frameCount, config.getWaveSpeed());
    float angleRad = PApplet.radians(config.getWaveAngle());
    float dx = PApplet.cos(angleRad);
    float dy = PApplet.sin(angleRad);
    float spatial = (x * dx + y * dy) * waveMultiplier;
    float input = frameCount * config.getWaveSpeed() + spatial * amplitude;
    float value = PApplet.map(PApplet.tan(PApplet.radians(input)), -1, 1, 
                              config.getBrightnessMin(), config.getBrightnessMax());
    return PApplet.constrain(value, config.getBrightnessMin(), config.getBrightnessMax());
  }
  
  /**
   * Calculates brightness using a custom wave function if set, or the default.
   * 
   * @param frameCount the current frame number
   * @param x the x grid coordinate
   * @param y the y grid coordinate
   * @param tilesX total horizontal tiles
   * @param tilesY total vertical tiles
   * @return brightness value
   */
  public float calculateColorCustom(int frameCount, int x, int y, float tilesX, float tilesY) {
    if (customWaveFunction != null) {
      float normalizedX = x / tilesX;
      float normalizedY = y / tilesY;
      float time = frameCount / (float)(config.getAnimationFPS() * config.getAnimationDuration());
      return customWaveFunction.calculate(frameCount, normalizedX, normalizedY, time, config);
    }
    return calculateColor(frameCount, x, y, calculateAmplitude(x, y));
  }
  
  /**
   * Calculates the saturation value for a grid position (S in HSB).
   * 
   * <p>Uses a phase-offset wave so saturation varies independently of brightness.
   * When saturationMin equals saturationMax, returns that fixed value (no wave).</p>
   * 
   * @param frameCount the current frame number
   * @param x the x grid coordinate
   * @param y the y grid coordinate
   * @param tilesX total horizontal tiles
   * @param tilesY total vertical tiles
   * @return saturation value in range (saturationMin to saturationMax)
   */
  public float calculateSaturation(int frameCount, int x, int y, float tilesX, float tilesY) {
    float sMin = config.getSaturationMin();
    float sMax = config.getSaturationMax();
    if (sMin == sMax) return sMin;
    
    ensureUpdated(frameCount, config.getWaveSpeed());
    // Phase-offset wave: decorrelated from brightness using angle offset
    float angleRad = PApplet.radians(config.getWaveAngle() + 30);
    float dx = PApplet.cos(angleRad);
    float dy = PApplet.sin(angleRad);
    float input = frameCount * config.getWaveSpeed() * 0.7f + (x * dx + y * dy) * waveMultiplier * 1.3f;
    float value = PApplet.map(PApplet.sin(PApplet.radians(input)), -1, 1, sMin, sMax);
    return PApplet.constrain(value, sMin, sMax);
  }
  
  /**
   * Calculates the hue value for a grid position (H in HSB).
   * 
   * <p>Uses a slow-sweeping wave so hue shifts smoothly across the grid.
   * When hueMin equals hueMax, returns that fixed value (no wave).</p>
   * 
   * @param frameCount the current frame number
   * @param x the x grid coordinate
   * @param y the y grid coordinate
   * @param tilesX total horizontal tiles
   * @param tilesY total vertical tiles
   * @return hue value in range (hueMin to hueMax)
   */
  public float calculateHue(int frameCount, int x, int y, float tilesX, float tilesY) {
    float hMin = config.getHueMin();
    float hMax = config.getHueMax();
    if (hMin == hMax) return hMin;
    
    ensureUpdated(frameCount, config.getWaveSpeed());
    // Slow spatial sweep + gentle time evolution using wave angle
    float angleRad = PApplet.radians(config.getWaveAngle());
    float dx = PApplet.cos(angleRad);
    float dy = PApplet.sin(angleRad);
    float input = frameCount * config.getWaveSpeed() * 0.3f + (x * dx + y * dy) * waveMultiplier * 0.5f;
    float value = PApplet.map(PApplet.sin(PApplet.radians(input)), -1, 1, hMin, hMax);
    return PApplet.constrain(value, hMin, hMax);
  }
  
  /**
   * Sets a custom wave function for color calculations.
   * 
   * @param function the custom WaveFunction implementation, or null to use default
   */
  public void setCustomWaveFunction(WaveFunction function) {
    this.customWaveFunction = function;
  }
  
  /**
   * Gets the current custom wave function.
   * 
   * @return the custom WaveFunction, or null if using default
   */
  public WaveFunction getCustomWaveFunction() {
    return customWaveFunction;
  }
  
  /**
   * Gets the current wave multiplier value.
   * 
   * @return the wave multiplier calculated for current frame
   */
  public float getWaveMultiplier() {
    return waveMultiplier;
  }
  
  /**
   * Resets the engine to use default wave functions.
   */
  public void reset() {
    customWaveFunction = null;
  }
}
