/**
 * HeadlessRenderer - Batch rendering without display.
 * 
 * This class enables rendering animation frames without a display,
 * useful for server-side batch processing and automated pipelines.
 * Uses offscreen PGraphics for pure software rendering.
 * 
 * @author Michail Semoglou
 * @version 1.0.0
 * @since 1.0.0
 */

package algorithmic.typography;

import processing.core.*;
import algorithmic.typography.core.*;
import algorithmic.typography.render.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HeadlessRenderer for batch mode rendering.
 * 
 * <p>Usage:</p>
 * <pre>
 * HeadlessRenderer renderer = new HeadlessRenderer(this, config);
 * renderer.renderSequence(100, "output/frame_####.png");
 * </pre>
 * 
 * <p><strong>Note:</strong> Headless mode requires a PApplet context for PGraphics creation.
 * For truly headless operation, use the Processing command-line mode.</p>
 */
public class HeadlessRenderer {
  
  private final PApplet parent;
  private Configuration config;
  private WaveEngine waveEngine;
  private ExportController exporter;
  
  private PGraphics buffer;
  private boolean initialized = false;
  
  /**
   * Creates a headless renderer.
   * 
   * @param parent the parent PApplet (required for PGraphics creation)
   * @param config the configuration
   * @throws IllegalArgumentException if parent or config is null
   */
  public HeadlessRenderer(PApplet parent, Configuration config) {
    if (parent == null) {
      throw new IllegalArgumentException("Parent PApplet cannot be null. HeadlessRenderer requires a PApplet context for PGraphics creation.");
    }
    if (config == null) {
      throw new IllegalArgumentException("Configuration cannot be null");
    }
    this.parent = parent;
    this.config = config;
    this.waveEngine = new WaveEngine(config);
  }
  
  /**
   * Initializes the offscreen buffer.
   * 
   * @throws IllegalStateException if buffer creation fails
   */
  public void initialize() {
    if (initialized) return;
    
    try {
      // Create PGraphics using parent's createGraphics - the proper Processing way
      buffer = parent.createGraphics(config.getCanvasWidth(), config.getCanvasHeight(), PApplet.JAVA2D);
      
      if (buffer == null) {
        throw new IllegalStateException("Failed to create PGraphics buffer. Ensure PApplet context is valid.");
      }
      
      // Initialize exporter with parent reference
      exporter = new ExportController(parent, config);
      exporter.setAsync(false); // Synchronous for batch
      
      initialized = true;
    } catch (Exception e) {
      throw new IllegalStateException("Failed to initialize headless renderer: " + e.getMessage(), e);
    }
  }
  
  /**
   * Renders a single frame.
   * 
   * @param frameNumber the frame to render
   * @return the rendered PGraphics
   * @throws IllegalStateException if renderer is not initialized
   */
  public PGraphics renderFrame(int frameNumber) {
    if (!initialized) {
      initialize();
    }
    
    if (buffer == null) {
      throw new IllegalStateException("Buffer not initialized. Call initialize() first.");
    }
    
    buffer.beginDraw();
    buffer.background(0);
    
    float tilesX = config.getInitialTilesX();
    float tilesY = config.getInitialTilesY();
    float tileW = (float)buffer.width / tilesX;
    float tileH = (float)buffer.height / tilesY;
    
    waveEngine.update(frameNumber, config.getWaveSpeed());
    
    float textSz = Math.min(tileW, tileH) * config.getTextScale();
    String ch = config.getCharacter();
    buffer.textSize(textSz);
    buffer.textAlign(PApplet.CENTER, PApplet.CENTER);
    
    for (int x = 0; x < tilesX; x++) {
      for (int y = 0; y < tilesY; y++) {
        float c = waveEngine.calculateColor(frameNumber, x, y, waveEngine.calculateAmplitude(x, y));
        buffer.fill(c);
        buffer.text(ch, x * tileW + tileW / 2, y * tileH + tileH / 2);
      }
    }
    
    buffer.endDraw();
    return buffer;
  }
  
  /**
   * Renders a sequence of frames to files.
   * 
   * @param totalFrames number of frames to render
   * @param outputPath output path pattern (e.g., "frames/frame_####.png")
   * @throws IllegalArgumentException if totalFrames is negative or outputPath is null
   * @throws IllegalStateException if rendering fails
   */
  public void renderSequence(int totalFrames, String outputPath) {
    if (totalFrames < 0) {
      throw new IllegalArgumentException("totalFrames must be non-negative, got: " + totalFrames);
    }
    if (outputPath == null || outputPath.isEmpty()) {
      throw new IllegalArgumentException("outputPath cannot be null or empty");
    }
    
    System.out.println("Starting batch render: " + totalFrames + " frames");
    System.out.println("Output pattern: " + outputPath);
    
    int successCount = 0;
    int failCount = 0;
    
    // Convert Processing-style #### padding to String.format %0Nd
    String formatPattern = Pattern.compile("#{2,}").matcher(outputPath)
        .replaceAll(m -> "%0" + m.group().length() + "d");
    
    for (int i = 0; i < totalFrames; i++) {
      try {
        PGraphics frame = renderFrame(i);
        
        String filename = String.format(formatPattern, i);
        frame.save(filename);
        successCount++;
        
        // Progress
        if (i % 10 == 0 || i == totalFrames - 1) {
          System.out.println("Rendered: " + (i + 1) + "/" + totalFrames);
        }
      } catch (Exception e) {
        failCount++;
        System.err.println("Failed to render frame " + i + ": " + e.getMessage());
        // Continue with next frame rather than failing entire batch
      }
    }
    
    System.out.println("Batch render complete! Success: " + successCount + ", Failed: " + failCount);
  }
  
  /**
   * Renders with a custom wave function.
   * 
   * @param function the custom wave function to use
   * @param totalFrames number of frames to render
   * @param outputPath output path pattern
   * @throws IllegalArgumentException if any parameter is invalid
   */
  public void renderWithWaveFunction(WaveFunction function, int totalFrames, String outputPath) {
    if (function == null) {
      throw new IllegalArgumentException("WaveFunction cannot be null");
    }
    waveEngine.setCustomWaveFunction(function);
    renderSequence(totalFrames, outputPath);
  }
  
  /**
   * Checks if the renderer has been initialized.
   * 
   * @return true if initialized
   */
  public boolean isInitialized() {
    return initialized;
  }
  
  /**
   * Gets the current buffer, or null if not initialized.
   * 
   * @return the PGraphics buffer
   */
  public PGraphics getBuffer() {
    return buffer;
  }
  
  /**
   * Disposes resources.
   */
  public void dispose() {
    if (buffer != null) {
      buffer.dispose();
      buffer = null;
    }
    if (exporter != null) {
      exporter.dispose();
    }
    initialized = false;
  }
}
