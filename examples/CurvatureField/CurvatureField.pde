/**
 * CurvatureField — v0.3.0 Example
 *
 * Four letterforms in a 2×2 grid, each driven by its own GlyphCurvatureField.
 *
 * What to look for:
 *   HIGH-CURVATURE zones (bowls, junctions, sharp corners) spread the wave's spatial
 *   phase across neighbouring cells — each cell sits at a different point in the wave
 *   cycle, so they ripple and flutter independently, tracing the letterform's geometry.
 *
 *   Press F to toggle ALL four fields OFF at once: every grid cell falls back to
 *   the same default amplitude and the wave becomes uniform across the canvas.
 *   Press F again to restore — the letterform geometry snaps back immediately.
 *   The contrast makes it clear exactly what the curvature field is doing.
 *
 * Controls
 *   F / f  : toggle curvature field ON / OFF (all four panels)
 *   + / -  : increase / decrease field intensity (0.0 – 1.0)
 *   [ / ]  : decrease / increase Gaussian falloff sigma
 *   S / s  : save current frame as PNG
 */

import algorithmic.typography.*;
import algorithmic.typography.core.*;
import algorithmic.typography.render.*;

// Characters chosen for strong curvature contrast (bowls + straight stems)
final char[] CHARS = { 'A', 'B', 'R', 'S' };

AlgorithmicTypography[] panels = new AlgorithmicTypography[4];
GlyphExtractor ge;
GlyphCurvatureField[] fields = new GlyphCurvatureField[4];

boolean fieldOn = true;
float intensity = 1.0f;
float falloff = 0.10f;

int HW, HH;  // half-canvas dimensions for each quadrant

void setup() {
  size(1080, 1080, P2D);
  colorMode(RGB, 255);

  HW = width / 2;  // 540
  HH = height / 2;  // 540

  ge = new GlyphExtractor(this, "Helvetica", 72);
  ge.setFlatness(0.3f);

  for (int i = 0; i < 4; i++) {
    panels[i] = new AlgorithmicTypography(this);
    panels[i].setAutoRender(false);
    panels[i].loadConfiguration("data/config.json");
    panels[i].getConfiguration().setCharacter(String.valueOf(CHARS[i]));
    panels[i].initialize();

    fields[i] = GlyphCurvatureField.from(ge, CHARS[i], 800f);
    fields[i].setIntensity(intensity).setFalloff(falloff);
    panels[i].setCurvatureField(fields[i]);
  }

  println("CurvatureField 2x2 example loaded.");
  println("  F     : toggle fields ON/OFF");
  println("  +/-   : adjust intensity");
  println("  [/]   : adjust falloff");
  println("  S     : save frame");
}

void draw() {
  background(10, 10, 20);

  // --- Four quadrants ---
  panels[0].renderAt(0, 0, HW, HH);   // A — top-left
  panels[1].renderAt(HW, 0, HW, HH);   // B — top-right
  panels[2].renderAt(0, HH, HW, HH);   // R — bottom-left
  panels[3].renderAt(HW, HH, HW, HH);   // S — bottom-right

  // --- Dividers ---
  stroke(40);
  strokeWeight(1);
  line(HW, 0, HW, height);
  line(0, HH, width, HH);

  // --- Per-quadrant labels ---
  for (int i = 0; i < 4; i++) {
    int qx = (i % 2) * HW;
    int qy = (i / 2) * HH;
    noStroke();
    fill(0, 150);
    rect(qx + 6, qy + 6, 36, 24);
    fill(255, 210);
    textSize(14);
    textAlign(LEFT, TOP);
    text(str(CHARS[i]), qx + 10, qy + 9);
  }

  // --- Status bar ---
  noStroke();
  fill(0, 170);
  rect(0, height - 28, width, 28);
  fill(fieldOn ? color(120, 220, 120) : color(200, 80, 80));
  textSize(12);
  textAlign(CENTER, CENTER);
  text("Field: " + (fieldOn ? "ON" : "OFF")
       + "   |   Intensity: " + nf(intensity, 1, 2)
       + "   |   Falloff: "   + nf(falloff, 1, 3)
       + "   |   F toggle · +/- intensity · [/] falloff · S save",
       width / 2, height - 14);
}

void keyPressed() {
  if (key == 'f' || key == 'F') {
    fieldOn = !fieldOn;
    for (int i = 0; i < 4; i++) {
      panels[i].setCurvatureField(fieldOn ? fields[i] : null);
    }
    println("Fields: " + (fieldOn ? "ON" : "OFF"));

  } else if (key == '+' || key == '=') {
    intensity = min(1.0f, intensity + 0.05f);
    for (int i = 0; i < 4; i++) fields[i].setIntensity(intensity);
    println("Intensity -> " + nf(intensity, 1, 2));

  } else if (key == '-' || key == '_') {
    intensity = max(0.0f, intensity - 0.05f);
    for (int i = 0; i < 4; i++) fields[i].setIntensity(intensity);
    println("Intensity -> " + nf(intensity, 1, 2));

  } else if (key == ']') {
    falloff = min(0.5f, falloff + 0.02f);
    for (int i = 0; i < 4; i++) fields[i].setFalloff(falloff);
    println("Falloff -> " + nf(falloff, 1, 3));

  } else if (key == '[') {
    falloff = max(0.02f, falloff - 0.02f);
    for (int i = 0; i < 4; i++) fields[i].setFalloff(falloff);
    println("Falloff -> " + nf(falloff, 1, 3));

  } else if (key == 's' || key == 'S') {
    saveFrame("curvature_###.png");
  }
}
