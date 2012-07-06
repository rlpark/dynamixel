package rlpark.plugin.dynamixel.internal;


import rlpark.plugin.rltoys.math.GrayCode;
import rlpark.plugin.rltoys.math.ranges.Range;
import rlpark.plugin.robot.internal.disco.datatype.GrayCodeConverter;
import rlpark.plugin.robot.internal.disco.datatype.ScalarReader;
import rlpark.plugin.robot.internal.disco.drops.DropData;
import rlpark.plugin.robot.internal.sync.LiteByteBuffer;

@SuppressWarnings("restriction")
public class DropLoad extends DropData implements ScalarReader, GrayCodeConverter {
  private int value;

  public DropLoad(String label) {
    this(label, -1);
  }

  public DropLoad(String label, int index) {
    super(label, false, index);
  }

  @Override
  public DropData clone(String label, int index) {
    return new DropLoad(label, index);
  }

  @Override
  public int getInt(LiteByteBuffer buffer) {
    int value = (buffer.getShort(index) & 0xffff);
    boolean direction = false;
    if ((value & 0x0400) == 0)
      direction = true;
    value = value & 0x03FF;
    if (direction == true)
      value *= -1;
    return value;
  }

  @Override
  public void convert(LiteByteBuffer source, LiteByteBuffer target) {
    value = getInt(source);
    value = GrayCode.shortToGrayCode((short) value);
    putData(target);
  }

  @Override
  public void putData(LiteByteBuffer buffer) {
    buffer.putShort((short) value);
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  @Override
  public int size() {
    return 2;
  }

  @Override
  public Range range() {
    return new Range(-511, 511);
  }

  @Override
  public double getDouble(LiteByteBuffer buffer) {
    return getInt(buffer);
  }
}
