package me.jfenn.alarmio.common.interfaces

import me.jfenn.alarmio.common.data.interfaces.AlertData
import java.util.*

interface AlertScheduler {

    /**
     * Schedule an alert to trigger at a certain time.
     *
     * @param alert         The alert to schedule.
     * @return The time of the scheduled alert.
     */
    fun schedule(alert: AlertData): Date

    /**
     * Re-schedule an alert if it is repeating.
     *
     * @param alert         The alert to reschedule.
     * @return The time of the scheduled alert.
     */
    fun reschedule(alert: AlertData): Date?

    /**
     * Cancel a scheduled alert.
     *
     * @param alert         The alert to cancel.
     */
    fun cancel(alert: AlertData)

}