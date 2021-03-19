package me.jfenn.alarmio.viewmodels

import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import me.jfenn.alarmio.data.alert.AlarmData
import me.jfenn.alarmio.data.alert.AlertData
import me.jfenn.alarmio.data.alert.TimerData
import me.jfenn.alarmio.impl.AppDatabase

class HomeViewModel(
        db : AppDatabase
) : ViewModel() {

    private val timerDao = db.timerDao()
    private val alarmDao = db.alarmDao()

    private val _timers = MutableLiveData<List<TimerData>>()
    val timers : LiveData<List<TimerData>> get() = _timers

    private val _alarms = MutableLiveData<List<AlarmData>>()
    val alarms : LiveData<List<AlarmData>> get() = _alarms

    // combined list of all alerts
    val alerts : LiveData<List<AlertData>> = MediatorLiveData<List<AlertData>>().apply {
        addSource(timers) {
            value = ArrayList<AlertData>().apply {
                addAll(it)
                alarms.value?.let { addAll(it) }
            }
        }
        addSource(alarms) {
            value = ArrayList<AlertData>().apply {
                timers.value?.let { addAll(it) }
                addAll(it)
            }
        }
    }

    init {
        viewModelScope.launch {
            _timers.value = timerDao.getAllTimers()
            _alarms.value = alarmDao.getAllAlarms()
        }
    }

    fun createTimer(timer: TimerData) {
        // TODO: schedule timer

        viewModelScope.launch {
            timerDao.insert(timer)
            _timers.value = ArrayList<TimerData>().apply {
                _timers.value?.let { addAll(it) }
                add(timer)
            }
        }
    }

    fun createAlarm(alarm: AlarmData) {
        // TODO: schedule alarm

        viewModelScope.launch {
            alarmDao.insert(alarm)
            _alarms.value = ArrayList<AlarmData>().apply {
                _alarms.value?.let { addAll(it) }
                add(alarm)
            }
        }
    }

}