/*
 * CustomFont
 *
 * Shows how to use a custom font with the AlgorithmicTypography library.
 *
 * Processing's createFont() loads any font installed on the system or
 * any .ttf / .otf file placed in the sketch's "data" folder. Because
 * the library only calls textSize() each frame (never textFont()), the
 * font you set before rendering is preserved.
 *
 * This example cycles through several fonts so you can compare the
 * typographic texture each one produces.
 *
 * To use your own font file:
 *   1. Drop the .ttf or .otf into this sketch's data/ folder.
 *   2. Call  createFont("YourFont.ttf", 48)  in setup().
 *
 * Controls:
 *   F / click  — cycle to the next font
 *   1-5        — jump to a specific font
 *   C          — cycle the displayed character
 *   R          — restart animation
 */

import algorithmic.typography.*;

AlgorithmicTypography at;
Configuration config;

// ── Font list ─────────────────────────────────────────────────────
PFont[] fonts;
String[] fontNames;
int currentFont = 0;

// ── Characters to cycle through ───────────────────────────────────
String[] chars = { "A", "W", "g", "&", "@", "R" };
int currentChar = 0;

void setup() {
  size(1080, 1080);

  config = new Configuration();
  config.loadFromJSON(loadJSONObject("config.json"));

  at = new AlgorithmicTypography(this);
  at.setConfiguration(config);
  at.setAutoRender(false);
  at.initialize();

  // ── Create fonts ────────────────────────────────────────────────
  // System fonts (available on most machines):
  //   - On macOS: Helvetica Neue, Futura, Menlo, Georgia, Didot …
  //   - On Windows: Arial, Consolas, Cambria, Georgia, Impact …
  //   - To use a local file: place it in data/ and reference by filename.
  //
  // createFont(name, size) — size here is the *internal* resolution;
  // the library overrides the display size via textScale each frame,
  // so 48 px is a good baseline for crisp rendering.

  fontNames = new String[] {
    "Helvetica",          // clean sans-serif
    "Georgia",            // elegant serif
    "Courier New",        // monospaced
    "Futura",             // geometric sans
    "Didot"               // high-contrast serif
    // ← add your own .ttf filename here, e.g. "MyFont.ttf"
  };

  fonts = new PFont[fontNames.length];
  for (int i = 0; i < fontNames.length; i++) {
    fonts[i] = createFont(fontNames[i], 48);
  }

  applyFont(0);

  println("CustomFont — press F or click to cycle fonts, 1-5 to jump, C to change character");
}

void draw() {
  at.render();

  // ── HUD overlay ─────────────────────────────────────────────────
  fill(0, 160);
  noStroke();
  rect(0, height - 50, width, 50);

  fill(255);
  textFont(fonts[currentFont]);   // re-assert after render
  textAlign(CENTER, CENTER);
  textSize(16);
  text("Font: " + fontNames[currentFont]
     + "   Char: \"" + config.getCharacter() + "\""
     + "   [F] next  [1-" + fonts.length + "] jump  [C] char",
       width / 2, height - 25);
}

// ── Helpers ───────────────────────────────────────────────────────

void applyFont(int index) {
  currentFont = index % fonts.length;
  textFont(fonts[currentFont]);
  println("Font → " + fontNames[currentFont]);
}

void nextFont() {
  applyFont(currentFont + 1);
}

void nextChar() {
  currentChar = (currentChar + 1) % chars.length;
  config.setCharacter(chars[currentChar]);
  println("Character → " + chars[currentChar]);
}

// ── Input ─────────────────────────────────────────────────────────

void keyPressed() {
  if (key == 'f' || key == 'F') nextFont();
  if (key == 'c' || key == 'C') nextChar();
  if (key == 'r' || key == 'R') at.restart();

  // Number keys 1-5 jump to a font directly
  if (key >= '1' && key <= '0' + fonts.length) {
    applyFont(key - '1');
  }
}

void mousePressed() {
  nextFont();
}
