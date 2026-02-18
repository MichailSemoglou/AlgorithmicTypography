/*
 * SaveSVG
 *
 * Demonstrates how to export the current frame as a
 * scalable vector graphic (SVG).  The grid is drawn
 * with HSB colour so the exported file keeps every
 * colour exactly as it appears on screen.
 *
 * Controls:
 *   S      Save current frame as SVG
 *   SPACE  Toggle wave angle (0° / 45° / 90° / 135°)
 *   UP     Increase wave speed
 *   DOWN   Decrease wave speed
 */

import processing.svg.*;
import algorithmic.typography.*;
import algorithmic.typography.core.WaveEngine;

Configuration config;
WaveEngine   wave;

boolean saveSVG  = false;
int     angleIdx = 1;            // start at 45°
float[] angles   = {0, 45, 90, 135};

void setup() {
  size(1080, 1080);

  config = new Configuration();
  config.setCanvasSize(width, height);
  config.setGridSize(16, 16);
  config.setTextScale(0.55);
  config.setWaveSpeed(1.5);
  config.setWaveAngle(angles[angleIdx]);
  config.setBrightnessRange(120, 255);
  config.setHueRange(180, 320);            // cyan → magenta
  config.setSaturationMin(180);
  config.setSaturationMax(255);
  config.setSaveFrames(false);

  wave = new WaveEngine(config);

  println("SaveSVG — S=save  SPACE=angle  UP/DOWN=speed");
}

void draw() {
  if (saveSVG) {
    String filename = "typography-" + nf(frameCount, 6) + ".svg";
    beginRecord(SVG, filename);
  }

  background(0);
  renderGrid();

  if (saveSVG) {
    endRecord();
    println("Saved: " + "typography-" + nf(frameCount, 6) + ".svg");
    saveSVG = false;
  }

  drawHUD();
}

void renderGrid() {
  int   tilesX = config.getInitialTilesX();
  int   tilesY = config.getInitialTilesY();
  float tileW  = (float) width  / tilesX;
  float tileH  = (float) height / tilesY;

  wave.update(frameCount, config.getWaveSpeed());

  colorMode(HSB, 360, 255, 255);
  noStroke();

  float textSz = min(tileW, tileH) * config.getTextScale();
  textSize(textSz);
  textAlign(CENTER, CENTER);

  String ch = config.getCharacter();

  for (int x = 0; x < tilesX; x++) {
    for (int y = 0; y < tilesY; y++) {
      float h = wave.calculateHue(frameCount, x, y, tilesX, tilesY);
      float s = wave.calculateSaturation(frameCount, x, y, tilesX, tilesY);
      float b = wave.calculateColorCustom(frameCount, x, y, tilesX, tilesY);
      fill(h, s, b);
      text(ch, x * tileW + tileW / 2, y * tileH + tileH / 2);
    }
  }

  colorMode(RGB, 255);
}

void drawHUD() {
  fill(255);
  textAlign(LEFT, TOP);
  textSize(14);

  int y = 20;
  text("Angle: " + (int)config.getWaveAngle() + "°", 20, y);  y += 20;
  text("Speed: " + nf(config.getWaveSpeed(), 1, 1), 20, y);    y += 20;
  text("Press S to save SVG", 20, y);
}

void keyPressed() {
  if (key == 's' || key == 'S') {
    saveSVG = true;
  } else if (key == ' ') {
    angleIdx = (angleIdx + 1) % angles.length;
    config.setWaveAngle(angles[angleIdx]);
    println("Wave angle: " + (int)angles[angleIdx] + "°");
  } else if (keyCode == UP) {
    config.setWaveSpeed(min(config.getWaveSpeed() + 0.2, 6.0));
    println("Speed: " + nf(config.getWaveSpeed(), 1, 1));
  } else if (keyCode == DOWN) {
    config.setWaveSpeed(max(config.getWaveSpeed() - 0.2, 0.2));
    println("Speed: " + nf(config.getWaveSpeed(), 1, 1));
  }
}
