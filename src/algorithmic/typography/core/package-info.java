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
 * 
 * @author Michail Semoglou
 * @version 1.0.0
 * @since 1.0.0
 */
package algorithmic.typography.core;
