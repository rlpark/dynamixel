package rlpark.plugin.dynamixel.robot;

import java.util.Arrays;

import rlpark.plugin.rltoys.envio.actions.Action;

public class DynamixelAction implements Action {
  private static final long serialVersionUID = 2229719515606347547L;
  final int[] positions;
  final int[] speeds;
  final int[] torques;

  public DynamixelAction(int[] positions) {
    this(positions, defaultSpeeds(positions), defaultTorques(positions));
  }

  private static int[] defaultSpeeds(int[] positions) {
    int[] speeds = new int[positions.length];
    Arrays.fill(speeds, 512);
    return speeds;
  }

  private static int[] defaultTorques(int[] positions) {
    int[] torques = new int[positions.length];
    Arrays.fill(torques, 512);
    return torques;
  }

  public DynamixelAction(int[] positions, int[] speeds, int[] torques) {
    assert positions.length == speeds.length;
    assert positions.length == torques.length;
    this.positions = positions;
    this.speeds = speeds;
    this.torques = torques;
  }
}
