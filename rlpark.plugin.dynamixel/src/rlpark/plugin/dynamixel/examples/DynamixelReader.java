package rlpark.plugin.dynamixel.examples;

import rlpark.plugin.dynamixel.robot.DynamixelAction;
import rlpark.plugin.dynamixel.robot.DynamixelRobot;
import rlpark.plugin.dynamixel.robot.DynamixelRobots;
import rlpark.plugin.dynamixel.robot.DynamixelSerialPort;
import zephyr.plugin.core.api.Zephyr;
import zephyr.plugin.core.api.monitoring.annotations.Monitor;
import zephyr.plugin.core.api.synchronization.Clock;

@Monitor
public class DynamixelReader implements Runnable {
  DynamixelSerialPort serial = new DynamixelSerialPort("/dev/ttyUSB0");
  public DynamixelRobot robot = new DynamixelRobot(serial, DynamixelRobots.scan(serial, 100));
  private final Clock clock = new Clock("robot");

  public DynamixelReader() {
    Zephyr.advertise(clock, this);
  }

  @Override
  public void run() {
    int[] pos = new int[robot.nbMotors()];

    // robot.sendAction(new DynamixelCompliantAction(true));

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
