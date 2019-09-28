package me.jfenn.alarmio.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import me.jfenn.alarmio.common.interfaces.AlertScheduler
import me.jfenn.alarmio.viewmodel.AlarmioViewModel
import org.koin.core.KoinComponent
import org.koin.core.inject

class RestoreOnBootReceiver : BroadcastReceiver(), KoinComponent {

    val alarmio: AlarmioViewModel by inject()
    val scheduler: AlertScheduler by inject()

    override fun onReceive(context: Context, intent: Intent) {
        for (alarm in alarmio.getAlarms()) {
            if (alarm.isEnabled)
                scheduler.schedule(alarm)
        }

        for (timer in alarmio.getTimers()) {
            if (timer.getRemainingMillis() > 0)
                scheduler.schedule(timer)
        }
    }
}
