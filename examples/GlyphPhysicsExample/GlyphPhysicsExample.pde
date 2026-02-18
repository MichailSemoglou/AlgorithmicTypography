/*
 * GlyphPhysicsExample
 *
 * Demonstrates the GlyphPhysics class — particle-based
 * physics for glyph outlines.
 *
 * Each vertex on the letterform becomes a particle with
 * attraction / repulsion to the mouse and a spring force
 * pulling it back to its home position.
 *
 * Controls:
 *   1-3      Switch display mode (Points / Lines / Filled)
 *   SPACE    Cycle through characters
 *   R        Reset particles to home positions
 *   UP/DOWN  Adjust mouse force strength
 *   +/-      Adjust mouse interaction radius
 *   C        Toggle rainbow colour mode
 */

import algorithmic.typography.render.GlyphExtractor;
import algorithmic.typography.render.GlyphPhysics;

GlyphExtractor glyph;
GlyphPhysics   physics;

int     modeIdx    = 0;
String[] modes     = {"Points", "Lines", "Filled"};
char[]  chars      = {'A', 'B', 'R', 'W', 'g', '&', '@', '%'};
int     charIdx    = 0;
float   strength   = -3.0;   // negative = repel from cursor
float   radius     = 200.0;
boolean rainbow    = true;

void setup() {
  size(1080, 1080);

  glyph = new GlyphExtractor(this, "Helvetica", 72);
  glyph.setFlatness(0.4);

  physics = new GlyphPhysics(this, glyph);
  physics.setMouseAttraction(strength);
  physics.setMouseRadius(radius);
  physics.setSpring(0.04);
  physics.setDamping(0.88);
  physics.setPointSize(3);
  physics.setRainbow(rainbow);

  loadChar();

  println("GlyphPhysics — 1-3=mode  SPACE=char  R=reset  UP/DOWN=strength  +/-=radius  C=colour");
}

void loadChar() {
  physics.setChar(chars[charIdx], 600);
}

void draw() {
  background(0);

  physics.update();

  switch (modeIdx) {
    case 0: physics.displayPoints(); break;
    case 1: physics.displayLines();  break;
    case 2: physics.displayFilled(); break;
  }

  // HUD
  fill(255, 180);
  noStroke();
  textSize(14);
  textAlign(LEFT, TOP);
  text("Mode: " + modes[modeIdx] +
       "\nChar: " + chars[charIdx] +
       "\nForce: " + nf(strength, 1, 1) +
       "\nRadius: " + (int)radius +
       "\nParticles: " + physics.getCount() +
       "\nFPS: " + (int)frameRate, 16, 16);
}

void keyPressed() {
  if (key == '1') modeIdx = 0;
  if (key == '2') modeIdx = 1;
  if (key == '3') modeIdx = 2;

  if (key == ' ') {
    charIdx = (charIdx + 1) % chars.length;
    loadChar();
  }

  if (key == 'r' || key == 'R') physics.reset();

  if (keyCode == UP) {
    strength -= 0.5;
    physics.setMouseAttraction(strength);
  }
  if (keyCode == DOWN) {
    strength += 0.5;
    physics.setMouseAttraction(strength);
  }

  if (key == '+' || key == '=') {
    radius = min(radius + 20, 600);
    physics.setMouseRadius(radius);
  }
  if (key == '-' || key == '_') {
    radius = max(radius - 20, 30);
    physics.setMouseRadius(radius);
  }

  if (key == 'c' || key == 'C') {
    rainbow = !rainbow;
    physics.setRainbow(rainbow);
  }
}