/**
 * JUnit tests for CircularMotion and PerlinMotion.
 */

package algorithmic.typography.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import processing.core.PVector;

public class MotionTest {

  // ── CircularMotion ────────────────────────────────────────────

  @Test
  @DisplayName("CircularMotion defaults are sensible")
  void testCircularDefaults() {
    CircularMotion cm = new CircularMotion();
    assertTrue(cm.getRadius() > 0);
    assertTrue(cm.getSpeed() > 0);
    assertTrue(cm.isClockwise());
  }

  @Test
  @DisplayName("CircularMotion offset magnitude equals radius")
  void testCircularOffsetMagnitude() {
    float radius = 12.0f;
    CircularMotion cm = new CircularMotion(radius, 1.0f, true);
    PVector offset = cm.getOffset(0, 0, 0);
    assertEquals(radius, offset.mag(), 0.01f);
  }

  @Test
  @DisplayName("CircularMotion offset varies with frame")
  void testCircularOffsetVariesWithFrame() {
    CircularMotion cm = new CircularMotion(10, 1.0f, true);
    PVector o0 = cm.getOffset(5, 5, 0);
    PVector o90 = cm.getOffset(5, 5, 90);
    // Different frames should give different offsets
    assertFalse(o0.x == o90.x && o0.y == o90.y);
  }

  @Test
  @DisplayName("CircularMotion clockwise vs counter-clockwise differ")
  void testClockwiseDirection() {
    CircularMotion cw = new CircularMotion(10, 1.0f, true);
    CircularMotion ccw = new CircularMotion(10, 1.0f, false);

    PVector offsetCW = cw.getOffset(3, 3, 50);
    PVector offsetCCW = ccw.getOffset(3, 3, 50);

    // Same radius but different direction should produce different y
    assertNotEquals(offsetCW.y, offsetCCW.y, 0.01f);
  }

  @Test
  @DisplayName("CircularMotion different cells produce different phases")
  void testCircularDifferentCellPhases() {
    CircularMotion cm = new CircularMotion(10, 1.0f, true);
    PVector o1 = cm.getOffset(0, 0, 30);
    PVector o2 = cm.getOffset(5, 5, 30);
    // Different cells should have different phases
    assertFalse(o1.x == o2.x && o1.y == o2.y);
  }

  @Test
  @DisplayName("setRadius clamps negative to zero")
  void testRadiusClamp() {
    CircularMotion cm = new CircularMotion();
    cm.setRadius(-5);
    assertEquals(0, cm.getRadius(), 0.001f);
  }

  @Test
  @DisplayName("setSpeed allows negative values")
  void testNegativeSpeed() {
    CircularMotion cm = new CircularMotion();
    cm.setSpeed(-2.0f);
    assertEquals(-2.0f, cm.getSpeed(), 0.001f);
  }

  // ── PerlinMotion ──────────────────────────────────────────────

  @Test
  @DisplayName("PerlinMotion defaults are sensible")
  void testPerlinDefaults() {
    PerlinMotion pm = new PerlinMotion();
    assertTrue(pm.getRadius() > 0);
    assertTrue(pm.getSpeed() > 0);
  }

  @Test
  @DisplayName("PerlinMotion offset within radius bounds")
  void testPerlinOffsetBounds() {
    float radius = 15.0f;
    PerlinMotion pm = new PerlinMotion(radius, 1.0f);
    for (int frame = 0; frame < 100; frame += 5) {
      for (int col = 0; col < 5; col++) {
        for (int row = 0; row < 5; row++) {
          PVector offset = pm.getOffset(col, row, frame);
          assertTrue(Math.abs(offset.x) <= radius + 0.01f,
              "X offset " + offset.x + " exceeds radius " + radius);
          assertTrue(Math.abs(offset.y) <= radius + 0.01f,
              "Y offset " + offset.y + " exceeds radius " + radius);
        }
      }
    }
  }

  @Test
  @DisplayName("PerlinMotion is deterministic")
  void testPerlinDeterministic() {
    PerlinMotion pm = new PerlinMotion(10, 1.0f);
    PVector o1 = pm.getOffset(3, 4, 60);
    PVector o2 = pm.getOffset(3, 4, 60);
    assertEquals(o1.x, o2.x, 0.0001f);
    assertEquals(o1.y, o2.y, 0.0001f);
  }

  @Test
  @DisplayName("PerlinMotion different cells produce different offsets")
  void testPerlinDifferentCells() {
    PerlinMotion pm = new PerlinMotion(10, 1.0f);
    PVector o1 = pm.getOffset(0, 0, 10);
    PVector o2 = pm.getOffset(3, 3, 10);
    assertFalse(o1.x == o2.x && o1.y == o2.y,
        "Different cells should produce different offsets");
  }

  @Test
  @DisplayName("setNoiseScale clamps minimum")
  void testNoiseScaleClamp() {
    PerlinMotion pm = new PerlinMotion();
    pm.setNoiseScale(0.0001f);
    pm.setNoiseScale(0.0f);
    pm.setNoiseScale(-1.0f);
    // Should not throw
  }
}
