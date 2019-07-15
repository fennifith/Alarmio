package me.jfenn.alarmio.common.interfaces

import me.jfenn.alarmio.common.data.interfaces.AlertData
import java.util.*

interface AlertScheduler {

    fun set(alert: AlertData): Date

    fun cancel(alert: AlertData)

}