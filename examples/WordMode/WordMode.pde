/**
 * WordMode — v0.3.0 Example
 *
 * Demonstrates setContent(String): the grid's tiles are filled left-to-right,
 * top-to-bottom with successive characters from the supplied string. Characters
 * that extend past the string length wrap around, creating a seamless repeating
 * typographic texture.
 *
 * Combined with the wave engine the characters pulse, shimmer, and breathe —
 * the text becomes a living material rather than static lettering.
 *
 * Controls
 *   ↑ / ↓    : cycle through preset messages
 *   Type     : enter a custom word (backspace to delete, Return to confirm)
 *   SPACE    : toggle between word-mode and single-character mode
 *   H        : cycle hue presets (cool, warm, mono)
 *   S        : save current frame as PNG
 */

import algorithmic.typography.*;

AlgorithmicTypography at;
Configuration config;

String[] presets = {
  "TYPOGRAPHY",
  "ALGORITHM",
  "THE GRID",
  "TYPE IS ALIVE",
  "0123456789",
  "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
};
int presetIndex = 0;

boolean wordModeOn = true;
boolean typingMode = false;
String  typingBuffer = "";

int[][] huePresets = {
  { 200, 260 },  // cool blue-violet
  {   0,  50 },  // warm red-orange
  {   0,   0 }   // monochrome
};
int[][] satPresets = {
  { 100, 220 },
  {  80, 200 },
  {   0,   0 }
};
int huePresetIndex = 0;

String activeContent = presets[presetIndex];

void setup() {
  size(1080, 1080, P2D);
  colorMode(RGB, 255);

  config = new Configuration();
  config.setCanvasSize(width, height)
        .setAnimationFPS(30).setAnimationDuration(18)
        .setGridSize(13, 13)
        .setWaveSpeed(0.7f).setWaveAngle(30f).setWaveType("SINE")
        .setWaveMultiplierRange(0.2f, 2.8f)
        .setBrightnessRange(15, 255)
        .setSaturationRange(100, 220)
        .setHueRange(200, 260)
        .setWaveAmplitudeRange(-280, 280)
        .setBackgroundColor(6, 6, 18)
        .setCharacter("A")  // fallback single-char
        .setTextScale(0.88f)
        .setContent(activeContent);

  at = new AlgorithmicTypography(this);
  at.setConfiguration(config);
  at.setAutoRender(false);
  at.initialize();

  println("WordMode example loaded. Content: \"" + activeContent + "\"");
  println("  ↑/↓ : cycle presets   SPACE : toggle word/single mode");
  println("  Type a custom word, Return to confirm   H : hue preset   S : save");
}

void draw() {
  at.render();

  // HUD
  fill(255, 200);
  noStroke();
  textSize(13);
  textAlign(LEFT, TOP);

  String modeStr = wordModeOn ? "WORD MODE" : "SINGLE (" + config.getCharacter() + ")";
  String contentStr = wordModeOn ? "\"" + activeContent + "\"" : "—";
  text("Mode: " + modeStr + "   Content: " + contentStr, 16, 16);

  if (typingMode) {
    fill(255, 220, 60, 220);
    text("Type: " + typingBuffer + "|", 16, 38);
  }
}

void keyPressed() {
  if (typingMode) {
    // Intercept all keys for custom input
    if (key == RETURN || key == ENTER) {
      if (typingBuffer.length() > 0) {
        activeContent = typingBuffer;
        config.setContent(activeContent);
        println("Custom content set: \"" + activeContent + "\"");
      }
      typingMode = false;
      typingBuffer = "";
    } else if (key == BACKSPACE) {
      if (typingBuffer.length() > 0)
        typingBuffer = typingBuffer.substring(0, typingBuffer.length() - 1);
    } else if (key != CODED) {
      typingBuffer += key;
    }
    return;
  }

  if (keyCode == UP) {
    presetIndex = (presetIndex - 1 + presets.length) % presets.length;
    activeContent = presets[presetIndex];
    config.setContent(activeContent);
    wordModeOn = true;
    println("Preset: \"" + activeContent + "\"");

  } else if (keyCode == DOWN) {
    presetIndex = (presetIndex + 1) % presets.length;
    activeContent = presets[presetIndex];
    config.setContent(activeContent);
    wordModeOn = true;
    println("Preset: \"" + activeContent + "\"");

  } else if (key == ' ') {
    wordModeOn = !wordModeOn;
    if (wordModeOn) {
      config.setContent(activeContent);
    } else {
      config.setContent(null);  // disables word mode
    }
    println("Word mode: " + wordModeOn);

  } else if (key == 'H' || key == 'h') {
    huePresetIndex = (huePresetIndex + 1) % huePresets.length;
    int[] hp = huePresets[huePresetIndex];
    int[] sp = satPresets[huePresetIndex];
    config.setHueRange(hp[0], hp[1]);
    config.setSaturationRange(sp[0], sp[1]);
    println("Hue preset -> [" + hp[0] + "," + hp[1] + "]");

  } else if (key == 'S' || key == 's') {
    saveFrame("wordmode_###.png");

  } else if (key != CODED) {
    // Any printable key starts typing mode
    typingMode   = true;
    typingBuffer = "" + key;
  }
}
