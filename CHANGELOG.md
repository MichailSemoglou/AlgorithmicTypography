# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.3.0] - 2026-03-27

### Added

- **`AmplitudeField`** (`algorithmic.typography.core`) — lightweight interface (`sampleAt(nx, ny)`, `getIntensity()`) that lets any spatial scalar field modulate `WaveEngine` amplitude per grid cell. Decouples the `core` package from the higher-level `render` package; `WaveEngine` holds an optional `AmplitudeField` reference and blends with it via `lerp`.
- **`GlyphCurvatureField`** (`algorithmic.typography.render`) — implements `AmplitudeField`; samples the curvature of a letterform's outline as a Gaussian kernel field. High-curvature regions (tight corners, terminals) produce stronger amplitude modulation, making the wave literally breathe harder where the letter has the most geometric energy. Factory methods `from(GlyphExtractor, char, float)` and `from(GlyphExtractor, char[], float)`; configurable `intensity` (0–1) and `falloff` (Gaussian σ). Exposed as `AlgorithmicTypography.setCurvatureField(GlyphCurvatureField)` / `getCurvatureField()`.
- **`CounterpointEngine`** (`algorithmic.typography.core`) — wraps two independent `WaveEngine` instances (`mainWave` and `counterWave`) so the outer letterform and inner counter-form regions animate on separate rhythms. Main wave drives hue/saturation/brightness for the filled cell; counter wave drives a second fill rendered over the inner contours via `GlyphExtractor.getInnerContours()`. Analogous to two melodic voices in musical counterpoint. Exposed as `AlgorithmicTypography.setCounterpointEngine(CounterpointEngine)` / `getCounterpointEngine()`.
- **Optical Rhythm Sync** — `AlgorithmicTypography.setRhythmFromFont(char)` derives `waveSpeed` from the actual typeface geometry: `freq = (0.5 + counterRatio) / (1.0 + strokeWeight / 80.0) * 0.04 * rhythmScale`. Open, airy letterforms (O, G) pulse faster; heavy, dense ones (M, B) pulse slower. `Configuration` gains `rhythmScale` (float, default 1.0) with getter/setter and JSON round-trip (`animation.rhythmScale`).
- **Word / sentence mode** — `AlgorithmicTypography.setContent(String)` / `Configuration.setContent(String)` fills grid tiles left-to-right, top-to-bottom with successive characters from the supplied string, wrapping around as needed. Setting `null` or `""` restores single-character mode. `content` is persisted to JSON (`text.content`) and round-tripped through `copy()`.
- **`CurvatureField` example** — interactive Processing sketch demonstrating `GlyphCurvatureField`. Keys: `C/c` cycle showcase characters (`B`, `R`, `O`, `S`, `A`, `&`), `+/-` adjust intensity, `[/]` adjust Gaussian falloff, `R` reset, `SPACE` toggle outline, `S` save frame.
- **`Counterpoint` example** — demonstrates `CounterpointEngine` with two independently configured wave systems. Keys: `C/c` cycle counter-rich characters (O, B, R, P, D, g, e, @), `1/2` main wave speed, `3/4` counter wave speed, `A/a` / `Z/z` wave angles, `S` save.
- **`OpticalRhythm` example** — cycles through a character set (`O`, `G`, `B`, `a`, `g`, `e`, `m`, `H`, `I`, `1`, `S`, `R`) and shows the computed wave speed from `setRhythmFromFont()`. Toggle auto vs manual mode, adjust `rhythmScale`, and see a live speed bar.
- **`WordMode` example** — demonstrates `setContent(String)` with preset messages (`TYPOGRAPHY`, `ALGORITHM`, `THE GRID`, `TYPE IS ALIVE`, etc.), live keyboard text entry, and hue preset cycling.
- **`VectorExporter.fixArtboardDimensions(PApplet, String, int, int)`** (`algorithmic.typography.render`) — new public static utility method that post-processes a saved SVG file to correct artboard dimensions in Affinity Designer, Adobe Illustrator, and Inkscape. Processing writes unitless `width`/`height` attributes (e.g. `width="1080"`); Affinity Designer interprets these as `px` values and applies a 96→72 DPI conversion, shrinking the artboard to 75% of the intended size. The method replaces `width="N"` → `width="Npt"` and `height="N"` → `height="Npt"` (pt units are 72 dpi-native and bypass the DPI conversion), and injects `viewBox="0 0 W H"` if absent so the coordinate space matches the artboard exactly.

### Changed

