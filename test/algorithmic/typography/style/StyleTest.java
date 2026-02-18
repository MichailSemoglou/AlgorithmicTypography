/**
 * JUnit tests for ColorPalette, DesignSystem, VibePreset, and Serendipity.
 */

package algorithmic.typography.style;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import algorithmic.typography.Configuration;

public class StyleTest {

  private Configuration config;

  @BeforeEach
  void setUp() {
    config = new Configuration();
  }

  // ── ColorPalette ──────────────────────────────────────────────

  @Test
  @DisplayName("apply() works for all Palette values")
  void testApplyAllPalettes() {
    for (ColorPalette.Palette palette : ColorPalette.Palette.values()) {
      Configuration c = new Configuration();
      assertDoesNotThrow(() -> ColorPalette.apply(c, palette),
          "apply failed for " + palette);
    }
  }

  @Test
  @DisplayName("apply() throws on null config")
  void testApplyNullConfig() {
    assertThrows(IllegalArgumentException.class,
        () -> ColorPalette.apply(null, ColorPalette.Palette.NEON_GLOW));
  }

  @Test
  @DisplayName("Each palette sets brightness values")
  void testPaletteSetsValues() {
    // After applying a palette, brightness range should differ from default
    Configuration defaultConfig = new Configuration();
    for (ColorPalette.Palette palette : ColorPalette.Palette.values()) {
      Configuration c = new Configuration();
      ColorPalette.apply(c, palette);
      // At least one brightness value should have changed
      boolean changed = c.getBrightnessMin() != defaultConfig.getBrightnessMin()
          || c.getBrightnessMax() != defaultConfig.getBrightnessMax();
      assertTrue(changed, "Palette " + palette + " didn't change brightness values");
    }
  }

  @Test
  @DisplayName("getDescription returns non-null for all palettes")
  void testPaletteDescriptions() {
    for (ColorPalette.Palette palette : ColorPalette.Palette.values()) {
      String desc = ColorPalette.getDescription(palette);
      assertNotNull(desc, "Description null for " + palette);
      assertFalse(desc.isEmpty(), "Description empty for " + palette);
    }
  }

  @Test
  @DisplayName("random() returns a valid Palette")
  void testPaletteRandom() {
    ColorPalette.Palette p = ColorPalette.random();
    assertNotNull(p);
  }

  @Test
  @DisplayName("NeonGlow sets expected ranges")
  void testNeonGlowValues() {
    ColorPalette.applyNeonGlow(config);
    assertTrue(config.getBrightnessMin() >= 200);
    assertTrue(config.getBrightnessMax() <= 255);
  }

  // ── Serendipity ───────────────────────────────────────────────

  @Test
  @DisplayName("Amount zero returns exact input")
  void testSerendipityZeroAmount() {
    Serendipity s = new Serendipity(0.0f);
    assertEquals(42.0f, s.addNoise(42.0f), 0.0001f);
    assertEquals(100.0f, s.addNoise(100.0f), 0.0001f);
  }

  @Test
  @DisplayName("Amount is clamped to [0, 1]")
  void testSerendipityAmountClamp() {
    Serendipity s1 = new Serendipity(-0.5f);
    assertEquals(0.0f, s1.getAmount(), 0.0001f);

    Serendipity s2 = new Serendipity(2.0f);
    assertEquals(1.0f, s2.getAmount(), 0.0001f);
  }

  @Test
  @DisplayName("setSeed makes output deterministic")
  void testSerendipitySeedDeterminism() {
    Serendipity s = new Serendipity(0.5f);
    s.setSeed(12345L);
    float v1 = s.addNoise(100.0f);
    s.setSeed(12345L);
    float v2 = s.addNoise(100.0f);
    assertEquals(v1, v2, 0.0001f);
  }

  @Test
  @DisplayName("setAmount updates the amount value")
  void testSetAmount() {
    Serendipity s = new Serendipity(0.0f);
    s.setAmount(0.7f);
    assertEquals(0.7f, s.getAmount(), 0.0001f);
  }

  @Test
  @DisplayName("getSeed returns current seed")
  void testGetSeed() {
    Serendipity s = new Serendipity(0.5f);
    s.setSeed(999L);
    assertEquals(999L, s.getSeed());
  }
}
