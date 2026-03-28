/*
 * RandomASCII
 *
 * Fills the grid with random printable ASCII characters
 * while using the library's wave engine for full HSB colour animation.
 * Characters reshuffle periodically for a "Matrix" feel.
 *
 * Controls:
 *   SPACE   Reshuffle characters now
 *   W       Cycle wave type (SINE → SQUARE → TRIANGLE → SAWTOOTH)
 *   +/-     Increase / decrease grid density
 *   R       Restart animation
 */

import algorithmic.typography.*;
import algorithmic.typography.core.*;

AlgorithmicTypography at;
Configuration config;

int cols = 40;
int rows = 40;
char[][] grid;
int reshuffleInterval = 120;  // frames between reshuffles

// printable ASCII range (! to ~)
final int ASCII_START = 33;
final int ASCII_END = 126;

// Wave type cycling
final WavePresets.Type[] WAVE_TYPES = {
  WavePresets.Type.SINE,
  WavePresets.Type.SQUARE,
  WavePresets.Type.TRIANGLE,
  WavePresets.Type.SAWTOOTH
};
final String[] WAVE_NAMES = { "SINE", "SQUARE", "TRIANGLE", "SAWTOOTH" };
int waveIndex = 0;

void setup() {
  size(800, 800);

  at = new AlgorithmicTypography(this);
  at.setAutoRender(false);

  config = new Configuration();
  config.setGridSize(cols, rows);
  config.setWaveSpeed(1.5f);
  config.setHueRange(180, 340);       // blue-purple-magenta band
  config.setSaturationRange(180, 255);
  config.setBrightnessRange(60, 255);
  config.setSaveFrames(false);
  at.setConfiguration(config);
  at.setWaveType(WAVE_TYPES[waveIndex]);

  grid = new char[cols][rows];
  reshuffleGrid();

  println("RandomASCII — SPACE reshuffle | W cycle wave | +/- density");
}

void draw() {
  background(0);

  if (frameCount % reshuffleInterval == 0) {
    reshuffleGrid();
  }

  WaveEngine wave = at.getWaveEngine();
  wave.update(frameCount, config.getWaveSpeed());

  float tileW = (float) width  / cols;
  float tileH = (float) height / rows;
  float ts = min(tileW, tileH) * config.getTextScale();
  textSize(ts);
  textAlign(CENTER, CENTER);
  noStroke();

  colorMode(HSB, 360, 255, 255);
  for (int x = 0; x < cols; x++) {
    for (int y = 0; y < rows; y++) {
      float h = wave.calculateHue(frameCount, x, y, cols, rows);
      float s = wave.calculateSaturation(frameCount, x, y, cols, rows);
      float b = wave.calculateColorCustom(frameCount, x, y, cols, rows);
      fill(h, s, b);
      text(grid[x][y], x * tileW + tileW / 2, y * tileH + tileH / 2);
    }
  }

  // HUD
  colorMode(RGB, 255);
  fill(255);
  textSize(14);
  textAlign(LEFT, TOP);
  text("Grid: " + cols + "x" + rows
     + "  |  Wave: " + WAVE_NAMES[waveIndex]
     + "  |  SPACE reshuffle  |  W wave  |  +/- density", 10, 10);
}

void reshuffleGrid() {
  for (int x = 0; x < cols; x++) {
    for (int y = 0; y < rows; y++) {
      grid[x][y] = (char) int(random(ASCII_START, ASCII_END + 1));
    }
  }
}

void rebuildGrid() {
  config.setGridSize(cols, rows);
  grid = new char[cols][rows];
  reshuffleGrid();
}

void keyPressed() {
  if (key == ' ') {
    reshuffleGrid();
  } else if (key == 'w' || key == 'W') {
    waveIndex = (waveIndex + 1) % WAVE_TYPES.length;
    at.setWaveType(WAVE_TYPES[waveIndex]);
  } else if (key == '+' || key == '=') {
    cols = min(cols + 4, 80);
    rows = min(rows + 4, 80);
    rebuildGrid();
  } else if (key == '-' || key == '_') {
    cols = max(cols - 4, 8);
    rows = max(rows - 4, 8);
    rebuildGrid();
  } else if (key == 'r' || key == 'R') {
    at.restart();
    rebuildGrid();
  }
}
