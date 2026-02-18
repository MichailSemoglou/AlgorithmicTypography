/**
 * Algorithmic Typography Library for Processing
 *
 * A library for creating algorithmic typography animations using mathematical
 * wave functions. This library enables designers, researchers, and artists to
 * explore parametric typography systems with configurable parameters.
 *
 * @author Michail Semoglou
 * @version 1.0.0
 * @since 1.0.0
 */

package algorithmic.typography;

import processing.core.*;
import processing.data.*;
import algorithmic.typography.core.WaveEngine;
import algorithmic.typography.core.WaveFunction;
import algorithmic.typography.core.WavePresets;
import algorithmic.typography.system.DesignSystem;
import algorithmic.typography.system.VibePreset;

/**
 * The main class for the Algorithmic Typography library.
 * 
 * This class provides a configurable system for rendering animated grids of
 * typographic elements where character colors and arrangements are driven by
 * mathematical functions (trigonometric, wave-based transformations).
 * 
 * <h2>Usage Modes</h2>
 * 
 * <h3>Automatic Mode (Recommended)</h3>
 * <p>The library automatically registers for Processing's draw cycle. Simply create
 * the instance and configure it - rendering happens automatically.</p>
 * <pre>
 * AlgorithmicTypography at;
 * 
 * void setup() {
 *   size(1080, 1080);  // Set your canvas size first
 *   at = new AlgorithmicTypography(this);
 *   at.loadConfiguration("config.json");
 *   // Rendering happens automatically in draw()
 * }
 * </pre>
 * 
 * <h3>Manual Mode</h3>
 * <p>For full control over when rendering occurs, disable auto-render:</p>
 * <pre>
 * void setup() {
 *   at = new AlgorithmicTypography(this);
 *   at.setAutoRender(false);  // Disable automatic rendering
 *   at.loadConfiguration("config.json");
 * }
 * 
 * void draw() {
 *   background(0);  // Custom background
 *   at.render();    // Manual render call
 *   // Add custom overlays here
 * }
 * </pre>
 * 
 * <h2>Renderer Compatibility</h2>
 * <p>This library works with all Processing renderers:</p>
 * <ul>
 *   <li><b>JAVA2D</b> (default) - Best compatibility</li>
 *   <li><b>P2D</b> - Hardware accelerated 2D, recommended for large grids</li>
 *   <li><b>P3D</b> - Full 3D support, works but uses 2D projection</li>
 * </ul>
 * <p>Note: When using P2D or P3D, call size() with the renderer BEFORE creating
 * the AlgorithmicTypography instance:</p>
 * <pre>
 * void setup() {
 *   size(1080, 1080, P2D);  // Set renderer first
 *   at = new AlgorithmicTypography(this);
 * }
 * </pre>
 * 
 * @author Michail Semoglou
 * @version 1.0.0
 * @see Configuration
 * @see WaveEngine
 */
public class AlgorithmicTypography {
  
  // Parent Processing applet
  private final PApplet parent;
  
  // Configuration
  private Configuration config;
  
  // Wave engine for mathematical calculations
  private WaveEngine waveEngine;
  
  // Animation state
  private int startTime;
  private boolean designChanged = false;
  private boolean changeMessagePrinted = false;
  private boolean secondChangeMessagePrinted = false;
  private int currentStage = 1;
  private int frameCounter = 0;
  private boolean isRunning = true;
  
  // Auto-render mode
  private boolean autoRender = true;
  private boolean preRegistered = false;
  
  // Frame export
  private String framesSubdir;
  private boolean framesDirectoryCreated = false;
  
  /**
   * Constructs a new AlgorithmicTypography instance with auto-render enabled.
   * 
   * <p>By default, the library registers for Processing's draw cycle and renders
   * automatically. To disable this behavior, call {@link #setAutoRender(boolean)}
   * before the first draw cycle.</p>
   * 
   * @param parent the parent PApplet (typically 'this' in the main sketch)
   * @throws IllegalArgumentException if parent is null
   */
  public AlgorithmicTypography(PApplet parent) {
    if (parent == null) {
      throw new IllegalArgumentException("Parent PApplet cannot be null - pass 'this' from your sketch");
    }
    this.parent = parent;
    this.config = new Configuration();
    this.waveEngine = new WaveEngine(config);
    this.startTime = parent.millis();
    
    // Register for Processing lifecycle callbacks
    parent.registerMethod("pre", this);
    parent.registerMethod("draw", this);
    parent.registerMethod("dispose", this);
    preRegistered = true;
    
    // Log renderer info for debugging
    logRendererInfo();
  }
  
