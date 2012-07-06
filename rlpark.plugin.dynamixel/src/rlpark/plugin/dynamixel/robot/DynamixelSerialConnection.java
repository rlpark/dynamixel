package rlpark.plugin.dynamixel.robot;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.util.TooManyListenersException;
import java.util.concurrent.Semaphore;

import rlpark.plugin.dynamixel.internal.DynamixelConstant;
import rlpark.plugin.irobot.internal.descriptors.IRobotObservationReceiver;
import rlpark.plugin.irobot.internal.serial.SerialPortToRobot.SerialPortInfo;
import rlpark.plugin.irobot.internal.serial.SerialPorts;
import rlpark.plugin.rltoys.envio.observations.Legend;
import rlpark.plugin.robot.internal.disco.datagroup.DropScalarGroup;
import rlpark.plugin.robot.internal.disco.drops.Drop;
import rlpark.plugin.robot.internal.sync.LiteByteBuffer;
import rlpark.plugin.robot.internal.sync.Syncs;
import rlpark.plugin.robot.observations.ObservationVersatile;
import zephyr.plugin.core.api.synchronization.Chrono;

@SuppressWarnings("restriction")
public class DynamixelSerialConnection implements IRobotObservationReceiver, SerialPortEventListener {
  private final String serialPortPath;
  private final byte[] motorIDs;
  private SerialPort serial;
  private final LiteByteBuffer motorSensorBuffer;
  private final Drop sensorDrop;
  private final DropScalarGroup sensors;
  private InputStream input;
  private OutputStream output;
  private final Semaphore semaphore = new Semaphore(1, true);

  DynamixelSerialConnection(String serialPortPath, byte[] motorIDs) {
    this.serialPortPath = serialPortPath;
    this.motorIDs = motorIDs;
    sensorDrop = DynamixelDescriptor.newMotorReadDrop(motorIDs.length);
    sensors = new DropScalarGroup(sensorDrop);
    motorSensorBuffer = new LiteByteBuffer(sensorDrop.dataSize(), ByteOrder.LITTLE_ENDIAN);

  }

  @Override
  synchronized public void initialize() {
    serial = openPort(serialPortPath, new SerialPortInfo(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                                                         SerialPort.PARITY_NONE, SerialPort.FLOWCONTROL_NONE));
    try {
      serial.addEventListener(this);
    } catch (TooManyListenersException e1) {
      e1.printStackTrace();
    }
    try {
      input = serial.getInputStream();
    } catch (IOException e) {
      e.printStackTrace();
    }
    try {
      output = serial.getOutputStream();
    } catch (IOException e) {
      e.printStackTrace();
    }
    flushSerialPort();
  }

  @Override
  public int packetSize() {
    return sensorDrop.packetSize();
  }


  @Override
  public ObservationVersatile waitForData() {
    acquireSemaphore();

    motorSensorBuffer.clear();
    for (byte motorID : motorIDs) {
      byte[] motorData = requestMotorSensors(motorID);
      if (motorData != null)
        motorSensorBuffer.put(motorData);
    }

    releaseSemaphore();
    return Syncs.createObservation(System.currentTimeMillis(), motorSensorBuffer, sensors);
  }

  private void releaseSemaphore() {
    semaphore.release();
  }

  private void acquireSemaphore() {
    try {
      semaphore.acquire();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private void sendReadRequest(byte servoID, byte address, byte nbRegisters) {
    byte length = 4; // instruction, address, size, checksum
    byte checksum = (byte) (255 - ((servoID + length + DynamixelConstant.DXL_READ_DATA + address + nbRegisters) % 256));
    byte[] data = new byte[] { (byte) 0xff, (byte) 0xff, servoID, length, DynamixelConstant.DXL_READ_DATA, address,
        nbRegisters, checksum };
    sendMessageInternal(data);
  }

  private byte[] flushSerialPort() {
    byte[] buffer;
    try {
      buffer = new byte[input.available()];
      input.read(buffer);
    } catch (IOException e) {
      e.printStackTrace();
      buffer = new byte[0];
    }
    return buffer;
  }

  private byte[] requestMotorSensors(byte motorID) {
    sendReadRequest(motorID, DynamixelConstant.DXL_PRESENT_POSITION_L, (byte) 6);
    byte[] buffer = new byte[12];
    try {
      Thread.sleep(1);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    receiveMessage(buffer);
    return buffer;
  }

  @Override
  public boolean isClosed() {
    return false;
  }

  private void receiveMessage(byte[] bytes) {
    int nbAvailable = 0;
    Chrono chrono = new Chrono();
    while (nbAvailable < bytes.length) {
      try {
        nbAvailable = input.available();
        if (chrono.getCurrentChrono() > 2) {
          byte[] buffer = flushSerialPort();
          System.out.print("timeout ");
          for (byte b : buffer)
            System.out.print(Integer.toHexString(b) + " ");
          System.out.println("");
          return;
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    try {
      input.read(bytes);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void sendMessage(byte[] bytes) {
    acquireSemaphore();
    sendMessageInternal(bytes);
    releaseSemaphore();
  }

  public void sendMessageInternal(byte[] bytes) {
    try {
      output.write(bytes);
      output.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public Legend legend() {
    return sensors.legend();
  }

  static public SerialPort openPort(String serialPortFile, SerialPortInfo portInfo) {
    SerialPorts.refreshPortIdentifiers();
    CommPortIdentifier identifier = SerialPorts.getPortIdentifier(serialPortFile);
    if (identifier == null)
      throw new RuntimeException("Port identifier " + serialPortFile + " not found");
    SerialPort serialPort;
    try {
      serialPort = (SerialPort) identifier.open("RLPark", 150);
    } catch (PortInUseException e) {
      e.printStackTrace();
      return null;
    }
    try {
      serialPort.setFlowControlMode(portInfo.flowControl);
      serialPort.setSerialPortParams(portInfo.rate, portInfo.databits, portInfo.stopbits, portInfo.parity);
    } catch (UnsupportedCommOperationException e) {
      e.printStackTrace();
      return null;
    }
    return serialPort;
  }

  @Override
  public void serialEvent(SerialPortEvent event) {
    switch (event.getEventType()) {
    case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
      System.out.println("Event received: outputBufferEmpty");
      break;

    case SerialPortEvent.DATA_AVAILABLE:
      System.out.println("Event received: dataAvailable");
      break;

    case SerialPortEvent.BI:
      System.out.println("Event received: breakInterrupt");
      break;

    case SerialPortEvent.CD:
      System.out.println("Event received: carrierDetect");
      break;

    case SerialPortEvent.CTS:
      System.out.println("Event received: clearToSend");
      break;

    case SerialPortEvent.DSR:
      System.out.println("Event received: dataSetReady");
      break;

    case SerialPortEvent.FE:
      System.out.println("Event received: framingError");
      break;

    case SerialPortEvent.OE:
      System.out.println("Event received: overrunError");
      break;

    case SerialPortEvent.PE:
      System.out.println("Event received: parityError");
      break;
    case SerialPortEvent.RI:
      System.out.println("Event received: ringIndicator");
      break;
    default:
      System.out.println("Event received: unknown");
    }
  }

  public byte[] motorIDs() {
    return motorIDs;
  }
}
