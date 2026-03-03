/**
 * GlyphExtractor - Extract glyph outlines as PShape objects.
 * 
 * <p>Provides access to typographic glyph contours as vector paths,
 * enabling direct vertex manipulation, deformation, and procedural
 * effects on letterforms. Inspired by the Geomerative library but
 * built as a lightweight, self-contained solution.</p>
 * 
 * <p>Uses Java AWT's {@code Font} and {@code GlyphVector} to extract
 * outlines, then converts them to Processing {@code PShape} objects
 * for seamless integration with the rendering pipeline.</p>
 * 
 * <h2>Features</h2>
 * <ul>
 *   <li>Extract single character or full string outlines</li>
 *   <li>Sample points along glyph contours at configurable density</li>
 *   <li>Access raw contour vertices for custom deformation</li>
 *   <li>Wave-driven vertex displacement using WaveEngine</li>
 *   <li>PShape output for easy rendering and SVG export</li>
 *   <li>Fill letterform interiors with scattered points ({@code fillWithPoints})</li>
 *   <li>Evenly distribute points along the perimeter by arc length ({@code distributeAlongOutline})</li>
 *   <li>Separate outer boundary from inner counter-forms ({@code getOuterContour} / {@code getInnerContours})</li>
 *   <li>Hatch-line fill clipped to the letterform interior ({@code fillWithLines})</li>
 *   <li>Expand or contract the letterform boundary ({@code offsetOutline})</li>
 *   <li>Morph between two letterform outlines ({@code morphShape} / {@code interpolateContours} / {@code interpolateTo})</li>
 *   <li>Centre a glyph at any canvas point without boilerplate ({@code drawAt} / {@code centerOf} / {@code morphCenterOf})</li>
 *   <li>Approximate medial axis / spine of the letterform ({@code getMedialAxis})</li>
 *   <li>Single point at a normalised arc-length position ({@code sampleAlongPath})</li>
 *   <li>Union outline of multiple characters as a single path ({@code getBoundingContour})</li>
 * </ul>
 * 
 * <h2>Example Usage</h2>
 * <pre>
 * GlyphExtractor glyph = new GlyphExtractor(this);
 * PShape letter = glyph.extractChar('A', 200);
 * shape(letter, 100, 100);
 * 
 * // Get raw points for custom manipulation
 * PVector[] pts = glyph.getContourPoints('W', 120, 4.0);
 * 
 * // Hatch fill (new in v0.2.3)
 * float[][] lines = glyph.fillWithLines('A', 600, 45, 8);
 * 
 * // Animate a particle along the outline (new in v0.2.3)
 * PVector pt = glyph.sampleAlongPath('O', 600, frameCount * 0.005 % 1.0);
 * </pre>
 * 
 * @author Michail Semoglou
 * @version 0.2.3
 * @since 1.0.0
 */

package algorithmic.typography.render;

