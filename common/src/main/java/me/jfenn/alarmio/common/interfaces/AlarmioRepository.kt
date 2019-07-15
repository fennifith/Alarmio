package me.jfenn.alarmio.common.interfaces

import me.jfenn.alarmio.common.data.AlarmData
import me.jfenn.alarmio.common.data.TimerData

interface AlarmioRepository {

    fun getAlarm(id: Int): AlarmData

    fun getTimer(id: Int): TimerData

    fun getAlarms(): List<AlarmData>

    fun getTimers(): List<TimerData>

    fun setAlarm(alarm: AlarmData)

    fun setTimer(timer: TimerData)

    fun setAlarms(alarms: List<AlarmData>)

    fun setTimers(timers: List<TimerData>)

}