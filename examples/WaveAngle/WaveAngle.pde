/*
 * WaveAngle
 *
 * Demonstrates how to control the wave propagation angle
 * using the library's built-in UI sliders and keyboard presets.
 *
 * The waveAngle parameter (0–360°) sets the direction in which
 * the colour wave travels across the grid:
 *   0°   — horizontal (left → right)
 *   45°  — diagonal   (top-left → bottom-right, default)
 *   90°  — vertical   (top → bottom)
 *   135° — anti-diagonal
 *   180° — horizontal reverse
 *   …and everything in between.
 *
 * Controls:
 *   Drag sliders     — adjust wave speed, angle, and colours
 *   LEFT / RIGHT     — rotate angle by 5°
 *   1                — preset  0° (horizontal)
 *   2                — preset 45° (diagonal)
 *   3                — preset 90° (vertical)
 *   4                — preset 135° (anti-diagonal)
 *   H                — toggle control panel
 *   R                — restart animation
 */

import algorithmic.typography.*;
import algorithmic.typography.ui.*;

AlgorithmicTypography at;
ObservableConfiguration config;

// ── Sliders ───────────────────────────────────────────────────────
Slider speedSlider;
Slider angleSlider;
Slider hueMinSlider;
Slider hueMaxSlider;
Slider satMaxSlider;
Slider[] sliders;

boolean panelVisible = true;

// ── Layout constants ──────────────────────────────────────────────
float panelX = 10, panelY = 10, panelW = 320;
float pad = 10, header = 24;

void setup() {
  size(1080, 1080);

  config = new ObservableConfiguration();
  config.loadFromJSON(loadJSONObject("config.json"));

  // Log every parameter change
  config.addListener((k, v) -> println("Config: " + k + " = " + v));

  at = new AlgorithmicTypography(this);
  at.setConfiguration(config);
  at.setAutoRender(false);
  at.initialize();

  buildSliders();

  println("WaveAngle — use sliders or LEFT/RIGHT keys, 1-4 for presets, H to toggle panel");
}

void draw() {
  at.render();
  if (panelVisible) drawPanel();

  // Persistent HUD in top-right
  fill(255, 200);
  noStroke();
  textAlign(RIGHT, TOP);
  textSize(13);
  text("Angle " + String.format("%.0f", config.getWaveAngle()) + "°"
     + "   Speed " + String.format("%.1f", config.getWaveSpeed())
     + "   FPS " + String.format("%.0f", frameRate),
       width - 12, 12);
}

// ── Slider setup ──────────────────────────────────────────────────

void buildSliders() {
  float tw = panelW - pad * 2 - 140;   // track width

  speedSlider  = mkSlider("Wave Speed",  0.1, 10,  config.getWaveSpeed(),  tw, 1);
  angleSlider  = mkSlider("Wave Angle",    0, 360, config.getWaveAngle(),  tw, 0).setSuffix("°");
  hueMinSlider = mkSlider("Hue Min",       0, 360, config.getHueMin(),     tw, 0).setSuffix("°");
  hueMaxSlider = mkSlider("Hue Max",       0, 360, config.getHueMax(),     tw, 0).setSuffix("°");
  satMaxSlider = mkSlider("Sat Max",        0, 255, config.getSaturationMax(), tw, 0);

  speedSlider .onChange(v -> config.setWaveSpeed(v));
  angleSlider .onChange(v -> config.setWaveAngle(v));
  hueMinSlider.onChange(v -> config.setHueRange(v, config.getHueMax()));
  hueMaxSlider.onChange(v -> config.setHueRange(config.getHueMin(), v));
  satMaxSlider.onChange(v -> config.setSaturationRange(config.getSaturationMin(), v));

  sliders = new Slider[] { speedSlider, angleSlider, hueMinSlider, hueMaxSlider, satMaxSlider };

  layoutSliders();
}

Slider mkSlider(String label, float mn, float mx, float init, float tw, int dec) {
  return new Slider(label, mn, mx, init, tw).setDecimals(dec);
}

void layoutSliders() {
  float y = panelY + header + pad;
  for (Slider s : sliders) {
    s.setPosition(panelX + pad, y);
    y += Slider.getRowHeight() + 6;
  }
}

// ── Panel drawing ─────────────────────────────────────────────────

void drawPanel() {
  // Sync sliders from config (in case keys changed values)
  speedSlider .setValue(config.getWaveSpeed());
  angleSlider .setValue(config.getWaveAngle());
  hueMinSlider.setValue(config.getHueMin());
  hueMaxSlider.setValue(config.getHueMax());
  satMaxSlider.setValue(config.getSaturationMax());

  float ph = header + pad + sliders.length * (Slider.getRowHeight() + 6) + 40;

  fill(0, 180);
  noStroke();
  rect(panelX, panelY, panelW, ph, 6);

  fill(255);
  textAlign(LEFT, TOP);
  textSize(13);
  text("WAVE ANGLE  (H to hide)", panelX + pad, panelY + 6);

  for (Slider s : sliders) s.display(g);

  // Angle compass preview
  float cx = panelX + panelW - 40;
  float cy = panelY + ph - 30;
  float r  = 14;
  noFill();
  stroke(100);
  ellipse(cx, cy, r * 2, r * 2);
  float rad = radians(config.getWaveAngle());
  stroke(255);
  line(cx, cy, cx + cos(rad) * r, cy + sin(rad) * r);
  noStroke();
}

// ── Input ─────────────────────────────────────────────────────────

void keyPressed() {
  switch (key) {
    case '1': config.setWaveAngle(0);   println("Preset: 0° (horizontal)");      break;
    case '2': config.setWaveAngle(45);  println("Preset: 45° (diagonal)");       break;
    case '3': config.setWaveAngle(90);  println("Preset: 90° (vertical)");       break;
    case '4': config.setWaveAngle(135); println("Preset: 135° (anti-diagonal)"); break;
    case 'h': case 'H': panelVisible = !panelVisible; break;
    case 'r': case 'R': at.restart(); break;
  }

  if (keyCode == LEFT)  config.setWaveAngle((config.getWaveAngle() - 5 + 360) % 360);
  if (keyCode == RIGHT) config.setWaveAngle((config.getWaveAngle() + 5) % 360);
}

void mousePressed()  { for (Slider s : sliders) s.mousePressed(mouseX, mouseY);  }
void mouseDragged()  { for (Slider s : sliders) s.mouseDragged(mouseX, mouseY);  }
void mouseReleased() { for (Slider s : sliders) s.mouseReleased();               }
