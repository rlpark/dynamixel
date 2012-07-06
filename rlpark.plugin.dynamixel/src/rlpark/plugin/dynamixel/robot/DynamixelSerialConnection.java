package rlpark.plugin.dynamixel.robot;

import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.concurrent.LinkedBlockingQueue;

import rlpark.plugin.dynamixel.internal.DynamixelConstant;
import rlpark.plugin.irobot.internal.descriptors.IRobotObservationReceiver;
import rlpark.plugin.irobot.internal.serial.SerialPortToRobot;
import rlpark.plugin.irobot.internal.serial.SerialPortToRobot.SerialPortInfo;
import rlpark.plugin.rltoys.envio.observations.Legend;
import rlpark.plugin.robot.internal.disco.datagroup.DropScalarGroup;
import rlpark.plugin.robot.internal.disco.drops.Drop;
import rlpark.plugin.robot.internal.sync.LiteByteBuffer;
import rlpark.plugin.robot.internal.sync.Syncs;
import rlpark.plugin.robot.observations.ObservationVersatile;
import zephyr.plugin.core.api.signals.Listener;
import zephyr.plugin.core.api.signals.Signal;

@SuppressWarnings("restriction")
public class DynamixelSerialConnection implements IRobotObservationReceiver {
  class DataListener implements Listener<SerialPortToRobot> {
    private final LiteByteBuffer byteBuffer;

    public DataListener(Drop drop) {
      byteBuffer = new LiteByteBuffer(drop.dataSize() / motorIDs.length, ByteOrder.LITTLE_ENDIAN);
    }

    @Override
    public void listen(SerialPortToRobot serial) {
      int maxSize = byteBuffer.capacity() - byteBuffer.offset();
      byte[] data = new byte[Math.min(maxSize, serial.available())];
      byteBuffer.put(serial.getAvailable(data));
      if (byteBuffer.capacity() - byteBuffer.offset() == 0) {
        serialPort.unregister(SerialPortEvent.DATA_AVAILABLE, this);
        try {
          queue.put(byteBuffer);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public Signal<DynamixelSerialConnection> onClosed = new Signal<DynamixelSerialConnection>();
  final LinkedBlockingQueue<LiteByteBuffer> queue = new LinkedBlockingQueue<LiteByteBuffer>();
  protected final String fileName;
  protected SerialPortToRobot serialPort;
  private final SerialPortInfo portInfo;
  private final Drop sensorDrop;
  private final DropScalarGroup sensors;
  private final byte[] motorIDs;
  private final LiteByteBuffer motorSensorBuffer;

  public DynamixelSerialConnection(String serialPortPath, byte[] motorIDs) {
    this(serialPortPath, new SerialPortInfo(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                                            SerialPort.PARITY_NONE, SerialPort.FLOWCONTROL_NONE), motorIDs);
  }

  public DynamixelSerialConnection(String serialPortPath, SerialPortInfo portInfo, byte[] motorIDs) {
    this.fileName = serialPortPath;
    this.portInfo = portInfo;
    this.motorIDs = motorIDs;
    sensorDrop = DynamixelDescriptor.newMotorReadDrop(motorIDs.length);
    sensors = new DropScalarGroup(sensorDrop);
    motorSensorBuffer = new LiteByteBuffer(sensorDrop.dataSize(), ByteOrder.LITTLE_ENDIAN);
  }

  @Override
  public void initialize() {
    serialPort = SerialPortToRobot.openPort(fileName, portInfo);
    if (serialPort == null)
      return;
    serialPort.onClosed.connect(new Listener<SerialPortToRobot>() {
      @Override
      public void listen(SerialPortToRobot eventInfo) {
        close();
      }
    });
    serialPort.getAvailable();
  }

  @Override
  public boolean isClosed() {
    return serialPort == null || serialPort.isClosed();
  }

  @Override
  public void sendMessage(byte[] bytes) {
    try {
      serialPort.send(bytes);
    } catch (IOException e) {
      e.printStackTrace();
      close();
      SerialPortToRobot.fatalError("error while sending message");
    }
  }

  synchronized public void close() {
    serialPort.close();
    notifyAll();
    onClosed.fire(this);
  }

  @Override
  public Legend legend() {
    return sensors.legend();
  }

  @Override
  public int packetSize() {
    return sensorDrop.dataSize();
  }

  private void sendReadRequest(byte servoID, byte address, byte nbRegisters) throws IOException {
    byte length = 4; // instruction, address, size, checksum
    byte checksum = (byte) (255 - ((servoID + length + DynamixelConstant.DXL_READ_DATA + address + nbRegisters) % 256));
    byte[] data = new byte[] { (byte) 0xff, (byte) 0xff, servoID, length, DynamixelConstant.DXL_READ_DATA, address,
        nbRegisters, checksum };
    serialPort.send(data);
  }

  private boolean checkSum(byte[] response) {
    byte checksum = 0;
    for (int i = 2; i < response.length - 1; i++)
      checksum += response[i];
    checksum = (byte) ~checksum;
    return (response[response.length - 1] == checksum);
  }

  @Override
  public ObservationVersatile waitForData() {
    motorSensorBuffer.clear();
    for (byte motorID : motorIDs) {
      LiteByteBuffer motorBuffer = requestMotorSensors(motorID);
      if (motorBuffer == null)
        return null;
      motorSensorBuffer.put(motorBuffer.array());
    }
    return Syncs.createObservation(System.currentTimeMillis(), motorSensorBuffer, sensors);
  }

  private LiteByteBuffer requestMotorSensors(byte motorID) {
    serialPort.register(SerialPortEvent.DATA_AVAILABLE, new DataListener(sensorDrop));
    try {
      sendReadRequest(motorID, DynamixelConstant.DXL_PRESENT_POSITION_L, (byte) 6);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
    LiteByteBuffer motorBuffer = null;
    try {
      motorBuffer = queue.take();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return motorBuffer;
  }
}
