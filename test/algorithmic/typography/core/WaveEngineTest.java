/**
 * JUnit tests for WaveEngine.
 */

package algorithmic.typography.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import algorithmic.typography.Configuration;

public class WaveEngineTest {

  private Configuration config;
  private WaveEngine engine;

  @BeforeEach
  void setUp() {
    config = new Configuration();
    engine = new WaveEngine(config);
  }

  @Test
  @DisplayName("Constructor creates engine with default state")
  void testConstructor() {
    assertNotNull(engine);
    assertTrue(engine.isAutoUpdate());
    assertNull(engine.getCustomWaveFunction());
  }

  @Test
  @DisplayName("calculateAmplitude returns value in [0, 1]")
  void testCalculateAmplitudeRange() {
    for (int x = 0; x < 20; x++) {
      for (int y = 0; y < 20; y++) {
        float amp = engine.calculateAmplitude(x, y);
        assertTrue(amp >= 0.0f && amp <= 1.0f,
            "Amplitude at (" + x + "," + y + ") = " + amp + " out of [0,1]");
      }
    }
  }

  @Test
  @DisplayName("calculateAmplitude is deterministic")
  void testCalculateAmplitudeDeterministic() {
    float a1 = engine.calculateAmplitude(5, 10);
    float a2 = engine.calculateAmplitude(5, 10);
    assertEquals(a1, a2, 0.0001f);
  }

  @Test
  @DisplayName("update changes waveMultiplier")
  void testUpdateChangesMultiplier() {
    engine.update(0, 1.0f);
    float m0 = engine.getWaveMultiplier();
    engine.update(45, 1.0f);
    float m45 = engine.getWaveMultiplier();
    // sin(radians(0)) != sin(radians(45)), so multipliers differ
    assertNotEquals(m0, m45, 0.001f);
  }

  @Test
  @DisplayName("waveMultiplier stays within configured range")
  void testWaveMultiplierRange() {
    float min = config.getWaveMultiplierMin();
    float max = config.getWaveMultiplierMax();
    for (int frame = 0; frame < 360; frame += 10) {
      engine.update(frame, 1.0f);
      float m = engine.getWaveMultiplier();
      assertTrue(m >= min && m <= max,
          "Multiplier " + m + " out of [" + min + "," + max + "] at frame " + frame);
    }
  }

  @Test
  @DisplayName("calculateColor returns value in brightness range")
  void testCalculateColorRange() {
    float bMin = config.getBrightnessMin();
    float bMax = config.getBrightnessMax();
    engine.update(10, 1.0f);
    for (int x = 0; x < 10; x++) {
      for (int y = 0; y < 10; y++) {
        float amp = engine.calculateAmplitude(x, y);
        float color = engine.calculateColor(10, x, y, amp);
        assertTrue(color >= bMin && color <= bMax,
            "Color " + color + " out of [" + bMin + "," + bMax + "]");
      }
    }
  }

  @Test
  @DisplayName("calculateSaturation returns fixed value when min equals max")
  void testSaturationFixedValue() {
    config.setSaturationRange(100, 100);
    engine.update(5, 1.0f);
    float sat = engine.calculateSaturation(5, 3, 3, 10, 10);
    assertEquals(100.0f, sat, 0.001f);
  }

  @Test
  @DisplayName("calculateSaturation returns value in range")
  void testSaturationRange() {
    float sMin = config.getSaturationMin();
    float sMax = config.getSaturationMax();
    for (int frame = 0; frame < 100; frame += 10) {
      float sat = engine.calculateSaturation(frame, 5, 5, 10, 10);
      assertTrue(sat >= sMin && sat <= sMax,
          "Saturation " + sat + " out of [" + sMin + "," + sMax + "]");
    }
  }

  @Test
  @DisplayName("calculateHue returns fixed value when min equals max")
  void testHueFixedValue() {
    config.setHueRange(180, 180);
    engine.update(5, 1.0f);
    float hue = engine.calculateHue(5, 3, 3, 10, 10);
    assertEquals(180.0f, hue, 0.001f);
  }

  @Test
  @DisplayName("calculateHue returns value in range")
  void testHueRange() {
    float hMin = config.getHueMin();
    float hMax = config.getHueMax();
    for (int frame = 0; frame < 100; frame += 10) {
      float hue = engine.calculateHue(frame, 5, 5, 10, 10);
      assertTrue(hue >= hMin && hue <= hMax,
          "Hue " + hue + " out of [" + hMin + "," + hMax + "]");
    }
  }

  @Test
  @DisplayName("Custom wave function is used when set")
  void testCustomWaveFunction() {
    WaveFunction custom = (frameCount, x, y, time, cfg) -> 42.0f;
    engine.setCustomWaveFunction(custom);
    assertSame(custom, engine.getCustomWaveFunction());

    float result = engine.calculateColorCustom(10, 5, 5, 10, 10);
    assertEquals(42.0f, result, 0.001f);
  }

  @Test
  @DisplayName("calculateColorCustom uses default when no custom function")
  void testCalculateColorCustomDefault() {
    // No custom set — should fall back to calculateColor
    engine.update(10, 1.0f);
    float expected = engine.calculateColor(10, 5, 5, engine.calculateAmplitude(5, 5));
    float actual = engine.calculateColorCustom(10, 5, 5, 10, 10);
    assertEquals(expected, actual, 0.001f);
  }

  @Test
  @DisplayName("reset clears custom wave function")
  void testReset() {
    engine.setCustomWaveFunction((f, x, y, t, c) -> 0.0f);
    assertNotNull(engine.getCustomWaveFunction());
    engine.reset();
    assertNull(engine.getCustomWaveFunction());
  }

  @Test
  @DisplayName("Auto-update toggle works")
  void testAutoUpdateToggle() {
    assertTrue(engine.isAutoUpdate());
    engine.setAutoUpdate(false);
    assertFalse(engine.isAutoUpdate());
    engine.setAutoUpdate(true);
    assertTrue(engine.isAutoUpdate());
  }
}
