/**
 * ProgressBar — lightweight read-only progress indicator.
 *
 * <p>Renders a horizontal track, a proportional fill, and a thumb dot
 * directly onto any Processing graphics context. Follows the same
 * dark-theme defaults and builder-style API as {@link Slider}.</p>
 *
 * <p>Usage:</p>
 * <pre>
 * ProgressBar bar;
 *
 * void setup() {
 *   bar = new ProgressBar(480)
 *             .setPosition(width / 2 - 240, height - 32);
 * }
 *
 * void draw() {
 *   // optional: tint with an animated hue each frame
 *   colorMode(HSB, 360, 255, 255);
 *   bar.setFillColor(color(frameCount % 360, 210, 255));
 *   colorMode(RGB, 255);
 *
 *   bar.setValue(t);   // t in [0, 1]
 *   bar.display(g);
 * }
 * </pre>
 *
 * @author Michail Semoglou
 * @version 0.2.5
 * @since 0.2.3
 */
package algorithmic.typography.ui;

import processing.core.*;

/**
 * A minimal horizontal progress bar that draws itself onto a {@link PGraphics} context.
 *
 * <p>The bar is purely read-only — it has no mouse interaction.
 * Call {@link #setValue(float)} each frame with a normalised value in [0, 1],
 * then call {@link #display(PGraphics)} to render it.</p>
 */
public class ProgressBar {

  // ── Geometry ──────────────────────────────────────────────────────
  private float x, y;
  private float barWidth;
  private static final float TRACK_HEIGHT  = 4;
  private static final float HANDLE_RADIUS = 4;

  // ── Value ─────────────────────────────────────────────────────────
  private float value = 0;   // normalised [0, 1]

  // ── Colour (dark-theme defaults) ──────────────────────────────────
  /** Track background: white at ~16 % alpha. */
  private int trackColor  = 0x29FFFFFF;
  /** Active fill: light grey. Override each frame for animated colour. */
  private int fillColor   = 0xFFCCCCCC;
  /** Thumb dot: solid white by default. */
  private int handleColor = 0xFFFFFFFF;

  // ── Constructor ───────────────────────────────────────────────────

  /**
   * Creates a ProgressBar with the given track width.
   *
   * @param barWidth width of the track in pixels
   */
  public ProgressBar(float barWidth) {
    this.barWidth = barWidth;
  }

  // ── Builder-style setters ─────────────────────────────────────────

  /**
   * Sets the top-left position of the bar.
   *
   * @param x left edge in pixels
   * @param y top edge in pixels
   * @return this, for chaining
   */
  public ProgressBar setPosition(float x, float y) {
    this.x = x;
    this.y = y;
    return this;
  }

  /**
   * Sets the track width.
   *
   * @param w new width in pixels
   * @return this, for chaining
   */
  public ProgressBar setWidth(float w) {
    this.barWidth = w;
    return this;
  }

  /**
   * Sets the colour of the track background.
   *
   * @param c ARGB colour (e.g. {@code 0x29FFFFFF} for transparent white)
   * @return this, for chaining
   */
  public ProgressBar setTrackColor(int c) {
    this.trackColor = c;
    return this;
  }

  /**
   * Sets the colour of the active fill and thumb dot together.
   * Call each frame to animate colour.
   *
   * @param c ARGB colour value (from Processing's {@code color()} function)
   * @return this, for chaining
   */
  public ProgressBar setFillColor(int c) {
    this.fillColor = c;
    return this;
  }

  /**
   * Sets the thumb dot colour independently of the fill.
   *
   * @param c ARGB colour value
   * @return this, for chaining
   */
  public ProgressBar setHandleColor(int c) {
    this.handleColor = c;
    return this;
  }

  // ── Value ─────────────────────────────────────────────────────────

  /**
   * Sets the normalised progress value.
   *
   * @param v value in [0, 1]; clamped automatically
   */
  public void setValue(float v) {
    this.value = PApplet.constrain(v, 0, 1);
  }

  /** Returns the current normalised value. */
  public float getValue() { return value; }

  // ── Drawing ───────────────────────────────────────────────────────

  /**
   * Renders the progress bar onto the given graphics context.
   * Call each frame after updating the value.
   *
   * @param g Processing graphics context (pass {@code g} from within a sketch)
   */
  public void display(PGraphics g) {
    float trackY    = y + TRACK_HEIGHT / 2;
    float thumbX    = x + barWidth * value;

    // ─ Track background ─
    g.noStroke();
    g.fill(trackColor);
    g.rect(x, y, barWidth, TRACK_HEIGHT, 2);

    // ─ Active fill ─
    if (value > 0) {
      g.fill(fillColor);
      g.rect(x, y, barWidth * value, TRACK_HEIGHT, 2);
    }

    // ─ Thumb dot ─
    g.fill(fillColor == handleColor ? fillColor : handleColor);
    g.ellipse(thumbX, trackY, HANDLE_RADIUS * 2, HANDLE_RADIUS * 2);
  }
}
