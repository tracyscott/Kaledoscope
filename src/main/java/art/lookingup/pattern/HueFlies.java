package art.lookingup.pattern;

import art.lookingup.LUButterfly;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;

public class HueFlies extends BPBase {

  public HueFlies(LX lx) {
    super(lx);
  }

  protected void renderButterfly(double drawDeltaMs, LUButterfly butterfly, int randomInt) {
    for (LXPoint p : butterfly.allPoints) {
      float hue = 360 * (float)(randomInt)/1000f;
      colors[p.index] = LXColor.hsb(hue, 100, 100);
    }
  }
}