import processing.core.*;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class GlyphExtractor {
  
  private final PApplet parent;
  private Font awtFont;
  private FontRenderContext frc;
  
  /** Default flatness for curve tessellation (smaller = more points). */
  private float flatness = 1.0f;
  
  /**
   * Creates a GlyphExtractor using Processing's default font.
   * 
   * @param parent the parent PApplet
   */
  public GlyphExtractor(PApplet parent) {
    this.parent = parent;
    this.awtFont = new Font("SansSerif", Font.PLAIN, 72);
    this.frc = new FontRenderContext(null, true, true);
  }
  
  /**
   * Creates a GlyphExtractor with a specific font name and size.
   * 
   * @param parent the parent PApplet
   * @param fontName the font family name (e.g. "Helvetica", "Courier")
   * @param fontSize the font size in points
   */
  public GlyphExtractor(PApplet parent, String fontName, float fontSize) {
    this.parent = parent;
    this.awtFont = new Font(fontName, Font.PLAIN, (int) fontSize);
    this.frc = new FontRenderContext(null, true, true);
  }
  
  /**
   * Creates a GlyphExtractor from a Processing PFont.
   * 
   * <p>Extracts the underlying AWT Font from the PFont. If the PFont
   * was created from a system font, the native outlines are used.
   * If it was created from an image, a fallback SansSerif font is used.</p>
   * 
   * @param parent the parent PApplet
   * @param pfont the Processing PFont
   * @param fontSize the desired font size
   */
  public GlyphExtractor(PApplet parent, PFont pfont, float fontSize) {
    this.parent = parent;
    this.frc = new FontRenderContext(null, true, true);
    
    // Try to get native AWT font from PFont
    Font nativeFont = (Font) pfont.getNative();
    if (nativeFont != null) {
      this.awtFont = nativeFont.deriveFont(fontSize);
    } else {
      this.awtFont = new Font(pfont.getName(), Font.PLAIN, (int) fontSize);
    }
  }
  
  // ── Font management ────────────────────────────────────────────
  
  /**
   * Sets the font by name and size.
   * 
   * @param fontName font family name
   * @param fontSize size in points
   */
  public void setFont(String fontName, float fontSize) {
    this.awtFont = new Font(fontName, Font.PLAIN, (int) fontSize);
  }
  
  /**
   * Sets the font from a Processing PFont.
   * 
   * @param pfont the Processing PFont
   * @param fontSize the desired size
   */
  public void setFont(PFont pfont, float fontSize) {
    Font nativeFont = (Font) pfont.getNative();
    if (nativeFont != null) {
      this.awtFont = nativeFont.deriveFont(fontSize);
    } else {
      this.awtFont = new Font(pfont.getName(), Font.PLAIN, (int) fontSize);
    }
  }
  
  /**
   * Sets the font size (keeps the current font family).
   * 
   * @param fontSize the new size in points
   */
  public void setFontSize(float fontSize) {
    this.awtFont = awtFont.deriveFont(fontSize);
  }
  
  /**
   * Sets the flatness for curve tessellation.
   * 
   * <p>Lower values produce more points (smoother curves).
   * Higher values produce fewer points (coarser approximation).</p>
   * 
   * @param flatness flatness value (default 1.0, range ~0.1 to 10.0)
   */
  public void setFlatness(float flatness) {
    this.flatness = Math.max(0.01f, flatness);
  }
  
  /**
   * Gets the current flatness value.
   * 
   * @return the flatness value
   */
  public float getFlatness() {
    return flatness;
  }
  
  // ── Character extraction ───────────────────────────────────────
  
  /**
   * Extracts a single character as a PShape.
   * 
   * <p>The returned PShape contains all contours of the glyph,
   * including holes (e.g. the counter in 'O' or 'A').</p>
   * 
   * @param ch the character to extract
   * @param fontSize the size to render at
   * @return a PShape containing the glyph outline
   */
  public PShape extractChar(char ch, float fontSize) {
    Font sized = awtFont.deriveFont(fontSize);
    GlyphVector gv = sized.createGlyphVector(frc, new char[]{ch});
    Shape outline = gv.getOutline();
    return shapeToProcessing(outline);
  }
  
  /**
   * Extracts a full string as a PShape.
   * 
   * <p>All characters are combined into a single shape with
   * proper kerning and spacing applied by the font engine.</p>
   * 
   * @param text the text string to extract
   * @param fontSize the size to render at
   * @return a PShape containing all glyph outlines
   */
  public PShape extractString(String text, float fontSize) {
    Font sized = awtFont.deriveFont(fontSize);
    GlyphVector gv = sized.createGlyphVector(frc, text);
    Shape outline = gv.getOutline();
    return shapeToProcessing(outline);
  }
  
  // ── Point extraction ───────────────────────────────────────────
  
  /**
   * Gets evenly sampled contour points for a character.
   * 
   * <p>Returns the vertices of the flattened glyph outline.
   * Use {@link #setFlatness(float)} to control point density.</p>
   * 
   * @param ch the character
   * @param fontSize the font size
   * @return array of PVector points along the contour
   */
  public PVector[] getContourPoints(char ch, float fontSize) {
    Font sized = awtFont.deriveFont(fontSize);
    GlyphVector gv = sized.createGlyphVector(frc, new char[]{ch});
    Shape outline = gv.getOutline();
    return extractPoints(outline);
  }
  
  /**
   * Gets contour points for a string.
   * 
   * @param text the text string
   * @param fontSize the font size
   * @return array of PVector points along the contour
   */
  public PVector[] getContourPoints(String text, float fontSize) {
    Font sized = awtFont.deriveFont(fontSize);
    GlyphVector gv = sized.createGlyphVector(frc, text);
    Shape outline = gv.getOutline();
    return extractPoints(outline);
  }
  
  /**
   * Gets separated contours for a character.
   * 
   * <p>Returns each contour (outer boundary, holes) as a separate
   * array of PVectors. Useful for treating each contour independently.</p>
   * 
   * @param ch the character
   * @param fontSize the font size
   * @return list of contours, each being an array of PVector points
   */
  public List<PVector[]> getContours(char ch, float fontSize) {
    Font sized = awtFont.deriveFont(fontSize);
    GlyphVector gv = sized.createGlyphVector(frc, new char[]{ch});
    Shape outline = gv.getOutline();
    return extractContours(outline);
  }
  
  /**
   * Gets separated contours for a string.
   * 
   * @param text the text string
   * @param fontSize the font size
   * @return list of contours, each being an array of PVector points
   */
  public List<PVector[]> getContours(String text, float fontSize) {
    Font sized = awtFont.deriveFont(fontSize);
    GlyphVector gv = sized.createGlyphVector(frc, text);
    Shape outline = gv.getOutline();
    return extractContours(outline);
  }
  
  // ── Deformed extraction ────────────────────────────────────────
  
  /**
   * Extracts a character with wave-based vertex displacement.
   * 
   * <p>Each vertex is offset by a sine/cosine wave driven by its
   * position and a time value, creating organic deformation effects.</p>
   * 
   * @param ch the character
   * @param fontSize the font size
   * @param amplitude displacement amount in pixels
   * @param frequency wave frequency
   * @param time animation time (e.g. frameCount * 0.02)
   * @return a deformed PShape
   */
  public PShape extractDeformed(char ch, float fontSize, 
                                 float amplitude, float frequency, float time) {
    List<PVector[]> contours = getContours(ch, fontSize);
    return buildDeformedShape(contours, amplitude, frequency, time);
  }
  
  /**
   * Extracts a string with wave-based vertex displacement.
   * 
   * @param text the text string
   * @param fontSize the font size
   * @param amplitude displacement amount in pixels
   * @param frequency wave frequency
   * @param time animation time
   * @return a deformed PShape
   */
  public PShape extractDeformed(String text, float fontSize, 
                                 float amplitude, float frequency, float time) {
    List<PVector[]> contours = getContours(text, fontSize);
    return buildDeformedShape(contours, amplitude, frequency, time);
  }
  
  // ── Resampling ─────────────────────────────────────────────────
  
  /**
   * Resamples a contour to have evenly spaced points.
   * 
   * @param points the original contour points
   * @param count target number of output points
   * @return resampled points with even spacing
   */
  public PVector[] resample(PVector[] points, int count) {
    if (points.length < 2 || count < 2) return points;
    
    // Calculate total length
    float totalLen = 0;
    for (int i = 1; i < points.length; i++) {
      totalLen += PVector.dist(points[i - 1], points[i]);
    }
    
    float step = totalLen / (count - 1);
    PVector[] result = new PVector[count];
    result[0] = points[0].copy();
    
    int srcIdx = 0;
    float accumulated = 0;
    float segStart = 0;
    
    for (int i = 1; i < count; i++) {
      float target = i * step;
      
      while (srcIdx < points.length - 1) {
        float segLen = PVector.dist(points[srcIdx], points[srcIdx + 1]);
        if (segStart + segLen >= target) {
          float t = (target - segStart) / segLen;
          result[i] = PVector.lerp(points[srcIdx], points[srcIdx + 1], t);
          break;
        }
        segStart += segLen;
        srcIdx++;
      }
      
      if (result[i] == null) {
        result[i] = points[points.length - 1].copy();
      }
    }
    
    return result;
  }
  
  // ── Bounding box ───────────────────────────────────────────────
  
  /**
   * Gets the bounding box of a character glyph.
   * 
   * @param ch the character
   * @param fontSize the font size
   * @return float array {x, y, width, height}
   */
  public float[] getBounds(char ch, float fontSize) {
    Font sized = awtFont.deriveFont(fontSize);
    GlyphVector gv = sized.createGlyphVector(frc, new char[]{ch});
    java.awt.geom.Rectangle2D r = gv.getOutline().getBounds2D();
    return new float[]{(float) r.getX(), (float) r.getY(), 
                       (float) r.getWidth(), (float) r.getHeight()};
  }
  
  /**
   * Gets the bounding box of a text string.
   * 
   * @param text the text string
   * @param fontSize the font size
   * @return float array {x, y, width, height}
   */
  public float[] getBounds(String text, float fontSize) {
    Font sized = awtFont.deriveFont(fontSize);
    GlyphVector gv = sized.createGlyphVector(frc, text);
    java.awt.geom.Rectangle2D r = gv.getOutline().getBounds2D();
    return new float[]{(float) r.getX(), (float) r.getY(), 
                       (float) r.getWidth(), (float) r.getHeight()};
  }

  /**
   * Returns the drawing origin that visually centres {@code ch} at {@code (cx, cy)}.
   *
   * <p>The dominant boilerplate pattern in glyph-drawing sketches is:
   * <pre>
   * float[] b = glyph.getBounds(ch, fontSize);
   * float ox  = cx - b[0] - b[2] / 2;
   * float oy  = cy - b[1] - b[3] / 2;
   * </pre>
   * This method encapsulates that calculation so the sketch can simply write:
   * <pre>
   * PVector o = glyph.centerOf('A', 600, width / 2, height / 2);
   * // then offset every point: ellipse(o.x + pts[i].x, o.y + pts[i].y, sz, sz);
   * </pre></p>
   *
   * @param ch       the character
   * @param fontSize the font size
   * @param cx       the desired canvas centre x
   * @param cy       the desired canvas centre y
   * @return a {@code PVector} whose x/y is the drawing origin to pass to each point
   */
  public PVector centerOf(char ch, float fontSize, float cx, float cy) {
    float[] b = getBounds(ch, fontSize);
    return new PVector(cx - b[0] - b[2] / 2f, cy - b[1] - b[3] / 2f);
  }

  /**
   * Returns the drawing origin that visually centres the morphed letterform at {@code (cx, cy)}.
   *
   * <p>Equivalent to {@link #centerOf} but lerps the bounding boxes of both characters
   * at parameter {@code t}, so the visual centre tracks the morph smoothly:
   * <pre>
   * PVector o = glyph.morphCenterOf('A', 'R', 600, t, width / 2, height / 2);
   * PShape  s = glyph.morphShape('A', 'R', 600, t);
   * shape(s, o.x, o.y);
   * </pre></p>
   *
   * @param charA    the source character
   * @param charB    the target character
   * @param fontSize the font size
   * @param t        interpolation parameter in [0.0, 1.0]
   * @param cx       the desired canvas centre x
   * @param cy       the desired canvas centre y
   * @return a {@code PVector} drawing origin centred between the two glyphs at {@code t}
   */
  public PVector morphCenterOf(char charA, char charB, float fontSize,
                               float t, float cx, float cy) {
    float[] bA = getBounds(charA, fontSize);
    float[] bB = getBounds(charB, fontSize);
    float ox = cx - bA[0] - (bA[2] + (bB[2] - bA[2]) * t) / 2f;
    float oy = cy - bA[1] - (bA[3] + (bB[3] - bA[3]) * t) / 2f;
    return new PVector(ox, oy);
  }

  /**
   * Draws a character centred at {@code (cx, cy)} using the sketch's current fill and stroke.
   *
   * <p>Eliminates the extract &rarr; getBounds &rarr; pushMatrix &rarr; translate &rarr;
   * shape &rarr; popMatrix ceremony so the sketch reads as design intent:
   * <pre>
   * fill(255, 80, 60);
   * glyph.drawAt('A', 600, width / 2, height / 2);
   * </pre></p>
   *
   * @param ch       the character to draw
   * @param fontSize the font size
   * @param cx       the canvas x coordinate to centre the glyph on
   * @param cy       the canvas y coordinate to centre the glyph on
   */
  public void drawAt(char ch, float fontSize, float cx, float cy) {
    PShape s = extractChar(ch, fontSize);
    s.disableStyle();   // respect sketch fill/stroke rather than PShape defaults
    PVector o = centerOf(ch, fontSize, cx, cy);
    parent.pushMatrix();
    parent.translate(o.x, o.y);
    parent.shape(s, 0, 0);
    parent.popMatrix();
  }

  // ── Designer utilities (v0.2.2) ────────────────────────────────
  
  /**
   * Fills a character outline with randomly distributed interior points.
   *
   * <p>Uses the AWT shape's built-in containment test so every returned
   * point is guaranteed to lie inside the closed letterform. Counter-forms
   * (e.g. the hole in 'O') are correctly excluded.</p>
   *
   * @param ch       the character to fill
   * @param fontSize the font size
   * @param count    the target number of interior points
   * @return array of PVector points scattered inside the letterform
   */
  public PVector[] fillWithPoints(char ch, float fontSize, int count) {
    Font sized = awtFont.deriveFont(fontSize);
    GlyphVector gv = sized.createGlyphVector(frc, new char[]{ch});
    return sampleInteriorPoints(gv.getOutline(), count);
  }
  
  /**
   * Fills a string outline with randomly distributed interior points.
   *
   * @param text     the text string
   * @param fontSize the font size
   * @param count    the target number of interior points
   * @return array of PVector points scattered inside the letterforms
   */
  public PVector[] fillWithPoints(String text, float fontSize, int count) {
    Font sized = awtFont.deriveFont(fontSize);
    GlyphVector gv = sized.createGlyphVector(frc, text);
    return sampleInteriorPoints(gv.getOutline(), count);
  }
  
  /**
   * Distributes N points evenly by arc length along the full perimeter.
   *
   * <p>Unlike {@link #getContourPoints(char, float)}, which returns raw
   * tessellation vertices (non-uniform spacing), this method resamples
   * by arc length so every neighbouring point is exactly
   * {@code totalPerimeter/count} apart. Useful for necklace-of-dots
   * effects, flow-field seeding, and stroke-based rendering.</p>
   *
   * @param ch       the character
   * @param fontSize the font size
   * @param count    the number of evenly spaced perimeter points
   * @return array of evenly distributed PVector points along the outline
   */
  public PVector[] distributeAlongOutline(char ch, float fontSize, int count) {
    Font sized = awtFont.deriveFont(fontSize);
    GlyphVector gv = sized.createGlyphVector(frc, new char[]{ch});
    return resamplePerContour(extractContours(gv.getOutline()), count);
  }
  
  /**
   * Distributes N points evenly by arc length along a string's full perimeter.
   *
   * @param text     the text string
   * @param fontSize the font size
   * @param count    the number of evenly spaced perimeter points
   * @return array of evenly distributed PVector points along the outline
   */
  public PVector[] distributeAlongOutline(String text, float fontSize, int count) {
    Font sized = awtFont.deriveFont(fontSize);
    GlyphVector gv = sized.createGlyphVector(frc, text);
    return resamplePerContour(extractContours(gv.getOutline()), count);
  }
  
  /**
   * Returns only the outer boundary contour of a character.
   *
   * <p>For letters with counter-forms (e.g. 'O', 'B', 'P', 'R'), this
   * returns the outermost closed path, identified as the contour with the
   * largest enclosed area via the shoelace formula. Inner holes are excluded.</p>
   *
   * @param ch       the character
   * @param fontSize the font size
   * @return the outer contour as an array of PVectors, or an empty array
   */
  public PVector[] getOuterContour(char ch, float fontSize) {
    return largestContour(getContours(ch, fontSize));
  }
  
  /**
   * Returns the inner contours (counter-forms, holes) of a character.
   *
   * <p>The inner contours are all closed paths except the one with the
   * largest enclosed area. For letters without counters (e.g. 'L', 'V'),
   * this returns an empty list.</p>
   *
   * @param ch       the character
   * @param fontSize the font size
   * @return list of inner contours, each as an array of PVectors
   */
  public List<PVector[]> getInnerContours(char ch, float fontSize) {
    return allButLargest(getContours(ch, fontSize));
  }
  
  // ── Designer utilities (v0.2.3) ────────────────────────────────

  /**
   * Fills the interior of a character with hatch lines at a given angle and spacing.
   *
   * <p>Lines are clipped to the closed letterform interior using AWT's {@code Area}
   * boolean intersection, so counter-forms (e.g. the hole in 'O') are correctly
   * excluded. The result is returned as an array of endpoint pairs suitable for
   * Processing's {@code line()} call:</p>
   * <pre>
   * float[][] segs = glyph.fillWithLines('A', 600, 45, 8);
   * for (float[] seg : segs) line(ox+seg[0], oy+seg[1], ox+seg[2], oy+seg[3]);
   * </pre>
   *
   * @param ch       the character to fill
   * @param fontSize the font size
   * @param angle    hatch angle in degrees (0 = horizontal, 90 = vertical)
   * @param spacing  distance between hatch lines in pixels
   * @return array of {x1, y1, x2, y2} endpoint pairs; never {@code null}
   */
  public float[][] fillWithLines(char ch, float fontSize, float angle, float spacing) {
    Font sized = awtFont.deriveFont(fontSize);
    GlyphVector gv = sized.createGlyphVector(frc, new char[]{ch});
    return clipHatchLines(gv.getOutline(), angle, spacing);
  }

  /**
   * Fills the interior of a string outline with hatch lines.
   *
   * @param text     the text string
   * @param fontSize the font size
   * @param angle    hatch angle in degrees
   * @param spacing  distance between lines in pixels
   * @return array of {x1, y1, x2, y2} endpoint pairs
   */
  public float[][] fillWithLines(String text, float fontSize, float angle, float spacing) {
    Font sized = awtFont.deriveFont(fontSize);
    GlyphVector gv = sized.createGlyphVector(frc, text);
    return clipHatchLines(gv.getOutline(), angle, spacing);
  }

  /**
   * Expands or contracts the letterform outline by a fixed distance.
   *
   * <p>Positive {@code distance} grows the outline (inline → outline effect);
   * negative {@code distance} shrinks it. Uses AWT's {@code BasicStroke} to
   * create a stroked version of the outline, then intersects/unions with the
   * original Area to produce the offset shape.</p>
   *
   * @param ch       the character
   * @param fontSize the font size
   * @param distance offset distance in pixels (positive = expand, negative = shrink)
   * @return array of PVector points along the offset contour vertices
   */
  public PVector[] offsetOutline(char ch, float fontSize, float distance) {
    Font sized = awtFont.deriveFont(fontSize);
    GlyphVector gv = sized.createGlyphVector(frc, new char[]{ch});
    Shape outline = gv.getOutline();
    return computeOffsetOutline(outline, distance);
  }

  /**
   * Morphs vertex by vertex between the outlines of two characters at parameter {@code t}.
   *
   * <p>Both characters are resampled to the same number of points (whichever
   * outline has more raw vertices, ensuring no points are dropped) so they can
   * be linearly interpolated. At
   * {@code t=0.0} the result is character {@code charA}; at {@code t=1.0} it is
   * character {@code charB}; intermediate values blend the two shapes.</p>
   *
   * <pre>
   * // Animate A → B
   * float t = (sin(frameCount * 0.03) + 1) * 0.5;
   * PVector[] morph = glyph.interpolateTo('A', 'B', 600, t);
   * </pre>
   *
   * @param charA    the source character
   * @param charB    the target character
   * @param fontSize the font size (applied to both characters)
   * @param t        interpolation parameter in [0.0, 1.0]
   * @return interpolated contour as an array of PVectors
   */
  public PVector[] interpolateTo(char charA, char charB, float fontSize, float t) {
    t = Math.max(0, Math.min(1, t));
    Font sized = awtFont.deriveFont(fontSize);
    GlyphVector gvA = sized.createGlyphVector(frc, new char[]{charA});
    GlyphVector gvB = sized.createGlyphVector(frc, new char[]{charB});
    PVector[] ptsA = extractPoints(gvA.getOutline());
    PVector[] ptsB = extractPoints(gvB.getOutline());
    int count = Math.max(ptsA.length, ptsB.length);   // use MAX so no points are dropped
    if (count == 0) return new PVector[0];
    ptsA = resample(ptsA, count);
    ptsB = resample(ptsB, count);
    PVector[] result = new PVector[count];
    for (int i = 0; i < count; i++) {
      result[i] = PVector.lerp(ptsA[i], ptsB[i], t);
    }
    return result;
  }

  /**
   * Returns a ready-to-draw {@code PShape} morphing from {@code charA} to {@code charB}.
   *
   * <p>Handles all hole rendering automatically: counter-forms (the eye of 'R', the bowl
   * of 'B', etc.) are preserved throughout the morph using Processing's built-in
   * {@code beginContour()} / {@code endContour()} mechanism. When contour counts differ
   * (e.g. 'A' has a triangular hole, 'C' does not), the extra contour collapses to its
   * own centroid so both endpoints are always complete, fully-formed characters.</p>
   *
   * <p>This is the recommended method for most morph use cases — just call
   * {@code shape()} on the result:</p>
   *
   * <pre>
   * float t = (sin(frameCount * 0.03) + 1) * 0.5;
   * PShape s = glyph.morphShape('A', 'R', 600, t);
   * shape(s, ox, oy);
   * </pre>
   *
   * @param charA    the source character
   * @param charB    the target character
   * @param fontSize the font size (applied to both characters)
   * @param t        interpolation parameter in [0.0, 1.0]
   * @return a {@code PShape} ready to draw with {@code shape()}
   */
  public PShape morphShape(char charA, char charB, float fontSize, float t) {
    List<PVector[]> contours = interpolateContours(charA, charB, fontSize, t);
    PShape s = parent.createShape();
    s.beginShape();
    for (int i = 0; i < contours.size(); i++) {
      PVector[] c = contours.get(i);
      if (i == 0) {
        for (PVector p : c) s.vertex(p.x, p.y);
      } else {
        s.beginContour();
        for (PVector p : c) s.vertex(p.x, p.y);
        s.endContour();
      }
    }
    s.endShape(PConstants.CLOSE);
    return s;
  }

  /**
   * Returns per-contour interpolated point arrays morphing from {@code charA} to {@code charB}.
   *
   * <p>Like {@link #morphShape} but exposes raw point arrays, giving full control over
   * rendering — useful for dot clouds, particle effects, or custom stroke work.
   * Index 0 is always the outer boundary; indices 1+ are counter-forms (holes).</p>
   *
   * <p>Contour counts are reconciled automatically: when one character has more contours
   * than the other, the missing contour is represented as a collapsed point cloud at
   * the centroid of the corresponding real contour, so holes open and close gracefully
   * and both endpoints remain complete, fully-formed characters.</p>
   *
   * <pre>
   * float t = (sin(frameCount * 0.03) + 1) * 0.5;
   * List&lt;PVector[]&gt; contours = glyph.interpolateContours('A', 'R', 600, t);
   * for (PVector[] contour : contours) {
   *   beginShape();
   *   for (PVector p : contour) vertex(ox + p.x, oy + p.y);
   *   endShape(CLOSE);
   * }
   * </pre>
   *
   * @param charA    the source character
   * @param charB    the target character
   * @param fontSize the font size (applied to both characters)
   * @param t        interpolation parameter in [0.0, 1.0]
   * @return list of interpolated contours; index 0 = outer boundary, 1+ = holes
   */
  public List<PVector[]> interpolateContours(char charA, char charB, float fontSize, float t) {
    t = Math.max(0, Math.min(1, t));
    List<PVector[]> ca = getContours(charA, fontSize);
    List<PVector[]> cb = getContours(charB, fontSize);
    int n = Math.max(ca.size(), cb.size());
    List<PVector[]> result = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      PVector[] pa = (i < ca.size()) ? ca.get(i) : null;
      PVector[] pb = (i < cb.size()) ? cb.get(i) : null;
      // Missing contour: collapse to the centroid of the contour that exists
      // so holes open or close gracefully rather than abruptly appearing/vanishing.
      if (pa == null) pa = glyphCentroidCollapsed(pb, pb.length);
      if (pb == null) pb = glyphCentroidCollapsed(pa, pa.length);
      int cnt = Math.max(pa.length, pb.length);
      if (cnt < 3) continue;
      pa = resample(pa, cnt);
      pb = resample(pb, cnt);
      PVector[] m = new PVector[cnt];
      for (int j = 0; j < cnt; j++) {
        m[j] = PVector.lerp(pa[j], pb[j], t);
      }
      result.add(m);
    }
    return result;
  }

  // Returns an array of `count` copies of the centroid of pts.
  // Used to represent a "zero-area" collapsed contour when a character has
  // fewer holes than its morph partner, so holes appear/disappear smoothly.
  private PVector[] glyphCentroidCollapsed(PVector[] pts, int count) {
    float cx = 0, cy = 0;
    for (PVector p : pts) { cx += p.x; cy += p.y; }
    PVector center = new PVector(cx / pts.length, cy / pts.length);
    PVector[] collapsed = new PVector[count];
    for (int i = 0; i < count; i++) collapsed[i] = center.copy();
    return collapsed;
  }

  /**
   * Returns an approximate medial axis (spine) of the letterform.
   *
   * <p>The medial axis is computed by distributing sample points evenly along
   * the outer contour, then for each point finding the closest point on the
   * opposite side of the outline and taking their midpoint. The result is a set
   * of points tracing the visual centre/backbone of the letterform — useful for
   * calligraphic rendering where a variable-width stroke is drawn along the axis.</p>
   *
   * @param ch       the character
   * @param fontSize the font size
   * @param samples  number of axis sample points (more = smoother but slower)
   * @return array of PVectors approximating the medial axis
   */
  public PVector[] getMedialAxis(char ch, float fontSize, int samples) {
    Font sized = awtFont.deriveFont(fontSize);
    GlyphVector gv = sized.createGlyphVector(frc, new char[]{ch});
    Shape outline = gv.getOutline();
    return approximateMedialAxis(outline, samples);
  }

  /**
   * Returns a single point at a normalised arc-length position along the outline.
   *
   * <p>Useful for animating objects that travel along a letterform's perimeter:
   * pass {@code frameCount * 0.005 % 1.0} as {@code t} and the returned point
   * advances steadily around the entire outline.</p>
   *
   * <pre>
   * // Particle orbiting the letter O
   * PVector pt = glyph.sampleAlongPath('O', 600, frameCount * 0.005 % 1.0);
   * ellipse(ox + pt.x, oy + pt.y, 8, 8);
   * </pre>
   *
   * @param ch       the character
   * @param fontSize the font size
   * @param t        normalised arc-length position in [0.0, 1.0]
   * @return the point on the outline at that position
   */
  public PVector sampleAlongPath(char ch, float fontSize, float t) {
    t = Math.max(0, Math.min(1, t));
    PVector[] pts = distributeAlongOutline(ch, fontSize, 512);
    if (pts.length == 0) return new PVector();
    // Arc-length distribution is uniform; index directly.
    int idx = (int)(t * pts.length) % pts.length;
    return pts[idx].copy();
  }

  /**
   * Returns a single point at a normalised arc-length position along a string's outline.
   *
   * @param text     the text string
   * @param fontSize the font size
   * @param t        normalised arc-length position in [0.0, 1.0]
   * @return the point on the outline at that position
   */
  public PVector sampleAlongPath(String text, float fontSize, float t) {
    t = Math.max(0, Math.min(1, t));
    PVector[] pts = distributeAlongOutline(text, fontSize, 512);
    if (pts.length == 0) return new PVector();
    int idx = (int)(t * pts.length) % pts.length;
    return pts[idx].copy();
  }

  /**
   * Returns the union outline of a sequence of characters as a single closed path.
   *
   * <p>Overlapping characters are merged; the result is the outer boundary of
   * the combined letterform group. Treat a whole word or character sequence as
   * one unified shape. Characters are laid out left-to-right with the spacing
   * the font engine supplies.</p>
   *
   * @param chars    array of characters forming the group
   * @param fontSize the font size
   * @param tracking additional spacing between characters in pixels (can be 0)
   * @return outer contour of the merged shape as an array of PVectors
   */
  public PVector[] getBoundingContour(char[] chars, float fontSize, float tracking) {
    if (chars == null || chars.length == 0) return new PVector[0];
    Font sized = awtFont.deriveFont(fontSize);
    // Build a string from the chars and lay it out
    String text = new String(chars);
    GlyphVector gv = sized.createGlyphVector(frc, text);
    // Apply tracking by shifting each glyph past the first
    Area union = new Area();
    float xOffset = 0;
    for (int i = 0; i < chars.length; i++) {
      GlyphVector single = sized.createGlyphVector(frc, new char[]{chars[i]});
      Shape glyphShape = single.getOutline();
      AffineTransform tx = AffineTransform.getTranslateInstance(xOffset, 0);
      Area glyphArea = new Area(tx.createTransformedShape(glyphShape));
      union.add(glyphArea);
      xOffset += single.getGlyphMetrics(0).getAdvance() + tracking;
    }
    // Extract the outer (largest) contour of the union
    List<PVector[]> contours = extractContours(union);
    return largestContour(contours);
  }

  // ── Fluent Builder ─────────────────────────────────────────────

  /**
   * Returns a {@link GlyphBuilder} — a chainable builder that removes boilerplate
   * from common glyph-extraction patterns.
   *
   * <pre>
   * // Draw 800 interior points of 'A' at size 600, coloured by position:
   * glyph.of('A', 600)
   *      .fillPoints(800)
   *      .colourByPosition(this)
   *      .draw(this, width/2, height/2);
   * </pre>
   *
   * @param ch       the character
   * @param fontSize font size in points
   * @return a fresh {@code GlyphBuilder} for the given character
   */
  public GlyphBuilder of(char ch, float fontSize) {
    return new GlyphBuilder(this, ch, fontSize);
  }

  /**
   * Chainable builder for common glyph-extraction draw patterns.
   *
   * <p>Obtain an instance via {@link GlyphExtractor#of(char, float)}.
   * Call terminal methods ({@link #draw(PApplet, float, float)},
   * {@link #getPoints()}) to consume the built state.</p>
   */
  public static class GlyphBuilder {

    private final GlyphExtractor ge;
    private final char ch;
    private final float fontSize;

    // What to draw
    private PVector[] pts;
    private float[][] lines;
    private boolean outlineOnly = false;
    private boolean outerOnly   = false;

    // Style
    private float noiseTime  = 0;
    private float noiseAmp   = 0;
    private boolean coloured = false;
    private float ptSize     = 3;
    private float hueStart   = 200;
    private float hueEnd     = 340;

    GlyphBuilder(GlyphExtractor ge, char ch, float fontSize) {
      this.ge = ge;
      this.ch = ch;
      this.fontSize = fontSize;
    }

    /** Fill interior with {@code count} randomly scattered points. */
    public GlyphBuilder fillPoints(int count) {
      pts = ge.fillWithPoints(ch, fontSize, count);
      return this;
    }

    /** Use the evenly distributed perimeter points. */
    public GlyphBuilder outlinePoints(int count) {
      pts = ge.distributeAlongOutline(ch, fontSize, count);
      outlineOnly = true;
      return this;
    }

    /** Use only the outer contour points. */
    public GlyphBuilder outerContour() {
      pts = ge.getOuterContour(ch, fontSize);
      outerOnly = true;
      return this;
    }

    /**
     * Add hatch-line fill at the given angle and spacing.
     * Can be combined with point modes; both will be drawn.
     */
    public GlyphBuilder hatch(float angle, float spacing) {
      lines = ge.fillWithLines(ch, fontSize, angle, spacing);
      return this;
    }

    /** Animate points with Perlin-noise displacement (applied at draw time). */
    public GlyphBuilder animateWithNoise(float time, float amplitude) {
      this.noiseTime = time;
      this.noiseAmp  = amplitude;
      return this;
    }

    /** Colour points by their index position (HSB hue sweep). */
    public GlyphBuilder colourByPosition() {
      this.coloured = true;
      return this;
    }

    /** Override the HSB hue range for position colouring. */
    public GlyphBuilder hueRange(float start, float end) {
      this.hueStart = start;
      this.hueEnd   = end;
      return this;
    }

    /** Override the rendered point diameter. */
    public GlyphBuilder pointSize(float size) {
      this.ptSize = size;
      return this;
    }

    /**
     * Returns the current point array so callers can do their own rendering.
     * Returns {@code null} if no point mode was configured.
     */
    public PVector[] getPoints() { return pts; }

    /**
     * Returns the current hatch-line segments or {@code null} if none configured.
     */
    public float[][] getLines() { return lines; }

    /**
     * Terminal: renders everything into the sketch using centred offset.
     *
     * @param p  the PApplet to draw into
     * @param cx horizontal centre of the glyph in sketch coordinates
     * @param cy vertical centre of the glyph in sketch coordinates
     */
    public void draw(PApplet p, float cx, float cy) {
      float[] b = ge.getBounds(ch, fontSize);
      float ox = cx - b[0] - b[2] / 2;
      float oy = cy - b[1] - b[3] / 2;

      // Draw hatch lines first (background layer)
      if (lines != null) {
        p.colorMode(PApplet.HSB, 360, 255, 255);
        p.stroke(coloured ? hueStart : 0, coloured ? 200 : 0, 200, 180);
        p.strokeWeight(1);
        for (float[] seg : lines) {
          p.line(ox + seg[0], oy + seg[1], ox + seg[2], oy + seg[3]);
        }
      }

      // Draw points
      if (pts != null) {
        p.colorMode(PApplet.HSB, 360, 255, 255);
        p.noStroke();
        for (int i = 0; i < pts.length; i++) {
          float x = ox + pts[i].x;
          float y = oy + pts[i].y;

          if (noiseAmp > 0) {
            float nx = (float)(Math.sin(pts[i].x * 0.01 + noiseTime) * noiseAmp);
            float ny = (float)(Math.cos(pts[i].y * 0.01 + noiseTime) * noiseAmp);
            x += nx; y += ny;
          }

          if (coloured) {
            float hue = PApplet.map(i, 0, pts.length, hueStart, hueEnd) % 360;
            p.fill(hue, 200, 255, 220);
          } else {
            p.fill(255);
          }
          p.ellipse(x, y, ptSize, ptSize);
        }
      }
    }
  }

  // ── Internal v0.2.3 helpers ────────────────────────────────────

  /**
   * Clips evenly-spaced hatch lines to the interior of an AWT shape.
   * Lines are produced in the shape's coordinate space (same as the
   * glyph outline) at the given angle and spacing.
   */
  private float[][] clipHatchLines(Shape shape, float angleDeg, float spacing) {
    Rectangle2D bounds = shape.getBounds2D();
    if (bounds.isEmpty()) return new float[0][];

    double rad   = Math.toRadians(angleDeg);
    double cosA  = Math.cos(rad);
    double sinA  = Math.sin(rad);
    double diag  = Math.sqrt(bounds.getWidth() * bounds.getWidth() +
                             bounds.getHeight() * bounds.getHeight());
    double cx    = bounds.getCenterX();
    double cy    = bounds.getCenterY();

    Area area = new Area(shape);
    List<float[]> segments = new ArrayList<>();

    // Generate parallel lines in rotated space, clipped to the bounding box,
    // then intersect with the actual glyph area.
    double halfLines = diag / spacing + 1;
    for (double i = -halfLines; i <= halfLines; i++) {
      // Line perpendicular-offset i*spacing from centre, then long enough to span the glyph.
      double px = cx + i * spacing * (-sinA);
      double py = cy + i * spacing * cosA;
      double x1 = px - cosA * diag;
      double y1 = py - sinA * diag;
      double x2 = px + cosA * diag;
      double y2 = py + sinA * diag;

      // Intersect this infinite line with the glyph area
      // by stroking a thin rectangle and intersecting Areas.
      Path2D.Double linePath = new Path2D.Double();
      linePath.moveTo(x1, y1);
      linePath.lineTo(x2, y2);

      // Stroke the line to a thin rectangle, then intersect with the glyph
      BasicStroke stroke = new BasicStroke(0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
      Area lineArea = new Area(stroke.createStrokedShape(linePath));
      lineArea.intersect(area);

      if (!lineArea.isEmpty()) {
        // Collect the clipped segments from the intersection
        collectLineSegments(lineArea, segments);
      }
    }

    return segments.toArray(new float[0][]);
  }

  /**
   * Walks the path of an Area and emits line segments (pairs of connected vertices).
   */
  private void collectLineSegments(Area area, List<float[]> out) {
    PathIterator pi = area.getPathIterator(null, flatness * 2.0);
    float[] coords = new float[6];
    float mx = 0, my = 0, lx = 0, ly = 0;
    boolean open = false;
    while (!pi.isDone()) {
      int type = pi.currentSegment(coords);
      switch (type) {
        case PathIterator.SEG_MOVETO:
          mx = coords[0]; my = coords[1];
          lx = mx;        ly = my;
          open = true;
          break;
        case PathIterator.SEG_LINETO:
          if (open) out.add(new float[]{lx, ly, coords[0], coords[1]});
          lx = coords[0]; ly = coords[1];
          break;
        case PathIterator.SEG_CLOSE:
          if (open) out.add(new float[]{lx, ly, mx, my});
          open = false;
          break;
      }
      pi.next();
    }
  }

  /**
   * Computes an offset outline using a stroke-expand or area-shrink approach.
   */
  private PVector[] computeOffsetOutline(Shape outline, float distance) {
    Area original = new Area(outline);
    Shape stroked;
    if (distance >= 0) {
      // Expand: stroke around the shape and union
      BasicStroke stroke = new BasicStroke(distance * 2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
      stroked = stroke.createStrokedShape(outline);
      Area expanded = new Area(stroked);
      expanded.add(original);
      return largestContour(extractContours(expanded));
    } else {
      // Shrink: stroke around with a negative (interior) width approximated
      // by eroding using the Minkowski approach via stroke + subtract.
      float abs = Math.abs(distance) * 2;
      BasicStroke stroke = new BasicStroke(abs, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
      Area stroked2 = new Area(stroke.createStrokedShape(outline));
      Area shrunk = new Area(original);
      shrunk.subtract(stroked2);
      if (shrunk.isEmpty()) return new PVector[0];
      return largestContour(extractContours(shrunk));
    }
  }

  /**
   * Approximates the medial axis by sampling opposite-side midpoints.
   */
  private PVector[] approximateMedialAxis(Shape outline, int samples) {
    // Get uniformly distributed perimeter points
    List<PVector[]> contours = extractContours(outline);
    PVector[] perimeter = resamplePerContour(contours, samples * 4);
    if (perimeter.length < 4) return new PVector[0];

    List<PVector> axis = new ArrayList<>();
    // For each point on the first half, find the closest point in the second half
    int half = perimeter.length / 2;
    for (int i = 0; i < half; i++) {
      PVector p = perimeter[i];
      // Search the opposite half for the closest point
      float minDist = Float.MAX_VALUE;
      PVector closest = null;
      for (int j = half; j < perimeter.length; j++) {
        float d = PVector.dist(p, perimeter[j]);
        if (d < minDist) {
          minDist = d;
          closest = perimeter[j];
        }
      }
      if (closest != null) {
        axis.add(PVector.lerp(p, closest, 0.5f));
      }
    }
    return axis.toArray(new PVector[0]);
  }

  /**
   * Extracts contours from an AWT {@code Area} (same logic as from a {@code Shape}).
   */
  private List<PVector[]> extractContours(Area area) {
    return extractContours((Shape) area);
  }

  // ── Internal conversion ────────────────────────────────────────
  
  /**
   * Converts a Java AWT Shape to a Processing PShape.
   */
  private PShape shapeToProcessing(Shape awtShape) {
    PShape group = parent.createShape(PConstants.GROUP);
    
    PathIterator pi = new FlatteningPathIterator(
        awtShape.getPathIterator(null), flatness);
    
    float[] coords = new float[6];
    PShape currentContour = null;
    
    while (!pi.isDone()) {
      int type = pi.currentSegment(coords);
      
      switch (type) {
        case PathIterator.SEG_MOVETO:
          // Start a new contour
          if (currentContour != null) {
            currentContour.endShape(PConstants.CLOSE);
            group.addChild(currentContour);
          }
          currentContour = parent.createShape();
          currentContour.beginShape();
          currentContour.noStroke();
          currentContour.fill(255);
          currentContour.vertex(coords[0], coords[1]);
          break;
          
        case PathIterator.SEG_LINETO:
          if (currentContour != null) {
            currentContour.vertex(coords[0], coords[1]);
          }
          break;
          
        case PathIterator.SEG_CLOSE:
          // Contour will be closed in endShape(CLOSE)
          break;
      }
      
      pi.next();
    }
    
    // Close last contour
    if (currentContour != null) {
      currentContour.endShape(PConstants.CLOSE);
      group.addChild(currentContour);
    }
    
    return group;
  }
  
  /**
   * Extracts all points from a flattened AWT Shape.
   */
  private PVector[] extractPoints(Shape awtShape) {
    List<PVector> points = new ArrayList<>();
    
    PathIterator pi = new FlatteningPathIterator(
        awtShape.getPathIterator(null), flatness);
    float[] coords = new float[6];
    
    while (!pi.isDone()) {
      int type = pi.currentSegment(coords);
      if (type == PathIterator.SEG_MOVETO || type == PathIterator.SEG_LINETO) {
        points.add(new PVector(coords[0], coords[1]));
      }
      pi.next();
    }
    
    return points.toArray(new PVector[0]);
  }
  
  /**
   * Extracts separated contours from a flattened AWT Shape.
   */
  private List<PVector[]> extractContours(Shape awtShape) {
    List<PVector[]> contours = new ArrayList<>();
    List<PVector> current = new ArrayList<>();
    
    PathIterator pi = new FlatteningPathIterator(
        awtShape.getPathIterator(null), flatness);
    float[] coords = new float[6];
    
    while (!pi.isDone()) {
      int type = pi.currentSegment(coords);
      
      switch (type) {
        case PathIterator.SEG_MOVETO:
          if (!current.isEmpty()) {
            contours.add(current.toArray(new PVector[0]));
            current = new ArrayList<>();
          }
          current.add(new PVector(coords[0], coords[1]));
          break;
          
        case PathIterator.SEG_LINETO:
          current.add(new PVector(coords[0], coords[1]));
          break;
          
        case PathIterator.SEG_CLOSE:
          if (!current.isEmpty()) {
            contours.add(current.toArray(new PVector[0]));
            current = new ArrayList<>();
          }
          break;
      }
      
      pi.next();
    }
    
    if (!current.isEmpty()) {
      contours.add(current.toArray(new PVector[0]));
    }
    
    return contours;
  }
  
  /**
   * Builds a PShape from contours with wave-based displacement.
   */
  private PShape buildDeformedShape(List<PVector[]> contours,
                                     float amplitude, float frequency, float time) {
    PShape group = parent.createShape(PConstants.GROUP);
    
    for (PVector[] contour : contours) {
      PShape s = parent.createShape();
      s.beginShape();
      s.noStroke();
      s.fill(255);
      
      for (int i = 0; i < contour.length; i++) {
        PVector p = contour[i];
        float angle = (p.x + p.y) * frequency + time;
        float dx = PApplet.sin(angle) * amplitude;
        float dy = PApplet.cos(angle * 0.7f) * amplitude;
        s.vertex(p.x + dx, p.y + dy);
      }
      
      s.endShape(PConstants.CLOSE);
      group.addChild(s);
    }
    
    return group;
  }
  
  /**
   * Samples random points inside an AWT shape using rejection sampling.
   * Uses the shape's built-in containment test; counters are excluded automatically.
   */
  private PVector[] sampleInteriorPoints(Shape shape, int count) {
    java.awt.geom.Rectangle2D bounds = shape.getBounds2D();
    float bx = (float) bounds.getX();
    float by = (float) bounds.getY();
    float bw = (float) bounds.getWidth();
    float bh = (float) bounds.getHeight();
    
    List<PVector> result = new ArrayList<>(count);
    int maxAttempts = count * 50; // guard against infinite loop on narrow glyphs
    int attempts = 0;
    
    while (result.size() < count && attempts < maxAttempts) {
      float x = bx + parent.random(bw);
      float y = by + parent.random(bh);
      if (shape.contains(x, y)) {
        result.add(new PVector(x, y));
      }
      attempts++;
    }
    
    return result.toArray(new PVector[0]);
  }
  
  /**
   * Resamples each contour independently, allocating points proportionally
   * by arc length, then concatenates all results. This prevents phantom
   * straight lines between separate contours (outer boundary and counters).
   */
  private PVector[] resamplePerContour(List<PVector[]> contours, int totalCount) {
    if (contours.isEmpty()) return new PVector[0];

    // Compute arc length of each contour
    float totalLen = 0;
    float[] lengths = new float[contours.size()];
    for (int i = 0; i < contours.size(); i++) {
      PVector[] c = contours.get(i);
      float len = 0;
      for (int j = 1; j < c.length; j++) {
        len += PVector.dist(c[j - 1], c[j]);
      }
      // Also close the loop: last point → first
      if (c.length > 1) len += PVector.dist(c[c.length - 1], c[0]);
      lengths[i] = len;
      totalLen += len;
    }

    // Allocate count proportionally; guarantee at least 2 per contour
    List<PVector> result = new ArrayList<>(totalCount);
    for (int i = 0; i < contours.size(); i++) {
      int n = Math.max(2, Math.round(totalCount * lengths[i] / totalLen));
      // Close the contour before resampling so the arc back to [0] is covered.
      // resample(n+1) gives n evenly-spaced points + a duplicate of [0] at the
      // tail; we keep only the first n so there is no repeated start point.
      PVector[] c = contours.get(i);
      PVector[] closed = new PVector[c.length + 1];
      System.arraycopy(c, 0, closed, 0, c.length);
      closed[c.length] = c[0].copy();
      PVector[] resampled = resample(closed, n + 1);
      for (int j = 0; j < n; j++) result.add(resampled[j]);
    }
    return result.toArray(new PVector[0]);
  }

  /**
   * Computes the signed area of a contour via the shoelace formula.
   * Positive = clockwise in screen coords (Y-down); negative = counter-clockwise.
   */
  private float signedArea(PVector[] pts) {
    float area = 0;
    int n = pts.length;
    for (int i = 0; i < n; i++) {
      PVector a = pts[i];
      PVector b = pts[(i + 1) % n];
      area += (a.x * b.y) - (b.x * a.y);
    }
    return area * 0.5f;
  }
  
  /**
   * Returns the contour with the largest absolute area (the outer boundary).
   */
  private PVector[] largestContour(List<PVector[]> contours) {
    if (contours.isEmpty()) return new PVector[0];
    PVector[] best = contours.get(0);
    float bestArea = Math.abs(signedArea(best));
    for (int i = 1; i < contours.size(); i++) {
      float a = Math.abs(signedArea(contours.get(i)));
      if (a > bestArea) {
        bestArea = a;
        best = contours.get(i);
      }
    }
    return best;
  }
  
  /**
   * Returns all contours except the one with the largest absolute area (the inner holes).
   */
  private List<PVector[]> allButLargest(List<PVector[]> contours) {
    if (contours.size() <= 1) return new ArrayList<>();
    int largestIdx = 0;
    float bestArea = Math.abs(signedArea(contours.get(0)));
    for (int i = 1; i < contours.size(); i++) {
      float a = Math.abs(signedArea(contours.get(i)));
      if (a > bestArea) {
        bestArea = a;
        largestIdx = i;
      }
    }
    List<PVector[]> result = new ArrayList<>(contours);
    result.remove(largestIdx);
    return result;
  }
}