- `WaveEngine` — new methods: `getConfig()` returns the `Configuration`; `setAmplitudeField(AmplitudeField)` / `getAmplitudeField()` for curvature-field hookup. `calculateColorCustom()` now lerps the wave amplitude with the field sample when an `AmplitudeField` is set.
- `Configuration` — new fields `content` (String) and `rhythmScale` (float); full JSON + `copy()` support.
- `AlgorithmicTypography` — `drawGrid()` / `drawGridAt()` extended with: per-cell character resolution (word mode), per-cell `PShape` cache, `CounterpointEngine`-aware hue/saturation/brightness selection, inner-contour rendering pass, and all outline-overlay variable aliases updated to match per-cell references.
- `library.properties` version bumped to 30 / prettyVersion 0.3.0.
- **`RandomASCII` example** — fully rewritten with full HSB colour using `calculateHue`, `calculateSaturation`, and `calculateColorCustom` per grid cell; `W` key cycles through all five wave types (SINE, SQUARE, TRIANGLE, SAWTOOTH, TANGENT); HUD displays the active wave name. Previously greyscale-only with no wave type control.

### Fixed

- **`GridStripWave` example** — `W` key now cycles the wave type for both the row axis and the column axis simultaneously. Previously it called `setRowWaveType()` only, so COLUMN and BOTH modes ignored the key entirely and were locked to their initial wave type.
- **`SaveSVG` example** — SVG files now open at the correct artboard size in Affinity Designer, Illustrator, and Inkscape. Three changes: `pixelDensity(1)` in `setup()` prevents Processing's Retina renderer from writing a `scale(2,2)` transform into the SVG root; `createGraphics(width, height, SVG, filename)` replaces `beginRecord`/`endRecord` so the SVG root element carries explicit width and height; `VectorExporter.fixArtboardDimensions()` post-processes the file to add `pt` units and a `viewBox`. HUD text is now correctly excluded from the SVG output.

---

## [0.2.6] - 2026-03-21

### Added

- **`GridStripMotion`** (`algorithmic.typography.core`) — new row/column strip displacement engine. Shifts every row and/or column by a wave function (SINE, SQUARE, TRIANGLE, SAWTOOTH, TANGENT, PERLIN) so the whole grid undulates like a banner. Independent axis control: `setAxis(ROW | COLUMN | BOTH)`, `setAmplitude(0–1)`, `setPhaseStep()`, per-axis speed (`setRowSpeed`, `setColumnSpeed`), and per-axis wave type. Fully wired into `Configuration`, `AlgorithmicTypography.drawGrid()` / `drawGridAt()`, JSON load/save round-trip, and the Builder.
- **`PerlinVertexMotion`** (`algorithmic.typography.core`) — utility class for per-vertex Perlin noise deformation. `deform(PVector[], frameCount)` returns a new non-destructive vertex array perturbed by independent X/Y noise fields; `deformContours(PVector[][], frameCount)` batches across all sub-contours. Configurable `amplitude`, `spatialScale`, `timeSpeed`, and `seed`.
- **`MagneticMotion` named intensity presets** — `GENTLE`, `MODERATE`, `STRONG`, `SNAPPING` constants with `setPreset(int)` for one-call configuration of strength, falloff, smoothing, and radius. New `setStrengthNormalized(float t)` overload maps a designer-friendly 0–1 value to the physical 400–4000 strength range.
- **`RippleMotion` named intensity presets** — `GENTLE`, `MODERATE`, `STRONG` constants with `setPreset(int)`. New `setExpandSpeedNormalized(float t)` overload maps 0–1 to the 60–400 px/s physical range.
- **`AudioBridge` semantic intensity mapping** — `SUBTLE`, `EXPRESSIVE`, `FULL` constants plus semantic overloads `mapBassTo(setter, int)`, `mapMidTo(setter, int)`, `mapTrebleTo(setter, int)`, `mapOverallTo(setter, int)` that apply pre-tuned standard ranges per band and intensity level, eliminating manual min/max lookup.
- **`GridStripWave` example** — interactive sketch demonstrating all three `GridStripMotion` axis modes (ROW / COLUMN / BOTH), live amplitude and phase-step adjustment via keyboard, and wave-type cycling. Loads `data/config.json` for base typography parameters.

### Changed

- `Configuration` gains `gridStripMotion` field with `getGridStripMotion()`, `setGridStripMotion(GridStripMotion)`, JSON round-trip in `loadFromJSON()`/`toJSON()`, `copy()`, and `Builder.gridStripMotion()`.
- `AlgorithmicTypography` gains `setGridStripMotion()` / `getGridStripMotion()` programmatic API; strip offset is applied _before_ `CellMotion` inside both `drawGrid()` and `drawGridAt()`; `loadConfiguration()` now calls `buildGridStripMotionFromJSON()`.

