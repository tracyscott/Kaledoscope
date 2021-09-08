package art.lookingup;

import heronarts.lx.model.LXPoint;
import heronarts.lx.model.StripModel;

import java.util.ArrayList;
import java.util.List;

/**
 * 16 LED Butterfly representation.  There are two 8 LED strips side-by-side.  For reference, we will refer to the view
 * of the butterfly from the belly side.  The first led is the on the top left.  For wiring purposes the LEDs are wired
 * from top left to bottom left to bottom right and then back up towards the top right.  The butterflies are then
 * wired into a "strand" of butterflies per output.  In physical space, there are maybe three or four long "runs" of
 * butterflies.  Each "run" can potentially be made up of several "strands" as might be necessary for FPS requirements.
 * So it may be possible that a single "run" which is suspended by steel wires may have multiple outputs.  In that case,
 * the long range data line for the successive "strands" on a "run" would need to be carried along the steel cable.
 * It would also require mounting a Pixlite Long Range Receiver along the cable somehow or located at intermediate
 * cable support trees/posts.
 *
 * To simplify patterns we want to be able to ask for LEDs in clockwise, counterclockwise (which is the actual wiring
 * order), and by row.  We should also implement some form of distance to each LED function to simplify spatial patterns
 * if we can get that far with the mapping.  It might also help with mapping.
 *
 * The current plan for spatial mapping is to implement something similar to a rotating radar beam but in light instead.
 * Since we know the distance between butterflies, we only need to know the position of the first butterfly and then
 * compute the relative angle of the second butterfly.  We will then be able to locate the second butterfly in 2D space
 * because it is just a vector of known length and orientation relative to the first butterfly.  We then repeat the
 * process for the next butterfly. It should move fairly quickly and we can adjust the beam width for an approximate
 * mapping that it not so tedious.
 */
public class LUButterfly {
  public static float ledSpacing = 0.3f;
  public static float stripSpacing = 0.6f;
  public List<LXPoint> right;
  public List<LXPoint> left;
  public List<LXPoint> allPoints;
  public List<LXPoint> pointsClockwise;
  public List<LXPoint> pointsCounterClockwise;
  public List<LXPoint> pointsByRow;
  public float x;
  public float y;
  public float z;
  public int strandIndex;
  public int runIndex;

  public LUButterfly(int strandIndex, int runIndex, float x, float y, float z) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.strandIndex = strandIndex;
    this.runIndex = runIndex;

    buildPoints();
  }

  public void buildPoints() {
    allPoints = new ArrayList<LXPoint>();
    left = new ArrayList<LXPoint>(8);
    right = new ArrayList<LXPoint>(8);
    pointsClockwise = new ArrayList<LXPoint>();
    pointsCounterClockwise = new ArrayList<LXPoint>();
    pointsByRow = new ArrayList<LXPoint>();

    for (int i = 0; i < 8; i++) {
      left.add(new LXPoint(x , y + i * ledSpacing, z));
    }
    for (int i = 0; i < 8; i++) {
      right.add(new LXPoint(x + stripSpacing, y + i * ledSpacing, z));
    }
    pointsClockwise.addAll(left);
    for (int i = right.size() - 1; i >= 0; i--)
      pointsClockwise.add(right.get(i));

    pointsCounterClockwise.addAll(right);
    for (int i = left.size() - 1; i >= 0; i--)
      pointsCounterClockwise.add(left.get(i));

    for (int i = 0; i < left.size(); i++) {
      pointsByRow.add(left.get(i));
      pointsByRow.add(right.get(i));
    }

    allPoints.addAll(left);
    allPoints.addAll(right);
  }
}
