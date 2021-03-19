package me.jfenn.alarmio.interfaces.dao

import androidx.room.*
import me.jfenn.alarmio.data.alert.AlarmData

@Dao
interface AlarmDao : BaseDao<AlarmData> {

    @Query("SELECT * from alarmdata")
    suspend fun getAllAlarms() : List<AlarmData>

}