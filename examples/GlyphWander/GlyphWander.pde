/*
 * GlyphWander
 *
 * Demonstrates per-glyph cell motion via config.setCellMotion().
 * Each motion type is applied directly through the high-level
 * AlgorithmicTypography API — no custom draw loop required.
 *
 * Motion modes (cycle with M):
 *   0  None           — glyphs sit still at cell centres
 *   1  Perlin         — organic, noise-driven wandering
 *   2  Circular CW    — clockwise orbits with per-cell phase offset
 *   3  Circular CCW   — counter-clockwise orbits
 *   4  Lissajous      — figure-8 and knot-shaped orbits
 *   5  Spring         — spring-damped glyphs pulled toward a drifting target
 *   6  Ripple         — click to trigger concentric displacement rings
 *   7  FlowField      — glyphs drift in a slowly evolving Perlin noise field
 *   8  Orbital        — glyphs orbit neighbours in a constellation pattern
 *
 * Controls:
 *   M / click   Cycle motion mode
 *   UP / DOWN   Increase / decrease radius
 *   + / -       Increase / decrease speed
 *   SPACE       Trigger a ripple at the sketch centre (Ripple mode only)
 *   R           Restart animation
 */

import algorithmic.typography.*;
import algorithmic.typography.core.CellMotion;
import algorithmic.typography.core.PerlinMotion;
import algorithmic.typography.core.CircularMotion;
import algorithmic.typography.core.LissajousMotion;
import algorithmic.typography.core.SpringMotion;
import algorithmic.typography.core.RippleMotion;
import algorithmic.typography.core.FlowFieldMotion;
import algorithmic.typography.core.OrbitalMotion;

AlgorithmicTypography at;
Configuration config;

// Motion instances (reused across keypresses so radius/speed edits stick)
PerlinMotion    perlin       = new PerlinMotion(12, 1.0);
CircularMotion  cwMotion     = new CircularMotion(12, 1.0, true);
CircularMotion  ccwMotion    = new CircularMotion(12, 1.0, false);
LissajousMotion lissajous    = new LissajousMotion(12, 1.0);
SpringMotion    springMotion = new SpringMotion(12, 1.0);
RippleMotion    ripple       = new RippleMotion(200, 80, 0.975f);
FlowFieldMotion flowField    = new FlowFieldMotion(12, 0.007f, 0.005f);
OrbitalMotion   orbital      = new OrbitalMotion(12, 0.6f);

int motionIdx = 1;  // start on Perlin
String[] motionLabels = {
  "None", "Perlin", "Circular CW", "Circular CCW", "Lissajous",
  "Spring", "Ripple", "FlowField", "Orbital"
};

void setup() {
  size(1080, 1080);

  // Ripple needs a tile grid for world-space calculation
  ripple.setTileGrid(width, height, 12, 12);
  ripple.setRadius(20);

  config = new Configuration();
  config.setCanvasSize(width, height);
  config.setGridSize(24, 24, 12, 12, 6, 6);
  config.setCharacter("A");
  config.setTextScale(0.65);
  config.setWaveSpeed(1.6);
  config.setWaveAngle(45);
  config.setHueRange(200, 340);
  config.setSaturationMin(160);
  config.setSaturationMax(255);
  config.setBrightnessRange(140, 255);
  config.setAnimationDuration(20);
  config.setAnimationFPS(60);
  config.setSaveFrames(false);

  at = new AlgorithmicTypography(this);
  at.setConfiguration(config);
  at.setAutoRender(false);

  applyMotion(motionIdx);
  at.initialize();

  println("GlyphWander — M=mode  UP/DOWN=radius  +/-=speed  SPACE=ripple  R=restart");
}

void draw() {
  at.render();
  drawHUD();
}

// ── Motion helpers ───────────────────────────────────────────────────────────

void applyMotion(int idx) {
  switch (idx) {
    case 0:  config.setCellMotion(null);         break;
    case 1:  config.setCellMotion(perlin);       break;
    case 2:  config.setCellMotion(cwMotion);     break;
    case 3:  config.setCellMotion(ccwMotion);    break;
    case 4:  config.setCellMotion(lissajous);    break;
    case 5:  config.setCellMotion(springMotion); break;
    case 6:  config.setCellMotion(ripple);       break;
    case 7:  config.setCellMotion(flowField);    break;
    case 8:  config.setCellMotion(orbital);      break;
  }
}

// Returns whichever motion object is currently active (or null)
CellMotion activeMotion() {
  switch (motionIdx) {
    case 1:  return perlin;
    case 2:  return cwMotion;
    case 3:  return ccwMotion;
    case 4:  return lissajous;
    case 5:  return springMotion;
    case 6:  return ripple;
    case 7:  return flowField;
    case 8:  return orbital;
    default: return null;
  }
}

// ── HUD ─────────────────────────────────────────────────────────────────────

void drawHUD() {
  float r = (activeMotion() != null) ? activeMotion().getRadius() : 0;
  float s = (activeMotion() != null) ? activeMotion().getSpeed()  : 0;
  String extra = (motionIdx == 6)
    ? "   Rings: " + ripple.getRippleCount() + "  (SPACE = trigger)"
    : "";

  fill(255, 200);
  noStroke();
  textSize(14);
  textAlign(LEFT, TOP);
  text("Motion: " + motionLabels[motionIdx]
     + "   Radius: " + nf(r, 1, 1) + " px"
     + "   Speed: "  + nf(s, 1, 2)
     + extra
     + "   FPS: "    + (int)frameRate, 16, 16);
}

// ── Controls ─────────────────────────────────────────────────────────────────

void keyPressed() {
  if (key == 'm' || key == 'M') {
    motionIdx = (motionIdx + 1) % motionLabels.length;
    applyMotion(motionIdx);
    println("Motion: " + motionLabels[motionIdx]);
    return;
  }

  if (key == 'r' || key == 'R') {
    at.restart();
    return;
  }

  // Trigger ripple from centre on SPACE
  if (key == ' ' && motionIdx == 6) {
    ripple.trigger(width / 2.0, height / 2.0);
    return;
  }

  CellMotion m = activeMotion();
  if (m == null) return;

  if (keyCode == UP)   m.setRadius(min(m.getRadius() + 2, 80));
  if (keyCode == DOWN) m.setRadius(max(m.getRadius() - 2,  0));
  if (key == '+' || key == '=') m.setSpeed(min(m.getSpeed() + 0.1, 5.0));
  if (key == '-' || key == '_') m.setSpeed(max(m.getSpeed() - 0.1, 0.1));
}

void mousePressed() {
  if (motionIdx == 6) {
    // In Ripple mode: trigger at mouse click position
    ripple.trigger(mouseX, mouseY);
  } else {
    motionIdx = (motionIdx + 1) % motionLabels.length;
    applyMotion(motionIdx);
    println("Motion: " + motionLabels[motionIdx]);
  }
}
