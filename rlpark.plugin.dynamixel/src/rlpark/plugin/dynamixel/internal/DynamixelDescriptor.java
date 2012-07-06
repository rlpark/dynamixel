package rlpark.plugin.dynamixel.internal;

import rlpark.plugin.dynamixel.data.DynamixelLabels;
import rlpark.plugin.robot.internal.disco.drops.Drop;
import rlpark.plugin.robot.internal.disco.drops.DropByteArray;
import rlpark.plugin.robot.internal.disco.drops.DropByteUnsigned;
import rlpark.plugin.robot.internal.disco.drops.DropData;
import rlpark.plugin.robot.internal.disco.drops.DropShortUnsigned;

@SuppressWarnings("restriction")
public class DynamixelDescriptor {
  final public static String DynamixelMotorDrop = "DynamixelMotorDrop";

  public static Drop newMotorReadDrop(int nbMotors) {
    DropData[] descriptors = new DropData[7 * nbMotors];
    int position = 0;
    for (int i = 0; i < nbMotors; i++) {
      descriptors[position] = new DropByteArray("Unused", 5);
      descriptors[position + 1] = new DropShortUnsigned(DynamixelLabels.Goal + i);
      descriptors[position + 2] = new DropSpeed(DynamixelLabels.Speed + i);
      descriptors[position + 3] = new DropLoad(DynamixelLabels.Load + i);
      descriptors[position + 4] = new DropByteUnsigned(DynamixelLabels.Voltage + i);
      descriptors[position + 5] = new DropByteUnsigned(DynamixelLabels.Temperature + i);
      descriptors[position + 6] = new DropByteArray("Unused", 1);
      position += 7;
    }
    return new Drop(DynamixelMotorDrop, descriptors);
  }
}
