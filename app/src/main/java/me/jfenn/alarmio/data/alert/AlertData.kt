package me.jfenn.alarmio.data.alert

import android.content.Context
import me.jfenn.alarmio.R
import me.jfenn.alarmio.data.SoundData
import java.util.*

interface AlertData {

    val id: Int
    var name: String?
    var isVibrate: Boolean
    var sound: SoundData?

    fun getAlertId(): String = "${javaClass.name}::$id"

    fun getAlertTime(): Date?

    fun getAlertName(context: Context?) = name ?: context?.getString(R.string.title_alarm, id + 1) ?: id.toString()

}