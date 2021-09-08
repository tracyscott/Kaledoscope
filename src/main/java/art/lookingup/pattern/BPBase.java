package art.lookingup.pattern;

import art.lookingup.KaledoscopeModel;
import art.lookingup.LUButterfly;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.pattern.LXPattern;

import java.util.*;

abstract public class BPBase extends LXPattern {

  protected static final Random random = new Random();
  protected Map<LUButterfly, Integer> randomInts;

  public BPBase(LX lx) {
    super(lx);

  }

  public void onActive() {
    randomInts = new HashMap<LUButterfly, Integer>();
    for (LUButterfly butterfly : KaledoscopeModel.allButterflies) {
      randomInts.put(butterfly, random.nextInt(1000));
    }
  }

  protected int getRandom(LUButterfly butterfly) {
    return randomInts.get(butterfly);
  }

  @Override
  protected void run(double deltaMs) {
    for (LXPoint p : model.points) {
      colors[p.index] = LXColor.rgb(0, 0, 0);
    }
    for (LUButterfly butterfly : KaledoscopeModel.allButterflies) {
      renderButterfly(deltaMs, butterfly, getRandom(butterfly));
    }
  }

  abstract void renderButterfly(double deltaMs, LUButterfly butterfly, int randomInt);
}
