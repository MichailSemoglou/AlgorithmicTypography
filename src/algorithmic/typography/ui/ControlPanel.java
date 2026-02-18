/**
 * ControlPanel — built-in GUI for live parameter adjustment.
 *
 * <p>Provides a collapsible panel of interactive {@link Slider} controls
 * that let users tweak every HSB and animation parameter in real time.
 * No external libraries (ControlP5, etc.) are required.</p>
 *
 * <h2>Usage</h2>
 * <pre>
 * ControlPanel panel;
 *
 * void setup() {
 *   at = new AlgorithmicTypography(this);
 *   panel = new ControlPanel(this, config);
 *   panel.createControls();
 * }
 *
 * void draw() {
 *   at.render();
 *   panel.draw();           // draws the panel on top
 * }
 *
 * void mousePressed()  { panel.mousePressed(mouseX, mouseY);  }
 * void mouseDragged()  { panel.mouseDragged(mouseX, mouseY);  }
 * void mouseReleased() { panel.mouseReleased();                }
 * </pre>
 *
 * <p>Keyboard shortcuts still work via {@link #handleKeyPress(char)}.</p>
 *
 * @author Michail Semoglou
 * @version 1.0.0
 * @since 1.0.0
 */
package algorithmic.typography.ui;

import processing.core.*;
import algorithmic.typography.*;

public class ControlPanel {

  private final PApplet parent;
  private final ObservableConfiguration config;

  // ── Layout ──────────────────────────────────────────────────────
  private boolean visible = true;
  private float panelX = 10;
  private float panelY = 10;
  private float panelWidth = 320;
  private static final float PADDING = 10;
  private static final float HEADER = 24;

  // ── Sliders ─────────────────────────────────────────────────────
  private Slider speedSlider;
  private Slider brightMinSlider;
  private Slider brightMaxSlider;
  private Slider satMinSlider;
  private Slider satMaxSlider;
  private Slider hueMinSlider;
  private Slider hueMaxSlider;
  private Slider textScaleSlider;
  private Slider[] sliders;

  // ── Constructor ─────────────────────────────────────────────────

  /**
   * Creates a new ControlPanel.
   *
   * @param parent the parent PApplet
   * @param config the observable configuration to control
   */
  public ControlPanel(PApplet parent, ObservableConfiguration config) {
    this.parent = parent;
    this.config = config;
  }

  // ── Setup ───────────────────────────────────────────────────────

  /**
   * Creates all slider controls and wires them to the configuration.
   * Call once in {@code setup()}.
   */
  public void createControls() {
    float tw = panelWidth - PADDING * 2 - 140;   // track width

    speedSlider    = slider("Wave Speed",    0.1f,  10, config.getWaveSpeed(),    tw, 1);
    brightMinSlider = slider("Bright Min",   0,    255, config.getBrightnessMin(), tw, 0);
    brightMaxSlider = slider("Bright Max",   0,    255, config.getBrightnessMax(), tw, 0);
    satMinSlider   = slider("Sat Min",       0,    255, config.getSaturationMin(), tw, 0);
    satMaxSlider   = slider("Sat Max",       0,    255, config.getSaturationMax(), tw, 0);
    hueMinSlider   = slider("Hue Min",       0,    360, config.getHueMin(),        tw, 0).setSuffix("°");
    hueMaxSlider   = slider("Hue Max",       0,    360, config.getHueMax(),        tw, 0).setSuffix("°");
    textScaleSlider = slider("Text Scale",  0.1f,    1, config.getTextScale(),     tw, 2);

    // Wire callbacks
    speedSlider    .onChange(v -> config.setWaveSpeed(v));
    brightMinSlider.onChange(v -> config.setBrightnessRange(v, config.getBrightnessMax()));
    brightMaxSlider.onChange(v -> config.setBrightnessRange(config.getBrightnessMin(), v));
    satMinSlider   .onChange(v -> config.setSaturationRange(v, config.getSaturationMax()));
    satMaxSlider   .onChange(v -> config.setSaturationRange(config.getSaturationMin(), v));
    hueMinSlider   .onChange(v -> config.setHueRange(v, config.getHueMax()));
    hueMaxSlider   .onChange(v -> config.setHueRange(config.getHueMin(), v));
    textScaleSlider.onChange(v -> config.setTextScale(v));

    sliders = new Slider[] {
      speedSlider, brightMinSlider, brightMaxSlider,
      satMinSlider, satMaxSlider,
      hueMinSlider, hueMaxSlider,
      textScaleSlider
    };

    layoutSliders();
    parent.println("Control panel ready (H to toggle)");
  }

  private Slider slider(String label, float min, float max, float init,
                         float trackWidth, int decimals) {
    return new Slider(label, min, max, init, trackWidth).setDecimals(decimals);
  }

  private void layoutSliders() {
    float y = panelY + HEADER + PADDING;
    for (Slider s : sliders) {
      s.setPosition(panelX + PADDING, y);
      y += Slider.getRowHeight() + 6;
    }
  }

  // ── Drawing ─────────────────────────────────────────────────────

