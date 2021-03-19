package me.jfenn.alarmio.interfaces.dao

import androidx.room.*
import me.jfenn.alarmio.data.alert.AlarmData
import me.jfenn.alarmio.data.alert.TimerData

@Dao
interface TimerDao : BaseDao<TimerData> {

    @Query("SELECT * from timerdata")
    suspend fun getAllTimers() : List<TimerData>

}