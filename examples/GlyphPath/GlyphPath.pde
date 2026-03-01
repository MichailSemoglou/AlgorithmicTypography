/*
 * GlyphPath
 *
 * Demonstrates the GlyphExtractor — our built-in
 * alternative to the Geomerative library.
 *
 * Extract glyph outlines as vertices, apply real-time wave
 * deformation to letterforms, and explore the designer-oriented
 * v0.2.2 API:
 *
 *   fillWithPoints()         — scattered points inside the letterform interior
 *   distributeAlongOutline() — evenly spaced points along the full perimeter
 *   getOuterContour()        — the outer boundary contour only
 *   getInnerContours()       — counter-forms only (holes in 'B', 'O', 'P', 'R')
 *
 * Controls:
 *   1-5    Switch display mode (outline/deformation modes)
 *   6-8    Switch display mode (v0.2.2 designer modes)
 *   UP     Increase deformation amplitude
 *   DOWN   Decrease deformation amplitude
 *   LEFT   Decrease point density (higher flatness)
 *   RIGHT  Increase point density (lower flatness)
 *   SPACE  Cycle through characters
 *   S      Save SVG
 */

import processing.svg.*;
import algorithmic.typography.*;
import algorithmic.typography.render.GlyphExtractor;
import java.util.List;

GlyphExtractor glyph;

int     modeIdx  = 0;
String[] modes   = {"Filled", "Lines", "Deformed", "Contours", "Grid 4x4",
                    "Fill Points", "Outer", "Outer+Inner"};
char[]  chars    = {'A', 'B', 'R', 'W', 'g', '&', '@', '%'};
int     charIdx  = 0;
float   amp      = 6.0f;     // deformation amplitude
boolean saveSVG  = false;

void setup() {
  size(1080, 1080);

  glyph = new GlyphExtractor(this, "Helvetica", 72);
  glyph.setFlatness(0.5f);

  println("GlyphPath — 1-8=mode  SPACE=char  UP/DOWN=amplitude");
}

void draw() {
  if (saveSVG) {
    beginRecord(SVG, "glyph-" + nf(frameCount, 6) + ".svg");
  }

  background(0);

  char ch = chars[charIdx];

  switch (modeIdx) {
    case 0: drawFilled(ch);      break;
    case 1: drawPoints(ch);      break;
    case 2: drawDeformed(ch);    break;
    case 3: drawContours(ch);    break;
    case 4: drawGrid(ch);        break;
    case 5: drawFillPoints(ch);  break;
    case 6: drawOuter(ch);       break;
    case 7: drawOuterInner(ch);  break;
  }

  if (saveSVG) {
    endRecord();
    println("Saved SVG");
    saveSVG = false;
  }

  drawHUD(ch);
}

// ── Mode 1: filled PShape ────────────────────────────────────
void drawFilled(char ch) {
  PShape s = glyph.extractChar(ch, 600);
  float[] b = glyph.getBounds(ch, 600);

  pushMatrix();
  translate(width / 2 - b[0] - b[2] / 2,
            height / 2 - b[1] - b[3] / 2);
  shape(s, 0, 0);
  popMatrix();
}

// ── Mode 2: perimeter diagonal lines (distributeAlongOutline) ─
// Uses arc-length resampling so every line is equally spaced
// around the full outline — unlike getContourPoints() whose
// vertex density varies with curve curvature.
void drawPoints(char ch) {
  PVector[] pts = glyph.distributeAlongOutline(ch, 600, 600);
  float[] b = glyph.getBounds(ch, 600);
  float ox = width / 2 - b[0] - b[2] / 2;
  float oy = height / 2 - b[1] - b[3] / 2;

  float t = frameCount * 0.04f;

  noFill();
  stroke(255);
  strokeWeight(1.2f);

  for (int i = 0; i < pts.length; i++) {
    float phase = (float) i / pts.length;
    float len   = 6 + sin(t * 2 + phase * TWO_PI * 4) * 3.0f;
    float cx = ox + pts[i].x;
    float cy = oy + pts[i].y;
    line(cx - len / 2, cy - len / 2, cx + len / 2, cy + len / 2);
  }

  noStroke();
}

