package me.jfenn.alarmio.data.alert

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import me.jfenn.alarmio.data.SoundData
import me.jfenn.alarmio.utils.room.Converters
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

@Entity
@TypeConverters(Converters::class)
data class AlarmData(
        @PrimaryKey override val id: Int,
        override var name: String?,
        var time: Calendar,
        var isEnabled: Boolean,
        var repeat: ArrayList<Boolean> = ArrayList(days.map { false }),
        override var isVibrate: Boolean = false,
        override var sound: SoundData? = null
) : AlertData, Serializable {

    companion object {
        val days = intArrayOf(Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY)
    }

    val isRepeat: Boolean get() = repeat.contains(true)

    /**
     * Get the next time that the alarm should wring.
     *
     * @return              A Calendar object defining the next time that the alarm should ring at.
     * @see [java.util.Calendar Documentation](https://developer.android.com/reference/java/util/Calendar)
     */
    fun getNext(now: Calendar = Calendar.getInstance()): Calendar? {
        if (isEnabled) {
            val next = now.clone() as Calendar
            next.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY))
            next.set(Calendar.MINUTE, time.get(Calendar.MINUTE))
            next.set(Calendar.SECOND, 0)

            while (now.after(next))
                next.add(Calendar.DATE, 1)

            if (isRepeat) {
                while (repeat.getOrNull(next.get(Calendar.DAY_OF_WEEK)) != true)
                    next.add(Calendar.DATE, 1)
            }

            return next
        }

        return null
    }

    override fun getAlertTime(): Date? {
        return getNext()?.time
    }

}


