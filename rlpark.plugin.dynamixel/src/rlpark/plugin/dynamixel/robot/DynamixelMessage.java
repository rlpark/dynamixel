package rlpark.plugin.dynamixel.robot;

import java.nio.ByteOrder;

import rlpark.plugin.dynamixel.internal.DynamixelConstant;
import rlpark.plugin.robot.internal.sync.LiteByteBuffer;

@SuppressWarnings("restriction")
public class DynamixelMessage {
  DynamixelMessage() {
  }

  public static byte[] buildPingMessage(byte servo_id) {
    byte length = (byte) 2;
    byte checksum = (byte) (255 - (servo_id + length + DynamixelConstant.DXL_PING) % 256);
    byte[] data = new byte[] { (byte) 0xff, (byte) 0xff, servo_id, length, DynamixelConstant.DXL_PING, checksum };
    return data;
  }

  public static byte[] buildCompliantMessage(DynamixelCompliantAction a, byte[] motorIDs) {
    byte length = (byte) (4 + motorIDs.length * 2);

    byte[] header = new byte[] { (byte) 0xff, (byte) 0xff, DynamixelConstant.DXL_BROADCAST, length,
        DynamixelConstant.DXL_SYNC_WRITE, DynamixelConstant.DXL_TORQUE_ENABLE, 1 };

    byte checksum = (byte) (DynamixelConstant.DXL_BROADCAST + length + DynamixelConstant.DXL_SYNC_WRITE
        + DynamixelConstant.DXL_TORQUE_ENABLE + 1);

    LiteByteBuffer motorData = new LiteByteBuffer(2 * motorIDs.length, ByteOrder.LITTLE_ENDIAN);
    byte enable = (byte) ((a.enable == false) ? 1 : 0);
    for (int i = 0; i < motorIDs.length; i++) {
      motorData.put(motorIDs[i]);
      motorData.put(enable);
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

  public static byte[] buildMessage(DynamixelAction a, byte[] motorIDs) {
    byte length = (byte) (4 + a.positions.length * 2 * 3 + a.positions.length);

    byte[] header = new byte[] { (byte) 0xff, (byte) 0xff, DynamixelConstant.DXL_BROADCAST, length,
        DynamixelConstant.DXL_SYNC_WRITE, DynamixelConstant.DXL_GOAL_POSITION_L, 6 };

    byte checksum = (byte) (DynamixelConstant.DXL_BROADCAST + length + DynamixelConstant.DXL_SYNC_WRITE
        + DynamixelConstant.DXL_GOAL_POSITION_L + 6);

    LiteByteBuffer motorData = new LiteByteBuffer(7 * a.positions.length, ByteOrder.LITTLE_ENDIAN);
    for (int i = 0; i < a.positions.length; i++) {
      motorData.put(motorIDs[i]);
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

  public static byte[] buildReadRequestMessage(byte servoID, byte address, byte nbRegisters) {
    byte length = 4; // instruction, address, size, checksum
    byte checksum = (byte) (255 - ((servoID + length + DynamixelConstant.DXL_READ_DATA + address + nbRegisters) % 256));
    byte[] data = new byte[] { (byte) 0xff, (byte) 0xff, servoID, length, DynamixelConstant.DXL_READ_DATA, address,
        nbRegisters, checksum };
    return data;
  }

}