---

## [0.2.5] - 2026-03-12

### Added

- **`GlyphExtractor.union(charA, charB, float)`** — merge two letterform outlines into a single closed `PShape` via AWT `Area.add()`; no external dependency required.
- **`GlyphExtractor.getUnionContour(charA, charB, float)`** — same operation returned as a `PVector[]` for point-level post-processing.
- **`GlyphExtractor.intersect(charA, charB, float)`** — `PShape` of the region shared by two overlapping letterforms (`Area.intersect()`).
- **`GlyphExtractor.getIntersectContour(charA, charB, float)`** — `PVector[]` overload.
- **`GlyphExtractor.subtract(charA, charB, float)`** — cut one letterform out of another (`Area.subtract()`); the foundation for knockouts and compound shapes.
- **`GlyphExtractor.getSubtractContour(charA, charB, float)`** — `PVector[]` overload.
- **`GlyphExtractor.getTangent(char, float, float)`** — unit `PVector` tangent to the outline at a normalised arc-length position `t ∈ [0, 1)`; samples 1 024 pts and uses an epsilon step for the finite-difference direction.
- **`GlyphExtractor.getDashedOutline(char, float, float dashLength, float gapLength)`** — segments the arc-length-resampled contour into alternating dash/gap pairs; returned as `float[][]` with each element `{x1, y1, x2, y2}` — a print-design staple (engraving, laser stencil, stamp aesthetics) absent from all other Processing typography libraries.
- **`GlyphExtractor.subdivide(char, float, int targetCount)`** — increase tessellation vertex density on demand by temporarily tightening flatness to `0.2 f`; useful when downstream physics or deformation effects need more vertices than the font naturally provides.
- **`GlyphExtractor.textOnPath(String text, PVector[] path, float fontSize)`** — lay out a string of glyphs along any arbitrary polyline or extracted contour, rotating each character to follow the local tangent via arc-length parameterisation; advances per-char by `GlyphVector.getGlyphMetrics(c).getAdvance()`.
- **`GlyphExtractor.getStressAxis(char, float)`** — dominant stroke-stress angle (degrees, `[0, 180)`) computed via PCA on inner-contour vertices; falls back to full outline for glyphs with no counter. Use directly as `waveAngle` to propagate the wave along the typeface's own optical axis.
- **`GlyphExtractor.getOpticalCentroid(char, float)`** — ink-density-weighted perceptual centre of mass (800 interior samples weighted by distance to the nearest of 512 contour points); use as pivot for rotation, orbit, or magnetic-field anchors.
- **`GlyphExtractor.getCounterRatio(char, float)`** — total counter-form area as a ratio of the glyph's bounding-box area (`O` ≈ high; `I` = 0); map to wave amplitude or breathing rate so open glyphs breathe more than dense ones, automatically.
- **`GlyphExtractor.getStrokeWeight(char, float)`** — estimated dominant stroke width from mean outline-to-medial-axis distance × 2 (80-sample medial axis, 512-pt contour); map to physics mass or brightness.
- **`GlyphExtractor.buildTypeDNAProfile(char[], float)`** — averages all four measurements across a character set to produce a `TypeDNAProfile`, the typographic fingerprint of the font.
- **`TypeDNAProfile`** (`algorithmic.typography.render`) — data class aggregating `stressAxis`, `opticalCentroid`, `counterRatio`, and `strokeWeight`; provides `toJSON()` and `static fromJSON(JSONObject)` for repeatable cross-sketch profiles; `toString()` for debug output.
- **`AlgorithmicTypography.applyTypeDNA(TypeDNAProfile)`** — one-call preset: maps the profile's four values to `waveAngle`, `waveAmplitudeRange`, and `brightnessRange`; returns `this` for chaining.
- **`GlyphBoolean` example** — interactive sketch demonstrating `union()`, `intersect()`, and `subtract()` across six curated character pairs with an animated hue fill and optional dashed overlay (`D` key) via `getUnionContour` / `getSubtractContour`.
- **`TextOnPath` example** — two-mode sketch: mode 1 flows a full text string around the outer contour of a large letterform via `textOnPath()`; mode 2 draws 48 `getTangent()`-oriented chevron ornaments that orbit the outline continuously.
- **28 new JUnit tests** in `test/algorithmic/typography/render/GlyphExtractorTest.java` covering all 15 new `GlyphExtractor` methods`, `TypeDNAProfile`construction, all getters, and a full`toJSON`/`fromJSON` round-trip.
- **`GlyphExtractor.textOnPath(String, PVector[], float, float spacing)`** — spacing overload of `textOnPath`; adds `spacing` pixels to every character advance so letter-spacing can be adjusted without rewriting the sketch. Wired to the `Slider` control in the `TextOnPath` example.
- **`glyphOutline` config.json block** — draw solid or per-contour dashed stroke outlines on every rendered glyph cell directly from `config.json`, without any sketch code. Keys: `style` (`"none"` / `"solid"` / `"dashed"` / `"dashedOnly"`), `r`, `g`, `b` (0–255), `weight` (stroke weight in pixels), `dashLength`, `gapLength` (dashed and dashedOnly modes). Four new constants: `Configuration.OUTLINE_NONE = 0`, `OUTLINE_SOLID = 1`, `OUTLINE_DASHED = 2`, `OUTLINE_DASHED_ONLY = 3`. Seven getters: `getGlyphOutlineStyle()`, `getGlyphOutlineRed()`, `getGlyphOutlineGreen()`, `getGlyphOutlineBlue()`, `getGlyphOutlineWeight()`, `getGlyphOutlineDashLength()`, `getGlyphOutlineGapLength()`. Four setters: `setGlyphOutlineStyle(int)`, `setGlyphOutlineColor(int,int,int)`, `setGlyphOutlineWeight(float)`, `setGlyphOutlineDash(float,float)`. Two Builder methods: `glyphOutlineSolid(r,g,b,weight)` and `glyphOutlineDashed(r,g,b,weight,dashLen,gapLen)`. Rendering is integrated into `AlgorithmicTypography.drawGrid()` / `drawGridAt()` with lazy `GlyphExtractor` instantiation — zero overhead when style is `"none"`.
- **`GlyphOutline` example** — dedicated showcase for the config-driven glyph outline system. Loads `data/config.json` (default `"style": "dashed"`); `O` cycles None → Solid → Dashed at runtime via `config.setGlyphOutlineStyle()`; `←/→` cycles 16 test characters chosen for diverse inner counter-form geometry (A, B, D, G, O, P, Q, R, a, b, e, g, o, p, &, 8); `S` saves PNG.
- **`Configuration.OUTLINE_DASHED_ONLY = 3`** — fourth glyph outline style: glyph cell fill is suppressed entirely so only the dashed stroke is visible against the background. JSON: `"style": "dashedOnly"`. Runtime: `config.setGlyphOutlineStyle(Configuration.OUTLINE_DASHED_ONLY)`. `GlyphOutline` example `O` key now cycles four modes: None → Solid → Dashed → Dashed Only.
- **`GlyphExtractor.centerOf(String, float, float, float)`** — word-support overload of `centerOf`; accepts a multi-character string (e.g. `"TYPE"`) and returns the same centering-offset `PVector`. `AlgorithmicTypography.drawGrid()` / `drawGridAt()` now call this overload for every character value, making single glyphs and full words work identically through the config `"character"` field.
- **`GlyphExtractor.getDashedOutline(String, float, float, float)`** — string overload; all contours of a word are collected via `getContours(String, float)` and walked per-contour through the shared `buildDashedOutline` helper — the same arc-length resample and per-contour dash-walker as the char version, extended to multi-glyph bounding boxes.
- `library.properties` version bumped to 25 / prettyVersion 0.2.5.

### Changed

- **`GlyphBoolean` example** — fully rewritten as a dual-mode interactive showcase. Mode A (Letter × Letter): boolean ops (union / intersect / subtract, keys 1/2/3) on curated character pairs. Mode B (Shape × Letter): a geometric shape (circle, diamond, band) is subtracted from or intersected with a single letterform; mode 4 adds a band-cutter pass unique to this mode. `TAB` switches between the two modes; `SPACE` cycles pairs / shapes; `S` saves PNG.
- **`GlyphWander` example** — `GravityMotion` and `MagneticMotion` removed from the cycle (both already have dedicated `GravityDynamics` and `MagneticDynamics` examples). Motion-mode count reduced from 11 to 9: 0 = None, 1 = Perlin, 2 = Circular CW, 3 = Circular CCW, 4 = Lissajous, 5 = Spring, 6 = Ripple, 7 = FlowField, 8 = Orbital. `motionLabels[]` and all cyclic-index guards updated accordingly; Ripple click-trigger now checks `motionIdx == 6`.
- **`AlgorithmicTypography.drawGrid()` / `drawGridAt()`** — `character` field now dispatches to `extractString()` / `getDashedOutline(String, …)` when the value is longer than one character, with no change required in the sketch. The `ch.length() == 1` guards have been removed; single-char paths now also go through the unified `centerOf(String, …)` overload.

### Fixed

- **`GlyphExtractor.getDashedOutline` per-contour correction** — on glyphs with inner counter-forms (A, B, O, P, R, etc.) the implementation previously merged all contours into a single flat `PVector[]` before the dash walker, causing it to treat the invisible jump from the outer path's last point to the inner counter's first point as a drawable segment — producing a stray connector line across the glyph interior. The method now calls `getContours()` and walks each sub-contour independently with its own arc-length resample and a fresh dash-walker state (`posInCycle`, `inDash`, `segStartX/Y`); outer and inner paths each carry their own rhythmic dash pattern with no cross-connecting artefact.
- **Glyph contours are now geometrically closed.** `extractContours()` and `extractPoints()` (the internal path-iterator helpers) previously dropped the `SEG_CLOSE` segment emitted by AWT's `FlatteningPathIterator`, leaving every returned `PVector[]` open (`path[last] ≠ path[0]`). All public methods that return contour arrays — `getContours()`, `getOuterContour()`, `getInnerContours()`, `getContourPoints()` — now append the sub-path origin point on `SEG_CLOSE` so the loop is fully closed. A guard in `resamplePerContour()` prevents a double-closing duplicate for the already-closed arrays fed into `distributeAlongOutline()` and `getDashedOutline()`. Downstream effects: `textOnPath()` arc-length table now spans the closing segment (no bare seam); `samplePath()`-based orbiters in the `TextOnPath` example wrap continuously without a jump at `t ≈ 1 → 0`; and drawing contours in a raw `for`/`vertex()` loop produces a visually complete outline even without `endShape(CLOSE)`.
- **Counter-form (winding) rendering cascade** — a suite of fixes to `GlyphExtractor` that collectively restore correct hole rendering for glyphs with counter-forms (A, B, D, O, P, Q, R, etc.):
  - `shapeToProcessing()` — rewritten with `signedPolygonArea()` classification; outer contours (negative signed area = CW in screen coordinates) are drawn with `beginShape()`/`vertex()`, and inner holes (positive signed area = CCW) are wrapped in `beginContour()`/`endContour()`. Previously all contours were treated as outer paths, filling counter-forms solid instead of punching holes.
  - `extractChar()` / `extractString()` — font outlines now normalised through `new Area(gv.getOutline())` before flattening, guaranteeing the canonical even-odd winding rule expected by `shapeToProcessing()`.
  - `getContours(char, float)` / `getContours(String, float)` — same `Area` normalisation applied so all downstream methods (`fillWithPoints`, `getOuterContour`, `buildDeformedShape`, etc.) receive correctly-wound contour lists.
  - `buildDeformedShape()` — ported to the hole-aware contour model using `signedPolygonAreaVec()` and `isPointInContourVec()` to classify deformed contours as outer or inner before constructing the returned `PShape`.
  - `getOuterContour()` — result path is now reversed via `reversePath(largestContour(…))` to guarantee clockwise traversal in screen coordinates, consistent with the winding convention expected by `textOnPath()` and `getTangent()`.

## [0.2.4] - 2026-03-05

### Added

- **`cellMotion` JSON block** — all 10 built-in motion styles are now fully configurable from `config.json` without any code. Add a `"cellMotion"` key with a `"style"` string (`"perlin"`, `"circular"`, `"lissajous"`, `"spring"`, `"gravity"`, `"magnetic"`, `"ripple"`, `"flowfield"`, `"orbital"`, or `"none"`) and optional style-specific parameters (`radius`, `speed`, `gravity`, `restitution`, `strength`, `falloff`, `stiffness`, `damping`, `fieldScale`, `evolutionRate`, `octaves`, `persistence`, `phaseRange`, `expandSpeed`, `waveWidth`, `decayRate`, `clockwise`, `attract`). String keys with sensible defaults — unknown style values fall back to no motion. `MagneticMotion` and `RippleMotion` are wired automatically using the sketch's `PApplet` reference and the canvas/grid dimensions already present in the config. Serialised back to JSON by `Configuration.toJSON()` — round-tripping is fully supported.
- `FlowFieldMotion.getFieldScale()` / `getEvolutionRate()` / `getOctaves()` / `getPersistence()` / `getPhaseRange()` — getters added to support JSON serialisation and code access to the new parameters.
- `RippleMotion.getExpandSpeed()` / `getWaveWidth()` / `getDecayRate()` — getters added to support JSON serialisation.

### Changed

- **`FlowFieldMotion` algorithm overhauled** — previously mapped a noise value directly to an angle, causing all cells to point the same direction simultaneously (block artefact). Rewritten to use **curl noise** (displacement derived from the gradient of a scalar potential, producing a divergence-free, solenoidal field — cells orbit eddies rather than converging to or diverging from fixed points), **per-cell deterministic phase seeding** (each `(col, row)` pair is hashed to a unique Z-axis offset so neighbouring cells evolve at different phases, eliminating grid-wide lockstep), and **multi-octave layering** (finer turbulence stacked on top of the large-scale current). Three new parameters: `setOctaves(int)` (default 2, range 1–4), `setPersistence(float)` (amplitude decay per octave, default 0.45), `setPhaseRange(float)` (maximum inter-cell phase spread, default 8.0; set 0 to restore fully synchronised behaviour). Fully backwards-compatible: the existing 3-argument constructor still works, and `GlyphWander` mode 9 automatically uses the improved algorithm.
- **`GlyphPath` mode 10 (`q`) visual** — three-orbit particles replaced with a single **comet trail**: a bright 20 px head followed by 80 history samples that fade in alpha and shrink in diameter toward the tail, with hue cycling as the head travels. The faint outline stroke has been removed so only particles are visible against the black background. Trail is cleared on character switch and on mode entry to avoid cross-mode ghosting.
- `library.properties` version bumped to 24 / prettyVersion 0.2.4.

## [0.2.3] - 2026-03-02

### Added

- `GlyphExtractor.fillWithLines(char, float, float, float)` — hatch lines clipped to the closed letterform interior via AWT `Area` boolean intersection; returned as `float[][]{x1,y1,x2,y2}` endpoint pairs. Angle and spacing are fully configurable. String overload included.
- `GlyphExtractor.offsetOutline(char, float, float)` — expand or contract the letterform boundary by a fixed pixel distance (positive = grow, negative = shrink). Uses `BasicStroke` area expansion/contraction; returns outer contour vertex array.
- `GlyphExtractor.interpolateTo(char, char, float, float)` — morph between two letterform outlines at a normalised `t` value (0 = charA, 1 = charB). Both outlines are arc-length resampled to a common vertex count before linear interpolation.
- `GlyphExtractor.getMedialAxis(char, float, int)` — approximate medial axis (spine) of a letterform by pairing opposite-side perimeter samples and taking midpoints. Useful for calligraphic/brush-stroke rendering.
- `GlyphExtractor.sampleAlongPath(char, float, float)` and string overload — single `PVector` at a normalised arc-length position on the outline; convenient for animating particles that orbit a letterform.
- `GlyphExtractor.getBoundingContour(char[], float, float)` — union outline of multiple characters (with configurable tracking) returned as a single closed outer contour via AWT `Area` boolean union.
- `GlyphExtractor.of(char, float)` — returns a `GlyphBuilder` chainable fluent builder; eliminates boilerplate for common glyph-extraction draw patterns. Supports `fillPoints()`, `outlinePoints()`, `outerContour()`, `hatch()`, `animateWithNoise()`, `colourByPosition()`, `hueRange()`, `pointSize()`, and the terminal `draw(PApplet, float, float)` method.
- `RippleMotion` — click-triggered concentric displacement rings; glyphs are displaced radially as the ring expands and decays. Supports multiple simultaneous ripples, configurable expansion speed, wave width, and per-frame amplitude decay. `trigger(x, y)` spawns a new ring. Compatible with `config.setCellMotion()`.
- `FlowFieldMotion` — spatially coherent Perlin-noise vector field; adjacent glyphs flow together in slowly evolving currents and eddies. Configurable field scale, evolution rate, and seed offset. Self-contained 3-D value noise (no `PApplet` reference required).
- `OrbitalMotion` — glyphs orbit neighbour-derived anchor points with configurable phase spread and radial wobble, producing a typographic constellation effect. Deterministic per-cell phase ensures a stable, aesthetically balanced pattern.
- `GlyphWander` example — updated to include Gravity, Magnetic, Ripple, FlowField, and Orbital motion modes (10 total). Ripple mode: mouse click triggers ring at cursor position; SPACE triggers ring at sketch centre.
- `GlyphPath` example — updated to add three v0.2.3 modes: hatch fill (key 9, rotating angle + breathing spacing), morph (key 0, ping-pong between character pairs), and path particle (key q, three particles orbiting the outline).
- `ProgressBar` UI component (`algorithmic.typography.ui`) — lightweight read-only horizontal progress indicator with dark-theme defaults, animated-colour support via `setFillColor()`, and the same builder-style API as `Slider`. First consumer is `GlyphMorph`.
- `GlyphMorph` example — new dedicated designer showcase for `interpolateTo()`: large central morph with auto-animated ping-pong t, five-thumbnail timeline strip (t = 0 / 0.25 / 0.5 / 0.75 / 1.0), animated `ProgressBar` above the strip, three display styles (Outline / Filled / Dot Cloud), mouse-X scrub mode, six curated character pairs (A/R, B/8, O/Q, C/G, P/D, S/5), and SVG export.

- `GlyphExtractor.morphShape(char, char, float, float)` — renders a morph between two letterforms as a ready-to-draw `PShape` with correct hole handling via `beginContour()`/`endContour()`; the sketch only sets `fill`/`stroke` and calls `shape(s, ox, oy)` — no manual contour iteration required.
- `GlyphExtractor.interpolateContours(char, char, float, float)` — returns the full interpolated contour list as `List<PVector[]>` for point-level morph effects (dot clouds, physics seeds, custom renderers) without materialising a `PShape`.
- `GlyphExtractor.centerOf(char, float, float, float)` → `PVector` — returns the translation offset needed to optically centre a single letterform at an arbitrary canvas point; eliminates the recurring `getBounds + ox/oy` arithmetic from every sketch.
- `GlyphExtractor.morphCenterOf(char, char, float, float, float, float)` → `PVector` — lerped centering offset for a morphing pair at a given `t`; keeps the letterform's apparent centre of mass stable throughout the morph.
- `GlyphExtractor.drawAt(char, float, float, float)` → `void` — draws a letterform centred at a canvas point, respecting the sketch's current `fill` and `stroke` state via `disableStyle()`; removes the `extractChar + getBounds + pushMatrix + translate + shape + popMatrix` boilerplate entirely.
- `waveType` config.json field — the wave shape is now readable from the `animation` block of `config.json`: `"waveType": "SINE"`. Accepted values: `SINE`, `TANGENT`, `SQUARE`, `TRIANGLE`, `SAWTOOTH`. A string overload `at.setWaveType(String)` is also available; invalid strings log a warning and leave the current type unchanged. `initialize()` applies the configured `waveType` on every init/restart so JSON changes are always reflected automatically.
- `waveAngle` in all example config.json files — `waveAngle` is now present in the `animation` block of all eight shipped example `config.json` files; previously four examples omitted the key.
- **Cell border system** — draws optional per-cell border strokes on any combination of sides. New API in `Configuration`:
  - Six bitmask constants: `BORDER_NONE=0`, `BORDER_TOP=1`, `BORDER_BOTTOM=2`, `BORDER_LEFT=4`, `BORDER_RIGHT=8`, `BORDER_ALL=15`
  - Two colour-mode constants: `BORDER_COLOR_STATIC=0` (fixed RGB), `BORDER_COLOR_WAVE=1` (border brightness pulses in sync with the cell's wave value)
  - Getters: `getCellBorderSides()`, `getCellBorderRed/Green/Blue()`, `getCellBorderWeight()`, `getCellBorderColorMode()`
  - Setters: `setCellBorderSides(int)`, `setCellBorderColor(int, int, int)`, `setCellBorderWeight(float)`, `setCellBorderColorMode(int)`
  - JSON block: `"cellBorder": { "sides": 0, "r": 255, "g": 255, "b": 255, "weight": 1.0, "colorMode": 0 }`
  - Builder methods: `.cellBorderSides(int)`, `.cellBorderColor(int, int, int)`, `.cellBorderWeight(float)`, `.cellBorderColorMode(int)`
  - Border drawing logic added to `AlgorithmicTypography.drawGrid()` and `drawGridAt()` (the standard rendering path used by all non-buffered examples) and to `GridRenderer.render()` / `renderTo()` (the buffered rendering path)
  - BasicGrid, LiveControls, WaveAngle, and ProgrammaticConfig examples updated with keyboard/slider controls and config.json `cellBorder` blocks

### Changed

- `GlyphPhysics.setChar()` / `setText()` now redistribute contour vertices by arc length before loading particles (`DEFAULT_DISTRIBUTE_PTS = 600` proportionally allocated across all contours, closing edge included). Eliminates sparse straight-stem and missing-closing-edge gaps in Points mode.
- Fixed `interpolateTo()` resampling to use `Math.max` (was `Math.min`) for the shared vertex count; the previous code dropped vertices to the smaller of the two outlines, causing letterform collapse at the midpoint of morphs with unequal tessellation densities.
- `AlgorithmicTypography.initialize()` now applies `config.getWaveType()` to the wave engine on every call, so `waveType` changes via config.json or the string setter persist across restarts without additional code in the sketch.
- `AlgorithmicTypography.setWaveType(WavePresets.Type)` now writes the enum name back into `Configuration` so `config.getWaveType()` always reflects the current wave shape.
- `GlyphMorph` example rewritten to delegate contour rendering and hole handling to `morphShape()` and `interpolateContours()`; sketch logic is now purely about display style and timing.
- `GlyphDesign`, `GlyphPath`, and `GlyphMorph` examples updated throughout to use `centerOf()`, `morphCenterOf()`, and `drawAt()`; all `getBounds + ox/oy` arithmetic has been removed from sketch-level code.
- `GlyphExtractor` Javadoc updated to v0.2.3 with new feature list in class header.
- `library.properties` version bumped to 23 / prettyVersion 0.2.3.

## [0.2.2] - 2026-02-28

### Added

- `GlyphExtractor.fillWithPoints(char, float, int)` — scatters N random points inside the closed letterform interior using AWT's built-in containment test; counter-forms (holes in B, O, P, R) are correctly excluded
- `GlyphExtractor.distributeAlongOutline(char, float, int)` — resamples the full perimeter by arc length so every neighbouring point is equally spaced; unlike raw tessellation vertices, spacing is uniform regardless of curve curvature
- `GlyphExtractor.getOuterContour(char, float)` — returns only the outermost closed path, identified via the shoelace formula (largest absolute area)
- `GlyphExtractor.getInnerContours(char, float)` — returns all counter-form contours (holes) separately from the outer boundary
- String overloads for `fillWithPoints` and `distributeAlongOutline`
- `GlyphDesign` example demonstrating all four new methods across three interactive modes (interior fill, perimeter dots, outer/inner colouring)

### Changed

- `GlyphExtractor` Javadoc updated to v0.2.2 with new feature list

## [0.2.1] - 2026-02-28

### Added

- `MagneticMotion` — mouse-driven repel/attract field with smooth lerping
  - `setTileGrid()` convenience helper for world-space mapping
  - `togglePolarity()` for real-time attract ↔ repel switching
- `GravityMotion.kick()` — upward impulse to re-energise settled glyphs
- `MagneticDynamics` example with full live UI (Strength, Falloff, Smoothing, Radius sliders)
- `GravityDynamics` Jump Strength slider + SPACE key handler
- 13 new JUnit tests (MagneticMotion × 7, GravityMotion.kick × 3)

### Changed

- `GravityDynamics` default character updated to `A`
- `GlyphWander` expanded to 8 motion modes (added Magnetic as mode 7)
- `TrailEffect` expanded to 7 motion modes (added Magnetic as mode 6)

## [0.1.2] - 2026-02-27

### Added

- **`LissajousMotion`** — new `CellMotion` subclass producing figure-8 and knot-shaped Lissajous orbits. Configurable frequency ratio (`freqX : freqY`), independent x/y amplitudes, x-channel phase offset, and per-cell phase spread. Extends `CellMotion`, works immediately with `config.setCellMotion()` and the full `VibePreset` pipeline.
- **Background colour** — `Configuration` now exposes `setBackgroundColor(int r, int g, int b)` and `setBackgroundColor(int gray)`, and accepts `backgroundR`, `backgroundG`, `backgroundB` keys in the `colors` section of `config.json`. All renderers (`GridRenderer`, `AlgorithmicTypography`, `HeadlessRenderer`, `VectorExporter`) now read the background colour from config instead of hardcoding black.

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

[0.3.0]: https://github.com/MichailSemoglou/AlgorithmicTypography/releases/tag/v0.3.0
[0.2.6]: https://github.com/MichailSemoglou/AlgorithmicTypography/releases/tag/v0.2.6
[0.2.5]: https://github.com/MichailSemoglou/AlgorithmicTypography/releases/tag/v0.2.5
[0.2.4]: https://github.com/MichailSemoglou/AlgorithmicTypography/releases/tag/v0.2.4
[0.2.3]: https://github.com/MichailSemoglou/AlgorithmicTypography/releases/tag/v0.2.3
[0.2.2]: https://github.com/MichailSemoglou/AlgorithmicTypography/releases/tag/v0.2.2
[0.2.1]: https://github.com/MichailSemoglou/AlgorithmicTypography/releases/tag/v0.2.1
[0.1.2]: https://github.com/MichailSemoglou/AlgorithmicTypography/releases/tag/v0.1.2
[1.0.0]: https://github.com/MichailSemoglou/AlgorithmicTypography/releases/tag/v1.0.0
