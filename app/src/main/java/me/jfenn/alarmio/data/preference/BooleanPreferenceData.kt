package me.jfenn.alarmio.data.preference

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
class BooleanPreferenceData(private val preference: PreferenceData, @StringRes private val title: Int, @StringRes private val description: Int) : BasePreferenceData<BooleanPreferenceData.ViewHolder>() {

    override fun getViewHolder(inflater: LayoutInflater, parent: ViewGroup): BasePreferenceData.ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.item_preference_boolean, parent, false))
    }

    @SuppressLint("CheckResult")
    override fun bindViewHolder(holder: ViewHolder) {
        holder.title.setText(title)
        holder.description.setText(description)
        holder.toggle.setOnCheckedChangeListener(null)

        holder.toggle.isChecked = preference.getValue(holder.itemView.context, false) ?: false
        holder.toggle.setOnCheckedChangeListener { compoundButton, b -> preference.setValue(compoundButton.context, b) }

        Aesthetic.get()
                .colorAccent()
                .take(1)
                .subscribe { colorAccent ->
                    Aesthetic.get()
                            .textColorPrimary()
                            .take(1)
                            .subscribe { textColorPrimary ->
                                CompoundButtonCompat.setButtonTintList(holder.toggle, ColorStateList(
                                        arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked)),
                                        intArrayOf(Color.argb(100, Color.red(textColorPrimary), Color.green(textColorPrimary), Color.blue(textColorPrimary)), colorAccent)
                                ))

                                holder.toggle.setTextColor(textColorPrimary)
                            }
                }
    }

    /**
     * Holds child views of the current item.
     */
    inner class ViewHolder(v: View) : BasePreferenceData.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.title)
        val description: TextView = v.findViewById(R.id.description)
        val toggle: SwitchCompat = v.findViewById(R.id.toggle)
    }

}
