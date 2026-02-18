/**
 * WebSocketServer - Browser-based remote control.
 * 
 * This class provides a WebSocket server that allows browsers
 * to connect and control the typography animation remotely.
 * Useful for installations and web-based control interfaces.
 * 
 * Requires: Java-WebSocket library
 * 
 * @author Michail Semoglou
 * @version 1.0.0
 */

package algorithmic.typography.net;

import processing.core.*;
import algorithmic.typography.*;
import algorithmic.typography.ui.*;

import java.net.InetSocketAddress;

/**
 * WebSocketServer for browser-based control.
 * 
 * <p>Provides a WebSocket endpoint at ws://localhost:8080
 * that accepts JSON commands for real-time control.</p>
 * 
 * <p>Example messages:</p>
 * <pre>
 * {"command": "setWaveSpeed", "value": 3.0}
 * {"command": "setSaturation", "min": 50, "max": 200}
 * {"command": "restart"}
 * </pre>
 */
public class WebSocketServer {
  
  private final PApplet parent;
  private final ObservableConfiguration config;
  
  private Object server; // WebSocket server via reflection
  private int port = 8080;
  private boolean running = false;
  
  /**
   * Creates a WebSocket server.
   * 
   * @param parent the parent PApplet
   * @param config the observable configuration
   * @param port the port number (default: 8080)
   */
  public WebSocketServer(PApplet parent, ObservableConfiguration config, int port) {
    this.parent = parent;
    this.config = config;
    this.port = port;
  }
  
  /**
   * Creates with default port 8080.
   * 
   * @param parent the parent PApplet
   * @param config the observable configuration
   */
  public WebSocketServer(PApplet parent, ObservableConfiguration config) {
    this(parent, config, 8080);
  }
  
  /**
   * Starts the WebSocket server.
   * 
   * @return true if started successfully
   */
  public boolean start() {
    try {
      // Try to load Java-WebSocket library via reflection
      Class<?> serverClass = Class.forName("org.java_websocket.server.WebSocketServer");
      
      // Create anonymous subclass
      server = serverClass.getConstructor(InetSocketAddress.class)
        .newInstance(new InetSocketAddress(port));
      
      // Start server
      server.getClass().getMethod("start").invoke(server);
      
      running = true;
      parent.println("WebSocket server started on ws://localhost:" + port);
      return true;
      
    } catch (Exception e) {
      parent.println("WebSocket server failed to start:");
      parent.println("  " + e.getMessage());
      parent.println("Install Java-WebSocket from:");
      parent.println("  https://github.com/TooTallNate/Java-WebSocket");
      return false;
    }
  }
  
  /**
   * Stops the server.
   */
  public void stop() {
    if (server != null && running) {
      try {
        server.getClass().getMethod("stop").invoke(server);
        running = false;
        parent.println("WebSocket server stopped");
      } catch (Exception e) {
        parent.println("Error stopping server: " + e.getMessage());
      }
    }
  }
  
  /**
   * Checks if server is running.
   * 
   * @return true if the WebSocket server is running
   */
  public boolean isRunning() {
    return running;
  }
  
  /**
   * Gets the server port.
   * 
   * @return the WebSocket server port number
   */
  public int getPort() {
    return port;
  }
  
  /**
   * Broadcasts a message to all connected clients.
   * 
   * @param message the message to send
   */
  public void broadcast(String message) {
    if (!running || server == null) return;
    
    try {
      server.getClass().getMethod("broadcast", String.class)
        .invoke(server, message);
    } catch (Exception e) {
      // Ignore broadcast errors
    }
  }
  
  /**
   * Handles incoming messages.
   * This would be called by the WebSocket callback.
   * 
   * @param message the JSON message string to process
   */
  public void handleMessage(String message) {
    try {
      // Parse JSON command
      processing.data.JSONObject json = parent.parseJSONObject(message);
      
      if (json == null) return;
      
      String command = json.getString("command", "");
      
      switch (command) {
        case "setWaveSpeed":
          float speed = json.getFloat("value", config.getWaveSpeed());
          config.setWaveSpeed(speed);
          break;
          
        case "setSaturation":
          float min = json.getFloat("min", config.getSaturationMin());
          float max = json.getFloat("max", config.getSaturationMax());
          config.setSaturationRange(min, max);
          break;
          
        case "setGridSize":
          int size = json.getInt("value", config.getInitialTilesX());
          config.setGridSize(size, size, size/2, size/2);
          break;
          
        case "restart":
          // Signal restart
          parent.println("Restart requested via WebSocket");
          break;
          
        case "getConfig":
          // Send current config back
          broadcast(config.toJSON().toString());
          break;
      }
      
    } catch (Exception e) {
      parent.println("Error handling WebSocket message: " + e.getMessage());
    }
  }
  
  /**
   * Returns the HTML control interface.
   * Users can save this to an .html file for browser control.
   * 
   * @return the HTML string for the browser-based control interface
   */
  public static String getControlInterface() {
    return "<!DOCTYPE html>\n" +
           "<html>\n" +
           "<head>\n" +
           "  <title>Algorithmic Typography Control</title>\n" +
           "  <style>\n" +
           "    body { font-family: sans-serif; padding: 20px; background: #222; color: #fff; }\n" +
           "    .control { margin: 10px 0; }\n" +
           "    label { display: inline-block; width: 150px; }\n" +
           "    input[type=range] { width: 300px; }\n" +
           "    button { padding: 10px 20px; margin: 5px; }\n" +
           "    #status { color: #0f0; }\n" +
           "  </style>\n" +
           "</head>\n" +
           "<body>\n" +
           "  <h1>Algorithmic Typography Remote Control</h1>\n" +
           "  <div id=\"status\">Connecting...</div>\n" +
           "  \n" +
           "  <div class=\"control\">\n" +
           "    <label>Wave Speed:</label>\n" +
           "    <input type=\"range\" id=\"speed\" min=\"0.1\" max=\"10\" step=\"0.1\" value=\"2\">\n" +
           "    <span id=\"speedVal\">2</span>\n" +
           "  </div>\n" +
           "  \n" +
           "  <div class=\"control\">\n" +
           "    <button onclick=\"restart()\">Restart Animation</button>\n" +
           "    <button onclick=\"getConfig()\">Get Config</button>\n" +
           "  </div>\n" +
           "  \n" +
           "  <script>\n" +
           "    const ws = new WebSocket('ws://localhost:8080');\n" +
           "    const status = document.getElementById('status');\n" +
           "    \n" +
           "    ws.onopen = () => { status.textContent = 'Connected'; };\n" +
           "    ws.onclose = () => { status.textContent = 'Disconnected'; };\n" +
           "    ws.onmessage = (e) => { console.log('Received:', e.data); };\n" +
           "    \n" +
           "    document.getElementById('speed').oninput = function() {\n" +
           "      document.getElementById('speedVal').textContent = this.value;\n" +
           "      ws.send(JSON.stringify({command: 'setWaveSpeed', value: parseFloat(this.value)}));\n" +
           "    };\n" +
           "    \n" +
           "    function restart() {\n" +
           "      ws.send(JSON.stringify({command: 'restart'}));\n" +
           "    }\n" +
           "    \n" +
           "    function getConfig() {\n" +
           "      ws.send(JSON.stringify({command: 'getConfig'}));\n" +
           "    }\n" +
           "  </script>\n" +
           "</body>\n" +
           "</html>";
  }
}
