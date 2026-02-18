/**
 * ExportController - Handles frame export in multiple formats.
 * 
 * This class manages asynchronous frame export, supporting both
 * raster (PNG) and vector (SVG, PDF) formats with embedded
 * metadata for reproducibility.
 * 
 * @author Michail Semoglou
 * @version 1.0.0
 * @since 1.0.0
 */

package algorithmic.typography.render;

import processing.core.*;
import algorithmic.typography.Configuration;
import java.util.concurrent.*;
import java.util.Queue;
import java.util.LinkedList;

/**
 * ExportController manages frame export operations.
 * 
 * <p>Features:</p>
 * <ul>
 *   <li>Asynchronous export queue to prevent frame drops</li>
 *   <li>Multiple format support (PNG, SVG, PDF)</li>
 *   <li>Automatic metadata embedding</li>
 *   <li>Timestamped output directories</li>
 *   <li>Automatic resource cleanup via shutdown hooks</li>
 * </ul>
 * 
 * <p><strong>Important:</strong> This controller uses an ExecutorService that must be 
 * properly shut down. Call {@link #dispose()} when done, or the controller will 
 * automatically register a shutdown hook to clean up resources.</p>
 */
public class ExportController {
  
  private final PApplet parent;
  private final Configuration config;
  
  // Export settings
  private String outputDirectory;
  private String format = "png";
  private boolean async = true;
  private boolean enabled = true;
  
  // Async export queue
  private final Queue<ExportTask> exportQueue;
  private final ExecutorService executor;
  private final Thread shutdownHook;
  
  // Export statistics
  private int exportedCount = 0;
  private int failedCount = 0;
  
  /**
   * Creates a new ExportController.
   * 
   * @param parent the parent PApplet (must not be null)
   * @param config the configuration (must not be null)
   * @throws IllegalArgumentException if parent or config is null
   */
  public ExportController(PApplet parent, Configuration config) {
    if (parent == null) {
      throw new IllegalArgumentException("Parent PApplet cannot be null");
    }
    if (config == null) {
      throw new IllegalArgumentException("Configuration cannot be null");
    }
    this.parent = parent;
    this.config = config;
    this.exportQueue = new LinkedList<>();
    this.executor = Executors.newSingleThreadExecutor(r -> {
      Thread t = new Thread(r, "ExportController-Worker");
      t.setDaemon(true);
      return t;
    });
    
    // Register shutdown hook for automatic cleanup
    this.shutdownHook = new Thread(this::emergencyShutdown);
    Runtime.getRuntime().addShutdownHook(shutdownHook);
    
    // Create default output directory
    createTimestampedDirectory();
  }
  
  /**
   * Creates a timestamped output directory.
   * 
   * @throws IllegalStateException if directory creation fails
   */
  private void createTimestampedDirectory() {
    if (parent != null) {
      outputDirectory = "frames/" + parent.nf(parent.year(), 4) + parent.nf(parent.month(), 2) + parent.nf(parent.day(), 2) + "_" + 
                        parent.nf(parent.hour(), 2) + parent.nf(parent.minute(), 2) + parent.nf(parent.second(), 2);
    } else {
      // Fallback for headless mode
      java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss");
      outputDirectory = "frames/" + sdf.format(new java.util.Date());
    }
    
    java.io.File dir = new java.io.File(outputDirectory);
    if (!dir.exists()) {
      boolean created = dir.mkdirs();
      if (!created) {
        System.err.println("Warning: Could not create output directory: " + outputDirectory);
      }
    }
  }
  
  /**
   * Queues a frame for export.
   * 
   * @param frameNumber the frame number (must be non-negative)
   * @param graphics the PGraphics to export (null for screen capture)
   * @throws IllegalArgumentException if frameNumber is negative
   * @throws IllegalStateException if executor has been shut down
   */
  public void exportFrame(int frameNumber, PGraphics graphics) {
    if (frameNumber < 0) {
      throw new IllegalArgumentException("frameNumber must be non-negative, got: " + frameNumber);
    }
    if (!enabled) return;
    if (executor.isShutdown()) {
      throw new IllegalStateException("ExportController has been disposed and cannot accept new frames");
    }
    
    ExportTask task = new ExportTask(frameNumber, graphics, outputDirectory, format);
    
    if (async) {
      exportQueue.offer(task);
      processQueue();
    } else {
      executeExport(task);
    }
  }
  
  /**
   * Queues screen capture for export.
   * 
   * @param frameNumber the frame number
   * @throws IllegalStateException if parent PApplet is null (required for screen capture)
   */
  public void exportScreen(int frameNumber) {
    if (parent == null) {
      throw new IllegalStateException("Cannot export from screen: parent PApplet is null. " +
                                      "Use exportFrame() with a PGraphics buffer instead.");
    }
    exportFrame(frameNumber, null);
  }
  
  /**
   * Processes the export queue asynchronously.
   */
  private void processQueue() {
    while (!exportQueue.isEmpty()) {
      ExportTask task = exportQueue.poll();
      if (task != null) {
        executor.submit(() -> executeExport(task));
      }
    }
  }
  
  /**
   * Executes a single export task.
   * 
   * @param task the export task to execute
   */
  private void executeExport(ExportTask task) {
    try {
      String filename = String.format("%s/frame_%04d.%s", task.directory, task.frameNumber, task.format);
      
      if (task.graphics != null) {
        // Export from PGraphics buffer
        task.graphics.save(filename);
      } else if (parent != null) {
        // Export from screen
        parent.saveFrame(filename);
      } else {
        throw new IllegalStateException("Cannot export: no PGraphics buffer and no parent PApplet available");
      }
      
      // Embed metadata for PNG files
      if ("png".equals(task.format)) {
        embedMetadata(filename);
      }
      
      exportedCount++;
      
    } catch (Exception e) {
      failedCount++;
      System.err.println("Export failed for frame " + task.frameNumber + ": " + e.getMessage());
    }
  }
  
