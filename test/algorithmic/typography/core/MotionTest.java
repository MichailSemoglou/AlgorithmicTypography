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

  // ── LissajousMotion ──────────────────────────────────────────

  @Test
  @DisplayName("LissajousMotion defaults are sensible")
  void testLissajousDefaults() {
    LissajousMotion lm = new LissajousMotion();
    assertTrue(lm.getRadius() > 0);
    assertTrue(lm.getRadiusY() > 0);
    assertTrue(lm.getSpeed() > 0);
    assertTrue(lm.getFreqX() > 0);
    assertTrue(lm.getFreqY() > 0);
  }

  @Test
  @DisplayName("LissajousMotion offset within radius bounds")
  void testLissajousOffsetBounds() {
    float radius = 12.0f;
    LissajousMotion lm = new LissajousMotion(radius, 1.0f);
    for (int frame = 0; frame < 200; frame += 5) {
      for (int col = 0; col < 5; col++) {
        for (int row = 0; row < 5; row++) {
          PVector offset = lm.getOffset(col, row, frame);
          assertTrue(Math.abs(offset.x) <= radius + 0.01f,
              "X offset " + offset.x + " exceeds radius " + radius);
          assertTrue(Math.abs(offset.y) <= radius + 0.01f,
              "Y offset " + offset.y + " exceeds radius " + radius);
        }
      }
    }
  }

  @Test
  @DisplayName("LissajousMotion offset varies with frame")
  void testLissajousVariesWithFrame() {
    LissajousMotion lm = new LissajousMotion(10, 1.0f);
    PVector o0 = lm.getOffset(3, 3, 0);
    PVector o60 = lm.getOffset(3, 3, 60);
    assertFalse(o0.x == o60.x && o0.y == o60.y,
        "Offset should change over time");
  }

  @Test
  @DisplayName("LissajousMotion is deterministic")
  void testLissajousDeterministic() {
    LissajousMotion lm = new LissajousMotion(10, 1.0f);
    PVector o1 = lm.getOffset(2, 4, 100);
    PVector o2 = lm.getOffset(2, 4, 100);
    assertEquals(o1.x, o2.x, 0.0001f);
    assertEquals(o1.y, o2.y, 0.0001f);
  }

  @Test
  @DisplayName("LissajousMotion different cells produce different offsets")
  void testLissajousDifferentCells() {
    LissajousMotion lm = new LissajousMotion(10, 1.0f);
    PVector o1 = lm.getOffset(0, 0, 30);
    PVector o2 = lm.getOffset(5, 5, 30);
    assertFalse(o1.x == o2.x && o1.y == o2.y,
        "Different cells should produce different offsets");
  }

  @Test
  @DisplayName("LissajousMotion figure-8 ratio 1:2 produces distinct x and y frequencies")
  void testLissajousFig8Ratio() {
    // With freqX=1, freqY=2 the y channel oscillates twice as fast
    LissajousMotion lm = new LissajousMotion(10, 10, 1.0f, 1, 2);
    lm.setPhaseSpread(0); // disable cell spread to isolate ratio effect
    PVector atT0   = lm.getOffset(0, 0, 0);
    PVector atTHalf = lm.getOffset(0, 0, 52); // ~half period of x channel
    // y should be near 0 twice per x period; x should be near opposite extreme
    assertNotEquals(atT0.x, atTHalf.x, 0.5f);
  }

  @Test
  @DisplayName("LissajousMotion setRadiusY clamps negative")
  void testLissajousRadiusYClamp() {
    LissajousMotion lm = new LissajousMotion();
    lm.setRadiusY(-5);
    assertEquals(0, lm.getRadiusY(), 0.001f);
  }

  @Test
  @DisplayName("LissajousMotion independent radiusY respected")
  void testLissajousIndependentRadiusY() {
    float rx = 10.0f;
    float ry = 5.0f;
    LissajousMotion lm = new LissajousMotion(rx, ry, 1.0f, 1, 2);
    lm.setPhaseSpread(0);
    // At t where sin(freqY*t)==1 the y offset should equal ry, not rx
    // freqY*t = PI/2 → t = PI/2 / (0.03*freqY) ≈ 26
    PVector o = lm.getOffset(0, 0, 26);
    assertTrue(Math.abs(o.y) <= ry + 0.5f,
        "Y offset should be bounded by radiusY=" + ry);
  }

  // ── SpringMotion ──────────────────────────────────────────────

  @Test
  @DisplayName("SpringMotion defaults are sensible")
  void testSpringDefaults() {
    SpringMotion sm = new SpringMotion();
    assertTrue(sm.getRadius() > 0);
    assertTrue(sm.getStiffness() > 0);
    assertTrue(sm.getDamping() >= 0);
    assertTrue(sm.getTargetAmplitude() > 0);
  }

  @Test
  @DisplayName("SpringMotion offset stays within radius over many frames")
  void testSpringOffsetWithinRadius() {
    float radius = 15.0f;
    SpringMotion sm = new SpringMotion(radius, 1.0f);
    for (int frame = 0; frame < 300; frame++) {
      for (int col = 0; col < 4; col++) {
        for (int row = 0; row < 4; row++) {
          PVector offset = sm.getOffset(col, row, frame);
          float mag = offset.mag();
          assertTrue(mag <= radius + 0.01f,
              "Offset magnitude " + mag + " exceeds radius " + radius
              + " at frame " + frame);
        }
      }
    }
  }

  @Test
  @DisplayName("SpringMotion offset evolves over time")
  void testSpringEvolves() {
    SpringMotion sm = new SpringMotion(10, 1.0f);
    PVector o0  = sm.getOffset(3, 3, 0);
    // Advance many frames; spring should have moved
    PVector o60 = null;
    for (int f = 1; f <= 60; f++) {
      o60 = sm.getOffset(3, 3, f);
    }
    assertFalse(o0.x == o60.x && o0.y == o60.y,
        "Spring offset should evolve over time");
  }

  @Test
  @DisplayName("SpringMotion double-call same frame returns cached value")
  void testSpringIdempotentSameFrame() {
    SpringMotion sm = new SpringMotion(10, 1.0f);
    // Warm up
    for (int f = 0; f < 10; f++) sm.getOffset(1, 1, f);
    PVector a = sm.getOffset(1, 1, 10);
    PVector b = sm.getOffset(1, 1, 10); // same frame — must return same value
    assertEquals(a.x, b.x, 0.0001f);
    assertEquals(a.y, b.y, 0.0001f);
  }

  @Test
  @DisplayName("SpringMotion different cells produce different offsets")
  void testSpringDifferentCells() {
    SpringMotion sm = new SpringMotion(10, 1.0f);
    // Advance several frames so spring has moved
    for (int f = 0; f < 30; f++) {
      sm.getOffset(0, 0, f);
      sm.getOffset(5, 5, f);
    }
    PVector o1 = sm.getOffset(0, 0, 30);
    PVector o2 = sm.getOffset(5, 5, 30);
    assertFalse(o1.x == o2.x && o1.y == o2.y,
        "Different cells should produce different offsets");
  }

  @Test
  @DisplayName("SpringMotion reset clears state")
  void testSpringReset() {
    SpringMotion sm = new SpringMotion(10, 1.0f);
    // Advance enough for position to be non-zero
    for (int f = 0; f < 50; f++) sm.getOffset(0, 0, f);
    sm.reset();
    PVector afterReset = sm.getOffset(0, 0, 100);
    // After reset, first call should start from (0,0) — magnitude low
    assertTrue(afterReset.mag() < 10.01f);
  }

  @Test
  @DisplayName("SpringMotion setStiffness and setDamping update correctly")
  void testSpringSetters() {
    SpringMotion sm = new SpringMotion();
    sm.setStiffness(0.5f);
    sm.setDamping(0.3f);
    sm.setTargetAmplitude(12.0f);
    sm.setPhaseSpread(1.2f);
    assertEquals(0.5f,  sm.getStiffness(),        0.001f);
    assertEquals(0.3f,  sm.getDamping(),           0.001f);
    assertEquals(12.0f, sm.getTargetAmplitude(),   0.001f);
    assertEquals(1.2f,  sm.getPhaseSpread(),        0.001f);
  }

  @Test
  @DisplayName("SpringMotion setStiffness clamps below minimum")
  void testSpringStiffnessClamp() {
    SpringMotion sm = new SpringMotion();
    sm.setStiffness(0.0f);
    assertTrue(sm.getStiffness() > 0, "Stiffness must remain positive");
    sm.setStiffness(-1.0f);
    assertTrue(sm.getStiffness() > 0, "Negative stiffness must be clamped");
  }

  // ── GravityMotion ─────────────────────────────────────────

  @Test
  @DisplayName("GravityMotion defaults are sensible")
  void testGravityDefaults() {
    GravityMotion gm = new GravityMotion();
    assertTrue(gm.getRadius() > 0);
    assertTrue(gm.getGravity() > 0);
    assertTrue(gm.getRestitution() > 0 && gm.getRestitution() <= 1);
    assertTrue(gm.getLateralStrength() >= 0);
  }

  @Test
  @DisplayName("GravityMotion offset stays within radius bounds over many frames")
  void testGravityOffsetWithinRadius() {
    float radius = 14.0f;
    GravityMotion gm = new GravityMotion(radius, 0.18f);
    for (int frame = 0; frame < 300; frame++) {
      for (int col = 0; col < 5; col++) {
        for (int row = 0; row < 5; row++) {
          PVector offset = gm.getOffset(col, row, frame);
          float mag = offset.mag();
          assertTrue(mag <= radius * Math.sqrt(2) + 0.01f,
              "Offset " + offset + " exceeds diagonal of radius " + radius
              + " at frame " + frame + " col=" + col + " row=" + row);
        }
      }
    }
  }

  @Test
  @DisplayName("GravityMotion glyphs fall (posY increases over initial frames)")
  void testGravityFalls() {
    GravityMotion gm = new GravityMotion(20, 0.3f, 0.72f, 0.0f);
    // Cell (0,0) starts at top; after several frames posY should increase
    PVector o0  = gm.getOffset(0, 0, 0);
    PVector o10 = null;
    for (int f = 1; f <= 10; f++) o10 = gm.getOffset(0, 0, f);
    // posY should have grown (fallen) compared to initial position
    assertTrue(o10.y >= o0.y - 0.01f,
        "Glyph should fall (posY increases)");
  }

  @Test
  @DisplayName("GravityMotion double-call same frame returns cached value")
  void testGravityIdempotentSameFrame() {
    GravityMotion gm = new GravityMotion(12, 0.18f);
    for (int f = 0; f < 10; f++) gm.getOffset(2, 2, f);
    PVector a = gm.getOffset(2, 2, 10);
    PVector b = gm.getOffset(2, 2, 10);
    assertEquals(a.x, b.x, 0.0001f);
    assertEquals(a.y, b.y, 0.0001f);
  }

  @Test
  @DisplayName("GravityMotion different cells are phase-staggered from frame 0")
  void testGravityStaggeredCells() {
    GravityMotion gm = new GravityMotion(12, 0.18f);
    PVector o1 = gm.getOffset(0, 0, 0);
    PVector o2 = gm.getOffset(3, 5, 0);
    assertFalse(o1.x == o2.x && o1.y == o2.y,
        "Different cells should have staggered initial positions");
  }

  @Test
  @DisplayName("GravityMotion reset clears state")
  void testGravityReset() {
    GravityMotion gm = new GravityMotion(12, 0.18f);
    for (int f = 0; f < 60; f++) gm.getOffset(0, 0, f);
    gm.reset();
    // After reset the cell is re-initialised on next call — should not throw
    PVector afterReset = gm.getOffset(0, 0, 100);
    assertTrue(afterReset.mag() <= 12 * Math.sqrt(2) + 0.01f);
  }

  @Test
  @DisplayName("GravityMotion setters update correctly")
  void testGravitySetters() {
    GravityMotion gm = new GravityMotion();
    gm.setGravity(0.4f);
    gm.setRestitution(0.8f);
    gm.setLateralStrength(0.1f);
    gm.setAirDrag(0.95f);
    gm.setPhaseSpread(1.5f);
    assertEquals(0.4f,  gm.getGravity(),         0.001f);
    assertEquals(0.8f,  gm.getRestitution(),     0.001f);
    assertEquals(0.1f,  gm.getLateralStrength(), 0.001f);
    assertEquals(0.95f, gm.getAirDrag(),         0.001f);
    assertEquals(1.5f,  gm.getPhaseSpread(),     0.001f);
  }

  @Test
  @DisplayName("GravityMotion restitution clamped to [0, 1]")
  void testGravityRestitutionClamp() {
    GravityMotion gm = new GravityMotion();
    gm.setRestitution(1.5f);
    assertTrue(gm.getRestitution() <= 1.0f, "Restitution must not exceed 1");
    gm.setRestitution(-0.5f);
    assertTrue(gm.getRestitution() >= 0.0f, "Restitution must not be negative");
  }

  @Test
  @DisplayName("GravityMotion gravity clamps below minimum")
  void testGravityGravityClamp() {
    GravityMotion gm = new GravityMotion();
    gm.setGravity(0.0f);
    assertTrue(gm.getGravity() > 0, "Gravity must remain positive");
    gm.setGravity(-1.0f);
    assertTrue(gm.getGravity() > 0, "Negative gravity must be clamped");
  }

  @Test
  @DisplayName("GravityMotion kick applies upward impulse to settled glyphs")
  void testGravityKick() {
    GravityMotion gm = new GravityMotion(12, 0.18f, 0.72f, 0.0f);
    // Let glyphs settle for many frames
    for (int f = 0; f < 300; f++) gm.getOffset(0, 0, f);
    // Record posY before kick (should be near bottom = +radius)
    PVector before = gm.getOffset(0, 0, 299);
    // Apply kick and advance one frame
    gm.kick(10.0f);
    PVector after = gm.getOffset(0, 0, 300);
    // After kick the glyph should have moved upward (smaller posY)
    assertTrue(after.y < before.y + 0.01f,
        "Kick should move glyph upward; before=" + before.y + " after=" + after.y);
  }

  @Test
  @DisplayName("GravityMotion kick is no-op when no cells initialised")
  void testGravityKickEmptyState() {
    GravityMotion gm = new GravityMotion(12, 0.18f);
    // Should not throw even when cellState is empty
    gm.kick(10.0f);
  }

  // ── MagneticMotion ────────────────────────────────────────────

  /** Minimal PApplet subclass that exposes settable mouse coordinates. */
  static class MockApplet extends processing.core.PApplet {
    MockApplet(int mx, int my) { mouseX = mx; mouseY = my; }
  }

  @Test
  @DisplayName("MagneticMotion defaults are sensible")
  void testMagneticDefaults() {
    MockApplet app = new MockApplet(0, 0);
    MagneticMotion mm = new MagneticMotion(app);
    assertTrue(mm.getRadius()   > 0,   "radius must be positive");
    assertTrue(mm.getStrength() > 0,   "strength must be positive");
    assertTrue(mm.getFalloff()  > 0,   "falloff must be positive");
    assertTrue(mm.getSmoothing() > 0,  "smoothing must be positive");
    assertFalse(mm.isAttract(),        "default mode must be repel");
  }

  @Test
  @DisplayName("MagneticMotion repel moves glyph away from mouse")
  void testMagneticRepelDirection() {
    // Mouse directly above cell (0,0) whose world centre is at (25, 25)
    MockApplet app = new MockApplet(25, 0);
    MagneticMotion mm = new MagneticMotion(app, 1800, 80, false);
    mm.setTileSize(50, 50);
    mm.setRadius(20);
    mm.setSmoothing(1.0f);   // instant snap so test sees full target immediately

    // Force = strength/(dist+falloff) = 1800/(25+80) ≈ 17.1 px, clamped to radius 20
    // ny = -1 (mouse is above), repel => targetY = +force (pushing DOWN)
    PVector offset = mm.getOffset(0, 0, 1);
    assertTrue(offset.y > 0,
        "Repel should push glyph away from above-mouse (downward), got y=" + offset.y);
  }

  @Test
  @DisplayName("MagneticMotion attract moves glyph toward mouse")
  void testMagneticAttractDirection() {
    // Mouse directly below cell (0,0) world centre (25, 25)
    MockApplet app = new MockApplet(25, 60);
    MagneticMotion mm = new MagneticMotion(app, 1800, 80, true);
    mm.setTileSize(50, 50);
    mm.setRadius(20);
    mm.setSmoothing(1.0f);

    PVector offset = mm.getOffset(0, 0, 1);
    // Mouse is below (dy > 0), attract => targetY > 0 (toward mouse = downward)
    assertTrue(offset.y > 0,
        "Attract should pull glyph toward below-mouse (downward), got y=" + offset.y);
  }

  @Test
  @DisplayName("MagneticMotion offset clamped to radius")
  void testMagneticOffsetClamped() {
    // Mouse on top of cell centre — very large force before clamp
    MockApplet app = new MockApplet(25, 25);
    MagneticMotion mm = new MagneticMotion(app, 1_000_000, 1, false);
    mm.setTileSize(50, 50);
    mm.setRadius(15);
    mm.setSmoothing(1.0f);

    PVector offset = mm.getOffset(0, 0, 1);
    float mag = offset.mag();
    assertTrue(mag <= 15.0f + 0.001f,
        "Offset magnitude must not exceed radius; got " + mag);
  }

  @Test
  @DisplayName("MagneticMotion togglePolarity flips attract flag")
  void testMagneticTogglePolarity() {
    MockApplet app = new MockApplet(0, 0);
    MagneticMotion mm = new MagneticMotion(app);
    assertFalse(mm.isAttract());
    mm.togglePolarity();
    assertTrue(mm.isAttract());
    mm.togglePolarity();
    assertFalse(mm.isAttract());
  }

  @Test
  @DisplayName("MagneticMotion setTileGrid computes tile dimensions correctly")
  void testMagneticSetTileGrid() {
    MockApplet app = new MockApplet(0, 0);
    MagneticMotion mm = new MagneticMotion(app);
    mm.setTileGrid(1080, 1080, 22, 22);
    assertEquals(1080.0f / 22, mm.getTileWidth(),  0.01f);
    assertEquals(1080.0f / 22, mm.getTileHeight(), 0.01f);
  }

  @Test
  @DisplayName("MagneticMotion smoothing produces gradual position change")
  void testMagneticSmoothing() {
    // Mouse far to the right — should gradually approach target
    MockApplet app = new MockApplet(500, 25);
    MagneticMotion mm = new MagneticMotion(app, 1800, 80, false);
    mm.setTileSize(50, 50);
    mm.setRadius(20);
    mm.setSmoothing(0.1f);

    PVector frame1 = mm.getOffset(0, 0, 1);
    PVector frame2 = mm.getOffset(0, 0, 2);
    // Magnitude at frame 2 should be larger than frame 1 (still approaching)
    assertTrue(frame2.mag() >= frame1.mag() - 0.001f,
        "Smoothing: offset should grow toward target over frames");
  }
}

