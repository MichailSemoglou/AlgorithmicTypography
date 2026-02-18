/**
 * interactive.js — Interactive demo with keyboard / mouse controls.
 *
 * Press 1-5 to switch wave type, click to randomise colours.
 */

let at;
let currentType = "TANGENT";

function setup() {
  createCanvas(windowWidth, windowHeight);

  const config = new Configuration();
  config.character = "W";
  config.textScale = 0.75;
  config.waveSpeed = 1.2;
  config.waveAngle = 30;

  // Colourful defaults
  config.hueMin = 0;
  config.hueMax = 360;
  config.saturationMin = 120;
  config.saturationMax = 255;
  config.brightnessMin = 80;
  config.brightnessMax = 255;

  config.initialTilesX = 20;
  config.initialTilesY = 20;
  config.changedTilesX = 10;
  config.changedTilesY = 10;
  config.finalTilesX = 5;
  config.finalTilesY = 5;

  config.changeTime = 8000;
  config.secondChangeTime = 16000;
  config.fadeDuration = 2500;

  at = new AlgorithmicTypography(window, config);
  at.initialize();
}

function draw() {
  at.render();
}

function windowResized() {
  resizeCanvas(windowWidth, windowHeight);
}

function keyPressed() {
  const types = [
    WavePresets.Type.SINE,
    WavePresets.Type.TANGENT,
    WavePresets.Type.SQUARE,
    WavePresets.Type.TRIANGLE,
    WavePresets.Type.SAWTOOTH,
  ];
  const idx = parseInt(key) - 1;
  if (idx >= 0 && idx < types.length) {
    currentType = types[idx];
    at.setWaveType(currentType);
    console.log("Wave type →", Object.keys(WavePresets.Type)[idx]);
  }
}

function mousePressed() {
  const cfg = at.getConfiguration();
  cfg.hueMin = floor(random(0, 180));
  cfg.hueMax = floor(random(cfg.hueMin + 60, 360));
  cfg.saturationMin = floor(random(80, 160));
  cfg.saturationMax = floor(random(cfg.saturationMin + 40, 255));
  console.log(
    `Colours → hue ${cfg.hueMin}-${cfg.hueMax}, sat ${cfg.saturationMin}-${cfg.saturationMax}`,
  );
}
