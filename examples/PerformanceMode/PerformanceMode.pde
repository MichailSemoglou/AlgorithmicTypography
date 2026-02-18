/*
 * PerformanceMode
 *
 * Live-performance setup with preset switching
 * and mouse-driven parameter morphing.
 *
 * Optional: OSC bridge for external control (VJing, installations).
 * To enable OSC, install oscP5 and uncomment the lines marked // OSC
 *
 * Controls:
 *   LEFT/RIGHT  Switch presets
 *   1-4         Direct preset selection
 *   SPACE       Save current as new preset
 *   F           Toggle fullscreen
 *   H           Toggle HUD
 *   R           Restart
 *   Mouse X     Wave speed
 *   Mouse Y     Saturation range
 */

import algorithmic.typography.*;
// import algorithmic.typography.ui.*;  // OSC — uncomment to enable OSC

AlgorithmicTypography at;
ObservableConfiguration config;
// OSCBridge osc;                      // OSC — uncomment to enable OSC

// Preset state
JSONObject presets;
String[] presetNames;
int currentPreset = 0;
boolean showHud = true;

void setup() {
  size(1920, 1080, P2D);
  frameRate(60);

  config = new ObservableConfiguration();
  config.setCanvasSize(width, height);
  config.setGridSize(64, 36);
  config.setSaveFrames(false);

  at = new AlgorithmicTypography(this);
  at.setConfiguration(config);
  at.setAutoRender(false);
  at.initialize();

  loadPresets();
  applyPreset(presetNames[currentPreset]);

  // osc = new OSCBridge(this, config, 12000, 12001);  // OSC — uncomment to enable OSC
  // osc.start();                                      // OSC — uncomment to enable OSC

  println("PerformanceMode — ARROWS=presets  F=fullscreen  H=HUD");
}

void draw() {
  // Smooth mouse → parameter morphing
  float targetSpeed  = map(mouseX, 0, width, 0.1, 10);
  float targetBrtMin = map(mouseY, height, 0, 0, 200);

  config.setWaveSpeed(lerp(config.getWaveSpeed(), targetSpeed, 0.05));
  config.setBrightnessRange(
    lerp(config.getBrightnessMin(), targetBrtMin, 0.05),
    config.getBrightnessMax()
  );

  at.render();

  if (showHud) drawHUD();
}

// ─── Presets ────────────────────────────────────────────

void loadPresets() {
  try {
    presets = loadJSONObject("presets.json");
  } catch (Exception e) {
    presets = null;
  }
  if (presets == null) {
    presets = createDefaultPresets();
    saveJSONObject(presets, "presets.json");
  }
  refreshPresetNames();
}

void refreshPresetNames() {
  java.util.Set keySet = presets.keys();
  presetNames = new String[keySet.size()];
  int i = 0;
  for (Object k : keySet) {
    presetNames[i++] = (String) k;
  }
}

JSONObject createDefaultPresets() {
  JSONObject p = new JSONObject();

  JSONObject calm = new JSONObject();
  calm.setFloat("waveSpeed", 0.5f);
  calm.setFloat("saturationMin", 100f);
  calm.setFloat("saturationMax", 200f);
  calm.setString("character", "~");
  p.setJSONObject("Calm Waves", calm);

  JSONObject intense = new JSONObject();
  intense.setFloat("waveSpeed", 4.0f);
  intense.setFloat("saturationMin", 0f);
  intense.setFloat("saturationMax", 255f);
  intense.setString("character", "X");
  p.setJSONObject("Intense", intense);

  JSONObject minimal = new JSONObject();
  minimal.setFloat("waveSpeed", 1.0f);
  minimal.setFloat("saturationMin", 200f);
  minimal.setFloat("saturationMax", 255f);
  minimal.setString("character", "\u00b7");
  p.setJSONObject("Minimal", minimal);

  JSONObject chaos = new JSONObject();
  chaos.setFloat("waveSpeed", 8.0f);
  chaos.setFloat("saturationMin", 0f);
  chaos.setFloat("saturationMax", 255f);
  chaos.setString("character", "#");
  p.setJSONObject("Chaos", chaos);

  return p;
}

