/**
 * ObservableConfiguration - Configuration with change listeners.
 * 
 * This class extends Configuration to support the observer pattern,
 * allowing UI controls and other components to react to configuration
 * changes in real-time.
 * 
 * @author Michail Semoglou
 * @version 1.0.0
 */

package algorithmic.typography;

import java.util.List;
import java.util.ArrayList;

/**
 * ObservableConfiguration provides live configuration updates.
 * 
 * <p>Components can register as listeners to receive notifications
 * when configuration values change, enabling real-time parameter
 * adjustment without restarting the animation.</p>
 */
public class ObservableConfiguration extends Configuration {
  
  private final List<ConfigurationListener> listeners = new ArrayList<>();
  private boolean notificationsEnabled = true;
  
  /**
   * Interface for configuration change listeners.
   */
  public interface ConfigurationListener {
    /**
     * Called when any configuration value changes.
     * 
     * @param key the name of the changed parameter
     * @param value the new value
     */
    void onConfigurationChanged(String key, Object value);
  }
  
  /**
   * Adds a configuration change listener.
   * 
   * @param listener the listener to add
   */
  public void addListener(ConfigurationListener listener) {
    listeners.add(listener);
  }
  
  /**
   * Removes a configuration change listener.
   * 
   * @param listener the listener to remove
   */
  public void removeListener(ConfigurationListener listener) {
    listeners.remove(listener);
  }
  
  /**
   * Sets whether to send change notifications.
   * 
   * @param enabled true to enable notifications
   */
  public void setNotificationsEnabled(boolean enabled) {
    this.notificationsEnabled = enabled;
  }
  
  /**
   * Notifies all listeners of a configuration change.
   *
   * @param key the name of the changed parameter
   * @param value the new value
   */
  private void notifyChange(String key, Object value) {
    if (notificationsEnabled) {
      for (ConfigurationListener listener : listeners) {
        listener.onConfigurationChanged(key, value);
      }
    }
  }
  
  /**
   * Intercepts configuration changes from parent setters and notifies listeners.
   * This is called automatically by all Configuration setters.
   *
   * @param key the name of the changed parameter
   * @param value the new value
   */
  @Override
  protected void onChange(String key, Object value) {
    notifyChange(key, value);
  }
  
  /**
   * Creates a regular Configuration copy (without listeners).
   *
   * @return a plain Configuration with the same values
   */
  public Configuration toConfiguration() {
    return super.copy();
  }
}
