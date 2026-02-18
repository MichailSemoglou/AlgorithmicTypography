/**
 * OSCBridge - Open Sound Control integration for external controllers.
 * 
 * This class enables control of typography parameters via OSC messages,
 * allowing integration with MIDI controllers, DAWs, and other OSC-capable
 * software and hardware.
 * 
 * Requires: oscP5 library (http://www.sojamo.de/libraries/oscP5/)
 * 
 * @author Michail Semoglou
 * @version 1.0.0
 */

package algorithmic.typography.ui;

import processing.core.*;
import algorithmic.typography.*;

/**
 * OSCBridge provides network-based parameter control.
 * 
 * <p>OSC messages can control all typography parameters:</p>
 * <ul>
 *   <li>/wave/speed [float] - Animation speed</li>
 *   <li>/brightness/min [float] - Minimum brightness (0-255)</li>
 *   <li>/brightness/max [float] - Maximum brightness (0-255)</li>
 *   <li>/saturation/min [float] - Minimum saturation (0-255)</li>
 *   <li>/saturation/max [float] - Maximum saturation (0-255)</li>
 *   <li>/hue/min [float] - Minimum hue (0-360)</li>
 *   <li>/hue/max [float] - Maximum hue (0-360)</li>
 *   <li>/character [string] - Display character</li>
 *   <li>/grid/size [int] - Grid tile count</li>
 *   <li>/render/restart - Restart animation</li>
 * </ul>
 * 
 * <p>Default listening port: 12000</p>
 */
public class OSCBridge {
  
  private final PApplet parent;
  private final ObservableConfiguration config;
  
  private Object oscP5;      // oscP5 instance
  private Object myRemoteLocation; // NetAddress
  private int receivePort = 12000;
  private int sendPort = 12001;
  private boolean connected = false;
  
  /**
   * Creates a new OSCBridge.
   * 
   * @param parent the parent PApplet
   * @param config the configuration to control
   */
  public OSCBridge(PApplet parent, ObservableConfiguration config) {
    this.parent = parent;
    this.config = config;
  }
  
  /**
   * Creates a new OSCBridge with specified ports.
   * 
   * @param parent the parent PApplet
   * @param config the configuration to control
   * @param receivePort port to receive OSC messages
   * @param sendPort port to send OSC messages
   */
  public OSCBridge(PApplet parent, ObservableConfiguration config, int receivePort, int sendPort) {
    this(parent, config);
    this.receivePort = receivePort;
    this.sendPort = sendPort;
  }
  
  /**
   * Starts the OSC server.
   * 
   * @return true if started successfully
   */
  public boolean start() {
    try {
      // Initialize oscP5 via reflection to avoid compile dependency
      Class<?> oscP5Class = Class.forName("oscP5.OscP5");
      Class<?> netAddressClass = Class.forName("netP5.NetAddress");
      
      oscP5 = oscP5Class.getConstructor(PApplet.class, int.class).newInstance(parent, receivePort);
      myRemoteLocation = netAddressClass.getConstructor(String.class, int.class).newInstance("127.0.0.1", sendPort);
      
      connected = true;
      parent.println("OSC server started on port " + receivePort);
      parent.println("Listening for messages...");
      printOscMessages();
      
      return true;
      
    } catch (Exception e) {
      parent.println("Could not start OSC server: " + e.getMessage());
      parent.println("Make sure oscP5 library is installed");
      return false;
    }
  }
  
  /**
   * Stops the OSC server.
   */
  public void stop() {
    if (oscP5 != null) {
      try {
        oscP5.getClass().getMethod("stop").invoke(oscP5);
      } catch (Throwable t) {
        // Ignore all errors during shutdown (including ThreadDeath)
      }
    }
    connected = false;
  }
  
  /**
   * Checks if OSC is connected.
   * 
   * @return true if the OSC server is connected
   */
  public boolean isConnected() {
    return connected;
  }
  
  /**
   * Gets the receive port.
   * 
   * @return the OSC receive port number
   */
  public int getReceivePort() {
    return receivePort;
  }
  
  /**
   * Gets the send port.
   * 
   * @return the OSC send port number
   */
  public int getSendPort() {
    return sendPort;
  }
  
