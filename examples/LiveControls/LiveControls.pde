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
 *   H   — Toggle control panel
 *   R   — Restart
 *   S   — Toggle frame saving
 */

import algorithmic.typography.*;
import algorithmic.typography.ui.*;

AlgorithmicTypography at;
ObservableConfiguration config;
ControlPanel panel;
OSCBridge osc;

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

  osc = new OSCBridge(this, config, 12000, 12001);
  if (osc.start()) {
    println("OSC ready on port 12000");
  }

  println("LiveControls — drag sliders or use 1-0 keys, H to toggle panel");
}

void draw() {
  at.render();
  panel.draw();
}

void keyPressed() {
  panel.handleKeyPress(key);

  if (key == 'r' || key == 'R') {
    at.restart();
  } else if (key == 's' || key == 'S') {
    at.toggleFrameSaving();
  }
}

void mousePressed() {
  panel.mousePressed(mouseX, mouseY);
}

void mouseDragged() {
  panel.mouseDragged(mouseX, mouseY);
}

void mouseReleased() {
  panel.mouseReleased();
}

// Called automatically when oscP5 is installed
void oscEvent(Object msg) {
  if (osc != null) osc.handleMessage(msg);
}

void dispose() {
  if (osc != null) osc.stop();
  if (panel != null) panel.dispose();
}
