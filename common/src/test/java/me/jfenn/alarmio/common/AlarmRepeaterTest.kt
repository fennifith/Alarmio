package me.jfenn.alarmio.common

import me.jfenn.alarmio.common.data.AlarmData
import org.junit.Test
import java.util.*
import java.util.concurrent.TimeUnit

class AlarmRepeaterTest {

    @Test
    fun alarmData_GetNext_ReturnsNull() {
        assert(AlarmData(
                id = 0,
                time = Calendar.getInstance(),
                isEnabled = false
        ).getNext() == null) {
            System.out.println("AlarmData.getNext() should return null if the alarm is not enabled.")
        }
    }

    @Test
    fun alarmData_GetNext_ReturnsCurrent() {
        val now = Calendar.getInstance().apply {
            set(2019, 2, 20, 14, 32)
        }

        val scheduled = Calendar.getInstance().apply {
            set(2019, 2, 20, 15, 32)
        }

        val returned = AlarmData(
                id = 0,
                time = scheduled,
                isEnabled = true
        ).getNext(now)

        assert((returned!!.timeInMillis - scheduled.timeInMillis) < TimeUnit.DAYS.toMillis(1)) {
            System.out.println("${returned.time} should equal ${scheduled.time} - from ${now.time}.")
        }
    }

    @Test
    fun alarmData_GetNext_RepeatSkipsWednesday() {
        val now = Calendar.getInstance().apply {
            set(2019, 2, 20, 14, 32) // Wednesday
        }

        val scheduled = Calendar.getInstance().apply {
            set(2019, 2, 19, 15, 32) // Tuesday
        }

        val returned = AlarmData(
                id = 0,
                time = scheduled,
                isEnabled = true,
                repeat = HashMap<Int, Boolean>().apply {
                    put(Calendar.TUESDAY, true)
                    put(Calendar.THURSDAY, true)
                }
        ).getNext(now)

        assert((returned!!.timeInMillis - now.timeInMillis) > TimeUnit.DAYS.toMillis(1)) {
            System.out.println("Alarm set for ${scheduled.time} should skip today (${now.time}) and set for 15:32 on Thursday - ${returned.time}.")
        }
    }

}
