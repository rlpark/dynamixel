package rlpark.plugin.dynamixel.examples;

import rlpark.plugin.dynamixel.robot.DynamixelAction;
import rlpark.plugin.dynamixel.robot.DynamixelRobot;
import zephyr.plugin.core.api.Zephyr;
import zephyr.plugin.core.api.monitoring.annotations.Monitor;
import zephyr.plugin.core.api.synchronization.Clock;

public class DynamixelReader implements Runnable {
  @Monitor
  public DynamixelRobot robot = new DynamixelRobot("/dev/ttyUSB0", new byte[] { 96 });
  private final Clock clock = new Clock("robot");

  public DynamixelReader() {
    Zephyr.advertise(clock, this);
  }

  @Override
  public void run() {
    while (clock.tick()) {
      robot.waitNewObs();
      robot.sendAction(new DynamixelAction(new int[] { 512 }, new int[] { 512 }, new int[] { 512 }));
    }
    robot.close();
  }
}
