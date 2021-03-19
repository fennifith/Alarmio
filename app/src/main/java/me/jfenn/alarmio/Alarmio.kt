package me.jfenn.alarmio

import android.Manifest
import android.app.Application
import android.content.Context
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
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import androidx.room.Room
import com.afollestad.aesthetic.Aesthetic
import com.afollestad.aesthetic.AutoSwitchMode
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator
import com.luckycatlabs.sunrisesunset.dto.Location
import me.jfenn.alarmio.data.AlarmData
import me.jfenn.alarmio.data.PreferenceData
import me.jfenn.alarmio.data.SoundData
import me.jfenn.alarmio.data.TimerData
import me.jfenn.alarmio.impl.AppDatabase
import me.jfenn.alarmio.impl.SoundPlayerImpl
import me.jfenn.alarmio.interfaces.SoundPlayer
import me.jfenn.alarmio.services.SleepReminderService
import me.jfenn.alarmio.services.TimerService
import me.jfenn.alarmio.utils.DebugUtils
import me.jfenn.alarmio.viewmodels.HomeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.util.*

class Alarmio : MultiDexApplication() {

    private val sunsetCalculator: SunriseSunsetCalculator? by lazy {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val location = locationManager.getLastKnownLocation(locationManager.getBestProvider(Criteria(), false))
                SunriseSunsetCalculator(Location(location!!.latitude, location.longitude), TimeZone.getDefault().id)
            } catch (ignored: NullPointerException) {
                null
            } catch (ignored: IllegalArgumentException) {
                null
            }
        } else null
    }

    val alarms: MutableList<AlarmData> = ArrayList()
    val timers: MutableList<TimerData> = ArrayList()

    private val listeners: MutableList<AlarmioListener> = ArrayList()
    private var listener: ActivityListener? = null

    private val appModule = module {
        single<SoundPlayer> { SoundPlayerImpl(androidContext()) }
        single<AppDatabase> {
            Room.databaseBuilder(androidContext(), AppDatabase::class.java, "alarmio")
                    .build()
        }

        viewModel { HomeViewModel(get()) }
    }

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@Alarmio)
            modules(appModule)
        }

        DebugUtils.setup(this)

        val alarmLength = PreferenceData.ALARM_LENGTH.getValue<Int>(this)
        for (id in 0 until alarmLength) {
            alarms.add(AlarmData(id, this))
        }

        val timerLength = PreferenceData.TIMER_LENGTH.getValue<Int>(this)
        for (id in 0 until timerLength) {
            val timer = TimerData(id, this)
            if (timer.isSet) timers.add(timer)
        }

        if (timerLength > 0)
            startService(Intent(this, TimerService::class.java))

        SleepReminderService.refreshSleepTime(this)
    }

    /**
     * Create a new alarm, assigning it an unused preference id.
     *
     * @return          The newly instantiated [AlarmData](./data/AlarmData).
     */
    fun newAlarm(): AlarmData {
        val alarm = AlarmData(alarms.size, Calendar.getInstance())
        alarm.sound = SoundData.fromString(PreferenceData.DEFAULT_ALARM_RINGTONE.getValue(this, ""))
        alarms.add(alarm)
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
        val index = alarms.indexOf(alarm)
        alarms.removeAt(index)
        for (i in index until alarms.size) {
            alarms[i].onIdChanged(i, this)
        }
        onAlarmCountChanged()
        onAlarmsChanged()
    }

    /**
     * Update preferences to show that the alarm count has been changed.
     */
    fun onAlarmCountChanged() {
        PreferenceData.ALARM_LENGTH.setValue(this, alarms.size)
    }

    /**
     * Notify the application of changes to the current alarms.
     */
    fun onAlarmsChanged() {
        for (listener in listeners) {
            listener.onAlarmsChanged()
        }
    }

    /**
     * Create a new timer, assigning it an unused preference id.
     *
     * @return          The newly instantiated [TimerData](./data/TimerData).
     */
    fun newTimer(): TimerData {
        val timer = TimerData(timers.size)
        timers.add(timer)
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
        val index = timers.indexOf(timer)
        timers.removeAt(index)
        for (i in index until timers.size) {
            timers[i].onIdChanged(i, this)
        }
        onTimerCountChanged()
        onTimersChanged()
    }

    /**
     * Update the preferences to show that the timer count has been changed.
     */
    fun onTimerCountChanged() {
        PreferenceData.TIMER_LENGTH.setValue(this, timers.size)
    }

    /**
     * Notify the application of changes to the current timers.
     */
    fun onTimersChanged() {
        for (listener in listeners) {
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
        if (isNight()) {
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
            val theme: Int = PreferenceData.THEME.getValue(this)
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
     * Determine if the theme should be a night theme.
     *
     * @return          True if the current theme is a night theme.
     */
    fun isNight(): Boolean {
        val time = Calendar.getInstance()[Calendar.HOUR_OF_DAY]
        val activityTheme : Int = PreferenceData.THEME.getValue(this)
        return (time < getDayStart() || time > getDayEnd()) && activityTheme == THEME_DAY_NIGHT || activityTheme == THEME_NIGHT
    }

    /**
     * Determine if the sunrise/sunset stuff should occur automatically.
     *
     * @return          True if the day/night stuff is automated.
     */
    fun isDayAuto() : Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && PreferenceData.DAY_AUTO.getValue(this)
    }

    /**
     * @return the hour of the start of the day (24h), as specified by the user
     */
    fun getDayStart() : Int {
        if (isDayAuto())
            getSunrise()?.let { return it }

        return PreferenceData.DAY_START.getValue(this)
    }

    /**
     * @return the hour of the end of the day (24h), as specified by the user
     */
    fun getDayEnd() : Int {
        if (isDayAuto())
            getSunset()?.let { return it }

        return PreferenceData.DAY_END.getValue(this)
    }

    /**
     * @return the hour of the calculated sunrise time, or null.
     */
    fun getSunrise() : Int? {
        return sunsetCalculator?.getOfficialSunriseCalendarForDate(Calendar.getInstance())?.get(Calendar.HOUR_OF_DAY)
    }

    /**
     * @return the hour of the calculated sunset time, or null.
     */
    fun getSunset() : Int? {
        return sunsetCalculator?.getOfficialSunsetCalendarForDate(Calendar.getInstance())?.get(Calendar.HOUR_OF_DAY)
    }

    fun addListener(listener: AlarmioListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: AlarmioListener) {
        listeners.remove(listener)
    }

    fun setListener(listener: ActivityListener?) {
        this.listener = listener
        if (listener != null) updateTheme()
    }

    fun requestPermissions(vararg permissions: String?) {
        listener?.requestPermissions(*permissions)
    }

    val fragmentManager: FragmentManager?
        get() = listener?.gettFragmentManager()

    interface AlarmioListener {
        fun onAlarmsChanged()
        fun onTimersChanged()
    }

    interface ActivityListener {
        fun requestPermissions(vararg permissions: String?)
        fun gettFragmentManager(): FragmentManager? //help
    }

    companion object {
        const val THEME_DAY_NIGHT = 0
        const val THEME_DAY = 1
        const val THEME_NIGHT = 2
        const val THEME_AMOLED = 3
        const val NOTIFICATION_CHANNEL_STOPWATCH = "stopwatch"
        const val NOTIFICATION_CHANNEL_TIMERS = "timers"
    }
}