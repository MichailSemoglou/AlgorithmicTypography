/**
 * AlgorithmicTypography — p5.js port
 *
 * Main class that orchestrates the wave-based typographic animation.
 * Uses a Configuration, WaveEngine, and WavePresets to replicate the
 * Processing/Java library behaviour in p5.js.
 *
 * Usage (instance mode):
 *   const at = new AlgorithmicTypography(p, config);
 *   at.initialize();
 *   // in draw():
 *   at.render();
 *
 * Usage (global mode):
 *   let at;
 *   function setup() { at = new AlgorithmicTypography(window, config); at.initialize(); }
 *   function draw()  { at.render(); }
 */

class AlgorithmicTypography {
  /**
   * @param {p5}            p      – p5 instance (or `window` in global mode)
   * @param {Configuration} [cfg]  – optional pre-built configuration
   */
  constructor(p, cfg) {
    if (!p)
      throw new Error("A p5 instance (or window in global mode) is required");
    this.p = p;
    this.config = cfg || new Configuration();
    this.waveEngine = new WaveEngine(this.config);

    // Animation state
    this.startTime = 0;
    this.currentStage = 1;
    this.frameCounter = 0;
    this.isRunning = true;
  }

  // ── Configuration helpers ───────────────────────────────────────────

  /** Replace the entire configuration. */
  setConfiguration(cfg) {
    this.config = cfg;
    this.waveEngine = new WaveEngine(cfg);
    return this;
  }

  getConfiguration() {
    return this.config;
  }
  getWaveEngine() {
    return this.waveEngine;
  }

  /** Plug in a custom wave function. */
  setWaveFunction(fn) {
    this.waveEngine.setCustomWaveFunction(fn);
    return this;
  }

  /** Use a built-in wave type (e.g. WavePresets.Type.SINE). */
  setWaveType(type) {
    this.waveEngine.setCustomWaveFunction(WavePresets.get(type));
    return this;
  }

  /**
   * Load configuration from a JSON URL (async).
   * @param {string} url
   * @returns {Promise<AlgorithmicTypography>}
   */
  async loadConfiguration(url) {
    await this.config.loadFromJSON(url);
    return this;
  }

  // ── Lifecycle ───────────────────────────────────────────────────────

  /** Call once in setup() after configuration is ready. */
  initialize() {
    const p = this.p;
    if (typeof p.frameRate === "function") {
      p.frameRate(this.config.animationFPS);
    }
    this.startTime = p.millis();
    this.frameCounter = 0;
    this.isRunning = true;
    return this;
  }

  /** Restart the animation from the beginning. */
  restart() {
    this.startTime = this.p.millis();
    this.frameCounter = 0;
    this.currentStage = 1;
    this.isRunning = true;
  }

  // ── Rendering ───────────────────────────────────────────────────────

  /** Call in draw() — clears background, draws the grid and handles stage fades. */
  render() {
    if (!this.isRunning) return;
    const p = this.p;

    p.background(0);
    this.waveEngine.update(p.frameCount, this.config.waveSpeed);

    const elapsed = p.millis() - this.startTime;
    const changeTime = this.config.changeTime;
    const secondChangeTime = this.config.secondChangeTime;
    const fadeDuration = this.config.fadeDuration;
    const hasThirdStage = secondChangeTime > 0;

    if (fadeDuration <= 0) {
      // Instant transitions
      if (hasThirdStage && elapsed > secondChangeTime) {
        this._drawGrid(this.config.finalTilesX, this.config.finalTilesY, 255);
      } else if (elapsed > changeTime) {
        this._drawGrid(
          this.config.changedTilesX,
          this.config.changedTilesY,
          255,
        );
      } else {
        this._drawGrid(
          this.config.initialTilesX,
          this.config.initialTilesY,
          255,
        );
      }
    } else {
      // Smooth fade transitions
      if (hasThirdStage && elapsed > secondChangeTime + fadeDuration) {
        this._drawGrid(this.config.finalTilesX, this.config.finalTilesY, 255);
      } else if (hasThirdStage && elapsed > secondChangeTime) {
        const t = AlgorithmicTypography._smoothStep(
          (elapsed - secondChangeTime) / fadeDuration,
        );
        this._drawGrid(
          this.config.changedTilesX,
          this.config.changedTilesY,
          255 * (1 - t),
        );
        this._drawGrid(
          this.config.finalTilesX,
          this.config.finalTilesY,
          255 * t,
        );
      } else if (elapsed > changeTime + fadeDuration) {
        this._drawGrid(
          this.config.changedTilesX,
          this.config.changedTilesY,
          255,
        );
      } else if (elapsed > changeTime) {
        const t = AlgorithmicTypography._smoothStep(
          (elapsed - changeTime) / fadeDuration,
        );
        this._drawGrid(
          this.config.initialTilesX,
          this.config.initialTilesY,
          255 * (1 - t),
        );
        this._drawGrid(
          this.config.changedTilesX,
          this.config.changedTilesY,
          255 * t,
        );
      } else {
        this._drawGrid(
          this.config.initialTilesX,
          this.config.initialTilesY,
          255,
        );
      }
    }

    this.frameCounter++;
  }

