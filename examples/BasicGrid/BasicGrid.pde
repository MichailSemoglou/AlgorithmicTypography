/*
 * BasicGrid
 *
 * Simplest usage of the AlgorithmicTypography library.
 * Loads a configuration from a JSON file and renders a
 * three-stage animation with smooth fade transitions:
 *
 *   Stage 1 (0-6s)    — 16x16 grid (beginning)
 *   Stage 2 (6-12s)   — 8x8 grid   (middle)
 *   Stage 3 (12-18s)  — 4x4 grid   (final)
 *
 * Each transition cross-fades over 2 seconds.
 *
 * Controls:
 *   R - Restart the animation
 *   S - Toggle frame saving
 */

import algorithmic.typography.*;

AlgorithmicTypography at;

void setup() {
  size(800, 800);

  at = new AlgorithmicTypography(this);
  at.loadConfiguration("config.json");
  at.setAutoRender(false);
  at.initialize();

  println("BasicGrid — press R to restart, S to toggle saving");
}

void draw() {
  at.render();
}

void keyPressed() {
  if (key == 'r' || key == 'R') {
    at.restart();
  } else if (key == 's' || key == 'S') {
    at.toggleFrameSaving();
  }
}
