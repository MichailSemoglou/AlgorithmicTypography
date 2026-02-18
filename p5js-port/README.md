# AlgorithmicTypography p5.js Port

> Web-based version of AlgorithmicTypography for browser deployment.

## Overview

This is a companion port of the AlgorithmicTypography library for p5.js,
enabling typography animations to run in web browsers without Java/Processing.

## Status

🚧 **Preview Implementation** - Core features ported, some advanced features pending

## Features

### ✅ Implemented

- Core wave function animations
- JSON configuration loading
- Basic grid rendering
- Canvas export

### 🚧 In Progress

- Live parameter controls (HTML sliders)
- WebSocket integration (native in browser)
- Rita.js integration (via npm rita package)

### 📋 Planned

- WebGL renderer support
- Service Worker for offline use
- Progressive Web App (PWA)

## Installation

### Via CDN

```html
<script src="https://cdnjs.cloudflare.com/ajax/libs/p5.js/1.9.0/p5.min.js"></script>
<script src="https://cdn.jsdelivr.net/gh/MichailSemoglou/AlgorithmicTypography@main/p5js-port/algorithmic-typography.js"></script>
```

### Via npm

```bash
npm install algorithmic-typography-p5
```

## Usage

```javascript
let typography;

function setup() {
  createCanvas(1080, 1080);

  typography = new AlgorithmicTypography();
  typography.loadConfig("config.json");
  typography.initialize();
}

function draw() {
  typography.render();
}
```

## API Differences from Java/Processing

| Java/Processing          | p5.js/JavaScript         |
| ------------------------ | ------------------------ |
| `at.render()`            | `at.render()`            |
| `config.setWaveSpeed(2)` | `config.setWaveSpeed(2)` |
| `saveFrame()`            | `saveCanvas()`           |
| `PGraphics`              | `createGraphics()`       |
| ControlP5                | HTML5 sliders            |
| OSC                      | WebSocket (native)       |

## Browser Support

- Chrome 80+
- Firefox 75+
- Safari 13+
- Edge 80+

## Repository

```
p5js-port/
├── src/                    # Source code
│   ├── AlgorithmicTypography.js
│   ├── Configuration.js
│   └── WaveEngine.js
├── examples/               # HTML examples
├── dist/                   # Compiled bundle
├── package.json
└── README.md
```

## Development

```bash
cd p5js-port
npm install
npm run build
npm run dev
```

## License

MIT License - same as parent library
