/*
 * TextOnPath
 *
 * Demonstrates two GlyphExtractor capabilities:
 *
 *   textOnPath(String, PVector[], float, float)
 *       — Lays a string of glyphs along any polyline path,
 *         rotating each character to follow the local tangent.
 *         The fourth parameter controls letter-spacing (px per character).
 *         Here the path is the outer contour of a large letterform.
 *
 *   getTangent(char, float, float)
 *       — Returns a unit PVector tangent to the outline at
 *         arc-length parameter t ∈ [0, 1).
 *         Used in mode 2 to draw small ornamental chevrons
 *         that orbit the primary character's outline.
 *
 * Both operations are computed from the font's native vector outline
 * with no external libraries required.
 *
 * Layout:
 *   Mode 1  A string of characters flows around the outline of the
 *            base letter, rotating to follow the tangent at each point.
 *            Use the SPACING slider to adjust letter-spacing live.
 *
 *   Mode 2  Small directional ornaments (arrows built from getTangent)
 *            rotate continuously around the outline.
 *
 * Controls:
 *   1 / 2   Switch display mode
 *   SPACE   Cycle base character (the letterform used as path)
 *   T       Cycle the text string that wraps around the letter
 *   S       Save PNG
 */

import algorithmic.typography.render.GlyphExtractor;
import algorithmic.typography.ui.Slider;

GlyphExtractor glyph;

// ── Base characters (their OUTER CONTOUR becomes the path) ─────
char[] bases = { 'O', 'A', 'Q', 'G', 'S', 'D', 'B', 'R' };
int baseIdx  = 0;

// ── Strings that will flow around the outline ─────────────────
String[] texts = {
  "TYPOGRAPHY IS THE ART OF ARRANGING TYPE  ",
  "ABCDEFGHIJKLMNOPQRSTUVWXYZ  ",
  "THE QUICK BROWN FOX JUMPS OVER THE LAZY DOG  ",
  "FORM FOLLOWS FUNCTION  ",
  "0123456789 +-=÷×<>[]{}()  "
};
int textIdx = 0;

int mode = 1;
boolean savePNG = false;

// Large base character — the outline of this becomes the path
static final float BASE_SIZE = 760;
// Size of the characters placed along the path
static final float PATH_FONT = 22;
// Size of ornaments in mode 2
static final float ORNAMENT = 14;

// ── Letter-spacing slider (mode 1 only) ────────────────────
float pathSpacing = 0;  // extra px per character; 0 = natural advance
Slider sldSpacing;

// Animation
float t = 0;
float orbitT = 0;

void setup() {
  size(1080, 1080);
  colorMode(HSB, 360, 255, 255);
  glyph = new GlyphExtractor(this, "Helvetica", 72);
  glyph.setFlatness(0.25);  // finer outline → smoother path sampling

  // Spacing slider: -10 px (tight) → +40 px (loose), default 0
  sldSpacing = new Slider("Spacing", -10, 40, 0, 180)
                 .setPosition(20, 20)
                 .setDecimals(1)
                 .setSuffix(" px");
  sldSpacing.onChange(v -> pathSpacing = v);

  println("TextOnPath  1-2=mode  SPACE=base  T=text  S=save");
}

void draw() {
  background(15);
  t += 0.008;
  orbitT = (orbitT + 0.0025) % 1.0;  // 0→1 loop for ornament position

  char base = bases[baseIdx];
  float[] bounds = glyph.getBounds(base, BASE_SIZE);
  float originX = width  / 2 - bounds[0] - bounds[2] / 2;
  float originY = height / 2 - bounds[1] - bounds[3] / 2;

  // ── Ghost base letter ──────────────────────────────────────
  PShape ghost = glyph.extractChar(base, BASE_SIZE);
  ghost.disableStyle();
  noStroke();
  fill(0, 0, 40, 30);   // very dark ghost
  pushMatrix();
  translate(originX, originY);
  shape(ghost, 0, 0);
  popMatrix();

  // ── Mode dispatch ──────────────────────────────────────────
  if (mode == 1) {
    drawTextOnPath(base);
  } else {
    drawOrnamentsOnPath(base, BASE_SIZE, originX, originY);
  }

  drawHUD(base);

  // Spacing slider is only relevant in mode 1
  if (mode == 1) sldSpacing.display(g);

  if (savePNG) {
    save("textonpath-" + nf(frameCount, 5) + ".png");
    savePNG = false;
  }
}

// ══════════════════════════════════════════════════════════════
//  MODE 1 — Text flowing around the outer contour
// ══════════════════════════════════════════════════════════════

void drawTextOnPath(char base) {
  String txt = texts[textIdx];

  // Use the library's textOnPath() to compute a group PShape
  // that represents the text laid along the base contour.
  // The path fed to the method should be in glyph (local) space;
  // the library returns shapes in that same space, which we then
  // translate into screen space.
  PVector[] localPath = glyph.getOuterContour(base, BASE_SIZE);
  PShape textGroup = glyph.textOnPath(txt, localPath, PATH_FONT, pathSpacing);
  if (textGroup == null) return;

  // Animated hue shift
  float baseHue = (frameCount * 0.3 + 30) % 360;

  // Draw each child shape with a per-character hue offset
  int n = textGroup.getChildCount();
  for (int i = 0; i < n; i++) {
    PShape ch = textGroup.getChild(i);
    ch.disableStyle();

    // Characters near the current "highlight" position glow brighter
    float norm = (float) i / max(n - 1, 1);
    float glow = abs(sin(t * 1.5 - norm * PI * 2));
    float hue = (baseHue + i * 3.2 + 20) % 360;
    float brt = 140 + glow * 115;
    float sat = 160 + glow * 80;

    noStroke();
    fill(hue, (int) constrain(sat, 0, 255), (int) constrain(brt, 0, 255), 220);

    // The textOnPath result is in the same coordinate space as base glyph;
    // translate to screen origin of the base letter
    float[] bounds = glyph.getBounds(base, BASE_SIZE);
    float ox = width  / 2 - bounds[0] - bounds[2] / 2;
    float oy = height / 2 - bounds[1] - bounds[3] / 2;
    pushMatrix();
    translate(ox, oy);
    shape(ch, 0, 0);
    popMatrix();
  }
}

