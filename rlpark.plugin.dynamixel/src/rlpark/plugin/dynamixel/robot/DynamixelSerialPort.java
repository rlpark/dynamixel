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
import java.util.TooManyListenersException;
import java.util.concurrent.Semaphore;

import rlpark.plugin.dynamixel.internal.DynamixelConstant;
import rlpark.plugin.irobot.internal.serial.SerialPortToRobot.SerialPortInfo;
import rlpark.plugin.irobot.internal.serial.SerialPorts;
import zephyr.plugin.core.api.synchronization.Chrono;

@SuppressWarnings("restriction")
public class DynamixelSerialPort implements SerialPortEventListener {
  private final SerialPort serial;
  private InputStream input;
  private OutputStream output;
  private final Semaphore semaphore = new Semaphore(1, true);

  public DynamixelSerialPort(String serialPortPath) {
    serial = openPort(serialPortPath, new SerialPortInfo(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                                                         SerialPort.PARITY_NONE, SerialPort.FLOWCONTROL_NONE));
    try {
      serial.addEventListener(this);
      serial.notifyOnDataAvailable(true);
      try {
        serial.enableReceiveTimeout(10);
      } catch (UnsupportedCommOperationException e) {
        System.err.println("Receive timeout unsupported");
        e.printStackTrace();
      }
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

  public boolean sendAndReceive(byte[] send, byte[] bytes) {
    acquireSemaphore();
    flushSerialPort();
    sendMessageInternal(send);
    boolean result = receiveMessageInternal(bytes);
    releaseSemaphore();
    return result;
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

  public void sendReadRequest(byte servoID, byte address, byte nbRegisters) {
    acquireSemaphore();
    byte length = 4; // instruction, address, size, checksum
    byte checksum = (byte) (255 - ((servoID + length + DynamixelConstant.DXL_READ_DATA + address + nbRegisters) % 256));
    byte[] data = new byte[] { (byte) 0xff, (byte) 0xff, servoID, length, DynamixelConstant.DXL_READ_DATA, address,
        nbRegisters, checksum };
    sendMessageInternal(data);
    releaseSemaphore();
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


  public boolean receiveMessage(byte[] bytes) {
    acquireSemaphore();
    boolean result = receiveMessageInternal(bytes);
    releaseSemaphore();
    return result;
  }

  private boolean receiveMessageInternal(byte[] bytes) {
    Chrono chrono = new Chrono();
    int nbAvailable = 0;
    while (nbAvailable < bytes.length) {
      try {
        nbAvailable = input.available();
        if (chrono.getCurrentMillis() > 50) {
          byte[] buffer = flushSerialPort();
          System.out.print("timeout ");
          for (byte b : buffer)
            System.out.print(Integer.toHexString(b) + " ");
          System.out.println("");
          return false;
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    try {
      if (input.read(bytes, 0, bytes.length) != bytes.length) {
        flushSerialPort();
        return false;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return true;
  }

  public void sendMessage(byte[] bytes) {
    acquireSemaphore();
    flushSerialPort();
    sendMessageInternal(bytes);
    releaseSemaphore();
  }

  private void sendMessageInternal(byte[] bytes) {
    // flushSerialPort();
    try {
      output.write(bytes);
      // output.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private SerialPort openPort(String serialPortFile, SerialPortInfo portInfo) {
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

}