// ── Mode 3: wave-deformed outline ────────────────────────────
void drawDeformed(char ch) {
  float t = frameCount * 0.05f;
  PShape s = glyph.extractDeformed(ch, 600, amp, 0.04f, t);
  float[] b = glyph.getBounds(ch, 600);

  // Apply colour to all children
  colorMode(HSB, 360, 255, 255);
  for (int i = 0; i < s.getChildCount(); i++) {
    PShape child = s.getChild(i);
    float hue = map(i, 0, max(1, s.getChildCount()), 180, 320);
    child.setFill(color(hue, 200, 255));
  }
  colorMode(RGB, 255);

  pushMatrix();
  translate(width / 2 - b[0] - b[2] / 2,
            height / 2 - b[1] - b[3] / 2);
  shape(s, 0, 0);
  popMatrix();
}

// ── Mode 4: separated contours ───────────────────────────────
void drawContours(char ch) {
  java.util.List<PVector[]> contours = glyph.getContours(ch, 600);
  float[] b = glyph.getBounds(ch, 600);
  float ox = width / 2 - b[0] - b[2] / 2;
  float oy = height / 2 - b[1] - b[3] / 2;

  float t = frameCount * 0.03f;

  colorMode(HSB, 360, 255, 255);
  noFill();
  strokeWeight(2);

  for (int c = 0; c < contours.size(); c++) {
    PVector[] pts = contours.get(c);
    float hue = map(c, 0, max(1, contours.size()), 0, 300);
    stroke(hue, 220, 255);

    beginShape();
    for (int i = 0; i < pts.length; i++) {
      float wave = sin(t + i * 0.08f) * amp * 0.5f;
      vertex(ox + pts[i].x + wave, oy + pts[i].y + wave * 0.5f);
    }
    endShape(CLOSE);
  }

  colorMode(RGB, 255);
  noStroke();
}

// ── Mode 5: 4×4 glyph grid with wave colour + deformation ───
int gridCols = 4;
int gridRows = 4;

void drawGrid(char ch) {
  float cellW = (float) width  / gridCols;
  float cellH = (float) height / gridRows;
  float fontSize = min(cellW, cellH) * 0.75f;
  float t = frameCount * 0.03f;

  float[] b = glyph.getBounds(ch, fontSize);

  colorMode(HSB, 360, 255, 255);

  for (int gx = 0; gx < gridCols; gx++) {
    for (int gy = 0; gy < gridRows; gy++) {
      float cx = gx * cellW + cellW / 2;
      float cy = gy * cellH + cellH / 2;

      // Wave-driven HSB colour per cell
      float hue = (sin(t + gx * 0.6f + gy * 0.5f) * 0.5f + 0.5f) * 360;
      float sat = 180 + sin(t * 0.7f + (gx + gy) * 0.4f) * 75;
      float bri = 180 + cos(t + gx * 0.3f + gy * 0.7f) * 75;

      // Per-cell deformation driven by position
      float cellAmp = amp * (0.5f + sin(t + gx * 1.2f + gy * 0.9f) * 0.5f);
      PShape s = glyph.extractDeformed(ch, fontSize, cellAmp, 0.04f, t + gx + gy);

      // Colour all contour children
      for (int i = 0; i < s.getChildCount(); i++) {
        s.getChild(i).setFill(color(hue, sat, bri));
      }

      pushMatrix();
      translate(cx - b[0] - b[2] / 2, cy - b[1] - b[3] / 2);
      shape(s, 0, 0);
      popMatrix();
    }
  }

  colorMode(RGB, 255);
}

