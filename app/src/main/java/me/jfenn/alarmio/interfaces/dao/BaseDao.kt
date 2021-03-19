package me.jfenn.alarmio.interfaces.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

interface BaseDao<T> {

    @Insert
    suspend fun insert(vararg obj : T)

    @Update
    suspend fun update(vararg obj : T)

    @Delete
    suspend fun delete(vararg obj : T)

}