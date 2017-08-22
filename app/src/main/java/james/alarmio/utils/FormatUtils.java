package james.alarmio.utils;

import android.content.Context;
import android.text.format.DateFormat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FormatUtils {

    private static final String FORMAT_12H = "h:mm:ss";
    private static final String FORMAT_24H = "HH:mm:ss";
    private static final String FORMAT_12H_SHORT = "h:mm";
    private static final String FORMAT_24H_SHORT = "HH:mm";

    public static SimpleDateFormat getFormat(Context context) {
        return new SimpleDateFormat(DateFormat.is24HourFormat(context) ? FORMAT_24H : FORMAT_12H, Locale.getDefault());
    }

    public static SimpleDateFormat getShortFormat(Context context) {
        return new SimpleDateFormat(DateFormat.is24HourFormat(context) ? FORMAT_24H_SHORT : FORMAT_12H_SHORT, Locale.getDefault());
    }

    public static String format(Context context, Date time) {
        return getFormat(context).format(time);
    }

    public static String formatShort(Context context, Date time) {
        return getShortFormat(context).format(time);
    }

}
