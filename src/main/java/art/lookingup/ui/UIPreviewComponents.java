package art.lookingup.ui;

import art.lookingup.KaledoscopeApp;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.studio.LXStudio;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UICollapsibleSection;
import art.lookingup.KaledoscopeApp;

public class UIPreviewComponents extends UICollapsibleSection {

  public UIPreviewComponents(final LXStudio.UI ui) {
    super(ui, 0, 0, ui.leftPane.global.getContentWidth(), 50);
    setTitle("Axes");
    UI2dContainer knobsContainer = new UI2dContainer(0, 0, getContentWidth(), 20);
    knobsContainer.setLayout(UI2dContainer.Layout.HORIZONTAL);
    knobsContainer.setPadding(10, 10, 10, 10);
    knobsContainer.addToContainer(this);

    UIButton showAxesBtn = new UIButton() {
      @Override
      public void onToggle(boolean on) {
        KaledoscopeApp.axes.showAxes = on;
      }
    }.setLabel("axes").setActive(KaledoscopeApp.axes.showAxes);
    showAxesBtn.setWidth(35).setHeight(16);
    showAxesBtn.addToContainer(knobsContainer);
    UIButton showFloorBtn = new UIButton() {
      @Override
      public void onToggle(boolean on) {
        KaledoscopeApp.axes.showFloor = on;
      }
    }.setLabel("floor").setActive(KaledoscopeApp.axes.showFloor);
    showFloorBtn.setWidth(35).setHeight(16);
    showFloorBtn.addToContainer(knobsContainer);
    UIButton showCtrlPointsBtn = new UIButton() {
      @Override
      public void onToggle(boolean on) {
        KaledoscopeApp.axes.showCtrlPoints = on;
      }
    }.setLabel("ctrl pts").setActive(KaledoscopeApp.axes.showCtrlPoints);
    showCtrlPointsBtn.setWidth(35).setHeight(16);
    showCtrlPointsBtn.addToContainer(knobsContainer);
  }
}
