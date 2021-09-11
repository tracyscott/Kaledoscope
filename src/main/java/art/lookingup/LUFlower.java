package art.lookingup;

import heronarts.lx.model.LXPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a flower fixture.  A flower has 1 central LED and 5 petal LEDs but the petals
 * all share an address so there are only 2 addressable LEDs.  We will create LXPoints for
 * all 6 LEDs, but only output the center LED and one petal value.
 */
public class LUFlower {

  public LXPoint center;
  public LXPoint[] petals;
  public List<LXPoint> mappablePoints;
  public List<LXPoint> allPoints;
  public float x;
  public float y;
  public float z;
  public int strandIndex;
  public int runIndex;
  static final float RADIUS = 1.5f;


  public LUFlower(int strandIndex, int runIndex, float x, float y, float z) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.strandIndex = strandIndex;
    this.runIndex = runIndex;

    buildPoints();
  }

  protected void buildPoints() {
    center = new LXPoint(x, y, z);
    petals = new LXPoint[4];
    petals[0] = new LXPoint(x + RADIUS, y, z);
    petals[1] = new LXPoint(x, y + RADIUS, z);
    petals[2] = new LXPoint(x - RADIUS, y, z);
    petals[3] = new LXPoint(x, y - RADIUS, z);
    mappablePoints = new ArrayList<LXPoint>();
    mappablePoints.add(center);
    mappablePoints.add(petals[0]);
    allPoints = new ArrayList<LXPoint>();
    allPoints.addAll(mappablePoints);
    allPoints.add(petals[1]);
    allPoints.add(petals[2]);
    allPoints.add(petals[3]);
  }
}
