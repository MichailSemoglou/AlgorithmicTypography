/**
 * Serendipity - Controlled randomness for "happy accidents".
 *
 * Introduces controlled randomness into wave calculations,
 * breaking mathematical perfection for organic, unexpected results.
 *
 * @author Michail Semoglou
 * @version 1.0.0
 * @since 1.0.0
 */

package algorithmic.typography.style;

import processing.core.PApplet;
import java.util.Random;

/**
 * Serendipity provides controlled randomness.
 *
 * <p>Usage:</p>
 * <pre>
 * Serendipity s = new Serendipity(0.3f); // 30% randomness
 * float value = s.addNoise(calculatedValue);
 * </pre>
 */
public class Serendipity {

  private float amount; // 0.0 to 1.0
  private long seed;
  private Random rng;

  /**
   * Creates Serendipity with given randomness amount.
   *
   * @param amount randomness amount (0.0 = none, 1.0 = maximum)
   */
  public Serendipity(float amount) {
    this.amount = PApplet.constrain(amount, 0.0f, 1.0f);
    this.seed = System.currentTimeMillis();
    this.rng = new Random(seed);
  }

  /**
   * Adds noise to a value.
   *
   * @param value the original calculated value
   * @return value with controlled randomness added
   */
  public float addNoise(float value) {
    if (amount <= 0) return value;

    float noise = (rng.nextFloat() - 0.5f) * 2; // -1 to 1
    return value + (noise * value * amount);
  }

  /**
   * Sets the randomness amount.
   *
   * @param amount 0.0 to 1.0
   */
  public void setAmount(float amount) {
    this.amount = PApplet.constrain(amount, 0.0f, 1.0f);
  }

  /**
   * Gets current randomness amount.
   *
   * @return the current amount (0.0 to 1.0)
   */
  public float getAmount() {
    return amount;
  }

  /**
   * Sets the random seed for reproducible results.
   *
   * @param seed the random seed
   */
  public void setSeed(long seed) {
    this.seed = seed;
    this.rng = new Random(seed);
  }

  /**
   * Gets the current random seed.
   *
   * @return the seed value
   */
  public long getSeed() {
    return seed;
  }
}
