package me.jfenn.alarmio.impl

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import me.jfenn.alarmio.common.interfaces.VibrationPlayer

class VibrationPlayerImpl(
        context: Context
) : VibrationPlayer {

    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    /**
     * Vibrate the device for a duration of time.
     *
     * @param millis        The amount of milliseconds
     *                      to vibrate for.
     */
    override fun vibrate(millis: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        else vibrator.vibrate(500)
    }

}