/**
 * Configuration — p5.js port of algorithmic.typography.Configuration
 *
 * Holds all parameters that drive the wave-based typography animation.
 * Mirrors the Processing/Java Configuration class defaults and validation.
 */
class Configuration {
  constructor() {
    // Canvas
    this.canvasWidth = 1080;
    this.canvasHeight = 1080;

    // Timing
    this.animationDuration = 18; // seconds
    this.animationFPS = 30;
    this.changeTime = 6000; // ms — first grid change
    this.secondChangeTime = 12000; // ms — second grid change (0 = disabled)
    this.fadeDuration = 2000; // ms — cross-fade duration

    // Character
    this.character = "A";
    this.textScale = 0.8;

    // Export
    this.saveFrames = false;

    // Wave
    this.waveSpeed = 1.0;
    this.waveAngle = 45.0;
    this.waveMultiplierMin = 0.0;
    this.waveMultiplierMax = 2.0;

    // Grid stages
    this.initialTilesX = 16;
    this.initialTilesY = 16;
    this.changedTilesX = 8;
    this.changedTilesY = 8;
    this.finalTilesX = 4;
    this.finalTilesY = 4;

    // HSB colour ranges
    this.hueMin = 0;
    this.hueMax = 0;
    this.saturationMin = 0;
    this.saturationMax = 0;
    this.brightnessMin = 50;
    this.brightnessMax = 255;

    // Amplitude
    this.waveAmplitudeMin = -200;
    this.waveAmplitudeMax = 200;
  }

  /**
   * Load configuration from a plain JS object (or parsed JSON).
   * Unknown keys are silently ignored.
   */
  loadFromObject(obj) {
    if (!obj) return;
    const fields = Object.keys(this);
    for (const key of fields) {
      if (obj[key] !== undefined) {
        this[key] = obj[key];
      }
    }
  }

  /**
   * Load configuration from a JSON file (async fetch).
   * @param {string} url - URL or path to JSON config file
   * @returns {Promise<Configuration>}
   */
  async loadFromJSON(url) {
    const resp = await fetch(url);
    const json = await resp.json();
    // Support nested "config" key or flat layout
    const data = json.config || json;
    this.loadFromObject(data);
    return this;
  }

  /** Returns a shallow copy of this configuration. */
  copy() {
    const c = new Configuration();
    Object.assign(c, this);
    return c;
  }

  /** Basic validation — throws on invalid state. */
  validate() {
    if (this.canvasWidth <= 0 || this.canvasHeight <= 0) {
      throw new Error("Canvas dimensions must be positive");
    }
    if (this.textScale < 0 || this.textScale > 1) {
      throw new Error("textScale must be in [0, 1]");
    }
    if (this.initialTilesX < 1 || this.initialTilesY < 1) {
      throw new Error("Tile counts must be >= 1");
    }
  }

  // — Convenience setters with validation —

  setCanvasWidth(w) {
    if (w <= 0) throw new Error("Canvas width must be positive");
    this.canvasWidth = w;
  }
  setCanvasHeight(h) {
    if (h <= 0) throw new Error("Canvas height must be positive");
    this.canvasHeight = h;
  }
  setWaveSpeed(s) {
    this.waveSpeed = s;
  }
  setTextScale(s) {
    if (s < 0 || s > 1) throw new Error("textScale must be in [0, 1]");
    this.textScale = s;
  }
  setCharacter(c) {
    this.character = c;
  }
  setBrightnessRange(lo, hi) {
    this.brightnessMin = lo;
    this.brightnessMax = hi;
  }
  setSaturationRange(lo, hi) {
    if (lo < 0 || hi > 255) throw new Error("Saturation out of range");
    this.saturationMin = lo;
    this.saturationMax = hi;
  }
  setHueRange(lo, hi) {
    this.hueMin = lo;
    this.hueMax = hi;
  }
  setWaveAmplitudeRange(lo, hi) {
    this.waveAmplitudeMin = lo;
    this.waveAmplitudeMax = hi;
  }
  setInitialGrid(x, y) {
    this.initialTilesX = Math.max(1, x);
    this.initialTilesY = Math.max(1, y);
  }
  setChangedGrid(x, y) {
    this.changedTilesX = Math.max(1, x);
    this.changedTilesY = Math.max(1, y);
  }
  setFinalGrid(x, y) {
    this.finalTilesX = Math.max(1, x);
    this.finalTilesY = Math.max(1, y);
  }
}

// Export for ES modules; also attach to window for script-tag usage
if (typeof module !== "undefined") module.exports = { Configuration };
if (typeof window !== "undefined") window.Configuration = Configuration;
