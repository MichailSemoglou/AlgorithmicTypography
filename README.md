# Algorithmic Typography

> A Processing library for graphic designers and typographers, covering wave-driven glyph animation, per-cell physics, live UI controls, and audio reactivity.

[![Processing](https://img.shields.io/badge/Processing-4.x-blue)](https://processing.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Version](https://img.shields.io/badge/Version-0.2.4-orange)](https://github.com/MichailSemoglou/AlgorithmicTypography/releases)

![AlgorithmicTypography showcase](docs/showcase.gif)
![AlgorithmicTypography showcase_2](docs/showcase_2.gif)

> [!NOTE]
> **Version 0.2.4** makes the library significantly more expressive from `config.json` alone. A new `"cellMotion"` JSON block configures any of the 10 built-in motion styles (`"perlin"`, `"circular"`, `"lissajous"`, `"spring"`, `"gravity"`, `"magnetic"`, `"ripple"`, `"flowfield"`, `"orbital"`, `"none"`) directly from the file, unifying wave, colour, and motion into a single declarative config. New `"waveType"` field selects the wave shape (`SINE`, `TANGENT`, `SQUARE`, `TRIANGLE`, `SAWTOOTH`) without any code changes; `waveAngle` is now present in all shipped example configs. A new `"cellBorder"` JSON block (plus matching `setCellBorder*()` API and `BORDER_*` bitmask constants) draws per-cell border strokes on any combination of sides with either a static colour or a wave-reactive colour mode that pulses in sync with glyph brightness. Several example sketches were also refined in this release: TrailEffect now focuses purely on motion and trail behaviour (wave angle controls removed to keep the demonstration clear), and its colour transitions were tuned for a smoother, more organic feel. Built on v0.2.3, which deepened the `GlyphExtractor` toolkit (`fillWithLines()`, `offsetOutline()`, `interpolateTo()`, `getMedialAxis()`, `sampleAlongPath()`, `getBoundingContour()`, and the fluent `GlyphBuilder` API) and added three `CellMotion` types (`RippleMotion`, `FlowFieldMotion`, `OrbitalMotion`). This project follows [Semantic Versioning](https://semver.org/); the `0.x` series reflects active development. AlgorithmicTypography is an ongoing project built for the Processing community. If you encounter a bug or have ideas, please [open an issue](https://github.com/MichailSemoglou/AlgorithmicTypography/issues) or join the [GitHub Discussions](https://github.com/MichailSemoglou/AlgorithmicTypography/discussions).

## Overview

AlgorithmicTypography is built mainly for **graphic designers and typographers**. It generates animated grids of glyphs whose colours, positions, and motion are driven by mathematical wave functions and a growing suite of per-cell physics engines, with live UI controls, audio reactivity, glyph physics, and vector export.

It is expressive enough for complex typographic systems and approachable enough for a designer's first generative sketch.

The library is also well suited to **design education**. Its layered API, spanning one-line vibe presets and JSON configuration through to custom wave functions and per-cell physics engines, makes it a practical tool for generative typography courses. Students can start with immediate visual results and progressively engage with the underlying parameters, using the library to explore the relationship between code, form, and typographic expression.

### Why AlgorithmicTypography?

What makes this library distinctive is that it was designed from the ground up with **designers in mind**. You do not need to understand signal processing to create a wave-driven animation, manage threads to export frames, or parse font internals to extract glyph outlines: the library handles all of that. The result is a tool that feels at home in a design workflow while remaining fully open to code-level customisation.

**For newcomers,** it is one of the most accessible entry points into generative typography in the Processing ecosystem. A working sketch requires just four lines of code; a JSON file handles the rest. From there, the API grows with you — every new concept (physics, audio, custom waves) builds on the same foundation rather than requiring a fresh start.

**As an alternative to Geomerative,** AlgorithmicTypography covers the same core use cases — outline extraction, contour separation, interior point distribution, arc-length-spaced perimeter sampling, hatch fill, outline offsetting, and letterform morphing — without requiring an external dependency, and with a few architectural differences worth knowing about. Geomerative parses font files directly through its own SVG/font engine; AlgorithmicTypography goes through Java2D's own `FlatteningPathIterator` on the raw AWT font outline, which means outer vs. inner contour separation is determined by contour area (deterministic and font-agnostic) rather than winding order, and arc-length resampling is calculated geometrically rather than parametrically so points are spaced evenly whether a stroke is a tight curve or a long straight stem. The `centerOf`, `drawAt`, and `morphShape` methods express design intent directly, with no glyph-bounds arithmetic or matrix boilerplate at the sketch level. Beyond glyph geometry, per-vertex physics, cell-level motion engines, audio reactivity, and live UI controls are all built in, making it a more complete toolkit for animated and interactive typographic work.

The long-term goal is to make AlgorithmicTypography the definitive library for generative typography in Processing. The current release focuses on integration and accessibility, bringing tools that previously required multiple libraries or significant boilerplate into a single, designer-friendly API. Genuinely new capabilities are on the roadmap.

## Features

- **Wave-driven animation** — Sine, tangent, square, triangle, sawtooth, Perlin noise, and custom wave functions — now selectable directly in `config.json` via `waveType`
- **HSB colour mapping** — Configurable hue, saturation, and brightness ranges across the grid
- **Wave angle** — Control the propagation direction of the colour wave (0–360°) — now available in `config.json` via `waveAngle`
- **Cell borders** — Draw optional per-cell border lines (top, bottom, left, right, or any bitmask combination) with configurable colour, weight, and a wave-reactive colour mode (`cellBorder`)
- **Glyph extraction** — Extract glyph outlines as vertices (built-in alternative to Geomerative)
- **Glyph physics** — Treat glyph vertices as particles with mouse attraction/repulsion
- **Cell motion** — Ten built-in movement strategies per glyph: Circular (CW/CCW), Perlin noise, Lissajous figure-8/knot, Spring-damped, Gravity + bounce (with `kick()`), Mouse-magnetic (repel/attract), Ripple (click-triggered concentric rings), FlowField (spatially coherent Perlin vector field), and Orbital (constellation orbit patterns) — all configurable via code _or_ the new `"cellMotion"` JSON block
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
    "waveType": "SINE",
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
  },
  "cellBorder": {
    "sides": 0,
    "r": 255,
    "g": 255,
    "b": 255,
    "weight": 1.0,
    "colorMode": 0
  },
  "cellMotion": {
    "style": "none",
    "radius": 12,
    "speed": 1.0
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

| Parameter           | Type    | Description                                                             | Default  |
| ------------------- | ------- | ----------------------------------------------------------------------- | -------- |
| `character`         | String  | Character to render in the grid                                         | `"A"`    |
| `textScale`         | float   | Text size relative to tile (0–1)                                        | 0.8      |
| `duration`          | int     | Animation duration in seconds                                           | 18       |
| `fps`               | int     | Frames per second                                                       | 30       |
| `saveFrames`        | boolean | Auto-save frames as PNG                                                 | false    |
| `waveSpeed`         | float   | Wave animation speed                                                    | 1.0      |
| `waveAngle`         | float   | Wave propagation direction in degrees (0–360)                           | 45       |
| `waveType`          | String  | Wave shape: `SINE`, `TANGENT`, `SQUARE`, `TRIANGLE`, or `SAWTOOTH`      | `"SINE"` |
| `waveMultiplierMin` | float   | Minimum wave multiplier applied during rendering                        | 0.0      |
| `waveMultiplierMax` | float   | Maximum wave multiplier applied during rendering                        | 2.0      |
| `changeTime`        | int     | Time in ms when grid transitions from stage 1 to stage 2                | 6000     |
| `secondChangeTime`  | int     | Time in ms when grid transitions from stage 2 to stage 3 (0 = disabled) | 12000    |
| `fadeDuration`      | int     | Crossfade duration in ms between grid stages                            | 2000     |

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

**cellBorder**

| Parameter   | Type  | Description                                                                                                     | Default |
| ----------- | ----- | --------------------------------------------------------------------------------------------------------------- | ------- |
| `sides`     | int   | Bitmask of sides to draw: `1`=top, `2`=bottom, `4`=left, `8`=right, `15`=all, `0`=none                          | 0       |
| `r`         | int   | Border colour red channel (0–255)                                                                               | 255     |
| `g`         | int   | Border colour green channel (0–255)                                                                             | 255     |
| `b`         | int   | Border colour blue channel (0–255)                                                                              | 255     |
| `weight`    | float | Stroke weight in pixels                                                                                         | 1.0     |
| `colorMode` | int   | `0` = static colour (r/g/b), `1` = wave-reactive (border brightness pulses in sync with each cell's wave value) | 0       |

Side bitmask constants in code: `Configuration.BORDER_TOP`, `BORDER_BOTTOM`, `BORDER_LEFT`, `BORDER_RIGHT`, `BORDER_ALL`, `BORDER_NONE`. Combine with `|`: e.g. `BORDER_TOP | BORDER_BOTTOM` = horizontal rules only.

**cellMotion**

| Parameter         | Type    | Description                                                                                                                                    | Default  |
| ----------------- | ------- | ---------------------------------------------------------------------------------------------------------------------------------------------- | -------- |
| `style`           | String  | Motion style: `"none"`, `"perlin"`, `"circular"`, `"lissajous"`, `"spring"`, `"gravity"`, `"magnetic"`, `"ripple"`, `"flowfield"`, `"orbital"` | `"none"` |
| `radius`          | float   | Maximum displacement from cell centre in pixels                                                                                                | 12       |
| `speed`           | float   | Animation speed multiplier                                                                                                                     | 1.0      |
| `clockwise`       | boolean | **circular only** — rotation direction                                                                                                         | true     |
| `stiffness`       | float   | **spring only** — spring constant (0.05–1.0)                                                                                                   | 0.3      |
| `damping`         | float   | **spring only** — damping coefficient (0.05–0.5)                                                                                               | 0.15     |
| `gravity`         | float   | **gravity only** — downward acceleration in px/frame²                                                                                          | 0.6      |
| `restitution`     | float   | **gravity only** — bounce energy retention (0–1)                                                                                               | 0.72     |
| `lateralStrength` | float   | **gravity only** — sinusoidal lateral force amplitude                                                                                          | 0.06     |
| `strength`        | float   | **magnetic only** — field strength (typical: 400–4000)                                                                                         | 1800     |
| `falloff`         | float   | **magnetic only** — half-force distance in pixels                                                                                              | 80       |
| `smoothing`       | float   | **magnetic only** — per-frame lerp factor (0.05–0.4)                                                                                           | 0.12     |
| `attract`         | boolean | **magnetic only** — `true` = attract, `false` = repel                                                                                          | false    |
| `expandSpeed`     | float   | **ripple only** — ring expansion speed (px/s equivalent)                                                                                       | 200      |
| `waveWidth`       | float   | **ripple only** — width of the displacement band                                                                                               | 80       |
| `decayRate`       | float   | **ripple only** — per-frame amplitude decay (0–1; 1 = no decay)                                                                                | 0.975    |
| `fieldScale`      | float   | **flowfield only** — spatial scale of the noise field                                                                                          | 0.007    |
| `evolutionRate`   | float   | **flowfield only** — temporal evolution speed per frame                                                                                        | 0.005    |
| `octaves`         | int     | **flowfield only** — number of curl-noise octave layers (1–4); more octaves add fine-grained turbulence                                        | 2        |
| `persistence`     | float   | **flowfield only** — amplitude decay per octave (0–1; lower = finer octaves have less influence)                                               | 0.45     |
| `phaseRange`      | float   | **flowfield only** — maximum inter-cell Z-axis phase spread; `0` restores fully synchronised (block) behaviour                                 | 8.0      |

Omit the `cellMotion` block entirely, or set `"style": "none"`, for no per-cell motion. `magnetic` and `ripple` are wired to the sketch automatically using the canvas dimensions and tile counts already in the config.

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

A dedicated showcase for `interpolateTo()`, the v0.2.3 morphing API. A large central letterform breathes between two characters via a smooth ping-pong animation. A timeline strip at the bottom shows five fixed-t snapshots (t = 0 / 0.25 / 0.5 / 0.75 / 1.0) so you can see the full morphing spread at a glance. Three display styles — Outline, Filled, and Dot Cloud — let you explore different creative directions. Mouse-X scrub mode hands control of t directly to the cursor. Six curated character pairs chosen for interesting midpoints; SPACE cycles through them.

### GlyphPath

Extracts glyph outlines as vertices with ten display modes: filled, points (arc-length distributed), deformed, contours, a 4×4 tiled grid, interior fill points, outer contour only, outer + inner counter-forms, hatch fill (`fillWithLines`), and path particle (`sampleAlongPath`).

### GlyphDynamics

Particle-based physics — each glyph vertex becomes a particle with mouse repulsion and spring-back forces.

### GlyphWander

Demonstrates per-glyph cell motion via `config.setCellMotion()`. Cycles through eleven motion modes — None, Perlin, Circular CW, Circular CCW, Lissajous, Spring, Gravity, Magnetic, Ripple, FlowField, and Orbital — with live radius and speed adjustment. In Ripple mode, clicking triggers a concentric ring at the cursor position.

### TrailEffect

Glyphs move within their grid cells and leave fading trails via semi-transparent overlay. Supports seven motion modes: Circular CW/CCW, Perlin noise, Lissajous, Spring, Gravity, and Magnetic. Trail length and motion radius are adjustable live.

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
PShape shape = glyph.extractChar('A', 400);  // filled PShape
PVector[] pts = glyph.getContourPoints('A', 400);  // outline vertices
List<PVector[]> contours = glyph.getContours('A', 400);
PShape deformed = glyph.extractDeformed('A', 400, amp, freq, phase);

// v0.2.2 — designer utilities
PVector[] fill = glyph.fillWithPoints('O', 400, 800);  // interior scatter
PVector[] ring = glyph.distributeAlongOutline('O', 400, 200);  // arc-length spaced
PVector[] outer = glyph.getOuterContour('B', 400);  // outer boundary only
List<PVector[]> holes = glyph.getInnerContours('B', 400);  // counter-forms only

// v0.2.3 — geometry depth
float[][] segs = glyph.fillWithLines('A', 400, 45, 8);  // hatch at 45°, 8 px apart
PVector[] shrunk = glyph.offsetOutline('A', 400, -6);  // contract by 6 px
PVector[] morph = glyph.interpolateTo('A', 'B', 400, 0.5);  // halfway between A and B
PVector[] spine = glyph.getMedialAxis('A', 400, 120);  // approximate letterform spine
PVector pt = glyph.sampleAlongPath('O', 400, 0.25);  // point at 25% around the outline

// v0.2.3 — centering & high-level drawing (eliminate getBounds boilerplate)
PVector o = glyph.centerOf('A', 400, width/2, height/2);  // centering offset for a single glyph
PVector om = glyph.morphCenterOf('A', 'B', 400, 0.5, cx, cy);  // lerped offset for a morphing pair
glyph.drawAt('A', 400, width/2, height/2);  // draw centred, current fill/stroke

// v0.2.3 — morph rendering (holes handled inside the library)
PShape ms = glyph.morphShape('A', 'B', 400, 0.5);  // ready-to-draw PShape for morph
ms.disableStyle(); fill(200); shape(ms, o.x, o.y);
List<PVector[]> mc = glyph.interpolateContours('A', 'B', 400, 0.5);  // raw contours for point effects
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
