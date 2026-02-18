/**
 * VibePreset - Natural language to animation parameters.
 * 
 * <p>This class enables "vibe coding" - using natural language descriptions
 * to configure animation parameters. It parses vibe strings and applies
 * compound effects when multiple vibes are detected.</p>
 * 
 * <p>Example usage:</p>
 * <pre>
 * Configuration config = new Configuration();
 * 
 * // Single vibe
 * VibePreset.apply(config, "calm");
 * 
 * // Compound vibes (effects are combined)
 * VibePreset.apply(config, "melancholic rain at night");
 * VibePreset.apply(config, "techno rave energy chaos");
 * </pre>
 * 
 * <h2>Available Vibe Keywords</h2>
 * <table border="1">
 *   <tr><th>Keyword</th><th>Effect</th></tr>
 *   <tr><td>calm, zen</td><td>Slow waves, muted colors, sparse grid</td></tr>
 *   <tr><td>energy, rave, techno</td><td>Fast waves, vibrant colors, dense grid</td></tr>
 *   <tr><td>melancholy, rain, sad</td><td>Slow waves, grayscale, medium grid</td></tr>
 *   <tr><td>chaos, glitch, noise</td><td>Very fast waves, extreme variation, dense grid</td></tr>
 *   <tr><td>ocean, flow, wave</td><td>Medium waves, blue-green tones, flowing grid</td></tr>
 *   <tr><td>minimal, sparse</td><td>Slow waves, limited colors, sparse grid</td></tr>
 *   <tr><td>dark, night</td><td>Dark tones, slow waves</td></tr>
 *   <tr><td>bright, day, light</td><td>Light tones, medium waves</td></tr>
 * </table>
 * 
 * @author Michail Semoglou
 * @version 1.0.0
 * @since 1.0.0
 */

package algorithmic.typography.system;

import algorithmic.typography.Configuration;
import java.util.HashMap;
import java.util.Map;

/**
 * VibePreset enables natural language configuration.
 * 
 * <p>The class uses a weighted blending system to combine multiple vibes,
 * allowing for nuanced and expressive configuration through simple text
 * descriptions.</p>
 */
public class VibePreset {
  
  // Vibe parameter presets
  private static final Map<String, VibeParams> VIBE_MAP = new HashMap<>();
  
