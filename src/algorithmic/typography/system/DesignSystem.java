/**
 * DesignSystem - Cultural and systematic design presets.
 * 
 * <p>This class provides ready-to-use design systems inspired by various cultural
 * and historical typographic traditions. Each design system applies a coordinated
 * set of parameters that reflect its aesthetic philosophy.</p>
 * 
 * <p>Example usage:</p>
 * <pre>
 * Configuration config = new Configuration();
 * 
 * // Apply Swiss design system
 * DesignSystem.apply(config, DesignSystem.SystemType.SWISS);
 * 
 * // Or use the convenience method
 * DesignSystem.applySwiss(config);
 * </pre>
 * 
 * <h2>Available Design Systems</h2>
 * <ul>
 *   <li><b>SWISS</b> - International Typographic Style, clean grids, neutral colors</li>
 *   <li><b>BAUHAUS</b> - Geometric forms, primary colors, functional aesthetic</li>
 *   <li><b>CHINESE_INK</b> - Traditional brush calligraphy aesthetic, grayscale</li>
 *   <li><b>ARABIC_KUFI</b> - Geometric Kufic script influence, balanced grids</li>
 *   <li><b>JAPANESE_MINIMAL</b> - Zen simplicity, sparse arrangements, subtle</li>
 *   <li><b>DECONSTRUCT</b> - Postmodern deconstruction, chaotic energy</li>
 *   <li><b>BRUTALIST</b> - Raw, unpolished, static and bold</li>
 *   <li><b>MEMPHIS</b> - 1980s Italian design, playful, colorful</li>
 * </ul>
 * 
 * @author Michail Semoglou
 * @version 1.0.0
 * @since 1.0.0
 */

package algorithmic.typography.system;

import algorithmic.typography.Configuration;

/**
 * DesignSystem provides cultural and systematic design presets.
 * 
 * <p>Each system is carefully calibrated to reflect the aesthetic principles
 * of its namesake, including appropriate grid proportions, color ranges,
 * and animation behaviors.</p>
 */
public class DesignSystem {
  
  /**
   * Available design system types.
   * 
   * <p>Each type corresponds to a distinct aesthetic tradition:</p>
   * <ul>
   *   <li>SWISS - International Typographic Style (1950s-60s)</li>
   *   <li>BAUHAUS - German design school (1919-1933)</li>
   *   <li>CHINESE_INK - Traditional East Asian calligraphy</li>
   *   <li>ARABIC_KUFI - Early Islamic geometric script</li>
   *   <li>JAPANESE_MINIMAL - Zen Buddhist aesthetic</li>
   *   <li>DECONSTRUCT - Postmodern typography (1990s)</li>
   *   <li>BRUTALIST - Raw concrete architecture aesthetic</li>
   *   <li>MEMPHIS - Italian postmodern design (1980s)</li>
   * </ul>
   */
  public enum SystemType {
    /** International Typographic Style - clean, objective, grid-based */
    SWISS,
    /** Bauhaus - geometric, functional, primary colors */
    BAUHAUS, 
    /** Chinese ink painting aesthetic - grayscale, flowing */
    CHINESE_INK, 
    /** Arabic Kufic script - geometric, balanced */
    ARABIC_KUFI, 
    /** Japanese minimalism - sparse, serene, subtle */
    JAPANESE_MINIMAL, 
    /** Deconstructivism - chaotic, fragmented, experimental */
    DECONSTRUCT, 
    /** Brutalism - raw, bold, static, unpolished */
    BRUTALIST, 
    /** Memphis Group - playful, colorful, 1980s */
    MEMPHIS
  }
  
  /**
   * Applies a design system to the configuration.
   * 
   * <p>This is the main entry point for applying design presets. It selects
   * the appropriate preset based on the SystemType enum.</p>
   * 
   * @param config the configuration to modify (must not be null)
   * @param type the design system type to apply
   * @throws IllegalArgumentException if config is null
   * 
   * <pre>
   * Configuration config = new Configuration();
   * DesignSystem.apply(config, DesignSystem.SystemType.BAUHAUS);
   * </pre>
   */
  public static void apply(Configuration config, SystemType type) {
    if (config == null) {
      throw new IllegalArgumentException("Configuration cannot be null");
    }
    
    switch (type) {
      case SWISS: applySwiss(config); break;
      case BAUHAUS: applyBauhaus(config); break;
      case CHINESE_INK: applyChineseInk(config); break;
      case ARABIC_KUFI: applyArabicKufi(config); break;
      case JAPANESE_MINIMAL: applyJapaneseMinimal(config); break;
      case DECONSTRUCT: applyDeconstruct(config); break;
      case BRUTALIST: applyBrutalist(config); break;
      case MEMPHIS: applyMemphis(config); break;
    }
  }
  
