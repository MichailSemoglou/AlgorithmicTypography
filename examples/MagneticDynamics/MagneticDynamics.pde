/*
 * MagneticDynamics
 *
 * A dedicated showcase of MagneticMotion — glyphs are pushed away
 * from or pulled toward the mouse cursor in real time.
 *
 * Two side-by-side control panels:
 *   LEFT  — library ControlPanel (wave speed, colour, text scale)
 *   RIGHT — Magnetic Panel: all MagneticMotion parameters as live
 *           Slider controls from the library's UI layer
 *
 * Presets (keys 1–3):
 *   1  Repel        — cursor pushes glyphs away (default)
 *   2  Attract      — cursor draws glyphs toward it
 *   3  Rubber Band  — gentle attract with slow, springy response
 *
 * Other controls:
 *   SPACE  Toggle attract / repel polarity
 *   H      Toggle both panels
 *   R      Restart animation
 *   S      Save frames toggle
 */

import algorithmic.typography.*;
import algorithmic.typography.core.MagneticMotion;
import algorithmic.typography.ui.*;

// ── Core objects ──────────────────────────────────────────────────
AlgorithmicTypography at;
ObservableConfiguration config;
ControlPanel wavePanel;  // standard library panel (left)
MagneticMotion magnetic;

// ── Magnetic panel sliders ────────────────────────────────────────
Slider sldStrength;
Slider sldFalloff;
Slider sldSmoothing;
Slider sldRadius;

Slider[] magSliders;

// ── Magnetic panel layout ─────────────────────────────────────────
final float MP_X = 20;  // x offset from RIGHT edge
final float MP_Y = 10;
final float MP_W = 310;
final float MP_PAD = 10;
final float MP_HDR = 24;
final float MP_ROW = 24;

// ── State ─────────────────────────────────────────────────────────
boolean panelsVisible = true;
String presetName = "Repel";

// ── Presets ───────────────────────────────────────────────────────
// Each preset: { strength, falloff, smoothing, radius, attract (0=repel 1=attract) }
float[][] PRESETS = {
  { 1800, 80,  0.12f, 20, 0 },   // 1 Repel
  { 2200, 60,  0.14f, 22, 1 },   // 2 Attract
  {  900, 130, 0.05f, 18, 1 },   // 3 Rubber Band
};
String[] PRESET_NAMES = { "Repel", "Attract", "Rubber Band" };

// ─────────────────────────────────────────────────────────────────

void setup() {
  size(1080, 1080);
  colorMode(RGB, 255);

  // ── Configuration ─────────────────────────────────────────────
  config = new ObservableConfiguration();
  config.setCanvasSize(width, height);
  config.setGridSize(22, 22, 14, 14, 6, 6);
  config.setCharacter("A");
  config.setTextScale(0.62f);
  config.setWaveSpeed(1.2f);
  config.setWaveAngle(45);
  config.setHueRange(160, 300);
  config.setSaturationMin(160);
  config.setSaturationMax(255);
  config.setBrightnessRange(140, 255);
  config.setAnimationDuration(3600);
  config.setSaveFrames(false);

  // ── MagneticMotion ────────────────────────────────────────────
  float[] p = PRESETS[0];
  magnetic = new MagneticMotion(this, p[0], p[1], p[4] == 1);
  magnetic.setSmoothing(p[2]);
  magnetic.setRadius(p[3]);
  magnetic.setTileGrid(width, height, 22, 22);
  config.setCellMotion(magnetic);

  // ── Library renderer ──────────────────────────────────────────
  at = new AlgorithmicTypography(this);
  at.setConfiguration(config);
  at.setAutoRender(false);
  at.initialize();

  // ── Wave panel (left) ─────────────────────────────────────────
  wavePanel = new ControlPanel(this, config);
  wavePanel.createControls();

  // ── Magnetic panel (right) ────────────────────────────────────
  float tw = MP_W - MP_PAD * 2 - 140;

  sldStrength = new Slider("Strength", 100, 4000, p[0], tw).setDecimals(0);
  sldFalloff = new Slider("Falloff", 10, 300, p[1], tw).setDecimals(0).setSuffix(" px");
  sldSmoothing = new Slider("Smoothing", 0.01f, 0.5f, p[2], tw).setDecimals(3);
  sldRadius = new Slider("Radius", 4, 40, p[3], tw).setDecimals(1).setSuffix(" px");

  // Live callbacks — each updates magnetic without resetting cell positions
  sldStrength.onChange(v -> magnetic.setStrength(v));
  sldFalloff.onChange(v -> magnetic.setFalloff(v));
  sldSmoothing.onChange(v -> magnetic.setSmoothing(v));
  sldRadius.onChange(v -> magnetic.setRadius(v));

  magSliders = new Slider[] { sldStrength, sldFalloff, sldSmoothing, sldRadius };

  layoutMagSliders();

  println("MagneticDynamics — 1-3 presets  SPACE toggle polarity  H panels  R restart  S save");
}