  /**
   * Logs renderer information for debugging P2D/P3D compatibility.
   */
  private void logRendererInfo() {
    String renderer = parent.g.getClass().getSimpleName();
    parent.println("AlgorithmicTypography initialized with " + renderer + " renderer");
    if (renderer.contains("PGraphics2D")) {
      parent.println("  (JAVA2D-compatible mode)");
    } else if (renderer.contains("PGraphicsJava2D")) {
      parent.println("  (Standard JAVA2D mode)");
    } else if (renderer.contains("PGraphicsOpenGL") || renderer.contains("P2D") || renderer.contains("P3D")) {
      parent.println("  (OpenGL mode - hardware accelerated)");
    }
  }
  
  /**
   * Loads configuration from a JSON file.
   * 
   * The configuration file should follow the schema defined in the
   * documentation. If the file cannot be loaded, default values are used
   * and a warning is printed to the console.
   * 
   * @param filename the path to the JSON configuration file
   * @return this instance for method chaining
   * @see Configuration
   */
  public AlgorithmicTypography loadConfiguration(String filename) {
    try {
      JSONObject json = parent.loadJSONObject(filename);
      if (json != null) {
        config.loadFromJSON(json);
        parent.println("Configuration loaded successfully from " + filename);
      } else {
        parent.println("Warning: Could not load configuration file " + filename + ". Using defaults.");
      }
    } catch (Exception e) {
      parent.println("Error loading configuration: " + e.getMessage());
      parent.println("Using default configuration values");
    }
    return this;
  }
  
  /**
   * Sets the configuration directly.
   * 
   * @param config the Configuration object to use
   * @return this instance for method chaining
   * @throws IllegalArgumentException if config is null
   */
  public AlgorithmicTypography setConfiguration(Configuration config) {
    if (config == null) {
      throw new IllegalArgumentException("Configuration cannot be null");
    }
    this.config = config;
    this.waveEngine = new WaveEngine(config);
    return this;
  }
  
  /**
   * Gets the current configuration.
   * 
   * @return the current Configuration object
   */
  public Configuration getConfiguration() {
    return config;
  }
  
  /**
   * Gets the WaveEngine for advanced wave function control.
   * 
   * <p>Use this to set custom wave functions or access wave parameters:</p>
   * <pre>
   * at.getWaveEngine().setCustomWaveFunction(myWaveFunction);
   * </pre>
   * 
   * @return the WaveEngine instance
   */
  public WaveEngine getWaveEngine() {
    return waveEngine;
  }
  
  /**
   * Sets a custom wave function for color calculations.
   * 
   * @param function the custom WaveFunction implementation, or null for default
   * @return this instance for method chaining
   * @see WaveFunction
   */
  public AlgorithmicTypography setWaveFunction(WaveFunction function) {
    waveEngine.setCustomWaveFunction(function);
    return this;
  }
  
  /**
   * Sets the wave type to one of the built-in mathematical presets.
   * 
   * <p>Available types: SINE, TANGENT, SQUARE, TRIANGLE, SAWTOOTH.
   * Use {@link #setWaveFunction(WaveFunction)} for custom or Perlin waves.</p>
   * 
   * <pre>
   * at.setWaveType(WavePresets.Type.SINE);
   * </pre>
   * 
   * @param type the wave preset type
   * @return this instance for method chaining
   * @see WavePresets
   */
  public AlgorithmicTypography setWaveType(WavePresets.Type type) {
    waveEngine.setCustomWaveFunction(WavePresets.get(type));
    return this;
  }
  
  /**
   * Applies a cultural design system preset to the configuration.
   * 
   * @param type the design system type (e.g., DesignSystem.SystemType.SWISS)
   * @return this instance for method chaining
   * @see DesignSystem
   */
  public AlgorithmicTypography useDesignSystem(DesignSystem.SystemType type) {
    DesignSystem.apply(config, type);
    return this;
  }
  
