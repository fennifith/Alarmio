package me.jfenn.alarmio.common.data

import me.jfenn.alarmio.common.data.interfaces.AlertData
import me.jfenn.alarmio.common.data.interfaces.TimedData
import java.util.*

data class TimerData(
        override val id: Int,
        override var startTime: Long?,
        override var duration: Long,
        override var isVibrate: Boolean,
        override var sound: SoundData?
): AlertData, TimedData {

    override fun getAlertTime(): Date? {
        return startTime?.let { Date(it + duration) }
    }

}