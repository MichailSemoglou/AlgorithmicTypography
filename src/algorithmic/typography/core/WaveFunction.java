/**
 * WaveFunction - Interface for custom wave calculations.
 * 
 * Implement this interface to create custom wave functions for
 * the AlgorithmicTypography system. Custom functions can be plugged
 * into the WaveEngine to replace or augment default wave calculations.
 * 
 * @author Michail Semoglou
 * @version 1.0.0
 */

package algorithmic.typography.core;

import algorithmic.typography.Configuration;

/**
 * Interface for custom wave function implementations.
 * 
 * <p>Custom wave functions allow users to define their own mathematical
 * transformations for brightness calculations. The function receives normalized
 * coordinates and time values, and should return a brightness value in the
 * range specified by the configuration's brightnessMin and brightnessMax.</p>
 * 
 * <p>Example implementation:</p>
 * <pre>
 * public class PerlinWave implements WaveFunction {
 *   public float calculate(int frameCount, float x, float y, float time, Configuration config) {
 *     float noise = noise(x * 10, y * 10, time);
 *     return map(noise, 0, 1, config.getBrightnessMin(), config.getBrightnessMax());
 *   }
 * }
 * </pre>
 */
public interface WaveFunction {
  
  /**
   * Calculates a color value based on position and time.
   * 
   * @param frameCount the current frame number
   * @param x normalized x coordinate (0.0 to 1.0)
   * @param y normalized y coordinate (0.0 to 1.0)
   * @param time normalized time (0.0 = start, 1.0 = end of animation)
   * @param config the current configuration
   * @return brightness value in range (brightnessMin to brightnessMax)
   */
  float calculate(int frameCount, float x, float y, float time, Configuration config);
  
  /**
   * Returns the name of this wave function.
   * 
   * @return human-readable name for the function
   */
  default String getName() {
    return getClass().getSimpleName();
  }
  
  /**
   * Returns a description of this wave function.
   * 
   * @return description of what the function does
   */
  default String getDescription() {
    return "Custom wave function";
  }
}
