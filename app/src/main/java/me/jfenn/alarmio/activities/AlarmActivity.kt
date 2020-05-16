package me.jfenn.alarmio.activities

import android.app.AlarmManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.os.*
import android.os.PowerManager.WakeLock
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.afollestad.aesthetic.Aesthetic.Companion.get
import com.afollestad.aesthetic.AestheticActivity
import io.reactivex.disposables.Disposable
import me.jfenn.alarmio.Alarmio
import me.jfenn.alarmio.R
import me.jfenn.alarmio.data.AlarmData
import me.jfenn.alarmio.data.PreferenceData
import me.jfenn.alarmio.data.SoundData
import me.jfenn.alarmio.data.TimerData
import me.jfenn.alarmio.dialogs.TimeChooserDialog
import me.jfenn.alarmio.interfaces.SoundPlayer
import me.jfenn.alarmio.services.SleepReminderService
import me.jfenn.alarmio.utils.FormatUtils
import me.jfenn.alarmio.utils.ImageUtils
import me.jfenn.alarmio.utils.extensions.bind
import me.jfenn.slideactionview.SlideActionListener
import me.jfenn.slideactionview.SlideActionView
import org.koin.android.ext.android.inject
import java.util.*
import java.util.concurrent.TimeUnit

class AlarmActivity : AestheticActivity(), SlideActionListener {

    companion object {
        const val EXTRA_ALARM = "james.alarmio.AlarmActivity.EXTRA_ALARM"
        const val EXTRA_TIMER = "james.alarmio.AlarmActivity.EXTRA_TIMER"
    }

    private val alarmio: Alarmio by lazy { applicationContext as Alarmio }
    private val player: SoundPlayer by inject()

    private val overlay: View? by bind(R.id.overlay)
    private val date: TextView? by bind(R.id.date)
    private val time: TextView? by bind(R.id.time)
    private val actionView: SlideActionView? by bind(R.id.slideView)

    private val vibrator: Vibrator? by lazy {
        getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    private var isAlarm = false
    private var triggerMillis: Long = 0

    private var alarm: AlarmData? = null
    private var timer: TimerData? = null
    private var sound: SoundData? = null
    private var isVibrate = false

    private var isSlowWake = false
    private var slowWakeMillis: Long = 0

    private val handler: Handler = Handler()
    private var runnable: Runnable? = null

    private val isWoken = false
    private val wakeLock: WakeLock? = null

    private var textColorPrimaryInverseSubscription: Disposable? = null
    private var isDarkSubscription: Disposable? = null
    private var isDark = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)

        // Lock orientation
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
        textColorPrimaryInverseSubscription = get().textColorPrimaryInverse().subscribe {integer: Int? ->
            integer?.let { overlay?.setBackgroundColor(it) }
        }

        isDarkSubscription = get().isDark.subscribe { aBoolean: Boolean -> isDark = aBoolean }

        actionView?.setLeftIcon(VectorDrawableCompat.create(resources, R.drawable.ic_snooze, theme))
        actionView?.setRightIcon(VectorDrawableCompat.create(resources, R.drawable.ic_close, theme))
        actionView?.setListener(this)

        isSlowWake = PreferenceData.SLOW_WAKE_UP.getValue(this)
        slowWakeMillis = PreferenceData.SLOW_WAKE_UP_TIME.getValue(this)

        isAlarm = intent.hasExtra(EXTRA_ALARM)
        if (isAlarm) {
            alarm = intent.getParcelableExtra(EXTRA_ALARM)

            isVibrate = alarm!!.isVibrate
            if (alarm!!.hasSound())
                sound = alarm!!.getSound()
        } else if (intent.hasExtra(EXTRA_TIMER)) {
            timer = intent.getParcelableExtra(EXTRA_TIMER)

            isVibrate = timer!!.isVibrate
            if (timer!!.hasSound())
                sound = timer!!.getSound()
        } else finish()

        date?.text = FormatUtils.format(Date(), FormatUtils.FORMAT_DATE + ", " + FormatUtils.getShortFormat(this))

