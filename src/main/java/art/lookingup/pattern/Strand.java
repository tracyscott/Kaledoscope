package art.lookingup.pattern;

import art.lookingup.KaledoscopeModel;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.pattern.LXPattern;

@LXCategory(LXCategory.TEST)
public class Strand extends LXPattern {
  DiscreteParameter strandNum = new DiscreteParameter("strand", 0, 0, 6);
  public Strand(LX lx) {
    super(lx);
    addParameter("strand", strandNum);
  }


  @Override
  protected void run(double deltaMs) {
    for (LXPoint p : model.points) {
      colors[p.index] = LXColor.rgb(0, 0, 0);
    }
    for (LXPoint p : KaledoscopeModel.allStrands.get(strandNum.getValuei()).allPoints) {
      colors[p.index] = LXColor.rgb(255, 255, 255);
    }
  }
}
