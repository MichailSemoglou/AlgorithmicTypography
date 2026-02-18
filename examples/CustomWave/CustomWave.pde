/*
 * CustomWave
 *
 * Demonstrates every wave type available in the library:
 * five built-in mathematical presets, Perlin noise, and a
 * custom Julia-set fractal wave.
 *
 * Controls:
 *   SPACE / RIGHT — Next wave type
 *   LEFT          — Previous wave type
 *   R             — Restart animation
 */

import algorithmic.typography.*;
import algorithmic.typography.core.*;

AlgorithmicTypography at;
WaveFunction fractalWave;

// Fractal parameters
float fractalZoom = 2.8;
int fractalMaxIter = 24;
float fractalOrbitSpeed = 0.008;

String[] waveNames = {
  "Default", "Sine", "Tangent", "Square",
  "Triangle", "Sawtooth", "Perlin", "Fractal"
};
int currentWave = 0;

void setup() {
  size(800, 800);

  // Create fractal wave as anonymous WaveFunction
  fractalWave = new WaveFunction() {
    public float calculate(int frameCount, float x, float y,
                           float time, Configuration cfg) {
      // Map normalised grid position to complex plane
      float zr = (x - 0.5) * fractalZoom;
      float zi = (y - 0.5) * fractalZoom;

      // Julia constant orbits through visually rich regions
      float angle = frameCount * fractalOrbitSpeed;
      float cr = -0.4  + 0.3  * sin(angle);
      float ci =  0.6  + 0.15 * cos(angle * 1.7);

      // Iterate z = z² + c
      int iter = 0;
      float zr2, zi2;
      while (iter < fractalMaxIter) {
        zr2 = zr * zr - zi * zi + cr;
        zi2 = 2 * zr * zi + ci;
        zr  = zr2;
        zi  = zi2;
        if (zr * zr + zi * zi > 4) break;
        iter++;
      }

      // Smooth escape-time colouring
      float smooth;
      if (iter >= fractalMaxIter) {
        smooth = 0;
      } else {
        float modulus = sqrt(zr * zr + zi * zi);
        smooth = (iter + 1 - log(log(modulus)) / log(2))
                 / fractalMaxIter;
        smooth = constrain(smooth, 0, 1);
      }

      return map(smooth, 0, 1,
          cfg.getBrightnessMin(), cfg.getBrightnessMax());
    }

    public String getName() {
      return "Fractal (Julia Set)";
    }

    public String getDescription() {
      return "Animated Julia set fractal with orbiting constant";
    }
  };

  at = new AlgorithmicTypography(this);
  at.loadConfiguration("config.json");
  at.setAutoRender(false);
  at.initialize();

  println("CustomWave — SPACE / arrows to cycle wave types");
}

void draw() {
  at.render();

  // HUD pill
  fill(255);
  noStroke();
  String label = (currentWave + 1) + "/" + waveNames.length
               + "  " + waveNames[currentWave];
  float tw = textWidth(label) + 24;
  rect(14, 14, tw, 28, 6);
  fill(0);
  textAlign(LEFT, CENTER);
  textSize(14);
  text(label, 26, 28);
}

void selectWave(int index) {
  currentWave = index;
  switch (index) {
    case 0: at.setWaveFunction(null);                    break;
    case 1: at.setWaveFunction(WavePresets.sine());      break;
    case 2: at.setWaveFunction(WavePresets.tangent());   break;
    case 3: at.setWaveFunction(WavePresets.square());    break;
    case 4: at.setWaveFunction(WavePresets.triangle());  break;
    case 5: at.setWaveFunction(WavePresets.sawtooth());  break;
    case 6: at.setWaveFunction(WavePresets.perlin(this)); break;
    case 7: at.setWaveFunction(fractalWave);             break;
  }
  println("Wave: " + waveNames[currentWave]);
}

void keyPressed() {
  if (key == ' ' || keyCode == RIGHT) {
    selectWave((currentWave + 1) % waveNames.length);
  } else if (keyCode == LEFT) {
    selectWave((currentWave - 1 + waveNames.length) % waveNames.length);
  } else if (key == 'r' || key == 'R') {
    at.restart();
  }
}
