package rlpark.plugin.dynamixel.robot;

import java.util.ArrayList;
import java.util.List;


@SuppressWarnings("restriction")
public class DynamixelRobots {

  static private boolean ping(DynamixelSerialPort serial, byte motorID) {
    byte[] data = DynamixelMessage.buildPingMessage(motorID);
    byte[] buffer = new byte[6];
    serial.sendMessage(data);
    boolean result = serial.receiveMessage(buffer);
    return result;
  }

  public static byte[] scan(DynamixelSerialPort serial, int maxMotorId) {
    List<Byte> motorsIDs = new ArrayList<Byte>();
    for (int i = 1; i < maxMotorId; i++) {
      if (ping(serial, (byte) i) == true)
        motorsIDs.add((byte) i);
    }
    byte[] result = new byte[motorsIDs.size()];
    for (int i = 0; i < motorsIDs.size(); i++)
      result[i] = motorsIDs.get(i);
    return result;
  }
}
