/**
 * JUnit tests for WavePresets.
 */

package algorithmic.typography.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import algorithmic.typography.Configuration;

public class WavePresetsTest {

  private Configuration config;

  @BeforeEach
  void setUp() {
    config = new Configuration();
  }

  @Test
  @DisplayName("get() returns non-null for all Type values")
  void testGetAllTypes() {
    for (WavePresets.Type type : WavePresets.Type.values()) {
      WaveFunction wf = WavePresets.get(type);
      assertNotNull(wf, "WavePresets.get(" + type + ") returned null");
    }
  }

  @Test
  @DisplayName("sine() produces values in brightness range")
  void testSineRange() {
    WaveFunction fn = WavePresets.sine();
    assertNotNull(fn);
    float bMin = config.getBrightnessMin();
    float bMax = config.getBrightnessMax();
    for (int frame = 0; frame < 360; frame += 15) {
      float v = fn.calculate(frame, 0.5f, 0.5f, frame / 540f, config);
      assertTrue(v >= bMin && v <= bMax,
          "Sine value " + v + " out of [" + bMin + "," + bMax + "] at frame " + frame);
    }
  }

  @Test
  @DisplayName("square() returns only min or max brightness")
  void testSquareBinary() {
    WaveFunction fn = WavePresets.square();
    float bMin = config.getBrightnessMin();
    float bMax = config.getBrightnessMax();
    for (int frame = 0; frame < 360; frame += 5) {
      float v = fn.calculate(frame, 0.3f, 0.7f, frame / 540f, config);
      assertTrue(Math.abs(v - bMin) < 0.01f || Math.abs(v - bMax) < 0.01f,
          "Square value " + v + " is neither " + bMin + " nor " + bMax);
    }
  }

  @Test
  @DisplayName("triangle() produces values in brightness range")
  void testTriangleRange() {
    WaveFunction fn = WavePresets.triangle();
    float bMin = config.getBrightnessMin();
    float bMax = config.getBrightnessMax();
    for (int frame = 0; frame < 360; frame += 10) {
      float v = fn.calculate(frame, 0.5f, 0.5f, frame / 540f, config);
      assertTrue(v >= bMin && v <= bMax,
          "Triangle value " + v + " out of [" + bMin + "," + bMax + "]");
    }
  }

  @Test
  @DisplayName("sawtooth() produces values in brightness range")
  void testSawtoothRange() {
    WaveFunction fn = WavePresets.sawtooth();
    float bMin = config.getBrightnessMin();
    float bMax = config.getBrightnessMax();
    for (int frame = 0; frame < 360; frame += 10) {
      float v = fn.calculate(frame, 0.5f, 0.5f, frame / 540f, config);
      assertTrue(v >= bMin && v <= bMax,
          "Sawtooth value " + v + " out of [" + bMin + "," + bMax + "]");
    }
  }

  @Test
  @DisplayName("tangent() produces values in brightness range")
  void testTangentRange() {
    WaveFunction fn = WavePresets.tangent();
    float bMin = config.getBrightnessMin();
    float bMax = config.getBrightnessMax();
    for (int frame = 0; frame < 360; frame += 10) {
      float v = fn.calculate(frame, 0.5f, 0.5f, frame / 540f, config);
      assertTrue(v >= bMin && v <= bMax,
          "Tangent value " + v + " out of [" + bMin + "," + bMax + "]");
    }
  }

  @Test
  @DisplayName("All presets have a name")
  void testPresetNames() {
    assertNotNull(WavePresets.sine().getName());
    assertNotNull(WavePresets.tangent().getName());
    assertNotNull(WavePresets.square().getName());
    assertNotNull(WavePresets.triangle().getName());
    assertNotNull(WavePresets.sawtooth().getName());
  }

  @Test
  @DisplayName("All presets have a description")
  void testPresetDescriptions() {
    assertNotNull(WavePresets.sine().getDescription());
    assertNotNull(WavePresets.tangent().getDescription());
    assertNotNull(WavePresets.square().getDescription());
    assertNotNull(WavePresets.triangle().getDescription());
    assertNotNull(WavePresets.sawtooth().getDescription());
  }
}
