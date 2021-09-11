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
public class RunPtrn extends LXPattern {
  DiscreteParameter runNum = new DiscreteParameter("run", 0, 0, 7);
  BooleanParameter tracer = new BooleanParameter("tracer", false);

  int currentIndex = 0;


  public RunPtrn(LX lx) {
    super(lx);
    addParameter("run", runNum);
    addParameter("tracer", tracer);
  }


  @Override
  protected void run(double deltaMs) {
    for (LXPoint p : model.points) {
      colors[p.index] = LXColor.rgb(0, 0, 0);
    }
    KaledoscopeModel.Run run = KaledoscopeModel.allRuns.get(runNum.getValuei());
    if (tracer.getValueb()) {
      for (int i = 0; i < run.allPoints.size(); i++) {
        if (currentIndex == i)
          colors[run.allPoints.get(i).index] = LXColor.rgb(255, 255, 255);
      }
      currentIndex = (currentIndex + 1) % run.allPoints.size();
    } else {
      for (LXPoint p : run.allPoints) {
        colors[p.index] = LXColor.rgb(255, 255, 255);
      }
    }
  }
}
