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
  public static List<LUFlower> allFlowers;
  public static List<Run> allRuns;
  public static List<Strand> allStrands;
  public static int numStrandsPerRun;

  /**
   * A Strand is some number of butterflies wired in series.  Multiple strands can be wired to a single
   * LED controller output.  Typically a strand would receive data via a Pixlite long range receiver or
   * something similar.
   *
   * A Strand can also be a number of flowers wired in series.
   */
  static public class Strand {
    // The global strandId.  These are allocated as we build the model.
    int strandId;
    // The index number of this strand on a particular run
    public int strandRunIndex;
    public enum StrandType {
      BUTTERFLY,
      FLOWER
    }
    StrandType strandType;
    public Run run;

    public List<LUButterfly> butterflies;
    public List<LUFlower> flowers;
    public List<LXPoint> allPoints;

    float x, y, z;

    public Strand(Run run, int strandId, StrandType strandType, float x, float y, float z, int strandRunIndex) {
      this.strandId = strandId;
      this.run = run;
      this.strandType = strandType;
      this.x = x;
      this.y = y;
      this.z = z;
      flowers = new ArrayList<LUFlower>();
      butterflies = new ArrayList<LUButterfly>();
      allPoints = new ArrayList<LXPoint>();

      int configuredNumFlowers = KaledoscopeApp.allStrandLengths.get(strandId);
      float flowerSpacing = 12f;
      for (int i = 0; i < configuredNumFlowers; i++) {
        int prevStrandsFlowers = run.flowers.size();
        // Flowers are wired from top to bottom since the wiring will be high up in the tree.
        LUFlower flower = new LUFlower(i, i + prevStrandsFlowers, x, y - i * flowerSpacing, z);
        flowers.add(flower);
        allFlowers.add(flower);
        allPoints.addAll(flower.allPoints);
      }
    }

    public Strand(Run run, int strandId, int numButterflies, float xpos, int strandRunIndex, List<Bezier> beziers) {
      this.strandId = strandId;
      strandType = StrandType.BUTTERFLY;
      butterflies = new ArrayList<LUButterfly>();
      flowers = new ArrayList<LUFlower>();
      allPoints = new ArrayList<LXPoint>();
      this.strandRunIndex = strandRunIndex;
      // The number of configured butterflies on this strand.
      // TODO(tracy): currently the butterfly positions are generated along a curve with an expected 20
      // butterflies per strand.  We need to re-parameterize T so that we can move a fixed distance along
      // the arc-length of the curve.  That way if we have only 10 butterflies then we would move 10 feet
      // along the curve since each butterfly is 1 foot apart.  Also, all current curves start and end at the
      // same Y position whereas in reality the start and end point for each cable could be arbitrary.
      int configuredNumButterflies = KaledoscopeApp.allStrandLengths.get(strandId);

      Bezier bezier = beziers.get(strandRunIndex);
      //float yOffset = strandIndex * numButterflies * butterflySpacingInches;
      for (int i = 0; i < configuredNumButterflies; i++) {
        // This provides the 't' parameter for our bezier curve.  We currently have 2 strands spanning
        // a single bezier curve.  There might not be a fixed mapping between bezier curves and strands.
        // It will more likely be anchor points at certain distances along the wire that generate the curves
        // so the bezier curves will need to be segmented by arc length or wire length or by number of
        // butterflies since we know they are 12 inches apart.
        float t;
        int bezierButterflyIndex;
        // NOTE(tracy): We are switching back to one strand per one bezier curve for a bit until we get
        // the configurable runs and strand lengths stuff worked out.  It is more likely that we will have
        // just 2 strands of 20 butterflies each on each separate run.  20 butterflies is 320 leds which is about the
        // max we should target for each pixlite output to keep the FPS reasonable.  So each steel wire will carry
        // two pixlite outputs.
        //if (strandIndex < 2) {
          bezierButterflyIndex = i; // + strandRunIndex * numButterflies;
        //} else {
        //  bezierButterflyIndex = i + (strandIndex - 2) * numButterflies;
        //}
        t = (float) bezierButterflyIndex / (float) (numButterflies * numStrandsPerRun/2);
        Point bPos = calculateBezierPoint(t, bezier.start, bezier.c1, bezier.c2, bezier.end);

        // The butterfly's index on the entire run length is the sum of all strands before this
        // strand plus the butterfly's position on this strand.
        // Check my parent 'Run', get the list of previous strands.  Count their butterflies.
        // Our parent Run's list of butterflies won't be updated with this strand's butterflies until the
        // constructor is finished so it currently accounts for all butterflies on previous strands.
        int prevStrandsButterflies = run.butterflies.size();
        LUButterfly butterfly = new LUButterfly(i, i + prevStrandsButterflies, bPos.x, 120f, bPos.y);
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
    public List<LUFlower> flowers;
    Bezier bezier1;
    Bezier bezier2;
    public List<Bezier> beziers;
    int runIndex;
    float cxOffset = 100f;
    float cyOffset = 30f;

    public enum RunType {
      BUTTERFLY,
      FLOWER
    }
    RunType runType;

    public Run(int runIndex, RunType runType, int numStrands, float x, float y, float z) {
      this.runIndex = runIndex;
      this.runType = runType;
      strands = new ArrayList<Strand>();
      butterflies = new ArrayList<LUButterfly>();
      flowers = new ArrayList<LUFlower>();
      allPoints = new ArrayList<LXPoint>();

      for (int i = 0; i < numStrands; i++) {
        Strand strand = new Strand(this, allStrands.size(), Strand.StrandType.FLOWER, x, y, z, i);
        allPoints.addAll(strand.allPoints);
        flowers.addAll(strand.flowers);
        allStrands.add(strand);
      }
    }

    public Run(int runIndex, float pos, int numStrands, int butterfliesPerStrand) {
      this.runIndex = runIndex;
      this.runType = RunType.BUTTERFLY;
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
      flowers = new ArrayList<LUFlower>();
      allPoints = new ArrayList<LXPoint>();
      beziers = new ArrayList<Bezier>();
      beziers.add(bezier1);
      beziers.add(bezier2);
      for (int i = 0; i < numStrands; i++) {
        Strand strand = new Strand(this, allStrands.size(), butterfliesPerStrand, pos, i, beziers);
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
    allFlowers = new ArrayList<LUFlower>();
    numStrandsPerRun = strandsPerRun;

    for (int i = 0; i < numRuns; i++) {
      Run run = new Run(i, i * lineSpacingInches, numStrandsPerRun, butterfliesPerStrand);
      allRuns.add(run);
      allPoints.addAll(run.allPoints);
    }

    int flowerRuns = KaledoscopeApp.runsFlowers;
    for (int i = 0; i < flowerRuns; i++) {
      // For now, flowers start at 8ft high.
      float x = -5f * 12f;
      float runSpacing = 10f * 12f;
      if (i % 2 == 1)
        x += 10 * 12f;
      Run run = new Run(allRuns.size(), Run.RunType.FLOWER, 1, x, 8f * 12f, i * runSpacing + 12f);
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
