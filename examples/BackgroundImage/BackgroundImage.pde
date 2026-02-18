/*
 * BackgroundImage
 *
 * Demonstrates rendering the typography grid on top of
 * a background photograph. Uses renderAt() instead of
 * render() so the canvas is not cleared each frame.
 *
 * Place your image as "background.jpg" in the data/ folder.
 * The image is resized to fill the canvas.
 *
 *   Stage 1 (0-6s)    — 16x16 grid
 *   Stage 2 (6-12s)   — 8x8 grid
 *   Stage 3 (12-18s)  — 4x4 grid
 *
 * Each transition cross-fades over 2 seconds.
 *
 * Controls:
 *   R - Restart the animation
 *   S - Toggle frame saving
 */

import algorithmic.typography.*;

AlgorithmicTypography at;
PImage bg;

void setup() {
  size(800, 800);

  bg = loadImage("background.png");
  bg.resize(width, height);

  at = new AlgorithmicTypography(this);
  at.loadConfiguration("config.json");
  at.setAutoRender(false);
  at.initialize();

  println("BackgroundImage — press R to restart, S to toggle saving");
}

void draw() {
  image(bg, 0, 0);
  at.renderAt(0, 0, width, height);
}

void keyPressed() {
  if (key == 'r' || key == 'R') {
    at.restart();
  } else if (key == 's' || key == 'S') {
    at.toggleFrameSaving();
  }
}
