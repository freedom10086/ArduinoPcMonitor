package me.yluo.androidble;


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
}
