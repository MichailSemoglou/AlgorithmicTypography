/*
 * AudioReactive
 *
 * Audio-reactive typography using the AudioBridge.
 * Bass frequencies drive wave speed,
 * treble frequencies control color saturation,
 * and beat detection triggers visual events.
 *
 * Requires: Sound library (bundled with Processing 4)
 *   Sketch > Import Library > Sound
 */

import algorithmic.typography.*;
import algorithmic.typography.audio.*;

AlgorithmicTypography at;
ObservableConfiguration config;
AudioBridge audio;

void setup() {
  size(1080, 1080);

  at = new AlgorithmicTypography(this);

  config = new ObservableConfiguration();
  config.loadFromJSON(loadJSONObject("config.json"));
  at.setConfiguration(config);

  audio = new AudioBridge(this, config);

  if (audio.useMicrophone()) {
    println("Audio initialised — make some noise!");
  } else {
    println("Audio not available. Install the Sound library.");
  }

  // Map audio → parameters (quiet = normal appearance, loud = more intense)
  audio.mapBassTo(config::setWaveSpeed, 2, 10);         // bass speeds up the wave
  audio.mapTrebleTo(config::setBrightnessMax, 180, 255); // treble brightens the grid

  at.setAutoRender(false);  // we call render() manually
  at.initialize();
}

void draw() {
  audio.update();
  at.render();

  // Optional debug overlay
  audio.displayDebug(10, 10);

  if (audio.isBeat()) {
    println("BEAT! Bass: " + nf(audio.getBass(), 1, 2));
  }
}

void dispose() {
  audio.stop();
}