  /**
   * Applies a natural-language vibe preset to the configuration.
   * 
   * <p>Multiple keywords can be combined for blended effects:</p>
   * <pre>
   * at.setVibe("calm zen morning");
   * at.setVibe("chaos glitch digital");
   * </pre>
   * 
   * @param vibe the vibe description string
   * @return this instance for method chaining
   * @see VibePreset
   */
  public AlgorithmicTypography setVibe(String vibe) {
    VibePreset.apply(config, vibe);
    return this;
  }
  
  /**
   * Initializes the animation system.
   * 
   * This method should be called once in setup() after loading configuration.
   * It sets up the canvas size, frame rate, and initializes frame export
   * directories.
   * 
   * @return this instance for method chaining
   */
  public AlgorithmicTypography initialize() {
    parent.frameRate(config.getAnimationFPS());
    startTime = parent.millis();
    frameCounter = 0;
    
    // Only create frames directory when saving is enabled
    if (config.isSaveFrames()) {
      framesSubdir = "frames/" + parent.nf(parent.year(), 4) + parent.nf(parent.month(), 2) + parent.nf(parent.day(), 2) + "_" + 
                     parent.nf(parent.hour(), 2) + parent.nf(parent.minute(), 2) + parent.nf(parent.second(), 2);
      createFramesDirectory();
    }
    isRunning = true;
    
    return this;
  }
  
  /**
   * Renders one frame of the animation.
   * 
   * This method should be called in the draw() loop. It handles:
   * - Background clearing
   * - Grid state updates based on elapsed time
   * - Grid rendering with wave functions
   * - Frame export (if enabled)
   * - Animation completion checking
   */
  public void render() {
    if (!isRunning) {
      return;
    }
    
    parent.background(0);
    
    // Update wave engine once per frame (before any grid drawing)
    waveEngine.update(parent.frameCount, config.getWaveSpeed());
    
    int elapsedTime = parent.millis() - startTime;
    int changeTime = config.getChangeTime();
    int secondChangeTime = config.getSecondChangeTime();
    int fadeDuration = config.getFadeDuration();
    boolean hasThirdStage = secondChangeTime > 0;
    
    if (fadeDuration <= 0) {
      // Instant transitions (backward-compatible snap behaviour)
      if (hasThirdStage && elapsedTime > secondChangeTime) {
        logStageTransition(3, elapsedTime);
        drawGrid(config.getFinalTilesX(), config.getFinalTilesY(), 255);
      } else if (elapsedTime > changeTime) {
        logStageTransition(2, elapsedTime);
        drawGrid(config.getChangedTilesX(), config.getChangedTilesY(), 255);
      } else {
        currentStage = 1;
        drawGrid(config.getInitialTilesX(), config.getInitialTilesY(), 255);
      }
    } else {
      // Smooth fade transitions
      if (hasThirdStage && elapsedTime > secondChangeTime + fadeDuration) {
        // Stage 3 — fully visible
        logStageTransition(3, elapsedTime);
        drawGrid(config.getFinalTilesX(), config.getFinalTilesY(), 255);
      } else if (hasThirdStage && elapsedTime > secondChangeTime) {
        // Cross-fading from stage 2 to stage 3
        float t = smoothStep((float)(elapsedTime - secondChangeTime) / fadeDuration);
        drawGrid(config.getChangedTilesX(), config.getChangedTilesY(), 255 * (1 - t));
        drawGrid(config.getFinalTilesX(), config.getFinalTilesY(), 255 * t);
      } else if (elapsedTime > changeTime + fadeDuration) {
        // Stage 2 — fully visible
        logStageTransition(2, elapsedTime);
        drawGrid(config.getChangedTilesX(), config.getChangedTilesY(), 255);
      } else if (elapsedTime > changeTime) {
        // Cross-fading from stage 1 to stage 2
        float t = smoothStep((float)(elapsedTime - changeTime) / fadeDuration);
        drawGrid(config.getInitialTilesX(), config.getInitialTilesY(), 255 * (1 - t));
        drawGrid(config.getChangedTilesX(), config.getChangedTilesY(), 255 * t);
      } else {
        // Stage 1 — fully visible
        currentStage = 1;
        drawGrid(config.getInitialTilesX(), config.getInitialTilesY(), 255);
      }
    }
    
    // Save each frame as an image if enabled
    if (config.isSaveFrames()) {
      saveCurrentFrame();
    }
    
    frameCounter++;
    
    // Only auto-stop when saving frames (interactive sketches run indefinitely)
    if (config.isSaveFrames() && frameCounter >= (config.getAnimationFPS() * config.getAnimationDuration())) {
      parent.println("Finished saving " + frameCounter + " frames to " + framesSubdir);
      isRunning = false;
      parent.noLoop();
    }
  }
  
