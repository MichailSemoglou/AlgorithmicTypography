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
 *
 * Controls:
 *   M / click   Cycle motion mode
 *   UP / DOWN   Increase / decrease radius
 *   + / -       Increase / decrease speed
 *   R           Restart animation
 */

import algorithmic.typography.*;
import algorithmic.typography.core.CellMotion;
import algorithmic.typography.core.PerlinMotion;
import algorithmic.typography.core.CircularMotion;

AlgorithmicTypography at;
Configuration        config;

// Motion instances (reused across keypresses so radius/speed edits stick)
PerlinMotion    perlin = new PerlinMotion(12, 1.0);
CircularMotion  cwMotion  = new CircularMotion(12, 1.0, true);
CircularMotion  ccwMotion = new CircularMotion(12, 1.0, false);

int      motionIdx = 1;   // start on Perlin
String[] motionLabels = {"None", "Perlin", "Circular CW", "Circular CCW"};

void setup() {
  size(1080, 1080);

  config = new Configuration();
  config.setCanvasSize(width, height);
  config.setGridSize(24, 24, 12, 12);
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

  println("GlyphWander — M=mode  UP/DOWN=radius  +/-=speed  R=restart");
}

void draw() {
  at.render();
  drawHUD();
}

// ── Motion helpers ───────────────────────────────────────────────────────────

void applyMotion(int idx) {
  switch (idx) {
    case 0: config.setCellMotion(null);      break;
    case 1: config.setCellMotion(perlin);    break;
    case 2: config.setCellMotion(cwMotion);  break;
    case 3: config.setCellMotion(ccwMotion); break;
  }
}

// Returns whichever motion object is currently active (or null)
CellMotion activeMotion() {
  switch (motionIdx) {
    case 1: return perlin;
    case 2: return cwMotion;
    case 3: return ccwMotion;
    default: return null;
  }
}

// ── HUD ─────────────────────────────────────────────────────────────────────

void drawHUD() {
  float r = (activeMotion() != null) ? activeMotion().getRadius() : 0;
  float s = (activeMotion() != null) ? activeMotion().getSpeed()  : 0;

  fill(255, 200);
  noStroke();
  textSize(14);
  textAlign(LEFT, TOP);
  text("Motion: " + motionLabels[motionIdx]
     + "   Radius: " + nf(r, 1, 1) + " px"
     + "   Speed: "  + nf(s, 1, 2)
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

  CellMotion m = activeMotion();
  if (m == null) return;

  if (keyCode == UP)   m.setRadius(min(m.getRadius() + 2, 80));
  if (keyCode == DOWN) m.setRadius(max(m.getRadius() - 2,  0));
  if (key == '+' || key == '=') m.setSpeed(min(m.getSpeed() + 0.1, 5.0));
  if (key == '-' || key == '_') m.setSpeed(max(m.getSpeed() - 0.1, 0.1));
}

void mousePressed() {
  motionIdx = (motionIdx + 1) % motionLabels.length;
  applyMotion(motionIdx);
  println("Motion: " + motionLabels[motionIdx]);
}