// ── Mode 6: fillWithPoints ──────────────────────────────────
void drawFillPoints(char ch) {
  PVector[] pts = glyph.fillWithPoints(ch, 600, 800);
  float[] b = glyph.getBounds(ch, 600);
  float ox = width / 2 - b[0] - b[2] / 2;
  float oy = height / 2 - b[1] - b[3] / 2;

  float t = frameCount * 0.04f;

  colorMode(HSB, 360, 255, 255);
  noStroke();

  for (int i = 0; i < pts.length; i++) {
    float hue = (map(i, 0, pts.length, 180, 300) + t * 20) % 360;
    float sz  = 2.5f + sin(t + i * 0.07f) * 1.2f;
    fill(hue, 200, 255, 220);
    ellipse(ox + pts[i].x, oy + pts[i].y, sz, sz);
  }

  colorMode(RGB, 255);
}

// ── Mode 7: getOuterContour ─────────────────────────────────
void drawOuter(char ch) {
  PVector[] outer = glyph.getOuterContour(ch, 600);
  float[] b = glyph.getBounds(ch, 600);
  float ox = width / 2 - b[0] - b[2] / 2;
  float oy = height / 2 - b[1] - b[3] / 2;

  float t = frameCount * 0.03f;

  colorMode(HSB, 360, 255, 255);
  noFill();
  strokeWeight(2);
  stroke((200 + t * 30) % 360, 200, 255);

  beginShape();
  for (PVector p : outer) vertex(ox + p.x, oy + p.y);
  endShape(CLOSE);

  colorMode(RGB, 255);
  noStroke();
}

// ── Mode 8: getOuterContour + getInnerContours ──────────────
void drawOuterInner(char ch) {
  PVector[] outer       = glyph.getOuterContour(ch, 600);
  List<PVector[]> inner = glyph.getInnerContours(ch, 600);
  float[] b = glyph.getBounds(ch, 600);
  float ox = width / 2 - b[0] - b[2] / 2;
  float oy = height / 2 - b[1] - b[3] / 2;

  float t = frameCount * 0.03f;

  colorMode(HSB, 360, 255, 255);
  noFill();
  strokeWeight(2);

  // Outer boundary — cyan-blue
  stroke((200 + t * 30) % 360, 200, 255);
  beginShape();
  for (PVector p : outer) vertex(ox + p.x, oy + p.y);
  endShape(CLOSE);

  // Inner contours (counter-forms) — warm orange
  stroke((25 + t * 20) % 360, 220, 255);
  for (PVector[] contour : inner) {
    beginShape();
    for (PVector p : contour) vertex(ox + p.x, oy + p.y);
    endShape(CLOSE);
  }

  colorMode(RGB, 255);
  noStroke();
}

// ── HUD ──────────────────────────────────────────────────────
void drawHUD(char ch) {
  fill(255);
  textAlign(LEFT, TOP);
  textSize(14);
  int y = 20;
  text("Mode: " + modes[modeIdx], 20, y);           y += 20;
  text("Char: " + ch, 20, y);                       y += 20;
  text("Amplitude: " + nf(amp, 1, 1), 20, y);       y += 20;
  text("Flatness: " + nf(glyph.getFlatness(), 1, 2), 20, y);
}

// ── Input ────────────────────────────────────────────────────
void keyPressed() {
  if (key >= '1' && key <= '8') {
    modeIdx = key - '1';
  } else if (key == ' ') {
    charIdx = (charIdx + 1) % chars.length;
  } else if (key == 's' || key == 'S') {
    saveSVG = true;
  } else if (keyCode == UP) {
    amp = min(amp + 1.0f, 40);
  } else if (keyCode == DOWN) {
    amp = max(amp - 1.0f, 0);
  } else if (keyCode == RIGHT) {
    glyph.setFlatness(max(0.1f, glyph.getFlatness() - 0.1f));
    println("Flatness: " + nf(glyph.getFlatness(), 1, 2));
  } else if (keyCode == LEFT) {
    glyph.setFlatness(min(5.0f, glyph.getFlatness() + 0.1f));
    println("Flatness: " + nf(glyph.getFlatness(), 1, 2));
  }
}
