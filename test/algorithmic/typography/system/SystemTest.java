/**
 * JUnit tests for DesignSystem and VibePreset.
 */

package algorithmic.typography.system;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import algorithmic.typography.Configuration;

public class SystemTest {

  private Configuration config;

  @BeforeEach
  void setUp() {
    config = new Configuration();
  }

  // ── DesignSystem ──────────────────────────────────────────────

  @Test
  @DisplayName("apply() works for all SystemType values")
  void testApplyAllSystemTypes() {
    for (DesignSystem.SystemType type : DesignSystem.SystemType.values()) {
      Configuration c = new Configuration();
      assertDoesNotThrow(() -> DesignSystem.apply(c, type),
          "apply failed for " + type);
    }
  }

  @Test
  @DisplayName("apply() throws on null config")
  void testDesignSystemNullConfig() {
    assertThrows(IllegalArgumentException.class,
        () -> DesignSystem.apply(null, DesignSystem.SystemType.SWISS));
  }

  @Test
  @DisplayName("Each system type modifies configuration")
  void testSystemTypeModifiesConfig() {
    for (DesignSystem.SystemType type : DesignSystem.SystemType.values()) {
      Configuration c = new Configuration();
      Configuration before = c.copy();
      DesignSystem.apply(c, type);
      // Wave speed or grid or brightness should change
      boolean changed = c.getWaveSpeed() != before.getWaveSpeed()
          || c.getInitialTilesX() != before.getInitialTilesX()
          || c.getBrightnessMin() != before.getBrightnessMin();
      assertTrue(changed, "SystemType " + type + " didn't modify config");
    }
  }

  @Test
  @DisplayName("getDescription returns non-null for all types")
  void testSystemDescriptions() {
    for (DesignSystem.SystemType type : DesignSystem.SystemType.values()) {
      String desc = DesignSystem.getDescription(type);
      assertNotNull(desc, "Description null for " + type);
      assertFalse(desc.isEmpty(), "Description empty for " + type);
    }
  }

  @Test
  @DisplayName("random() returns a valid SystemType")
  void testSystemRandom() {
    DesignSystem.SystemType type = DesignSystem.random();
    assertNotNull(type);
  }

  @Test
  @DisplayName("Swiss preset sets expected grid and monochrome")
  void testSwissPreset() {
    DesignSystem.applySwiss(config);
    assertEquals(20, config.getInitialTilesX());
    assertEquals(20, config.getInitialTilesY());
    // Swiss is greyscale: saturation 0-0
    assertEquals(0, config.getSaturationMin(), 0.01f);
    assertEquals(0, config.getSaturationMax(), 0.01f);
  }

  @Test
  @DisplayName("Bauhaus preset sets expected vivid parameters")
  void testBauhausPreset() {
    DesignSystem.applyBauhaus(config);
    assertEquals(10, config.getInitialTilesX());
    assertEquals(10, config.getInitialTilesY());
    assertTrue(config.getSaturationMin() > 0);
  }

  // ── VibePreset ────────────────────────────────────────────────

  @Test
  @DisplayName("apply with 'calm' keyword sets low speed")
  void testVibeCalmSpeed() {
    VibePreset.apply(config, "calm");
    assertTrue(config.getWaveSpeed() <= 1.0f,
        "Calm speed should be low, got " + config.getWaveSpeed());
  }

  @Test
  @DisplayName("apply with 'chaos' keyword sets high speed")
  void testVibeChaosSpeed() {
    VibePreset.apply(config, "chaos");
    assertTrue(config.getWaveSpeed() >= 4.0f,
        "Chaos speed should be high, got " + config.getWaveSpeed());
  }

  @Test
  @DisplayName("apply with null/empty falls back to balanced")
  void testVibeNullEmpty() {
    Configuration c1 = new Configuration();
    Configuration c2 = new Configuration();
    VibePreset.apply(c1, null);
    VibePreset.apply(c2, "");
    // Both should produce balanced preset
    assertEquals(c1.getWaveSpeed(), c2.getWaveSpeed(), 0.01f);
  }

  @Test
  @DisplayName("apply with unknown keyword falls back to balanced")
  void testVibeUnknown() {
    Configuration c1 = new Configuration();
    Configuration c2 = new Configuration();
    VibePreset.apply(c1, "xyzzy_nonexistent");
    VibePreset.applyBalanced(c2);
    assertEquals(c1.getWaveSpeed(), c2.getWaveSpeed(), 0.01f);
  }

  @Test
  @DisplayName("apply with null config throws")
  void testVibeNullConfig() {
    assertThrows(IllegalArgumentException.class,
        () -> VibePreset.apply(null, "calm"));
  }

  @Test
  @DisplayName("isValidVibe recognizes known keywords")
  void testIsValidVibe() {
    assertTrue(VibePreset.isValidVibe("calm"));
    assertTrue(VibePreset.isValidVibe("chaos"));
    assertTrue(VibePreset.isValidVibe("ocean"));
    assertFalse(VibePreset.isValidVibe("xyzzy"));
    assertFalse(VibePreset.isValidVibe(""));
  }

  @Test
  @DisplayName("getValidVibes returns non-empty array")
  void testGetValidVibes() {
    String[] vibes = VibePreset.getValidVibes();
    assertNotNull(vibes);
    assertTrue(vibes.length > 0);
  }

  @Test
  @DisplayName("applyRandom with intensity 0 sets low speed")
  void testApplyRandomLowIntensity() {
    VibePreset.applyRandom(config, 0.0f);
    assertTrue(config.getWaveSpeed() <= 1.0f);
  }

  @Test
  @DisplayName("applyRandom with intensity 1 sets high speed")
  void testApplyRandomHighIntensity() {
    VibePreset.applyRandom(config, 1.0f);
    assertTrue(config.getWaveSpeed() >= 4.0f);
  }
}
