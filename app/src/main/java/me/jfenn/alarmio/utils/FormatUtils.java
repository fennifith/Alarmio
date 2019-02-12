package me.jfenn.alarmio.utils;

import android.content.Context;
import android.text.format.DateFormat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import me.jfenn.alarmio.R;

public class FormatUtils {

    public static final String FORMAT_12H = "h:mm:ss";
    public static final String FORMAT_24H = "HH:mm:ss";
    public static final String FORMAT_12H_SHORT = "h:mm a";
    public static final String FORMAT_24H_SHORT = "HH:mm";
    public static final String FORMAT_DATE = "MMMM d yyyy";

    /**
     * Get the proper hh:mm:ss time format to use, dependent on whether
     * 24-hour time is enabled in the system settings.
     *
     * @param context       An active context instance.
     * @return              A string to format hh:mm:ss time.
     */
    public static String getFormat(Context context) {
        return DateFormat.is24HourFormat(context) ? FORMAT_24H : FORMAT_12H;
    }

    /**
     * A shorter version of [getFormat](#getformat) with the AM/PM indicator
     * in the 12-hour version.
     *
     * @param context       An active context instance.
     * @return              A string to format hh:mm time.
     */
    public static String getShortFormat(Context context) {
        return DateFormat.is24HourFormat(context) ? FORMAT_24H_SHORT : FORMAT_12H_SHORT;
    }

    /**
     * Formats the provided time into a string using [getFormat](#getformat).
     *
     * @param context       An active context instance.
     * @param time          The time to be formatted.
     * @return              A formatted hh:mm:ss string.
     */
    public static String format(Context context, Date time) {
        return format(time, getFormat(context));
    }

    /**
     * Formats the provided time into a string using [getShortFormat](#getshortformat).
     *
     * @param context       An active context instance.
     * @param time          The time to be formatted.
     * @return              A formatted hh:mm string.
     */
    public static String formatShort(Context context, Date time) {
        return format(time, getShortFormat(context));
    }

    /**
     * Formats the provided time into the provided format.
     *
     * @param time          The time to be formatted.
     * @param format        The format to format the time into.
     * @return              The formatted time string.
     */
    public static String format(Date time, String format) {
        return new SimpleDateFormat(format, Locale.getDefault()).format(time);
    }

    /**
     * Formats a duration of milliseconds into a "0h 00m 00s 00" string.
     *
     * @param millis        The millisecond duration to be formatted.
     * @return              The formatted time string.
     */
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

    /**
     * Formats a duration of minutes into a meaningful string to be used in
     * idk maybe a sentence or something. An input of 60 becomes "1 hour", 59
     * becomes "59 minutes", and so on.
     *
     * @param context       An active context instance.
     * @param minutes       The duration of minutes to format.
     * @return              The formatted time string.
     */
    public static String formatUnit(Context context, int minutes) {
        long days = TimeUnit.MINUTES.toDays(minutes);
        long hours = TimeUnit.MINUTES.toHours(minutes) % TimeUnit.DAYS.toHours(1);
        minutes %= TimeUnit.HOURS.toMinutes(1);
        if (days > 0)
            return String.format(Locale.getDefault(),  "%d " + context.getString(days > 1 ? R.string.word_days : R.string.word_day) + ", %d " + context.getString(hours > 1 ? R.string.word_hours : R.string.word_hour) + (minutes > 0 ? ", " + context.getString(R.string.word_join) + " %d " + context.getString(minutes > 1 ? R.string.word_minutes : R.string.word_minute) : ""), days, hours, minutes);
        else if (hours > 0)
            return String.format(Locale.getDefault(), "%d " + context.getString(hours > 1 ? R.string.word_hours : R.string.word_hour) + (minutes > 0 ? " " + context.getString(R.string.word_join) + " %d " + context.getString(minutes > 1 ? R.string.word_minutes : R.string.word_minute) : ""), hours, minutes);
        else
            return String.format(Locale.getDefault(), "%d " + context.getString(minutes > 1 ? R.string.word_minutes : R.string.word_minute), minutes);
    }
}