  /**
   * Render into a sub-region of the canvas (for side-by-side layouts).
   * Does NOT clear the background.
   */
  renderAt(x, y, w, h) {
    if (!this.isRunning) return;
    const p = this.p;
    this.waveEngine.update(p.frameCount, this.config.waveSpeed);

    const elapsed = p.millis() - this.startTime;
    const changeTime = this.config.changeTime;
    const secondChangeTime = this.config.secondChangeTime;
    const fadeDuration = this.config.fadeDuration;
    const hasThirdStage = secondChangeTime > 0;

    const draw = (tx, ty, alpha) => this._drawGridAt(x, y, w, h, tx, ty, alpha);

    if (fadeDuration <= 0) {
      if (hasThirdStage && elapsed > secondChangeTime) {
        draw(this.config.finalTilesX, this.config.finalTilesY, 255);
      } else if (elapsed > changeTime) {
        draw(this.config.changedTilesX, this.config.changedTilesY, 255);
      } else {
        draw(this.config.initialTilesX, this.config.initialTilesY, 255);
      }
    } else {
      if (hasThirdStage && elapsed > secondChangeTime + fadeDuration) {
        draw(this.config.finalTilesX, this.config.finalTilesY, 255);
      } else if (hasThirdStage && elapsed > secondChangeTime) {
        const t = AlgorithmicTypography._smoothStep(
          (elapsed - secondChangeTime) / fadeDuration,
        );
        draw(
          this.config.changedTilesX,
          this.config.changedTilesY,
          255 * (1 - t),
        );
        draw(this.config.finalTilesX, this.config.finalTilesY, 255 * t);
      } else if (elapsed > changeTime + fadeDuration) {
        draw(this.config.changedTilesX, this.config.changedTilesY, 255);
      } else if (elapsed > changeTime) {
        const t = AlgorithmicTypography._smoothStep(
          (elapsed - changeTime) / fadeDuration,
        );
        draw(
          this.config.initialTilesX,
          this.config.initialTilesY,
          255 * (1 - t),
        );
        draw(this.config.changedTilesX, this.config.changedTilesY, 255 * t);
      } else {
        draw(this.config.initialTilesX, this.config.initialTilesY, 255);
      }
    }

    this.frameCounter++;
  }

  // ── Grid drawing (private) ──────────────────────────────────────────

  /** @private */
  _drawGrid(tilesX, tilesY, alpha) {
    this._drawGridAt(0, 0, this.p.width, this.p.height, tilesX, tilesY, alpha);
  }

  /** @private */
  _drawGridAt(ox, oy, w, h, tilesX, tilesY, alpha) {
    const p = this.p;
    const cfg = this.config;
    const we = this.waveEngine;

    const tileW = w / tilesX;
    const tileH = h / tilesY;

    p.colorMode(p.HSB, 360, 255, 255, 255);

    const ts = Math.min(tileW, tileH) * cfg.textScale;
    p.textSize(ts);
    p.textAlign(p.CENTER, p.CENTER);
    p.noStroke();

    const ch = cfg.character;
    const clamped = Math.max(0, Math.min(255, alpha));
    const fc = p.frameCount;

    for (let x = 0; x < tilesX; x++) {
      for (let y = 0; y < tilesY; y++) {
        const hue = we.calculateHue(fc, x, y, tilesX, tilesY);
        const sat = we.calculateSaturation(fc, x, y, tilesX, tilesY);
        const bri = we.calculateColorCustom(fc, x, y, tilesX, tilesY);
        p.fill(hue, sat, bri, clamped);
        p.text(ch, ox + x * tileW + tileW / 2, oy + y * tileH + tileH / 2);
      }
    }

    p.colorMode(p.RGB, 255);
  }

  // ── SmoothStep (static utility) ────────────────────────────────────

  static _smoothStep(x) {
    const t = Math.max(0, Math.min(1, x));
    return t * t * (3 - 2 * t);
  }
}

if (typeof module !== "undefined") module.exports = { AlgorithmicTypography };
if (typeof window !== "undefined")
  window.AlgorithmicTypography = AlgorithmicTypography;
