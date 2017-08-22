package james.alarmio.utils;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;

import james.alarmio.R;

public class ColorUtils {

    public static boolean isColorDark(@ColorInt int color) {
        return (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255 < 0.5;
    }

    @ColorInt
    public static int getPrimaryTextColor(Context context, @ColorInt int background) {
        return ContextCompat.getColor(context, isColorDark(background) ? R.color.textColorPrimaryNight : R.color.textColorPrimary);
    }

    @ColorInt
    public static int getSecondaryTextColor(Context context, @ColorInt int background) {
        return ContextCompat.getColor(context, isColorDark(background) ? R.color.textColorSecondaryNight : R.color.textColorSecondary);
    }

}
