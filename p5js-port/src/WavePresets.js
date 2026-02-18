/**
 * WavePresets — p5.js port of algorithmic.typography.core.WavePresets
 *
 * Standard mathematical wave functions: sine, tangent, square, triangle,
 * sawtooth.  Each returns a function with signature:
 *
 *   (frameCount, x, y, time, config) => brightness
 *
 * plus .name and .description properties.
 */

const WavePresets = (() => {
  // ─── Shared phase calculation ──────────────────────────────────────
  function computePhase(frameCount, x, y, config) {
    return (
      frameCount * config.waveSpeed * 0.05 +
      x * Math.PI * 2 * 3 +
      y * Math.PI * 2 * 3
    );
  }

  // ─── Helper: p5-style map ──────────────────────────────────────────
  function mapValue(n, start1, stop1, start2, stop2) {
    return start2 + (stop2 - start2) * ((n - start1) / (stop1 - start1));
  }

  function constrain(n, lo, hi) {
    return Math.max(lo, Math.min(hi, n));
  }

  // ─── Preset factories ─────────────────────────────────────────────

  function sine() {
    const fn = (frameCount, x, y, time, config) => {
      const phase = computePhase(frameCount, x, y, config);
      return mapValue(
        Math.sin(phase),
        -1,
        1,
        config.brightnessMin,
        config.brightnessMax,
      );
    };
    fn.waveName = "Sine";
    fn.description = "Smooth sinusoidal oscillation";
    return fn;
  }

  function tangent() {
    const fn = (frameCount, x, y, time, config) => {
      const phase = computePhase(frameCount, x, y, config);
      const raw = mapValue(
        Math.tan(phase),
        -1,
        1,
        config.brightnessMin,
        config.brightnessMax,
      );
      return constrain(raw, config.brightnessMin, config.brightnessMax);
    };
    fn.waveName = "Tangent";
    fn.description = "Sharp, angular tangent oscillation";
    return fn;
  }

  function square() {
    const fn = (frameCount, x, y, time, config) => {
      const phase = computePhase(frameCount, x, y, config);
      return Math.sin(phase) >= 0 ? config.brightnessMax : config.brightnessMin;
    };
    fn.waveName = "Square";
    fn.description = "Binary on/off square wave";
    return fn;
  }

  function triangle() {
    const fn = (frameCount, x, y, time, config) => {
      const phase = computePhase(frameCount, x, y, config);
      let t = (phase / (Math.PI * 2)) % 1.0;
      if (t < 0) t += 1;
      const tri = t < 0.5 ? 4 * t - 1 : 3 - 4 * t; // −1 → +1
      return mapValue(tri, -1, 1, config.brightnessMin, config.brightnessMax);
    };
    fn.waveName = "Triangle";
    fn.description = "Linear ramp up then down";
    return fn;
  }

  function sawtooth() {
    const fn = (frameCount, x, y, time, config) => {
      const phase = computePhase(frameCount, x, y, config);
      let t = (phase / (Math.PI * 2)) % 1.0;
      if (t < 0) t += 1;
      return mapValue(t, 0, 1, config.brightnessMin, config.brightnessMax);
    };
    fn.waveName = "Sawtooth";
    fn.description = "Linear ramp with sharp drop";
    return fn;
  }

  // ─── Type enumeration ─────────────────────────────────────────────

  const Type = Object.freeze({
    SINE: "SINE",
    TANGENT: "TANGENT",
    SQUARE: "SQUARE",
    TRIANGLE: "TRIANGLE",
    SAWTOOTH: "SAWTOOTH",
  });

  function get(type) {
    switch (type) {
      case Type.SINE:
        return sine();
      case Type.TANGENT:
        return tangent();
      case Type.SQUARE:
        return square();
      case Type.TRIANGLE:
        return triangle();
      case Type.SAWTOOTH:
        return sawtooth();
      default:
        return sine();
    }
  }

  // ─── Public API ────────────────────────────────────────────────────
  return Object.freeze({
    Type,
    get,
    sine,
    tangent,
    square,
    triangle,
    sawtooth,
  });
})();

if (typeof module !== "undefined") module.exports = { WavePresets };
if (typeof window !== "undefined") window.WavePresets = WavePresets;
