/**
 * Counterpoint — v0.3.0 Example
 *
 * Two independent wave systems animate the same glyph simultaneously:
 *
 *   Outer letterform  — slow SINE wave, greyscale, angle 45°
 *   Inner counter-form — fast SAWTOOTH wave, saturated colour, angle 135°
 *
 * Because the two waves run at different speeds, with different shapes and
 * perpendicular directions, the positive and negative space of each letter
 * appear to move as separate animated entities — typographic counterpoint.
 *
 * Characters with enclosed counter-forms (O, B, R, P, D, g, e, @) show the
 * full effect. Use large type so the counter-form carries enough area to
 * reveal its own wave.
 *
 * Controls
 *   C / c  : cycle characters
 *   S      : save current frame as PNG
 */

import algorithmic.typography.*;
import algorithmic.typography.core.*;
import algorithmic.typography.render.*;

AlgorithmicTypography at;
CounterpointEngine counterpoint;

Configuration mainCfg;
Configuration counterCfg;
WaveEngine mainWave;
WaveEngine counterWave;

char[] chars = { 'O', 'B', 'R', 'P', 'D', 'g', 'e', '@' };
int charIdx = 0;

void setup() {
  size(1080, 1080, P2D);
  colorMode(RGB, 255);

  // ── Outer letterform: slow, smooth, greyscale ──────────────────────────
  mainCfg = new Configuration();
  mainCfg.setCanvasSize(width, height)
         .setGridSize(2, 2, 2, 2)
         .setWaveSpeed(0.7f).setWaveAngle(45f).setWaveType("SINE")
         .setWaveAmplitudeRange(-200, 200)
         .setBrightnessRange(30, 255)
         .setSaturationRange(0, 0)
         .setHueRange(0, 0)
         .setBackgroundColor(0)
         .setCharacter(String.valueOf(chars[charIdx]))
         .setTextScale(0.88f);

  // ── Inner counter-form: fast, sharp, warm colour ────────────────────────
  counterCfg = new Configuration();
  counterCfg.setCanvasSize(width, height)
             .setWaveSpeed(2.4f).setWaveAngle(135f).setWaveType("SAWTOOTH")
             .setWaveAmplitudeRange(-250, 250)
             .setBrightnessRange(160, 255)
             .setSaturationRange(200, 255)
             .setHueRange(15, 45);

  mainWave = new WaveEngine(mainCfg);
  counterWave = new WaveEngine(counterCfg);

  counterpoint = new CounterpointEngine(mainWave, counterWave);

  at = new AlgorithmicTypography(this);
  at.setConfiguration(mainCfg);
  at.setCounterpointEngine(counterpoint);
  at.setAutoRender(false);
  at.initialize();

  println("Counterpoint  C/c: cycle characters   S: save");
}

void draw() {
  background(0);
  at.render();
}

void keyPressed() {
  if (key == 'c' || key == 'C') {
    charIdx = (charIdx + 1) % chars.length;
    mainCfg.setCharacter(String.valueOf(chars[charIdx]));
    println("Character -> " + chars[charIdx]);
  } else if (key == 'S' || key == 's') {
    saveFrame("counterpoint_###.png");
  }
}
