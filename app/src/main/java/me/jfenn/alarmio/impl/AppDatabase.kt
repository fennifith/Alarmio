package me.jfenn.alarmio.impl

import androidx.room.Database
import androidx.room.RoomDatabase
import me.jfenn.alarmio.data.alert.AlarmData
import me.jfenn.alarmio.data.alert.TimerData
import me.jfenn.alarmio.interfaces.dao.AlarmDao
import me.jfenn.alarmio.interfaces.dao.TimerDao

@Database(entities = [AlarmData::class, TimerData::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alarmDao() : AlarmDao
    abstract fun timerDao() : TimerDao
}