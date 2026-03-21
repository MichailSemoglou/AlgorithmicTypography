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
 *   <li>Boolean Area operations — merge, overlap, and punch letterforms ({@code union} / {@code intersect} / {@code subtract})</li>
 *   <li>Tangent direction at any arc-length position ({@code getTangent})</li>
 *   <li>Lay a string of glyphs along any arbitrary curve ({@code textOnPath})</li>
 *   <li>Increase tessellation density on demand ({@code subdivide})</li>
 *   <li>Dashed stroke outline — dash/gap pairs along the arc-length-resampled contour ({@code getDashedOutline})</li>
 *   <li>Typographic fingerprint derived from outline geometry ({@code getStressAxis}, {@code getOpticalCentroid}, {@code getCounterRatio}, {@code getStrokeWeight}, {@code buildTypeDNAProfile})</li>
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
 * @version 0.2.6
 * @since 1.0.0
 */

package algorithmic.typography.render;

import processing.core.*;
import processing.data.JSONObject;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
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
    return shapeToProcessing(new Area(gv.getOutline()));
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
    return shapeToProcessing(new Area(gv.getOutline()));
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
    return extractContours(new Area(gv.getOutline()));
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
    return extractContours(new Area(gv.getOutline()));
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
   * Returns the drawing origin that visually centres {@code text} at {@code (cx, cy)}.
   *
   * <p>Equivalent to {@link #centerOf(char, float, float, float)} but accepts a
   * multi-character string (e.g. a word), using the combined glyph-vector bounding
   * box so the entire word is centred at the given canvas point:</p>
   * <pre>
   * PVector o = glyph.centerOf("TYPO", 400, width / 2, height / 2);
   * PShape  s = glyph.extractString("TYPO", 400);
   * shape(s, o.x, o.y);
   * </pre>
   *
   * @param text     the text string to centre
   * @param fontSize the font size
   * @param cx       the desired canvas centre x
   * @param cy       the desired canvas centre y
   * @return a {@code PVector} whose x/y is the drawing origin
   */
  public PVector centerOf(String text, float fontSize, float cx, float cy) {
    float[] b = getBounds(text, fontSize);
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
    PVector[] outer = largestContour(getContours(ch, fontSize));
    // getContours() normalises winding via Area — outers become CCW in y-down.
    // textOnPath expects CW traversal (original TrueType convention), so reverse.
    return reversePath(outer);
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

  // ── Boolean operations (v0.2.5) ───────────────────────────────

  /**
   * Merges two letterform outlines into a single closed PShape.
   *
   * <p>Uses AWT {@code Area.add()} to compute the union of the two glyph
   * footprints. The result is the outer boundary enclosing both characters,
   * including any overlap region. Useful for ligature-style compositions and
   * overlapping-letterform designs.</p>
   *
   * <pre>
   * PShape merged = glyph.union('O', 'C', 600);
   * shape(merged, ox, oy);
   * </pre>
   *
   * @param charA    the first character
   * @param charB    the second character
   * @param fontSize the font size applied to both characters
   * @return a {@code PShape} containing the merged outline, ready to draw
   */
  public PShape union(char charA, char charB, float fontSize) {
    Area a = glyphArea(charA, fontSize);
    Area b = glyphArea(charB, fontSize);
    a.add(b);
    return shapeToProcessing(a);
  }

  /**
   * Returns the union outline of two letterforms as a raw contour array.
   *
   * <p>Same operation as {@link #union(char, char, float)} but returns the
   * outer boundary as a {@code PVector[]} for point-level effects.</p>
   *
   * @param charA    the first character
   * @param charB    the second character
   * @param fontSize the font size
   * @return the outer contour of the merged shape as an array of PVectors
   */
  public PVector[] getUnionContour(char charA, char charB, float fontSize) {
    Area a = glyphArea(charA, fontSize);
    Area b = glyphArea(charB, fontSize);
    a.add(b);
    return largestContour(extractContours(a));
  }

  /**
   * Returns a PShape containing only the region where two letterforms overlap.
   *
   * <p>Uses AWT {@code Area.intersect()}: an empty shape is returned when the
   * two outlines do not overlap at all. Useful for revealing shared geometric
   * structure and for compound-shape masking.</p>
   *
   * <pre>
   * PShape overlap = glyph.intersect('O', 'C', 600);
   * shape(overlap, ox, oy);
   * </pre>
   *
   * @param charA    the first character
   * @param charB    the second character
   * @param fontSize the font size applied to both characters
   * @return a {@code PShape} containing the intersection region
   */
  public PShape intersect(char charA, char charB, float fontSize) {
    Area a = glyphArea(charA, fontSize);
    Area b = glyphArea(charB, fontSize);
    a.intersect(b);
    return shapeToProcessing(a);
  }

  /**
   * Returns the intersection region of two letterforms as a raw contour array.
   *
   * @param charA    the first character
   * @param charB    the second character
   * @param fontSize the font size
   * @return outer contour of the intersection, or an empty array if no overlap
   */
  public PVector[] getIntersectContour(char charA, char charB, float fontSize) {
    Area a = glyphArea(charA, fontSize);
    Area b = glyphArea(charB, fontSize);
    a.intersect(b);
    return largestContour(extractContours(a));
  }

  /**
   * Cuts the second letterform out of the first, returning the remaining shape.
   *
   * <p>Uses AWT {@code Area.subtract()} (charA minus charB). The result is the
   * portion of charA that does not overlap charB — useful for knockouts,
   * compound shapes, and layered type design.</p>
   *
   * <pre>
   * PShape knocked = glyph.subtract('O', 'C', 600);
   * shape(knocked, ox, oy);
   * </pre>
   *
   * @param charA    the base character (will be cut)
   * @param charB    the cutter character (its area is removed from charA)
   * @param fontSize the font size applied to both characters
   * @return a {@code PShape} representing charA with charB punched out
   */
  public PShape subtract(char charA, char charB, float fontSize) {
    Area a = glyphArea(charA, fontSize);
    Area b = glyphArea(charB, fontSize);
    a.subtract(b);
    return shapeToProcessing(a);
  }

  /**
   * Returns the subtraction result as a raw contour array (charA minus charB).
   *
   * @param charA    the base character
   * @param charB    the cutter character
   * @param fontSize the font size
   * @return outer contour of the remaining shape, or an empty array if fully cut
   */
  public PVector[] getSubtractContour(char charA, char charB, float fontSize) {
    Area a = glyphArea(charA, fontSize);
    Area b = glyphArea(charB, fontSize);
    a.subtract(b);
    return largestContour(extractContours(a));
  }

  // ── Primitive area factories (v0.2.5) ──────────────────────────

  /**
   * Returns an AWT {@code Area} representing a circle in glyph space.
   *
   * <p>Use with the mixed boolean overloads to combine geometric primitives
   * with letterforms:
   * <pre>
   * float[] b = glyph.getBounds('A', 600);
   * float gcx = b[0] + b[2] / 2;  // glyph-space centre x
   * float gcy = b[1] + b[3] / 2;
   * Area circle = glyph.circleArea(gcx, gcy, b[3] * 0.55f);
   * PShape result = glyph.subtract(circle, 'A', 600);  // circle with A punched out
   * </pre></p>
   *
   * @param cx     centre x in glyph space
   * @param cy     centre y in glyph space
   * @param radius radius in pixels
   * @return an {@code Area} for use in mixed boolean operations
   */
  public Area circleArea(float cx, float cy, float radius) {
    double d = radius * 2.0;
    return new Area(new Ellipse2D.Double(cx - radius, cy - radius, d, d));
  }

  /**
   * Returns an AWT {@code Area} representing an ellipse in glyph space.
   *
   * @param cx centre x in glyph space
   * @param cy centre y in glyph space
   * @param w  full width
   * @param h  full height
   * @return an {@code Area} for use in mixed boolean operations
   */
  public Area ellipseArea(float cx, float cy, float w, float h) {
    return new Area(new Ellipse2D.Double(cx - w / 2.0, cy - h / 2.0, w, h));
  }

  /**
   * Returns an AWT {@code Area} representing an axis-aligned rectangle in glyph space.
   *
   * @param x      left edge x in glyph space
   * @param y      top edge y in glyph space
   * @param w      width
   * @param h      height
   * @return an {@code Area} for use in mixed boolean operations
   */
  public Area rectArea(float x, float y, float w, float h) {
    return new Area(new Rectangle2D.Double(x, y, w, h));
  }

  /**
   * Returns an AWT {@code Area} representing a rounded rectangle in glyph space.
   *
   * @param x    left edge x in glyph space
   * @param y    top edge y in glyph space
   * @param w    width
   * @param h    height
   * @param arcW corner arc width
   * @param arcH corner arc height
   * @return an {@code Area} for use in mixed boolean operations
   */
  public Area roundedRectArea(float x, float y, float w, float h, float arcW, float arcH) {
    return new Area(new RoundRectangle2D.Double(x, y, w, h, arcW, arcH));
  }

  /**
   * Converts an AWT {@code Area} to a Processing {@code PShape}.
   *
   * <p>Use this to render any {@code Area} returned by the primitive factories
   * or composed manually:
   * <pre>
   * Area circle = glyph.circleArea(cx, cy, r);
   * circle.subtract(glyph.circleArea(cx, cy, r * 0.6f));  // ring
   * PShape ring = glyph.areaToShape(circle);
   * </pre></p>
   *
   * @param area the AWT Area to convert
   * @return a Processing {@code PShape} ready to pass to {@code shape()}
   */
  public PShape areaToShape(Area area) {
    return shapeToProcessing(area);
  }

  // ── Mixed boolean overloads: Area ✕ glyph (v0.2.5) ───────────────

  /**
   * Merges a geometric primitive with a letterform.
   *
   * <pre>
   * Area circle = glyph.circleArea(gcx, gcy, 300);
   * PShape merged = glyph.union(circle, 'A', 600);
   * </pre>
   *
   * @param shapeA   the geometric primitive (built with {@link #circleArea} etc.)
   * @param charB    the letterform to merge in
   * @param fontSize font size for {@code charB}
   * @return merged {@code PShape}
   */
  public PShape union(Area shapeA, char charB, float fontSize) {
    Area a = new Area(shapeA);
    a.add(glyphArea(charB, fontSize));
    return shapeToProcessing(a);
  }

  /**
   * Merges a letterform with a geometric primitive.
   *
   * @param charA    the letterform
   * @param fontSize font size for {@code charA}
   * @param shapeB   the geometric primitive to merge in
   * @return merged {@code PShape}
   */
  public PShape union(char charA, float fontSize, Area shapeB) {
    Area a = glyphArea(charA, fontSize);
    a.add(new Area(shapeB));
    return shapeToProcessing(a);
  }

  /**
   * Intersects a geometric primitive with a letterform (region where both overlap).
   *
   * <pre>
   * Area rect = glyph.rectArea(bx, by, bw, bh * 0.5f);  // top half
   * PShape sliced = glyph.intersect(rect, 'A', 600);
   * </pre>
   *
   * @param shapeA   the geometric primitive
   * @param charB    the letterform
   * @param fontSize font size for {@code charB}
   * @return intersected {@code PShape}
   */
  public PShape intersect(Area shapeA, char charB, float fontSize) {
    Area a = new Area(shapeA);
    a.intersect(glyphArea(charB, fontSize));
    return shapeToProcessing(a);
  }

  /**
   * Intersects a letterform with a geometric primitive.
   *
   * @param charA    the letterform
   * @param fontSize font size for {@code charA}
   * @param shapeB   the geometric primitive
   * @return intersected {@code PShape}
   */
  public PShape intersect(char charA, float fontSize, Area shapeB) {
    Area a = glyphArea(charA, fontSize);
    a.intersect(new Area(shapeB));
    return shapeToProcessing(a);
  }

  /**
   * Subtracts a letterform from a geometric primitive (letter punched out of shape).
   *
   * <pre>
   * Area circle = glyph.circleArea(gcx, gcy, 300);
   * PShape knocked = glyph.subtract(circle, 'A', 600);  // circle with A punched through
   * </pre>
   *
   * @param shapeA   the base geometric primitive (will be cut)
   * @param charB    the cutter letterform
   * @param fontSize font size for {@code charB}
   * @return {@code PShape} with the letterform punched out of the primitive
   */
  public PShape subtract(Area shapeA, char charB, float fontSize) {
    Area a = new Area(shapeA);
    a.subtract(glyphArea(charB, fontSize));
    return shapeToProcessing(a);
  }

  /**
   * Subtracts a geometric primitive from a letterform (shape punched out of letter).
   *
   * @param charA    the base letterform (will be cut)
   * @param fontSize font size for {@code charA}
   * @param shapeB   the cutter geometric primitive
   * @return {@code PShape} with the primitive punched out of the letterform
   */
  public PShape subtract(char charA, float fontSize, Area shapeB) {
    Area a = glyphArea(charA, fontSize);
    a.subtract(new Area(shapeB));
    return shapeToProcessing(a);
  }

  // ── Mixed boolean overloads: Area ✕ Area (v0.2.5) ────────────────

  /**
   * Boolean union of two geometric primitives.
   *
   * @param shapeA the first primitive
   * @param shapeB the second primitive
   * @return merged {@code PShape}
   */
  public PShape union(Area shapeA, Area shapeB) {
    Area a = new Area(shapeA);
    a.add(new Area(shapeB));
    return shapeToProcessing(a);
  }

  /**
   * Boolean intersection of two geometric primitives.
   *
   * @param shapeA the first primitive
   * @param shapeB the second primitive
   * @return intersected {@code PShape}
   */
  public PShape intersect(Area shapeA, Area shapeB) {
    Area a = new Area(shapeA);
    a.intersect(new Area(shapeB));
    return shapeToProcessing(a);
  }

  /**
   * Boolean subtraction of two geometric primitives (shapeA minus shapeB).
   *
   * @param shapeA the base primitive (will be cut)
   * @param shapeB the cutter primitive
   * @return {@code PShape} of shapeA with shapeB removed
   */
  public PShape subtract(Area shapeA, Area shapeB) {
    Area a = new Area(shapeA);
    a.subtract(new Area(shapeB));
    return shapeToProcessing(a);
  }

  // ── Path utilities (v0.2.5) ────────────────────────────────────

  /**
   * Returns the normalised tangent direction at a given arc-length position.
   *
   * <p>The tangent is computed by looking at the outline at positions
   * {@code t} and {@code t + ε}, then normalising the difference vector.
   * Useful for orienting ornaments, arrows, or glyphs that follow a letterform.
   * At {@code t = 0}, the tangent points in the direction the perimeter travels
   * away from the start point.</p>
   *
   * <pre>
   * PVector tan = glyph.getTangent('O', 600, frameCount * 0.005 % 1.0);
   * float angle = atan2(tan.y, tan.x);  // use in rotate()
   * </pre>
   *
   * @param ch       the character
   * @param fontSize the font size
   * @param t        normalised arc-length position in [0.0, 1.0]
   * @return a unit {@code PVector} tangent to the outline at {@code t}; defaults
   *         to {@code (1, 0)} if the outline is degenerate
   */
  public PVector getTangent(char ch, float fontSize, float t) {
    t = Math.max(0, Math.min(1, t));
    PVector[] pts = distributeAlongOutline(ch, fontSize, 1024);
    if (pts.length < 2) return new PVector(1, 0);
    float eps = 1.0f / pts.length;
    float t2  = (t + eps) % 1.0f;
    int   idx1 = (int)(t  * pts.length) % pts.length;
    int   idx2 = (int)(t2 * pts.length) % pts.length;
    PVector tangent = PVector.sub(pts[idx2], pts[idx1]);
    if (tangent.mag() < 1e-6f) return new PVector(1, 0);
    tangent.normalize();
    return tangent;
  }

  /**
   * Returns a dashed stroke representation of a character's outline.
   *
   * <p>The contour is arc-length-resampled and then walked in alternating
   * dash/gap cycles. Each dash is returned as a {@code {x1, y1, x2, y2}}
   * endpoint pair — the same format as {@link #fillWithLines(char, float, float, float)} —
   * so they can be rendered directly with Processing's {@code line()} call:</p>
   *
   * <pre>
   * float[][] segs = glyph.getDashedOutline('A', 600, 12, 6);
   * for (float[] s : segs) line(ox+s[0], oy+s[1], ox+s[2], oy+s[3]);
   * </pre>
   *
   * <p>This is a print-design staple (engraving, laser stencil, stamp aesthetics)
   * not available in other Processing typography libraries.</p>
   *
   * @param ch         the character
   * @param fontSize   the font size
   * @param dashLength length of each visible dash in pixels (must be &gt; 0)
   * @param gapLength  length of each gap between dashes in pixels (must be &gt; 0)
   * @return array of {@code {x1, y1, x2, y2}} endpoint pairs; never {@code null}
   */
  public float[][] getDashedOutline(char ch, float fontSize, float dashLength, float gapLength) {
    if (dashLength <= 0 || gapLength <= 0) return new float[0][];
    List<PVector[]> contours = getContours(ch, fontSize);
    if (contours.isEmpty()) return new float[0][];
    return buildDashedOutline(contours, dashLength, gapLength);
  }

  /**
   * Produces dashed-outline segment pairs for a text string.
   *
   * <p>Equivalent to {@link #getDashedOutline(char, float, float, float)} but accepts
   * a multi-character string (e.g. a word). Each contour of every character is walked
   * independently, so dash seams never cross between glyphs.</p>
   *
   * <pre>
   * float[][] segs = glyph.getDashedOutline("TYPO", 400, 12, 6);
   * for (float[] s : segs) line(ox+s[0], oy+s[1], ox+s[2], oy+s[3]);
   * </pre>
   *
   * @param text       the text string
   * @param fontSize   the font size
   * @param dashLength length of each visible dash in pixels (must be &gt; 0)
   * @param gapLength  length of each gap between dashes in pixels (must be &gt; 0)
   * @return array of {@code {x1, y1, x2, y2}} endpoint pairs; never {@code null}
   */
  public float[][] getDashedOutline(String text, float fontSize, float dashLength, float gapLength) {
    if (dashLength <= 0 || gapLength <= 0) return new float[0][];
    List<PVector[]> contours = getContours(text, fontSize);
    if (contours.isEmpty()) return new float[0][];
    return buildDashedOutline(contours, dashLength, gapLength);
  }

  /**
   * Core dash-walking algorithm shared by both {@code getDashedOutline} overloads.
   *
   * <p>Processes each contour independently so the dash walker never bridges
   * two separate letterform boundaries. 2048 samples are distributed across
   * contours proportionally by arc length.</p>
   */
  private float[][] buildDashedOutline(List<PVector[]> contours, float dashLength, float gapLength) {
    List<float[]> result = new ArrayList<>();

    // Allocate 2048 samples distributed proportionally across all contours.
    float totalLen = 0;
    float[] lengths = new float[contours.size()];
    for (int i = 0; i < contours.size(); i++) {
      PVector[] c = contours.get(i);
      float len = 0;
      for (int j = 1; j < c.length; j++) len += PVector.dist(c[j - 1], c[j]);
      if (c.length > 1) len += PVector.dist(c[c.length - 1], c[0]);
      lengths[i] = len;
      totalLen += len;
    }
    if (totalLen == 0) return new float[0][];

    for (int ci = 0; ci < contours.size(); ci++) {
      int n = Math.max(4, Math.round(2048 * lengths[ci] / totalLen));

      // Close the contour for resampling (same as resamplePerContour does)
      PVector[] c = contours.get(ci);
      boolean alreadyClosed = c.length > 1 &&
          PVector.dist(c[c.length - 1], c[0]) < 0.01f;
      PVector[] closed;
      if (alreadyClosed) {
        closed = c;
      } else {
        closed = new PVector[c.length + 1];
        System.arraycopy(c, 0, closed, 0, c.length);
        closed[c.length] = c[0].copy();
      }
      PVector[] pts = resample(closed, n + 1);
      // pts[n] is a duplicate of pts[0]; keep it so the closing segment is walked.

      // Fresh dash-walker state for each contour
      float posInCycle = 0;
      boolean inDash   = true;
      float segStartX  = pts[0].x, segStartY = pts[0].y;

      for (int i = 1; i <= n; i++) {
        float dx   = pts[i].x - pts[i - 1].x;
        float dy   = pts[i].y - pts[i - 1].y;
        float seg  = (float) Math.sqrt(dx * dx + dy * dy);
        float walked = 0;

        while (walked < seg) {
          float remaining = inDash ? (dashLength - posInCycle) : (gapLength - posInCycle);
          float step      = Math.min(remaining, seg - walked);
          float frac      = (walked + step) / seg;
          float px = pts[i - 1].x + frac * dx;
          float py = pts[i - 1].y + frac * dy;

          posInCycle += step;
          walked     += step;

          if (posInCycle >= (inDash ? dashLength : gapLength) - 1e-4f) {
            if (inDash) result.add(new float[]{ segStartX, segStartY, px, py });
            inDash     = !inDash;
            posInCycle = 0;
            segStartX  = px;
            segStartY  = py;
          }
        }
      }
    }
    return result.toArray(new float[0][]);
  }

  /**
   * Returns a higher-density tessellation of a character outline.
   *
   * <p>Temporarily applies a finer flatness than the current instance setting
   * to derive more tessellation vertices, then resamples the result to exactly
   * {@code targetCount} evenly-spaced points. The instance's flatness value is
   * not mutated. Useful when downstream physics or deformation effects need more
   * points than the font naturally provides.</p>
   *
   * <pre>
   * PVector[] dense = glyph.subdivide('A', 600, 1200);
   * for (PVector p : dense) ellipse(ox+p.x, oy+p.y, 2, 2);
   * </pre>
   *
   * @param ch          the character
   * @param fontSize    the font size
   * @param targetCount the desired number of output points (&ge; 2)
   * @return arc-length resampled contour with {@code targetCount} points
   */
  public PVector[] subdivide(char ch, float fontSize, int targetCount) {
    if (targetCount < 2) targetCount = 2;
    // Temporarily drop flatness for a denser tessellation; save and restore.
    float savedFlatness = this.flatness;
    this.flatness = Math.min(savedFlatness, 0.2f);
    Font sized = awtFont.deriveFont(fontSize);
    GlyphVector gv = sized.createGlyphVector(frc, new char[]{ch});
    PVector[] raw = extractPoints(gv.getOutline());
    this.flatness = savedFlatness;
    if (raw.length < 2) return raw;
    return resample(raw, targetCount);
  }

  /**
   * Lays out a string of glyphs along an arbitrary path.
   *
   * <p>Characters are placed sequentially along the supplied {@code path}
   * array (any {@code PVector[]} — e.g. from {@link #getOuterContour(char, float)}
   * or {@link #distributeAlongOutline(char, float, int)}). Each glyph is rotated
   * to align with the local tangent of the path at its placement point.</p>
   *
   * <p>The advance width from the font's own glyph metrics () is used for spacing,
   * so kerning is respected if the font provides it. Characters that exceed the
   * remaining path length are silently omitted.</p>
   *
   * <pre>
   * PVector[] oval = glyph.getOuterContour('O', 700);
   * PShape label = glyph.textOnPath("DESIGN", oval, 48);
   * shape(label, 0, 0);
   * </pre>
   *
   * @param text     the string to lay out along the path
   * @param path     the array of path points (must have at least 2 entries)
   * @param fontSize the size of each glyph in points
   * @return a {@code PShape} GROUP containing one child per character, positioned
   *         and rotated along the path; ready to be drawn with {@code shape()}
   */
  public PShape textOnPath(String text, PVector[] path, float fontSize) {
    PShape group = parent.createShape(PConstants.GROUP);
    if (text == null || text.isEmpty() || path == null || path.length < 2) return group;

    // Build a cumulative arc-length table for the path
    int n = path.length;
    float[] arcLen = new float[n];
    arcLen[0] = 0;
    for (int i = 1; i < n; i++) {
      arcLen[i] = arcLen[i - 1] + PVector.dist(path[i - 1], path[i]);
    }
    float totalLen = arcLen[n - 1];

    Font sized        = awtFont.deriveFont(fontSize);
    char[] chars      = text.toCharArray();
    GlyphVector fullGv = sized.createGlyphVector(frc, chars);

    float cursor = 0; // position along the path in pixels

    for (int c = 0; c < chars.length; c++) {
      float advance = (float) fullGv.getGlyphMetrics(c).getAdvance();
      if (advance <= 0) advance = fontSize * 0.5f;

      float placementAt = cursor + advance * 0.5f; // centre of glyph on path
      if (placementAt > totalLen) break;

      // Interpolate position along path
      PVector pos    = arcLengthPoint(path, arcLen, placementAt);
      PVector posAlt = arcLengthPoint(path, arcLen, Math.min(placementAt + 1f, totalLen));
      PVector tangent = PVector.sub(posAlt, pos);
      float angle = (tangent.mag() < 1e-6f) ? 0 : (float) Math.atan2(tangent.y, tangent.x);

      // Extract, centre, rotate, and translate the glyph
      PShape glyph = extractChar(chars[c], fontSize);
      glyph.disableStyle();
      float[] b = getBounds(chars[c], fontSize);
      float localOx = -b[0] - b[2] * 0.5f;
      float localOy = -b[1] - b[3] * 0.5f;

      PShape placed = parent.createShape(PConstants.GROUP);
      // Wrap in a group so we can apply a matrix without affecting the child's style
      PShape proxy = parent.createShape();
      proxy.beginShape();
      // Re-extract points from the glyph and apply rotation+translation manually
      List<PVector[]> contours = getContours(chars[c], fontSize);
      boolean first = true;
      for (PVector[] ctour : contours) {
        if (!first) proxy.beginContour();
        for (PVector pt : ctour) {
          float rx = localOx + pt.x;
          float ry = localOy + pt.y;
          float cosA = (float) Math.cos(angle);
          float sinA = (float) Math.sin(angle);
          proxy.vertex(pos.x + cosA * rx - sinA * ry,
                       pos.y + sinA * rx + cosA * ry);
        }
        if (!first) proxy.endContour();
        first = false;
      }
      proxy.endShape(PConstants.CLOSE);
      group.addChild(proxy);

      cursor += advance;
    }
    return group;
  }

  /**
   * Lays a string of glyphs along an arbitrary path with extra letter-spacing.
   *
   * <p>Identical to {@link #textOnPath(String, PVector[], float)} except that
   * {@code spacing} pixels are added to every advance width before placing the
   * next character. Positive values spread the text out; negative values
   * tighten it (characters may overlap if spacing is sufficiently negative).</p>
   *
   * @param text     the string to lay out along the path
   * @param path     the array of path points (must have at least 2 entries)
   * @param fontSize the size of each glyph in points
   * @param spacing  extra pixels between each character (may be negative)
   * @return a {@code PShape} GROUP containing one child per character
   */
  public PShape textOnPath(String text, PVector[] path, float fontSize, float spacing) {
    PShape group = parent.createShape(PConstants.GROUP);
    if (text == null || text.isEmpty() || path == null || path.length < 2) return group;

    int n = path.length;
    float[] arcLen = new float[n];
    arcLen[0] = 0;
    for (int i = 1; i < n; i++) {
      arcLen[i] = arcLen[i - 1] + PVector.dist(path[i - 1], path[i]);
    }
    float totalLen = arcLen[n - 1];

    Font sized       = awtFont.deriveFont(fontSize);
    char[] chars     = text.toCharArray();
    GlyphVector fullGv = sized.createGlyphVector(frc, chars);

    float cursor = 0;

    for (int c = 0; c < chars.length; c++) {
      float advance = (float) fullGv.getGlyphMetrics(c).getAdvance() + spacing;
      if (advance <= 0) advance = fontSize * 0.5f + spacing;

      float placementAt = cursor + advance * 0.5f;
      if (placementAt > totalLen) break;

      PVector pos    = arcLengthPoint(path, arcLen, placementAt);
      PVector posAlt = arcLengthPoint(path, arcLen, Math.min(placementAt + 1f, totalLen));
      PVector tangent = PVector.sub(posAlt, pos);
      float angle = (tangent.mag() < 1e-6f) ? 0 : (float) Math.atan2(tangent.y, tangent.x);

      float[] b = getBounds(chars[c], fontSize);
      float localOx = -b[0] - b[2] * 0.5f;
      float localOy = -b[1] - b[3] * 0.5f;

      PShape proxy = parent.createShape();
      proxy.beginShape();
      List<PVector[]> contours = getContours(chars[c], fontSize);
      boolean first = true;
      for (PVector[] ctour : contours) {
        if (!first) proxy.beginContour();
        for (PVector pt : ctour) {
          float rx = localOx + pt.x;
          float ry = localOy + pt.y;
          float cosA = (float) Math.cos(angle);
          float sinA = (float) Math.sin(angle);
          proxy.vertex(pos.x + cosA * rx - sinA * ry,
                       pos.y + sinA * rx + cosA * ry);
        }
        if (!first) proxy.endContour();
        first = false;
      }
      proxy.endShape(PConstants.CLOSE);
      group.addChild(proxy);

      cursor += advance;
    }
    return group;
  }

  // ── Type DNA (v0.2.5) ──────────────────────────────────────────

  /**
   * Returns the dominant stroke-stress angle of the letterform in degrees [0, 180).
   *
   * <p>The stress axis is the direction along which strokes thin and thicken:
   * roughly 0° in old-style italics, near 90° in vertical-stress moderns, and
   * close to zero in monolines. Computed via PCA on the inner contour vertices;
   * returns 90° (vertical stress) for glyphs with no inner contours.</p>
   *
   * <p><b>Note:</b> This is a geometric approximation. Results are reliable for
   * well-hinted text faces but may be less accurate for highly decorative or
   * irregular display typefaces.</p>
   *
   * <pre>
   * float stress = glyph.getStressAxis('O', 600);
   * at.setWaveAngle(stress); // wave follows the typeface's own optical axis
   * </pre>
   *
   * @param ch       the character
   * @param fontSize the font size
   * @return stress angle in degrees [0, 180) — 90° means vertical stress
   */
  public float getStressAxis(char ch, float fontSize) {
    List<PVector[]> inner = getInnerContours(ch, fontSize);
    if (inner.isEmpty()) {
      // Use full outline for monolinear / no-counter glyphs
      Font sized = awtFont.deriveFont(fontSize);
      GlyphVector gv = sized.createGlyphVector(frc, new char[]{ch});
      PVector[] pts = extractPoints(gv.getOutline());
      if (pts.length < 3) return 90f;
      inner = new ArrayList<>();
      inner.add(pts);
    }
    // Pool all inner-contour points for PCA
    List<PVector> all = new ArrayList<>();
    for (PVector[] c : inner) for (PVector p : c) all.add(p);
    return pcaAngle(all);
  }

  /**
   * Returns the optical centroid of the letterform — the ink-density-weighted
   * centre of mass.
   *
   * <p>Unlike the geometric bounding-box centre, the optical centroid is biased
   * toward the interior regions of the letterform that carry the most ink. Use
   * it as a pivot for rotation, orbit origins, or magnetic field anchors so that
   * glyphs feel visually balanced rather than mechanically centred.</p>
   *
   * <pre>
   * PVector centre = glyph.getOpticalCentroid('A', 600);
   * // orbit anchor at the ink centre rather than the bounding-box centre
   * </pre>
   *
   * @param ch       the character
   * @param fontSize the font size
   * @return a {@code PVector} at the ink-density-weighted perceptual centre
   */
  public PVector getOpticalCentroid(char ch, float fontSize) {
    Font sized = awtFont.deriveFont(fontSize);
    GlyphVector gv = sized.createGlyphVector(frc, new char[]{ch});
    Shape outline = gv.getOutline();

    // Sample interior points and weight each by its approximate distance
    // to the nearest contour point (deeper inside = higher ink density).
    PVector[] interior = fillWithPoints(ch, fontSize, 800);
    PVector[] contour  = distributeAlongOutline(ch, fontSize, 512);
    if (interior.length == 0) {
      // Fallback: plain bounding-box centre
      float[] b = getBounds(ch, fontSize);
      return new PVector(b[0] + b[2] * 0.5f, b[1] + b[3] * 0.5f);
    }

    float wx = 0, wy = 0, wTotal = 0;
    for (PVector p : interior) {
      float minDist = Float.MAX_VALUE;
      for (PVector c : contour) {
        float d = PVector.dist(p, c);
        if (d < minDist) minDist = d;
      }
      float weight = minDist + 1f; // +1 avoids zero weight on boundary points
      wx += p.x * weight;
      wy += p.y * weight;
      wTotal += weight;
    }
    return new PVector(wx / wTotal, wy / wTotal);
  }

  /**
   * Returns the counter ratio of a character: the proportion of the glyph's
   * bounding box area that is enclosed white space (counter-forms).
   *
   * <p>Open glyphs like 'O' return a high ratio (≈ 0.3–0.5); dense glyphs like
   * 'I' return 0. Map to wave amplitude, breathing rate, or motion radius: open
   * glyphs breathe more, dense glyphs move less, automatically.</p>
   *
   * <pre>
   * float ratio = glyph.getCounterRatio('O', 600);
   * at.getConfiguration().setWaveAmplitudeMin(-200 * ratio);
   * </pre>
   *
   * @param ch       the character
   * @param fontSize the font size
   * @return counter area / bounding-box area in [0, 1]; 0 = no counter-forms
   */
  public float getCounterRatio(char ch, float fontSize) {
    List<PVector[]> inners = getInnerContours(ch, fontSize);
    if (inners.isEmpty()) return 0f;
    float[] b = getBounds(ch, fontSize);
    float bboxArea = b[2] * b[3];
    if (bboxArea <= 0) return 0f;
    float counterArea = 0;
    for (PVector[] c : inners) counterArea += Math.abs(signedArea(c));
    return Math.min(1f, counterArea / bboxArea);
  }

  /**
   * Estimates the dominant stroke weight of a character in pixels.
   *
   * <p>Computed as twice the mean distance from the approximate medial axis to
   * the nearest outline point — a proxy for the average stroke width. Map to
   * physics mass, brightness, or saturation: light glyphs behave differently
   * from bold ones without any manual tuning.</p>
   *
   * <pre>
   * float sw = glyph.getStrokeWeight('H', 600);
   * // heavy glyphs (sw large) move more slowly
   * </pre>
   *
   * @param ch       the character
   * @param fontSize the font size
   * @return estimated stroke width in pixels; returns 0 if the axis is degenerate
   */
  public float getStrokeWeight(char ch, float fontSize) {
    PVector[] axis    = getMedialAxis(ch, fontSize, 80);
    PVector[] contour = distributeAlongOutline(ch, fontSize, 512);
    if (axis.length == 0 || contour.length == 0) return 0f;

    float totalDist = 0;
    for (PVector axPt : axis) {
      float minD = Float.MAX_VALUE;
      for (PVector cPt : contour) {
        float d = PVector.dist(axPt, cPt);
        if (d < minD) minD = d;
      }
      totalDist += minD;
    }
    // × 2 because the axis-to-outline distance is half the stroke width
    return 2f * totalDist / axis.length;
  }

  /**
   * Builds a {@link TypeDNAProfile} by averaging all four Type DNA measurements
   * across a character set.
   *
   * <p>A profile captures the typographic fingerprint of the loaded font at the
   * given size. Call {@link algorithmic.typography.AlgorithmicTypography#applyTypeDNA(TypeDNAProfile)}
   * to apply it as an animation preset.</p>
   *
   * <pre>
   * TypeDNAProfile profile = glyph.buildTypeDNAProfile(
   *     new char[]{'A','B','C','H','O','R'}, 600);
   * at.applyTypeDNA(profile);
   * profile.toJSON().save(sketchPath("data/dna.json"));
   * </pre>
   *
   * @param chars    the characters to measure (should be a representative sample)
   * @param fontSize the font size applied to all measurements
   * @return a {@code TypeDNAProfile} averaging the measurements across {@code chars}
   */
  public TypeDNAProfile buildTypeDNAProfile(char[] chars, float fontSize) {
    if (chars == null || chars.length == 0) {
      return new TypeDNAProfile(90f, new PVector(), 0f, 0f);
    }
    float stressSum = 0, ratioSum = 0, weightSum = 0;
    float cx = 0, cy = 0;
    int n = chars.length;
    for (char ch : chars) {
      stressSum  += getStressAxis(ch, fontSize);
      ratioSum   += getCounterRatio(ch, fontSize);
      weightSum  += getStrokeWeight(ch, fontSize);
      PVector oc  = getOpticalCentroid(ch, fontSize);
      cx += oc.x;
      cy += oc.y;
    }
    return new TypeDNAProfile(
        stressSum  / n,
        new PVector(cx / n, cy / n),
        ratioSum   / n,
        weightSum  / n
    );
  }

  // ── Internal v0.2.3 helpers ────────────────────────────────────
  /**
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
    // Collect all closed sub-path contours from the flattened path.
    List<List<float[]>> contours = new ArrayList<>();
    PathIterator pi = new FlatteningPathIterator(awtShape.getPathIterator(null), flatness);
    float[] coords = new float[6];
    List<float[]> current = null;

    while (!pi.isDone()) {
      int seg = pi.currentSegment(coords);
      if (seg == PathIterator.SEG_MOVETO) {
        if (current != null && !current.isEmpty()) contours.add(current);
        current = new ArrayList<>();
        current.add(new float[]{coords[0], coords[1]});
      } else if (seg == PathIterator.SEG_LINETO && current != null) {
        current.add(new float[]{coords[0], coords[1]});
      }
      pi.next();
    }
    if (current != null && !current.isEmpty()) contours.add(current);

    if (contours.isEmpty()) return parent.createShape();

    // Classify contours by winding direction (signed area in y-down screen space):
    // TrueType/OpenType fonts on macOS wind outer boundaries CCW (negative
    // signed area in y-down coords) and counter-forms CW (positive).
    List<List<float[]>> outers = new ArrayList<>();
    List<List<float[]>> holes  = new ArrayList<>();
    for (List<float[]> c : contours) {
      if (signedPolygonArea(c) <= 0) outers.add(c);
      else                           holes.add(c);
    }

    // Fall back: if classification yields no outers, render everything filled.
    if (outers.isEmpty()) {
      PShape group = parent.createShape(PConstants.GROUP);
      for (List<float[]> c : contours) {
        PShape s = parent.createShape();
        s.beginShape(); s.noStroke(); s.fill(255);
        for (float[] pt : c) s.vertex(pt[0], pt[1]);
        s.endShape(PConstants.CLOSE);
        group.addChild(s);
      }
      return group;
    }

    // Build one PShape per outer contour; embed any hole that lies inside it
    // via beginContour/endContour so Processing's non-zero fill rule treats it
    // as a transparent cutout.  AWT Area holes are already wound CCW — the
    // opposite of the CW outer — so they are added as-is (no reversal needed).
    PShape group = parent.createShape(PConstants.GROUP);
    for (List<float[]> outer : outers) {
      PShape s = parent.createShape();
      s.beginShape(); s.noStroke(); s.fill(255);
      for (float[] pt : outer) s.vertex(pt[0], pt[1]);

      for (List<float[]> hole : holes) {
        float[] hp = hole.get(0);
        if (isPointInContour(hp[0], hp[1], outer)) {
          s.beginContour();
          for (float[] pt : hole) s.vertex(pt[0], pt[1]);
          s.endContour();
        }
      }
      s.endShape(PConstants.CLOSE);
      group.addChild(s);
    }
    return group;
  }

  /** Signed polygon area via shoelace formula.
   *  Positive = CW in screen y-down coordinates (outer boundary).
   *  Negative = CCW = counter-form / hole. */
  private float signedPolygonArea(List<float[]> pts) {
    double sum = 0;
    int n = pts.size();
    for (int i = 0; i < n; i++) {
      float[] a = pts.get(i), b = pts.get((i + 1) % n);
      sum += (double) a[0] * b[1] - (double) b[0] * a[1];
    }
    return (float) (sum * 0.5);
  }

  /** Ray-casting point-in-polygon test (works for any simple polygon). */
  private boolean isPointInContour(float px, float py, List<float[]> contour) {
    boolean inside = false;
    int n = contour.size();
    for (int i = 0, j = n - 1; i < n; j = i++) {
      float xi = contour.get(i)[0], yi = contour.get(i)[1];
      float xj = contour.get(j)[0], yj = contour.get(j)[1];
      if ((yi > py) != (yj > py) &&
          px < (xj - xi) * (py - yi) / (yj - yi) + xi) {
        inside = !inside;
      }
    }
    return inside;
  }
  
  /**
   * Extracts all points from a flattened AWT Shape.
   */
  private PVector[] extractPoints(Shape awtShape) {
    List<PVector> points = new ArrayList<>();
    float[] subPathOrigin = new float[2];

    PathIterator pi = new FlatteningPathIterator(
        awtShape.getPathIterator(null), flatness);
    float[] coords = new float[6];

    while (!pi.isDone()) {
      int type = pi.currentSegment(coords);
      if (type == PathIterator.SEG_MOVETO) {
        subPathOrigin[0] = coords[0];
        subPathOrigin[1] = coords[1];
        points.add(new PVector(coords[0], coords[1]));
      } else if (type == PathIterator.SEG_LINETO) {
        points.add(new PVector(coords[0], coords[1]));
      } else if (type == PathIterator.SEG_CLOSE && !points.isEmpty()) {
        // Close the sub-path: append origin if not already there
        PVector prev = points.get(points.size() - 1);
        if (PVector.dist(prev, new PVector(subPathOrigin[0], subPathOrigin[1])) > 0.01f) {
          points.add(new PVector(subPathOrigin[0], subPathOrigin[1]));
        }
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
            // Close the loop: append first point if not already there
            PVector first = current.get(0);
            PVector last  = current.get(current.size() - 1);
            if (PVector.dist(first, last) > 0.01f) {
              current.add(new PVector(first.x, first.y));
            }
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
    if (contours.isEmpty()) return group;

    // Classify contours using the same signed-area convention as shapeToProcessing:
    // negative signed area (CCW in y-down) = outer; positive (CW) = hole.
    List<PVector[]> outers = new ArrayList<>();
    List<PVector[]> holes  = new ArrayList<>();
    for (PVector[] c : contours) {
      if (signedPolygonAreaVec(c) <= 0) outers.add(c);
      else                              holes.add(c);
    }
    if (outers.isEmpty()) { outers = contours; holes = new ArrayList<>(); }

    for (PVector[] outer : outers) {
      PShape s = parent.createShape();
      s.beginShape();
      s.noStroke();
      s.fill(255);

      for (int i = 0; i < outer.length; i++) {
        PVector p = outer[i];
        float angle = (p.x + p.y) * frequency + time;
        s.vertex(p.x + PApplet.sin(angle) * amplitude,
                 p.y + PApplet.cos(angle * 0.7f) * amplitude);
      }

      for (PVector[] hole : holes) {
        if (isPointInContourVec(hole[0].x, hole[0].y, outer)) {
          s.beginContour();
          for (int i = 0; i < hole.length; i++) {
            PVector p = hole[i];
            float angle = (p.x + p.y) * frequency + time;
            s.vertex(p.x + PApplet.sin(angle) * amplitude,
                     p.y + PApplet.cos(angle * 0.7f) * amplitude);
          }
          s.endContour();
        }
      }

      s.endShape(PConstants.CLOSE);
      group.addChild(s);
    }
    return group;
  }

  /** Signed polygon area via shoelace over PVector list. */
  private float signedPolygonAreaVec(PVector[] pts) {
    double sum = 0;
    int n = pts.length;
    for (int i = 0; i < n; i++) {
      PVector a = pts[i], b = pts[(i + 1) % n];
      sum += (double) a.x * b.y - (double) b.x * a.y;
    }
    return (float) (sum * 0.5);
  }

  /** Ray-casting point-in-polygon over PVector array. */
  private boolean isPointInContourVec(float px, float py, PVector[] contour) {
    boolean inside = false;
    int n = contour.length;
    for (int i = 0, j = n - 1; i < n; j = i++) {
      float xi = contour[i].x, yi = contour[i].y;
      float xj = contour[j].x, yj = contour[j].y;
      if ((yi > py) != (yj > py) &&
          px < (xj - xi) * (py - yi) / (yj - yi) + xi) {
        inside = !inside;
      }
    }
    return inside;
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
      // Guard: if extractContours already closed the contour (first == last),
      // do not append another copy — that would create a triple-duplicate.
      PVector[] c = contours.get(i);
      boolean alreadyClosed = c.length > 1 &&
          PVector.dist(c[c.length - 1], c[0]) < 0.01f;
      PVector[] closed;
      if (alreadyClosed) {
        closed = c;
      } else {
        closed = new PVector[c.length + 1];
        System.arraycopy(c, 0, closed, 0, c.length);
        closed[c.length] = c[0].copy();
      }
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
  
  /** Reverses the point order of a path array. */
  private PVector[] reversePath(PVector[] path) {
    PVector[] rev = new PVector[path.length];
    for (int i = 0; i < path.length; i++) rev[i] = path[path.length - 1 - i];
    return rev;
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

  // ── Internal v0.2.5 helpers ────────────────────────────────────

  /**
   * Returns an AWT Area for a single character at the given font size.
   * Shared by all boolean-operation methods.
   */
  private Area glyphArea(char ch, float fontSize) {
    Font sized = awtFont.deriveFont(fontSize);
    GlyphVector gv = sized.createGlyphVector(frc, new char[]{ch});
    return new Area(gv.getOutline());
  }

  /**
   * Linearly interpolates a point along a polyline at a given arc-length position.
   * {@code arcLen} must be the cumulative arc-length table computed externally.
   */
  private PVector arcLengthPoint(PVector[] path, float[] arcLen, float target) {
    if (target <= arcLen[0]) return path[0].copy();
    int n = path.length;
    if (target >= arcLen[n - 1]) return path[n - 1].copy();
    // Binary search for the segment containing target
    int lo = 0, hi = n - 1;
    while (hi - lo > 1) {
      int mid = (lo + hi) >>> 1;
      if (arcLen[mid] <= target) lo = mid; else hi = mid;
    }
    float segLen = arcLen[hi] - arcLen[lo];
    float t = (segLen < 1e-6f) ? 0 : (target - arcLen[lo]) / segLen;
    return PVector.lerp(path[lo], path[hi], t);
  }

  /**
   * Computes the dominant axis angle (in degrees, [0, 180)) of a point cloud
   * using 2×2 PCA (principal component analysis).
   * Used by {@link #getStressAxis(char, float)}.
   */
  private float pcaAngle(List<PVector> pts) {
    if (pts.size() < 2) return 90f;
    float mx = 0, my = 0;
    for (PVector p : pts) { mx += p.x; my += p.y; }
    mx /= pts.size(); my /= pts.size();

    double xx = 0, xy = 0, yy = 0;
    for (PVector p : pts) {
      double dx = p.x - mx, dy = p.y - my;
      xx += dx * dx;
      xy += dx * dy;
      yy += dy * dy;
    }
    // Angle of the eigenvector corresponding to the larger eigenvalue
    double angle = 0.5 * Math.atan2(2.0 * xy, xx - yy);
    // atan2 returns [-π/2, π/2]; map to [0, 180)
    float deg = (float) Math.toDegrees(angle);
    if (deg < 0) deg += 180f;
    return deg;
  }
}

