# Algorithmic Typography

> A Processing library for graphic designers and typographers, covering wave-driven glyph animation, per-cell physics, live UI controls, and audio reactivity.

[![Processing](https://img.shields.io/badge/Processing-4.x-blue)](https://processing.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Version](https://img.shields.io/badge/Version-0.2.3-orange)](https://github.com/MichailSemoglou/AlgorithmicTypography/releases)

![AlgorithmicTypography showcase](docs/showcase.gif)
![AlgorithmicTypography showcase_2](docs/showcase_2.gif)

> [!NOTE]
> **Version 0.2.3** deepens the `GlyphExtractor` designer toolkit and expands the `CellMotion` catalogue. New `GlyphExtractor` geometry methods: `fillWithLines()` (hatch lines clipped to the letterform interior), `offsetOutline()` (inline/outline expansion or contraction), `interpolateTo()` (morph between two letterform outlines at a normalised `t`), `getMedialAxis()` (approximate spine for calligraphic rendering), `sampleAlongPath()` (single point at a normalised arc-length position for particle animation), and `getBoundingContour()` (union outline of multiple characters as one path). New high-level convenience methods that eliminate sketch boilerplate: `morphShape()` (ready-to-draw `PShape` for morphs with correct hole handling), `interpolateContours()` (interpolated contour list for point-level morph effects), `centerOf()` (centering offset for a single glyph), `morphCenterOf()` (lerped centering offset for a morphing pair), and `drawAt()` (draw a centred letterform in one call). A new chainable `GlyphBuilder` fluent API removes boilerplate from common effects. Three new `CellMotion` types: `RippleMotion` (click-triggered concentric displacement rings), `FlowFieldMotion` (spatially coherent Perlin-noise vector field), and `OrbitalMotion` (glyphs orbit neighbour anchors in constellation patterns). This project follows [Semantic Versioning](https://semver.org/); the `0.x` series reflects active development. AlgorithmicTypography is an ongoing project built for the Processing community. If you encounter a bug or have ideas, please [open an issue](https://github.com/MichailSemoglou/AlgorithmicTypography/issues) or join the [GitHub Discussions](https://github.com/MichailSemoglou/AlgorithmicTypography/discussions).

## Overview

AlgorithmicTypography is built mainly for **graphic designers and typographers**. It generates animated grids of glyphs whose colours, positions, and motion are driven by mathematical wave functions and a growing suite of per-cell physics engines, with live UI controls, audio reactivity, glyph physics, and vector export.

It is expressive enough for complex typographic systems and approachable enough for a designer's first generative sketch.

The library is also well suited to **design education**. Its layered API — from one-line vibe presets and JSON configuration to custom wave functions and per-cell physics engines — makes it a practical tool for generative typography courses. Students can start with immediate visual results and progressively engage with the underlying parameters, using the library to explore the relationship between code, form, and typographic expression.

### Why AlgorithmicTypography?

What makes this library distinctive is that it was designed from the ground up with **designers in mind**. You do not need to understand signal processing to create a wave-driven animation, manage threads to export frames, or parse font internals to extract glyph outlines — the library handles all of that. The result is a tool that feels at home in a design workflow while remaining fully open to code-level customisation.

**For newcomers,** it is one of the most accessible entry points into generative typography in the Processing ecosystem. A working sketch requires just four lines of code; a JSON file handles the rest. From there, the API grows with you — every new concept (physics, audio, custom waves) builds on the same foundation rather than requiring a fresh start.

**As an alternative to Geomerative,** AlgorithmicTypography already covers the core use cases — outline extraction, contour separation, interior point distribution, arc-length-spaced perimeter sampling, hatch fill, outline offsetting, and letterform morphing — without requiring an external dependency. Critically, it also resolves several long-standing issues with Geomerative's approach: Geomerative's contour extraction suffered from inconsistent winding order across different fonts, missing sub-paths on complex glyphs, and no reliable method for separating outer boundaries from inner counter-forms (counters — the enclosed negative spaces in 'B', 'O', 'P', 'R'). This required fragile sketch-level workarounds that broke as soon as the font changed. AlgorithmicTypography solves this at the source — contour extraction goes directly through Java2D's own `FlatteningPathIterator` on the raw AWT font outline (no third-party parser), outer vs. inner separation is determined by contour area (deterministic and font-agnostic), and arc-length resampling is calculated geometrically rather than parametrically so points are spaced evenly whether a stroke is a tight curve or a long straight stem. The `centerOf`, `drawAt`, and `morphShape` methods go a step further: sketch code expresses design intent directly, with no glyph-bounds arithmetic or matrix boilerplate surfacing at the sketch level. Beyond glyph geometry, per-vertex physics, cell-level motion engines, audio reactivity, and live UI controls are all built in. Version 1.0.0 is expected to consolidate these systems into a significantly more powerful and stable release.

## Features

- **Wave-driven animation** — Sine, tangent, square, triangle, sawtooth, Perlin noise, and custom wave functions
- **HSB colour mapping** — Configurable hue, saturation, and brightness ranges across the grid
- **Wave angle** — Control the propagation direction of the colour wave (0–360°)
- **Glyph extraction** — Extract glyph outlines as vertices (built-in alternative to Geomerative)
- **Glyph physics** — Treat glyph vertices as particles with mouse attraction/repulsion
- **Cell motion** — Nine built-in movement strategies per glyph: Circular (CW/CCW), Perlin noise, Lissajous figure-8/knot, Spring-damped, Gravity + bounce (with `kick()`), Mouse-magnetic (repel/attract), Ripple (click-triggered concentric rings), FlowField (spatially coherent Perlin vector field), and Orbital (constellation orbit patterns)
- **Trail effects** — Semi-transparent overlay trails with temporal displacement
- **Audio reactivity** — Map bass, mid, treble, and beat detection to animation parameters
- **Cultural design presets** — Parameter sets inspired by Swiss, Bauhaus, Chinese Ink, Arabic Kufi, Japanese Minimal, and related typographic traditions (see [editorial note](#editorial-note))
- **Vibe presets** — Natural-language mood mapping (e.g. "calm", "techno", "ocean") (see [editorial note](#editorial-note))
- **SVG/PDF export** — HSB-correct colour rendering; unlike raw Processing sketches, the library explicitly switches colour mode before every draw call, so hue-driven palettes look identical on screen and in exported PDF/SVG files
- **JSON configuration** — Customise behaviour without touching code
- **Live controls** — GUI sliders, keyboard shortcuts, and OSC input
- **Programmatic API** — Build configurations dynamically in code

## Installation

### Manual Installation

1. Go to the [Releases page](https://github.com/MichailSemoglou/AlgorithmicTypography/releases) and download **`AlgorithmicTypography.zip`** — this is the pre-built library package. Do **not** download "Source code (zip)" or "Source code (tar.gz)", as those do not include the compiled library.
2. Unzip the file. A folder named `AlgorithmicTypography` will be created.
3. Move that folder into your Processing libraries directory:
   - **macOS:** `~/Documents/Processing/libraries/`
   - **Windows:** `Documents\Processing\libraries\`
   - **Linux:** `~/sketchbook/libraries/`
4. Restart Processing IDE.
5. Verify the installation via **Sketch → Import Library** — you should see _AlgorithmicTypography_ listed.

### Building from Source

```bash
./build.sh
```

Requires Processing 4.x installed at the default location.

### Optional Dependencies

| Feature         | Library | Install via                                      |
| --------------- | ------- | ------------------------------------------------ |
| OSC             | oscP5   | Sketch → Import Library → Add Library... → oscP5 |
| Text Generation | RiTa    | Sketch → Import Library → Add Library... → RiTa  |
| Audio           | Sound   | Bundled with Processing 4                        |

GUI controls are built in (no external UI library needed). The optional libraries above add enhanced functionality when present.

## Quick Start

```java
import algorithmic.typography.*;

AlgorithmicTypography at;

void setup() {
  size(1080, 1080);
  at = new AlgorithmicTypography(this);
  at.loadConfiguration("config.json");
  at.initialize();
}

void draw() {
  at.render();
}
```

## Architecture

```
algorithmic.typography
├── AlgorithmicTypography          Main API entry point
├── Configuration                  JSON/programmatic configuration
├── ObservableConfiguration        Live-update configuration with listeners
├── HeadlessRenderer               Batch/offscreen rendering
│
├── core/
│   ├── WaveEngine                 Wave calculation engine
│   ├── WaveFunction               Plugin interface for custom waves
│   ├── WavePresets                Built-in wave types (Sine, Tangent, Square, Triangle, Sawtooth, Perlin)
│   ├── TemporalTrail              Delay/trail compositing buffer
│   ├── CellMotion                 Abstract base for per-cell glyph movement
│   ├── CircularMotion             Clockwise / counter-clockwise orbital motion
│   ├── PerlinMotion               Perlin-noise organic wandering
│   ├── LissajousMotion            Figure-8 and knot-shaped Lissajous orbits
│   ├── SpringMotion               Spring-damped glyphs chasing a drifting target
│   ├── GravityMotion              Gravity, bounce, and kick() impulse physics
│   ├── MagneticMotion             Mouse-driven repel / attract field
│   ├── RippleMotion               Click-triggered concentric displacement rings
│   ├── FlowFieldMotion            Spatially coherent Perlin-noise vector field
│   └── OrbitalMotion              Glyphs orbit neighbour-derived anchors (constellation)
│
├── render/
│   ├── GridRenderer               Offscreen PGraphics rendering
│   ├── ExportController           Async PNG frame export
│   ├── VectorExporter             SVG/PDF vector export
│   ├── GlyphExtractor             Glyph outline extraction (vertices, contours, deformation)
│   └── GlyphPhysics               Particle-based physics for glyph vertices
│
├── audio/
│   └── AudioBridge                Real-time FFT, beat detection, parameter mapping
│
├── style/
│   ├── ColorPalette               Named colour palettes
│   └── Serendipity                Random design exploration
│
├── system/
│   ├── DesignSystem               Cultural typography presets
│   └── VibePreset                 Natural-language mood mapping
│
├── text/
│   └── RitaBridge                 RiTa computational literature integration
│
├── ui/
│   ├── ControlPanel               GUI sliders and keyboard controls
│   ├── Slider                     Individual slider widget
│   └── OSCBridge                  OSC network control
│
├── ml/
│   └── AIWaveFunction             ML-driven wave function
│
└── net/
    └── WebSocketServer            WebSocket control interface
```

## Configuration

Create a `config.json` file in your sketch's `data/` folder:

```json
{
  "canvas": {
    "width": 1080,
    "height": 1080
  },
  "animation": {
    "duration": 18,
    "fps": 30,
    "character": "A",
    "textScale": 0.8,
    "saveFrames": false,
    "waveSpeed": 1.0,
    "waveAngle": 45,
    "waveMultiplierMin": 0.0,
    "waveMultiplierMax": 2.0,
    "changeTime": 6000,
    "secondChangeTime": 12000,
    "fadeDuration": 2000
  },
  "grid": {
    "initialTilesX": 16,
    "initialTilesY": 16,
    "changedTilesX": 8,
    "changedTilesY": 8,
    "finalTilesX": 4,
    "finalTilesY": 4
  },
  "colors": {
    "brightnessMin": 50,
    "brightnessMax": 255,
    "saturationMin": 0,
    "saturationMax": 0,
    "hueMin": 0,
    "hueMax": 0,
    "waveAmplitudeMin": -200,
    "waveAmplitudeMax": 200,
    "backgroundR": 0,
    "backgroundG": 0,
    "backgroundB": 0
  }
}
```

> **Note:** The example above shows all available parameters with their default values. You do not need to include every key — any parameter you omit will fall back to its default. A minimal config can be as short as a single `character` entry.

### Key Parameters

**canvas**

| Parameter | Type | Description             | Default |
| --------- | ---- | ----------------------- | ------- |
| `width`   | int  | Canvas width in pixels  | 1080    |
| `height`  | int  | Canvas height in pixels | 1080    |

**animation**

| Parameter           | Type    | Description                                                             | Default |
| ------------------- | ------- | ----------------------------------------------------------------------- | ------- |
| `character`         | String  | Character to render in the grid                                         | `"A"`   |
| `textScale`         | float   | Text size relative to tile (0–1)                                        | 0.8     |
| `duration`          | int     | Animation duration in seconds                                           | 18      |
| `fps`               | int     | Frames per second                                                       | 30      |
| `saveFrames`        | boolean | Auto-save frames as PNG                                                 | false   |
| `waveSpeed`         | float   | Wave animation speed                                                    | 1.0     |
| `waveAngle`         | float   | Wave propagation direction in degrees (0–360)                           | 45      |
| `waveMultiplierMin` | float   | Minimum wave multiplier applied during rendering                        | 0.0     |
| `waveMultiplierMax` | float   | Maximum wave multiplier applied during rendering                        | 2.0     |
| `changeTime`        | int     | Time in ms when grid transitions from stage 1 to stage 2                | 6000    |
| `secondChangeTime`  | int     | Time in ms when grid transitions from stage 2 to stage 3 (0 = disabled) | 12000   |
| `fadeDuration`      | int     | Crossfade duration in ms between grid stages                            | 2000    |

**grid**

| Parameter       | Type | Description             | Default |
| --------------- | ---- | ----------------------- | ------- |
| `initialTilesX` | int  | Grid columns in stage 1 | 16      |
| `initialTilesY` | int  | Grid rows in stage 1    | 16      |
| `changedTilesX` | int  | Grid columns in stage 2 | 8       |
| `changedTilesY` | int  | Grid rows in stage 2    | 8       |
| `finalTilesX`   | int  | Grid columns in stage 3 | 4       |
| `finalTilesY`   | int  | Grid rows in stage 3    | 4       |

**colors**

| Parameter          | Type  | Description                                                                 | Default |
| ------------------ | ----- | --------------------------------------------------------------------------- | ------- |
| `brightnessMin`    | float | Minimum brightness (0–255)                                                  | 50      |
| `brightnessMax`    | float | Maximum brightness (0–255)                                                  | 255     |
| `saturationMin`    | float | Minimum saturation (0–255); 0 = greyscale                                   | 0       |
| `saturationMax`    | float | Maximum saturation (0–255)                                                  | 0       |
| `hueMin`           | float | Minimum hue (0–360); set both hue values to 0 for greyscale                 | 0       |
| `hueMax`           | float | Maximum hue (0–360)                                                         | 0       |
| `waveAmplitudeMin` | float | Minimum wave amplitude used for colour mapping                              | -200    |
| `waveAmplitudeMax` | float | Maximum wave amplitude used for colour mapping                              | 200     |
| `backgroundR/G/B`  | int   | Background colour channels (0–255 each); 0,0,0 = black; 255,255,255 = white | 0       |

When `hueMin` and `hueMax` are different, the renderer switches to HSB colour mode automatically.

## Examples

### BasicGrid

Simplest usage — loads a JSON configuration and renders the animation.

### ProgrammaticConfig

Configures the system entirely in code. Each restart generates a random configuration — including character, grid size, wave speed, and background colour chosen from a curated dark palette.

### CustomWave

Demonstrates all wave types: five built-in presets, Perlin noise, and a custom Julia-set fractal wave.

### CustomFont

Cycles through system fonts (Helvetica, Georgia, Courier, Futura, Didot) to compare typographic texture.

### WaveAngle

Controls the wave propagation angle (0–360°) with UI sliders and keyboard presets.

### CulturalStyles _(temporarily unavailable)_

Applies parameter sets inspired by typographic traditions across different cultural and historical design movements. The underlying `DesignSystem` API is fully functional; a new example sketch will be reintroduced in a later release.

> **Editorial note:** The cultural and mood presets in this library — including `DesignSystem` and `VibePreset` — are the author's artistic interpretation, informed by 30+ years of professional practice in the design industry. They are expressive starting points for creative exploration, not ethnographic, anthropological, or perceptual research models.

### VibeCoding

Showcases all 26 vibe keywords supported by `VibePreset.apply()`, organised across seven groups (Calm, Energetic, Melancholy, Chaotic, Ocean, Minimal, Light/Dark). Arrow keys cycle through every preset; `SPACE` blends two random keywords into a compound vibe; `R` restarts.

> **Editorial note:** The mood-to-parameter mappings in `VibePreset` are the author's artistic interpretation based on 30+ years in the design industry. They are not grounded in psychoacoustics, affective computing research, or colour psychology literature. The labels (e.g. "calm", "techno", "ocean") serve as expressive shorthand for creative workflows, not as scientifically validated mood or emotion models.

### LiveControls

Real-time parameter adjustment with GUI sliders, keyboard shortcuts, and optional OSC input.

### MultipleSystems

Runs two typography instances side by side with independent configurations.

### PerformanceMode _(temporarily unavailable)_

Optimised rendering path for high-resolution grids. The example sketch will be reintroduced in a later release.

### AudioReactive

Audio-reactive typography. Bass drives wave speed, treble controls brightness, and beat detection triggers visual events.

### TextDrivenAnimation

Demonstrates RiTa integration. Words are generated from Markov chains and grammars, with linguistic features mapped to animation parameters.

### RandomASCII

Fills the grid with random printable ASCII characters with periodic reshuffling.

### SaveSVG

Exports the current frame as an SVG vector graphic with full HSB colour preservation.

### BackgroundImage

Renders typography over a photograph. Place `background.png` in the sketch's `data/` folder; the grid is composited over it each frame.

### MultiPhoto

Assigns a different photograph to each grid cell. Place `photo-01.png` … `photo-16.png` in `data/`; the sketch tiles them behind the glyph grid with centre-crop and 3-stage grid tracking.

### GlyphDesign

Demonstrates four new designer-oriented `GlyphExtractor` methods added in v0.2.2. Three interactive modes:

1. **Interior fill** (`fillWithPoints`) — scatter points randomly inside the closed letterform; counter-forms are excluded automatically
2. **Perimeter dots** (`distributeAlongOutline`) — N points evenly spaced by arc length around the full outline, not by raw tessellation density
3. **Outer + inner** (`getOuterContour` / `getInnerContours`) — colour the outer boundary and counter-forms independently; shows the counter count for each character

Useful starting point for stippling effects, flow-field seeding, necklace-of-dots typography, and counter-aware colouring.

### GlyphMorph

A dedicated showcase for `interpolateTo()`, the v0.2.3 morphing API. A large central letterform breathes between two characters via a smooth ping-pong animation. A timeline strip at the bottom shows five fixed-t snapshots (t = 0 / 0.25 / 0.5 / 0.75 / 1.0) so you can see the full morphing spread at a glance. Four display styles — Outline, Filled, Dot Cloud, and Dual (ghosted source + target + morph) — let you explore different creative directions. Mouse-X scrub mode hands control of t directly to the cursor. Eight curated character pairs chosen for interesting midpoints; SPACE cycles through them.

### GlyphPath

Extracts glyph outlines as vertices with eleven display modes: filled, points (arc-length distributed), deformed, contours, a 4×4 tiled grid, interior fill points, outer contour only, outer + inner counter-forms, hatch fill (`fillWithLines`), character morph (`interpolateTo`), and path particle (`sampleAlongPath`).

### GlyphDynamics

Particle-based physics — each glyph vertex becomes a particle with mouse repulsion and spring-back forces.

### GlyphWander

Demonstrates per-glyph cell motion via `config.setCellMotion()`. Cycles through eleven motion modes — None, Perlin, Circular CW, Circular CCW, Lissajous, Spring, Gravity, Magnetic, Ripple, FlowField, and Orbital — with live radius and speed adjustment. In Ripple mode, clicking triggers a concentric ring at the cursor position.

### TrailEffect

Glyphs move within their grid cells and leave fading trails via semi-transparent overlay. Supports seven motion modes: Circular CW/CCW, Perlin noise, Lissajous, Spring, Gravity, and Magnetic. Trail length and wave angle are also adjustable live.

### GravityDynamics

A full showcase for `GravityMotion`. Two side-by-side control panels — library `ControlPanel` on the left, a custom gravity panel on the right with live sliders for Gravity, Restitution, Lateral force, Air Drag, Phase Spread, Radius, and Jump Strength. Four presets (Default / Heavy / Floaty / Pinball); `SPACE` to kick all glyphs back into the air.

### MagneticDynamics

A full showcase for `MagneticMotion`. Live sliders for Strength, Falloff, Smoothing, and Radius. Three presets (Repel / Attract / Rubber Band); `SPACE` to switch between repel and attract modes.

## Documentation

A four-page printable cheat sheet is available in:

- **Online:** [qide.studio/libraries/processing/algorithmictypography/cheat-sheet.html](https://qide.studio/libraries/processing/algorithmictypography/cheat-sheet.html)
- **HTML:** [`docs/CHEAT_SHEET.html`](docs/CHEAT_SHEET.html)
- **PDF:** [`docs/CHEAT_SHEET.pdf`](docs/CHEAT_SHEET.pdf)

Full Javadoc is in the [`reference/`](reference/index.html) directory.

## API Highlights

### Custom Wave Function

```java
public class MyWave implements WaveFunction {
  public float calculate(int frame, float x, float y,
                         float time, Configuration config) {
    return value;  // saturationMin to saturationMax
  }
  public String getName() { return "My Wave"; }
  public String getDescription() { return "Custom wave"; }
}

WaveEngine engine = new WaveEngine(config);
engine.setCustomWaveFunction(new MyWave());
```

### Glyph Extraction

```java
GlyphExtractor glyph = new GlyphExtractor(this, "Helvetica", 72);
PShape shape = glyph.extractChar('A', 400);           // filled PShape
PVector[] pts = glyph.getContourPoints('A', 400);     // outline vertices
List<PVector[]> contours = glyph.getContours('A', 400);
PShape deformed = glyph.extractDeformed('A', 400, amp, freq, phase);

// v0.2.2 — designer utilities
PVector[] fill   = glyph.fillWithPoints('O', 400, 800);        // interior scatter
PVector[] ring   = glyph.distributeAlongOutline('O', 400, 200); // arc-length spaced
PVector[] outer  = glyph.getOuterContour('B', 400);            // outer boundary only
List<PVector[]> holes = glyph.getInnerContours('B', 400);      // counter-forms only

// v0.2.3 — geometry depth
float[][] segs  = glyph.fillWithLines('A', 400, 45, 8);       // hatch at 45°, 8 px apart
PVector[] shrunk = glyph.offsetOutline('A', 400, -6);         // contract by 6 px
PVector[] morph  = glyph.interpolateTo('A', 'B', 400, 0.5);   // halfway between A and B
PVector[] spine  = glyph.getMedialAxis('A', 400, 120);        // approximate letterform spine
PVector  pt      = glyph.sampleAlongPath('O', 400, 0.25);     // point at 25% around the outline

// v0.2.3 — centering & high-level drawing (eliminate getBounds boilerplate)
PVector o  = glyph.centerOf('A', 400, width/2, height/2);         // centering offset for a single glyph
PVector om = glyph.morphCenterOf('A', 'B', 400, 0.5, cx, cy);     // lerped offset for a morphing pair
glyph.drawAt('A', 400, width/2, height/2);                         // draw centred, current fill/stroke

// v0.2.3 — morph rendering (holes handled inside the library)
PShape ms = glyph.morphShape('A', 'B', 400, 0.5);                 // ready-to-draw PShape for morph
ms.disableStyle(); fill(200); shape(ms, o.x, o.y);
List<PVector[]> mc = glyph.interpolateContours('A', 'B', 400, 0.5); // raw contours for point effects
```

### Glyph Physics

```java
GlyphPhysics physics = new GlyphPhysics(this, glyphExtractor);
physics.setChar('A', 600);
physics.setMouseAttraction(-3.0);  // negative = repel
physics.setSpring(0.04);
physics.setDamping(0.88);

// in draw():
physics.update();
physics.displayPoints();  // or displayLines(), displayFilled()
```

### Cell Motion

```java
// Clockwise orbit
CircularMotion cw = new CircularMotion(8, 1.0, true);

// Counter-clockwise
CircularMotion ccw = new CircularMotion(8, 1.0, false);

// Perlin noise wandering
PerlinMotion perlin = new PerlinMotion(10, 1.0);

// Lissajous figure-8 (1:2 ratio — default)
LissajousMotion fig8 = new LissajousMotion(10, 1.0);

// Lissajous three-lobed knot (3:2 ratio)
LissajousMotion knot = new LissajousMotion(10, 10, 1.0, 3, 2);

// Spring-damped: glyphs chase a sinusoidally drifting target
SpringMotion spring = new SpringMotion(12, 1.0);
spring.setStiffness(0.35f);   // spring pull strength
spring.setDamping(0.12f);     // energy loss per frame

// Gravity + bounce: glyphs fall and bounce inside their cells
GravityMotion gravity = new GravityMotion(12, 0.18f);
gravity.setRestitution(0.72f); // energy retained per bounce
gravity.kick(10.0f);           // upward impulse — re-energise settled glyphs

// Magnetic: glyphs repel or attract toward the mouse cursor
MagneticMotion magnetic = new MagneticMotion(this); // pass sketch reference
magnetic.setTileGrid(width, height, tilesX, tilesY); // call once in setup()
magnetic.setStrength(1800);   // field intensity
magnetic.setFalloff(80);      // half-force distance in pixels
magnetic.setAttract(false);   // false = repel, true = attract
magnetic.togglePolarity();    // flip attract ↔ repel at runtime

// Ripple: click-triggered concentric displacement rings
RippleMotion ripple = new RippleMotion(14, 1.0);
ripple.setTileGrid(width, height, tilesX, tilesY); // call once in setup()
ripple.trigger(mouseX, mouseY);                    // call from mousePressed()
ripple.setExpandSpeed(3.5);    // px per frame the ring expands
ripple.setWaveWidth(60);       // radial width of each ring
ripple.setDecayRate(0.015);    // amplitude loss per frame

// FlowField: spatially coherent Perlin-noise vector field
FlowFieldMotion flow = new FlowFieldMotion(12, 1.0);
flow.setFieldScale(0.003);     // spatial frequency of the noise field
flow.setEvolutionRate(0.008);  // how fast the field evolves over time
flow.setSeedOffset(42.0);      // seed for field isolation

// Orbital: glyphs orbit neighbour-derived anchors
OrbitalMotion orbital = new OrbitalMotion(10, 1.0);
orbital.setWobble(0.3);        // radial wobble magnitude (0 = perfect circle)

// Apply any motion via config (works with VibePreset pipeline too)
config.setCellMotion(spring);

// Or compute offset manually in draw():
PVector offset = gravity.getOffset(col, row, frameCount);
text(ch, cx + offset.x, cy + offset.y);
```

### Audio Bridge

```java
AudioBridge audio = new AudioBridge(this, config);
audio.useMicrophone();
audio.mapBassTo(config::setWaveSpeed, 2, 10);
audio.mapTrebleTo(config::setBrightnessMax, 180, 255);

// in draw():
audio.update();
```

### Vibe Presets

```java
VibePreset vibe = new VibePreset(config);
vibe.apply("calm meditation");    // gentle, muted
vibe.apply("techno rave energy"); // neon HSB, fast
vibe.apply("ocean waves");        // 90° angle, flowing
```

### Vector Export

```java
import processing.svg.*;

beginRecord(SVG, "frame.svg");
// draw your grid...
endRecord();
```

## Output

Frames are saved to timestamped subdirectories:

```
frames/
└── 20260217_120000/
    ├── frame_0001.png
    ├── frame_0002.png
    └── ...
```

Compile into video with FFmpeg:

```bash
ffmpeg -framerate 30 -i frames/*/frame_%04d.png -c:v libx264 output.mp4
```

## Troubleshooting

| Problem                        | Solution                                                        |
| ------------------------------ | --------------------------------------------------------------- |
| "Could not load configuration" | Ensure `config.json` is in the sketch's `data/` folder          |
| UI not responding              | Ensure the sketch window has focus; check keyboard shortcut map |
| Audio not working              | Install the Sound library (bundled with Processing 4)           |
| Performance issues             | Reduce grid size, disable frame saving, or use P2D renderer     |

## Editorial Note

The **`DesignSystem`** (cultural presets) and **`VibePreset`** (natural-language mood mapping) components of this library are the author's artistic interpretation, developed from 30+ years of professional practice in the design industry. The parameter mappings — grid proportions, wave speed, colour ranges, animation angle — reflect decades of design intuition and aesthetic sensibility, not empirical research.

Specifically:

- **Cultural presets** (Swiss, Bauhaus, Chinese Ink, Arabic Kufi, Japanese Minimal, etc.) are expressive starting points inspired by broad typographic traditions. They do not claim ethnographic, anthropological, or historical accuracy, and should not be cited as culturally definitive representations.
- **Vibe presets** (calm, techno, melancholic, chaotic, ocean, etc.) are qualitative mood anchors for creative workflows. They are not derived from psychoacoustic research, affective computing models, or colour psychology studies.

Users who require culturally or scientifically rigorous representations are encouraged to define their own parameter sets using the `Configuration` API and `CellMotion` extension points.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for setup instructions, code style, and how to add new features.

## Author

[Michail Semoglou](mailto:m.semoglou@tongji.edu.cn)

## License

Released under the MIT License. See [LICENSE](LICENSE) for details.

## Acknowledgments

- [Processing Foundation](https://processing.org/) for the Processing platform
- [sojamo](http://www.sojamo.de/) for oscP5
- [Daniel Howe](https://rednoise.org/rita/) for the RiTa computational literature library
