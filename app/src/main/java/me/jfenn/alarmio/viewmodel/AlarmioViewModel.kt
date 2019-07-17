package me.jfenn.alarmio.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import me.jfenn.alarmio.common.data.AlarmData
import me.jfenn.alarmio.common.data.TimerData
import me.jfenn.alarmio.common.data.interfaces.AlertData
import me.jfenn.alarmio.common.interfaces.AlarmioRepository

class AlarmioViewModel(
        application: Application,
        val repo: AlarmioRepository
): AndroidViewModel(application) {

    private val alarms: MutableLiveData<MutableList<MutableLiveData<AlarmData>>> by lazy {
        MutableLiveData<MutableList<MutableLiveData<AlarmData>>>().apply {
            value = ArrayList(repo.getAlarms().map { observeAlarm(it) })
            observeForever {
                repo.setAlarms(getAlarms())
            }
        }
    }

    private val timers: MutableLiveData<MutableList<MutableLiveData<TimerData>>> by lazy {
        MutableLiveData<MutableList<MutableLiveData<TimerData>>>().apply {
            value = ArrayList(repo.getTimers().map { observeTimer(it) })
            observeForever {
                repo.setTimers(getTimers())
            }
        }
    }

    private fun observeAlarm(alarm: AlarmData): MutableLiveData<AlarmData> {
        return MutableLiveData<AlarmData>().apply {
            value = alarm
            observeForever(repo::setAlarm)
        }
    }

    private fun observeTimer(timer: TimerData): MutableLiveData<TimerData> {
        return MutableLiveData<TimerData>().apply {
            value = timer
            observeForever(repo::setTimer)
        }
    }

    fun getAlarms(): List<AlarmData> {
        val list: ArrayList<AlarmData> = ArrayList()
        alarms.value?.forEach { alarm ->
            alarm.value?.let { list.add(it) }
        }

        return list
    }

    fun getTimers(): List<TimerData> {
        val list: ArrayList<TimerData> = ArrayList()
        timers.value?.forEach { timer ->
            timer.value?.let { list.add(it) }
        }

        return list
    }

    fun getAlarm(id: Int): MutableLiveData<AlarmData>? {
        return alarms.value?.firstOrNull { it.value?.id == id }
    }

    fun getTimer(id: Int): MutableLiveData<TimerData>? {
        return timers.value?.firstOrNull { it.value?.id == id }
    }

    fun setAlarm(alarm: AlarmData) {
        getAlarm(alarm.id)?.apply {
            value = alarm
        } ?: observeAlarm(alarm).also {
            alarms.value = alarms.value?.apply { add(it) }
        }
    }

    fun setTimer(timer: TimerData) {
        getTimer(timer.id)?.apply {
            value = timer
        } ?: observeTimer(timer).also {
            timers.value = timers.value?.apply { add(it) }
        }
    }

    fun getAlert(id: String): AlertData? {
        return id.split("::").let {
            when (it[0]) {
                AlarmData::class.java.name -> getAlarm(it[1].toInt())?.value
                TimerData::class.java.name -> getTimer(it[1].toInt())?.value
                else -> null
            }
        }
    }

}