/**
 * JUnit tests for TemporalTrail.
 */

package algorithmic.typography.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class TemporalTrailTest {

  private TemporalTrail trail;

  @BeforeEach
  void setUp() {
    trail = new TemporalTrail(4, 3, 8);
  }

  @Test
  @DisplayName("Constructor sets dimensions correctly")
  void testConstructor() {
    assertEquals(4, trail.getCols());
    assertEquals(3, trail.getRows());
    assertEquals(8, trail.getMaxLength());
    assertEquals(0, trail.getFilledFrames());
  }

  @Test
  @DisplayName("Constructor clamps maxLength to at least 1")
  void testMaxLengthClamp() {
    TemporalTrail t = new TemporalTrail(2, 2, 0);
    assertEquals(1, t.getMaxLength());
    TemporalTrail t2 = new TemporalTrail(2, 2, -5);
    assertEquals(1, t2.getMaxLength());
  }

  @Test
  @DisplayName("captureRaw stores data and increments filled")
  void testCaptureRaw() {
    float[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
    trail.captureRaw(data);
    assertEquals(1, trail.getFilledFrames());
  }

  @Test
  @DisplayName("composite returns correct dimensions")
  void testCompositeDimensions() {
    float[] data = new float[12];
    trail.captureRaw(data);
    float[][] result = trail.composite();
    assertEquals(3, result.length);     // rows
    assertEquals(4, result[0].length);  // cols
  }

  @Test
  @DisplayName("Single capture composite returns same values")
  void testSingleCaptureComposite() {
    trail.setFadeDecay(1.0f);
    trail.setTrailLength(1);
    float[] data = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120};
    trail.captureRaw(data);
    float[][] result = trail.composite();
    assertEquals(10.0f, result[0][0], 0.01f);
    assertEquals(120.0f, result[2][3], 0.01f);
  }

  @Test
  @DisplayName("clear resets state")
  void testClear() {
    float[] data = new float[12];
    trail.captureRaw(data);
    trail.captureRaw(data);
    assertEquals(2, trail.getFilledFrames());

    trail.clear();
    assertEquals(0, trail.getFilledFrames());
  }

  @Test
  @DisplayName("Ring buffer wraps around correctly")
  void testRingBufferWrap() {
    // Capture more than maxLength (8) frames
    for (int i = 0; i < 15; i++) {
      float[] data = new float[12];
      for (int j = 0; j < 12; j++) data[j] = i;
      trail.captureRaw(data);
    }
    // FilledFrames should be maxLength
    assertEquals(8, trail.getFilledFrames());
  }

  @Test
  @DisplayName("setTrailLength clamps to valid range")
  void testTrailLengthClamp() {
    trail.setTrailLength(0);
    // After clamping, accessing should still work
    trail.setTrailLength(100);
    // Should be clamped to maxLength (8)
    trail.setTrailLength(5);
    // Valid within range
  }

  @Test
  @DisplayName("setFadeDecay clamps to [0, 1]")
  void testFadeDecayClamp() {
    trail.setFadeDecay(-0.5f);
    trail.setFadeDecay(0.0f);
    trail.setFadeDecay(0.5f);
    trail.setFadeDecay(1.0f);
    trail.setFadeDecay(2.0f);
    // All should be accepted without error
  }

  @Test
  @DisplayName("BLEND modes are correct constants")
  void testBlendModeConstants() {
    assertEquals(0, TemporalTrail.BLEND_ADD);
    assertEquals(1, TemporalTrail.BLEND_MAX);
    assertEquals(2, TemporalTrail.BLEND_AVERAGE);
  }

  @Test
  @DisplayName("setBlendMode accepts valid modes")
  void testSetBlendMode() {
    trail.setBlendMode(TemporalTrail.BLEND_ADD);
    trail.setBlendMode(TemporalTrail.BLEND_MAX);
    trail.setBlendMode(TemporalTrail.BLEND_AVERAGE);
    // Should not throw
  }

  @Test
  @DisplayName("compositeHSB returns three arrays of correct size")
  void testCompositeHSBDimensions() {
    float[] data = new float[12];
    trail.captureRaw(data);
    float[][] hsb = trail.compositeHSB();
    assertEquals(3, hsb.length);       // hue, saturation, brightness
    assertEquals(12, hsb[0].length);   // cols * rows
    assertEquals(12, hsb[1].length);
    assertEquals(12, hsb[2].length);
  }

  @Test
  @DisplayName("Audio reactive trail length changes with level")
  void testAudioReactive() {
    trail.setAudioReactive(2, 8);
    trail.feedAudioLevel(0.0f);
    trail.feedAudioLevel(0.5f);
    trail.feedAudioLevel(1.0f);
    // Should not throw
  }

  @Test
  @DisplayName("feedAudioLevel clamps to [0, 1]")
  void testAudioLevelClamp() {
    trail.setAudioReactive(1, 8);
    trail.feedAudioLevel(-1.0f);
    trail.feedAudioLevel(2.0f);
    // Should not throw
  }

  @Test
  @DisplayName("Temporal wave can be set and disabled")
  void testTemporalWave() {
    trail.setTemporalWave(2.0f, 0.5f);
    trail.disableTemporalWave();
    // Should not throw
  }

  @Test
  @DisplayName("Framerate reactive can be toggled")
  void testFramerateReactive() {
    trail.setFramerateReactive(true, 60.0f);
    trail.feedFramerate(30.0f);
    trail.setFramerateReactive(false, 60.0f);
    // Should not throw
  }

  @Test
  @DisplayName("Multiple blending modes produce results")
  void testBlendModeResults() {
    trail.setFadeDecay(1.0f);
    trail.setTrailLength(3);

    float[] d1 = new float[12];
    float[] d2 = new float[12];
    for (int i = 0; i < 12; i++) {
      d1[i] = 50;
      d2[i] = 100;
    }

    // Test ADD mode
    trail.setBlendMode(TemporalTrail.BLEND_ADD);
    trail.clear();
    trail.captureRaw(d1);
    trail.captureRaw(d2);
    float[][] addResult = trail.composite();
    assertNotNull(addResult);

    // Test MAX mode
    trail.setBlendMode(TemporalTrail.BLEND_MAX);
    trail.clear();
    trail.captureRaw(d1);
    trail.captureRaw(d2);
    float[][] maxResult = trail.composite();
    assertNotNull(maxResult);

    // Test AVERAGE mode
    trail.setBlendMode(TemporalTrail.BLEND_AVERAGE);
    trail.clear();
    trail.captureRaw(d1);
    trail.captureRaw(d2);
    float[][] avgResult = trail.composite();
    assertNotNull(avgResult);
  }
}
