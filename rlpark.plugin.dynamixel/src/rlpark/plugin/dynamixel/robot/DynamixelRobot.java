package rlpark.plugin.dynamixel.robot;

import rlpark.plugin.rltoys.envio.actions.Action;
import rlpark.plugin.rltoys.envio.observations.Legend;
import rlpark.plugin.robot.helpers.RobotEnvironment;
import rlpark.plugin.robot.helpers.Robots;
import rlpark.plugin.robot.observations.ObservationReceiver;
import zephyr.plugin.core.api.monitoring.abstracts.DataMonitor;
import zephyr.plugin.core.api.monitoring.abstracts.MonitorContainer;

public class DynamixelRobot extends RobotEnvironment implements MonitorContainer {
  private final DynamixelSerialConnection connection;

  public DynamixelRobot(DynamixelSerialPort serial, byte[] motorIDs) {
    this(new DynamixelSerialConnection(serial, motorIDs), false);
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
    byte[] message = null;
    if (a instanceof DynamixelAction)
      message = DynamixelMessage.buildMessage((DynamixelAction) a, connection.motorIDs());
    if (a instanceof DynamixelCompliantAction)
      message = DynamixelMessage.buildCompliantMessage((DynamixelCompliantAction) a, connection.motorIDs());
    connection.sendMessage(message);
  }

  @Override
  public void addToMonitor(DataMonitor monitor) {
    Robots.addToMonitor(monitor, this);
  }

  public int nbMotors() {
    return connection.motorIDs().length;
  }
}
