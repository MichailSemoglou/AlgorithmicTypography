/**
 * VectorExporter - SVG and PDF export for print-resolution output.
 * 
 * This class provides vector-based export capabilities, enabling
 * high-resolution output suitable for print and large-format display.
 * Unlike raster export, vector output scales infinitely without
 * quality loss and includes embedded metadata.
 * 
 * @author Michail Semoglou
 * @version 1.0.0
 */

package algorithmic.typography.render;

import processing.core.*;
import processing.data.*;
import algorithmic.typography.Configuration;
import algorithmic.typography.core.WaveEngine;

/**
 * VectorExporter handles SVG and PDF export.
 * 
 * <p>Features:</p>
 * <ul>
 *   <li>SVG export for web and illustration software</li>
 *   <li>PDF export for print production</li>
 *   <li>Embedded metadata (configuration, timestamp)</li>
 *   <li>Layer support for animation frames</li>
 *   <li>Resolution-independent output</li>
 * </ul>
 */
public class VectorExporter {
  
  private final PApplet parent;
  private final Configuration config;
  private final WaveEngine waveEngine;
  
  private String outputDirectory = "export/vector";
  private String format = "svg"; // "svg" or "pdf"
  private boolean recording = false;
  private int frameCount = 0;
  
  // Vector graphics context
  private Object svgGraphics; // via reflection to avoid compile dep
  private Object pdfGraphics;
  
  /**
   * Creates a new VectorExporter.
   * 
   * @param parent the parent PApplet
   * @param config the configuration
   */
  public VectorExporter(PApplet parent, Configuration config) {
    this.parent = parent;
    this.config = config;
    this.waveEngine = new WaveEngine(config);
    createOutputDirectory();
  }
  
  /**
   * Creates the output directory if needed.
   */
  private void createOutputDirectory() {
    java.io.File dir = new java.io.File(outputDirectory);
    if (!dir.exists()) {
      dir.mkdirs();
    }
  }
  
  /**
   * Starts recording vector frames.
   * 
   * @param format "svg" or "pdf"
   * @param filename base filename
   */
  public void beginRecord(String format, String filename) {
    this.format = format.toLowerCase();
    this.recording = true;
    this.frameCount = 0;
    
    try {
      if ("svg".equals(this.format)) {
        beginSVG(filename);
      } else if ("pdf".equals(this.format)) {
        beginPDF(filename);
      }
    } catch (Exception e) {
      System.err.println("Error starting vector recording: " + e.getMessage());
      recording = false;
    }
  }
  
  /**
   * Starts SVG recording.
   */
  private void beginSVG(String filename) throws Exception {
    // Processing's SVG support via PGraphicsSVG
    String fullPath = outputDirectory + "/" + filename + ".svg";
    
    // Create SVG via Processing's beginRecord
    parent.beginRecord(PApplet.SVG, fullPath);
    
    System.out.println("Recording SVG to: " + fullPath);
  }
  
  /**
   * Starts PDF recording.
   */
  private void beginPDF(String filename) throws Exception {
    String fullPath = outputDirectory + "/" + filename + ".pdf";
    
    // Processing's PDF support via PGraphicsPDF
    parent.beginRecord(PApplet.PDF, fullPath);
    
    System.out.println("Recording PDF to: " + fullPath);
  }
  
  /**
   * Ends the current recording.
   */
  public void endRecord() {
    if (!recording) return;
    
    parent.endRecord();
    recording = false;
    
    // Write metadata sidecar
    writeMetadata();
    
    System.out.println("Vector export complete. Frames: " + frameCount);
  }
  
  /**
   * Records a single frame.
   * 
   * @return true if recording is active
   */
  public boolean recordFrame() {
    if (!recording) return false;
    
    frameCount++;
    
    // For multi-frame PDF, we need to handle separately
    if ("pdf".equals(format) && frameCount > 1) {
      // PDF doesn't support multiple frames natively
      // Create separate files or use layers
      String filename = String.format("frame_%04d", frameCount);
      endRecord();
      beginRecord("pdf", filename);
    }
    
    return true;
  }
  
