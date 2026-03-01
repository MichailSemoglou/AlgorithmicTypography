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

> **Note:** `build.sh` and `build.xml` both contain a hardcoded `VERSION` constant. When bumping the library version, update it in both files as well as in `library.properties`.

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

High-impact areas right now (v0.3 targets):

- [ ] **Optical weight mapping** — modulate wave amplitude from glyph ink density; heavy glyphs move less, exposing `config.setWeightDriven(true)`
- [ ] **Kerning-aware grid** — read kern pairs from font metrics to adjust horizontal tile spacing; especially useful in word-mode layouts
- [ ] **Word / sentence mode** — `config.setContent("text")` fills tiles left-to-right with proper word-wrap and leading; each glyph retains full motion + wave control
- [ ] **Baseline wave** — modulate the text baseline per-glyph (rise/fall) rather than raw Y displacement; respects ascenders/descenders
- [ ] **Documentation** — tutorials, video guides, type-specimen showcase examples

#### GlyphExtractor — Current Status (v0.2.2)

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

**Ideas for new methods:**

- `sampleAlongPath(char, float, float)` — sample a point at a normalised arc-length position (0–1) for animation along the outline
- `getBoundingContour(char[], float)` — union outline of multiple characters as a single closed path
- `getSkeletonPoints(char, float)` — medial-axis / skeleton approximation for stroke-based rendering

#### Motion System — Current Status (v0.2.2)

The `CellMotion` base class makes it straightforward to add new per-glyph movement strategies.
To add one, extend `CellMotion`, implement `getOffset(col, row, frameCount)`, and it integrates immediately with `config.setCellMotion()` and the full `VibePreset` pipeline.

**Implemented:**

- ~~`CircularMotion`~~ ✅ — clockwise / counter-clockwise orbital motion
- ~~`PerlinMotion`~~ ✅ — organic noise-driven wandering
- ~~`LissajousMotion`~~ ✅ — figure-8 and knot-shaped orbits
- ~~`SpringMotion`~~ ✅ — spring-damped glyphs pulled toward a drifting target
- ~~`GravityMotion`~~ ✅ — glyphs fall and bounce within their cells (`kick()` for re-energising)
- ~~`MagneticMotion`~~ ✅ — mouse-driven repel/attract field with smooth lerping

**Ideas for new motion types:**

- `RippleMotion` — concentric displacement waves emanating from a click point
- `FlowFieldMotion` — Perlin-noise vector field that glyphs follow as particles
- `OrbitalMotion` — glyphs orbit other glyphs (typographic constellation)

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
