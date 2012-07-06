package rlpark.plugin.dynamixel.robot;

import java.nio.ByteOrder;
import java.util.Arrays;

import rlpark.plugin.dynamixel.internal.DynamixelConstant;
import rlpark.plugin.rltoys.envio.actions.Action;
import rlpark.plugin.rltoys.envio.observations.Legend;
import rlpark.plugin.robot.helpers.RobotEnvironment;
import rlpark.plugin.robot.helpers.Robots;
import rlpark.plugin.robot.internal.sync.LiteByteBuffer;
import rlpark.plugin.robot.observations.ObservationReceiver;
import zephyr.plugin.core.api.monitoring.abstracts.DataMonitor;
import zephyr.plugin.core.api.monitoring.abstracts.MonitorContainer;


@SuppressWarnings("restriction")
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
    byte[] message = null;
    if (a instanceof DynamixelAction)
      message = buildMessage((DynamixelAction) a);
    connection.sendMessage(message);
  }

  private byte[] buildMessage(DynamixelAction a) {
    byte length = (byte) (4 + a.positions.length * 2 * 3 + a.positions.length);

    byte[] header = new byte[] { (byte) 0xff, (byte) 0xff, DynamixelConstant.DXL_BROADCAST, length,
        DynamixelConstant.DXL_SYNC_WRITE, DynamixelConstant.DXL_GOAL_POSITION_L, 6 };

    byte checksum = (byte) (DynamixelConstant.DXL_BROADCAST + length + DynamixelConstant.DXL_SYNC_WRITE
        + DynamixelConstant.DXL_GOAL_POSITION_L + 6);

    LiteByteBuffer motorData = new LiteByteBuffer(7 * a.positions.length, ByteOrder.LITTLE_ENDIAN);
    for (int i = 0; i < a.positions.length; i++) {
      motorData.put(connection.motorIDs()[i]);
      motorData.putShort((short) a.positions[i]);
      motorData.putShort((short) a.speeds[i]);
      motorData.putShort((short) a.torques[i]);
    }

    for (byte b : motorData.array())
      checksum += b;

    checksum = (byte) (255 - (checksum % 256));
    LiteByteBuffer buffer = new LiteByteBuffer(header.length + motorData.capacity() + 1, ByteOrder.LITTLE_ENDIAN);
    buffer.put(header);
    buffer.put(motorData.array());
    buffer.put(checksum);
    return buffer.array();
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

  public int nbMotors() {
    return connection.motorIDs().length;
  }
}
