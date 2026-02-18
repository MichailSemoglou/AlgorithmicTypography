/**
 * RitaBridge - Computational Literature Integration for AlgorithmicTypography
 * 
 * This class provides integration with Rita.js (https://rednoise.org/rita/)
 * for computational literature features including:
 * - Text generation for grid cells
 * - Linguistic analysis driving animation parameters
 * - Grammar-based configuration generation
 * - Markov chain text generation
 * 
 * Requires: Rita library (https://github.com/dhowe/rita4processing)
 * 
 * @author Michail Semoglou
 * @version 1.0.0
 */

package algorithmic.typography.text;

import processing.core.*;
import algorithmic.typography.*;

/**
 * RitaBridge connects computational literature with typography animation.
 * 
 * <p>Rita.js is a library for computational literature that provides:
 * <ul>
 *   <li>Natural language generation</li>
 *   <li>Text analysis (POS tagging, syllables, sentiment)</li>
 *   <li>Markov chains for text generation</li>
 *   <li>Context-free grammars</li>
 * </ul>
 * </p>
 * 
 * <p>Usage:</p>
 * <pre>
 * RitaBridge rita = new RitaBridge(this);
 * rita.loadMarkov("source-text.txt");
 * String[] words = rita.generateWords(gridSize);
 * </pre>
 */
public class RitaBridge {
  
  private final PApplet parent;
  private Class<?> ritaClass; // RiTa class (static methods, loaded via reflection)
  private Object markov; // Markov chain generator
  private Object grammar; // Grammar generator
  
  // Analysis cache
  private String[] lastGeneratedWords;
  private float[] sentimentScores;
  private int[] syllableCounts;
  
  /**
   * Creates a new RitaBridge.
   * 
   * @param parent the parent PApplet
   */
  public RitaBridge(PApplet parent) {
    this.parent = parent;
    initializeRita();
  }
  
  /**
   * Initializes Rita via reflection to avoid compile dependency.
   * RiTa 2.x is a static utility class — no getInstance() needed.
   */
  private void initializeRita() {
    try {
      ritaClass = Class.forName("rita.RiTa");
      parent.println("RiTa library loaded successfully");
    } catch (Exception e) {
      parent.println("RiTa not available. Install via:");
      parent.println("Sketch → Import Library… → Manage Libraries… → search 'RiTa'");
    }
  }
  
  /**
   * Checks if Rita is available.
   * 
   * @return true if Rita library is loaded
   */
  public boolean isAvailable() {
    return ritaClass != null;
  }
  
  /**
   * Loads a text file for Markov chain generation.
   * 
   * @param filename the text file path
   * @param n the Markov order (default: 4)
   */
  public void loadMarkov(String filename, int n) {
    if (!isAvailable()) return;
    
    try {
      String[] lines = parent.loadStrings(filename);
      if (lines != null) {
        String text = String.join(" ", lines);
        
        Class<?> markovClass = Class.forName("rita.RiMarkov");
        markov = markovClass.getConstructor(int.class).newInstance(n);
        markov.getClass().getMethod("addText", String.class).invoke(markov, text);
        
        parent.println("Markov chain loaded from: " + filename);
      }
    } catch (Exception e) {
      parent.println("Error loading Markov chain: " + e.getMessage());
    }
  }
  
  /**
   * Loads Markov with default order (4).
   * 
   * @param filename the text file path
   */
  public void loadMarkov(String filename) {
    loadMarkov(filename, 4);
  }
  
  /**
   * Generates words using Markov chains.
   * 
   * @param count number of words to generate
   * @return array of generated words
   */
  public String[] generateWords(int count) {
    if (!isAvailable() || markov == null) {
      return generatePlaceholderWords(count);
    }
    
    try {
      // RiTa 2.x: generate() returns String[]
      java.util.ArrayList<String> allWords = new java.util.ArrayList<String>();
      while (allWords.size() < count) {
        Object result = markov.getClass().getMethod("generate").invoke(markov);
        String[] sentences = (String[]) result;
        for (String sentence : sentences) {
          for (String w : sentence.split("\\s+")) {
            if (w.length() > 0) allWords.add(w);
            if (allWords.size() >= count) break;
          }
          if (allWords.size() >= count) break;
        }
      }
      String[] words = allWords.subList(0, count).toArray(new String[0]);
      lastGeneratedWords = words;
      analyzeWords(words);
      return words;
    } catch (Exception e) {
      parent.println("Markov generate error: " + e.getMessage());
      return generatePlaceholderWords(count);
    }
  }
  
  /**
   * Generates words from a grammar.
   * 
   * @param grammarJson JSON grammar definition
   * @param count number of words to generate
   * @return array of generated words
   */
  public String[] generateFromGrammar(String grammarJson, int count) {
    if (!isAvailable()) {
      return generatePlaceholderWords(count);
    }
    
    try {
      // RiTa 2.x: RiGrammar constructor takes the JSON string
      Class<?> grammarClass = Class.forName("rita.RiGrammar");
      grammar = grammarClass.getConstructor(String.class).newInstance(grammarJson);
      
      String[] words = new String[count];
      for (int i = 0; i < count; i++) {
        Object result = grammar.getClass().getMethod("expand").invoke(grammar);
        words[i] = (String) result;
      }
      lastGeneratedWords = words;
      analyzeWords(words);
      return words;
    } catch (Exception e) {
      return generatePlaceholderWords(count);
    }
  }
  
