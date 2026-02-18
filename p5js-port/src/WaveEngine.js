/**
 * WaveEngine — p5.js port of algorithmic.typography.core.WaveEngine
 *
 * Core mathematical engine that drives the wave-based HSB calculations.
 * All maths are pure — no p5.js dependency — so the engine can be tested
 * in Node.js or any browser context.
 */

class WaveEngine {
  /**
   * @param {Configuration} config
   */
  constructor(config) {
    this.config = config;
    this.customWaveFunction = null;

    // Pre-calculated per-frame values
    this.waveMultiplier = 0;
    this.frameRadians = 0;

    // Auto-update tracking
    this.autoUpdate = true;
    this._lastUpdateFrame = -1;
    this._lastWaveSpeed = -1;
  }

  // ── Helpers (static, pure) ──────────────────────────────────────────

  static radians(deg) {
    return (deg * Math.PI) / 180;
  }

  static map(n, start1, stop1, start2, stop2) {
    return start2 + (stop2 - start2) * ((n - start1) / (stop1 - start1));
  }

  static constrain(n, lo, hi) {
    return Math.max(lo, Math.min(hi, n));
  }

  static norm(n, lo, hi) {
    return (n - lo) / (hi - lo);
  }

  // ── Per-frame update ────────────────────────────────────────────────

  /**
   * Call once per frame before any grid calculations.
   * @param {number} frameCount
   * @param {number} waveSpeed
   */
  update(frameCount, waveSpeed) {
    this.frameRadians = WaveEngine.radians(frameCount);
    this.waveMultiplier = WaveEngine.map(
      Math.sin(this.frameRadians),
      -1,
      1,
      this.config.waveMultiplierMin,
      this.config.waveMultiplierMax,
    );
    this._lastUpdateFrame = frameCount;
    this._lastWaveSpeed = waveSpeed;
  }

  /** @private */
  _ensureUpdated(frameCount, waveSpeed) {
    if (
      this.autoUpdate &&
      (frameCount !== this._lastUpdateFrame ||
        waveSpeed !== this._lastWaveSpeed)
    ) {
      this.update(frameCount, waveSpeed);
    }
  }

  setAutoUpdate(enabled) {
    this.autoUpdate = enabled;
  }
  isAutoUpdate() {
    return this.autoUpdate;
  }

  // ── Amplitude ───────────────────────────────────────────────────────

  /**
   * Normalised amplitude for a grid cell (0–1).
   * @param {number} x  grid column
   * @param {number} y  grid row
   * @returns {number}
   */
  calculateAmplitude(x, y) {
    const a = WaveEngine.map(
      Math.tan(WaveEngine.radians(x + y)),
      -1,
      1,
      this.config.waveAmplitudeMin,
      this.config.waveAmplitudeMax,
    );
    return WaveEngine.norm(
      a,
      this.config.waveAmplitudeMin,
      this.config.waveAmplitudeMax,
    );
  }

  // ── Brightness (B in HSB) ───────────────────────────────────────────

  /**
   * Default brightness: tangent-based spatial + temporal wave.
   * @param {number} frameCount
   * @param {number} x  grid column
   * @param {number} y  grid row
   * @param {number} amplitude  pre-calculated amplitude
   * @returns {number}  brightness in [brightnessMin, brightnessMax]
   */
  calculateColor(frameCount, x, y, amplitude) {
    this._ensureUpdated(frameCount, this.config.waveSpeed);
    const angleRad = WaveEngine.radians(this.config.waveAngle);
    const dx = Math.cos(angleRad);
    const dy = Math.sin(angleRad);
    const spatial = (x * dx + y * dy) * this.waveMultiplier;
    const input = frameCount * this.config.waveSpeed + spatial * amplitude;
    const value = WaveEngine.map(
      Math.tan(WaveEngine.radians(input)),
      -1,
      1,
      this.config.brightnessMin,
      this.config.brightnessMax,
    );
    return WaveEngine.constrain(
      value,
      this.config.brightnessMin,
      this.config.brightnessMax,
    );
  }

  /**
   * Brightness via custom wave function (if set), otherwise default.
   */
  calculateColorCustom(frameCount, x, y, tilesX, tilesY) {
    if (this.customWaveFunction) {
      const nx = x / tilesX;
      const ny = y / tilesY;
      const time =
        frameCount / (this.config.animationFPS * this.config.animationDuration);
      return this.customWaveFunction(frameCount, nx, ny, time, this.config);
    }
    return this.calculateColor(frameCount, x, y, this.calculateAmplitude(x, y));
  }

  // ── Saturation (S in HSB) ───────────────────────────────────────────

  calculateSaturation(frameCount, x, y, tilesX, tilesY) {
    const sMin = this.config.saturationMin;
    const sMax = this.config.saturationMax;
    if (sMin === sMax) return sMin;

    this._ensureUpdated(frameCount, this.config.waveSpeed);
    const angleRad = WaveEngine.radians(this.config.waveAngle + 30);
    const dx = Math.cos(angleRad);
    const dy = Math.sin(angleRad);
    const input =
      frameCount * this.config.waveSpeed * 0.7 +
      (x * dx + y * dy) * this.waveMultiplier * 1.3;
    const value = WaveEngine.map(
      Math.sin(WaveEngine.radians(input)),
      -1,
      1,
      sMin,
      sMax,
    );
    return WaveEngine.constrain(value, sMin, sMax);
  }

  // ── Hue (H in HSB) ─────────────────────────────────────────────────

  calculateHue(frameCount, x, y, tilesX, tilesY) {
    const hMin = this.config.hueMin;
    const hMax = this.config.hueMax;
    if (hMin === hMax) return hMin;

    this._ensureUpdated(frameCount, this.config.waveSpeed);
    const angleRad = WaveEngine.radians(this.config.waveAngle);
    const dx = Math.cos(angleRad);
    const dy = Math.sin(angleRad);
    const input =
      frameCount * this.config.waveSpeed * 0.3 +
      (x * dx + y * dy) * this.waveMultiplier * 0.5;
    const value = WaveEngine.map(
      Math.sin(WaveEngine.radians(input)),
      -1,
      1,
      hMin,
      hMax,
    );
    return WaveEngine.constrain(value, hMin, hMax);
  }

  // ── Custom wave function ────────────────────────────────────────────

  setCustomWaveFunction(fn) {
    this.customWaveFunction = fn;
  }
  getCustomWaveFunction() {
    return this.customWaveFunction;
  }
  getWaveMultiplier() {
    return this.waveMultiplier;
  }

  reset() {
    this.customWaveFunction = null;
  }
}

if (typeof module !== "undefined") module.exports = { WaveEngine };
if (typeof window !== "undefined") window.WaveEngine = WaveEngine;
