/**
 * ColorPalette - Experimental color presets for kinetic typography.
 *
 * <p>This class provides vibrant, high-impact color palettes suitable for
 * motion graphics, live performances, and experimental design. Each palette
 * is optimized for visual impact in animated typography contexts.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * Configuration config = new Configuration();
 * 
 * // Apply a neon glow palette
 * ColorPalette.apply(config, ColorPalette.Palette.NEON_GLOW);
 * 
 * // Or apply directly
 * ColorPalette.applyNeonGlow(config);
 * </pre>
 *
 * <h2>Available Palettes</h2>
 * <ul>
 *   <li><b>NEON_GLOW</b> - Fluorescent colors on black background</li>
 *   <li><b>RAVE_ENERGY</b> - High contrast primaries for club visuals</li>
 *   <li><b>DIGITAL_GLITCH</b> - RGB separation, corrupted data aesthetic</li>
 *   <li><b>ACID_BRIGHT</b> - Toxic greens and yellows, high visibility</li>
 *   <li><b>RAW_PUNK</b> - Black, white, blood red, anarchist aesthetic</li>
 *   <li><b>SWISS_CLASSIC</b> - Muted neutrals, timeless elegance</li>
 *   <li><b>BAUHAUS_PRIMARY</b> - Red, yellow, blue, classic modernism</li>
 *   <li><b>JAPANESE_INK</b> - Sumi ink gradients, subtle grayscale</li>
 *   <li><b>ARABIC_GOLD</b> - Gold, lapis lazuli, turquoise</li>
 *   <li><b>MEMPHIS_80S</b> - Pastel neons, playful retro</li>
 * </ul>
 *
 * @author Michail Semoglou
 * @version 1.0.0
 * @since 1.0.0
 */

package algorithmic.typography.style;

import algorithmic.typography.Configuration;

/**
 * ColorPalette provides experimental color schemes for typography animation.
 *
 * <p>Each palette is defined by saturation and wave amplitude parameters that
 * work together to create distinctive visual effects. The palettes are designed
 * for grayscale animation where color values control brightness/intensity.</p>
 */
public class ColorPalette {

  /**
   * Available color palette types.
   * 
   * <p>Each palette is optimized for specific use cases:</p>
   * <ul>
   *   <li>Performance/club: NEON_GLOW, RAVE_ENERGY, ACID_BRIGHT</li>
   *   <li>Experimental: DIGITAL_GLITCH, RAW_PUNK</li>
   *   <li>Classic design: SWISS_CLASSIC, BAUHAUS_PRIMARY</li>
   *   <li>Cultural: JAPANESE_INK, ARABIC_GOLD</li>
   *   <li>Retro: MEMPHIS_80S</li>
   * </ul>
   */
  public enum Palette {
    /** Fluorescent on black - cyberpunk aesthetic */
    NEON_GLOW,
    /** High contrast primaries - club/rave visuals */
    RAVE_ENERGY,
    /** RGB separation - corrupted digital aesthetic */
    DIGITAL_GLITCH,
    /** Toxic greens and yellows - high visibility */
    ACID_BRIGHT,
    /** Black, white, blood red - anarchist punk */
    RAW_PUNK,
    /** Muted neutrals - timeless elegance */
    SWISS_CLASSIC,
    /** Red, yellow, blue - classic modernism */
    BAUHAUS_PRIMARY,
    /** Sumi ink gradients - subtle grayscale */
    JAPANESE_INK,
    /** Gold, lapis, turquoise - Islamic art influence */
    ARABIC_GOLD,
    /** Pastel neons - playful 1980s retro */
    MEMPHIS_80S
  }

  /**
   * Applies a color palette to the configuration.
   *
   * <p>This is the main entry point for applying color presets. Each palette
   * sets the saturation range and wave amplitude parameters to create a
   * distinctive visual effect.</p>
   *
   * @param config the configuration to modify (must not be null)
   * @param palette the palette type to apply
   * @throws IllegalArgumentException if config is null
   *
   * <pre>
   * Configuration config = new Configuration();
   * ColorPalette.apply(config, ColorPalette.Palette.NEON_GLOW);
   * </pre>
   */
  public static void apply(Configuration config, Palette palette) {
    if (config == null) {
      throw new IllegalArgumentException("Configuration cannot be null");
    }
    
    switch (palette) {
      case NEON_GLOW: applyNeonGlow(config); break;
      case RAVE_ENERGY: applyRaveEnergy(config); break;
      case DIGITAL_GLITCH: applyDigitalGlitch(config); break;
      case ACID_BRIGHT: applyAcidBright(config); break;
      case RAW_PUNK: applyRawPunk(config); break;
      case SWISS_CLASSIC: applySwissClassic(config); break;
      case BAUHAUS_PRIMARY: applyBauhausPrimary(config); break;
      case JAPANESE_INK: applyJapaneseInk(config); break;
      case ARABIC_GOLD: applyArabicGold(config); break;
      case MEMPHIS_80S: applyMemphis80s(config); break;
    }
  }

  /**
   * Applies the Neon Glow palette.
   * 
   * <p>Characteristics:</p>
   * <ul>
   *   <li>High saturation for fluorescent effect</li>
   *   <li>Medium amplitude for subtle pulsing</li>
   *   <li>Best for dark backgrounds</li>
   * </ul>
   * 
   * @param config the configuration to modify
   */
  public static void applyNeonGlow(Configuration config) {
    config.setBrightnessRange(200, 255);
    config.setWaveAmplitudeRange(-100, 255);
  }

