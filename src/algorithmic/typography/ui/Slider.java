/**
 * Slider — lightweight GUI slider for Processing sketches.
 *
 * <p>A fully self-contained, mouse-interactive slider that renders
 * itself directly to the Processing canvas. No external libraries needed.</p>
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Horizontal slider with label, value readout, and unit suffix</li>
 *   <li>Click-to-set and drag-to-scrub interaction</li>
 *   <li>Configurable range, step snapping, and colour</li>
 *   <li>Compact footprint: 180 × 18 px default</li>
 * </ul>
 *
 * @author Michail Semoglou
 * @version 1.0.0
 * @since 1.0.0
 */
package algorithmic.typography.ui;

import processing.core.*;

/**
 * A minimal horizontal slider that draws and handles mouse events itself.
 *
 * <pre>
 * Slider s = new Slider("Speed", 0.1f, 10, 2, 160)
 *                .setPosition(20, 40);
 *
 * // in draw():
 * s.display(g);
 *
 * // in mousePressed / mouseDragged / mouseReleased:
 * s.mousePressed(mouseX, mouseY);
 * s.mouseDragged(mouseX, mouseY);
 * s.mouseReleased();
 * </pre>
 */
public class Slider {

  // ── Geometry ──────────────────────────────────────────────────────
  private float x, y;
  private float trackWidth;
  private static final float TRACK_HEIGHT = 4;
  private static final float HANDLE_RADIUS = 7;
  private static final float ROW_HEIGHT = 18;

  // ── Value ─────────────────────────────────────────────────────────
  private final String label;
  private float min, max, value;
  private String suffix = "";
  private int decimals = 1;

  // ── Interaction ───────────────────────────────────────────────────
  private boolean dragging = false;

  // ── Colour (dark-theme defaults) ──────────────────────────────────
  private int trackColor   = 0xFF444444;
  private int fillColor    = 0xFFCCCCCC;
  private int handleColor  = 0xFFFFFFFF;
  private int labelColor   = 0xFFCCCCCC;

  // ── Callback ──────────────────────────────────────────────────────
  private SliderCallback callback;

  /** Functional callback for value changes. */
  @FunctionalInterface
  public interface SliderCallback {
    void onValueChanged(float value);
  }

  // ── Constructors ──────────────────────────────────────────────────

  /**
   * Creates a slider.
   *
   * @param label      display name
   * @param min        minimum value
   * @param max        maximum value
   * @param initial    initial value
   * @param trackWidth width of the slider track in pixels
   */
  public Slider(String label, float min, float max, float initial, float trackWidth) {
    this.label = label;
    this.min = min;
    this.max = max;
    this.value = PApplet.constrain(initial, min, max);
    this.trackWidth = trackWidth;
  }

  // ── Builder-style setters ─────────────────────────────────────────

  /** Sets the top-left position of this slider row. */
  public Slider setPosition(float x, float y) {
    this.x = x;
    this.y = y;
    return this;
  }

  /** Sets the unit suffix shown after the value (e.g. "px", "°"). */
  public Slider setSuffix(String suffix) {
    this.suffix = suffix;
    return this;
  }

  /** Sets the number of decimal places in the readout. */
  public Slider setDecimals(int decimals) {
    this.decimals = decimals;
    return this;
  }

  /** Sets an on-change callback. */
  public Slider onChange(SliderCallback cb) {
    this.callback = cb;
    return this;
  }

  /** Sets the track fill colour (for the active portion). */
  public Slider setFillColor(int c) {
    this.fillColor = c;
    return this;
  }

  // ── Value access ──────────────────────────────────────────────────

  /** Returns the current value. */
  public float getValue() { return value; }

  /** Sets the value programmatically (does NOT fire callback). */
  public void setValue(float v) {
    this.value = PApplet.constrain(v, min, max);
  }

  /** Updates the allowed range (clamps current value). */
  public void setRange(float min, float max) {
    this.min = min;
    this.max = max;
    this.value = PApplet.constrain(value, min, max);
  }

  /** Returns the row height in pixels. */
  public static float getRowHeight() { return ROW_HEIGHT; }

  // ── Drawing ───────────────────────────────────────────────────────

  /**
   * Draws the slider onto the given graphics context.
   *
   * @param g the PGraphics (usually {@code this.g} from the sketch)
   */
  public void display(PGraphics g) {
    float labelWidth = 90;
    float trackX = x + labelWidth;
    float trackY = y + ROW_HEIGHT / 2;
    float norm = (max > min) ? (value - min) / (max - min) : 0;

    // ─ Label (left) ─
    g.fill(labelColor);
    g.textAlign(PApplet.LEFT, PApplet.CENTER);
    g.textSize(11);
    g.text(label, x, trackY);

    // ─ Track background ─
    g.noStroke();
    g.fill(trackColor);
    g.rect(trackX, trackY - TRACK_HEIGHT / 2, trackWidth, TRACK_HEIGHT, 2);

    // ─ Track fill ─
    g.fill(fillColor);
    g.rect(trackX, trackY - TRACK_HEIGHT / 2, trackWidth * norm, TRACK_HEIGHT, 2);

    // ─ Handle ─
    float hx = trackX + trackWidth * norm;
    g.fill(dragging ? 0xFFFFFF00 : handleColor);
    g.ellipse(hx, trackY, HANDLE_RADIUS * 2, HANDLE_RADIUS * 2);

    // ─ Value readout (right of track) ─
    g.fill(labelColor);
    g.textAlign(PApplet.LEFT, PApplet.CENTER);
    String fmt = "%." + decimals + "f" + suffix;
    g.text(String.format(fmt, value), trackX + trackWidth + 10, trackY);
  }

  // ── Mouse interaction ─────────────────────────────────────────────

  /**
   * Call from {@code mousePressed()}. Returns true if this slider
   * captured the press.
   */
  public boolean mousePressed(float mx, float my) {
    if (hitTest(mx, my)) {
      dragging = true;
      updateValue(mx);
      return true;
    }
    return false;
  }

  /**
   * Call from {@code mouseDragged()}.
   */
  public void mouseDragged(float mx, float my) {
    if (dragging) updateValue(mx);
  }

  /**
   * Call from {@code mouseReleased()}.
   */
  public void mouseReleased() {
    dragging = false;
  }

  /** Returns true if the slider is currently being dragged. */
  public boolean isDragging() { return dragging; }

  // ── Internals ─────────────────────────────────────────────────────

  private boolean hitTest(float mx, float my) {
    float labelWidth = 90;
    float trackX = x + labelWidth;
    float trackY = y + ROW_HEIGHT / 2;
    // generous vertical hit zone
    return mx >= trackX - HANDLE_RADIUS && mx <= trackX + trackWidth + HANDLE_RADIUS
        && my >= trackY - ROW_HEIGHT / 2 && my <= trackY + ROW_HEIGHT / 2;
  }

  private void updateValue(float mx) {
    float labelWidth = 90;
    float trackX = x + labelWidth;
    float norm = PApplet.constrain((mx - trackX) / trackWidth, 0, 1);
    float newVal = PApplet.lerp(min, max, norm);
    if (newVal != value) {
      value = newVal;
      if (callback != null) callback.onValueChanged(value);
    }
  }
}
