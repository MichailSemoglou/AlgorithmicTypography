/*
 * GlyphMorph
 *
 * A designer-focused showcase of GlyphExtractor.morphShape(),
 * which returns a ready-to-draw PShape morphing one letterform
 * into another at t ∈ [0, 1]. Counter-forms, contour counts and
 * resampling are all handled inside the library — the sketch
 * just calls glyph.morphShape() and shape().
 *
 * Layout:
 *   Upper area  —  main morph glyph, auto-animated or mouse-scrubbed
 *   Bottom strip  —  five thumbnail snapshots at t = 0 / 0.25 / 0.5 / 0.75 / 1
 *   Progress bar  —  shows current t value above the strip
 *
 * Controls:
 *   SPACE        Cycle to the next character pair
 *   LEFT / RIGHT Decrease / increase morph speed
 *   M            Toggle auto-animate ↔ mouse-X scrub
 *   R            Reverse auto direction
 *   1 – 3        Switch display style  (Outline / Filled / Dot Cloud)
 *   S            Save current frame as SVG
 */

import processing.svg.*;
import algorithmic.typography.render.GlyphExtractor;
import algorithmic.typography.ui.ProgressBar;

GlyphExtractor glyph;
ProgressBar progressBar;

// ── Curated character pairs ───────────────────────────────────
// Any pair works — mismatched holes gracefully collapse to their
// own centroid so both endpoints are always complete characters.
char[][] pairs = {
  { 'A', 'R' },  // 2 contours → 2  (counter shifts from triangle to eye)
  { 'B', '8' },  // 3 contours → 3  (two holes swap geometry)
  { 'O', 'Q' },  // 2 contours → 2  (round hole gains tail)
  { 'C', 'G' },  // 1 contour  → 1
  { 'P', 'D' },  // 2 contours → 2
  { 'S', '5' }   // 1 contour  → 1
};
int pairIdx = 0;

// ── Morph state ───────────────────────────────────────────────
float   t         = 0;       // current interpolation parameter [0..1]
float   speed     = 0.007f;  // auto-advance step per frame
int     dir       = 1;       // +1 forward, -1 reverse
boolean autoMode  = true;    // true = auto-animate, false = mouse-X scrub
int     styleIdx  = 0;       // active display style
boolean saveSVG   = false;

String[] styleNames = { "Outline", "Filled", "Dot Cloud" };

// ── Layout constants ─────────────────────────────────────────
static final float FONT_MAIN  = 620;   // main glyph font size
static final float FONT_THUMB = 110;   // thumbnail glyph font size
static final float STRIP_H    = 175;   // height of thumbnail strip
static final int   THUMBS     = 5;     // number of thumbnails

float thumbW;  // computed in setup()
float stripY;  // computed in setup()

// ─────────────────────────────────────────────────────────────

void setup() {
  size(1080, 1080);
  colorMode(RGB, 255);
  textFont(createFont("Helvetica", 14));

  glyph = new GlyphExtractor(this, "Helvetica", 72);
  glyph.setFlatness(0.35f);

  thumbW = (float) width / THUMBS;
  stripY = height - STRIP_H;

  float barW = width * 0.45f;
  progressBar = new ProgressBar(barW)
                    .setPosition(width / 2 - barW / 2, stripY - 18);

  println("GlyphMorph — SPACE pair | 1-3 style | M mouse/auto | R reverse | LEFT/RIGHT speed | S svg");
}

void draw() {
  if (saveSVG) beginRecord(SVG, "glyphmorph-" + nf(frameCount, 5) + ".svg");

  background(11, 11, 15);

  // ── Advance t ─────────────────────────────────────────────
  if (autoMode) {
    t += speed * dir;
    if (t >= 1.0f) { t = 1.0f; dir = -1; }
    if (t <= 0.0f) { t = 0.0f; dir =  1; }
  } else {
    t = constrain(map(mouseX, 0, width, 0, 1), 0, 1);
  }

  char charA = pairs[pairIdx][0];
  char charB = pairs[pairIdx][1];

  // ── Draw layers ───────────────────────────────────────────
  drawThumbnailStrip(charA, charB);

  colorMode(HSB, 360, 255, 255);
  int pbColor = color((t * 210 + frameCount * 0.35f) % 360, 210, 255);
  colorMode(RGB, 255);
  progressBar.setFillColor(pbColor);
  progressBar.setValue(t);
  progressBar.display(g);

  drawMainMorph(charA, charB, t);
  drawHUD(charA, charB);

  if (saveSVG) { endRecord(); saveSVG = false; println("SVG saved."); }
}

