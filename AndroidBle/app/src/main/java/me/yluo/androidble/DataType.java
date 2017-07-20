package me.yluo.androidble;

public enum DataType {
    //自定义 Arduino端口数据类型
    SET_TIME(100),
    SHOW_TIME(101),
    SET_RGB(102),
    SHOW_RGB(103),
    SHOW_SENSOR(104),
    REBOOT(999),
    CUSTOM(120),

    RESULT_SUCCESS(0xff),
    RESULT_ERROR(0x55);

    private int value;

    DataType(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
