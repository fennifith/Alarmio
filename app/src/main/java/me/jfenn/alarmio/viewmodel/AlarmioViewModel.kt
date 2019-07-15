package me.jfenn.alarmio.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import me.jfenn.alarmio.common.data.AlarmData
import me.jfenn.alarmio.common.data.TimerData
import me.jfenn.alarmio.common.data.interfaces.AlertData
import me.jfenn.alarmio.common.interfaces.AlarmioRepository
import java.util.*

class AlarmioViewModel(
        application: Application,
        val repo: AlarmioRepository
): AndroidViewModel(application) {

    private val alarms: MutableLiveData<List<AlarmData>> by lazy {
        MutableLiveData<List<AlarmData>>().also {
            repo.getAlarms()
        }
    }

    private val timers: MutableLiveData<List<TimerData>> by lazy {
        MutableLiveData<List<TimerData>>().also {
            repo.getTimers()
        }
    }

    fun getAlarms(): LiveData<List<AlarmData>> = alarms

    fun getTimers(): LiveData<List<TimerData>> = timers

    fun getAlarm(id: Int): AlarmData? {
        return alarms.value?.firstOrNull { it.id == id }
    }

    fun getTimer(id: Int): TimerData? {
        return timers.value?.firstOrNull { it.id == id }
    }

    fun getAlert(id: String): AlertData? {
        return id.split("::").let {
            when (it[0]) {
                AlarmData::class.java.name -> getAlarm(it[1].toInt())
                TimerData::class.java.name -> getTimer(it[1].toInt())
                else -> null
            }
        }
    }

    fun newAlarm(): AlarmData? {
        val alarms = alarms.value as? MutableList ?: return null

        val alarm = AlarmData(
                id = alarms.size + (timers.value?.size ?: 0),
                time = Calendar.getInstance(),
                isEnabled = false
        )

        this.alarms.value = alarms.apply {
            add(alarm)
        }

        return alarm
    }

    fun newTimer(): TimerData? {
        val timers = timers.value as? MutableList ?: return null

        val timer = TimerData(
                id = timers.size + (alarms.value?.size ?: 0)
        )

        this.timers.value = timers.apply {
            add(timer)
        }

        return timer
    }

    fun save() {
        alarms.value?.let { repo.setAlarms(it) }
        timers.value?.let { repo.setTimers(it) }
    }

}