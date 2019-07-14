package me.jfenn.alarmio.common.data

import me.jfenn.alarmio.common.data.interfaces.AlertData
import me.jfenn.alarmio.common.data.interfaces.TimedData

data class TimerData(
        val timerID: Int,
        override var startTime: Long?,
        override var duration: Long,
        override var isVibrate: Boolean,
        override var sound: SoundData?
): AlertData, TimedData