void applyPreset(String name) {
  if (!presets.hasKey(name)) return;
  JSONObject preset = presets.getJSONObject(name);
  if (preset.hasKey("waveSpeed")) {
    config.setWaveSpeed(preset.getFloat("waveSpeed"));
  }
  if (preset.hasKey("brightnessMin") && preset.hasKey("brightnessMax")) {
    config.setBrightnessRange(
      preset.getFloat("brightnessMin"),
      preset.getFloat("brightnessMax")
    );
  }
  if (preset.hasKey("character")) {
    config.setCharacter(preset.getString("character"));
  }
}

void saveCurrentAsPreset(String name) {
  JSONObject preset = new JSONObject();
  preset.setFloat("waveSpeed", config.getWaveSpeed());
  preset.setFloat("brightnessMin", config.getBrightnessMin());
  preset.setFloat("brightnessMax", config.getBrightnessMax());
  preset.setString("character", config.getCharacter());
  presets.setJSONObject(name, preset);
  saveJSONObject(presets, "presets.json");
  refreshPresetNames();
}

// ─── HUD ────────────────────────────────────────────────

void drawHUD() {
  fill(0, 150);
  noStroke();
  rect(20, 20, 300, 120);

  fill(255);
  textAlign(LEFT, TOP);

  int y = 30;
  textSize(16);
  text("ALGORITHMIC TYPOGRAPHY", 30, y);
  y += 25;

  textSize(14);
  text("Preset: " + presetNames[currentPreset], 30, y);  y += 20;
  text("Speed:  " + nf(config.getWaveSpeed(), 1, 2), 30, y); y += 20;
  text("Brt:    " + nf(config.getBrightnessMin(), 0, 0)
       + " – " + nf(config.getBrightnessMax(), 0, 0), 30, y); y += 20;
  text("FPS:    " + nf(frameRate, 0, 1), 30, y);

  drawPresetSelector();
}

void drawPresetSelector() {
  int boxW = width / presetNames.length;

  for (int i = 0; i < presetNames.length; i++) {
    boolean cur = (i == currentPreset);
    fill(cur ? 255 : 0, 100);
    rect(i * boxW, height - 40, boxW, 40);

    fill(cur ? 0 : 255);
    textAlign(CENTER, CENTER);
    textSize(12);
    text(presetNames[i], i * boxW + boxW / 2, height - 20);
  }
}

// ─── Input ──────────────────────────────────────────────

void keyPressed() {
  if (keyCode == LEFT) {
    currentPreset = (currentPreset - 1 + presetNames.length) % presetNames.length;
    applyPreset(presetNames[currentPreset]);
    println("Preset: " + presetNames[currentPreset]);
  } else if (keyCode == RIGHT) {
    currentPreset = (currentPreset + 1) % presetNames.length;
    applyPreset(presetNames[currentPreset]);
    println("Preset: " + presetNames[currentPreset]);
  } else if (key == ' ') {
    String name = "Preset " + (presetNames.length + 1);
    saveCurrentAsPreset(name);
    println("Saved: " + name);
  } else if (key == 'f' || key == 'F') {
    if (width == displayWidth && height == displayHeight) {
      surface.setSize(1920, 1080);
      surface.setLocation(100, 100);
    } else {
      surface.setLocation(0, 0);
      surface.setSize(displayWidth, displayHeight);
    }
  } else if (key == 'h' || key == 'H') {
    showHud = !showHud;
  } else if (key >= '1' && key <= '4') {
    int idx = key - '1';
    if (idx < presetNames.length) {
      currentPreset = idx;
      applyPreset(presetNames[currentPreset]);
      println("Preset: " + presetNames[currentPreset]);
    }
  } else if (key == 'r' || key == 'R') {
    at.restart();
  }
}

// void oscEvent(Object msg) {                          // OSC — uncomment to enable OSC
//   if (osc != null) osc.handleMessage(msg);
// }

// void dispose() {                                     // OSC — uncomment to enable OSC
//   if (osc != null) {
//     try { osc.stop(); } catch (Throwable t) { }
//   }
// }