  /**
   * Handles incoming OSC messages.
   * Call from oscEvent(OscMessage msg) in your sketch.
   * 
   * @param msg the OSC message
   */
  public void handleMessage(Object msg) {
    if (!connected || msg == null) return;
    
    try {
      String addrPattern = (String) msg.getClass().getMethod("addrPattern").invoke(msg);
      
      if ("/wave/speed".equals(addrPattern)) {
        float value = ((Number) msg.getClass().getMethod("get", int.class).invoke(msg, 0)).floatValue();
        config.setWaveSpeed(value);
        parent.println("OSC: wave speed -> " + value);
        
      } else if ("/brightness/min".equals(addrPattern)) {
        float value = ((Number) msg.getClass().getMethod("get", int.class).invoke(msg, 0)).floatValue();
        config.setBrightnessRange(value, config.getBrightnessMax());
        parent.println("OSC: brightness min -> " + value);
        
      } else if ("/brightness/max".equals(addrPattern)) {
        float value = ((Number) msg.getClass().getMethod("get", int.class).invoke(msg, 0)).floatValue();
        config.setBrightnessRange(config.getBrightnessMin(), value);
        parent.println("OSC: brightness max -> " + value);
        
      } else if ("/saturation/min".equals(addrPattern)) {
        float value = ((Number) msg.getClass().getMethod("get", int.class).invoke(msg, 0)).floatValue();
        config.setSaturationRange(value, config.getSaturationMax());
        parent.println("OSC: saturation min -> " + value);
        
      } else if ("/saturation/max".equals(addrPattern)) {
        float value = ((Number) msg.getClass().getMethod("get", int.class).invoke(msg, 0)).floatValue();
        config.setSaturationRange(config.getSaturationMin(), value);
        parent.println("OSC: saturation max -> " + value);
        
      } else if ("/hue/min".equals(addrPattern)) {
        float value = ((Number) msg.getClass().getMethod("get", int.class).invoke(msg, 0)).floatValue();
        config.setHueRange(value, config.getHueMax());
        parent.println("OSC: hue min -> " + value);
        
      } else if ("/hue/max".equals(addrPattern)) {
        float value = ((Number) msg.getClass().getMethod("get", int.class).invoke(msg, 0)).floatValue();
        config.setHueRange(config.getHueMin(), value);
        parent.println("OSC: hue max -> " + value);
        
      } else if ("/character".equals(addrPattern)) {
        String value = (String) msg.getClass().getMethod("get", int.class).invoke(msg, 0);
        config.setCharacter(value);
        parent.println("OSC: character -> " + value);
        
      } else if ("/grid/size".equals(addrPattern)) {
        int value = ((Number) msg.getClass().getMethod("get", int.class).invoke(msg, 0)).intValue();
        config.setGridSize(value, value, value / 2, value / 2);
        parent.println("OSC: grid size -> " + value);
        
      } else if ("/render/restart".equals(addrPattern)) {
        parent.println("OSC: restart animation");
        // Signal to restart - implementation depends on TypographySystem
      }
      
    } catch (Exception e) {
      parent.println("Error handling OSC message: " + e.getMessage());
    }
  }
  
  /**
   * Sends a float value via OSC.
   * 
   * @param address the OSC address pattern
   * @param value the value to send
   */
  public void send(String address, float value) {
    if (!connected) return;
    
    try {
      Class<?> oscMessageClass = Class.forName("oscP5.OscMessage");
      Object msg = oscMessageClass.getConstructor(String.class).newInstance(address);
      msg.getClass().getMethod("add", float.class).invoke(msg, value);
      
      oscP5.getClass().getMethod("send", Class.forName("oscP5.OscMessage"), Class.forName("netP5.NetAddress"))
             .invoke(oscP5, msg, myRemoteLocation);
             
    } catch (Exception e) {
      // Ignore send errors
    }
  }
  
  /**
   * Prints available OSC message addresses.
   */
  public void printOscMessages() {
    parent.println("");
    parent.println("Available OSC messages:");
    parent.println("  /wave/speed [float]       - Animation speed (0.1 - 10.0)");
    parent.println("  /brightness/min [float]   - Min brightness (0 - 255)");
    parent.println("  /brightness/max [float]   - Max brightness (0 - 255)");
    parent.println("  /saturation/min [float]   - Min saturation (0 - 255)");
    parent.println("  /saturation/max [float]   - Max saturation (0 - 255)");
    parent.println("  /hue/min [float]          - Min hue (0 - 360)");
    parent.println("  /hue/max [float]          - Max hue (0 - 360)");
    parent.println("  /character [string]       - Display character");
    parent.println("  /grid/size [int]          - Grid tiles (8, 16, 32, 64)");
    parent.println("  /render/restart           - Restart animation");
    parent.println("");
  }
}
