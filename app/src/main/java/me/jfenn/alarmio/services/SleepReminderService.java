package me.jfenn.alarmio.services;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import me.jfenn.alarmio.Alarmio;
import me.jfenn.alarmio.R;
import me.jfenn.alarmio.data.AlarmData;
import me.jfenn.alarmio.data.PreferenceData;
import me.jfenn.alarmio.utils.FormatUtils;

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

    /**
     * Refresh the state of the sleepy stuff. This will either show a notification if a notification
     * should be shown, or stop the service if it shouldn't.
     */
    public void refreshState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? powerManager.isInteractive() : powerManager.isScreenOn()) {
            AlarmData nextAlarm = getSleepyAlarm(alarmio);
            if (nextAlarm != null) {
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
                        .setCategory(NotificationCompat.CATEGORY_SERVICE)
                        .build());
                return;
            }
        }

        stopForeground(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            stopSelf();
    }

    /**
     * Get a sleepy alarm. Well, get the next alarm that should trigger a sleep alert.
     *
     * @param alarmio       The active Application instance.
     * @return              The next [AlarmData](../data/AlarmData) that should trigger a
     *                      sleep alert, or null if there isn't one.
     */
    @Nullable
    public static AlarmData getSleepyAlarm(Alarmio alarmio) {
        if (PreferenceData.SLEEP_REMINDER.getValue(alarmio)) {
            AlarmData nextAlarm = getNextWakeAlarm(alarmio);
            if (nextAlarm != null) {
                Calendar nextTrigger = nextAlarm.getNext();
                nextTrigger.set(Calendar.MINUTE, nextTrigger.get(Calendar.MINUTE) - (int) TimeUnit.MILLISECONDS.toMinutes((long) PreferenceData.SLEEP_REMINDER_TIME.getValue(alarmio)));

                if (Calendar.getInstance().after(nextTrigger))
                    return nextAlarm;
            }
        }

        return null;
    }

    /**
     * Get the next scheduled [AlarmData](../data/AlarmData) that will ring.
     *
     * @param alarmio       The active Application instance.
     * @return              The next AlarmData that will wake the user up.
     */
    @Nullable
    public static AlarmData getNextWakeAlarm(Alarmio alarmio) {
        Calendar nextNoon = Calendar.getInstance();
        nextNoon.set(Calendar.HOUR_OF_DAY, 12);
        if (nextNoon.before(Calendar.getInstance()))
            nextNoon.set(Calendar.DAY_OF_YEAR, nextNoon.get(Calendar.DAY_OF_YEAR) + 1);
        else return null;

        Calendar nextDay = Calendar.getInstance();
        nextDay.set(Calendar.HOUR_OF_DAY, 0);
        while (nextDay.before(Calendar.getInstance()))
            nextDay.set(Calendar.DAY_OF_YEAR, nextDay.get(Calendar.DAY_OF_YEAR) + 1);

        List<AlarmData> alarms = alarmio.getAlarms();
        AlarmData nextAlarm = null;
        for (AlarmData alarm : alarms) {
            Calendar next = alarm.getNext();
            if (alarm.isEnabled && next.before(nextNoon) && next.after(nextDay) && (nextAlarm == null || nextAlarm.getNext().after(next)))
                nextAlarm = alarm;
        }

        return nextAlarm;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * To be called whenever an alarm is changed, might change, or when time might have
     * unexpectedly leaped forwards. This will start the service if there is a
     * [sleepy alarm](#getsleepyalarm) present.
     *
     * @param context       An active context instance.
     */
    public static void refreshSleepTime(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && ContextCompat.checkSelfPermission(context, Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED)
            return;

        Alarmio alarmio;
        if (context instanceof Alarmio)
            alarmio = (Alarmio) context;
        else alarmio = (Alarmio) context.getApplicationContext();

        if (getSleepyAlarm(alarmio) != null)
            ContextCompat.startForegroundService(context, new Intent(alarmio, SleepReminderService.class));
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
