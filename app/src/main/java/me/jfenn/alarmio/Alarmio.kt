package me.jfenn.alarmio

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Criteria
import android.location.LocationManager
import android.os.Build
import android.preference.PreferenceManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.afollestad.aesthetic.Aesthetic
import com.afollestad.aesthetic.AutoSwitchMode
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator
import com.luckycatlabs.sunrisesunset.dto.Location
import me.jfenn.alarmio.common.impl.AlertPlayerImpl
import me.jfenn.alarmio.common.interfaces.*
import me.jfenn.alarmio.data.AlarmData
import me.jfenn.alarmio.data.PreferenceData
import me.jfenn.alarmio.data.SoundData
import me.jfenn.alarmio.data.TimerData
import me.jfenn.alarmio.impl.AlarmioRepositoryImpl
import me.jfenn.alarmio.impl.AlertSchedulerImpl
import me.jfenn.alarmio.impl.SoundPlayerImpl
import me.jfenn.alarmio.impl.VibrationPlayerImpl
import me.jfenn.alarmio.services.SleepReminderService
import me.jfenn.alarmio.services.TimerService
import me.jfenn.alarmio.utils.DebugUtils
import me.jfenn.alarmio.viewmodel.AlarmioViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.util.*

class Alarmio : Application() {

    /**
     * Get an instance of SharedPreferences.
     *
     * @return          The instance of SharedPreferences being used by the application.
     * @see [android.content.SharedPreferences Documentation]
     */
    var prefs: SharedPreferences? = null
        private set
    private var sunsetCalculator: SunriseSunsetCalculator? = null

    private var alarms: MutableList<AlarmData>? = null
    private var timers: MutableList<TimerData>? = null

    private var listeners: MutableList<AlarmioListener>? = null
    private var listener: ActivityListener? = null

    /**
     * Determine if the theme should be a night theme.
     *
     * @return          True if the current theme is a night theme.
     */
    val isNight: Boolean
        get() {
            val time = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            return (time < dayStart || time > dayEnd) && activityTheme == THEME_DAY_NIGHT || activityTheme == THEME_NIGHT
        }

    /**
     * Get the theme to be used for activities and things. Despite
     * what the name implies, it does not return a theme resource,
     * but rather one of Alarmio.THEME_DAY_NIGHT, Alarmio.THEME_DAY,
     * Alarmio.THEME_NIGHT, or Alarmio.THEME_AMOLED.
     *
     * @return          The theme to be used for activites.
     */
    val activityTheme: Int
        get() = PreferenceData.THEME.getValue(this)

    /**
     * Determine if the sunrise/sunset stuff should occur automatically.
     *
     * @return          True if the day/night stuff is automated.
     */
    val isDayAuto: Boolean
        get() = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && PreferenceData.DAY_AUTO.getValue<Any>(this) as Boolean

    /**
     * @return the hour of the start of the day (24h), as specified by the user
     */
    val dayStart: Int
        get() = if (isDayAuto && getSunsetCalculator() != null)
            getSunsetCalculator()!!.getOfficialSunriseCalendarForDate(Calendar.getInstance()).get(Calendar.HOUR_OF_DAY)
        else
            PreferenceData.DAY_START.getValue(this)

    /**
     * @return the hour of the end of the day (24h), as specified by the user
     */
    val dayEnd: Int
        get() = if (isDayAuto && getSunsetCalculator() != null)
            getSunsetCalculator()!!.getOfficialSunsetCalendarForDate(Calendar.getInstance()).get(Calendar.HOUR_OF_DAY)
        else
            PreferenceData.DAY_END.getValue(this)

    /**
     * @return the hour of the calculated sunrise time, or null.
     */
    val sunrise: Int?
        get() = if (getSunsetCalculator() != null)
            getSunsetCalculator()!!.getOfficialSunsetCalendarForDate(Calendar.getInstance()).get(Calendar.HOUR_OF_DAY)
        else
            null

    /**
     * @return the hour of the calculated sunset time, or null.
     */
    val sunset: Int?
        get() = if (getSunsetCalculator() != null)
            getSunsetCalculator()!!.getOfficialSunsetCalendarForDate(Calendar.getInstance()).get(Calendar.HOUR_OF_DAY)
        else
            null

    val fragmentManager: FragmentManager?
        get() = if (listener != null)
            listener!!.gettFragmentManager()
        else
            null

