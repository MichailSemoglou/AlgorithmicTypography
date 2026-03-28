/**
 * GridRenderer - Handles rendering of typography grids.
 * 
 * This class separates rendering logic from the main typography system,
 * enabling offscreen rendering to PGraphics buffers and supporting
 * multiple output formats including raster and vector.
 * 
 * @author Michail Semoglou
 * @version 0.3.0
 */

package algorithmic.typography.render;

import processing.core.*;
import algorithmic.typography.Configuration;
import algorithmic.typography.core.WaveEngine;

/**
 * GridRenderer handles the visual rendering of typography grids.
 * 
 * <p>The renderer supports both direct screen output and offscreen
 * rendering to PGraphics buffers. It works with the WaveEngine to
 * calculate colors and the Configuration for styling parameters.</p>
 */
public class GridRenderer {
  
  private final PApplet parent;
  private final Configuration config;
  private final WaveEngine waveEngine;
  
  // Rendering buffer for offscreen mode
  private PGraphics buffer;
  private boolean useBuffer = false;
  
  // Current rendering target
  private PGraphics currentTarget;
  
  /**
   * Creates a new GridRenderer.
   * 
   * @param parent the parent PApplet
   * @param config the configuration
   * @param waveEngine the wave engine for color calculations
   */
  public GridRenderer(PApplet parent, Configuration config, WaveEngine waveEngine) {
    this.parent = parent;
    this.config = config;
    this.waveEngine = waveEngine;
    this.currentTarget = null;
  }
  
  /**
   * Creates an offscreen buffer for rendering.
   * 
   * @param width buffer width
   * @param height buffer height
   * @param renderer renderer type (P2D, P3D, or JAVA2D)
   */
  public void createBuffer(int width, int height, String renderer) {
    buffer = parent.createGraphics(width, height, renderer);
    useBuffer = true;
    currentTarget = buffer;
  }
  
  /**
   * Creates an offscreen buffer with default JAVA2D renderer.
   * 
   * @param width buffer width
   * @param height buffer height
   */
  public void createBuffer(int width, int height) {
    createBuffer(width, height, PApplet.JAVA2D);
  }
  
  /**
   * Renders the grid to the current target (screen or buffer).
   * 
   * @param tilesX number of horizontal tiles
   * @param tilesY number of vertical tiles
   * @param frameCount current frame number
   */
  public void render(float tilesX, float tilesY, int frameCount) {
    PGraphics target = getTarget();
    
    float tileW = (float)target.width / tilesX;
    float tileH = (float)target.height / tilesY;
    
    // Update wave engine for this frame
    waveEngine.update(frameCount, config.getWaveSpeed());
    
    float textSz = Math.min(tileW, tileH) * config.getTextScale();
    String ch = config.getCharacter();
    
    boolean offscreen = (useBuffer && buffer != null);
    if (offscreen) target.beginDraw();
    target.background(config.getBackgroundRed(), config.getBackgroundGreen(), config.getBackgroundBlue());
    target.noStroke();
    target.textSize(textSz);
    target.textAlign(PApplet.CENTER, PApplet.CENTER);

    int   borderSides     = config.getCellBorderSides();
    int   borderR         = config.getCellBorderRed();
    int   borderG         = config.getCellBorderGreen();
    int   borderB         = config.getCellBorderBlue();
    float borderWeight    = config.getCellBorderWeight();
    int   borderColorMode = config.getCellBorderColorMode();

    boolean useHSB = config.getHueMin() != config.getHueMax();
    if (useHSB) {
      target.colorMode(PApplet.HSB, 360, 255, 255);
    }
    
    for (int x = 0; x < tilesX; x++) {
      for (int y = 0; y < tilesY; y++) {
        float waveH = 0, waveS = 0, waveVal = 0;
        if (useHSB) {
          waveH   = waveEngine.calculateHue(frameCount, x, y, tilesX, tilesY);
          waveS   = waveEngine.calculateSaturation(frameCount, x, y, tilesX, tilesY);
          waveVal = waveEngine.calculateColorCustom(frameCount, x, y, tilesX, tilesY);
          target.fill(waveH, waveS, waveVal);
        } else {
          waveVal = waveEngine.calculateColorCustom(frameCount, x, y, tilesX, tilesY);
          target.fill(waveVal);
        }
        target.text(ch, x * tileW + tileW / 2, y * tileH + tileH / 2);
        if (borderSides != 0) {
          target.strokeWeight(borderWeight);
          if (borderColorMode == Configuration.BORDER_COLOR_WAVE) {
            if (useHSB) {
              target.stroke(waveH, waveS, waveVal * 0.5f);
            } else {
              target.stroke(waveVal * 0.5f);
            }
          } else {
            if (useHSB) target.colorMode(PApplet.RGB, 255);
            target.stroke(borderR, borderG, borderB);
            if (useHSB) target.colorMode(PApplet.HSB, 360, 255, 255);
          }
          float bx0 = x * tileW,      by0 = y * tileH;
          float bx1 = bx0 + tileW,    by1 = by0 + tileH;
          if ((borderSides & Configuration.BORDER_TOP)    != 0) target.line(bx0, by0, bx1, by0);
          if ((borderSides & Configuration.BORDER_BOTTOM) != 0) target.line(bx0, by1, bx1, by1);
          if ((borderSides & Configuration.BORDER_LEFT)   != 0) target.line(bx0, by0, bx0, by1);
          if ((borderSides & Configuration.BORDER_RIGHT)  != 0) target.line(bx1, by0, bx1, by1);
          target.noStroke();
        }
      }
    }
    
    if (useHSB) {
      target.colorMode(PApplet.RGB, 255);
    }
    if (offscreen) target.endDraw();
  }
  
