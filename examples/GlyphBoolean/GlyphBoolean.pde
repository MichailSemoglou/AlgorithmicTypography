/*
 * GlyphBoolean
 *
 * Demonstrates two families of boolean Area operations in GlyphExtractor:
 *
 *   Letter × Letter  — union / intersect / subtract between any two characters
 *   Shape  × Letter  — union / intersect / subtract between a geometric
 *                       primitive (circle, rectangle, rounded rectangle) and
 *                       a letterform — letter punched through a shape, a letter
 *                       sliced by a rectangle, and more.
 *
 * All operations are computed directly from AWT Area boolean algebra on the
 * font's native outlines. No external dependency required.
 *
 * Controls:
 *   TAB        Toggle mode: Letter×Letter  ↔  Shape×Letter
 *
 *   In Letter×Letter mode:
 *     1 / 2 / 3    Union / Intersect / Subtract
 *     SPACE        Cycle character pairs
 *
 *   In Shape×Letter mode:
 *     1 / 2 / 3    Union / Intersect / Subtract (shape minus letter)
 *     4            Subtract (letter minus shape)
 *     SPACE        Cycle primitive shapes
 *     LEFT / RIGHT Cycle letters
 *
 *   S    Save PNG (both modes)
 */

import algorithmic.typography.render.GlyphExtractor;
import java.awt.geom.Area;

GlyphExtractor glyph;

// ── Shared ────────────────────────────────────────────────────
static final float FONT_SIZE = 600;
int    mode     = 1;      // 1=union  2=intersect  3=subtract  4=subtract-reverse
boolean glyphMode = true; // true = Letter×Letter, false = Shape×Letter
boolean savePNG = false;
float   t       = 0;

// ── Letter × Letter ───────────────────────────────────────────
// Pairs chosen for interesting boolean geometry
char[][] pairs = {
  { 'O', 'C' },
  { 'B', 'E' },
  { 'A', 'V' },
  { 'P', 'D' },
  { 'R', 'F' },
  { 'S', '5' }
};
int pairIdx  = 0;

// ── Shape × Letter ────────────────────────────────────────────
String[] shapeNames = { "Circle", "Rectangle", "Rounded Rect" };
int shapeIdx = 0;
char[] letters = { 'A', 'B', 'E', 'G', 'O', 'P', 'R', 'S', '8', '&' };
int letterIdx = 0;

// ─────────────────────────────────────────────────────────────

void setup() {
  size(1080, 1080);
  colorMode(HSB, 360, 255, 255);
  glyph = new GlyphExtractor(this, "Helvetica", 72);
  glyph.setFlatness(0.35);
  textFont(createFont("Helvetica", 13));
  println("GlyphBoolean  TAB=mode  1-4=op  SPACE=cycle  ←/→=letter  S=save");
}

void draw() {
  background(0);
  t += 0.012;

  if (glyphMode) drawLetterLetterMode();
  else           drawShapeLetterMode();

  drawHUD();

  if (savePNG) {
    saveFrame("glyphboolean-####.png");
    savePNG = false;
  }
}

// ══════════════════════════════════════════════════════════════
//  LETTER × LETTER MODE
// ══════════════════════════════════════════════════════════════

void drawLetterLetterMode() {
  char a = pairs[pairIdx][0];
  char b = pairs[pairIdx][1];

  // Centre using the combined bounding box of both glyphs (both sit at origin 0)
  float[] bA = glyph.getBounds(a, FONT_SIZE);
  float[] bB = glyph.getBounds(b, FONT_SIZE);
  float minX = min(bA[0], bB[0]);
  float minY = min(bA[1], bB[1]);
  float maxX = max(bA[0] + bA[2], bB[0] + bB[2]);
  float maxY = max(bA[1] + bA[3], bB[1] + bB[3]);
  float ox = width  / 2 - (minX + maxX) / 2;
  float oy = height / 2 - (minY + maxY) / 2;

  // Ghost inputs — both letters overlap at the same origin (as the boolean op does)
  noStroke();
  fill(0, 0, 60, 40);
  PShape ga = glyph.extractChar(a, FONT_SIZE);
  ga.disableStyle();
  pushMatrix(); translate(ox, oy); shape(ga, 0, 0); popMatrix();

  PShape gb = glyph.extractChar(b, FONT_SIZE);
  gb.disableStyle();
  pushMatrix(); translate(ox, oy); shape(gb, 0, 0); popMatrix();

  // Boolean result
  float hue = (frameCount * 0.25 + 180) % 360;
  float bri  = 200 + sin(t) * 40;
  noStroke();
  fill(hue, 190, (int)bri, 230);

  PShape result;
  switch (mode) {
    case 1: result = glyph.union(a, b, FONT_SIZE);     break;
    case 2: result = glyph.intersect(a, b, FONT_SIZE); break;
    default: result = glyph.subtract(a, b, FONT_SIZE); break;
  }
  result.disableStyle();
  pushMatrix();
  translate(ox, oy);
  shape(result, 0, 0);
  popMatrix();
}

// ══════════════════════════════════════════════════════════════
//  SHAPE × LETTER MODE
// ══════════════════════════════════════════════════════════════

