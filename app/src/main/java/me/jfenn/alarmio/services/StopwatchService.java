package me.jfenn.alarmio.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import me.jfenn.alarmio.Alarmio;
import me.jfenn.alarmio.R;
import me.jfenn.alarmio.activities.MainActivity;
import me.jfenn.alarmio.utils.FormatUtils;

public class StopwatchService extends Service {

    private static final int NOTIFICATION_ID = 247;
    private static final String ACTION_RESET = "james.alarmio.StopwatchFragment.ACTION_RESET";
    private static final String ACTION_TOGGLE = "james.alarmio.StopwatchFragment.ACTION_TOGGLE";
    private static final String ACTION_LAP = "james.alarmio.StopwatchFragment.ACTION_LAP";

    private final IBinder binder = new LocalBinder();
    private Listener listener;

    private Handler handler;
    private Runnable runnable;

    private long startTime, pauseTime, stopTime;
    private List<Long> laps;
    private long lastLapTime;
    private boolean isRunning;

    private String notificationText;
    private NotificationManager notificationManager;
    private NotificationReceiver receiver;

    @Override
    public void onCreate() {
        super.onCreate();
        receiver = new NotificationReceiver(this);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        laps = new ArrayList<>();

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    long currentTime = System.currentTimeMillis() - startTime;
                    String text = FormatUtils.formatMillis(currentTime);
                    if (listener != null)
                        listener.onTick(currentTime, text);
                    text = text.substring(0, text.length() - 3);
                    if (notificationText == null || !notificationText.equals(text)) {
                        startForeground(NOTIFICATION_ID, getNotification(text));
                        notificationText = text;
                    }
                    handler.removeCallbacks(this);
                    handler.postDelayed(this, 10);
                } else if (listener != null) {
                    long time = startTime == 0 ? 0 : stopTime - startTime;
                    listener.onTick(time, FormatUtils.formatMillis(time));
                }
            }
        };

        startForeground(NOTIFICATION_ID, getNotification("0s"));
        handler.postDelayed(runnable, 1000);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_RESET);
        filter.addAction(ACTION_TOGGLE);
        filter.addAction(ACTION_LAP);
        registerReceiver(receiver, filter);
    }

    @Override
    public void onDestroy() {
        if (receiver != null)
            unregisterReceiver(receiver);
        super.onDestroy();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public List<Long> getLaps() {
        return laps;
    }

    public long getElapsedTime() {
        return stopTime - startTime;
    }

    public long getLastLapTime() {
        return lastLapTime;
    }

    /**
     * Reset the stopwatch, cancelling any notifications and setting everything to zero.
     */
    public void reset() {
        if (isRunning)
            toggle();

        startTime = 0;
        pauseTime = 0;
        handler.post(runnable);
        laps.clear();
        lastLapTime = 0;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            stopForeground(true);

        if (listener != null)
            listener.onReset();
    }

    /**
     * Toggle whether the stopwatch is currently running (pausing it and storing a temporary
     * time if so).
     */
    public void toggle() {
        stopTime = System.currentTimeMillis();
        isRunning = !isRunning;
        if (isRunning) {
            if (startTime == 0)
                startTime = System.currentTimeMillis();
            else if (pauseTime != 0)
                startTime += System.currentTimeMillis() - pauseTime;

            handler.post(runnable);
        } else pauseTime = System.currentTimeMillis();

        notificationText = FormatUtils.formatMillis(System.currentTimeMillis() - startTime);
        startForeground(NOTIFICATION_ID, getNotification(notificationText));

        if (listener != null)
            listener.onStateChanged(isRunning);
    }

    /**
     * Record the current time as a "lap".
     */
    public void lap() {
        long lapTime = System.currentTimeMillis() - startTime;
        long lapDiff = lapTime - lastLapTime;
        laps.add(lapDiff);
        long lastLastLapTime = lastLapTime;
        lastLapTime = lapTime;

        if (listener != null)
            listener.onLap(laps.size(), lapTime, lastLastLapTime, lapDiff);
    }

    /**
     * Get a notification to send to the user for the current time.
     *
     * @param time      A formatted string defining the current time on the stopwatch.
     * @return          A notification to use for this stopwatch.
     */
    private Notification getNotification(String time) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            notificationManager.createNotificationChannel(new NotificationChannel(Alarmio.NOTIFICATION_CHANNEL_STOPWATCH, getString(R.string.title_stopwatch), NotificationManager.IMPORTANCE_DEFAULT));

        return new NotificationCompat.Builder(this, Alarmio.NOTIFICATION_CHANNEL_STOPWATCH)
                .setSmallIcon(R.drawable.ic_stopwatch_notification)
                .setContentTitle(getString(R.string.title_stopwatch))
                .setContentText(time)
                .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class).putExtra(MainActivity.EXTRA_FRAGMENT, MainActivity.FRAGMENT_STOPWATCH), 0))
                .setDeleteIntent(PendingIntent.getBroadcast(this, 0, new Intent(ACTION_RESET).setPackage(getPackageName()), 0))
                .addAction(
                        isRunning ? R.drawable.ic_pause_notification : R.drawable.ic_play_notification,
                        isRunning ? "Pause" : "Play",
                        PendingIntent.getBroadcast(this, 0, new Intent(ACTION_TOGGLE), 0)
                )
                .addAction(
                        R.drawable.ic_lap_notification,
                        "Lap",
                        PendingIntent.getBroadcast(this, 0, new Intent(ACTION_LAP), 0)
                )
                .build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        listener = null;
        return super.onUnbind(intent);
    }

    public class LocalBinder extends Binder {
        public StopwatchService getService() {
            return StopwatchService.this;
        }
    }

    public interface Listener {
        void onStateChanged(boolean isRunning);

        void onReset();

        void onTick(long currentTime, String text);

        void onLap(int lapNum, long lapTime, long lastLapTime, long lapDiff);
    }

    private static class NotificationReceiver extends BroadcastReceiver {

        private WeakReference<StopwatchService> serviceReference;

        public NotificationReceiver(StopwatchService service) {
            serviceReference = new WeakReference<>(service);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            StopwatchService service = serviceReference.get();
            if (intent != null && intent.getAction() != null && service != null) {
                switch (intent.getAction()) {
                    case ACTION_RESET:
                        service.reset();
                        break;
                    case ACTION_TOGGLE:
                        service.toggle();
                        break;
                    case ACTION_LAP:
                        service.lap();
                        break;
                }
            }
        }
    }

}
