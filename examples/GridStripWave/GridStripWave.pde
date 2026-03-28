/**
 * GridStripWave
 *
 * Demonstrates the new GridStripMotion class introduced in v0.2.6.
 *
 * GridStripMotion shifts every row (or column, or both) by a wave
 * function so the whole grid undulates like a ribbon. It is distinct
 * from CellMotion: where CellMotion moves individual glyphs based on
 * per-cell calculations, GridStripMotion displaces entire rows/columns
 * in unison for a smooth, banner-like sweep.
 *
 * ── Controls ──────────────────────────────────────────────────────────────
 *  1 / 2 / 3   Switch axis mode: 1 = ROW, 2 = COLUMN, 3 = BOTH
 *  UP / DOWN   Increase / decrease amplitude (range 0 – 1)
 *  LEFT/ RIGHT Decrease / increase phase step (controls wave frequency)
 *  W           Cycle row wave type  (SINE → SQUARE → TRIANGLE → SAWTOOTH)
 *  SPACE       Toggle animation pause
 *  R           Reset to defaults
 *
 * ── Config ────────────────────────────────────────────────────────────────
 *  The sketch also loads data/config.json on start so you can tweak
 *  the base typography parameters without touching this file.
 * ──────────────────────────────────────────────────────────────────────────
 *
 */

import algorithmic.typography.*;
import algorithmic.typography.core.*;

AlgorithmicTypography at;
GridStripMotion stripMotion;

// Wave type names for display cycling
final String[] WAVE_NAMES = {"SINE", "SQUARE", "TRIANGLE", "SAWTOOTH", "TANGENT"};
int rowWaveIndex = 0;
boolean paused = false;

// Live-tunable parameters
float amp = 0.40f;  // GridStripMotion amplitude  (0 – 1 designer range)
float phaseStep = 0.30f;  // Phase shift per strip
int axisMode = GridStripMotion.ROW;  // ROW | COLUMN | BOTH

void setup() {
  size(1080, 1080);
  frameRate(60);
  colorMode(HSB, 360, 255, 255);

  // ── Core library setup ──────────────────────────────────────────
  at = new AlgorithmicTypography(this);
  at.loadConfiguration("config.json");

  // ── GridStripMotion ─────────────────────────────────────────────
  stripMotion = new GridStripMotion();
  stripMotion
    .setAxis(axisMode)
    .setAmplitude(amp)
    .setPhaseStep(phaseStep)
    .setRowSpeed(1.0f)
    .setColumnSpeed(0.8f)
    .setRowWaveType("SINE")
    .setColumnWaveType("SINE");

  at.setGridStripMotion(stripMotion);
  at.setAutoRender(false);  // driven manually so pause (noLoop/loop) works correctly
}

void draw() {
  background(0);
  at.render();
  drawHUD();
}

// ── Key handling ──────────────────────────────────────────────────

void keyPressed() {
  if (keyCode == UP) {
    amp = min(1.0f, amp + 0.05f);
    stripMotion.setAmplitude(amp);

  } else if (keyCode == DOWN) {
    amp = max(0.0f, amp - 0.05f);
    stripMotion.setAmplitude(amp);

  } else if (keyCode == LEFT) {
    phaseStep = max(0.05f, phaseStep - 0.05f);
    stripMotion.setPhaseStep(phaseStep);

  } else if (keyCode == RIGHT) {
    phaseStep = min(2.0f, phaseStep + 0.05f);
    stripMotion.setPhaseStep(phaseStep);

  } else if (key == '1') {
    axisMode = GridStripMotion.ROW;
    stripMotion.setAxis(axisMode);

  } else if (key == '2') {
    axisMode = GridStripMotion.COLUMN;
    stripMotion.setAxis(axisMode);

  } else if (key == '3') {
    axisMode = GridStripMotion.BOTH;
    stripMotion.setAxis(axisMode);

  } else if (key == 'w' || key == 'W') {
    rowWaveIndex = (rowWaveIndex + 1) % WAVE_NAMES.length;
    stripMotion.setRowWaveType(WAVE_NAMES[rowWaveIndex]);
    stripMotion.setColumnWaveType(WAVE_NAMES[rowWaveIndex]);

  } else if (key == ' ') {
    paused = !paused;
    if (paused) { noLoop(); redraw(); }  // freeze + update HUD once
    else loop();

  } else if (key == 'r' || key == 'R') {
    amp = 0.40f;
    phaseStep = 0.30f;
    axisMode = GridStripMotion.ROW;
    rowWaveIndex = 0;
    stripMotion
      .setAxis(axisMode)
      .setAmplitude(amp)
      .setPhaseStep(phaseStep)
      .setRowWaveType("SINE")
      .setColumnWaveType("SINE");
  }
}

// ── HUD overlay ───────────────────────────────────────────────────

void drawHUD() {
  String axisLabel = axisMode == GridStripMotion.ROW    ? "ROW"
                   : axisMode == GridStripMotion.COLUMN ? "COLUMN"
                   :                                      "BOTH";

  colorMode(RGB, 255);
  fill(0, 160);
  noStroke();
  rect(10, 10, 320, 130, 6);

  fill(255);
  textSize(13);
  textAlign(LEFT, TOP);
  int lx = 18, ly = 18, ls = 17;
  text("GridStripWave [v0.2.6]", lx, ly);
  text("Axis: " + axisLabel + "  (1/2/3)", lx, ly + ls);
  text("Amp: " + nf(amp, 1, 2) + "  (UP/DOWN)", lx, ly + ls * 2);
  text("Phase: " + nf(phaseStep, 1, 2) + "  (LEFT/RIGHT)", lx, ly + ls * 3);
  text("Wave: " + WAVE_NAMES[rowWaveIndex] + "  (W)", lx, ly + ls * 4);
  text(paused ? "PAUSED  (SPACE to resume)" : "Playing  (SPACE to pause)", lx, ly + ls * 5);

  colorMode(HSB, 360, 255, 255);
}
