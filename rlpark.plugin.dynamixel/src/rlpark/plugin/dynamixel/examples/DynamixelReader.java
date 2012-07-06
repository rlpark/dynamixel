package rlpark.plugin.dynamixel.examples;

import rlpark.plugin.dynamixel.robot.DynamixelAction;
import rlpark.plugin.dynamixel.robot.DynamixelRobot;
import zephyr.plugin.core.api.Zephyr;
import zephyr.plugin.core.api.monitoring.annotations.IgnoreMonitor;
import zephyr.plugin.core.api.monitoring.annotations.Monitor;
import zephyr.plugin.core.api.synchronization.Clock;

@Monitor
public class DynamixelReader implements Runnable {
  @IgnoreMonitor
  private static final byte[] motorIDs = new byte[] { 91, 92, 93, 94, 95, 96 };
  public DynamixelRobot robot = new DynamixelRobot("/dev/ttyUSB0", motorIDs);
  private final Clock clock = new Clock("robot");
  private final int[] pos = new int[motorIDs.length];

  public DynamixelReader() {
    Zephyr.advertise(clock, this);
  }

  @Override
  public void run() {
    while (clock.tick()) {
      robot.waitNewObs();
      for (int i = 0; i < robot.nbMotors(); i++) {
        pos[i] = (int) ((Math.sin(((clock.timeStep() + i * 100) % 360) / 180.0 * Math.PI)) * 200 + 512);
      }
      robot.sendAction(new DynamixelAction(pos));
      // robot.sendAction(new DynamixelCompliantAction(true));
    }
    robot.close();
  }
}
