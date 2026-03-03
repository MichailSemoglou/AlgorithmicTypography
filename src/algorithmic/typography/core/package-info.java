/**
 * Core mathematical engine for AlgorithmicTypography.
 * 
 * This package contains the wave function calculations and mathematical
 * transformations that drive the typography animations.
 * 
 * Key classes:
 * - {@link WaveEngine}: Wave calculation engine
 * - {@link WaveFunction}: Plugin interface for custom waves
 * - {@link WavePresets}: Built-in wave types (Sine, Tangent, Square, Triangle, Sawtooth, Perlin)
 * - {@link TemporalTrail}: Delay/trail compositing buffer with audio/framerate reactivity
 * - {@link CellMotion}: Abstract base for per-cell glyph movement
 * - {@link CircularMotion}: Clockwise / counter-clockwise orbital motion
 * - {@link PerlinMotion}: Perlin-noise organic wandering
 * - {@link LissajousMotion}: Figure-8 and knot-shaped Lissajous orbits
 * - {@link SpringMotion}: Spring-damped glyphs pulled toward a drifting target
 * - {@link GravityMotion}: Gravity-driven fall and bounce within a cell
 * - {@link MagneticMotion}: Mouse-driven repel / attract field
 * - {@link RippleMotion}: Click-triggered concentric displacement rings (v0.2.3)
 * - {@link FlowFieldMotion}: Spatially coherent Perlin-noise vector field (v0.2.3)
 * - {@link OrbitalMotion}: Glyphs orbit neighbour anchors in constellation patterns (v0.2.3)
 * 
 * @author Michail Semoglou
 * @version 0.2.3
 * @since 1.0.0
 */
package algorithmic.typography.core;
