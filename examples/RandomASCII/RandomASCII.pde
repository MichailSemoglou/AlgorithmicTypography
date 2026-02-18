/*
 * RandomASCII
 *
 * Fills the grid with random printable ASCII characters
 * while using the library's wave engine for colour animation.
 * Characters reshuffle periodically for a "Matrix" feel.
 *
 * Controls:
 *   SPACE - Reshuffle characters now
 *   +/-   - Increase / decrease grid density
 *   R     - Restart animation
 */

import algorithmic.typography.*;
import algorithmic.typography.core.*;

AlgorithmicTypography at;
Configuration config;

int cols = 40;
int rows = 40;
char[][] grid;          // random character per cell
int reshuffleInterval = 120;  // frames between reshuffles

// printable ASCII range (! to ~)
final int ASCII_START = 33;
final int ASCII_END   = 126;

void setup() {
  size(800, 800);

  at = new AlgorithmicTypography(this);
  at.setAutoRender(false);

  config = new Configuration();
  config.setGridSize(cols, rows);
  config.setWaveSpeed(2.0);
  config.setBrightnessRange(80, 255);
  config.setSaveFrames(false);
  at.setConfiguration(config);

  grid = new char[cols][rows];
  reshuffleGrid();

  println("RandomASCII — press SPACE to reshuffle, +/- to resize grid");
}

void draw() {
  background(0);

  // Reshuffle on interval
  if (frameCount % reshuffleInterval == 0) {
    reshuffleGrid();
  }

  // Use the wave engine for colour values
  WaveEngine wave = at.getWaveEngine();
  wave.update(frameCount, config.getWaveSpeed());

  float tileW = (float) width  / cols;
  float tileH = (float) height / rows;
  float ts = min(tileW, tileH) * config.getTextScale();
  textSize(ts);
  textAlign(CENTER, CENTER);
  noStroke();

  for (int x = 0; x < cols; x++) {
    for (int y = 0; y < rows; y++) {
      float c = wave.calculateColorCustom(frameCount, x, y, cols, rows);
      fill(c);
      text(grid[x][y], x * tileW + tileW / 2, y * tileH + tileH / 2);
    }
  }

  // HUD
  fill(255);
  textSize(14);
  textAlign(LEFT, TOP);
  text("Grid: " + cols + "x" + rows + "  |  SPACE reshuffle  |  +/- density", 10, 10);
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
