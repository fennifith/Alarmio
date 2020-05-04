package me.jfenn.alarmio.data.preference

import android.annotation.SuppressLint
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.widget.SwitchCompat
import androidx.core.widget.CompoundButtonCompat
import com.afollestad.aesthetic.Aesthetic
import me.jfenn.alarmio.R
import me.jfenn.alarmio.data.PreferenceData

/**
 * Allow the user to choose from a simple boolean
 * using a switch item view.
 */
class SliderPreferenceData(private val preference: PreferenceData, private val maximum: Int, @StringRes private val title: Int, @StringRes private val description: Int) : BasePreferenceData<SliderPreferenceData.ViewHolder>() {

    override fun getViewHolder(inflater: LayoutInflater, parent: ViewGroup): BasePreferenceData.ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.item_preference_slider, parent, false))
    }

    @SuppressLint("CheckResult")
    override fun bindViewHolder(holder: ViewHolder) {
        holder.title.setText(title)
        holder.description.setText(description)
        holder.sliderChoice.max = maximum
        holder.sliderChoice.progress = preference.getValue(holder.context)
        holder.sliderChoiceView.text = preference.getValue<Int>(holder.context).toString()

        holder.sliderChoice.onProgressChanged { _, progress, _ ->
            preference.setValue(holder.context, progress)
            holder.sliderChoiceView.text = progress.toString()
        }
    }

    /**
     * Holds child views of the current item.
     */
    inner class ViewHolder(v: View) : BasePreferenceData.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.title)
        val description: TextView = v.findViewById(R.id.description)
        val sliderChoice: SeekBar = v.findViewById(R.id.sliderChoice)
        val sliderChoiceView: TextView = v.findViewById(R.id.sliderChoiceView)
    }

    private fun SeekBar.onProgressChanged(onProgressChanged: (seekBar: SeekBar?, progress: Int, fromUser: Boolean) -> Unit) {
        this.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                onProgressChanged.invoke(seekBar, progress, fromUser)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }

}
