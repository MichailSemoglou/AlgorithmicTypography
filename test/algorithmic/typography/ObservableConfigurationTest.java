/**
 * JUnit tests for ObservableConfiguration.
 */

package algorithmic.typography;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ObservableConfigurationTest {

  private ObservableConfiguration config;

  @BeforeEach
  void setUp() {
    config = new ObservableConfiguration();
  }

  @Test
  @DisplayName("Listener receives notification on value change")
  void testListenerNotified() {
    AtomicReference<String> receivedKey = new AtomicReference<>();
    AtomicReference<Object> receivedValue = new AtomicReference<>();

    config.addListener((key, value) -> {
      receivedKey.set(key);
      receivedValue.set(value);
    });

    config.setWaveSpeed(3.5f);
    assertNotNull(receivedKey.get(), "Listener should have been called");
  }

  @Test
  @DisplayName("Removed listener is not notified")
  void testRemovedListenerNotNotified() {
    AtomicInteger callCount = new AtomicInteger(0);
    ObservableConfiguration.ConfigurationListener listener = (key, value) -> callCount.incrementAndGet();

    config.addListener(listener);
    config.setWaveSpeed(2.0f);
    assertEquals(1, callCount.get());

    config.removeListener(listener);
    config.setWaveSpeed(4.0f);
    assertEquals(1, callCount.get(), "Removed listener should not be called again");
  }

  @Test
  @DisplayName("Notifications can be disabled and re-enabled")
  void testNotificationsDisabled() {
    AtomicInteger callCount = new AtomicInteger(0);
    config.addListener((key, value) -> callCount.incrementAndGet());

    config.setNotificationsEnabled(false);
    config.setWaveSpeed(5.0f);
    assertEquals(0, callCount.get(), "Should not notify when disabled");

    config.setNotificationsEnabled(true);
    config.setWaveSpeed(6.0f);
    assertTrue(callCount.get() > 0, "Should notify after re-enabling");
  }

  @Test
  @DisplayName("Multiple listeners all receive notification")
  void testMultipleListeners() {
    AtomicInteger count1 = new AtomicInteger(0);
    AtomicInteger count2 = new AtomicInteger(0);

    config.addListener((key, value) -> count1.incrementAndGet());
    config.addListener((key, value) -> count2.incrementAndGet());

    config.setWaveSpeed(1.0f);
    assertTrue(count1.get() > 0, "First listener should be called");
    assertTrue(count2.get() > 0, "Second listener should be called");
  }

  @Test
  @DisplayName("toConfiguration returns independent plain Configuration")
  void testToConfiguration() {
    config.setWaveSpeed(7.0f);
    Configuration plain = config.toConfiguration();

    assertNotNull(plain);
    assertEquals(7.0f, plain.getWaveSpeed(), 0.001f);
    assertFalse(plain instanceof ObservableConfiguration,
        "toConfiguration should return plain Configuration");

    // Modifying plain copy should not affect original
    plain.setWaveSpeed(1.0f);
    assertEquals(7.0f, config.getWaveSpeed(), 0.001f);
  }

  @Test
  @DisplayName("ObservableConfiguration inherits Configuration defaults")
  void testInheritsDefaults() {
    assertEquals(1080, config.getCanvasWidth());
    assertEquals(1080, config.getCanvasHeight());
    assertEquals("A", config.getCharacter());
  }
}
