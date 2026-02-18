/*
 * CulturalStyles
 *
 * Switch between cultural design-system presets.
 * All styles use "A" so you can compare the grid,
 * motion, and brightness character of each tradition.
 *
 * Controls:
 *   1 SWISS            5 JAPANESE_MINIMAL
 *   2 BAUHAUS          6 DECONSTRUCT
 *   3 CHINESE_INK      7 BRUTALIST
 *   4 ARABIC_KUFI      8 MEMPHIS
 */

import algorithmic.typography.*;
import algorithmic.typography.system.DesignSystem;

AlgorithmicTypography at;
String currentLabel = "1 — Swiss";

void setup() {
  size(1080, 1080);

  at = new AlgorithmicTypography(this);
  at.setAutoRender(false);
  applyStyle(DesignSystem.SystemType.SWISS);
  at.initialize();

  println("CulturalStyles — press 1-8 to switch");
}

void draw() {
  at.render();

  // HUD label
  fill(255);
  noStroke();
  float tw = textWidth(currentLabel) + 24;
  rect(14, 14, tw, 28, 6);
  fill(0);
  textAlign(LEFT, CENTER);
  textSize(14);
  text(currentLabel, 26, 28);
}

void applyStyle(DesignSystem.SystemType style) {
  DesignSystem.apply(at.getConfiguration(), style);
  at.getConfiguration().setCharacter("A");
}

void keyPressed() {
  DesignSystem.SystemType style = null;
  String label = null;

  switch (key) {
    case '1': style = DesignSystem.SystemType.SWISS;            label = "1 — Swiss";             break;
    case '2': style = DesignSystem.SystemType.BAUHAUS;          label = "2 — Bauhaus";           break;
    case '3': style = DesignSystem.SystemType.CHINESE_INK;      label = "3 — Chinese Ink";       break;
    case '4': style = DesignSystem.SystemType.ARABIC_KUFI;      label = "4 — Arabic Kufi";       break;
    case '5': style = DesignSystem.SystemType.JAPANESE_MINIMAL; label = "5 — Japanese Minimal";  break;
    case '6': style = DesignSystem.SystemType.DECONSTRUCT;      label = "6 — Deconstruct";       break;
    case '7': style = DesignSystem.SystemType.BRUTALIST;        label = "7 — Brutalist";         break;
    case '8': style = DesignSystem.SystemType.MEMPHIS;          label = "8 — Memphis";           break;
  }

  if (style != null) {
    applyStyle(style);
    currentLabel = label;
    println("Style: " + label + " — " + DesignSystem.getDescription(style));
  }
}
