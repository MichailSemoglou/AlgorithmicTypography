# Algorithmic Typography

> A Processing library for creating algorithmic typography animations using mathematical wave functions.

[![Processing](https://img.shields.io/badge/Processing-4.x-blue)](https://processing.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Version](https://img.shields.io/badge/Version-1.1.1-orange)](https://github.com/MichailSemoglou/AlgorithmicTypography/releases)

> [!NOTE]
> **Version 1.1.1 resolves all known issues.** The library is stable and ready for use. If you encounter a bug or unexpected behaviour, please [open an issue](https://github.com/MichailSemoglou/AlgorithmicTypography/issues).

## Overview

AlgorithmicTypography is a Processing library that enables designers, researchers, and artists to explore **parametric typography systems**. It generates animated grids of typographic characters whose colours, positions, and arrangements are driven by mathematical wave functions — with support for glyph extraction, physics simulation, cell motion, audio reactivity, and cultural design presets.

## Features

- **Wave-driven animation** — Sine, tangent, square, triangle, sawtooth, Perlin noise, and custom wave functions
- **HSB colour mapping** — Configurable hue, saturation, and brightness ranges across the grid
- **Wave angle** — Control the propagation direction of the colour wave (0–360°)
- **Glyph extraction** — Extract glyph outlines as vertices (built-in alternative to Geomerative)
- **Glyph physics** — Treat glyph vertices as particles with mouse attraction/repulsion
- **Cell motion** — Clockwise, counter-clockwise, and Perlin noise movement within grid cells
- **Trail effects** — Semi-transparent overlay trails with temporal displacement
- **Audio reactivity** — Map bass, mid, treble, and beat detection to animation parameters
- **Cultural design presets** — Parameter sets inspired by Swiss, Bauhaus, Chinese Ink, Arabic Kufi, Japanese Minimal, and related typographic traditions (see [editorial note](#editorial-note))
- **Vibe presets** — Natural-language mood mapping (e.g. "calm", "techno", "ocean") (see [editorial note](#editorial-note))
- **SVG/PDF export** — Vector export with full colour preservation
- **JSON configuration** — Customise behaviour without touching code
- **Live controls** — GUI sliders, keyboard shortcuts, and OSC input
- **Programmatic API** — Build configurations dynamically in code

## Installation

### Manual Installation

1. Go to the [Releases page](https://github.com/MichailSemoglou/AlgorithmicTypography/releases) and download **`AlgorithmicTypography.zip`** — this is the pre-built library package. Do **not** download "Source code (zip)" or "Source code (tar.gz)", as those do not include the compiled library.
2. Unzip the file — you will get an `AlgorithmicTypography` folder.
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
│   └── PerlinMotion               Perlin-noise organic wandering
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
    "duration": 24,
    "fps": 30,
    "character": "A",
    "textScale": 0.8,
    "saveFrames": false,
    "waveSpeed": 2.0,
    "waveAngle": 45
  },
  "grid": {
    "initialTilesX": 32,
    "initialTilesY": 32
  },
  "colors": {
    "brightnessMin": 120,
    "brightnessMax": 255,
    "saturationMin": 0,
    "saturationMax": 255,
    "hueMin": 0,
    "hueMax": 0
  }
}
```

### Key Parameters

| Parameter       | Type    | Description                                    | Default |
| --------------- | ------- | ---------------------------------------------- | ------- |
| `character`     | String  | Character to render in the grid                | `"A"`   |
| `textScale`     | float   | Text size relative to tile (0–1)               | 0.8     |
| `waveSpeed`     | float   | Wave animation speed                           | 2.0     |
| `waveAngle`     | float   | Wave propagation direction in degrees          | 45      |
| `brightnessMin` | float   | Minimum brightness (0–255)                     | 0       |
| `brightnessMax` | float   | Maximum brightness (0–255)                     | 255     |
| `hueMin/Max`    | float   | Hue range (0–360); set both to 0 for greyscale | 0       |
| `saveFrames`    | boolean | Auto-save frames as PNG                        | true    |
| `initialTilesX` | int     | Grid columns                                   | 32      |
| `initialTilesY` | int     | Grid rows                                      | 32      |

When `hueMin` and `hueMax` are different, the renderer switches to HSB colour mode automatically.

## Examples

### BasicGrid

Simplest usage — loads a JSON configuration and renders the animation.

### ProgrammaticConfig

Configures the system entirely in code. Each restart generates a random configuration.

### CustomWave

Demonstrates all wave types: five built-in presets, Perlin noise, and a custom Julia-set fractal wave.

### CustomFont

Cycles through system fonts (Helvetica, Georgia, Courier, Futura, Didot) to compare typographic texture.

### WaveAngle

Controls the wave propagation angle (0–360°) with UI sliders and keyboard presets.

### CulturalStyles _(temporarily unavailable)_

Applies parameter sets inspired by typographic traditions across different cultural and historical design movements — Swiss International Style, Bauhaus, Chinese ink calligraphy, Arabic Kufic geometry, Japanese minimalism, and others. The underlying `DesignSystem` API is fully functional; the example sketch will be reintroduced in a later release.

> **Editorial note:** The cultural and mood presets in this library — including `DesignSystem` and `VibePreset` — are the author's artistic interpretation, informed by 30+ years of professional practice in the design industry. They are expressive starting points for creative exploration, not ethnographic, anthropological, or perceptual science models. Parameter choices (grid density, wave speed, colour range, angle) reflect aesthetic sensibility and accumulated design intuition, and should not be treated as culturally definitive or scientifically validated representations of the traditions they reference.

### VibeCoding

Natural-language configuration — press a key to set a mood (calm, techno, melancholic, chaotic, ocean) and the library adjusts wave speed, colour, grid density, and cell motion automatically.

> **Editorial note:** The mood-to-parameter mappings in `VibePreset` are the author's artistic interpretation based on 30+ years in the design industry. They are not grounded in psychoacoustics, affective computing research, or colour psychology literature. The labels (e.g. "calm", "techno", "ocean") serve as expressive shorthand for creative workflows, not as scientifically validated mood or emotion models.

### LiveControls

Real-time parameter adjustment with GUI sliders, keyboard shortcuts, and optional OSC input.

### MultipleSystems

Runs two typography instances side by side with independent configurations.

### PerformanceMode _(temporarily unavailable)_

Optimised rendering path for high-resolution grids — demonstrates frame-rate management, reduced draw calls, and export-compatible output at large tile counts. The example sketch will be reintroduced in a later release.

### AudioReactive

Audio-reactive typography — bass drives wave speed, treble controls brightness, beat detection triggers visual events.

### TextDrivenAnimation

RiTa integration — generates words from Markov chains/grammars and maps linguistic features to animation parameters.

### RandomASCII

Fills the grid with random printable ASCII characters with periodic reshuffling.

### SaveSVG

Exports the current frame as an SVG vector graphic with full HSB colour preservation.

### BackgroundImage

Renders typography on top of a photograph — place `background.png` in the sketch's `data/` folder and the grid is composited over it each frame.

### MultiPhoto

Assigns a different photograph to each grid cell. Place `photo-01.png` … `photo-16.png` in `data/`; the sketch tiles them behind the glyph grid with centre-crop and 3-stage grid tracking.

### GlyphPath

Extracts glyph outlines as vertices with five display modes: filled, points, deformed, contours, and a 4×4 tiled grid.

### GlyphDynamics

Particle-based physics — each glyph vertex becomes a particle with mouse repulsion and spring-back forces.

### TrailEffect

Glyphs move within their grid cells (CW, CCW, or Perlin noise) and leave fading trails via semi-transparent overlay.

## Documentation

A four-page printable cheat sheet (A4 landscape, dark theme) is available in:

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

// in draw():
PVector offset = perlin.getOffset(col, row, frameCount);
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

The **`DesignSystem`** (cultural presets) and **`VibePreset`** (natural-language mood mapping) components of this library are the author's artistic interpretation, developed from 30+ years of professional practice in the design industry. The parameter mappings — grid proportions, wave speed, colour ranges, animation angle — reflect accumulated design intuition and aesthetic sensibility, not empirical research.

Specifically:

- **Cultural presets** (Swiss, Bauhaus, Chinese Ink, Arabic Kufi, Japanese Minimal, etc.) are expressive starting points inspired by broad typographic traditions. They do not claim ethnographic, anthropological, or historical accuracy, and should not be cited as culturally definitive representations.
- **Vibe presets** (calm, techno, melancholic, chaotic, ocean, etc.) are qualitative mood anchors for creative workflows. They are not derived from psychoacoustic research, affective computing models, or colour psychology studies.

Users who require culturally or scientifically rigorous representations are encouraged to define their own parameter sets using the `Configuration` API and `CellMotion` extension points.

## Citation

```bibtex
@software{semoglou_algorithmic_typography_2026,
  title     = {Algorithmic Typography: A Processing Library
               for Parametric Typography Animation},
  author    = {Semoglou, Michail},
  year      = {2026},
  version   = {1.1.1},
  url       = {https://github.com/MichailSemoglou/AlgorithmicTypography}
}
```

## Author

[Michail Semoglou](mailto:m.semoglou@tongji.edu.cn)

## License

Released under the MIT License. See [LICENSE](LICENSE) for details.

## Acknowledgments

- [Processing Foundation](https://processing.org/) for the Processing platform
- [sojamo](http://www.sojamo.de/) for oscP5
- [Daniel Howe](https://rednoise.org/rita/) for the RiTa computational literature library
