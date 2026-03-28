/*
 * GravityDynamics
 *
 * A dedicated showcase of GravityMotion — glyphs fall under gravity
 * and bounce elastically inside their grid cells.
 *
 * Two side-by-side control panels:
 *   LEFT  — library ControlPanel (wave speed, colour, text scale)
 *   RIGHT — Gravity Panel: all six GravityMotion parameters as live
 *           Slider controls from the library's UI layer
 *
 * Presets (keys 1–4):
 *   1  Default   — balanced gravity, satisfying bounce
 *   2  Heavy     — fast fall, low restitution, near-dead bounce
 *   3  Floaty    — slow fall, highly elastic, wide lateral wander
 *   4  Pinball   — stiff high-speed chaos
 *
 * Other controls:
 *   SPACE  Jump — kick all glyphs upward from where they are
 *   H      Toggle both panels
 *   G      Reset physics (re-stagger all glyphs from top)
 *   R      Restart animation
 *   S      Save frames toggle
 */

import algorithmic.typography.*;
import algorithmic.typography.core.GravityMotion;
import algorithmic.typography.ui.*;

// ── Core objects ──────────────────────────────────────────────────
AlgorithmicTypography   at;
ObservableConfiguration config;
ControlPanel            wavePanel;  // standard library panel (left)
GravityMotion           gravity;

// ── Gravity panel sliders ─────────────────────────────────────────
Slider sldGravity;
Slider sldRestitution;
Slider sldLateral;
Slider sldAirDrag;
Slider sldPhaseSpread;
Slider sldRadius;
Slider sldJumpStrength;

Slider[] gravSliders;

// ── Gravity panel layout ──────────────────────────────────────────
final float GP_X   = 20;  // x offset from RIGHT edge
final float GP_Y   = 10;
final float GP_W   = 310;
final float GP_PAD = 10;
final float GP_HDR = 24;
final float GP_ROW = 24;

// ── State ─────────────────────────────────────────────────────────
boolean panelsVisible = true;
String presetName = "Default";

// ── Presets ──────────────────────────────────────────────────────
// Each preset: { gravity, restitution, lateralStrength, airDrag, phaseSpread, radius }
float[][] PRESETS = {
  { 0.18f, 0.72f, 0.06f, 0.97f, 1.0f,  12 },  // 1 Default
  { 0.55f, 0.30f, 0.03f, 0.96f, 0.8f,  12 },  // 2 Heavy
  { 0.08f, 0.92f, 0.15f, 0.99f, 1.4f,  10 },  // 3 Floaty
  { 0.40f, 0.88f, 0.22f, 0.94f, 1.8f,  14 },  // 4 Pinball
};
String[] PRESET_NAMES = { "Default", "Heavy", "Floaty", "Pinball" };

// ─────────────────────────────────────────────────────────────────

void setup() {
  size(1080, 1080);
  colorMode(RGB, 255);

  // ── Configuration ─────────────────────────────────────────────
  config = new ObservableConfiguration();
  config.setCanvasSize(width, height);
  config.setGridSize(22, 22, 14, 14, 6, 6);
  config.setCharacter("A");
  config.setTextScale(0.60f);
  config.setWaveSpeed(1.4f);
  config.setWaveAngle(0);
  config.setHueRange(200, 320);
  config.setSaturationMin(160);
  config.setSaturationMax(255);
  config.setBrightnessRange(140, 255);
  config.setAnimationDuration(3600);  // run for one hour (effectively indefinitely)
  config.setSaveFrames(false);

  // ── GravityMotion ─────────────────────────────────────────────
  float[] p = PRESETS[0];
  gravity = new GravityMotion(p[5], p[0], p[1], p[2]);
  gravity.setAirDrag(p[3]);
  gravity.setPhaseSpread(p[4]);
  config.setCellMotion(gravity);

  // ── Library renderer ──────────────────────────────────────────
  at = new AlgorithmicTypography(this);
  at.setConfiguration(config);
  at.setAutoRender(false);
  at.initialize();

  // ── Wave panel (left) ─────────────────────────────────────────
  wavePanel = new ControlPanel(this, config);
  wavePanel.createControls();

  // ── Gravity panel (right) ─────────────────────────────────────
  float tw = GP_W - GP_PAD * 2 - 140;

  sldGravity     = new Slider("Gravity",        0.02f, 0.80f, p[0], tw).setDecimals(3);
  sldRestitution = new Slider("Restitution",    0.00f, 1.00f, p[1], tw).setDecimals(2);
  sldLateral     = new Slider("Lateral",        0.00f, 0.30f, p[2], tw).setDecimals(3);
  sldAirDrag     = new Slider("Air Drag",       0.80f, 1.00f, p[3], tw).setDecimals(3);
  sldPhaseSpread = new Slider("Phase Spread",   0.20f, 2.50f, p[4], tw).setDecimals(2);
  sldRadius      = new Slider("Radius",         4,     40,    p[5], tw).setDecimals(1).setSuffix(" px");
  sldJumpStrength= new Slider("Jump Strength",  1.0f,  30.0f, 8.0f, tw).setDecimals(1).setSuffix(" px/f");

  // Live callbacks — each updates gravity and resets physics for instant feel
  sldGravity    .onChange(v -> { gravity.setGravity(v);         gravity.reset(); });
  sldRestitution.onChange(v -> { gravity.setRestitution(v);     gravity.reset(); });
  sldLateral    .onChange(v -> { gravity.setLateralStrength(v); gravity.reset(); });
  sldAirDrag    .onChange(v -> { gravity.setAirDrag(v);         gravity.reset(); });
  sldPhaseSpread.onChange(v -> { gravity.setPhaseSpread(v);     gravity.reset(); });
  sldRadius     .onChange(v -> { gravity.setRadius(v);          gravity.reset(); });
  // sldJumpStrength has no onChange — it's read on demand when SPACE is pressed

  gravSliders = new Slider[] {
    sldGravity, sldRestitution, sldLateral,
    sldAirDrag, sldPhaseSpread, sldRadius, sldJumpStrength
  };

  layoutGravSliders();

  println("GravityDynamics — 1-4 presets  SPACE jump  H panels  G reset  R restart  S save");
}

