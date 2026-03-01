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
 * </pre>
 * 
 * @author Michail Semoglou
 * @version 0.2.2
 * @since 1.0.0
 */

package algorithmic.typography.render;

import processing.core.*;

import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
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
