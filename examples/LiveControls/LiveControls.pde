/*
 * LiveControls
 *
 * Real-time parameter control with interactive GUI sliders,
 * keyboard shortcuts, and optional OSC input.
 *
 * The library's built-in ControlPanel provides draggable sliders
 * for every HSB and animation parameter — no external GUI library needed.
 *
 * Optional library:
 *   oscP5 — OSC remote control (port 12000)
 *
 * Keyboard:
 *   1/2 — Wave speed down/up
 *   3/4 — Brightness min down/up
 *   5/6 — Saturation max down/up
 *   7/8 — Hue range shift left/right
 *   9/0 — Text scale down/up
 *   B   — Cycle cell border sides: None → Horizontal rules → All sides
 *   V   — Toggle cell border colour: Static ⇄ Wave (grid lines pulse with the wave)
 *   H   — Toggle both panels
 *   R   — Restart
 *   S   — Toggle frame saving
 */

import algorithmic.typography.*;
import algorithmic.typography.ui.*;

AlgorithmicTypography at;
ObservableConfiguration config;
ControlPanel panel;
OSCBridge osc;

// Border controls
Slider borderWeightSlider;
int    borderSideMode  = 0;   // 0=NONE, 1=TOP+BOTTOM, 2=ALL
boolean borderWaveMode = false;
boolean showPanels     = true;

// Layout: ControlPanel is 10,10 : 320 wide, 8 sliders
// panelHeight = 24 + 10 + 8*(18+6) + 40 = 266  → bottom at y=276
final float BORDER_PANEL_Y = 280;
final float BORDER_PANEL_W = 320;

void setup() {
  size(1080, 1080);

  config = new ObservableConfiguration();
  config.loadFromJSON(loadJSONObject("config.json"));

  config.addListener((k, v) -> {
    println("Config changed: " + k + " = " + v);
  });

  at = new AlgorithmicTypography(this);
  at.setConfiguration(config);
  at.setAutoRender(false);
  at.initialize();

  panel = new ControlPanel(this, config);
  panel.createControls();

  // Cell border weight slider — positioned just below the ControlPanel
  float tw = BORDER_PANEL_W - 10 * 2 - 140;
  borderWeightSlider = new Slider("Border Weight", 0.1, 5, config.getCellBorderWeight(), tw)
                         .setDecimals(1);
  borderWeightSlider.setPosition(20, BORDER_PANEL_Y + 34);
  borderWeightSlider.onChange(v -> config.setCellBorderWeight(v));

  osc = new OSCBridge(this, config, 12000, 12001);
  if (osc.start()) {
    println("OSC ready on port 12000");
  }

  println("LiveControls — drag sliders or use 1-0 keys, H to toggle panel");
}

void draw() {
  at.render();
  panel.draw();

  if (showPanels) {
    // Border mini-panel
    fill(0, 180);
    noStroke();
    rect(10, BORDER_PANEL_Y, BORDER_PANEL_W, 60, 6);
    fill(255);
    textAlign(LEFT, TOP);
    textSize(12);
    String[] sideLabels  = { "None", "Horiz rules", "All sides" };
    text("BORDERS  sides:" + sideLabels[borderSideMode]
       + "  colour:" + (borderWaveMode ? "Wave" : "Static")
       + "  [B / V]",
         20, BORDER_PANEL_Y + 6);
    borderWeightSlider.display(g);
  }
}

void keyPressed() {
  panel.handleKeyPress(key);

  if (key == 'h' || key == 'H') {
    showPanels = !showPanels;
  } else if (key == 'r' || key == 'R') {
    at.restart();
  } else if (key == 's' || key == 'S') {
    at.toggleFrameSaving();
  } else if (key == 'b' || key == 'B') {
    final int[] SIDE_MODES = {
      Configuration.BORDER_NONE,
      Configuration.BORDER_TOP | Configuration.BORDER_BOTTOM,
      Configuration.BORDER_ALL
    };
    final String[] SIDE_NAMES = { "None", "Horizontal rules", "All sides" };
    borderSideMode = (borderSideMode + 1) % 3;
    config.setCellBorderSides(SIDE_MODES[borderSideMode]);
    println("Border sides: " + SIDE_NAMES[borderSideMode]);
  } else if (key == 'v' || key == 'V') {
    borderWaveMode = !borderWaveMode;
    config.setCellBorderColorMode(
      borderWaveMode ? Configuration.BORDER_COLOR_WAVE : Configuration.BORDER_COLOR_STATIC);
    println("Border colour: " + (borderWaveMode ? "Wave" : "Static"));
  }
}

void mousePressed() {
  panel.mousePressed(mouseX, mouseY);
  borderWeightSlider.mousePressed(mouseX, mouseY);
}

void mouseDragged() {
  panel.mouseDragged(mouseX, mouseY);
  borderWeightSlider.mouseDragged(mouseX, mouseY);
}

void mouseReleased() {
  panel.mouseReleased();
  borderWeightSlider.mouseReleased();
}

// Called automatically when oscP5 is installed
void oscEvent(Object msg) {
  if (osc != null) osc.handleMessage(msg);
}

void dispose() {
  if (osc != null) osc.stop();
  if (panel != null) panel.dispose();
}
