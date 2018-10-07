package james.alarmio.utils;

import android.graphics.Color;

import androidx.annotation.ColorInt;

public class ColorUtils {

    /**
     * Determine if a color is dark or not, using some magic numbers.
     *
     * @see [this confusing wikipedia article](https://en.wikipedia.org/wiki/Luma_%28video%29)
     *
     * @param color         A color int to determine the luminance of.
     * @return              True if the color should be considered "light".
     */
    public static boolean isColorDark(@ColorInt int color) {
        return (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255 < 0.5;
    }

}