  static {
    // Calm vibes
    VIBE_MAP.put("calm", new VibeParams(0.5f, 20, 100, 1.0f, 8, 8));
    VIBE_MAP.put("zen", new VibeParams(0.5f, 20, 100, 1.0f, 8, 8));
    VIBE_MAP.put("peaceful", new VibeParams(0.6f, 30, 120, 1.2f, 10, 10));
    VIBE_MAP.put("serene", new VibeParams(0.4f, 15, 80, 0.8f, 6, 6));
    
    // Energetic vibes — neon magenta-cyan palette, steep wave angle
    VIBE_MAP.put("energy", new VibeParams(4.0f, 150, 255, 5.0f, 32, 24,
                                           60f, 270f, 330f, 200f, 255f));
    VIBE_MAP.put("rave",   new VibeParams(5.0f, 180, 255, 6.0f, 40, 30,
                                           135f, 260f, 340f, 220f, 255f));
    VIBE_MAP.put("techno", new VibeParams(4.5f, 160, 255, 5.5f, 36, 28,
                                           120f, 240f, 320f, 210f, 255f));
    VIBE_MAP.put("hype",   new VibeParams(4.0f, 150, 255, 5.0f, 32, 24,
                                           90f, 280f, 350f, 200f, 255f));
    
    // Melancholic vibes
    VIBE_MAP.put("melancholy", new VibeParams(0.8f, 0, 60, 2.0f, 16, 12));
    VIBE_MAP.put("rain", new VibeParams(0.8f, 0, 60, 2.0f, 16, 12));
    VIBE_MAP.put("sad", new VibeParams(0.7f, 0, 50, 1.8f, 14, 10));
    VIBE_MAP.put("nostalgic", new VibeParams(0.9f, 20, 80, 1.5f, 12, 12));
    
    // Chaotic vibes — full-spectrum rainbow, extreme angle
    VIBE_MAP.put("chaos",   new VibeParams(6.0f, 100, 255, 8.0f, 48, 48,
                                             200f, 0f, 360f, 180f, 255f));
    VIBE_MAP.put("glitch",  new VibeParams(7.0f, 80, 255, 10.0f, 64, 48,
                                             225f, 80f, 200f, 150f, 255f));
    VIBE_MAP.put("noise",   new VibeParams(5.5f, 120, 255, 7.0f, 40, 40,
                                             180f, 30f, 360f, 160f, 255f));
    VIBE_MAP.put("digital", new VibeParams(6.0f, 100, 255, 8.0f, 48, 48,
                                             210f, 150f, 270f, 180f, 255f));
    
    // Ocean vibes — 90° wave angle for horizontal rolling waves
    VIBE_MAP.put("ocean", new VibeParams(0.7f, 80, 200, 2.0f, 24, 16,
                                          90f, 0f, 0f, 0f, 0f));
    VIBE_MAP.put("flow",  new VibeParams(0.7f, 80, 200, 2.0f, 24, 16,
                                          90f, 0f, 0f, 0f, 0f));
    VIBE_MAP.put("wave",  new VibeParams(0.9f, 90, 220, 2.5f, 28, 18,
                                          90f, 0f, 0f, 0f, 0f));
    VIBE_MAP.put("water", new VibeParams(0.8f, 70, 180, 2.2f, 22, 16,
                                          90f, 0f, 0f, 0f, 0f));
    
    // Minimal vibes
    VIBE_MAP.put("minimal", new VibeParams(0.6f, 30, 100, 1.0f, 6, 6));
    VIBE_MAP.put("sparse", new VibeParams(0.5f, 20, 80, 0.8f, 5, 5));
    VIBE_MAP.put("simple", new VibeParams(0.7f, 40, 120, 1.2f, 8, 8));
    
    // Light/dark vibes
    VIBE_MAP.put("dark", new VibeParams(0.8f, 0, 40, 1.5f, 16, 16));
    VIBE_MAP.put("night", new VibeParams(0.7f, 0, 50, 1.3f, 14, 14));
    VIBE_MAP.put("bright", new VibeParams(2.0f, 100, 255, 2.5f, 20, 20));
    VIBE_MAP.put("day", new VibeParams(2.0f, 100, 255, 2.5f, 20, 20));
    VIBE_MAP.put("light", new VibeParams(1.8f, 90, 255, 2.3f, 18, 18));
  }
  
  /**
   * Internal class to hold vibe parameters.
   */
  private static class VibeParams {
    final float waveSpeed;
    final int briMin;
    final int briMax;
    final float waveMultiplierMax;
    final int tilesX;
    final int tilesY;
    final float waveAngle;
    final float hueMin;
    final float hueMax;
    final float satMin;
    final float satMax;
    
    /** Greyscale preset (no color). */
    VibeParams(float waveSpeed, int briMin, int briMax, float waveMultiplierMax, int tilesX, int tilesY) {
      this(waveSpeed, briMin, briMax, waveMultiplierMax, tilesX, tilesY, 45f, 0f, 0f, 0f, 0f);
    }
    
    /** Full preset with color, saturation and wave angle. */
    VibeParams(float waveSpeed, int briMin, int briMax, float waveMultiplierMax, int tilesX, int tilesY,
              float waveAngle, float hueMin, float hueMax, float satMin, float satMax) {
      this.waveSpeed = waveSpeed;
      this.briMin = briMin;
      this.briMax = briMax;
      this.waveMultiplierMax = waveMultiplierMax;
      this.tilesX = tilesX;
      this.tilesY = tilesY;
      this.waveAngle = waveAngle;
      this.hueMin = hueMin;
      this.hueMax = hueMax;
      this.satMin = satMin;
      this.satMax = satMax;
    }
  }
  
