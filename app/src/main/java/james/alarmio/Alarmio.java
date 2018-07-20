package james.alarmio;

import android.Manifest;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.LocationManager;
import android.media.Ringtone;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import io.multimoon.colorful.ColorPack;
import io.multimoon.colorful.ColorfulColor;
import io.multimoon.colorful.ColorfulKt;
import io.multimoon.colorful.Defaults;
import io.multimoon.colorful.ThemeColor;
import io.multimoon.colorful.ThemeColorInterface;
import james.alarmio.data.AlarmData;
import james.alarmio.data.PreferenceData;
import james.alarmio.data.TimerData;
import james.alarmio.services.SleepReminderService;
import james.alarmio.services.TimerService;
import james.crasher.Crasher;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;

public class Alarmio extends Application implements Player.EventListener {

    public static final int THEME_DAY_NIGHT = 0;
    public static final int THEME_DAY = 1;
    public static final int THEME_NIGHT = 2;
    public static final int THEME_AMOLED = 3;

    public static final String NOTIFICATION_CHANNEL_STOPWATCH = "stopwatch";
    public static final String NOTIFICATION_CHANNEL_TIMERS = "timers";

    private SharedPreferences prefs;
    private SunriseSunsetCalculator sunsetCalculator;

    private Ringtone currentRingtone;

    private List<AlarmData> alarms;
    private List<TimerData> timers;

    private List<AlarmioListener> listeners;
    private ActivityListener listener;

    private SimpleExoPlayer player;
    private HlsMediaSource.Factory mediaSourceFactory;
    private String currentStream;

