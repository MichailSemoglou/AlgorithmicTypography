/*
 * VibeCoding
 *
 * Demonstrates every vibe keyword supported by VibePreset.apply().
 * Arrow keys cycle through all 26 presets; SPACE applies a random
 * two-word compound; R restarts the animation.
 *
 * CALM       calm · zen · peaceful · serene
 * ENERGETIC  energy · rave · techno · hype
 * MELANCHOLY melancholy · rain · sad · nostalgic
 * CHAOTIC    chaos · glitch · noise · digital
 * OCEAN      ocean · flow · wave · water
 * MINIMAL    minimal · sparse · simple
 * LIGHT/DARK dark · night · bright · day · light
 *
 * Controls:
 *   ← / →    Previous / next vibe
 *   SPACE    Random compound vibe (two keywords blended)
 *   R        Restart animation
 *
 * Editorial note:
 *   The following vibe presets are the author's artistic interpretation,
 *   developed from 30+ years of professional practice in the design industry.
 *   They are qualitative mood anchors for creative workflows —
 *   not derived from psychoacoustic research, affective computing models,
 *   or colour psychology studies.
 */

import algorithmic.typography.*;
import algorithmic.typography.system.VibePreset;

AlgorithmicTypography at;

// ── All 26 vibe keywords ──────────────────────────────────────────────────
String[][] VIBES = {
  { "CALM",       "calm",       "zen",      "peaceful",  "serene"   },
  { "ENERGETIC",  "energy",     "rave",     "techno",    "hype"     },
  { "MELANCHOLY", "melancholy", "rain",     "sad",       "nostalgic"},
  { "CHAOTIC",    "chaos",      "glitch",   "noise",     "digital"  },
  { "OCEAN",      "ocean",      "flow",     "wave",      "water"    },
  { "MINIMAL",    "minimal",    "sparse",   "simple"                },
  { "LIGHT/DARK", "dark",       "night",    "bright",    "day",     "light"   }
};

// Flat list built from the table above (index 0 of each row is the label, skip it)
String[] allVibes;
String[] allLabels;

int vibeIndex = 0;
String currentVibe  = "";
String currentLabel = "";
String compound     = "";   // shown when SPACE was last used

void setup() {
  size(1080, 1080);

  // Build flat arrays
  int count = 0;
  for (String[] g : VIBES) count += g.length - 1;
  allVibes  = new String[count];
  allLabels = new String[count];
  int i = 0;
  for (String[] g : VIBES) {
    for (int j = 1; j < g.length; j++) {
      allVibes[i]  = g[j];
      allLabels[i] = g[0];
      i++;
    }
  }

  at = new AlgorithmicTypography(this);
  at.setAutoRender(false);
  at.getConfiguration().setSaveFrames(false);

  applyVibe(0);
  at.initialize();

  println("VibeCoding — ←/→ cycle  SPACE compound  R restart");
}

void draw() {
  at.render();

  // HUD
  fill(255, 220);
  noStroke();
  textAlign(LEFT, TOP);
  textSize(13);
  String info = compound.isEmpty()
    ? "Vibe: " + currentVibe + "  [" + currentLabel + "]"
    : "Compound: " + compound;
  text(info + "   FPS: " + (int)frameRate, 16, 16);

  // Mini legend — dim, bottom-left
  fill(255, 80);
  textSize(11);
  textAlign(LEFT, BOTTOM);
  text("← → cycle all vibes   SPACE random compound   R restart", 16, height - 14);
}

// ── Helpers ───────────────────────────────────────────────────────────────

void applyVibe(int idx) {
  vibeIndex    = idx;
  currentVibe  = allVibes[idx];
  currentLabel = allLabels[idx];
  compound     = "";
  VibePreset.apply(at.getConfiguration(), currentVibe);
  println("Vibe: " + currentVibe + " [" + currentLabel + "]");
}

void applyCompound() {
  int a = (int)random(allVibes.length);
  int b;
  do { b = (int)random(allVibes.length); } while (b == a);
  compound    = allVibes[a] + " " + allVibes[b];
  currentVibe = compound;
  VibePreset.apply(at.getConfiguration(), compound);
  println("Compound: " + compound);
}

// ── Controls ──────────────────────────────────────────────────────────────

void keyPressed() {
  if (keyCode == RIGHT) {
    applyVibe((vibeIndex + 1) % allVibes.length);
  } else if (keyCode == LEFT) {
    applyVibe((vibeIndex - 1 + allVibes.length) % allVibes.length);
  } else if (key == ' ') {
    applyCompound();
  } else if (key == 'r' || key == 'R') {
    at.restart();
  }
}