  /**
   * Draws the panel and all sliders. Call each frame in {@code draw()}.
   */
  public void draw() {
    if (!visible) return;

    // Sync slider positions from config (external changes, OSC, etc.)
    syncFromConfig();

    float panelHeight = HEADER + PADDING + sliders.length * (Slider.getRowHeight() + 6) + 40;

    // Background
    parent.fill(0, 180);
    parent.noStroke();
    parent.rect(panelX, panelY, panelWidth, panelHeight, 6);

    // Title bar
    parent.fill(255);
    parent.textAlign(PApplet.LEFT, PApplet.TOP);
    parent.textSize(13);
    parent.text("CONTROLS  (H to hide)", panelX + PADDING, panelY + 6);

    // Sliders
    for (Slider s : sliders) {
      s.display(parent.g);
    }

    // Footer info line
    float footerY = panelY + panelHeight - 22;
    parent.fill(150);
    parent.textSize(10);
    parent.textAlign(PApplet.LEFT, PApplet.TOP);
    parent.text("Grid " + config.getInitialTilesX() + "×" + config.getInitialTilesY()
                + "   Char \"" + config.getCharacter() + "\""
                + "   FPS " + String.format("%.0f", parent.frameRate),
                panelX + PADDING, footerY);
  }

  /**
   * @deprecated Use {@link #draw()} instead. Kept for backward compatibility.
   */
  @Deprecated
  public void displayHelp() {
    draw();
  }

  // ── Mouse forwarding ──────────────────────────────────────────────

  /**
   * Forwards a mouse-press event to the sliders.
   * Call from the sketch's {@code mousePressed()}.
   *
   * @return true if a slider captured the press
   */
  public boolean mousePressed(float mx, float my) {
    if (!visible) return false;
    for (Slider s : sliders) {
      if (s.mousePressed(mx, my)) return true;
    }
    return false;
  }

  /** Forwards a mouse-drag event. Call from {@code mouseDragged()}. */
  public void mouseDragged(float mx, float my) {
    if (!visible) return;
    for (Slider s : sliders) {
      s.mouseDragged(mx, my);
    }
  }

  /** Forwards a mouse-release event. Call from {@code mouseReleased()}. */
  public void mouseReleased() {
    for (Slider s : sliders) {
      s.mouseReleased();
    }
  }

  // ── Config sync ────────────────────────────────────────────────────

  private void syncFromConfig() {
    speedSlider.setValue(config.getWaveSpeed());
    brightMinSlider.setValue(config.getBrightnessMin());
    brightMaxSlider.setValue(config.getBrightnessMax());
    satMinSlider.setValue(config.getSaturationMin());
    satMaxSlider.setValue(config.getSaturationMax());
    hueMinSlider.setValue(config.getHueMin());
    hueMaxSlider.setValue(config.getHueMax());
    textScaleSlider.setValue(config.getTextScale());
  }

  // ── Visibility ─────────────────────────────────────────────────────

  /** Shows or hides the panel. */
  public void setVisible(boolean visible) { this.visible = visible; }

  /** Toggles panel visibility. */
  public void toggleVisible() { setVisible(!visible); }

  /** Checks if panel is visible. */
  public boolean isVisible() { return visible; }

  // ── Keyboard (kept as a fallback / power-user shortcut) ────────────

  /**
   * Handles keyboard shortcuts for parameter adjustment.
   *
   * <p>Key map:</p>
   * <ul>
   *   <li>1/2 — Wave speed down/up</li>
   *   <li>3/4 — Brightness min down/up</li>
   *   <li>5/6 — Saturation max down/up</li>
   *   <li>7/8 — Hue range shift left/right</li>
   *   <li>9/0 — Text scale down/up</li>
   *   <li>H   — Toggle panel</li>
   * </ul>
   *
   * @param key the key pressed
   */
  public void handleKeyPress(char key) {
    switch (key) {
      case '1':
        config.setWaveSpeed(Math.max(0.1f, config.getWaveSpeed() - 0.2f));
        break;
      case '2':
        config.setWaveSpeed(config.getWaveSpeed() + 0.2f);
        break;
      case '3':
        config.setBrightnessRange(Math.max(0, config.getBrightnessMin() - 10), config.getBrightnessMax());
        break;
      case '4':
        config.setBrightnessRange(Math.min(config.getBrightnessMax(), config.getBrightnessMin() + 10), config.getBrightnessMax());
        break;
      case '5':
        config.setSaturationRange(config.getSaturationMin(), Math.max(0, config.getSaturationMax() - 10));
        break;
      case '6':
        config.setSaturationRange(config.getSaturationMin(), Math.min(255, config.getSaturationMax() + 10));
        break;
      case '7': {
        float s = -10;
        config.setHueRange(Math.max(0, config.getHueMin() + s), Math.max(0, config.getHueMax() + s));
        break;
      }
      case '8': {
        float s = 10;
        config.setHueRange(Math.min(360, config.getHueMin() + s), Math.min(360, config.getHueMax() + s));
        break;
      }
      case '9':
        config.setTextScale(Math.max(0.1f, config.getTextScale() - 0.05f));
        break;
      case '0':
        config.setTextScale(Math.min(1.0f, config.getTextScale() + 0.05f));
        break;
      case 'h': case 'H':
        toggleVisible();
        break;
    }
  }

  // ── Lifecycle ──────────────────────────────────────────────────────

  /**
   * Disposes of the control panel. No-op for built-in controls.
   */
  public void dispose() {
    // Nothing to clean up — all self-contained
  }

  /**
   * @deprecated No longer needed — ControlP5 dependency removed.
   */
  @Deprecated
  public void update() { }
}
