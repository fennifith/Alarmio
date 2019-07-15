package me.jfenn.alarmio.impl

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import me.jfenn.alarmio.common.data.AlarmData
import me.jfenn.alarmio.common.data.SoundData
import me.jfenn.alarmio.common.data.TimerData
import me.jfenn.alarmio.common.interfaces.AlarmioRepository
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class AlarmioRepositoryImpl(context: Context): AlarmioRepository {

    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    override fun getAlarm(id: Int): AlarmData {
        val prefix = "alarms::$id"

        return AlarmData(
                id = id,
                time = Calendar.getInstance().apply {
                    timeInMillis = prefs.getLong("$prefix-time", 0)
                },
                isEnabled = prefs.getBoolean("$prefix-enabled", true),
                repeat = HashMap<Int, Boolean>().apply {
                    AlarmData.days.forEach { day ->
                        put(day, prefs.getBoolean("$prefix-repeat::$day", false))
                    }
                },
                isVibrate = prefs.getBoolean("$prefix-vibrate", false),
                sound = SoundData.fromString(prefs.getString("$prefix-sound", "") ?: "")
        )
    }

    override fun getTimer(id: Int): TimerData {
        val prefix = "timers::$id"

        return TimerData(
                id = id,
                startTime = prefs.getLong("$prefix-startTime", -1),
                duration = prefs.getLong("$prefix-duration", 0),
                isVibrate = prefs.getBoolean("$prefix-vibrate", false),
                sound = SoundData.fromString(prefs.getString("$prefix-sound", "") ?: "")
        )
    }

    override fun getAlarms(): List<AlarmData> {
        val alarms: ArrayList<AlarmData> = ArrayList()
        prefs.getString("alarms", "")?.split(":")?.forEach {
            if (!it.isEmpty()) try {
                alarms.add(getAlarm(it.toInt()))
            } catch (e: NumberFormatException) {}
        }

        return alarms
    }

    override fun getTimers(): List<TimerData> {
        val timers: ArrayList<TimerData> = ArrayList()
        prefs.getString("timers", "")?.split(":")?.forEach {
            if (!it.isEmpty()) try {
                timers.add(getTimer(it.toInt()))
            } catch (e: NumberFormatException) {}
        }

        return timers
    }

    override fun setAlarm(alarm: AlarmData) {
        val prefix = "alarms::${alarm.id}"

        prefs.edit()
                .putLong("$prefix-time", alarm.time.timeInMillis)
                .putBoolean("$prefix-enabled", alarm.isEnabled)
                .apply {
                    for ((day, isEnabled) in alarm.repeat)
                        putBoolean("$prefix-repeat::$day", isEnabled)
                }
                .putBoolean("$prefix-vibrate", alarm.isVibrate)
                .putString("$prefix-sound", alarm.sound?.toString())
                .apply()
    }

    override fun setTimer(timer: TimerData) {
        val prefix = "timers::${timer.id}"

        prefs.edit()
                .apply {
                    timer.startTime?.let { putLong("$prefix-startTime", it) }
                }
                .putLong("$prefix-duration", timer.duration)
                .putBoolean("$prefix-vibrate", timer.isVibrate)
                .putString("$prefix-sound", timer.sound.toString())
                .apply()
    }

}