        triggerMillis = System.currentTimeMillis()
        runnable = object : Runnable {
            override fun run() {
                val elapsedMillis = System.currentTimeMillis() - triggerMillis
                val text = FormatUtils.formatMillis(elapsedMillis)
                time?.text = String.format("-%s", text.substring(0, text.length - 3))

                if (isVibrate) {
                    if (Build.VERSION.SDK_INT >= 26)
                        vibrator?.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
                    else vibrator?.vibrate(500)
                }

                sound?.let { sound ->
                    if (!player.isPlaying(sound))
                        player.play(sound)
                }

                if (alarm != null && isSlowWake) {
                    val slowWakeProgress = (elapsedMillis.toFloat() / slowWakeMillis).coerceIn(0.01f, 1f)

                    // set display brightness
                    window.attributes.screenBrightness = slowWakeProgress
                    window.addFlags(WindowManager.LayoutParams.FLAGS_CHANGED)

                    // update ringtone volume
                    player.setVolume(slowWakeProgress)
                }

                handler.postDelayed(this, 1000)
            }
        }

        handler.post(runnable)

        SleepReminderService.refreshSleepTime(alarmio)

        if (PreferenceData.RINGING_BACKGROUND_IMAGE.getValue(this))
            ImageUtils.getBackgroundImage(findViewById(R.id.background))
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                or WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    override fun onDestroy() {
        super.onDestroy()
        textColorPrimaryInverseSubscription?.dispose()
        isDarkSubscription?.dispose()
        stopAnnoyingness()
    }

    private fun stopAnnoyingness() {
        runnable?.let { handler.removeCallbacks(it) }
        player.stop()

        if (isSlowWake) {
            // reset volume to initial value
            player.setVolume(1f)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        finish()
        startActivity(Intent(intent))
    }

    override fun onSlideLeft() {
        overlay?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        stopAnnoyingness()

        val options = intArrayOf(2, 5, 10, 20, 30, 60)
        val optionNames = arrayOfNulls<CharSequence>(options.size + 1)
        for (i in options.indices) {
            optionNames[i] = FormatUtils.formatUnit(this@AlarmActivity, options[i])
        }
        optionNames[options.size] = getString(R.string.title_snooze_custom)

        AlertDialog.Builder(this@AlarmActivity, if (isDark) R.style.Theme_AppCompat_Dialog_Alert else R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setItems(optionNames) { dialog: DialogInterface?, which: Int ->
                    if (which < options.size) {
                        alarmio.newTimer().apply {
                            setDuration(TimeUnit.MINUTES.toMillis(options[which].toLong()), alarmio)
                            setVibrate(this@AlarmActivity, isVibrate)
                            setSound(this@AlarmActivity, sound)
                            set(alarmio, getSystemService(Context.ALARM_SERVICE) as AlarmManager)
                        }

                        alarmio.onTimerStarted()
                        finish()
                    } else {
                        val timerDialog = TimeChooserDialog(this@AlarmActivity)
                        timerDialog.setListener { hours: Int, minutes1: Int, seconds: Int ->
                            alarmio.newTimer().apply {
                                setVibrate(this@AlarmActivity, isVibrate)
                                setSound(this@AlarmActivity, sound)
                                setDuration(TimeUnit.HOURS.toMillis(hours.toLong())
                                        + TimeUnit.MINUTES.toMillis(minutes1.toLong())
                                        + TimeUnit.SECONDS.toMillis(seconds.toLong()),
                                        alarmio)
                                set(alarmio, getSystemService(Context.ALARM_SERVICE) as AlarmManager)
                            }

                            alarmio.onTimerStarted()
                            finish()
                        }
                        timerDialog.show()
                    }
                }
                .setNegativeButton(android.R.string.cancel) { dialog: DialogInterface, which: Int -> dialog.dismiss() }
                .show()
    }

    override fun onSlideRight() {
        overlay?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        finish()
    }
}