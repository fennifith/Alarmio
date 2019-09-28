package me.jfenn.alarmio.viewmodel

import android.app.Application
import androidx.arch.core.util.Function
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import me.jfenn.alarmio.common.data.AlarmData
import me.jfenn.alarmio.common.data.TimerData
import me.jfenn.alarmio.common.data.interfaces.AlertData
import me.jfenn.alarmio.common.interfaces.AlarmioRepository
import java.util.*
import kotlin.collections.ArrayList

class AlarmioViewModel(
        application: Application,
        val repo: AlarmioRepository
): AndroidViewModel(application) {

    private val alarmsData: MutableLiveData<MutableList<MutableLiveData<AlarmData>>> by lazy {
        MutableLiveData<MutableList<MutableLiveData<AlarmData>>>().apply {
            value = ArrayList(repo.getAlarms().map { observeAlarm(it) })
            observeForever {
                repo.setAlarms(getAlarms())
            }
        }
    }

    private val timersData: MutableLiveData<MutableList<MutableLiveData<TimerData>>> by lazy {
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
        alarmsData.value?.forEach { alarm ->
            alarm.value?.let { list.add(it) }
        }

        return list
    }

    fun getTimers(): List<TimerData> {
        val list: ArrayList<TimerData> = ArrayList()
        timersData.value?.forEach { timer ->
            timer.value?.let { list.add(it) }
        }

        return list
    }

    fun getAlarm(id: Int): MutableLiveData<AlarmData>? {
        return alarmsData.value?.firstOrNull { it.value?.id == id }
    }

    fun getTimer(id: Int): MutableLiveData<TimerData>? {
        return timersData.value?.firstOrNull { it.value?.id == id }
    }

    fun setAlarm(alarm: AlarmData) {
        getAlarm(alarm.id)?.apply {
            value = alarm
        } ?: observeAlarm(alarm).also {
            alarmsData.value = alarmsData.value?.apply { add(it) }
        }
    }

    fun setTimer(timer: TimerData) {
        getTimer(timer.id)?.apply {
            value = timer
        } ?: observeTimer(timer).also {
            timersData.value = timersData.value?.apply { add(it) }
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

    fun newAlarm(): MutableLiveData<AlarmData>? {
        val alarms = alarmsData.value ?: return null

        val alarm = observeAlarm(AlarmData(
                id = alarms.size + (timersData.value?.size ?: 0),
                time = Calendar.getInstance(),
                isEnabled = false
        ))

        this.alarmsData.value = alarms.apply {
            add(alarm)
        }

        return alarm
    }

    fun newTimer(): MutableLiveData<TimerData>? {
        val timers = timersData.value ?: return null

        val timer = observeTimer(TimerData(
                id = timers.size + (alarmsData.value?.size ?: 0)
        ))

        this.timersData.value = timers.apply {
            add(timer)
        }

        return timer
    }

}