/*
 * ProgrammaticConfig
 *
 * Configure the typography system in code instead of a JSON file.
 * Each restart generates a random configuration — including background colour.
 *
 * Controls:
 *   R - New random configuration
 *   S - Toggle frame saving
 */

import algorithmic.typography.*;

AlgorithmicTypography at;

void setup() {
  size(800, 800);

  at = new AlgorithmicTypography(this);
  at.setConfiguration(randomConfig());
  at.setAutoRender(false);
  at.initialize();

  println("ProgrammaticConfig — press R for a new random config");
}

void draw() {
  at.render();
}

void keyPressed() {
  if (key == 'r' || key == 'R') {
    at.setConfiguration(randomConfig());
    at.initialize();
    at.restart();
  } else if (key == 's' || key == 'S') {
    at.toggleFrameSaving();
  }
}

// Build a random Configuration
Configuration randomConfig() {
  Configuration c = new Configuration();

  c.setCanvasSize(800, 800);

  String[] chars = {"A", "X", "0", "+", "#", "*"};
  c.setCharacter(chars[int(random(chars.length))]);

  int[] sizes = {8, 16, 32, 64};
  int tiles = sizes[int(random(sizes.length))];
  c.setGridSize(tiles, tiles, tiles / 2, tiles / 2);

  c.setWaveSpeed(random(0.5, 5));
  c.setBrightnessRange(random(0, 100), random(150, 255));

  c.setAnimationDuration(8);
  c.setAnimationFPS(30);
  c.setSaveFrames(false);

  // Random background colour from a curated dark palette
  int[][] bgPalette = {
    {  0,   0,   0},   // black
    { 10,  10,  30},   // deep navy
    { 20,   0,  30},   // dark violet
    { 30,  10,   0},   // near-black warm
    {  0,  20,  20},   // dark teal
    { 15,  15,  15}    // dark grey
  };
  int[] bg = bgPalette[int(random(bgPalette.length))];
  c.setBackgroundColor(bg[0], bg[1], bg[2]);

  println("New config  char=" + c.getCharacter()
    + "  grid=" + c.getInitialTilesX() + "x" + c.getInitialTilesY()
    + "  speed=" + nf(c.getWaveSpeed(), 1, 2)
    + "  bg=(" + bg[0] + "," + bg[1] + "," + bg[2] + ")");

  return c;
}
