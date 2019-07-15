package me.jfenn.alarmio.common.data.interfaces

import me.jfenn.alarmio.common.data.SoundData
import java.util.*

interface AlertData {

    val id: Int
    var isVibrate: Boolean
    var sound: SoundData?

    fun getAlertId(): String = "${javaClass.name}::$id"

    fun getAlertTime(): Date?

}