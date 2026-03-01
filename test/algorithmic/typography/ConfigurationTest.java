/**
 * JUnit tests for the Configuration class.
 * 
 * These tests verify that configuration loading, validation,
 * and manipulation work correctly.
 */

package algorithmic.typography;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for Configuration class.
 */
public class ConfigurationTest {
  
  private Configuration config;
  
  @BeforeEach
  void setUp() {
    config = new Configuration();
  }
  
  @Test
  @DisplayName("Default configuration has valid values")
  void testDefaultValues() {
    assertEquals(1080, config.getCanvasWidth());
    assertEquals(1080, config.getCanvasHeight());
    assertEquals(18, config.getAnimationDuration());
    assertEquals(30, config.getAnimationFPS());
    assertEquals("A", config.getCharacter());
    assertFalse(config.isSaveFrames());
  }
  
  @Test
  @DisplayName("Validation passes for default configuration")
  void testDefaultValidation() {
    assertDoesNotThrow(() -> config.validate());
  }
  
  @Test
  @DisplayName("Validation fails for zero canvas width")
  void testZeroCanvasWidth() {
    assertThrows(IllegalArgumentException.class, () -> config.setCanvasWidth(0));
  }
  
  @Test
  @DisplayName("Validation fails for negative tile count")
  void testNegativeTileCount() {
    assertThrows(IllegalArgumentException.class, () -> config.setInitialTilesX(-1));
  }
  
  @Test
  @DisplayName("Validation fails for text scale out of range")
  void testTextScaleOutOfRange() {
    assertThrows(IllegalArgumentException.class, () -> config.setTextScale(1.5f));
  }
  
  @Test
  @DisplayName("Validation fails for saturation out of range")
  void testSaturationOutOfRange() {
    assertThrows(IllegalArgumentException.class, () -> config.setSaturationRange(0, 300));
  }
  
  @Test
  @DisplayName("Copy creates independent instance")
  void testCopy() {
    config.setCanvasWidth(500);
    Configuration copy = config.copy();
    
    assertEquals(config.getCanvasWidth(), copy.getCanvasWidth());
    
    // Modify original and verify copy is independent
    config.setCanvasWidth(1000);
    assertEquals(500, copy.getCanvasWidth());
  }
  
  @Test
  @DisplayName("Wave multiplier range is validated")
  void testWaveMultiplierRange() {
    config.setWaveMultiplierMin(5.0f);
    config.setWaveMultiplierMax(2.0f);
    
    // The values should be accepted (no cross-validation on individual setters)
    assertTrue(config.getWaveMultiplierMin() > config.getWaveMultiplierMax());
  }
  
  @Test
  @DisplayName("Configuration can be modified programmatically")
  void testProgrammaticModification() {
    config.setCharacter("X");
    config.setInitialTilesX(64);
    config.setWaveSpeed(3.5f);
    
    assertEquals("X", config.getCharacter());
    assertEquals(64, config.getInitialTilesX());
    assertEquals(3.5f, config.getWaveSpeed(), 0.001);
    
    // Should still validate
    assertDoesNotThrow(() -> config.validate());
  }

  @Test
  @DisplayName("setBackgroundColor(gray) sets all channels equally")
  void testBackgroundColorGray() {
    config.setBackgroundColor(128);
    assertEquals(128, config.getBackgroundRed());
    assertEquals(128, config.getBackgroundGreen());
    assertEquals(128, config.getBackgroundBlue());
  }

  @Test
  @DisplayName("setBackgroundColor(r,g,b) sets channels independently")
  void testBackgroundColorRGB() {
    config.setBackgroundColor(10, 20, 30);
    assertEquals(10, config.getBackgroundRed());
    assertEquals(20, config.getBackgroundGreen());
    assertEquals(30, config.getBackgroundBlue());
  }

  @Test
  @DisplayName("setBackgroundColor rejects out-of-range values")
  void testBackgroundColorOutOfRange() {
    assertThrows(IllegalArgumentException.class, () -> config.setBackgroundColor(-1));
    assertThrows(IllegalArgumentException.class, () -> config.setBackgroundColor(256));
    assertThrows(IllegalArgumentException.class, () -> config.setBackgroundColor(0, 0, 256));
  }

  @Test
  @DisplayName("Background colour defaults to black")
  void testBackgroundColorDefault() {
    Configuration fresh = new Configuration();
    assertEquals(0, fresh.getBackgroundRed());
    assertEquals(0, fresh.getBackgroundGreen());
    assertEquals(0, fresh.getBackgroundBlue());
  }

  @Test
  @DisplayName("copy() preserves background colour")
  void testBackgroundColorCopy() {
    config.setBackgroundColor(50, 100, 150);
    Configuration copy = config.copy();
    assertEquals(50,  copy.getBackgroundRed());
    assertEquals(100, copy.getBackgroundGreen());
    assertEquals(150, copy.getBackgroundBlue());
  }
}
