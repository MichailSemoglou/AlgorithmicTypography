/**
 * basic.js — Minimal Algorithmic Typography p5.js sketch
 *
 * Open index.html in a browser to see the animation.
 * Modify the config below or replace it with a JSON file.
 */

let at;

function setup() {
  // Create a square canvas (or use windowWidth/windowHeight for fullscreen)
  createCanvas(1080, 1080);

  // Build a configuration — all fields are optional; defaults match the Java library
  const config = new Configuration();
  config.character = "A";
  config.textScale = 0.8;
  config.waveSpeed = 1.0;
  config.waveAngle = 45;

  // HSB colour ranges (0 = achromatic by default)
  config.brightnessMin = 50;
  config.brightnessMax = 255;
  config.saturationMin = 0;
  config.saturationMax = 0;
  config.hueMin = 0;
  config.hueMax = 0;

  // Grid stages (tiles reduce over time)
  config.initialTilesX = 16;
  config.initialTilesY = 16;
  config.changedTilesX = 8;
  config.changedTilesY = 8;
  config.finalTilesX = 4;
  config.finalTilesY = 4;

  // Timing
  config.animationDuration = 18; // seconds
  config.animationFPS = 30;
  config.changeTime = 6000;
  config.secondChangeTime = 12000;
  config.fadeDuration = 2000;

  // Create the main library instance and initialise
  at = new AlgorithmicTypography(window, config);
  at.initialize();
}

function draw() {
  at.render();
}

// Resize canvas when the window changes
function windowResized() {
  resizeCanvas(windowWidth, windowHeight);
}
