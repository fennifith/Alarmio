package me.jfenn.alarmio.common.data

import me.jfenn.alarmio.common.data.interfaces.AlertData
import me.jfenn.alarmio.common.data.interfaces.TimedData
import java.io.Serializable
import java.util.*

data class TimerData(
        override val id: Int,
        override var startTime: Long? = null,
        override var duration: Long = 0,
        override var isVibrate: Boolean = false,
        override var sound: SoundData? = null
): AlertData, TimedData, Serializable {

    override fun getAlertTime(): Date? {
        return startTime?.let { Date(it + duration) }
    }

}