package me.jfenn.alarmio.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import androidx.core.app.NotificationCompat

import me.jfenn.alarmio.Alarmio
import me.jfenn.alarmio.R
import me.jfenn.alarmio.ui.activities.MainActivity
import me.jfenn.alarmio.utils.FormatUtils
import me.jfenn.alarmio.viewmodel.AlarmioViewModel
import org.koin.android.ext.android.inject

class TimerService : Service() {

    companion object {

        private val NOTIFICATION_ID = 427

    }

    private val alarmio: AlarmioViewModel by inject()

    private val binder = LocalBinder()

    private val handler = Handler()
    private val runnable = object : Runnable {
        override fun run() {
            if (timers!!.size > 0) {
                val notification = notification
                if (notification != null)
                    startForeground(NOTIFICATION_ID, notification)

                handler.removeCallbacks(this)
                handler.postDelayed(this, 10)
            } else
                stopForeground(true)
        }
    }

    private var notificationManager: NotificationManager? = null
    private var notificationString: String? = null

    private val notification: Notification?
        get() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                notificationManager!!.createNotificationChannel(NotificationChannel(Alarmio.NOTIFICATION_CHANNEL_TIMERS, "Timers", NotificationManager.IMPORTANCE_LOW))

            val inboxStyle = NotificationCompat.InboxStyle()
            var string = ""
            for (timer in timers!!) {
                if (!timer.isSet())
                    continue

                var time = FormatUtils.formatMillis(timer.getRemainingMillis())
                time = time.substring(0, time.length - 3)
                inboxStyle.addLine(time)
                string += "/$time/"
            }

            if (notificationString != null && notificationString == string)
                return null

            notificationString = string

            val intent = Intent(this, MainActivity::class.java)
            if (timers!!.size == 1)
                intent.putExtra(TimerReceiver.EXTRA_TIMER_ID, 0)

            return NotificationCompat.Builder(this, Alarmio.NOTIFICATION_CHANNEL_TIMERS)
                    .setSmallIcon(R.drawable.ic_timer_notification)
                    .setContentTitle(getString(R.string.title_set_timer))
                    .setContentText("")
                    .setContentIntent(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT))
                    .setStyle(inboxStyle)
                    .build()
        }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        handler.removeCallbacks(runnable)
        runnable.run()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    override fun onUnbind(intent: Intent): Boolean {
        //listener = null;
        return super.onUnbind(intent)
    }

    inner class LocalBinder : Binder() {
        val service: TimerService
            get() = this@TimerService
    }

}