  /**
   * Exports a single frame to SVG/PDF.
   * 
   * @param frameNumber the frame number
   * @param format "svg" or "pdf"
   */
  public void exportFrame(int frameNumber, String format) {
    String filename = String.format("frame_%04d", frameNumber);
    beginRecord(format, filename);
    // Rendering happens in draw()
    // User calls endRecord() after draw completes
  }
  
  /**
   * Writes metadata sidecar file.
   */
  private void writeMetadata() {
    try {
      JSONObject metadata = new JSONObject();
      metadata.setString("format", format);
      metadata.setInt("frames", frameCount);
      metadata.setString("timestamp", new java.util.Date().toString());
      metadata.setJSONObject("config", config.toJSON());
      
      String metaPath = outputDirectory + "/metadata.json";
      parent.saveJSONObject(metadata, metaPath);
      
    } catch (Exception e) {
      System.err.println("Error writing metadata: " + e.getMessage());
    }
  }
  
  /**
   * Sets the output directory.
   * 
   * @param directory the output directory path
   */
  public void setOutputDirectory(String directory) {
    this.outputDirectory = directory;
    createOutputDirectory();
  }
  
  /**
   * Gets the current format.
   * 
   * @return the export format string (e.g., "svg", "pdf")
   */
  public String getFormat() {
    return format;
  }
  
  /**
   * Checks if currently recording.
   * 
   * @return true if recording is in progress
   */
  public boolean isRecording() {
    return recording;
  }
  
  /**
   * Gets the frame count.
   * 
   * @return the number of frames exported so far
   */
  public int getFrameCount() {
    return frameCount;
  }
  
  /**
   * Creates an SVG file from current configuration (one-shot export).
   * 
   * @param filename the output filename
   * @param width canvas width
   * @param height canvas height
   * @param tilesX grid tiles X
   * @param tilesY grid tiles Y
   */
  public void exportSingleSVG(String filename, int width, int height, 
                               float tilesX, float tilesY) {
    beginRecord("svg", filename);
    
    // Render single frame
    renderVectorFrame(width, height, tilesX, tilesY);
    
    endRecord();
  }
  
  /**
   * Renders a vector frame using the WaveEngine for consistent color output.
   */
  private void renderVectorFrame(int width, int height, float tilesX, float tilesY) {
    float tileW = (float)width / tilesX;
    float tileH = (float)height / tilesY;
    
    parent.background(0);
    parent.noStroke();
    
    waveEngine.update(parent.frameCount, config.getWaveSpeed());
    float textSz = Math.min(tileW, tileH) * config.getTextScale();
    String ch = config.getCharacter();
    parent.textSize(textSz);
    parent.textAlign(PApplet.CENTER, PApplet.CENTER);
    
    boolean useHSB = config.getHueMin() != config.getHueMax();
    if (useHSB) {
      parent.colorMode(PApplet.HSB, 360, 255, 255);
    }
    
    for (int x = 0; x < tilesX; x++) {
      for (int y = 0; y < tilesY; y++) {
        if (useHSB) {
          float h = waveEngine.calculateHue(parent.frameCount, x, y, tilesX, tilesY);
          float s = waveEngine.calculateSaturation(parent.frameCount, x, y, tilesX, tilesY);
          float b = waveEngine.calculateColorCustom(parent.frameCount, x, y, tilesX, tilesY);
          parent.fill(h, s, b);
        } else {
          float c = waveEngine.calculateColorCustom(parent.frameCount, x, y, tilesX, tilesY);
          parent.fill(c);
        }
        parent.text(ch, x * tileW + tileW / 2, y * tileH + tileH / 2);
      }
    }
    
    if (useHSB) {
      parent.colorMode(PApplet.RGB, 255);
    }
  }
}
