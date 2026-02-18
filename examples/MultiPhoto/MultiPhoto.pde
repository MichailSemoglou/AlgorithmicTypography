/*
 * MultiPhoto
 *
 * Each cell in the typography grid displays a different
 * photograph as its background. The library's wave-driven
 * glyphs are rendered on top.
 *
 * Place images named photo-01.png … photo-16.png in the
 * data/ folder.  If fewer images are supplied the sketch
 * cycles through whatever is available.  If none are found
 * it generates colourful gradient placeholders so the
 * example works out of the box.
 *
 *   Stage 1 (0-6s)   — 4x4 grid  (up to 16 photos)
 *   Stage 2 (6-12s)  — 3x3 grid  (up to 9 photos)
 *   Stage 3 (12-18s) — 2x2 grid  (up to 4 photos)
 *
 * Controls:
 *   R  - Restart the animation
 *   S  - Toggle frame saving
 */

import algorithmic.typography.*;

AlgorithmicTypography at;
Configuration cfg;

// Maximum grid dimension across all stages
final int MAX_CELLS = 16;
PImage[] photos;
int photoCount;
int startTime;

void setup() {
  size(800, 800);

  // ── Load photographs ──────────────────────────────────
  photos = new PImage[MAX_CELLS];
  photoCount = 0;

  for (int i = 1; i <= MAX_CELLS; i++) {
    String name = "photo-" + nf(i, 2) + ".png";
    PImage img = loadImage(name);
    if (img != null && img.width > 0) {
      photos[photoCount++] = img;
    }
  }

  // Also try .jpg variants
  if (photoCount == 0) {
    for (int i = 1; i <= MAX_CELLS; i++) {
      String name = "photo-" + nf(i, 2) + ".jpg";
      PImage img = loadImage(name);
      if (img != null && img.width > 0) {
        photos[photoCount++] = img;
      }
    }
  }

  // Generate placeholders when no photos found
  if (photoCount == 0) {
    println("No photos found — generating gradient placeholders.");
    println("Add photo-01.png … photo-16.png to data/ for real images.");
    for (int i = 0; i < MAX_CELLS; i++) {
      photos[i] = createPlaceholder(200, 200, i);
    }
    photoCount = MAX_CELLS;
  } else {
    println("Loaded " + photoCount + " photograph(s).");
  }

  // ── Initialise the typography system ──────────────────
  at = new AlgorithmicTypography(this);
  at.loadConfiguration("config.json");
  at.setAutoRender(false);
  at.initialize();
  cfg = at.getConfiguration();
  startTime = millis();

  println("MultiPhoto — press R to restart, S to toggle saving");
}

void draw() {
  background(0);

  // Determine current grid size from elapsed time
  int elapsed = millis() - startTime;
  int changeTime       = cfg.getChangeTime();
  int secondChangeTime = cfg.getSecondChangeTime();
  int cols, rows;

  if (secondChangeTime > 0 && elapsed > secondChangeTime) {
    cols = cfg.getFinalTilesX();
    rows = cfg.getFinalTilesY();
  } else if (elapsed > changeTime) {
    cols = cfg.getChangedTilesX();
    rows = cfg.getChangedTilesY();
  } else {
    cols = cfg.getInitialTilesX();
    rows = cfg.getInitialTilesY();
  }

  // ── Draw per-cell photographs ─────────────────────────
  float cellW = (float) width  / cols;
  float cellH = (float) height / rows;

  for (int gx = 0; gx < cols; gx++) {
    for (int gy = 0; gy < rows; gy++) {
      int idx = (gy * cols + gx) % photoCount;
      PImage img = photos[idx];

      // Centre-crop the photo to fill the cell
      float cx = gx * cellW;
      float cy = gy * cellH;
      drawCroppedImage(img, cx, cy, cellW, cellH);
    }
  }

  // ── Overlay the typography grid ───────────────────────
  at.renderAt(0, 0, width, height);
}

// ── Helpers ──────────────────────────────────────────────

/** Draws `img` cropped to cover the rectangle (dx, dy, dw, dh). */
void drawCroppedImage(PImage img, float dx, float dy, float dw, float dh) {
  float imgAspect  = (float) img.width / img.height;
  float cellAspect = dw / dh;

  int sx, sy, sw, sh;

  if (imgAspect > cellAspect) {
    // Image is wider → crop sides
    sh = img.height;
    sw = (int)(sh * cellAspect);
    sx = (img.width - sw) / 2;
    sy = 0;
  } else {
    // Image is taller → crop top/bottom
    sw = img.width;
    sh = (int)(sw / cellAspect);
    sx = 0;
    sy = (img.height - sh) / 2;
  }

  image(img, dx, dy, dw, dh, sx, sy, sx + sw, sy + sh);
}

/** Creates a colourful gradient placeholder image. */
PImage createPlaceholder(int w, int h, int index) {
  PImage img = createImage(w, h, RGB);
  img.loadPixels();

  float hueBase = (index * 137.508) % 360;   // golden-angle spread

  for (int y = 0; y < h; y++) {
    for (int x = 0; x < w; x++) {
      float hue = (hueBase + x * 0.5 + y * 0.3) % 360;
      float sat = 180 + sin(x * 0.05) * 50;
      float bri = 160 + cos(y * 0.08 + index) * 60;
      colorMode(HSB, 360, 255, 255);
      img.pixels[y * w + x] = color(hue, sat, bri);
    }
  }

  img.updatePixels();
  colorMode(RGB, 255);
  return img;
}

void keyPressed() {
  if (key == 'r' || key == 'R') {
    at.restart();
    startTime = millis();
  } else if (key == 's' || key == 'S') {
    at.toggleFrameSaving();
  }
}
