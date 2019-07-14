package me.jfenn.alarmio.common.data

import me.jfenn.alarmio.common.data.interfaces.AlertData
import java.util.*

data class AlarmData(
        val id: Int,
        var time: Calendar,
        var isEnabled: Boolean,
        var repeat: MutableMap<Int, Boolean>,
        override var isVibrate: Boolean,
        override var sound: SoundData?
): AlertData {

    val days = intArrayOf(Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY)

    fun isRepeat(): Boolean {
        days.forEach {
            if (repeat[it] == true) return true
        }

        return false
    }

    /**
     * Get the next time that the alarm should wring.
     *
     * @return              A Calendar object defining the next time that the alarm should ring at.
     * @see [java.util.Calendar Documentation](https://developer.android.com/reference/java/util/Calendar)
     */
    fun getNext(): Calendar? {
        if (isEnabled) {
            val now = Calendar.getInstance()
            val next = Calendar.getInstance()
            next.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY))
            next.set(Calendar.MINUTE, time.get(Calendar.MINUTE))
            next.set(Calendar.SECOND, 0)

            while (now.after(next))
                next.add(Calendar.DATE, 1)

            if (isRepeat()) {
                while (repeat[next.get(Calendar.DAY_OF_WEEK)] != true)
                    next.add(Calendar.DATE, 1)
            }

            return next
        }

        return null
    }

}
