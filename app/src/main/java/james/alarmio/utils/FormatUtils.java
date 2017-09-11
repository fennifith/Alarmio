package james.alarmio.utils;

import android.content.Context;
import android.text.format.DateFormat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class FormatUtils {

    private static final String FORMAT_12H = "h:mm:ss";
    private static final String FORMAT_24H = "HH:mm:ss";
    private static final String FORMAT_12H_SHORT = "h:mm a";
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

    public static String formatMillis(long millis) {
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1);
        long micros = TimeUnit.MILLISECONDS.toMicros(millis) % TimeUnit.SECONDS.toMicros(1) / 10000;

        if (hours > 0)
            return String.format(Locale.getDefault(), "%dh %02dm %02ds %02d", hours, minutes, seconds, micros);
        else if (minutes > 0)
            return String.format(Locale.getDefault(), "%dm %02ds %02d", minutes, seconds, micros);
        else return String.format(Locale.getDefault(), "%ds %02d", seconds, micros);
    }
}
