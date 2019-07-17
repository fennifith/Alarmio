package me.jfenn.alarmio.common.impl

import me.jfenn.alarmio.common.data.interfaces.AlertData
import me.jfenn.alarmio.common.interfaces.AlertPlayer
import me.jfenn.alarmio.common.interfaces.SoundPlayer
import me.jfenn.alarmio.common.interfaces.VibrationPlayer
import java.util.*

class AlertPlayerImpl(
        private val soundPlayer: SoundPlayer,
        private val vibrationPlayer: VibrationPlayer
): TimerTask(), AlertPlayer {

    private var timer: Timer? = null
    private var task: () -> Unit = {}
    private var alert: AlertData? = null
    private var vibrateDuration: Long = 500

    /**
     * Start playing the alert.
     *
     * @param interval          The time (milliseconds) between
     *                          each alert.
     */
    override fun start(alert: AlertData, task: () -> Unit, interval: Long, vibrateDuration: Long) {
        timer ?: run {
            timer = Timer()
            this.alert = alert
            this.task = task
            this.vibrateDuration = vibrateDuration
            timer?.schedule(this, 0, interval)
        }
    }

    /**
     * Stop playing the alert.
     */
    override fun stop() {
        timer?.cancel()
        timer = null
    }

    override fun run() {
        task()

        alert?.sound?.let {
            if (!soundPlayer.isPlaying(it))
                soundPlayer.play(sound = it)
        }

        if (alert?.isVibrate == true)
            vibrationPlayer.vibrate(millis = vibrateDuration)
    }

}