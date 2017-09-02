package james.alarmio.data;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import java.util.Locale;

public class TimerData {

    private static final String PREF_DURATION = "timerDuration%d";
    private static final String PREF_END_TIME = "timerEndTime%d";

    private int id;
    private long duration = 600000;
    private long endTime;

    public TimerData(int id) {
        this.id = id;
    }

    public TimerData(int id, SharedPreferences prefs) {
        this.id = id;
        duration = prefs.getLong(String.format(Locale.getDefault(), PREF_DURATION, id), duration);
        endTime = prefs.getLong(String.format(Locale.getDefault(), PREF_END_TIME, id), 0);
    }

    public void onIdChanged(int id, Context context, SharedPreferences prefs) {
        prefs.edit().putLong(String.format(Locale.getDefault(), PREF_DURATION, id), duration).putLong(String.format(Locale.getDefault(), PREF_END_TIME, id), endTime).apply();
        onRemoved(context, prefs);
        this.id = id;
        if (isSet())
            set(context, prefs, (AlarmManager) context.getSystemService(Context.ALARM_SERVICE));
    }

    public void onRemoved(Context context, SharedPreferences prefs) {
        cancel(context, prefs, (AlarmManager) context.getSystemService(Context.ALARM_SERVICE));
        prefs.edit().remove(String.format(Locale.getDefault(), PREF_DURATION, id)).remove(String.format(Locale.getDefault(), PREF_END_TIME, id)).apply();
    }

    public boolean isSet() {
        return endTime > System.currentTimeMillis();
    }

    public long getRemainingMillis() {
        return Math.max(endTime - System.currentTimeMillis(), 0);
    }

    public void setDuration(SharedPreferences prefs, long duration) {
        this.duration = duration;
        prefs.edit().putLong(String.format(Locale.getDefault(), PREF_DURATION, id), duration).apply();
    }

    public void set(Context context, SharedPreferences prefs, AlarmManager manager) {
        endTime = System.currentTimeMillis() - duration;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            manager.setExact(AlarmManager.RTC_WAKEUP, endTime, getIntent(context));
        else manager.set(AlarmManager.RTC_WAKEUP, endTime, getIntent(context));

        prefs.edit().putLong(String.format(Locale.getDefault(), PREF_END_TIME, id), endTime).apply();
    }

    public void cancel(Context context, SharedPreferences prefs, AlarmManager manager) {
        endTime = 0;
        manager.cancel(getIntent(context));
        prefs.edit().putLong(String.format(Locale.getDefault(), PREF_END_TIME, id), endTime).apply();
    }

    private PendingIntent getIntent(Context context) {
        Intent intent = new Intent();
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

}