  /**
   * Applies a vibe description to a configuration.
   * 
   * <p>This method parses the vibe string, looking for known keywords and
   * blending their parameters together. Multiple keywords can be combined
   * for nuanced effects.</p>
   * 
   * @param config the configuration to modify (must not be null)
   * @param vibe the vibe description string (e.g., "calm zen morning" or "chaos glitch")
   * @throws IllegalArgumentException if config is null
   */
  public static void apply(Configuration config, String vibe) {
    if (config == null) {
      throw new IllegalArgumentException("Configuration cannot be null");
    }
    if (vibe == null || vibe.trim().isEmpty()) {
      applyBalanced(config);
      return;
    }
    
    String[] words = vibe.toLowerCase().split("\\s+");
    applyCompound(config, words);
  }
  
  /**
   * Applies multiple vibes by blending their parameters.
   * 
   * @param config the configuration to modify
   * @param words array of vibe keywords
   */
  private static void applyCompound(Configuration config, String[] words) {
    float totalWeight = 0;
    float waveSpeedSum = 0;
    float briMinSum = 0;
    float briMaxSum = 0;
    float waveMultiplierSum = 0;
    float tilesXSum = 0;
    float tilesYSum = 0;
    float waveAngleSum = 0;
    float hueMinSum = 0;
    float hueMaxSum = 0;
    float satMinSum = 0;
    float satMaxSum = 0;
    int matchedCount = 0;
    
    for (String word : words) {
      VibeParams params = VIBE_MAP.get(word);
      if (params != null) {
        totalWeight += 1;
        waveSpeedSum += params.waveSpeed;
        briMinSum += params.briMin;
        briMaxSum += params.briMax;
        waveMultiplierSum += params.waveMultiplierMax;
        tilesXSum += params.tilesX;
        tilesYSum += params.tilesY;
        waveAngleSum += params.waveAngle;
        hueMinSum += params.hueMin;
        hueMaxSum += params.hueMax;
        satMinSum += params.satMin;
        satMaxSum += params.satMax;
        matchedCount++;
      }
    }
    
    if (matchedCount == 0) {
      // No recognized vibes - use balanced default
      applyBalanced(config);
      return;
    }
    
    // Apply averaged values
    config.setWaveSpeed(waveSpeedSum / matchedCount);
    config.setBrightnessRange(briMinSum / matchedCount, briMaxSum / matchedCount);
    config.setWaveMultiplierMax(waveMultiplierSum / matchedCount);
    config.setInitialTilesX(Math.round(tilesXSum / matchedCount));
    config.setInitialTilesY(Math.round(tilesYSum / matchedCount));
    config.setWaveAngle(waveAngleSum / matchedCount);
    
    // Apply color (hue/saturation) — only when at least one vibe defines a hue range
    float avgHueMin = hueMinSum / matchedCount;
    float avgHueMax = hueMaxSum / matchedCount;
    if (avgHueMin != avgHueMax) {
      config.setHueRange(avgHueMin, avgHueMax);
      config.setSaturationMin(satMinSum / matchedCount);
      config.setSaturationMax(satMaxSum / matchedCount);
    } else {
      // Reset to greyscale
      config.setHueRange(0, 0);
      config.setSaturationMin(0);
      config.setSaturationMax(0);
    }
    
    // Set changed tiles to be different from initial
    config.setChangedTilesX(Math.max(4, config.getInitialTilesX() / 2));
    config.setChangedTilesY(Math.max(4, config.getInitialTilesY() / 2));
  }
  
  /**
   * Applies a calm, peaceful preset.
   * 
   * @param config the configuration to modify
   */
  public static void applyCalm(Configuration config) {
    config.setWaveSpeed(0.5f);
    config.setBrightnessRange(20, 100);
    config.setWaveMultiplierMax(1.0f);
    config.setGridSize(8, 8);
    config.setChangedTilesX(4);
    config.setChangedTilesY(4);
    config.setWaveAngle(45f);
    config.setHueRange(0, 0);
    config.setSaturationMin(0);
    config.setSaturationMax(0);
  }
  