    private val appModule = module {
        single<SoundPlayer> { SoundPlayerImpl(get()) }
        single<VibrationPlayer> { VibrationPlayerImpl(get()) }
        single<AlertPlayer> { AlertPlayerImpl(get(), get()) }
        single<AlertScheduler> { AlertSchedulerImpl(get()) }
        single<AlarmioRepository> { AlarmioRepositoryImpl(get()) }
        viewModel { AlarmioViewModel(get(), get()) }
    }

    override fun onCreate() {
        super.onCreate()
        DebugUtils.setup(this)

        startKoin {
            androidLogger()
            androidContext(this@Alarmio)
            modules(appModule)
        }

        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        listeners = ArrayList()
        alarms = ArrayList()
        timers = ArrayList()

        val alarmLength = PreferenceData.ALARM_LENGTH.getValue<Int>(this)
        for (id in 0 until alarmLength) {
            alarms!!.add(AlarmData(id, this))
        }

        val timerLength = PreferenceData.TIMER_LENGTH.getValue<Int>(this)
        for (id in 0 until timerLength) {
            val timer = TimerData(id, this)
            if (timer.isSet)
                timers!!.add(timer)
        }

        if (timerLength > 0)
            startService(Intent(this, TimerService::class.java))

        SleepReminderService.refreshSleepTime(this)
    }

    fun getAlarms(): List<AlarmData>? {
        return alarms
    }

    fun getTimers(): List<TimerData>? {
        return timers
    }

    /**
     * Create a new alarm, assigning it an unused preference id.
     *
     * @return          The newly instantiated [AlarmData](./data/AlarmData).
     */
    fun newAlarm(): AlarmData {
        val alarm = AlarmData(alarms!!.size, Calendar.getInstance())
        alarm.sound = SoundData.fromString(PreferenceData.DEFAULT_ALARM_RINGTONE.getValue(this, ""))
        alarms!!.add(alarm)
        onAlarmCountChanged()
        return alarm
    }

    /**
     * Remove an alarm and all of its its preferences.
     *
     * @param alarm     The alarm to be removed.
     */
    fun removeAlarm(alarm: AlarmData) {
        alarm.onRemoved(this)

        val index = alarms!!.indexOf(alarm)
        alarms!!.removeAt(index)
        for (i in index until alarms!!.size) {
            alarms!![i].onIdChanged(i, this)
        }

        onAlarmCountChanged()
        onAlarmsChanged()
    }

    /**
     * Update preferences to show that the alarm count has been changed.
     */
    fun onAlarmCountChanged() {
        PreferenceData.ALARM_LENGTH.setValue(this, alarms!!.size)
    }

    /**
     * Notify the application of changes to the current alarms.
     */
    fun onAlarmsChanged() {
        for (listener in listeners!!) {
            listener.onAlarmsChanged()
        }
    }

    /**
     * Create a new timer, assigning it an unused preference id.
     *
     * @return          The newly instantiated [TimerData](./data/TimerData).
     */
    fun newTimer(): TimerData {
        val timer = TimerData(timers!!.size)
        timers!!.add(timer)
        onTimerCountChanged()
        return timer
    }

    /**
     * Remove a timer and all of its preferences.
     *
     * @param timer     The timer to be removed.
     */
    fun removeTimer(timer: TimerData) {
        timer.onRemoved(this)

        val index = timers!!.indexOf(timer)
        timers!!.removeAt(index)
        for (i in index until timers!!.size) {
            timers!![i].onIdChanged(i, this)
        }

        onTimerCountChanged()
        onTimersChanged()
    }

    /**
     * Update the preferences to show that the timer count has been changed.
     */
    fun onTimerCountChanged() {
        PreferenceData.TIMER_LENGTH.setValue(this, timers!!.size)
    }

    /**
     * Notify the application of changes to the current timers.
     */
    fun onTimersChanged() {
        for (listener in listeners!!) {
            listener.onTimersChanged()
        }
    }

    /**
     * Starts the timer service after a timer has been set.
     */
    fun onTimerStarted() {
        startService(Intent(this, TimerService::class.java))
    }

