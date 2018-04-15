package james.alarmio.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import james.alarmio.Alarmio;
import james.alarmio.R;
import james.alarmio.data.AlarmData;
import james.alarmio.data.PreferenceData;
import james.alarmio.utils.FormatUtils;

public class SleepReminderService extends Service {

    private Alarmio alarmio;
    private PowerManager powerManager;
    private ScreenReceiver receiver;

    @Override
    public void onCreate() {
        super.onCreate();
        alarmio = (Alarmio) getApplicationContext();
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        receiver = new ScreenReceiver(this);
        refreshState();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(receiver, filter);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        refreshState();
        return super.onStartCommand(intent, flags, startId);
    }

    public void refreshState() {
        if ((boolean) PreferenceData.SLEEP_REMINDER.getValue(this) && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? powerManager.isInteractive() : powerManager.isScreenOn())) {
            AlarmData nextAlarm = getNextWakeAlarm(alarmio);
            if (nextAlarm != null) {
                Calendar nextTrigger = (Calendar) nextAlarm.getNext().clone();
                nextTrigger.set(Calendar.MINUTE, nextTrigger.get(Calendar.MINUTE) - (int) PreferenceData.SLEEP_REMINDER_TIME.getValue(this));

                if (Calendar.getInstance().after(nextTrigger)) {
                    NotificationCompat.Builder builder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        if (manager != null)
                            manager.createNotificationChannel(new NotificationChannel("sleepReminder", getString(R.string.title_sleep_reminder), NotificationManager.IMPORTANCE_DEFAULT));

                        builder = new NotificationCompat.Builder(this, "sleepReminder");
                    } else builder = new NotificationCompat.Builder(this);

                    startForeground(540, builder.setContentTitle(getString(R.string.title_sleep_reminder))
                            .setContentText(String.format(getString(R.string.msg_sleep_reminder),
                                    FormatUtils.formatUnit(this, (int) TimeUnit.MILLISECONDS.toMinutes(nextAlarm.getNext().getTimeInMillis() - System.currentTimeMillis()))))
                            .setSmallIcon(R.drawable.ic_notification_sleep)
                            .setPriority(NotificationCompat.PRIORITY_LOW)
                            .build());
                    return;
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            stopSelf();

        stopForeground(true);
    }

    public static AlarmData getNextWakeAlarm(Alarmio alarmio) {
        Calendar nextNoon = Calendar.getInstance();
        nextNoon.set(Calendar.HOUR_OF_DAY, 12);
        while (nextNoon.before(Calendar.getInstance()))
            nextNoon.set(Calendar.DAY_OF_YEAR, nextNoon.get(Calendar.DAY_OF_YEAR) + 1);

        List<AlarmData> alarms = alarmio.getAlarms();
        AlarmData nextAlarm = null;
        for (AlarmData alarm : alarms) {
            Calendar next = alarm.getNext();
            if (alarm.isEnabled && next.before(nextNoon) && (nextAlarm == null || nextAlarm.getNext().after(next)))
                nextAlarm = alarm;
        }

        return nextAlarm;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static class ScreenReceiver extends BroadcastReceiver {

        private WeakReference<SleepReminderService> serviceReference;

        public ScreenReceiver(SleepReminderService service) {
            serviceReference = new WeakReference<>(service);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            SleepReminderService service = serviceReference.get();
            if (service != null)
                service.refreshState();
        }
    }

}
