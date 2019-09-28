package me.jfenn.alarmio.ui.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.afollestad.aesthetic.Aesthetic
import com.trello.rxlifecycle3.components.support.RxAppCompatActivity
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import me.jfenn.alarmio.R
import me.jfenn.alarmio.common.data.AlarmData
import me.jfenn.alarmio.common.data.interfaces.AlertData
import me.jfenn.alarmio.common.interfaces.AlertPlayer
import me.jfenn.alarmio.common.interfaces.AlertScheduler
import me.jfenn.alarmio.common.util.formatDayHourString
import me.jfenn.alarmio.data.PreferenceData
import me.jfenn.alarmio.extensions.bind
import me.jfenn.alarmio.ui.dialogs.TimeChooserDialog
import me.jfenn.alarmio.utils.FormatUtils
import me.jfenn.alarmio.utils.ImageUtils
import me.jfenn.alarmio.viewmodel.AlarmioViewModel
import me.jfenn.slideactionview.SlideActionListener
import me.jfenn.slideactionview.SlideActionView
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.*
import java.util.concurrent.TimeUnit

class AlertActivity: RxAppCompatActivity(), SlideActionListener {

    companion object {
        val EXTRA_ALERT = "${AlertActivity::class.java.name}.EXTRA_ALERT"
        val EXTRA_ALERT_ID = "${AlertActivity::class.java.name}.EXTRA_ALERT_ID"
    }

    private val alarmio: AlarmioViewModel by viewModel()
    private val alertPlayer: AlertPlayer by inject()
    private val alertScheduler: AlertScheduler by inject()

    lateinit var alert: AlertData

    private var isDark: Boolean = false

    private val overlay: View? by bind(R.id.overlay)
    private val date: TextView? by bind(R.id.date)
    private val time: TextView? by bind(R.id.time)
    private val actionView: SlideActionView? by bind(R.id.slideView)
    private val background: ImageView? by bind(R.id.background)

    private val isSlowWake by lazy {
        PreferenceData.SLOW_WAKE_UP.getValue<Boolean>(this)
    }

    private val slowWakeMillis by lazy {
        PreferenceData.SLOW_WAKE_UP_TIME.getValue<Long>(this)
    }

    private val audioManager: AudioManager by lazy {
        getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    private var originalVolume = 0
    private var currentVolume = 0
    private val minVolume by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            audioManager.getStreamMinVolume(AudioManager.STREAM_ALARM)
        } else 0
    }

    private val triggerMillis by lazy {
        System.currentTimeMillis()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)

        if (intent.hasExtra(EXTRA_ALERT)) {
            val obj = intent.getSerializableExtra(EXTRA_ALERT)
            (obj as? AlertData)?.let { alert = it }
        } else if (intent.hasExtra(EXTRA_ALERT_ID)) {
            alarmio.getAlert(intent.getStringExtra(EXTRA_ALERT_ID))?.let { alert = it }
        }

        date?.text = Date().formatDayHourString(is24Hour = DateFormat.is24HourFormat(this))

        if (PreferenceData.RINGING_BACKGROUND_IMAGE.getValue(this))
            background?.let { ImageUtils.getBackgroundImage(it) }

        originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
    }

    @SuppressLint("CheckResult")
    fun bindTheme() {
        Aesthetic.get()
                .textColorPrimaryInverse()
                .bindToLifecycle(this)
                .subscribe { integer -> overlay?.setBackgroundColor(integer) }

        actionView?.setLeftIcon(VectorDrawableCompat.create(resources, R.drawable.ic_snooze, theme))
        actionView?.setRightIcon(VectorDrawableCompat.create(resources, R.drawable.ic_close, theme))
        actionView?.setListener(this)
    }

    private fun updateAlert() {
        val elapsedMillis = System.currentTimeMillis() - triggerMillis
        val text = FormatUtils.formatMillis(elapsedMillis)
        time?.text = String.format("-%s", text.substring(0, text.length - 3))

        if (alert is AlarmData && isSlowWake) {
            val slowWakeProgress = elapsedMillis.toFloat() / slowWakeMillis

            val params = window.attributes
            params.screenBrightness = Math.max(0.01f, Math.min(1f, slowWakeProgress))
            window.attributes = params
            window.addFlags(WindowManager.LayoutParams.FLAGS_CHANGED)

            if (currentVolume < originalVolume) {
                val newVolume = minVolume + Math.min(originalVolume.toFloat(), slowWakeProgress * (originalVolume - minVolume)).toInt()
                if (newVolume != currentVolume) {
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, newVolume, 0)
                    currentVolume = newVolume
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        bindTheme()
        alertPlayer.start(
                alert = alert,
                task = ::updateAlert
        )
    }

    override fun onStop() {
        super.onStop()
        alertPlayer.stop()
    }

    fun snooze(snoozeDuration: Long) {
        val timer = alarmio.newTimer() ?: return
        timer.value = timer.value?.apply {
            isVibrate = alert.isVibrate
            sound = alert.sound
            duration = snoozeDuration
        }

        timer.value?.let { alertScheduler.schedule(it) }
    }

    override fun onSlideLeft() {
        val minutes = intArrayOf(2, 5, 10, 20, 30, 60, -1)
        val names = minutes.map {
            if (it > 0)
                FormatUtils.formatUnit(this, it)
            else getString(R.string.title_snooze_custom)
        }.toTypedArray()

        alertPlayer.stop()
        AlertDialog.Builder(this, if (isDark) R.style.Theme_AppCompat_Dialog_Alert else R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setItems(names) { _, which ->
                    if (minutes[which] > 0) {
                        snooze(TimeUnit.MINUTES.toMillis(minutes[which].toLong()))
                        finish()
                    } else {
                        val timerDialog = TimeChooserDialog(this)
                        timerDialog.setListener { hours, minutes1, seconds ->
                            snooze(TimeUnit.HOURS.toMillis(hours.toLong())
                                    + TimeUnit.MINUTES.toMillis(minutes1.toLong())
                                    + TimeUnit.SECONDS.toMillis(seconds.toLong()))

                            finish()
                        }
                        timerDialog.show()
                    }
                }
                .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
                .show()

        overlay?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }

    override fun onSlideRight() {
        overlay?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        finish()
    }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                or WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    override fun onNewIntent(intent: Intent) {
        finish()
        startActivity(Intent(intent))
    }

}