package me.jfenn.alarmio.common.interfaces

interface VibrationPlayer {

    /**
     * Vibrate the device for a duration of time.
     *
     * @param millis        The amount of milliseconds
     *                      to vibrate for.
     */
    fun vibrate(millis: Long = 500)

}