/**
 * algorithmic-typography-p5 — entry point
 *
 * Bundles Configuration, WaveEngine, WavePresets, and
 * AlgorithmicTypography into a single module export.
 */

// When loaded via <script> tags, each file attaches itself to `window`.
// When loaded via require() / import, we re-export everything.

if (typeof module !== "undefined") {
  const { Configuration } = require("./Configuration");
  const { WaveEngine } = require("./WaveEngine");
  const { WavePresets } = require("./WavePresets");
  const { AlgorithmicTypography } = require("./AlgorithmicTypography");

  module.exports = {
    Configuration,
    WaveEngine,
    WavePresets,
    AlgorithmicTypography,
  };
}