    @Override
    public void onCreate() {
        super.onCreate();
        Crasher crasher = new Crasher(this);
        crasher.setEmail("dev@jfenn.me");
        crasher.setColor(ContextCompat.getColor(this, R.color.colorNightPrimary));

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        listeners = new ArrayList<>();
        alarms = new ArrayList<>();
        timers = new ArrayList<>();

        player = ExoPlayerFactory.newSimpleInstance(this, new DefaultTrackSelector());
        player.addListener(this);

        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "exoplayer2example"), null);
        mediaSourceFactory = new HlsMediaSource.Factory(dataSourceFactory);

        int alarmLength = PreferenceData.ALARM_LENGTH.getValue(this);
        for (int id = 0; id < alarmLength; id++) {
            alarms.add(new AlarmData(id, this));
        }

        int timerLength = PreferenceData.TIMER_LENGTH.getValue(this);
        for (int id = 0; id < timerLength; id++) {
            TimerData timer = new TimerData(id, this);
            if (timer.isSet())
                timers.add(timer);
        }

        if (timerLength > 0)
            startService(new Intent(this, TimerService.class));

        SleepReminderService.refreshSleepTime(this);

        ColorfulKt.initColorful(this, new Defaults(
                ThemeColor.WHITE,
                ThemeColor.BLUE,
                false,
                true,
                0
        ));
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
        alarm.onRemoved(this);

        int index = alarms.indexOf(alarm);
        alarms.remove(index);
        for (int i = index; i < alarms.size(); i++) {
            alarms.get(i).onIdChanged(i, this);
        }

        onAlarmCountChanged();
        onAlarmsChanged();
    }

    public void onAlarmCountChanged() {
        PreferenceData.ALARM_LENGTH.setValue(this, alarms.size());
    }

    public void onAlarmsChanged() {
        for (AlarmioListener listener : listeners) {
            listener.onAlarmsChanged();
        }
    }

    public TimerData newTimer() {
        TimerData timer = new TimerData(timers.size());
        timers.add(timer);
        onTimerCountChanged();
        return timer;
    }

    public void removeTimer(TimerData timer) {
        timer.onRemoved(this);

        int index = timers.indexOf(timer);
        timers.remove(index);
        for (int i = index; i < timers.size(); i++) {
            timers.get(i).onIdChanged(i, this);
        }

        onTimerCountChanged();
        onTimersChanged();
    }

    public void onTimerCountChanged() {
        PreferenceData.TIMER_LENGTH.setValue(this, alarms.size());
    }

    public void onTimersChanged() {
        for (AlarmioListener listener : listeners) {
            listener.onTimersChanged();
        }
    }

    public void onTimerStarted() {
        startService(new Intent(this, TimerService.class));
    }

    public SharedPreferences getPrefs() {
        return prefs;
    }

    public void updateTheme() {
        Function0<Unit> callback = new Function0<Unit>() {
            @Override
            public Unit invoke() {
                if (listener != null)
                    listener.recreate();

                return null;
            }
        };

        if (isNight()) {
            ColorfulKt.Colorful().edit()
                    .setDarkTheme(true)
                    .setPrimaryColor(new ThemeColorInterface() {
                        @Override
                        public int primaryStyle() {
                            return 0;
                        }

                        @Override
                        public int accentStyle() {
                            return 0;
                        }

                        @NotNull
                        @Override
                        public ColorPack getColorPack() {
                            return new ColorPack(new ColorfulColor("#212121"), new ColorfulColor("#101010"));
                        }

                        @NotNull
                        @Override
                        public String getThemeName() {
                            return null;
                        }
                    })
                    .setAccentColor(ThemeColor.RED)
                    .apply(this, callback);
        } else {
            int theme = getActivityTheme();
            if (theme == THEME_DAY || theme == THEME_DAY_NIGHT) {
                ColorfulKt.Colorful().edit()
                        .setDarkTheme(false)
                        .setPrimaryColor(ThemeColor.WHITE)
                        .setAccentColor(ThemeColor.BLUE)
                        .apply(this, callback);
            } else if (theme == THEME_AMOLED) {
                ColorfulKt.Colorful().edit()
                        .setDarkTheme(true)
                        .setPrimaryColor(ThemeColor.BLACK)
                        .setAccentColor(ThemeColor.WHITE)
                        .apply(this, callback);
            }
        }
    }

    public boolean isNight() {
        int time = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        return ((time < getDayStart() || time > getDayEnd()) && getActivityTheme() == THEME_DAY_NIGHT) || getActivityTheme() == THEME_NIGHT;
    }

    public int getActivityTheme() {
        return PreferenceData.THEME.getValue(this);
    }

    @ColorInt
    public int getTextColor() {
        return getTextColor(true, false);
    }

    @ColorInt
    public int getTextColor(boolean primary, boolean inverse) {
        return ContextCompat.getColor(this, inverse != ColorfulKt.Colorful().getDarkTheme()
                ? (primary ? R.color.textColorPrimaryNight : R.color.textColorSecondaryNight)
                : (primary ? R.color.textColorPrimary : R.color.textColorSecondary));
    }

    public boolean isDayAuto() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && (boolean) PreferenceData.DAY_AUTO.getValue(this);
    }

    /**
     * @return the hour of the start of the day (24h), as specified by the user
     */
    public int getDayStart() {
        if (isDayAuto() && getSunsetCalculator() != null)
            return getSunsetCalculator().getOfficialSunriseCalendarForDate(Calendar.getInstance()).get(Calendar.HOUR_OF_DAY);
        else return PreferenceData.DAY_START.getValue(this);
    }

    /**
     * @return the hour of the end of the day (24h), as specified by the user
     */
    public int getDayEnd() {
        if (isDayAuto() && getSunsetCalculator() != null)
            return getSunsetCalculator().getOfficialSunsetCalendarForDate(Calendar.getInstance()).get(Calendar.HOUR_OF_DAY);
        else return PreferenceData.DAY_END.getValue(this);
    }

    @Nullable
    public Integer getSunrise() {
        if (getSunsetCalculator() != null)
            return getSunsetCalculator().getOfficialSunsetCalendarForDate(Calendar.getInstance()).get(Calendar.HOUR_OF_DAY);
        else return null;
    }

    @Nullable
    public Integer getSunset() {
        if (getSunsetCalculator() != null)
            return getSunsetCalculator().getOfficialSunsetCalendarForDate(Calendar.getInstance()).get(Calendar.HOUR_OF_DAY);
        else return null;
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

    public boolean isRingtonePlaying() {
        return currentRingtone != null && currentRingtone.isPlaying();
    }

    public Ringtone getCurrentRingtone() {
        return currentRingtone;
    }

    public void playRingtone(Ringtone ringtone) {
        if (!ringtone.isPlaying()) {
            stopCurrentSound();
            ringtone.play();
        }

        currentRingtone = ringtone;
    }

    public void playStream(String url) {
        stopCurrentSound();
        player.prepare(mediaSourceFactory.createMediaSource(Uri.parse(url)));
        player.setPlayWhenReady(true);
        currentStream = url;
    }

    public void playStream(String url, AudioAttributes attributes) {
        player.stop();
        player.setAudioAttributes(attributes);
        playStream(url);
    }

    public void stopStream() {
        player.stop();
        currentStream = null;
    }

    public boolean isPlayingStream(String url) {
        return currentStream != null && currentStream.equals(url);
    }

    public void stopCurrentSound() {
        if (isRingtonePlaying())
            currentRingtone.stop();

        stopStream();
    }

    public void addListener(AlarmioListener listener) {
        listeners.add(listener);
    }

    public void removeListener(AlarmioListener listener) {
        listeners.remove(listener);
    }

    public void setListener(ActivityListener listener) {
        this.listener = listener;
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        switch (playbackState) {
            case Player.STATE_BUFFERING:
            case Player.STATE_READY:
                break;
            default:
                currentStream = null;
                break;
        }
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {
    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        currentStream = null;
        Exception exception;
        switch (error.type) {
            case ExoPlaybackException.TYPE_RENDERER:
                exception = error.getRendererException();
                break;
            case ExoPlaybackException.TYPE_SOURCE:
                exception = error.getSourceException();
                break;
            case ExoPlaybackException.TYPE_UNEXPECTED:
                exception = error.getUnexpectedException();
                break;
            default:
                return;
        }

        exception.printStackTrace();
        Toast.makeText(this, exception.getClass().getName() + ": " + exception.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPositionDiscontinuity(int reason) {
    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
    }

    @Override
    public void onSeekProcessed() {
    }

    public void requestPermissions(String... permissions) {
        if (listener != null)
            listener.requestPermissions(permissions);
    }

    public FragmentManager getFragmentManager() {
        if (listener != null)
            return listener.gettFragmentManager();
        else return null;
    }

    public interface AlarmioListener {
        void onAlarmsChanged();

        void onTimersChanged();
    }

    public interface ActivityListener {
        void requestPermissions(String... permissions);

        FragmentManager gettFragmentManager(); //help

        void recreate();
    }

}
