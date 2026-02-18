# AlgorithmicTypography — p5.js Port

> Wave-based algorithmic typography for the browser, powered by [p5.js](https://p5js.org).

## Overview

A JavaScript port of the [AlgorithmicTypography](https://github.com/MichailSemoglou/AlgorithmicTypography)
Processing library. It reproduces the same wave-driven grid animation — three
grid stages with smooth cross-fade transitions, HSB colour mapping, and
pluggable wave functions — entirely in the browser with zero server requirements.

## Features

- **Full wave engine** — tangent, sine, square, triangle and sawtooth presets
- **3-stage grid progression** with configurable timing and smooth-step fades
- **HSB colour model** — independent hue, saturation and brightness waves
- **Custom wave functions** — plug in any `(frameCount, x, y, time, config) => brightness`
- **JSON configuration** — load settings from a file or set them programmatically
- **Sub-region rendering** — place multiple typographic systems side-by-side

## Quick Start

### Via `<script>` tags

```html
<script src="https://cdn.jsdelivr.net/npm/p5@1.9.4/lib/p5.min.js"></script>
<script src="src/Configuration.js"></script>
<script src="src/WavePresets.js"></script>
<script src="src/WaveEngine.js"></script>
<script src="src/AlgorithmicTypography.js"></script>
<script>
  let at;
  function setup() {
    createCanvas(1080, 1080);
    at = new AlgorithmicTypography(window); // global mode
    at.initialize();
  }
  function draw() {
    at.render();
  }
</script>
```

### Via npm (CommonJS)

```bash
npm install algorithmic-typography-p5
```

```js
const {
  AlgorithmicTypography,
  Configuration,
  WavePresets,
} = require("algorithmic-typography-p5");
```

## Usage

```javascript
let at;

function setup() {
  createCanvas(1080, 1080);

  const config = new Configuration();
  config.character = "A";
  config.waveSpeed = 1.0;
  config.hueMin = 0;
  config.hueMax = 360;
  config.saturationMin = 120;
  config.saturationMax = 255;

  at = new AlgorithmicTypography(window, config);
  at.initialize();
}

function draw() {
  at.render();
}
```

### Switching wave types at runtime

```javascript
at.setWaveType(WavePresets.Type.SINE); // smooth
at.setWaveType(WavePresets.Type.SQUARE); // bold checkerboard
at.setWaveType(WavePresets.Type.TRIANGLE); // gradient ramps
```

### Custom wave function

```javascript
at.setWaveFunction((frameCount, x, y, time, config) => {
  const n = (Math.sin(x * 10 + time * 5) + Math.cos(y * 10 - time * 3)) / 2;
  return (
    config.brightnessMin + n * (config.brightnessMax - config.brightnessMin)
  );
});
```

## API Differences from Java/Processing

| Java / Processing                 | p5.js / JavaScript                       |
| --------------------------------- | ---------------------------------------- |
| `new AlgorithmicTypography(this)` | `new AlgorithmicTypography(window, cfg)` |
| `at.loadConfiguration("f.json")`  | `await at.loadConfiguration("f.json")`   |
| `config.getWaveSpeed()`           | `config.waveSpeed`                       |
| `config.setWaveSpeed(2)`          | `config.waveSpeed = 2`                   |
| `WavePresets.Type.SINE`           | `WavePresets.Type.SINE`                  |
| `saveFrame()`                     | `saveCanvas()`                           |

## Examples

| File                                                   | Description                                                |
| ------------------------------------------------------ | ---------------------------------------------------------- |
| [examples/index.html](examples/index.html)             | Basic grid with default settings                           |
| [examples/interactive.html](examples/interactive.html) | Keyboard (1-5) wave switching + click to randomise colours |

## File Structure

```
p5js-port/
├── src/
│   ├── Configuration.js          # All animation parameters
│   ├── WavePresets.js            # 5 built-in wave functions
│   ├── WaveEngine.js             # HSB wave calculations
│   ├── AlgorithmicTypography.js  # Main render loop + stage transitions
│   └── index.js                  # CommonJS entry point
├── examples/
│   ├── index.html                # Basic demo
│   ├── basic.js
│   ├── interactive.html          # Interactive demo
│   └── interactive.js
├── package.json
└── README.md
```

## Browser Support

- Chrome 80+
- Firefox 75+
- Safari 13+
- Edge 80+

## License

MIT — same as parent library