void drawShapeLetterMode() {
  char ch  = letters[letterIdx];
  float[] b = glyph.getBounds(ch, FONT_SIZE);

  float gl = b[0], gt = b[1], gw = b[2], gh = b[3];
  float gcx = gl + gw / 2;
  float gcy = gt + gh / 2;

  // Drawing origin — centre the glyph on canvas
  float ox = width  / 2 - gcx;
  float oy = height / 2 - gcy;

  // Modes 1-3 use a large outer shape that generously contains the glyph.
  // Mode 4 uses a narrow horizontal band that reliably slices through every
  // letter's strokes — using the same outer shape would subtract the entire
  // letter and produce nothing visible.
  Area prim = (mode == 4)
    ? buildCutter(shapeIdx, gl, gw, gh, gcx, gcy)
    : buildOuter(shapeIdx, gl, gt, gw, gh, gcx, gcy);

  // Ghost: primitive outline
  noFill();
  stroke(0, 0, 100, 50);
  strokeWeight(1.5);
  PShape primShape = glyph.areaToShape(prim);
  primShape.disableStyle();
  pushMatrix(); translate(ox, oy); shape(primShape, 0, 0); popMatrix();

  // Ghost: letter fill
  noStroke();
  fill(0, 0, 60, 35);
  PShape ghost = glyph.extractChar(ch, FONT_SIZE);
  ghost.disableStyle();
  pushMatrix(); translate(ox, oy); shape(ghost, 0, 0); popMatrix();

  // Boolean result
  float hue = (frameCount * 0.25 + 30) % 360;
  float bri  = 200 + sin(t) * 40;
  noStroke();
  fill(hue, 190, (int)bri, 230);

  PShape result;
  switch (mode) {
    case 1:  result = glyph.union(prim, ch, FONT_SIZE);     break;
    case 2:  result = glyph.intersect(prim, ch, FONT_SIZE); break;
    case 4:  result = glyph.subtract(ch, FONT_SIZE, prim);  break;
    default: result = glyph.subtract(prim, ch, FONT_SIZE);  break;
  }
  result.disableStyle();
  pushMatrix(); translate(ox, oy); shape(result, 0, 0); popMatrix();
}

// Large outer primitive (modes 1/2/3).
// Uses circumradius × 1.30 for the circle so the shape is guaranteed to fully
// contain the glyph regardless of letter proportions.
Area buildOuter(int idx, float gl, float gt, float gw, float gh, float gcx, float gcy) {
  float pad = min(gw, gh) * 0.22f;
  switch (idx) {
    case 1: // Rectangle — generous padding on all sides
      return glyph.rectArea(gl - pad, gt - pad, gw + pad * 2, gh + pad * 2);
    case 2: { // Rounded rectangle
      float arc = min(gw, gh) * 0.32f;
      return glyph.roundedRectArea(gl - pad, gt - pad,
                                   gw + pad * 2, gh + pad * 2, arc, arc);
    }
    default: { // Circle: circumradius × 1.30
      float r = (float) Math.sqrt(gw * gw / 4.0 + gh * gh / 4.0) * 1.30f;
      return glyph.circleArea(gcx, gcy, r);
    }
  }
}

// Horizontal-band cutter (mode 4): spans the full glyph width at 45 % of height,
// centred vertically — always intersects the opaque strokes of every letter.
// Shaped as ellipse / rounded-rect / rect to match the active shape family.
Area buildCutter(int idx, float gl, float gw, float gh,
                 float gcx, float gcy) {
  float pad = min(gw, gh) * 0.12f;
  float bw  = gw + pad * 2;
  float bh  = gh * 0.45f;
  float bx  = gl - pad;
  float by  = gcy - bh / 2;
  switch (idx) {
    case 1:
      return glyph.rectArea(bx, by, bw, bh);
    case 2: {
      float arc = bh * 0.38f;
      return glyph.roundedRectArea(bx, by, bw, bh, arc, arc);
    }
    default: // Horizontal ellipse
      return glyph.ellipseArea(gcx, gcy, bw, bh);
  }
}

// ══════════════════════════════════════════════════════════════
//  HUD
// ══════════════════════════════════════════════════════════════

void drawHUD() {
  colorMode(RGB, 255);
  fill(200, 200, 200, 180);
  noStroke();
  textSize(13);
  textAlign(LEFT, BOTTOM);

  String modeName;
  if (mode == 1)      modeName = "UNION";
  else if (mode == 2) modeName = "INTERSECT";
  else if (mode == 4) modeName = "CUTOUT (letter \u2212 band)";
  else                modeName = "SUBTRACT (shape \u2212 letter)";

  if (glyphMode) {
    char a = pairs[pairIdx][0], b = pairs[pairIdx][1];
    text("Letter\u00d7Letter  \u2014  " + modeName + "   " + a + " \u25cc " + b, 20, height - 40);
    text("TAB=mode  1=union  2=intersect  3=subtract  SPACE=pair  S=save", 20, height - 20);
  } else {
    char ch = letters[letterIdx];
    text("Shape\u00d7Letter  \u2014  " + modeName + "   " + shapeNames[shapeIdx] + " \u25cc '" + ch + "'", 20, height - 40);
    text("TAB=mode  1=union  2=intersect  3=subtract  4=cutout  SPACE=shape  \u2190/\u2192=letter  S=save", 20, height - 20);
  }
  colorMode(HSB, 360, 255, 255);
}

// ══════════════════════════════════════════════════════════════
//  INPUT
// ══════════════════════════════════════════════════════════════

void keyPressed() {
  if (key == TAB) {
    glyphMode = !glyphMode;
    mode = 1;
  } else if (key == '1') { mode = 1; }
  else if (key == '2') { mode = 2; }
  else if (key == '3') { mode = 3; }
  else if (key == '4') { mode = 4; }
  else if (key == ' ') {
    if (glyphMode) pairIdx  = (pairIdx  + 1) % pairs.length;
    else           shapeIdx = (shapeIdx + 1) % shapeNames.length;
  }
  else if (key == 's' || key == 'S') { savePNG = true; }

  if (!glyphMode) {
    if (keyCode == LEFT)  letterIdx = (letterIdx - 1 + letters.length) % letters.length;
    if (keyCode == RIGHT) letterIdx = (letterIdx + 1) % letters.length;
  }
}