// ══════════════════════════════════════════════════════════════
//  MAIN MORPH
// ══════════════════════════════════════════════════════════════

void drawMainMorph(char a, char b, float mt) {
  float mainH = stripY;
  PVector o   = glyph.morphCenterOf(a, b, FONT_MAIN, mt, width / 2, mainH / 2);
  float ox    = o.x;
  float oy    = o.y;
  float hue   = (mt * 210 + frameCount * 0.35f) % 360;

  colorMode(HSB, 360, 255, 255);
  switch (styleIdx) {
    case 0: styleOutline(a, b, mt, ox, oy, hue); break;
    case 1: styleFilled (a, b, mt, ox, oy, hue); break;
    case 2: styleDots   (a, b, mt, ox, oy, hue); break;
  }
  colorMode(RGB, 255);
}

// ── Style 0 — clean stroke outline ──
void styleOutline(char a, char b, float mt, float ox, float oy, float hue) {
  PShape s = glyph.morphShape(a, b, FONT_MAIN, mt);
  s.disableStyle();
  noFill();
  strokeWeight(2.5f);
  stroke(hue, 200, 255);
  shape(s, ox, oy);
  noStroke();
}

// ── Style 1 — solid fill; holes handled inside the library ──
void styleFilled(char a, char b, float mt, float ox, float oy, float hue) {
  PShape s = glyph.morphShape(a, b, FONT_MAIN, mt);
  s.disableStyle();
  fill(hue, 185, 235);
  noStroke();
  shape(s, ox, oy);
}

// ── Style 2 — dots along every morphed contour ──
// Mirrors GlyphDesign's distributeAlongOutline approach: a fixed total dot
// count (TOTAL_DOTS) is distributed across all contours proportionally by
// arc length, so the outer boundary gets most dots and each inner counter
// gets its fair share — no flat per-contour cap, no gaps on straight stems.
static final int TOTAL_DOTS = 400;

void styleDots(char a, char b, float mt, float ox, float oy, float hue) {
  java.util.List<PVector[]> contours = glyph.interpolateContours(a, b, FONT_MAIN, mt);
  if (contours.isEmpty()) return;

  // ── Compute arc length of each contour (closed) ──
  float[] lengths = new float[contours.size()];
  float totalLen  = 0;
  for (int i = 0; i < contours.size(); i++) {
    PVector[] c = contours.get(i);
    float len = 0;
    for (int j = 1; j < c.length; j++) len += PVector.dist(c[j-1], c[j]);
    if (c.length > 1) len += PVector.dist(c[c.length-1], c[0]); // close
    lengths[i] = len;
    totalLen += len;
  }

  // ── Allocate dots proportionally ──
  int[] counts  = new int[contours.size()];
  int   totalPts = 0;
  for (int i = 0; i < contours.size(); i++) {
    counts[i] = max(3, round(TOTAL_DOTS * lengths[i] / max(totalLen, 1)));
    totalPts += counts[i];
  }

  float tf  = frameCount * 0.045f;
  noStroke();
  int idx = 0;
  for (int ci = 0; ci < contours.size(); ci++) {
    // Close the contour by appending the first point so the closing
    // segment (last → first) is always covered by resampled dots.
    PVector[] raw    = contours.get(ci);
    PVector[] closed = java.util.Arrays.copyOf(raw, raw.length + 1);
    closed[raw.length] = raw[0].copy();
    PVector[] dense = glyph.resample(closed, counts[ci]);
    for (int i = 0; i < dense.length; i++) {
      float phase = (float)(idx++) / max(totalPts, 1);
      float ph    = (hue + phase * 70) % 360;
      float sz    = 3.8f + sin(tf + phase * TWO_PI * 3) * 2.2f;
      fill(ph, 230, 255, 210);
      ellipse(ox + dense[i].x, oy + dense[i].y, sz, sz);
    }
  }
}