  /**
   * Applies the Rave Energy palette.
   * 
   * <p>Characteristics:</p>
   * <ul>
   *   <li>Full saturation range for high contrast</li>
   *   <li>Wide amplitude for intense variation</li>
   *   <li>Optimized for club/projection environments</li>
   * </ul>
   * 
   * @param config the configuration to modify
   */
  public static void applyRaveEnergy(Configuration config) {
    config.setBrightnessRange(0, 255);
    config.setWaveAmplitudeRange(-255, 255);
  }

  /**
   * Applies the Digital Glitch palette.
   * 
   * <p>Characteristics:</p>
   * <ul>
   *   <li>High base saturation with variation</li>
   *   <li>Asymmetric amplitude for glitch effect</li>
   *   <li>Creates RGB separation-like patterns</li>
   * </ul>
   * 
   * @param config the configuration to modify
   */
  public static void applyDigitalGlitch(Configuration config) {
    config.setBrightnessRange(100, 255);
    config.setWaveAmplitudeRange(-200, 200);
  }

  /**
   * Applies the Acid Bright palette.
   * 
   * <p>Characteristics:</p>
   * <ul>
   *   <li>Very high saturation for toxic effect</li>
   *   <li>Positive-biased amplitude</li>
   *   <li>High visibility in any environment</li>
   * </ul>
   * 
   * @param config the configuration to modify
   */
  public static void applyAcidBright(Configuration config) {
    config.setBrightnessRange(150, 255);
    config.setWaveAmplitudeRange(50, 200);
  }

  /**
   * Applies the Raw Punk palette.
   * 
   * <p>Characteristics:</p>
   * <ul>
   *   <li>Low saturation for stark monochrome</li>
   *   <li>Negative amplitude for dark, aggressive feel</li>
   *   <li>High contrast black/white/red aesthetic</li>
   * </ul>
   * 
   * @param config the configuration to modify
   */
  public static void applyRawPunk(Configuration config) {
    config.setBrightnessRange(0, 50);
    config.setWaveAmplitudeRange(-255, 0);
  }

  /**
   * Applies the Swiss Classic palette.
   * 
   * <p>Characteristics:</p>
   * <ul>
   *   <li>Muted saturation for elegance</li>
   *   <li>Balanced amplitude for subtlety</li>
   *   <li>Timeless, professional appearance</li>
   * </ul>
   * 
   * @param config the configuration to modify
   */
  public static void applySwissClassic(Configuration config) {
    config.setBrightnessRange(0, 80);
    config.setWaveAmplitudeRange(-100, 100);
  }

  /**
   * Applies the Bauhaus Primary palette.
   * 
   * <p>Characteristics:</p>
   * <ul>
   *   <li>High saturation for primary colors</li>
   *   <li>Medium amplitude for geometric feel</li>
   *   <li>Classic modernist aesthetic</li>
   * </ul>
   * 
   * @param config the configuration to modify
   */
  public static void applyBauhausPrimary(Configuration config) {
    config.setBrightnessRange(180, 255);
    config.setWaveAmplitudeRange(-150, 150);
  }

  /**
   * Applies the Japanese Ink palette.
   * 
   * <p>Characteristics:</p>
   * <ul>
   *   <li>Very low saturation for ink wash effect</li>
   *   <li>Asymmetric amplitude for brush-like variation</li>
   *   <li>Subtle, meditative aesthetic</li>
   * </ul>
   * 
   * @param config the configuration to modify
   */
  public static void applyJapaneseInk(Configuration config) {
    config.setBrightnessRange(0, 60);
    config.setWaveAmplitudeRange(-200, 50);
  }

  /**
   * Applies the Arabic Gold palette.
   * 
   * <p>Characteristics:</p>
   * <ul>
   *   <li>Medium-high saturation for metallic effect</li>
   *   <li>Positive-biased amplitude for warmth</li>
   *   <li>Islamic art and calligraphy influence</li>
   * </ul>
   * 
   * @param config the configuration to modify
   */
  public static void applyArabicGold(Configuration config) {
    config.setBrightnessRange(120, 255);
    config.setWaveAmplitudeRange(-100, 200);
  }

  /**
   * Applies the Memphis 80s palette.
   * 
   * <p>Characteristics:</p>
   * <ul>
   *   <li>Medium-high saturation for playful colors</li>
   *   <li>Balanced amplitude for bouncy feel</li>
   *   <li>Retro 1980s Memphis Group aesthetic</li>
   * </ul>
   * 
   * @param config the configuration to modify
   */
  public static void applyMemphis80s(Configuration config) {
    config.setBrightnessRange(100, 255);
    config.setWaveAmplitudeRange(-80, 180);
  }
  
  /**
   * Gets a random palette.
   * 
   * @return a randomly selected Palette
   */
  public static Palette random() {
    Palette[] palettes = Palette.values();
    return palettes[(int)(Math.random() * palettes.length)];
  }
  
  /**
   * Gets a description of a palette.
   * 
   * @param palette the palette type
   * @return human-readable description
   */
  public static String getDescription(Palette palette) {
    switch (palette) {
      case NEON_GLOW: return "Fluorescent on black - cyberpunk aesthetic";
      case RAVE_ENERGY: return "High contrast primaries - club/rave visuals";
      case DIGITAL_GLITCH: return "RGB separation - corrupted digital aesthetic";
      case ACID_BRIGHT: return "Toxic greens and yellows - high visibility";
      case RAW_PUNK: return "Black, white, blood red - anarchist punk";
      case SWISS_CLASSIC: return "Muted neutrals - timeless elegance";
      case BAUHAUS_PRIMARY: return "Red, yellow, blue - classic modernism";
      case JAPANESE_INK: return "Sumi ink gradients - subtle grayscale";
      case ARABIC_GOLD: return "Gold, lapis, turquoise - Islamic art influence";
      case MEMPHIS_80S: return "Pastel neons - playful 1980s retro";
      default: return "Unknown palette";
    }
  }
}