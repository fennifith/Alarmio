package me.jfenn.alarmio.impl

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import me.jfenn.alarmio.common.data.AlarmData
import me.jfenn.alarmio.common.data.interfaces.AlertData
import me.jfenn.alarmio.common.interfaces.AlertScheduler
import me.jfenn.alarmio.receivers.AlertReceiver
import me.jfenn.alarmio.ui.activities.MainActivity
import java.util.*

class AlertSchedulerImpl(
        val context: Context
) : AlertScheduler {

    private val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Schedule an alert to trigger at a certain time.
     *
     * @param alert         The alert to schedule.
     * @return The time of the scheduled alert.
     */
    override fun schedule(alert: AlertData): Date = alert.getAlertTime()?.let { alertTime ->
        if (alert is AlarmData && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            manager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(
                            alertTime.time,
                            PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), 0)
                    ),
                    getIntent(alert)
            )
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                manager.setExact(AlarmManager.RTC_WAKEUP, alertTime.time, getIntent(alert))
            else
                manager.set(AlarmManager.RTC_WAKEUP, alertTime.time, getIntent(alert))

            if (alert is AlarmData) {
                val intent = Intent("android.intent.action.ALARM_CHANGED")
                intent.putExtra("alarmSet", true)
                context.sendBroadcast(intent)
            }
        }

        alertTime
    } ?: run {
        throw RuntimeException()
    }

    /**
     * Re-schedule an alert if it is repeating.
     *
     * @param alert         The alert to reschedule.
     * @return The time of the scheduled alert.
     */
    override fun reschedule(alert: AlertData): Date? {
        return if (alert is AlarmData && alert.isRepeat()) {
            schedule(alert)
        } else null
    }

    /**
     * Cancel a scheduled alert.
     *
     * @param alert         The alert to cancel.
     */
    override fun cancel(alert: AlertData) {
        manager.cancel(getIntent(alert))

        if (alert is AlarmData && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            val intent = Intent("android.intent.action.ALARM_CHANGED")
            intent.putExtra("alarmSet", false)
            context.sendBroadcast(intent)
        }
    }

    /**
     * The intent to fire when the alert should ring.
     *
     * @param alert         The alert that should ring.
     * @return              A PendingIntent that will open the alert screen.
     */
    fun getIntent(alert: AlertData): PendingIntent {
        val intent = Intent(context, AlertReceiver::class.java)
        intent.putExtra(AlertReceiver.EXTRA_ALERT_ID, alert.getAlertId())
        return PendingIntent.getBroadcast(context, alert.id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

}