  /**
   * Renders the grid at a specific position and size without clearing the background.
   * 
   * <p>Use this method to place multiple typography systems side by side
   * on the same canvas:</p>
   * <pre>
   * at1.setAutoRender(false);
   * at2.setAutoRender(false);
   * 
   * void draw() {
   *   background(0);
   *   at1.renderAt(0, 0, width/2, height);
   *   at2.renderAt(width/2, 0, width/2, height);
   * }
   * </pre>
   * 
   * @param x the x position of the top-left corner
   * @param y the y position of the top-left corner
   * @param w the width of the rendering area
   * @param h the height of the rendering area
   */
  public void renderAt(float x, float y, float w, float h) {
    if (!isRunning) {
      return;
    }
    
    // Update wave engine once per frame
    waveEngine.update(parent.frameCount, config.getWaveSpeed());
    
    int elapsedTime = parent.millis() - startTime;
    int changeTime = config.getChangeTime();
    int secondChangeTime = config.getSecondChangeTime();
    int fadeDuration = config.getFadeDuration();
    boolean hasThirdStage = secondChangeTime > 0;
    
    if (fadeDuration <= 0) {
      // Instant transitions
      if (hasThirdStage && elapsedTime > secondChangeTime) {
        drawGridAt(x, y, w, h, config.getFinalTilesX(), config.getFinalTilesY(), 255);
      } else if (elapsedTime > changeTime) {
        drawGridAt(x, y, w, h, config.getChangedTilesX(), config.getChangedTilesY(), 255);
      } else {
        drawGridAt(x, y, w, h, config.getInitialTilesX(), config.getInitialTilesY(), 255);
      }
    } else {
      // Smooth fade transitions
      if (hasThirdStage && elapsedTime > secondChangeTime + fadeDuration) {
        drawGridAt(x, y, w, h, config.getFinalTilesX(), config.getFinalTilesY(), 255);
      } else if (hasThirdStage && elapsedTime > secondChangeTime) {
        float t = smoothStep((float)(elapsedTime - secondChangeTime) / fadeDuration);
        drawGridAt(x, y, w, h, config.getChangedTilesX(), config.getChangedTilesY(), 255 * (1 - t));
        drawGridAt(x, y, w, h, config.getFinalTilesX(), config.getFinalTilesY(), 255 * t);
      } else if (elapsedTime > changeTime + fadeDuration) {
        drawGridAt(x, y, w, h, config.getChangedTilesX(), config.getChangedTilesY(), 255);
      } else if (elapsedTime > changeTime) {
        float t = smoothStep((float)(elapsedTime - changeTime) / fadeDuration);
        drawGridAt(x, y, w, h, config.getInitialTilesX(), config.getInitialTilesY(), 255 * (1 - t));
        drawGridAt(x, y, w, h, config.getChangedTilesX(), config.getChangedTilesY(), 255 * t);
      } else {
        drawGridAt(x, y, w, h, config.getInitialTilesX(), config.getInitialTilesY(), 255);
      }
    }
    
    frameCounter++;
  }
  
  /**
   * Draws the grid of characters with mathematical wave patterns.
   * 
   * <p>This method renders a grid where each cell contains the configured
   * character, with color values determined by the {@link WaveEngine}.
   * Custom wave functions can be plugged in via
   * {@link #setWaveFunction(WaveFunction)}.</p>
   * 
   * @param tilesX the number of horizontal tiles
   * @param tilesY the number of vertical tiles
   * @param alpha  the opacity (0 = transparent, 255 = fully opaque)
   */
  private void drawGrid(float tilesX, float tilesY, float alpha) {
    float tileW = (float)parent.width / tilesX;
    float tileH = (float)parent.height / tilesY;
    
    // Switch to HSB colour mode with alpha channel
    parent.colorMode(PApplet.HSB, 360, 255, 255, 255);
    
    // Set text properties once before the loop (R2: avoid per-cell overhead)
    float textSize = Math.min(tileW, tileH) * config.getTextScale();
    parent.textSize(textSize);
    parent.textAlign(PApplet.CENTER, PApplet.CENTER);
    String ch = config.getCharacter();
    
    float clampedAlpha = Math.max(0, Math.min(255, alpha));
    
    for (int x = 0; x < tilesX; x++) {
      for (int y = 0; y < tilesY; y++) {
        float h = waveEngine.calculateHue(parent.frameCount, x, y, tilesX, tilesY);
        float s = waveEngine.calculateSaturation(parent.frameCount, x, y, tilesX, tilesY);
        float b = waveEngine.calculateColorCustom(parent.frameCount, x, y, tilesX, tilesY);
        parent.fill(h, s, b, clampedAlpha);
        parent.text(ch, x * tileW + tileW / 2, y * tileH + tileH / 2);
      }
    }
    
    // Restore default colour mode
    parent.colorMode(PApplet.RGB, 255);
  }
  
