package me.jfenn.alarmio.common.data.interfaces

interface TimedData {

    var startTime: Long?
    var duration: Long

    /**
     * Decides if the Timer has been set or should be ignored.
     *
     * @return              True if the timer should go off at some time in the future.
     */
    fun isStarted(): Boolean {
        return startTime != null
    }

    /**
     * Get the remaining amount of milliseconds before the timer should go off. Returns
     * a negative number if the timer has not been set yet.
     *
     * @return              The amount of milliseconds before the timer should go off.
     */
    fun getRemainingMillis(): Long {
        return startTime?.let {
            Math.max((it + duration) - System.currentTimeMillis(), 0)
        } ?: -1
    }

}