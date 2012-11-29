package rlpark.plugin.dynamixel.robot;

import java.nio.ByteOrder;

import rlpark.plugin.dynamixel.internal.DynamixelConstant;
import rlpark.plugin.dynamixel.internal.DynamixelDescriptor;
import rlpark.plugin.irobot.internal.descriptors.IRobotObservationReceiver;
import rlpark.plugin.rltoys.envio.observations.Legend;
import rlpark.plugin.robot.internal.disco.datagroup.DropScalarGroup;
import rlpark.plugin.robot.internal.disco.drops.Drop;
import rlpark.plugin.robot.internal.sync.LiteByteBuffer;
import rlpark.plugin.robot.internal.sync.Syncs;
import rlpark.plugin.robot.observations.ObservationVersatile;

@SuppressWarnings("restriction")
public class DynamixelSerialConnection implements IRobotObservationReceiver {
  private final byte[] motorIDs;
  private final DynamixelSerialPort serial;
  private final LiteByteBuffer motorSensorBuffer;
  private final Drop sensorDrop;
  private final DropScalarGroup sensors;


  DynamixelSerialConnection(DynamixelSerialPort serial, byte[] motorIDs) {
    this.serial = serial;
    this.motorIDs = motorIDs;
    sensorDrop = DynamixelDescriptor.newMotorReadDrop(motorIDs.length);
    sensors = new DropScalarGroup(sensorDrop);
    motorSensorBuffer = new LiteByteBuffer(sensorDrop.dataSize(), ByteOrder.LITTLE_ENDIAN);
  }

  @Override
  synchronized public void initialize() {
  }

  @Override
  public int packetSize() {
    return sensorDrop.packetSize();
  }

  @Override
  public ObservationVersatile waitForData() {
    motorSensorBuffer.clear();
    for (byte motorID : motorIDs) {
      byte[] motorData = requestMotorSensors(motorID);
      if (motorData != null)
        motorSensorBuffer.put(motorData);
    }

    return Syncs.createObservation(System.currentTimeMillis(), motorSensorBuffer, sensors);
  }


  private byte[] requestMotorSensors(byte motorID) {
    byte[] buffer = new byte[14];
    while (!serial.sendAndReceive(DynamixelMessage.buildReadRequestMessage(motorID,
                                                                           DynamixelConstant.DXL_PRESENT_POSITION_L,
                                                                           (byte) 8), buffer)) {
      try {
        Thread.sleep(0, 100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    return buffer;
  }

  @Override
  public boolean isClosed() {
    return false;
  }

  @Override
  public Legend legend() {
    return sensors.legend();
  }

  public byte[] motorIDs() {
    return motorIDs;
  }

  @Override
  public void sendMessage(byte[] bytes) {
    serial.sendMessage(bytes);
  }
}
