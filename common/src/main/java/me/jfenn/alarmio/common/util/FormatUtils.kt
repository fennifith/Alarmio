package me.jfenn.alarmio.common.util

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Formats the provided time into the provided format.
 *
 * @param time          The time to be formatted.
 * @param format        The format to format the time into.
 * @return              The formatted time string.
 */
fun Date.format(format: String): String {
    return SimpleDateFormat(format, Locale.getDefault()).format(this)
}

/**
 * Formats the provided time into a string using [format](#format).
 *
 * @param is24Hour      Whether the time should be 24-hour.
 * @return              A formatted hh:mm:ss string.
 */
fun Date.formatString(is24Hour: Boolean = false): String {
    return format(if (is24Hour) FormatUtils.TIME_FORMAT_24H else FormatUtils.TIME_FORMAT_12H)
}

/**
 * Formats the provided time into a string using [format](#format).
 *
 * @param is24Hour      Whether the time should be 24-hour.
 * @return              A formatted hh:mm string.
 */
fun Date.formatShortString(is24Hour: Boolean = false): String {
    return format(if (is24Hour) FormatUtils.TIME_FORMAT_24H_SHORT else FormatUtils.TIME_FORMAT_12H_SHORT)
}

/**
 * Formats a set of arguments into a string.
 *
 * @param locale        The current locale.
 * @param args          The arguments to format.
 * @return              A formatted string.
 */
fun String.format(locale: Locale = Locale.getDefault(), vararg args: Any?): String {
    return String.format(locale, this, args)
}

val Calendar.timeInMinutes: Long
    get() {
        return TimeUnit.MILLISECONDS.toMinutes(timeInMillis)
    }

object FormatUtils {
    const val TIME_FORMAT_12H = "h:mm:ss"
    const val TIME_FORMAT_24H = "HH:mm:ss"
    const val TIME_FORMAT_12H_SHORT = "h:mm a"
    const val TIME_FORMAT_24H_SHORT = "HH:mm"
    const val DATE_FORMAT = "MMMM d yyyy"

    /**
     * Formats a duration of milliseconds into a "0h 00m 00s 00" string.
     *
     * @param millis        The millisecond duration to be formatted.
     * @return              The formatted time string.
     */
    fun formatMillis(
            millis: Long,
            abbr_hour: String = "h",
            abbr_minute: String = "m",
            abbr_second: String = "s"
    ): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1)
        val micros = TimeUnit.MILLISECONDS.toMicros(millis) % TimeUnit.SECONDS.toMicros(1) / 10000

        return when {
            hours > 0 -> "%d$abbr_hour %02d$abbr_minute %02d$abbr_second %02d".format(hours, minutes, seconds, micros)
            minutes > 0 -> "%d$abbr_minute %02d$abbr_second %02d".format(minutes, seconds, micros)
            else -> "%d$abbr_second %02d".format(seconds, micros)
        }
    }

    /**
     * Formats a duration of minutes into a meaningful string to be used in
     * idk maybe a sentence or something. An input of 60 becomes "1 hour", 59
     * becomes "59 minutes", and so on.
     *
     * @param mins          The duration of minutes to format.
     * @return              The formatted time string.
     */
    fun formatMinutes(
            mins: Int,
            word_days: String = "days",
            word_day: String = "day",
            word_hours: String = "hours",
            word_hour: String = "hour",
            word_minutes: String = "minutes",
            word_minute: String = "minute",
            word_join: String = "and"
    ): String {
        val days = TimeUnit.MINUTES.toDays(mins.toLong())
        val hours = TimeUnit.MINUTES.toHours(mins.toLong()) % TimeUnit.DAYS.toHours(1)
        val minutes = mins % TimeUnit.HOURS.toMinutes(1).toInt()

        return when {
            days > 0 -> "$days " + (if (days > 1) word_days else word_day) + ", $hours " + (if (hours > 1) word_hours else word_hour) + if (minutes > 0) ", $word_join $minutes " + (if (minutes > 1) word_minutes else word_minute) else ""
            hours > 0 -> "$hours " + (if (hours > 1) word_hours else word_hour) + if (minutes > 0) " $word_join $minutes " + (if (minutes > 1) word_minutes else word_minute) else ""
            else -> "$minutes " + (if (minutes > 1) word_minutes else word_minute)
        }
    }
}
