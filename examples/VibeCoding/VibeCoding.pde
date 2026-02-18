/*
 * VibeCoding
 *
 * Natural-language configuration: press a number key
 * to set a mood and the library adjusts wave speed,
 * colours, and grid density automatically.
 *
 * 1 calm meditation   4 chaotic energy
 * 2 techno rave       5 ocean waves
 * 3 melancholic rain  R restart
 */

import algorithmic.typography.*;
import algorithmic.typography.system.VibePreset;

AlgorithmicTypography at;
String currentVibe = "balanced";

void setup() {
  size(1080, 1080);

  at = new AlgorithmicTypography(this);
  at.setAutoRender(false);
  at.getConfiguration().setSaveFrames(false);
  VibePreset.apply(at.getConfiguration(), "balanced");
  at.initialize();

  println("VibeCoding — press 1-5 to change mood");
}

void draw() {
  at.render();

  fill(255);
  textAlign(LEFT, TOP);
  textSize(14);
  text("Vibe: " + currentVibe, 10, 10);
}

void keyPressed() {
  switch (key) {
    case '1': currentVibe = "calm meditation";  break;
    case '2': currentVibe = "techno rave 3am";  break;
    case '3': currentVibe = "melancholic rain";  break;
    case '4': currentVibe = "chaotic energy";    break;
    case '5': currentVibe = "ocean waves";       break;
    case 'r': case 'R': at.restart(); return;
    default: return;
  }

  VibePreset.apply(at.getConfiguration(), currentVibe);
  println("Vibe: " + currentVibe);
}
