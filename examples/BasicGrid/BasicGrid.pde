/*
 * BasicGrid
 *
 * Simplest usage of the AlgorithmicTypography library.
 * Loads a configuration from a JSON file and renders a
 * three-stage animation with smooth fade transitions:
 *
 *   Stage 1 (0-6s)    — 8x16 grid (beginning)
 *   Stage 2 (6-12s)   — 4x8 grid  (middle)
 *   Stage 3 (12-18s)  — 2x4 grid  (final)
 *
 * Each transition cross-fades over 2 seconds.
 * The word "HELLO" is rendered across all cells.
 *
 * Controls:
 *   R - Restart the animation
 *   S - Toggle frame saving
 *   B - Cycle cell border mode:
 *         None → Horizontal rules (top + bottom) → All four sides
 */

import algorithmic.typography.*;

AlgorithmicTypography at;

// Border modes cycled by the B key
final int[] BORDER_MODES = {
  Configuration.BORDER_NONE,
  Configuration.BORDER_TOP | Configuration.BORDER_BOTTOM,
  Configuration.BORDER_ALL
};
final String[] BORDER_LABELS = { "None", "Horizontal rules", "All sides" };
int borderIdx = 2;  // start on BORDER_ALL to match config.json default

void setup() {
  size(800, 800);

  at = new AlgorithmicTypography(this);
  at.loadConfiguration("config.json");
  at.setAutoRender(false);
  at.initialize();

  println("BasicGrid — press R to restart, S to toggle saving, B to cycle borders");
}

void draw() {
  at.render();
}

void keyPressed() {
  if (key == 'r' || key == 'R') {
    at.restart();
  } else if (key == 's' || key == 'S') {
    at.toggleFrameSaving();
  } else if (key == 'b' || key == 'B') {
    borderIdx = (borderIdx + 1) % BORDER_MODES.length;
    at.getConfiguration().setCellBorderSides(BORDER_MODES[borderIdx]);
    println("Cell border: " + BORDER_LABELS[borderIdx]);
  }
}
