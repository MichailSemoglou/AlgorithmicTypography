# Contributing to AlgorithmicTypography

Thank you for your interest in contributing to AlgorithmicTypography! This document provides guidelines for contributing to the project.

## How to Contribute

### Reporting Issues

If you find a bug or have a suggestion:

1. Check if the issue already exists in the [issue tracker](https://github.com/MichailSemoglou/AlgorithmicTypography/issues)
2. If not, create a new issue with:
   - Clear title and description
   - Steps to reproduce (for bugs)
   - Expected vs actual behavior
   - Processing version and OS
   - Code example if applicable

### Code Contributions

#### Setting Up Development Environment

1. Fork the repository
2. Clone your fork:
   ```bash
   git clone https://github.com/MichailSemoglou/AlgorithmicTypography.git
   ```
3. Ensure you have:
   - Processing 4.x or later
   - Java 17 or later (library targets `--release 17`)
   - Ant (optional; `build.sh` works without it on macOS/Linux)

#### Building the Library

```bash
# Using the shell script (primary, no extra tooling needed)
./build.sh

# Or using Ant (all targets: compile, build, test, document, dist)
ant build
```

> **Note:** `build.sh` and `build.xml` both contain a hardcoded `VERSION` constant. When bumping the library version, update it in both files as well as in `library.properties` (both `version` and `prettyVersion`) and add a new entry to `CHANGELOG.md`.

#### Making Changes

1. Create a new branch:

   ```bash
   git checkout -b feature/your-feature-name
   ```

2. Make your changes:
   - Follow existing code style
   - Add Javadoc for public methods
   - Update tests if needed
   - Update documentation

3. Test your changes:

   ```bash
   ant test
   ```

4. Commit with clear messages:
   ```bash
   git commit -m "Add feature: description"
   ```

#### Code Style

- Use 2-space indentation
- Follow Java naming conventions
- Keep methods focused and small
- Add Javadoc for all public APIs
- Use meaningful variable names

Example:

```java
/**
 * Renders the grid with specified tile count.
 *
 * @param tilesX number of horizontal tiles
 * @param tilesY number of vertical tiles
 */
private void drawGrid(float tilesX, float tilesY) {
  // Implementation
}
```

### Areas for Contribution

#### Priority Areas

All v0.2.6 targets are shipped. High-impact areas for v0.3 (Typography as Material):

- [x] ~~**`GlyphExtractor.union/intersect/subtract`**~~ ✅ — shipped in v0.2.5
- [x] ~~**`textOnPath`**~~ ✅ — shipped in v0.2.5
- [x] ~~**`getTangent`**~~ ✅ — shipped in v0.2.5
- [x] ~~**`subdivide`**~~ ✅ — shipped in v0.2.5
- [x] ~~**`glyphOutline` config.json block**~~ ✅ — shipped in v0.2.5 (`OUTLINE_NONE/SOLID/DASHED`, solid + dashed per-contour rendering in `drawGrid`/`drawGridAt`)
- [x] ~~**`GlyphOutline` example**~~ ✅ — shipped in v0.2.5 (config-driven showcase; `O` to cycle outline modes)
- [x] ~~**Normalised 0–1 overloads**~~ ✅ — shipped in v0.2.6 (`setStrengthNormalized(float)` on `MagneticMotion`, `setExpandSpeedNormalized(float)` on `RippleMotion`)
- [x] ~~**Named intensity presets**~~ ✅ — shipped in v0.2.6 (`GENTLE/MODERATE/STRONG/SNAPPING` on `MagneticMotion`; `GENTLE/MODERATE/STRONG` on `RippleMotion`; all via `setPreset(int)`)
- [x] ~~**`AudioBridge` semantic mapping**~~ ✅ — shipped in v0.2.6 (`SUBTLE/EXPRESSIVE/FULL` constants; `mapBassTo/mapMidTo/mapTrebleTo/mapOverallTo(setter, intensity)` overloads)
- [x] ~~**`PerlinVertexMotion`**~~ ✅ — shipped in v0.2.6 (`deform(PVector[], int)` non-destructive; `deformContours(PVector[][], int)` batch; amplitude, spatialScale, timeSpeed configurable)
- [x] ~~**`GridStripMotion`**~~ ✅ — shipped in v0.2.6 (`ROW/COLUMN/BOTH` axis; seven wave types; `\"gridStripMotion\"` JSON block; `setGridStripMotion()` API; `GridStripWave` example)
- [ ] **Letterform Curvature Field** — `GlyphCurvatureField.from(extractor, char, fontSize)` projects per-point curvature as a spatial scalar field; `wave.setAmplitudeField(field)` modulates wave amplitude cell-by-cell from the typeface's own geometry
- [ ] **Typographic Counterpoint** — independent wave systems for glyph positive form and counter-forms; `CounterpointEngine(mainWave, counterWave)` API
- [ ] **Optical Rhythm Sync** — derive wave period from stem width, counter aperture, and advance width; `config.setRhythmFromFont(extractor, char)`
- [ ] **Documentation** — tutorials, video guides, type-specimen showcase examples

#### GlyphExtractor — Current Status (v0.2.6)

The `GlyphExtractor` class extracts outline data from any system font at any size. All methods below are currently implemented:

**Implemented:**

- ~~`extractChar(char, float)`~~ ✅ — filled `PShape` from font outlines
- ~~`extractDeformed(char, float, float, float, float)`~~ ✅ — wave-deformed filled `PShape`
- ~~`getContourPoints(char, float)`~~ ✅ — raw tessellation vertices along the outline
- ~~`getContours(char, float)`~~ ✅ — per-contour vertex arrays (outer + all counters)
- ~~`getBounds(char, float)`~~ ✅ — bounding box `[x, y, w, h]`
- ~~`getFlatness()` / `setFlatness(float)`~~ ✅ — curve-to-segment tessellation tolerance
- ~~`fillWithPoints(char, float, int)`~~ ✅ — N scattered points inside the letterform interior; counter-forms excluded
- ~~`distributeAlongOutline(char, float, int)`~~ ✅ — N arc-length-equalised points around the full perimeter
- ~~`getOuterContour(char, float)`~~ ✅ — outermost contour only (largest area via shoelace)
- ~~`getInnerContours(char, float)`~~ ✅ — counter-form contours only (holes in B, O, P, R, etc.)
- ~~`fillWithLines(char, float, float, float)`~~ ✅ — hatch lines clipped to letterform interior (v0.2.3)
- ~~`offsetOutline(char, float, float)`~~ ✅ — expand or contract the letterform boundary (v0.2.3)
- ~~`interpolateTo(char, char, float, float)`~~ ✅ — morph between two letterform outlines at `t` (v0.2.3)
- ~~`getMedialAxis(char, float, int)`~~ ✅ — approximate spine of the letterform (v0.2.3)
- ~~`sampleAlongPath(char, float, float)`~~ ✅ — point at a normalised arc-length position (v0.2.3)
- ~~`getBoundingContour(char[], float, float)`~~ ✅ — union outline of multiple characters as one closed path (v0.2.3)
- ~~`of(char, float)` / `GlyphBuilder`~~ ✅ — fluent chainable builder for common draw patterns (v0.2.3)
- ~~`union(char, char, float)` / `getUnionContour(char, char, float)`~~ ✅ — AWT `Area` boolean union of two letterforms; returns `PShape` or `PVector[]` (v0.2.5)
- ~~`intersect(char, char, float)` / `getIntersectContour(char, char, float)`~~ ✅ — boolean intersection (v0.2.5)
- ~~`subtract(char, char, float)` / `getSubtractContour(char, char, float)`~~ ✅ — boolean subtraction (v0.2.5)
- ~~`getTangent(char, float, float)`~~ ✅ — unit direction vector at a normalised arc-length position; orients objects travelling along an outline (v0.2.5)
- ~~`getDashedOutline(char, float, float, float)`~~ ✅ — arc-length-sampled dashed stroke as `PVector[]` segments; **per-contour bug fixed in v0.2.5** (each sub-contour walks independently — no cross-connecting line between outer and inner paths) (v0.2.5)
- ~~`subdivide(char, float, int)`~~ ✅ — increase tessellation density on demand for physics-heavy sketches (v0.2.5)
- ~~`textOnPath(String, PVector[], float)`~~ ✅ — lay out a string of glyphs along any arbitrary extracted contour (v0.2.5)
- ~~`getStressAxis(char, float)`~~ ✅ — PCA-derived stress angle of the letterform (v0.2.5)
- ~~`getOpticalCentroid(char, float)`~~ ✅ — area-weighted centroid of the filled outline (v0.2.5)
- ~~`getCounterRatio(char, float)`~~ ✅ — ratio of counter (hole) area to total glyph area (v0.2.5)
- ~~`getStrokeWeight(char, float)`~~ ✅ — estimated average stroke width from area / perimeter (v0.2.5)
- ~~`buildTypeDNAProfile(char, float)`~~ ✅ — aggregates all four Type DNA metrics into a `TypeDNAProfile` (serialisable to JSON) (v0.2.5)

**Ideas for new methods:**

- `GlyphCurvatureField` — project per-point outline curvature as a scalar field for wave amplitude modulation
- Calligraphic stroke sequencing — animate letterforms being drawn stroke-by-stroke from the medial axis
- Word / sentence layout mode — `textOnPath` variant that respects word spacing and optical kerning

#### Configuration System — Current Status (v0.2.6)

The `Configuration` class and its JSON schema are the primary interface between designers and the library. All parameters in the `config.json` file map 1-to-1 to fields in `Configuration` with matching getters, setters, Builder methods, `loadFromJSON`, `toJSON`, and `copy` implementations. Follow this pattern when adding any new parameter.

**Implemented blocks:**

- ~~`canvas`~~ ✅ — `width`, `height`
- ~~`animation`~~ ✅ — `duration`, `fps`, `changeTime`, `secondChangeTime`, `fadeDuration`, `character`, `textScale`, `waveSpeed`, `waveAngle`, `waveType` (v0.2.4), `waveMultiplierMin/Max`, `saveFrames`
- ~~`grid`~~ ✅ — `initialTilesX/Y`, `changedTilesX/Y`, `finalTilesX/Y`
- ~~`colors`~~ ✅ — `hueMin/Max`, `saturationMin/Max`, `brightnessMin/Max`, `waveAmplitudeMin/Max`, `backgroundR/G/B`
- ~~`cellBorder`~~ ✅ (v0.2.4) — `sides` (bitmask: `BORDER_TOP=1`, `BORDER_BOTTOM=2`, `BORDER_LEFT=4`, `BORDER_RIGHT=8`, `BORDER_ALL=15`), `r/g/b`, `weight`, `colorMode` (`BORDER_COLOR_STATIC=0`, `BORDER_COLOR_WAVE=1`)
- ~~`glyphOutline`~~ ✅ (v0.2.5) — `style` (string: `"none"` / `"solid"` / `"dashed"`), `r/g/b`, `weight`, `dashLength`, `gapLength`; constants `OUTLINE_NONE=0`, `OUTLINE_SOLID=1`, `OUTLINE_DASHED=2`; seven getters; setters `setGlyphOutlineStyle(int)`, `setGlyphOutlineColor(int,int,int)`, `setGlyphOutlineWeight(float)`, `setGlyphOutlineDash(float,float)`; Builder methods `glyphOutlineSolid(r,g,b,weight)` and `glyphOutlineDashed(r,g,b,weight,dashLen,gapLen)`; rendering wired into `AlgorithmicTypography.drawGrid()` / `drawGridAt()` with lazy `GlyphExtractor` instantiation

**Ideas for new config blocks:**

- `typography` — word/sentence mode (`content`, `wordWrap`, `leading`)
- `optical` — weight-driven animation toggles (`weightDriven`, `kernAware`)

#### Motion System — Current Status (v0.2.3)

The `CellMotion` base class makes it straightforward to add new per-glyph movement strategies.
To add one, extend `CellMotion`, implement `getOffset(col, row, frameCount)`, and it integrates immediately with `config.setCellMotion()` and the full `VibePreset` pipeline.

**Implemented:**

- ~~`CircularMotion`~~ ✅ — clockwise / counter-clockwise orbital motion
- ~~`PerlinMotion`~~ ✅ — organic noise-driven wandering
- ~~`LissajousMotion`~~ ✅ — figure-8 and knot-shaped orbits
- ~~`SpringMotion`~~ ✅ — spring-damped glyphs pulled toward a drifting target
- ~~`GravityMotion`~~ ✅ — glyphs fall and bounce within their cells (`kick()` for re-energising)
- ~~`MagneticMotion`~~ ✅ — mouse-driven repel/attract field with smooth lerping
- ~~`RippleMotion`~~ ✅ — click-triggered concentric displacement rings that expand and decay (v0.2.3)
- ~~`FlowFieldMotion`~~ ✅ — spatially coherent Perlin-noise vector field; adjacent glyphs flow together (v0.2.3)
- ~~`OrbitalMotion`~~ ✅ — glyphs orbit neighbour-derived anchors in constellation patterns (v0.2.3)

**Ideas for new motion types:**

- `SwarmMotion` — emergent flocking behaviour (separation, alignment, cohesion) per glyph agent
- `WaveCollapseMotion` — glyphs lock into quantised angle states and flip between them using WFC-inspired rules
- `PhysicsChainMotion` — glyphs hang from a verlet chain; gravity + mouse drag creates pendulum cascades

#### Example Sketches — Frame-Saving Convention

All examples that save PNG frames must follow this output convention:

```
frames/
└── 20260228_120000/
    ├── frame_0001.png
    ├── frame_0002.png
    └── ...
```

Create the timestamped directory once in `setup()` and use it in every `saveFrame()` call:

```java
String framesDir;

void setup() {
  // ...
  framesDir = "frames/" + nf(year(),4) + nf(month(),2) + nf(day(),2)
            + "_" + nf(hour(),2) + nf(minute(),2) + nf(second(),2);
}

// in draw() or keyPressed():
saveFrame(framesDir + "/frame_####.png");
```

Note: sketches using `at.toggleFrameSaving()` do not need a manual `saveFrame()` call — the library applies this convention internally.

#### Other Areas

- New wave functions (reaction-diffusion, Chladni figures, custom equations)
- Additional export formats (MP4 via FFmpeg, animated GIF)
- Machine learning integration (style transfer, generative preset creation)
- Risograph / screen-print simulation presets

### Submitting Pull Requests

1. Push your branch:

   ```bash
   git push origin feature/your-feature-name
   ```

2. Create a pull request with:
   - Clear title describing the change
   - Description of what and why
   - Reference to any related issues
   - Screenshots/GIFs for UI changes

3. Wait for review. Maintainers will:
   - Review code quality
   - Test functionality
   - Suggest changes if needed
   - Merge when ready

### Documentation Contributions

Documentation improvements are always welcome:

- Fix typos or unclear explanations
- Add examples
- Create tutorials
- Translate documentation
- Add screenshots/GIFs

### Testing

When adding features, include tests:

```java
@Test
@DisplayName("New feature works correctly")
void testNewFeature() {
  // Test implementation
}
```

Run tests with:

```bash
ant test
```

## Questions?

- Join discussions in [GitHub Discussions](https://github.com/MichailSemoglou/AlgorithmicTypography/discussions)
- Contact maintainers: [Michail Semoglou](mailto:m.semoglou@tongji.edu.cn)

## Code of Conduct

- Be respectful and inclusive
- Welcome newcomers
- Focus on constructive feedback
- Respect different viewpoints

Thank you for contributing!
