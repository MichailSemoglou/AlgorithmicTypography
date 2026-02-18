/*
 * MultipleSystems
 *
 * Run two AlgorithmicTypography instances side by side,
 * each with its own configuration.
 *
 * Controls:
 *   R - Restart both systems
 */

import algorithmic.typography.*;

AlgorithmicTypography at1;
AlgorithmicTypography at2;

void setup() {
  size(1200, 600);

  // System 1 — letters (disable auto-render so we position manually)
  at1 = new AlgorithmicTypography(this);
  at1.setAutoRender(false);
  Configuration config1 = new Configuration();
  config1.setCharacter("A");
  config1.setGridSize(32, 32);
  config1.setWaveSpeed(1.5);
  config1.setBrightnessRange(100, 255);
  config1.setSaveFrames(false);
  at1.setConfiguration(config1);

  // System 2 — numbers
  at2 = new AlgorithmicTypography(this);
  at2.setAutoRender(false);
  Configuration config2 = new Configuration();
  config2.setCharacter("1");
  config2.setGridSize(32, 32);
  config2.setWaveSpeed(2.5);
  config2.setBrightnessRange(50, 200);
  config2.setSaveFrames(false);
  at2.setConfiguration(config2);

  println("MultipleSystems — two grids side by side");
}

void draw() {
  background(0);

  // Render each system into its half of the canvas
  at1.renderAt(0, 40, width / 2, height - 40);
  at2.renderAt(width / 2, 40, width / 2, height - 40);

  // Labels
  fill(255);
  textAlign(CENTER);
  textSize(20);
  text("System 1: Letter Grid", width / 4, 25);
  text("System 2: Number Grid", width * 3 / 4, 25);

  // Divider
  stroke(100);
  strokeWeight(2);
  line(width / 2, 0, width / 2, height);
}

void keyPressed() {
  if (key == 'r' || key == 'R') {
    at1.restart();
    at2.restart();
  }
}
