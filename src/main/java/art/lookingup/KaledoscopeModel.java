package art.lookingup;

import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class KaledoscopeModel extends LXModel {

  public static float butterflySpacingInches = 12f;
  public static float lineSpacingInches = 24f;

  private static final Logger logger = Logger.getLogger(KaledoscopeModel.class.getName());

  public static List<LUButterfly> allButterflies;
  public static List<Run> allRuns;
  public static List<Strand> allStrands;
  public static int numStrandsPerRun;

  /**
   * A Strand is some number of butterflies wired in series.  Each strand should be wired to a single
   * LED controller output.  Typically a strand would receive data via a Pixlite long range receiver or
   * something similar.
   */
  static public class Strand {
    public List<LUButterfly> butterflies;
    public List<LXPoint> allPoints;
    public int strandIndex;

    public Strand(int numButterflies, float xpos, int strandIndex, List<Bezier> beziers) {
      butterflies = new ArrayList<LUButterfly>();
      allPoints = new ArrayList<LXPoint>();
      this.strandIndex = strandIndex;
      Bezier bezier = beziers.get(strandIndex / 2);
      //float yOffset = strandIndex * numButterflies * butterflySpacingInches;
      for (int i = 0; i < numButterflies; i++) {
        // This provides the 't' parameter for our bezier curve.  We currently have 2 strands spanning
        // a single bezier curve.  There might not be a fixed mapping between bezier curves and strands.
        // It will more likely be anchor points at certain distances along the wire that generate the curves
        // so the bezier curves will need to be segmented by arc length or wire length or by number of
        // butterflies since we know they are 12 inches apart.
        float t;
        int bezierButterflyIndex;
        if (strandIndex < 2) {
          bezierButterflyIndex = i + strandIndex * numButterflies;
        } else {
          bezierButterflyIndex = i + (strandIndex - 2) * numButterflies;
        }
        t = (float) bezierButterflyIndex / (float) (numButterflies * numStrandsPerRun/2);
        Point bPos = calculateBezierPoint(t, bezier.start, bezier.c1, bezier.c2, bezier.end);
        LUButterfly butterfly = new LUButterfly(i, i + strandIndex * numButterflies, bPos.x, bPos.y, 0.0f);
        butterflies.add(butterfly);
        allButterflies.add(butterfly);
        allPoints.addAll(butterfly.allPoints);
      }
    }
  }

  static public class Bezier {
    Point start;
    Point c1;
    Point c2;
    Point end;
    public Bezier(Point s, Point c1, Point c2, Point e) {
      start = s;
      this.c1 = c1;
      this.c2 = c2;
      end = e;
    }
  }

  /**
   * A Run is a single full line of butterflies.  It is composed of multiple strands wired in series.  The
   * purpose of a strand is to limit the number of LEDs on a single output in order to increase the FPS.
   * A run also consists of a series of bezier curves to model the curvature of the wires.
   */
  static public class Run {
    public List<LXPoint> allPoints;
    public List<Strand> strands;
    public List<LUButterfly> butterflies;
    Bezier bezier1;
    Bezier bezier2;
    public List<Bezier> beziers;
    int runIndex;
    float cxOffset = 100f;
    float cyOffset = 30f;

    public Run(int runIndex, float pos, int numStrands, int butterfliesPerStrand) {
      this.runIndex = runIndex;
      if (runIndex == 0)
        cxOffset = - cxOffset;
      if (runIndex == 2) {
        cyOffset = cyOffset * 5;
        cxOffset = cxOffset * 2;
      }

      Point bezierStart = new Point(pos, 0f);
      Point bezierEnd = new Point(pos, numStrands * butterfliesPerStrand * butterflySpacingInches/2);
      Point bezierC1 = new Point(bezierStart.x + cxOffset, bezierStart.y + cyOffset);
      Point bezierC2 = new Point(bezierEnd.x + cxOffset, bezierEnd.y - cyOffset);
      bezier1 = new Bezier(bezierStart, bezierC1, bezierC2, bezierEnd);


      Point b2Start = new Point(bezierEnd.x, bezierEnd.y);
      Point b2End = new Point(bezierEnd.x, numStrands * butterfliesPerStrand * butterflySpacingInches);
      Point b2C1 = new Point(b2Start.x - cxOffset, b2Start.y + cyOffset);
      Point b2C2 = new Point(b2End.x - cxOffset, b2End.y - cyOffset);
      bezier2 = new Bezier(b2Start, b2C1, b2C2, b2End);
      strands = new ArrayList<Strand>();
      butterflies = new ArrayList<LUButterfly>();
      allPoints = new ArrayList<LXPoint>();
      beziers = new ArrayList<Bezier>();
      beziers.add(bezier1);
      beziers.add(bezier2);
      for (int i = 0; i < numStrands; i++) {
        Strand strand = new Strand(butterfliesPerStrand, pos, i, beziers);
        allPoints.addAll(strand.allPoints);
        butterflies.addAll(strand.butterflies);
        allStrands.add(strand);
      }
    }
  }

  static public KaledoscopeModel createModel(int numRuns, int strandsPerRun, int butterfliesPerStrand) {
    List<LXPoint> allPoints = new ArrayList<LXPoint>();

    allRuns = new ArrayList<Run>(numRuns);
    allStrands = new ArrayList<Strand>();
    allButterflies = new ArrayList<LUButterfly>();
    numStrandsPerRun = strandsPerRun;

    for (int i = 0; i < numRuns; i++) {
      Run run = new Run(i, i * lineSpacingInches, numStrandsPerRun, butterfliesPerStrand);
      allRuns.add(run);
      allPoints.addAll(run.allPoints);
    }

    return new KaledoscopeModel(allPoints);
  }

  public KaledoscopeModel(List<LXPoint> points) {
    super(points);

  }

  static public class Point {
    public float x;
    public float y;
    public Point(float x, float y) {
      this.x = x;
      this.y = y;
    }
  }

  /* t is time(value of 0.0f-1.0f; 0 is the start 1 is the end) */
  static public Point calculateBezierPoint(float t, Point s, Point c1, Point c2, Point e)
  {
    float u = 1 - t;
    float tt = t*t;
    float uu = u*u;
    float uuu = uu * u;
    float ttt = tt * t;

    Point p = new Point(s.x * uuu, s.y * uuu);
    p.x += 3 * uu * t * c1.x;
    p.y += 3 * uu * t * c1.y;
    p.x += 3 * u * tt * c2.x;
    p.y += 3 * u * tt * c2.y;
    p.x += ttt * e.x;
    p.y += ttt * e.y;

    return p;
  }
}
