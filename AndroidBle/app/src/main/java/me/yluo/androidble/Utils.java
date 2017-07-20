package me.yluo.androidble;


import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.util.TypedValue;

public class Utils {
    /**
     * dp 2 px
     */
    public static int dp2px(Context context, int dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.getResources().getDisplayMetrics());
    }

    public static String getProperty(int p) {
        String property = "";
        if ((p | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
            property += " R";
        }

        if ((p | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
            property += " W";
        }

        if ((p | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) {
            property += " WN";
        }

        if ((p | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            property += " N";
        }

        return property;
    }
}
