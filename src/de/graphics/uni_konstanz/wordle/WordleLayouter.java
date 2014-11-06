package de.graphics.uni_konstanz.wordle;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class WordleLayouter {

  enum RotationMode {
    NO_ROTATION, ROT_90_DEG_LEFT, ROT_90_DEG_RIGHT, ROT_90_DEG_LEFT_AND_RIGHT,
    ROT_90_DEG_RIGHT_AND_LEFT
  }

  public static List<Shape> generateLayoutLinear(final List<Shape> input,
      final boolean doSort, final RotationMode rotationMode) {
    if(doSort) {
      Collections.sort(input, new Comparator<Shape>() {

        @Override
        public int compare(final Shape o1, final Shape o2) {
          return Double.compare(o1.getBounds2D().getCenterX(), o2
              .getBounds2D().getCenterX());
        }
      });
    }
    final LinkedList<Area> layouted = new LinkedList<Area>();
    for(final Shape cur : input) {
      double t = 3.0;
      // spiral depending on the size of the object
      final double minSide = Math.min(cur.getBounds2D().getWidth(), cur
          .getBounds2D().getHeight());
      final double spiralFactor = minSide / 17.0;
      final double spiralStep = minSide / 10.0;
      final Area bleeded = createBleededArea(new Area(cur));
      while(true) {
        final double tx = Math.sin(t) * t * spiralFactor;
        final double ty = Math.cos(t) * t * spiralFactor;
        final AffineTransform at = new AffineTransform();
        at.translate(tx, ty);
        // transformed object
        final Area transformedArea = getTransformedArea(cur, at);
        final Area transformedBleeded = getTransformedArea(bleeded, at);
        if(!hasOverlap(layouted, transformedBleeded, new AffineTransform())) {
          // found placement
          layouted.add(transformedArea);
          break;
        }
        // no result by translation -> try rotation depending on
        // rotation mode

        final Rectangle2D rect = transformedArea.getBounds2D();
        final double centerX = rect.getCenterX();
        final double centerY = rect.getCenterY();

        boolean foundSolution = false;
        switch(rotationMode) {

          case ROT_90_DEG_LEFT:
            final AffineTransform rotLeft = new AffineTransform();
            rotLeft.rotate(-Math.PI / 2.0, centerX, centerY);
            if(!hasOverlap(layouted, transformedBleeded, rotLeft)) {
              layouted.add(getTransformedArea(transformedArea, rotLeft));
              foundSolution = true;
            }
            break;

          case ROT_90_DEG_RIGHT:
            final AffineTransform rotRight = new AffineTransform();
            rotRight.rotate(-Math.PI / 2.0, centerX, centerY);
            if(!hasOverlap(layouted, transformedBleeded, rotRight)) {
              layouted.add(getTransformedArea(transformedArea, rotRight));
              foundSolution = true;
            }
            break;

          case ROT_90_DEG_LEFT_AND_RIGHT:
            // left
            AffineTransform rot = new AffineTransform();
            rot.rotate(-Math.PI / 2.0, centerX, centerY);
            if(!hasOverlap(layouted, transformedBleeded, rot)) {
              layouted.add(getTransformedArea(transformedArea, rot));
              foundSolution = true;
            }
            // right
            rot = new AffineTransform();
            rot.rotate(Math.PI / 2.0, centerX, centerY);
            if(!hasOverlap(layouted, transformedBleeded, rot)) {
              layouted.add(getTransformedArea(transformedArea, rot));
              foundSolution = true;
            }
            break;

          case ROT_90_DEG_RIGHT_AND_LEFT:
            // right
            rot = new AffineTransform();
            rot.rotate(Math.PI / 2.0, centerX, centerY);
            if(!hasOverlap(layouted, transformedBleeded, rot)) {
              layouted.add(getTransformedArea(transformedArea, rot));
              foundSolution = true;
            }
            // left
            rot = new AffineTransform();
            rot.rotate(-Math.PI / 2.0, centerX, centerY);
            if(!hasOverlap(layouted, transformedBleeded, rot)) {
              layouted.add(getTransformedArea(transformedArea, rot));
              foundSolution = true;
            }
            break;
          case NO_ROTATION:
            break;
        }

        if(foundSolution) {
          break;
        }
        t += spiralStep / t;
      }
    }
    return new ArrayList<Shape>(layouted);
  }

  public static List<Shape> generateLayoutCircular(final List<Shape> input,
      final boolean doSort, final RotationMode rotationMode) {
    // calculate center
    double sumX = 0;
    double sumY = 0;
    int count = 0;
    for(final Shape s : input) {
      final Rectangle2D r = s.getBounds2D();
      sumX += r.getCenterX();
      sumY += r.getCenterY();
      ++count;
    }
    final Point2D center = new Point2D.Double(sumX / count, sumY / count);

    if(doSort) {
      Collections.sort(input, new Comparator<Shape>() {

        @Override
        public int compare(final Shape o1, final Shape o2) {
          final Rectangle2D r1 = o1.getBounds2D();
          final Rectangle2D r2 = o2.getBounds2D();
          final Point2D c1 = new Point2D.Double(r1.getCenterX(), r1
              .getCenterY());
          final Point2D c2 = new Point2D.Double(r2.getCenterX(), r2
              .getCenterY());
          return Double.compare(calcEuclideanDistanceSq(c1, center),
              calcEuclideanDistanceSq(c2, center));
        }
      });
    }

    final LinkedList<Area> layouted = new LinkedList<Area>();
    for(final Shape cur : input) {
      double t = 3.0;
      // spiral depending on the size of the object
      final double minSide = Math.min(cur.getBounds2D().getWidth(), cur
          .getBounds2D().getHeight());
      final double spiralFactor = minSide / 17.0;
      final double spiralStep = minSide / 10.0;

      final Area bleeded = createBleededArea(new Area(cur));

      while(true) {
        final double tx = Math.sin(t) * t * spiralFactor;
        final double ty = Math.cos(t) * t * spiralFactor;
        final AffineTransform at = new AffineTransform();
        at.translate(tx, ty);
        // transformed object
        final Area transformedArea = getTransformedArea(bleeded, at);
        final Area transformedBleeded = getTransformedArea(bleeded, at);
        if(!hasOverlap(layouted, transformedBleeded, new AffineTransform())) {
          // found placement
          layouted.add(getTransformedArea(cur, at));
          break;
        }

        // no result by translation -> try rotation depending on
        // rotation mode

        final Rectangle2D rect = transformedArea.getBounds2D();
        final double centerX = rect.getCenterX();
        final double centerY = rect.getCenterY();

        boolean foundSolution = false;
        switch(rotationMode) {

          case ROT_90_DEG_LEFT:
            final AffineTransform rotLeft = new AffineTransform();
            rotLeft.rotate(-Math.PI / 2.0, centerX, centerY);
            if(!hasOverlap(layouted, transformedBleeded, rotLeft)) {
              layouted.add(getTransformedArea(transformedArea, rotLeft));
              foundSolution = true;
            }
            break;

          case ROT_90_DEG_RIGHT:
            final AffineTransform rotRight = new AffineTransform();
            rotRight.rotate(-Math.PI / 2.0, centerX, centerY);
            if(!hasOverlap(layouted, transformedBleeded, rotRight)) {
              layouted.add(getTransformedArea(transformedArea, rotRight));
              foundSolution = true;
            }
            break;

          case ROT_90_DEG_LEFT_AND_RIGHT:
            // left
            AffineTransform rot = new AffineTransform();
            rot.rotate(-Math.PI / 2.0, centerX, centerY);
            if(!hasOverlap(layouted, transformedBleeded, rot)) {
              layouted.add(getTransformedArea(transformedArea, rot));
              foundSolution = true;
            }
            // right
            rot = new AffineTransform();
            rot.rotate(Math.PI / 2.0, centerX, centerY);
            if(!hasOverlap(layouted, transformedBleeded, rot)) {
              layouted.add(getTransformedArea(transformedArea, rot));
              foundSolution = true;
            }
            break;

          case ROT_90_DEG_RIGHT_AND_LEFT:
            // right
            rot = new AffineTransform();
            rot.rotate(Math.PI / 2.0, centerX, centerY);
            if(!hasOverlap(layouted, transformedBleeded, rot)) {
              layouted.add(getTransformedArea(transformedArea, rot));
              foundSolution = true;
            }
            // left
            rot = new AffineTransform();
            rot.rotate(-Math.PI / 2.0, centerX, centerY);
            if(!hasOverlap(layouted, transformedBleeded, rot)) {
              layouted.add(getTransformedArea(transformedArea, rot));
              foundSolution = true;
            }
            break;
          case NO_ROTATION:
            break;
        }

        if(foundSolution) {
          break;
        }
        t += spiralStep / t;
      }
    }
    return new ArrayList<Shape>(layouted);
  }

  private static Area createBleededArea(final Area area) {
    final Area copy = new Area(area);
    final Stroke stroke = new BasicStroke(1.5f);
    final Shape bleed = stroke.createStrokedShape(area);
    final Area a = new Area(bleed);
    copy.add(a);
    return copy;
  }

  private static Area getTransformedArea(final Shape original,
      final AffineTransform transform) {
    final Area copy = new Area(original);
    final Area a = copy.createTransformedArea(transform);
    return a;
  }

  private static boolean hasOverlap(final Deque<Area> alreadyLayouted,
      final Area bleeded, final AffineTransform aff) {
    final Iterator<Area> already = alreadyLayouted.iterator();
    final Area current = getTransformedArea(bleeded, aff);
    boolean first = true;
    while(already.hasNext()) {
      final Area s = already.next();
      if(hasOverlap(s, current)) {
        // move overlapping shape to front
        if(!first) {
          already.remove();
          alreadyLayouted.addFirst(s);
        }
        return true;
      }
      first = false;
    }
    return false;
  }

  private static boolean hasOverlap(final Area a1, final Area s2) {
    final Area a2bleeded = new Area(s2); // already bleeded
    a2bleeded.intersect(a1);
    return !a2bleeded.isEmpty();
  }

  public static final double calcEuclideanDistanceSq(final Point2D p1,
      final Point2D p2) {
    return (p1.getX() - p2.getX()) * (p1.getX() - p2.getX())
        + (p1.getY() - p2.getY()) * (p1.getY() - p2.getY());
  }
}
