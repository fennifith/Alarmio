package me.jfenn.alarmio.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import me.jfenn.alarmio.Alarmio;
import me.jfenn.alarmio.R;
import me.jfenn.alarmio.activities.MainActivity;
import me.jfenn.alarmio.data.TimerData;
import me.jfenn.alarmio.receivers.TimerReceiver;
import me.jfenn.alarmio.utils.FormatUtils;

public class TimerService extends Service {

    private static final int NOTIFICATION_ID = 427;

    private final IBinder binder = new LocalBinder();

    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (timers.size() > 0) {
                Notification notification = getNotification();
                if (notification != null)
                    startForeground(NOTIFICATION_ID, notification);
                handler.removeCallbacks(this);
                handler.postDelayed(this, 10);
            } else stopForeground(true);
        }
    };

    private List<TimerData> timers;
    private NotificationManager notificationManager;
    private String notificationString;

    @Override
    public void onCreate() {
        super.onCreate();
        timers = ((Alarmio) getApplicationContext()).getTimers();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.removeCallbacks(runnable);
        runnable.run();
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    private Notification getNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            notificationManager.createNotificationChannel(new NotificationChannel(Alarmio.NOTIFICATION_CHANNEL_TIMERS, "Timers", NotificationManager.IMPORTANCE_LOW));

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        String string = "";
        for (TimerData timer : timers) {
            if (!timer.isSet())
                continue;

            String time = FormatUtils.formatMillis(timer.getRemainingMillis());
            time = time.substring(0, time.length() - 3);
            inboxStyle.addLine(time);
            string += "/" + time + "/";
        }

        if (notificationString != null && notificationString.equals(string))
            return null;

        notificationString = string;

        Intent intent = new Intent(this, MainActivity.class);
        if (timers.size() == 1)
            intent.putExtra(TimerReceiver.EXTRA_TIMER_ID, 0);

        return new NotificationCompat.Builder(this, Alarmio.NOTIFICATION_CHANNEL_TIMERS)
                .setSmallIcon(R.drawable.ic_timer_notification)
                .setContentTitle(getString(R.string.title_set_timer))
                .setContentText("")
                .setContentIntent(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT))
                .setStyle(inboxStyle)
                .build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        //listener = null;
        return super.onUnbind(intent);
    }

    public class LocalBinder extends Binder {
        public TimerService getService() {
            return TimerService.this;
        }
    }
}
