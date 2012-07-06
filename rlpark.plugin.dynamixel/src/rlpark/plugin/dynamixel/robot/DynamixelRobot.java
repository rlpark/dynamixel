package rlpark.plugin.dynamixel.robot;

import java.util.Arrays;

import rlpark.plugin.rltoys.envio.actions.Action;
import rlpark.plugin.rltoys.envio.observations.Legend;
import rlpark.plugin.robot.helpers.RobotEnvironment;
import rlpark.plugin.robot.helpers.Robots;
import rlpark.plugin.robot.observations.ObservationReceiver;
import zephyr.plugin.core.api.monitoring.abstracts.DataMonitor;
import zephyr.plugin.core.api.monitoring.abstracts.MonitorContainer;

public class DynamixelRobot extends RobotEnvironment implements MonitorContainer {
  private final DynamixelSerialConnection connection;

  public DynamixelRobot(String serialPortPath, byte[] motorIDs) {
    this(new DynamixelSerialConnection(serialPortPath, motorIDs), false);
  }

  private DynamixelRobot(ObservationReceiver receiver, boolean persistent) {
    super(receiver, persistent);
    connection = (DynamixelSerialConnection) receiver;
  }

  @Override
  public Legend legend() {
    return connection.legend();
  }

  @Override
  public void sendAction(Action a) {
    connection.sendMessage(((DynamixelAction) a).buildMessage());
  }

  @Override
  public void addToMonitor(DataMonitor monitor) {
    Robots.addToMonitor(monitor, this);
  }

  public static void main(String[] args) {
    DynamixelRobot robot = new DynamixelRobot("/dev/ttyUSB0", new byte[] { 96 });
    for (int i = 0; i < 10000; i++) {
      System.out.println(Arrays.toString(robot.waitNewObs()));
    }
  }
}
