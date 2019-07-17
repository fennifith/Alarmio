package me.jfenn.alarmio.common.interfaces

import me.jfenn.alarmio.common.data.interfaces.AlertData

interface AlertPlayer {

    /**
     * Start playing the alert.
     *
     * @param interval          The time (milliseconds) between
     *                          each alert.
     */
    fun start(
            alert: AlertData,
            task: () -> Unit = {},
            interval: Long = 1000,
            vibrateDuration: Long = 500
    )

    /**
     * Stop playing the alert.
     */
    fun stop()

}