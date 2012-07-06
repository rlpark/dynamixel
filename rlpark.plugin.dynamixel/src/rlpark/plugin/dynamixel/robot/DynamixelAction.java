package rlpark.plugin.dynamixel.robot;

import java.nio.ByteOrder;

import rlpark.plugin.dynamixel.internal.DynamixelConstant;
import rlpark.plugin.rltoys.envio.actions.Action;
import rlpark.plugin.robot.internal.sync.LiteByteBuffer;

@SuppressWarnings("restriction")
public class DynamixelAction implements Action {
  private final int[] positions;
  private final int[] speeds;
  private final int[] torques;

  public DynamixelAction(int[] positions, int[] speeds, int[] torques) {
    assert positions.length == speeds.length;
    assert positions.length == torques.length;
    this.positions = positions;
    this.speeds = speeds;
    this.torques = torques;
  }

  /*
   * 
   * 0xff 0xff 0xfe 0xb 0x83 0x1e 0x6 0x60 0x43 0x1 0x58 0x2 0x58 0x2 0xf7
   */
  public byte[] buildMessage() {
    byte length = (byte) (4 + positions.length * 2 * 3 + positions.length);

    byte[] header = new byte[] { (byte) 0xff, (byte) 0xff, DynamixelConstant.DXL_BROADCAST, length,
        DynamixelConstant.DXL_SYNC_WRITE, DynamixelConstant.DXL_GOAL_POSITION_L, 6 };

    byte checksum = (byte) (DynamixelConstant.DXL_BROADCAST + length + DynamixelConstant.DXL_SYNC_WRITE
        + DynamixelConstant.DXL_GOAL_POSITION_L + 6);

    LiteByteBuffer motorData = new LiteByteBuffer(length, ByteOrder.LITTLE_ENDIAN);
    for (int i = 0; i < positions.length; i++) {
      // motorData.putShort((short) positions[i]);
      motorData.putShort((short) positions[i]);
      motorData.putShort((short) speeds[i]);
      motorData.putShort((short) torques[i]);
    }

    for (byte b : motorData.array())
      checksum += b;

    checksum = (byte) (255 - (checksum % 256));
    LiteByteBuffer buffer = new LiteByteBuffer(header.length + length + 1, ByteOrder.LITTLE_ENDIAN);
    buffer.put(header);
    buffer.put(motorData.array());
    buffer.put(checksum);
    return buffer.array();
  }
}
