/*
 * TrailEffect
 *
 * Glyphs move within their grid cells and leave fading
 * trails behind them.  The trail is created by drawing a
 * semi-transparent black overlay each frame instead of
 * clearing the background — so previous positions bleed
 * through and gradually fade out.
 *
 * Controls:
 *   M           Cycle motion: CW → CCW → Perlin
 *   UP/DOWN     Trail length (overlay opacity)
 *   +/-         Motion radius
 *   SPACE       Toggle wave angle (0 / 45 / 90 / 135)
 */

import algorithmic.typography.*;
import algorithmic.typography.core.WaveEngine;
import algorithmic.typography.core.CellMotion;
import algorithmic.typography.core.CircularMotion;
import algorithmic.typography.core.PerlinMotion;

Configuration  config;
WaveEngine     wave;

// Motion
CellMotion      motion;
int             motionIdx = 2;   // 0=CW 1=CCW 2=Perlin
String[]        motionNames = {"Clockwise", "Counter-CW", "Perlin"};
CircularMotion  cwMotion;
CircularMotion  ccwMotion;
PerlinMotion    perlinMotion;

int   tilesX    = 20;
int   tilesY    = 20;
int   trailAlpha = 30;          // 0 = infinite trail, 255 = no trail

int   angleIdx  = 1;
float[] angles  = {0, 45, 90, 135};

void setup() {
  size(1080, 1080);

  config = new Configuration();
  config.setCanvasSize(width, height);
  config.setGridSize(tilesX, tilesY);
  config.setTextScale(0.55);
  config.setWaveSpeed(1.8);
  config.setWaveAngle(angles[angleIdx]);
  config.setBrightnessRange(120, 255);
  config.setHueRange(180, 330);       // cyan → pink
  config.setSaturationMin(180);
  config.setSaturationMax(255);
  config.setSaveFrames(false);

  wave = new WaveEngine(config);

  // Motion presets
  cwMotion     = new CircularMotion(12, 1.0, true);
  ccwMotion    = new CircularMotion(12, 1.0, false);
  perlinMotion = new PerlinMotion(14, 1.0);

  // Start with Perlin
  motion = perlinMotion;

  background(0);   // clear once at start

  println("TrailEffect — M=motion  UP/DOWN=trail  +/-=radius  SPACE=angle");
}

void draw() {
  // ── Semi-transparent overlay = trail ──────────────────────
  noStroke();
  fill(0, trailAlpha);
  rect(0, 0, width, height);

  // ── Update wave engine ────────────────────────────────────
  wave.update(frameCount, config.getWaveSpeed());

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
       "   Angle: " + (int)config.getWaveAngle() + "°" +
       "   FPS: " + (int)frameRate, 16, 16);
}

void keyPressed() {
  // Trail opacity (lower = longer trail)
  if (keyCode == DOWN) trailAlpha = max(trailAlpha - 5, 5);
  if (keyCode == UP)   trailAlpha = min(trailAlpha + 5, 255);

  // Wave angle
  if (key == ' ') {
    angleIdx = (angleIdx + 1) % angles.length;
    config.setWaveAngle(angles[angleIdx]);
  }

  // Motion cycling
  if (key == 'm' || key == 'M') {
    motionIdx = (motionIdx + 1) % 3;
    switch (motionIdx) {
      case 0: motion = cwMotion;      break;
      case 1: motion = ccwMotion;     break;
      case 2: motion = perlinMotion;  break;
    }
    background(0);  // clear old trails on mode switch
    println("Motion: " + motionNames[motionIdx]);
  }

  // Motion radius
  if (key == '+' || key == '=') motion.setRadius(motion.getRadius() + 2);
  if (key == '-' || key == '_') motion.setRadius(max(0, motion.getRadius() - 2));
}
