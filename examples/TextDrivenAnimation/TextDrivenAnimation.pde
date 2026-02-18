/*
 * TextDrivenAnimation
 *
 * Rita library integration: generate words from Markov
 * chains or grammars and map linguistic features
 * (syllables, sentiment) to grid animation parameters.
 *
 * Requires: RiTa library
 *   Install via Processing:
 *     Sketch → Import Library… → Manage Libraries…
 *     Search "RiTa" and click Install
 *
 * Controls:
 *   SPACE  Generate new text
 *   M      Toggle Markov / Grammar mode
 *   R      Restart animation
 *   S      Save frame
 */

import algorithmic.typography.*;
import algorithmic.typography.text.*;

AlgorithmicTypography at;
ObservableConfiguration config;
RitaBridge rita;

String[] gridWords;
boolean useMarkov = true;
String mode = "Markov";

void setup() {
  size(1200, 800, P2D);

  config = new ObservableConfiguration();
  config.setCanvasSize(width, height);
  config.setGridSize(12, 8);
  config.setTextScale(0.5);
  config.setSaveFrames(false);

  at = new AlgorithmicTypography(this);
  at.setAutoRender(false);   // We render our own word grid
  at.setConfiguration(config);
  at.initialize();

  rita = new RitaBridge(this);

  if (rita.isAvailable()) {
    rita.loadMarkov("sample-text.txt");
    generateNewText();
  } else {
    println("RiTa not found — loading words directly from sample-text.txt");
    println("Install RiTa for Markov/grammar generation:");
    println("  Sketch → Import Library… → Manage Libraries… → search 'RiTa'");
    loadFallbackText();
  }

  println("TextDrivenAnimation — SPACE=new text  M=toggle mode");
}

void draw() {
  background(0);
  renderWordGrid();
  drawInfo();
}

void renderWordGrid() {
  int tilesX = config.getInitialTilesX();
  int tilesY = config.getInitialTilesY();
  float tileW = (float) width / tilesX;
  float tileH = (float) height / tilesY;

  colorMode(HSB, 360, 255, 255);

  int wi = 0;
  float t = frameCount * 0.03;   // time factor for animation

  for (int x = 0; x < tilesX; x++) {
    for (int y = 0; y < tilesY; y++) {
      if (wi >= gridWords.length) break;

      String word = gridWords[wi];

      // Wave-driven colour: hue cycles with position + time
      float hue = (sin(t + x * 0.4 + y * 0.3) * 0.5 + 0.5) * 360;
      float sat = 180 + sin(t * 0.7 + wi * 0.1) * 75;
      float bri = 180 + cos(t + (x + y) * 0.5) * 75;

      fill(hue, sat, bri);
      pushMatrix();
      translate(x * tileW, y * tileH);

      float ts = min(tileW, tileH) * config.getTextScale();
      textSize(ts);
      textAlign(CENTER, CENTER);
      text(word, tileW / 2, tileH / 2);
      popMatrix();

      wi++;
    }
  }

  colorMode(RGB, 255);
}

void drawInfo() {
  fill(255);
  textAlign(LEFT, TOP);
  textSize(14);

  int y = 20;
  text("Mode: " + mode, 20, y); y += 20;
  text("Rita: " + rita.isAvailable(), 20, y); y += 20;
  text("Words: " + (gridWords != null ? gridWords.length : 0), 20, y);
}

void generateNewText() {
  if (!rita.isAvailable()) {
    // Without RiTa: shuffle words from the sample text
    loadFallbackText();
    return;
  }

  int count = config.getInitialTilesX() * config.getInitialTilesY();

  if (useMarkov) {
    gridWords = rita.generateWords(count);
    mode = "Markov Chain";
  } else {
    String grammar = RitaBridge.createPoetryGrammar();
    gridWords = rita.generateFromGrammar(grammar, count);
    mode = "Grammar";
  }

  rita.applyTextAnalysisToConfig(config, String.join(" ", gridWords));
  println("Generated " + gridWords.length + " words (" + mode + ")");
}

// Fallback: load actual words from sample-text.txt when RiTa is unavailable
String[] allSampleWords;

void loadFallbackText() {
  if (allSampleWords == null) {
    String[] lines = loadStrings("sample-text.txt");
    if (lines == null || lines.length == 0) {
      println("ERROR: Could not load data/sample-text.txt");
      gridWords = new String[]{"file", "not", "found"};
      return;
    }
    String raw = join(lines, " ");
    // Remove attribution line
    raw = raw.replaceAll("--.*$", "").trim();
    // Split into words, remove empty entries
    String[] tokens = splitTokens(raw, " \t\n\r");
    allSampleWords = tokens;
    println("Loaded " + allSampleWords.length + " words from sample-text.txt");
  }

  int count = config.getInitialTilesX() * config.getInitialTilesY();
  gridWords = new String[count];

  // Pick a random starting position and fill the grid
  int start = (int) random(max(1, allSampleWords.length - count));
  for (int i = 0; i < count; i++) {
    gridWords[i] = allSampleWords[(start + i) % allSampleWords.length];
  }
  mode = "Sample Text (no RiTa)";
  println("Showing " + count + " words from sample-text.txt (SPACE to reshuffle)");
}

void keyPressed() {
  if (key == ' ') {
    generateNewText();
  } else if (key == 'm' || key == 'M') {
    useMarkov = !useMarkov;
    generateNewText();
  } else if (key == 'r' || key == 'R') {
    at.restart();
  } else if (key == 's' || key == 'S') {
    saveFrame("text-driven-####.png");
  }
}
