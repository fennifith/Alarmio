package me.jfenn.alarmio.data.preference

import me.jfenn.alarmio.data.PreferenceData
import me.jfenn.alarmio.dialogs.TimeChooserDialog
import me.jfenn.alarmio.utils.FormatUtils
import java.util.concurrent.TimeUnit

/**
 * A preference item that holds / displays a time value.
 */
class TimePreferenceData(private val preference: PreferenceData, name: Int) : CustomPreferenceData(name) {

    override fun getValueName(holder: CustomPreferenceData.ViewHolder): String {
        return FormatUtils.formatMillis(preference.getValue<Long>(holder.context)).run {
            substring(0, length - 3)
        }
    }

    override fun onClick(holder: CustomPreferenceData.ViewHolder) {
        val dialog = run {
            var seconds = TimeUnit.MILLISECONDS.toSeconds(preference.getValue<Long>(holder.context)).toInt()
            var minutes = TimeUnit.SECONDS.toMinutes(seconds.toLong()).toInt()
            val hours = TimeUnit.MINUTES.toHours(minutes.toLong()).toInt()
            minutes %= TimeUnit.HOURS.toMinutes(1).toInt()
            seconds %= TimeUnit.MINUTES.toSeconds(1).toInt()

            TimeChooserDialog(holder.context).apply { setDefault(hours, minutes, seconds) }
        }

        dialog.setListener { hours, minutes, seconds ->
            run {
                seconds + TimeUnit.HOURS.toSeconds(hours.toLong()).toInt() + TimeUnit.MINUTES.toSeconds(minutes.toLong()).toInt()
            }.let { totalSeconds ->
                preference.setValue(holder.context, TimeUnit.SECONDS.toMillis(totalSeconds.toLong()))
                bindViewHolder(holder)
            }
        }
        dialog.show()
    }

}
