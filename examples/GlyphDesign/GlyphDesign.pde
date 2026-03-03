/*
 * GlyphDesign
 *
 * Demonstrates three designer-oriented GlyphExtractor methods new in v0.2.2:
 *
 *   fillWithPoints()         — scattered points inside the letterform interior
 *   distributeAlongOutline() — evenly spaced points along the full perimeter
 *   getOuterContour()        — the outer boundary contour only
 *   getInnerContours()       — counter-forms only (holes in 'B', 'O', 'P', 'R')
 *
 * Unlike a raw vertex dump, these methods give you meaningful geometric
 * structure: interior vs. boundary, outer vs. inner, uniform vs.
 * density-variable sampling — the building blocks for stippling, flow-field
 * seeding, necklace effects, and counter-aware colouring.
 *
 * Controls:
 *   1    Interior fill        (fillWithPoints)
 *   2    Perimeter dots       (distributeAlongOutline)
 *   3    Outer + inner        (getOuterContour / getInnerContours)
 *   SPACE  Cycle characters
 *   UP / DOWN  Adjust point count
 *   S    Save PNG
 */

import algorithmic.typography.render.GlyphExtractor;
import java.util.List;

GlyphExtractor glyph;

int     mode    = 1;
char[]  chars   = { 'B', 'O', 'R', 'P', 'A', 'S', 'g', '&' };
int     charIdx = 0;
int     ptCount = 600;
boolean savePNG = false;
float   t       = 0;

void setup() {
  size(1080, 1080);
  colorMode(HSB, 360, 255, 255);
  glyph = new GlyphExtractor(this, "Helvetica", 72);
  glyph.setFlatness(0.3);
  println("GlyphDesign  1-3=mode  SPACE=char  UP/DOWN=point count  S=save");
}

void draw() {
  background(0);
  t += 0.015;
  char ch = chars[charIdx];

  switch (mode) {
    case 1: drawInteriorFill(ch);  break;
    case 2: drawPerimeterDots(ch); break;
    case 3: drawOuterInner(ch);    break;
  }

  drawHUD(ch);
  if (savePNG) {
    save("glyphdesign-" + nf(frameCount, 5) + ".png");
    savePNG = false;
  }
}

// ── Mode 1: fillWithPoints ────────────────────────────────────────────────
void drawInteriorFill(char ch) {
  PVector[] pts = glyph.fillWithPoints(ch, 800, ptCount);
  PVector   o   = glyph.centerOf(ch, 800, width / 2, height / 2);

  noStroke();
  for (int i = 0; i < pts.length; i++) {
    float hue = (map(i, 0, pts.length, 180, 300) + t * 20) % 360;
    float sz  = 2.5 + sin(t + i * 0.07) * 1.2;
    fill(hue, 200, 255, 220);
    ellipse(o.x + pts[i].x, o.y + pts[i].y, sz, sz);
  }
}

// ── Mode 2: distributeAlongOutline ───────────────────────────────────────
void drawPerimeterDots(char ch) {
  PVector[] pts = glyph.distributeAlongOutline(ch, 800, ptCount);
  PVector   o   = glyph.centerOf(ch, 800, width / 2, height / 2);

  noStroke();
  for (int i = 0; i < pts.length; i++) {
    float phase = (float) i / pts.length;
    float hue   = (phase * 360 + t * 40) % 360;
    float sz    = 3 + sin(t * 2 + phase * TWO_PI * 4) * 1.5;
    fill(hue, 220, 255, 230);
    ellipse(o.x + pts[i].x, o.y + pts[i].y, sz, sz);
  }
}

// ── Mode 3: getOuterContour + getInnerContours ───────────────────────────
void drawOuterInner(char ch) {
  PVector[]       outer = glyph.getOuterContour(ch, 800);
  List<PVector[]> inner = glyph.getInnerContours(ch, 800);
  PVector         o     = glyph.centerOf(ch, 800, width / 2, height / 2);

  strokeWeight(1.5);
  noFill();

  // Outer boundary - cyan-blue, closed
  stroke((200 + t * 30) % 360, 200, 255);
  beginShape();
  for (PVector p : outer) vertex(o.x + p.x, o.y + p.y);
  endShape(CLOSE);

  // Inner contours (counter-forms) - warm orange, each closed
  stroke((25 + t * 20) % 360, 220, 255);
  for (PVector[] contour : inner) {
    beginShape();
    for (PVector p : contour) vertex(o.x + p.x, o.y + p.y);
    endShape(CLOSE);
  }

  fill(0, 0, 200);
  noStroke();
  textSize(14);
  textAlign(CENTER);
  text("outer: 1   counters: " + inner.size(), width / 2, height - 50);
}


// ── HUD ───────────────────────────────────────────────────────────────────
void drawHUD(char ch) {
  String[] labels = {
    "", "fillWithPoints", "distributeAlongOutline",
    "getOuterContour + getInnerContours"
  };
  fill(0, 0, 200);
  noStroke();
  textAlign(LEFT);
  textSize(14);
  text(labels[mode] + "  |  '" + ch + "'  |  pts: " + ptCount, 24, height - 24);
}

// ── Controls ──────────────────────────────────────────────────────────────
void keyPressed() {
  if      (key == '1')          mode = 1;
  else if (key == '2')          mode = 2;
  else if (key == '3')          mode = 3;
  else if (key == ' ')          charIdx = (charIdx + 1) % chars.length;
  else if (key == 's' || key == 'S') savePNG = true;
  else if (keyCode == UP)   ptCount = min(ptCount + 100, 2000);
  else if (keyCode == DOWN) ptCount = max(ptCount - 100, 100);
}
