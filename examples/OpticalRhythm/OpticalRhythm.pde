/**
 * OpticalRhythm — v0.3.0 Example
 *
 * Demonstrates setRhythmFromFont(char): wave speed is derived from the actual
 * geometry of the chosen letterform. Characters with large counter-forms (O, G, B)
 * produce faster rhythms; narrow strokes (I, l, 1) produce slower ones. A rounded
 * serif like 'a' sits between these extremes.
 *
 * The algorithm samples strokeWeight and counterRatio via GlyphExtractor at 600px,
 * then computes:
 *
 *   freq = (0.5 + counterRatio) / (1.0 + strokeWeight / 80.0) * rhythmScale
 *
 * This makes the wave literally breathe at the pace implied by each letterform's
 * optical weight and openness — heavier, darker letters pulse slower; open, airy
 * ones pulse faster.
 *
 * Controls
 *   ← / →    : cycle through character set
 *   + / -    : adjust rhythmScale (multiplier applied to computed freq)
 *   S        : save current frame as PNG
 */

import algorithmic.typography.*;
import algorithmic.typography.core.*;

AlgorithmicTypography at;
Configuration config;

String[] charSet = {
  "O", "G", "B", "a", "g", "e", "m", "H", "I", "1", "S", "R"
};
int charIndex = 0;

float computedSpeed = 0f;

void setup() {
  size(1080, 1080, P2D);
  colorMode(RGB, 255);

  config = new Configuration();

  at = new AlgorithmicTypography(this);
  at.setConfiguration(config);
  at.loadConfiguration("config.json");
  at.initialize();

  config.setCharacter(charSet[charIndex]);
  applyRhythm();

  println("OpticalRhythm example loaded.");
  println("  ←/→ : cycle chars   +/- : rhythmScale   S : save");
}

void draw() {
  background(config.getBackgroundRed(),
             config.getBackgroundGreen(),
             config.getBackgroundBlue());
  at.render();

  // HUD
  fill(255, 200);
  noStroke();
  textSize(13);
  textAlign(LEFT, TOP);
  text("Char: " + charSet[charIndex]
       + "   WaveSpeed: " + nf(computedSpeed, 1, 3)
       + "   RhythmScale: " + nf(config.getRhythmScale(), 1, 2),
       16, 16);

  // Speed bar — mapped to a 0–2 range that covers all possible auto values
  noFill();
  stroke(100);
  rect(16, 40, 300, 10);
  float barW = map(computedSpeed, 0f, 2f, 0, 300);
  noStroke();
  fill(80, 200, 255);
  rect(16, 40, barW, 10);
}

void applyRhythm() {
  char c = charSet[charIndex].charAt(0);
  at.setRhythmFromFont(c);
  computedSpeed = config.getWaveSpeed();
  println("Rhythm for '" + c + "' -> speed=" + nf(computedSpeed, 1, 4));
}

void keyPressed() {
  if (keyCode == RIGHT) {
    charIndex = (charIndex + 1) % charSet.length;
    config.setCharacter(charSet[charIndex]);
    applyRhythm();

  } else if (keyCode == LEFT) {
    charIndex = (charIndex - 1 + charSet.length) % charSet.length;
    config.setCharacter(charSet[charIndex]);
    applyRhythm();

  } else if (key == '+' || key == '=') {
    config.setRhythmScale(config.getRhythmScale() + 0.1f);
    applyRhythm();
    println("RhythmScale -> " + nf(config.getRhythmScale(), 1, 2));

  } else if (key == '-') {
    config.setRhythmScale(max(0.1f, config.getRhythmScale() - 0.1f));
    applyRhythm();
    println("RhythmScale -> " + nf(config.getRhythmScale(), 1, 2));

  } else if (key == 'S' || key == 's') {
    saveFrame("optical_rhythm_###.png");
  }
}