  /**
   * Renders directly to the specified PGraphics target.
   * 
   * @param target the PGraphics to render to
   * @param tilesX number of horizontal tiles
   * @param tilesY number of vertical tiles
   * @param frameCount current frame number
   */
  public void renderTo(PGraphics target, float tilesX, float tilesY, int frameCount) {
    PGraphics prevTarget = currentTarget;
    currentTarget = target;
    
    float tileW = (float)target.width / tilesX;
    float tileH = (float)target.height / tilesY;
    
    waveEngine.update(frameCount, config.getWaveSpeed());
    
    float textSz = Math.min(tileW, tileH) * config.getTextScale();
    String ch = config.getCharacter();
    
    target.beginDraw();
    target.background(config.getBackgroundRed(), config.getBackgroundGreen(), config.getBackgroundBlue());
    target.noStroke();
    target.textSize(textSz);
    target.textAlign(PApplet.CENTER, PApplet.CENTER);

    int   borderSides     = config.getCellBorderSides();
    int   borderR         = config.getCellBorderRed();
    int   borderG         = config.getCellBorderGreen();
    int   borderB         = config.getCellBorderBlue();
    float borderWeight    = config.getCellBorderWeight();
    int   borderColorMode = config.getCellBorderColorMode();
    
    boolean useHSB = config.getHueMin() != config.getHueMax();
    if (useHSB) {
      target.colorMode(PApplet.HSB, 360, 255, 255);
    }
    
    for (int x = 0; x < tilesX; x++) {
      for (int y = 0; y < tilesY; y++) {
        float waveH = 0, waveS = 0, waveVal = 0;
        if (useHSB) {
          waveH   = waveEngine.calculateHue(frameCount, x, y, tilesX, tilesY);
          waveS   = waveEngine.calculateSaturation(frameCount, x, y, tilesX, tilesY);
          waveVal = waveEngine.calculateColorCustom(frameCount, x, y, tilesX, tilesY);
          target.fill(waveH, waveS, waveVal);
        } else {
          waveVal = waveEngine.calculateColorCustom(frameCount, x, y, tilesX, tilesY);
          target.fill(waveVal);
        }
        target.text(ch, x * tileW + tileW / 2, y * tileH + tileH / 2);
        if (borderSides != 0) {
          target.strokeWeight(borderWeight);
          if (borderColorMode == Configuration.BORDER_COLOR_WAVE) {
            if (useHSB) {
              target.stroke(waveH, waveS, waveVal * 0.5f);
            } else {
              target.stroke(waveVal * 0.5f);
            }
          } else {
            if (useHSB) target.colorMode(PApplet.RGB, 255);
            target.stroke(borderR, borderG, borderB);
            if (useHSB) target.colorMode(PApplet.HSB, 360, 255, 255);
          }
          float bx0 = x * tileW,      by0 = y * tileH;
          float bx1 = bx0 + tileW,    by1 = by0 + tileH;
          if ((borderSides & Configuration.BORDER_TOP)    != 0) target.line(bx0, by0, bx1, by0);
          if ((borderSides & Configuration.BORDER_BOTTOM) != 0) target.line(bx0, by1, bx1, by1);
          if ((borderSides & Configuration.BORDER_LEFT)   != 0) target.line(bx0, by0, bx0, by1);
          if ((borderSides & Configuration.BORDER_RIGHT)  != 0) target.line(bx1, by0, bx1, by1);
          target.noStroke();
        }
      }
    }
    
    if (useHSB) {
      target.colorMode(PApplet.RGB, 255);
    }
    target.endDraw();
    
    currentTarget = prevTarget;
  }
  
  /**
   * Displays the buffer to the screen at the specified position.
   * 
   * @param x x position
   * @param y y position
   */
  public void displayBuffer(float x, float y) {
    if (buffer != null && useBuffer) {
      parent.image(buffer, x, y);
    }
  }
  
  /**
   * Displays the buffer to the screen at (0, 0).
   */
  public void displayBuffer() {
    displayBuffer(0, 0);
  }
  
  /**
   * Gets the current rendering target.
   * 
   * @return PGraphics buffer if using buffer, null for direct rendering
   */
  public PGraphics getBuffer() {
    return buffer;
  }
  
  /**
   * Checks if using offscreen buffer mode.
   * 
   * @return true if rendering to buffer
   */
  public boolean isUsingBuffer() {
    return useBuffer;
  }
  
  /**
   * Sets whether to use the offscreen buffer.
   * 
   * @param use true to render to buffer, false for direct rendering
   */
  public void setUseBuffer(boolean use) {
    this.useBuffer = use;
    if (use && buffer != null) {
      currentTarget = buffer;
    } else {
      currentTarget = null;
    }
  }
  
  /**
   * Disposes of the buffer to free memory.
   */
  public void disposeBuffer() {
    if (buffer != null) {
      buffer.dispose();
      buffer = null;
      useBuffer = false;
      currentTarget = null;
    }
  }
  
  /**
   * Gets the rendering target.
   * 
   * <p>Returns the offscreen buffer if buffer mode is active, otherwise
   * returns the parent PApplet's main graphics context for direct
   * on-screen rendering.</p>
   * 
   * @return the PGraphics target (never null)
   */
  private PGraphics getTarget() {
    if (useBuffer && buffer != null) {
      return buffer;
    }
    return parent.g;
  }
}
