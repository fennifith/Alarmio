package james.alarmio.utils;

import android.content.Context;
import android.content.res.Resources;

public class ConversionUtils {

    public static int getStatusBarHeight(Context context) {
        int resId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) return context.getResources().getDimensionPixelSize(resId);
        else return 0;
    }

    public static int dpToPx(float dp) {
        return (int) (Resources.getSystem().getDisplayMetrics().density * dp);
    }

    public static float pxToDp(int pixels) {
        return pixels / Resources.getSystem().getDisplayMetrics().density;
    }

    public static int spToPx(float sp) {
        return (int) (Resources.getSystem().getDisplayMetrics().scaledDensity * sp);
    }

    public static float pxToSp(int pixels) {
        return pixels / Resources.getSystem().getDisplayMetrics().scaledDensity;
    }

}
