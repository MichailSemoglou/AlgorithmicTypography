/*
 * SaveSVG
 *
 * Demonstrates how to export the current frame as a
 * scalable vector graphic (SVG).  The grid is drawn
 * with HSB colour so the exported file keeps every
 * colour exactly as it appears on screen.
 *
 * The SVG is exported at the full canvas size (1080×1080)
 * with explicit width/height attributes so it opens at
 * the correct artboard size in Affinity Designer,
 * Illustrator, Inkscape, etc.
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
import algorithmic.typography.render.VectorExporter;

Configuration config;
WaveEngine wave;

boolean saveSVG = false;
int angleIdx = 1;  // start at 45°
float[] angles = {0, 45, 90, 135};

void setup() {
  pixelDensity(1);  // disable Retina 2x scaling — keeps SVG coordinates clean
  size(1080, 1080);

  config = new Configuration();
  config.setCanvasSize(width, height);
  config.setGridSize(16, 16);
  config.setTextScale(0.55f);
  config.setWaveSpeed(1.5f);
  config.setWaveAngle(angles[angleIdx]);
  config.setBrightnessRange(120, 255);
  config.setHueRange(180, 320);
  config.setSaturationMin(180);
  config.setSaturationMax(255);
  config.setSaveFrames(false);

  wave = new WaveEngine(config);

  println("SaveSVG — S=save  SPACE=angle  UP/DOWN=speed");
}

void draw() {
  // Update wave once per frame before any rendering
  wave.update(frameCount, config.getWaveSpeed());

  background(0);
  renderGrid(g);
  drawHUD();

  if (saveSVG) {
    String filename = "typography-" + nf(frameCount, 6) + ".svg";
    // createGraphics(w, h, SVG) embeds explicit width/height in the SVG root
    // element so Affinity Designer and other apps open it at the correct size.
    PGraphics svg = createGraphics(width, height, SVG, filename);
    svg.beginDraw();
    svg.background(0);
    renderGrid(svg);
    svg.endDraw();
    svg.dispose();
    // Fix SVG artboard dimensions for Affinity Designer / Illustrator / Inkscape.
    // Processing writes unitless width/height; this corrects the DPI mismatch.
    VectorExporter.fixArtboardDimensions(this, filename, width, height);
    println("Saved: " + filename);
    saveSVG = false;
  }
}

void renderGrid(PGraphics pg) {
  int tilesX = config.getInitialTilesX();
  int tilesY = config.getInitialTilesY();
  float tileW = (float) pg.width  / tilesX;
  float tileH = (float) pg.height / tilesY;

  pg.colorMode(HSB, 360, 255, 255);
  pg.noStroke();

  float textSz = min(tileW, tileH) * config.getTextScale();
  pg.textSize(textSz);
  pg.textAlign(CENTER, CENTER);

  String ch = config.getCharacter();

  for (int x = 0; x < tilesX; x++) {
    for (int y = 0; y < tilesY; y++) {
      float h = wave.calculateHue(frameCount, x, y, tilesX, tilesY);
      float s = wave.calculateSaturation(frameCount, x, y, tilesX, tilesY);
      float b = wave.calculateColorCustom(frameCount, x, y, tilesX, tilesY);
      pg.fill(h, s, b);
      pg.text(ch, x * tileW + tileW / 2, y * tileH + tileH / 2);
    }
  }

  pg.colorMode(RGB, 255);
}

void drawHUD() {
  fill(255);
  textAlign(LEFT, TOP);
  textSize(14);

  int y = 20;
  text("Angle: " + (int)config.getWaveAngle() + "°", 20, y); y += 20;
  text("Speed: " + nf(config.getWaveSpeed(), 1, 1), 20, y); y += 20;
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
    config.setWaveSpeed(min(config.getWaveSpeed() + 0.2f, 6.0f));
    println("Speed: " + nf(config.getWaveSpeed(), 1, 1));
  } else if (keyCode == DOWN) {
    config.setWaveSpeed(max(config.getWaveSpeed() - 0.2f, 0.2f));
    println("Speed: " + nf(config.getWaveSpeed(), 1, 1));
  }
}
