/*
 * BackgroundImage
 *
 * Demonstrates rendering the typography grid on top of
 * a background photograph. Uses renderAt() instead of
 * render() so the canvas is not cleared each frame.
 *
 * Place your image as "background.png" in the data/ folder.
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
Configuration cfg;
String framesDir;

void setup() {
  size(800, 800);

  bg = loadImage("background.png");
  bg.resize(width, height);

  at = new AlgorithmicTypography(this);
  at.loadConfiguration("config.json");
  at.setAutoRender(false);
  at.initialize();
  cfg = at.getConfiguration();

  framesDir = "frames/" + nf(year(),4) + nf(month(),2) + nf(day(),2) + "_" + nf(hour(),2) + nf(minute(),2) + nf(second(),2);
  println("BackgroundImage — press R to restart, S to toggle saving");
}

void draw() {
  image(bg, 0, 0);
  at.renderAt(0, 0, width, height);

  // Save frame if enabled (renderAt doesn't auto-save)
  if (cfg.isSaveFrames()) {
    saveFrame(framesDir + "/frame_####.png");
  }
}

void keyPressed() {
  if (key == 'r' || key == 'R') {
    at.restart();
  } else if (key == 's' || key == 'S') {
    at.toggleFrameSaving();
  }
}