// ══════════════════════════════════════════════════════════════
//  MODE 2 — Directional ornaments orbiting via getTangent
// ══════════════════════════════════════════════════════════════

// Draws 48 small chevron/arrow ornaments distributed evenly around the
// outline. One group advances continuously (orbitT) giving a "flow" feel.
// The rotation of each ornament is taken from getTangent().

void drawOrnamentsOnPath(char base, float bSize, float ox, float oy) {
  int ornamentCount = 48;
  float arcStep = 1.0 / ornamentCount;

  float baseHue = (frameCount * 0.2 + 200) % 360;
  PVector[] localPath = glyph.getOuterContour(base, bSize);

  for (int i = 0; i < ornamentCount; i++) {
    float tParam = (orbitT + i * arcStep) % 1.0;

    // Position on the outline (arc-length parameterised, 0→1)
    PVector pos = samplePath(localPath, tParam);
    if (pos == null) continue;

    // Tangent direction at this point from the library
    PVector tan = glyph.getTangent(base, bSize, tParam);
    if (tan == null) continue;
    float angle = atan2(tan.y, tan.x);

    // Colour: wave along the orbit
    float wave = sin(tParam * TWO_PI * 3 + t * 2);
    float hue = (baseHue + i * 4.5 + wave * 30) % 360;
    float brt = 170 + wave * 70;
    float alpha = 160 + sin(tParam * TWO_PI + t) * 60;

    stroke(hue, 210, (int) constrain(brt, 80, 255), (int) constrain(alpha, 80, 255));
    strokeWeight(1.5);
    noFill();

    // Draw a small chevron pointing in the tangent direction
    pushMatrix();
    translate(ox + pos.x, oy + pos.y);
    rotate(angle);
    float sz = ORNAMENT * (0.7 + wave * 0.3);  // size breathes with wave
    // Chevron: two lines from the tip
    line(-sz, -sz * 0.5, 0, 0);
    line(-sz,  sz * 0.5, 0, 0);
    popMatrix();
  }

  // Also draw the outline itself faintly
  stroke(baseHue, 180, 200, 60);
  strokeWeight(1);
  noFill();
  beginShape();
  PVector[] sp = localPath;
  for (PVector v : sp) {
    vertex(ox + v.x, oy + v.y);
  }
  endShape(CLOSE);
}

// Linear-interpolation arc-length path sampler (t ∈ [0, 1])
PVector samplePath(PVector[] path, float t) {
  if (path == null || path.length < 2) return null;

  // Compute cumulative arc lengths
  float[] arcLen = new float[path.length];
  arcLen[0] = 0;
  for (int i = 1; i < path.length; i++) {
    float dx = path[i].x - path[i-1].x;
    float dy = path[i].y - path[i-1].y;
    arcLen[i] = arcLen[i-1] + sqrt(dx*dx + dy*dy);
  }

  float total  = arcLen[path.length - 1];
  float target = t * total;

  // Binary search
  int lo = 0, hi = path.length - 1;
  while (hi - lo > 1) {
    int mid = (lo + hi) / 2;
    if (arcLen[mid] <= target) lo = mid; else hi = mid;
  }
  if (arcLen[hi] <= arcLen[lo] + 0.0001) return path[lo].copy();
  float frac = (target - arcLen[lo]) / (arcLen[hi] - arcLen[lo]);
  return PVector.lerp(path[lo], path[hi], frac);
}

// ══════════════════════════════════════════════════════════════
//  HUD
// ══════════════════════════════════════════════════════════════

void drawHUD(char base) {
  textFont(createFont("Helvetica", 13));
  fill(0, 0, 255, 180);
  noStroke();
  String modeName = (mode == 1) ? "TEXT ON PATH" : "TANGENT ORNAMENTS";
  textAlign(LEFT, BOTTOM);
  text(modeName + "  base='" + base + "'", 20, height - 40);
  text("1=text mode  2=ornament mode  SPACE=base  T=text  S=save", 20, height - 20);
}

// ══════════════════════════════════════════════════════════════
//  INPUT
// ══════════════════════════════════════════════════════════════

void keyPressed() {
  switch (key) {
    case '1': mode = 1; break;
    case '2': mode = 2; break;
    case ' ': baseIdx = (baseIdx + 1) % bases.length; break;
    case 't': case 'T': textIdx = (textIdx + 1) % texts.length; break;
    case 's': case 'S': savePNG = true; break;
  }
}

void mousePressed()  { if (mode == 1) sldSpacing.mousePressed(mouseX, mouseY); }
void mouseDragged()  { if (mode == 1) sldSpacing.mouseDragged(mouseX, mouseY); }
void mouseReleased() { sldSpacing.mouseReleased(); }
