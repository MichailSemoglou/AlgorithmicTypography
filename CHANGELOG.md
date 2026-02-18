# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-02-18

### Fixed

- **library.properties** — Renamed `author` to `authors` (required field); removed invalid categories `Graphics` and `Library`; set `minRevision` to `0`
- **build.xml** — Fixed unescaped `&` entity that caused XML parse failure; aligned Java source/target to 17 (was 11, conflicting with build.sh); removed references to nonexistent `CHANGELOG.md` and `ROADMAP.md` in dist target
- **ConfigurationTest** — Fixed assertions for `animationDuration` (expected 24, default is 18) and `saveFrames` (expected true, default is false)
- **HeadlessRenderer** — `renderSequence()` now converts Processing-style `####` path patterns to `String.format`-compatible `%04d` specifiers; previously all frames would overwrite the same file
- **GridRenderer** — `render()` no longer calls `beginDraw()`/`endDraw()` on the main PGraphics context (only on offscreen buffers), preventing rendering glitches with P2D/P3D renderers
- **Configuration.Builder** — `gridSize(1)` no longer produces a zero-tile grid from integer division; changed tiles now floor to `Math.max(1, tiles / 2)`
- **TemporalTrail** — Removed unused `age` and `bufIdx` variables in `composite()` and `compositeHSB()`; fixed bounds check that could skip valid frames when temporal offset is negative

### Added

- This changelog

[1.0.0]: https://github.com/MichailSemoglou/AlgorithmicTypography/releases/tag/v1.0.0