  /**
   * Applies the Swiss International Typographic Style.
   * 
   * <p>Characteristics:</p>
   * <ul>
   *   <li>Asymmetric layouts with mathematical grid systems</li>
   *   <li>Objective photography and clean sans-serif typography</li>
   *   <li>Neutral colors with occasional bold accents</li>
   *   <li>Clear visual hierarchy</li>
   * </ul>
   * 
   * @param config the configuration to modify
   */
  public static void applySwiss(Configuration config) {
    config.setGridSize(20, 20);
    config.setChangedTilesX(12);
    config.setChangedTilesY(12);
    config.setBrightnessRange(120, 220);   // mid-range, restrained contrast
    config.setSaturationRange(0, 0);        // greyscale — pure Swiss neutrality
    config.setHueRange(0, 0);
    config.setWaveSpeed(0.8f);              // slow, precise movement
    config.setWaveMultiplierRange(0.2f, 1.0f);
    config.setWaveAmplitudeRange(-60, 60);
    config.setCharacter("A");
    config.setTextScale(0.75f);
  }
  
  /**
   * Applies the Bauhaus design aesthetic.
   * 
   * <p>Characteristics:</p>
   * <ul>
   *   <li>Geometric shapes: circle, square, triangle</li>
   *   <li>Primary colors: red, yellow, blue</li>
   *   <li>Functional, form-follows-function philosophy</li>
   *   <li>Grid-based but with geometric playfulness</li>
   * </ul>
   * 
   * @param config the configuration to modify
   */
  public static void applyBauhaus(Configuration config) {
    config.setGridSize(10, 10);
    config.setChangedTilesX(6);
    config.setChangedTilesY(6);
    config.setBrightnessRange(180, 255);   // bright, bold
    config.setSaturationRange(180, 255);    // fully saturated primaries
    config.setHueRange(0, 60);              // red-yellow primary palette
    config.setWaveSpeed(1.5f);              // deliberate rhythm
    config.setWaveMultiplierRange(0.8f, 3.0f);
    config.setWaveAmplitudeRange(-120, 120);
    config.setCharacter("A");
    config.setTextScale(0.9f);
  }
  
  /**
   * Applies the Chinese ink painting aesthetic.
   * 
   * <p>Characteristics:</p>
   * <ul>
   *   <li>Grayscale tones simulating ink wash</li>
   *   <li>Vertical emphasis reflecting traditional scroll format</li>
   *   <li>Flowing, organic movement</li>
   *   <li>Emphasis on negative space (ma)</li>
   * </ul>
   * 
   * @param config the configuration to modify
   */
  public static void applyChineseInk(Configuration config) {
    config.setGridSize(8, 16);            // vertical scroll proportion
    config.setChangedTilesX(4);
    config.setChangedTilesY(8);
    config.setBrightnessRange(60, 160);    // ink-wash mid-tones
    config.setSaturationRange(0, 30);       // near-greyscale ink wash
    config.setHueRange(0, 0);
    config.setWaveSpeed(0.4f);             // slow and meditative
    config.setWaveMultiplierRange(0.0f, 0.8f);
    config.setWaveAmplitudeRange(-50, 50);
    config.setCharacter("A");
    config.setTextScale(0.85f);
  }
  
  /**
   * Applies the Arabic Kufic geometric script aesthetic.
   * 
   * <p>Characteristics:</p>
   * <ul>
   *   <li>Geometric, angular letterforms</li>
   *   <li>Square proportion grid</li>
   *   <li>Balanced, symmetrical compositions</li>
   *   <li>Medium-paced flowing motion</li>
   * </ul>
   * 
   * @param config the configuration to modify
   */
  public static void applyArabicKufi(Configuration config) {
    config.setGridSize(16, 16);           // square, balanced
    config.setChangedTilesX(8);
    config.setChangedTilesY(8);
    config.setBrightnessRange(100, 230);
    config.setSaturationRange(150, 255);    // rich, jewel-tone colours
    config.setHueRange(180, 240);           // turquoise → blue
    config.setWaveSpeed(1.2f);             // flowing, structured
    config.setWaveMultiplierRange(0.5f, 2.5f);
    config.setWaveAmplitudeRange(-100, 100);
    config.setCharacter("A");
    config.setTextScale(0.8f);
  }
  
