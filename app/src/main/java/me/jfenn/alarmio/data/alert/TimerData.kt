package me.jfenn.alarmio.data.alert

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import me.jfenn.alarmio.data.SoundData
import me.jfenn.alarmio.utils.room.Converters
import java.io.Serializable
import java.util.*

@Entity
@TypeConverters(Converters::class)
data class TimerData(
        @PrimaryKey override val id: Int,
        var startTime: Long? = null,
        var duration: Long = 0,
        override var isVibrate: Boolean = false,
        override var sound: SoundData? = null
) : AlertData, Serializable {

    override var name: String? = null

    /**
     * Decides if the Timer has been set or should be ignored.
     *
     * @return              True if the timer should go off at some time in the future.
     */
    fun isStarted(): Boolean {
        return startTime != null
    }

    /**
     * Get the remaining amount of milliseconds before the timer should go off. Returns
     * a negative number if the timer has not been set yet.
     *
     * @return              The amount of milliseconds before the timer should go off.
     */
    fun getRemainingMillis(): Long {
        return startTime?.let {
            (it + duration) - System.currentTimeMillis()
        }?.coerceAtLeast(0) ?: -1
    }

    override fun getAlertTime(): Date? {
        return startTime?.let { Date(it + duration) }
    }

}