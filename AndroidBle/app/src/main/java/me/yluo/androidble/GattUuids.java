package me.yluo.androidble;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class GattUuids {
    private static HashMap<String, String> attributes = new HashMap();
    private static final String GNENERAL_END = "0000-1000-8000-00805f9b34fb";
    //https://www.bluetooth.com/specifications/gatt/characteristics
    public static final String CUSTOM_SERVICE = "0000ffe0";
    public static final String SERIAL_PORT = "0000ffe1";

    static {
        attributes.put("00001800", "通用接入规范");//Generic Access Profile
        attributes.put("00001801", "通用属性规范");//Generic Attribute Profile
        attributes.put("0000180d", "Heart Rate Service");
        attributes.put("0000180a", "Device Information Service");

        attributes.put("00002800","GATT Primary Service Declaration");
        attributes.put("00002801","GATT Secondary Service Declaration");
        attributes.put("00002802","GATT Include Declaration");
        attributes.put("00002803","GATT Characteristic Declaration");

        attributes.put("00002a00", "设备名称");//Device Name
        attributes.put("00002a01", "外貌特征");//Appearance
        attributes.put("00002a02", "外围设备隐蔽标志");//Peripheral Privacy Flag
        attributes.put("00002a03", "连接地址");//Peconnection Address
        attributes.put("00002a04", "外围设备首选连接参数");//Peripheral Preferred Connection Parameters
        attributes.put("00002a05", "服务改变");//Service Changed
        attributes.put("00002a29", "Manufacturer Name String");
        attributes.put("00002a37", "Heart Rate Measurement");

        attributes.put(CUSTOM_SERVICE,"自定义服务");
        attributes.put(SERIAL_PORT,"串口(TX RX)");
    }

    public static String lookup(String uuid, String defaultName) {
        int pos = uuid.indexOf("-");
        if (uuid.substring(pos + 1).equals(GNENERAL_END)) {
            String name = attributes.get(uuid.substring(0, pos));
            if (name != null) return name;
        }
        return defaultName;
    }
}
