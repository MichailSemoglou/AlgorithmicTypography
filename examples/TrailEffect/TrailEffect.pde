/*
 * TrailEffect
 *
 * Glyphs move within their grid cells and leave fading
 * trails behind them. The trail is created by drawing a
 * semi-transparent black overlay each frame instead of
 * clearing the background — so previous positions bleed
 * through and gradually fade out.
 *
 * Controls:
 *   M           Cycle motion: CW → CCW → Perlin → Lissajous → Spring → Gravity → Magnetic
 *   UP/DOWN     Trail length (overlay opacity)
 *   +/-         Motion radius
 */

import algorithmic.typography.*;
import algorithmic.typography.core.WaveEngine;
import algorithmic.typography.core.CellMotion;
import algorithmic.typography.core.CircularMotion;
import algorithmic.typography.core.LissajousMotion;
import algorithmic.typography.core.PerlinMotion;
import algorithmic.typography.core.SpringMotion;
import algorithmic.typography.core.GravityMotion;
import algorithmic.typography.core.MagneticMotion;

Configuration  config;
WaveEngine     wave;

// Motion
CellMotion      motion;
int             motionIdx = 2;   // 0=CW 1=CCW 2=Perlin 3=Lissajous 4=Spring 5=Gravity 6=Magnetic
String[]        motionNames = {"Clockwise", "Counter-CW", "Perlin", "Lissajous", "Spring", "Gravity", "Magnetic"};
CircularMotion  cwMotion;
CircularMotion  ccwMotion;
PerlinMotion    perlinMotion;
LissajousMotion lissajousMotion;
SpringMotion    springMotion;
GravityMotion   gravityMotion;
MagneticMotion  magneticMotion;

int   tilesX    = 20;
int   tilesY    = 20;
int   trailAlpha = 30;          // 0 = infinite trail, 255 = no trail

void setup() {
  size(1080, 1080);

  config = new Configuration();
  config.setCanvasSize(width, height);
  config.setGridSize(tilesX, tilesY);
  config.setTextScale(0.55f);
  config.setBrightnessRange(150, 255);
  config.setHueRange(200, 310);       // cyan → pink
  config.setSaturationMin(180);
  config.setSaturationMax(255);
  config.setWaveAmplitudeRange(-80, 80);
  config.setSaveFrames(false);

  wave = new WaveEngine(config);

  // Motion presets
  cwMotion        = new CircularMotion(12, 1.0f, true);
  ccwMotion       = new CircularMotion(12, 1.0f, false);
  perlinMotion    = new PerlinMotion(14, 1.0f);
  lissajousMotion = new LissajousMotion(12, 1.0f);   // figure-8 default
  springMotion    = new SpringMotion(12, 1.0f);       // spring-damped
  gravityMotion   = new GravityMotion(12, 0.18f);     // gravity + bounce
  magneticMotion  = new MagneticMotion(this);         // mouse-driven
  magneticMotion.setTileGrid(width, height, tilesX, tilesY);
  magneticMotion.setRadius(20);

  // Start with Perlin
  motion = perlinMotion;

  background(0);   // clear once at start

  println("TrailEffect — M=motion  UP/DOWN=trail  +/-=radius");
}

void draw() {
  // ── Semi-transparent overlay = trail ──────────────────────
  noStroke();
  fill(0, trailAlpha);
  rect(0, 0, width, height);

  // ── Update wave engine ────────────────────────────────────
  wave.update(frameCount, 0.9f);

  // ── Draw grid ─────────────────────────────────────────────
  float tileW  = (float) width  / tilesX;
  float tileH  = (float) height / tilesY;
  float textSz = min(tileW, tileH) * config.getTextScale();

  textSize(textSz);
  textAlign(CENTER, CENTER);
  noStroke();
  String ch = config.getCharacter();

  boolean hasHue = config.getHueMin() != config.getHueMax();
  if (hasHue) colorMode(HSB, 360, 255, 255);

  for (int x = 0; x < tilesX; x++) {
    for (int y = 0; y < tilesY; y++) {
      // Colour from wave
      if (hasHue) {
        float h = wave.calculateHue(frameCount, x, y, tilesX, tilesY);
        float s = wave.calculateSaturation(frameCount, x, y, tilesX, tilesY);
        float b = wave.calculateColorCustom(frameCount, x, y, tilesX, tilesY);
        fill(h, s, b);
      } else {
        float b = wave.calculateColorCustom(frameCount, x, y, tilesX, tilesY);
        fill(b);
      }

      // Position with motion offset
      float cx = x * tileW + tileW / 2;
      float cy = y * tileH + tileH / 2;
      PVector off = motion.getOffset(x, y, frameCount);
      cx += off.x;
      cy += off.y;

      text(ch, cx, cy);
    }
  }

  if (hasHue) colorMode(RGB, 255);

  // ── HUD (drawn over everything) ──────────────────────────
  drawHUD();
}

void drawHUD() {
  fill(255, 200);
  textSize(14);
  textAlign(LEFT, TOP);
  noStroke();
  float motionR = motion.getRadius();
  text("Motion: " + motionNames[motionIdx] + " (r=" + (int)motionR + ")" +
       "   Trail: " + trailAlpha +
       "   FPS: " + (int)frameRate, 16, 16);
}

void keyPressed() {
  // Trail opacity (lower = longer trail)
  if (keyCode == DOWN) trailAlpha = max(trailAlpha - 5, 5);
  if (keyCode == UP)   trailAlpha = min(trailAlpha + 5, 255);

  // Motion cycling
  if (key == 'm' || key == 'M') {
    motionIdx = (motionIdx + 1) % motionNames.length;
    switch (motionIdx) {
      case 0: motion = cwMotion;        break;
      case 1: motion = ccwMotion;       break;
      case 2: motion = perlinMotion;    break;
      case 3: motion = lissajousMotion; break;
      case 4: motion = springMotion;    break;
      case 5: motion = gravityMotion;   break;
      case 6: motion = magneticMotion;  break;
    }
    background(0);  // clear old trails on mode switch
    println("Motion: " + motionNames[motionIdx]);
  }

  // Motion radius
  if (key == '+' || key == '=') motion.setRadius(motion.getRadius() + 2);
  if (key == '-' || key == '_') motion.setRadius(max(0, motion.getRadius() - 2));
}
