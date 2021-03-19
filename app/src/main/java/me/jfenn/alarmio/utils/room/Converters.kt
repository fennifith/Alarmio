package me.jfenn.alarmio.utils.room

import androidx.room.TypeConverter
import me.jfenn.alarmio.data.SoundData
import java.util.*

class Converters {

    @TypeConverter
    fun longToCalendar(ms: Long) : Calendar {
        return Calendar.getInstance().apply {
            timeInMillis = ms
        }
    }

    @TypeConverter
    fun calendarToLong(calendar: Calendar) : Long {
        return calendar.timeInMillis
    }

    @TypeConverter
    fun soundToString(sound: SoundData?) : String {
        return sound?.toString() ?: ""
    }

    @TypeConverter
    fun stringToSound(string: String) : SoundData? {
        return SoundData.fromString(string)
    }

    @TypeConverter
    fun booleanListToString(list: ArrayList<Boolean>) : String {
        return buildString {
            list.forEach { append(if (it) '*' else ' ') }
        }
    }

    @TypeConverter
    fun stringToBooleanList(string: String) : ArrayList<Boolean> {
        return ArrayList<Boolean>().apply {
            string.forEach { char -> add(char == '*') }
        }
    }

}