  /**
   * Draws the grid at a specific position and size.
   * 
   * @param ox the x offset
   * @param oy the y offset
   * @param w  the width of the rendering area
   * @param h  the height of the rendering area
   * @param tilesX the number of horizontal tiles
   * @param tilesY the number of vertical tiles
   * @param alpha  the opacity (0 = transparent, 255 = fully opaque)
   */
  private void drawGridAt(float ox, float oy, float w, float h, float tilesX, float tilesY, float alpha) {
    float tileW = w / tilesX;
    float tileH = h / tilesY;
    
    parent.colorMode(PApplet.HSB, 360, 255, 255, 255);
    
    float textSize = Math.min(tileW, tileH) * config.getTextScale();
    parent.textSize(textSize);
    parent.textAlign(PApplet.CENTER, PApplet.CENTER);
    String ch = config.getCharacter();
    
    float clampedAlpha = Math.max(0, Math.min(255, alpha));
    
    for (int x = 0; x < tilesX; x++) {
      for (int y = 0; y < tilesY; y++) {
        float hue = waveEngine.calculateHue(parent.frameCount, x, y, tilesX, tilesY);
        float sat = waveEngine.calculateSaturation(parent.frameCount, x, y, tilesX, tilesY);
        float bri = waveEngine.calculateColorCustom(parent.frameCount, x, y, tilesX, tilesY);
        parent.fill(hue, sat, bri, clampedAlpha);
        parent.text(ch, ox + x * tileW + tileW / 2, oy + y * tileH + tileH / 2);
      }
    }
    
    parent.colorMode(PApplet.RGB, 255);
  }
  
  /**
   * Creates the frames directory and subdirectory for export.
   * 
   * This method attempts to create the directory structure needed for
   * frame export. If creation fails, it falls back to the base frames/
   * directory.
   */
  private void createFramesDirectory() {
    java.io.File framesDir = new java.io.File(framesSubdir);
    if (!framesDir.exists()) {
      boolean dirCreated = framesDir.mkdirs();
      if (dirCreated) {
        parent.println("Frames directory created: " + framesSubdir);
        framesDirectoryCreated = true;
      } else {
        parent.println("Failed to create frames directory: " + framesSubdir);
        // Fallback to frames/
        framesSubdir = "frames";
        framesDirectoryCreated = false;
      }
    } else {
      framesDirectoryCreated = true;
    }
  }
  
  /**
   * Applies a smooth Hermite interpolation (smoothstep) to a transition value.
   * 
   * This produces a more natural-looking fade compared to linear interpolation,
   * with ease-in and ease-out at the transition boundaries.
   * 
   * @param t the raw transition progress (0.0 to 1.0, will be clamped)
   * @return the smoothed value (0.0 to 1.0)
   */
  private float smoothStep(float t) {
    t = Math.max(0, Math.min(1, t));
    return t * t * (3 - 2 * t);
  }
  
  /**
   * Logs console messages when the animation enters a new stage.
   * 
   * @param stage the stage number (2 or 3)
   * @param elapsedTime the current elapsed time in milliseconds
   */
  private void logStageTransition(int stage, int elapsedTime) {
    if (stage == 2 && !changeMessagePrinted) {
      designChanged = true;
      currentStage = 2;
      parent.println("Stage 2 (middle) at " + elapsedTime + "ms — grid: " +
          config.getChangedTilesX() + "x" + config.getChangedTilesY());
      changeMessagePrinted = true;
    } else if (stage == 3 && !secondChangeMessagePrinted) {
      currentStage = 3;
      parent.println("Stage 3 (final) at " + elapsedTime + "ms — grid: " +
          config.getFinalTilesX() + "x" + config.getFinalTilesY());
      secondChangeMessagePrinted = true;
    }
  }
  
