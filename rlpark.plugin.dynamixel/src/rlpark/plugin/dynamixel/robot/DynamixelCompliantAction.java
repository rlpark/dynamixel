package rlpark.plugin.dynamixel.robot;

import rlpark.plugin.rltoys.envio.actions.Action;

public class DynamixelCompliantAction implements Action {
  boolean enable = false;

  private static final long serialVersionUID = 2394578411873944176L;

  public DynamixelCompliantAction(boolean enable) {
    this.enable = enable;
  }

}