// ─────────────────────────────────────────────────────────────────

void draw() {
  at.render();

  if (panelsVisible) {
    wavePanel.draw();
    drawGravityPanel();
    drawHUD();
  }
}

// ── Gravity panel rendering ───────────────────────────────────────

void drawGravityPanel() {
  float px = width - GP_X - GP_W;
  float panelH = GP_HDR + GP_PAD + gravSliders.length * (GP_ROW + 6) + 36;

  // Background
  fill(0, 180);
  noStroke();
  rect(px, GP_Y, GP_W, panelH, 6);

  // Title
  fill(255);
  textAlign(LEFT, TOP);
  textSize(13);
  text("GRAVITY CONTROLS  —  " + presetName, px + GP_PAD, GP_Y + 6);

  // Sliders
  for (Slider s : gravSliders) {
    s.display(g);
  }

  // Footer
  float footerY = GP_Y + panelH - 20;
  fill(150);
  textSize(10);
  textAlign(LEFT, TOP);
  text("1=Default  2=Heavy  3=Floaty  4=Pinball   SPACE=jump  G=reset", px + GP_PAD, footerY);
}

void drawHUD() {
  fill(255, 180);
  textSize(13);
  textAlign(LEFT, BOTTOM);
  noStroke();
  text("Preset: " + presetName
     + "   Gravity: "     + nf(gravity.getGravity(), 1, 3)
     + "   Restitution: " + nf(gravity.getRestitution(), 1, 2)
     + "   Jump: " + nf(sldJumpStrength.getValue(), 1, 1) + " px/f"
     + "   FPS: "         + (int)frameRate,
     16, height - 14);
}

// ── Panel layout ─────────────────────────────────────────────────

void layoutGravSliders() {
  float px = width - GP_X - GP_W;
  float y = GP_Y + GP_HDR + GP_PAD;
  for (Slider s : gravSliders) {
    s.setPosition(px + GP_PAD, y);
    y += GP_ROW + 6;
  }
}

// ── Preset application ────────────────────────────────────────────

void applyPreset(int idx) {
  float[] p = PRESETS[idx];
  presetName = PRESET_NAMES[idx];

  gravity.setGravity(p[0]);
  gravity.setRestitution(p[1]);
  gravity.setLateralStrength(p[2]);
  gravity.setAirDrag(p[3]);
  gravity.setPhaseSpread(p[4]);
  gravity.setRadius(p[5]);
  gravity.reset();

  // Sync sliders to preset values
  sldGravity.setValue(p[0]);
  sldRestitution.setValue(p[1]);
  sldLateral.setValue(p[2]);
  sldAirDrag.setValue(p[3]);
  sldPhaseSpread.setValue(p[4]);
  sldRadius.setValue(p[5]);

  println("Preset: " + presetName);
}

// ── Input ─────────────────────────────────────────────────────────

void keyPressed() {
  // Presets
  if (key == '1') { applyPreset(0); return; }
  if (key == '2') { applyPreset(1); return; }
  if (key == '3') { applyPreset(2); return; }
  if (key == '4') { applyPreset(3); return; }

  // Jump
  if (key == ' ') {
    gravity.kick(sldJumpStrength.getValue());
    return;
  }

  // Toggle panels
  if (key == 'h' || key == 'H') {
    panelsVisible = !panelsVisible;
    return;
  }

  // Reset physics
  if (key == 'g' || key == 'G') {
    gravity.reset();
    println("Physics reset — glyphs re-staggered from top");
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

  // Delegate remaining keys to wave panel shortcuts (1-0, H handled above)
  wavePanel.handleKeyPress(key);
}

void mousePressed() {
  // Check gravity panel sliders first (right side)
  boolean handled = false;
  for (Slider s : gravSliders) {
    if (s.mousePressed(mouseX, mouseY)) { handled = true; break; }
  }
  // Then wave panel (left side)
  if (!handled) wavePanel.mousePressed(mouseX, mouseY);
}

void mouseDragged() {
  for (Slider s : gravSliders) s.mouseDragged(mouseX, mouseY);
  wavePanel.mouseDragged(mouseX, mouseY);
}

void mouseReleased() {
  for (Slider s : gravSliders) s.mouseReleased();
  wavePanel.mouseReleased();
}

void dispose() {
  if (wavePanel != null) wavePanel.dispose();
}
