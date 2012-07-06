package rlpark.plugin.dynamixel.robot;

import rlpark.plugin.robot.internal.disco.drops.Drop;
import rlpark.plugin.robot.internal.disco.drops.DropByteArray;
import rlpark.plugin.robot.internal.disco.drops.DropData;
import rlpark.plugin.robot.internal.disco.drops.DropShortUnsigned;

@SuppressWarnings("restriction")
public class DynamixelDescriptor {
  final public static String DynamixelMotorDrop = "DynamixelMotorDrop";

  public static Drop newMotorReadDrop(int nbMotors) {
    DropData[] descriptors = new DropData[5 * nbMotors];
    int position = 0;
    for (int i = 0; i < nbMotors; i++) {
      descriptors[position] = new DropByteArray("Unused", 5);
      descriptors[position + 1] = new DropShortUnsigned("Goal" + i);
      descriptors[position + 2] = new DropShortUnsigned("Torque" + i);
      descriptors[position + 3] = new DropShortUnsigned("Load" + i);
      descriptors[position + 4] = new DropByteArray("Unused", 1);
      position += 5;
    }
    return new Drop(DynamixelMotorDrop, descriptors);
  }
}