// ══════════════════════════════════════════════════════════════
//  THUMBNAIL STRIP
// ══════════════════════════════════════════════════════════════

void drawThumbnailStrip(char a, char b) {
  // Separator
  stroke(255, 255, 255, 28);
  strokeWeight(1);
  line(0, stripY, width, stripY);
  noStroke();

  colorMode(HSB, 360, 255, 255);
  for (int i = 0; i < THUMBS; i++) {
    float tval  = map(i, 0, THUMBS - 1, 0, 1);
    float cx    = i * thumbW + thumbW / 2;
    float cy    = stripY + STRIP_H / 2 - 14;   // leave room for label
    float hue   = (tval * 210 + frameCount * 0.35f) % 360;
    boolean hot = abs(tval - t) < (1.0f / (THUMBS - 1) * 0.52f);

    PVector o = glyph.morphCenterOf(a, b, FONT_THUMB, tval, cx, cy);
    float ox  = o.x;
    float oy  = o.y;

    PShape s = glyph.morphShape(a, b, FONT_THUMB, tval);
    s.disableStyle();
    noFill();
    strokeWeight(hot ? 2f : 1f);
    stroke(hue, hot ? 230 : 120, hot ? 255 : 150, hot ? 255 : 180);
    shape(s, ox, oy);
    noStroke();

    // label
    fill(255, 0, hot ? 220 : 110);
    noStroke();
    textAlign(CENTER, BOTTOM);
    textSize(11);
    text("t=" + nf(tval, 1, 2), cx, stripY + STRIP_H - 8);

    // divider between cells
    if (i < THUMBS - 1) {
      stroke(255, 0, 40, 80);
      strokeWeight(1);
      line((i + 1) * thumbW, stripY + 10, (i + 1) * thumbW, stripY + STRIP_H - 24);
      noStroke();
    }
  }
  colorMode(RGB, 255);
}

// ══════════════════════════════════════════════════════════════
//  HUD
// ══════════════════════════════════════════════════════════════

void drawHUD(char a, char b) {
  // Pair label  (top-left)
  fill(255);
  textAlign(LEFT, TOP);
  textSize(15);
  text("'" + a + "'  →  '" + b + "'", 24, 24);

  fill(255, 150);
  textSize(12);
  text("Style: " + styleNames[styleIdx], 24, 46);
  text((autoMode ? "Auto" : "Mouse") + "   t = " + nf(t, 1, 3), 24, 65);
  text("Speed: " + nf(speed * 1000, 0, 1) + "  ‹›", 24, 84);

  // Controls hint  (top-right)
  fill(255, 75);
  textAlign(RIGHT, TOP);
  textSize(11);
  text("SPACE pair  |  1–3 style  |  M mouse/auto  |  R reverse  |  ←→ speed  |  S svg",
       width - 24, 24);

  // Pair counter  (top-centre)
  fill(255, 100);
  textAlign(CENTER, TOP);
  textSize(11);
  text((pairIdx + 1) + " / " + pairs.length, width / 2, 24);
}

// ══════════════════════════════════════════════════════════════
//  INPUT
// ══════════════════════════════════════════════════════════════

void keyPressed() {
  if (key == ' ') {
    pairIdx = (pairIdx + 1) % pairs.length;
    t = 0;  dir = 1;
  } else if (key >= '1' && key <= '3') {
    styleIdx = key - '1';
  } else if (key == 'm' || key == 'M') {
    autoMode = !autoMode;
  } else if (key == 'r' || key == 'R') {
    dir *= -1;
  } else if (key == 's' || key == 'S') {
    saveSVG = true;
  } else if (keyCode == RIGHT) {
    speed = min(speed + 0.002f, 0.04f);
  } else if (keyCode == LEFT) {
    speed = max(speed - 0.002f, 0.001f);
  }
}