    /**
     * Update the application theme.
     */
    fun updateTheme() {
        if (isNight) {
            Aesthetic.get()
                    .isDark(true)
                    .lightStatusBarMode(AutoSwitchMode.OFF)
                    .colorPrimary(ContextCompat.getColor(this, R.color.colorNightPrimary))
                    .colorStatusBar(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) Color.TRANSPARENT else ContextCompat.getColor(this, R.color.colorNightPrimaryDark))
                    .colorNavigationBar(ContextCompat.getColor(this, R.color.colorNightPrimaryDark))
                    .colorAccent(ContextCompat.getColor(this, R.color.colorNightAccent))
                    .colorCardViewBackground(ContextCompat.getColor(this, R.color.colorNightForeground))
                    .colorWindowBackground(ContextCompat.getColor(this, R.color.colorNightPrimaryDark))
                    .textColorPrimary(ContextCompat.getColor(this, R.color.textColorPrimaryNight))
                    .textColorSecondary(ContextCompat.getColor(this, R.color.textColorSecondaryNight))
                    .textColorPrimaryInverse(ContextCompat.getColor(this, R.color.textColorPrimary))
                    .textColorSecondaryInverse(ContextCompat.getColor(this, R.color.textColorSecondary))
                    .apply()
        } else {
            val theme = activityTheme
            if (theme == THEME_DAY || theme == THEME_DAY_NIGHT) {
                Aesthetic.get()
                        .isDark(false)
                        .lightStatusBarMode(AutoSwitchMode.ON)
                        .colorPrimary(ContextCompat.getColor(this, R.color.colorPrimary))
                        .colorStatusBar(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) Color.TRANSPARENT else ContextCompat.getColor(this, R.color.colorPrimaryDark))
                        .colorNavigationBar(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                        .colorAccent(ContextCompat.getColor(this, R.color.colorAccent))
                        .colorCardViewBackground(ContextCompat.getColor(this, R.color.colorForeground))
                        .colorWindowBackground(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                        .textColorPrimary(ContextCompat.getColor(this, R.color.textColorPrimary))
                        .textColorSecondary(ContextCompat.getColor(this, R.color.textColorSecondary))
                        .textColorPrimaryInverse(ContextCompat.getColor(this, R.color.textColorPrimaryNight))
                        .textColorSecondaryInverse(ContextCompat.getColor(this, R.color.textColorSecondaryNight))
                        .apply()
            } else if (theme == THEME_AMOLED) {
                Aesthetic.get()
                        .isDark(true)
                        .lightStatusBarMode(AutoSwitchMode.OFF)
                        .colorPrimary(Color.BLACK)
                        .colorStatusBar(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) Color.TRANSPARENT else Color.BLACK)
                        .colorNavigationBar(Color.BLACK)
                        .colorAccent(Color.WHITE)
                        .colorCardViewBackground(Color.BLACK)
                        .colorWindowBackground(Color.BLACK)
                        .textColorPrimary(Color.WHITE)
                        .textColorSecondary(Color.WHITE)
                        .textColorPrimaryInverse(Color.BLACK)
                        .textColorSecondaryInverse(Color.BLACK)
                        .apply()
            }
        }
    }

    /**
     * @return the current SunriseSunsetCalculator object, or null if it cannot
     * be instantiated.
     * @see [SunriseSunsetLib Repo]
     */
    private fun getSunsetCalculator(): SunriseSunsetCalculator? {
        if (sunsetCalculator == null && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
                val location = locationManager.getLastKnownLocation(locationManager.getBestProvider(Criteria(), false))
                sunsetCalculator = SunriseSunsetCalculator(Location(location.latitude, location.longitude), TimeZone.getDefault().id)
            } catch (ignored: NullPointerException) {
            }

        }

        return sunsetCalculator
    }

    fun addListener(listener: AlarmioListener) {
        listeners!!.add(listener)
    }

    fun removeListener(listener: AlarmioListener) {
        listeners!!.remove(listener)
    }

    fun setListener(listener: ActivityListener?) {
        this.listener = listener

        if (listener != null)
            updateTheme()
    }

    fun requestPermissions(vararg permissions: String) {
        if (listener != null)
            listener!!.requestPermissions(*permissions)
    }

    interface AlarmioListener {
        fun onAlarmsChanged()

        fun onTimersChanged()
    }

    interface ActivityListener {
        fun requestPermissions(vararg permissions: String)

        fun gettFragmentManager(): FragmentManager  //help
    }

    companion object {

        val THEME_DAY_NIGHT = 0
        val THEME_DAY = 1
        val THEME_NIGHT = 2
        val THEME_AMOLED = 3

        val NOTIFICATION_CHANNEL_STOPWATCH = "stopwatch"
        val NOTIFICATION_CHANNEL_TIMERS = "timers"
    }

}