  /**
   * Embeds configuration metadata into PNG files.
   * 
   * @param filename the PNG file path
   */
  private void embedMetadata(String filename) {
    // Save a sidecar JSON file with configuration metadata
    try {
      String jsonFilename = filename.replace(".png", ".json");
      java.io.FileWriter writer = new java.io.FileWriter(jsonFilename);
      writer.write(config.toJSON().toString());
      writer.close();
    } catch (Exception e) {
      System.err.println("Failed to write metadata for " + filename + ": " + e.getMessage());
    }
  }
  
  /**
   * Sets the export format.
   * 
   * @param format "png", "svg", or "pdf" (case-insensitive)
   * @throws IllegalArgumentException if format is not supported
   */
  public void setFormat(String format) {
    if (format == null || format.isEmpty()) {
      throw new IllegalArgumentException("Format cannot be null or empty");
    }
    String fmt = format.toLowerCase();
    if (!fmt.equals("png") && !fmt.equals("svg") && !fmt.equals("pdf")) {
      throw new IllegalArgumentException("Unsupported format: " + format + ". Use 'png', 'svg', or 'pdf'.");
    }
    this.format = fmt;
  }
  
  /**
   * Gets the current export format.
   * 
   * @return the format string ("png", "svg", or "pdf")
   */
  public String getFormat() {
    return format;
  }
  
  /**
   * Sets whether to use asynchronous export.
   * 
   * @param async true for async, false for synchronous
   */
  public void setAsync(boolean async) {
    this.async = async;
  }
  
  /**
   * Checks if async mode is enabled.
   * 
   * @return true if using async export
   */
  public boolean isAsync() {
    return async;
  }
  
  /**
   * Enables or disables export.
   * 
   * @param enabled true to enable
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
  
  /**
   * Checks if export is enabled.
   * 
   * @return true if export is enabled
   */
  public boolean isEnabled() {
    return enabled;
  }
  
  /**
   * Sets the output directory.
   * 
   * @param directory the output directory path (must not be null)
   * @throws IllegalArgumentException if directory is null
   * @throws IllegalStateException if directory cannot be created
   */
  public void setOutputDirectory(String directory) {
    if (directory == null) {
      throw new IllegalArgumentException("Output directory cannot be null");
    }
    this.outputDirectory = directory;
    java.io.File dir = new java.io.File(directory);
    if (!dir.exists()) {
      boolean created = dir.mkdirs();
      if (!created) {
        throw new IllegalStateException("Failed to create output directory: " + directory);
      }
    }
  }
  
  /**
   * Gets the current output directory.
   * 
   * @return the output directory path
   */
  public String getOutputDirectory() {
    return outputDirectory;
  }
  
  /**
   * Gets the number of successfully exported frames.
   * 
   * @return count of successful exports
   */
  public int getExportedCount() {
    return exportedCount;
  }
  
  /**
   * Gets the number of failed exports.
   * 
   * @return count of failed exports
   */
  public int getFailedCount() {
    return failedCount;
  }
  
  /**
   * Waits for all pending exports to complete without shutting down the executor.
   * The controller remains usable after flushing.
   * 
   * @param timeoutSeconds maximum time to wait
   * @return true if all exports completed, false if timeout occurred
   */
  public boolean flush(int timeoutSeconds) {
    if (!async || executor.isShutdown()) {
      return true;
    }
    // Submit a sentinel task and wait for it to complete,
    // which guarantees all previously submitted tasks have finished.
    try {
      java.util.concurrent.Future<?> sentinel = executor.submit(() -> {});
      sentinel.get(timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS);
      return true;
    } catch (java.util.concurrent.TimeoutException e) {
      System.err.println("Export flush timed out after " + timeoutSeconds + " seconds");
      return false;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      System.err.println("Export flush interrupted");
      return false;
    } catch (java.util.concurrent.ExecutionException e) {
      System.err.println("Export flush failed: " + e.getMessage());
      return false;
    }
  }
  
  /**
   * Waits for all pending exports to complete with default 60 second timeout.
   * 
   * @return true if all exports completed
   */
  public boolean flush() {
    return flush(60);
  }
  
  /**
   * Emergency shutdown for JVM termination.
   */
  private void emergencyShutdown() {
    if (!executor.isShutdown()) {
      executor.shutdownNow();
    }
  }
  
  /**
   * Shuts down the export controller and releases resources.
   * <p>This method should be called when the controller is no longer needed
   * to ensure proper cleanup of the thread pool.</p>
   */
  public void dispose() {
    // Remove shutdown hook to prevent memory leaks
    try {
      Runtime.getRuntime().removeShutdownHook(shutdownHook);
    } catch (IllegalStateException e) {
      // JVM already shutting down, ignore
    }
    
    flush();
    if (!executor.isShutdown()) {
      executor.shutdownNow();
    }
  }
  
  /**
   * Internal class representing an export task.
   */
  private static class ExportTask {
    final int frameNumber;
    final PGraphics graphics;
    final String directory;
    final String format;
    
    ExportTask(int frameNumber, PGraphics graphics, String directory, String format) {
      this.frameNumber = frameNumber;
      this.graphics = graphics;
      this.directory = directory;
      this.format = format;
    }
  }
}
