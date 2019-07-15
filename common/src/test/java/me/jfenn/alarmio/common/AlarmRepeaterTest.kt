package me.jfenn.alarmio.common

import me.jfenn.alarmio.common.data.AlarmData
import org.junit.Test
import java.util.*
import kotlin.collections.HashMap

class AlarmRepeaterTest {

    @Test
    fun alarmData_GetNext_ReturnsNull() {
        assert(AlarmData(
                id = 0,
                time = Calendar.getInstance(),
                isEnabled = false,
                repeat = HashMap(),
                isVibrate = false,
                sound = null
        ).getNext() == null)
    }

}