  /**
   * Applies the Japanese minimal aesthetic.
   * 
   * <p>Characteristics:</p>
   * <ul>
   *   <li>Extreme simplicity and sparse arrangements</li>
   *   <li>Limited color palette</li>
   *   <li>Emphasis on emptiness (ma)</li>
   *   <li>Very slow, subtle motion</li>
   * </ul>
   * 
   * @param config the configuration to modify
   */
  public static void applyJapaneseMinimal(Configuration config) {
    config.setGridSize(5, 5);              // very sparse
    config.setChangedTilesX(3);
    config.setChangedTilesY(3);
    config.setBrightnessRange(80, 140);     // subtle, muted
    config.setSaturationRange(0, 20);       // near monochrome
    config.setHueRange(0, 0);
    config.setWaveSpeed(0.2f);              // barely moving
    config.setWaveMultiplierRange(0.0f, 0.3f);
    config.setWaveAmplitudeRange(-30, 30);
    config.setCharacter("A");
    config.setTextScale(0.95f);
  }
  
  /**
   * Applies the Deconstructivist aesthetic.
   * 
   * <p>Characteristics:</p>
   * <ul>
   *   <li>Fragmented, layered compositions</li>
   *   <li>High energy and visual tension</li>
   *   <li>Dense, overlapping elements</li>
   *   <li>Fast, erratic motion</li>
   * </ul>
   * 
   * @param config the configuration to modify
   */
  public static void applyDeconstruct(Configuration config) {
    config.setGridSize(48, 48);            // very dense
    config.setChangedTilesX(32);
    config.setChangedTilesY(32);
    config.setBrightnessRange(30, 255);     // extreme contrast range
    config.setSaturationRange(100, 255);    // intense colour
    config.setHueRange(0, 360);             // full spectrum chaos
    config.setWaveSpeed(4.0f);              // fast, erratic
    config.setWaveMultiplierRange(2.0f, 7.0f);
    config.setWaveAmplitudeRange(-200, 200);
    config.setCharacter("A");
    config.setTextScale(0.65f);
  }
  
  /**
   * Applies the Brutalist aesthetic.
   * 
   * <p>Characteristics:</p>
   * <ul>
   *   <li>Raw, unpolished appearance</li>
   *   <li>High contrast, bold forms</li>
   *   <li>Static or minimal animation</li>
   *   <li>Monochromatic or limited palette</li>
   * </ul>
   * 
   * @param config the configuration to modify
   */
  public static void applyBrutalist(Configuration config) {
    config.setGridSize(14, 18);            // tall, imposing proportions
    config.setChangedTilesX(10);
    config.setChangedTilesY(12);
    config.setBrightnessRange(200, 255);    // hard-lit, high contrast
    config.setSaturationRange(0, 0);        // monochromatic
    config.setHueRange(0, 0);
    config.setWaveSpeed(0.1f);              // nearly static
    config.setWaveMultiplierRange(0.0f, 0.1f);
    config.setWaveAmplitudeRange(-10, 10);
    config.setCharacter("A");
    config.setTextScale(1.0f);
  }
  
  /**
   * Applies the Memphis Group (1980s) aesthetic.
   * 
   * <p>Characteristics:</p>
   * <ul>
   *   <li>Playful, colorful compositions</li>
   *   <li>Geometric shapes with personality</li>
   *   <li>Bright, saturated colors</li>
   *   <li>Energetic, bouncy motion</li>
   * </ul>
   * 
   * @param config the configuration to modify
   */
  public static void applyMemphis(Configuration config) {
    config.setGridSize(12, 9);
    config.setChangedTilesX(8);
    config.setChangedTilesY(6);
    config.setBrightnessRange(150, 255);    // vibrant, punchy
    config.setSaturationRange(200, 255);    // max saturation
    config.setHueRange(280, 360);           // pink → magenta pop
    config.setWaveSpeed(2.5f);              // energetic bounce
    config.setWaveMultiplierRange(1.5f, 5.0f);
    config.setWaveAmplitudeRange(-150, 150);
    config.setCharacter("A");
    config.setTextScale(0.85f);
  }
  
  /**
   * Gets a random design system.
   * 
   * @return a randomly selected SystemType
   */
  public static SystemType random() {
    SystemType[] types = SystemType.values();
    return types[(int)(Math.random() * types.length)];
  }
  
  /**
   * Gets a description of a design system.
   * 
   * @param type the design system type
   * @return human-readable description
   */
  public static String getDescription(SystemType type) {
    switch (type) {
      case SWISS: return "International Typographic Style - clean, objective, grid-based";
      case BAUHAUS: return "Bauhaus - geometric, functional, primary colors";
      case CHINESE_INK: return "Chinese Ink - traditional brush calligraphy aesthetic";
      case ARABIC_KUFI: return "Arabic Kufic - geometric, balanced script influence";
      case JAPANESE_MINIMAL: return "Japanese Minimal - Zen simplicity, sparse";
      case DECONSTRUCT: return "Deconstructivism - chaotic, fragmented, experimental";
      case BRUTALIST: return "Brutalism - raw, bold, static";
      case MEMPHIS: return "Memphis Group - playful, colorful, 1980s";
      default: return "Unknown design system";
    }
  }
}