  /**
   * Saves the current frame as a PNG image.
   * 
   * Frame files are named with sequential numbering (frame_0001.png,
   * frame_0002.png, etc.) for easy compilation into video formats.
   */
  private void saveCurrentFrame() {
    try {
      String filename = String.format("%s/frame_%04d.png", framesSubdir, frameCounter);
      parent.saveFrame(filename);
    } catch (Exception e) {
      parent.println("Error saving frame " + frameCounter + ": " + e.getMessage());
    }
  }
  
  /**
   * Restarts the animation from the beginning.
   * 
   * This resets all animation state including timing, frame counter,
   * and grid change tracking.
   */
  public void restart() {
    startTime = parent.millis();
    designChanged = false;
    changeMessagePrinted = false;
    secondChangeMessagePrinted = false;
    currentStage = 1;
    frameCounter = 0;
    isRunning = true;
    parent.loop();
    parent.println("Animation restarted");
  }
  
  /**
   * Sets whether automatic rendering is enabled.
   * 
   * <p>When auto-render is enabled (default), the library automatically calls
   * render() during Processing's draw cycle. Disable for manual control.</p>
   * 
   * @param auto true for automatic rendering, false for manual
   * @return this instance for method chaining
   */
  public AlgorithmicTypography setAutoRender(boolean auto) {
    this.autoRender = auto;
    return this;
  }
  
  /**
   * Checks if automatic rendering is enabled.
   * 
   * @return true if auto-render is enabled
   */
  public boolean isAutoRender() {
    return autoRender;
  }
  
  /**
   * Called by Processing before each draw cycle (registered via registerMethod).
   * 
   * <p>This method is called automatically by Processing. Do not call directly.</p>
   */
  public void pre() {
    // Pre-frame setup - called before draw()
    // Animation timing updates happen here
  }
  
  /**
   * Called by Processing during each draw cycle (registered via registerMethod).
   * 
   * <p>This method is called automatically by Processing when auto-render is enabled.
   * Do not call directly - use render() for manual rendering.</p>
   */
  public void draw() {
    if (autoRender) {
      render();
    }
  }
  
  /**
   * Toggles frame saving on or off.
   * 
   * @return this instance for method chaining
   */
  public AlgorithmicTypography toggleFrameSaving() {
    config.setSaveFrames(!config.isSaveFrames());
    parent.println("Frame saving: " + (config.isSaveFrames() ? "ON" : "OFF"));
    return this;
  }
  
  /**
   * Sets whether frames should be saved.
   * 
   * @param save true to enable frame saving, false to disable
   * @return this instance for method chaining
   */
  public AlgorithmicTypography setFrameSaving(boolean save) {
    config.setSaveFrames(save);
    return this;
  }
  
  /**
   * Checks if the animation is still running.
   * 
   * @return true if the animation has not reached its duration limit
   */
  public boolean isRunning() {
    return isRunning;
  }
  
  /**
   * Gets the current frame count.
   * 
   * @return the number of frames rendered since start or last restart
   */
  public int getFrameCount() {
    return frameCounter;
  }
  
  /**
   * Gets the total number of frames in the animation.
   * 
   * @return the total frame count based on duration and FPS
   */
  public int getTotalFrames() {
    return config.getAnimationFPS() * config.getAnimationDuration();
  }
  
  /**
   * Gets the progress of the animation as a value from 0.0 to 1.0.
   * 
   * @return the animation progress (0.0 = start, 1.0 = complete)
   */
  public float getProgress() {
    return (float)frameCounter / getTotalFrames();
  }
  
  /**
   * Gets the directory where frames are being saved.
   * 
   * @return the frames subdirectory path
   */
  public String getFramesDirectory() {
    return framesSubdir;
  }
  
  /**
   * Disposes of resources used by this instance.
   * 
   * This method is called automatically when the sketch is closed.
   */
  public void dispose() {
    // Cleanup if needed
    parent.println("AlgorithmicTypography disposed");
  }
}
