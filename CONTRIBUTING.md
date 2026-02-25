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
   - Java 11 or later
   - Ant (optional, for building)

#### Building the Library

```bash
# Using Ant (recommended)
ant build

# Or using the shell script
./build.sh
```

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

- [ ] **Documentation**: Tutorials, examples, video guides

#### New Motion Types

The `CellMotion` base class makes it straightforward to add new per-glyph movement strategies. Ideas:

- **`SpringMotion`** — glyphs pulled toward a target point with damping
- **`GravityMotion`** — glyphs fall and bounce within their cells
- **`MagneticMotion`** — glyphs repel/attract based on mouse position
- **`LissajousMotion`** — figure-8 and knot-shaped orbits

To add one, extend `CellMotion`, implement `getOffset(col, row, frameCount)`, and it works immediately with `config.setCellMotion()` and the full `VibePreset` pipeline.

#### Other Ideas

- New wave functions (custom equations, reaction-diffusion)
- Additional export formats (TIFF with metadata, MP4 via FFmpeg)
- WebSocket server for browser-based control
- Machine learning integration (style transfer, generative text selection)
- Expanded p5.js port (motion types, audio reactivity)

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