  /**
   * Applies an energetic, high-intensity preset.
   * 
   * @param config the configuration to modify
   */
  public static void applyEnergetic(Configuration config) {
    config.setWaveSpeed(4.0f);
    config.setBrightnessRange(150, 255);
    config.setWaveMultiplierMax(5.0f);
    config.setGridSize(32, 24);
    config.setChangedTilesX(16);
    config.setChangedTilesY(12);
    config.setWaveAngle(60f);
    config.setHueRange(270f, 330f);
    config.setSaturationMin(200f);
    config.setSaturationMax(255f);
  }
  
  /**
   * Applies a melancholic, subdued preset.
   * 
   * @param config the configuration to modify
   */
  public static void applyMelancholic(Configuration config) {
    config.setWaveSpeed(0.8f);
    config.setBrightnessRange(0, 60);
    config.setWaveMultiplierMax(2.0f);
    config.setGridSize(16, 12);
    config.setChangedTilesX(8);
    config.setChangedTilesY(6);
    config.setWaveAngle(45f);
    config.setHueRange(0, 0);
    config.setSaturationMin(0);
    config.setSaturationMax(0);
  }
  
  /**
   * Applies a chaotic, glitchy preset.
   * 
   * @param config the configuration to modify
   */
  public static void applyChaos(Configuration config) {
    config.setWaveSpeed(6.0f);
    config.setBrightnessRange(100, 255);
    config.setWaveMultiplierMax(8.0f);
    config.setGridSize(48, 48);
    config.setChangedTilesX(32);
    config.setChangedTilesY(32);
    config.setWaveAngle(200f);
    config.setHueRange(0f, 360f);
    config.setSaturationMin(180f);
    config.setSaturationMax(255f);
  }
  
  /**
   * Applies an ocean-like, flowing preset.
   * 
   * @param config the configuration to modify
   */
  public static void applyOcean(Configuration config) {
    config.setWaveSpeed(0.7f);
    config.setBrightnessRange(80, 200);
    config.setWaveMultiplierMax(2.0f);
    config.setGridSize(24, 16);
    config.setChangedTilesX(12);
    config.setChangedTilesY(8);
    config.setWaveAngle(90f);
    config.setHueRange(0, 0);
    config.setSaturationMin(0);
    config.setSaturationMax(0);
  }
  
  /**
   * Applies a balanced, neutral preset.
   * 
   * @param config the configuration to modify
   */
  public static void applyBalanced(Configuration config) {
    config.setWaveSpeed(2.0f);
    config.setBrightnessRange(50, 200);
    config.setWaveMultiplierMax(2.0f);
    config.setGridSize(16, 16);
    config.setChangedTilesX(8);
    config.setChangedTilesY(8);
    config.setWaveAngle(45f);
    config.setHueRange(0, 0);
    config.setSaturationMin(0);
    config.setSaturationMax(0);
  }
  
  /**
   * Checks if a vibe keyword is recognized.
   * 
   * @param keyword the vibe keyword to check
   * @return true if the keyword is a recognized vibe
   */
  public static boolean isValidVibe(String keyword) {
    return VIBE_MAP.containsKey(keyword.toLowerCase());
  }
  
  /**
   * Gets all recognized vibe keywords.
   * 
   * @return array of valid vibe keywords
   */
  public static String[] getValidVibes() {
    return VIBE_MAP.keySet().toArray(new String[0]);
  }
  
  /**
   * Creates a random vibe combination.
   * 
   * @param config the configuration to modify
   * @param intensity intensity level (0.0 = calm, 1.0 = chaotic)
   */
  public static void applyRandom(Configuration config, float intensity) {
    intensity = Math.max(0, Math.min(1, intensity));
    
    // Interpolate between calm and chaos based on intensity
    config.setWaveSpeed(0.5f + intensity * 5.5f);  // 0.5 to 6.0
    config.setBrightnessRange(
      (int)(20 + intensity * 130),  // 20 to 150
      255
    );
    config.setWaveMultiplierMax(1.0f + intensity * 7.0f);  // 1.0 to 8.0
    int tiles = (int)(8 + intensity * 40);  // 8 to 48
    config.setGridSize(tiles, tiles);
    config.setChangedTilesX(tiles / 2);
    config.setChangedTilesY(tiles / 2);
  }
}