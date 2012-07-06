package rlpark.plugin.dynamixel.internal;

public class DynamixelConstant {
  public final static byte DXL_MODEL_NUMBER_L = 0;
  public final static byte DXL_MODEL_NUMBER_H = 1;
  public final static byte DXL_VERSION = 2;
  public final static byte DXL_ID = 3;
  public final static byte DXL_BAUD_RATE = 4;
  public final static byte DXL_RETURN_DELAY_TIME = 5;
  public final static byte DXL_CW_ANGLE_LIMIT_L = 6;
  public final static byte DXL_CW_ANGLE_LIMIT_H = 7;
  public final static byte DXL_CCW_ANGLE_LIMIT_L = 8;
  public final static byte DXL_CCW_ANGLE_LIMIT_H = 9;
  public final static byte DXL_DRIVE_MODE = 10;
  public final static byte DXL_LIMIT_TEMPERATURE = 11;
  public final static byte DXL_DOWN_LIMIT_VOLTAGE = 12;
  public final static byte DXL_UP_LIMIT_VOLTAGE = 13;
  public final static byte DXL_MAX_TORQUE_L = 14;
  public final static byte DXL_MAX_TORQUE_H = 15;
  public final static byte DXL_RETURN_LEVEL = 16;
  public final static byte DXL_ALARM_LED = 17;
  public final static byte DXL_ALARM_SHUTDOWN = 18;
  public final static byte DXL_OPERATING_MODE = 19;
  public final static byte DXL_DOWN_CALIBRATION_L = 20;
  public final static byte DXL_DOWN_CALIBRATION_H = 21;
  public final static byte DXL_UP_CALIBRATION_L = 22;
  public final static byte DXL_UP_CALIBRATION_H = 23;
  public final static byte DXL_TORQUE_ENABLE = 24;
  public final static byte DXL_LED = 25;
  public final static byte DXL_CW_COMPLIANCE_MARGIN = 26;
  public final static byte DXL_CCW_COMPLIANCE_MARGIN = 27;
  public final static byte DXL_CW_COMPLIANCE_SLOPE = 28;
  public final static byte DXL_CCW_COMPLIANCE_SLOPE = 29;
  public final static byte DXL_GOAL_POSITION_L = 30;
  public final static byte DXL_GOAL_POSITION_H = 31;
  public final static byte DXL_GOAL_SPEED_L = 32;
  public final static byte DXL_GOAL_SPEED_H = 33;
  public final static byte DXL_TORQUE_LIMIT_L = 34;
  public final static byte DXL_TORQUE_LIMIT_H = 35;
  public final static byte DXL_PRESENT_POSITION_L = 36;
  public final static byte DXL_PRESENT_POSITION_H = 37;
  public final static byte DXL_PRESENT_SPEED_L = 38;
  public final static byte DXL_PRESENT_SPEED_H = 39;
  public final static byte DXL_PRESENT_LOAD_L = 40;
  public final static byte DXL_PRESENT_LOAD_H = 41;
  public final static byte DXL_PRESENT_VOLTAGE = 42;
  public final static byte DXL_PRESENT_TEMPERATURE = 43;
  public final static byte DXL_REGISTERED_INSTRUCTION = 44;
  public final static byte DXL_PAUSE_TIME = 45;
  public final static byte DXL_MOVING = 46;
  public final static byte DXL_LOCK = 47;
  public final static byte DXL_PUNCH_L = 48;
  public final static byte DXL_PUNCH_H = 49;
  public final static byte DXL_SENSED_CURRENT_L = 56;
  public final static byte DXL_SENSED_CURRENT_H = 57;

  public final static byte DXL_RETURN_NONE = 0;
  public final static byte DXL_RETURN_READ = 1;
  public final static byte DXL_RETURN_ALL = 2;

  public final static byte DXL_PING = 1;
  public final static byte DXL_READ_DATA = 2;
  public final static byte DXL_WRITE_DATA = 3;
  public final static byte DXL_REG_WRITE = 4;
  public final static byte DXL_ACTION = 5;
  public final static byte DXL_RESET = 6;
  public final static byte DXL_SYNC_WRITE = (byte) 131;

  public final static byte DXL_BROADCAST = (byte) 254;

  public final static byte DXL_INSTRUCTION_ERROR = 64;
  public final static byte DXL_OVERLOAD_ERROR = 32;
  public final static byte DXL_CHECKSUM_ERROR = 16;
  public final static byte DXL_RANGE_ERROR = 8;
  public final static byte DXL_OVERHEATING_ERROR = 4;
  public final static byte DXL_ANGLE_LIMIT_ERROR = 2;
  public final static byte DXL_INPUT_VOLTAGE_ERROR = 1;
  public final static byte DXL_NO_ERROR = 0;

  public final static byte DXL_MIN_COMPLIANCE_MARGIN = 0;
  public final static byte DXL_MAX_COMPLIANCE_MARGIN = (byte) 255;

  public final static byte DXL_MIN_COMPLIANCE_SLOPE = 0;
  public final static byte DXL_MAX_COMPLIANCE_SLOPE = (byte) 254;

  public final static byte DXL_MIN_PUNCH = 0;
  public final static byte DXL_MAX_PUNCH = (byte) 255;

  final static int DXL_MAX_SPEED_TICK = 1023; // maximum speed in encoder units
  final static int DXL_MAX_TORQUE_TICK = 1023; // maximum torque in encoder
                                               // units

  final static float KGCM_TO_NM = (float) 0.0980665; // 1 kg-cm is that many N-m
  final static float RPM_TO_RADSEC = (float) 0.104719755; // 1 RPM is that many
                                                          // rad/sec
}
