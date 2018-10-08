package james.alarmio.utils;

import android.content.Context;
import android.content.res.Resources;

public class ConversionUtils {

    /**
     * Returns the height of the device's status bar, in px.
     *
     * @param context       An active context instance.
     * @return              The height of the status bar, in pixels.
     */
    public static int getStatusBarHeight(Context context) {
        int resId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) return context.getResources().getDimensionPixelSize(resId);
        else return 0;
    }

    /**
     * Converts dp units to pixels.
     *
     * @param dp            A distance measurement, in dp.
     * @return              The value of the provided dp units, in pixels.
     */
    public static int dpToPx(float dp) {
        return (int) (Resources.getSystem().getDisplayMetrics().density * dp);
    }

    /**
     * Converts pixels to dp.
     *
     * @param pixels        A distance measurement, in pixels.
     * @return              The value of the provided pixel units, in dp.
     */
    public static float pxToDp(int pixels) {
        return pixels / Resources.getSystem().getDisplayMetrics().density;
    }

    /**
     * Converts sp to pixels.
     *
     * @param sp            A distance measurement, in sp.
     * @return              The value of the provided sp units, in pixels.
     */
    public static int spToPx(float sp) {
        return (int) (Resources.getSystem().getDisplayMetrics().scaledDensity * sp);
    }

    /**
     * Converts pixels to sp.
     *
     * @param pixels        A distance measurement, in pixels.
     * @return              The value of the provided pixel units, in sp.
     */
    public static float pxToSp(int pixels) {
        return pixels / Resources.getSystem().getDisplayMetrics().scaledDensity;
    }

}