// ─────────────────────────────────────────────────────────────────

void draw() {
  at.render();

  if (panelsVisible) {
    wavePanel.draw();
    drawMagneticPanel();
    drawHUD();
  }
}

// ── Magnetic panel rendering ──────────────────────────────────────

void drawMagneticPanel() {
  float px = width - MP_X - MP_W;
  float panelH = MP_HDR + MP_PAD + magSliders.length * (MP_ROW + 6) + 36;

  // Background
  fill(0, 180);
  noStroke();
  rect(px, MP_Y, MP_W, panelH, 6);

  // Title
  fill(255);
  textAlign(LEFT, TOP);
  textSize(13);
  String mode = magnetic.isAttract() ? "ATTRACT" : "REPEL";
  text("MAGNETIC CONTROLS  —  " + presetName + "  [" + mode + "]",
       px + MP_PAD, MP_Y + 6);

  // Sliders
  for (Slider s : magSliders) {
    s.display(g);
  }

  // Footer
  float footerY = MP_Y + panelH - 20;
  fill(150);
  textSize(10);
  textAlign(LEFT, TOP);
  text("1=Repel  2=Attract  3=Rubber Band   SPACE=toggle polarity", px + MP_PAD, footerY);
}

void drawHUD() {
  String polarity = magnetic.isAttract() ? "ATTRACT" : "REPEL";
  fill(255, 180);
  textSize(13);
  textAlign(LEFT, BOTTOM);
  noStroke();
  text("Preset: " + presetName
     + "   Mode: "      + polarity
     + "   Strength: "  + (int)magnetic.getStrength()
     + "   Falloff: "   + (int)magnetic.getFalloff() + " px"
     + "   FPS: "       + (int)frameRate,
     16, height - 14);
}

// ── Panel layout ──────────────────────────────────────────────────

void layoutMagSliders() {
  float px = width - MP_X - MP_W;
  float y = MP_Y + MP_HDR + MP_PAD;
  for (Slider s : magSliders) {
    s.setPosition(px + MP_PAD, y);
    y += MP_ROW + 6;
  }
}

// ── Preset application ────────────────────────────────────────────

void applyPreset(int idx) {
  float[] p = PRESETS[idx];
  presetName = PRESET_NAMES[idx];

  magnetic.setStrength(p[0]);
  magnetic.setFalloff(p[1]);
  magnetic.setSmoothing(p[2]);
  magnetic.setRadius(p[3]);
  magnetic.setAttract(p[4] == 1);

  // Sync sliders to preset values
  sldStrength .setValue(p[0]);
  sldFalloff  .setValue(p[1]);
  sldSmoothing.setValue(p[2]);
  sldRadius   .setValue(p[3]);

  println("Preset: " + presetName + "  [" + (magnetic.isAttract() ? "ATTRACT" : "REPEL") + "]");
}

// ── Input ─────────────────────────────────────────────────────────

void keyPressed() {
  // Presets
  if (key == '1') { applyPreset(0); return; }
  if (key == '2') { applyPreset(1); return; }
  if (key == '3') { applyPreset(2); return; }

  // Toggle polarity
  if (key == ' ') {
    magnetic.togglePolarity();
    println("Polarity: " + (magnetic.isAttract() ? "ATTRACT" : "REPEL"));
    return;
  }

  // Toggle panels
  if (key == 'h' || key == 'H') {
    panelsVisible = !panelsVisible;
    return;
  }

  // Restart animation
  if (key == 'r' || key == 'R') {
    at.restart();
    return;
  }

  // Frame saving
  if (key == 's' || key == 'S') {
    at.toggleFrameSaving();
    return;
  }

  // Delegate remaining keys to wave panel shortcuts
  wavePanel.handleKeyPress(key);
}

void mousePressed() {
  // Check magnetic panel sliders first (right side)
  boolean handled = false;
  for (Slider s : magSliders) {
    if (s.mousePressed(mouseX, mouseY)) { handled = true; break; }
  }
  // Then wave panel (left side)
  if (!handled) wavePanel.mousePressed(mouseX, mouseY);
}

void mouseDragged() {
  for (Slider s : magSliders) s.mouseDragged(mouseX, mouseY);
  wavePanel.mouseDragged(mouseX, mouseY);
}

void mouseReleased() {
  for (Slider s : magSliders) s.mouseReleased();
  wavePanel.mouseReleased();
}

void dispose() {
  if (wavePanel != null) wavePanel.dispose();
}