  /**
   * Analyzes words and caches metrics.
   */
  private void analyzeWords(String[] words) {
    if (!isAvailable()) return;
    
    sentimentScores = new float[words.length];
    syllableCounts = new int[words.length];
    
    try {
      for (int i = 0; i < words.length; i++) {
        // RiTa 2.x: RiTa.syllables(word) is a static method
        Object syllables = ritaClass.getMethod("syllables", String.class).invoke(null, words[i]);
        syllableCounts[i] = syllables.toString().split("/").length;
        
        // Simple sentiment approximation based on word length and syllables
        sentimentScores[i] = mapSentiment(words[i], syllableCounts[i]);
      }
    } catch (Exception e) {
      // Use defaults
      for (int i = 0; i < words.length; i++) {
        sentimentScores[i] = 0.5f;
        syllableCounts[i] = 1;
      }
    }
  }
  
  /**
   * Maps word characteristics to sentiment value.
   */
  private float mapSentiment(String word, int syllables) {
    // Simple heuristic: shorter words with fewer syllables = positive
    float lengthFactor = 1.0f - (word.length() / 20.0f);
    float syllableFactor = 1.0f - (syllables / 5.0f);
    return PApplet.constrain((lengthFactor + syllableFactor) / 2, 0, 1);
  }
  
  /**
   * Gets syllable count for a word at index.
   * 
   * @param index the word index in the last generated array
   * @return the syllable count (defaults to 1 if unavailable)
   */
  public int getSyllableCount(int index) {
    if (syllableCounts != null && index < syllableCounts.length) {
      return syllableCounts[index];
    }
    return 1;
  }
  
  /**
   * Gets sentiment score for a word at index.
   * 
   * @param index the word index in the last generated array
   * @return the sentiment score (0.0 - 1.0, defaults to 0.5 if unavailable)
   */
  public float getSentiment(int index) {
    if (sentimentScores != null && index < sentimentScores.length) {
      return sentimentScores[index];
    }
    return 0.5f;
  }
  
  /**
   * Maps syllable count to wave speed.
   * 
   * @param syllables the number of syllables
   * @return the mapped wave speed value (0.5 - 5.0)
   */
  public float syllablesToWaveSpeed(int syllables) {
    return PApplet.map(syllables, 1, 5, 0.5f, 5.0f);
  }
  
  /**
   * Maps sentiment to saturation range.
   * 
   * @param sentiment the sentiment score (0.0 - 1.0)
   * @return the mapped saturation value (50 - 255)
   */
  public float sentimentToSaturation(float sentiment) {
    return PApplet.map(sentiment, 0, 1, 50, 255);
  }
  
  /**
   * Creates a configuration driven by text analysis.
   * 
   * @param config the configuration to modify
   * @param text the text to analyze
   */
  public void applyTextAnalysisToConfig(Configuration config, String text) {
    if (!isAvailable()) return;
    
    try {
      // RiTa 2.x: RiTa.tokenize() and RiTa.syllables() are static
      Object tokens = ritaClass.getMethod("tokenize", String.class).invoke(null, text);
      String[] words = (String[]) tokens;
      
      if (words.length > 0) {
        // Average syllables affects wave speed
        int totalSyllables = 0;
        for (String word : words) {
          Object syl = ritaClass.getMethod("syllables", String.class).invoke(null, word);
          totalSyllables += syl.toString().split("/").length;
        }
        float avgSyllables = (float) totalSyllables / words.length;
        config.setWaveSpeed(syllablesToWaveSpeed((int) avgSyllables));
      }
      
    } catch (Exception e) {
      parent.println("Error analyzing text: " + e.getMessage());
    }
  }
  
  /**
   * Generates placeholder words when Rita is not available.
   */
  private String[] generatePlaceholderWords(int count) {
    String[] words = {"the", "and", "of", "to", "a", "in", "is", "that", "for", "it"};
    String[] result = new String[count];
    for (int i = 0; i < count; i++) {
      result[i] = words[i % words.length];
    }
    return result;
  }
  
  /**
   * Gets the last generated words.
   * 
   * @return the array of last generated words, or null if none generated
   */
  public String[] getLastGeneratedWords() {
    return lastGeneratedWords;
  }
  
  /**
   * Creates a grammar JSON for poetry generation.
   * 
   * @return a JSON string defining a simple poetry grammar
   */
  public static String createPoetryGrammar() {
    return "{\n" +
           "  \"<start>\": \"<noun-phrase> <verb-phrase>\",\n" +
           "  \"<noun-phrase>\": [\"the <adj> <noun>\", \"a <noun>\"],\n" +
           "  \"<verb-phrase>\": [\"<verb> <adverb>\", \"<verb>\"],\n" +
           "  \"<adj>\": [\"dark\", \"light\", \"silent\", \"loud\", \"soft\"],\n" +
           "  \"<noun>\": [\"wave\", \"letter\", \"type\", \"form\", \"pattern\"],\n" +
           "  \"<verb>\": [\"flows\", \"moves\", " +
           "\"shifts\", \"transforms\", \"dances\"],\n" +
           "  \"<adverb>\": [\"slowly\", \"quickly\", \"silently\", \"gently\"]\n" +
           "}";
  }
}
