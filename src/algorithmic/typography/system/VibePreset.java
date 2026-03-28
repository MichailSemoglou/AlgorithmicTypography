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
 * @version 0.3.0
 * @since 1.0.0
 */

package algorithmic.typography.system;

import algorithmic.typography.Configuration;
import algorithmic.typography.core.PerlinMotion;
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
    // Calm vibes — distinct pastel palettes, horizontal wave (0°), slow, long fade
    VIBE_MAP.put("calm",     new VibeParams(0.25f, 100, 210, 1.0f, 8, 8,
                                              0f, 240f, 270f, 20f, 60f, 3500));
    VIBE_MAP.put("zen",      new VibeParams(0.0f,  90, 200, 0.9f, 8, 8,
                                              0f, 100f, 140f, 15f, 50f, 4000));
    VIBE_MAP.put("peaceful", new VibeParams(0.3f, 100, 215, 1.2f, 10, 10,
                                              0f,  15f,  45f, 20f, 55f, 3000));
    VIBE_MAP.put("serene",   new VibeParams(0.15f,  80, 195, 0.8f, 6, 6,
                                              0f, 195f, 225f, 12f, 40f, 4500));
    
    // Energetic vibes — neon magenta-cyan palette, steep wave angle
    VIBE_MAP.put("energy", new VibeParams(4.0f, 150, 255, 5.0f, 32, 24,
                                           60f, 270f, 330f, 200f, 255f));
    VIBE_MAP.put("rave",   new VibeParams(5.0f, 180, 255, 6.0f, 40, 30,
                                           135f, 260f, 340f, 220f, 255f));
    VIBE_MAP.put("techno", new VibeParams(4.5f, 160, 255, 5.5f, 36, 28,
                                           120f, 240f, 320f, 210f, 255f));
    VIBE_MAP.put("hype",   new VibeParams(4.0f, 150, 255, 5.0f, 32, 24,
                                           90f, 280f, 350f, 200f, 255f));
    
    // Melancholic vibes — greyscale, asymmetric grids, distinct wave angles
    VIBE_MAP.put("melancholy", new VibeParams(0.4f,  10,  90, 2.0f, 18,  8,
                                               45f, 0f, 0f, 0f, 0f, 2000));
    VIBE_MAP.put("rain",       new VibeParams(0.6f,  20, 130, 2.5f,  8, 20,
                                              270f, 0f, 0f, 0f, 0f, 2000));
    VIBE_MAP.put("sad",        new VibeParams(0.2f,   0,  50, 1.5f,  6,  6,
                                              180f, 0f, 0f, 0f, 0f, 2000));
    VIBE_MAP.put("nostalgic",  new VibeParams(0.35f, 40, 160, 1.8f, 14, 10,
                                               45f, 0f, 0f, 0f, 0f, 2000));
    
    // Chaotic vibes — each with a distinct grid shape, angle and palette
    VIBE_MAP.put("chaos",   new VibeParams(6.0f,  80, 255,  8.0f, 48, 32,
                                             135f, 0f, 360f, 180f, 255f));  // full-spectrum, wide sweep
    VIBE_MAP.put("glitch",  new VibeParams(8.0f,   0, 255, 12.0f, 12, 48,
                                             270f, 160f, 320f, 200f, 255f)); // scan-line columns, cyan-magenta
    VIBE_MAP.put("noise",   new VibeParams(4.0f,  50, 255,  6.0f, 36, 36,
                                              45f,   0f,   0f,   0f,   0f)); // greyscale static, dense square
    VIBE_MAP.put("digital", new VibeParams(5.0f,  20, 255,  7.0f, 24, 16,
                                               0f, 100f, 140f, 200f, 255f)); // neon green terminal, horizontal
    
    // Ocean vibes — each keyword has a distinct grid, angle, speed, blue/magenta/purple palette, and dark background
    VIBE_MAP.put("ocean", new VibeParams(0.55f, 80, 210, 2.0f, 28, 14,
                                           90f, 190f, 220f, 150f, 220f, 2000,
                                             0,   8,  35)); // wide cinematic, horizontal swells, cyan-blue on deep navy
    VIBE_MAP.put("flow",  new VibeParams(0.4f,  70, 190, 1.5f, 18, 18,
                                          135f, 210f, 255f, 100f, 180f, 2000,
                                             5,   0,  28)); // square grid, diagonal drift, blue-indigo on dark indigo
    VIBE_MAP.put("wave",  new VibeParams(1.1f,  90, 235, 3.0f, 10, 28,
                                          270f, 170f, 310f, 180f, 255f, 2000,
                                             0,  12,  22)); // tall columns, fast, cyan-to-magenta on dark teal
    VIBE_MAP.put("water", new VibeParams(0.3f,  60, 180, 1.2f, 32, 10,
                                          315f, 220f, 270f,  80f, 160f, 2000,
                                             3,   0,  40)); // wide shallow, very slow, blue-purple on midnight
    
    // Minimal vibes — greyscale, no wave (multiplier=0), 1s fade, three distinct static grids
    VIBE_MAP.put("minimal", new VibeParams(0.4f,  80, 200, 0.0f,  6,  6,
                                             0f, 0f, 0f, 0f, 0f, 1000)); // tight 6×6, classic contrast
    VIBE_MAP.put("sparse",  new VibeParams(0.2f,  20, 240, 0.0f,  3,  3,
                                             0f, 0f, 0f, 0f, 0f, 1000)); // very open 3×3, high contrast, near-still
    VIBE_MAP.put("simple",  new VibeParams(0.6f,  60, 180, 0.0f, 12, 10,
                                             0f, 0f, 0f, 0f, 0f, 1000)); // wide 12×10, gentle character cycling
    
    // Light/dark vibes — distinct grids, angles, brightness ranges, and backgrounds
    VIBE_MAP.put("dark",   new VibeParams(0.7f,   0,  35, 1.5f, 16, 16,
                                           45f, 0f, 0f, 0f, 0f, 2000,
                                             0,   0,   0)); // pure dark, square grid, classic 45°
    VIBE_MAP.put("night",  new VibeParams(0.4f,   0,  60, 1.2f, 10, 20,
                                          270f, 0f, 0f, 0f, 0f, 2000,
                                             3,   3,  18)); // tall columns, vertical drift, cool near-black blue
    VIBE_MAP.put("bright", new VibeParams(2.5f, 180, 255, 2.5f, 24, 24,
                                           45f, 0f, 0f, 0f, 0f, 2000,
                                           255, 252, 240)); // fine grid, dark glyphs on warm white bg
    VIBE_MAP.put("day",    new VibeParams(1.8f, 140, 240, 2.0f, 20, 14,
                                           90f, 0f, 0f, 0f, 0f, 2000,
                                           245, 240, 220)); // wide landscape, horizontal sweep, cream bg
    VIBE_MAP.put("light",  new VibeParams(1.2f, 100, 210, 1.5f, 14,  8,
                                          135f, 0f, 0f, 0f, 0f, 2000,
                                          240, 238, 230)); // wide shallow tiles, diagonal, off-white bg
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
    final int fadeDuration;
    final int bgR;
    final int bgG;
    final int bgB;
    
    /** Greyscale preset (no color), default 2000ms fade, black background. */
    VibeParams(float waveSpeed, int briMin, int briMax, float waveMultiplierMax, int tilesX, int tilesY) {
      this(waveSpeed, briMin, briMax, waveMultiplierMax, tilesX, tilesY, 45f, 0f, 0f, 0f, 0f, 2000, 0, 0, 0);
    }
    
    /** Full preset with color, saturation and wave angle, default 2000ms fade, black background. */
    VibeParams(float waveSpeed, int briMin, int briMax, float waveMultiplierMax, int tilesX, int tilesY,
              float waveAngle, float hueMin, float hueMax, float satMin, float satMax) {
      this(waveSpeed, briMin, briMax, waveMultiplierMax, tilesX, tilesY, waveAngle, hueMin, hueMax, satMin, satMax, 2000, 0, 0, 0);
    }
    
    /** Full preset with color, saturation, wave angle, and explicit fade duration, black background. */
    VibeParams(float waveSpeed, int briMin, int briMax, float waveMultiplierMax, int tilesX, int tilesY,
              float waveAngle, float hueMin, float hueMax, float satMin, float satMax, int fadeDuration) {
      this(waveSpeed, briMin, briMax, waveMultiplierMax, tilesX, tilesY, waveAngle, hueMin, hueMax, satMin, satMax, fadeDuration, 0, 0, 0);
    }
    
    /** Full preset with color, saturation, wave angle, explicit fade duration, and custom background color. */
    VibeParams(float waveSpeed, int briMin, int briMax, float waveMultiplierMax, int tilesX, int tilesY,
              float waveAngle, float hueMin, float hueMax, float satMin, float satMax, int fadeDuration,
              int bgR, int bgG, int bgB) {
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
      this.fadeDuration = fadeDuration;
      this.bgR = bgR;
      this.bgG = bgG;
      this.bgB = bgB;
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
      config.setCellMotion(null);
      return;
    }
    
    String[] words = vibe.toLowerCase().split("\\s+");
    applyCompound(config, words);
  }
  
  // Chaotic vibe keywords that trigger Perlin motion
  private static final java.util.Set<String> CHAOTIC_WORDS = new java.util.HashSet<>(java.util.Arrays.asList(
    "chaos", "glitch", "noise", "digital"
  ));
  
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
    float fadeDurationSum = 0;
    float bgRSum = 0;
    float bgGSum = 0;
    float bgBSum = 0;
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
        fadeDurationSum += params.fadeDuration;
        bgRSum += params.bgR;
        bgGSum += params.bgG;
        bgBSum += params.bgB;
        matchedCount++;
      }
    }
    
    if (matchedCount == 0) {
      // No recognized vibes - use balanced default
      applyBalanced(config);
      config.setCellMotion(null);
      return;
    }
    
    // Apply averaged values
    config.setWaveSpeed(waveSpeedSum / matchedCount);
    config.setBrightnessRange(briMinSum / matchedCount, briMaxSum / matchedCount);
    config.setWaveMultiplierMax(waveMultiplierSum / matchedCount);
    config.setInitialTilesX(Math.round(tilesXSum / matchedCount));
    config.setInitialTilesY(Math.round(tilesYSum / matchedCount));
    config.setWaveAngle(waveAngleSum / matchedCount);
    config.setFadeDuration(Math.round(fadeDurationSum / matchedCount));
    config.setBackgroundColor(Math.round(bgRSum / matchedCount),
                              Math.round(bgGSum / matchedCount),
                              Math.round(bgBSum / matchedCount));
    
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
    
    // If any matched word is chaotic, use Perlin noise motion; otherwise clear it
    boolean usesPerlin = false;
    for (String word : words) {
      if (CHAOTIC_WORDS.contains(word)) {
        usesPerlin = true;
        break;
      }
    }
    config.setCellMotion(usesPerlin ? new PerlinMotion(14, 1.5f) : null);
    
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
    config.setWaveSpeed(0.25f);
    config.setBrightnessRange(100, 210);
    config.setWaveMultiplierMax(1.0f);
    config.setGridSize(8, 8);
    config.setChangedTilesX(4);
    config.setChangedTilesY(4);
    config.setWaveAngle(0f);
    config.setHueRange(240f, 270f);
    config.setSaturationMin(20f);
    config.setSaturationMax(60f);
    config.setFadeDuration(3500);
    config.setCellMotion(null);
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
    config.setCellMotion(null);
  }
  
  /**
   * Applies a melancholic, subdued preset.
   * 
   * @param config the configuration to modify
   */
  public static void applyMelancholic(Configuration config) {
    config.setWaveSpeed(0.4f);
    config.setBrightnessRange(10, 90);
    config.setWaveMultiplierMax(2.0f);
    config.setGridSize(18, 8);
    config.setChangedTilesX(9);
    config.setChangedTilesY(4);
    config.setWaveAngle(45f);
    config.setHueRange(0, 0);
    config.setSaturationMin(0);
    config.setSaturationMax(0);
    config.setFadeDuration(2000);
    config.setCellMotion(null);
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
    config.setCellMotion(new PerlinMotion(14, 1.5f));
  }
  
  /**
   * Applies an ocean-like, flowing preset.
   * 
   * @param config the configuration to modify
   */
  public static void applyOcean(Configuration config) {
    config.setWaveSpeed(0.55f);
    config.setBrightnessRange(80, 210);
    config.setWaveMultiplierMax(2.0f);
    config.setGridSize(28, 14);
    config.setChangedTilesX(14);
    config.setChangedTilesY(7);
    config.setWaveAngle(90f);
    config.setHueRange(190, 220);
    config.setSaturationMin(150);
    config.setSaturationMax(220);
    config.setBackgroundColor(0, 8, 35);
    config.setCellMotion(null);
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
    config.setCellMotion(null);
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
    config.setCellMotion(intensity > 0.7f ? new PerlinMotion(6 + (int)(intensity * 12), 1.0f + intensity * 0.5f) : null);
  }
}