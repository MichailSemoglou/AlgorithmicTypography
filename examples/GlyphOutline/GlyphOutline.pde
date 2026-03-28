/*
 * GlyphOutline
 *
 * Demonstrates solid and dashed stroke outlines on letterforms — something
 * Processing's built-in text() function cannot do at all: stroke colour is
 * silently ignored when drawing text.
 *
 * GlyphExtractor solves this by exposing the raw AWT outline geometry.
 * The "glyphOutline" block in config.json enables either style:
 *
 *   "style": "solid"   — continuous stroke drawn via extractChar() + PShape
 *   "style": "dashed"  — per-contour dash walk via getDashedOutline()
 *                        each sub-contour (outer + inner counter-forms)
 *                        is walked independently — no cross-connector line.
 *
 * Outline parameters (all read from config.json on startup):
 *   r, g, b       — stroke colour (RGB 0–255)
 *   weight        — stroke weight in pixels
 *   dashLength    — visible dash length (dashed only)
 *   gapLength     — gap length (dashed only)
 *
 * Controls:
 *   O          Cycle outline mode: None → Solid → Dashed
 *   LEFT/RIGHT Cycle characters
 *   S          Save PNG
 */

import algorithmic.typography.*;

AlgorithmicTypography at;

// Characters chosen to show outer + inner counter-form geometry
char[] chars = { 'A', 'B', 'D', 'G', 'O', 'P', 'Q', 'R',
                 'a', 'b', 'e', 'g', 'o', 'p', '&', '8' };
int charIdx  = 0;

final int[] OUTLINE_STYLES = {
  Configuration.OUTLINE_NONE,
  Configuration.OUTLINE_SOLID,
  Configuration.OUTLINE_DASHED,
  Configuration.OUTLINE_DASHED_ONLY
};
final String[] OUTLINE_LABELS = { "None", "Solid", "Dashed", "Dashed Only" };
int outlineIdx = 2;  // default: OUTLINE_DASHED (matches config.json "style":"dashed")

void setup() {
  size(1080, 1080);

  at = new AlgorithmicTypography(this);
  at.loadConfiguration("config.json");
  at.setAutoRender(false);
  at.initialize();

  println("GlyphOutline — O=outline mode  ←/→=character  S=save");
}

void draw() {
  // Push the active character into config, then render one frame
  at.getConfiguration().setCharacter(String.valueOf(chars[charIdx]));
  at.render();
  drawHUD();
}

void drawHUD() {
  fill(200, 200, 200, 170);
  noStroke();
  textSize(13);
  textAlign(LEFT, BOTTOM);
  text("'" + chars[charIdx] + "'  Outline: " + OUTLINE_LABELS[outlineIdx], 20, height - 40);
  text("O=outline mode  \u2190/\u2192=character  S=save", 20, height - 20);
}

void keyPressed() {
  if (key == 'o' || key == 'O') {
    outlineIdx = (outlineIdx + 1) % OUTLINE_STYLES.length;
    at.getConfiguration().setGlyphOutlineStyle(OUTLINE_STYLES[outlineIdx]);
    println("Outline: " + OUTLINE_LABELS[outlineIdx]);
  } else if (key == 's' || key == 'S') {
    saveFrame("glyphoutline-####.png");
  }
  if (keyCode == LEFT)  charIdx = (charIdx - 1 + chars.length) % chars.length;
  if (keyCode == RIGHT) charIdx = (charIdx + 1) % chars.length;
}

