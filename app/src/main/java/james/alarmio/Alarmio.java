package james.alarmio;

import android.Manifest;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;

import com.afollestad.aesthetic.Aesthetic;
import com.afollestad.aesthetic.AutoSwitchMode;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import io.reactivex.annotations.Nullable;
import james.alarmio.data.AlarmData;
import james.alarmio.data.TimerData;

public class Alarmio extends Application {

    public static final String PREF_THEME = "theme";
    public static final String PREF_DAY_AUTO = "dayAuto";
    public static final String PREF_DAY_START = "dayStart";
    public static final String PREF_DAY_END = "dayEnd";
    public static final String PREF_ALARM_LENGTH = "alarmLength";

    public static final int THEME_DAY_NIGHT = 0;
    public static final int THEME_DAY = 1;
    public static final int THEME_NIGHT = 2;

    private SharedPreferences prefs;
    private SunriseSunsetCalculator sunsetCalculator;

    private List<AlarmData> alarms;
    private List<TimerData> timers;

    private List<AlarmioListener> listeners;

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        listeners = new ArrayList<>();
        alarms = new ArrayList<>();
        timers = new ArrayList<>();

        int alarmLength = prefs.getInt(PREF_ALARM_LENGTH, 0);
        for (int id = 0; id < alarmLength; id++) {
            alarms.add(new AlarmData(id, this, prefs));
        }
    }

    public List<AlarmData> getAlarms() {
        return alarms;
    }

    public List<TimerData> getTimers() {
        return timers;
    }

    public AlarmData newAlarm() {
        AlarmData alarm = new AlarmData(alarms.size(), Calendar.getInstance());
        alarms.add(alarm);
        onAlarmCountChanged();
        return alarm;
    }

    public void removeAlarm(AlarmData alarm) {
        alarm.onRemove(this, getPrefs());

        int index = alarms.indexOf(alarm);
        alarms.remove(index);
        for (int i = index; i < alarms.size(); i++) {
            alarms.get(i).onIdChanged(i, this, getPrefs());
        }

        onAlarmCountChanged();
        onAlarmsChanged();
    }

    public void onAlarmCountChanged() {
        prefs.edit().putInt(PREF_ALARM_LENGTH, alarms.size()).apply();
    }

    public void onAlarmsChanged() {
        for (AlarmioListener listener : listeners) {
            listener.onAlarmsChanged();
        }
    }

    public TimerData newTimer() {
        TimerData timer = new TimerData(timers.size());
        timers.add(timer);
        return timer;
    }

    public void removeTimer(TimerData timer) {
        timers.remove(timer);
        onTimersChanged();
    }

    public void onTimersChanged() {
        for (AlarmioListener listener : listeners) {
            listener.onTimersChanged();
        }
    }

    public SharedPreferences getPrefs() {
        return prefs;
    }

    public void onActivityResume() {
        if (isNight()) {
            Aesthetic.get()
                    .isDark(true)
                    .lightStatusBarMode(AutoSwitchMode.OFF)
                    .colorPrimary(ContextCompat.getColor(this, R.color.colorNightPrimary))
                    .colorStatusBar(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ? Color.TRANSPARENT : ContextCompat.getColor(this, R.color.colorNightPrimaryDark))
                    .colorNavigationBar(ContextCompat.getColor(this, R.color.colorNightPrimaryDark))
                    .colorAccent(ContextCompat.getColor(this, R.color.colorNightAccent))
                    .colorCardViewBackground(ContextCompat.getColor(this, R.color.colorNightForeground))
                    .colorWindowBackground(ContextCompat.getColor(this, R.color.colorNightPrimaryDark))
                    .textColorPrimary(ContextCompat.getColor(this, R.color.textColorPrimaryNight))
                    .textColorSecondary(ContextCompat.getColor(this, R.color.textColorSecondaryNight))
                    .textColorPrimaryInverse(ContextCompat.getColor(this, R.color.textColorPrimary))
                    .textColorSecondaryInverse(ContextCompat.getColor(this, R.color.textColorSecondary))
                    .apply();
        } else {
            Aesthetic.get()
                    .lightStatusBarMode(AutoSwitchMode.ON)
                    .colorPrimary(ContextCompat.getColor(this, R.color.colorPrimary))
                    .colorStatusBar(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ? Color.TRANSPARENT : ContextCompat.getColor(this, R.color.colorPrimaryDark))
                    .colorNavigationBar(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                    .colorAccent(ContextCompat.getColor(this, R.color.colorAccent))
                    .colorCardViewBackground(ContextCompat.getColor(this, R.color.colorForeground))
                    .colorWindowBackground(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                    .textColorPrimary(ContextCompat.getColor(this, R.color.textColorPrimary))
                    .textColorSecondary(ContextCompat.getColor(this, R.color.textColorSecondary))
                    .textColorPrimaryInverse(ContextCompat.getColor(this, R.color.textColorPrimaryNight))
                    .textColorSecondaryInverse(ContextCompat.getColor(this, R.color.textColorSecondaryNight))
                    .apply();
        }
    }

    public boolean isNight() {
        int time = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        return ((time < getDayStart() || time > getDayEnd()) && getActivityTheme() == THEME_DAY_NIGHT) || getActivityTheme() == THEME_NIGHT;
    }

    public int getActivityTheme() {
        return prefs.getInt(PREF_THEME, THEME_DAY_NIGHT);
    }

    public boolean isDayAuto() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && prefs.getBoolean(PREF_DAY_AUTO, true);
    }

    public int getDayStart() {
        if (isDayAuto() && getSunsetCalculator() != null)
            return getSunsetCalculator().getOfficialSunriseCalendarForDate(Calendar.getInstance()).get(Calendar.HOUR_OF_DAY);
        else return prefs.getInt(PREF_DAY_START, 6);
    }

    public int getDayEnd() {
        if (isDayAuto() && getSunsetCalculator() != null)
            return getSunsetCalculator().getOfficialSunsetCalendarForDate(Calendar.getInstance()).get(Calendar.HOUR_OF_DAY);
        else return prefs.getInt(PREF_DAY_END, 18);
    }

    @Nullable
    private SunriseSunsetCalculator getSunsetCalculator() {
        if (sunsetCalculator == null && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                android.location.Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(new Criteria(), false));
                sunsetCalculator = new SunriseSunsetCalculator(new Location(location.getLatitude(), location.getLongitude()), TimeZone.getDefault().getID());
            } catch (NullPointerException ignored) {
            }
        }

        return sunsetCalculator;
    }

    public void addListener(AlarmioListener listener) {
        listeners.add(listener);
    }

    public void removeListener(AlarmioListener listener) {
        listeners.remove(listener);
    }

    public interface AlarmioListener {
        void onAlarmsChanged();

        void onTimersChanged();
    }

}
